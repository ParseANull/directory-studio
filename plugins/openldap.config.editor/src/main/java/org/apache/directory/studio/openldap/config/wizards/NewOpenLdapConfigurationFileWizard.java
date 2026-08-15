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
package org.apache.directory.studio.openldap.config.wizards;


import org.apache.directory.studio.ldapbrowser.common.dialogs.preferences.AttributeValueEditorDialog;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.editor.NewServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.editor.dialogs.OpenLdapConfigDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewOpenLdapConfigurationFileWizard — The Emperor's Plan to Create a New Config
// The Emperor doesn't improvise — every move is a deliberate, sequenced plan: identify
// the target (dialog), get the details (format + version), then open the editor to begin
// the real work. This wizard follows the same pattern: one dialog for options, then the
// full OpenLDAP server configuration editor springs to life in the workbench.
// Think of it as the Emperor executing Order 66 in stages — each step leads inexorably
// to the next until the editor is open and the config is ready to write.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * This class implements the New OpenLDAP Configuration File Wizard.
 * It guides the user through creating a new OpenLDAP server configuration: it shows
 * a dialog for choosing the config format and version, then opens the configuration
 * editor with a fresh, empty server configuration.
 * Think of this as the Emperor executing his plan step-by-step to convert the workbench
 * into an active OpenLDAP configuration editing session.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewOpenLdapConfigurationFileWizard extends Wizard implements INewWizard
{
    /** The window. */
    private IWorkbenchWindow window;


    // ── constructor — The Emperor Prepares to Issue His Directive ──────────────────
    // Before Palpatine issues Order 66, he does nothing flashy — he simply waits,
    // ready. This constructor is equally minimal; it sets nothing, because the
    // actual choices happen later when the user fills out the dialog.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of NewOpenLDAPConfigurationFileWizard.
     * Nothing happens here — the wizard is empty until {@link #performFinish()} runs.
     */
    public NewOpenLdapConfigurationFileWizard()
    {
        // Nothing to do
    }


    // ── init — The Emperor Identifies the Active Workbench Window ─────────────────
    // Palpatine always knows which room he's in — he captures the active window so
    // he knows exactly where to open the new editor when the time comes. Without
    // this reference, performFinish wouldn't know which workbench window to use.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the wizard with the active workbench and the current selection.
     * We capture the active workbench window here so {@link #performFinish()} knows
     * where to open the editor.
     *
     * <p>For example — the Emperor identifies his command centre:</p>
     * <pre>
     *   wizard.init( workbench, selection );
     *   // wizard.window is now set to the active workbench window
     * </pre>
     *
     * @param workbench  the active Eclipse workbench
     * @param selection  the current workbench selection (not used here)
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        window = workbench.getActiveWorkbenchWindow();
    }


    // ── dispose — The Emperor Closes the Session ───────────────────────────────────
    // When the wizard is done, Palpatine releases his grip on the workbench window
    // reference. There is no state to carry forward — we null the window out so the
    // GC can clean up cleanly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes of wizard resources.
     * We release the window reference here to avoid holding onto Eclipse UI objects
     * longer than necessary.
     *
     * <p>For example — the Emperor wraps up the briefing:</p>
     * <pre>
     *   wizard.dispose();
     *   // wizard.window == null
     * </pre>
     */
    public void dispose()
    {
        window = null;
    }


    // ── getId — The Emperor Announces the Wizard's Registry ID ────────────────────
    // The plugin manifest registers this wizard under a constant ID. Anyone who
    // wants to refer to this wizard (e.g., to show it from a menu or test) calls
    // this static method to get the registered ID string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the wizard's registered plugin extension ID.
     * Use this to look up or reference the wizard by ID from the plugin registry.
     *
     * <p>For example — the Emperor reveals the wizard's ID:</p>
     * <pre>
     *   String id = NewOpenLdapConfigurationFileWizard.getId();
     *   // id == OpenLdapConfigurationPluginConstants.WIZARD_NEW_OPENLDAP_CONFIG
     * </pre>
     *
     * @return  the string ID used in the plugin manifest for this wizard
     */
    public static String getId()
    {
        return OpenLdapConfigurationPluginConstants.WIZARD_NEW_OPENLDAP_CONFIG;
    }


    // ── addPages — The Emperor Needs No Additional Pages ──────────────────────────
    // Palpatine's plan is efficient — instead of walking the user through multiple
    // wizard pages, we open a single dialog in performFinish. So addPages does
    // nothing; this wizard has no wizard pages at all.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds pages to this wizard.
     * This wizard has no wizard pages — the user interaction happens through a
     * dialog opened in {@link #performFinish()}, not through standard wizard pages.
     *
     * <p>For example — the Emperor skips the page setup:</p>
     * <pre>
     *   wizard.addPages(); // no-op — no pages needed
     * </pre>
     */
    public void addPages()
    {
        // This wizard has no page
    }


    // ── performFinish — The Emperor Executes the Plan ─────────────────────────────
    // Palpatine opens the briefing room (dialog), learns the format and version,
    // then immediately activates the new configuration editor in the workbench. If
    // the user cancels the dialog, nothing happens. If the editor can't open,
    // we return false — which is fine, it should never happen in normal use.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes when the user clicks "Finish" in the wizard.
     * We open the {@link OpenLdapConfigDialog} to ask for config format and version,
     * then open the {@link OpenLdapServerConfigurationEditor} with those settings.
     * If the dialog is cancelled or the editor can't open, we return false.
     *
     * <p>For example — the Emperor executes Order 66 in two steps:</p>
     * <pre>
     *   // Step 1: open dialog, get format + version
     *   // Step 2: open editor with NewServerConfigurationInput
     *   performFinish(); // returns true if editor was opened successfully
     * </pre>
     *
     * @return  {@code true} if the editor was opened successfully;
     *          {@code false} if the user cancelled or the editor couldn't open
     */
    public boolean performFinish()
    {
        try
        {
            OpenLdapConfigDialog dialog = new OpenLdapConfigDialog( getShell() );

            if ( dialog.open() == AttributeValueEditorDialog.OK )
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                NewServerConfigurationInput configInput = new NewServerConfigurationInput();
                configInput.setOpenLdapConfigFormat( dialog.getOpenLdapConfigFormat() );
                configInput.setOpenLdapVersion( dialog.getOpenLdapVersion() );

                page.openEditor( configInput, OpenLdapServerConfigurationEditor.ID );
            }
        }
        catch ( PartInitException e )
        {
            // Should never happen
            return false;
        }

        return true;
    }
}
