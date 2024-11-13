package org.jboss.as.quickstarts.helloworld;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/logging")
public class Logging {
    @Inject
    private Logger logger;

    @Inject
    private MeterRegistry registry;

    private Counter totalLogsCounter;

    @PostConstruct
    private void createMeters() {
        totalLogsCounter = Counter.builder("total_logs")
                .description("Total number of logging events")
                .register(registry);
    }

    @GET
    @Path("/severities")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Level> getLevels() {
        logger.debug("Returning all available levels");
        return EnumSet.allOf(Level.class);
    }

    @GET
    @Path("/loggers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getLoggers() {
        return Stream.of(ActiveUsers.class, GreetingRepository.class, Greetings.class, Message.class,
                MyApplication.class, Resources.class, Wellbeing.class).map(Class::getName).collect(Collectors.toList());
    }

    @POST
    @Path("/log")
    public void doLog(LogData logData) {
        Level level = Level.valueOf(logData.severity());
        if (logData.includeException()) {
            Exception e = new RuntimeException(logData.message());
            Logger.getLogger(logData.logger()).log(level, "Exception: " + logData.message(), e);
        } else {
            Logger.getLogger(logData.logger()).log(level, logData.message());
        }
        totalLogsCounter.increment();
    }
}
