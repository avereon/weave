package com.xeomar.annex.task;

import com.xeomar.annex.AnnexTask;
import com.xeomar.annex.TaskResult;
import com.xeomar.annex.TaskStatus;
import com.xeomar.annex.UpdateTask;
import com.xeomar.util.FileUtil;
import com.xeomar.util.HashUtil;
import com.xeomar.util.LogUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class UnpackTask extends AnnexTask {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String DEL_SUFFIX = ".del";

	private static final String ADD_SUFFIX = ".add";

	private Path source;

	private Path target;

	private ZipFile zip;

	public UnpackTask( List<String> parameters ) {
		super( UpdateTask.UNPACK, parameters );
		source = Paths.get( getParameters().get( 0 ) );
		target = Paths.get( getParameters().get( 1 ) );
	}

	@Override
	public void validate() {
		if( !Files.exists( source ) ) throw new IllegalArgumentException( "Source not found: " + source );
		if( Files.exists( target ) && !Files.isDirectory( target ) ) throw new IllegalArgumentException( "Target already exists and is not a folder: " + target );
	}

	@Override
	public int getStepCount() throws Exception {
		return getZipFile().size() + 1;
	}

	@Override
	public boolean needsElevation() {
		return Files.exists( target ) && !Files.isWritable( target );
	}

	@Override
	public TaskResult execute() throws Exception {
		Files.createDirectories( target );

		log.debug( "Staging: {}", source );

		try {
			stage( source, target );
		} catch( ZipException exception ) {
			throw new IOException( "Source not a valid zip file: " + source );
		} catch( Throwable throwable ) {
			log.warn( "Unpack failed: " + target, throwable.getMessage() );
			revert( target );
			throw throwable;
		}

		log.debug( "Committing: {}", target );
		setMessage( "Committing " + target );
		commit( target );
		incrementProgress();

		return new TaskResult( this, TaskStatus.SUCCESS, "Unpacked: " + source + " to " + target );
	}

	private ZipFile getZipFile() throws IOException {
		if( zip == null ) zip = new ZipFile( source.toFile() );
		return zip;
	}

	private void stage( Path source, Path target ) throws IOException {
		log.trace( "Staging: {} to {}...", source.getFileName(), target );

		final ZipFile zip = getZipFile();
		try( zip ) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while( entries.hasMoreElements() ) {
				ZipEntry entry = entries.nextElement();
				setMessage( "Unpacking " + entry );
				boolean staged = stage( zip.getInputStream( entry ), target, entry.getName() );
				if( !staged ) throw new RuntimeException( "Could not stage: " + target.resolve( entry.getName() ) );
				incrementProgress();
			}
		}

		log.debug( "Staged: {} to {}", source.getFileName(), target );
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
				IOUtils.copy( input, output );
			}
		}

		log.debug( "Staging: {}", entry );
		return true;
	}

	private void revert( Path target ) throws IOException {
		revert( target, target );
	}

	private void revert( Path root, Path target ) throws IOException {
		// Revert staged changes.
		if( Files.isDirectory( target ) ) {
			try( Stream<Path> paths = Files.list( target ) ) {
				for( Path file : paths.collect( Collectors.toList() ) ) revert( root, file );
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
				for( Path file : paths.collect( Collectors.toList() ) ) commit( root, file );
			}
		} else {
			if( target.getFileName().toString().endsWith( ADD_SUFFIX ) ) {
				String sourceHash = HashUtil.hash( target );
				Path file = FileUtil.removeExtension( target );
				Files.move( target, file, StandardCopyOption.REPLACE_EXISTING );
				String targetHash = HashUtil.hash( file );
				if( !targetHash.equals( sourceHash ) ) throw new RuntimeException( "Hash code mismatch committing file: " + file );
				log.trace( "Commit: {}", root.relativize( file ) );
			} else if( target.getFileName().toString().endsWith( DEL_SUFFIX ) ) {
				Path file = removeSuffix( target, DEL_SUFFIX );
				if( !Files.exists( file ) ) log.trace( "Remove: {}", root.relativize( file ) );
				Files.delete( target );
			}
		}
	}

	private Path removeSuffix( Path path, String suffix ) {
		String name = path.getFileName().toString();
		int index = name.indexOf( suffix );
		return index < 0 ? path : path.getParent().resolve( name.substring( 0, index ) );
	}

}
