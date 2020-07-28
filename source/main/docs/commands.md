# Weave Commands

Commands are be given to ${project.name} using this format. The commands can be
provided through stdin or a file. Either way, the format is the same.

## Command Line
The following command line parameters are supported

    --stream - The commands will be read via stdin
    --file <file> - The commands will be read from the specifed file
    
## Basic Format

    [command] [parameter]...

The following commands are currently supported:
* launch - Launch a command
* pause  - Pause execution for a time
* unpack - Unpack a zip file to a location
* update - Update a file

### Samples
The following updates the program.jar and utility.jar files then launches
the program:

    update program.jar /usr/share/program/program.jar
    update utility.jar /usr/share/program/utility.jar
    launch /usr/bin/java -jar /usr/share/program/program.jar

The following unpacks the update.zip file into /usr/share/program folder:
    
    unpack update.zip /usr/share/program
