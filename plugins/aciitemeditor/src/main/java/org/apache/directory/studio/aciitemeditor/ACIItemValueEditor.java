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
package org.apache.directory.studio.aciitemeditor;


import org.apache.directory.studio.aciitemeditor.dialogs.ACIItemDialog;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ACIItemValueEditor — THE GRAND MOFF ISSUING SECURITY DIRECTIVES ────
// Grand Moff Tarkin doesn't just read clearance strings out of a file — when a
// security directive needs editing, he calls up the full ISB console and works
// through it interactively before committing the change.
// This class is that console: it acts as the bridge between the LDAP browser's
// attribute table and the full ACI editor dialog, packaging up the raw value plus
// its context before handing it off to the dialog, then writing the result back.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The value-editor plugin point for ACI item attributes.
 * When the user double-clicks an {@code aciItem} attribute in the LDAP browser,
 * this class opens the {@link ACIItemDialog} so they can edit the ACI text in a
 * structured GUI instead of fighting with raw LDAP syntax.
 * Think of it as the Grand Moff's security console: it turns a flat string into
 * an interactive editing session and writes the result back.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemValueEditor extends AbstractDialogStringValueEditor
{
    // ── THE GRAND MOFF OPENS THE SECURITY CONSOLE ────────────────────────────────
    // Tarkin receives a raw clearance string and decides it needs revision. He opens
    // the ISB console, keys in the current directive, and works through it field by field.
    // Only once he presses "Confirm" does the revised directive get written back to the
    // fleet manifest — cancelling the console leaves the original untouched.
    // That is exactly what this method does: open the dialog, and on OK write back the
    // new value; on cancel return false so the browser knows nothing changed.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link ACIItemDialog} for the current attribute value.
     * If the user confirms, we store the updated ACI string and return {@code true}.
     * If they cancel — or the value isn't the right type — we return {@code false}.
     *
     * <p>For example — Tarkin opens the directive console and confirms a change:</p>
     * <pre>
     *   dialog.open() == OK  →  setValue(newAci); return true;
     *   dialog.open() == CANCEL  →  return false;
     * </pre>
     *
     * @param shell  The parent SWT shell for the dialog window.
     * @return {@code true} if the user confirmed a non-empty value; {@code false} otherwise.
     */
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof ACIItemValueWithContext )
        {
            ACIItemValueWithContext context = ( ACIItemValueWithContext ) value;

            ACIItemDialog dialog = new ACIItemDialog( shell, context );

            if ( ( dialog.open() == ACIItemDialog.OK ) && !EMPTY.equals( dialog.getACIItemValue() ) ) //$NON-NLS-1$
            {
                setValue( dialog.getACIItemValue() );

                return true;
            }
        }

        return false;
    }


    // ── TARKIN REVIEWS THE CURRENT FLEET MANIFEST ENTRY ──────────────────────────
    // Before opening the console, Tarkin's aide retrieves the current directive for the
    // target officer. If the roster entry is blank (no prior directive), the aide hands
    // Tarkin an empty form. Either way, the context packet includes the fleet link and
    // the officer's file so the console can run lookups.
    // This method wraps the attribute hierarchy's current value (or an empty string)
    // in an ACIItemValueWithContext so openDialog has everything it needs.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Wraps the first value of the attribute hierarchy in an {@link ACIItemValueWithContext},
     * including the browser connection and entry needed by the dialog.
     * Returns {@code null} if the hierarchy is null, has more than one attribute, or has
     * more than one value — we only handle the single-attribute, single-value case.
     *
     * <p>For example — Tarkin's aide fetches the current directive or an empty form:</p>
     * <pre>
     *   if (valueSize == 0) → new ACIItemValueWithContext(conn, entry, "")
     *   if (valueSize == 1) → new ACIItemValueWithContext(conn, entry, currentValue)
     *   else                → null  (don't open for multi-value attributes)
     * </pre>
     *
     * @param attributeHierarchy  The attribute hierarchy from the LDAP browser selection.
     * @return an {@link ACIItemValueWithContext}, or {@code null} if conditions aren't met.
     */
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        if ( ( attributeHierarchy != null ) && ( attributeHierarchy.size() == 1 ) )
        {
            if ( attributeHierarchy.getAttribute().getValueSize() == 0 )
            {
                IEntry entry = attributeHierarchy.getAttribute().getEntry();
                IBrowserConnection connection = entry.getBrowserConnection();

                return new ACIItemValueWithContext( connection, entry, EMPTY ); //$NON-NLS-1$
            }
            else if ( attributeHierarchy.getAttribute().getValueSize() == 1 )
            {
                IEntry entry = attributeHierarchy.getAttribute().getEntry();
                IBrowserConnection connection = entry.getBrowserConnection();
                String value = getDisplayValue( attributeHierarchy );

                return new ACIItemValueWithContext( connection, entry, value );
            }
        }

        return null;
    }


    // ── TARKIN WRAPS A SINGLE DIRECTIVE FOR EDITING ───────────────────────────────
    // When Tarkin wants to edit one specific existing directive (not browsing a whole
    // attribute), his aide wraps just that one record — entry coordinates plus the
    // directive text — into the context packet before handing it to the console.
    // This method does the same: takes a single IValue and returns a context wrapper.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Wraps a single {@link IValue} in an {@link ACIItemValueWithContext}.
     * Delegates string extraction to the parent class, then attaches the connection
     * and entry from the value's owning attribute.
     *
     * <p>For example — Tarkin wraps one specific officer directive for editing:</p>
     * <pre>
     *   String raw = super.getRawValue(value);          // "{ identificationTag ... }"
     *   return new ACIItemValueWithContext(conn, entry, raw);
     * </pre>
     *
     * @param value  The LDAP attribute value to wrap.
     * @return an {@link ACIItemValueWithContext}, or {@code null} if the value isn't a String.
     */
    public Object getRawValue( IValue value )
    {
        Object object = super.getRawValue( value );

        if ( object instanceof String )
        {
            IEntry entry = value.getAttribute().getEntry();
            IBrowserConnection connection = entry.getBrowserConnection();
            String valueStr = ( String ) object;

            return new ACIItemValueWithContext( connection, entry, valueStr );
        }

        return null;
    }
}
