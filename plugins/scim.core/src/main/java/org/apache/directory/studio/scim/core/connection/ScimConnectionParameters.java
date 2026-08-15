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
package org.apache.directory.studio.scim.core.connection;


import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.scim.core.model.ProviderCapabilityProfile;
import org.apache.directory.studio.scim.core.model.ScimVersion;


// Requires org.apache.directory.studio.connection.core in Require-Bundle (see MANIFEST.MF).
// scim.core -> connection.core is safe directionally; connection.core has no SCIM dependency.
public class ScimConnectionParameters implements IScimConnectionParameters
{

    private static final String KEY_BASE_URL = "scim.baseUrl";
    private static final String KEY_VERSION = "scim.version";
    private static final String KEY_PROVIDER_ID = "scim.providerId";
    private static final String KEY_AUTH_METHOD = "scim.authMethod";
    private static final String KEY_BEARER_TOKEN = "scim.bearerToken";
    private static final String KEY_OAUTH_TOKEN_ENDPOINT = "scim.oauthTokenEndpoint";
    private static final String KEY_OAUTH_CLIENT_ID = "scim.oauthClientId";
    private static final String KEY_OAUTH_CLIENT_SECRET = "scim.oauthClientSecret";
    private static final String KEY_OAUTH_SCOPE = "scim.oauthScope";
    private static final String KEY_BASIC_USERNAME = "scim.basicUsername";
    private static final String KEY_BASIC_PASSWORD = "scim.basicPassword";

    private final ConnectionParameter connectionParameter;


    public ScimConnectionParameters( ConnectionParameter connectionParameter )
    {
        this.connectionParameter = connectionParameter;
    }


    @Override
    public String getBaseUrl()
    {
        return connectionParameter.getExtendedProperty( KEY_BASE_URL );
    }


    @Override
    public ScimVersion getScimVersion()
    {
        String value = connectionParameter.getExtendedProperty( KEY_VERSION );
        if ( value == null )
        {
            return ScimVersion.V2_0;
        }
        try
        {
            return ScimVersion.valueOf( value );
        }
        catch ( IllegalArgumentException e )
        {
            return ScimVersion.V2_0;
        }
    }


    @Override
    public String getProviderId()
    {
        return connectionParameter.getExtendedProperty( KEY_PROVIDER_ID );
    }


    @Override
    public String getAuthMethod()
    {
        return connectionParameter.getExtendedProperty( KEY_AUTH_METHOD );
    }


    @Override
    public String getBearerToken()
    {
        return connectionParameter.getExtendedProperty( KEY_BEARER_TOKEN );
    }


    @Override
    public String getOAuthTokenEndpoint()
    {
        return connectionParameter.getExtendedProperty( KEY_OAUTH_TOKEN_ENDPOINT );
    }


    @Override
    public String getOAuthClientId()
    {
        return connectionParameter.getExtendedProperty( KEY_OAUTH_CLIENT_ID );
    }


    @Override
    public String getOAuthClientSecret()
    {
        return connectionParameter.getExtendedProperty( KEY_OAUTH_CLIENT_SECRET );
    }


    @Override
    public String getOAuthScope()
    {
        return connectionParameter.getExtendedProperty( KEY_OAUTH_SCOPE );
    }


    @Override
    public String getBasicUsername()
    {
        return connectionParameter.getExtendedProperty( KEY_BASIC_USERNAME );
    }


    @Override
    public String getBasicPassword()
    {
        return connectionParameter.getExtendedProperty( KEY_BASIC_PASSWORD );
    }


    @Override
    public String getConnectionId()
    {
        return connectionParameter.getId();
    }


    // TODO: deserialize persisted JSON from extended properties when scim.client's Jackson is available.
    @Override
    public ProviderCapabilityProfile getCapabilityProfile()
    {
        return new ProviderCapabilityProfile(
            getScimVersion(),
            true,   // supportsDiscovery — assume true until probed
            false,  // supportsBulk
            false,  // bulkVendorSet
            null,   // membershipModel
            false,  // membershipModelVendorSet
            null,   // paginationStyle
            false,  // supportsFilter
            false,  // supportsSorting
            false,  // supportsETag
            null    // vendorExtensions
        );
    }

}
