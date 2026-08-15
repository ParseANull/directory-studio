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

package org.apache.directory.studio.connection.ui;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


// ── CLASS: ConnectionParameterPageManager — R2-D2 READING THE PANEL BLUEPRINTS ──
// R2-D2 doesn't carry every upgrade pre-installed; he reads the ship's
// configuration manifest and assembles the right components for the mission.
// ConnectionParameterPageManager does the same thing with OSGi extension points:
// it queries the Eclipse extension registry for every contribution to the
// "connectionparameterpages" extension point, instantiates each page class, tags it
// with its metadata (id, name, description, dependency), and then sorts the pages
// into the correct dependency order using a topological comparator.
// The result is an array of ready-to-use ConnectionParameterPage instances in
// the order they should appear in the wizard.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static factory that discovers and instantiates all registered
 * {@link ConnectionParameterPage} contributions via the
 * {@code org.apache.directory.studio.connectionparameterpages} Eclipse extension point.
 *
 * <p>Pages are sorted topologically using each page's {@code dependsOnId} attribute
 * so that a page always appears after the page it depends on.</p>
 *
 * <p>This class is non-instantiable; use {@link #getConnectionParameterPages()} directly.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ConnectionParameterPageManager
{
    // ── PRIVATE CONSTRUCTOR — UTILITY CLASS ───────────────────────────────────────
    /**
     * Prevents instantiation.  All methods are static.
     */
    private ConnectionParameterPageManager()
    {
    }


    // ── GET CONNECTION PARAMETER PAGES — DISCOVER, INSTANTIATE, AND SORT ──────────
    // 1. Ask the Eclipse extension registry for every "connectionparameterpages"
    //    contribution.
    // 2. Instantiate each class, set its metadata from the extension element.
    // 3. Sort the array so that each page comes after the page it depends on.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all registered {@link ConnectionParameterPage} instances, sorted so
     * that dependency ordering is respected (a page always follows the page named
     * by its {@code dependsOnId}).
     *
     * <p>If a page fails to instantiate, an error is logged and that page is skipped.</p>
     *
     * @return  A topologically sorted array of {@link ConnectionParameterPage}s.
     */
    public static ConnectionParameterPage[] getConnectionParameterPages()
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint( "org.apache.directory.studio.connectionparameterpages" ); //$NON-NLS-1$
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();
        final Map<String, ConnectionParameterPage> pageMap = new ConcurrentHashMap<>();

        // ── DISCOVER — FOR EACH EXTENSION, INSTANTIATE AND TAG ────────────────────
        // Like R2 reading each panel's blueprint and initialising the hardware.
        // ──────────────────────────────────────────────────────────────────────────
        for ( IConfigurationElement member : members )
        {
            try
            {
                ConnectionParameterPage page = ( ConnectionParameterPage ) member.createExecutableExtension( "class" ); //$NON-NLS-1$
                page.setPageId( member.getAttribute( "id" ) ); //$NON-NLS-1$
                page.setPageName( member.getAttribute( "name" ) ); //$NON-NLS-1$
                page.setPageDescription( member.getAttribute( "description" ) ); //$NON-NLS-1$
                page.setPageDependsOnId( member.getAttribute( "dependsOnId" ) ); //$NON-NLS-1$
                pageMap.put( page.getPageId(), page );
            }
            catch ( Exception e )
            {
                ConnectionUIPlugin
                    .getDefault()
                    .getLog()
                    .log(
                        new Status(
                            IStatus.ERROR,
                            ConnectionUIConstants.PLUGIN_ID,
                            1,
                            Messages.getString( "ConnectionParameterPageManager.UnableCreateConnectionParamPage" ) //$NON-NLS-1$
                                + member.getAttribute( "class" ), //$NON-NLS-1$
                            e ) );
            }
        }

        final ConnectionParameterPage[] pages = pageMap.values().toArray( new ConnectionParameterPage[0] );

        // ── SORT — TOPOLOGICAL DEPENDENCY ORDER ───────────────────────────────────
        // We walk the dependsOnId chain of each page upward until we can determine
        // the relative ordering.  Pages with no dependency come first.
        // ──────────────────────────────────────────────────────────────────────────
        Comparator<? super ConnectionParameterPage> pageComparator =
            ( ConnectionParameterPage page1, ConnectionParameterPage page2 ) ->
            {
                String dependsOnId1 = page1.getPageDependsOnId();
                String dependsOnId2 = page2.getPageDependsOnId();

                do
                {
                    if ( ( dependsOnId1 == null ) && ( dependsOnId2 != null ) )
                    {
                        return -1;
                    }
                    else if ( ( dependsOnId2 == null ) && ( dependsOnId1 != null ) )
                    {
                        return 1;
                    }
                    else if ( ( dependsOnId1 != null ) && dependsOnId1.equals( page2.getPageId() ) )
                    {
                        return 1;
                    }
                    else if ( ( dependsOnId2 != null ) && dependsOnId2.equals( page1.getPageId() ) )
                    {
                        return -1;
                    }

                    ConnectionParameterPage page = pageMap.get( dependsOnId1 );

                    if ( ( page != null ) && !page.getPageDependsOnId().equals( dependsOnId1 ) )
                    {
                        dependsOnId1 = page.getPageDependsOnId();
                    }
                    else
                    {
                        dependsOnId1 = null;
                    }
                }
                while ( ( dependsOnId1 != null ) && !dependsOnId1.equals( page1.getPageId() ) );

                dependsOnId1 = page1.getPageDependsOnId();
                dependsOnId2 = page2.getPageDependsOnId();

                do
                {
                    if ( ( dependsOnId1 == null ) && ( dependsOnId2 != null ) )
                    {
                        return -1;
                    }
                    else if ( ( dependsOnId2 == null ) && ( dependsOnId1 != null ) )
                    {
                        return 1;
                    }
                    else if ( ( dependsOnId1 != null ) && dependsOnId1.equals( page2.getPageId() ) )
                    {
                        return 1;
                    }
                    else if ( ( dependsOnId2 != null ) && dependsOnId2.equals( page1.getPageId() ) )
                    {
                        return -1;
                    }

                    ConnectionParameterPage page = pageMap.get( dependsOnId2 );

                    if ( page == null )
                    {
                        dependsOnId2 = null;
                    }
                    else
                    {
                        dependsOnId2 = page.getPageDependsOnId();
                    }
                }
                while ( ( dependsOnId2 != null ) && !dependsOnId2.equals( page2.getPageId() ) );

                return 0;
            };

        Arrays.sort( pages, pageComparator );

        return pages;
    }
}
