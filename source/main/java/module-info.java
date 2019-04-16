import com.xeomar.zenna.Program;

module com.xeomar.xevra {

	requires java.logging;
	requires javafx.controls;
	requires com.xeomar.zenna;
	requires org.slf4j;

	exports com.xeomar.zenna;

	provides com.xeomar.product.Product with Program;

}
