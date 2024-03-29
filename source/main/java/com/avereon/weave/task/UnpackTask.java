package com.avereon.weave.task;

import com.avereon.util.FileUtil;
import com.avereon.util.HashUtil;
import com.avereon.util.IoUtil;
import com.avereon.weave.Task;
import com.avereon.weave.TaskResult;
import com.avereon.weave.TaskStatus;
import com.avereon.weave.UpdateTask;
import lombok.CustomLog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@CustomLog
public class UnpackTask extends Task {

	private static final String DEL_SUFFIX = ".del";

	private static final String ADD_SUFFIX = ".add";

	private final Path source;

	private final Path target;

	private ZipFile zip;

	public UnpackTask( List<String> parameters ) {
		super( UpdateTask.UNPACK, parameters );
		source = Paths.get( getParameters().get( 0 ) );
		target = Paths.get( getParameters().get( 1 ) );
	}

	@Override
	public int getStepCount() throws Exception {
		if( !Files.exists( source ) ) throw new IllegalArgumentException( "Source not found: " + source );
		return getZipFile().size() + 1;
	}

	@Override
	public void validate() {
		if( !Files.exists( source ) ) throw new IllegalArgumentException( "Source not found: " + source );
		if( Files.exists( target ) && !Files.isDirectory( target ) ) throw new IllegalArgumentException( "Target already exists and is not a folder: " + target );
	}

	@Override
	public boolean needsElevation() {
		if( !Files.exists( target ) ) return !Files.isWritable( target.getParent() );
		if( Files.exists( target ) ) return !Files.isWritable( target );
		return false;
	}

	@Override
	public TaskResult execute() throws Exception {
		Files.createDirectories( target );

		log.atFine().log( "Staging: %s", source );

		try {
			stage( source, target );
		} catch( ZipException exception ) {
			throw new IOException( "Source not a valid zip file: " + source );
		} catch( Throwable throwable ) {
			log.atWarning().withCause( throwable ).log( "Unpack failed: %s", target );
			revert( target );
			throw throwable;
		}

		log.atFine().log( "Committing: %s", target );
		setMessage( "Committing " + target );
		commit( target );
		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, source + " to " + target );
	}

	private ZipFile getZipFile() throws IOException {
		if( zip == null ) zip = new ZipFile( source.toFile() );
		return zip;
	}

	private void stage( Path source, Path target ) throws IOException {
		log.atFiner().log( "Staging: %s to %s...", source.getFileName(), target );

		final ZipFile zip = getZipFile();
		try( zip ) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while( entries.hasMoreElements() ) {
				ZipEntry entry = entries.nextElement();
				setMessage( "Unpacking " + entry );
				String name = entry.getName();
				if( !name.contains( "../")) {
					boolean staged = stage( zip.getInputStream( entry ), target, entry.getName() );
					if( !staged ) throw new IOException( "Could not stage: " + target.resolve( entry.getName() ) );
				}
				incrementProgress();
			}
		}

		log.atFiner().log( "Staged: %s to %s...", source.getFileName(), target );
	}

	private boolean stage( InputStream input, Path target, String entry ) throws IOException {
		Path file = target.resolve( entry );
		boolean folder = entry.endsWith( "/" );

		if( folder ) {
			Files.createDirectories( file );
		} else {
			Path delFile = file.getParent().resolve( file.getFileName().toString() + DEL_SUFFIX );
			Path addFile = file.getParent().resolve( file.getFileName().toString() + ADD_SUFFIX );
			if( Files.exists( file ) ) Files.move( file, delFile, StandardCopyOption.ATOMIC_MOVE );
			Files.createDirectories( file.getParent() );
			try( FileOutputStream output = new FileOutputStream( addFile.toFile() ) ) {
				IoUtil.copy( input, output );
			}
		}

		log.atFiner().log( "Unpack: %s", entry );
		return true;
	}

	private void revert( Path target ) throws IOException {
		revert( target, target );
	}

	@SuppressWarnings( "unused" )
	private void revert( Path root, Path target ) throws IOException {
		// Revert staged changes.
		if( Files.isDirectory( target ) ) {
			try( Stream<Path> paths = Files.list( target ) ) {
				for( Path file : paths.toList() ) revert( root, file );
			}
		} else {
			if( target.getFileName().toString().endsWith( DEL_SUFFIX ) ) {
				Files.move( target, FileUtil.removeExtension( target ), StandardCopyOption.REPLACE_EXISTING );
			} else if( target.getFileName().toString().endsWith( ADD_SUFFIX ) ) {
				Files.delete( target );
			}
		}
	}

	private void commit( Path target ) throws IOException {
		commit( target, target );
	}

	private void commit( Path root, Path target ) throws IOException {
		// Commit staged changes.
		if( Files.isDirectory( target ) ) {
			try( Stream<Path> paths = Files.list( target ) ) {
				for( Path file : paths.toList() ) commit( root, file );
			}
		} else {
			if( target.getFileName().toString().endsWith( ADD_SUFFIX ) ) {
				String sourceHash = HashUtil.hash( target );
				Path file = FileUtil.removeExtension( target );
				Files.move( target, file, StandardCopyOption.REPLACE_EXISTING );
				String targetHash = HashUtil.hash( file );
				if( !targetHash.equals( sourceHash ) ) throw new RuntimeException( "Hash code mismatch committing file: " + file );
				log.atFiner().log( "Commit: %s", root.relativize( file ) );
			} else if( target.getFileName().toString().endsWith( DEL_SUFFIX ) ) {
				Path file = removeSuffix( target, DEL_SUFFIX );
				if( !Files.exists( file ) ) log.atFiner().log( "Remove: %s", root.relativize( file ) );
				Files.delete( target );
			}
		}
	}

	@SuppressWarnings( "SameParameterValue" )
	private Path removeSuffix( Path path, String suffix ) {
		String name = path.getFileName().toString();
		int index = name.indexOf( suffix );
		return index < 0 ? path : path.getParent().resolve( name.substring( 0, index ) );
	}

	@Override
	public TaskResult rollback() throws Exception {
		revert( target );
		decrementProgress();
		return new TaskResult( this, TaskStatus.ROLLBACK, String.valueOf( target ) );
	}

}
