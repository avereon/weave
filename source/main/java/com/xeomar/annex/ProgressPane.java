package com.xeomar.annex;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProgressPane extends VBox {

	private ProgressBar indicator;

	private Label label;

	public ProgressPane() {
		indicator = new ProgressBar();
		indicator.prefWidthProperty().bind( this.widthProperty() );
		label = new Label();
		label.prefWidthProperty().bind( this.widthProperty() );

		getChildren().addAll( indicator, label );
	}

	public void setProgress( double progress ) {
		Platform.runLater( () -> indicator.setProgress( progress ) );
	}

	public void setText( String label ) {
		Platform.runLater( () -> this.label.setText( label ) );
	}

}
