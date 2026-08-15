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
package org.apache.directory.studio.templateeditor.editor.widgets;


import java.util.Comparator;

import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateTable;


// ── CLASS: EditorTable — THE TANTIVE IV MULTI-VALUE DATA PANEL ───────────────────
// On the Tantive IV, the crew roster panel shows multiple entries in a scrollable
// list — each officer's name on its own row. Operators can add new crew to the
// roster, edit existing names, or delete departed crew from the list. This class
// is that roster panel: a JFace {@link TableViewer} that displays all values of
// a multi-valued LDAP attribute, with optional Add/Edit/Delete toolbar buttons.
// Double-clicking a row acts as a shortcut for the Edit button.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A multi-value editor widget that displays all string values of a single LDAP
 * attribute in a scrollable {@link TableViewer}. Optionally provides Add, Edit,
 * and Delete toolbar buttons for managing the list of values. Values are kept
 * sorted case-insensitively. Double-clicking a row opens the Edit dialog.
 * Think of this as the Tantive IV multi-value data panel (crew roster).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorTable extends EditorWidget<TemplateTable>
{
    /** The widget's composite */
    private Composite composite;

    /** The table viewer */
    private TableViewer tableViewer;

    /** The 'Add...' button */
    private ToolItem addToolItem;

    /** The 'Edit...' button */
    private ToolItem editToolItem;

    /** The 'Delete...' button */
    private ToolItem deleteToolItem;


    // ── CONSTRUCTOR: INSTALL THE MULTI-VALUE DATA PANEL ──────────────────────────
    // The technician installs the table panel, binding it to the multi-valued LDAP
    // attribute declared in the template model and configuring which action buttons
    // should appear in the toolbar.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorTable} bound to the given template table model.
     *
     * @param editor         the owning entry editor
     * @param templateTable  the template model specifying attribute type and button visibility
     * @param toolkit        the form toolkit used to create the SWT table
     */
    public EditorTable( IEntryEditor editor, TemplateTable templateTable, FormToolkit toolkit )
    {
        super( templateTable, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE ROSTER PANEL ────────────────────────────────────
    /**
     * Creates the table viewer and optional toolbar, fills the table with the
     * current LDAP attribute values, and attaches all event listeners.
     *
     * @param parent  the parent composite
     * @return the table widget composite
     */
    public Composite createWidget( Composite parent )
    {
        // Creating and initializing the widget UI
        Composite composite = initWidget( parent );

        // Updating the widget's content
        updateWidget();

        // Adding the listeners
        addListeners();

        return composite;
    }


    // ── INIT WIDGET: BUILD THE TABLE AND TOOLBAR ──────────────────────────────────
    // We create a composite with 1 column (table only) or 2 columns (table + toolbar).
    // The TableViewer is configured with a case-insensitive comparator. The toolbar
    // adds Add/Edit/Delete buttons based on the template's visibility flags.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the {@link TableViewer} with a case-insensitive comparator and an
     * optional vertical toolbar containing Add, Edit, and Delete buttons per the
     * template model's configuration.
     *
     * @param parent  the parent composite
     * @return the table widget composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the widget composite
        composite = getToolkit().createComposite( parent );
        composite.setLayoutData( getGridata() );

        // Creating the layout
        GridLayout gl = new GridLayout( ( needsToolbar() ? 2 : 1 ), false );
        gl.marginHeight = gl.marginWidth = 0;
        gl.horizontalSpacing = gl.verticalSpacing = 0;
        composite.setLayout( gl );

        // Table Viewer
        Table table = getToolkit().createTable( composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
        table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        tableViewer = new TableViewer( table );
        tableViewer.setContentProvider( new ArrayContentProvider() );
        tableViewer.setComparator( new ViewerComparator( new Comparator<String>()
        {
            public int compare( String s1, String s2 )
            {
                if ( s1 == null )
                {
                    return 1;
                }
                else if ( s2 == null )
                {
                    return -1;
                }
                else
                {
                    return s1.compareToIgnoreCase( s2 );
                }
            }
        } ) );

        // Toolbar (if needed)
        if ( needsToolbar() )
        {
            ToolBar toolbar = new ToolBar( composite, SWT.VERTICAL );
            toolbar.setLayoutData( new GridData( SWT.NONE, SWT.FILL, false, true ) );

            // Add Button
            if ( getWidget().isShowAddButton() )
            {
                addToolItem = new ToolItem( toolbar, SWT.PUSH );
                addToolItem.setToolTipText( Messages.getString( "EditorTable.Add" ) ); //$NON-NLS-1$
                addToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_ADD_VALUE ) );
            }

            // Edit Button
            if ( getWidget().isShowEditButton() )
            {
                editToolItem = new ToolItem( toolbar, SWT.PUSH );
                editToolItem.setToolTipText( Messages.getString( "EditorTable.Edit" ) ); //$NON-NLS-1$
                editToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_EDIT_VALUE ) );
                editToolItem.setEnabled( false );
            }

            // Delete Button
            if ( getWidget().isShowDeleteButton() )
            {
                deleteToolItem = new ToolItem( toolbar, SWT.PUSH );
                deleteToolItem.setToolTipText( Messages.getString( "EditorTable.Delete" ) ); //$NON-NLS-1$
                deleteToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                    EntryTemplatePluginConstants.IMG_TOOLBAR_DELETE_VALUE ) );
                deleteToolItem.setEnabled( false );
            }
        }

        return composite;
    }


    // ── NEEDS TOOLBAR: CHECK IF ANY BUTTONS ARE CONFIGURED ───────────────────────
    /**
     * Returns {@code true} if at least one toolbar button (Add, Edit, or Delete)
     * is configured in the template model.
     *
     * @return {@code true} if a toolbar should be created
     */
    private boolean needsToolbar()
    {
        return getWidget().isShowAddButton() || getWidget().isShowEditButton() || getWidget().isShowDeleteButton();
    }


    // ── UPDATE WIDGET: REFRESH THE ROSTER FROM THE ATTRIBUTE ─────────────────────
    /**
     * Re-reads the LDAP attribute's string values and refreshes the table viewer.
     * Shows an empty table if the attribute has no values.
     */
    private void updateWidget()
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            tableViewer.setInput( attribute.getStringValues() );
        }
        else
        {
            tableViewer.setInput( new String[0] );
        }
    }


    // ── ADD LISTENERS: WIRE ALL TOOLBAR AND TABLE LISTENERS ──────────────────────
    /**
     * Attaches selection listeners to the Add, Edit, and Delete toolbar buttons
     * (if present), and selection/double-click listeners to the table viewer.
     */
    private void addListeners()
    {
        // Add button
        if ( ( addToolItem != null ) && ( !addToolItem.isDisposed() ) )
        {
            addToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    addToolItemAction();
                }
            } );
        }

        // Edit button
        if ( ( editToolItem != null ) && ( !editToolItem.isDisposed() ) )
        {
            editToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    editToolItemAction();
                }
            } );
        }

        // Delete button
        if ( ( deleteToolItem != null ) && ( !deleteToolItem.isDisposed() ) )
        {
            deleteToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    deleteToolItemAction();
                }
            } );
        }

        // Table Viewer
        if ( ( tableViewer != null ) && ( !tableViewer.getTable().isDisposed() ) )
        {
            tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
            {
                public void selectionChanged( SelectionChangedEvent event )
                {
                    updateButtonsStates();
                }
            } );

            tableViewer.addDoubleClickListener( new IDoubleClickListener()
            {
                public void doubleClick( DoubleClickEvent event )
                {
                    if ( ( editToolItem != null ) && ( !editToolItem.isDisposed() ) )
                    {
                        editToolItemAction();
                    }
                }
            } );
        }
    }


    // ── ADD TOOL ITEM ACTION: ADD A NEW VALUE TO THE ROSTER ──────────────────────
    /**
     * Opens a {@link TextDialog} for the user to enter a new string value. Adds
     * the entered value to the LDAP attribute and selects it in the viewer.
     */
    private void addToolItemAction()
    {
        TextDialog textDialog = new TextDialog( tableViewer.getTable().getShell(), "" ); //$NON-NLS-1$
        if ( textDialog.open() == Dialog.OK )
        {
            String value = textDialog.getText();

            addAttributeValue( value );
            tableViewer.setSelection( new StructuredSelection( value ) );
        }
    }


    // ── EDIT TOOL ITEM ACTION: EDIT THE SELECTED VALUE ───────────────────────────
    /**
     * Opens a {@link TextDialog} pre-populated with the selected value. If the user
     * changes it and confirms, removes the old value and adds the new one.
     */
    private void editToolItemAction()
    {
        StructuredSelection selection = ( StructuredSelection ) tableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            String selectedValue = ( String ) selection.getFirstElement();

            IAttribute attribute = getAttribute();
            if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
            {
                IValue value = null;

                for ( IValue attributeValue : attribute.getValues() )
                {
                    if ( selectedValue.equals( attributeValue.getStringValue() ) )
                    {
                        value = attributeValue;
                        break;
                    }
                }

                if ( value != null )
                {
                    TextDialog textDialog = new TextDialog( tableViewer.getTable().getShell(), selectedValue );
                    if ( textDialog.open() == Dialog.OK )
                    {
                        String newValue = textDialog.getText();
                        if ( !selectedValue.equals( newValue ) )
                        {
                            deleteAttributeValue( selectedValue );
                            addAttributeValue( newValue );
                            tableViewer.setSelection( new StructuredSelection( newValue ) );
                        }
                    }
                }
            }
        }
    }


    // ── DELETE TOOL ITEM ACTION: REMOVE THE SELECTED VALUE ───────────────────────
    /**
     * Opens a confirmation dialog, then removes the selected string value from the
     * LDAP attribute if the user confirms.
     */
    private void deleteToolItemAction()
    {
        StructuredSelection selection = ( StructuredSelection ) tableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            // Launching a confirmation dialog
            if ( MessageDialog.openConfirm( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages
                .getString( "EditorTable.Confirmation" ), Messages.getString( "EditorTable.ConfirmationDeleteValue" ) ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                deleteAttributeValue( ( String ) selection.getFirstElement() );
            }
        }
    }


    // ── UPDATE BUTTONS STATES: ENABLE/DISABLE EDIT AND DELETE ────────────────────
    /**
     * Enables or disables the Edit and Delete toolbar buttons based on whether
     * there is a current selection in the table viewer.
     */
    private void updateButtonsStates()
    {
        StructuredSelection selection = ( StructuredSelection ) tableViewer.getSelection();

        if ( ( editToolItem != null ) && ( !editToolItem.isDisposed() ) )
        {
            editToolItem.setEnabled( !selection.isEmpty() );
        }

        if ( ( deleteToolItem != null ) && ( !deleteToolItem.isDisposed() ) )
        {
            deleteToolItem.setEnabled( !selection.isEmpty() );
        }
    }


    // ── UPDATE: REFRESH THE TABLE ─────────────────────────────────────────────────
    /**
     * Refreshes the table viewer from the current LDAP attribute values.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT Table and TableViewer are disposed by their parent composite.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
