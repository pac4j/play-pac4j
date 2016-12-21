package org.pac4j.play.deadbolt2;

import be.objectify.deadbolt.java.models.Permission;
import play.libs.concurrent.HttpExecutionContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface Pac4jRoleHandler {

   default  CompletionStage<List<? extends Permission>> getPermissionsForRole(String clients, String roleName, HttpExecutionContext httpExecutionContext) {
       return CompletableFuture.completedFuture(Collections.emptyList());
    }
}
