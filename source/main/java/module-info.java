module com.xeomar.annex {

	requires java.logging;
	requires javafx.controls;
	requires com.xeomar.razor;
	requires org.apache.commons.io;
	requires org.slf4j;

	exports com.xeomar.annex;

	provides com.xeomar.product.Product with com.xeomar.annex.Program;

}
