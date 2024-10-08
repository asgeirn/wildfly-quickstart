package org.jboss.as.quickstarts.helloworld;

import java.util.concurrent.ConcurrentSkipListSet;

import org.jboss.logging.MDC;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ActiveUsers {
    @Inject
    private MeterRegistry registry;

    private ConcurrentSkipListSet<String> activeUsers;

    @PostConstruct
    private void createMeters() {
        activeUsers = registry.gaugeCollectionSize("active_users", Tags.empty(), new ConcurrentSkipListSet<>());
    }

    public void register(String user) {
        activeUsers.add(user);
        MDC.put("active", activeUsers.size());
    }

    public void unregister(String user) {
        activeUsers.remove(user);
    }
}
