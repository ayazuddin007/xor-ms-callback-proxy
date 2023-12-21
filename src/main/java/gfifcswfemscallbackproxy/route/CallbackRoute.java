package gfxorwfemscallbackproxy.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

@Component
public class CallbackRoute extends RouteBuilder {

    @Override
    public void configure() {
        restConfiguration().component("http").bindingMode(RestBindingMode.json);

        from("direct:start")
                .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON))
                .toD("rest:post:?host=${header.serviceURL}&outType=com.wex.schema.callbackresponse._1.ReturnFlag");
    }
}
