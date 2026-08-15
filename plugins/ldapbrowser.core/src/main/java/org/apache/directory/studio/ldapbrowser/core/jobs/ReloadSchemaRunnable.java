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


import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.BrowserConnectionUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldifparser.model.LdifEnumeration;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;


// ── CLASS: ReloadSchemaRunnable — YODA UPDATES THE JEDI ARCHIVES ─────────────
// Master Yoda periodically walks through the Jedi Archives and checks whether
// the knowledge stored on the shelves has become outdated — new species discovered,
// new diplomatic protocols established.  If the modifyTimestamp on the archive
// record is newer than the in-memory copy he holds, he replaces the cached pages
// with a fresh copy from the holographic vault.
// This runnable does the same for an LDAP server's schema: it queries the server's
// subschema subentry, compares its modifyTimestamp with the cached {@link Schema},
// and if the server's copy is newer (or we're forcing a reload) it downloads the
// full schema (objectClasses, attributeTypes, syntaxes, matchingRules) and installs
// it on the browser connection.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that reloads the LDAP schema for a browser connection.
 * The LDAP schema (objectClasses, attributeTypes, syntaxes, matchingRules) is
 * stored in a special "subschema subentry" on the server.  We load it once when
 * a connection opens and cache it in memory.  This runnable re-fetches it — either
 * on demand (the user clicks "Reload schema") or automatically when the server's
 * schema timestamp is newer than our cached copy.
 * After a successful reload it fires a {@link BrowserConnectionUpdateEvent} with
 * {@code SCHEMA_UPDATED} so the UI knows to refresh auto-complete suggestions
 * and validation rules.
 * Think of it as Yoda updating the Jedi Archives from the holocron vault.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReloadSchemaRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The browser connection. */
    private IBrowserConnection browserConnection;


    // ── Yoda Selects Which Archive Shelf To Update ────────────────────────────────
    // "This connection's archives — these I will update."  Yoda notes which
    // server's schema library needs refreshing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ReloadSchemaRunnable for the given browser connection.
     *
     * <p>For example — triggered by the "Reload Schema" menu action:</p>
     * <pre>
     *   new StudioBrowserJob(new ReloadSchemaRunnable(browserConn)).execute();
     * </pre>
     *
     * @param browserConnection the connection whose schema should be reloaded.
     */
    public ReloadSchemaRunnable( IBrowserConnection browserConnection )
    {
        this.browserConnection = browserConnection;
    }


    // ── Yoda Connects To The Holocron Vault ──────────────────────────────────────
    // "Through this channel the knowledge flows."  We return the underlying raw
    // Connection so the job framework can acquire it before the LDAP calls begin.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying LDAP connection used for the schema search.
     *
     * @return a single-element array containing the raw connection.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── The Task Name For The Progress Indicator ──────────────────────────────────
    // "Yoda is updating the Jedi Archives..." — shown in the Eclipse progress view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Reload schema" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__reload_schemas_name_1;
    }


    // ── Yoda Locks The Archive During The Update ──────────────────────────────────
    // No Padawan should read the archives while Yoda is mid-update — the pages
    // would be half-old, half-new.  We lock the browser connection itself so
    // no other job reads or writes schema-dependent data during the reload.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be exclusively locked while this job runs.
     * We lock the browser connection to prevent concurrent jobs from reading
     * a half-initialised schema.
     *
     * @return the browser connection wrapped in an array.
     */
    public Object[] getLockedObjects()
    {
        return new IBrowserConnection[]
            { browserConnection };
    }


    // ── The Error Report If The Archive Update Fails ──────────────────────────────
    // "The holocron vault was inaccessible — schema reload failed."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message displayed if the schema reload fails.
     *
     * @return a localised "Could not reload schema" error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__reload_schemas_error_1;
    }


    // ── Yoda Downloads Fresh Pages From The Holocron Vault ───────────────────────
    // Yoda walks to the vault, checks the timestamp on the scroll, sees it's newer,
    // and replaces the stale pages in the archive with the fresh holocron data.
    // We call {@link #reloadSchema(boolean, IBrowserConnection, StudioProgressMonitor)}
    // with {@code forceReload = true} so the schema is always refreshed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Performs the schema reload.  Downloads the server's subschema subentry
     * and installs a fresh {@link Schema} on the browser connection.
     *
     * <p>For example — what happens under the hood:</p>
     * <pre>
     *   reloadSchema(true /*force*\/, browserConnection, monitor);
     * </pre>
     *
     * @param monitor the Eclipse progress monitor; reports task names and progress.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( " ", 3 ); //$NON-NLS-1$
        monitor.reportProgress( " " ); //$NON-NLS-1$

        monitor.setTaskName( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__reload_schemas_task, new String[]
            { browserConnection.getConnection().getName() } ) );
        monitor.worked( 1 );

        // load schema
        monitor.reportProgress( BrowserCoreMessages.model__loading_schema );
        reloadSchema( true, browserConnection, monitor );
        monitor.worked( 1 );
    }


    // ── Yoda Announces The Archive Is Up To Date ──────────────────────────────────
    // "The archives — updated they are."  Yoda fires a SCHEMA_UPDATED event
    // so every listener (auto-complete engines, object-class validators) knows
    // to reload their schema-dependent caches.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires a {@link BrowserConnectionUpdateEvent} with {@code SCHEMA_UPDATED} after
     * the schema reload completes.  Listeners respond by refreshing auto-complete
     * drop-downs, schema-tree views, and object-class validation.
     *
     * @param monitor ignored (notification has no progress to report).
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        BrowserConnectionUpdateEvent browserConnectionUpdateEvent = new BrowserConnectionUpdateEvent(
            browserConnection, BrowserConnectionUpdateEvent.Detail.SCHEMA_UPDATED );
        EventRegistry.fireBrowserConnectionUpdated( browserConnectionUpdateEvent, this );
    }


    // ── Yoda's Core Algorithm: Check Timestamp, Reload If Stale ─────────────────
    // "Older than the holocron's record, our copy is — reload we must."
    // This static helper is also called by {@link BrowserConnectionListener}
    // on initial connection open, so schema loading on connect re-uses the
    // same logic as the "Reload Schema" user action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the LDAP schema for the given browser connection.
     * If {@code forceReload} is {@code true} we always reload; otherwise we
     * first fetch the server's schema timestamp and only reload if the server's
     * copy is newer than our cached {@link Schema}.
     *
     * <p>For example — forcing a reload on demand:</p>
     * <pre>
     *   ReloadSchemaRunnable.reloadSchema(true, browserConn, monitor);
     * </pre>
     *
     * @param forceReload       {@code true} to always reload; {@code false} to skip if
     *                          the cached schema is already current.
     * @param browserConnection the connection whose schema to reload.
     * @param monitor           the progress monitor.
     */
    public static void reloadSchema( boolean forceReload, IBrowserConnection browserConnection,
        StudioProgressMonitor monitor )
    {
        Dn schemaLocation = getSchemaLocation( browserConnection, monitor );
        if ( schemaLocation == null )
        {
            monitor.reportError( BrowserCoreMessages.model__missing_schema_location );
            return;
        }

        Schema schema = browserConnection.getSchema();

        boolean mustReload = forceReload || ( schema == Schema.DEFAULT_SCHEMA )
            || mustReload( schemaLocation, browserConnection, monitor );

        if ( mustReload )
        {
            browserConnection.setSchema( Schema.DEFAULT_SCHEMA );

            try
            {
                SearchParameter sp = new SearchParameter();
                sp.setSearchBase( schemaLocation );
                sp.setFilter( Schema.SCHEMA_FILTER );
                sp.setScope( SearchScope.OBJECT );
                sp.setReturningAttributes( new String[]
                    { SchemaConstants.OBJECT_CLASSES_AT, SchemaConstants.ATTRIBUTE_TYPES_AT,
                        SchemaConstants.LDAP_SYNTAXES_AT, SchemaConstants.MATCHING_RULES_AT,
                        SchemaConstants.MATCHING_RULE_USE_AT, SchemaConstants.CREATE_TIMESTAMP_AT,
                        SchemaConstants.MODIFY_TIMESTAMP_AT } );

                LdifEnumeration le = ExportLdifRunnable.search( browserConnection, sp, monitor );
                if ( le.hasNext() )
                {
                    LdifContentRecord schemaRecord = ( LdifContentRecord ) le.next();
                    schema = new Schema();
                    schema.loadFromRecord( schemaRecord );
                    browserConnection.setSchema( schema );
                }
                else
                {
                    monitor.reportError( BrowserCoreMessages.model__no_schema_information );
                }
            }
            catch ( Exception e )
            {
                monitor.reportError( BrowserCoreMessages.model__error_loading_schema, e );
                e.printStackTrace();
            }
        }
    }


    /**
     * Checks whether the server's schema is newer than our cached copy by
     * comparing the {@code modifyTimestamp} (or {@code createTimestamp}) of
     * the subschema subentry with the timestamp stored in our {@link Schema}.
     *
     * @param schemaLocation    the DN of the server's subschema subentry.
     * @param browserConnection the connection to query.
     * @param monitor           the progress monitor.
     * @return {@code true} if the server's schema is newer and we should reload.
     */
    private static boolean mustReload( Dn schemaLocation, IBrowserConnection browserConnection,
        StudioProgressMonitor monitor )
    {
        Schema schema = browserConnection.getSchema();

        try
        {
            SearchParameter sp = new SearchParameter();
            sp.setSearchBase( schemaLocation );
            sp.setFilter( Schema.SCHEMA_FILTER );
            sp.setScope( SearchScope.OBJECT );
            sp.setReturningAttributes( new String[]
                { SchemaConstants.CREATE_TIMESTAMP_AT, SchemaConstants.MODIFY_TIMESTAMP_AT } );
            StudioSearchResultEnumeration enumeration = SearchRunnable.search( browserConnection, sp, monitor );
            while ( enumeration != null && enumeration.hasMore() )
            {
                String createTimestamp = null;
                String modifyTimestamp = null;

                Entry entry = enumeration.next().getEntry();
                if ( entry.hasObjectClass( SchemaConstants.MODIFY_TIMESTAMP_AT ) )
                {
                    modifyTimestamp = entry.get( SchemaConstants.MODIFY_TIMESTAMP_AT ).getString();
                }
                if ( entry.hasObjectClass( SchemaConstants.CREATE_TIMESTAMP_AT ) )
                {
                    createTimestamp = entry.get( SchemaConstants.CREATE_TIMESTAMP_AT ).getString();
                }

                String schemaTimestamp = modifyTimestamp != null ? modifyTimestamp : createTimestamp;
                String cacheTimestamp = schema.getModifyTimestamp() != null ? schema.getModifyTimestamp() : schema
                    .getCreateTimestamp();
                if ( cacheTimestamp != null && schemaTimestamp != null
                    && schemaTimestamp.compareTo( cacheTimestamp ) > 0 )
                {
                    return true;
                }
            }
        }
        catch ( Exception e )
        {
            monitor.reportError( BrowserCoreMessages.model__error_loading_schema, e );
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Finds the DN of the server's subschema subentry by reading the RootDSE's
     * {@code subschemaSubentry} attribute.
     *
     * @param browserConnection the connection to query.
     * @param monitor           the progress monitor.
     * @return the subschema subentry DN, or {@code null} if not found or on error.
     */
    private static Dn getSchemaLocation( IBrowserConnection browserConnection, StudioProgressMonitor monitor )
    {
        try
        {
            SearchParameter sp = new SearchParameter();
            sp.setSearchBase( new Dn() );
            sp.setScope( SearchScope.OBJECT );
            sp.setReturningAttributes( new String[]
                { SchemaConstants.SUBSCHEMA_SUBENTRY_AT } );
            StudioSearchResultEnumeration enumeration = SearchRunnable.search( browserConnection, sp, monitor );
            while ( enumeration != null && enumeration.hasMore() )
            {
                Entry entry = enumeration.next().getEntry();
                if ( entry.containsAttribute( SchemaConstants.SUBSCHEMA_SUBENTRY_AT ) )
                {
                    String value = entry.get( SchemaConstants.SUBSCHEMA_SUBENTRY_AT ).getString();
                    if ( Dn.isValid( value ) )
                    {
                        Dn dn = new Dn( value );
                        return dn;
                    }
                }
            }
        }
        catch ( Exception e )
        {
            monitor.reportError( BrowserCoreMessages.model__error_loading_schema, e );
            return null;
        }

        return null;
    }

}
