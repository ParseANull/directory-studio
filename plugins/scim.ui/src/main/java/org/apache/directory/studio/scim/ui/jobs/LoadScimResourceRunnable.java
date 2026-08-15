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
package org.apache.directory.studio.scim.ui.jobs;


import java.util.Map;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.scim.core.adapter.IScimAdapter;
import org.apache.directory.studio.scim.core.exception.ScimException;
import org.apache.directory.studio.scim.core.model.ScimResource;
import org.apache.directory.studio.scim.ui.model.ScimAttributeEntry;
import org.apache.directory.studio.scim.ui.model.ScimBrowserConnection;
import org.apache.directory.studio.scim.ui.model.ScimResourceEntry;
import org.apache.directory.studio.scim.ui.model.ScimResourceTypeEntry;


/**
 * Fetches the full detail of a single SCIM resource and populates its attributes.
 * Equivalent to InitializeAttributesRunnable for LDAP connections.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LoadScimResourceRunnable implements StudioConnectionBulkRunnableWithProgress
{
    private final ScimBrowserConnection browserConnection;
    private final ScimResourceEntry resourceEntry;


    public LoadScimResourceRunnable( ScimBrowserConnection browserConnection, ScimResourceEntry resourceEntry )
    {
        this.browserConnection = browserConnection;
        this.resourceEntry = resourceEntry;
    }


    @Override
    public String getName()
    {
        return "Loading SCIM resource attributes"; //$NON-NLS-1$
    }


    @Override
    public Connection[] getConnections()
    {
        return new Connection[]{ browserConnection.getConnection() };
    }


    @Override
    public void run( StudioProgressMonitor monitor )
    {
        IScimAdapter adapter = browserConnection.getAdapter();
        if ( adapter == null )
        {
            return;
        }
        try
        {
            ScimResourceTypeEntry typeEntry = ( ScimResourceTypeEntry ) resourceEntry.getParententry();
            String resourceTypeName = typeEntry.getResourceType().getName();
            String id = resourceEntry.getResource().getId();
            ScimResource full = adapter.getResource( resourceTypeName, id );
            if ( full != null && full.getAttributes() != null )
            {
                for ( Map.Entry<String, Object> attr : full.getAttributes().entrySet() )
                {
                    ScimAttributeEntry attribute = new ScimAttributeEntry( attr.getKey(), resourceEntry );
                    resourceEntry.addAttribute( attribute );
                }
            }
            resourceEntry.setAttributesInitialized( true );
        }
        catch ( ScimException e )
        {
            monitor.reportError( e );
        }
    }


    @Override
    public void runNotification( StudioProgressMonitor monitor )
    {
        if ( resourceEntry.isAttributesInitialized() )
        {
            EventRegistry.fireEntryUpdated( new AttributesInitializedEvent( resourceEntry ), this );
        }
    }


    @Override
    public String getErrorMessage()
    {
        return "Failed to load SCIM resource attributes"; //$NON-NLS-1$
    }
}
