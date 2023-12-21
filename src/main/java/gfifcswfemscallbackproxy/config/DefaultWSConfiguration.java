package gfxorwfemscallbackproxy.config;

import com.wex.integration.callback.CallbackPortTypeSOAP;
import gfxorwfemscallbackproxy.Callback;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingConfiguration;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

@Configuration
//@ImportResource({"classpath*:applicationContext.xml"})
public class DefaultWSConfiguration {

    @Bean
    public ServletRegistrationBean messageDispatcherServlet() {
        return new ServletRegistrationBean(new CXFServlet(), "/integration/callback/*");
    }

    @Bean(name= Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public CallbackPortTypeSOAP callbackSOAP() {
        return new Callback();
    }

    @Bean
    public BindingConfiguration bindingConfiguration(){return  new CallbackBindingConfiguration();}

    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), callbackSOAP());
        endpoint.setServiceName(new QName("http://www.wex.com/integration/callback","CallbackService",""));
        endpoint.setEndpointName(new QName("CallbackSOAP"));
        endpoint.setBindingConfig(bindingConfiguration());
        endpoint.publish("/CallbackSOAP");
        endpoint.setWsdlLocation("wsdl/xorCallback.wsdl");
        return endpoint;
    }

}
