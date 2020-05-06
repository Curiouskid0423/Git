package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** Commit class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Commit implements Serializable {

    /** Constructor for a new Commit.
     * @param parent parent hash
     * @param merge merge parent hash
     * @param date current date
     * @param parentBMap parent blobMap
     * @param msg commit message
     * */
    public Commit(String parent, String merge, Date date,
                  HashMap<String, String> parentBMap, String msg) {
        this._parent = parent;
        this._mergeParent = merge;
        this._timestamp = (date == null)
                ? new Date(System.currentTimeMillis()) : date;
        this._message = msg;
        this._blobsMap = parentBMap;
        this._uid
                = Utils.sha1(Utils.serialize(this), "commit");
    }
    /** Factory of initial commit.
     * @return the initial commit*/
    public static Commit initialCommit() {
        return new Commit(null, null,
                new Date(0), new HashMap<>(),
                "initial commit");
    }

    /** Helper function to get formatted time based on local time zone.
     * @return formatTime. */
    String formatTime() {
        String pattern = "E MMM d HH:mm:ss yyyy Z";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(_timestamp);
    }

    /** A Method to add or remove staged files to blobsMap.
     * @param stage satge
     * */
    void processStage(Stage stage) {
        HashMap<String, String> addition = stage.getStage("add");
        HashMap<String, String> removal = stage.getStage("remove");
        _blobsMap.putAll(addition);
        for (String s : removal.keySet()) {
            _blobsMap.remove(s);
        }
        stage.reset();
    }

    /** Getter method for _blobsMap.
     * @return hashmap is this*/
    HashMap<String, String> blobsMap() {
        return _blobsMap;
    }
    /** Getter method for a new timestamp.
     * @return the timestamp */
    public Date getTimestamp() {
        return _timestamp;
    }

    /** Getter method for _uid.
     * @return getter result*/
    String getHash() {
        return _uid;
    }
    /** Getter method for _parent.
     * @return getter result*/
    String getParent() {
        return _parent;
    }
    /** Getter method _mergeParent.
     * @return getter result*/
    String getMessage() {
        return _message;
    }
    /** Getter method _mergeParent.
     *  @return get merge parent*/
    String getMergeParent() {
        return _mergeParent;
    }
    /** Setter method _mergeParent.
     * @param m is param*/
    void setMergeParent(String m) {
        _mergeParent = m;
    }


    /** UID, but only ever access it with getter. */
    private String _uid;
    /** Parent Commit. */
    private String _parent;
    /** Second Parent Commit for merging. */
    private String _mergeParent;
    /** Commit message. */
    private String _message;
    /** A string for timestamp. */
    private Date _timestamp;
    /** BlobsMap maps the filename(key) to the hash-id (value). */
    private HashMap<String, String> _blobsMap;
}
