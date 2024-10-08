package org.jboss.as.quickstarts.helloworld;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import io.opentelemetry.api.trace.Span;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class Resources {
    @Produces
    @PersistenceContext
    private EntityManager em;

    @Produces
    public Logger produceLog(InjectionPoint injectionPoint, Span currentSpan) {
        MDC.put("traceId", currentSpan.getSpanContext().getTraceId());
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass());
    }

}
