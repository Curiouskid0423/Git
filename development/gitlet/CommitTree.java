package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/** CTree class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class CommitTree implements Serializable {

    /** CommitTree instantiation should: make an initial commit,
     *  make master branch and write into it, readContent into HEAD pointer,
     *  and serialize the entire CTree. */
    public CommitTree() {
        Commit root = Commit.initialCommit();
        writeCommit(root);
        writeBranch(_currBranch, root.getHash());
        writeCTree();
    }

    /**
     * Process a commit. Clone the parent commit, modify the metadata when
     * needed (including reassigning the blobSpace).
     * @param isInitial is initial commit or not
     * @param stage both addition and removal stage
     * @param merge merge Parent string
     * @param msg commit message
     * @return a new commit
     */
    public Commit commit(boolean isInitial,
                         Stage stage, String merge, String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            throw new GitletException("Please enter a commit message.");
        } else if (isInitial) {
            throw new GitletException("Initial commit should not "
                    + "be passed into commit(). ");
        }
        checkStage(stage);
        Commit head = copyFrom(getHeadCommit(), msg);
        head.processStage(stage);
        head.setMergeParent(merge);
        writeBranch(_currBranch, head.getHash());
        writeCommit(head);
        writeCTree();
        return head;
    }
    /** Checker for stage.
     * @param stage passed in stage*/
    private void checkStage(Stage stage) {
        boolean stageEmpty = stage.getStage("add").isEmpty()
                        && stage.getStage("remove").isEmpty();
        if (stageEmpty) {
            throw new GitletException("No changes added to the commit.");
        }
    }

    /** Copy from the parent commit, and replace the message with the new
     *  commit msg.
     *  @param head is HEAD
     *  @param msg message
     *  @return copy */
    private Commit copyFrom(Commit head, String msg) {
        return new Commit(head.getHash(), null,
                    null, head.blobsMap(), msg);
    }

    /** Get the Head commit of this CTree from /refs/branches/master.
     * @return a new commit*/
    private Commit getHeadCommit() {
        String headPath = Utils.readContentsAsString(
                new File("." + SLASH + ".gitlet" + SLASH + "HEAD"));
        String activeBranchContent
                = Utils.readContentsAsString(new File(headPath));
        return Utils.uidToCommit(activeBranchContent);
    }
    /** Getter for current branch.
     * @return the current branch*/
    public String getCurrBranch() {
        return _currBranch;
    }

    /** Set current branch.
     * @param branch this branch*/
    public void setCurrBranch(String branch) {
        _currBranch = branch;
        writeCTree();
    }

    /** Write commit to ./objects/commits/<new_fileName>, regardless
     * of its branch (_branches takes care of this).
     * @param curr current commit */
    private void writeCommit(Commit curr) {
        try {
            File hashFile = Utils.join(_commitPath, curr.getHash());
            hashFile.createNewFile();
            Utils.writeObject(hashFile, curr);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }
    /** Writing into a branch with an assumption that the branch FILE
     *  is created already.
     * @param branch branch to write in
     * @param hashCommit commit to be written
     *  */
    private void writeBranch(String branch, String hashCommit) {
        File branchPath = Utils.join(_branchPath, branch);
        Utils.writeContents(branchPath, hashCommit);
    }

    /** Write in Commit Tree. */
    private void writeCTree() {
        cTreePath = Utils.join(objectPath, "CTree");
        try {
            cTreePath.createNewFile();
        } catch (IOException e) {
            System.out.println("Error in write CTree\n" + e);
        }
        Utils.writeObject(cTreePath, this);
    }

    /** Check if is in HEAD blob.
     * @param fname filename
     * @return is returnable*/
    boolean isInHEADBlob(String fname) {
        return getHeadCommit().blobsMap().containsKey(fname);
    }
    /** Getter for head commit. Might be inefficient since every
     *  request require a I/O operation.
     *  @return the HEAD
     *  */
    Commit getHEAD() {
        return getHeadCommit();
    }

    /** Make a new file with the branch name, and write in the HEAD hash.
     * @param branch is a new branch*/
    public void createBranch(String branch) {
        String headHash = getHeadCommit().getHash();
        File path = Utils.join(_branchPath, branch);
        Utils.writeContents(path, headHash);
        writeCTree();
    }

    /** Helper function for merge to get a chain list of parent
     *  starting from a commit. Returns a HashSet of parents' id.
     *  @param curr curr commit
     *  @return a new set of parents*/
    Set<String> getParentChain(Commit curr) {
        Set<String> idSet = new HashSet<>();
        idSet.add(curr.getHash());
        List<String> prober = new ArrayList<>();
        prober.add(curr.getHash());

        while (!prober.isEmpty()) {
            Commit step = Utils.uidToCommit(prober.get(0));
            if (step.getParent() != null) {
                prober.add(step.getParent());
                idSet.add(step.getParent());
            }
            if (step.getMergeParent() != null) {
                prober.add(step.getMergeParent());
                idSet.add(step.getMergeParent());
            }
            prober.remove(0);
        }
        return idSet;
    }

    /** Gitlet command for pushing.
     * @param branch branch name
     * @param remote given remote to push to */
    public void push(String remote, String branch) {
        checkPushCondition(remote, branch);
        File requested = Utils.join(_remotePath, remote);
        Remote stored = Utils.readObject(requested, Remote.class);

        copyFutureCommits(stored, branch);
        writeForeignBranch(stored, branch);
        System.out.println("Push request to "
                + remote + SLASH + branch + " successful.");
    }

    /** Gitlet command for fetching.
     * @param branch branch name
     * @param remote given remote to fetch from */
    public void fetch(String remote, String branch) {
        checkFetchCondition(remote, branch);
        File requested = Utils.join(_remotePath, remote);
        Remote stored = Utils.readObject(requested, Remote.class);
        File abBranchPath = Utils.join(stored.location(),
                    "refs" + SLASH + "branches");
        File abCommitPath = Utils.join(stored.location(),
                    "objects" + SLASH + "commits");
        File abBlobsPath = Utils.join(stored.location(),
                    "objects" + SLASH + "blobs");
        String remoteHeadHash
                = Utils.readContentsAsString(Utils.join(abBranchPath, branch));

        recursiveWriteCommits(remoteHeadHash, abCommitPath,
                new ArrayList<>());
        List<String> fetchBlobs = Utils.plainFilenamesIn(abBlobsPath);
        assert fetchBlobs != null;
        for (String b : fetchBlobs) {
            byte[] remoteBlobsContents
                    = Utils.readContents((Utils.join(abBlobsPath, b)));
            Utils.writeContents(Utils.join(_blobsPath, b),
                    remoteBlobsContents);
        }
        overWriteBranch(remote + "_" + branch, remoteHeadHash);
    }

    /** For push command, write remote's assigned branch with local's
     *  current head Hash.
     *  @param stored a remote object converted
     *  @param branch is a branch name */
    private void writeForeignBranch(Remote stored, String branch) {
        File toWrite = Utils.join(stored.location(),
                    "refs" + SLASH + "branches" + SLASH + branch);
        Utils.writeContents(toWrite, getHeadCommit().getHash());
    }

    /** Commit all commits in the future of remote's given branch head.
     *  @param stored is a remote object converted back
     *  @param branch is branch name given */
    private void copyFutureCommits(Remote stored, String branch) {
        File remHeadPath = Utils.join(stored.location(),
                "refs" + SLASH + "branches" + SLASH + branch);
        String remHeadHash = Utils.readContentsAsString(remHeadPath);
        String headHash = getHeadCommit().getHash();
        List<String> prober = new ArrayList<>();
        prober.add(headHash);

        foreignCommit(stored, headHash, Utils.uidToCommit(headHash));
        while (!prober.isEmpty()) {
            Commit step = Utils.uidToCommit(prober.remove(0));
            String p = step.getParent();
            String mp = step.getMergeParent();
            if (p != null && !p.equals(remHeadHash)) {
                prober.add(step.getParent());
            }
            if (mp != null && !mp.equals(remHeadHash)) {
                prober.add(step.getMergeParent());
            }
            foreignCommit(stored, step.getHash(), step);
        }
    }

    /** A helper for copying commit to foreign remote.
     *  Also deals with blobs copying.
     * @param fname is filename
     * @param commit is assigned commit that would be written in
     * @param stored is a remote object previously added */
    private void foreignCommit(Remote stored, String fname, Commit commit) {
        File absPath = Utils.join(stored.location(),
                "objects" + SLASH + "commits" + SLASH + fname);
        Utils.writeObject(absPath, commit);
        File remoteBlobDir
                = Utils.join(stored.location(), "objects" + SLASH + "blobs");
        foreignCopyBlobs(commit.blobsMap(), remoteBlobDir);
    }

    /** Helper function to move blobs to remote blobs directory, when
     *  `gitlet push` is called.
     *  @param blobsMap current local commit's blob map
     *  @param absDir is the blobs path in remote to write in. */
    private void foreignCopyBlobs(HashMap<String, String> blobsMap,
                                  File absDir) {
        for (String fname : blobsMap.keySet()) {
            File blobSerialized = Utils.join(_blobsPath, blobsMap.get(fname));
            Blob target = Utils.readObject(blobSerialized, Blob.class);
            Utils.writeObject(Utils.join(absDir, blobsMap.get(fname)), target);
        }
    }

    /** A helper for copying commit to overwrite remote's branch.
     * @param bName is branch name
     * @param remoteHead is the head hash of remote branch
     * */
    private void overWriteBranch(String bName, String remoteHead) {
        File branchPath = Utils.join(_branchPath, bName);
        Utils.writeContents(branchPath, remoteHead);
    }

    /** To fetch all commits from given parent and write them
     * into local repo, from remote.
     *  @param cHash is commit hash
     *  @param absPath absolute path to commit that would be written
     *  @param memo memoization for efficient writing */
    private void recursiveWriteCommits(String cHash,
                                       File absPath, List<String> memo) {
        if (cHash == null || memo.contains(cHash)) {
            return;
        }
        Commit curr
                = Utils.readObject(Utils.join(absPath, cHash), Commit.class);
        writeCommit(curr);

        memo.add(cHash);
        recursiveWriteCommits(curr.getParent(), absPath, memo);
        recursiveWriteCommits(curr.getMergeParent(), absPath, memo);
    }

    /** Check fetch condition.
     * @param remote is remote name
     * @param branch is remote's branch name */
    private void checkFetchCondition(String remote, String branch) {
        File requested = Utils.join(_remotePath, remote);
        Remote stored = Utils.readObject(requested, Remote.class);
        String localBranch = "refs" + SLASH + "branches" + SLASH + branch;
        if (!requested.exists() || !stored.location().exists()) {
            throw new GitletException("Remote directory not found.");
        }
        File branchPath = Utils.join(stored.location(), localBranch);
        if (!branchPath.exists()) {
            throw new GitletException(" That remote does"
                    + " not have that branch.");
        }
    }

    /** Helper function to checkout current head's file to remote's cwd,
     *  also move over necessary blobs.
     *  @param stored is a converted remote object*/
    private void foreignCheckOut(Remote stored) {
        File remoteCWD = stored.location();
        List<String> currDest = Utils.plainFilenamesIn(remoteCWD);
        assert currDest != null;
        HashMap<String, String> headBlobs
                = getHeadCommit().blobsMap();
        Set<String> filesToTrack = headBlobs.keySet();

        for (String i : filesToTrack) {
            Utils.writeContents(Utils.join(remoteCWD, i),
                    findBlobByUID(headBlobs.get(i)).getContent());
            currDest.remove(i);
        }
        for (String c : currDest) {
            Utils.join(remoteCWD, c).delete();
        }
    }
    /** Helper function that maps UID to Blob object.
     * @param blobHash is file
     * @return the corresponding blob
     * */
    private Blob findBlobByUID(String blobHash) {
        return Utils.readObject(Utils.join(_blobsPath, blobHash), Blob.class);
    }

    /** Check condition before pushing.
     *  If existing remote does not have the requested branch, do
     *  not error, just create the branch later (not in this function.
     * @param remote a converted remote object
     * @param branch a given branch name
     * @return true if the requested branch exists. */
    private boolean checkPushCondition(String remote, String branch) {
        File requested = Utils.join(_remotePath, remote);
        if (!requested.exists()) {
            throw new GitletException("Remote directory not found.");
        }
        Remote stored = Utils.readObject(requested, Remote.class);
        if (!stored.location().exists()) {
            throw new GitletException("Remote directory not found.");
        }

        String dir = "refs" + SLASH + "branches" + SLASH + branch;
        File actualBranch = Utils.join(stored.location(), dir);
        if (actualBranch.exists()) {
            String remoteHEAD = Utils.readContentsAsString(actualBranch);
            boolean inHistory
                    = getParentChain(getHeadCommit()).contains(remoteHEAD);
            if (!inHistory) {
                throw new GitletException("Please pull down"
                        + " remote changes before pushing.");
            }
            return true;
        }
        return false;
    }

    /** Current branch name. Default to master. */
    private String _currBranch = "master";
    /** File separator. */
    private static final String SLASH = File.separator;
    /** Object path. */
    private File objectPath = new File("." + SLASH
            + ".gitlet" + SLASH + "objects");
    /** Branch path. */
    private File _branchPath = new File("." + SLASH
            + ".gitlet" + SLASH + "refs" + SLASH + "branches");
    /** Commits path. */
    private File _commitPath = new File("." + SLASH
            + ".gitlet" + SLASH + "objects" + SLASH + "commits");
    /** Commit Tree path. */
    private static File cTreePath;
    /** Path File to remote. */
    private File _remotePath = new File("." + SLASH
            + ".gitlet" + SLASH + "refs" + SLASH + "remotes");
    /** Path File to local blobs. */
    private File _blobsPath = new File("." + SLASH
            + ".gitlet" + SLASH + "objects" + SLASH + "blobs");
}
