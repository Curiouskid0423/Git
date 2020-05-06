package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/** Status class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Status {

    /**Constructor for status object.
     * @param ctree is param
     * @param stage is new stage*/
    public Status(CommitTree ctree, Stage stage) {
        _logMessage = "Status object is initialized but "
                + "never serialized.";
        _CTree = ctree;
        _stage = stage;
    }

    /** A static function that print info in `gitlet status` command.
     *  Assuming that _CTree and _stage instance variables work, and is stored.
     * @param indicator to check what status section to print
     *  */
    public void staticInfo(String indicator) {
        String result = "";
        List<String> cwdFiles = new ArrayList<>();
        switch (indicator) {
        case "branches":
            result = "=== Branches ===\n";
            List<String> branches = Utils.plainFilenamesIn(
                    new File("." + SLASH + ".gitlet"
                            + SLASH + "refs" + SLASH + "branches"));
            assert branches != null;
            for (String s : branches) {
                if (!_CTree.getCurrBranch().equals(s)) {
                    result = result.concat(s + "\n");
                } else {
                    result = result.concat("*" + s + "\n");
                }
            }
            break;
        case "staged":
            result = "=== Staged Files ===\n";
            result = result.concat(formatStage("add", _stage));
            break;
        case "removed":
            result = "=== Removed Files ===\n";
            result = result.concat(formatStage("remove", _stage));
            break;
        case "modified":
            result = "=== Modifications Not Staged For Commit ===\n";
            cwdFiles = Utils.plainFilenamesIn(new File("."));
            TreeSet<String> buffer = traverseAdd(_stage.getStage("add"));
            buffer.addAll(traverseTracked(_CTree.getHEAD().blobsMap()));
            for (String i : buffer) {
                result = result.concat(i);
            }
            break;
        case "untracked":
            result = "=== Untracked Files ===\n";
            cwdFiles = Utils.plainFilenamesIn(new File("."));
            if (cwdFiles != null) {
                for (String fname : cwdFiles) {
                    boolean tracked
                            = _CTree.getHEAD().blobsMap().containsKey(fname);
                    String staged = _stage.contain(fname);
                    if (!tracked && !staged.equals("add")) {
                        result = result.concat(fname + "\n");
                    }
                }
            }
            break;
        default:
            throw new GitletException("Erroneous indicator.");
        }
        System.out.println(result);
    }

    /** Helper function that traverses over `compare` and concat
     *  the files that are not staged for addition.
     *  @param addStage addStage
     *  @return treeset add stage */
    private TreeSet<String> traverseAdd(HashMap<String, String> addStage) {
        Set<String> key = addStage.keySet();
        TreeSet<String> buffer =  new TreeSet<>();
        for (String fname : key) {
            File f = new File(fname);
            if (!f.exists() && !_stage.contain(fname).equals("remove")) {
                buffer.add(fname + " (deleted)\n");
            } else {
                String cwdVer = (!f.exists())
                        ? "" : Utils.readContentsAsString(f);
                Blob staged = findBlobByUid(addStage.get(fname));
                if (!cwdVer.equals(staged.getContent())) {
                    buffer.add(fname + " (modified)\n");
                }
            }
        }
        return buffer;
    }

    /** Traverse tracked.
     *  @param head is head Map
     *  @return a TreeSet trackedfiles */
    private TreeSet<String> traverseTracked(HashMap<String, String> head) {
        Set<String> trackedSet = head.keySet();
        TreeSet<String> buffer = new TreeSet<>();
        for (String fname : trackedSet) {
            File f = new File(fname);
            if (!f.exists() && !_stage.contain(fname).equals("remove")) {
                buffer.add(fname + " (deleted)\n");
            } else {
                String cwdVer = (!f.exists())
                        ? "" : Utils.readContentsAsString(f);
                Blob tracked = findBlobByUid(head.get(fname));
                boolean unstaged = _stage.contain(fname).equals("none");
                if (!cwdVer.equals(tracked.getContent()) && unstaged) {
                    buffer.add(fname + " (modified)\n");
                }
            }
        }
        return buffer;
    }

    /** Helper function that maps UID to Blob object.
     * @param fname is file
     * @return the corresponding blob
     * */
    private Blob findBlobByUid(String fname) {
        assert Utils.join(_blobPath, fname).exists();
        return Utils.readObject(Utils.join(_blobPath, fname), Blob.class);
    }

    /** Helper function to format stage in gitlet status. Helper function
     *  used twice.
     *  @param stage current stage
     *  @param id identifier for what stage to retrieve
     *  @return `stage` section string in a status call */
    private static String formatStage(String id, Stage stage) {
        String temp = "";
        TreeSet<String> st = new TreeSet<>(stage.getStage(id).keySet());
        for (String s : st) {
            temp = temp.concat(s + "\n");
        }
        return temp;
    }

    /** Dummy data generated when a Status object is created. For debug use. */
    private String _logMessage;
    /** Store CommitTree when instantiated, since statusInfo will always
     *  be called five times. */
    private CommitTree _CTree;
    /** Dummy data generated when a Status object is created. For debug use. */
    private Stage _stage;
    /** File separator. */
    private static final String SLASH = File.separator;
    /** Path File to blobs. */
    private File _blobPath = new File("." + SLASH
            + ".gitlet" + SLASH + "objects" + SLASH + "blobs");
}
