/*
 * Copyright 2024 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.helloworld;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import java.util.logging.Logger;

@ApplicationScoped
public class DatabaseInitializer {

    @Inject
    private Logger logger;

    @Inject
    private GreetingRepository greetingRepository;

    @Inject
    private UserTransaction txn;

    @PostConstruct
    public void initializeDatabase() {
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
            } else {
                logger.info(
                    "Database already contains greetings data, skipping initialization"
                );
            }

            txn.commit();
        } catch (Exception e) {
            logger.severe("Failed to initialize database: " + e.getMessage());
            try {
                txn.rollback();
            } catch (Exception rollbackException) {
                logger.severe(
                    "Failed to rollback transaction: " +
                        rollbackException.getMessage()
                );
            }
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
            logger.severe(
                "Failed to insert greeting for location: " +
                    location +
                    ", error: " +
                    e.getMessage()
            );
        }
    }
}
