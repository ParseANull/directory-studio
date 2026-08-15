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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.wizards.EditEntryWizard;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;


// ── CLASS: OpenEntryEditorAction — Clone Trooper Opening the Big Mission ──────
// While most Order 66 troopers deal with individual targets, some missions
// are bigger — they open a full field operation (the EditEntryWizard) rather
// than just firing a single shot (opening a cell editor).
// This action bypasses the inline cell editor entirely and opens the multi-step
// entry editor wizard, giving the user full control over the whole entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the full {@link EditEntryWizard} for the LDAP entry under the cursor,
 * rather than opening an inline cell editor for a single attribute value.
 * The wizard lets the user add, remove, and modify multiple attributes in one
 * multi-step dialog — useful when the single-cell editor isn't sufficient.
 * Think of this as the trooper who escalates from a quick shot to a full field
 * operation when the situation demands it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEntryEditorAction extends AbstractOpenEditorAction
{

    /** The value editor. */
    private IValueEditor valueEditor;


    // ── Trooper Readies for the Big Operation ────────────────────────────────
    // The trooper is briefed: here's the entry-value editor (basically a no-op
    // inline editor), here's the chain of command.  The real work happens in run().
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the action with its collaborators.
     * The {@code valueEditor} here is the entry value editor — it's used to provide
     * the cell editor reference expected by the superclass, even though we override
     * {@link #run()} to open a wizard instead of an inline editor.
     *
     * @param viewer             the JFace TableViewer showing search results
     * @param cursor             tracks the currently selected cell
     * @param valueEditorManager manages all available value editors
     * @param valueEditor        the entry value editor (provides the cell editor reference)
     * @param actionGroup        manages global action handler lifecycle
     */
    public OpenEntryEditorAction( TableViewer viewer, SearchResultEditorCursor cursor,
        ValueEditorManager valueEditorManager, IValueEditor valueEditor, SearchResultEditorActionGroup actionGroup )
    {
        super( viewer, cursor, valueEditorManager, actionGroup );
        super.cellEditor = valueEditor.getCellEditor();
        this.valueEditor = valueEditor;
    }


    // ── Trooper Gets Their Editor Reference ───────────────────────────────────
    // The CO asks which editor is assigned here — we hand over the entry value
    // editor reference so callers can inspect it if needed.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value editor associated with this action.
     *
     * @return the {@link IValueEditor} instance fixed at construction time
     */
    public IValueEditor getValueEditor()
    {
        return valueEditor;
    }


    // ── Trooper Opens the Full Field Operation ────────────────────────────────
    // Instead of a quick shot, this trooper calls in the full squad: the
    // EditEntryWizard dialog opens and blocks until the user finishes.
    // We deactivate global action handlers during the wizard to prevent
    // double-triggers, then re-activate when it closes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link EditEntryWizard} for the entry under the cursor.
     * We get the entry from the first selected search result, open the wizard in a
     * blocking dialog, and restore global action handlers once the wizard closes.
     * If no search result is selected, we do nothing.
     *
     * <p>For example — trooper escalates to the full field operation:</p>
     * <pre>
     *   IEntry entry = getSelectedSearchResults()[0].getEntry();
     *   EditEntryWizard wizard = new EditEntryWizard(entry);
     *   WizardDialog dialog = new WizardDialog(shell, wizard);
     *   dialog.open();  // blocks until user finishes or cancels
     * </pre>
     */
    public void run()
    {
        IEntry entry = getSelectedSearchResults().length > 0 ? getSelectedSearchResults()[0].getEntry() : null;
        if ( entry != null )
        {
            // disable action handlers
            actionGroup.deactivateGlobalActionHandlers();

            EditEntryWizard wizard = new EditEntryWizard( entry );
            WizardDialog dialog = new WizardDialog( getShell(), wizard );
            dialog.setBlockOnOpen( true );
            dialog.create();
            dialog.open();

            // enable action handlers
            actionGroup.activateGlobalActionHandlers();
        }
    }


    // ── Trooper Stands Down After the Operation ───────────────────────────────
    // The field operation is complete — the trooper releases their equipment.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the value editor reference and delegates to the superclass dispose.
     */
    public void dispose()
    {
        valueEditor = null;
        super.dispose();
    }


    // ── Trooper Carries the Field Operation Command ID ────────────────────────
    // This action is wired to the "Edit Record" global command — the big-picture
    // edit operation as opposed to the single-value edit.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the global Eclipse command ID for "Edit Record".
     * This wires the action to the workbench's "edit full entry" keybinding.
     *
     * @return {@link BrowserCommonConstants#ACTION_ID_EDIT_RECORD}
     */
    public String getCommandId()
    {
        return BrowserCommonConstants.ACTION_ID_EDIT_RECORD;
    }


    // ── Trooper's Mission Has No Special Badge ────────────────────────────────
    // The full-entry-edit operation doesn't have a distinct icon — it appears as
    // a plain text menu item.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this action has no icon.
     *
     * @return {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── Trooper Announces the Field Operation ─────────────────────────────────
    // "Edit Entry" — the label is clear and distinct from the single-value edit actions.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized "Edit Entry" label for this action.
     *
     * @return the localized string from the message bundle
     */
    public String getText()
    {
        return Messages.getString( "OpenEntryEditorAction.EditEntry" ); //$NON-NLS-1$
    }


    // ── Full Operation Is Always Available ────────────────────────────────────
    // Unlike the cell-editor actions that require specific preconditions, the
    // entry editor wizard is always available as long as the action group is wired up.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true}.
     * The entry editor wizard can always be opened; it handles its own internal
     * validation when the user steps through its pages.
     *
     * @return {@code true}
     */
    public boolean isEnabled()
    {
        return true;
    }

}
