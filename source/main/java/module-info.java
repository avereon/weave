module com.xeomar.xevra {

	requires java.logging;
	requires javafx.controls;
	requires com.xeomar.zenna;
	requires org.slf4j;

	exports com.xeomar.xevra;

	provides com.xeomar.product.Product with com.xeomar.xevra.Program;

}
