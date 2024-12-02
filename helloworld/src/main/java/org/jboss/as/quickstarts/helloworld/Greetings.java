package org.jboss.as.quickstarts.helloworld;

import java.util.List;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

@RequestScoped
@Path("/greetings")
public class Greetings {
    @Inject
    private Logger log;

    @Inject
    private GreetingRepository repository;

    @Inject
    private UserTransaction txn;

    @Inject
    private Tracer tracer;

    @Inject
    private MeterRegistry registry;

    @Inject
    private Wellbeing wellbeing;

    @Inject
    private ActiveUsers activeUsers;

    private Counter totalRequestsCounter;

    @PostConstruct
    private void createMeters() {
        totalRequestsCounter = Counter.builder("total_requests")
                .description("Total number of requests")
                .register(registry);
    }

    @GET
    @Path("/hello/{location}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(
            @PathParam("location") String location,
            @HeaderParam("X-Forwarded-For") @DefaultValue("0.0.0.0") String source,
            @HeaderParam("X-User") @DefaultValue("unknown") String user,
            @HeaderParam("X-Seasons") @DefaultValue("false") Boolean seasons) {
        Span prepareHelloSpan = tracer.spanBuilder("prepare-hello").startSpan();
        prepareHelloSpan.setAttribute("greeting", location);
        prepareHelloSpan.setAttribute("source", source);
        prepareHelloSpan.setAttribute("user", user);
        try (var scope = prepareHelloSpan.makeCurrent()) {
            activeUsers.register(user);
            MDC.put("user", user);
            MDC.put("greeting", location);
            log.infof("Handling greeting request for %s from %s", location, source);
            var greeting = repository.findByLocation(location);
            Span processHelloSpan = tracer.spanBuilder("process-hello").startSpan();
            try (var innerScope = processHelloSpan.makeCurrent()) {
                if (greeting != null) {
                    var message = greeting.getMessage();
                    MDC.put("message", message);
                    StringBuilder builder = new StringBuilder();
                    if (Boolean.TRUE.equals(seasons)) {
                        builder.append("Ho-ho-ho, ");
                    } else {
                        builder.append("Hello, ");
                    }
                    builder.append(message).append('!');
                    var wish = wellbeing.wish();
                    if (wish.message() != null && !wish.message().isBlank()) {
                        MDC.put("wish", wish.message());
                        builder.append(' ').append(wish.message());
                    }
                    processHelloSpan.setStatus(StatusCode.OK);
                    return Response.ok(builder.append('\n').toString()).build();
                } else {
                    processHelloSpan.setStatus(StatusCode.ERROR, "Greeting not found");
                    return Response.status(Status.NOT_FOUND).build();
                }
            } catch (Exception e) {
                processHelloSpan.setStatus(StatusCode.ERROR);
                processHelloSpan.recordException(e);
                throw e;
            } finally {
                processHelloSpan.end();
            }
        } catch (Exception e) {
            prepareHelloSpan.setStatus(StatusCode.ERROR);
            prepareHelloSpan.recordException(e);
            throw e;
        } finally {
            activeUsers.unregister(user);
            totalRequestsCounter.increment();
            prepareHelloSpan.end();
            MDC.clear();
        }
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Greeting> getAllGreetings(
            @HeaderParam("X-Forwarded-For") @DefaultValue("0.0.0.0") String source,
            @HeaderParam("X-User") @DefaultValue("unknown") String user) {
        var listGreetingsSpan = tracer.spanBuilder("list-greetings").startSpan();
        listGreetingsSpan.setAttribute("user", user);
        listGreetingsSpan.setAttribute("source", source);
        MDC.put("user", user);
        MDC.put("source", source);
        try (var scope = listGreetingsSpan.makeCurrent()) {
            activeUsers.register(user);
            var greetings = repository.getAllGreetings();
            log.infof("System contains %d greetings.", greetings.size());
            listGreetingsSpan.setStatus(StatusCode.OK);
            return greetings;
        } catch (Exception e) {
            listGreetingsSpan.setStatus(StatusCode.ERROR);
            listGreetingsSpan.recordException(e);
            throw e;
        } finally {
            listGreetingsSpan.end();
            activeUsers.unregister(user);
            MDC.clear();
        }
    }

    @PUT
    @Path("/hello/{location}/{message}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createHello(
            @PathParam("location") String location,
            @PathParam("message") String message,
            @HeaderParam("X-Forwarded-For") String source) {
        Span createHelloSpan = tracer.spanBuilder("create-hello").startSpan();
        MDC.put("message", message);
        log.infof("Creating greeting for %s from %s", location, source);
        try (var scope = createHelloSpan.makeCurrent()) {
            txn.begin();
            Greeting greeting = new Greeting();
            greeting.setLocation(location);
            greeting.setMessage(message);
            repository.insert(greeting);
            txn.commit();
            createHelloSpan.setStatus(StatusCode.OK);
            return Response.ok().build();
        } catch (Exception e) {
            MDC.put("error", e);
            createHelloSpan.setStatus(StatusCode.ERROR);
            createHelloSpan.recordException(e);
            throw new RuntimeException(e);
        } finally {
            createHelloSpan.end();
            MDC.clear();
        }
    }

    @GET
    @Path("/headers")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getHeaders(@Context HttpHeaders headers) {
        StringBuilder builder = new StringBuilder();
        headers.getRequestHeaders().forEach((k, v) -> builder.append(k).append('=').append(String.join(",", v)).append('\n'));
        return Response.ok(builder.toString()).build();
    }

}
