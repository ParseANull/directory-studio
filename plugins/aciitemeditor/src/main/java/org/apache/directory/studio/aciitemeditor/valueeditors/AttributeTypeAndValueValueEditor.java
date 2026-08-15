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


import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AttributeTypeAndValueValueEditor — ISB ATTRIBUTE-VALUE TERMINAL ────
// The ISB data-entry terminal for the "attributeValue" protected-item row.
// When the officer double-clicks the row, this editor opens
// AttributeTypeAndValueDialog so he can pick a schema attribute type and type
// a value; the result is encoded as "type=value" and stored back in the row.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractDialogStringValueEditor} for the {@code attributeValue}
 * protected-item category in the ACI visual editor.
 * Opens an {@link AttributeTypeAndValueDialog} when activated; encodes the
 * result as {@code "attributeType=value"} and passes it back to the table row.
 * Think of this as the ISB attribute-value terminal: one dialog, two fields,
 * one encoded result.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeAndValueValueEditor extends AbstractDialogStringValueEditor
{
    // ── OPEN THE TWO-FIELD DIALOG ─────────────────────────────────────────────
    // The ISB terminal opens so the officer can pick an attribute type and enter
    // a value.  If he confirms with non-empty fields, we encode "type=value"
    // and tell the cell editor the new raw value is ready.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link AttributeTypeAndValueDialog} and, if the user confirms
     * with non-empty fields, stores the result as {@code "attributeType=value"}
     * in the cell editor's value.
     *
     * <p>For example — the table composite opens this editor on double-click:</p>
     * <pre>
     *   cellEditor.activate();
     *   // → AttributeTypeAndValueDialog opens
     *   // user selects "cn", types "John Doe"
     *   // → setValue("cn=John Doe") is called on this editor
     * </pre>
     *
     * @param shell  the parent shell for the dialog
     * @return       {@code true} if the user confirmed a non-empty attribute type and value
     */
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof AttributeTypeAndValueValueEditorRawValueWrapper )
        {
            AttributeTypeAndValueValueEditorRawValueWrapper wrapper = ( AttributeTypeAndValueValueEditorRawValueWrapper ) value;
            AttributeTypeAndValueDialog dialog = new AttributeTypeAndValueDialog( shell, wrapper.schema,
                wrapper.attributeType, wrapper.value );

            if ( ( dialog.open() == TextDialog.OK ) && !EMPTY.equals( dialog.getAttributeType() )
                && !EMPTY.equals( dialog.getValue() ) )
            {
                setValue( dialog.getAttributeType() + '=' + dialog.getValue() );

                return true;
            }
        }

        return false;
    }


    // ── BUILD THE RAW VALUE WRAPPER ───────────────────────────────────────────
    // Before opening the dialog, we need to pass the schema and the split-up
    // "type" and "value" separately.  We parse the existing "type=value" string,
    // bundle everything into a wrapper, and return it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link AttributeTypeAndValueValueEditorRawValueWrapper} containing
     * the schema, the attribute type, and the value parsed from the current
     * {@link IValue}'s string representation.
     * Returns {@code null} if the value or schema is unavailable.
     *
     * @param value  the LDAP attribute value to wrap; must be non-null
     * @return       the raw value wrapper, or {@code null}
     */
    public Object getRawValue( IValue value )
    {
        if ( value != null )
        {
            return getRawValue( value.getAttribute().getEntry().getBrowserConnection(),
                                value.getStringValue() );
        }

        return null;
    }


    // ── PARSE THE STRING INTO SCHEMA + TYPE + VALUE ───────────────────────────
    // We split the "type=value" string at the first "=" and bundle the pieces
    // with the connection's schema into a wrapper for the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses {@code value} as {@code "attributeType=value"} and bundles it
     * with the schema from {@code connection} into a raw value wrapper.
     *
     * @param connection  the browser connection whose schema to use
     * @param value       the string value to parse
     * @return            the wrapper, or {@code null} if no schema or invalid input
     */
    private Object getRawValue( IBrowserConnection connection, Object value )
    {
        Schema schema = null;

        if ( connection != null )
        {
            schema = connection.getSchema();
        }

        if ( ( schema == null ) || !( value instanceof String ) )
        {
            return null;
        }

        String atavValue = ( String ) value;
        String[] atav = atavValue.split( "=", 2 ); //$NON-NLS-1$
        String at = atav.length > 0 ? atav[0] : EMPTY;
        String v = atav.length > 1 ? atav[1] : EMPTY;
        AttributeTypeAndValueValueEditorRawValueWrapper wrapper = new AttributeTypeAndValueValueEditorRawValueWrapper(
            schema, at, v );

        return wrapper;
    }

    // ── CLASS: AttributeTypeAndValueValueEditorRawValueWrapper — THE PARSED PAIR
    // A private DTO that bundles schema + attribute type + value so the dialog
    // can be pre-populated without needing to re-parse anything inside it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private DTO that carries the schema, attribute type, and value
     * from {@link #getRawValue(IValue)} to {@link #openDialog(Shell)}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class AttributeTypeAndValueValueEditorRawValueWrapper
    {
        /**
         * The schema, used in AttributeTypeDialog to build the list
         * with possible attribute types.
         */
        private Schema schema;

        /** The attribute type, used as initial attribute type. */
        private String attributeType;

        /** The value, used as initial value. */
        private String value;


        // ── BUNDLE THE PARTS ──────────────────────────────────────────────────
        // Pack the three pieces into the wrapper so they travel together to
        // the dialog without losing context.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a new {@code AttributeTypeAndValueValueEditorRawValueWrapper}.
         *
         * @param schema         the LDAP schema for the attribute-type combo
         * @param attributeType  the pre-parsed attribute type
         * @param value          the pre-parsed attribute value
         */
        private AttributeTypeAndValueValueEditorRawValueWrapper( Schema schema, String attributeType, String value )
        {
            super();
            this.schema = schema;
            this.attributeType = attributeType;
            this.value = value;
        }
    }

}
