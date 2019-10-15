import com.avereon.zenna.Program;

module com.avereon.zenna {

	requires static java.logging;
	requires javafx.controls;
	requires com.avereon.zevra;

	exports com.avereon.zenna;

	provides com.avereon.product.Product with Program;

}
