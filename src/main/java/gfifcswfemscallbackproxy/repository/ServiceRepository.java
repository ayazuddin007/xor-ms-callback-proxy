package gfxorwfemscallbackproxy.repository;

import gfxorwfemscallbackproxy.model.Service;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ServiceRepository extends CrudRepository<Service, Long> {

    Optional<Service> getServiceByCorrelationIdAndClientIdAndInterfaceId(String correlationId, String clientId, String interfaceId);
}