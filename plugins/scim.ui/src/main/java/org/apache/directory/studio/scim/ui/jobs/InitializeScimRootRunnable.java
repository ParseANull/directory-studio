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
import org.apache.directory.studio.scim.core.model.ScimResourceType;
import org.apache.directory.studio.scim.ui.model.ScimBrowserConnection;
import org.apache.directory.studio.scim.ui.model.ScimResourceTypeEntry;
import org.apache.directory.studio.scim.ui.model.ScimRootEntry;


/**
 * Fetches the SCIM resource types from the server and populates the root entry's children.
 * Equivalent to InitializeRootDSERunnable for LDAP connections.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitializeScimRootRunnable implements StudioConnectionBulkRunnableWithProgress
{
    private final ScimBrowserConnection browserConnection;


    public InitializeScimRootRunnable( ScimBrowserConnection browserConnection )
    {
        this.browserConnection = browserConnection;
    }


    @Override
    public String getName()
    {
        return "Initializing SCIM connection"; //$NON-NLS-1$
    }


    @Override
    public Connection[] getConnections()
    {
        return new Connection[]{ browserConnection.getConnection() };
    }


    @Override
    public Object[] getLockedObjects()
    {
        return new Object[]{ browserConnection.getConnection() };
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
            List<ScimResourceType> resourceTypes = adapter.getResourceTypes();
            ScimRootEntry root = ( ScimRootEntry ) browserConnection.getRootDSE();
            for ( ScimResourceType rt : resourceTypes )
            {
                ScimResourceTypeEntry entry = new ScimResourceTypeEntry( browserConnection, root, rt );
                entry.setTopPageChildrenRunnable( new LoadScimResourceTypeRunnable( browserConnection, entry ) );
                root.addChild( entry );
                browserConnection.cacheEntry( entry );
            }
            root.setChildrenInitialized( true );
        }
        catch ( ScimException e )
        {
            monitor.reportError( e );
        }
    }


    @Override
    public void runNotification( StudioProgressMonitor monitor )
    {
        ScimRootEntry root = ( ScimRootEntry ) browserConnection.getRootDSE();
        if ( root.isChildrenInitialized() )
        {
            EventRegistry.fireEntryUpdated( new ChildrenInitializedEvent( root ), this );
        }
    }


    @Override
    public String getErrorMessage()
    {
        return "Failed to initialize SCIM connection"; //$NON-NLS-1$
    }
}
