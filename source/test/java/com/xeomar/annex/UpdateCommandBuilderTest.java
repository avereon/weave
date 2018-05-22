package com.xeomar.annex;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UpdateCommandBuilderTest {

	@Test
	public void testConstructor() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		assertThat( builder.toString(), is( "\n" ) );
	}

	@Test
	public void testConstructorWithOneParameter() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder( UpdateTask.ECHO );
		assertThat( builder.toString(), is( UpdateTask.ECHO + "\n" ) );
	}

	@Test
	public void testConstructorWithTwoParametersWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder( UpdateTask.ECHO, "Hello World!" );
		assertThat( builder.toString(), is( UpdateTask.ECHO + " \"Hello World!\"\n" ) );
	}

	@Test
	public void testAdd() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.ECHO );
		assertThat( builder.toString(), is( UpdateTask.ECHO + "\n" ) );
	}

	@Test
	public void testChainedAdd() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.ECHO ).add( "Hello" );
		assertThat( builder.toString(), is( UpdateTask.ECHO + " Hello\n" ) );
	}

	@Test
	public void testChainedAddWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.ECHO ).add( "Hello World!" );
		assertThat( builder.toString(), is( UpdateTask.ECHO + " \"Hello World!\"\n" ) );
	}

	@Test
	public void testChainedLines() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.ECHO ).add( "Hello 1" ).line();
		builder.add( UpdateTask.ECHO ).add( "Hello 2" ).line();
		builder.add( UpdateTask.ECHO ).add( "Hello 3" ).line();
		assertThat( builder.toString(), is( UpdateTask.ECHO + " \"Hello 1\"\n" + UpdateTask.ECHO + " \"Hello 2\"\n" + UpdateTask.ECHO + " \"Hello 3\"\n\n" ) );
	}

}