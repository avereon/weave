package com.avereon.weave;

public interface UpdateTask {

	String ELEVATED_PREFIX = "elevated-";

	/**
	 * The delete command indicates that a path be deleted.
	 */
	String DELETE = "delete";

	/**
	 * Echo the command parameters to the log.
	 */
	String LOG = "log";

	String ELEVATED_LOG = ELEVATED_PREFIX + LOG;

	/**
	 * The launch command indicates that a new process should be executed. Unlike
	 * the {@link #LAUNCH} command, this command waits for the executed process to
	 * complete before proceeding.
	 */
	String EXECUTE = "execute";

	/**
	 * The fail command indicated that the task should intentionally fail. This
	 * is primarily used for testing.
	 */
	String FAIL = "fail";

	/**
	 * The header command sets the UI header, not the progress message. The header
	 * should be set for every significant group of tasks.
	 */
	String HEADER = "header";

	/**
	 * The launch command indicates that a new process should be launched. This
	 * is typically used near the end of an update process to restart the updated
	 * program. Unlike the {@link #EXECUTE} command, this command does not wait
	 * for the process to complete before proceeding.
	 * <p>
	 * The launch command will also attempt to start the process until the process
	 * is started successfully, or a timout is reached. In this way the launch
	 * command is more robust than the {@link #EXECUTE} command.
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

	String ELEVATED_PAUSE = ELEVATED_PREFIX + PAUSE;

	String PERMISSIONS = "permissions";

	/**
	 * The rename command indicates that a path be given a different name.
	 */
	String RENAME = "rename";

	/**
	 * The start command indicates that a new process should be started. Unlike
	 * the {@link #EXECUTE} command, this command does not wait for the process
	 * to complete before proceeding, it merely waits for the process to start.
	 * <p>
	 * If the process fails to start, this command will retry until it does,
	 * or the timeout is reached. The command will wait for the process to start,
	 * or fail if it does not.
	 */
	String START = "start";

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
