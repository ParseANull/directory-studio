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
package org.apache.directory.studio.apacheds.configuration.wizards;


import org.apache.directory.studio.apacheds.configuration.editor.NewServerConfigurationInput;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewApacheDSConfigurationFileWizard — THE BLANK REQUISITION FORM WIZARD ────────
// An Imperial engineering officer walks into the requisition office and says "I need a new
// blank configuration form."  There are no questions to ask — just hand them the form.
// So this wizard has zero pages.  Its only job is to perform the "finish" action: open a
// blank configuration editor tab so the engineer can start filling in settings.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Zero-page Eclipse wizard that opens a blank {@link ServerConfigurationEditor} tab.
 * Registered as a "New…" wizard in plugin.xml under the configuration category.
 * When the user launches it and clicks Finish (no pages, so Finish is immediate),
 * it opens the editor with a {@link NewServerConfigurationInput}.
 * Think of it as the officer walking out with a blank requisition form in hand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewApacheDSConfigurationFileWizard extends Wizard implements INewWizard
{
    // ── Adding No Pages To The Wizard ─────────────────────────────────────────────────────────
    // This wizard skips all the "what kind of file?" questions.
    // One action, no choices — open a blank config editor.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds no pages — this wizard has no intermediate steps.
     * Eclipse calls this during wizard initialisation; we intentionally leave it empty.
     */
    public void addPages()
    {
        // This wizard has no page
    }


    // ── Opening The Blank Configuration Editor ────────────────────────────────────────────────
    // When Finish is clicked, we grab the active workbench page and open the configuration
    // editor with a NewServerConfigurationInput (blank form).
    // If the editor can't open (PartInitException), we return false — that shouldn't happen
    // in practice since the editor is registered via plugin.xml.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link ServerConfigurationEditor} with a blank {@link NewServerConfigurationInput}.
     * Returns {@code false} only if the editor fails to open (which should not happen in practice).
     *
     * <p>For example — the wizard completes instantly:</p>
     * <pre>
     *   user clicks "New > ApacheDS 2.0 Configuration" → wizard opens → Finish
     *   → page.openEditor(NewServerConfigurationInput, ServerConfigurationEditor.ID)
     *   → blank editor tab appears with default settings
     * </pre>
     *
     * @return {@code true} if the editor opened successfully; {@code false} otherwise
     */
    public boolean performFinish()
    {
        try
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            page.openEditor( new NewServerConfigurationInput(), ServerConfigurationEditor.ID );
        }
        catch ( PartInitException e )
        {
            // Should never happen
            return false;
        }

        return true;
    }


    // ── Initialising The Wizard From The Workbench ────────────────────────────────────────────
    // Eclipse calls init() after the wizard is created.  We have nothing to do here because
    // we don't need the workbench reference or the current selection.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to initialise the wizard. Nothing to do here — we don't use
     * the workbench reference or the current selection.
     *
     * @param workbench  the current workbench
     * @param selection  the current object selection in the workbench (ignored)
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
    }
}
