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

package org.apache.directory.studio.valueeditors;


import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: TextValueEditor — C-3PO UNFURLS THE PLAIN-TEXT SCROLL ─────────────
// In the Rebellion's command center on Hoth, C-3PO receives a data transmission
// — just plain text, no fancy encoding, nothing binary. He carefully unrolls the
// scroll, reads it aloud, and if anyone wants to make changes he hands it back
// for editing before rolling it up again and filing it. That's this class: the
// default editor for plain multi-line string LDAP attribute values, opening the
// simple TextDialog where the user can read and freely edit the text content.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The default dialog-based editor for plain string LDAP attribute values, especially
 * multi-line ones. When the user double-clicks a text-type attribute value in the
 * Entry Editor, this class pops open a {@link TextDialog} — a simple scrollable
 * text area — where they can read and edit the current string.
 * Think of this class as C-3PO unfurling a plain-text scroll on Hoth: he reads
 * the current content, lets someone make changes, then rolls it back up and files it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TextValueEditor extends AbstractDialogStringValueEditor
{
    // ── C-3PO UNFURLS THE SCROLL AND OFFERS IT FOR EDITING ──────────────────────
    // C-3PO carefully unrolls the data scroll in front of the commander. If it
    // contains readable text, he places it on the table; the commander reads it,
    // makes edits if desired, and hands it back. C-3PO checks whether any changes
    // were actually made before storing the updated content.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link TextDialog} populated with the current string value, waits
     * for the user to confirm or cancel, and stores the new text if they clicked OK
     * and actually typed something. Returns {@code true} when a non-empty new value
     * was accepted so the parent editor knows to commit the change.
     *
     * <p>For example — C-3PO presents the scroll for editing:</p>
     * <pre>
     *   C-3PO: "Commander, the scroll reads: 'Echo Base power generator status: nominal'."
     *   Commander: [edits] "Change that to 'nominal — prepare for evacuation'."
     *   C-3PO: "The scroll has been updated. I am storing the revised text now."
     *   C-3PO: "If the scroll came back blank or unchanged, I would return false."
     * </pre>
     *
     * @param shell  the parent SWT shell used to position the TextDialog on screen
     * @return       {@code true} if the user clicked OK and provided a non-empty string;
     *               {@code false} if they cancelled, cleared the field, or the current
     *               value was not a String to begin with
     */
    @Override
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof String )
        {
            TextDialog dialog = new TextDialog( shell, ( String ) value );

            if ( ( dialog.open() == TextDialog.OK ) && ( dialog.getText() != null )
                && ( dialog.getText().length() != 0 ) )
            {
                setValue( dialog.getText() );

                return true;
            }
        }

        return false;
    }
}
