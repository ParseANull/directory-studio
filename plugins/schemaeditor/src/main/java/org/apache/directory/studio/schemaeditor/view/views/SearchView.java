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

package org.apache.directory.studio.schemaeditor.view.views;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.controller.SearchViewController;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditorInput;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditorInput;
import org.apache.directory.studio.schemaeditor.view.search.SearchPage;
import org.apache.directory.studio.schemaeditor.view.search.SearchPage.SearchInEnum;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: SearchView — R2-D2 at the Death Star Computer ──────────────────────
// In A New Hope, R2-D2 plugs into the Death Star's computer terminal and runs a
// search: "I'm looking for the detention block where Princess Leia is being held."
// He probes the system with a query, scans for matching records across multiple
// fields (name, sector, cell number), and surfaces the result. Everything R2 does
// is a search: plug in a query, specify what fields to look in (aliases, OID,
// description, matching rules), optionally restrict scope (AT only, OC only, both),
// hit "search," and get a list of matching entries.
// This view is R2-D2's terminal. It has a search field, a "Search In" menu that
// lets you pick which LDAP schema fields to match against, a "Scope" menu for
// AT/OC/both, a search button that fires the actual regex match, and a results
// table that shows what was found. Double-click a result and the editor opens.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Search View lets users search for attribute types and object classes in the
 * loaded schema by name, OID, description, or any other field the LDAP schema object
 * exposes. The view builds a regex-based search from the entered string, applies it
 * across all checked "Search In" fields, filters by scope (AT only, OC only, or both),
 * and displays the sorted results in a table. Double-clicking or pressing Enter opens
 * the editor for the selected schema object. Think of it as R2-D2 plugged into the
 * Death Star computer — query in, matching records out.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchView extends ViewPart
{
    /** The view's ID */
    public static final String ID = PluginConstants.VIEW_SEARCH_VIEW_ID;

    /** The current Search String */
    private String searchString;

    // UI fields
    private Text searchField;
    private Button searchButton;
    private Label searchResultsLabel;
    private Table resultsTable;
    private TableViewer resultsTableViewer;
    private Composite searchFieldComposite;
    private Composite searchFieldInnerComposite;
    private Label separatorLabel;

    /** The parent composite */
    private Composite parent;


    // ── R2 Plugs Into the Terminal ────────────────────────────────────────────
    // When R2-D2 plugs into the Death Star terminal, he builds out the interface:
    // registers a help context, sets up the layout, creates the results label,
    // creates the table viewer, and initializes the search controller that will
    // respond to events. That's exactly what createPartControl does — it builds
    // the whole view and wires up its controller.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Search View UI and wires up its controller.
     * We register a dynamic help context, set up a zero-margin grid layout on the parent
     * composite, create a search results label, a separator, and the results table viewer.
     * A {@link SearchViewController} is instantiated last — it registers listeners on the
     * schema handler so the view refreshes when the schema changes.
     *
     * @param parent  the SWT composite provided by Eclipse to host this view
     */
    public void createPartControl( Composite parent )
    {
        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent, PluginConstants.PLUGIN_ID + "." + "search_view" ); //$NON-NLS-1$ //$NON-NLS-2$

        this.parent = parent;
        GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 0;
        parent.setLayout( gridLayout );

        // Search Field
        searchFieldComposite = new Composite( parent, SWT.NONE );
        gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 0;
        searchFieldComposite.setLayout( gridLayout );
        searchFieldComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        // This searchFieldCompositeSeparator is used to display correctly the searchFieldComposite,
        // since an empty composite does not display well.
        Label searchFieldCompositeSeparator = new Label( searchFieldComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
        GridData gridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        gridData.heightHint = 1;
        searchFieldCompositeSeparator.setLayoutData( gridData );
        searchFieldCompositeSeparator.setVisible( false );

        // Search Results Label
        searchResultsLabel = new Label( parent, SWT.NONE );
        searchResultsLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Separator Label
        Label separatorLabel2 = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        separatorLabel2.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Create the table
        createTableViewer();

        setSearchResultsLabel( null, 0 );

        new SearchViewController( this );
    }


    // ── R2 Opens the Query Interface ──────────────────────────────────────────
    // After plugging in, R2 opens the search input panel: a text field, toolbar
    // menus for "Search In" and "Scope", and a search button. When the search
    // field appears (from the search page or toolbar action), this is called to
    // materialize those controls inside the searchFieldComposite.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds and displays the search field row inside the view.
     * Called when the user opens a search from the search page or when the controller
     * decides to show the field. Creates the text input, "Search In" and "Scope"
     * drop-down toolbar items, and the search button. Attaches listeners that enable
     * the button only when text is present and trigger {@link #search()} on Enter or
     * button click. Also creates the separator below the field row.
     */
    private void createSearchField()
    {
        // Search Inner Composite
        searchFieldInnerComposite = new Composite( searchFieldComposite, SWT.NONE );
        GridLayout searchFieldInnerCompositeGridLayout = new GridLayout( 4, false );
        searchFieldInnerCompositeGridLayout.horizontalSpacing = 1;
        searchFieldInnerCompositeGridLayout.verticalSpacing = 1;
        searchFieldInnerCompositeGridLayout.marginHeight = 1;
        searchFieldInnerCompositeGridLayout.marginWidth = 2;
        searchFieldInnerComposite.setLayout( searchFieldInnerCompositeGridLayout );
        searchFieldInnerComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Search Label
        Label searchFieldLabel = new Label( searchFieldInnerComposite, SWT.NONE );
        searchFieldLabel.setText( Messages.getString( "SearchView.SearchColon" ) ); //$NON-NLS-1$
        searchFieldLabel.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, false, false ) );

        // Search Text Field
        searchField = new Text( searchFieldInnerComposite, SWT.BORDER | SWT.SEARCH | SWT.CANCEL );
        if ( searchString != null )
        {
            searchField.setText( searchString );
        }
        searchField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        searchField.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validateSearchField();
            }
        } );
        searchField.addKeyListener( new KeyAdapter()
        {
            public void keyReleased( KeyEvent e )
            {
                if ( e.keyCode == SWT.ARROW_DOWN )
                {
                    resultsTable.setFocus();
                }
                else if ( ( e.keyCode == Action.findKeyCode( "RETURN" ) ) || ( e.keyCode == SWT.KEYPAD_CR ) ) //$NON-NLS-1$
                {
                    search();
                }
            }
        } );

        // Search Toolbar
        final ToolBar searchToolBar = new ToolBar( searchFieldInnerComposite, SWT.HORIZONTAL | SWT.FLAT );
        // Creating the Search In ToolItem
        final ToolItem searchInToolItem = new ToolItem( searchToolBar, SWT.DROP_DOWN );
        searchInToolItem.setText( Messages.getString( "SearchView.SearchIn" ) ); //$NON-NLS-1$
        // Adding the action to display the Menu when the item is clicked
        searchInToolItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                Rectangle rect = searchInToolItem.getBounds();
                Point pt = new Point( rect.x, rect.y + rect.height );
                pt = searchToolBar.toDisplay( pt );

                Menu menu = createSearchInMenu();
                menu.setLocation( pt.x, pt.y );
                menu.setVisible( true );
            }
        } );

        new ToolItem( searchToolBar, SWT.SEPARATOR );

        final ToolItem scopeToolItem = new ToolItem( searchToolBar, SWT.DROP_DOWN );
        scopeToolItem.setText( Messages.getString( "SearchView.Scope" ) ); //$NON-NLS-1$
        // Adding the action to display the Menu when the item is clicked
        scopeToolItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                Rectangle rect = scopeToolItem.getBounds();
                Point pt = new Point( rect.x, rect.y + rect.height );
                pt = searchToolBar.toDisplay( pt );

                Menu menu = createScopeMenu();
                menu.setLocation( pt.x, pt.y );
                menu.setVisible( true );
            }
        } );
        searchToolBar.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, false, false ) );

        // Search Button
        searchButton = new Button( searchFieldInnerComposite, SWT.PUSH | SWT.DOWN );
        searchButton.setEnabled( false );
        searchButton.setImage( Activator.getDefault().getImage( PluginConstants.IMG_SEARCH ) );
        searchButton.setToolTipText( Messages.getString( "SearchView.Search" ) ); //$NON-NLS-1$
        searchButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                search();
            }
        } );
        searchButton.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, false, false ) );

        // Separator Label
        separatorLabel = new Label( searchFieldComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
        separatorLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── R2 Configures Which Fields to Probe ───────────────────────────────────
    // R2-D2 doesn't just search one field — he can probe by name, sector code,
    // cell number, or description, and the Rebel operators choose which fields
    // to include. createSearchInMenu builds the "Search In" drop-down with check
    // items for aliases, OID, description, superior, syntax, matching rules,
    // superiors, mandatory attributes, and optional attributes.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the "Search In" pop-up menu.
     * Each menu item is a checkbox that persists its state in the plugin's dialog
     * settings. The choices determine which LDAP schema fields the search regex is
     * applied against: aliases (names), OID, description, superior, syntax, matching
     * rules, superior OCs, mandatory attributes, and optional attributes. Pre-checked
     * based on saved dialog settings (aliases, OID, and description default to checked).
     *
     * @return  the populated pop-up menu, ready to be shown
     */
    public Menu createSearchInMenu()
    {
        final IDialogSettings settings = Activator.getDefault().getDialogSettings();

        // Creating the associated Menu
        Menu searchInMenu = new Menu( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.POP_UP );

        // Filling the menu
        // Aliases
        final MenuItem aliasesMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        aliasesMenuItem.setText( Messages.getString( "SearchView.Aliases" ) ); //$NON-NLS-1$
        aliasesMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES, aliasesMenuItem.getSelection() );
            }
        } );
        // OID
        final MenuItem oidMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        oidMenuItem.setText( Messages.getString( "SearchView.OID" ) ); //$NON-NLS-1$
        oidMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID, oidMenuItem.getSelection() );
            }
        } );
        // Description
        final MenuItem descriptionMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        descriptionMenuItem.setText( Messages.getString( "SearchView.Description" ) ); //$NON-NLS-1$
        descriptionMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION, descriptionMenuItem
                    .getSelection() );
            }
        } );
        // Separator
        new MenuItem( searchInMenu, SWT.SEPARATOR );
        // Superior
        final MenuItem superiorMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        superiorMenuItem.setText( Messages.getString( "SearchView.Superior" ) ); //$NON-NLS-1$
        superiorMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIOR, superiorMenuItem.getSelection() );
            }
        } );
        // Syntax
        final MenuItem syntaxMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        syntaxMenuItem.setText( Messages.getString( "SearchView.Syntax" ) ); //$NON-NLS-1$
        syntaxMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SYNTAX, syntaxMenuItem.getSelection() );
            }
        } );
        // Matching Rules
        final MenuItem matchingRulesMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        matchingRulesMenuItem.setText( Messages.getString( "SearchView.MatchingRules" ) ); //$NON-NLS-1$
        matchingRulesMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MATCHING_RULES, matchingRulesMenuItem
                    .getSelection() );
            }
        } );
        // Separator
        new MenuItem( searchInMenu, SWT.SEPARATOR );
        // Superiors
        final MenuItem superiorsMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        superiorsMenuItem.setText( Messages.getString( "SearchView.Superiors" ) ); //$NON-NLS-1$
        superiorsMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIORS, superiorsMenuItem.getSelection() );
            }
        } );
        // Mandatory Attributes
        final MenuItem mandatoryAttributesMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        mandatoryAttributesMenuItem.setText( Messages.getString( "SearchView.MandatoryAttributes" ) ); //$NON-NLS-1$
        mandatoryAttributesMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MANDATORY_ATTRIBUTES,
                    mandatoryAttributesMenuItem.getSelection() );
            }
        } );
        // Optional Attributes
        final MenuItem optionalAttributesMenuItem = new MenuItem( searchInMenu, SWT.CHECK );
        optionalAttributesMenuItem.setText( Messages.getString( "SearchView.OptionalAttributes" ) ); //$NON-NLS-1$
        optionalAttributesMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OPTIONAL_ATTRIBUTES,
                    optionalAttributesMenuItem.getSelection() );
            }
        } );

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES ) == null )
        {
            aliasesMenuItem.setSelection( true );
        }
        else
        {
            aliasesMenuItem.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_ALIASES ) );
        }

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID ) == null )
        {
            oidMenuItem.setSelection( true );
        }
        else
        {

            oidMenuItem.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OID ) );
        }

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION ) == null )
        {
            descriptionMenuItem.setSelection( true );
        }
        else
        {
            descriptionMenuItem.setSelection( settings
                .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_DESCRIPTION ) );
        }

        superiorMenuItem.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIOR ) );
        syntaxMenuItem.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SYNTAX ) );
        matchingRulesMenuItem.setSelection( settings
            .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MATCHING_RULES ) );
        superiorsMenuItem.setSelection( settings.getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_SUPERIORS ) );
        mandatoryAttributesMenuItem.setSelection( settings
            .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_MANDATORY_ATTRIBUTES ) );
        optionalAttributesMenuItem.setSelection( settings
            .getBoolean( PluginConstants.PREFS_SEARCH_PAGE_SEARCH_IN_OPTIONAL_ATTRIBUTES ) );

        return searchInMenu;
    }


    // ── R2 Selects the Search Scope ───────────────────────────────────────────
    // "Search the detention block, the entire base, or just the cargo bay?"
    // The Scope menu limits the search to attribute types only, object classes
    // only, or both. Three radio items, one saved state.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the "Scope" pop-up menu.
     * Three mutually exclusive radio items control whether the search applies to both
     * attribute types and object classes, attribute types only, or object classes only.
     * The chosen scope is persisted in dialog settings and pre-selected from saved state.
     * Defaults to "Attribute Types and Object Classes" if no setting has been saved.
     *
     * @return  the populated scope pop-up menu
     */
    public Menu createScopeMenu()
    {
        final IDialogSettings settings = Activator.getDefault().getDialogSettings();

        // Creating the associated Menu
        Menu scopeMenu = new Menu( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.POP_UP );

        // Filling the menu
        // Attribute Types And Object Classes
        final MenuItem attributeTypesAndObjectClassesMenuItem = new MenuItem( scopeMenu, SWT.RADIO );
        attributeTypesAndObjectClassesMenuItem.setText( Messages.getString( "SearchView.TypesAndClasses" ) ); //$NON-NLS-1$
        attributeTypesAndObjectClassesMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SCOPE,
                    PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC );
            }
        } );
        // Attributes Type Only
        final MenuItem attributesTypesOnlyMenuItem = new MenuItem( scopeMenu, SWT.RADIO );
        attributesTypesOnlyMenuItem.setText( Messages.getString( "SearchView.TypesOnly" ) ); //$NON-NLS-1$
        attributesTypesOnlyMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SCOPE, PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_ONLY );
            }
        } );
        // Object Classes Only
        final MenuItem objectClassesMenuItem = new MenuItem( scopeMenu, SWT.RADIO );
        objectClassesMenuItem.setText( Messages.getString( "SearchView.ClassesOnly" ) ); //$NON-NLS-1$
        objectClassesMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                settings.put( PluginConstants.PREFS_SEARCH_PAGE_SCOPE, PluginConstants.PREFS_SEARCH_PAGE_SCOPE_OC_ONLY );
            }
        } );

        if ( settings.get( PluginConstants.PREFS_SEARCH_PAGE_SCOPE ) == null )
        {
            attributeTypesAndObjectClassesMenuItem.setSelection( true );
        }
        else
        {
            switch ( settings.getInt( PluginConstants.PREFS_SEARCH_PAGE_SCOPE ) )
            {
                case PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC:
                    attributeTypesAndObjectClassesMenuItem.setSelection( true );
                    break;
                case PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_ONLY:
                    attributesTypesOnlyMenuItem.setSelection( true );
                    break;
                case PluginConstants.PREFS_SEARCH_PAGE_SCOPE_OC_ONLY:
                    objectClassesMenuItem.setSelection( true );
                    break;
            }
        }

        return scopeMenu;
    }


    // ── R2 Builds the Readout Screen ──────────────────────────────────────────
    // After plugging in, R2 opens the results display: a scrollable table with
    // a full-row selection model, wired to a JFace TableViewer with our content
    // and label providers, and with listeners for Enter-key and double-click to
    // open the associated editor.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates the results table and its JFace TableViewer.
     * The underlying SWT table uses single-selection, H/V scroll, full-row selection,
     * and hidden-selection mode. The viewer gets a {@link DecoratingLabelProvider}
     * wrapping {@link SearchViewLabelProvider} (so Eclipse decorator overlays still
     * work) and a {@link SearchViewContentProvider} for grouping/sorting. Listeners
     * on the table handle Enter-key presses and double-clicks to open editors.
     */
    private void createTableViewer()
    {
        // Creating the TableViewer
        resultsTable = new Table( parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
            | SWT.HIDE_SELECTION );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        resultsTable.setLayoutData( gridData );
        resultsTable.setLinesVisible( true );

        // Creating the TableViewer
        resultsTableViewer = new TableViewer( resultsTable );
        resultsTableViewer.setLabelProvider( new DecoratingLabelProvider( new SearchViewLabelProvider(), Activator
            .getDefault().getWorkbench().getDecoratorManager().getLabelDecorator() ) );
        resultsTableViewer.setContentProvider( new SearchViewContentProvider() );

        // Adding listeners
        resultsTable.addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent e )
            {
                if ( ( e.keyCode == Action.findKeyCode( "RETURN" ) ) || ( e.keyCode == SWT.KEYPAD_CR ) ) // return key //$NON-NLS-1$
                {
                    openEditor();
                }
            }
        } );

        resultsTableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                openEditor();
            }
        } );
    }


    // ── R2 Retrieves the Detention Block Plans ────────────────────────────────
    // "Found her. Opening the cell door now." R2 gets a match and immediately
    // opens the associated resource — the editor for the selected schema object.
    // openEditor picks up the current table selection and opens either the
    // AttributeTypeEditor or ObjectClassEditor, depending on what was selected.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Opens the schema editor for the currently selected table row.
     * Reads the viewer's current selection, determines whether it's an
     * {@link AttributeType} or {@link ObjectClass}, constructs the appropriate editor
     * input, and calls Eclipse's page API to open the editor. If the editor fails to
     * open (a {@link PartInitException}), we log the error and show an error dialog.
     * Does nothing if there's no selection or no schema handler.
     */
    private void openEditor()
    {
        if ( Activator.getDefault().getSchemaHandler() != null )
        {
            StructuredSelection selection = ( StructuredSelection ) resultsTableViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                Object item = selection.getFirstElement();

                IEditorInput input = null;
                String editorId = null;

                // Here is the double clicked item
                if ( item instanceof AttributeType )
                {
                    input = new AttributeTypeEditorInput( ( AttributeType ) item );
                    editorId = AttributeTypeEditor.ID;
                }
                else if ( item instanceof ObjectClass )
                {
                    input = new ObjectClassEditorInput( ( ObjectClass ) item );
                    editorId = ObjectClassEditor.ID;
                }

                // Let's open the editor
                if ( input != null )
                {
                    try
                    {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor( input,
                            editorId );
                    }
                    catch ( PartInitException exception )
                    {
                        PluginUtils.logError( Messages.getString( "SearchView.ErrorOpeningEditor" ), exception ); //$NON-NLS-1$
                        ViewUtils
                            .displayErrorMessageDialog(
                                Messages.getString( "SearchView.Error" ), Messages.getString( "SearchView.ErrorOpeningEditor" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
    }


    // ── R2 Stays Ready at the Keyboard ────────────────────────────────────────
    // R2 keeps the cursor in the search field so the user can type immediately.
    // If the field isn't visible, the results table gets focus as a fallback.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Gives focus to the search field, or to the results table if the field is hidden.
     * Eclipse calls this when the user switches to this view. We put focus in the
     * text field whenever it's visible so the user can start typing right away.
     */
    public void setFocus()
    {
        if ( searchField != null && !searchField.isDisposed() )
        {
            searchField.setFocus();
        }
        else
        {
            resultsTable.setFocus();
        }
    }


    // ── R2 Extends the Interface Panel ───────────────────────────────────────
    // When the Rebel operator calls up the search from the search page, R2 extends
    // the input panel so there's a text field to type into. showSearchFieldSection
    // materializes the search field row that was previously hidden, relays out the
    // view, and puts focus in the text field.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Shows the search-field row above the results table.
     * Called by the {@link SearchViewController} when the user initiates a search from
     * the search page (which pre-fills the field). Calls {@link #createSearchField()} to
     * build the controls, forces a layout refresh on the parent, then focuses the field
     * and validates its content to enable/disable the search button.
     */
    public void showSearchFieldSection()
    {
        createSearchField();
        parent.layout( true, true );
        searchField.setFocus();
        validateSearchField();
    }


    // ── R2 Retracts the Input Panel ───────────────────────────────────────────
    // When the search is complete and the field is no longer needed, R2 retracts
    // the panel — disposing the controls and relaying out the view so the results
    // table fills the space.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Hides and disposes the search-field row.
     * Disposes both the inner composite (containing the text field, toolbar, and button)
     * and the separator label below it. Forces a layout refresh on the parent so the
     * results table expands to fill the reclaimed space. Focus moves to the results table.
     */
    public void hideSearchFieldSection()
    {
        if ( searchFieldInnerComposite != null )
        {
            searchFieldInnerComposite.dispose();
            searchFieldInnerComposite = null;
        }
        if ( separatorLabel != null )
        {
            separatorLabel.dispose();
            separatorLabel = null;
        }
        parent.layout( true, true );
        resultsTable.setFocus();
    }


    // ── R2 Checks Whether the Query Is Ready ─────────────────────────────────
    // R2 won't start transmitting until the query has at least one character —
    // an empty query returns everything, which isn't useful. validateSearchField
    // enables the search button only when there's something to search for.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the search button based on whether the search field has content.
     * Called by the modify listener on the text field on every keystroke. The button is
     * useless when the field is empty, so we keep it disabled until there's at least
     * one character.
     */
    private void validateSearchField()
    {
        searchButton.setEnabled( searchField.getText().length() > 0 );
    }


    // ── R2 Receives External Search Input ────────────────────────────────────
    // The Rebel operator can feed R2 a pre-built query from the search page:
    // a search string, a list of fields to probe, and a scope. setSearchInput
    // saves the query, updates the search field if it's visible, runs the search,
    // and updates the results label and table.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Accepts a fully specified search and runs it.
     * Called by the {@link SearchViewController} when the user submits a search from
     * the search page. We save the search string (so we can re-run it later), push it
     * to the search history, update the text field if visible, run the actual match,
     * update the results label, and push the result list to the table viewer.
     *
     * @param searchString  the user's search string (may contain * and ? wildcards)
     * @param searchIn      array of {@link SearchInEnum} values indicating which fields to match
     * @param scope         one of the {@code PREFS_SEARCH_PAGE_SCOPE_*} constants (AT only, OC only, both)
     */
    public void setSearchInput( String searchString, SearchInEnum[] searchIn, int scope )
    {
        this.searchString = searchString;

        // Saving search String and Search Scope to dialog settings
        SearchPage.addSearchStringHistory( searchString );
        SearchPage.saveSearchScope( Arrays.asList( searchIn ) );

        if ( ( searchField != null ) && ( !searchField.isDisposed() ) )
        {
            searchField.setText( searchString );
            validateSearchField();
        }

        List<SchemaObject> results = search( searchString, searchIn, scope );
        setSearchResultsLabel( searchString, results.size() );
        resultsTableViewer.setInput( results );
    }


    // ── R2 Runs the Query Against the Death Star Database ────────────────────
    // "Searching... found it. Detention Block AA-23, Level 5." R2 converts the
    // user's search string into a regex (translating * and ? wildcards), loops
    // over all attribute types and object classes in the schema, and checks each
    // requested field against the pattern. Matches go into the results list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Runs the actual schema search and returns the list of matching objects.
     * We translate * to {@code [\S]*} and ? to {@code [\S]} to create a case-insensitive
     * regex pattern. We then loop over the attribute types and/or object classes loaded
     * in the {@link SchemaHandler}, checking each enabled "Search In" field against the
     * pattern. Each schema object is added at most once (we {@code continue} after the
     * first match to avoid duplicates).
     *
     * <p>For example — R2 probes the system:</p>
     * <pre>
     *   search("cn*", [ALIASES, DESCRIPTION], AT_AND_OC)
     *   → pattern = "cn[\S]*" (case-insensitive)
     *   → matches cn, cnAddress, commonName aliases → adds those ATs
     * </pre>
     *
     * @param searchString  the user's raw search string (with * and ? wildcards)
     * @param searchIn      which fields to match against
     * @param scope         whether to search ATs, OCs, or both
     * @return              the list of matching {@link SchemaObject} instances
     */
    private List<SchemaObject> search( String searchString, SearchInEnum[] searchIn, int scope )
    {
        List<SchemaObject> searchResults = new ArrayList<SchemaObject>();

        if ( searchString != null )
        {
            String computedSearchString = searchString.replaceAll( "\\*", "[\\\\S]*" ); //$NON-NLS-1$ //$NON-NLS-2$
            computedSearchString = computedSearchString.replaceAll( "\\?", "[\\\\S]" ); //$NON-NLS-1$ //$NON-NLS-2$

            Pattern pattern = Pattern.compile( computedSearchString, Pattern.CASE_INSENSITIVE );

            SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
            if ( schemaHandler != null )
            {
                List<SearchInEnum> searchScope = new ArrayList<SearchInEnum>( Arrays.asList( searchIn ) );

                if ( ( scope == PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC )
                    || ( scope == PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_ONLY ) )
                {
                    // Looping on attribute types
                    List<AttributeType> attributeTypes = schemaHandler.getAttributeTypes();
                    for ( AttributeType at : attributeTypes )
                    {
                        // Aliases
                        if ( searchScope.contains( SearchInEnum.ALIASES ) )
                        {
                            if ( checkList( pattern, at.getNames() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }
                        }

                        // OID
                        if ( searchScope.contains( SearchInEnum.OID ) )
                        {
                            if ( checkString( pattern, at.getOid() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }
                        }

                        // Description
                        if ( searchScope.contains( SearchInEnum.DESCRIPTION ) )
                        {
                            if ( checkString( pattern, at.getDescription() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }
                        }

                        // Superior
                        if ( searchScope.contains( SearchInEnum.SUPERIOR ) )
                        {
                            if ( checkString( pattern, at.getSuperiorOid() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }
                        }

                        // Syntax
                        if ( searchScope.contains( SearchInEnum.SYNTAX ) )
                        {
                            if ( checkString( pattern, at.getSyntaxOid() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }
                        }

                        // Matching Rules
                        if ( searchScope.contains( SearchInEnum.MATCHING_RULES ) )
                        {
                            // Equality
                            if ( checkString( pattern, at.getEqualityOid() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }

                            // Ordering
                            if ( checkString( pattern, at.getOrderingOid() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }

                            // Substring
                            if ( checkString( pattern, at.getSubstringOid() ) )
                            {
                                searchResults.add( at );
                                continue;
                            }
                        }
                    }
                }

                if ( ( scope == PluginConstants.PREFS_SEARCH_PAGE_SCOPE_AT_AND_OC )
                    || ( scope == PluginConstants.PREFS_SEARCH_PAGE_SCOPE_OC_ONLY ) )
                {
                    // Looping on object classes
                    List<ObjectClass> objectClasses = schemaHandler.getObjectClasses();
                    for ( ObjectClass oc : objectClasses )
                    {
                        // Aliases
                        if ( searchScope.contains( SearchInEnum.ALIASES ) )
                        {
                            if ( checkList( pattern, oc.getNames() ) )
                            {
                                searchResults.add( oc );
                                continue;
                            }
                        }

                        // OID
                        if ( searchScope.contains( SearchInEnum.OID ) )
                        {
                            if ( checkString( pattern, oc.getOid() ) )
                            {
                                searchResults.add( oc );
                                continue;
                            }
                        }

                        // Description
                        if ( searchScope.contains( SearchInEnum.DESCRIPTION ) )
                        {
                            if ( checkString( pattern, oc.getDescription() ) )
                            {
                                searchResults.add( oc );
                                continue;
                            }
                        }

                        // Superiors
                        if ( searchScope.contains( SearchInEnum.SUPERIORS ) )
                        {
                            if ( checkList( pattern, oc.getSuperiorOids() ) )
                            {
                                searchResults.add( oc );
                                continue;
                            }
                        }

                        // Mandatory Attributes
                        if ( searchScope.contains( SearchInEnum.MANDATORY_ATTRIBUTES ) )
                        {
                            if ( checkList( pattern, oc.getMustAttributeTypeOids() ) )
                            {
                                searchResults.add( oc );
                                continue;
                            }
                        }

                        // Optional Attributes
                        if ( searchScope.contains( SearchInEnum.OPTIONAL_ATTRIBUTES ) )
                        {
                            if ( checkList( pattern, oc.getMayAttributeTypeOids() ) )
                            {
                                searchResults.add( oc );
                                continue;
                            }
                        }
                    }
                }
            }
        }

        return searchResults;
    }


    // ── R2 Checks a Multi-Value Field ─────────────────────────────────────────
    // R2 probes a list of values — like scanning all the names (aliases) of an
    // attribute type. The moment one name matches the pattern, R2 reports a hit.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if any string in the list fully matches the given pattern.
     * We iterate through each string in {@code list} and test with {@code pattern.matcher(s).matches()}.
     * We use full-string matching (not find()) because the pattern already has wildcards
     * encoded. Returns {@code false} if the list is null or empty.
     *
     * @param pattern  the compiled regex pattern (with wildcards pre-translated)
     * @param list     the list of strings to test (may be null)
     * @return         {@code true} if any element in the list matches the pattern
     */
    private boolean checkList( Pattern pattern, List<String> list )
    {
        if ( list != null )
        {
            for ( String string : list )
            {
                if ( pattern.matcher( string ).matches() )
                {
                    return true;
                }
            }
        }

        return false;
    }


    // ── R2 Checks a Single Field Value ────────────────────────────────────────
    // R2 probes a single field — like the OID or description. A null field
    // (the attribute type has no description) doesn't match; only a real string
    // that matches the pattern returns true.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given string fully matches the pattern.
     * Null values never match — we return {@code false} immediately without throwing.
     * Uses full-string matching ({@code matches()}) rather than substring find.
     *
     * @param pattern  the compiled regex pattern
     * @param string   the single field value to test (may be null)
     * @return         {@code true} if the string is non-null and matches the pattern
     */
    private boolean checkString( Pattern pattern, String string )
    {
        if ( string != null )
        {
            return pattern.matcher( string ).matches();
        }

        return false;
    }


    // ── R2 Re-Runs the Last Query ─────────────────────────────────────────────
    // "Running the same search again — Rebel intel says things may have changed."
    // search() re-runs the query using the currently visible text field and the
    // saved dialog settings for Search In and Scope, and pushes the new results
    // back to the viewer.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Re-runs the search using the current contents of the search field.
     * Called when the user clicks the search button or presses Enter. Reads the
     * text field, loads the current Search In and Scope settings from dialog settings,
     * and calls {@link #setSearchInput(String, SearchInEnum[], int)} to run the search
     * and update the results label and table.
     */
    private void search()
    {
        String searchString = searchField.getText();
        List<SearchInEnum> searchScope = SearchPage.loadSearchIn();

        setSearchInput( searchString, searchScope.toArray( new SearchInEnum[0] ), SearchPage.loadScope() );
    }


    // ── R2 Updates the Mission Status Display ─────────────────────────────────
    // Above the results table, R2 prints a status line: "No search" (before any
    // query), or "'cn' — 42 matches in workspace." setSearchResultsLabel formats
    // and sets that status string.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Updates the results count label above the table.
     * Shows "No search" before any search has been run. After a search, shows the
     * query string in single quotes and the count of matches, using singular "match"
     * or plural "matches" correctly. Appends " in workspace" at the end.
     *
     * <p>For example — R2's status readout:</p>
     * <pre>
     *   setSearchResultsLabel("cn*", 42) → "'cn*' - 42 matches in workspace"
     *   setSearchResultsLabel("cn",  1)  → "'cn' - 1 match in workspace"
     *   setSearchResultsLabel(null,  0)  → "(no search text)"
     * </pre>
     *
     * @param searchString  the search string to display, or {@code null} for "no search" state
     * @param resultsCount  the number of matching results found
     */
    public void setSearchResultsLabel( String searchString, int resultsCount )
    {
        StringBuffer sb = new StringBuffer();

        if ( searchString == null )
        {
            sb.append( Messages.getString( "SearchView.NoSearch" ) ); //$NON-NLS-1$
        }
        else
        {
            // Search String
            sb.append( "'" + searchString + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append( " - " ); //$NON-NLS-1$

            // Search results count
            sb.append( resultsCount );
            sb.append( " " ); //$NON-NLS-1$
            if ( resultsCount > 1 )
            {
                sb.append( Messages.getString( "SearchView.Matches" ) ); //$NON-NLS-1$
            }
            else
            {
                sb.append( Messages.getString( "SearchView.Match" ) ); //$NON-NLS-1$
            }

            sb.append( Messages.getString( "SearchView.InWorkspace" ) ); //$NON-NLS-1$
        }

        searchResultsLabel.setText( sb.toString() );
    }


    // ── R2 Repeats the Last Search ────────────────────────────────────────────
    // "The schema changed — running the last query again to see if results differ."
    // runCurrentSearchAgain is called by the controller when a schema-change event
    // arrives, so the search results stay current without the user having to
    // re-type anything.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Re-runs the most recently executed search with the same parameters.
     * Called by the {@link SearchViewController} when it detects a schema change (an
     * attribute type or object class was added, modified, or removed). We re-run with
     * the saved search string and the current Search In / Scope settings. Does nothing
     * if no search has been run yet.
     */
    public void runCurrentSearchAgain()
    {
        if ( searchString != null )
        {
            setSearchInput( searchString, SearchPage.loadSearchIn().toArray( new SearchInEnum[0] ), SearchPage
                .loadScope() );
        }
    }


    // ── R2 Reports the Current Query ─────────────────────────────────────────
    // Other parts of the plugin may need to know what R2 was searching for —
    // for example, to pre-populate another dialog. getSearchString exposes the
    // saved search string.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the most recently used search string.
     * Other components (such as the controller or a search page) may call this to
     * read back what the user searched for. Returns {@code null} if no search has been
     * run yet in this session.
     *
     * @return  the last search string, or {@code null} if none
     */
    public String getSearchString()
    {
        return searchString;
    }


    // ── R2 Refreshes the Screen ───────────────────────────────────────────────
    // R2 re-renders the results table without re-running the query — just forcing
    // the viewer to re-ask the content and label providers for current values.
    // Useful when sort preferences change.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the results table viewer without re-running the search query.
     * Forces the {@link TableViewer} to re-ask the content and label providers for
     * current data. Useful when sort or grouping preferences change but the underlying
     * result list hasn't changed.
     */
    public void refresh()
    {
        resultsTableViewer.refresh();
    }
}
