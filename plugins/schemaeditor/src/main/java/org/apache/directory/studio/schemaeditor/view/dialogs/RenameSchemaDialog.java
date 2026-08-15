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


import org.apache.directory.studio.schemaeditor.Activator;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: RenameSchemaDialog — OBI-WAN SENSING A DISTURBANCE IN THE FORCE ───
// Obi-Wan is aboard the Millennium Falcon when he suddenly stops, grips his head,
// and says "I felt a great disturbance in the Force — as if millions of voices
// suddenly cried out in terror." He's detecting something wrong before anyone else
// can see it — a schema name collision that would break the registry if allowed.
// This dialog inherits the rename UI from {@link AbstractRenameDialog} and provides
// the schema-specific version of the uniqueness check: it queries the schema handler
// to detect whether the proposed schema name is already in use, reporting the disturbance
// before it can cause damage.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Dialog for renaming a schema within a Schema Editor project.
 * It inherits the full validation UI from {@link AbstractRenameDialog} — text field,
 * real-time error strip, OK/Cancel buttons — and provides the schema-specific
 * uniqueness check and error message.
 * Think of this class as Obi-Wan: it senses the disturbance (a duplicate schema name)
 * before the user can commit the rename, and raises the alarm so the conflict can
 * be resolved before any damage is done.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameSchemaDialog extends AbstractRenameDialog
{
    // ── Obi-Wan Boards the Falcon with the Schema's Current Name ─────────────
    // Obi-Wan steps aboard the Millennium Falcon knowing the schema's identity —
    // its current name. That name is the baseline; if the user types it straight
    // back in, there's no conflict and we don't block them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the rename dialog, pre-filling the text field with the schema's current name.
     * The parent snapshots this name as the baseline so the "already taken" check
     * doesn't block the user from keeping the same name.
     *
     * @param originalName  the current name of the schema; shown pre-filled in the text field
     */
    public RenameSchemaDialog( String originalName )
    {
        super( originalName );

    }


    // ── Obi-Wan Labels This Mission "Rename Schema" ───────────────────────────
    // Before the Falcon jumps to hyperspace, Obi-Wan makes sure the mission dossier
    // is labelled correctly: "Rename Schema" — not "Rename Project," not anything
    // else. We set the shell title accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the shell title to read the localised "Rename" label for schemas.
     * We call {@code super.configureShell(newShell)} first so the parent's setup runs,
     * then overwrite with the schema-rename-specific string.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "RenameSchemaDialog.Rename" ) ); //$NON-NLS-1$
    }


    // ── Obi-Wan Voices the Disturbance ───────────────────────────────────────
    // "I felt a great disturbance — a schema name that is already registered."
    // Obi-Wan reports the disturbance in plain terms so the crew knows exactly
    // what went wrong. The error message is schema-specific.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised error message displayed when the proposed new schema name
     * is already claimed by another schema in the current project.
     *
     * @return  a non-null localised string describing the schema name conflict
     */
    @Override
    protected String getErrorMessage()
    {
        return Messages.getString( "RenameSchemaDialog.NameExists" ); //$NON-NLS-1$
    }


    // ── Obi-Wan Checks the Schema Registry ───────────────────────────────────
    // Obi-Wan reaches out through the Force — through the schema handler — to check
    // whether another schema already holds that name. If the Force confirms the
    // disturbance (the name is taken), the parent blocks the rename immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the name currently in the text field is already claimed by another
     * schema in the active schema handler.
     * We delegate to the schema handler's name-existence check. If it returns true,
     * the parent disables the OK button and shows the error strip.
     *
     * @return  {@code true} if another schema already uses that name, {@code false} if it's free
     */
    @Override
    protected boolean isNewNameAlreadyTaken()
    {
        return Activator.getDefault().getSchemaHandler().isSchemaNameAlreadyTaken( getNewName() );
    }
}
