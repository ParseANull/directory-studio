/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.studio.scim.client.auth;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.directory.studio.scim.core.exception.ScimAuthException;


/**
 * Auth strategy that obtains a Bearer token via the OAuth 2.0 client_credentials grant and
 * re-fetches it on demand when {@link #refresh()} is called.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OAuth2ClientCredentialsAuthStrategy implements IScimAuthStrategy
{
    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    private String currentToken;


    public OAuth2ClientCredentialsAuthStrategy( String tokenEndpoint, String clientId,
        String clientSecret, String scope )
    {
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }


    @Override
    public void applyAuth( java.net.http.HttpRequest.Builder builder ) throws ScimAuthException
    {
        ensureValidToken();
        builder.header( "Authorization", "Bearer " + currentToken );
    }


    @Override
    public void refresh() throws ScimAuthException
    {
        fetchToken();
    }


    private void ensureValidToken() throws ScimAuthException
    {
        if ( currentToken == null )
        {
            fetchToken();
        }
    }


    private void fetchToken() throws ScimAuthException
    {
        try
        {
            String formBody = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode( clientId, StandardCharsets.UTF_8 )
                + "&client_secret=" + URLEncoder.encode( clientSecret, StandardCharsets.UTF_8 )
                + "&scope=" + URLEncoder.encode( scope != null ? scope : "", StandardCharsets.UTF_8 );

            HttpRequest request = HttpRequest.newBuilder()
                .uri( URI.create( tokenEndpoint ) )
                .header( "Content-Type", "application/x-www-form-urlencoded" )
                .POST( HttpRequest.BodyPublishers.ofString( formBody ) )
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send( request, HttpResponse.BodyHandlers.ofString() );

            if ( response.statusCode() < 200 || response.statusCode() >= 300 )
            {
                throw new ScimAuthException(
                    "Token endpoint returned " + response.statusCode() + ": " + response.body() );
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree( response.body() );
            JsonNode tokenNode = node.get( "access_token" );
            if ( tokenNode == null || tokenNode.asText().isEmpty() )
            {
                throw new ScimAuthException( "No access_token in token endpoint response" );
            }
            currentToken = tokenNode.asText();
        }
        catch ( ScimAuthException e )
        {
            throw e;
        }
        catch ( IOException e )
        {
            throw new ScimAuthException( "Failed to fetch OAuth2 token: " + e.getMessage() );
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            throw new ScimAuthException( "Interrupted while fetching OAuth2 token" );
        }
    }
}
