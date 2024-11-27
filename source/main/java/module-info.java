module com.avereon.weave {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.zevra;
	requires com.avereon.zenna;
	requires com.avereon.zarra;
	requires javafx.controls;

	exports com.avereon.weave;
	exports com.avereon.weave.icon;

	provides com.avereon.product.Product with com.avereon.weave.Weave;
}
