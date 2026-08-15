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

package org.apache.directory.studio.ldapbrowser.common.actions;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.action.Action;


// ── CLASS: ShowDecoratedValuesAction — YODA REVEALS A PLANET'S TRUE SIZE ─────
// In The Empire Strikes Back, during Luke's training on Dagobah, Yoda explains
// that the Force reveals the true nature of things — not just the surface, but
// what's underneath.  "Luminous beings are we, not this crude matter," he says.
// Decorated values are exactly that: LDAP stores raw bytes, but "decorated"
// display means we use specialized value editors to show those bytes as something
// meaningful — a human-readable date instead of epoch seconds, a pretty image
// instead of raw binary.  This toggle action switches between showing the
// decorated (Yoda's Force-revealed) view and the raw bytes (crude matter).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that switches between decorated and raw value display in the
 * LDAP entry editor.
 *
 * <p>"Decorated" means the value is rendered by a specialized editor —
 * timestamps shown as readable dates, binary data as images, Boolean values as
 * checkboxes, and so on.  "Raw" means you see the literal bytes as-is.  Most
 * users want decorated; power users sometimes need raw to see exactly what the
 * server holds.</p>
 *
 * <p>Think of this action as Yoda toggling between revealing a thing's true
 * nature (decorated) and showing just the crude matter (raw bytes).</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowDecoratedValuesAction extends Action
{
    // ── Yoda Opens Luke's Eyes to the Force View ──────────────────────────────
    // When Luke first arrives on Dagobah, Yoda immediately begins helping him
    // see past the surface — the swamp is teeming with life, not just mud.
    // We initialize the action as a checkbox, already reflecting whatever the
    // user last chose (from the preference store), so the state is always
    // consistent with reality.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code ShowDecoratedValuesAction} as a checkbox-style
     * toggle.  We read the current preference to set the initial checked state
     * so the menu item accurately reflects the live setting right away.
     *
     * <p>Note: the preference is {@code PREFERENCE_SHOW_RAW_VALUES} — so
     * "decorated" = NOT raw, meaning we invert the boolean when reading it.</p>
     */
    public ShowDecoratedValuesAction()
    {
        super( Messages.getString( "ShowDecoratedValuesAction.ShowDecoratedValues" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( getText() );
        setEnabled( true );

        super.setChecked( !BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES ) );
    }


    // ── Yoda Flips the Perception Between Force-Sight and Normal Sight ────────
    // When Luke toggles his Force-sight on or off, Yoda records the change so
    // it persists — next training session starts wherever Luke left off.
    // We write the inverted value back to the preference store so the editor
    // immediately picks up the new display mode.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the toggle: flips the {@code PREFERENCE_SHOW_RAW_VALUES}
     * preference.  The entry editor listens for preference changes and will
     * re-render values in the new mode without needing a manual refresh.
     *
     * <p>Note the inversion: when this action is <em>checked</em> (decorated
     * values ON), we store {@code false} for "show raw".</p>
     */
    public void run()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES, !super.isChecked() );
    }


    // ── Yoda Records Luke's Current Mode of Perception ───────────────────────
    // Yoda notes in his training log whether Luke is currently using Force-sight
    // or ordinary sight.  The log needs to be updated explicitly when it changes.
    // We override setChecked so callers can keep the action in sync when the
    // preference changes from outside (e.g. from the preferences dialog).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the checked state of this toggle explicitly.  Called by external
     * code that wants to synchronize the action's visual state with a preference
     * change that happened elsewhere (e.g. the user changed the setting through
     * the preferences dialog rather than this menu item).
     *
     * @param checked  {@code true} if decorated values should be shown;
     *                 {@code false} if raw values should be shown
     */
    public void setChecked( boolean checked )
    {
        super.setChecked( checked );
    }


    // ── Yoda Reports Whether Force-Sight Is Currently Active ─────────────────
    // At any moment, Yoda can tell you whether Luke is currently seeing through
    // the Force or through ordinary eyes.
    // We delegate to the parent Action's checked state — a simple query.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this toggle is currently in the "checked" (decorated
     * values ON) state.  Callers use this to know whether to render values
     * with their specialized editors or as raw bytes.
     *
     * @return {@code true} if decorated mode is active; {@code false} if raw mode
     */
    public boolean isChecked()
    {
        return super.isChecked();
    }
}
