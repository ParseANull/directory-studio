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


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: ValueEditorPreferencesAction — C-3PO ADJUSTS HIS PROTOCOLS ────────
// In various moments across the saga, C-3PO retreats to a maintenance bay and
// has his translation protocols tuned: which languages to prioritize, how to
// handle ambiguous phrases, which dialects to suppress.  It's the settings menu
// for a highly configurable interpretation system.  That's exactly what this
// action does: it opens the Value Editors preference page, where users configure
// how different LDAP attribute types are displayed and edited — which specialized
// editor handles binary data, which handles timestamps, which handles images.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Value Editors preference page in the Eclipse preferences dialog.
 *
 * <p>Value editors are the specialized UI controls that know how to display and
 * edit specific types of LDAP attribute values — dates, images, passwords,
 * binary data, and so on.  Users can configure which editor handles which
 * attribute type from this preference page.</p>
 *
 * <p>Think of this class as C-3PO's protocol maintenance session: we open the
 * settings panel that governs how values are interpreted and rendered, let the
 * user adjust the configuration, and close cleanly when done.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueEditorPreferencesAction extends Action
{
    // ── C-3PO Enters the Maintenance Bay ─────────────────────────────────────
    // C-3PO walks in, identifies himself ("Preferences" is the label on his
    // access panel), and stands ready — always enabled, because the settings
    // panel is always accessible.
    // We set the text and tooltip from the message bundle, and enable the
    // action unconditionally.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code ValueEditorPreferencesAction}.  Sets the action
     * label and tooltip from the message bundle, and enables it immediately —
     * the preference page is always accessible regardless of context.
     */
    public ValueEditorPreferencesAction()
    {
        super.setText( Messages.getString( "ValueEditorPreferencesAction.Preferences" ) ); //$NON-NLS-1$
        super.setToolTipText( Messages.getString( "ValueEditorPreferencesAction.Preferences" ) ); //$NON-NLS-1$
        super.setEnabled( true );
    }


    // ── C-3PO Opens His Protocol Configuration Panel ──────────────────────────
    // The technician activates the maintenance interface — a dialog opens showing
    // all the translation protocol settings.  C-3PO (or the user) can adjust
    // which protocol handles which language, then close and walk away updated.
    // We grab the active shell and open Eclipse's preferences dialog pointed
    // at the Value Editors page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: finds the currently active shell and opens Eclipse's
     * preferences dialog, pre-navigated to the Value Editors preference page.
     *
     * <p>For example — C-3PO adjusts his translation protocols:</p>
     * <pre>
     *   Technician activates the panel (PreferencesUtil.createPreferenceDialogOn).
     *   C-3PO's settings screen opens on the "Value Editors" tab.
     *   Adjustments are made; dialog closes; new protocols are active.
     * </pre>
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String pageId = BrowserCommonConstants.PREFERENCEPAGEID_VALUEEDITORS;
        PreferencesUtil.createPreferenceDialogOn( shell, pageId, new String[]
            { pageId }, null ).open();
    }
}
