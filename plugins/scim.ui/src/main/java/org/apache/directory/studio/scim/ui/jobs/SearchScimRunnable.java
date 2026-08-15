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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.scim.core.adapter.IScimAdapter;
import org.apache.directory.studio.scim.core.exception.ScimException;
import org.apache.directory.studio.scim.core.model.ScimFilter;
import org.apache.directory.studio.scim.core.model.ScimListResponse;
import org.apache.directory.studio.scim.core.model.ScimResource;
import org.apache.directory.studio.scim.ui.model.ScimBrowserConnection;


/**
 * Executes a SCIM filter search across all resource types.
 * Phase 1: results are stored in memory only; no LDAP SearchResult wiring.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchScimRunnable implements StudioConnectionBulkRunnableWithProgress
{
    private static final int PAGE_SIZE = 100;

    private final ScimBrowserConnection browserConnection;
    private final String resourceType;
    private final ScimFilter filter;
    private final List<ScimResource> results = new ArrayList<>();


    public SearchScimRunnable( ScimBrowserConnection browserConnection, String resourceType, ScimFilter filter )
    {
        this.browserConnection = browserConnection;
        this.resourceType = resourceType;
        this.filter = filter;
    }


    @Override
    public String getName()
    {
        return "Searching SCIM resources"; //$NON-NLS-1$
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
            ScimListResponse<ScimResource> response = adapter.listResources(
                resourceType, filter, 1, PAGE_SIZE );
            if ( response != null && response.getResources() != null )
            {
                results.addAll( response.getResources() );
            }
        }
        catch ( ScimException e )
        {
            monitor.reportError( e );
        }
    }


    @Override
    public void runNotification( StudioProgressMonitor monitor )
    {
        // Phase 1: no UI notification — callers access results via getResults()
    }


    @Override
    public String getErrorMessage()
    {
        return "Failed to search SCIM resources"; //$NON-NLS-1$
    }


    public List<ScimResource> getResults()
    {
        return results;
    }
}
