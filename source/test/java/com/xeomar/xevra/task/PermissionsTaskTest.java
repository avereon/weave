package com.xeomar.xevra.task;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PermissionsTaskTest {

	@Test
	public void testGetUserPermissions() {
		assertThat( new PermissionsTask( List.of( "" ) ).getUserPermissions( "777" ), is( 7 ) );
		assertThat( new PermissionsTask( List.of( "" ) ).getUserPermissions( "500" ), is( 5 ) );
		assertThat( new PermissionsTask( List.of( "" ) ).getUserPermissions( "000" ), is( 0 ) );
	}

	@Test
	public void testGetGroupPermissions() {
		assertThat( new PermissionsTask( List.of( "" ) ).getGroupPermissions( "777" ), is( 7 ) );
		assertThat( new PermissionsTask( List.of( "" ) ).getGroupPermissions( "050" ), is( 5 ) );
		assertThat( new PermissionsTask( List.of( "" ) ).getGroupPermissions( "000" ), is( 0 ) );
	}

	@Test
	public void testGetWorldPermissions() {
		assertThat( new PermissionsTask( List.of( "" ) ).getWorldPermissions( "777" ), is( 7 ) );
		assertThat( new PermissionsTask( List.of( "" ) ).getWorldPermissions( "005" ), is( 5 ) );
		assertThat( new PermissionsTask( List.of( "" ) ).getWorldPermissions( "000" ), is( 0 ) );
	}

	@Test
	public void testIsRead() {
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 0 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 1 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 2 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 3 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 4 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 5 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 6 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isRead( 7 ), is( true ) );
	}

	@Test
	public void testIsWrite() {
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 0 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 1 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 2 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 3 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 4 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 5 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 6 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isWrite( 7 ), is( true ) );
	}

	@Test
	public void testIsExec() {
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 0 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 1 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 2 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 3 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 4 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 5 ), is( true ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 6 ), is( false ) );
		assertThat( new PermissionsTask( List.of( "" ) ).isExec( 7 ), is( true ) );
	}

}
