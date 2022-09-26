package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

public class Stage implements Serializable, Dumpable{
    private static final File STAGE_AREA = join(Repository.GITLET_DIR, "index");
    protected HashMap<String, String> staged_for_add;
    protected HashSet<String> staged_for_rm;

    public Stage() {
        staged_for_add = new HashMap<>();
        staged_for_rm = new HashSet<>();
    }

    public void write() {
        Utils.writeObject(STAGE_AREA, this);
    }

    public void save() { // the same as write(), just for readable.
        Utils.writeObject(STAGE_AREA, this);
    }

    public static Stage read() {
        return Utils.readObject(STAGE_AREA, Stage.class);
    }

    public void clear() {
        staged_for_add.clear();
        staged_for_rm.clear();
    }

    public void add(String filename, String blob_id) {
        staged_for_add.put(filename, blob_id);
    }

    public void cancelAdd(String filename) {
        staged_for_add.remove(filename);
    }

    public void remove(String filename) {
        staged_for_rm.add(filename);
    }

    public void cancelRemove(String filename) {
        staged_for_rm.remove(filename);
    }

    public boolean isEmpty() {
        return staged_for_add.isEmpty() && staged_for_rm.isEmpty();
    }

    public Set<String> addFilenameSet() {
        return staged_for_add.keySet();
    }

    public String getAddFileID(String filename) {
        return staged_for_add.get(filename);
    }

    public Set<String> rmFilenameSet() {
        return staged_for_rm;
    }

    public boolean containsAddFile(String filename) {
        return staged_for_add.containsKey(filename);
    }

    public boolean containsRmFile(String filename) { return staged_for_rm.contains(filename); }

    @Override
    public void dump() {
        System.out.println("staged_for_add:\n");
        Set<String> addFiles = staged_for_add.keySet();
        for (String addFile : addFiles) {
            System.out.println(addFile + " " + staged_for_add.get(addFile) +"\n");
        }
        for (String rmFile : staged_for_rm) {
            System.out.println(rmFile +"\n");
        }
    }
}
