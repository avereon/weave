module com.avereon.weave {

	requires javafx.controls;
	requires com.avereon.zevra;
	requires com.avereon.zenna;

	exports com.avereon.weave;

	provides com.avereon.product.Product with com.avereon.weave.Program;

}
