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


// ── CLASS: AttributeTypeValueEditor — ISB SINGLE-TYPE SELECTION TERMINAL ──────
// The ISB terminal for "attributeType", "allAttributeValues", and "selfValue"
// protected-item rows.  When the officer activates the row, this editor opens
// AttributeTypeDialog so he can pick one schema attribute type; the result is
// stored as a plain type name back in the row.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractDialogStringValueEditor} for ACI protected-item categories that
 * store a single attribute type name.
 * Opens an {@link AttributeTypeDialog} when activated and stores the confirmed
 * type name as the new raw value.
 * Think of this as the ISB single-type terminal: one combo, one confirmed name,
 * done.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeValueEditor extends AbstractDialogStringValueEditor
{

    private static final String EMPTY = ""; //$NON-NLS-1$


    // ── OPEN THE SINGLE-TYPE DIALOG ───────────────────────────────────────────
    // The ISB terminal opens so the officer can pick one attribute type.
    // If he confirms with a non-empty selection, we store it and return true.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link AttributeTypeDialog} and stores the confirmed attribute
     * type name as the new cell value.
     *
     * <p>For example — the table composite opens this editor on double-click:</p>
     * <pre>
     *   cellEditor.activate();
     *   // → AttributeTypeDialog opens, user selects "sn"
     *   // → setValue("sn") is called on this editor
     * </pre>
     *
     * @param shell  the parent shell for the dialog
     * @return       {@code true} if the user confirmed a non-empty attribute type
     */
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof AttributeTypeValueEditorRawValueWrapper )
        {
            AttributeTypeValueEditorRawValueWrapper wrapper = ( AttributeTypeValueEditorRawValueWrapper ) value;
            AttributeTypeDialog dialog = new AttributeTypeDialog( shell, wrapper.schema, wrapper.attributeType );

            if ( ( dialog.open() == TextDialog.OK ) && !EMPTY.equals( dialog.getAttributeType() ) )
            {
                setValue( dialog.getAttributeType() );

                return true;
            }
        }

        return false;
    }


    // ── BUILD THE RAW VALUE WRAPPER ───────────────────────────────────────────
    // We bundle the schema and the current attribute type into a wrapper so
    // the dialog can pre-populate its combo.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link AttributeTypeValueEditorRawValueWrapper} containing the
     * schema and the current attribute type from {@code value}'s string
     * representation.
     * Returns {@code null} if the value or schema is unavailable.
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

        return null;
    }


    // ── BUNDLE SCHEMA + TYPE NAME ─────────────────────────────────────────────
    // We pull the schema out of the connection and wrap it with the current
    // string value so the dialog can pre-fill its combo.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Bundles the schema from {@code connection} with the string {@code value}
     * into a raw value wrapper.
     *
     * @param connection  the browser connection whose schema to use
     * @param value       the current attribute type string
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

        String atValue = ( String ) value;
        AttributeTypeValueEditorRawValueWrapper wrapper = new AttributeTypeValueEditorRawValueWrapper( schema, atValue );

        return wrapper;
    }

    // ── CLASS: AttributeTypeValueEditorRawValueWrapper — THE SCHEMA-TYPE BUNDLE
    // A private DTO carrying the schema and the current attribute type so they
    // travel together from getRawValue() to openDialog() without reparsing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private DTO that bundles the schema and the current attribute type for
     * use by {@link AttributeTypeDialog}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class AttributeTypeValueEditorRawValueWrapper
    {
        /**
         * The schema, used in AttributeTypeDialog to build the list
         * with possible attribute types.
         */
        private Schema schema;

        /** The attribute type, used as initial value in AttributeTypeDialog. */
        private String attributeType;


        // ── BUNDLE THE PIECES ─────────────────────────────────────────────────
        // Pack schema and type into the wrapper so they reach the dialog as
        // a single object.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a new {@code AttributeTypeValueEditorRawValueWrapper}.
         *
         * @param schema         the LDAP schema for the attribute-type combo
         * @param attributeType  the current attribute type string
         */
        private AttributeTypeValueEditorRawValueWrapper( Schema schema, String attributeType )
        {
            super();
            this.schema = schema;
            this.attributeType = attributeType;
        }
    }
}
