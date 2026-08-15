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
package org.apache.directory.studio.scim.ui;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionParameter.ConnectionProtocol;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.scim.ui.model.ScimBrowserConnection;


public class ScimBrowserConnectionManager implements IConnectionListener
{
    private static final Map<String, ScimBrowserConnection> cache = new ConcurrentHashMap<>();


    public static ScimBrowserConnection get( Connection conn )
    {
        return cache.get( conn.getId() );
    }


    @Override
    public void connectionOpened( Connection connection, StudioProgressMonitor monitor )
    {
        if ( connection.getConnectionParameter().getConnectionProtocol() == ConnectionProtocol.SCIM )
        {
            cache.put( connection.getId(), new ScimBrowserConnection( connection ) );
        }
    }


    @Override
    public void connectionClosed( Connection connection, StudioProgressMonitor monitor )
    {
        cache.remove( connection.getId() );
    }
}
