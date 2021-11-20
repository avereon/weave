package com.avereon.weave;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateCommandBuilderTest {

	@Test
	public void testConstructor() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		assertThat( builder.toString() ).isEqualTo( "" );
	}

	@Test
	public void testConstructorWithOneParameter() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder().add( UpdateTask.LOG );
		assertThat( builder.toString() ).isEqualTo( UpdateTask.LOG + "\n" );
	}

	@Test
	public void testConstructorWithTwoParametersWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder().add( UpdateTask.LOG, "Hello World!" );
		assertThat( builder.toString() ).isEqualTo( UpdateTask.LOG + " \"Hello World!\"\n" );
	}

	@Test
	public void testAdd() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG );
		assertThat( builder.toString() ).isEqualTo( UpdateTask.LOG + "\n" );
	}

	@Test
	public void testChainedAdd() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG, "Hello" );
		assertThat( builder.toString() ).isEqualTo( UpdateTask.LOG + " Hello\n" );
	}

	@Test
	public void testChainedAddWithSpaces() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG, "Hello World!" );
		assertThat( builder.toString() ).isEqualTo( UpdateTask.LOG + " \"Hello World!\"\n" );
	}

	@Test
	public void testChainedLines() {
		UpdateCommandBuilder builder = new UpdateCommandBuilder();
		builder.add( UpdateTask.LOG, "Hello 1" );
		builder.add( UpdateTask.LOG, "Hello 2" );
		builder.add( UpdateTask.LOG, "Hello 3" );
		assertThat( builder.toString() ).isEqualTo( UpdateTask.LOG + " \"Hello 1\"\n" + UpdateTask.LOG + " \"Hello 2\"\n" + UpdateTask.LOG + " \"Hello 3\"\n" );
	}

}
