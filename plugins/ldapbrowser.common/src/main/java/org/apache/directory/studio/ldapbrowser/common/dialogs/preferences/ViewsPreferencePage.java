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

package org.apache.directory.studio.ldapbrowser.common.dialogs.preferences;


import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: ViewsPreferencePage — ADMIRAL ACKBAR CONFIGURES HOME ONE BRIDGE DISPLAYS ──
// Admiral Ackbar stands at the command centre of Home One, pulling up the master
// display configuration panel.  The panel is titled "Views" and describes which
// sub-panels govern which bridge stations.  The master panel itself holds no
// controls — it delegates to sub-panels for the actual configuration.  Ackbar
// removes the Default and Apply buttons because there's nothing to apply at
// this level; all meaningful settings live one level down.  This class is exactly
// that master panel: a top-level container preference page with a title, a
// description, and intentionally no interactive widgets.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The top-level Eclipse preference page for the Views section of the LDAP Browser.
 * Like {@link MainPreferencePage}, this is a pure container node — its only job
 * is to exist as the parent in the preference tree.  We suppress the Default and
 * Apply buttons because there is nothing at this level to apply or reset.
 * Think of this class as Admiral Ackbar's master bridge display — the overview
 * that delegates real work to the sub-stations beneath it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ViewsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    // ── ACKBAR ACTIVATES THE MASTER DISPLAY PANEL ─────────────────────────────────
    // Admiral Ackbar presses the activation key on the bridge console.  The panel
    // announces itself as "Views," reads out its description, and immediately
    // removes the Default and Apply buttons — they have nothing to act on here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the page with its title and description text, then removes the
     * Default and Apply buttons via {@code noDefaultAndApplyButton()} because this
     * page holds no configurable values of its own.
     */
    public ViewsPreferencePage()
    {
        super( Messages.getString( "ViewsPreferencePage.Views" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ViewsPreferencePage.Description" ) ); //$NON-NLS-1$

        // Removing Default and Apply buttons
        noDefaultAndApplyButton();
    }


    // ── ACKBAR ACKNOWLEDGES THE WORKBENCH FRAMEWORK ───────────────────────────────
    // Ackbar nods at the platform systems check and moves on.  There is nothing
    // actionable here — just satisfying the interface contract.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Required by {@link IWorkbenchPreferencePage} — not used here because
     * this page has no workbench-dependent initialisation to perform.
     *
     * @param workbench  The Eclipse workbench instance — not used.
     */
    public void init( IWorkbench workbench )
    {
        // Nothing to do
    }


    // ── ACKBAR DISPLAYS THE BLANK MASTER PANEL ────────────────────────────────────
    // The master bridge display has a title and description but no dials, no
    // switches — those live on the sub-panels.  Ackbar gestures at the empty
    // panel and says: "The sub-stations are this way."  We return the parent
    // composite untouched.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the (intentionally empty) content area.  This page is a container
     * node, so we return the parent composite as-is without adding any widgets.
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The parent composite, unmodified.
     */
    protected Control createContents( Composite parent )
    {
        // Nothing to do

        return parent;
    }
}
