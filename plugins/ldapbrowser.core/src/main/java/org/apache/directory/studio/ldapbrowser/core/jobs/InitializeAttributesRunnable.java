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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;


// ── CLASS: InitializeAttributesRunnable — CLONE TROOPERS LOAD TARGET BRIEFINGS ─
// The Empire's Clone Army has just received Order 66.  Before each clone
// executes his part of the order, Commander Cody issues a full mission briefing:
// target DN, location, special attributes like "jedi master" or "referral", and
// whether to pull the classified operational data too.  Each clone loads exactly
// the right briefing for his assigned target before acting.
// This runnable does the same: for each entry in the list, we figure out which
// set of attributes to request (user attrs, operational attrs, ref attr for
// referrals), clear any stale attributes, then fetch the fresh data via an LDAP
// OBJECT-scope search and mark the entry as "attributesInitialized".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that loads (or reloads) all attributes of one or more
 * LDAP entries.  When the user expands an entry in the browser tree, this job
 * runs to fetch the entry's attributes from the server.
 * We request user attributes ({@code *}) always, operational attributes
 * ({@code +}) when the connection or entry preference says so, and the {@code ref}
 * attribute for referral entries so we can follow them.
 * Afterwards we fire an {@link AttributesInitializedEvent} per entry so the
 * browser's attribute editor panel refreshes.
 * Think of it as Clone troopers loading their target briefings.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitializeAttributesRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The entries. */
    private IEntry[] entries;


    // ── Commander Cody Assigns Targets ────────────────────────────────────────
    // Accepts any number of entries (varargs).  Each entry gets its attributes
    // loaded during run().  Null entries in the list are silently skipped.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new InitializeAttributesRunnable for one or more entries.
     *
     * <p>For example:</p>
     * <pre>
     *   new StudioBrowserJob(new InitializeAttributesRunnable(entry)).execute();
     * </pre>
     *
     * @param entries the entries whose attributes should be loaded or refreshed.
     */
    public InitializeAttributesRunnable( IEntry... entries )
    {
        this.entries = entries;
    }


    // ── Each Clone Gets His Own Comms Channel ─────────────────────────────────
    // One connection per entry (entries may span multiple connections if the
    // browser has more than one server open simultaneously).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns one connection per entry, since different entries can belong to
     * different servers.
     *
     * @return array of {@link Connection} objects, one per non-null entry.
     */
    public Connection[] getConnections()
    {
        List<Connection> connections = new ArrayList<Connection>();

        for ( IEntry entry : entries )
        {
            if ( entry != null )
            {
                connections.add( entry.getBrowserConnection().getConnection() );
            }
        }

        return connections.toArray( new Connection[0] );
    }


    // ── The Job Title For The Progress Bar ────────────────────────────────────
    // "Initialising entry attributes..." in the Eclipse progress view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "initialise entry attributes" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__init_entries_title_attonly;
    }


    // ── Lock Each Target Entry During The Briefing ────────────────────────────
    // Two simultaneous attribute-load jobs on the same entry would conflict.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entries as the set of objects to lock during this job.
     *
     * @return the entries array, cast to {@link Object[]}.
     */
    public Object[] getLockedObjects()
    {
        Object[] lockedObjects = new Object[entries.length];

        System.arraycopy( entries, 0, lockedObjects, 0, entries.length );

        return lockedObjects;
    }


    // ── If The Briefing Fails ─────────────────────────────────────────────────
    // Different messages for single-entry vs multi-entry failures.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown when attribute initialisation fails.
     * Uses a singular message for one entry and a plural message for many.
     *
     * @return a localised error string.
     */
    public String getErrorMessage()
    {
        if ( entries.length == 1 )
        {
            return BrowserCoreMessages.jobs__init_entries_error_1;
        }
        else
        {
            return BrowserCoreMessages.jobs__init_entries_error_n;
        }
    }


    // ── The Clones Load Their Briefings One By One ────────────────────────────
    // Loops through the entry list, reporting progress per entry.  Skips null
    // entries and entries whose connection is gone (disconnected mid-operation).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loops over the entries and calls {@link #initializeAttributes(IEntry, StudioProgressMonitor)}
     * for each one.  Cancelled jobs break out of the loop early.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( " ", entries.length + 2 ); //$NON-NLS-1$
        monitor.reportProgress( " " ); //$NON-NLS-1$

        for ( IEntry entry : entries )
        {
            if ( monitor.isCanceled() )
            {
                break;
            }

            if ( entry != null )
            {
                monitor.setTaskName( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__init_entries_task,
                    new String[]
                        { entry.getDn().getName() } ) );
                monitor.worked( 1 );

                if ( entry.getBrowserConnection() != null )
                {
                    initializeAttributes( entry, monitor );
                }
            }
        }
    }


    // ── The Clones Report Back To Commander Cody ──────────────────────────────
    // Fires an AttributesInitializedEvent per entry.  Uses the cached version
    // of the entry (not the stale reference) so the UI gets the latest copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires one {@link AttributesInitializedEvent} per successfully initialized
     * entry.  Looks up the entry from the browser cache first to ensure the
     * event carries the most up-to-date entry reference.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        for ( IEntry entry : entries )
        {
            if ( ( entry != null ) && ( entry.getBrowserConnection() != null ) && ( entry.isAttributesInitialized() ) )
            {
                // lookup the entry from cache and fire event with real entry
                if ( entry.getBrowserConnection().getEntryFromCache( entry.getDn() ) != null )
                {
                    entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );
                }

                EventRegistry.fireEntryUpdated( new AttributesInitializedEvent( entry ), this );
            }
        }
    }


    // ── One Clone Loads His Briefing: Phase 1 — Pick The Right Attributes ─────
    // Decides which attributes to request: always user attrs ("*"), operational
    // attrs ("+" or explicit names) when the connection/entry flag says so, and
    // the "ref" attribute when the entry is a referral.
    // Synchronized because the entry model is shared mutable state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Determines the set of returning attributes and delegates to the two-arg
     * {@link #initializeAttributes(IEntry, String[], boolean, StudioProgressMonitor)}.
     * This is the public entry point used by the run() loop.
     *
     * @param entry   the entry to load attributes for.
     * @param monitor the progress monitor.
     */
    public static synchronized void initializeAttributes( IEntry entry, StudioProgressMonitor monitor )
    {
        // get user attributes or both user and operational attributes
        String[] returningAttributes = null;
        LinkedHashSet<String> raSet = new LinkedHashSet<String>();
        raSet.add( SchemaConstants.ALL_USER_ATTRIBUTES );
        boolean initOperationalAttributes = entry.getBrowserConnection().isFetchOperationalAttributes()
            || entry.isInitOperationalAttributes();

        if ( initOperationalAttributes )
        {
            if ( entry.getBrowserConnection().getRootDSE().isFeatureSupported(
                SchemaConstants.FEATURE_ALL_OPERATIONAL_ATTRIBUTES ) )
            {
                raSet.add( SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES );
            }
            else
            {
                Collection<AttributeType> opAtds = SchemaUtils.getOperationalAttributeDescriptions( entry
                    .getBrowserConnection().getSchema() );
                Collection<String> atdNames = SchemaUtils.getNames( opAtds );
                raSet.addAll( atdNames );
            }
        }

        if ( entry.isReferral() )
        {
            raSet.add( SchemaConstants.REF_AT );
        }

        returningAttributes = ( String[] ) raSet.toArray( new String[raSet.size()] );

        initializeAttributes( entry, returningAttributes, true, monitor );
    }


    // ── One Clone Loads His Briefing: Phase 2 — Execute The LDAP Search ───────
    // Special case: Root DSE delegates to loadRootDSE (it has its own loading
    // logic).  For real entries: optionally clears old attributes, creates an
    // OBJECT-scope search, adds ManageDsaIT for referral entries, calls
    // SearchRunnable.searchAndUpdateModel, then marks attributesInitialized=true.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads attributes for the entry using the specified returning attributes list.
     * Handles the Root DSE as a special case (delegates to
     * {@link InitializeRootDSERunnable#loadRootDSE}).  For normal entries:
     * optionally wipes existing attributes, builds an OBJECT-scope search, adds
     * the ManageDsaIT control for referrals, calls
     * {@link SearchRunnable#searchAndUpdateModel}, and sets
     * {@code attributesInitialized = true}.
     *
     * @param entry               the entry to populate.
     * @param attributes          the attribute names (or wildcards) to request.
     * @param clearAllAttributes  if {@code true}, delete all existing attributes
     *                            from the model before fetching new ones.
     * @param monitor             the progress monitor.
     */
    public static synchronized void initializeAttributes( IEntry entry, String[] attributes,
        boolean clearAllAttributes, StudioProgressMonitor monitor )
    {
        monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__init_entries_progress_att,
            new String[]
                { entry.getDn().getName() } ) );

        if ( entry instanceof IRootDSE )
        {
            // special handling for Root DSE
            InitializeRootDSERunnable.loadRootDSE( entry.getBrowserConnection(), monitor );
        }
        else
        {
            AliasDereferencingMethod aliasesDereferencingMethod = entry.getBrowserConnection()
                .getAliasesDereferencingMethod();

            if ( entry.isAlias() )
            {
                aliasesDereferencingMethod = AliasDereferencingMethod.NEVER;
            }

            ReferralHandlingMethod referralsHandlingMethod = entry.getBrowserConnection().getReferralsHandlingMethod();

            if ( clearAllAttributes )
            {
                // Clear all attributes (user and operational)
                // Must be done here because SearchRunnable.searchAndUpdateModel only clears
                // requested attributes. If the user switches the "Show operational attributes"
                // property then the operational attributes are not cleared.
                IAttribute[] oldAttributes = entry.getAttributes();

                if ( oldAttributes != null )
                {
                    for ( IAttribute oldAttribute : oldAttributes )
                    {
                        entry.deleteAttribute( oldAttribute );
                    }
                }
            }

            // create search
            ISearch search = new Search( null, entry.getBrowserConnection(), entry.getDn(),
                entry.isSubentry() ? ISearch.FILTER_SUBENTRY : ISearch.FILTER_TRUE, attributes, SearchScope.OBJECT, 0,
                0, aliasesDereferencingMethod, referralsHandlingMethod, false, null, false );

            // add controls
            if ( entry.isReferral() )
            {
                search.getControls().add( Controls.MANAGEDSAIT_CONTROL );
            }

            // search
            SearchRunnable.searchAndUpdateModel( entry.getBrowserConnection(), search, monitor );

            // we requested all attributes, set initialized state
            entry.setAttributesInitialized( true );
        }
    }
}
