package com.avereon.zenna;

public interface TaskListener {

	void updateMessage( String message );

	void updateProgress( int step );

}