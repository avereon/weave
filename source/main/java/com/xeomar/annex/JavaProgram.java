package com.xeomar.annex;

import java.io.BufferedInputStream;
import java.io.IOException;

public class JavaProgram {

	public static void main( String[] commands ) {
		new JavaProgram().start();
	}

	private void start() {
		try( BufferedInputStream input = new BufferedInputStream( System.in )){
			byte[] bytes = input.readAllBytes();
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

}
