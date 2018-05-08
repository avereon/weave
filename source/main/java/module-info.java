module com.xeomar.annex {

	requires com.xeomar.razor;
	requires org.slf4j;
	requires commons.io;
	requires java.logging;

	exports com.xeomar.annex;

	provides com.xeomar.product.Product with com.xeomar.annex.Program;

}