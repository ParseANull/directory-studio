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

package org.apache.directory.studio.valueeditors.dn;


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.dialogs.DnDialog;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: DnValueEditor — C-3PO COMPUTING THE FULL CHAIN OF COMMAND ─────────
// When Admiral Piett asks C-3PO for the full title and chain of command for
// an officer, C-3PO doesn't just say "Commander" — he navigates the Imperial
// hierarchy and returns the full DN: "cn=Piett,ou=Admirals,o=Empire".
// In single-select mode C-3PO verifies one officer's credentials; in multi-select
// mode (when adding a fresh record) he can batch-enroll an entire squad at once,
// writing each DN as its own attribute value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for LDAP Distinguished Name syntax
 * (OID 1.3.6.1.4.1.1466.115.121.1.12).
 * A Distinguished Name (DN) is the full hierarchical path to an LDAP entry,
 * like {@code "cn=John,ou=People,dc=example,dc=com"}.
 * When editing an existing DN value we open a single-select DN picker dialog.
 * When adding a brand-new value (empty placeholder), we open the dialog in
 * multi-select mode so the user can add several members to a group in one step;
 * if more than one is chosen, we write the extras directly via
 * {@link CompoundModification} to avoid going through the cell-editor path twice.
 * Think of this as C-3PO's chain-of-command resolver — he navigates the hierarchy
 * and returns the full canonical path.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnValueEditor extends AbstractDialogStringValueEditor
{

    // ── C-3PO Opens the Chain-of-Command Lookup Console ──────────────────────
    // Admiral Piett asks C-3PO to update the "member" attribute for a group.
    // If the slot is empty (new entry), C-3PO offers a multi-select roster so
    // the Admiral can enroll multiple officers in one ceremony.  If a DN is
    // already set, single-select mode lets him correct one name at a time.
    // We branch on whether the current value is an empty placeholder to decide
    // which mode to open DnDialog in.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link DnDialog} for the user to browse the directory and pick a DN.
     * Two modes:
     * <ul>
     *   <li><strong>Multi-select</strong> — used when the current value is an empty
     *       placeholder (a newly added slot).  The user can pick multiple DNs at
     *       once; the first goes through the cell-editor path, the rest are written
     *       directly via {@link CompoundModification}.</li>
     *   <li><strong>Single-select</strong> — used for existing (non-empty) values.
     *       The chosen DN replaces the current one.</li>
     * </ul>
     * Returns {@code true} if the cell-editor path should commit a new value;
     * {@code false} if cancelled, or if all values were committed directly
     * (multi-select with more than one DN chosen).
     *
     * <p>For example — C-3PO updates the group's member list:</p>
     * <pre>
     *   // Empty slot: multi-select mode opens
     *   boolean changed = editor.openDialog(shell);
     *   // If the user picks 3 DNs, 2 are written directly; changed == false
     * </pre>
     *
     * @param shell  The parent SWT shell for the DnDialog.
     * @return       {@code true} if a single DN was set via the cell-editor;
     *               {@code false} otherwise.
     */
    @Override
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof DnValueEditorRawValueWrapper )
        {
            DnValueEditorRawValueWrapper wrapper = ( DnValueEditorRawValueWrapper ) value;

            // --- Multi-select mode for new (empty) values ---
            if ( wrapper.ivalue != null && wrapper.ivalue.isEmpty() )
            {
                DnDialog dialog = new DnDialog( shell,
                    Messages.getString( "DnValueEditor.DNEditor" ), null, wrapper.connection, new Dn[0] ); //$NON-NLS-1$

                if ( dialog.open() != TextDialog.OK )
                {
                    return false;
                }

                Dn[] selectedDns = dialog.getDns();
                if ( selectedDns.length == 0 )
                {
                    return false;
                }

                if ( selectedDns.length == 1 )
                {
                    // Single selection – use the normal cell-editor path
                    setValue( selectedDns[0].getName() );
                    return true;
                }

                // Multiple selections: commit everything directly so that each DN
                // becomes its own attribute value.
                IValue original = wrapper.ivalue;
                IAttribute attribute = original.getAttribute();
                IEntry entry = attribute.getEntry();
                String attrDesc = attribute.getDescription();

                CompoundModification modification = new CompoundModification();
                // Replace the empty placeholder with the first selected DN.
                modification.modifyValue( original, selectedDns[0].getName() );
                // Append the remaining DNs as additional values.
                for ( int i = 1; i < selectedDns.length; i++ )
                {
                    modification.createValue( entry, attrDesc, selectedDns[i].getName() );
                }
                // We have already committed the changes; tell the cell editor to cancel
                // so it does not try to set a value a second time.
                return false;
            }

            // --- Single-select mode for existing values (original behaviour) ---
            Dn dn;
            try
            {
                dn = wrapper.dn != null ? new Dn( wrapper.dn ) : null;
            }
            catch ( LdapInvalidDnException e )
            {
                dn = null;
            }
            DnDialog dialog = new DnDialog( shell,
                Messages.getString( "DnValueEditor.DNEditor" ), null, wrapper.connection, dn ); //$NON-NLS-1$
            if ( dialog.open() == TextDialog.OK && dialog.getDn() != null )
            {
                setValue( dialog.getDn().getName() );
                return true;
            }
        }
        return false;
    }


    // ── C-3PO Prepares a DN Lookup Packet for an Attribute Hierarchy ─────────
    // C-3PO receives a whole attribute hierarchy (multiple values, or an empty
    // attribute) and decides which DN to pre-load in the dialog.  An attribute
    // with no values gets a null DN (blank picker); one with exactly one value
    // gets that DN pre-selected.  Everything else returns null (not editable here).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link DnValueEditorRawValueWrapper} encoding the browsing
     * connection and current DN string for the given attribute hierarchy.
     * This is used when the editor is invoked from the attribute-hierarchy context
     * (e.g. the table's read-only display path).
     * Returns {@code null} if the hierarchy has zero attributes, or more than one
     * value (we only handle single-value DN attributes here).
     *
     * <p>For example — C-3PO builds a lookup packet:</p>
     * <pre>
     *   Object raw = editor.getRawValue(hierarchy);
     *   // raw is a DnValueEditorRawValueWrapper with the current DN pre-loaded
     * </pre>
     *
     * @param attributeHierarchy  The attribute hierarchy from the browser model.
     * @return                    A wrapper with connection and DN, or {@code null}.
     */
    @Override
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        if ( attributeHierarchy == null )
        {
            return null;
        }
        else if ( attributeHierarchy.size() == 1 && attributeHierarchy.getAttribute().getValueSize() == 0 )
        {
            IBrowserConnection connection = attributeHierarchy.getAttribute().getEntry().getBrowserConnection();
            return new DnValueEditorRawValueWrapper( connection, null );
        }
        else if ( attributeHierarchy.size() == 1 && attributeHierarchy.getAttribute().getValueSize() == 1 )
        {
            IBrowserConnection connection = attributeHierarchy.getAttribute().getEntry().getBrowserConnection();
            return new DnValueEditorRawValueWrapper( connection, getDisplayValue( attributeHierarchy ) );
        }
        else
        {
            return null;
        }
    }


    // ── C-3PO Prepares a DN Lookup Packet for a Single Value ─────────────────
    // C-3PO is handed an individual attribute value (the cell-editor path) and
    // wraps it in a packet containing the LDAP connection and the DN string plus
    // the original IValue object, so the dialog knows whether it's editing a
    // fresh empty slot or an existing DN.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link DnValueEditorRawValueWrapper} for the given single
     * attribute value.  The wrapper carries the connection (needed for the DN
     * browser), the current DN string, and the original {@link IValue} reference
     * so {@link #openDialog} can detect whether the value is an empty placeholder
     * and switch to multi-select mode.
     *
     * <p>For example — C-3PO wraps a member value for editing:</p>
     * <pre>
     *   Object raw = editor.getRawValue(memberValue);
     *   // raw is a DnValueEditorRawValueWrapper with ivalue set
     * </pre>
     *
     * @param value  The LDAP attribute value to wrap.
     * @return       A {@code DnValueEditorRawValueWrapper}, or {@code null} if
     *               the value is not a string-type DN.
     */
    @Override
    public Object getRawValue( IValue value )
    {
        Object o = super.getRawValue( value );
        if ( o instanceof String )
        {
            IBrowserConnection connection = value.getAttribute().getEntry().getBrowserConnection();
            return new DnValueEditorRawValueWrapper( connection, ( String ) o, value );
        }

        return null;
    }

    // ── CLASS: DnValueEditorRawValueWrapper — C-3PO'S MISSION BRIEFING PACKET ─
    // Before C-3PO opens the DN picker dialog he assembles a briefing packet:
    // the LDAP connection (so the picker can browse the live directory), the
    // current DN string (to pre-select in the browser), and a reference to the
    // original IValue (to distinguish new empty slots from existing values).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Internal transfer object that carries contextual data into the
     * {@link DnDialog}: the browser connection, the current DN string, and
     * (optionally) the original {@link IValue} being edited.
     * The {@link IValue} is set only when the wrapper comes from the cell-editor
     * path ({@link DnValueEditor#getRawValue(IValue)}); it is {@code null} when
     * the wrapper comes from the display path
     * ({@link DnValueEditor#getRawValue(AttributeHierarchy)}).
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class DnValueEditorRawValueWrapper
    {
        /** The connection, used in DnDialog to browse for an entry */
        private IBrowserConnection connection;

        /** The Dn, used as initial value in DnDialog */
        private String dn;

        /**
         * The IValue being edited.  Set when the wrapper originates from the
         * cell-editor path ({@link #getRawValue(IValue)}); null when it comes
         * from the read-only display path ({@link #getRawValue(AttributeHierarchy)}).
         */
        private IValue ivalue;


        // ── C-3PO Assembles the Display-Path Briefing Packet ─────────────────
        // When showing a DN in the table (not editing), C-3PO only needs the
        // connection and the current DN string — no IValue reference needed.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Creates a wrapper for the display path (no original IValue needed).
         * Used by {@link DnValueEditor#getRawValue(AttributeHierarchy)}.
         *
         * @param connection  The LDAP browser connection for the directory browse.
         * @param dn          The current DN string to pre-select, or {@code null}.
         */
        private DnValueEditorRawValueWrapper( IBrowserConnection connection, String dn )
        {
            this( connection, dn, null );
        }


        // ── C-3PO Assembles the Full Cell-Editor Briefing Packet ─────────────
        // When the user actually edits a DN cell, C-3PO includes the original
        // IValue so the dialog can tell whether this is a fresh empty slot.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Creates a wrapper for the cell-editor path (includes the original IValue).
         * Used by {@link DnValueEditor#getRawValue(IValue)}.
         *
         * @param connection  The LDAP browser connection for the directory browse.
         * @param dn          The current DN string to pre-select, or {@code null}.
         * @param ivalue      The original attribute value being edited.
         */
        private DnValueEditorRawValueWrapper( IBrowserConnection connection, String dn, IValue ivalue )
        {
            this.connection = connection;
            this.dn = dn;
            this.ivalue = ivalue;
        }


        // ── C-3PO Reads the DN String From His Notes ─────────────────────────
        // C-3PO needs to hand the DN string to the JFace cell editor as a plain
        // String (toString is what JFace calls).  An absent DN renders as empty.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Returns the DN string so that JFace's cell editor can use this wrapper
         * directly as a string value.  Returns an empty string if the DN is null.
         *
         * @return  The DN string, or {@code ""} if {@code dn} is {@code null}.
         */
        @Override
        public String toString()
        {
            return dn == null ? "" : dn; //$NON-NLS-1$
        }

    }

}
