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


// ── CLASS: RenameProjectDialog — MACE WINDU CONFRONTS PALPATINE ───────────────
// Mace Windu strides into Palpatine's private office and demands that the Chancellor
// account for himself. Before Palpatine can assume the title "Emperor" — a new name
// for the same villain — Mace validates whether that identity is already taken by
// someone in the Republic's registry. If it is, Mace blocks the rebranding cold.
// We do the same for schema editor projects: the user types a new project name,
// we check whether that name is already claimed by another project in the projects
// handler, and we block the rename if it conflicts.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Dialog for renaming a Schema Editor project.
 * It inherits the full validation UI from {@link AbstractRenameDialog} — text field,
 * real-time error strip, OK/Cancel buttons — and plugs in the project-specific name
 * uniqueness check and error message.
 * Think of this class as Mace Windu: it confronts every proposed new name with the
 * question "is this name already taken in the projects handler?" and blocks the rename
 * if the answer is yes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameProjectDialog extends AbstractRenameDialog
{
    // ── Mace Arrives Knowing the Accused's Current Identity ───────────────────
    // Mace walks in knowing exactly who he's confronting — the project's current name.
    // That original name is the baseline; if the user proposes the same name back,
    // it's a no-op and we don't block them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the rename dialog, pre-filling the text field with the project's current name.
     * The parent snapshots this name as the baseline so it can permit the user to type
     * the same name back without triggering the "already taken" error.
     *
     * @param originalName  the current name of the project; shown pre-filled in the text field
     */
    public RenameProjectDialog( String originalName )
    {
        super( originalName );

    }


    // ── Mace Labels the Hearing "Rename Project" ──────────────────────────────
    // The door to Palpatine's office is relabelled "Rename Project Hearing" so
    // everyone who passes knows this is specifically about a project, not a schema.
    // We set the shell title accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the shell title to read the localised "Rename" label for projects.
     * We call {@code super.configureShell(newShell)} first so the parent's setup runs,
     * then overwrite with the project-rename-specific string.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "RenameProjectDialog.Rename" ) ); //$NON-NLS-1$
    }


    // ── Mace Reads the Charge Sheet ───────────────────────────────────────────
    // The formal charge sheet reads: "A project with that name already exists in
    // this workspace." Mace hands it to the bailiff (the parent's error strip) so
    // it can be posted for all to see.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised error message displayed when the proposed new project name
     * is already claimed by another project in the projects handler.
     *
     * @return  a non-null localised string describing the project name conflict
     */
    @Override
    protected String getErrorMessage()
    {
        return Messages.getString( "RenameProjectDialog.NameExists" ); //$NON-NLS-1$
    }


    // ── Mace Checks the Projects Registry ────────────────────────────────────
    // Mace asks the Senate's projects registry: "Is the proposed name already
    // registered to another project?" If yes, the rename is blocked. Simple, fast,
    // no appeals process.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the name currently in the text field is already claimed by another
     * project in the workspace.
     * We delegate to the projects handler's name-existence check. If it returns true,
     * the parent disables the OK button and shows the error strip.
     *
     * @return  {@code true} if another project already uses that name, {@code false} if it's free
     */
    @Override
    protected boolean isNewNameAlreadyTaken()
    {
        return Activator.getDefault().getProjectsHandler().isProjectNameAlreadyTaken( getNewName() );
    }
}
