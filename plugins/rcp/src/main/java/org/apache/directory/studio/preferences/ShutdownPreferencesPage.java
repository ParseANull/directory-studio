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
package org.apache.directory.studio.preferences;


import org.apache.directory.studio.Activator;
import org.apache.directory.studio.Messages;
import org.apache.directory.studio.PluginConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ShutdownPreferencesPage — The Death Star's Self-Destruct Countdown ──
// On the Death Star, Commander Tagge has a two-step confirmation protocol
// before the self-destruct fires: press the button, then confirm with a toggle
// switch to prevent accidental activation.
// ShutdownPreferencesPage is our equivalent: it lets the user decide whether
// Studio should ask "Are you sure?" before closing, and it is the component
// that shows (and saves the answer to) that confirmation dialog.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The preference page for controlling Studio's exit confirmation dialog.
 * Users can toggle whether closing the last window shows a "Really quit?" prompt.
 * The static {@link #promptOnExit()} method is the one called at runtime to
 * actually check the preference and show the dialog when needed.
 * Think of this as the Death Star's two-stage self-destruct confirmation —
 * you set the policy here, and the system enforces it when the moment comes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShutdownPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    // UI fields
    private Button confirmExitClosingLastWindowCheckbox;


    // ── Commander Tagge Opens the Self-Destruct Config Panel ──────────────────
    // Commander Tagge walks to the red panel, lifts the cover, and sees the
    // current setting: "Confirm before activation: ON."  He sets the page title
    // and hooks in the preference store so any changes he makes will persist.
    // Our constructor sets the page title and wires up the preference store.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the shutdown preferences page with the correct title and preference store.
     * Eclipse calls this when the user navigates to our page in the Preferences dialog.
     * We set the page title from our message bundle and point Eclipse at the rcp
     * plugin's preference store so our settings are saved to the right place.
     */
    public ShutdownPreferencesPage()
    {
        super( Messages.getString( "ShutdownPreferencesPage.PageTitle" ) ); //$NON-NLS-1$
        super.setPreferenceStore( Activator.getDefault().getPreferenceStore() );
    }


    // ── Tagge Acknowledges the Briefing — No Extra Setup Needed ──────────────
    // Tagge listens to the briefing about this particular console but has nothing
    // extra to set up — the console already knows which ship it's on.
    // init() is called by Eclipse with a reference to the workbench; we don't
    // need it here because our preference store is already set in the constructor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the page is initialised within the Preferences dialog.
     * We have nothing to do here — all setup happens in the constructor.
     *
     * @param workbench  the workbench instance — not used by this page.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── Tagge Reveals the Control Panel — One Toggle, One Choice ─────────────
    // Tagge flips open the panel to reveal a single clearly-labelled toggle
    // switch: "Confirm before closing last window."  It's pre-loaded with the
    // current setting so Tagge immediately sees what state the system is in.
    // createContents() builds that exact UI: a checkbox pre-ticked based on
    // what the preference store currently says.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page's SWT widget tree.
     * We create a simple composite with one checkbox labelled "Confirm on exit
     * when closing the last window", then call {@link #refreshUI()} to set its
     * initial state from the preference store.
     *
     * @param parent  the SWT container provided by the Preferences dialog — we
     *                create our widgets as children of this composite.
     * @return        the root {@link Control} of our widget tree.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout();
        gl.marginHeight = gl.marginWidth = 0;
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        confirmExitClosingLastWindowCheckbox = new Button( composite, SWT.CHECK );
        confirmExitClosingLastWindowCheckbox.setText( Messages
            .getString( "ShutdownPreferencesPage.ConfirmExitClosingLastWindow" ) ); //$NON-NLS-1$

        refreshUI();

        return composite;
    }


    // ── Tagge Reads the Current Switch Position ───────────────────────────────
    // Tagge glances at the toggle and notes its current position — on or off —
    // so the UI reflects what is actually stored in the system memory.
    // refreshUI() reads the preference store and sets the checkbox accordingly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Synchronises the checkbox widget with the current preference store value.
     * We call this during construction and after "Restore Defaults" to ensure
     * the UI accurately reflects what's actually saved.
     */
    private void refreshUI()
    {
        confirmExitClosingLastWindowCheckbox.setSelection( getPreferenceStore().getBoolean(
            PluginConstants.PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW ) );
    }


    // ── Tagge Locks In the Setting — The Panel Is Now Armed ──────────────────
    // Tagge flips the toggle to its new position and presses the commit button;
    // the self-destruct panel records the new setting to permanent memory.
    // performOk() reads the current checkbox state and writes it to the
    // Eclipse preference store so it persists across restarts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current checkbox state to the preference store when the user clicks OK.
     * Eclipse calls this when the user clicks "OK" or "Apply" in the Preferences dialog.
     *
     * @return  {@code true} always — we never need to veto the OK action here.
     */
    public boolean performOk()
    {
        getPreferenceStore().setValue( PluginConstants.PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
            confirmExitClosingLastWindowCheckbox.getSelection() );

        return true;
    }


    // ── Tagge Resets the Toggle to Factory Default ────────────────────────────
    // Tagge hits the "Reset to Default" recessed button; the panel reverts to
    // the factory-programmed setting (confirmation enabled) and the toggle
    // position snaps back to ON.
    // performDefaults() restores the preference to its default value and
    // refreshes the checkbox to show the new state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Restores the exit-confirmation preference to its factory default value.
     * Eclipse calls this when the user clicks "Restore Defaults".  We write the
     * default value to the store, then refresh the UI so the checkbox updates.
     */
    protected void performDefaults()
    {
        getPreferenceStore().setValue( PluginConstants.PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
            getPreferenceStore().getDefaultBoolean( PluginConstants.PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW ) );

        super.performDefaults();
    }


    // ── The Self-Destruct Sequence Initiates — Confirm or Stand Down ──────────
    // The Death Star's self-destruct protocol fires: Commander Tagge has ten
    // seconds to confirm or abort.  If he says "confirm" the sequence continues;
    // if he says "abort" or toggles off the "ask me again" switch, it stops.
    // promptOnExit() is that protocol: it checks the preference, shows the dialog
    // if needed, and tells the caller whether to proceed with shutdown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Decides whether to proceed with application exit, optionally prompting the user.
     * Called by {@link org.apache.directory.studio.ApplicationWorkbenchAdvisor#preShutdown()}
     * just before the workbench closes.  If there are multiple windows open we skip
     * the prompt (closing one window never exits the app).  If the "confirm on exit"
     * preference is enabled we show a yes/no dialog with a "don't ask again" toggle.
     *
     * <p>For example — Commander Tagge at the self-destruct console:</p>
     * <pre>
     *   if (multipleWindowsOpen) return true;  // not the last one, no drama
     *   if (!promptEnabled) return true;        // preference says just quit
     *   show dialog → user clicks OK → return true (proceed)
     *   user clicks Cancel → return false (abort shutdown)
     *   user ticks "don't ask again" → save that choice and return true
     * </pre>
     *
     * @return  {@code true} if the application should exit, {@code false} if the
     *          user cancelled and shutdown should be aborted.
     */
    public static boolean promptOnExit()
    {
        // Checking for multiple workbench windows
        if ( PlatformUI.getWorkbench().getWorkbenchWindowCount() > 1 )
        {
            return true;
        }

        // Getting the preferred exit mode from the preferences
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        boolean promptOnExit = store.getBoolean( PluginConstants.PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW );

        if ( promptOnExit )
        {
            MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm( PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(),
                Messages.getString( "ShutdownPreferencesPage.PromptOnExitTitle" ), //$NON-NLS-1$
                Messages.getString( "ShutdownPreferencesPage.PromptOnExitMessage" ), //$NON-NLS-1$
                Messages.getString( "ShutdownPreferencesPage.PromptOnExitToggleMessage" ), false, null, null ); //$NON-NLS-1$

            // Checking the dialog's return code
            if ( dialog.getReturnCode() != IDialogConstants.OK_ID )
            {
                return false;
            }

            // Saving the preferred exit mode value to the preferences
            if ( dialog.getToggleState() )
            {
                store.setValue( PluginConstants.PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, false );
                Activator.getDefault().savePluginPreferences();
            }
        }

        return true;
    }
}
