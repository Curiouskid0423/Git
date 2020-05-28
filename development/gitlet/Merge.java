package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/** Merge Helper class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Merge {

    /** An empty constructor for merge command. Split out from
     * Repo.java simply for the sake of better organization.
     * MUST be instantiated every time called upon.
     * @param repo is current repository */
    public Merge(Repo repo) {
        _repo = repo;
        _CTree = repo.getCTree();
        _stage = repo.getStage();
    }

    /** Gitlet command merge.
     *  @param mergeB branch to merge from. */
    public void merge(String mergeB) {
        String originMerge = mergeB;
        mergeB = Repo.reconstructBranch(mergeB);
        checkMerge(mergeB); Commit mergeHead = Repo.readBranch(mergeB);
        HashMap<String, String> currMap = _CTree.getHEAD().blobsMap();
        HashMap<String, String> mergeMap = mergeHead.blobsMap();
        if (hasUntracked()) {
            throw new GitletException("There is an untracked file "
                    + "in the way; delete it, or add and commit it first.");
        }
        Commit lca = _repo.findLCA(_CTree.getHEAD(), mergeHead);
        if (lca.getHash().equals(mergeHead.getHash())) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            return;
        } else if (lca.getHash().equals(_CTree.getHEAD().getHash())) {
            System.out.println("Current branch fast-forwarded.");
            String fastHash = mergeHead.getHash();
            _repo.checkOutCommit(fastHash);
            Utils.writeContents(
                    Utils.join(_branchPath, _CTree.getCurrBranch()), fastHash);
            return;
        }
        HashMap<String, String> lcaMap = lca.blobsMap();
        HashMap<String, String> unionMap = new HashMap<>(mergeMap);
        unionMap.putAll(currMap);
        Set<String> unionCM = unionMap.keySet();
        for (String i : unionCM) {
            String currHash = currMap.get(i);
            String mergeHash = mergeMap.get(i);
            if (currHash != null && mergeHash != null) {
                if (lcaMap.containsKey(i)) {
                    processChange(lcaMap.get(i), currHash,
                            mergeHash, i);
                } else if (!currHash.equals(mergeHash)) {
                    processConflict(currHash, mergeHash, i);
                }
            } else if (currHash != null) {
                if (lcaMap.containsKey(i)) {
                    if (currHash.equals(lcaMap.get(i))) {
                        _stage.addToStage("remove", i, currHash);
                        Utils.restrictedDelete(i);
                    } else {
                        processConflict(currHash, mergeHash, i);
                    }
                }
            } else if (mergeHash != null) {
                if (!lcaMap.containsKey(i)) {
                    Utils.writeContents(new File(i),
                            Repo.findBlobByUid(mergeHash).getContent());
                    _stage.addToStage("add", i, mergeHash);
                } else if (!mergeHash.equals(lcaMap.get(i))) {
                    processConflict(currHash, mergeHash, i);
                }
            }
        }
        String commitMsg = String.format("Merged %s into %s.",
                originMerge, _CTree.getCurrBranch());
        _CTree.commit(false, _stage, mergeHead.getHash(), commitMsg);
    }

    /** Helper function for merge to process file change conditions.
     *  Assume that the file is presented in both Branch Head, and LCA.
     * @param ref LCA for reference
     * @param cHash first blob hash
     * @param mHash second blob hash
     * @param fname filename
     */
    private void processChange(String ref, String cHash,
                               String mHash, String fname) {
        boolean currChange = ref.compareTo(cHash) != 0;
        boolean mergeChange = ref.compareTo(mHash) != 0;
        if (!currChange && mergeChange) {
            Utils.writeContents(new File(fname),
                    Repo.findBlobByUid(mHash).getContent());
            _stage.addToStage("add", fname, mHash);
        } else if (currChange && mergeChange
                && !cHash.equals(mHash)) {
            processConflict(cHash, mHash, fname);
        }
    }

    /** Given two blobHash, merge the files.
     * @param cHash first hash
     * @param mHash second hash
     * @param fname file*/
    private void processConflict(String cHash, String mHash,
                                 String fname) {
        System.out.println("Encountered a merge conflict.");
        String currContent, mergeContent;
        currContent = mergeContent = "";
        if (cHash != null) {
            currContent = Repo.findBlobByUid(cHash).getContent();
        }
        if (mHash != null) {
            mergeContent = Repo.findBlobByUid(mHash).getContent();
        }
        String result = "<<<<<<<< HEAD\n" + currContent + "========\n"
                + mergeContent + ">>>>>>>>\n";
        Utils.writeContents(new File(fname), result);
        Blob newBlob = new Blob(new File(fname));
        _stage.addToStage("add", fname, newBlob.getHash());
    }

    /** Helper function for Merge to verify condition.
     * @param merge is merge checking*/
    private void checkMerge(String merge) {
        File verify = Utils.join(_branchPath, merge);
        boolean stageCleared = _stage.getStage("add").size() == 0
                && _stage.getStage("remove").size() == 0;

        if (merge.equals(_CTree.getCurrBranch())) {
            throw new GitletException("Cannot merge a "
                    + "branch with itself.");
        } else if (!verify.exists()) {
            throw new GitletException("A branch with that"
                    + " name does not exist.");
        } else if (!stageCleared) {
            throw new GitletException("You have uncommitted changes.");
        }
    }

    /** Specifically for merge.
     * @return true is there is untrack files*/
    private boolean hasUntracked() {
        List<String> cwdFiles = Utils.plainFilenamesIn(new File("."));
        if (cwdFiles != null) {
            for (String fname : cwdFiles) {
                boolean tracked
                        = _CTree.getHEAD().blobsMap().containsKey(fname);
                if (!tracked) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Private instance variable for repo. */
    private Repo _repo;
    /** Private instance variable for Commit tree. */
    private CommitTree _CTree;
    /** Private instance variable for Stage. */
    private Stage _stage;
    /** File separator. */
    private static final String S = File.separator;
    /** Path File to branches. */
    private static File _branchPath
            = new File("." + S
            + ".gitlet" + S + "refs" + S + "branches");

}
