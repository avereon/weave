import com.xeomar.zenna.Program;

module com.xeomar.zenna {

	requires java.logging;
	requires javafx.controls;
	requires com.xeomar.zevra;
	requires org.slf4j;

	exports com.xeomar.zenna;

	provides com.xeomar.product.Product with Program;

}
