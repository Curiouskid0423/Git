package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/** Blob class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Blob implements Serializable {

    /** Constructor method.
     * @param f file
     * */
    public Blob(File f) {
        _content = Utils.readContentsAsString(f);
        _fname = f.getName();
        _blobHash = generateHash();
        writeBlob();
    }


    /** Getter function for _fname.
     * @return filename*/
    public String getName() {
        return _fname;
    }

    /** Getter function for _content.
     * @return content*/
    public String getContent() {
        return _content;
    }

    /** Getter function for _content.
     * @return hash*/
    public String getHash() {
        return _blobHash;
    }

    /** Generate the hash of this blob upon creation.
     *  Never use this other than in the constructor.
     *  @return generated hash*/
    private String generateHash() {
        return Utils.sha1(Utils.serialize(this), "blob");
    }

    /** Write the blob into the disk. If any file has the
     * exact same hash (should also have the exact same object),
     * it would be overwritten. */
    private void writeBlob() {
        try {
            File blobItem = Utils.join(blobPath, getHash());
            blobItem.createNewFile();
            Utils.writeObject(blobItem, this);
        } catch (IOException e) {
            System.out.println("IOException in writeBlob()");
        }
    }

    /** Instance variable blobhash. */
    private String _blobHash;
    /** Instance variable content. */
    private String _content;
    /** Instance variable filename. */
    private String _fname;
    /** File separator. */
    private static final String SLASH = File.separator;
    /** Instance variable blobpath. */
    private static File blobPath = new File("." + SLASH
            + ".gitlet" + SLASH + "objects" + SLASH + "blobs");
}
