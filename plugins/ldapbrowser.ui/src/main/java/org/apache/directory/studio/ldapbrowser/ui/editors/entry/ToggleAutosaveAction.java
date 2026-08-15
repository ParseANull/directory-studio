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
import org.eclipse.jface.action.Action;


// ── CLASS: ToggleAutosaveAction — HAN SHOOTS FIRST ────────────────────────────
// In the Mos Eisley cantina, Han Solo doesn't wait for Greedo to fire — he makes
// the call himself, immediately, before the situation can get worse. That's the
// philosophy of auto-save: don't wait for the user to explicitly hit Save, just
// commit the change to the LDAP server the moment the user makes it.
// ToggleAutosaveAction is the switch between "Han shoots first" (auto-save on —
// every edit fires immediately) and "wait and see" (auto-save off — edits accumulate
// in the working copy until the user explicitly saves).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A checkbox action that toggles the auto-save preference for the current entry editor.
 * When auto-save is on, every edit to an attribute value is immediately committed to
 * the LDAP server — no "Save" button needed. When off, changes accumulate in a local
 * working copy and the user must explicitly save.
 * The correct preference constant is chosen based on whether the editor is a single-tab
 * or multi-tab editor (they have separate settings).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ToggleAutosaveAction extends Action
{

    private EntryEditor entryEditor;


    // ── HAN DECIDES WHETHER TO HOLSTER OR DRAW ────────────────────────────────
    // Han enters the cantina and immediately decides his stance — is he going to
    // react on instinct (auto-save on) or wait and think (auto-save off)?
    // We create the checkbox action with the right label, preset to "always enabled"
    // since toggling is always valid regardless of what entry is shown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the toggle action as a checkbox ({@link Action#AS_CHECK_BOX}).
     * The checked state drives the auto-save preference; we don't set it here because
     * {@link #updateSetChecked()} will be called right before the menu opens to sync
     * the checkbox with the current preference value.
     *
     * @param entryEditor  The entry editor whose auto-save preference we're toggling.
     */
    public ToggleAutosaveAction( EntryEditor entryEditor )
    {
        super( Messages.getString( "ToggleAutosaveAction.Autosave" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( getText() );
        setEnabled( true );
        this.entryEditor = entryEditor;
    }


    // ── HAN FIRES (OR HOLSTERS) — THE TOGGLE FIRES ───────────────────────────
    // Han makes his call: "Auto-save is now on/off" — and the preference store
    // records the new standing order for all future edits in this editor type.
    // We write the new boolean value to the right preference key
    // (single-tab vs. multi-tab) so the setting persists across sessions.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires the toggle — persists the new auto-save state to the preference store.
     * Eclipse calls this when the user clicks the checkbox menu item; the checkbox
     * state ({@link #isChecked()}) already reflects the new desired state, so we
     * just write it to the preference store under the correct key.
     */
    @Override
    public void run()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().setValue( getConstant(), super.isChecked() );
    }


    // ── HAN CHECKS WHICH HOLSTER HE'S USING ───────────────────────────────────
    // Han has two holsters: one for quick-draw (single-tab), one for deliberate
    // fire (multi-tab). Which preference key applies depends on the editor type.
    // We ask the editor's extension whether it's a multi-window editor to pick
    // the right preference constant.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the preference store constant for the current editor's auto-save setting.
     * Single-tab and multi-tab editors have separate auto-save preferences so users
     * can configure them independently. We check the editor extension to know which one applies.
     *
     * @return the preference key string for either the single-tab or multi-tab auto-save setting.
     */
    private String getConstant()
    {
        boolean multiTab = entryEditor.getEntryEditorInput().getExtension().isMultiWindow();
        if ( multiTab )
        {
            return BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_MULTI_TAB;
        }
        else
        {
            return BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_SINGLE_TAB;
        }
    }


    // ── HAN CHECKS THE CURRENT STATUS OF HIS DRAW ────────────────────────────
    // Right before entering a cantina, Han checks: "Is my safety on or off right now?"
    // We read the current preference value and sync the checkbox to match — so the
    // menu item always reflects what's actually configured, not a stale remembered state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Syncs the checkbox state with the current preference store value.
     * Called by {@link EntryEditorActionGroup#fillMenu} right before the menu opens
     * so the checkbox reflects the real current setting, not whatever state it was
     * last set to (which could be stale if the preference changed via another path).
     */
    public void updateSetChecked()
    {
        setChecked( BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean( getConstant() ) );
    }

}
