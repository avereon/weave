import com.avereon.zenna.Program;

module com.avereon.zenna {

	requires javafx.controls;
	requires com.avereon.zevra;
	requires com.avereon.rossa;

	exports com.avereon.zenna;

	provides com.avereon.product.Product with Program;

}
