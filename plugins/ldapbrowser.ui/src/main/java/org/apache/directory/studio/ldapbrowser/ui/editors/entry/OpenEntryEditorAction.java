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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.AbstractOpenEditorAction;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetActionGroup;
import org.apache.directory.studio.ldapbrowser.common.wizards.EditEntryWizard;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;


// ── CLASS: OpenEntryEditorAction — CLONE TROOPER EXECUTES THE FIELD MISSION ───
// A clone trooper receives a specific mission target — not a generic "patrol"
// order, but a precise "capture this specific target now" directive.
// Trooper FN-2187 marches straight to the target location, disables the local
// defenses (deactivates action handlers), executes the operation (opens the wizard),
// and re-enables the base (reactivates handlers) when done.
// OpenEntryEditorAction is that trooper: it opens the EditEntryWizard dialog
// for the entry currently selected in the attribute table.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An action that opens the {@link EditEntryWizard} for the entry currently selected in the attribute-table viewer.
 * The wizard gives users a full multi-page editing experience for an LDAP entry,
 * as opposed to inline cell editing in the table.
 * Think of this as the clone trooper with a specific assignment: he gets the target (the IEntry),
 * opens the operation (the wizard), and handles clean-up when it's done.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEntryEditorAction extends AbstractOpenEditorAction
{
    /** The value editor. */
    private IValueEditor valueEditor;


    // ── THE TROOPER RECEIVES HIS ASSIGNMENT AND EQUIPMENT ────────────────────
    // The trooper is handed his assignment dossier — which viewer to watch,
    // which value editor to use, who to report to — and takes his position.
    // We store the value editor and delegate the rest to the superclass
    // which hooks us up to the viewer's cell editor infrastructure.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action, linking it to the viewer, value editor manager, and action group.
     * We configure the action's cell editor from the given value editor so the superclass
     * infrastructure can open it via the standard cell-editor activation pathway.
     *
     * @param viewer             The tree viewer this action operates on.
     * @param valueEditorManager The manager that resolves which value editor to use for each value.
     * @param valueEditor        The specific entry value editor (the "entry editor" variant).
     * @param actionGroup        The action group — used to deactivate/reactivate global handlers
     *                           around the wizard dialog so shortcuts don't conflict.
     */
    public OpenEntryEditorAction( TreeViewer viewer, ValueEditorManager valueEditorManager, IValueEditor valueEditor,
        EntryEditorWidgetActionGroup actionGroup )
    {
        super( viewer, valueEditorManager, actionGroup );
        setCellEditor( valueEditor.getCellEditor() );
        this.valueEditor = valueEditor;
    }


    // ── THE TROOPER RETRIEVES HIS ASSIGNED WEAPON TYPE ───────────────────────
    // Command asks which weapon class this trooper is carrying so they can
    // log it in the deployment manifest.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value editor this action is associated with.
     * Used by the action group to identify which editor this action wraps.
     *
     * @return the {@link IValueEditor} that provides the cell editor and edit logic.
     */
    public IValueEditor getValueEditor()
    {
        return valueEditor;
    }


    // ── THE TROOPER EXECUTES THE MISSION: OPEN THE WIZARD ────────────────────
    // The trooper gets the coordinates of the target, disables the perimeter
    // defenses, walks in and does the job, then brings the defenses back up
    // before reporting mission complete.
    // We resolve the target entry, deactivate global handlers while the wizard
    // is open (to avoid double-firing), open the wizard, and reactivate on close.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link EditEntryWizard} for the entry selected in the viewer.
     * We resolve the target entry from the selection (falling back through values →
     * attributes → the whole entry input). We deactivate global action handlers during
     * the wizard to prevent keyboard shortcuts from firing into the background editor,
     * then reactivate them when the wizard closes.
     */
    public void run()
    {
        IEntry entry = getSelectedValues().length > 0 ? getSelectedValues()[0].getAttribute().getEntry()
            : getSelectedAttributes().length > 0 ? getSelectedAttributes()[0].getEntry()
                : ( getInput() instanceof IEntry ) ? ( IEntry ) getInput() : null;
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


    // ── THE TROOPER STANDS DOWN AND RETURNS EQUIPMENT ────────────────────────
    // The mission is over — the trooper holsters his weapon, logs the completion,
    // and releases any borrowed equipment back to the armory.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases resources held by this action.
     * We null the value editor reference and delegate to the superclass to dispose
     * the cell editor and unhook from the viewer.
     */
    public void dispose()
    {
        valueEditor = null;
        super.dispose();
    }


    // ── THE TROOPER QUOTES HIS STANDING ORDER CODE ────────────────────────────
    // The command center asks for the standing order code under which this
    // trooper is operating — it maps to the Eclipse command ID for keybindings.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action's keybinding.
     * Eclipse uses this to wire keyboard shortcuts defined in the plugin's plugin.xml
     * to this action when it's registered as a global handler.
     *
     * @return the command ID constant for "Edit Record".
     */
    public String getCommandId()
    {
        return BrowserCommonConstants.ACTION_ID_EDIT_RECORD;
    }


    // ── THE TROOPER WEARS THE RIGHT INSIGNIA ──────────────────────────────────
    // The trooper's shoulder patch shows his unit — we put the right icon
    // on this action's toolbar button and menu item.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action — the entry editor icon.
     *
     * @return the image descriptor for the "entry editor" icon.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_ENTRY_EDITOR );
    }


    // ── THE TROOPER ANNOUNCES THE MISSION NAME ────────────────────────────────
    // The trooper identifies himself by mission name when asked by a colleague.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for this action's menu item or tooltip.
     *
     * @return the localized "Edit Entry" label string.
     */
    public String getText()
    {
        return Messages.getString( "OpenEntryEditorAction.EditEntry" ); //$NON-NLS-1$
    }


    // ── THE TROOPER IS ALWAYS READY TO EXECUTE ────────────────────────────────
    // Unlike actions that disable based on selection, this trooper is always
    // standing by — the entry resolution happens inside run() rather than here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true} — this action is always enabled.
     * We don't pre-check selection here; the {@link #run()} method handles
     * the case where no valid entry can be resolved from the selection.
     *
     * @return always {@code true}.
     */
    public boolean isEnabled()
    {
        return true;
    }

}
