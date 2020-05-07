# Gitlet 
#### CS61B Data Structure, Spring 2020, Final project expanded version.
<br>

Implemented my own git, a version control language, with no external backbone code support. Gitlet support plain files tracking, and remote collaboration, with most of the essential git commands and functionality.
<br>Instructions for using the production version, and development reference will be posted soon, along with a demo GIF :)
<br><i>May, 2020</i><br>
<div style="display: flex; padding-top: .5rem; ">
    <img src="git_logo.png" alt="git logo" width="30" height="30"/></div>
<hr>

### Commands
###### Local features
*   `init `
*   `add`
*   `commit`
*   `rm`
*   `log`
*   `global-log`
*   `find`
*   `status`
*   `checkout`
*   `branch`
*   `rm-branch`
*   `reset`
*   `merge`
*   `list-remote`
###### Remote features
*   `add-remote`
*   `rm-remote`
*   `push`
*   `fetch`
*   `pull`

<hr>

#### Development uses
- Check out the java files in `/development` directory. Makefile and tester files are provided by CS61B course staff.

#### Production uses
- Check out the `/production` directory, which includes a `.launcher.sh` shell script and some other java class files. Setup with the following steps.
    - `source .launcher.sh` : Run the shell script every time the terminal is restarted, or add the path to your `./bashrc` file (or whatever shell rc file).
    - `gitlet init`
    - `gitlet <any command above>`  
     

<hr>

###### Disclaimer
*Future 61b peeps, this file would NOT work for your integration test. It's modified quite a lot after the class, so do yourself a favor and do not clone it*. :)