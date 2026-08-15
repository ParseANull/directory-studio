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

package org.apache.directory.studio.ldapbrowser.ui.views.searchlogs;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldifeditor.widgets.LdifEditorWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: SearchLogsView — THE IMPERIAL DATA VAULT UNDER REVIEW ──────────────
// Deep inside the Death Star, Imperial officers review scrolls of operational
// records — every search request and response captured for the Emperor's
// inspection. This view is that vault: it displays the raw LDIF search logs
// for whichever LDAP connection is currently selected, rendered read-only
// so they can be inspected but not accidentally modified.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The search logs view shows all LDAP search operations performed against
 * the currently selected connection, displayed as a read-only LDIF document.
 * When a different connection is selected in the connection view, the universal
 * listener automatically loads that connection's most recent search log file.
 * Think of it as the Imperial data vault: every search request is logged
 * by the connection layer and this view is the window into those records.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchLogsView extends ViewPart
{

    /** The action group. */
    private SearchLogsViewActionGroup actionGroup;

    /** The main widget. */
    private LdifEditorWidget mainWidget;

    /** The universal listener. */
    private SearchLogsViewUniversalListener universalListener;


    // ── The Vault Door Has a Known Address ───────────────────────────────────────
    // Every Imperial record room has a sector number — you can't navigate the
    // Death Star without one. Eclipse uses view IDs to find and activate views.
    // We return the constant that identifies this view in the workbench registry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse view ID for the search logs view.
     * Other parts of the UI (e.g., actions that open this view) use this ID
     * to locate and activate the view in the workbench.
     *
     * @return  the Eclipse view ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.VIEW_SEARCH_LOGS_VIEW;
    }


    // ── The Vault Is Prepared ────────────────────────────────────────────────────
    // An empty record room is set up before the records arrive —
    // the furniture is arranged and the lighting adjusted.
    // We do nothing but call super(); Eclipse will call createPartControl next.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SearchLogsView instance.
     * All real initialisation happens in {@link #createPartControl(Composite)};
     * this constructor just satisfies the Eclipse ViewPart contract.
     */
    public SearchLogsView()
    {
        super();
    }


    // ── The Officer Steps Up to the Terminal ─────────────────────────────────────
    // When an Imperial officer enters the vault, they sit at the main terminal
    // and direct focus there — the blinking cursor tells the system who is watching.
    // We forward keyboard focus to the LDIF viewer's text widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Moves keyboard focus into the LDIF source viewer so keyboard navigation
     * and copy commands work immediately after the view is activated.
     */
    public void setFocus()
    {
        mainWidget.getSourceViewer().getTextWidget().setFocus();
    }


    // ── The Vault Is Sealed ───────────────────────────────────────────────────────
    // When the Death Star is done, every system is powered down in order:
    // the consoles first, then the displays, then the door locks.
    // We dispose in the same orderly fashion: action group → listener → widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Shuts down the view in order: action group first (clears menu/toolbar
     * registrations), then the universal listener (unsubscribes from events),
     * then the main widget (releases SWT resources).
     * We guard with a null-check because dispose() can be called even if
     * createPartControl() never ran (e.g., the workbench is closing early).
     */
    public void dispose()
    {
        if ( mainWidget != null )
        {
            actionGroup.dispose();
            actionGroup = null;
            universalListener.dispose();
            universalListener = null;
            mainWidget.dispose();
            mainWidget = null;
        }
        super.dispose();
    }


    // ── The Vault Is Furnished and Opened ────────────────────────────────────────
    // Workers set up the displays, install the control panel, and wire in the
    // alert system — after this, the vault is ready for operation.
    // We create the LDIF widget (read-only display), the action group (toolbar
    // and menu buttons), and the universal listener (event subscriptions).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the view's UI: a zero-margin grid layout holding a read-only
     * {@link LdifEditorWidget}, then wires up the action group (toolbar, menu)
     * and the universal listener (event subscriptions).
     * The help context is registered so F1 in this view opens the right help page.
     *
     * @param parent  the parent composite provided by Eclipse.
     */
    public void createPartControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout( layout );

        // create main widget
        mainWidget = new LdifEditorWidget( null, "", false ); //$NON-NLS-1$
        mainWidget.createWidget( composite );
        mainWidget.getSourceViewer().setEditable( false );

        // create actions and context menu (and register global actions)
        actionGroup = new SearchLogsViewActionGroup( this );
        actionGroup.fillActionBars( getViewSite().getActionBars() );
        // this.actionGroup.fillContextMenu(this.configuration.getContextMenuManager(this.mainWidget.getViewer()));

        // create the listener
        universalListener = new SearchLogsViewUniversalListener( this );

        // set help context
        PlatformUI.getWorkbench().getHelpSystem().setHelp( mainWidget.getSourceViewer().getTextWidget(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_search_logs_view" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── The Officer Reads the Main Display ───────────────────────────────────────
    // The main terminal shows the LDIF records — other classes need access to
    // it for scrolling and document manipulation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDIF editor widget that displays the search log content.
     * Other parts of the view (universal listener, actions) use this to
     * manipulate the document, scroll the viewer, or read the LDIF model.
     *
     * @return  the main {@link LdifEditorWidget}.
     */
    public LdifEditorWidget getMainWidget()
    {
        return mainWidget;
    }


    // ── The Alert Wiring Is Accessible ───────────────────────────────────────────
    // The vault's alert system (event listener) can be queried and triggered
    // by external parties — the action group calls it to clear or refresh.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the universal listener that handles LDAP events and drives updates
     * to the view's content.
     * Actions call into the listener to trigger refreshes, navigation, and clears.
     *
     * @return  the {@link SearchLogsViewUniversalListener}.
     */
    public SearchLogsViewUniversalListener getUniversalListener()
    {
        return universalListener;
    }


    // ── The Control Panel Is Accessible ──────────────────────────────────────────
    // The action group manages all buttons and menus for the vault's control panel;
    // external code (like the listener) needs to push input changes through it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the action group that manages this view's toolbar and menu actions.
     * The universal listener uses this to propagate input changes to all actions
     * so their enabled/disabled states stay current.
     *
     * @return  the {@link SearchLogsViewActionGroup}.
     */
    public SearchLogsViewActionGroup getActionGroup()
    {
        return actionGroup;
    }

}
