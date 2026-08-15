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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.properties;


import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.core.BrowserConnectionManager;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReloadSchemaRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: SchemaPropertyPage — LUKE'S BINARY SUNSET ON TATOOINE ──────────────
// Luke watches both suns set and understands the full landscape — where Tatooine
// sits, what rules govern it, how it came to be.  The LDAP schema is the set of
// rules that governs a directory: what object classes exist, what attributes they
// allow, what syntax each attribute must follow.
// This property page shows the full schema landscape: where the schema entry lives
// in the directory (DN), when it was created and last modified, and where the
// local schema cache file is stored — plus a Reload button so Luke can re-fetch
// the schema if the server's rules have changed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse property page displaying schema metadata and cache information for
 * an LDAP connection.
 * Shows the schema entry's DN, createTimestamp, modifyTimestamp, and the local
 * cache file's path, last-modified date, and size.
 * A "Reload Schema" button lets the user re-fetch schema from the server without
 * closing the page.
 * Think of this page as Luke's binary sunset — understanding the rules that govern
 * the directory before committing to any structural changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

    /** Text field containing the Dn of the schema entry. */
    private Text dnText;

    /** Text field containing the create timestamp of the schema entry. */
    private Text ctText;

    /** Text field containing the modify timestamp of the schema entry. */
    private Text mtText;

    /** Button to reload the scheam. */
    private Button reloadSchemaButton;

    /** Text field containing the path to the schema cache file. */
    private Text cachePathText;

    /** Text field containing last modify date of the schema cache file. */
    private Text cacheDateText;

    /** Text field containing the size of the schema cache file. */
    private Text cacheSizeText;


    // ── LUKE STEPS OUT TO THE VIEWPOINT ───────────────────────────────────────
    // Luke arrives at his usual spot without any tools or configuration panels —
    // just the view and a pair of eyes.  No Apply, no Defaults, just read and
    // optionally trigger a reload.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the property page and suppresses the Default and Apply buttons.
     * The schema page is primarily informational; the only action is reloading
     * the schema, which is done via a dedicated button rather than Apply.
     *
     * <p>For example — Luke arrives at the viewpoint unencumbered:</p>
     * <pre>
     *   noDefaultAndApplyButton() → clean display with one explicit Reload action
     * </pre>
     */
    public SchemaPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── LUKE SURVEYS THE SCHEMA LANDSCAPE ─────────────────────────────────────
    // Luke's panorama has two sections: the horizon (schema information from the
    // live server: DN, create and modify timestamps) and the local cache (where
    // the schema is stored on disk, how big it is, when it was last updated).
    // A Reload button in the schema information section lets Luke fetch the latest
    // rules without leaving the viewpoint.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the property page UI: a "Schema Information" group with DN, create
     * timestamp, modify timestamp, and a Reload Schema button, and a "Schema Cache"
     * group with the local cache file path, date, and size.
     * Calls {@link #update(IBrowserConnection)} immediately to populate all fields.
     *
     * <p>For example — Luke's two-panel schema panorama:</p>
     * <pre>
     *   Schema Information:
     *     Schema DN: cn=schema
     *     Create Timestamp: 2025-05-04T00:00:00Z
     *     Modify Timestamp: 2025-08-01T12:00:00Z  [Reload Schema]
     *   Schema Cache:
     *     Cache Location: ~/.eclipse/.../schema-cache.xml
     *     Cache Date: August 1, 2025, 12:00 PM
     *     Cache Size: 256 kB
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's property dialog.
     * @return        The top-level composite we built.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Group infoGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "SchemaPropertyPage.SchemaInformation" ), 1 ); //$NON-NLS-1$
        Composite infoComposite = BaseWidgetUtils.createColumnContainer( infoGroup, 2, 1 );
        Composite infoGroupLeft = BaseWidgetUtils.createColumnContainer( infoComposite, 2, 1 );

        BaseWidgetUtils.createLabel( infoGroupLeft, Messages.getString( "SchemaPropertyPage.SchemaDN" ), 1 ); //$NON-NLS-1$
        dnText = BaseWidgetUtils.createWrappedLabeledText( infoGroupLeft, "-", 1, 200 ); //$NON-NLS-1$

        BaseWidgetUtils.createLabel( infoGroupLeft, Messages.getString( "SchemaPropertyPage.CreateTimestamp" ), 1 ); //$NON-NLS-1$
        ctText = BaseWidgetUtils.createWrappedLabeledText( infoGroupLeft, "-", 1, 200 ); //$NON-NLS-1$

        BaseWidgetUtils.createLabel( infoGroupLeft, Messages.getString( "SchemaPropertyPage.ModifyTimestamp" ), 1 ); //$NON-NLS-1$
        mtText = BaseWidgetUtils.createWrappedLabeledText( infoGroupLeft, "-", 1, 200 ); //$NON-NLS-1$

        reloadSchemaButton = BaseWidgetUtils.createButton( infoComposite, "-", 1 ); //$NON-NLS-1$
        GridData gd = new GridData();
        gd.verticalAlignment = SWT.BOTTOM;
        reloadSchemaButton.setLayoutData( gd );
        reloadSchemaButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                reloadSchema();
            }
        } );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        Group cacheGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "SchemaPropertyPage.SchemaCache" ), 1 ); //$NON-NLS-1$
        Composite cacheComposite = BaseWidgetUtils.createColumnContainer( cacheGroup, 2, 1 );

        BaseWidgetUtils.createLabel( cacheComposite, Messages.getString( "SchemaPropertyPage.CacheLocation" ), 1 ); //$NON-NLS-1$
        cachePathText = BaseWidgetUtils.createWrappedLabeledText( cacheComposite, "-", 1, 200 ); //$NON-NLS-1$

        BaseWidgetUtils.createLabel( cacheComposite, Messages.getString( "SchemaPropertyPage.CacheDate" ), 1 ); //$NON-NLS-1$
        cacheDateText = BaseWidgetUtils.createWrappedLabeledText( cacheComposite, "-", 1, 200 ); //$NON-NLS-1$

        BaseWidgetUtils.createLabel( cacheComposite, Messages.getString( "SchemaPropertyPage.CacheSize" ), 1 ); //$NON-NLS-1$
        cacheSizeText = BaseWidgetUtils.createWrappedLabeledText( cacheComposite, "-", 1, 200 ); //$NON-NLS-1$

        IBrowserConnection connection = RootDSEPropertyPage.getConnection( getElement() );
        update( connection );

        return composite;
    }


    // ── LUKE SIGNALS FOR A FRESH SCHEMA READING ───────────────────────────────
    // When the rules have changed — new object classes added, attributes renamed —
    // Luke wants an updated view.  He triggers a reload from the server and then
    // repaints the panorama with the fresh data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fetches the schema from the LDAP server via {@link ReloadSchemaRunnable},
     * then calls {@link #update(IBrowserConnection)} to refresh all text fields.
     * Triggered by the "Reload Schema" button click.
     *
     * <p>For example — Luke signals for a fresh schema reading:</p>
     * <pre>
     *   ReloadSchemaRunnable runs → schema re-fetched from server →
     *   update(connection) → all timestamps and cache info refreshed
     * </pre>
     */
    private void reloadSchema()
    {
        final IBrowserConnection browserConnection = RootDSEPropertyPage.getConnection( getElement() );
        ReloadSchemaRunnable runnable = new ReloadSchemaRunnable( browserConnection );
        RunnableContextRunner.execute( runnable, null, true );
        update( browserConnection );
    }


    // ── LUKE REPAINTS THE SCHEMA PANORAMA ─────────────────────────────────────
    // After a reload — or on first display — Luke repaints every field from the
    // live schema and the cache file on disk.  If the schema isn't loaded yet,
    // the DN and timestamp fields show dashes, and the button label changes from
    // "Reload Schema" to "Load Schema."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes all text fields from the given connection's schema and the local
     * cache file.
     * Silently skips the update if the DN text widget is disposed (dialog closed).
     * If the cache file doesn't exist all cache fields display {@code "-"}.
     *
     * <p>For example — Luke repaints the full schema panorama:</p>
     * <pre>
     *   schema loaded  → dnText=cn=schema, timestamps populated, button="Reload Schema"
     *   schema not yet → dnText="-", timestamps="-", button="Load Schema"
     *   cache exists   → path, date, size fields populated
     *   cache absent   → all cache fields="-"
     * </pre>
     *
     * @param browserConnection  The LDAP connection whose schema to display;
     *                           may be {@code null} if the element didn't adapt.
     */
    private void update( IBrowserConnection browserConnection )
    {
        if ( !dnText.isDisposed() )
        {
            Schema schema = null;
            if ( browserConnection != null )
            {
                schema = browserConnection.getSchema();
            }

            if ( schema != null && schema.getDn() != null )
            {
                dnText.setText( schema.getDn().toString() );
            }
            else
            {
                dnText.setText( "-" ); //$NON-NLS-1$
            }

            if ( schema != null && schema.getCreateTimestamp() != null )
            {
                ctText.setText( schema.getCreateTimestamp() );
            }
            else
            {
                ctText.setText( "-" ); //$NON-NLS-1$
            }

            if ( schema != null && schema.getModifyTimestamp() != null )
            {
                mtText.setText( schema.getModifyTimestamp() );
            }
            else
            {
                mtText.setText( "-" ); //$NON-NLS-1$
            }

            if ( schema != null )
            {
                reloadSchemaButton.setText( Messages.getString( "SchemaPropertyPage.ReloadSchema" ) ); //$NON-NLS-1$
            }
            else
            {
                reloadSchemaButton.setText( Messages.getString( "SchemaPropertyPage.LoadSchema" ) ); //$NON-NLS-1$
            }

            if ( browserConnection != null )
            {
                String cacheFileName = BrowserConnectionManager.getSchemaCacheFileName( browserConnection
                    .getConnection().getId() );
                File cacheFile = new File( cacheFileName );
                if ( cacheFile.exists() )
                {
                    cachePathText.setText( cacheFile.getPath() );
                    DateFormat format = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.MEDIUM );
                    cacheDateText.setText( format.format( new Date( cacheFile.lastModified() ) ) );
                    cacheSizeText.setText( Utils.formatBytes( cacheFile.length() ) );
                }
                else
                {
                    cachePathText.setText( "-" ); //$NON-NLS-1$
                    cacheDateText.setText( "-" ); //$NON-NLS-1$
                    cacheSizeText.setText( "-" ); //$NON-NLS-1$
                }
            }

            reloadSchemaButton.setEnabled( true );
        }
    }


    // ── LUKE CHECKS IF THE VIEWPOINT IS STILL ACCESSIBLE ─────────────────────
    // If a sandstorm swept through and destroyed the viewpoint, Luke can't use it.
    // We check whether the root text widget has been disposed so callers know
    // whether they can still push updates to this page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the page's DN text widget has been disposed,
     * indicating the property dialog was closed.
     * Callers use this to guard against pushing updates to a dead page.
     *
     * <p>For example — Luke checks if the viewpoint survived the sandstorm:</p>
     * <pre>
     *   dialog still open → dnText.isDisposed() = false → isDisposed() = false
     *   dialog closed     → dnText.isDisposed() = true  → isDisposed() = true
     * </pre>
     *
     * @return  {@code true} if the root text widget has been disposed.
     */
    public boolean isDisposed()
    {
        return dnText.isDisposed();
    }

}
