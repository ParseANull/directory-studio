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
package org.apache.directory.studio.scim.core.adapter;


import java.util.List;

import org.apache.directory.studio.scim.core.exception.ScimException;
import org.apache.directory.studio.scim.core.exception.ScimNotImplementedException;
import org.apache.directory.studio.scim.core.model.BulkRequest;
import org.apache.directory.studio.scim.core.model.BulkResponse;
import org.apache.directory.studio.scim.core.model.ProviderCapabilityProfile;
import org.apache.directory.studio.scim.core.model.ScimFilter;
import org.apache.directory.studio.scim.core.model.ScimListResponse;
import org.apache.directory.studio.scim.core.model.ScimPatchOp;
import org.apache.directory.studio.scim.core.model.ScimResource;
import org.apache.directory.studio.scim.core.model.ScimResourceType;
import org.apache.directory.studio.scim.core.model.ScimSchema;
import org.apache.directory.studio.scim.core.model.ScimServiceProviderConfig;


public abstract class AbstractScimAdapter implements IScimAdapter
{

    protected ProviderCapabilityProfile capabilityProfile;


    @Override
    public ProviderCapabilityProfile getCapabilityProfile()
    {
        return capabilityProfile;
    }


    @Override
    public void setCapabilityProfile( ProviderCapabilityProfile profile )
    {
        this.capabilityProfile = profile;
    }


    @Override
    public abstract ScimListResponse<ScimResource> listResources( String resourceType, ScimFilter filter,
        int startIndex, int count ) throws ScimException;


    @Override
    public abstract ScimResource getResource( String resourceType, String id ) throws ScimException;


    @Override
    public abstract List<ScimResourceType> getResourceTypes() throws ScimException;


    @Override
    public abstract List<ScimSchema> getSchemas() throws ScimException;


    @Override
    public abstract ScimServiceProviderConfig getServiceProviderConfig() throws ScimException;


    @Override
    public ScimResource createResource( String resourceType, ScimResource resource ) throws ScimException
    {
        throw new ScimNotImplementedException( "createResource" );
    }


    @Override
    public ScimResource replaceResource( String resourceType, String id, ScimResource resource ) throws ScimException
    {
        throw new ScimNotImplementedException( "replaceResource" );
    }


    @Override
    public ScimResource patchResource( String resourceType, String id, ScimPatchOp patchOp ) throws ScimException
    {
        throw new ScimNotImplementedException( "patchResource" );
    }


    @Override
    public void deleteResource( String resourceType, String id ) throws ScimException
    {
        throw new ScimNotImplementedException( "deleteResource" );
    }


    @Override
    public BulkResponse bulk( BulkRequest request ) throws ScimException
    {
        throw new ScimNotImplementedException( "bulk" );
    }


    protected abstract ProviderCapabilityProfile discoverCapabilities();

}
