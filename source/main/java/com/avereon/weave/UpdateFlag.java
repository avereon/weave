package com.avereon.weave;

import com.avereon.util.LogFlag;

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
	 * updater.jar --stdin
	 * }
	 */
	String STDIN = "--stdin";

	/**
	 * The title flag specifies a title for the update window while the updates
	 * are running. Example:
	 * {@code
	 * updater.jar --title "Updating program"
	 * }
	 */
	String TITLE = "--title";

	/**
	 * The update flag indicates the program should read commands from the
	 * specified file. Example:
	 * {@code
	 * updater.jar --update "foo/bar.txt"
	 * }
	 */
	String UPDATE = "--update";

}
