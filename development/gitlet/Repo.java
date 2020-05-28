package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;


/** Repo class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Repo {

    /** NEVER call this constructor directly. Use getRepo() instead.
     *  Constructor to read from serialized data and reconstruct the data.
     *  @param ct commit tree
     *  @param stage a passed in stage*/
    public Repo(CommitTree ct, Stage stage) {
        _CTree = ct;
        _stage = stage;
    }

    /** Gitlet command add().
     * @param fname filename
     * */
    public void add(String fname) {
        File fileLoc = new File(fname);
        if (!fileLoc.exists()) {
            throw new GitletException("File does not exist.");
        }
        Blob fileBlob = new Blob(fileLoc);
        HashMap<String, String> headBlobs = _CTree.getHEAD().blobsMap();
        boolean isIdentical = _CTree.isInHEADBlob(fname)
                        && headBlobs.get(fname).equals(fileBlob.getHash());
        if (isIdentical) {
            safeRemoveStage(fname);
        } else {
            _stage.addToStage("add", fname, fileBlob.getHash());
        }
        _stage.writeStage();
    }

    /** Gitlet command rm().
     * @param fname filename
     * */
    public void rm(String fname) {
        boolean tracked = _CTree.getHEAD().blobsMap().containsKey(fname);
        String staged = _stage.contain(fname);
        if (!staged.equals("add") && !tracked) {
            throw new GitletException("No reason to remove the file.");
        } else if (staged.equals("add")) {
            _stage.removeStageItem("add", fname);
        } else {
            _stage.addToStage("remove", fname, null);
            Utils.restrictedDelete(fname);
        }
        _stage.writeStage();
    }

    /** Gitlet command log.
     * @param isGlobal if is global*/
    public void log(boolean isGlobal) {
        List<String> allCommits = Utils.plainFilenamesIn(_commitPath);
        assert allCommits != null;
        createLog(allCommits, isGlobal);
    }
    /** Gitlet command find.
     * @param msg msg to be found*/
    public void find(String msg) {
        List<String> allCommits = Utils.plainFilenamesIn(_commitPath);
        boolean found = false;
        assert allCommits != null;
        for (String s : allCommits) {
            String commitMsg = Utils.uidToCommit(s).getMessage();
            if (commitMsg.equals(msg)) {
                found = true;
                System.out.println(s);
            }
        }
        if (!found) {
            throw new GitletException("Did not find any commit with that message.");
        }
    }

    /** Gitlet command status.
     *  Deliberately split into five cases to ease debug pain. */
    public void status() {
        Status current = new Status(_CTree, _stage);
        current.staticInfo("branches");
        current.staticInfo("staged");
        current.staticInfo("removed");
        current.staticInfo("modified");
        current.staticInfo("untracked");
    }

    /** Gitlet command checkout.
     * @param args arguments passed in
     * @param checkoutCase case of checkout*/
    public void checkout(int checkoutCase, String... args) {
        if (checkoutCase == 1) {
            HashMap<String, String> headBlobs = _CTree.getHEAD().blobsMap();
            checkFnameExist(_CTree.getHEAD(), args[2]);
            blobToCWD(headBlobs, args[2]);
        } else if (checkoutCase == 2) {
            Commit commit = Utils.uidToCommit(reconstructUID(args[1]));
            String fname = args[3];
            checkFnameExist(commit, fname);
            blobToCWD(commit.blobsMap(), fname);
        } else {
            String name = args[1];
            name = reconstructBranch(name);
            checkBranch(name);
            File branchPath = Utils.join(_branchPath, name);
            checkOutCommit(Utils.readContentsAsString(branchPath));
            Utils.writeContents(
                    Utils.join("." + S + ".gitlet", "HEAD"),
                    branchPath.toString());
            _CTree.setCurrBranch(name);
        }
    }

    /** Gitlet command branch.
     * @param fname filename. */
    public void branch(String fname) {
        if (fname.contains(File.separator)) {
            throw new GitletException("Branch name cannot"
                    + " contain Separator character.");
        }
        if (Utils.join(_branchPath, fname).exists()) {
            throw new GitletException("One branch with that name"
                    + " already exists.");
        }
        _CTree.createBranch(fname);
    }

    /** Gitlet command rm-branch.
     * @param branch branch to rm*/
    public void rmBranch(String branch) {
        branch = reconstructBranch(branch);
        File path = Utils.join(_branchPath, branch);
        checkRmBranch(branch, path);
        path.delete();
    }

    /** Gitlet command reset.
     * @param commitID ID to be reset to*/
    public void reset(String commitID) {
        String uid = reconstructUID(commitID);
        checkOutCommit(uid);
        File branch = Utils.join(_branchPath, _CTree.getCurrBranch());
        Utils.writeContents(branch, uid);
    }

    /** Gitlet command merge.
     *  @param mergeB branch to merge from. */
    public void merge(String mergeB) {
        Merge helper = new Merge(this);
        helper.merge(mergeB);
    }

    /** Add-remote command of Gitlet.
     * @param name name of remote
     * @param remoteLoc file path string to remote. */
    public void addRemote(String name, String remoteLoc) {
        if (Utils.join(_remotePath, name).exists()) {
            throw new GitletException("A gitlet remote "
                    + "with that name already exists!");
        } else if (!getExtension(remoteLoc).equals(".gitlet")) {
            throw new GitletException("Not a gitlet"
                    + " initialized folder.");
        }
        Remote remote = new Remote(name, remoteLoc);
    }

    /** Remote-remote command of Gitlet.
     * @param name branch name that would be removed.*/
    public void rmRemote(String name) {
        File assignedRemote = Utils.join(_remotePath, name);
        if (!assignedRemote.exists()) {
            throw new GitletException("A remote with"
                    + " that name does not exist.");
        }
        assignedRemote.delete();
    }
    /** Remote-remote command of Gitlet.
     * @param remote remote repo to pull from.
     * @param branch assigned branch inremote*/
    public void pull(String remote, String branch) {
        _CTree.fetch(remote, branch);
        merge(remote + S + branch);
    }

    /** Get extension of a file.
     * @param s is original path
     * @return the extension
     * */
    private String getExtension(String s) {
        String[] buffer = s.split(File.separator);
        int len = buffer.length;
        if (len <= 1) {
            throw new GitletException("Invalid remote path.");
        }
        return buffer[len - 1];
    }

    /** A little modification based on the implementation of remote.
     * @param branch is branch to be reconstructed
     * @return a new string of branch name */
    public static String reconstructBranch(String branch) {
        if (branch.contains(File.separator)) {
            return branch.replaceAll(File.separator, "_");
        }
        return branch;
    }

    /** Helper function for merge() to find a latest common ancesetor.
     *  If there are multiple LCAs, return the first arbitrary one found.
     * @param curr is current branch
     * @param merge merge head
     * @return the Commit LCA
     *  */
    public Commit findLCA(Commit curr, Commit merge) {
        Set<String> mergeParents = _CTree.getParentChain(merge);
        LinkedList<String> choices = new LinkedList<>();

        choices.add(curr.getHash());
        while (!choices.isEmpty()) {
            String i = choices.removeFirst();
            Commit state = Utils.uidToCommit(i);
            if (mergeParents.contains(i)) {
                return state;
            }
            if (state.getParent() != null) {
                choices.add(state.getParent());
            }
            if (state.getMergeParent() != null) {
                choices.add(state.getMergeParent());
            }
        }
        throw new GitletException("Cannot find LCA!");
    }

    /** A Helper function to read Branch and return branch head's Commit object.
     * @param name is branch name
     * @return the result of reading a branch */
    public static Commit readBranch(String name) {
        File branchPath = Utils.join(_branchPath, name);
        return Utils.uidToCommit(
                Utils.readContentsAsString(branchPath));
    }

    /** Helper function that checks out everything in a commit, given a FULL
     *  commit ID. It does not write anything out! This helper avoids redundancy
     *  in checkout case 3, reset, and merge.
     *  Notes:   Below the two sout [Affected file] statement will cause
     *  integration test to error.
     *  @param commitID commit id
     *  */
    public void checkOutCommit(String commitID) {
        HashMap<String, String> blobMap
                = Utils.uidToCommit(commitID).blobsMap();
        Set<String> filesToTrack = blobMap.keySet();
        Set<String> originTrack = _CTree.getHEAD().blobsMap().keySet();
        for (String fname : filesToTrack) {
            if (presentButUntracked(fname)) {
                throw new GitletException("There is an untracked file "
                        + "in the way; delete it or add and commit first.");
            }
            blobToCWD(blobMap, fname);
            System.out.println("[Affected file]: " + fname + " (written in CWD)");
            originTrack.remove(fname);
        }
        for (String remain : originTrack) {
            Utils.restrictedDelete(remain);
            System.out.println("[Affected file]: " + remain + " (deleted in CWD)");
        }
        _stage.reset();
    }

    /** Helper function for rmBranch to verify condition.
     * @param branch check rm branch
     * @param verify verify file*/
    private void checkRmBranch(String branch, File verify) {
        if (!verify.exists()) {
            throw new GitletException("A branch with"
                    + " that name does not exist.");
        }
        if (branch.equals(_CTree.getCurrBranch())) {
            throw new GitletException("Cannot remove "
                    + "the current branch.");
        }
    }

    /** Helper function specifically for checkout.
     *  Return true if the file is currently untracked but the
     *  file is present (problematic for checking-out).
     *  @param fname file
     *  @return boolean value for untrack or not */
    private boolean presentButUntracked(String fname) {
        Commit curr = _CTree.getHEAD();
        File fileLoc = Utils.join(new File("."), fname);
        return fileLoc.exists()
                && !curr.blobsMap().containsKey(fname);
    }

    /** Helper function for checkout to verify branch condition.
     * @param branch to be checked*/
    private void checkBranch(String branch) {
        if (branch.equals(_CTree.getCurrBranch())) {
            throw new GitletException("No need to "
                    + "checkout the current branch.");
        }
        File verify = Utils.join(_branchPath, branch);
        if (!verify.exists()) {
            throw new GitletException("No such branch exists.");
        }
    }
    /** Helper function for checkout to overwrite blob to CWD files.
     * @param blob is a blob map
     * @param fname is file
     * */
    private void blobToCWD(HashMap<String, String> blob, String fname) {
        Blob target = findBlobByUid(blob.get(fname));
        Utils.writeContents(Utils.join(".", fname),
                target.getContent());
    }

    /** Reconstruct UID for for commit in case of abbreviation.
     * @param shortuid abbrev id
     * @return constructed uid*/
    private String reconstructUID(String shortuid) {
        int len = shortuid.length();
        List<String> allCommits = Utils.plainFilenamesIn(_commitPath);
        assert allCommits != null;
        for (String i : allCommits) {
            if (i.substring(0, len).equals(shortuid)) {
                return i;
            }
        }
        throw new GitletException("No commit with that id exists.");
    }

    /** Check if the file is tracked by verifying whether the
     *  filename exist in given commit's blobMap.
     *  @param commit a commit
     *  @param fname file name */
    private void checkFnameExist(Commit commit,
                                 String fname) {
        if (!commit.blobsMap().containsKey(fname)) {
            throw new GitletException("File does not exist in that commit.");
        }
    }
    /** Helper function that maps UID to Blob object.
     * @param fname is file
     * @return the corresponding blob
     * */
    public static Blob findBlobByUid(String fname) {
        assert Utils.join(_blobPath, fname).exists();
        return Utils.readObject(Utils.join(_blobPath, fname), Blob.class);
    }

    /** createLog is a helper method to format the log.
     * @param commits a commit
     * @param isGlobal whether global or not
     * */
    private void createLog(List<String> commits, boolean isGlobal) {
        if (isGlobal) {
            for (String i : commits) {
                Commit curr = Utils.readObject(
                                Utils.join(_commitPath, i), Commit.class);
                System.out.println(formatLogItem(curr));
            }
        } else {
            Commit curr = _CTree.getHEAD();
            while (curr.getParent() != null) {
                System.out.println(formatLogItem(curr));
                curr = Utils.uidToCommit(curr.getParent());
            }
            System.out.println(formatLogItem(curr));
        }
    }
    /** Individual Log Item format helper.
     *  @param curr is current commit
     *  @return a new log item */
    String formatLogItem(Commit curr) {
        String result;
        if (curr.getMergeParent() == null) {
            result =  "===\n" + "commit " + curr.getHash() + "\n"
                    + "Date: " + curr.formatTime() + "\n"
                    + curr.getMessage() + "\n";
        } else {
            result = "===\n" + "commit " + curr.getHash() + "\n"
                    + "Merge: " + curr.getParent().substring(0, 7)
                    + " " + curr.getMergeParent().substring(0, 7) + "\n"
                    + "Date: " + curr.formatTime() + "\n"
                    + curr.getMessage() + "\n";
        }
        return result;
    }

    /** Helper function that removes the file from corresponding stage if
     *  the file exist in the stage.
     *  @param fname is a file */
    private void safeRemoveStage(String fname) {
        boolean addContain = _stage.getStage("add").containsKey(fname);
        boolean removeContain = _stage.getStage("remove").containsKey(fname);
        if (addContain) {
            _stage.removeStageItem("add", fname);
        } else if (removeContain) {
            _stage.removeStageItem("remove", fname);
        }
    }

    /** Create a new Repo if nothing exists. readContent
     *  from serialized data if it's not the first call.
     *  @param isInitial boolean check if is initial
     * @return a new repo
     *  */
    public static Repo getRepo(boolean isInitial) {
        if (isInitial) {
            return new Repo(new CommitTree(), new Stage());
        } else {
            File cTreePath = new File("." + S
                    + ".gitlet" + S + "objects" + S + "CTree");
            File stagePath = new File("." + S
                    + ".gitlet" + S + "stage");
            return new Repo(Utils.readObject(cTreePath, CommitTree.class),
                            Utils.readObject(stagePath, Stage.class));
        }
    }

    /** Getter Method for Commit Tree.
     * @return CommitTree*/
    public CommitTree getCTree() {
        return _CTree;
    }
    /** Getter Method for Stage.
     * @return a new stage */
    public Stage getStage() {
        return _stage;
    }
    /** Instance var for Commit Tree. */
    private CommitTree _CTree;
    /** Instance var for Stage. */
    private Stage _stage;
    /** File separator. */
    private static final String S = File.separator;
    /** Path File to branches. */
    private static File _branchPath
            = new File("." + S
            + ".gitlet" + S + "refs" + S + "branches");
    /** Path File to blobs. */
    private static File _blobPath
            = new File("." + S
            + ".gitlet" + S + "objects" + S + "blobs");
    /** Path File to commits. */
    private static File _commitPath
            = new File("." + S
            + ".gitlet" + S + "objects" + S + "commits");
    /** Path File to remote. */
    private static File _remotePath
            = new File("." + S
            + ".gitlet" + S + "refs" + S + "remotes");
}
