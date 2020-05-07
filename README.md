# Gitlet 
#### CS61B Data Structure, Spring 2020, Final project expanded version.
<br>

Implemented my own git, a version control language, with no external backbone code support. Gitlet support plain files tracking, and remote collaboration, with most of the essential git commands and functionality. Demo GIF would be posted soon :)
<br><i>May, 2020</i><br>
<div style="display: flex; padding-top: .5rem; ">
    <img src="git_logo.png" alt="git logo" width="30" height="30"/></div>
<hr>

### Commands
###### Local features
`init` &nbsp;`add` &nbsp;`commit` &nbsp;`rm` &nbsp;`log` &nbsp;`global-log` &nbsp;`find` &nbsp;`status` &nbsp;`checkout` &nbsp;`branch` &nbsp;`rm-branch` &nbsp;`reset` &nbsp;`merge` &nbsp;`help`
###### Remote features
`add-remote` &nbsp;`rm-remote` &nbsp;`push` &nbsp;`fetch` &nbsp;`pull` &nbsp;`list-remote`

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
*Future 61b peeps, this file would NOT work for your integration tests. It's modified quite a lot after the class, so do yourself a favor and do not clone it* :)