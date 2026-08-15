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
package org.apache.directory.studio.scim.client.adapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.directory.studio.scim.client.auth.IScimAuthStrategy;
import org.apache.directory.studio.scim.client.auth.ScimAuthStrategyFactory;
import org.apache.directory.studio.scim.client.http.IScimHttpClient;
import org.apache.directory.studio.scim.client.http.ScimHttpClient;
import org.apache.directory.studio.scim.core.adapter.AbstractScimAdapter;
import org.apache.directory.studio.scim.core.connection.IScimConnectionParameters;
import org.apache.directory.studio.scim.core.exception.ScimException;
import org.apache.directory.studio.scim.core.model.PaginationStyle;
import org.apache.directory.studio.scim.core.model.ProviderCapabilityProfile;
import org.apache.directory.studio.scim.core.model.ScimFilter;
import org.apache.directory.studio.scim.core.model.ScimVersion;
import org.apache.directory.studio.scim.core.model.UserGroupMembershipModel;
import org.apache.directory.studio.scim.core.model.ScimListResponse;
import org.apache.directory.studio.scim.core.model.ScimResource;
import org.apache.directory.studio.scim.core.model.ScimResource.ScimMeta;
import org.apache.directory.studio.scim.core.model.ScimResourceType;
import org.apache.directory.studio.scim.core.model.ScimSchema;
import org.apache.directory.studio.scim.core.model.ScimServiceProviderConfig;


/**
 * Full SCIM 2.0 HTTP read implementation. Provider bundles extend this class instead of
 * implementing transport themselves.
 *
 * <p>Write operations ({@code createResource}, {@code replaceResource}, {@code patchResource},
 * {@code deleteResource}, {@code bulk}) remain as inherited from {@link AbstractScimAdapter};
 * subclasses override individual write methods as needed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractScim2Adapter extends AbstractScimAdapter
{
    protected final IScimHttpClient httpClient;

    private final ObjectMapper mapper = new ObjectMapper()
        .configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );


    protected AbstractScim2Adapter( IScimConnectionParameters params )
    {
        IScimAuthStrategy strategy = ScimAuthStrategyFactory.create( params );
        this.httpClient = new ScimHttpClient( params.getBaseUrl(), strategy );
    }


    @Override
    public ScimListResponse<ScimResource> listResources( String resourceType, ScimFilter filter,
        int startIndex, int count ) throws ScimException
    {
        Map<String, String> params = new HashMap<>();
        params.put( "startIndex", String.valueOf( startIndex ) );
        params.put( "count", String.valueOf( count ) );
        if ( filter != null )
        {
            String expr = filter.getFilterExpression();
            if ( expr != null && !expr.isEmpty() )
            {
                params.put( "filter", expr );
            }
        }

        JsonNode root = httpClient.get( resourceTypeToPath( resourceType ), params, JsonNode.class );

        int totalResults = root.path( "totalResults" ).asInt( 0 );
        int respStartIndex = root.path( "startIndex" ).asInt( startIndex );
        int itemsPerPage = root.path( "itemsPerPage" ).asInt( count );

        List<ScimResource> resources = new ArrayList<>();
        JsonNode resourcesNode = root.path( "Resources" );
        if ( resourcesNode.isArray() )
        {
            for ( JsonNode node : resourcesNode )
            {
                resources.add( parseResource( node ) );
            }
        }

        ScimListResponse<ScimResource> response = new ScimListResponse<>();
        response.setTotalResults( totalResults );
        response.setStartIndex( respStartIndex );
        response.setItemsPerPage( itemsPerPage );
        response.setResources( resources );

        return response;
    }


    @Override
    public ScimResource getResource( String resourceType, String id ) throws ScimException
    {
        JsonNode node = httpClient.get( resourceTypeToPath( resourceType ) + "/" + id, null, JsonNode.class );
        return parseResource( node );
    }


    @Override
    public List<ScimResourceType> getResourceTypes() throws ScimException
    {
        JsonNode root = httpClient.get( "/ResourceTypes", null, JsonNode.class );
        List<ScimResourceType> result = new ArrayList<>();
        JsonNode resources = root.path( "Resources" );
        if ( resources.isArray() )
        {
            for ( JsonNode node : resources )
            {
                try
                {
                    result.add( mapper.treeToValue( node, ScimResourceType.class ) );
                }
                catch ( JsonProcessingException e )
                {
                    throw new ScimException( e.getMessage(), 0 );
                }
            }
        }
        return result;
    }


    @Override
    public List<ScimSchema> getSchemas() throws ScimException
    {
        JsonNode root = httpClient.get( "/Schemas", null, JsonNode.class );
        List<ScimSchema> result = new ArrayList<>();
        JsonNode resources = root.path( "Resources" );
        if ( resources.isArray() )
        {
            for ( JsonNode node : resources )
            {
                try
                {
                    result.add( mapper.treeToValue( node, ScimSchema.class ) );
                }
                catch ( JsonProcessingException e )
                {
                    throw new ScimException( e.getMessage(), 0 );
                }
            }
        }
        return result;
    }


    @Override
    public ScimServiceProviderConfig getServiceProviderConfig() throws ScimException
    {
        return httpClient.get( "/ServiceProviderConfig", null, ScimServiceProviderConfig.class );
    }


    @Override
    public ProviderCapabilityProfile discoverCapabilities()
    {
        try
        {
            ScimServiceProviderConfig config = getServiceProviderConfig();
            return new ProviderCapabilityProfile(
                ScimVersion.V2_0,
                true,
                config.isBulkSupported(),
                false,
                UserGroupMembershipModel.GROUP_HAS_MEMBERS,
                false,
                PaginationStyle.INDEX_BASED,
                config.isFilterSupported(),
                config.isSortSupported(),
                config.isEtagSupported(),
                java.util.Collections.emptyMap()
            );
        }
        catch ( Exception e )
        {
            return new ProviderCapabilityProfile(
                ScimVersion.V2_0, true, false, false,
                UserGroupMembershipModel.GROUP_HAS_MEMBERS, false,
                PaginationStyle.INDEX_BASED, false, false, false,
                java.util.Collections.emptyMap()
            );
        }
    }


    private ScimResource parseResource( JsonNode node )
    {
        ScimResource resource = new ScimResource();
        resource.setId( node.path( "id" ).asText( null ) );
        resource.setExternalId( node.path( "externalId" ).asText( null ) );

        List<String> schemas = new ArrayList<>();
        JsonNode schemasNode = node.path( "schemas" );
        if ( schemasNode.isArray() )
        {
            for ( JsonNode s : schemasNode )
            {
                schemas.add( s.asText() );
            }
        }
        resource.setSchemas( schemas );

        JsonNode metaNode = node.path( "meta" );
        if ( !metaNode.isMissingNode() )
        {
            ScimMeta meta = new ScimMeta();
            meta.setResourceType( metaNode.path( "resourceType" ).asText( null ) );
            meta.setCreated( metaNode.path( "created" ).asText( null ) );
            meta.setLastModified( metaNode.path( "lastModified" ).asText( null ) );
            meta.setLocation( metaNode.path( "location" ).asText( null ) );
            meta.setVersion( metaNode.path( "version" ).asText( null ) );
            resource.setMeta( meta );
        }

        Map<String, Object> attributes = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while ( fields.hasNext() )
        {
            Map.Entry<String, JsonNode> field = fields.next();
            String name = field.getKey();
            if ( !"id".equals( name ) && !"externalId".equals( name )
                && !"schemas".equals( name ) && !"meta".equals( name ) )
            {
                attributes.put( name, field.getValue() );
            }
        }
        resource.setAttributes( attributes );

        return resource;
    }


    private String resourceTypeToPath( String resourceType )
    {
        if ( resourceType != null && resourceType.startsWith( "/" ) )
        {
            return resourceType;
        }
        return "/" + resourceType;
    }
}
