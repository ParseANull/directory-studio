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
package org.apache.directory.studio.scim.provider.onelogin;


import java.util.Collections;
import java.util.List;

import org.apache.directory.studio.scim.core.adapter.IScimAdapter;
import org.apache.directory.studio.scim.core.adapter.IScimAdapterFactory;
import org.apache.directory.studio.scim.core.connection.IScimConnectionParameters;
import org.apache.directory.studio.scim.core.model.PaginationStyle;
import org.apache.directory.studio.scim.core.model.ProviderCapabilityProfile;
import org.apache.directory.studio.scim.core.model.ScimSchema;
import org.apache.directory.studio.scim.core.model.ScimVersion;
import org.apache.directory.studio.scim.core.model.UserGroupMembershipModel;


public class OneLoginScimAdapterFactory implements IScimAdapterFactory
{

    @Override
    public String getProviderId()
    {
        return "onelogin";
    }


    @Override
    public String getDisplayName()
    {
        return "OneLogin";
    }


    @Override
    public ScimVersion[] getSupportedVersions()
    {
        return new ScimVersion[]{ ScimVersion.V2_0 };
    }


    @Override
    public ProviderCapabilityProfile getDefaultProfile()
    {
        return new ProviderCapabilityProfile(
            ScimVersion.V2_0,
            true,
            false,
            true,
            UserGroupMembershipModel.USER_HAS_GROUPS,
            false,
            PaginationStyle.INDEX_BASED,
            true,
            false,
            false,
            Collections.emptyMap()
        );
    }


    @Override
    public List<ScimSchema> getBundledSchemas()
    {
        return Collections.emptyList();
    }


    @Override
    public IScimAdapter create( IScimConnectionParameters params )
    {
        return new OneLoginScim2Adapter( params );
    }

}
