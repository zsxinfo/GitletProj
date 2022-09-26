package gitlet;

import java.io.*;
import static gitlet.Utils.*;

import java.nio.file.Files;
import java.time.temporal.Temporal;
import java.util.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Zhong
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");

    /* TODO: fill in the rest of this class. */

    private static void validateGITLET_DIR() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        Commit initCommit = new Commit("initial commit", null, null);
        String initCommitID = initCommit.save();
        Stage stg = new Stage();
        stg.write();
        Branching.initialize(initCommitID);
    }

    private static void copyFile(File FROM, File TO) throws IOException {
        Files.copy(FROM.toPath(), TO.toPath());
    }

    private static String getSha1(File FILE) {
        return Utils.sha1(readContents(FILE));
    }

    public static void add(String filename) throws IOException {
        validateGITLET_DIR();
        File FILE_TO_ADD = join(CWD, filename);
        if (!FILE_TO_ADD.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String blob_id = getSha1(FILE_TO_ADD);
        Stage stg = Stage.read();
        stg.add(filename, blob_id);
        Commit head = Commit.getHeadCommit();
        boolean isIdenticalToVerInHEAD = blob_id.equals(head.getBlobID(filename));
        if (isIdenticalToVerInHEAD) {
            stg.cancelAdd(filename);
        } else {
            File BLOB = join(BLOB_DIR, blob_id);
            if (!BLOB.exists()) {
                copyFile(FILE_TO_ADD, BLOB);
            }
        }
        stg.cancelRemove(filename);
        stg.save();
    }

    public static void commit(String message) {
        validateGITLET_DIR();
        Stage stg = Stage.read();
        if (stg.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        String headID = Branching.getHeadID();
        Commit newCommit = new Commit(message, headID, null);
        Set<String> addFilenameSet = stg.addFilenameSet();
        for (String filename : addFilenameSet) {
            String blob_id = stg.getAddFileID(filename);
            newCommit.putBlob(filename, blob_id);
        }
        Set<String> rmFilenameSet = stg.rmFilenameSet();
        for (String filename : rmFilenameSet) {
            newCommit.rmBlob(filename);
        }
        String newCommitID = newCommit.save();
        Branching.save(newCommitID);
        stg.clear();
        stg.save();
    }

    public static void rm(String filename) {
        validateGITLET_DIR();
        Commit head = Commit.getHeadCommit();
        Stage stg = Stage.read();
        boolean needToRemove = head.containsFilename(filename) || stg.containsAddFile(filename);
        if (!needToRemove) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        stg.cancelAdd(filename);
        if (head.containsFilename(filename)) {
            stg.remove(filename);
            File RM_FILE = join(CWD, filename);
            if (RM_FILE.exists()) {
                RM_FILE.delete();
            }
        }
        stg.save();
    }

    public static void log() {
        validateGITLET_DIR();
        String nextCommitID = Branching.getHeadID();
        while (nextCommitID != null) {
            nextCommitID = Commit.printInfo(nextCommitID);
        }
    }

    public static void global_log() {
        validateGITLET_DIR();
        List<String> commitIDs = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String commitID : commitIDs) {
            Commit.printInfo(commitID);
        }
    }

    public static void find(String message) {
        validateGITLET_DIR();
        List<String> commitIDs = Utils.plainFilenamesIn(COMMIT_DIR);
        boolean found = false;
        for (String commitID : commitIDs) {
            Commit cm = Commit.read(commitID);
            if (message.equals(cm.getMessage())) {
                System.out.println(commitID);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    private static void showBranches() {
        List<String> branches = Branching.getBranches();
        Collections.sort(branches);
        String currentBranch = Branching.getCurrentBranch();
        System.out.println("=== Branches ===");
        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
    }

    private static void printFilenameList(List<String> filenameList) {
        Collections.sort(filenameList);
        for (String filename : filenameList) {
            System.out.println(filename);
        }
    }

    private static void printFilenameSet(Set<String> filenameSet) {
        ArrayList<String> filenameList = new ArrayList<>();
        for (String filename : filenameSet) {
            filenameList.add(filename);
        }
        printFilenameList(filenameList);
    }

    public static void status() {
        validateGITLET_DIR();
        showBranches();
        Stage stg = Stage.read();
        System.out.println("\n=== Staged Files ===");
        Set<String> addFilenameSet = stg.addFilenameSet();
        printFilenameSet(addFilenameSet);
        System.out.println("\n=== Removed Files ===");
        Set<String> rmFilenameSet = stg.rmFilenameSet();
        printFilenameSet(rmFilenameSet);
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        Commit head = Commit.getHeadCommit();
        List<String> modifiedNotStaged = new ArrayList<>();
        Set<String> trackedInHEAD = head.filenameSet();
        for (String tracked : trackedInHEAD) {
            File TRACKED = join(CWD, tracked);
            if (TRACKED.exists()) {
                boolean changed = !getSha1(TRACKED).equals(head.getBlobID(tracked));
                boolean added = stg.containsAddFile(tracked);
                if (changed && !added) {
                    modifiedNotStaged.add(tracked + " (modified)");
                }
            } else {
                if (!stg.containsRmFile(tracked))
                    modifiedNotStaged.add(tracked + " (deleted)");
            }
        }
        Set<String> stagedForAdd = stg.addFilenameSet();
        for (String staged : stagedForAdd) {
            File STAGED = join(CWD, staged);
            if (STAGED.exists()) {
                if (!getSha1(STAGED).equals(stg.getAddFileID(staged))) {
                    modifiedNotStaged.add(staged + " (modified)");
                }
            } else {
                modifiedNotStaged.add(staged + " (deleted)");
            }
        }
        printFilenameList(modifiedNotStaged);
        System.out.println("\n=== Untracked Files ===");
        List<String> untrackedList = new ArrayList<>();
        List<String> CWD_List = Utils.plainFilenamesIn(CWD);
        for (String filename : CWD_List) {
            boolean tracked_state = head.containsFilename(filename) || stg.containsAddFile(filename);
            if (!tracked_state) {
                untrackedList.add(filename);
            }
        }
        // files that have been staged for removal, but then re-created without Gitlet’s knowledge
        for (String rmFilename : rmFilenameSet) {
            File REMOVED = join(CWD, rmFilename);
            if (REMOVED.exists()) {
                untrackedList.add(rmFilename);
            }
        }
        printFilenameList(untrackedList);
        System.out.print("\n");
    }

    private static void copyBlobToCWD(String filename, String blobID) throws IOException {
        File BLOB = join(BLOB_DIR, blobID);
        File CWD_FILE = join(CWD, filename);
        if (CWD_FILE.exists()) CWD_FILE.delete();
        copyFile(BLOB, CWD_FILE);
    }

    private static void checkoutFileFromGivenCommit(Commit cm, String filename) throws IOException {
        if (!cm.containsFilename(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        copyBlobToCWD(filename, cm.getBlobID(filename));
    }

    public static void checkoutFileFromHead(String filename) throws IOException { // No.9: checkout -- filename
        validateGITLET_DIR();
        Commit head = Commit.getHeadCommit();
        checkoutFileFromGivenCommit(head, filename);
    }

    public static void checkoutFileFromGivenCommit(String commitID, String filename) throws IOException {   // No.10: checkout commit -- filename
        validateGITLET_DIR();
        if (!Commit.exist(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit cm = Commit.read(commitID);
        checkoutFileFromGivenCommit(cm, filename);
    }

    public static void checkoutBranch(String branch) throws IOException {   // No.11: checkout branchName
        validateGITLET_DIR();
        if (!Branching.containsBranch(branch)) {
            System.out.println("No such branch exist.");
            System.exit(0);
        }
        if (branch.equals(Branching.getCurrentBranch())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String commitID = Branching.getCommitID(branch);
        String headID = Branching.getHeadID();
        if (commitID.equals(headID)) {

        }
        Commit checkoutCommit = Commit.read(commitID);
        Commit head = Commit.getHeadCommit();
        List<String> identicalFilenames = doOverwrittenCheck(checkoutCommit, head);
        checkoutArbitraryCommit(checkoutCommit, identicalFilenames);
        Branching.changeHeadBranch(branch);
    }


    private static void checkoutArbitraryCommit(Commit checkoutCommit, List<String> identicalFilenames) throws IOException {
        Set<String> filenameSet = checkoutCommit.filenameSet();
        List<String> CWD_filenames = plainFilenamesIn(CWD);
        for (String filename : CWD_filenames) {
            if (!identicalFilenames.contains(filename)) {
                File DEL_FILE = join(CWD, filename);
                DEL_FILE.delete();
            }
        }
        for (String filename : filenameSet) {
            if (!identicalFilenames.contains(filename)) {
                copyBlobToCWD(filename, checkoutCommit.getBlobID(filename));
            }
        }
        Stage stg = Stage.read();
        stg.clear();
        stg.save();
    }

    private static List<String> doOverwrittenCheck(Commit checkoutCommit, Commit head) {
        // returns a list of filenames which should stay identical without deletion when running checkout command.
        List<String> re = new ArrayList<>();
        List<String> filenameList = plainFilenamesIn(CWD);
        for (String filename : filenameList) {
            File CWD_FILE = join(CWD, filename);
            if (CWD_FILE.exists()) {
                boolean untracked = !head.containsFilename(filename);
                boolean wouldBeOverwritten = true;
                if (checkoutCommit.containsFilename(filename)) {
                    boolean identical = getSha1(CWD_FILE).equals(checkoutCommit.getBlobID(filename));
                    if (identical) {
                        re.add(filename);
                        wouldBeOverwritten = false;
                    }
                }
                if (untracked && wouldBeOverwritten) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        return re;
    }

    public static void branch(String branchName) throws IOException {
        validateGITLET_DIR();
        if (Branching.containsBranch(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branching.createNewBranch(branchName);
    }

    public static void rm_branch(String branchName) {
        validateGITLET_DIR();
        if (!Branching.containsBranch(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(Branching.getCurrentBranch())) {
            System.out.println("Can not remove current branch.");
            System.exit(0);
        }
        Branching.removeBranch(branchName);
    }


    public static void reset(String commitID) throws IOException {
        validateGITLET_DIR();
        if (!Commit.exist(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit checkoutCommit = Commit.read(commitID);
        Commit head = Commit.getHeadCommit();
        List<String> identicalFilenames = doOverwrittenCheck(checkoutCommit, head);
        checkoutArbitraryCommit(checkoutCommit, identicalFilenames);
        Branching.save(commitID);
    }

    public static void merge(String givenBranchName) throws IOException {
        validateGITLET_DIR();
        Stage stg = Stage.read();
        String currentBranchName = Branching.getCurrentBranch();
        if (!stg.isEmpty()) {
            System.out.println("You hava uncommitted changes.");
            System.exit(0);
        }
        if (!Branching.containsBranch(givenBranchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (givenBranchName.equals(currentBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String otherCommitID = Branching.getCommitID(givenBranchName);
        String headCommitID = Branching.getHeadID();
        Commit otherCommit = Commit.read(otherCommitID);
        Commit headCommit = Commit.read(headCommitID);
        List<String> identicalFilenames = doOverwrittenCheck(otherCommit, headCommit);
        String splitPointID = findSplitPoint(otherCommitID, headCommitID);
        if (splitPointID.equals(otherCommitID)) {
            System.out.println("Given branch is an ancestor of current branch.");
            System.exit(0);
        }
        if (splitPointID.equals(headCommitID)) {
            checkoutBranch(givenBranchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        // if splitPoint is not mergeCommit or headCommit, then do follows:
        Commit splitPoint = Commit.read(splitPointID);
        Set<String> splitPointFilenames = splitPoint.filenameSet();
        Commit newCommit = new Commit("Merged " + givenBranchName + " into " + currentBranchName + " .", headCommitID, otherCommitID);
        for (String filename : splitPointFilenames) {
            boolean existInHead = headCommit.containsFilename(filename);
            boolean existInOther = otherCommit.containsFilename(filename);
            String idInHead = headCommit.getBlobID(filename);
            String idInOther = otherCommit.getBlobID(filename);
            String idInSplitPoint = splitPoint.getBlobID(filename);
            boolean identicalInHead = false;
            boolean identicalInOther = false;
            if (existInHead) identicalInHead = idInHead.equals(idInSplitPoint);
            if (existInOther) identicalInOther = idInOther.equals(idInSplitPoint);
            if (identicalInHead) {
                if (identicalInOther) {
                    // both identical : stay
                } else {
                    if (existInOther) {
                        // identicalInHead, but modified in other --> f1
                        checkoutFileFromGivenCommit(otherCommit, filename);
                        stg.add(filename, idInOther);
                    } else {
                        // identicalInHead, but deleted in other --> f6
                        newCommit.rmBlob(filename);
                        File RM_FILE = join(CWD, filename);
                        RM_FILE.delete();
                    }
                }
            } else {
                if (identicalInOther) {
                    // stay as they are
                } else {
                    if (existInHead && existInOther) {
                        if (!idInHead.equals(idInOther)) {
                            // file changed and different from each other. --> f3b
                            String idInConflict = conflict(filename, idInHead, idInOther);
                            stg.add(filename, idInConflict);
                        }
                        // if (idInHead.equals(idInOther)) {} // stay as they are. --> f3a
                    } else if (!existInHead && !existInOther) {
                        // stay as they are
                    } else {
                        // one is null, one is different. --> f3c, f3d
                        String idInConflict = conflict(filename, idInHead, idInOther);
                        stg.add(filename, idInConflict);
                    }
                }
            }
        }
        Set<String> head_files = headCommit.filenameSet();
        for (String filename : head_files) {
            boolean presentInSplit = splitPoint.containsFilename(filename);
            if (!presentInSplit ) {
                boolean presentInOther = otherCommit.containsFilename(filename);
                String idInHead = headCommit.getBlobID(filename);
                if (!presentInOther) {
                    // not in split nor other --> stay as they are --> f4
                    // newCommit.putBlob(filename, idInHead);
                } else {
                    String idInOther = otherCommit.getBlobID(filename);
                    if (idInHead.equals(idInOther)) {
                        // not in split, but in other and head, and they are the same
                        // stay as they are.
                    } else {
                        // absent in splitPoint, but different in head and other.
                        String idInConflict = conflict(filename, idInHead, idInOther);
                        stg.add(filename, idInConflict);
                    }
                }
            }
        }
        Set<String> other_files = otherCommit.filenameSet();
        for (String filename : other_files) {
            boolean presentInSplit = splitPoint.containsFilename(filename);
            if (!presentInSplit) {
                boolean presentInHead = headCommit.containsFilename(filename);
                if (!presentInHead) {
                    // not in split nor head, but in other --> f5
                    checkoutFileFromGivenCommit(otherCommit, filename);
                    stg.add(filename, otherCommit.getBlobID(filename));
                }
            }
        }
        String newCommitID = newCommit.save();
        Branching.save(newCommitID);
        stg.save();
    }

    private static String conflict(String filename, String idInHead, String idInOther) throws IOException {
        File EMPTY = join(GITLET_DIR, "Empty");
        EMPTY.createNewFile();
        writeContents(EMPTY, "\n");
        File IN_HEAD = EMPTY;
        File IN_OTHER = EMPTY;
        if (idInHead != null) {
            IN_HEAD = join(BLOB_DIR, idInHead);
        }
        if (idInOther != null) {
            IN_OTHER = join(BLOB_DIR, idInOther);
        }
        File CWD_FILE = join(CWD, filename);
        writeContents(CWD_FILE, "<<<<<<< HEAD\n", readContentsAsString(IN_HEAD), "=======\n", readContentsAsString(IN_OTHER), ">>>>>>>\n");
        String idInConflict = getSha1(CWD_FILE);
        File BLOB = join(BLOB_DIR, idInConflict);
        copyFile(CWD_FILE, BLOB);
        EMPTY.delete();
        return idInConflict;
    }

    private static String findSplitPoint(String cm_a_id, String cm_b_id) {
        HashSet<String> cms = new HashSet<>();
        Commit cm;
        while(cm_a_id != null) {
            cms.add(cm_a_id);
            cm = Commit.read(cm_a_id);
            cm_a_id = cm.getParentID();
        }
        while(cm_b_id != null) {
            if (cms.contains(cm_b_id)) {
                return cm_b_id;
            }
            cm = Commit.read(cm_b_id);
            cm_b_id = cm.getParentID();
        }
        return null;
    }
}
