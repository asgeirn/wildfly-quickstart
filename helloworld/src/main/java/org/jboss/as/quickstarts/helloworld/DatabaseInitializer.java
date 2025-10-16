package org.jboss.as.quickstarts.helloworld;

import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/database")
public class DatabaseInitializer {

    @Inject
    private Logger logger;

    @Inject
    private GreetingRepository greetingRepository;

    @Inject
    private UserTransaction txn;

    @PUT
    @Path("/initialize")
    @Produces(MediaType.APPLICATION_JSON)
    public Response initializeDatabase() {
        try {
            txn.begin();

            // Check if database is empty
            if (greetingRepository.getAllGreetings().isEmpty()) {
                logger.info("Database is empty, inserting example greetings");

                // Insert example greetings
                insertGreeting("Drammen", "E18");
                insertGreeting("Sandvika", "storsenter");
                insertGreeting("Norway", "vikings");
                insertGreeting("Denmark", "hunde");
                insertGreeting("Sweeden", "grabbar");
                insertGreeting("England", "chaps");
                insertGreeting("America", "make it great again");
                insertGreeting("Trump", "tariffs");
                insertGreeting("Kolsås", "soldater");
                insertGreeting("NATO", "alliance");
                insertGreeting("Bouvet", "pingviner");

                logger.info("Successfully inserted example greetings data");
                txn.commit();
                return Response.ok()
                    .entity(
                        "{\"message\": \"Database initialized successfully with example greetings\"}"
                    )
                    .build();
            } else {
                logger.info(
                    "Database already contains greetings data, skipping initialization"
                );
                txn.commit();
                return Response.ok()
                    .entity(
                        "{\"message\": \"Database already contains data, initialization skipped\"}"
                    )
                    .build();
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            try {
                txn.rollback();
            } catch (Exception rollbackException) {
                logger.error(
                    "Failed to rollback transaction",
                    rollbackException
                );
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(
                    "{\"error\": \"Failed to initialize database: " +
                        e.getMessage() +
                        "\"}"
                )
                .build();
        }
    }

    private void insertGreeting(String location, String message) {
        try {
            Greeting greeting = new Greeting();
            greeting.setLocation(location);
            greeting.setMessage(message);
            greetingRepository.insert(greeting);
            logger.info("Inserted greeting for location: " + location);
        } catch (Exception e) {
            logger.errorf(
                e,
                "Failed to insert greeting for location %s",
                location
            );
            throw new RuntimeException(
                "Failed to insert greeting for location: " + location,
                e
            );
        }
    }
}
