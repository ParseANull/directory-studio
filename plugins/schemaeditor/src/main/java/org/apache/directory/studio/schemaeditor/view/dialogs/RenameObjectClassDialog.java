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


// ── CLASS: RenameObjectClassDialog — LANDO SHOOTS FIRST AT CLOUD CITY ────────
// Lando Calrissian, facing down an Imperial officer who's threatening to renegotiate
// the deal, cuts the conversation short and takes control of the situation on his
// own terms. He doesn't let the conflict drag out — he acts decisively and labels
// the outcome clearly: "Cloud City is under new management."
// This class is the object-class counterpart to {@link RenameAttributeTypeDialog}:
// it wraps {@link EditObjectClassAliasesDialog} and simply overrides the window title
// to say "Rename Object Class," making the user's intent unambiguous.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Dialog for renaming an LDAP object class by editing its full alias list.
 * This class inherits the complete alias-editing UI from {@link EditObjectClassAliasesDialog}
 * and overrides only the window title to reflect the "rename" intent rather than the
 * more generic "edit aliases" framing.
 * Think of this class as Lando taking charge: minimal new code, maximum clarity about
 * what's happening — the window says "Rename Object Class," full stop.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameObjectClassDialog extends EditObjectClassAliasesDialog
{
    // ── Lando Steps Up to Manage the Situation ───────────────────────────────
    // Lando steps into the room with the same ledger of current object-class aliases
    // that EditObjectClassAliasesDialog already knows how to handle. He just needs
    // to make sure the conference room door says "Rename Object Class."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the rename dialog with the object class's current aliases pre-loaded.
     * Everything is delegated up the chain to {@link EditObjectClassAliasesDialog},
     * which in turn delegates to {@link AbstractAliasesDialog}.
     *
     * @param aliases  the object class's current alias list; may be empty but not null
     */
    public RenameObjectClassDialog( List<String> aliases )
    {
        super( aliases );
    }


    // ── Lando Relabels the Conference Room Door ───────────────────────────────
    // Lando strides to the door, removes the old sign ("Edit Object Class Aliases"),
    // and replaces it with "Rename Object Class." The room setup is identical — only
    // the label changes to reflect who's in charge now and what this meeting is for.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the window title from the parent's "Edit Object Class Aliases" to the
     * more specific "Rename Object Class" label.
     * We call {@code super.configureShell(newShell)} first to respect the parent's setup,
     * then overwrite the text to reflect this dialog's more specific purpose.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "RenameObjectClassDialog.RenameObjectClass" ) ); //$NON-NLS-1$
    }
}
