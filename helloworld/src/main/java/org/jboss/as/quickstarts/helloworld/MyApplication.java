package org.jboss.as.quickstarts.helloworld;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationScoped
@ApplicationPath("/services")
public class MyApplication extends Application {

}