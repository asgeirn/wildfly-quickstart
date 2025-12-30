package org.jboss.as.quickstarts.helloworld;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BuildInfoService {

    private static final Logger LOGGER = Logger.getLogger(
        BuildInfoService.class
    );

    private String buildTimestamp;
    private String commitSha;
    private String commitTag;
    private String version;
    private String buildNumber;
    private String platformVersion;

    @PostConstruct
    public void init() {
        loadBuildInfo();
    }

    private void loadBuildInfo() {
        Properties props = new Properties();
        try (
            InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("build-info.properties")
        ) {
            if (is != null) {
                props.load(is);
                buildTimestamp = getValueWithEnvFallback(
                    props,
                    "build.timestamp",
                    "BUILD_TIMESTAMP"
                );
                commitSha = getValueWithEnvFallback(
                    props,
                    "build.commit.sha",
                    "BUILD_COMMIT_SHA"
                );
                commitTag = getValueWithEnvFallback(
                    props,
                    "build.commit.tag",
                    "BUILD_COMMIT_TAG"
                );
                version = getValueWithEnvFallback(
                    props,
                    "build.version",
                    "BUILD_VERSION"
                );
                buildNumber = getValueWithEnvFallback(
                    props,
                    "build.number",
                    "BUILD_PIPELINE_ID"
                );

                platformVersion = detectPlatformVersion();

                LOGGER.infof(
                    "Build info loaded successfully - Timestamp: %s, SHA: %s, Tag: %s, Version: %s, Build: %s, Platform: %s",
                    buildTimestamp,
                    commitSha,
                    commitTag,
                    version,
                    buildNumber,
                    platformVersion
                );
            } else {
                LOGGER.warn(
                    "build-info.properties not found, trying environment variables"
                );
                loadFromEnvironment();
            }
        } catch (IOException e) {
            LOGGER.warnf(
                "Failed to load build-info.properties: %s, trying environment variables",
                e.getMessage()
            );
            loadFromEnvironment();
        }
    }

    private String getValueWithEnvFallback(
        Properties props,
        String propKey,
        String envKey
    ) {
        String value = props.getProperty(propKey);
        if (
            value == null || "unknown".equals(value) || value.startsWith("${")
        ) {
            // Property not found or not filtered properly, try environment variable
            value = System.getenv(envKey);
            if (value == null) {
                value = "unknown";
            }
        }
        return value;
    }

    private void loadFromEnvironment() {
        buildTimestamp = System.getenv("BUILD_TIMESTAMP");
        commitSha = System.getenv("BUILD_COMMIT_SHA");
        commitTag = System.getenv("BUILD_COMMIT_TAG");
        version = System.getenv("BUILD_VERSION");
        buildNumber = System.getenv("BUILD_PIPELINE_ID");

        if (buildTimestamp == null) buildTimestamp = "unknown";
        if (commitSha == null) commitSha = "unknown";
        if (commitTag == null) commitTag = "unknown";
        if (version == null) version = "unknown";
        if (buildNumber == null) buildNumber = "unknown";
        platformVersion = detectPlatformVersion();

        LOGGER.infof(
            "Build info loaded from environment variables - Timestamp: %s, SHA: %s, Tag: %s, Version: %s, Build: %s, Platform: %s",
            buildTimestamp,
            commitSha,
            commitTag,
            version,
            buildNumber,
            platformVersion
        );
    }

    private String detectPlatformVersion() {
        // Try to get platform version from system property
        String version = System.getProperty("jboss.product.version");
        if (version == null) {
            // Fallback to AS version if product version not available
            version = System.getProperty("jboss.as.release.version");
        }
        if (version == null) {
            version = "unknown";
        }
        return version;
    }

    public BuildInfo getBuildInfo() {
        return new BuildInfo(
            buildTimestamp,
            commitSha,
            commitTag,
            version,
            buildNumber,
            platformVersion
        );
    }
}
