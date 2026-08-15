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
package org.apache.directory.studio.schemaeditor.view.editors;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;


// ── CLASS: EditorsUtils — HAN SHOOTS FIRST IN THE MOS EISLEY CANTINA ────────────────
// In the Mos Eisley cantina, Han Solo doesn't wait for Greedo to pull the trigger —
// he acts first, cleanly handling the threat before it becomes a real crisis.
// This utility class does the same thing with unsaved editors: before any dangerous
// operation (like replacing the schema), it proactively hunts down every dirty editor,
// presents the list to the user, and forces a save — no data-loss surprises later.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Utility class with static helpers for managing open Eclipse editors.
 * It lives in the editors package because its job is orchestrating editor-level
 * operations — specifically finding and saving dirty editors before a schema change
 * wipes out their unsaved content underneath them.
 * Think of this as Han Solo at the cantina: we act before the problem acts on us.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorsUtils
{
    // ── Han Spots Every Armed Patron Before Drawing ──────────────────────────────────
    // Han surveys the cantina, spots Greedo (and any other trouble), and handles each
    // threat in sequence before they can fire first.  Here, "dirty editors" are the
    // threats — unsaved schema changes that would be silently discarded if we let the
    // operation proceed without asking.
    // We show the user a dialog listing every dirty editor, let them confirm the save,
    // and only then let the operation continue.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves all open editors that have unsaved changes, asking the user first via a
     * dialog that lists which editors are dirty.
     * We need this before schema-level operations (like reloading or removing a schema)
     * because those operations change the model that editors are displaying — if the
     * editor was dirty, those unsaved changes would be quietly lost.
     * If there are no dirty editors, we return true immediately without showing any UI.
     * If the user clicks Cancel in the dialog, we return false and the caller should
     * abort whatever operation they were about to do.
     *
     * <p>For example — Han handles the cantina threat proactively:</p>
     * <pre>
     *   if ( !EditorsUtils.saveAllDirtyEditors() )
     *   {
     *       // User cancelled — abort the schema operation, nothing was saved.
     *       return;
     *   }
     *   // All editors saved — safe to proceed with the destructive operation.
     * </pre>
     *
     * @return  {@code true} if all dirty editors were saved successfully (or there were
     *          none), {@code false} if the user cancelled or a save error occurred
     */
    public static boolean saveAllDirtyEditors()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        List<IEditorPart> dirtyEditorsList = getDirtyEditorsList( workbench );

        if ( dirtyEditorsList.size() > 0 )
        {
            // Creating the dialog to ask the user if the dirty editors must be saved
            ListDialog dialog = new ListDialog( workbenchWindow.getShell() );
            dialog.setTitle( Messages.getString( "EditorsUtils.SaveDialogTitle" ) ); //$NON-NLS-1$
            dialog.setMessage( Messages.getString( "EditorsUtils.SaveDialogMessage" ) ); //$NON-NLS-1$
            dialog.setLabelProvider( new LabelProvider()
            {
                public Image getImage( Object element )
                {
                    return ( ( IEditorPart ) element ).getTitleImage();
                }


                public String getText( Object element )
                {
                    IEditorPart editorPart = ( IEditorPart ) element;

                    StringBuilder sb = new StringBuilder();
                    sb.append( editorPart.getTitle() );

                    String tooltip = editorPart.getTitleToolTip();

                    if ( ( tooltip != null ) && ( !"".equals( tooltip ) ) ) //$NON-NLS-1$
                    {
                        sb.append( " [" ); //$NON-NLS-1$
                        sb.append( tooltip );
                        sb.append( "]" ); //$NON-NLS-1$
                    }

                    return sb.toString();
                }
            } );
            dialog.setContentProvider( new ArrayContentProvider() );
            dialog.setHelpAvailable( false );
            dialog.setAddCancelButton( true );
            dialog.setInput( dirtyEditorsList );

            // Opening the dialog
            if ( dialog.open() != Dialog.OK )
            {
                // Cancel
                return false;
            }

            // Forcing the save of all dirty editors
            return workbenchWindow.getWorkbench().saveAllEditors( false );
        }

        return true;
    }


    // ── Han Catalogues Every Armed Patron in the Room ───────────────────────────────
    // Before Han can deal with trouble, he needs to know who's carrying.  He scans
    // every table in the cantina — every window, every page in those windows, every
    // editor on those pages — and compiles a list of armed patrons (dirty editors).
    // We deduplicate by editor input so the same file opened twice doesn't appear twice.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Collects all open editor parts that have unsaved changes across all workbench
     * windows and pages.
     * We deduplicate by IEditorInput so that if the same schema object is open in two
     * different windows, it only appears once in the returned list — otherwise the save
     * dialog would show duplicate entries and the user would be confused.
     *
     * <p>For example — Han surveys every table in the cantina:</p>
     * <pre>
     *   // window 1, page 1 → [ dirtyEditor A, dirtyEditor B ]
     *   // window 1, page 2 → [ dirtyEditor A (same input as above — skip it) ]
     *   // Result: [ A, B ]  — each dirty editor input counted once
     * </pre>
     *
     * @param workbench  the Eclipse IWorkbench to scan; if null we return an empty list
     *                   rather than throwing
     * @return           a deduplicated list of IEditorPart instances with unsaved changes;
     *                   never null, may be empty
     */
    public static List<IEditorPart> getDirtyEditorsList( IWorkbench workbench )
    {
        List<IEditorPart> dirtyEditorsList = new ArrayList<IEditorPart>();

        if ( workbench != null )
        {
            List<IEditorInput> processedInputs = new ArrayList<IEditorInput>();

            for ( IWorkbenchWindow workbenchWindow : workbench.getWorkbenchWindows() )
            {
                for ( IWorkbenchPage workbenchPage : workbenchWindow.getPages() )
                {
                    for ( IEditorPart editor : workbenchPage.getDirtyEditors() )
                    {
                        IEditorInput input = editor.getEditorInput();

                        if ( !processedInputs.contains( input ) )
                        {
                            processedInputs.add( input );
                            dirtyEditorsList.add( editor );
                        }
                    }
                }
            }
        }

        return dirtyEditorsList;
    }
}
