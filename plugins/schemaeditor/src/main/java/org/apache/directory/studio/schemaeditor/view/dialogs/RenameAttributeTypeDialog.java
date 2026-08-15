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
package org.apache.directory.studio.schemaeditor.view.dialogs;


import java.util.List;

import org.eclipse.swt.widgets.Shell;


// ── CLASS: RenameAttributeTypeDialog — HAN SHOOTING FIRST IN THE CANTINA ─────
// Han Solo shoots Greedo before the bounty hunter can pull his own weapon —
// he handles the conflict fast, on his terms, before it can escalate into something
// worse. Renaming an attribute type is similar: we deal with the naming collision
// up front (via the alias-editing machinery we inherit), and we label the window
// clearly so the user knows they're here specifically to rename an attribute type.
// This class is a thin wrapper around {@link EditAttributeTypeAliasesDialog} that
// just changes the window title to read "Rename Attribute Type."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Dialog for renaming an LDAP attribute type by editing its full alias list.
 * Renaming an attribute type in the LDAP world means changing the names it's known
 * by — the aliases. This dialog inherits the full alias-editing UI from
 * {@link EditAttributeTypeAliasesDialog} and simply overrides the window title to
 * make the user's intent ("rename", not just "edit aliases") crystal clear.
 * Think of this class as Han shooting first: it gets the job done with minimal fuss,
 * delegating all the heavy lifting upward and just labelling the result correctly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameAttributeTypeDialog extends EditAttributeTypeAliasesDialog
{
    // ── Han Takes His Seat at the Right Table ────────────────────────────────
    // Han walks into the cantina knowing exactly who he's dealing with — the same
    // alias list that EditAttributeTypeAliasesDialog already knows how to handle.
    // He just needs to make sure the sign above the table says "Rename" not "Edit."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the rename dialog with the attribute type's current aliases pre-loaded.
     * Everything is delegated up the chain to {@link EditAttributeTypeAliasesDialog},
     * which in turn delegates to {@link AbstractAliasesDialog}.
     *
     * @param aliases  the attribute type's current alias list; may be empty but not null
     */
    public RenameAttributeTypeDialog( List<String> aliases )
    {
        super( aliases );
    }


    // ── Han Marks His Table "Rename Attribute Type" ───────────────────────────
    // Han scrawls his mark on the reserved sign so everyone in the cantina
    // knows this seat is taken by a rename operation — not a generic alias edit.
    // We call super first (so the parent's title logic runs), then overwrite with
    // the more specific "Rename Attribute Type" label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the window title from the parent's "Edit Attribute Type Aliases" to the
     * more specific "Rename Attribute Type" label.
     * We call {@code super.configureShell(newShell)} first to respect the parent's setup,
     * then overwrite the text to reflect this dialog's more specific purpose.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "RenameAttributeTypeDialog.RenameAttributeType" ) ); //$NON-NLS-1$
    }
}
