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
import org.eclipse.swt.widgets.Spinner;


// ── CLASS: MaxValueCountValueEditor — ISB ATTRIBUTE-LIMIT ENTRY TERMINAL ──────
// The ISB terminal for the "maxValueCount" protected-item row allows the officer
// to specify how many values a given attribute may hold.  The row encodes as
// "{ type cn, maxCount 5 }", and this editor parses it apart for editing
// before reassembling it on confirmation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractDialogStringValueEditor} for the {@code maxValueCount}
 * protected-item category in the ACI visual editor.
 * Parses the current row value (format: {@code { type atName, maxCount N }}),
 * opens an inner {@code MaxValueCountDialog} for editing, and re-encodes the
 * result on confirmation.
 * Think of this as the ISB attribute-limit terminal: pick a type, set a count,
 * the directive row stores both as one encoded string.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MaxValueCountValueEditor extends AbstractDialogStringValueEditor
{

    private static final String L_CURLY_TYPE = "{ type "; //$NON-NLS-1$
    private static final String SEP_MAXCOUNT = ", maxCount "; //$NON-NLS-1$
    private static final String R_CURLY = " }"; //$NON-NLS-1$
    private static final String EMPTY = ""; //$NON-NLS-1$


    // ── OPEN THE MAX-VALUE-COUNT DIALOG ───────────────────────────────────────
    // The ISB terminal opens the two-field dialog: attribute-type combo and
    // integer spinner.  If both fields are confirmed non-empty, we encode the
    // pair as "{ type atName, maxCount N }" and store it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the inner {@code MaxValueCountDialog} and, if the user confirms
     * with a non-empty type and a valid count, stores the encoded result.
     *
     * <p>For example — editing an existing maxValueCount row:</p>
     * <pre>
     *   cellEditor.activate();
     *   // → MaxValueCountDialog opens pre-filled with "cn", 5
     *   // user changes to "sn", 10
     *   // → setValue("{ type sn, maxCount 10 }") is called
     * </pre>
     *
     * @param shell  the parent shell for the dialog
     * @return       {@code true} if the user confirmed a valid type and count
     */
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();
        if ( value instanceof MaxValueCountValueEditorRawValueWrapper )
        {
            MaxValueCountValueEditorRawValueWrapper wrapper = ( MaxValueCountValueEditorRawValueWrapper ) value;
            MaxValueCountDialog dialog = new MaxValueCountDialog( shell, wrapper.schema, wrapper.type, wrapper.maxCount );
            if ( dialog.open() == TextDialog.OK && !EMPTY.equals( dialog.getType() ) && dialog.getMaxCount() > -1 )
            {
                setValue( L_CURLY_TYPE + dialog.getType() + SEP_MAXCOUNT + dialog.getMaxCount() + R_CURLY );
                return true;
            }
        }
        return false;
    }


    // ── BUILD THE RAW VALUE WRAPPER FROM AN IVALUE ────────────────────────────
    // We extract the string representation from the IValue and delegate to the
    // private overload that pulls schema from the connection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link MaxValueCountValueEditorRawValueWrapper} containing the
     * schema, parsed attribute type, and parsed max count from {@code value}.
     * Returns {@code null} if the value is unavailable.
     *
     * @param value  the LDAP attribute value to wrap
     * @return       the raw value wrapper, or {@code null}
     */
    public Object getRawValue( IValue value )
    {
        if ( value != null )
        {
            return getRawValue( value.getAttribute().getEntry().getBrowserConnection(),
                                value.getStringValue() );
        }
        else
        {
            return null;
        }
    }


    // ── PARSE THE ENCODED STRING ──────────────────────────────────────────────
    // We regex-parse the "{ type atName, maxCount N }" string and bundle the
    // three pieces (schema, type, count) into a wrapper for the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses {@code value} as {@code { type atName, maxCount N }} and bundles
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
        int maxCount = 0;
        try
        {
            // for example: { type userPassword, maxCount 10 }
            Pattern pattern = Pattern.compile( "\\s*\\{\\s*type\\s*([^,]*),\\s*maxCount\\s*(\\d*)\\s*\\}\\s*" ); //$NON-NLS-1$
            Matcher matcher = pattern.matcher( stringValue );
            type = matcher.matches() ? matcher.group( 1 ) : EMPTY;
            maxCount = matcher.matches() ? Integer.valueOf( matcher.group( 2 ) ) : 0;
        }
        catch ( Exception e )
        {
        }

        MaxValueCountValueEditorRawValueWrapper wrapper = new MaxValueCountValueEditorRawValueWrapper( schema, type,
            maxCount );
        return wrapper;
    }

    // ── CLASS: MaxValueCountValueEditorRawValueWrapper — SCHEMA + TYPE + COUNT ─
    // A private DTO that carries schema, attribute type, and max count from
    // getRawValue() to openDialog() so the inner dialog can pre-populate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private DTO that bundles the schema, attribute type, and max count for
     * the inner {@code MaxValueCountDialog}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class MaxValueCountValueEditorRawValueWrapper
    {
        /**
         * The schema, used in MaxValueCountDialog to build the list
         * with possible attribute types.
         */
        private Schema schema;

        /** The attribute type, used as initial attribute type. */
        private String type;

        /** The max count, used as initial value. */
        private int maxCount;


        // ── BUNDLE THE THREE PIECES ───────────────────────────────────────────
        /**
         * Creates a new {@code MaxValueCountValueEditorRawValueWrapper}.
         *
         * @param schema    the LDAP schema for the attribute-type combo
         * @param type      the pre-parsed attribute type
         * @param maxCount  the pre-parsed max value count
         */
        private MaxValueCountValueEditorRawValueWrapper( Schema schema, String type, int maxCount )
        {
            this.schema = schema;
            this.type = type;
            this.maxCount = maxCount;
        }
    }

    // ── CLASS: MaxValueCountDialog — ATTRIBUTE-TYPE + COUNT EDITOR DIALOG ─────
    // The inner dialog lays out: "{ type " + combo + ", maxCount " + spinner + " }"
    // as a compact inline form so the encoded format is visually clear.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private inner {@link Dialog} that presents a schema-driven attribute-type
     * combo and an integer spinner, arranged inline to mirror the ACI encoding
     * {@code { type atName, maxCount N }}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class MaxValueCountDialog extends Dialog
    {

        /** The schema. */
        private Schema schema;

        /** The initial attribute type. */
        private String initialType;

        /** The initial max count. */
        private int initialMaxCount;

        /** The attribute type combo. */
        private Combo attributeTypeCombo;

        /** The max count spinner. */
        private Spinner maxCountSpinner;

        /** The return attribute type. */
        private String returnType;

        /** The return value. */
        private int returnMaxCount;


        // ── OPEN THE INLINE-FORMAT DIALOG ─────────────────────────────────────
        // Pre-fill the combo and spinner with the parsed values so the officer
        // sees the current state and can adjust it.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a new {@code MaxValueCountDialog}.
         *
         * @param parentShell     the parent SWT shell
         * @param schema          the LDAP schema for the attribute-type combo
         * @param initialType     the pre-parsed attribute type
         * @param initialMaxCount the pre-parsed max value count
         */
        public MaxValueCountDialog( Shell parentShell, Schema schema, String initialType, int initialMaxCount )
        {
            super( parentShell );
            super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
            this.initialType = initialType;
            this.initialMaxCount = initialMaxCount;
            this.schema = schema;
            this.returnType = null;
            this.returnMaxCount = -1;
        }


        // ── SET TITLE AND ICON ────────────────────────────────────────────────
        /**
         * {@inheritDoc}
         */
        protected void configureShell( Shell shell )
        {
            super.configureShell( shell );
            shell.setText( Messages.getString( "MaxValueCountValueEditor.title" ) ); //$NON-NLS-1$
            shell.setImage( Activator.getDefault().getImage( Messages.getString( "MaxValueCountValueEditor.icon" ) ) ); //$NON-NLS-1$
        }


        // ── SNAPSHOT BOTH FIELDS ON OK ────────────────────────────────────────
        /**
         * {@inheritDoc}
         */
        protected void okPressed()
        {
            returnType = attributeTypeCombo.getText();
            returnMaxCount = maxCountSpinner.getSelection();
            super.okPressed();
        }


        // ── BUILD THE INLINE FIVE-WIDGET FORM ─────────────────────────────────
        // Layout: label("{ type ") + combo + label(", maxCount ") + spinner + label(" }")
        // mirrors the ACI text format so officers can see exactly what they're editing.
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

            // attribute combo with field decoration and content proposal
            attributeTypeCombo = BaseWidgetUtils.createCombo( composite, allAtNames, -1, 1 );
            attributeTypeCombo.setText( initialType );
            new ExtendedContentAssistCommandAdapter( attributeTypeCombo, new ComboContentAdapter(),
                new ListContentProposalProvider( attributeTypeCombo.getItems() ), null, null, true );

            BaseWidgetUtils.createLabel( composite, SEP_MAXCOUNT, 1 );

            maxCountSpinner = new Spinner( composite, SWT.BORDER );
            maxCountSpinner.setMinimum( 0 );
            maxCountSpinner.setMaximum( Integer.MAX_VALUE );
            maxCountSpinner.setDigits( 0 );
            maxCountSpinner.setIncrement( 1 );
            maxCountSpinner.setPageIncrement( 100 );
            maxCountSpinner.setSelection( initialMaxCount );
            maxCountSpinner.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

            BaseWidgetUtils.createLabel( composite, R_CURLY, 1 );

            applyDialogFont( composite );
            return composite;
        }


        // ── RETURN THE CONFIRMED ATTRIBUTE TYPE ───────────────────────────────
        /**
         * Returns the attribute type chosen in the combo, or {@code null} if cancelled.
         *
         * @return the attribute type, or {@code null}
         */
        public String getType()
        {
            return returnType;
        }


        // ── RETURN THE CONFIRMED MAX COUNT ────────────────────────────────────
        /**
         * Returns the max count set in the spinner, or {@code -1} if cancelled.
         *
         * @return the max count, or {@code -1}
         */
        public int getMaxCount()
        {
            return returnMaxCount;
        }

    }

}
