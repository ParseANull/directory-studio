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

package org.apache.directory.studio.schemaeditor.view.search;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.views.SearchView;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;


// ── CLASS: SearchPage — R2-D2 PLUGGING INTO THE DEATH STAR COMPUTER ──────────
// R2-D2 rolls up to the Death Star's central computer port, extends his probe,
// and plugs in. In seconds he has identified every data corridor on the station:
// detention level, reactor core, tractor beam controls — each a potential search
// scope. He sets his filters (search in aliases? OIDs? descriptions?), narrows
// the scope (attribute types only? object classes only? both?), and when Luke
// gives the word, he transmits the query and pipes the results straight to
// Princess Leia's cell coordinates.
// This class is that data probe: it builds the Search dialog UI, remembers
// previous queries, executes the search against the schema when the user
// clicks Search, and hands the results off to the SearchView to display.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse Search dialog page for the Schema Editor — the UI that appears
 * when a user opens Eclipse's Search dialog and switches to the Schema tab.
 * It lets the user type a search string, choose which schema element fields
 * to search in (aliases, OID, description, superiors, syntax, matching rules,
 * mandatory/optional attributes), and narrow the scope to attribute types,
 * object classes, or both.
 * When the user clicks Search, we fire the query at the schema model and
 * display the results in the {@link SearchView}.
 * Think of this class as R2-D2 at the Death Star terminal: it knows every
 * data port, can filter on any field, and delivers exactly what the Rebellion
 * needs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchPage extends DialogPage implements ISearchPage
{
    /** The SearchPageContainer */
    private ISearchPageContainer container;

    // UI Fields
    private Combo searchCombo;
    private Button aliasesButton;
    private Button oidButton;
    private Button descriptionButon;
    private Button superiorButton;
    private Button syntaxButton;
    private Button matchingRulesButton;
    private Button superiorsButton;
    private Button mandatoryAttributesButton;
    private Button optionalAttributesButton;
    private Button attributeTypesAndObjectClassesButton;
    private Button attributeTypesOnlyButton;
    private Button objectClassesOnly;

    /**
     * The set of schema element fields the user can search in.
     * Think of each value as a different data corridor R2-D2 can scan:
     * {@code ALIASES} is the name corridor, {@code OID} the identifier
     * corridor, {@code DESCRIPTION} the documentation corridor, and so on.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum SearchInEnum
    {
        ALIASES,
        OID,
        DESCRIPTION,
        SUPERIOR,
        SYNTAX,
        MATCHING_RULES,
        SUPERIORS,
        MANDATORY_ATTRIBUTES,
        OPTIONAL_ATTRIBUTES
    }


    // ── R2 LOCATES THE RIGHT PORT AND PLUGS IN ───────────────────────────────
    // R2-D2 rolls along the Death Star corridor, inspecting each data port
    // until he finds the right one. He extends his probe, establishes the
    // connection, and the search terminal springs to life — search field,
    // filter checkboxes, scope radio buttons, all ready for input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Search dialog UI inside the provided parent composite.
     * Eclipse calls this when the user opens the Search dialog and selects the
     * Schema Editor tab; we create the search string combo (with history),
     * the "Search In" group of checkboxes (aliases, OID, description, etc.),
     * the scope radio buttons, and then load previously saved settings so the
     * dialog opens in the state the user left it.
     *
     * <p>For example — R2 sets up the Death Star terminal:</p>
     * <pre>
     *   Search string: [____________________________v]  (combo with history)
     *   Search In:     [x] Aliases  [x] OID  [x] Description
     *                  For Attribute Types: [ ] Superior  [ ] Syntax
     *                  For Object Classes:  [ ] Superiors [ ] Mandatory attrs
     *   Scope:         (o) Both  ( ) Attribute Types Only  ( ) Object Classes Only
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides as the dialog page container
     */
    public void createControl( Composite parent )
    {
        parent.setLayout( new GridLayout() );

        // Search String Label
        Label searchStringLabel = new Label( parent, SWT.NONE );
        searchStringLabel.setText( Messages.getString( "SearchPage.SearchString" ) ); //$NON-NLS-1$
        searchStringLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Search Combo
        searchCombo = new Combo( parent, SWT.DROP_DOWN | SWT.BORDER );
        searchCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        searchCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent arg0 )
            {
                validate();
            }
        } );

        // Specific Scope Composite
        Composite searchInComposite = new Composite( parent, SWT.NONE );
        GridLayout SearchInLayout = new GridLayout( 3, true );
        SearchInLayout.marginBottom = 0;
        SearchInLayout.marginHeight = 0;
        SearchInLayout.marginLeft = 0;
        SearchInLayout.marginRight = 0;
        SearchInLayout.marginTop = 0;
        SearchInLayout.marginWidth = 0;
        searchInComposite.setLayout( SearchInLayout );
        searchInComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // Search In Group
        Group searchInGroup = new Group( searchInComposite, SWT.NONE );
        searchInGroup.setLayout( new GridLayout() );
        searchInGroup.setText( Messages.getString( "SearchPage.SearchIn" ) ); //$NON-NLS-1$
        searchInGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Aliases Button
        aliasesButton = new Button( searchInGroup, SWT.CHECK );
        aliasesButton.setText( Messages.getString( "SearchPage.Aliases" ) ); //$NON-NLS-1$

        // OID Button
        oidButton = new Button( searchInGroup, SWT.CHECK );
        oidButton.setText( Messages.getString( "SearchPage.OID" ) ); //$NON-NLS-1$

        // Description Button
        descriptionButon = new Button( searchInGroup, SWT.CHECK );
        descriptionButon.setText( Messages.getString( "SearchPage.Description" ) ); //$NON-NLS-1$

        // Attribute Types Group
        Group attributeTypesSearchInGroup = new Group( searchInComposite, SWT.NONE );
        attributeTypesSearchInGroup.setText( Messages.getString( "SearchPage.SearchInForAttribute" ) ); //$NON-NLS-1$
        attributeTypesSearchInGroup.setLayout( new GridLayout() );
        attributeTypesSearchInGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Superior Button
        superiorButton = new Button( attributeTypesSearchInGroup, SWT.CHECK );
        superiorButton.setText( Messages.getString( "SearchPage.Superior" ) ); //$NON-NLS-1$

        // Syntax Button
        syntaxButton = new Button( attributeTypesSearchInGroup, SWT.CHECK );
        syntaxButton.setText( Messages.getString( "SearchPage.Syntax" ) ); //$NON-NLS-1$

        // Matching Rules Button
        matchingRulesButton = new Button( attributeTypesSearchInGroup, SWT.CHECK );
        matchingRulesButton.setText( Messages.getString( "SearchPage.MatchingRules" ) ); //$NON-NLS-1$

        // Object Classes Group
        Group objectClassesSearchInGroup = new Group( searchInComposite, SWT.NONE );
        objectClassesSearchInGroup.setText( Messages.getString( "SearchPage.SearchInObject" ) ); //$NON-NLS-1$
        objectClassesSearchInGroup.setLayout( new GridLayout() );
        objectClassesSearchInGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Superiors Button
        superiorsButton = new Button( objectClassesSearchInGroup, SWT.CHECK );
        superiorsButton.setText( Messages.getString( "SearchPage.Superiors" ) ); //$NON-NLS-1$

        // Mandatory Attributes Button
        mandatoryAttributesButton = new Button( objectClassesSearchInGroup, SWT.CHECK );
        mandatoryAttributesButton.setText( Messages.getString( "SearchPage.MandatoryAttributes" ) ); //$NON-NLS-1$

        // Optional Attributes Button
        optionalAttributesButton = new Button( objectClassesSearchInGroup, SWT.CHECK );
        optionalAttributesButton.setText( Messages.getString( "SearchPage.OptionalAttributes" ) ); //$NON-NLS-1$

        // Scope Group
        Group scopeGroup = new Group( parent, SWT.NONE );
        scopeGroup.setText( Messages.getString( "SearchPage.Scope" ) ); //$NON-NLS-1$
        scopeGroup.setLayout( new GridLayout() );
        scopeGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Attribute Types and Object Classes
        attributeTypesAndObjectClassesButton = new Button( scopeGroup, SWT.RADIO );
        attributeTypesAndObjectClassesButton.setText( Messages.getString( "SearchPage.TypesAndClasses" ) ); //$NON-NLS-1$

        // Attribute Types Only
        attributeTypesOnlyButton = new Button( scopeGroup, SWT.RADIO );
        attributeTypesOnlyButton.setText( Messages.getString( "SearchPage.TypesOnly" ) ); //$NON-NLS-1$

        // Object Classes Only
        objectClassesOnly = new Button( scopeGroup, SWT.RADIO );
        objectClassesOnly.setText( Messages.getString( "SearchPage.ClassesOnly" ) ); //$NON-NLS-1$

        initSearchStringHistory();

        initSearchIn();

        initSearchScope();

        searchCombo.setFocus();

        super.setControl( parent );
    }


    // ── R2 LOADS HIS PREVIOUS QUERY HISTORY FROM MEMORY ─────────────────────
    // R2-D2 doesn't start every mission fresh — he has a memory bank of every
    // Death Star query he has run before, and he loads them into the terminal's
    // history dropdown so he can repeat them quickly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the search combo's dropdown list with previously run search
     * strings loaded from the plugin's dialog settings.
     * This gives the user a convenient history of recent searches so they
     * don't have to retype the same query every time they open the dialog.
     */
    private void initSearchStringHistory()
    {
        searchCombo.setItems( loadSearchStringHistory() );
    }


    // ── R2 SETS HIS SEARCH FIELD FILTERS ─────────────────────────────────────
    // R2-D2 calibrates which data corridors to scan: aliases only? OIDs too?
    // How about the description fields and the superior-type linkages?
    // He reads his last saved filter configuration from memory and activates
    // only the checkboxes that were on when he last ran a search.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Restores the "Search In" checkbox states from the plugin's saved dialog
     * settings, so the dialog opens with the same field filters the user
     * had selected last time.
     * For the three most common fields (aliases, OID, description) we default
     * to {@code true} if no saved value exists, because those are the fields
     * users almost always want to search.
     *
     * <p>For example — R2 loads his last filter configuration:</p>
     * <pre>
     *   settings.get("PREFS_SEARCH_IN_ALIASES") = null → aliasesButton checked (default on)
     *   settings.get("PREFS_SEARCH_IN_OID")     = true → oidButton checked
     *   settings.get("PREFS_SEARCH_IN_SUPERIOR") = false → superiorButton unchecked
     * </pre>
     */
    private void initSearchIn()
    {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES ) == null )
        {
            aliasesButton.setSelection( true );
        }
        else
        {
            aliasesButton.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES ) );
        }

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID ) == null )
        {
            oidButton.setSelection( true );
        }
        else
        {
            oidButton.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID ) );
        }

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION ) == null )
        {
            descriptionButon.setSelection( true );
        }
        else
        {
            descriptionButon.setSelection( settings
                .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION ) );
        }
        superiorButton.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIOR ) );
        syntaxButton.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SYNTAX ) );
        matchingRulesButton.setSelection( settings
            .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MATCHING_RULES ) );
        superiorsButton.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIORS ) );
        mandatoryAttributesButton.setSelection( settings
            .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MANDATORY_ATTRIBUTES ) );
        optionalAttributesButton.setSelection( settings
            .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OPTIONAL_ATTRIBUTES ) );
    }


    // ── R2 DEFINES THE SEARCH SCOPE — WHICH CORRIDORS TO ENTER ───────────────
    // Before probing the Death Star's data network, R2-D2 defines the scope:
    // scan all corridors (attribute types and object classes), only the
    // attribute-type wing, or only the object-class wing?
    // He loads his last saved scope from memory so the terminal opens exactly
    // as he left it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Restores the search scope radio-button state from the plugin's saved
     * dialog settings, so the dialog opens with the same scope the user
     * had selected last time.
     * Defaults to "both attribute types and object classes" if no saved scope
     * exists — the broadest search is the safest default.
     *
     * <p>For example — R2 loads his last scope configuration:</p>
     * <pre>
     *   settings.get("PREFS_SEARCH_PAGE_SCOPE") = null
     *     → attributeTypesAndObjectClassesButton selected (default)
     *   settings.getInt("PREFS_SEARCH_PAGE_SCOPE") = SCOPE_AT_ONLY
     *     → attributeTypesOnlyButton selected
     * </pre>
     */
    private void initSearchScope()
    {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SCOPE ) == null )
        {
            attributeTypesAndObjectClassesButton.setSelection( true );
        }
        else
        {
            switch ( settings.getInt( PluginConstants.PREFS_SEARCH_PAGE_SCOPE ) )
            {
                case PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC:
                    attributeTypesAndObjectClassesButton.setSelection( true );
                    break;
                case PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_ONLY:
                    attributeTypesOnlyButton.setSelection( true );
                    break;
                case PluginConstants.PREFS_SEARCH_PAGE_SCOPE_OC_ONLY:
                    objectClassesOnly.setSelection( true );
                    break;
            }
        }
    }


    // ── R2 EXECUTES THE SEARCH AND PIPES RESULTS TO THE REBELLION ────────────
    // R2-D2 has his filters set and his scope defined. He transmits the query
    // into the Death Star's data network and streams the results directly to
    // Princess Leia's cell coordinates — in our case, to the SearchView panel
    // so the user can see which schema elements matched.
    // If the terminal can't open the SearchView (a PartInitException), R2
    // beeps an error and C-3PO logs the complaint.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires the search when the user clicks the Search button in the Eclipse
     * Search dialog, collecting the selected filter and scope settings, opening
     * the SearchView, and handing it the query so it can display the results.
     * If the SearchView cannot be opened (unusual, but possible if the view
     * is not registered or there is a workbench problem), we log the error and
     * show a dialog so the user knows what went wrong.
     *
     * <p>For example — R2 executes the query and streams results:</p>
     * <pre>
     *   searchString = "person"
     *   searchIn     = [ALIASES, OID, DESCRIPTION]
     *   scope        = SCOPE_AT_AND_OC
     *   → SearchView opens and shows all schema elements matching "person"
     * </pre>
     *
     * @return  {@code true} always; Eclipse uses this to close the Search dialog
     */
    public boolean performAction()
    {
        // Search In
        List<SearchInEnum> searchIn = new ArrayList<SearchInEnum>();
        if ( aliasesButton.getSelection() )
        {
            searchIn.add( SearchInEnum.ALIASES );
        }
        if ( oidButton.getSelection() )
        {
            searchIn.add( SearchInEnum.OID );
        }
        if ( descriptionButon.getSelection() )
        {
            searchIn.add( SearchInEnum.DESCRIPTION );
        }
        if ( superiorButton.getSelection() )
        {
            searchIn.add( SearchInEnum.SUPERIOR );
        }
        if ( syntaxButton.getSelection() )
        {
            searchIn.add( SearchInEnum.SYNTAX );
        }
        if ( matchingRulesButton.getSelection() )
        {
            searchIn.add( SearchInEnum.MATCHING_RULES );
        }
        if ( superiorsButton.getSelection() )
        {
            searchIn.add( SearchInEnum.SUPERIORS );
        }
        if ( mandatoryAttributesButton.getSelection() )
        {
            searchIn.add( SearchInEnum.MANDATORY_ATTRIBUTES );
        }
        if ( optionalAttributesButton.getSelection() )
        {
            searchIn.add( SearchInEnum.OPTIONAL_ATTRIBUTES );
        }

        // Scope
        int scope = 0;
        if ( attributeTypesAndObjectClassesButton.getSelection() )
        {
            scope = PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC;
        }
        else if ( attributeTypesOnlyButton.getSelection() )
        {
            scope = PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_ONLY;
        }
        else if ( objectClassesOnly.getSelection() )
        {
            scope = PluginConstants.PREFS_SEARCH_PAGE_SCOPE_OC_ONLY;
        }

        // Opening the SearchView and displaying the results
        try
        {
            SearchView searchView = ( SearchView ) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().showView( SearchView.ID );
            searchView.setSearchInput( searchCombo.getText(), searchIn.toArray( new SearchInEnum[0] ), scope );
        }
        catch ( PartInitException e )
        {
            PluginUtils.logError( Messages.getString( "SearchPage.ErrorOpeningView" ), e ); //$NON-NLS-1$
            ViewUtils.displayErrorMessageDialog(
                Messages.getString( "SearchPage.Error" ), Messages.getString( "SearchPage.ErrorOpeningView" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return true;
    }


    // ── R2 REGISTERS WITH THE DEATH STAR SEARCH INFRASTRUCTURE ───────────────
    // Before R2-D2 can use the terminal, he has to register with the facility's
    // search infrastructure — handing over a reference to the container that
    // will manage his session and trigger the actual search when he's ready.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to give us a reference to the search dialog container
     * that hosts this page.
     * We store it so we can call {@link ISearchPageContainer#setPerformActionEnabled}
     * during validation, which enables or disables the Search button based on
     * whether the user has typed a query string.
     *
     * @param container  the search dialog container that manages this page;
     *                   we call back into it during validation
     */
    public void setContainer( ISearchPageContainer container )
    {
        this.container = container;
    }


    // ── R2 ACTIVATES HIS DISPLAY PANEL ───────────────────────────────────────
    // R2-D2 lights up his projection dome whenever his panel becomes visible —
    // he wants the validation state to be correct the moment the user sees him,
    // not in some stale state from the last time he was visible.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this page becomes visible or hidden in the Search
     * dialog (e.g. the user switches tabs).
     * We run {@link #validate} when becoming visible so the Search button is
     * enabled/disabled correctly based on the current combo text, then delegate
     * to the parent for the actual show/hide mechanics.
     *
     * @param visible  {@code true} if this page is becoming visible,
     *                 {@code false} if it is being hidden
     */
    public void setVisible( boolean visible )
    {
        validate();
        super.setVisible( visible );
    }


    // ── R2 CHECKS THAT HIS QUERY ISN'T EMPTY BEFORE TRANSMITTING ─────────────
    // R2-D2 never transmits a blank query — that would return the entire Death
    // Star database. He checks that the search field is non-empty before giving
    // the all-clear.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the search combo contains a non-empty string,
     * meaning there is something worth searching for.
     * We use this to decide whether the Search button should be enabled; an
     * empty search string would produce a meaningless result against the entire
     * schema, so we block it here.
     *
     * @return  {@code true} if the combo text is non-null and non-empty
     */
    private boolean isValid()
    {
        return ( ( searchCombo.getText() != null ) && ( !"".equals( searchCombo.getText() ) ) ); //$NON-NLS-1$
    }


    // ── R2 VALIDATES HIS DATA PACKET BEFORE SENDING ──────────────────────────
    // R2-D2 does a quick integrity check on his outgoing data packet — if it
    // is empty, he signals the terminal to lock out the "Transmit" button so
    // the user can't fire a blank query.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the Search button in the Eclipse Search dialog based
     * on whether the current search combo text is non-empty.
     * We call this every time the combo text changes and every time the page
     * becomes visible so the button state always reflects the actual input.
     */
    private void validate()
    {
        container.setPerformActionEnabled( isValid() );
    }


    // ── R2 LOGS THE QUERY IN HIS INTERNAL DATA BANK ──────────────────────────
    // After a successful search, R2-D2 records the query string in his memory
    // banks — bumping it to the top of the list if it already exists, pruning
    // the oldest entry if the list grows beyond 10, so his history stays
    // compact and relevant.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a new search string to the top of the search history, moving it
     * there if it already exists, and trims the list to a maximum of 10
     * entries so it doesn't grow without bound.
     * The history is persisted in the plugin's dialog settings so it survives
     * across Eclipse sessions.
     *
     * <p>For example — R2 logs "inetOrgPerson" at the top:</p>
     * <pre>
     *   history before: ["person", "cn", "objectClass"]
     *   addSearchStringHistory("inetOrgPerson")
     *   history after:  ["inetOrgPerson", "person", "cn", "objectClass"]
     * </pre>
     *
     * @param value  the search string to add or promote to the top of history;
     *               must not be {@code null}
     */
    public static void addSearchStringHistory( String value )
    {
        // get current history
        String[] history = loadSearchStringHistory();
        List<String> list = new ArrayList<String>( Arrays.asList( history ) );

        // add new value or move to first position
        if ( list.contains( value ) )
        {
            list.remove( value );
        }
        list.add( 0, value );

        // check history size
        while ( list.size() > 10 )
        {
            list.remove( list.size() - 1 );
        }

        // save
        history = ( String[] ) list.toArray( new String[list.size()] );
        Activator.getDefault().getDialogSettings().put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_HISTORY, history );
    }


    // ── R2 PURGES A QUERY FROM MEMORY ────────────────────────────────────────
    // Sometimes a recorded query is no longer useful — the search string was
    // a mistake, or the schema element no longer exists. R2-D2 purges that
    // entry from his memory bank cleanly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific search string from the history if it is present,
     * then persists the updated list to the plugin's dialog settings.
     * This is a no-op if the value is not in the history.
     *
     * <p>For example — R2 purges a stale entry:</p>
     * <pre>
     *   history before: ["inetOrgPerson", "person", "cn"]
     *   removeSearchStringHistory("person")
     *   history after:  ["inetOrgPerson", "cn"]
     * </pre>
     *
     * @param value  the search string to remove from history; must not be {@code null}
     */
    public static void removeSearchStringHistory( String value )
    {
        // get current history
        String[] history = loadSearchStringHistory();
        List<String> list = new ArrayList<String>( Arrays.asList( history ) );

        // add new value or move to first position
        if ( list.contains( value ) )
        {
            list.remove( value );
        }

        // save
        history = ( String[] ) list.toArray( new String[list.size()] );
        Activator.getDefault().getDialogSettings().put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_HISTORY, history );
    }


    // ── R2 RETRIEVES HIS SEARCH LOG ──────────────────────────────────────────
    // R2-D2 opens his memory banks and streams out the full list of past query
    // strings so the search combo can populate its dropdown.
    // If no history has been saved yet — first run, or after a wipe — he
    // returns an empty array rather than null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads the saved search string history from the plugin's dialog settings
     * and returns it as a String array for use in the search combo dropdown.
     * Returns an empty array (never {@code null}) if no history has been saved
     * yet, so callers don't need a null check.
     *
     * <p>For example — R2 retrieves his search log on startup:</p>
     * <pre>
     *   settings saved: ["inetOrgPerson", "cn", "person"]
     *   loadSearchStringHistory() returns: ["inetOrgPerson", "cn", "person"]
     *
     *   no history saved yet:
     *   loadSearchStringHistory() returns: []
     * </pre>
     *
     * @return  an array of previously searched strings, newest first;
     *          never {@code null}, may be empty
     */
    public static String[] loadSearchStringHistory()
    {
        String[] history = Activator.getDefault().getDialogSettings().getArray(
            PluginConstants.PREFS_SEARCH_PAGE_SEARCH_HISTORY );
        if ( history == null )
        {
            history = new String[0];
        }
        return history;
    }


    // ── R2 LOADS HIS SEARCH FILTER CONFIGURATION ─────────────────────────────
    // R2-D2 reads his saved filter list from persistent storage — a record of
    // which data corridors were active last time he ran a search — and
    // reconstructs it as an enum list the rest of the code can work with.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads the saved "Search In" field selection from the plugin's dialog
     * settings and returns it as a {@link List} of {@link SearchInEnum} values.
     * This is used by other parts of the plugin (like the SearchView) to run
     * a programmatic search using the same filters the user last configured.
     * For the three common fields (aliases, OID, description) we default to
     * {@code true} if no saved value exists.
     *
     * <p>For example — R2 loads his last filter configuration:</p>
     * <pre>
     *   saved: aliases=true, OID=true, description=false, superior=false
     *   loadSearchIn() returns: [ALIASES, OID]
     * </pre>
     *
     * @return  a list of the {@link SearchInEnum} values representing the
     *          fields to search; never {@code null}, may be empty
     */
    public static List<SearchInEnum> loadSearchIn()
    {
        List<SearchInEnum> searchScope = new ArrayList<SearchInEnum>();
        IDialogSettings settings = Activator.getDefault().getDialogSettings();

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES ) == null )
        {
            searchScope.add( SearchInEnum.ALIASES );
        }
        else
        {
            if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES ) )
            {
                searchScope.add( SearchInEnum.ALIASES );
            }
        }

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID ) == null )
        {
            searchScope.add( SearchInEnum.OID );
        }
        else
        {
            if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID ) )
            {
                searchScope.add( SearchInEnum.OID );
            }
        }

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION ) == null )
        {
            searchScope.add( SearchInEnum.DESCRIPTION );
        }
        else
        {
            if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION ) )
            {
                searchScope.add( SearchInEnum.DESCRIPTION );
            }
        }
        if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIOR ) )
        {
            searchScope.add( SearchInEnum.SUPERIOR );
        }
        if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SYNTAX ) )
        {
            searchScope.add( SearchInEnum.SYNTAX );
        }
        if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MATCHING_RULES ) )
        {
            searchScope.add( SearchInEnum.MATCHING_RULES );
        }
        if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIORS ) )
        {
            searchScope.add( SearchInEnum.SUPERIORS );
        }
        if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MANDATORY_ATTRIBUTES ) )
        {
            searchScope.add( SearchInEnum.MANDATORY_ATTRIBUTES );
        }
        if ( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OPTIONAL_ATTRIBUTES ) )
        {
            searchScope.add( SearchInEnum.OPTIONAL_ATTRIBUTES );
        }

        return searchScope;
    }


    // ── R2 LOADS HIS CURRENT SCOPE SETTING ───────────────────────────────────
    // R2-D2 checks his navigation log to see which section of the Death Star
    // he last targeted — all sectors, attribute-type wing only, or
    // object-class wing only — and returns that setting so the search runs
    // against the right part of the schema.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads the saved search scope setting from the plugin's dialog settings
     * and returns its integer constant value.
     * Used by other components that need to re-run the last search
     * programmatically rather than through the dialog.
     * Defaults to "both attribute types and object classes" if no saved scope
     * exists.
     *
     * <p>For example — R2 loads his last scope setting:</p>
     * <pre>
     *   saved: SCOPE_AT_ONLY
     *   loadScope() returns: PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_ONLY
     *
     *   no saved scope:
     *   loadScope() returns: PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC
     * </pre>
     *
     * @return  one of the {@code PREFS_SEARCH_PAGE_SCOPE_*} constants from
     *          {@link PluginConstants}
     */
    public static int loadScope()
    {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SCOPE ) == null )
        {
            return PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC;
        }
        else
        {
            return settings.getInt( PluginConstants.PREFS_SEARCH_PAGE_SCOPE );
        }
    }


    // ── R2 WRITES THE SCOPE CONFIG TO PERSISTENT STORAGE ─────────────────────
    // After a successful search, R2-D2 writes his filter configuration to
    // persistent memory so the next time he's called up, he can pick up
    // exactly where he left off without any re-calibration.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the given "Search In" field list to the plugin's dialog settings
     * so it can be reloaded next time the Search dialog opens.
     * This is called after a search runs successfully so the dialog remembers
     * which fields were active.
     * A {@code null} or empty scope list is silently ignored — we won't write
     * a blank configuration that wipes out previously saved values.
     *
     * <p>For example — R2 writes the filter config to memory:</p>
     * <pre>
     *   scope = [ALIASES, OID, DESCRIPTION]
     *   → settings: SEARCH_IN_ALIASES=true, SEARCH_IN_OID=true,
     *               SEARCH_IN_DESCRIPTION=true, all others false
     * </pre>
     *
     * @param scope  the list of {@link SearchInEnum} values representing which
     *               fields are currently active; ignored if {@code null} or empty
     */
    public static void saveSearchScope( List<SearchInEnum> scope )
    {
        if ( ( scope != null ) && ( scope.size() > 0 ) )
        {
            IDialogSettings settings = Activator.getDefault().getDialogSettings();

            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES, scope.contains( SearchInEnum.ALIASES ) );
            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID, scope.contains( SearchInEnum.OID ) );
            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION, scope
                .contains( SearchInEnum.DESCRIPTION ) );
            settings
                .put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIOR, scope.contains( SearchInEnum.SUPERIOR ) );
            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SYNTAX, scope.contains( SearchInEnum.SYNTAX ) );
            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MATCHING_RULES, scope
                .contains( SearchInEnum.MATCHING_RULES ) );
            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIORS, scope
                .contains( SearchInEnum.SUPERIORS ) );
            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MANDATORY_ATTRIBUTES, scope
                .contains( SearchInEnum.MANDATORY_ATTRIBUTES ) );
            settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OPTIONAL_ATTRIBUTES, scope
                .contains( SearchInEnum.OPTIONAL_ATTRIBUTES ) );
        }
    }


    // ── R2 WIPES HIS SEARCH LOG CLEAN ────────────────────────────────────────
    // On rare occasions — say, after a security wipe or a fresh mission — R2
    // erases his entire search history and starts from a clean memory slate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clears the entire search string history from the plugin's dialog settings,
     * replacing it with an empty array.
     * Use this when the history needs to be reset — for example, from a "Clear
     * History" action or during testing.
     * After this call, {@link #loadSearchStringHistory} will return an empty
     * array until new searches are performed.
     *
     * <p>For example — R2 wipes his search log:</p>
     * <pre>
     *   history before: ["inetOrgPerson", "cn", "person"]
     *   clearSearchHistory()
     *   history after:  []
     * </pre>
     */
    public static void clearSearchHistory()
    {
        Activator.getDefault().getDialogSettings()
            .put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_HISTORY, new String[0] );
    }
}
