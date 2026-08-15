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

package org.apache.directory.studio.valueeditors.administrativerole;


import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: AdministrativeRoleValueEditor — PALPATINE DISPATCHING A SECTOR CHIEF
// Palpatine doesn't just announce administrative roles — he also delegates the
// actual selection session to his Senate chamber (AdministrativeRoleDialog).
// When a subtree's role attribute needs updating, this editor summons the
// chamber, records Palpatine's decision, and writes it back to the directory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for the LDAP {@code administrativeRole} attribute.
 * When the user double-clicks the attribute cell, we open
 * {@link AdministrativeRoleDialog} so they can pick from the known role names
 * (or type their own).  On confirmation, the new value is stored back in the
 * LDAP entry.
 * Think of this as Palpatine's dispatch agent — it summons the Senate session
 * and relays the outcome to the directory.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AdministrativeRoleValueEditor extends AbstractDialogStringValueEditor
{

    // ── Palpatine Convenes the Designation Session ────────────────────────────
    // Palpatine's dispatch agent opens the Senate chamber door and invites the
    // appropriate parties to select a new sector role.
    // If the session concludes with a confirmed, non-empty designation, the
    // agent records it in the Imperial register (the LDAP attribute value).
    // We open AdministrativeRoleDialog, and if the user confirms a non-empty
    // role, we update the current value and signal success.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens {@link AdministrativeRoleDialog} for the user to select or type an
     * administrative role.
     * Returns {@code true} if the user clicked OK and provided a non-empty role
     * string (meaning the value was updated); {@code false} if the user cancelled
     * or left the field blank.
     *
     * <p>For example — Palpatine's agent opens the session:</p>
     * <pre>
     *   boolean changed = editor.openDialog(shell);
     *   if (changed) {
     *       // The administrativeRole attribute now holds the new value
     *   }
     * </pre>
     *
     * @param shell  The parent SWT shell for the AdministrativeRoleDialog.
     * @return       {@code true} if a new role was selected; {@code false} otherwise.
     */
    @Override
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof String )
        {
            AdministrativeRoleDialog dialog = new AdministrativeRoleDialog( shell, ( String ) value );

            if ( ( dialog.open() == TextDialog.OK ) && !"".equals( dialog.getAdministrativeRole() ) ) //$NON-NLS-1$
            {
                setValue( dialog.getAdministrativeRole() );

                return true;
            }
        }

        return false;
    }
}
