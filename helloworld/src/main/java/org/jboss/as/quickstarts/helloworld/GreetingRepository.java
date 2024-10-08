package org.jboss.as.quickstarts.helloworld;

import java.util.List;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class GreetingRepository {

    @Inject
    private EntityManager em;

    @WithSpan
    public Greeting findByLocation(String location) {
        return em.find(Greeting.class, location);
    }

    @WithSpan
    public void insert(Greeting greeting) {
        em.persist(greeting);
    }

    @WithSpan
    public List<Greeting> getAllGreetings() {
        return em.createQuery("SELECT g FROM Greeting g", Greeting.class).getResultList();
    }
}
