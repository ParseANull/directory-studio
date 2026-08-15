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

package org.apache.directory.studio.ldapbrowser.ui.search;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.SearchPageWrapper;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;


// ── CLASS: SearchPage — R2-D2 PLUGGING INTO THE DEATH STAR COMPUTER ──────────
// In the Death Star control room, R2-D2 rolls up to a computer terminal, extends
// his interface arm, and queries the station's database — probing for tractor beam
// controls, prison locations, whatever the mission needs. The results come back
// and he reports them to the team in real time.
// This class is that terminal interface: it's the Eclipse Search dialog page that
// lets users build an LDAP query, validates it as they type, and fires it off
// to the directory server when they press "Search."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The LDAP Search page shown inside Eclipse's standard Search dialog.
 * We implement {@link ISearchPage} so Eclipse includes us in the "Search > LDAP..."
 * dialog alongside any other search pages contributed by other plugins.
 * The actual search fields (base DN, filter, scope, attributes) are rendered by a
 * {@link SearchPageWrapper}; we just handle the container lifecycle and relay
 * validation state back to the container's "Search" button.
 * Think of R2-D2 at the terminal: we provide the interface, the wrapper handles
 * the actual query construction, and {@link #performAction} sends the signal.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchPage extends DialogPage implements ISearchPage, WidgetModifyListener
{

    /** The search page container. */
    private ISearchPageContainer container;

    /** The search. */
    private ISearch search;

    /** The search page wrapper. */
    private SearchPageWrapper spw;

    /** The error message label. */
    private Label errorMessageLabel;


    // ── R2 Reads His Own Identifier Off the Manifest ────────────────────────────
    // Before plugging into anything, R2 checks which terminal socket he's meant to
    // use — his station ID on the mission manifest.
    // We return the constant ID Eclipse uses to route the search dialog to this page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the unique ID of this LDAP search page.
     * Eclipse uses this to match the page registered in the plugin XML to the class
     * that should be instantiated when the Search dialog opens.
     *
     * @return  the search page ID from {@link BrowserUIConstants#SEARCH_PAGE_LDAP_SEARCH}
     */
    public static String getId()
    {
        return BrowserUIConstants.SEARCH_PAGE_LDAP_SEARCH;
    }


    // ── R2 Retracts His Interface Arm and Powers Down Gracefully ────────────────
    // When R2 is done with the terminal, he cleanly disconnects — no dangling
    // connections, no listeners left firing after he's rolled away.
    // We remove our widget-modify listener from the wrapper before calling super.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up when this search page is disposed.
     * We remove ourselves as a listener from the {@link SearchPageWrapper} before
     * delegating to the superclass, so we don't receive modification events after
     * the page's SWT controls have been destroyed.
     */
    public void dispose()
    {
        spw.removeWidgetModifyListener( this );
        super.dispose();
    }


    // ── R2 Powers Up in Default Mode, Ready for Any Terminal ────────────────────
    // R2 boots up with default configuration — no mission-specific title or image
    // loaded yet. He's ready to interface with whatever terminal the mission needs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SearchPage with no title or image.
     * Eclipse may use this no-arg constructor when instantiating the page from the
     * extension registry.
     */
    public SearchPage()
    {
    }


    // ── R2 Powers Up with a Mission Designation Pre-Loaded ──────────────────────
    // For specific missions, R2 is given a callsign to display on his dome panel
    // so the crew know which terminal he's at — the page title plays that role.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SearchPage with the given title shown in the dialog header.
     *
     * @param title  the title string displayed at the top of the search page
     */
    public SearchPage( String title )
    {
        super( title );
    }


    // ── R2 Powers Up with Both a Callsign and a Squadron Insignia ───────────────
    // For high-profile missions, R2 displays both his designation and the Rebel
    // Alliance insignia on his dome panel — title and image both pre-configured.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SearchPage with both a title and a header image.
     *
     * @param title  the title string displayed at the top of the search page
     * @param image  the image descriptor for the page's header icon
     */
    public SearchPage( String title, ImageDescriptor image )
    {
        super( title, image );
    }


    // ── R2 Transmits the Query to the Death Star Computer and Reports Back ───────
    // R2 translates the assembled query into the station's protocol, fires it off,
    // and confirms the results are coming back to Leia's mission recorder.
    // We save the search parameters from the UI into the ISearch model, register
    // it with the connection's search manager, and execute it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires the LDAP search when the user clicks the "Search" button.
     * We flush the current UI state into the {@link ISearch} model via
     * {@link SearchPageWrapper#saveToSearch}, register it with the connection's
     * search manager so it appears in the searches tree, and then delegate to
     * {@link SearchPageWrapper#performSearch} to actually run it.
     *
     * @return  {@code true} if the search was submitted successfully; {@code false} if
     *          there is no browser connection configured (nothing to query)
     */
    public boolean performAction()
    {
        spw.saveToSearch( search );
        if ( search.getBrowserConnection() != null )
        {
            search.getBrowserConnection().getSearchManager().addSearch( search );
            return spw.performSearch( search );
        }

        return false;
    }


    // ── R2 Acknowledges Which Terminal He's Been Assigned To ────────────────────
    // The mission coordinator tells R2 which console to report his status to —
    // R2 stores the reference so he can enable or disable the "Search" button
    // through the container's API as his validation state changes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to tell us which container (dialog) we're embedded in.
     * We store the reference so we can call {@link ISearchPageContainer#setPerformActionEnabled}
     * to control whether the Search button is enabled based on our validation state.
     *
     * @param container  the Eclipse search dialog container that hosts this page
     */
    public void setContainer( ISearchPageContainer container )
    {
        this.container = container;
    }


    // ── R2 Extends His Interface Arm and Connects to the Terminal ───────────────
    // R2 rolls into position, extends his arm, and interfaces with the terminal:
    // he reads the current context (what the user has selected), builds the query
    // form, pre-fills it with sensible defaults, and registers himself to receive
    // change notifications as the user edits.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT controls for this search page and populates them with defaults.
     * We derive a starting {@link ISearch} from the current workbench selection so the
     * base DN, connection, and scope are pre-filled intelligently. The
     * {@link SearchPageWrapper} creates the actual fields; we just lay out the container
     * and hook the error label and context-sensitive help.
     *
     * @param parent  the SWT composite provided by Eclipse as our root widget
     */
    public void createControl( Composite parent )
    {
        // declare search
        search = BrowserSelectionUtils.getExampleSearch( container.getSelection() );

        // create search page content
        GridLayout gl = new GridLayout();
        parent.setLayout( gl );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        // gd.heightHint =
        // convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        parent.setLayoutData( gd );

        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );
        spw = new SearchPageWrapper( SearchPageWrapper.NONE );
        spw.createContents( composite );
        spw.loadFromSearch( search );
        spw.addWidgetModifyListener( this );

        errorMessageLabel = BaseWidgetUtils.createLabel( parent, "", 3 ); //$NON-NLS-1$

        PlatformUI.getWorkbench().getHelpSystem().setHelp( composite,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_search_dialog" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_search_dialog" ); //$NON-NLS-1$ //$NON-NLS-2$

        super.setControl( parent );
    }


    // ── R2 Signals the Team When the Terminal Is Ready to Accept Commands ────────
    // As R2's panel lights up or goes dark, he broadcasts his status so the team
    // knows whether to hold fire or proceed. When we become visible we re-validate
    // and update the container's "Search" button state accordingly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this page becomes visible or hidden in the dialog.
     * We update the "Search" button state when becoming visible so it reflects the
     * current validity of the form — prevents the user clicking Search with an
     * invalid configuration.
     *
     * @param visible  {@code true} when this page is made visible; {@code false} when hidden
     */
    public void setVisible( boolean visible )
    {
        container.setPerformActionEnabled( spw.isValid() );
        super.setVisible( visible );
    }


    // ── R2 Updates His Status Lights as the User Adjusts the Query Parameters ───
    // Every time the user tweaks a field, R2's dome indicators change: a green light
    // means the query is valid, a red one means something is wrong. We relay the
    // validity and any error message to the container and the error label.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called whenever the user changes any field inside the {@link SearchPageWrapper}.
     * We re-check validity and push the result to the container (enabling or disabling
     * the Search button), then update the inline error label at the bottom of the page.
     *
     * @param event  the modification event from the search page wrapper (content not used directly)
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        container.setPerformActionEnabled( spw.isValid() );

        setErrorMessage( spw.getErrorMessage() );
        errorMessageLabel.setText( getErrorMessage() != null ? getErrorMessage() : "" ); //$NON-NLS-1$
    }

}
