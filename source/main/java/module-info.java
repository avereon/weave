import com.avereon.zenna.Program;

module com.avereon.zenna {

	requires java.logging;
	requires javafx.controls;
	requires com.avereon.zevra;
	requires org.slf4j;

	exports com.avereon.zenna;

	// For testing
	opens com.avereon.zenna.task;

	provides com.avereon.product.Product with Program;

}
