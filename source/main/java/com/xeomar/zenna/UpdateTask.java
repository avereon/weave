package com.xeomar.zenna;

public interface UpdateTask {

	/**
	 * The delete command indicates that a path be deleted.
	 */
	String DELETE = "delete";

	/**
	 * Echo the command parameters to the log.
	 */
	String ECHO = "echo";

	String ELEVATED_ECHO  = "elevated-echo";

	/**
	 * The execute command indicates that a new process should be launched. The
	 * task will wait for the process to complete.
	 */
	String EXECUTE = "execute";

	/**
	 * The launch command indicates that a new process should be launched. This
	 * typically used at the end of an update process to start the updated
	 * application.
	 */
	String LAUNCH = "launch";

	/**
	 * The move command indicates that a source path be moved to a target path.
	 * This is typically used to move files or folders from one location to
	 * another. While this command may be used to rename a file or folder, it is
	 * recommended to use the <code>rename</code> command instead.
	 */
	String MOVE = "move";

	/**
	 * The pause command indicates that the update process wait for a specified
	 * number of milliseconds before continuing to the next step.
	 */
	String PAUSE = "pause";

	String ELEVATED_PAUSE = "elevated-pause";

	String PERMISSIONS = "permissions";

	/**
	 * The rename command indicates that a path be given a different name.
	 */
	String RENAME = "rename";

	/**
	 * The unpack command indicates that a file or directory should be overlaid
	 * with the contents of a zip file. Syntax:
	 * {@code
	 * overlay [source path] [target path]
	 * }
	 * Example:
	 * {@code
	 * overlay modules/editor editor.pack
	 * }
	 */
	String UNPACK = "unpack";

}
