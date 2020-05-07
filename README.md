# Gitlet 
#### CS61B Data Structure, Spring 2020, Final project expanded version.
<br>

Implemented my own git, a version control language, with no external backbone code support. Gitlet is written with about 2000 lines of Java. It support plain files tracking, remote collaboration, basically most of the essential git commands and functionality. Demo GIF would be posted soon :)

###### Disclaimer
*Future CS61b peeps, this repo would NOT work for your integration tests. It's modified quite a lot after the class to achieve the result that I want, so do yourself a favor and do not clone it* :)
<br><i>May, 2020</i><br>
<div style="display: flex;">
    <img src="git_logo.png" alt="git logo" width="30" height="30"/></div>
<hr>  

### Commands
###### Local features
`init` &nbsp;`add` &nbsp;`commit` &nbsp;`rm` &nbsp;`log` &nbsp;`global-log` &nbsp;`find` &nbsp;`status` &nbsp;`checkout` &nbsp;`branch` &nbsp;`rm-branch` &nbsp;`reset` &nbsp;`merge` &nbsp;`help`
###### Remote features
`add-remote` &nbsp;`rm-remote` &nbsp;`push` &nbsp;`fetch` &nbsp;`pull` &nbsp;`list-remote`

<hr>

#### Development uses
- If you want to create your own git as well (super fun!), check out the java files in `/development` directory. Makefile and tester files are provided by CS61B course staff.

#### Production uses
- Check out the `/production` directory, which includes a `.launcher.sh` shell script and some other java class files. Setup with the following steps.
    - `source .launcher.sh` : Run the shell script every time the terminal is restarted, or add the path to your `./bashrc` file (or whatever shell rc file).
    - `gitlet init`
    - `gitlet <any command above>`  