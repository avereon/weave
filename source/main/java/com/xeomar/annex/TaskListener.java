package com.xeomar.annex;

public interface TaskListener {

	void updateMessage( String message );

	void updateProgress( int step );

}
