import com.avereon.zenna.Program;

module com.avereon.zenna {

	requires static java.logging;
	requires javafx.controls;
	requires transitive com.avereon.zevra;
	requires transitive com.avereon.rossa;

	exports com.avereon.zenna;

	provides com.avereon.product.Product with Program;

}
