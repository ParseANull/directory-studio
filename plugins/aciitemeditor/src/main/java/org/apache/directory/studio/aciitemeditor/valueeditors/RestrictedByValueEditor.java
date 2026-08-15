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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.common.widgets.ListContentProposalProvider;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
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


// ── CLASS: RestrictedByValueEditor — ISB CROSS-ATTRIBUTE RESTRICTION TERMINAL ──
// The ISB terminal for the "restrictedBy" protected-item row specifies that
// the values of one attribute type are constrained by the values of another.
// The row encodes as "{ type cn, valuesIn sn }" — two attribute types.
// This editor parses that pair, opens a two-combo dialog for editing, and
// re-encodes on confirmation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractDialogStringValueEditor} for the {@code restrictedBy}
 * protected-item category in the ACI visual editor.
 * Parses the current row value (format: {@code { type atA, valuesIn atB }}),
 * opens an inner {@code RestrictedByDialog} for editing, and re-encodes the
 * result on confirmation.
 * Think of this as the ISB cross-attribute restriction terminal: two combos,
 * two attribute types, one cross-reference constraint.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RestrictedByValueEditor extends AbstractDialogStringValueEditor
{

    private static final String L_CURLY_TYPE = "{ type "; //$NON-NLS-1$
    private static final String SEP_VALUESIN = ", valuesIn "; //$NON-NLS-1$
    private static final String R_CURLY = " }"; //$NON-NLS-1$
    private static final String EMPTY = ""; //$NON-NLS-1$


    // ── OPEN THE TWO-COMBO DIALOG ─────────────────────────────────────────────
    // The ISB terminal opens a dialog with two schema-driven combos.
    // If the officer confirms both fields as non-empty, we encode the pair as
    // "{ type atA, valuesIn atB }" and store it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the inner {@code RestrictedByDialog} and, if the user confirms
     * both attribute type fields as non-empty, stores the encoded result.
     *
     * <p>For example — editing an existing restrictedBy row:</p>
     * <pre>
     *   cellEditor.activate();
     *   // → RestrictedByDialog opens pre-filled with "sn", "cn"
     *   // user changes valuesIn to "givenName"
     *   // → setValue("{ type sn, valuesIn givenName }") is called
     * </pre>
     *
     * @param shell  the parent shell for the dialog
     * @return       {@code true} if the user confirmed two non-empty attribute types
     */
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof RestrictedByValueEditorRawValueWrapper )
        {
            RestrictedByValueEditorRawValueWrapper wrapper = ( RestrictedByValueEditorRawValueWrapper ) value;
            RestrictedByDialog dialog = new RestrictedByDialog( shell, wrapper.schema, wrapper.type, wrapper.valuesIn );

            if ( dialog.open() == TextDialog.OK && !EMPTY.equals( dialog.getType() )
                && !EMPTY.equals( dialog.getValuesIn() ) )
            {
                setValue( L_CURLY_TYPE + dialog.getType() + SEP_VALUESIN + dialog.getValuesIn() + R_CURLY );
                return true;
            }
        }

        return false;
    }


    // ── BUILD THE RAW VALUE WRAPPER FROM AN IVALUE ────────────────────────────
    /**
     * Returns a {@link RestrictedByValueEditorRawValueWrapper} containing the
     * schema and parsed attribute type pair from {@code value}.
     * Returns {@code null} if the value is unavailable.
     *
     * @param value  the LDAP attribute value to wrap
     * @return       the raw value wrapper, or {@code null}
     */
    public Object getRawValue( IValue value )
    {
        if ( value != null )
        {
            return getRawValue( value.getAttribute().getEntry().getBrowserConnection(), value
                .getStringValue() );
        }

        return null;
    }


    // ── PARSE THE ENCODED STRING ──────────────────────────────────────────────
    // We regex-parse "{ type atA, valuesIn atB }" and bundle the pieces with
    // the schema into a wrapper for the inner dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses {@code value} as {@code { type atA, valuesIn atB }} and bundles
     * the pieces with the schema from {@code connection} into a raw value wrapper.
     *
     * @param connection  the browser connection whose schema to use
     * @param value       the encoded row string to parse
     * @return            the wrapper, or {@code null} if no schema or invalid input
     */
    private Object getRawValue( IBrowserConnection connection, Object value )
    {
        Schema schema = null;

        if ( connection != null )
        {
            schema = connection.getSchema();
        }

        if ( schema == null || !( value instanceof String ) )
        {
            return null;
        }

        String stringValue = ( String ) value;
        String type = EMPTY;
        String valuesIn = EMPTY;
        try
        {
            // for example: { type sn, valuesIn cn }
            Pattern pattern = Pattern
                .compile( "\\s*\\{\\s*type\\s*([^,\\s]*)\\s*,\\s*valuesIn\\s*([^,\\s]*)\\s*\\}\\s*" ); //$NON-NLS-1$
            Matcher matcher = pattern.matcher( stringValue );
            type = matcher.matches() ? matcher.group( 1 ) : EMPTY;
            valuesIn = matcher.matches() ? matcher.group( 2 ) : EMPTY;
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
        }

        RestrictedByValueEditorRawValueWrapper wrapper = new RestrictedByValueEditorRawValueWrapper( schema, type,
            valuesIn );
        return wrapper;
    }

    // ── CLASS: RestrictedByValueEditorRawValueWrapper — SCHEMA + TYPE + VALUESIN
    // A private DTO bundling schema and the two parsed attribute type names so
    // they travel together from getRawValue() to openDialog().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private DTO that bundles the schema and parsed attribute type pair for
     * the inner {@code RestrictedByDialog}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class RestrictedByValueEditorRawValueWrapper
    {
        /**
         * The schema, used in RestrictedByDialog to build the list
         * with possible attribute types.
         */
        private final Schema schema;

        /** The type, used as initial type. */
        private final String type;

        /** The values in, used as initial values in. */
        private final String valuesIn;


        // ── BUNDLE THE THREE PIECES ───────────────────────────────────────────
        /**
         * Creates a new {@code RestrictedByValueEditorRawValueWrapper}.
         *
         * @param schema    the LDAP schema for both combos
         * @param type      the pre-parsed "type" attribute type
         * @param valuesIn  the pre-parsed "valuesIn" attribute type
         */
        private RestrictedByValueEditorRawValueWrapper( Schema schema, String type, String valuesIn )
        {
            this.schema = schema;
            this.type = type;
            this.valuesIn = valuesIn;
        }
    }

    // ── CLASS: RestrictedByDialog — TWO-COMBO CROSS-ATTRIBUTE EDITOR DIALOG ───
    // The inner dialog lays out: "{ type " + combo + ", valuesIn " + combo + " }"
    // mirroring the ACI encoding so officers can see exactly what they're editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private inner {@link Dialog} presenting two schema-driven attribute-type
     * combos (with content assist), arranged inline to mirror the ACI encoding
     * {@code { type atA, valuesIn atB }}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class RestrictedByDialog extends Dialog
    {

        /** The schema. */
        private Schema schema;

        /** The initial type. */
        private String initialType;

        /** The initial values in. */
        private String initialValuesIn;

        /** The type combo. */
        private Combo typeCombo;

        /** The values in combo. */
        private Combo valuesInCombo;

        /** The return type. */
        private String returnType;

        /** The return values in. */
        private String returnValuesIn;


        // ── OPEN THE TWO-COMBO DIALOG ─────────────────────────────────────────
        /**
         * Creates a new {@code RestrictedByDialog}.
         *
         * @param parentShell      the parent SWT shell
         * @param schema           the LDAP schema for both combos
         * @param initialType      the pre-parsed "type" attribute type
         * @param initialValuesIn  the pre-parsed "valuesIn" attribute type
         */
        public RestrictedByDialog( Shell parentShell, Schema schema, String initialType, String initialValuesIn )
        {
            super( parentShell );
            super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
            this.initialType = initialType;
            this.initialValuesIn = initialValuesIn;
            this.schema = schema;
            this.returnType = null;
            this.returnValuesIn = null;
        }


        // ── SET TITLE AND ICON ────────────────────────────────────────────────
        /**
         * {@inheritDoc}
         */
        protected void configureShell( Shell shell )
        {
            super.configureShell( shell );
            shell.setText( Messages.getString( "RestrictedByValueEditor.title" ) ); //$NON-NLS-1$
            shell.setImage( Activator.getDefault().getImage( Messages.getString( "RestrictedByValueEditor.icon" ) ) ); //$NON-NLS-1$
        }


        // ── SNAPSHOT BOTH COMBOS ON OK ────────────────────────────────────────
        /**
         * {@inheritDoc}
         */
        protected void okPressed()
        {
            returnType = typeCombo.getText();
            returnValuesIn = valuesInCombo.getText();
            super.okPressed();
        }


        // ── BUILD THE INLINE FIVE-WIDGET FORM ─────────────────────────────────
        // Layout: label + type combo + label + valuesIn combo + label, mirroring
        // the ACI text "{ type atA, valuesIn atB }".
        // ─────────────────────────────────────────────────────────────────────
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
            composite.setLayout( new GridLayout( 5, false ) );

            BaseWidgetUtils.createLabel( composite, L_CURLY_TYPE, 1 );

            // combo widget
            Collection<String> names = SchemaUtils.getNames( schema.getAttributeTypeDescriptions() );
            String[] allAtNames = names.toArray( new String[names.size()] );
            Arrays.sort( allAtNames );

            // type combo with field decoration and content proposal
            typeCombo = BaseWidgetUtils.createCombo( composite, allAtNames, -1, 1 );
            typeCombo.setText( initialType );
            new ExtendedContentAssistCommandAdapter( typeCombo, new ComboContentAdapter(),
                new ListContentProposalProvider( typeCombo.getItems() ), null, null, true );

            BaseWidgetUtils.createLabel( composite, SEP_VALUESIN, 1 );

            // valuesIn combo with field decoration and content proposal
            valuesInCombo = BaseWidgetUtils.createCombo( composite, allAtNames, -1, 1 );
            valuesInCombo.setText( initialValuesIn );
            new ExtendedContentAssistCommandAdapter( valuesInCombo, new ComboContentAdapter(),
                new ListContentProposalProvider( valuesInCombo.getItems() ), null, null, true );

            BaseWidgetUtils.createLabel( composite, R_CURLY, 1 );

            applyDialogFont( composite );
            return composite;
        }


        // ── RETURN THE CONFIRMED TYPE ─────────────────────────────────────────
        /**
         * Returns the "type" attribute type confirmed in the combo, or {@code null}
         * if cancelled.
         *
         * @return the attribute type, or {@code null}
         */
        public String getType()
        {
            return returnType;
        }


        // ── RETURN THE CONFIRMED VALUESIN ─────────────────────────────────────
        /**
         * Returns the "valuesIn" attribute type confirmed in the combo, or
         * {@code null} if cancelled.
         *
         * @return the attribute type, or {@code null}
         */
        public String getValuesIn()
        {
            return returnValuesIn;
        }
    }
}
