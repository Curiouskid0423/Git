package gitlet;

import java.io.File;
import java.io.Serializable;

/** Remote class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Remote implements Serializable {

    /** Constructor for remote class.
     * @param name is remote name
     * @param loc is remote's relative location */
    public Remote(String name, String loc) {
        _name = name;
        _loc = processPath(loc);
        _remoteHash = generateHash();
        writeRemote();
    }

    /** Helper function that takes in a String path, and converts
     *  into a File object with File separator.
     * @param loc is location string that should be processed
     *  @return new File path object */
    private File processPath(String loc) {
        String[] buffer = loc.split(File.separator);
        String result = "";
        for (String i : buffer) {
            result = result.concat(i + File.separator);
        }
        return new File(result);
    }

    /** Generate a hash once the remote is created, for future
     * comparison.
     * @return hash */
    private String generateHash() {
        return Utils.sha1(Utils.serialize(this), "remote");
    }
    /** Getter method for hash.
     * @return a new string for hash */
    public String getHash() {
        return _remoteHash;
    }
    /** Getter method for location.
     * @return a File object of location*/
    public File location() {
        return _loc;
    }

    /** Setter method for remote. */
    public void writeRemote() {
        File storeLoc = Utils.join(_remotePath, _name);
        Utils.writeObject(storeLoc, this);
    }

    /** Instance variable name. */
    private String _name;
    /** Instance variable location. */
    private File _loc;
    /** Hash the remote as well, in case future comparison. */
    private String _remoteHash;
    /** File separator. */
    private static final String SLASH = File.separator;
    /** Instance variable remote path File. */
    private File _remotePath
            = new File("." + SLASH + ".gitlet" + SLASH
            + "refs" + SLASH + "remotes");
}
