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

package org.apache.directory.studio.valueeditors.objectclass;


import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ObjectClassValueEditor — YODA'S LINEAGE CLASSIFIER ─────────────────
// Yoda doesn't just list a candidate's lineage — he appends the category:
// "(structural)", "(abstract)", "(auxiliary)", or "(obsolete)".  For the quick
// table view he reads the class name and category from the archives; for editing
// he opens the full Council session (ObjectClassDialog).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for the LDAP {@code objectClass} attribute.
 * For display we append the object class type (structural, abstract, auxiliary,
 * or obsolete) so the table cell reads like "inetOrgPerson (structural)".
 * For editing we open {@link ObjectClassDialog} which lets the user browse and
 * select from all schema-known object classes.
 * Editing via the search-result editor is not supported (returns null from
 * the {@link #getRawValue(AttributeHierarchy)} path).
 * Think of this as Yoda's lineage classifier — annotates the class in the table
 * and opens the Council session on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassValueEditor extends AbstractDialogStringValueEditor
{

    // ── Yoda Opens the Council Selection Session ──────────────────────────────
    // A candidate's objectClass attribute needs updating; Yoda's representative
    // opens the Council chamber (ObjectClassDialog) and waits for the verdict.
    // If the Council confirms a non-empty class name, the representative records
    // it in the entry.
    // We open ObjectClassDialog with the schema and current class value, and
    // commit the result if the user confirmed a non-empty string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens {@link ObjectClassDialog} for the user to select an object class from
     * the schema.
     * Returns {@code true} if the user clicked OK with a non-empty class name
     * (meaning the attribute value was updated); {@code false} otherwise.
     *
     * <p>For example — Yoda's representative opens the selection session:</p>
     * <pre>
     *   boolean changed = editor.openDialog(shell);
     *   if (changed) {
     *       // objectClass value is now updated
     *   }
     * </pre>
     *
     * @param shell  The parent SWT shell for the ObjectClassDialog.
     * @return       {@code true} if the class was updated; {@code false} otherwise.
     */
    @Override
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();
        if ( value instanceof ObjectClassValueEditorRawValueWrapper )
        {
            ObjectClassValueEditorRawValueWrapper wrapper = ( ObjectClassValueEditorRawValueWrapper ) value;
            ObjectClassDialog dialog = new ObjectClassDialog( shell, wrapper.schema, wrapper.objectClass );
            if ( dialog.open() == TextDialog.OK && !"".equals( dialog.getObjectClass() ) ) //$NON-NLS-1$
            {
                setValue( dialog.getObjectClass() );
                return true;
            }
        }
        return false;
    }


    // ── Yoda Reads the Lineage Type for the Table ─────────────────────────────
    // In the quick-overview table, Yoda doesn't open the full session — he just
    // reads the class name from the archives and appends its type tag.
    // "person (structural)", "extensibleObject (auxiliary)", etc.
    // We look up the ObjectClass from the schema and append the type/obsolete suffix.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a display string for the object class that includes the class type
     * in parentheses, e.g. {@code "inetOrgPerson (structural)"}.
     * If the value is empty or raw-values mode is on, we return the plain class name.
     * Returns {@link #NULL} if the value is null or not editable here.
     *
     * <p>For example — Yoda annotates the lineage in the table:</p>
     * <pre>
     *   "person"                → "person (structural)"
     *   "extensibleObject"      → "extensibleObject (auxiliary)"
     *   "obsoleteSomething"     → "obsoleteSomething (structural) (obsolete)"
     * </pre>
     *
     * @param value  The LDAP attribute value holding the object class name.
     * @return       The annotated display string.
     */
    @Override
    public String getDisplayValue( IValue value )
    {
        if ( getRawValue( value ) == null )
        {
            return NULL;
        }

        String displayValue = value.getStringValue();

        if ( !showRawValues() && !"".equals( displayValue ) ) //$NON-NLS-1$
        {
            Schema schema = value.getAttribute().getEntry().getBrowserConnection().getSchema();
            ObjectClass ocd = schema.getObjectClassDescription( displayValue );
            switch ( ocd.getType() )
            {
                case STRUCTURAL:
                    displayValue = displayValue + Messages.getString( "ObjectClassValueEditor.Structural" ); //$NON-NLS-1$
                    break;
                case ABSTRACT:
                    displayValue = displayValue + Messages.getString( "ObjectClassValueEditor.Abstract" ); //$NON-NLS-1$
                    break;
                case AUXILIARY:
                    displayValue = displayValue + Messages.getString( "ObjectClassValueEditor.Auxiliary" ); //$NON-NLS-1$
                    break;
            }
            if ( ocd.isObsolete() )
            {
                displayValue = displayValue + Messages.getString( "ObjectClassValueEditor.Obsolete" ); //$NON-NLS-1$
            }
        }

        return displayValue;
    }


    // ── Yoda Signals "Not My Department" for Search Results ──────────────────
    // The search-result editor asks Yoda to classify an entry, but Yoda says
    // "Modify objectClass in search results, we do not."  Returns null.
    // Editing objectClass via the search-result editor is deliberately unsupported.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code null} — editing the {@code objectClass} attribute via
     * the search-result editor is not supported.
     * This prevents accidental mass-modification of object classes across a result set.
     *
     * @param attributeHierarchy  The attribute hierarchy from the search result.
     * @return                    Always {@code null}.
     */
    @Override
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        return null;
    }


    // ── Yoda Packages the Lineage for the Council Session ────────────────────
    // Before opening the Council session, Yoda's representative wraps the current
    // class name and the schema together in a briefing packet for the dialog.
    // If the value isn't a string objectClass attribute, the packet is null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ObjectClassValueEditorRawValueWrapper} carrying the schema
     * and current object class name for the given attribute value.
     * Returns {@code null} if the value is not a string, is null, or doesn't
     * belong to the {@code objectClass} attribute.
     *
     * @param value  The LDAP attribute value to wrap.
     * @return       A wrapper with schema and class name, or {@code null}.
     */
    @Override
    public Object getRawValue( IValue value )
    {
        if ( value == null || !value.isString() || !value.getAttribute().isObjectClassAttribute() )
        {
            return null;
        }
        else
        {
            return getRawValue( value.getAttribute().getEntry().getBrowserConnection(), value.getStringValue() );
        }
    }


    // ── Yoda Assembles the Council Briefing Packet ────────────────────────────
    // Given the connection and the raw object class name, Yoda assembles the
    // briefing packet (schema + class name) that ObjectClassDialog expects.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link ObjectClassValueEditorRawValueWrapper} from the given
     * browser connection and raw object class string.
     * Returns {@code null} if the value is not a String.
     *
     * @param connection  The browser connection, used to retrieve the schema.
     * @param value       The raw object class name string.
     * @return            A wrapper, or {@code null} if {@code value} is not a String.
     */
    private Object getRawValue( IBrowserConnection connection, Object value )
    {
        Schema schema = null;

        if ( connection != null )
        {
            schema = connection.getSchema();
        }

        if ( !( value instanceof String ) )
        {
            return null;
        }

        String ocValue = ( String ) value;
        ObjectClassValueEditorRawValueWrapper wrapper = new ObjectClassValueEditorRawValueWrapper( schema, ocValue );

        return wrapper;
    }

    // ── CLASS: ObjectClassValueEditorRawValueWrapper — YODA'S BRIEFING PACKET ─
    // Before the Council session, Yoda's representative assembles a sealed
    // briefing packet: the schema (the full roster of lineages) and the current
    // class name (the candidate's present classification).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Internal transfer object that passes the schema and current object class
     * name to {@link ObjectClassDialog}.
     * The schema is needed to build the dropdown list of all known class names;
     * the object class string is used to pre-select the current value.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class ObjectClassValueEditorRawValueWrapper
    {
        /**
         * The schema, used in ObjectClassDialog to build the list
         * with possible object classes.
         */
        private Schema schema;

        /** The object class, used as initial value in ObjectClassDialog. */
        private String objectClass;


        // ── Yoda's Representative Seals the Briefing Packet ──────────────────
        // The representative binds the schema and the candidate's current class
        // into a single sealed packet before presenting it to the Council chamber.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Creates a new ObjectClassValueEditorRawValueWrapper.
         *
         * @param schema       The directory schema for building the class list.
         * @param objectClass  The current object class name to pre-select.
         */
        private ObjectClassValueEditorRawValueWrapper( Schema schema, String objectClass )
        {
            super();
            this.schema = schema;
            this.objectClass = objectClass;
        }
    }

}
