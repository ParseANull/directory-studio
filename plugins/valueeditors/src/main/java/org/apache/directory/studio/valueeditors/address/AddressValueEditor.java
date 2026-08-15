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

package org.apache.directory.studio.valueeditors.address;


import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AddressValueEditor — C-3PO READS A JAWA POSTAL MANIFEST ALOUD ─────
// In the Mos Eisley bazaar, C-3PO reads the "$"-separated Jawa filing notation
// and announces each street line as a comma-separated summary so Han can skim it
// quickly on the market board.  When Han wants the full detail, C-3PO opens the
// complete multi-line transcript in the briefing room (AddressDialog).
// This editor does the same: it shows a compact comma-separated summary in the
// LDAP attribute table, and opens AddressDialog when the user double-clicks to edit.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for LDAP Postal Address syntax (OID 1.3.6.1.4.1.1466.115.121.1.41).
 * LDAP stores postal addresses with {@code $} as a line separator; this editor
 * renders them with commas for the summary view and opens {@link AddressDialog}
 * for full multi-line editing.
 * Used by the LDAP browser whenever it encounters a {@code postalAddress} or
 * similar attribute.
 * Think of this as C-3PO's quick-summary mode: he condenses the multi-line manifest
 * into a tidy one-liner for the table, and expands it on request.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AddressValueEditor extends AbstractDialogStringValueEditor
{
    /** The postal address decoder. */
    private CharSequenceTranslator decoder = Utils.createPostalAddressDecoder( ", " ); //$NON-NLS-1$


    // ── C-3PO Opens the Briefing Room Transcript ──────────────────────────────
    // Han wants to review the full cargo address in detail, so C-3PO opens the
    // briefing room and spreads the complete multi-line manifest on the table.
    // If Han confirms the content, C-3PO files the updated address; otherwise
    // the original is unchanged.
    // We open AddressDialog and, if the user clicked OK with a non-empty result,
    // we store the new LDAP-encoded value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link AddressDialog} so the user can edit the postal address
     * in full multi-line form.
     * Returns {@code true} if the user clicked OK and provided a non-empty
     * address (meaning the underlying value was updated); {@code false} if the
     * user cancelled or left the field blank.
     *
     * <p>For example — C-3PO opens the address briefing:</p>
     * <pre>
     *   // User double-clicks the postalAddress attribute cell
     *   boolean changed = editor.openDialog(shell);
     *   // If true, the new LDAP value is ready in editor.getValue()
     * </pre>
     *
     * @param shell  The parent SWT shell for the AddressDialog.
     * @return       {@code true} if the value was updated; {@code false} otherwise.
     */
    @Override
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof String )
        {
            AddressDialog dialog = new AddressDialog( shell, ( String ) value );

            if ( ( dialog.open() == AddressDialog.OK ) && !EMPTY.equals( dialog.getAddress() ) ) //$NON-NLS-1$
            {
                setValue( dialog.getAddress() );

                return true;
            }
        }

        return false;
    }


    // ── C-3PO Reads the Manifest Aloud in Comma Format ───────────────────────
    // Han asks for a quick summary of the cargo address, so C-3PO reads each
    // "$"-separated line aloud separated by commas: "123 Main St, Anytown, CA 90210".
    // In raw-values mode he recites the literal LDAP string with the "$" markers intact.
    // We translate the "$" separators to ", " for the display string, unless the
    // user has opted to see raw LDAP values.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a display-friendly version of the postal address for the attribute
     * table cell.
     * Normally we replace the LDAP {@code $} line-separators with {@code ", "} so
     * the address reads naturally in a single table row.  When the user has enabled
     * "show raw values", we return the string as-is (with {@code $} intact) so
     * they can see exactly what's stored in the directory.
     *
     * <p>For example — C-3PO's quick-summary translation:</p>
     * <pre>
     *   LDAP stored:  "123 Main St$Anytown$CA 90210"
     *   Displayed as: "123 Main St, Anytown, CA 90210"
     * </pre>
     *
     * @param value  The LDAP attribute value to display.
     * @return       A human-readable string suitable for the table cell.
     */
    @Override
    public String getDisplayValue( IValue value )
    {
        String displayValue = super.getDisplayValue( value );

        if ( !showRawValues() )
        {
            displayValue = decoder.translate( displayValue );
        }

        return displayValue;
    }
}
