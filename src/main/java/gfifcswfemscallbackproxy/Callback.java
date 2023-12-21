package gfxorwfemscallbackproxy;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.integration.callback.CallbackPortTypeSOAP;
import com.wex.schema.callbackresponse._1.BatchProcessingResultType;
import com.wex.schema.callbackresponse._1.InterfaceProcessingResultType;
import com.wex.schema.callbackresponse._1.ReturnFlag;
import com.wexinc.service.logging.loggingSchema.EventDocument;
import gfxorwfemscallbackproxy.model.Service;
import gfxorwfemscallbackproxy.repository.ServiceRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.jws.WebService;
import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Optional;

@WebService(serviceName = "CallbackService",//External service name
        targetNamespace = "http://www.wex.com/integration/callback",//Specify the namespace you want, usually using package name inversion
        endpointInterface = "com.wex.integration.callback.CallbackPortTypeSOAP")
//Full path of service interface, Designated to do SEI(Service EndPoint Interface)Service
public class Callback implements CallbackPortTypeSOAP {

    private static final String STATUS_WAITING_CALLBACK = "WAITING_CALLBACK";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String BATCH_SERVICE_NAME = "BatchJobCallbackProxyService";
    private static final String CALLBACK_PROXY_SERVICE_NAME = "CallbackProxyService";
    private static final String BATCH_EVENT_TYPE = "Batch_Request_Received";
    private static final String CALLBACK_EVENT_TYPE = "Interface_Request_Received";

    private static final Logger LOGGER = LoggerFactory.getLogger(Callback.class);

    @Autowired
    private CamelContext context;

    @Autowired
    ServiceRepository serviceRepository;

    @Override
    public void callback(InterfaceProcessingResultType request) {

        StringWriter sw = new StringWriter();
        JAXB.marshal(request, sw);
        logRequest(request.getCorrelationID(), sw.toString(), CALLBACK_PROXY_SERVICE_NAME, CALLBACK_EVENT_TYPE);

        try {
            processRequest(request, CALLBACK_PROXY_SERVICE_NAME);
        } catch (Exception e) {
            logError(CALLBACK_PROXY_SERVICE_NAME, request.getCorrelationID(), e.getMessage());
        }
    }

    @Override
    public void batchCallback(BatchProcessingResultType request) {

        StringWriter sw = new StringWriter();
        JAXB.marshal(request, sw);
        logRequest(request.getCorrelationID(), sw.toString(), BATCH_SERVICE_NAME, BATCH_EVENT_TYPE);

        try {
            InterfaceProcessingResultType intRequest = new InterfaceProcessingResultType();

            intRequest.setInterfaceID(request.getJobName());
            intRequest.setCorrelationID(request.getCorrelationID());
            intRequest.setClientMid(request.getClientMid());

            processRequest(intRequest, BATCH_SERVICE_NAME);
        } catch (Exception e) {
            logError(BATCH_SERVICE_NAME, request.getCorrelationID(), e.getMessage());
        }
    }

    private void processRequest(InterfaceProcessingResultType request, String serviceName) throws Exception {

        Optional<Service> serviceOptional = serviceRepository.getServiceByCorrelationIdAndClientIdAndInterfaceId(request.getCorrelationID(),request.getClientMid(),request.getInterfaceID());

        if (serviceOptional.isPresent()) {

            Service service = serviceOptional.get();

            switch (service.getStatus()) {
                case STATUS_WAITING_CALLBACK: // Hit back end

                    ReturnFlag returnFlag = invokeBackend(request, service);
                    StringWriter sw = new StringWriter();
                    JAXB.marshal(returnFlag, sw);
                    logRequest(request.getCorrelationID(), sw.toString(), serviceName, "xor_RESPONSE");

                    if (returnFlag.isIsErrorCode() != null && returnFlag.isIsErrorCode()) {
                        if (!StringUtils.isEmpty(returnFlag.getMessage())) {
                            service.setStatus(returnFlag.getMessage().length() > 150 ? returnFlag.getMessage().substring(0, 150) : returnFlag.getMessage());
                        } else {
                            service.setStatus("GENERIC_xor_ERROR");
                        }
                    } else {
                        service.setStatus(STATUS_COMPLETED);
                    }
                    serviceRepository.save(service);

                    break;
                case STATUS_COMPLETED: // If completed then log error
                    logError(serviceName,
                            request.getCorrelationID(),
                            "Callback is already complete");
                    break;
                default: // If error, log error
                    logError(serviceName,
                            request.getCorrelationID(),
                            "Record is in error, to re-run, change status to WAITING_CALLBACK");
            }
        } else {// if no service URL found, log error
            logError(serviceName,
                    request.getCorrelationID(),
                    "Record not found for given correlationID");
        }


    }

    public ReturnFlag invokeBackend(InterfaceProcessingResultType request, Service service) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJSON = objectMapper.writeValueAsString(request);
        logRequest(request.getCorrelationID(), requestJSON, CALLBACK_PROXY_SERVICE_NAME, "Interface Request Sent");

        return (ReturnFlag) context.createProducerTemplate().sendBodyAndHeader("direct:start", ExchangePattern.InOut,
                request, "serviceURL", service.getServiceUrl());
    }

    public void logError(String serviceName, String correlationID, String message) {

        EventDocument eventDocument = EventDocument.Factory.newInstance();
        EventDocument.Event event = eventDocument.addNewEvent();

        event.setServiceName(serviceName);
        event.setEventType("CALLBACK_REQUEST_ERROR");
        event.setCreationTimestamp(Calendar.getInstance());
        event.setCorrelationId(correlationID);
        event.setPayload(message);

        LOGGER.error(eventDocument.xmlText());

    }

    private void logRequest(String correlationID, String payload, String serviceName, String eventType) {

        EventDocument eventDocument = EventDocument.Factory.newInstance();
        EventDocument.Event event = eventDocument.addNewEvent();

        event.setServiceName(serviceName);
        event.setEventType(eventType);
        event.setCreationTimestamp(Calendar.getInstance());
        event.setCorrelationId(correlationID);
        event.setPayload(payload);

        LOGGER.info(eventDocument.xmlText());

    }


}
