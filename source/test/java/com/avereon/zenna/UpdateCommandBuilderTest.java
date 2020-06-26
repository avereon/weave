package com.avereon.zenna;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UpdateCommandBuilderTest {

	@Test
	public void testConstructor() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		assertThat( builder.toString(), is( "" ) );
	}

	@Test
	public void testConstructorWithOneParameter() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder().add( UpdateTask.LOG );
		assertThat( builder.toString(), is( UpdateTask.LOG + "\n" ) );
	}

	@Test
	public void testConstructorWithTwoParametersWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder().add( UpdateTask.LOG, "Hello World!" );
		assertThat( builder.toString(), is( UpdateTask.LOG + " \"Hello World!\"\n" ) );
	}

	@Test
	public void testAdd() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG );
		assertThat( builder.toString(), is( UpdateTask.LOG + "\n" ) );
	}

	@Test
	public void testChainedAdd() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG, "Hello" );
		assertThat( builder.toString(), is( UpdateTask.LOG + " Hello\n" ) );
	}

	@Test
	public void testChainedAddWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG, "Hello World!" );
		assertThat( builder.toString(), is( UpdateTask.LOG + " \"Hello World!\"\n" ) );
	}

	@Test
	public void testChainedLines() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG, "Hello 1" );
		builder.add( UpdateTask.LOG, "Hello 2" );
		builder.add( UpdateTask.LOG, "Hello 3" );
		assertThat( builder.toString(), is( UpdateTask.LOG + " \"Hello 1\"\n" + UpdateTask.LOG + " \"Hello 2\"\n" + UpdateTask.LOG + " \"Hello 3\"\n" ) );
	}

}
