package com.xeomar.annex;

import com.xeomar.util.LogFlag;

public interface UpdateFlag extends LogFlag {

	/**
	 * The file flag indicates the program should read commands from the
	 * specified file. Example:
	 * {@code
	 * updater.jar --file "foo/bar.txt"
	 * }
	 */
	String FILE = "--file";

	/**
	 * The stream flag indicates the program should read commands from STDIN.
	 * Example:
	 * {@code
	 * updater.jar --stream
	 * }
	 */
	String STREAM = "--stream";

	/**
	 * The title flag specifies a title for the update window while the updates
	 * are running. Example:
	 * {@code
	 * updater.jar --title "Updating program"
	 * }
	 */
	String TITLE = "--title";

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

	/**
	 * The move command indicates that a source path be moved to a target path.
	 * This is typically used to move files or folders from one location to
	 * another. While this command may be used to rename a file or folder, it is
	 * recommended to use the <code>rename</code> command instead.
	 */
	String MOVE = "move";

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
	 * The pause command indicates that the update process wait for a specified
	 * number of milliseconds before continuing to the next step.
	 */
	String PAUSE = "pause";

	/**
	 * The rename command indicates that a path be given a different name.
	 */
	String RENAME = "rename";

}
