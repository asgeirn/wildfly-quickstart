package org.jboss.as.quickstarts.helloworld;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/build-info")
public class BuildInfoResource {

    @Inject
    private BuildInfoService buildInfoService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuildInfo() {
        BuildInfo buildInfo = buildInfoService.getBuildInfo();
        return Response.ok(buildInfo).build();
    }

    @GET
    @Path("/timestamp")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getBuildTimestamp() {
        return Response.ok(buildInfoService.getBuildInfo().timestamp()).build();
    }

    @GET
    @Path("/commit-sha")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCommitSha() {
        return Response.ok(buildInfoService.getBuildInfo().commitSha()).build();
    }

    @GET
    @Path("/commit-tag")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCommitTag() {
        return Response.ok(buildInfoService.getBuildInfo().commitTag()).build();
    }

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(buildInfoService.getBuildInfo().version()).build();
    }

    @GET
    @Path("/build-number")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getBuildNumber() {
        return Response.ok(
            buildInfoService.getBuildInfo().buildNumber()
        ).build();
    }

    @GET
    @Path("/wildfly-version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWildflyVersion() {
        return Response.ok(
            buildInfoService.getBuildInfo().wildflyVersion()
        ).build();
    }
}
