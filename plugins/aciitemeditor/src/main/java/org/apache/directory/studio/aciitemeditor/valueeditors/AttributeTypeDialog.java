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

package org.apache.directory.studio.aciitemeditor.valueeditors;


import java.util.Arrays;
import java.util.Collection;

import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.widgets.ListContentProposalProvider;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AttributeTypeDialog — ISB ATTRIBUTE-TYPE SELECTION TERMINAL ─────────
// The ISB terminal for the "attributeType" protected-item row shows a single
// schema-driven drop-down: the officer picks one attribute type from the list,
// and the terminal returns the chosen name back to the table row.
// AttributeTypeDialog is that single-field terminal.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Dialog} for selecting a single attribute type from a schema-driven
 * combo (with content assist).
 * Used by {@link AttributeTypeValueEditor} when the user edits an
 * {@code attributeType}, {@code allAttributeValues}, or {@code selfValue}
 * protected-item row.
 * Think of this as the ISB attribute-type terminal: one combo, one result,
 * the schema keeps the list valid.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeDialog extends Dialog
{

    /** The schema. */
    private Schema schema;

    /** The initial value. */
    private String initialValue;

    /** The attribute type combo. */
    private Combo attributeTypeCombo;

    /** The return value. */
    private String returnValue;


    // ── OPEN THE ATTRIBUTE-TYPE TERMINAL ──────────────────────────────────────
    // The ISB terminal opens pre-filled with the existing attribute type (if
    // editing) or blank (if adding a new type entry).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code AttributeTypeDialog}.
     *
     * <p>For example — editing an existing attributeType row:</p>
     * <pre>
     *   AttributeTypeDialog dlg = new AttributeTypeDialog(shell, schema, "cn");
     *   if (dlg.open() == Dialog.OK) {
     *     String chosen = dlg.getAttributeType();  // e.g. "sn"
     *   }
     * </pre>
     *
     * @param parentShell  the parent SWT shell
     * @param schema       the LDAP schema to populate the attribute-type combo
     * @param initialValue the pre-filled attribute type, or empty string for blank
     */
    public AttributeTypeDialog( Shell parentShell, Schema schema, String initialValue )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.initialValue = initialValue;
        this.schema = schema;
        this.returnValue = null;
    }


    // ── SET TITLE AND ICON ────────────────────────────────────────────────────
    // The orderly labels the terminal window.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "AttributeTypeDialog.title" ) ); //$NON-NLS-1$
        shell.setImage( Activator.getDefault().getImage( Messages.getString( "AttributeTypeDialog.icon" ) ) ); //$NON-NLS-1$
    }


    // ── STANDARD BUTTONS ─────────────────────────────────────────────────────
    // The standard OK/Cancel button bar is sufficient.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        super.createButtonsForButtonBar( parent );
    }


    // ── COMMIT THE CHOSEN TYPE ────────────────────────────────────────────────
    // Grand Moff selects an attribute type and presses OK; we snapshot the
    // combo's text before delegating to the superclass close.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        returnValue = attributeTypeCombo.getText();
        super.okPressed();
    }


    // ── BUILD THE SINGLE-COMBO FORM ───────────────────────────────────────────
    // The orderly lays out one schema-driven combo with content assist so the
    // officer can type a prefix and see matching attribute type names.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        // combo widget
        Collection<String> names = SchemaUtils.getNames( schema.getAttributeTypeDescriptions() );
        String[] allAtNames = names.toArray( new String[names.size()] );
        Arrays.sort( allAtNames );

        // attribute combo with field decoration and content proposal
        attributeTypeCombo = BaseWidgetUtils.createCombo( composite, allAtNames, -1, 1 );
        attributeTypeCombo.setText( initialValue );
        new ExtendedContentAssistCommandAdapter( attributeTypeCombo, new ComboContentAdapter(),
            new ListContentProposalProvider( attributeTypeCombo.getItems() ), null, null, true );

        applyDialogFont( composite );
        return composite;
    }


    // ── RETURN THE CHOSEN ATTRIBUTE TYPE ──────────────────────────────────────
    // The value editor retrieves the chosen name to write back into the
    // multi-valued list or the table row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type chosen in the combo, or {@code null} if the
     * dialog was cancelled.
     *
     * @return the attribute type string, or {@code null}
     */
    public String getAttributeType()
    {
        return returnValue;
    }

}
