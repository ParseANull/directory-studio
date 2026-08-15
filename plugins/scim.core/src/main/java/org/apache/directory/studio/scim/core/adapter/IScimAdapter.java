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


public interface IScimAdapter
{

    ScimListResponse<ScimResource> listResources( String resourceType, ScimFilter filter, int startIndex, int count )
        throws ScimException;

    ScimResource getResource( String resourceType, String id ) throws ScimException;

    List<ScimResourceType> getResourceTypes() throws ScimException;

    List<ScimSchema> getSchemas() throws ScimException;

    ScimServiceProviderConfig getServiceProviderConfig() throws ScimException;

    ScimResource createResource( String resourceType, ScimResource resource ) throws ScimException;

    ScimResource replaceResource( String resourceType, String id, ScimResource resource ) throws ScimException;

    ScimResource patchResource( String resourceType, String id, ScimPatchOp patchOp ) throws ScimException;

    void deleteResource( String resourceType, String id ) throws ScimException;

    BulkResponse bulk( BulkRequest request ) throws ScimException;

    ProviderCapabilityProfile getCapabilityProfile();

    void setCapabilityProfile( ProviderCapabilityProfile profile );

}
