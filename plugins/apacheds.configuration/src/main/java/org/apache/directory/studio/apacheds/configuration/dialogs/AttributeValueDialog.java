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
package org.apache.directory.studio.apacheds.configuration.dialogs;


import org.apache.directory.studio.apacheds.configuration.editor.AttributeValueObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


// ── CLASS: AttributeValueDialog — IMPERIAL BUREAUCRAT FILLS OUT A FORM ───────────────────
// An Imperial bureaucrat sits down at a desk to fill in two fields on a requisition form:
// the attribute type they need and the value they want to assign.
// Once the form is complete and signed (OK clicked), it goes into the partition's record.
// This dialog does exactly that: a two-field form for editing an LDAP attribute-value pair.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Simple two-field dialog for editing an {@link AttributeValueObject} — one field for the
 * attribute name and one for the value.
 * Sets the dirty flag whenever either field is changed; on OK writes both fields back
 * to the model object.
 * Think of it as the Imperial bureaucrat's requisition form: fill in attribute and value,
 * click OK, and the entry goes into the record.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeValueDialog extends Dialog
{
    /** The Attribute Value Object */
    private AttributeValueObject attributeValueObject;

    /** The dirty flag */
    private boolean dirty = false;

    // UI Fields
    private Text attributeText;
    private Text valueText;


    // ── Opening The Form Pre-Populated From The Model Object ──────────────────────────────────
    // We receive the model object up front so we can pre-populate both text fields and write
    // back to the same object when OK is clicked.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the dialog backed by the given attribute-value pair.
     * The dialog will pre-populate from the object and write back to it on OK.
     *
     * @param attributeValueObject  the attribute-value pair to edit (must not be null)
     */
    public AttributeValueDialog( AttributeValueObject attributeValueObject )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        this.attributeValueObject = attributeValueObject;
    }


    // ── Setting The Dialog Window Title ───────────────────────────────────────────────────────
    // This is the title that appears in the dialog's title bar.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title to the localised "Attribute/Value Dialog" string.
     *
     * @param newShell  the shell being configured
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "AttributeValueDialog.AttributeValueDialog" ) ); //$NON-NLS-1$
    }


    // ── Building The Two-Field Form ───────────────────────────────────────────────────────────
    // Lay out the two text fields (attribute, value) side-by-side in a two-column grid.
    // Pre-populate both from the model object; attach modify listeners to track changes.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the form body with two labelled text fields.
     *
     * <p>For example — the resulting form:</p>
     * <pre>
     *   +------------------------------------------------+
     *   | Attribute: [           ]  Value: [           ] |
     *   +------------------------------------------------+
     * </pre>
     *
     * @param parent  the parent composite provided by the Dialog framework
     * @return the created composite
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

        Label attributeLabel = new Label( composite, SWT.NONE );
        attributeLabel.setText( Messages.getString( "AttributeValueDialog.Attribute" ) ); //$NON-NLS-1$

        attributeText = new Text( composite, SWT.BORDER );
        attributeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        Label valueLabel = new Label( composite, SWT.NONE );
        valueLabel.setText( Messages.getString( "AttributeValueDialog.Value" ) ); //$NON-NLS-1$

        valueText = new Text( composite, SWT.BORDER );
        valueText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        initFromInput();
        addListeners();

        return composite;
    }


    // ── Pre-Populating The Fields From The Model Object ───────────────────────────────────────
    // Pull the current attribute name and value from the model and stuff them into the text
    // fields.  Null values become empty strings so the text widgets don't blow up.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the attribute and value text fields from the current state of
     * {@link #attributeValueObject}.  Null fields are replaced by empty strings.
     */
    private void initFromInput()
    {
        String attribute = attributeValueObject.getAttribute();
        attributeText.setText( ( attribute == null ) ? "" : attribute ); //$NON-NLS-1$

        Object value = attributeValueObject.getValue();
        valueText.setText( ( value == null ) ? "" : value.toString() ); //$NON-NLS-1$
    }


    // ── Attaching Change Listeners To Mark The Form Dirty ────────────────────────────────────
    // Any keystroke in either field sets dirty=true so the caller can tell whether
    // something actually changed when the dialog is closed.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches {@link ModifyListener}s to both text fields; each listener sets the dirty
     * flag to {@code true} when the field content changes.
     */
    private void addListeners()
    {
        attributeText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dirty = true;
            }
        } );

        valueText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dirty = true;
            }
        } );
    }


    // ── Writing The Form Values Back To The Model Object ─────────────────────────────────────
    // When the engineer clicks OK, we pull both field values out of the text boxes and write
    // them back to the AttributeValueObject before closing.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the current text field values back to the {@link AttributeValueObject} and
     * then closes the dialog via the superclass.
     */
    protected void okPressed()
    {
        attributeValueObject.setId( attributeText.getText() );
        attributeValueObject.setValue( valueText.getText() );

        super.okPressed();
    }


    // ── Exposing The Edited Model Object To The Caller ────────────────────────────────────────
    // After the dialog closes the caller retrieves the updated object from here.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AttributeValueObject} that was edited in this dialog.
     * After the dialog closes with OK, this object contains the updated values.
     *
     * @return the attribute-value pair, updated with the values the user entered
     */
    public AttributeValueObject getAttributeValueObject()
    {
        return attributeValueObject;
    }


    // ── Reporting Whether The User Changed Anything ───────────────────────────────────────────
    // The caller can skip expensive model updates if dirty is still false.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user changed anything in either text field.
     *
     * @return {@code true} if any field was modified; {@code false} if the dialog was
     *         opened and closed without changes
     */
    public boolean isDirty()
    {
        return dirty;
    }
}
