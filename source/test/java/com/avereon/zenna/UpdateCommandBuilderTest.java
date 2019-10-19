package com.avereon.zenna;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UpdateCommandBuilderTest {

	@Test
	public void testConstructor() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		assertThat( builder.toString(), is( "\n" ) );
	}

	@Test
	public void testConstructorWithOneParameter() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder().add( UpdateTask.ECHO );
		assertThat( builder.toString(), is( UpdateTask.ECHO + "\n" ) );
	}

	@Test
	public void testConstructorWithTwoParametersWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder().add( UpdateTask.ECHO, "Hello World!" );
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
		builder.add( UpdateTask.ECHO, "Hello" );
		assertThat( builder.toString(), is( UpdateTask.ECHO + " Hello\n" ) );
	}

	@Test
	public void testChainedAddWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.ECHO, "Hello World!" );
		assertThat( builder.toString(), is( UpdateTask.ECHO + " \"Hello World!\"\n" ) );
	}

	@Test
	public void testChainedLines() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.ECHO, "Hello 1" ).line();
		builder.add( UpdateTask.ECHO, "Hello 2" ).line();
		builder.add( UpdateTask.ECHO, "Hello 3" ).line();
		assertThat( builder.toString(), is( UpdateTask.ECHO + " \"Hello 1\"\n" + UpdateTask.ECHO + " \"Hello 2\"\n" + UpdateTask.ECHO + " \"Hello 3\"\n\n" ) );
	}

}
