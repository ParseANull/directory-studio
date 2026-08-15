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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenSchemaViewPreferenceAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenSchemaViewSortingDialogAction;
import org.apache.directory.studio.schemaeditor.model.difference.AttributeTypeDifference;
import org.apache.directory.studio.schemaeditor.model.difference.DifferenceType;
import org.apache.directory.studio.schemaeditor.model.difference.ObjectClassDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SchemaDifference;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;


// ── CLASS: DifferencesWidget — THE TANTIVE IV BRIDGE IN TWO STATIONS ─────────
// On the Tantive IV, the bridge has two distinct stations operating side by side:
// one for navigation (the tree on the left showing which schemas changed) and one
// for weapons/communications (the table on the right listing the exact property
// deltas). Both stations are coordinated — selecting something on the left updates
// the right, exactly as a tree selection here refreshes the properties table.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A composite widget that displays a set of {@link SchemaDifference} objects in a
 * two-panel layout: a tree on the left groups differences by schema, and a table on
 * the right shows the individual property-level changes for whatever is selected.
 * Think of it as the Tantive IV bridge: the left panel is navigation (schema tree),
 * the right panel is tactical readout (property detail table), and both panels
 * share the same preference store so they stay in sync with the user's sorting and
 * grouping preferences.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DifferencesWidget
{
    /** The PreferenceStore */
    private IPreferenceStore store;

    /** The authorized Preferences keys */
    private List<String> authorizedPrefs;

    /** The preference listener */
    private IPropertyChangeListener preferenceListener = new IPropertyChangeListener()
    {
        /**
         * {@inheritDoc}
         */
        public void propertyChange( PropertyChangeEvent event )
        {
            if ( authorizedPrefs.contains( event.getProperty() ) )
            {
                treeViewer.refresh();
            }
        }
    };

    // The MenuItems
    private TreeViewer treeViewer;
    private TableViewer tableViewer;
    private MenuItem groupByType;
    private MenuItem groupByProperty;


    // ── THE CREW REPORTS FOR DUTY: CONSTRUCTING THE WIDGET ───────────────────────
    // The bridge crew assembles before the Tantive IV leaves port — here we initialise
    // the shared preference store that both the tree and table panels will consult.
    // Everything else is deferred until createWidget() is called with a parent composite.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DifferencesWidget and wires it to the plugin's preference store.
     * We grab the store here so both panels created later in
     * {@link #createWidget(Composite)} can share it. No SWT widgets are created yet —
     * those come when the caller provides a parent composite.
     */
    public DifferencesWidget()
    {
        store = Activator.getDefault().getPreferenceStore();
    }


    // ── BOTH BRIDGE STATIONS COME ONLINE ─────────────────────────────────────────
    // Captain Antilles gives the order and both bridge stations light up: the left
    // navigation station (tree viewer showing schema differences) and the right
    // tactical station (table viewer listing property changes). Each station has its
    // own toolbar menu for sorting and grouping, and they talk to each other — a
    // selection on the left automatically updates what the right station shows.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the two-panel UI — a tree viewer on the left and a table viewer on the
     * right — and attaches everything to {@code parent}. We also wire up the
     * preference listener so display changes (label format, sorting, grouping)
     * propagate to both panels without requiring a manual refresh.
     * Call this once after construction; calling it again will create duplicate widgets.
     *
     * <p>For example — the Tantive IV bridge comes to life:</p>
     * <pre>
     *   Left station: tree of SchemaDifference nodes, collapsible by double-click.
     *   Right station: table of PropertyDifference rows for whatever is selected.
     *   Each station has a menu for "Sorting..." and "Preferences..." options.
     * </pre>
     *
     * @param parent  the SWT composite that will host both panels side by side
     */
    public void createWidget( Composite parent )
    {
        // Composite
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gridLayout = new GridLayout( 2, true );
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Left Composite
        Composite leftComposite = new Composite( composite, SWT.NONE );
        gridLayout = new GridLayout();
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout( gridLayout );
        leftComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // ToolBar
        final ToolBar leftToolBar = new ToolBar( leftComposite, SWT.HORIZONTAL | SWT.FLAT );
        leftToolBar.setLayoutData( new GridData( SWT.RIGHT, SWT.NONE, false, false ) );
        // Creating the 'Menu' ToolBar item
        final ToolItem leftMenuToolItem = new ToolItem( leftToolBar, SWT.PUSH );
        leftMenuToolItem.setImage( Activator.getDefault().getImage( PluginConstants.IMG_TOOLBAR_MENU ) );
        leftMenuToolItem.setToolTipText( Messages.getString( "DifferencesWidget.MenuToolTip" ) ); //$NON-NLS-1$
        // Creating the associated Menu
        final Menu leftMenu = new Menu( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.POP_UP );
        // Adding the action to display the Menu when the item is clicked
        leftMenuToolItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                Rectangle rect = leftMenuToolItem.getBounds();
                Point pt = new Point( rect.x, rect.y + rect.height );
                pt = leftToolBar.toDisplay( pt );
                leftMenu.setLocation( pt.x, pt.y );
                leftMenu.setVisible( true );
            }
        } );
        // Adding the 'Sorting...' MenuItem
        MenuItem sortingMenuItem = new MenuItem( leftMenu, SWT.PUSH );
        sortingMenuItem.setText( Messages.getString( "DifferencesWidget.Sorting" ) ); //$NON-NLS-1$
        sortingMenuItem.setImage( Activator.getDefault().getImage( PluginConstants.IMG_SORTING ) );
        sortingMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                new OpenSchemaViewSortingDialogAction().run();
            }
        } );
        // Adding the 'Separator' MenuItem
        new MenuItem( leftMenu, SWT.SEPARATOR );
        // Adding the 'Preferences...' MenuItem
        MenuItem preferencesMenuItem = new MenuItem( leftMenu, SWT.PUSH );
        preferencesMenuItem.setText( Messages.getString( "DifferencesWidget.Preferences" ) ); //$NON-NLS-1$
        preferencesMenuItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                new OpenSchemaViewPreferenceAction().run();
            }
        } );

        // TreeViewer
        treeViewer = new TreeViewer( leftComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        gridData.heightHint = 250;
        treeViewer.getTree().setLayoutData( gridData );
        treeViewer.setContentProvider( new DifferencesWidgetSchemaContentProvider() );
        treeViewer.setLabelProvider( new DifferencesWidgetSchemaLabelProvider() );
        treeViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();
                Object element = selection.getFirstElement();
                if ( element instanceof AttributeTypeDifference )
                {
                    AttributeTypeDifference atd = ( AttributeTypeDifference ) element;
                    if ( atd.getType().equals( DifferenceType.MODIFIED ) )
                    {
                        tableViewer.setInput( atd.getDifferences() );
                        return;
                    }
                }
                else if ( element instanceof ObjectClassDifference )
                {
                    ObjectClassDifference ocd = ( ObjectClassDifference ) element;
                    if ( ocd.getType().equals( DifferenceType.MODIFIED ) )
                    {
                        tableViewer.setInput( ocd.getDifferences() );
                        return;
                    }
                }

                // Default
                tableViewer.setInput( null );
            }
        } );
        treeViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();
                Object element = selection.getFirstElement();
                if ( ( element instanceof Folder ) || ( element instanceof SchemaDifference ) )
                {
                    treeViewer.setExpandedState( element, !treeViewer.getExpandedState( element ) );
                }
            }
        } );

        // Right Composite
        Composite rightComposite = new Composite( composite, SWT.NONE );
        gridLayout = new GridLayout();
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        rightComposite.setLayout( gridLayout );
        rightComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // ToolBar
        final ToolBar rightToolBar = new ToolBar( rightComposite, SWT.HORIZONTAL | SWT.FLAT );
        rightToolBar.setLayoutData( new GridData( SWT.RIGHT, SWT.NONE, false, false ) );
        // Creating the 'Menu' ToolBar item
        final ToolItem rightMenuToolItem = new ToolItem( rightToolBar, SWT.PUSH );
        rightMenuToolItem.setImage( Activator.getDefault().getImage( PluginConstants.IMG_TOOLBAR_MENU ) );
        rightMenuToolItem.setToolTipText( Messages.getString( "DifferencesWidget.MenuToolTip" ) ); //$NON-NLS-1$
        // Creating the associated Menu
        final Menu rightMenu = new Menu( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.POP_UP );
        // Adding the action to display the Menu when the item is clicked
        rightMenuToolItem.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                Rectangle rect = rightMenuToolItem.getBounds();
                Point pt = new Point( rect.x, rect.y + rect.height );
                pt = rightToolBar.toDisplay( pt );
                rightMenu.setLocation( pt.x, pt.y );
                rightMenu.setVisible( true );
            }
        } );
        // Adding the 'Group By Property' MenuItem
        groupByProperty = new MenuItem( rightMenu, SWT.CHECK );
        groupByProperty.setText( Messages.getString( "DifferencesWidget.GroupByProperty" ) ); //$NON-NLS-1$
        groupByProperty.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                changeGrouping( PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING_PROPERTY );
            }
        } );
        // Adding the 'Group By Type' MenuItem
        groupByType = new MenuItem( rightMenu, SWT.CHECK );
        groupByType.setText( Messages.getString( "DifferencesWidget.GroupByType" ) ); //$NON-NLS-1$
        groupByType.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                changeGrouping( PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING_TYPE );
            }
        } );
        updateMenuItemsCheckStatus();

        // TableViewer
        tableViewer = new TableViewer( rightComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        tableViewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        tableViewer.setContentProvider( new DifferencesWidgetPropertiesContentProvider() );
        tableViewer.setLabelProvider( new DifferencesWidgetPropertiesLabelProvider() );

        initAuthorizedPrefs();
        initPreferencesListener();
    }


    // ── NEW MISSION BRIEFING ARRIVES AT THE BRIDGE ────────────────────────────────
    // A new set of orders comes in from command: here is the updated list of schema
    // differences to analyse. We hand them directly to the tree viewer and let the
    // content provider do the work of unpacking the hierarchy.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Feeds a fresh list of {@link SchemaDifference} objects into the tree viewer on
     * the left panel. The right-panel table is cleared automatically because the tree
     * selection resets. Call this whenever you have a new comparison result to display.
     *
     * <p>For example — the Rebel briefing officer hands new intel to the bridge:</p>
     * <pre>
     *   List&lt;SchemaDifference&gt; diffs = comparator.compare(local, remote);
     *   widget.setInput(diffs);
     *   // Tree now shows each schema with its ADDED / MODIFIED / REMOVED status.
     * </pre>
     *
     * @param input  the list of schema-level differences to display; may be empty but not null
     */
    public void setInput( List<SchemaDifference> input )
    {
        treeViewer.setInput( input );
    }


    // ── SWITCHING TACTICAL READOUT MODE: GROUPING OPTIONS ────────────────────────
    // The right station officer says "give me readouts grouped by property" or "by
    // change type." We update the preference store so the change persists across
    // sessions, sync the menu checkmarks, then refresh the table to apply the new layout.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Persists a new grouping preference and refreshes the right-panel table so it
     * immediately reflects the new layout. We write to the preference store so the
     * choice survives until the user changes it again.
     *
     * <p>For example — the right-station officer changes the display mode:</p>
     * <pre>
     *   changeGrouping(PREFS_DIFFERENCES_WIDGET_GROUPING_PROPERTY)
     *     →  "Group by Property" checked, "Group by Type" unchecked, table refreshes.
     * </pre>
     *
     * @param value  the grouping constant to store —
     *               {@code PREFS_DIFFERENCES_WIDGET_GROUPING_PROPERTY} or
     *               {@code PREFS_DIFFERENCES_WIDGET_GROUPING_TYPE}
     */
    private void changeGrouping( int value )
    {
        store.setValue( PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING, value );
        updateMenuItemsCheckStatus();
        tableViewer.refresh();
    }


    // ── SYNCING THE BRIDGE CONSOLE LIGHTS ────────────────────────────────────────
    // The status lights on both bridge stations need to match the actual preference
    // value. We read the current setting from the store and update the checkmarks on
    // both "Group By Property" and "Group By Type" menu items accordingly.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current grouping preference and updates the check state on the two
     * grouping menu items so the UI accurately reflects what is stored. We call this
     * after every grouping change and also during initial widget setup.
     *
     * <p>For example — Captain Antilles checks all console lights are consistent:</p>
     * <pre>
     *   pref == GROUPING_PROPERTY  →  groupByProperty checked,  groupByType unchecked
     *   pref == GROUPING_TYPE      →  groupByProperty unchecked, groupByType checked
     *   pref == anything else      →  both unchecked
     * </pre>
     */
    private void updateMenuItemsCheckStatus()
    {
        int prefValue = store.getInt( PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING );
        if ( prefValue == PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING_PROPERTY )
        {
            groupByProperty.setSelection( true );
            groupByType.setSelection( false );
        }
        else if ( prefValue == PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING_TYPE )
        {
            groupByProperty.setSelection( false );
            groupByType.setSelection( true );
        }
        else
        {
            groupByProperty.setSelection( false );
            groupByType.setSelection( false );
        }
    }


    // ── AUTHORISING WHICH SIGNALS THE BRIDGE MONITORS ────────────────────────────
    // Not every preference change should trigger a tree refresh — only the ones
    // relevant to how schema items are labelled and sorted. We build a whitelist here
    // so the listener ignores unrelated preference changes and avoids unnecessary redraws.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the whitelist of preference keys that the tree viewer should react to.
     * Any preference change whose key is not in this list is ignored by our listener,
     * so we only refresh when something visual actually changed (labels, sorting, grouping).
     *
     * <p>For example — the bridge only monitors channels relevant to navigation:</p>
     * <pre>
     *   authorizedPrefs includes PREFS_SCHEMA_VIEW_LABEL, PREFS_SCHEMA_VIEW_GROUPING, etc.
     *   A change to an unrelated pref (e.g. connection timeout) is silently skipped.
     * </pre>
     */
    private void initAuthorizedPrefs()
    {
        authorizedPrefs = new ArrayList<String>();
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER );
    }


    // ── OPENING THE COMM CHANNEL TO FLEET COMMAND ────────────────────────────────
    // Obi-Wan senses every disturbance in the Force — we register our listener on the
    // preference store so we are notified whenever a relevant preference changes and
    // can refresh the tree viewer to reflect the new display settings.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Registers our {@link IPropertyChangeListener} on the preference store so we
     * receive a callback whenever a watched preference changes. This keeps the tree
     * viewer in sync with user settings without requiring a manual refresh.
     * Always paired with the removal in {@link #dispose()}.
     */
    private void initPreferencesListener()
    {
        store.addPropertyChangeListener( preferenceListener );
    }


    // ── THE TANTIVE IV GOES DOWN: RELEASING BRIDGE RESOURCES ─────────────────────
    // When the Tantive IV is boarded by the Empire, everything shuts down gracefully.
    // Here we remove our preference listener from the store so we do not leave a
    // dangling reference that could cause updates after the widget is gone.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases this widget's preference listener from the store, preventing memory
     * leaks and stale callbacks after the widget is removed from the UI. Call this
     * from the owning dialog or view's {@code dispose()} method.
     *
     * <p>For example — the bridge shuts down in an orderly fashion:</p>
     * <pre>
     *   widget.dispose();  // listener removed, no further callbacks
     * </pre>
     */
    public void dispose()
    {
        store.removePropertyChangeListener( preferenceListener );
    }
}
