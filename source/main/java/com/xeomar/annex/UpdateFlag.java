package com.xeomar.annex;

public interface UpdateFlag {
	// Apparently these constants are not visible outside the module if this file
	// is converted to Kotlin.

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
	 * The launch command indicates that a new process should be launched. This
	 * typically used at the end of an update process to start the updated
	 * application.
	 */
	String LAUNCH = "launch";

	/**
	 * The pause command indicates that the update process wait for a specified
	 * number of milliseconds before continuing to the next step.
	 */
	String PAUSE = "pause";

	/**
	 * The update command indicates that a file or directory should be updated
	 * with a file, directory or the contents of a zip file. Syntax:
	 * {@code
	 * update [source path] [target path]
	 * }
	 * Example:
	 * {@code
	 * update modules/editor editor.pack
	 * }
	 */
	String UPDATE = "update";

}
