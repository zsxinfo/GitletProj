package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static gitlet.Utils.join;

public class Branching {
    private static final File BRANCHES_DIR = join(Repository.GITLET_DIR, "heads");
    private static final File HEAD = join(Repository.GITLET_DIR, "HEAD");

    public static void initialize(String initCommitID) throws IOException {
        BRANCHES_DIR.mkdir();
        HEAD.createNewFile();
        Utils.writeContents(HEAD, "master");
        File MASTER = join(BRANCHES_DIR, "master");
        MASTER.createNewFile();
        Utils.writeContents(MASTER, initCommitID);
    }

    public static String getCurrentBranch() {
        return Utils.readContentsAsString(HEAD);
    }

    public static String getHeadID() {
        String currentBranch = Utils.readContentsAsString(HEAD);    // read current branch name
        File CUR_BRANCH = Utils.join(BRANCHES_DIR, currentBranch);
        String headID = Utils.readContentsAsString(CUR_BRANCH);
        return headID;
    }

    public static void save(String newCommitID) {
        String currentBranch = Utils.readContentsAsString(HEAD);    // read current branch name
        File CUR_BRANCH = Utils.join(BRANCHES_DIR, currentBranch);
        Utils.writeContents(CUR_BRANCH, newCommitID);
    }

    public static List<String> getBranches() {
        return Utils.plainFilenamesIn(BRANCHES_DIR);
    }

    public static boolean containsBranch(String branch) {
        File BRANCH = join(BRANCHES_DIR, branch);
        if (BRANCH.exists()) return true;
        return false;
    }

    public static void createNewBranch(String branch) throws IOException {
        File NEW_BRANCH = join(BRANCHES_DIR, branch);
        NEW_BRANCH.createNewFile();
        Utils.writeContents(NEW_BRANCH, getHeadID());
    }

    public static void removeBranch(String branch) {
        File DEL_BRANCH = join(BRANCHES_DIR, branch);
        DEL_BRANCH.delete();
    }

    public static String getCommitID(String branch) {
        File BRANCH = join(BRANCHES_DIR, branch);
        return Utils.readContentsAsString(BRANCH);
    }

    public static void changeHeadBranch(String branch) {
        Utils.writeContents(HEAD, branch);
    }
}
