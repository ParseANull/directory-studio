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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.search.SearchPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;


// ── CLASS: NewSearchWizard — R2-D2 PLUGS INTO THE DEATH STAR COMPUTER ────────
// R2-D2 doesn't browse directories manually — he plugs in and opens the search
// interface immediately. This wizard is a stub that bypasses the normal wizard
// flow entirely: {@code performFinish()} just opens the platform's search dialog,
// which is the real interface. The wizard exists only so we can register a
// "New Search" entry in the Eclipse "File → New" menu.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Stub wizard that adds "New Search" to the Eclipse "File → New..." menu.
 * Does not show any wizard pages. {@code performFinish()} immediately opens
 * Eclipse's search dialog ({@link NewSearchUI#openSearchDialog}) pre-focused
 * on the {@link SearchPage}.
 * The wizard has no pages, no state, and needs no progress monitor.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewSearchWizard extends Wizard implements INewWizard
{
    /** The window. */
    private IWorkbenchWindow window;


    // ── R2-D2 Boots Up ────────────────────────────────────────────────────────────
    // The constructor is empty — all setup happens in init().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewSearchWizard. No pages, no title — this wizard is
     * never shown to the user; it just triggers the search dialog.
     */
    public NewSearchWizard()
    {
        // Nothing to do
    }


    // ── R2-D2 Notes the Active Window ─────────────────────────────────────────────
    // We need the window reference to open the search dialog in the correct context.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Stores the active workbench window for use in {@link #performFinish()}.
     *
     * @param workbench  the current workbench.
     * @param selection  the current selection (unused).
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        window = workbench.getActiveWorkbenchWindow();
    }


    // ── R2-D2 Disconnects ─────────────────────────────────────────────────────────
    // Release the window reference on disposal to avoid holding a stale reference.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Releases the window reference on disposal.
     */
    public void dispose()
    {
        window = null;
    }


    // ── R2-D2 Has a Registry ID ───────────────────────────────────────────────────
    // The search wizard is registered by constant ID in the Eclipse wizard registry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the new search wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_NEW_SEARCH;
    }


    // ── R2-D2 Plugs In and Opens the Search Panel ────────────────────────────────
    // Rather than filling in wizard pages, we just open the platform's search
    // dialog directly. The wizard closes immediately after.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Opens the Eclipse search dialog pre-focused on the LDAP browser
     * {@link SearchPage} and returns {@code true} so the wizard closes.
     * This is the entire purpose of this class — no wizard pages are shown.
     *
     * @return  {@code true} always.
     */
    public boolean performFinish()
    {
        NewSearchUI.openSearchDialog( window, SearchPage.getId() );

        return true;
    }
}
