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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: MainPreferencePage — THE ALLIANCE WAR COUNCIL AGENDA ───────────────
// The Alliance War Council assembles on Home One, Mon Mothma presiding.  The
// agenda cover sheet is stamped "LDAP Browser — General Settings."  There are
// no action items on the cover sheet itself — it exists to frame the agenda and
// tell sub-committees (Browser, Attributes, Entry Editor…) where their sections
// begin.  This class is exactly that cover sheet: a top-level preference page
// with a description and no widgets of its own, serving purely as the root node
// that Eclipse uses to group the LDAP Browser sub-pages in the preference tree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The top-level Eclipse preference page for the LDAP Browser plugin.
 * It holds no editable widgets — its only job is to be the parent node in the
 * preference tree under which Browser, Attributes, Entry Editor, and other
 * sub-pages appear.
 * Think of this class as the Alliance War Council's agenda cover sheet — the
 * title and description that introduce everything underneath.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MainPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    // ── MON MOTHMA CALLS THE COUNCIL TO ORDER ─────────────────────────────────────
    // Mon Mothma places the agenda cover sheet on the table: title "LDAP Browser,"
    // description "General Settings," and points the secretariat to the correct
    // preference store for any decisions recorded at this level.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the top-level preference page with title, description, and the
     * correct preference store.  Eclipse calls this when the user first expands
     * the LDAP Browser node in the preference tree.
     */
    public MainPreferencePage()
    {
        super( Messages.getString( "MainPreferencePage.LDAP" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserCommonActivator.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "MainPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── THE COUNCIL NOTES THE WORKBENCH'S PRESENCE ────────────────────────────────
    // The council acknowledges the platform framework and moves straight to the
    // agenda.  Nothing actionable here — just satisfying the interface contract.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Required by {@link IWorkbenchPreferencePage} — not used here because
     * this page has no workbench-dependent initialisation to perform.
     *
     * @param workbench  The Eclipse workbench instance — not used.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── MON MOTHMA DISPLAYS THE BLANK AGENDA COVER ────────────────────────────────
    // The cover sheet has a title and description but no actionable items.
    // Mon Mothma pins it to the board and gestures for sub-committees to proceed.
    // We create a minimal composite here — just spacers — because the real work
    // lives in the child preference pages.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the (intentionally empty) content area for this page.
     * We create a single-column container with spacers and nothing else —
     * this page is a container node, not an editor.
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        A minimal composite with no interactive widgets.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        return composite;
    }


    // ── MON MOTHMA ADJOURNS WITHOUT DECISIONS AT THIS LEVEL ──────────────────────
    // The cover-sheet session has no resolutions to record — sub-committees handle
    // their own minutes.  We call super to satisfy the Eclipse contract and move on.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to the superclass — this top-level page has no preference values
     * of its own to restore to defaults.
     */
    protected void performDefaults()
    {
        super.performDefaults();
    }


    // ── THE COUNCIL ACCEPTS THE COVER SHEET AND CLOSES ────────────────────────────
    // Mon Mothma signs the agenda cover — no objections, no changes needed at this
    // level.  Sub-committees have already handled their own sections.  We return
    // true to let Eclipse know the save succeeded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK or Apply.  This page has no preference values
     * of its own, so we always return true and let the child pages do their work.
     *
     * @return  Always true — nothing to validate or save at this level.
     */
    public boolean performOk()
    {
        return true;
    }

}
