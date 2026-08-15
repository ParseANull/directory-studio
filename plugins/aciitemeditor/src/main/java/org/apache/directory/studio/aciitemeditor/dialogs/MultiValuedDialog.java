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
package org.apache.directory.studio.aciitemeditor.dialogs;


import java.util.List;

import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;


// ── CLASS: MultiValuedDialog — THE ISB MULTI-ENTRY MANIFEST EDITOR ────────────
// Some rows on the ISB clearance manifest require multiple entries — a list of
// DNs for a user group, several attribute types, or multiple subtree specs.
// Grand Moff opens a small secondary form showing all current entries in a
// table, with Add / Edit / Delete buttons to manage them.
// MultiValuedDialog is that secondary form.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Dialog} for editing a list of string values for a multi-valued
 * ACI protected item or user class.
 * Displays the current values in a table and provides Add, Edit, and Delete
 * buttons backed by a delegated {@link AbstractDialogStringValueEditor} for the
 * actual value input.
 * Think of this as the ISB multi-entry form: one dialog, one list, one editor
 * per entry type.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultiValuedDialog extends Dialog
{
    /** The dialog title */
    private String displayName;

    /** The value editor */
    private AbstractDialogStringValueEditor valueEditor;

    /** The values, may be empty. */
    private List<String> values;

    /** The context */
    private ACIItemValueWithContext context;

    /** The inner composite for all the content */
    private Composite composite = null;

    /** The table viewer containing all user classes */
    private TableViewer tableViewer = null;

    /** The edit button */
    private Button editButton = null;

    /** The delete button */
    private Button deleteButton = null;


    // ── OPEN THE MULTI-ENTRY FORM ─────────────────────────────────────────────
    // Grand Moff opens the secondary form, handing it the live values list to
    // mutate and the value editor to open when adding or editing entries.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code MultiValuedDialog}.
     * The {@code values} list is mutated directly — no copy is made — so callers
     * see the changes immediately when the dialog closes.
     *
     * <p>For example — editing the subtree list for a user class:</p>
     * <pre>
     *   MultiValuedDialog dlg = new MultiValuedDialog(
     *       shell, "Subtree", wrapper.getValues(), context, subtreeEditor);
     *   dlg.open();
     *   // wrapper.getValues() now reflects the user's edits
     * </pre>
     *
     * @param parentShell  the parent SWT shell
     * @param displayName  the category label used as the dialog title suffix
     * @param values       the live, modifiable list of string values to edit
     * @param context      the DTO carrying connection and entry for the value editor
     * @param valueEditor  the editor to open when adding or changing an entry
     */
    public MultiValuedDialog( Shell parentShell, String displayName, List<String> values,
        ACIItemValueWithContext context, AbstractDialogStringValueEditor valueEditor )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );

        this.displayName = displayName;
        this.values = values;
        this.context = context;
        this.valueEditor = valueEditor;
    }


    // ── SET TITLE ─────────────────────────────────────────────────────────────
    // The orderly labels the secondary form window with the category name so
    // Grand Moff knows which manifest row he is editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog title to a localised prefix plus the {@code displayName}.
     *
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "MultiValuedDialog.dialog.titlePrefix" ) + displayName ); //$NON-NLS-1$
        shell.setImage( Activator.getDefault().getImage( Messages.getString( "MultiValuedDialog.dialog.icon" ) ) ); //$NON-NLS-1$
    }


    // ── ONLY AN OK BUTTON ─────────────────────────────────────────────────────
    // The multi-entry form edits the list in-place, so Cancel is meaningless —
    // the user just closes with OK when done.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates only an OK button (no Cancel — edits are applied immediately to
     * the live list).
     *
     * {@inheritDoc}
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
    }


    // ── BUILD THE TABLE-AND-BUTTONS LAYOUT ───────────────────────────────────
    // The orderly sets up the two-column layout: table on the left, button
    // column on the right.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 );
        composite.setLayoutData( gd );
        GridLayout layout = ( GridLayout ) composite.getLayout();
        layout.makeColumnsEqualWidth = false;
        layout.numColumns = 2;

        createTable();

        createButtonComposite();

        applyDialogFont( composite );
        return composite;
    }


    // ── CREATE THE VALUES TABLE ───────────────────────────────────────────────
    // The values list is bound directly to the table viewer so Add/Edit/Delete
    // operations are immediately visible to the user.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the table and {@link TableViewer} bound to the live values list.
     * Installs selection and double-click listeners to enable/disable buttons
     * and open the edit dialog.
     */
    private void createTable()
    {
        GridData tableGridData = new GridData( GridData.FILL_BOTH );
        tableGridData.grabExcessHorizontalSpace = true;
        tableGridData.verticalAlignment = GridData.FILL;
        tableGridData.horizontalAlignment = GridData.FILL;
        //tableGridData.heightHint = 100;

        Table table = new Table( composite, SWT.BORDER );
        table.setHeaderVisible( false );
        table.setLayoutData( tableGridData );
        table.setLinesVisible( false );
        tableViewer = new TableViewer( table );
        tableViewer.setContentProvider( new ArrayContentProvider() );
        tableViewer.setLabelProvider( new LabelProvider() );
        tableViewer.setInput( values );

        tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                valueSelected();
            }
        } );

        tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                editValue();
            }
        } );
    }


    // ── CREATE THE BUTTON COLUMN ──────────────────────────────────────────────
    // Three buttons — Add, Edit, Delete — sit in the right column; Edit and Delete
    // start disabled and are enabled only when a row is selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Add, Edit, and Delete buttons in the right column.
     * Edit and Delete are initially disabled; they become enabled when a row
     * is selected in the table.
     */
    private void createButtonComposite()
    {
        GridData deleteButtonGridData = new GridData();
        deleteButtonGridData.horizontalAlignment = GridData.FILL;
        deleteButtonGridData.grabExcessHorizontalSpace = false;
        deleteButtonGridData.verticalAlignment = GridData.BEGINNING;
        deleteButtonGridData.widthHint = Activator.getButtonWidth( composite );

        GridData editButtonGridData = new GridData();
        editButtonGridData.horizontalAlignment = GridData.FILL;
        editButtonGridData.grabExcessHorizontalSpace = false;
        editButtonGridData.verticalAlignment = GridData.BEGINNING;
        editButtonGridData.widthHint = Activator.getButtonWidth( composite );

        GridData addButtonGridData = new GridData();
        addButtonGridData.horizontalAlignment = GridData.FILL;
        addButtonGridData.grabExcessHorizontalSpace = false;
        addButtonGridData.verticalAlignment = GridData.BEGINNING;
        addButtonGridData.widthHint = Activator.getButtonWidth( composite );

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.CENTER;
        gridData.grabExcessHorizontalSpace = false;
        gridData.grabExcessVerticalSpace = false;
        gridData.verticalAlignment = GridData.FILL;

        Composite buttonComposite = new Composite( composite, SWT.NONE );
        buttonComposite.setLayoutData( gridData );
        buttonComposite.setLayout( gridLayout );

        Button addButton = new Button( buttonComposite, SWT.NONE );
        addButton.setText( Messages.getString( "MultiValuedDialog.button.add" ) ); //$NON-NLS-1$
        addButton.setLayoutData( addButtonGridData );
        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addValue();
            }
        } );

        editButton = new Button( buttonComposite, SWT.NONE );
        editButton.setText( Messages.getString( "MultiValuedDialog.button.edit" ) ); //$NON-NLS-1$
        editButton.setLayoutData( editButtonGridData );
        editButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                editValue();
            }
        } );
        editButton.setEnabled( false );

        deleteButton = new Button( buttonComposite, SWT.NONE );
        deleteButton.setText( Messages.getString( "MultiValuedDialog.button.delete" ) ); //$NON-NLS-1$
        deleteButton.setLayoutData( deleteButtonGridData );
        deleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                deleteValue();
            }
        } );
        deleteButton.setEnabled( false );

    }


    // ── ADD A NEW ENTRY ───────────────────────────────────────────────────────
    // Grand Moff clicks Add: the delegated value editor opens for a blank entry,
    // and if he fills it in, the new value is appended to the list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the delegated value editor for a new entry and appends the result
     * to the values list if the user provides a non-null value.
     */
    private void addValue()
    {
        IAttribute attribute = new Attribute( context.getEntry(), "" ); //$NON-NLS-1$
        IValue value = new Value( attribute, "" ); //$NON-NLS-1$
        Object oldRawValue = valueEditor.getRawValue( value ); //$NON-NLS-1$

        CellEditor cellEditor = valueEditor.getCellEditor();
        cellEditor.setValue( oldRawValue );
        cellEditor.activate();
        Object newRawValue = cellEditor.getValue();

        if ( newRawValue != null )
        {
            String newValue = ( String ) valueEditor.getStringOrBinaryValue( newRawValue );

            values.add( newValue );
            tableViewer.refresh();
        }
    }


    // ── EDIT THE SELECTED ENTRY ───────────────────────────────────────────────
    // Grand Moff double-clicks or presses Edit: the delegated editor opens
    // pre-filled with the selected value, and the replacement is swapped in.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the delegated value editor pre-populated with the currently selected
     * value.  If the user confirms, the old value is removed and the new value
     * is appended.
     */
    private void editValue()
    {
        String oldValue = getSelectedValue();
        if ( oldValue != null )
        {
            IAttribute attribute = new Attribute( context.getEntry(), "" ); //$NON-NLS-1$
            IValue value = new Value( attribute, oldValue ); //$NON-NLS-1$
            Object oldRawValue = valueEditor.getRawValue( value ); //$NON-NLS-1$

            CellEditor cellEditor = valueEditor.getCellEditor();
            cellEditor.setValue( oldRawValue );
            cellEditor.activate();
            Object newRawValue = cellEditor.getValue();

            if ( newRawValue != null )
            {
                String newValue = ( String ) valueEditor.getStringOrBinaryValue( newRawValue );

                values.remove( oldValue );
                values.add( newValue );
                tableViewer.refresh();
            }
        }
    }


    // ── DELETE THE SELECTED ENTRY ─────────────────────────────────────────────
    // Grand Moff selects a row and presses Delete: the entry is removed from
    // the list and the table refreshes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected value from the list and refreshes the table.
     */
    private void deleteValue()
    {
        String value = getSelectedValue();
        if ( value != null )
        {
            values.remove( value );
            tableViewer.refresh();
        }
    }


    // ── ENABLE/DISABLE BUTTONS ON SELECTION ──────────────────────────────────
    // When a row is selected, Edit and Delete activate; when the selection is
    // cleared, they go back to disabled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the enabled state of the Edit and Delete buttons based on whether
     * a row is currently selected in the table.
     */
    private void valueSelected()
    {
        String value = getSelectedValue();

        if ( value == null )
        {
            editButton.setEnabled( false );
            deleteButton.setEnabled( false );
        }
        else
        {
            editButton.setEnabled( true );
            deleteButton.setEnabled( true );
        }
    }


    // ── RETRIEVE THE SELECTED ROW ─────────────────────────────────────────────
    // A helper that extracts the selected string from the table viewer's
    // structured selection, or returns null if nothing is selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string value currently selected in the table viewer,
     * or {@code null} if nothing is selected.
     *
     * @return the selected string, or {@code null}
     */
    private String getSelectedValue()
    {
        String value = null;

        IStructuredSelection selection = ( IStructuredSelection ) tableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            Object element = selection.getFirstElement();
            if ( element instanceof String )
            {
                value = ( String ) element;
            }
        }

        return value;
    }

}
