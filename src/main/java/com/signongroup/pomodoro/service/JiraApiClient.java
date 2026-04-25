package com.signongroup.pomodoro.service;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.client.annotation.Client;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Generic Micronaut HTTP client for Jira API calls.
 * URI is passed per call to support the user-configured dynamic base URL.
 * Authorization header is injected at runtime by {@link JiraAuthFilter}.
 */
@Client
public interface JiraApiClient {

    @Get
    CompletableFuture<HttpResponse<JsonNode>> get(URI uri);

    @Post
    CompletableFuture<HttpResponse<JsonNode>> post(URI uri, @Body String body);

    @Put
    CompletableFuture<HttpResponse<JsonNode>> put(URI uri, @Body String body);
}
