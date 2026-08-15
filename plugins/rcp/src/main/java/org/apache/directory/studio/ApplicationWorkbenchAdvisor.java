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

package org.apache.directory.studio;


import org.apache.directory.studio.preferences.ShutdownPreferencesPage;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;


// ── CLASS: ApplicationWorkbenchAdvisor — Rebel Fleet Assembles at Sullust ────
// Admiral Ackbar on Home One orchestrates the entire fleet's preparation before
// the Battle of Endor: he chooses the staging area (initial perspective), sets
// up communications (window advisor), and decides what happens if something goes
// wrong at the last moment (preShutdown).
// The WorkbenchAdvisor is Eclipse's equivalent — it governs the whole-workbench
// lifecycle from first initialization through to final shutdown.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Controls the overall Eclipse workbench lifecycle for Directory Studio.
 * Eclipse calls the methods here at specific moments — before any windows open,
 * after all windows are restored, before shutdown, and after shutdown.
 * Think of this as Admiral Ackbar on Home One: the strategic commander who
 * sets the battle plan before the first X-wing launches.
 *
 * <p>Lifecycle order (all within the scope of {@code PlatformUI.createAndRunWorkbench}):
 * <ol>
 *   <li>{@code initialize} — parse command line, register adapters, declare images</li>
 *   <li>{@code preStartup} — options affecting which editors/views open initially</li>
 *   <li>{@code postStartup} — start automatic processes, open tips windows</li>
 *   <li>{@code preShutdown} — last chance to veto the shutdown</li>
 *   <li>{@code postShutdown} — final cleanup after all windows are closed</li>
 * </ol>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor
{
    // ── Ackbar Configures Home One's Battle Systems ───────────────────────────
    // Before a single ship jumps to Endor, Ackbar sets Home One's systems:
    // shields to save-and-restore mode so nothing is lost if comms are cut,
    // help beacons activated on all channels, and tactical imagery preloaded.
    // initialize() mirrors this: we enable window geometry save/restore so the
    // user's layout survives restarts, and wire up the help icon in JFace dialogs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Performs one-time workbench initialisation before any windows open.
     * We enable Eclipse's save-and-restore feature (so window positions and sizes
     * survive a restart) and wire up the help icon that appears in the corner of
     * every JFace dialog — without this the help icon silently fails to appear.
     *
     * <p>For example — Ackbar configures Home One before the fleet jumps:</p>
     * <pre>
     *   configurer.setSaveAndRestore(true)   // window state survives restarts
     *   TrayDialog.setDialogHelpAvailable(true)  // help icon in dialogs
     *   register help image in JFace image registry
     * </pre>
     *
     * @param configurer  Eclipse's handle for tweaking the workbench — we use it
     *                    to turn on window geometry persistence.
     */
    public void initialize( IWorkbenchConfigurer configurer )
    {
        //enable the save/restore windows size & position feature
        configurer.setSaveAndRestore( true );

        //enable help button in dialogs
        TrayDialog.setDialogHelpAvailable( true );
        ImageRegistry reg = JFaceResources.getImageRegistry();
        ImageDescriptor helpImage = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
            ISharedImages.IMG_LCL_LINKTO_HELP );
        reg.put( Dialog.DLG_IMG_HELP, helpImage );
    }


    // ── Ackbar Assigns a Window Commander ─────────────────────────────────────
    // When a new Rebel cruiser joins the fleet, Ackbar assigns an experienced
    // commander to that ship's bridge — someone who knows how to configure it
    // for our specific mission profile.
    // createWorkbenchWindowAdvisor() does the same: each new workbench window
    // gets its own ApplicationWorkbenchWindowAdvisor to configure it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the advisor that configures each new workbench window.
     * Eclipse calls this once per window; we return our
     * {@link ApplicationWorkbenchWindowAdvisor} which sets the initial size,
     * enables the cool bar and perspective bar, and manages the title bar text.
     *
     * @param configurer  Eclipse's per-window configuration handle — passed
     *                    straight through to our window advisor.
     * @return            a new {@link ApplicationWorkbenchWindowAdvisor} for the window.
     */
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor( IWorkbenchWindowConfigurer configurer )
    {
        return new ApplicationWorkbenchWindowAdvisor( configurer );
    }


    // ── Ackbar Names the Staging Area — "Rendezvous at the Forest Moon" ───────
    // Ackbar designates the Forest Moon of Endor as the fleet's initial staging
    // area — that's where everyone gathers first before moving to their positions.
    // getInitialWindowPerspectiveId() names the perspective that opens when the
    // user launches Studio: the LDAP Browser perspective, which is our home base.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the perspective to open in the first workbench window at startup.
     * We always open the LDAP Browser perspective — that's where users spend
     * most of their time browsing directory entries, so it makes sense as the
     * default starting view.
     *
     * @return  the fully-qualified perspective ID for the LDAP Browser perspective.
     */
    public String getInitialWindowPerspectiveId()
    {
        return "org.apache.directory.studio.ldapbrowser.ui.perspective.BrowserPerspective"; //$NON-NLS-1$
    }


    // ── Ackbar Considers Whether to Abort the Attack Run ─────────────────────
    // Before the fleet commits to the attack run on the Death Star, Ackbar has
    // one last moment to call it off if something critical is wrong.
    // preShutdown() is that moment: we ask the user to confirm they really want
    // to quit, and if they say no we return false to cancel the shutdown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse just before the workbench starts closing all windows.
     * We delegate to {@link ShutdownPreferencesPage#promptOnExit()} which shows
     * a confirmation dialog if the user has that preference enabled.
     * Returning {@code false} cancels the shutdown entirely — like Ackbar
     * ordering the fleet to fall back.
     *
     * @return  {@code true} to proceed with shutdown, {@code false} to cancel it.
     */
    public boolean preShutdown()
    {
        return ShutdownPreferencesPage.promptOnExit();
    }


    // ── The Fleet Is Assembled — Final Checks Before the Battle ──────────────
    // After all ships have arrived at the Endor rendezvous and the briefing is
    // done, Ackbar runs a few last-minute housekeeping tasks before giving the
    // order to attack — clearing old nav data, double-checking the tactical display.
    // postStartup() runs after all windows are open and lets us do those same
    // kinds of cleanup tasks that need a fully-running workbench to operate.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called after all workbench windows are open and the event loop is about to start.
     * We do two housekeeping tasks here: remove a stale JDT JVM preference that
     * can confuse ApacheDS startup, and swap out the built-in Eclipse Appearance
     * preference page for our own streamlined version.
     */
    @Override
    public void postStartup()
    {
        super.postStartup();
        removeDefaultJvmSetting();
        replaceAppearancePage();
    }


    // ── Ackbar Swaps the Tactical Display for a Better One ───────────────────
    // The standard briefing room hologram shows more information than the Rebels
    // need for this mission; Ackbar replaces it with a focused tactical overlay
    // that hides the distracting "Color and Font theme" selector.
    // replaceAppearancePage() does the same in Eclipse's preference system.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces Eclipse's built-in Appearance preference page with our own.
     * The stock {@code ViewsPreferencePage} shows a "Color and Fonts theme"
     * combo that is superseded by the CSS theme selector — it's confusing and
     * unnecessary, so we substitute our {@link StudioAppearancePage} which hides it.
     */
    private void replaceAppearancePage()
    {
        PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode general = manager.find( "org.eclipse.ui.preferencePages.Workbench" );
        if ( general == null )
        {
            return;
        }
        for ( IPreferenceNode node : general.getSubNodes() )
        {
            if ( "org.eclipse.ui.preferencePages.Views".equals( node.getId() ) )
            {
                general.remove( node );
                general.add( new PreferenceNode( "org.eclipse.ui.preferencePages.Views",
                    new StudioAppearancePage() ) );
                return;
            }
        }
    }


    // ── Ackbar Clears the Old Nav Coordinates — Fresh Start Only ─────────────
    // After a long campaign the fleet's nav computer can accumulate outdated jump
    // coordinates that cause the next mission to go to the wrong system entirely.
    // Ackbar's crew wipes those stale entries before every new deployment.
    // removeDefaultJvmSetting() does the same: it clears a stale JDT preference
    // that would otherwise point ApacheDS at the wrong JVM on launch.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a stale JDT "default JVM" preference to prevent ApacheDS launch problems.
     * DIRSTUDIO-1188: when you first launch ApacheDS from Studio, the JDT plugin
     * records the current Studio JVM as the default and saves it to disk.  If the
     * Studio JVM later changes (update, different install) that saved path is wrong
     * and ApacheDS fails to start.  Since Studio has no "Installed JREs" page we
     * simply delete the setting every launch so the current JVM is always used.
     */
    private void removeDefaultJvmSetting()
    {
        IEclipsePreferences pref = InstanceScope.INSTANCE.getNode( "org.eclipse.jdt.launching" );
        pref.remove( "org.eclipse.jdt.launching.PREF_VM_XML" );
    }

}
