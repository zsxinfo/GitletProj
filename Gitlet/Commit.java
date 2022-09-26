package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.Set;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable, Dumpable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */


    /** The message of this Commit. */
    private String commitMessage;
    private Date timestamp;
    private String parentID;
    private String parent2ID;
    private HashMap<String, String> blobsMap; // filename --> blob_id

    private HashMap<String, String> getBlobsMap() {
        return blobsMap;
    }

    /* TODO: fill in the rest of this class. */
    public Commit(String m, String pid, String p2id) {
        commitMessage = m;
        if (pid == null) {
            timestamp = new Date(0); // timestamp set to 1970.1.1
        } else {
            timestamp = new Date(); // timestamp set to current time
        }
        parentID = pid;
        parent2ID = p2id;
        if (pid == null) {
            blobsMap = new HashMap<>();   // initial commit with no blobs mapping
        } else {
            blobsMap = Commit.read(pid).getBlobsMap();  // get blobs mapping of parent commit
        }
    }

    public static Commit read(String commit_id) {
        File COMMIT_FILE = join(Repository.COMMIT_DIR, commit_id);
        Commit commit = Utils.readObject(COMMIT_FILE, Commit.class);
        return commit;
    }

    public String save() {  // save commit with a real time stamp.
        this.timestamp = new Date();
        String commit_id = Utils.sha1(serialize(this));
        File COMMIT_FILE = Utils.join(Repository.COMMIT_DIR, commit_id);
        Utils.writeObject(COMMIT_FILE, this);
        return commit_id;
    }

    public static Commit getHeadCommit() {
        String headID = Branching.getHeadID();
        File HEAD_COMMIT = Utils.join(Repository.COMMIT_DIR, headID);
        return Utils.readObject(HEAD_COMMIT, Commit.class);
    }

    public void putBlob(String filename, String blob_id) {
        blobsMap.put(filename, blob_id);
    }

    public boolean rmBlob(String filename, String blobID) {
        return blobsMap.remove(filename, blobID);
    }

    public String rmBlob(String filename) { return blobsMap.remove(filename); }

    public String getBlobID(String filename) {
        return blobsMap.get(filename);
    }

    public boolean containsFilename(String fileName) {
        return blobsMap.containsKey(fileName);
    }

    public Set<String> filenameSet() {
        return blobsMap.keySet();
    }

    public static String printInfo(String commitID) {
        Commit cm = Commit.read(commitID);
        System.out.println("===");
        System.out.println("commit " + commitID);
        if (cm.existMerge()) {
            System.out.println("Merge: " + cm.getParentID().substring(0,7) + " " + cm.getParent2ID().substring(0,7));
        }
        System.out.println("Date: " + cm.getDate());
        System.out.println(cm.getMessage());
        System.out.println();
        return cm.getParentID();
    }

    @Override
    public void dump() {
        // print blob items
        Set<String> filenameSet = blobsMap.keySet();
        for (String filename : filenameSet) {
            System.out.println(filename + " " + getBlobID(filename));
        }
    }

    public String getParentID(){
        return parentID;
    }

    public String getParent2ID() { return parent2ID; }

    public boolean existMerge() {
        if (this.parent2ID != null) return true;
        return false;
    }

    public Date getDate() {
        return timestamp;
    }

    public String getMessage() {
        return commitMessage;
    }

    public static boolean exist(String commitID) {
        File CM = join(Repository.COMMIT_DIR, commitID);
        if (CM.exists()) return true;
        return false;
    }
}
