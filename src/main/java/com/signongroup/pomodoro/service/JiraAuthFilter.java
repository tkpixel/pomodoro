package com.signongroup.pomodoro.service;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Intercepts all JiraApiClient requests and injects the Authorization header
 * using the currently saved credentials from JiraAuthService.
 *
 * This allows the Jira base URL and token to remain dynamic (user-configured at runtime)
 * while keeping the JiraApiClient interface clean and declarative.
 */
@Singleton
public class JiraAuthFilter implements HttpClientFilter {

    private final JiraAuthService authService;

    @Inject
    public JiraAuthFilter(JiraAuthService authService) {
        this.authService = authService;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request,
                                                          ClientFilterChain chain) {
        String email = authService.getSavedEmail();
        String token = authService.getSavedToken();
        if (email != null && !email.isBlank() && token != null && !token.isBlank()) {
            String encoded = Base64.getEncoder().encodeToString(
                (email + ":" + token).getBytes(StandardCharsets.UTF_8));
            request.header("Authorization", "Basic " + encoded);
        }
        request.header("Accept", "application/json");
        return chain.proceed(request);
    }
}
