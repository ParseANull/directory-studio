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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import java.util.Arrays;
import java.util.Collection;

import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.ListContentProposalProvider;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.impl.QuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: BrowserQuickSearchWidget — R2 AT THE DEATH STAR DETENTION TERMINAL ─
// In A New Hope, R2-D2 plugs into the Death Star's detention-level data port
// and fires off a quick query — attribute "cell-block", value "AA-23" — and
// the result comes back immediately without opening the full search dialog.
// BrowserQuickSearchWidget is that exact inline terminal: a collapsible bar at
// the top of the browser tree where the user picks an attribute, an operator,
// and a value, then hits Run to fire an LDAP query under the selected entry.
// The results appear as a sub-node in the tree — no wizard, no dialog, just R2
// plugging in and getting the answer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An inline quick-search bar embedded at the top of the browser tree widget.
 * The user picks an attribute name, an operator (=, !=, <=, >=, ~=), and a
 * value, then hits Run to execute an LDAP search under the selected entry.
 * The widget can be shown or hidden via {@link #setActive(boolean)}, and its
 * SWT controls are created lazily only when activated.
 * Think of this class as R2-D2 at the Death Star detention terminal — a fast
 * inline query tool that doesn't need the full search dialog.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserQuickSearchWidget
{

    /** The Constant VALUE_HISTORY_DIALOGSETTING_KEY. */
    public static final String VALUE_HISTORY_DIALOGSETTING_KEY = BrowserQuickSearchWidget.class.getName()
        + ".valueHistory"; //$NON-NLS-1$

    /** The Constant ATTRIBUTE_HISTORY_DIALOGSETTING_KEY. */
    public static final String ATTRIBUTE_HISTORY_DIALOGSETTING_KEY = BrowserQuickSearchWidget.class.getName()
        + ".attributeHistory"; //$NON-NLS-1$

    /** An empty string array */
    private static final String[] EMPTY = new String[0];

    /** The browser widget. */
    private BrowserWidget browserWidget;

    /** The parent, used to create the composite. */
    private Composite parent;

    /** The outer composite. */
    private Composite composite;

    /** The inner composite, it is created/destroyed when showing/hiding the quick search. */
    private Composite innerComposite;

    /** The quick search attribute combo. */
    private Combo quickSearchAttributeCombo;

    /** The quick search attribute proposal provider. */
    private ListContentProposalProvider quickSearchAttributePP;

    /** The quick search operator combo. */
    private Combo quickSearchOperatorCombo;

    /** The quick search value combo. */
    private Combo quickSearchValueCombo;

    /** The quick search value proposal provider. */
    private ListContentProposalProvider quickSearchValuePP;

    /** The quick search scope button. */
    private Button quickSearchScopeButton;

    /** The quick search run button. */
    private Button quickSearchRunButton;

    /** Listener that listens for selections of connections */
    private ISelectionChangedListener selectionListener = new ISelectionChangedListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation sets the input when another connection was selected.
         */
        public void selectionChanged( SelectionChangedEvent event )
        {
            setEnabled( getSelectedEntry() != null );
        }
    };


    // ── R2 CONNECTS HIS PROBE AND LOADS DEFAULT QUERY HISTORY ────────────────
    // R2 rolls up to the Death Star terminal, extends his probe arm, and plugs in.
    // He immediately loads his default query dictionary — "cn", "sn", "uid",
    // "mail" — the most common LDAP attributes — so the operator gets useful
    // auto-complete suggestions right away without typing anything.
    // We seed the attribute history in dialog settings if it's empty (first run).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BrowserQuickSearchWidget linked to the given browser widget.
     * If the attribute history in dialog settings is empty (first run), we seed it
     * with a default list of common LDAP attributes (cn, sn, givenName, mail, uid, etc.)
     * so the user gets useful auto-complete suggestions immediately.
     *
     * <p>For example — R2 loads his default query dictionary on first connect:</p>
     * <pre>
     *   if (attributeHistory.isEmpty()) {
     *     attributeHistory.seed(["cn","sn","givenName","mail","uid","ou","o","member"]);
     *   }
     * </pre>
     *
     * @param browserWidget   the browser tree widget this quick-search bar belongs to;
     *                        we need it to get the viewer and listen for selection changes
     */
    public BrowserQuickSearchWidget( BrowserWidget browserWidget )
    {
        this.browserWidget = browserWidget;

        if ( HistoryUtils.load( BrowserCommonActivator.getDefault().getDialogSettings(),
            ATTRIBUTE_HISTORY_DIALOGSETTING_KEY ).length == 0 )
        {
            BrowserCommonActivator.getDefault().getDialogSettings().put( ATTRIBUTE_HISTORY_DIALOGSETTING_KEY,
                new String[]
                    { "cn", //$NON-NLS-1$
                        "sn", //$NON-NLS-1$
                        "givenName", //$NON-NLS-1$
                        "mail", //$NON-NLS-1$
                        "uid", //$NON-NLS-1$
                        "description", //$NON-NLS-1$
                        "o", //$NON-NLS-1$
                        "ou", //$NON-NLS-1$
                        "member" //$NON-NLS-1$
                    } );
        }
    }


    // ── R2 PARKS NEXT TO THE DATA PORT WITHOUT PLUGGING IN YET ───────────────
    // R2 wheels up to the Death Star wall panel and positions himself next to the
    // data port — he's in the right place but hasn't extended his probe arm yet.
    // The actual terminal interface (inner controls) only appears when the quick
    // search bar is activated.
    // We create the outer SWT container but leave the inner controls uncreated.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the outer container composite for the quick search bar.
     * At this point the bar takes up no visible space (zero height and width) —
     * it's hidden by default. The actual input controls are only created when
     * {@link #setActive(boolean)} is called with {@code true}.
     *
     * <p>For example — R2 parks at the Death Star port without plugging in:</p>
     * <pre>
     *   R2.positionAt(dataPortLocation);
     *   R2.collapseToZeroSize();
     *   // Inner controls (attribute, operator, value, button) not created yet
     * </pre>
     *
     * @param parent   the SWT composite to attach our container to; we'll add the
     *                 inner controls as children when activated
     */
    public void createComposite( Composite parent )
    {
        this.parent = parent;

        composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
        GridLayout gl = new GridLayout();
        gl.marginHeight = 2;
        gl.marginWidth = 2;
        composite.setLayout( gl );
        // Setting the default width and height of the composite to 0
        GridData compositeGridData = new GridData( SWT.NONE, SWT.NONE, false, false );
        compositeGridData.heightHint = 0;
        compositeGridData.widthHint = 0;
        composite.setLayoutData( compositeGridData );

        innerComposite = null;
    }


    // ── R2 PLUGS IN AND DEPLOYS HIS QUERY INTERFACE ──────────────────────────
    // R2 extends his probe arm and jacks into the Death Star terminal. A row of
    // controls appears: attribute field, operator picker (=, !=, <=, >=, ~=),
    // value field, scope toggle (one-level vs subtree), and a Run button.
    // He also registers a selection listener so the controls grey out if the
    // user deselects an entry in the browser tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates all the inner SWT controls: attribute combo with content-assist,
     * operator combo, value combo, scope toggle button, and run button.
     * Also registers a post-selection listener on the browser viewer so the
     * controls disable when no entry is selected.
     * This method is called by {@link #setActive(boolean)} when showing the bar.
     *
     * <p>For example — R2 jacks in and deploys his full query interface:</p>
     * <pre>
     *   attributeCombo.show();   // with schema-driven auto-complete
     *   operatorCombo.show();    // =, !=, <=, >=, ~=
     *   valueCombo.show();       // with history auto-complete
     *   scopeToggle.show();      // one-level or subtree
     *   runButton.show();        // fires the search
     *   viewer.addSelectionListener(enableDisableControls);
     * </pre>
     */
    private void create()
    {
        this.browserWidget.getViewer().addPostSelectionChangedListener( selectionListener );

        IDialogSettings dialogSettings = BrowserCommonActivator.getDefault().getDialogSettings();

        // Reseting the layout of the composite to be displayed correctly
        GridData compositeGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        composite.setLayoutData( compositeGridData );

        innerComposite = BaseWidgetUtils.createColumnContainer( composite, 5, 1 );

        String[] attributes = HistoryUtils.load( dialogSettings, ATTRIBUTE_HISTORY_DIALOGSETTING_KEY );
        quickSearchAttributeCombo = BaseWidgetUtils.createCombo( innerComposite, attributes, -1, 1 );
        quickSearchAttributePP = new ListContentProposalProvider( attributes );
        new ExtendedContentAssistCommandAdapter( quickSearchAttributeCombo, new ComboContentAdapter(),
            quickSearchAttributePP, null, null, true );
        quickSearchAttributeCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                quickSearchRunButton.setEnabled( !"".equals( quickSearchAttributeCombo.getText() ) ); //$NON-NLS-1$
            }
        } );
        quickSearchAttributeCombo.addSelectionListener( new SelectionAdapter()
        {
            public void widgetDefaultSelected( SelectionEvent e )
            {
                performSearch();
            }
        } );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.widthHint = 50;
        quickSearchAttributeCombo.setLayoutData( gd );

        String[] operators = new String[]
            { "=", "!=", "<=", ">=", "~=" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        quickSearchOperatorCombo = BaseWidgetUtils.createReadonlyCombo( innerComposite, operators, 0, 1 );
        GridData data = new GridData();
        quickSearchOperatorCombo.setLayoutData( data );

        String[] values = HistoryUtils.load( dialogSettings, VALUE_HISTORY_DIALOGSETTING_KEY );
        quickSearchValueCombo = BaseWidgetUtils.createCombo( innerComposite, values, -1, 1 );
        quickSearchValuePP = new ListContentProposalProvider( values );
        new ExtendedContentAssistCommandAdapter( quickSearchValueCombo, new ComboContentAdapter(), quickSearchValuePP,
            null, null, true );
        quickSearchValueCombo.addSelectionListener( new SelectionAdapter()
        {
            public void widgetDefaultSelected( SelectionEvent e )
            {
                performSearch();
            }
        } );
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.widthHint = 50;
        quickSearchValueCombo.setLayoutData( gd );

        quickSearchScopeButton = new Button( innerComposite, SWT.TOGGLE );
        quickSearchScopeButton.setToolTipText( Messages.getString( "BrowserQuickSearchWidget.ScopeOneLevelToolTip" ) ); //$NON-NLS-1$
        quickSearchScopeButton.setImage( BrowserCommonActivator.getDefault().getImage(
            BrowserCommonConstants.IMG_SUBTREE ) );
        quickSearchScopeButton.setSelection( BrowserCommonActivator.getDefault().getPreferenceStore()
            .getBoolean( BrowserCommonConstants.PREFERENCE_BROWSER_QUICK_SEARCH_SUBTREE_SCOPE ) );
        quickSearchScopeButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                String one = Messages.getString( "BrowserQuickSearchWidget.ScopeOneLevelToolTip" ); //$NON-NLS-1$
                String sub = Messages.getString( "BrowserQuickSearchWidget.ScopeSubtreeToolTip" ); //$NON-NLS-1$
                boolean selected = quickSearchScopeButton.getSelection();
                quickSearchScopeButton.setToolTipText( selected ? sub : one );
                BrowserCommonActivator.getDefault().getPreferenceStore()
                    .setValue( BrowserCommonConstants.PREFERENCE_BROWSER_QUICK_SEARCH_SUBTREE_SCOPE, selected );
            }
        } );

        quickSearchRunButton = new Button( innerComposite, SWT.PUSH );
        quickSearchRunButton.setToolTipText( Messages.getString( "BrowserQuickSearchWidget.RunQuickSearch" ) ); //$NON-NLS-1$
        quickSearchRunButton.setImage( BrowserCommonActivator.getDefault().getImage(
            BrowserCommonConstants.IMG_QUICKSEARCH ) );
        quickSearchRunButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                performSearch();
            }
        } );

        setEnabled( getSelectedEntry() != null );

        composite.layout( true, true );
        parent.layout( true, true );
    }


    private void performSearch()
    {
        if ( !quickSearchRunButton.isEnabled() )
        {
            return;
        }

        IEntry entry = getSelectedEntry();
        if ( entry == null )
        {
            return;
        }

        IDialogSettings dialogSettings = BrowserCommonActivator.getDefault().getDialogSettings();

        HistoryUtils.save( dialogSettings, ATTRIBUTE_HISTORY_DIALOGSETTING_KEY, quickSearchAttributeCombo.getText() );
        String[] attributes = HistoryUtils.load( dialogSettings, ATTRIBUTE_HISTORY_DIALOGSETTING_KEY );
        quickSearchAttributeCombo.setItems( attributes );
        quickSearchAttributeCombo.select( 0 );
        HistoryUtils.save( dialogSettings, VALUE_HISTORY_DIALOGSETTING_KEY, quickSearchValueCombo.getText() );
        String[] values = HistoryUtils.load( dialogSettings, VALUE_HISTORY_DIALOGSETTING_KEY );
        quickSearchValueCombo.setItems( values );
        quickSearchValueCombo.select( 0 );
        quickSearchValuePP.setProposals( Arrays.asList( values ) );

        IBrowserConnection conn = entry.getBrowserConnection();

        QuickSearch quickSearch = new QuickSearch( entry, conn );
        quickSearch.getSearchParameter().setScope(
            quickSearchScopeButton.getSelection() ? SearchScope.SUBTREE : SearchScope.ONELEVEL );

        StringBuffer filter = new StringBuffer();
        filter.append( "(" ); //$NON-NLS-1$
        if ( "!=".equals( quickSearchOperatorCombo.getText() ) ) //$NON-NLS-1$
        {
            filter.append( "!(" ); //$NON-NLS-1$
        }
        filter.append( quickSearchAttributeCombo.getText() );
        filter
            .append( Messages.getString( "BrowserQuickSearchWidget.9" ).equals( quickSearchOperatorCombo.getText() ) ? "=" : quickSearchOperatorCombo.getText() ); //$NON-NLS-1$ //$NON-NLS-2$

        // only escape '\', '(', ')', and ' '
        // don't escape '*' to allow substring search
        String value = quickSearchValueCombo.getText();
        value = value.replaceAll( "\\\\", "\\\\5c" ); //$NON-NLS-1$ //$NON-NLS-2$
        value = value.replaceAll( " ", "\\\\00" ); //$NON-NLS-1$ //$NON-NLS-2$
        value = value.replaceAll( "\\(", "\\\\28" ); //$NON-NLS-1$ //$NON-NLS-2$
        value = value.replaceAll( "\\)", "\\\\29" ); //$NON-NLS-1$ //$NON-NLS-2$
        filter.append( value );
        if ( "!=".equals( quickSearchOperatorCombo.getText() ) ) //$NON-NLS-1$
        {
            filter.append( ")" ); //$NON-NLS-1$
        }
        filter.append( ")" ); //$NON-NLS-1$
        quickSearch.getSearchParameter().setFilter( filter.toString() );

        // set new quick search
        conn.setQuickSearch( quickSearch );

        // execute quick search
        new StudioBrowserJob( new SearchRunnable( new ISearch[]
            { quickSearch } ) ).execute();
    }


    private IEntry getSelectedEntry()
    {
        ISelection selection = browserWidget.getViewer().getSelection();
        IEntry[] entries = BrowserSelectionUtils.getEntries( selection );
        ISearch[] searches = BrowserSelectionUtils.getSearches( selection );
        if ( entries != null && entries.length == 1 )
        {
            IEntry entry = entries[0];
            return entry;
        }
        else if ( searches != null && searches.length == 1 && ( searches[0] instanceof IQuickSearch ) )
        {
            IQuickSearch quickSearch = ( IQuickSearch ) searches[0];
            IEntry entry = quickSearch.getSearchBaseEntry();
            return entry;
        }
        else
        {
            return null;
        }
    }


    // ── R2 RETRACTS HIS PROBE AND COLLAPSES THE INTERFACE ────────────────────
    // R2 finishes his query, retracts the probe arm, and rolls back from the
    // terminal. The display collapses back to zero height as if it were never
    // there. The selection listener is removed since there's nothing to enable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Destroys the inner SWT controls and collapses the composite to zero size.
     * Called by {@link #setActive(boolean)} when hiding the quick search bar.
     * We remove the selection listener and dispose the inner composite to free
     * OS resources, then trigger a layout pass so the tree expands back up.
     *
     * <p>For example — R2 retracts his probe and the terminal interface disappears:</p>
     * <pre>
     *   terminal.removeSelectionListener(R2.handler);
     *   R2.retractProbeArm();
     *   terminal.collapseTo(height=0, width=0);
     *   parent.relayout();
     * </pre>
     */
    private void destroy()
    {
        browserWidget.getViewer().removePostSelectionChangedListener( selectionListener );

        // Reseting the layout of the composite with a width and height set to 0
        GridData compositeGridData = new GridData( SWT.NONE, SWT.NONE, false, false );
        compositeGridData.heightHint = 0;
        compositeGridData.widthHint = 0;
        composite.setLayoutData( compositeGridData );

        innerComposite.dispose();
        innerComposite = null;

        composite.layout( true, true );
        parent.layout( true, true );
    }


    // ── R2 FULLY DISCONNECTS AND RELEASES THE PORT ────────────────────────────
    // When the Falcon leaves the Death Star for good, R2 fully disconnects from
    // the terminal, releases all references, and powers down permanently. This
    // is not just hiding the controls — it's the final cleanup that frees
    // everything so the garbage collector can do its job.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fully disposes this widget and releases all SWT resources.
     * Call this when the parent browser widget is being disposed.
     * Unlike {@link #setActive(boolean) setActive(false)} which just hides the controls,
     * this method permanently destroys the outer composite and nulls all field references.
     *
     * <p>For example — R2 fully disconnects as the Falcon exits the Death Star forever:</p>
     * <pre>
     *   R2.retractAllCables();
     *   R2.powerDown();
     *   R2.nullAllReferences();   // GC can now collect everything
     * </pre>
     */
    public void dispose()
    {
        if ( browserWidget != null )
        {
            quickSearchAttributeCombo = null;
            quickSearchOperatorCombo = null;
            quickSearchValueCombo = null;
            quickSearchRunButton = null;
            innerComposite = null;
            composite.dispose();
            composite = null;
            parent = null;
            browserWidget = null;
        }
    }


    // ── R2 DIMS OR LIGHTS UP THE CONTROLS BASED ON SELECTION ─────────────────
    // If no entry is selected in the browser tree, R2 dims all the terminal
    // controls — the operator knows there's nothing to search under. When a
    // valid entry gets selected, everything lights back up and is ready to go.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables all the quick-search input controls.
     * When no entry is selected in the browser tree, we disable everything and show a
     * tooltip explaining why. When an entry is selected, we enable the controls.
     * The Run button is also disabled if the attribute field is empty.
     *
     * <p>For example — R2 dims or re-lights the terminal based on whether an entry is selected:</p>
     * <pre>
     *   if (noEntrySelected) {
     *     R2.dimControls();
     *     R2.showTooltip("Select an entry first");
     *   } else {
     *     R2.enableControls();
     *   }
     * </pre>
     *
     * @param enabled   true to enable all controls, false to disable and grey them out
     */
    private void setEnabled( boolean enabled )
    {
        if ( composite != null && !composite.isDisposed() )
        {
            composite.setEnabled( enabled );
        }
        if ( innerComposite != null && !innerComposite.isDisposed() )
        {
            innerComposite.setEnabled( enabled );
            quickSearchAttributeCombo.setEnabled( enabled );
            quickSearchOperatorCombo.setEnabled( enabled );
            quickSearchValueCombo.setEnabled( enabled );
            quickSearchScopeButton.setEnabled( enabled );
            quickSearchRunButton.setEnabled( enabled && !"".equals( quickSearchAttributeCombo.getText() ) ); //$NON-NLS-1$

            if ( !enabled )
            {
                quickSearchAttributeCombo.setToolTipText( null );
                quickSearchOperatorCombo.setToolTipText( null );
                quickSearchValueCombo.setToolTipText( null );
                parent.setToolTipText( Messages.getString( "BrowserQuickSearchWidget.DisabledToolTipText" ) ); //$NON-NLS-1$
            }
            else
            {
                quickSearchAttributeCombo.setToolTipText( Messages
                    .getString( "BrowserQuickSearchWidget.SearchAttribute" ) ); //$NON-NLS-1$
                quickSearchOperatorCombo
                    .setToolTipText( Messages.getString( "BrowserQuickSearchWidget.SearchOperator" ) ); //$NON-NLS-1$
                quickSearchValueCombo.setToolTipText( Messages.getString( "BrowserQuickSearchWidget.SearchValue" ) ); //$NON-NLS-1$
                parent.setToolTipText( null );
            }
        }
    }


    // ── R2 DEPLOYS OR RETRACTS THE TERMINAL PANEL ON COMMAND ─────────────────
    // When the Rebel operator asks R2 to start the quick search, he plugs in and
    // deploys the full terminal interface. When they ask him to stop, he retracts
    // everything and returns attention to the browser tree. This is the main
    // on/off switch for the quick-search bar.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Shows or hides the quick-search bar.
     * When {@code visible} is true and the bar is currently hidden, we call
     * {@link #create()} to build the inner controls, then update the attribute
     * proposals for the current connection and focus the attribute field.
     * When {@code visible} is false and the bar is currently shown, we call
     * {@link #destroy()} to tear it down and return focus to the browser tree.
     *
     * <p>For example — R2 deploys or retracts his terminal panel on command:</p>
     * <pre>
     *   R2.setActive(true);   // plugs in, loads schema, focuses attribute field
     *   R2.setActive(false);  // retracts, returns focus to tree
     * </pre>
     *
     * @param visible   true to show the quick-search bar, false to hide it
     */
    public void setActive( boolean visible )
    {
        if ( visible && innerComposite == null && composite != null )
        {
            create();
            Object input = browserWidget.getViewer().getInput();
            if ( input instanceof IBrowserConnection )
            {
                setInput( ( IBrowserConnection ) input );
            }
            else if ( input instanceof IEntry[] )
            {
                setInput( ( ( IEntry[] ) input )[0].getBrowserConnection() );
            }
            quickSearchAttributeCombo.setFocus();
        }
        else if ( !visible && innerComposite != null && composite != null )
        {
            destroy();
            browserWidget.getViewer().getTree().setFocus();
        }
    }


    // ── R2 SWAPS HIS ATTRIBUTE DICTIONARY FOR A NEW SERVER'S SCHEMA ──────────
    // When the Rebels connect to a different Death Star terminal (a different LDAP
    // server), R2 swaps his attribute auto-complete list — because the new server's
    // schema might have a completely different set of attribute types.
    // We update the proposal provider with the new connection's schema.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the attribute auto-complete proposals for a new active connection.
     * When the user switches to a different LDAP connection, this refreshes the
     * attribute name suggestions in the attribute combo to match the new schema.
     * If the connection is null, we clear the suggestions and disable the controls.
     *
     * <p>For example — R2 loads a new attribute dictionary when connecting to a different terminal:</p>
     * <pre>
     *   Collection&lt;AttributeType&gt; atds = newConnection.getSchema().getAttributeTypeDescriptions();
     *   R2.loadDictionary(SchemaUtils.getNames(atds));
     *   // Auto-complete now suggests the new server's attribute types
     * </pre>
     *
     * @param connection   the LDAP connection whose schema should drive attribute
     *                     auto-complete; pass null to clear suggestions and disable controls
     */
    public void setInput( IBrowserConnection connection )
    {
        if ( innerComposite != null && !innerComposite.isDisposed() )
        {
            String[] atdNames;
            if ( connection != null )
            {
                Collection<AttributeType> atds = connection.getSchema().getAttributeTypeDescriptions();
                atdNames = SchemaUtils.getNames( atds ).toArray( EMPTY );
            }
            else
            {
                atdNames = EMPTY;
                setEnabled( false );
            }
            quickSearchAttributePP.setProposals( Arrays.asList( atdNames ) );
        }
    }

}
