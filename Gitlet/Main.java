package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs("init", args, 1);
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateNumArgs("add", args, 2);
                String filename2 = args[1];
                Repository.add(filename2);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                validateNumArgs("commit", args, 2);
                String commitMessage3 = args[1];
                Repository.commit(commitMessage3);
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                String filename4 = args[1];
                Repository.rm(filename4);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                Repository.global_log();
                break;
            case "find":
                validateNumArgs("find", args, 2);
                String commitMessage7 = args[1];
                Repository.find(commitMessage7);
                break;
            case "status":
                validateNumArgs("status", args, 1);
                Repository.status();
                break;
            case "checkout":
                switch(args.length) {
                    case 2:
                        // checkout branchName
                        // checkoutBranch11
                        String branch11 = args[1];
                        Repository.checkoutBranch(branch11);
                        break;
                    case 3:
                        if (!args[1].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        // checkout -- filename
                        // checkoutFileFromHead9
                        String filename9 = args[2];
                        Repository.checkoutFileFromHead(filename9);
                        break;
                    case 4:
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        // checkout commit -- filename
                        // checkoutFileFromGivenCommit10
                        String commit10 = args[1];
                        String filename10 = args[3];
                        Repository.checkoutFileFromGivenCommit(commit10, filename10);
                        break;
                    default:
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                }
                break;
            case "branch":
                validateNumArgs("branch", args, 2);
                String branch12 = args[1];
                Repository.branch(branch12);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                String branch13 = args[1];
                Repository.rm_branch(branch13);
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                String commit14 = args[1];
                Repository.reset(commit14);
                break;
            case "merge":
                validateNumArgs("merge", args, 2);
                String branch15 = args[1];
                Repository.merge(branch15);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
