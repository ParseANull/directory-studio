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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: AttributeTypeAndValueDialog — ISB TERMINAL FOR ATTRIBUTE-VALUE PAIRS
// The ISB data-entry terminal for the "attributeValue" protected-item row
// presents two fields side by side: an attribute-type drop-down (autocomplete
// from the schema) and a free-text value field.
// The officer picks a type, types a value, and the terminal encodes the pair
// as "type=value" for insertion into the ACI directive.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Dialog} for entering an attribute type and a value together.
 * Used by {@link AttributeTypeAndValueValueEditor} when the user edits an
 * {@code attributeValue} protected item.
 * Displays a schema-driven combo for the attribute type (with content assist)
 * and a free-text field for the value, separated by " = ".
 * Think of this as the ISB terminal for attribute-value pairs: two fields,
 * one clear separator, one combined result.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeAndValueDialog extends Dialog
{

    /** The schema. */
    private Schema schema;

    /** The initial attribute type. */
    private String initialAttributeType;

    /** The initial value. */
    private String initialValue;

    /** The attribute type combo. */
    private Combo attributeTypeCombo;

    /** The value text. */
    private Text valueText;

    /** The return attribute type. */
    private String returnAttributeType;

    /** The return value. */
    private String returnValue;


    // ── OPEN THE ATTRIBUTE-VALUE TERMINAL ─────────────────────────────────────
    // The ISB terminal opens pre-filled with the existing attribute type and value
    // (if editing) or blank (if adding a new attributeValue entry).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code AttributeTypeAndValueDialog}.
     *
     * <p>For example — editing an existing attributeValue entry:</p>
     * <pre>
     *   AttributeTypeAndValueDialog dlg =
     *       new AttributeTypeAndValueDialog(shell, schema, "cn", "John Doe");
     *   if (dlg.open() == Dialog.OK) {
     *     String type  = dlg.getAttributeType();  // e.g. "sn"
     *     String value = dlg.getValue();           // e.g. "Smith"
     *   }
     * </pre>
     *
     * @param parentShell           the parent SWT shell
     * @param schema                the LDAP schema used to populate the attribute-type combo
     * @param initialAttributeType  pre-filled attribute type, or empty string for blank
     * @param initialValue          pre-filled attribute value, or empty string for blank
     */
    public AttributeTypeAndValueDialog( Shell parentShell, Schema schema, String initialAttributeType,
        String initialValue )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.initialAttributeType = initialAttributeType;
        this.initialValue = initialValue;
        this.schema = schema;
        this.returnValue = null;
    }


    // ── SET TITLE AND ICON ────────────────────────────────────────────────────
    // The orderly labels the terminal window with the appropriate title.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "AttributeTypeAndValueDialog.title" ) ); //$NON-NLS-1$
        shell.setImage( Activator.getDefault().getImage( Messages.getString( "AttributeTypeAndValueDialog.icon" ) ) ); //$NON-NLS-1$
    }


    // ── COMMIT THE PAIR ───────────────────────────────────────────────────────
    // Grand Moff confirms the attribute type and value; we snapshot both fields
    // into the return variables before delegating to the superclass close.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        returnAttributeType = attributeTypeCombo.getText();
        returnValue = valueText.getText();
        super.okPressed();
    }


    // ── BUILD THE TWO-FIELD FORM ──────────────────────────────────────────────
    // The orderly lays out three widgets in a row: attribute-type combo,
    // " = " separator label, and value text field.
    // Content assist is attached to the combo so the user can type a prefix
    // and see matching attribute types from the schema.
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
        composite.setLayout( new GridLayout( 3, false ) );

        // combo widget
        Collection<String> names = SchemaUtils.getNames( schema.getAttributeTypeDescriptions() );
        String[] allAtNames = names.toArray( new String[names.size()] );
        Arrays.sort( allAtNames );

        // attribute combo with field decoration and content proposal
        attributeTypeCombo = BaseWidgetUtils.createCombo( composite, allAtNames, -1, 1 );
        attributeTypeCombo.setText( initialAttributeType );
        new ExtendedContentAssistCommandAdapter( attributeTypeCombo, new ComboContentAdapter(),
            new ListContentProposalProvider( attributeTypeCombo.getItems() ), null, null, true );

        BaseWidgetUtils.createLabel( composite, " = ", 1 ); //$NON-NLS-1$

        valueText = BaseWidgetUtils.createText( composite, initialValue, 1 );

        applyDialogFont( composite );
        return composite;
    }


    // ── RETURN THE SELECTED TYPE ──────────────────────────────────────────────
    // The value editor retrieves the chosen attribute type to build the
    // "type=value" string for the ACI directive.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type chosen in the combo, or {@code null} if the
     * dialog was cancelled.
     *
     * @return the attribute type string, or {@code null}
     */
    public String getAttributeType()
    {
        return returnAttributeType;
    }


    // ── RETURN THE ENTERED VALUE ──────────────────────────────────────────────
    // The value editor retrieves the entered value to complete the pair.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value entered in the text field, or {@code null} if the
     * dialog was cancelled.
     *
     * @return the attribute value string, or {@code null}
     */
    public String getValue()
    {
        return returnValue;
    }

}
