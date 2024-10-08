package org.jboss.as.quickstarts.helloworld;

public record LogData(String severity, String logger, String message, boolean includeException) {}
