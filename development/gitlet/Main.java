package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    public static void main(String... args) {
        try {
            checkArgs(args);
            switch (args[0]) {
            case "init":
                validateNumArgs(args, 0); init(); break;
            case "add":
                validateAndFetch(1, args);
                repo.add(args[1]); break;
            case "commit":
                validateAndFetch(1, args);
                CommitTree cTree = repo.getCTree();
                cTree.commit(false, repo.getStage(), null, args[1]);
                break;
            case "rm":
                validateAndFetch(1, args);
                repo.rm(args[1]); break;
            case "log":
                displayCmd("log", args); break;
            case "global-log":
                displayCmd("global-log", args); break;
            case "find":
                validateAndFetch(1, args);
                repo.find(args[1]); break;
            case "status":
                displayCmd("status", args); break;
            case "checkout":
                int check = identifyCheckoutCase(args);
                Repo.getRepo(false).checkout(check, args); break;
            case "branch":
                validateAndFetch(1, args);
                repo.branch(args[1]); break;
            case "rm-branch":
                validateAndFetch(1, args);
                repo.rmBranch(args[1]); break;
            case "reset":
                validateAndFetch(1, args);
                repo.reset(args[1]); break;
            case "merge":
                validateAndFetch(1, args);
                repo.merge(args[1]); break;
            case "list-remote":
                remoteCmd("list-remote", args); break;
            case "add-remote":
                remoteCmd("add-remote", args); break;
            case "rm-remote":
                remoteCmd("rm-remote", args); break;
            case "push":
                remoteCmd("push", args); break;
            case "fetch":
                remoteCmd("fetch", args); break;
            case "pull":
                remoteCmd("pull", args); break;
            case "help": case "?":
                printHelp(); break;
            default:
                throw new GitletException("No command with that name exists.");
            }
        } catch (GitletException e) {
            Utils.message(e.getMessage()); System.exit(0);
        }
    }

    /** Gitlet command help. */
    private static void printHelp() {
        isGitletRepo();
        File helpPath =  new File(initDIR + SLASH + "help.txt");
        if (!helpPath.exists()) {
            System.out.println("Help text does not exist.");
        }
        String helpTxt = Utils.readContentsAsString(helpPath);
        System.out.println(helpTxt);
    }
    /** Display Info Helper function.
     * @param cmd command
     * @param args arguments */
    private static void displayCmd(String cmd, String... args) {
        if (cmd.equals("status")) {
            validateAndFetch(0, args);
            repo.status();
        } else if (cmd.equals("log")) {
            validateAndFetch(0, args);
            repo.log(false);
        } else if (cmd.equals("global-log")) {
            validateAndFetch(0, args);
            repo.log(true);
        }
    }
    /** Helper function to process remote Commands.
     * @param cmd command
     * @param args arguments */
    private static void remoteCmd(String cmd, String... args) {
        if (cmd.equals("pull")) {
            validateAndFetch(2, args);
            repo.pull(args[1], args[2]);
        } else if (cmd.equals("push")) {
            validateAndFetch(2, args);
            repo.getCTree().push(args[1], args[2]);
        } else if (cmd.equals("add-remote")) {
            validateAndFetch(2, args);
            repo.addRemote(args[1], args[2]);
        } else if (cmd.equals("rm-remote")) {
            validateAndFetch(1, args);
            repo.rmRemote(args[1]);
        } else if (cmd.equals("fetch")) {
            validateAndFetch(2, args);
            repo.getCTree().fetch(args[1], args[2]);
        } else if (cmd.equals("list-remote")) {
            validateNumArgs(args, 0);
            listRemote();
        }
    }

    /** List remote helper.*/
    private static void listRemote() {
        String remote
                = ".gitlet" + SLASH + "refs" + SLASH + "remotes";
        List<String> remList = Utils.plainFilenamesIn(remote);
        System.out.println("\n=== Remote List ===");
        for (String i : remList) {
            Remote rObject
                    = Utils.readObject(Utils.join(remote, i), Remote.class);
            System.out.println(">>> " + i + "\t" + rObject.location());
        }
        System.out.println("=== List end ===\n");
    }

    /** Method to check if arguments are presented, and if
     *  the user attempts to modify the launcher script.
     * @param args are the arguments passed in. */
    private static void checkArgs(String... args) {
        if (args.length == 0) {
            throw new GitletException("Please enter a command.");
        }
    }

    /** Specifically for checkout.
     *  Case 1: -- [file name]
     *  Case 2: [commit id] -- [file name]
     *  Case 3: [branch name]
     * @param args pass in all args from main
     * @return the number fo case of checkout
     * */
    public static int identifyCheckoutCase(String... args) {
        isGitletRepo();
        if (args.length == 3 && args[1].equals("--")) {
            return 1;
        } else if (args.length == 4
                && args[2].equals("--")) {
            return 2;
        } else if (args.length == 2) {
            return 3;
        } else {
            throw new GitletException("Incorrect operands.");
        }
    }
    /** Gitlet init folder. */
    public static void init() {
        if (Utils.join(_cwd, ".gitlet").exists()) {
            throw new GitletException("A Gitlet version-control"
                    + " system already exists in the current directory.");
        }
        setPersistence();
        repo = Repo.getRepo(true);
    }

    /** Make the directories. Set up some static variable for future use.
     *  Write content in "master" branch. */
    private static void setPersistence() {
        initDIR.mkdir();
        try {
            _head = Utils.join(initDIR, "HEAD");
            _stage = Utils.join(initDIR, "stage");
            _refs = Utils.join(initDIR, "refs");
            _objects = Utils.join(initDIR, "objects");
            _head.createNewFile();
            _stage.createNewFile();
            _refs.mkdir();
            Utils.writeContents(Utils.join(initDIR, "help.txt"),
                    Help.helpTxt());
            setupObjects(_objects);
            setUpRefs(_refs);
            Utils.writeContents(_head, _master.toString());
        } catch (IOException e) {
            System.out.println("Writing error in serPersistence method.");
        }
    }

    /** Set up Object directory in .gitlet/objects.
     * @param obj for setup obj folder */
    private static void setupObjects(File obj) {
        obj.mkdir();
        Utils.join(obj, "commits").mkdir();
        Utils.join(obj, "blobs").mkdir();
    }

    /** Helper function for setting up the /stage directory.
     * @param ref set up ref dir*/
    private static void setUpRefs(File ref) {
        try {
            _branches = Utils.join(ref, "branches");
            _branches.mkdir();
            Utils.join(ref, "remotes").mkdir();
            _master = Utils.join(_branches, "master");
            _master.createNewFile();
        } catch (IOException e) {
            System.out.println("Writing error in setUpRefs method.");
        }
    }

    /** Validate the number of arguments, check gitlet repo, and fetch
     *  repo from disk. This function should never be called before init().
     *  @param num number of args to take
     *  @param args arguments passed in
     *  */
    private static void validateAndFetch(int num, String... args) {
        isGitletRepo();
        validateNumArgs(args, num);
        int len = args.length;
        for (int i = 1; i < len; i++) {
            if (args[i].equals(".launcher.sh")) {
                throw new GitletException("Do not attempt to "
                        + "modify launcher script.");
            }
        }
        repo = Repo.getRepo(false);
    }

    /**
     * Helper function to validate number of arguments.
     * @param args the arguments passsed in
     * @param n number that the cmd should take in*/
    private static void validateNumArgs(String[] args, int n) {
        if (n == NONZERO && args.length == 1) {
            throw new GitletException("Incorrect operands.");
        } else if (args.length != n + 1) {
            throw new GitletException("Incorrect operands.");
        }
    }
    /**
     * Helper function to check if is a Gitlet repo. Throw an error if not.
     * */
    private static void isGitletRepo() {
        if (!Utils.join(_cwd, ".gitlet").exists()) {
            throw new GitletException("Not in an "
                    + "initialized Gitlet directory.");
        }
    }

    /** A variable for a default repo. */
    private static Repo repo = null;
    /** A variable to stand for non-zero value in validateArgs. */
    private static final int NONZERO = 100;
    /** Current working directory. */
    private static File _cwd = new File(".");
    /** Init .gitlet directory. */
    private static File initDIR = new File(".gitlet");
    /** HEAD pointer for commitTree. HEAD points to a branch in
     *  ./refs/branches */
    private static File _head;
    /** A ref directory stores branches and remotes.*/
    private static File _refs;
    /** Stage for addition. */
    private static File _stage;
    /** Should contain `commits` directory and other
     *  serialized obj if necessary. */
    private static File _objects;
    /** Branches directory.*/
    private static File _branches;
    /** Master branch.*/
    private static File _master;
    /** Separator string.*/
    private static final String SLASH = File.separator;
}
