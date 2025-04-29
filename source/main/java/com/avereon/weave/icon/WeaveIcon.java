package com.avereon.weave.icon;

import com.avereon.zerra.image.SvgIcon;

public class WeaveIcon extends SvgIcon {

	private static final double C = 16;

	private static final double SIZE = 4;

	private static final double SPACE = 12;

	public WeaveIcon() {
		draw( weave1() );
	}

	public String weave1() {
		StringBuilder builder = new StringBuilder();
		builder.append( "M " + (C - (0.5 * SPACE)) + "," + C + " l -" + SIZE + ",0" );
		builder.append( "M " + (C + (0.5 * SPACE)) + "," + C + " l " + SIZE + ",0" );
		builder.append( "M " + (C - (0.5 * SIZE)) + "," + (C - (0.5 * (SPACE + SIZE))) + "l " + SIZE + ",0" );
		builder.append( "M " + (C - (0.5 * SIZE)) + "," + (C + (0.5 * (SPACE + SIZE))) + "l " + SIZE + ",0" );

		builder.append( "M " + C + "," + (C - (0.5 * SIZE)) + " l 0," + SIZE );
		builder.append( "M " + (C - (0.5 * (SPACE + SIZE))) + "," + (C - (0.5 * SPACE) - SIZE) + " l 0," + SIZE );
		builder.append( "M " + (C + (0.5 * (SPACE + SIZE))) + "," + (C - (0.5 * SPACE) - SIZE) + " l 0," + SIZE );
		builder.append( "M " + (C - (0.5 * (SPACE + SIZE))) + "," + (C + (0.5 * SPACE)) + " l 0," + SIZE );
		builder.append( "M " + (C + (0.5 * (SPACE + SIZE))) + "," + (C + (0.5 * SPACE)) + " l 0," + SIZE );

		return builder.toString();
	}

	public String weave2() {
		StringBuilder builder = new StringBuilder();
		builder.append( "M " + C + "," + (C - (0.5 * SPACE)) + " l 0,-" + SIZE );
		builder.append( "M " + C + "," + (C + (0.5 * SPACE)) + " l 0," + SIZE );
		builder.append( "M " + (C - (0.5 * (SPACE + SIZE))) + "," + (C - (0.5 * SIZE)) + "l 0," + SIZE );
		builder.append( "M " + (C + (0.5 * (SPACE + SIZE))) + "," + (C - (0.5 * SIZE)) + "l 0," + SIZE );

		builder.append( "M " + (C - (0.5 * SIZE)) + "," + C + " l " + SIZE + ",0" );
		builder.append( "M " + (C - (0.5 * SPACE) - SIZE) + "," + (C - (0.5 * (SPACE + SIZE))) + " l " + SIZE + ",0" );
		builder.append( "M " + (C - (0.5 * SPACE) - SIZE) + "," + (C + (0.5 * (SPACE + SIZE))) + " l " + SIZE + ",0" );
		builder.append( "M " + (C + (0.5 * SPACE)) + "," + (C - (0.5 * (SPACE + SIZE))) + " l " + SIZE + ",0" );
		builder.append( "M " + (C + (0.5 * SPACE)) + "," + (C + (0.5 * (SPACE + SIZE))) + " l " + SIZE + ",0" );
		return builder.toString();
	}

	public static void main( String[] parameters ) {
		proof( new WeaveIcon() );
	}

}
