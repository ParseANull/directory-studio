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


import java.util.List;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.scim.core.adapter.IScimAdapter;
import org.apache.directory.studio.scim.core.exception.ScimException;
import org.apache.directory.studio.scim.core.model.ScimListResponse;
import org.apache.directory.studio.scim.core.model.ScimResource;
import org.apache.directory.studio.scim.ui.model.ScimBrowserConnection;
import org.apache.directory.studio.scim.ui.model.ScimResourceEntry;
import org.apache.directory.studio.scim.ui.model.ScimResourceTypeEntry;


/**
 * Fetches SCIM resources for a single resource type and populates the type entry's children.
 * Equivalent to InitializeChildrenRunnable for LDAP connections.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LoadScimResourceTypeRunnable implements StudioConnectionBulkRunnableWithProgress
{
    private static final int PAGE_SIZE = 100;

    private final ScimBrowserConnection browserConnection;
    private final ScimResourceTypeEntry typeEntry;


    public LoadScimResourceTypeRunnable( ScimBrowserConnection browserConnection, ScimResourceTypeEntry typeEntry )
    {
        this.browserConnection = browserConnection;
        this.typeEntry = typeEntry;
    }


    @Override
    public String getName()
    {
        return "Loading SCIM resources"; //$NON-NLS-1$
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
            String resourceTypeName = typeEntry.getResourceType().getName();
            ScimListResponse<ScimResource> response = adapter.listResources(
                resourceTypeName, null, 1, PAGE_SIZE );
            if ( response != null && response.getResources() != null )
            {
                for ( ScimResource resource : response.getResources() )
                {
                    ScimResourceEntry entry = new ScimResourceEntry( browserConnection, typeEntry, resource );
                    entry.setTopPageChildrenRunnable( null );
                    typeEntry.addChild( entry );
                    browserConnection.cacheEntry( entry );
                }
            }
            typeEntry.setChildrenInitialized( true );
            typeEntry.setTopPageChildrenRunnable( null );
        }
        catch ( ScimException e )
        {
            monitor.reportError( e );
        }
    }


    @Override
    public void runNotification( StudioProgressMonitor monitor )
    {
        if ( typeEntry.isChildrenInitialized() )
        {
            EventRegistry.fireEntryUpdated( new ChildrenInitializedEvent( typeEntry ), this );
        }
    }


    @Override
    public String getErrorMessage()
    {
        return "Failed to load SCIM resources for " + typeEntry.getResourceType().getName(); //$NON-NLS-1$
    }
}
