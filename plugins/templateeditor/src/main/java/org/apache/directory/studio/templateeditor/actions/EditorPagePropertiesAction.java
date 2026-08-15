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
package org.apache.directory.studio.templateeditor.actions;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.common.actions.PropertiesAction;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: EditorPagePropertiesAction — CLONE TROOPER OPENING THE DOSSIER ────────
// When a clone trooper receives orders to investigate a specific target, he opens
// the target's official dossier — the Properties Dialog. He only does this when
// no specific value is already selected (otherwise a different dossier would open
// for the value, not the entry). This action extends the standard PropertiesAction
// and overrides which entries to show properties for: always the entry currently
// open in the template editor, never the sub-values.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Properties Dialog for the LDAP entry currently shown in a
 * template-based entry editor. Extends {@link PropertiesAction} from the browser
 * common layer and overrides {@link #getSelectedEntries()} so it always returns
 * the editor's active entry (rather than whatever is selected in a tree view).
 * The properties dialog is only opened for the entry itself — if the user has a
 * value selected the returned array is empty and the dialog doesn't open.
 * Think of this as a clone trooper opening the official dossier for the target.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorPagePropertiesAction extends PropertiesAction
{
    /** The associated editor */
    private IEntryEditor editor;


    // ── CONSTRUCTOR: TROOPER ASSIGNED TO THE EDITOR ───────────────────────────────
    // The trooper is assigned to guard this particular editor — he knows whose
    // dossier to open when the order comes in. We keep a reference to the editor
    // so we can pull the active entry from it at run time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action bound to the given entry editor. The editor reference
     * is used in {@link #getSelectedEntries()} to resolve the current entry.
     *
     * <p>For example — the trooper is assigned to the editor:</p>
     * <pre>
     *   new EditorPagePropertiesAction(templateEntryEditor);
     *   // "You're on dossier duty for this editor, trooper."
     * </pre>
     *
     * @param editor  the template entry editor whose current entry's properties
     *                this action will open
     */
    public EditorPagePropertiesAction( IEntryEditor editor )
    {
        super();
        this.editor = editor;
    }


    // ── GET SELECTED ENTRIES: TROOPER IDENTIFIES THE INVESTIGATION TARGET ─────────
    // The trooper checks: "Is a specific value already under investigation?" If so,
    // the standard behavior handles it. If not, the trooper looks at the editor
    // to identify the main target — the LDAP entry currently open — and hands
    // back a one-element array containing that entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP entry currently open in the associated editor, but only
     * when no attribute values are selected. If values are selected the returned
     * array is empty and the parent class opens the value-properties dialog instead
     * of the entry-properties dialog.
     *
     * <p>For example — the trooper identifies the main target:</p>
     * <pre>
     *   // No value selected → return [currentEntry]  (open entry properties)
     *   // Value selected    → return []              (let parent handle value properties)
     * </pre>
     *
     * @return a single-element array with the current entry, or an empty array
     *         if values are selected or the editor has no resolved entry
     */
    public IEntry[] getSelectedEntries()
    {
        // We're only returning the entry when no value is selected
        if ( getSelectedValues().length == 0 )
        {
            if ( editor != null )
            {
                EntryEditorInput input = editor.getEntryEditorInput();

                IEntry entry = ( ( EntryEditorInput ) input ).getResolvedEntry();
                if ( entry != null )
                {
                    return new IEntry[]
                        { entry };
                }
            }
        }

        return new IEntry[0];
    }
}
