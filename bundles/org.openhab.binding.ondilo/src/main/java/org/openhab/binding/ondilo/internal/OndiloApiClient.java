/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ondilo.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OndiloApiClient} for accessing the Ondilo API using OAuth2 authentication.
 * handlers.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloApiClient {
    private final Logger logger = LoggerFactory.getLogger(OndiloApiClient.class);
    private @Nullable OAuthClientService oAuthService;
    private @Nullable String bearer;
    private @Nullable AccessTokenResponse accessTokenResponse;
    private static final String ONDILO_API_URL = "https://interop.ondilo.com/api/customer/v1";

    public OndiloApiClient(OAuthClientService oAuthService, AccessTokenResponse accessTokenResponse) {
        this.oAuthService = oAuthService;
        this.accessTokenResponse = accessTokenResponse;
        this.bearer = accessTokenResponse.getAccessToken();
        logger.trace("OndiloApiClient initialized with OAuth2 service and bearer token");
    }

    @Nullable
    public synchronized String get(String endpoint) {
        try {
            refreshAccessTokenIfNeeded();
            URL url = new URI(ONDILO_API_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + bearer);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is, "UTF-8")) {
                    return scanner.useDelimiter("\\A").next();
                }
            } else {
                logger.warn("Ondilo API request failed with code: {}", responseCode);
            }
        } catch (InterruptedIOException e) {
            logger.debug("Ondilo API request interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException | URISyntaxException e) {
            logger.warn("Ondilo API request error", e);
        }
        return null;
    }

    @Nullable
    public synchronized String put(String endpoint) {
        try {
            refreshAccessTokenIfNeeded();
            URL url = new URI(ONDILO_API_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Authorization", "Bearer " + bearer);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is, "UTF-8")) {
                    return scanner.useDelimiter("\\A").next();
                }
            } else {
                logger.warn("Ondilo API request failed with code: {}", responseCode);
            }
        } catch (InterruptedIOException e) {
            logger.debug("Ondilo API request interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException | URISyntaxException e) {
            logger.warn("Ondilo API request error", e);
        }
        return null;
    }

    private void refreshAccessTokenIfNeeded() {
        OAuthClientService oAuthService = this.oAuthService;
        AccessTokenResponse accessTokenResponse = this.accessTokenResponse;
        if (oAuthService != null && accessTokenResponse != null) {
            if (accessTokenResponse.isExpired(Instant.now(), 120)) {
                try {
                    this.accessTokenResponse = oAuthService.refreshToken();
                    accessTokenResponse = this.accessTokenResponse;
                    if (accessTokenResponse != null) {
                        this.bearer = accessTokenResponse.getAccessToken();
                        logger.trace("AccessToken renewed: {}", bearer);
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("OAuth token refresh interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (OAuthException | OAuthResponseException | IOException e) {
                    logger.warn("Failed to refresh OAuth token for Ondilo API", e);
                }
            }
        }
    }
}
