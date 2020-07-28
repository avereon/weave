[build-status]: https://github.com/avereon/weave/workflows/Avereon%20Weave%20Continuous/badge.svg "Build status"

# Weave [![][build-status]](https://github.com/avereon/weave/actions)

A Java program to apply updates. This program is commonly used with other 
programs to automate the update process. It has pre-defined tasks that can be 
specified via stdin or file. If any task requires elevated privileges the user 
is prompted for proper credentials. If specified, it has a simple UI that can 
be used to provide user feedback regarding the progress.

## Tasks
There are several pre-defined tasks that can be specified.

| Task | Description |
| --- | --- |
| Move | Move a file or folder |
| Delete | Delete a file or folder |
| Unpack | Unpack a zip (or jar) file |
| Permissions | Change file permissions on a file or folder |
| Execute | Run a program and wait for it to complete |
| Launch | Asynchronously run a program |
| Pause | Show a message and pause for an amount of time |
| Echo | Output a message to the log file |

# Demonstration
To run a demonstration of the program use the following command line parameters:
    
    java -jar weave.jar --title "Weave Test" --file source/test/resources/commands.txt

