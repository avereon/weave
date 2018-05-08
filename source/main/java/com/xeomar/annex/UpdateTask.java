package com.xeomar.annex;

public interface UpdateTask {

	/**
	 * The overlay command indicates that a file or directory should be overlaid
	 * with a file, directory or the contents of a zip file. Syntax:
	 * {@code
	 * overlay [source path] [target path]
	 * }
	 * Example:
	 * {@code
	 * overlay modules/editor editor.pack
	 * }
	 */
	String OVERLAY = "overlay";

	/**
	 * The delete command indicates that a path be deleted.
	 */
	String DELETE = "delete";

	/**
	 * The launch command indicates that a new process should be launched. This
	 * typically used at the end of an update process to start the updated
	 * application.
	 */
	String LAUNCH = "launch";

	String LOG = "log";

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

	/**
	 * The rename command indicates that a path be given a different name.
	 */
	String RENAME = "rename";
}
