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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.SearchPageWrapper;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.ui.PlatformUI;


// ── CLASS: BatchOperationApplyOnWizardPage — LUKE CHOOSES WHO TO RESCUE ──────
// At the Rebel briefing Luke has to answer: "Who is the target of this mission?
// The entries I have selected right now, or the results of a search I'll
// define?" This page answers that question for batch operations — the user
// can either pick from a dropdown of pre-selected entries/searches/attributes,
// or define a fresh search whose results become the target set.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * First page of the batch operation wizard: lets the user choose which LDAP
 * entries the operation will apply to.
 * The user can choose "current selection" (from a dropdown pre-populated with
 * whatever is selected in the browser) or "results of a search" (defined using
 * the embedded SearchPageWrapper).
 * Think of Luke at the Rebel briefing deciding who he's going to rescue:
 * "These specific people I know about" vs. "whoever matches this search criterion."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BatchOperationApplyOnWizardPage extends WizardPage
{

    private String[] initCurrentSelectionTexts;

    private Dn[][] initCurrentSelectionDns;

    private ISearch initSearch;

    private Button currentSelectionButton;

    private Combo currentSelectionCombo;

    private Button searchButton;

    private SearchPageWrapper spw;


    // ── Luke Checks His Mission Brief ────────────────────────────────────────────
    // Luke studies the briefing board — it shows who is already in the selection
    // and what default search parameters he can start from.
    // We pre-populate the current-selection dropdown and prepare a default search
    // from the workbench selection so the page starts in a useful state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BatchOperationApplyOnWizardPage, reading the current workbench
     * selection to pre-populate the "current selection" dropdown and the default
     * search parameters.
     * We call {@code prepareCurrentSelection()} and {@code prepareSearch()} in
     * the constructor so the data is ready before {@code createControl()} runs.
     *
     * @param pageName  the wizard page name (typically the class name).
     * @param wizard    the parent batch operation wizard.
     */
    public BatchOperationApplyOnWizardPage( String pageName, BatchOperationWizard wizard )
    {
        super( pageName );
        super.setTitle( Messages.getString( "BatchOperationApplyOnWizardPage.SelectApplicationEntries" ) ); //$NON-NLS-1$
        super.setDescription( Messages.getString( "BatchOperationApplyOnWizardPage.PleaseSelectEntries" ) ); //$NON-NLS-1$
        super.setPageComplete( false );

        this.prepareCurrentSelection();
        this.prepareSearch();
    }


    private void validate()
    {
        setPageComplete( getApplyOnDns() != null || spw.isValid() );
        setErrorMessage( searchButton.getSelection() ? spw.getErrorMessage() : null );
    }


    // ── Luke Sees the Mission Board ───────────────────────────────────────────────
    // The mission board shows two options: "use who we already have" or "run a
    // fresh search to find more targets". Luke picks one and the other dims out.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI: a "current selection" radio + dropdown (pre-populated
     * from the workbench selection), and a "results of search" radio + embedded
     * SearchPageWrapper for defining a new search.
     * Radio-button selection enables/disables the corresponding sub-widgets and
     * triggers validation.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {

        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        Composite applyOnGroup = composite;

        this.currentSelectionButton = BaseWidgetUtils.createRadiobutton( applyOnGroup, Messages
            .getString( "BatchOperationApplyOnWizardPage.CurrentSelection" ), 1 ); //$NON-NLS-1$
        this.currentSelectionButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                enableCurrentSelectionWidgets( currentSelectionButton.getSelection() );
                validate();
            }
        } );

        Composite currentSelectionComposite = BaseWidgetUtils.createColumnContainer( applyOnGroup, 2, 1 );
        BaseWidgetUtils.createRadioIndent( currentSelectionComposite, 1 );
        this.currentSelectionCombo = BaseWidgetUtils.createReadonlyCombo( currentSelectionComposite,
            this.initCurrentSelectionTexts, 0, 1 );
        this.currentSelectionCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        BaseWidgetUtils.createSpacer( applyOnGroup, 1 );
        BaseWidgetUtils.createSpacer( applyOnGroup, 1 );

        this.searchButton = BaseWidgetUtils.createRadiobutton( applyOnGroup, Messages
            .getString( "BatchOperationApplyOnWizardPage.ResultsOfSearch" ), 1 ); //$NON-NLS-1$
        this.searchButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                enableSearchWidgets( searchButton.getSelection() );
                validate();
            }
        } );

        Composite searchComposite = BaseWidgetUtils.createColumnContainer( applyOnGroup, 2, 1 );
        BaseWidgetUtils.createRadioIndent( searchComposite, 1 );
        Composite innerSearchComposite = BaseWidgetUtils.createColumnContainer( searchComposite, 3, 1 );
        this.spw = new SearchPageWrapper( SearchPageWrapper.NAME_INVISIBLE
            | SearchPageWrapper.REFERRALOPTIONS_FOLLOW_MANUAL_INVISIBLE
            | SearchPageWrapper.RETURNINGATTRIBUTES_INVISIBLE | SearchPageWrapper.REFERRALOPTIONS_READONLY );
        this.spw.createContents( innerSearchComposite );
        this.spw.loadFromSearch( this.initSearch );
        this.spw.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        this.currentSelectionButton.setSelection( this.currentSelectionCombo.getItemCount() > 0 );
        this.currentSelectionButton.setEnabled( this.currentSelectionCombo.getItemCount() > 0 );
        this.searchButton.setSelection( this.currentSelectionCombo.getItemCount() == 0 );
        this.enableCurrentSelectionWidgets( this.currentSelectionButton.getSelection() );
        this.enableSearchWidgets( this.searchButton.getSelection() );

        validate();

        setControl( composite );
    }


    // ── Luke Reads Off the Target DNs ────────────────────────────────────────────
    // Luke reads the list of known targets from the briefing board — if the
    // current-selection radio is active, the chosen DN array comes back;
    // otherwise null signals "use a search instead."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the array of target DNs selected from the "current selection" dropdown,
     * or {@code null} if the "results of a search" radio is selected instead.
     * The calling wizard uses this to decide whether to run a search or to use
     * the DNs directly when assembling the LDIF.
     *
     * @return  the selected DN array, or {@code null} if a search should be run.
     */
    public Dn[] getApplyOnDns()
    {
        if ( currentSelectionButton.getSelection() )
        {
            int index = currentSelectionCombo.getSelectionIndex();
            return initCurrentSelectionDns[index];
        }
        else
        {
            return null;
        }
    }


    // ── Luke Checks If a Search Was Defined ───────────────────────────────────────
    // If Luke chose "results of a search", the wizard needs to know which search
    // to run before it can build the LDIF.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search that was defined in the SearchPageWrapper, if the
     * "results of a search" radio is selected; otherwise returns {@code null}.
     * The wizard runs this search to resolve the target DN set.
     *
     * @return  the {@link ISearch}, or {@code null} if "current selection" is chosen.
     */
    public ISearch getApplyOnSearch()
    {
        if ( searchButton.getSelection() )
        {
            return this.initSearch;
        }
        else
        {
            return null;
        }
    }


    private void enableCurrentSelectionWidgets( boolean b )
    {
        currentSelectionCombo.setEnabled( b );
    }


    private void enableSearchWidgets( boolean b )
    {
        spw.setEnabled( b );
    }


    private void prepareSearch()
    {
        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
            .getSelection();
        this.initSearch = BrowserSelectionUtils.getExampleSearch( selection );
        this.initSearch.setName( null );

        // never follow referrals for a batch operation!
        this.initSearch.setReferralsHandlingMethod( Connection.ReferralHandlingMethod.IGNORE );
    }


    private void prepareCurrentSelection()
    {

        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
            .getSelection();
        ISearch[] searches = BrowserSelectionUtils.getSearches( selection );
        IEntry[] entries = BrowserSelectionUtils.getEntries( selection );
        ISearchResult[] searchResults = BrowserSelectionUtils.getSearchResults( selection );
        IBookmark[] bookmarks = BrowserSelectionUtils.getBookmarks( selection );
        IAttribute[] attributes = BrowserSelectionUtils.getAttributes( selection );
        IValue[] values = BrowserSelectionUtils.getValues( selection );

        List<String> textList = new ArrayList<String>();
        List<Dn[]> dnsList = new ArrayList<Dn[]>();

        if ( attributes.length + values.length > 0 )
        {
            Set<Dn> internalDnSet = new LinkedHashSet<Dn>();
            for ( int v = 0; v < values.length; v++ )
            {
                if ( values[v].isString() )
                {
                    try
                    {
                        Dn dn = new Dn( values[v].getStringValue() );
                        internalDnSet.add( dn );
                    }
                    catch ( LdapInvalidDnException e )
                    {
                    }
                }
            }

            for ( int a = 0; a < attributes.length; a++ )
            {
                IValue[] vals = attributes[a].getValues();
                for ( int v = 0; v < vals.length; v++ )
                {
                    if ( vals[v].isString() )
                    {
                        try
                        {
                            Dn dn = new Dn( vals[v].getStringValue() );
                            internalDnSet.add( dn );
                        }
                        catch ( LdapInvalidDnException e )
                        {
                        }
                    }
                }
            }

            if ( !internalDnSet.isEmpty() )
            {
                dnsList.add( internalDnSet.toArray( new Dn[internalDnSet.size()] ) );
                textList
                    .add( NLS
                        .bind(
                            Messages.getString( "BatchOperationApplyOnWizardPage.DNsOfSelectedAttributes" ), new Object[] { internalDnSet.size() } ) ); //$NON-NLS-1$
            }
        }
        if ( searches.length == 1 && searches[0].getSearchResults() != null )
        {
            Set<Dn> internalDnSet = new LinkedHashSet<Dn>();
            ISearchResult[] srs = searches[0].getSearchResults();
            for ( int i = 0; i < srs.length; i++ )
            {
                internalDnSet.add( srs[i].getDn() );
            }

            dnsList.add( internalDnSet.toArray( new Dn[internalDnSet.size()] ) );
            textList
                .add( NLS
                    .bind(
                        Messages.getString( "BatchOperationApplyOnWizardPage.SearchResultOf" ), new Object[] { searches[0].getName(), searches[0].getSearchResults().length } ) ); //$NON-NLS-1$
        }
        if ( entries.length + searchResults.length + bookmarks.length > 0 )
        {
            Set<Dn> internalDnSet = new LinkedHashSet<Dn>();
            for ( int i = 0; i < entries.length; i++ )
            {
                internalDnSet.add( entries[i].getDn() );
            }
            for ( int i = 0; i < searchResults.length; i++ )
            {
                internalDnSet.add( searchResults[i].getDn() );
            }
            for ( int i = 0; i < bookmarks.length; i++ )
            {
                internalDnSet.add( bookmarks[i].getDn() );
            }

            dnsList.add( internalDnSet.toArray( new Dn[internalDnSet.size()] ) );
            textList
                .add( NLS
                    .bind(
                        Messages.getString( "BatchOperationApplyOnWizardPage.SelectedEntries" ), new Object[] { internalDnSet.size() } ) ); //$NON-NLS-1$
        }

        this.initCurrentSelectionTexts = textList.toArray( new String[textList.size()] );
        this.initCurrentSelectionDns = dnsList.toArray( new Dn[0][0] );

    }


    // ── Luke Saves His Mission Notes ─────────────────────────────────────────────
    // Luke jots down the search parameters so they survive wizard restarts and
    // are available in performFinish().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the search page wrapper's current state back to the search object.
     * Called by the wizard in {@code performFinish()} so the search parameters
     * are available when resolving target DNs.
     */
    public void saveDialogSettings()
    {
        this.spw.saveToSearch( initSearch );
    }

}
