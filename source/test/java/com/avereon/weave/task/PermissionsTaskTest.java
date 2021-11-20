package com.avereon.weave.task;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PermissionsTaskTest {

	@Test
	public void testGetUserPermissions() {
		assertThat( new PermissionsTask( List.of( "" ) ).getUserPermissions( "777" ) ).isEqualTo( 7 );
		assertThat( new PermissionsTask( List.of( "" ) ).getUserPermissions( "500" ) ).isEqualTo( 5 );
		assertThat( new PermissionsTask( List.of( "" ) ).getUserPermissions( "000" ) ).isEqualTo( 0 );
	}

	@Test
	public void testGetGroupPermissions() {
		assertThat( new PermissionsTask( List.of( "" ) ).getGroupPermissions( "777" ) ).isEqualTo( 7 );
		assertThat( new PermissionsTask( List.of( "" ) ).getGroupPermissions( "050" ) ).isEqualTo( 5 );
		assertThat( new PermissionsTask( List.of( "" ) ).getGroupPermissions( "000" ) ).isEqualTo( 0 );
	}

	@Test
	public void testGetWorldPermissions() {
		assertThat( new PermissionsTask( List.of( "" ) ).getWorldPermissions( "777" ) ).isEqualTo( 7 );
		assertThat( new PermissionsTask( List.of( "" ) ).getWorldPermissions( "005" ) ).isEqualTo( 5 );
		assertThat( new PermissionsTask( List.of( "" ) ).getWorldPermissions( "000" ) ).isEqualTo( 0 );
	}

	@Test
	public void testIsRead() {
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 0 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 1 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 2 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 3 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 4 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 5 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 6 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 7 ) ).isEqualTo( true );
	}

	@Test
	public void testIsWrite() {
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 0 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 1 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 2 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 3 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 4 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 5 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 6 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 7 ) ).isEqualTo( true );
	}

	@Test
	public void testIsExec() {
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 0 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 1 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 2 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 3 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 4 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 5 ) ).isEqualTo( true );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 6 ) ).isEqualTo( false );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 7 ) ).isEqualTo( true );
	}

}
