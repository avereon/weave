package com.avereon.zenna;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProgressPane extends VBox {

	/**
	 * How long to wait before showing the user how long the remainder of the
	 * process will take. A rule of thumb is to wait only as long as the average
	 * user is willing to wait before knowing how much longer they have to wait.
	 * This is usually between zero and five seconds.
	 */
	private static final int SHOW_REMAINING_DELAY = 3000;

	private final Label message;

	private final ProgressBar indicator;

	private final Label throughput;

	private final long startTimestamp;

	/**
	 * This is how long it took to start the elevated updater process. This delay
	 * can be relatively long due to the need for user interaction (usually to
	 * authorize elevated privileges).
	 */
	private long elevatedStartDelay;

	/**
	 * The internal flag to show the remaining time to the user. This usually
	 * starts off false (not showing) until a certain amount of time has passed
	 * ({@link #SHOW_REMAINING_DELAY}), then it is set to true for the remainder
	 * of the process.
	 */
	private boolean showRemaining;

	public ProgressPane() {
		setSpacing( 5 );
		message = new Label();
		message.prefWidthProperty().bind( this.widthProperty() );
		indicator = new ProgressBar( 0 );
		indicator.prefWidthProperty().bind( this.widthProperty() );
		throughput = new Label();
		throughput.prefWidthProperty().bind( this.widthProperty() );

		getChildren().addAll( message, indicator, throughput );

		startTimestamp = System.currentTimeMillis();
	}

	public void setMessage( String text ) {
		Platform.runLater( () -> this.message.setText( text ) );
	}

	public void setProgress( double progress ) {
		long duration = (System.currentTimeMillis() - startTimestamp) - elevatedStartDelay;
		double rate = progress / duration;
		long remaining = (long)((1 - progress) / rate);
		showRemaining = showRemaining || (duration > SHOW_REMAINING_DELAY);
		if( progress == -1.0 ) showRemaining = false;

		Platform.runLater( () -> {
			indicator.setProgress( Math.max( 0.0, progress ) );
			if( showRemaining ) {
				if( progress == 1.0 ) {
					throughput.setText( "Completed: " + formatDuration( duration ) );
				} else {
					throughput.setText( "Remaining: " + formatDuration( remaining ) );
				}
			} else {
				throughput.setText( "" );
			}
		} );
	}

	void setElevatedStartDelay( long elevatedStartDelay ) {
		this.elevatedStartDelay = elevatedStartDelay;
	}

	private String formatDuration( long duration ) {
		return String.format( "%2.0f seconds", Math.ceil( duration / 1000.0 ) );
	}

}
