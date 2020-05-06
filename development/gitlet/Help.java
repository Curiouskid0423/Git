package gitlet;

/** Help class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Li
 */
public class Help {

    /** Help constructor. This is a static class not meant to be
     *  created. It's only job is to print txt. */
    public Help() { }

    /** Method to return help txt.
     * @return Help txt in terminal
     * */
    public static String helpTxt() {
        return helpText;
    }

    /** Instance variable, help txt components.*/
    private static String _init = "init:\t\tTo initizalize a gitlet repo.";
    /** Instance variable, help txt components.*/
    private static String _add
            = "add:\t\tResembles `git add`. " + "To stage file for commit.";
    /** Instance variable, help txt components.*/
    private static String _commit
            = "commit:\t\tResembles `git commit`. To commit for file tracking.";
    /** Instance variable, help txt components.*/
    private static String _log
            = "log:\t\tResembles `git log`. Print log.";
    /** Instance variable, help txt components.*/
    private static String _globalLog
            = "global-log:\tLog for every commit created (not "
            + "necessarily this branch).";
    /** Instance variable, help txt components.*/
    private static String _find
            = "find:\t\tFind commit based on given message. "
            + "Doesn't exist in real Git.";
    /** Instance variable, help txt components.*/
    private static String _status
            = "status:\t\tResembles `git status`. Print "
            + "status of current CWD.";
    /** Instance variable, help txt components.*/
    private static String _checkout
            = "checkout:\tResembles `git checkout`. Checkout files "
            + "in three following ways.";
    /** Instance variable, help txt components.*/
    private static String _branch
            = "branch:\t\tResembles `git branch`. Make a new branch.";
    /** Instance variable, help txt components.*/
    private static String _rmBranch
            = "rm-branch:\tRemove the given branch.";
    /** Instance variable, help txt components.*/
    private static String _reset
            = "reset:\t\tResembles `git reset --hard [commit hash]`. "
            + "Reset current HEAD to the given commit.";
    /** Instance variable, help txt components.*/
    private static String _merge
            = "merge:\t\tResembles `git merge`. Merge "
            + "files from a branch to current branch.";
    /** Instance variable, help txt components.*/
    private static String _listRemote
            = "list-remote:\tList out all remotes added.";
    /** Instance variable, help txt components.*/
    private static String _addRemote
            = "add-remote:\tResembles `git remote add`. "
            + "Has to be a .gitlet directory.";
    /** Instance variable, help txt components.*/
    private static String _rmRemote
            = "rm-remote:\tResembles `git remote remove`. "
            + "Remove the remote from local repo.";
    /** Instance variable, help txt components.*/
    private static String _push
            = "push:\t\tResembles `git push`. Push files "
            + "to given remote's branch.";
    /** Instance variable, help txt components.*/
    private static String _fetch
            = "fetch:\t\tResembles `git fetch`. Fetch file "
            + "from a given remote's branch.";
    /** Instance variable, help txt components.*/
    private static String _pull
            = "pull:\t\tResembles `git pull`. Pull from a "
            + "given remote's branch.";

    /** Instance variable Help txt. */
    private static String helpText
            = "\n========= GitLet Version Control System =========\n\n"
            + "\tWelcome to Gitlet. Gitlet is a version control system that\n"
            + "\thas most of the basic functionality in Git. We currently \n"
            + "\tdo not support sub-directory tracking (only able to track \n"
            + "\tfiles). To use Gitlet, try the following commands:\n\n"
            + "\t" + _init + "\n"
            + "\t\t\t>>> gitlet init\n"
            + "\t" + _add + "\n"
            + "\t\t\t>>> gitlet add [file]\n"
            + "\t" + _commit + "\n"
            + "\t\t\t>>> gitlet commit [message]\n"
            + "\t" + _log + "\n"
            + "\t\t\t>>> gitlet log\n"
            + "\t" + _globalLog + "\n"
            + "\t\t\t>>> gitlet global-log\n"
            + "\t" + _find + "\n"
            + "\t\t\t>>> gitlet find [commit message]\n"
            + "\t" + _status + "\n"
            + "\t\t\t>>> gitlet status\n"
            + "\t" + _checkout + "\n"
            + "\t\t\t>>> gitlet checkout -- [file name]\n"
            + "\t\t\t>>> gitlet checkout [commit id] -- [file name]\n"
            + "\t\t\t>>> gitlet checkout [branch name]\n"
            + "\t" + _branch + "\n"
            + "\t\t\t>>> gitlet branch [branch]\n"
            + "\t" + _rmBranch + "\n"
            + "\t\t\t>>> gitlet rm-branch [branch]\n"
            + "\t" + _reset + "\n"
            + "\t\t\t>>> gitlet reset [commit]\n"
            + "\t" + _merge + "\n"
            + "\t\t\t>>> gitlet merge [branch]\n\n"
            + "\t" + "===== Remote commands =====" + "\n"
            + "\t" + _listRemote + "\n"
            + "\t\t\t>>> gitlet list-remote\n"
            + "\t" + _addRemote + "\n"
            + "\t\t\t>>> gitlet add-remote [remote name] [directory]/.gitlet\n"
            + "\t" + _rmRemote + "\n"
            + "\t\t\t>>> gitlet rm-remote [remote name]\n"
            + "\t" + _push + "\n"
            + "\t\t\t>>> gitlet push [remote] [branch name]\n"
            + "\t" + _fetch + "\n"
            + "\t\t\t>>> gitlet fetch [remote] [branch name]\n"
            + "\t" + _pull + "\n"
            + "\t\t\t>>> gitlet pull [remote] [branch name]\n"
            + "\nEnjoy :) May 2020, CS61B final project by Kevin Li.\n"
            + "\n========= End of Help text =========\n";
}
