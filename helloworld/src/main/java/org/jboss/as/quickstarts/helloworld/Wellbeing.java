package org.jboss.as.quickstarts.helloworld;

import org.jboss.logging.Logger;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class Wellbeing {

    @Inject
    private Logger logger;

    @WithSpan
    public Message wish() {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target("https://api.twingine.com/wish");
            try (Response response = target.request().get()) {
                logger.infof("Response: %s", response.getStatusInfo());
                var message = response.readEntity(Message.class);
                return message;
            }
        }
    }
}
