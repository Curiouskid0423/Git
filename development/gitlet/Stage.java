package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
/** Stage class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Stage implements Serializable {

    /** Should include addition and removal stage. */
    public Stage() {
        reset();
        writeStage();
    }

    /** Get stage by keyword.
     *  @param s keyword either "add" or "remove".
     * @return a new hashmap for stage
     * */
    public HashMap<String, String> getStage(String s) {
        if (s.equals("add")) {
            return _addStage;
        } else if (s.equals("remove")) {
            return _removeStage;
        } else {
            throw new GitletException("No such stage exist.");
        }
    }

    /** Check if the file is staged either in add.
     * @param fname is file
     * @return what contains
     * */
    public String contain(String fname) {
        if (_addStage.containsKey(fname)) {
            return "add";
        } else if (_removeStage.containsKey(fname)) {
            return "remove";
        }
        return "none";
    }

    /** To add item that was staged for add, from  the add stage.
     * @param id is param
     * @param fname is param
     * @param hash is a hash id */
    public void addToStage(String id, String fname, String hash) {
        if (id.equals("add")) {
            _addStage.put(fname, hash);
        } else if (id.equals("remove")) {
            _removeStage.put(fname, hash);
        } else {
            throw new GitletException("No such stage exist.");
        }
        writeStage();
    }

    /** To remove item that was staged for removal, from  the removal stage.
     * @param  item is param
     * @param stage is param*/
    public void removeStageItem(String stage, String item) {
        if (stage.equals("add")) {
            _addStage.remove(item);
        } else if (stage.equals("remove")) {
            _removeStage.remove(item);
        } else {
            throw new GitletException("No such stage exist.");
        }
        writeStage();
    }

    /** Write stage with an assumption that _stage is assigned
     *  during init() already. Write a "serialized" stage
     *  into addition and removal files.*/
    public void writeStage() {
        Utils.writeObject(_stagePath, this);
    }
    /** Reset / clear the stage after committing. */
    public void reset() {
        _addStage = new HashMap<>();
        _removeStage = new HashMap<>();
        writeStage();
    }


    /** _addStage is a HashMap that maps filename (key) to blob-Hash (value)
     *  in order to record the files to be added. */
    private HashMap<String, String> _addStage;
    /** _removeStage has the same structure as _addStage, but for removal. */
    private HashMap<String, String> _removeStage;
    /** File separator. */
    private static final String SLASH = File.separator;
    /** Stage Directory. Assume that the /stage file is created
     *  already when init. */
    private File _stagePath
            = new File("." + SLASH + ".gitlet" + SLASH + "stage");

}
