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
package org.apache.directory.studio.scim.client.http;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.directory.studio.scim.client.auth.IScimAuthStrategy;
import org.apache.directory.studio.scim.core.exception.ScimAuthException;
import org.apache.directory.studio.scim.core.exception.ScimException;


/**
 * JDK {@link HttpClient}-backed implementation of {@link IScimHttpClient}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ScimHttpClient implements IScimHttpClient
{
    private static final String SCIM_MEDIA_TYPE = "application/scim+json";

    private final String baseUrl;
    private final IScimAuthStrategy authStrategy;
    private final HttpClient client;
    private final ObjectMapper mapper;


    public ScimHttpClient( String baseUrl, IScimAuthStrategy authStrategy )
    {
        this.baseUrl = baseUrl;
        this.authStrategy = authStrategy;
        this.client = HttpClient.newBuilder().build();
        this.mapper = new ObjectMapper()
            .configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
    }


    @Override
    public <T> T get( String path, Map<String, String> queryParams, Class<T> responseType ) throws ScimException
    {
        StringBuilder urlBuilder = new StringBuilder( baseUrl ).append( path );
        if ( queryParams != null && !queryParams.isEmpty() )
        {
            urlBuilder.append( "?" );
            List<String> parts = new ArrayList<>();
            for ( Map.Entry<String, String> entry : queryParams.entrySet() )
            {
                parts.add( URLEncoder.encode( entry.getKey(), StandardCharsets.UTF_8 )
                    + "=" + URLEncoder.encode( entry.getValue(), StandardCharsets.UTF_8 ) );
            }
            urlBuilder.append( String.join( "&", parts ) );
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri( URI.create( urlBuilder.toString() ) )
            .header( "Accept", SCIM_MEDIA_TYPE )
            .GET();
        return execute( builder, responseType );
    }


    @Override
    public <T> T post( String path, Object body, Class<T> responseType ) throws ScimException
    {
        String json = serialize( body );
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri( URI.create( baseUrl + path ) )
            .header( "Content-Type", SCIM_MEDIA_TYPE )
            .header( "Accept", SCIM_MEDIA_TYPE )
            .POST( HttpRequest.BodyPublishers.ofString( json, StandardCharsets.UTF_8 ) );
        return execute( builder, responseType );
    }


    @Override
    public <T> T put( String path, Object body, Class<T> responseType ) throws ScimException
    {
        String json = serialize( body );
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri( URI.create( baseUrl + path ) )
            .header( "Content-Type", SCIM_MEDIA_TYPE )
            .header( "Accept", SCIM_MEDIA_TYPE )
            .PUT( HttpRequest.BodyPublishers.ofString( json, StandardCharsets.UTF_8 ) );
        return execute( builder, responseType );
    }


    @Override
    public <T> T patch( String path, Object body, Class<T> responseType ) throws ScimException
    {
        String json = serialize( body );
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri( URI.create( baseUrl + path ) )
            .header( "Content-Type", SCIM_MEDIA_TYPE )
            .header( "Accept", SCIM_MEDIA_TYPE )
            .method( "PATCH", HttpRequest.BodyPublishers.ofString( json, StandardCharsets.UTF_8 ) );
        return execute( builder, responseType );
    }


    @Override
    public void delete( String path ) throws ScimException
    {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri( URI.create( baseUrl + path ) )
            .header( "Accept", SCIM_MEDIA_TYPE )
            .DELETE();
        execute( builder, null );
    }


    private <T> T execute( HttpRequest.Builder builder, Class<T> responseType ) throws ScimException
    {
        try
        {
            authStrategy.applyAuth( builder );
        }
        catch ( ScimAuthException e )
        {
            throw new ScimException( e.getMessage(), 0 );
        }

        HttpRequest request = builder.build();
        try
        {
            HttpResponse<String> response = client.send( request, HttpResponse.BodyHandlers.ofString() );
            int statusCode = response.statusCode();
            if ( statusCode < 200 || statusCode >= 300 )
            {
                throw new ScimException( response.body(), statusCode );
            }
            if ( responseType == null )
            {
                return null;
            }
            String body = response.body();
            if ( body == null || body.isEmpty() )
            {
                return null;
            }
            return mapper.readValue( body, responseType );
        }
        catch ( ScimException e )
        {
            throw e;
        }
        catch ( IOException e )
        {
            throw new ScimException( e.getMessage(), 0 );
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            throw new ScimException( e.getMessage(), 0 );
        }
    }


    private String serialize( Object body ) throws ScimException
    {
        try
        {
            return mapper.writeValueAsString( body );
        }
        catch ( IOException e )
        {
            throw new ScimException( e.getMessage(), 0 );
        }
    }
}
