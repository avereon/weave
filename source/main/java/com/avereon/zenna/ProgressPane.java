package com.avereon.zenna;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProgressPane extends VBox {

	private final Label message;

	private final ProgressBar indicator;

	private final Label throughput;

	private final long startTimestamp;

	private boolean showRemaining;

	public ProgressPane() {
		setSpacing( 5 );
		message = new Label();
		message.prefWidthProperty().bind( this.widthProperty() );
		indicator = new ProgressBar(0);
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
		long duration = System.currentTimeMillis() - startTimestamp;
		double rate = progress / duration;
		long remaining = (long)((1 - progress) / rate);
		showRemaining = showRemaining || (duration > 5000);

		Platform.runLater( () -> {
			indicator.setProgress( progress );
			if( showRemaining ) {
				if( progress == 1.0 ) {
					throughput.setText( "Completed: " + formatDuration( duration ) );
				} else {
					throughput.setText( "Remaining: " + formatDuration( remaining ) );
				}
			}
		} );
	}

	private String formatDuration( long duration ) {
		return String.format( "%2.0f seconds", Math.ceil( duration / 1000.0 ) );
	}

}
