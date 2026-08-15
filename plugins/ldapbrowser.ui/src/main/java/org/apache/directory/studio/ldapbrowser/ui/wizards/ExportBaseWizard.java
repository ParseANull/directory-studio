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


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportBaseWizard — LUKE'S HERO JOURNEY STARTS HERE ────────────────
// Every export wizard is a version of Luke's journey: choose the source
// (the From page), choose the destination (the To page), and then launch the
// job that converts and delivers the data. This abstract base class holds
// the common state — the search (what to export), the export filename
// (where to put it) — and provides the scaffold for the two standard phases.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all export wizards in the ldapbrowser.ui package.
 * Concrete subclasses inherit the search and exportFilename fields and
 * the init/setters/getters, while supplying their own {@code addPages()},
 * {@code createPageControls()}, and {@code performFinish()} implementations.
 * Think of this as the common Rebel briefing template: every mission has a
 * target and a delivery point; the specifics differ per wizard.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class ExportBaseWizard extends Wizard implements IExportWizard
{

    /** The export filename. */
    protected String exportFilename = ""; //$NON-NLS-1$

    /** The search. */
    protected ISearch search;


    // ── Luke Reads the Mission Title and Sets the Stage ───────────────────────────
    // Every mission gets a title on the briefing-room whiteboard, and Luke's
    // target is read from the current workbench selection.
    // We set the window title and call init() immediately so the search is
    // ready before pages are added.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportBaseWizard with the given window title and initialises
     * the search from the current workbench selection.
     * We call {@code init()} immediately rather than waiting for Eclipse to call it,
     * because the subclass {@code addPages()} may need the search object.
     *
     * @param title  the wizard window title (shown in the title bar).
     */
    public ExportBaseWizard( String title )
    {
        super();
        setWindowTitle( title );
        init( null, ( IStructuredSelection ) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
            .getSelection() );
    }


    // ── Luke Derives the Target From the Current Selection ────────────────────────
    // The Rebel briefing room shows Luke which connection and search parameters
    // are currently active — he doesn't have to type them from scratch.
    // We derive an "example search" from the selection so the From page
    // pre-populates correctly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Initialises the search from the current workbench selection and resets the
     * export filename. Called by the constructor and by Eclipse when the wizard
     * is opened via the File → Export menu.
     *
     * @param workbench   the current workbench (unused; selection comes from PlatformUI).
     * @param selection   the current structured selection used to derive the search.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        search = BrowserSelectionUtils.getExampleSearch( selection );
        search.setName( null );
        exportFilename = ""; //$NON-NLS-1$
    }


    // ── Luke Logs the Delivery Address ────────────────────────────────────────────
    // The To page tells Luke where to deliver the exported data — he records it
    // so performFinish() knows the destination.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the destination file path for the export.
     * Called by the To wizard page when the user picks or types a file path.
     *
     * @param exportFilename  the full path of the file to write the export to.
     */
    public void setExportFilename( String exportFilename )
    {
        this.exportFilename = exportFilename;
    }


    // ── Luke Checks the Delivery Address ──────────────────────────────────────────
    // The finish step needs to know where to deliver the payload.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the destination file path set by the To wizard page.
     * Used in {@code performFinish()} to pass to the export runnable.
     *
     * @return  the export file path, or an empty string if not yet set.
     */
    public String getExportFilename()
    {
        return exportFilename;
    }


    // ── Luke Checks His Mission Parameters ───────────────────────────────────────
    // The From page populates the search object with the user's choices;
    // performFinish() reads it back to configure the export job.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search that describes the data to export (connection, base DN,
     * filter, scope, returning attributes).
     * The From wizard page modifies this search; {@code performFinish()} reads it.
     *
     * @return  the export search object.
     */
    public ISearch getSearch()
    {
        return search;
    }


    // ── Luke Updates His Mission Parameters ──────────────────────────────────────
    // Sometimes the calling code (e.g., ExportAction) sets the search directly
    // rather than waiting for the From page to fill it in.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the search used by this wizard.
     * Used by callers that already have a fully configured search (e.g., from
     * a view's current input) and want to skip the From page's setup.
     *
     * @param search  the new search to use for the export.
     */
    public void setSearch( ISearch search )
    {
        this.search = search;
    }

}
