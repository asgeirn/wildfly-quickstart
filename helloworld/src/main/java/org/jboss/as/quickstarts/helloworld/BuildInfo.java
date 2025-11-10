package org.jboss.as.quickstarts.helloworld;

/**
 * Build information record containing metadata about the application build.
 * This includes commit information, timestamps, and build identifiers.
 */
public record BuildInfo(
    String timestamp,
    String commitSha,
    String commitTag,
    String version,
    String buildNumber
) {}
