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

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import org.apache.directory.api.ldap.model.schema.AbstractSchemaObject;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.MatchingRuleUse;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;


// ── CLASS: SchemaBrowser — The Death Star Blueprints R2 Is Carrying ───────────
// R2-D2 carries the complete Death Star blueprints — five major system diagrams
// (structural, mechanical, targeting, power, communications) organised into
// tabs that any authorised technician can flip through.  He can show a specific
// diagram on command, record where you were in the history, and refresh all five
// from the server when new data arrives.  SchemaBrowser is exactly that: an
// Eclipse EditorPart with five tabs (object classes, attribute types, matching
// rules, matching rule uses, syntaxes), each backed by a SchemaPage that lists
// the server's schema elements and drills into their details.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main schema browser Eclipse editor part, showing a tabbed view of all
 * schema element types retrieved from an LDAP server.
 * It hosts five {@link SchemaPage} tabs and implements {@link IReusableEditor}
 * so only one instance is ever open — navigating to a different schema element
 * reuses the same tab rather than opening a new one.
 * Think of this class as R2-D2 with the blueprints: a single carrier that holds
 * all five categories of schema information and can jump to any specific element
 * on request while recording every stop in the navigation history.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaBrowser extends EditorPart implements INavigationLocationProvider, IReusableEditor
{

    /** The tab folder with all the schema element tabs */
    private CTabFolder tabFolder;

    /** The object class tab */
    private CTabItem ocdTab;

    /** The object class page */
    private ObjectClassDescriptionPage ocdPage;

    /** The attribute type tab */
    private CTabItem atdTab;

    /** The attribute type page */
    private AttributeTypeDescriptionPage atdPage;

    /** The matching rule tab */
    private CTabItem mrdTab;

    /** The matching rule page */
    private MatchingRuleDescriptionPage mrdPage;

    /** The matching rule use tab */
    private CTabItem mrudTab;

    /** The matching rule use page */
    private MatchingRuleUseDescriptionPage mrudPage;

    /** The syntax tab */
    private CTabItem lsdTab;

    /** The syntax page */
    private LdapSyntaxDescriptionPage lsdPage;


    // ── R2 Announces His Serial Number ────────────────────────────────────────────
    // When the base computer asks "which droid are you?" R2 returns his unique ID
    // so the system knows exactly which blueprint carrier has been registered.
    // We return the Eclipse editor ID constant that identifies the schema browser
    // so other parts of the application can find and reuse this editor.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse editor ID for the schema browser.
     * Other code uses this ID to open or locate the single schema browser instance
     * via {@code IWorkbenchPage.openEditor()} and {@code findEditor()}.
     *
     * @return the schema browser editor ID constant
     */
    public static String getId()
    {
        return BrowserUIConstants.EDITOR_SCHEMA_BROWSER;
    }


    // ── R2 Powers On And Loads The Initial Blueprint ──────────────────────────────
    // R2 powers on his systems in sequence, first recording a dummy location so
    // the navigation history has a valid starting point, then loading the real
    // blueprint that was requested.
    // We mark a dummy navigation location first — Eclipse ignores the very first
    // marked location in its history, so this ensures the real input actually
    // appears when the user presses Back.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the editor with the given site and input.
     * We perform a two-step setup: first we set a null/dummy input and mark a
     * navigation location (Eclipse discards the first mark, so this is a workaround
     * to make the real location visible in history), then we set the actual input.
     *
     * <p>For example — R2 powers up and loads Leia's message:</p>
     * <pre>
     *   init(site, input);
     *   // dummy location marked → real input set → schema browser shows correctly
     * </pre>
     *
     * @param site   the editor site provided by Eclipse
     * @param input  the initial SchemaBrowserInput to display
     * @throws PartInitException if the site cannot be initialised
     */
    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        setSite( site );

        // mark dummy location, necessary because the first marked
        // location doesn't appear in history
        setInput( new SchemaBrowserInput( null, null ) );
        getSite().getPage().getNavigationHistory().markLocation( this );

        // set real input
        setInput( input );
    }


    // ── R2 Powers Down All Five Blueprint Displays ────────────────────────────────
    // After the mission R2 shuts down each of his five system displays in turn,
    // retracts the holoprojector, and powers off cleanly.
    // We dispose each page, then the tab folder, then call super so Eclipse can
    // finish its own cleanup.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all five schema pages, the tab folder, and any other resources held
     * by the schema browser when the editor is closing.
     * Always call {@code super.dispose()} last so Eclipse can finish its own cleanup.
     *
     * <p>For example — R2 shuts down after the mission:</p>
     * <pre>
     *   dispose();
     *   // ocdPage, atdPage, mrdPage, mrudPage, lsdPage all disposed
     * </pre>
     */
    @Override
    public void dispose()
    {
        ocdPage.dispose();
        atdPage.dispose();
        mrdPage.dispose();
        mrudPage.dispose();
        lsdPage.dispose();
        tabFolder.dispose();
        super.dispose();
    }


    // ── R2 Unfolds His Five Holographic Panels ────────────────────────────────────
    // R2 extends his holoprojector and arranges five separate holographic panels
    // side by side — one for each major blueprint category — selecting the object
    // class diagram as the default view.
    // We create a CTabFolder with five pages, wire up each SchemaPage, and set
    // the initial tab selection to object classes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the five-tab UI of the schema browser inside the given parent
     * composite.
     * Each tab contains one {@link SchemaPage} subclass that manages its own
     * master/detail split.  We also set up Eclipse help context IDs here so the
     * F1 key opens the correct help topic.
     *
     * <p>For example — R2 deploys his blueprint panels:</p>
     * <pre>
     *   createPartControl(parent);
     *   // tabFolder now has five tabs: Object Classes, Attribute Types,
     *   // Matching Rules, Matching Rule Use, Syntaxes
     *   // default selection is the Object Classes tab
     * </pre>
     *
     * @param parent  the SWT composite provided by the Eclipse editor area
     */
    @Override
    public void createPartControl( Composite parent )
    {
        tabFolder = new CTabFolder( parent, SWT.BOTTOM );

        ocdTab = new CTabItem( tabFolder, SWT.NONE );
        ocdTab.setText( Messages.getString( "SchemaBrowser.ObjectClasses" ) ); //$NON-NLS-1$
        ocdTab.setImage( BrowserUIPlugin.getDefault().getImage( BrowserUIConstants.IMG_OCD ) );
        ocdPage = new ObjectClassDescriptionPage( this );
        Control ocdPageControl = ocdPage.createControl( tabFolder );
        ocdTab.setControl( ocdPageControl );

        atdTab = new CTabItem( tabFolder, SWT.NONE );
        atdTab.setText( Messages.getString( "SchemaBrowser.AttributeTypes" ) ); //$NON-NLS-1$
        atdTab.setImage( BrowserUIPlugin.getDefault().getImage( BrowserUIConstants.IMG_ATD ) );
        atdPage = new AttributeTypeDescriptionPage( this );
        Control atdPageControl = atdPage.createControl( tabFolder );
        atdTab.setControl( atdPageControl );

        mrdTab = new CTabItem( tabFolder, SWT.NONE );
        mrdTab.setText( Messages.getString( "SchemaBrowser.MatchingRules" ) ); //$NON-NLS-1$
        mrdTab.setImage( BrowserUIPlugin.getDefault().getImage( BrowserUIConstants.IMG_MRD ) );
        mrdPage = new MatchingRuleDescriptionPage( this );
        Control mrdPageControl = mrdPage.createControl( tabFolder );
        mrdTab.setControl( mrdPageControl );

        mrudTab = new CTabItem( tabFolder, SWT.NONE );
        mrudTab.setImage( BrowserUIPlugin.getDefault().getImage( BrowserUIConstants.IMG_MRUD ) );
        mrudTab.setText( Messages.getString( "SchemaBrowser.MatchingRulesUse" ) ); //$NON-NLS-1$
        mrudPage = new MatchingRuleUseDescriptionPage( this );
        Control mrudPageControl = mrudPage.createControl( tabFolder );
        mrudTab.setControl( mrudPageControl );

        lsdTab = new CTabItem( tabFolder, SWT.NONE );
        lsdTab.setImage( BrowserUIPlugin.getDefault().getImage( BrowserUIConstants.IMG_LSD ) );
        lsdTab.setText( Messages.getString( "SchemaBrowser.Syntaxes" ) ); //$NON-NLS-1$
        lsdPage = new LdapSyntaxDescriptionPage( this );
        Control lsdPageControl = lsdPage.createControl( tabFolder );
        lsdTab.setControl( lsdPageControl );

        // set default selection
        tabFolder.setSelection( ocdTab );

        // init help context
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_schema_browser" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( tabFolder,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_schema_browser" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( ocdPageControl,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_schema_browser" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── R2 Switches To The Requested Blueprint Section ────────────────────────────
    // The officer calls out "Show me the targeting system — sub-section B" and R2
    // scrolls to the right panel, highlights that component, and records the stop
    // in his navigation log.
    // We inspect the schema element type, select the matching page, scroll the list
    // to that element, and record the navigation history entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Navigates the schema browser to display the connection and schema element
     * specified in the given input.
     * We determine which tab to activate based on the Java type of the schema
     * element (ObjectClass → ocdTab, AttributeType → atdTab, etc.), then tell
     * that page to scroll its list to the requested element.
     * Navigation history is marked only when both connection and element are
     * non-null, avoiding spurious history entries for programmatic clears.
     *
     * <p>For example — R2 jumps to the requested blueprint section:</p>
     * <pre>
     *   setInput(new SchemaBrowserInput(conn, attributeType));
     *   // atdPage.select(attributeType) called
     *   // tabFolder switches to atdTab
     *   // navigation history records this location
     * </pre>
     *
     * @param input  a SchemaBrowserInput carrying the target connection and element
     */
    @Override
    public void setInput( IEditorInput input )
    {
        super.setInput( input );

        if ( input instanceof SchemaBrowserInput && tabFolder != null )
        {
            SchemaBrowserInput sbi = ( SchemaBrowserInput ) input;

            // set connection;
            IBrowserConnection connection = sbi.getConnection();
            setConnection( connection );

            // set schema element and activate tab
            AbstractSchemaObject schemaElement = sbi.getSchemaElement();
            if ( schemaElement instanceof ObjectClass )
            {
                ocdPage.select( schemaElement );
                tabFolder.setSelection( ocdTab );
            }
            else if ( schemaElement instanceof AttributeType )
            {
                atdPage.select( schemaElement );
                tabFolder.setSelection( atdTab );
            }
            else if ( schemaElement instanceof MatchingRule )
            {
                mrdPage.select( schemaElement );
                tabFolder.setSelection( mrdTab );
            }
            else if ( schemaElement instanceof MatchingRuleUse )
            {
                mrudPage.select( schemaElement );
                tabFolder.setSelection( mrudTab );
            }
            else if ( schemaElement instanceof LdapSyntax )
            {
                lsdPage.select( schemaElement );
                tabFolder.setSelection( lsdTab );
            }

            if ( connection != null && schemaElement != null )
            {
                // disable one instance hack before fireing the input change event
                // otherwise the navigation history is cleared.
                // Note: seems this behavior has been changed with Eclipse 3.3
                SchemaBrowserInput.enableOneInstanceHack( false );
                firePropertyChange( IEditorPart.PROP_INPUT );

                // enable one instance hack for marking the location
                // Note: seems this behavior has been changed with Eclipse 3.3
                SchemaBrowserInput.enableOneInstanceHack( true );
                getSite().getPage().getNavigationHistory().markLocation( this );
            }

            // finally enable the one instance hack
            SchemaBrowserInput.enableOneInstanceHack( true );
        }
    }


    // ── R2 Refreshes All Five Blueprint Panels ────────────────────────────────────
    // After a schema reload R2 updates each of his five panels from his storage —
    // discarding stale data and projecting the fresh version.
    // We call refresh() on all five pages so they each re-query their schema input.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes all five schema pages so their viewer lists reflect the latest
     * schema data from the connection.
     * Typically called after a schema reload action completes.
     *
     * <p>For example — R2 refreshes all five panels after a download:</p>
     * <pre>
     *   refresh();
     *   // all tabs now show the updated schema
     * </pre>
     */
    public void refresh()
    {
        ocdPage.refresh();
        atdPage.refresh();
        mrdPage.refresh();
        mrudPage.refresh();
        lsdPage.refresh();
    }


    // ── R2 Switches All Panels To Default Blueprint Mode ──────────────────────────
    // R2 flips a master switch that makes all five panels show the factory-default
    // baseline diagram rather than the server-specific customised version.
    // We push the flag to all five pages simultaneously.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Tells all five schema pages whether to display the built-in default schema
     * or the live schema from the selected connection.
     * Delegates to each page's {@code setShowDefaultSchema()} so all tabs switch
     * in sync when the user toggles the toolbar action.
     *
     * @param b  true to show the default schema, false to show the connection schema
     */
    public void setShowDefaultSchema( boolean b )
    {
        ocdPage.setShowDefaultSchema( b );
        atdPage.setShowDefaultSchema( b );
        mrdPage.setShowDefaultSchema( b );
        mrudPage.setShowDefaultSchema( b );
        lsdPage.setShowDefaultSchema( b );
    }


    // ── R2 Re-Connects All Panels To The New Ship ─────────────────────────────────
    // When the crew boards a different ship R2 re-links all five blueprint panels
    // to the new vessel's computer so every diagram reflects the new environment.
    // We push the connection to all five pages so they all query the same server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP connection on all five schema pages, causing them all to
     * reload their viewer content from the new connection's schema.
     *
     * <p>For example — R2 links to a new ship's computer:</p>
     * <pre>
     *   setConnection(newConnection);
     *   // all five pages now show that connection's schema
     * </pre>
     *
     * @param connection  the LDAP connection to display; null clears all pages
     */
    public void setConnection( IBrowserConnection connection )
    {
        ocdPage.setConnection( connection );
        atdPage.setConnection( connection );
        mrdPage.setConnection( connection );
        mrudPage.setConnection( connection );
        lsdPage.setConnection( connection );
    }


    // ── R2 Acknowledges A Focus Request ───────────────────────────────────────────
    // The base computer pings R2 to take keyboard focus — R2 acknowledges but
    // has nothing specific to focus on at the top level; individual pages
    // handle their own focus internally.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the editor should take keyboard focus.
     * We have nothing to focus at the top level; the active tab page manages
     * its own focus internally.
     */
    @Override
    public void setFocus()
    {
    }


    // ── R2's Blueprint Is Read-Only ────────────────────────────────────────────────
    // Leia's blueprints are for reading, not editing — R2 has no "save" function.
    // These lifecycle methods are no-ops because the schema browser is read-only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * No-op: the schema browser is read-only and never needs saving.
     *
     * @param monitor  ignored
     */
    @Override
    public void doSave( IProgressMonitor monitor )
    {
    }


    /**
     * No-op: the schema browser does not support "Save As."
     */
    @Override
    public void doSaveAs()
    {
    }


    /**
     * Returns false always; the schema browser is read-only and never dirty.
     *
     * @return false
     */
    @Override
    public boolean isDirty()
    {
        return false;
    }


    /**
     * Returns false always; the schema browser does not support "Save As."
     *
     * @return false
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── R2 Creates An Empty Slot In The Navigation Log ────────────────────────────
    // When Eclipse asks for an empty navigation location slot (e.g. to prepare
    // for a new history entry) R2 returns null to indicate no empty slot is needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns null because the schema browser does not need to pre-allocate
     * an empty navigation location.
     *
     * @return null always
     */
    @Override
    public INavigationLocation createEmptyNavigationLocation()
    {
        return null;
    }


    // ── R2 Stamps The Current Position Into The Navigation Log ────────────────────
    // Each time R2 moves to a new blueprint section he stamps the current
    // coordinates into his navigation log so the crew can go back.
    // We create a SchemaBrowserNavigationLocation capturing the current input.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation location representing the schema browser's current state
     * for Eclipse's back/forward navigation history.
     * Called by Eclipse each time the user navigates to a different schema element.
     *
     * <p>For example — R2 stamps the current blueprint position:</p>
     * <pre>
     *   INavigationLocation loc = createNavigationLocation();
     *   // loc.getText() returns something like "Object Class: inetOrgPerson"
     * </pre>
     *
     * @return a new SchemaBrowserNavigationLocation for the current state
     */
    @Override
    public INavigationLocation createNavigationLocation()
    {
        return new SchemaBrowserNavigationLocation( this );
    }

}
