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

}
