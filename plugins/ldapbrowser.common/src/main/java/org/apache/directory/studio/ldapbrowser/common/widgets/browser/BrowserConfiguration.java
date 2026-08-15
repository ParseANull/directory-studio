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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;


// ── CLASS: BrowserConfiguration — HAN'S PRE-FLIGHT CHECKLIST ─────────────────
// Before the Millennium Falcon jumps to hyperspace, Han Solo runs through his
// pre-flight checklist: nav computer calibrated, hyperdrive primed, shields up,
// landing struts retracted. Every subsystem gets wired up in the right order
// before the ship is handed to the pilot.
// BrowserConfiguration is the Falcon's pre-flight checklist for the browser
// widget — it lazily creates and connects each subsystem (content provider,
// label provider, sorter, preferences, context menu) and hands them to whoever
// needs them, all through a single, well-known configuration object.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Assembles and owns all the configurable subsystems for the browser tree widget.
 * Rather than having the widget create its own content provider, label provider,
 * sorter, and preferences, we centralize all of that here — it makes it easy
 * to swap implementations or extend the browser for specific use cases.
 * Think of this class as Han's pre-flight checklist: every component is
 * initialized on demand and disposed cleanly when the widget shuts down.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserConfiguration
{

    /** The disposed flag */
    private boolean disposed = false;

    /** The sorter. */
    protected BrowserSorter sorter;

    /** The preferences. */
    protected BrowserPreferences preferences;

    /** The content provider. */
    protected BrowserContentProvider contentProvider;

    /** The label provider. */
    protected BrowserLabelProvider labelProvider;

    /** The decorating label provider. */
    protected DecoratingLabelProvider decoratingLabelProvider;

    /** The context menu manager. */
    protected MenuManager contextMenuManager;


    // ── THE FALCON SITS ON THE TARMAC, READY TO BE PREPPED ───────────────────
    // The Falcon is parked in the Mos Eisley hangar — clean slate, no systems
    // active yet. Han hasn't started the checklist; every component will be
    // initialized lazily on first request.
    // Our constructor is intentionally empty; we use lazy initialization in each
    // getter instead of creating everything up front.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BrowserConfiguration with no subsystems initialized yet.
     * Everything (the sorter, label provider, content provider, etc.) is created
     * lazily — only when first requested. This keeps startup lightweight.
     *
     * <p>For example — the Falcon sits ready in the Mos Eisley hangar:</p>
     * <pre>
     *   BrowserConfiguration config = new BrowserConfiguration();
     *   // Nothing started yet — Han hasn't touched the controls
     * </pre>
     */
    public BrowserConfiguration()
    {
    }


    // ── FALCON POWERS DOWN: ALL SYSTEMS OFF ──────────────────────────────────
    // After the Battle of Yavin, Han powers down every system on the Falcon —
    // shields, nav computer, hyperdrive. Doing it in the right order matters,
    // or you risk a cascade failure (memory leak in our case).
    // We dispose each subsystem carefully, nulling references so the garbage
    // collector can clean up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases all subsystems owned by this configuration and marks it as disposed.
     * This is critical — Eclipse SWT components like menus must be explicitly
     * disposed or they leak OS-level resources. We guard against double-disposal
     * with the {@code disposed} flag.
     *
     * <p>For example — Han powers down every system on the Falcon after Yavin:</p>
     * <pre>
     *   shields.powerDown();
     *   navComputer.powerDown();
     *   hyperdrive.powerDown();
     *   falcon.setDisposed(true);
     * </pre>
     */
    public void dispose()
    {
        if ( !disposed )
        {
            if ( preferences != null )
            {
                preferences.dispose();
                preferences = null;
            }

            if ( contentProvider != null )
            {
                contentProvider.dispose();
                contentProvider = null;
            }

            if ( labelProvider != null )
            {
                labelProvider.dispose();
                labelProvider = null;
                decoratingLabelProvider.dispose();
                decoratingLabelProvider = null;
            }

            if ( contextMenuManager != null )
            {
                contextMenuManager.dispose();
                contextMenuManager = null;
            }

            disposed = true;
        }
    }


    // ── DEPLOYING THE HOLOGRAPHIC NAV DISPLAY ────────────────────────────────
    // When a passenger right-clicks a holographic display panel on the Falcon,
    // the nav computer pops up a context menu — but only if the display system
    // is actually running (lazy initialization: don't start what you don't need).
    // Our context menu is created lazily and attached to the tree's SWT control.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the context menu manager for the browser tree, creating it on first call.
     * The context menu is the right-click popup that appears when the user right-clicks
     * an entry in the tree. We create it lazily and register it with the viewer's
     * underlying SWT control so Eclipse knows to show it on right-click.
     *
     * <p>For example — the Falcon's context-sensitive nav display boots up when first needed:</p>
     * <pre>
     *   if (navDisplay == null) {
     *     navDisplay = new NavDisplay();
     *     navDisplay.attachTo(pilotConsole);
     *   }
     *   return navDisplay;
     * </pre>
     *
     * @param viewer   the browser's tree viewer — we attach the SWT menu to its control
     * @return the context menu manager, creating it if necessary
     */
    public IMenuManager getContextMenuManager( TreeViewer viewer )
    {
        if ( contextMenuManager == null )
        {
            contextMenuManager = new MenuManager();
            Menu menu = contextMenuManager.createContextMenu( viewer.getControl() );
            viewer.getControl().setMenu( menu );
        }

        return contextMenuManager;
    }


    // ── WIRING IN THE CARGO MANIFEST SYSTEM ──────────────────────────────────
    // The Falcon's cargo hold has a manifest system that tracks what's in each
    // crate — it needs to know the ship's preferences and how things should be
    // sorted. We wire those dependencies in when the content provider is first
    // requested.
    // The content provider tells JFace which child objects to show for each node
    // in the browser tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the content provider for the browser tree, creating it on first call.
     * The content provider is the JFace object that answers "what are the children of
     * this tree node?" It needs the browser widget, preferences (for folding settings),
     * and sorter (to sort children before displaying them).
     *
     * <p>For example — Han wires the cargo manifest system into the Falcon's hold:</p>
     * <pre>
     *   if (cargoManifest == null) {
     *     cargoManifest = new CargoManifest(ship, preferences, sorter);
     *   }
     *   return cargoManifest;
     * </pre>
     *
     * @param widget   the browser widget — the content provider needs this to access
     *                 the tree viewer for things like paged loading
     * @return the content provider, creating it if necessary
     */
    public BrowserContentProvider getContentProvider( BrowserWidget widget )
    {
        if ( contentProvider == null )
        {
            contentProvider = new BrowserContentProvider( widget, getPreferences(), getSorter() );
        }

        return contentProvider;
    }


    // ── THE HOLOGRAPHIC LABEL DISPLAY SYSTEM ─────────────────────────────────
    // C-3PO's translation circuit on the Falcon shows each cargo crate's label
    // in a consistent, readable format — and Eclipse's decorator manager can
    // paint an extra icon overlay on top (like a warning symbol). We wrap
    // our label provider in a DecoratingLabelProvider so plugin decorators
    // can add their own overlays to tree nodes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the (decorating) label provider for the browser tree, creating it on first call.
     * The label provider tells JFace what text and icon to show for each tree node.
     * We wrap it in a {@link DecoratingLabelProvider} so that Eclipse platform decorators
     * (like version-control status overlays) can paint additional icons on top.
     *
     * <p>For example — C-3PO's display system plus the Falcon's warning overlays:</p>
     * <pre>
     *   if (labelSystem == null) {
     *     labelSystem = new C3POTranslator(preferences);
     *     decoratedSystem = new OverlaySystem(labelSystem, warningDecorator);
     *   }
     *   return decoratedSystem;
     * </pre>
     *
     * @param viewer   the browser's tree viewer — needed to get the workbench decorator manager
     * @return the decorating label provider, creating it if necessary
     */
    public DecoratingLabelProvider getLabelProvider( TreeViewer viewer )
    {
        if ( labelProvider == null )
        {
            labelProvider = new BrowserLabelProvider( getPreferences() );
            decoratingLabelProvider = new DecoratingLabelProvider( labelProvider, BrowserCommonActivator.getDefault()
                .getWorkbench().getDecoratorManager().getLabelDecorator() );
        }

        return decoratingLabelProvider;
    }


    // ── CALIBRATING THE FALCON'S SORT ORDER ──────────────────────────────────
    // Han dials in the cargo hold's sorting algorithm — how crates get stacked
    // and which ones get priority access. He only sets this up once; subsequent
    // calls just return the already-configured sorter.
    // The BrowserSorter determines how entries are ordered in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sorter for the browser tree, creating it on first call.
     * The sorter controls how child entries appear in the tree — alphabetically
     * by RDN, by RDN value, or unsorted. It reads its settings from the shared
     * preferences object.
     *
     * <p>For example — Han calibrates the Falcon's cargo sort algorithm:</p>
     * <pre>
     *   if (cargoSorter == null) {
     *     cargoSorter = new CargoSorter(preferences);
     *   }
     *   return cargoSorter;
     * </pre>
     *
     * @return the sorter, creating it if necessary
     */
    public BrowserSorter getSorter()
    {
        if ( sorter == null )
        {
            sorter = new BrowserSorter( getPreferences() );
        }

        return sorter;
    }


    // ── LOADING THE FALCON'S CONTROL SETTINGS ────────────────────────────────
    // Before any other system starts, Han needs to load the Falcon's preference
    // settings — display mode, power levels, what to show on the heads-up
    // display. Everything else depends on these settings being available first.
    // BrowserPreferences is the shared settings object that all other components read.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the preferences object for the browser widget, creating it on first call.
     * The preferences object wraps the Eclipse preference store and exposes settings
     * like "sort entries by RDN", "show bookmarks category", "folding threshold", etc.
     * All other subsystems (sorter, content provider, label provider) read from this.
     *
     * <p>For example — Han loads the Falcon's control settings before any subsystem starts:</p>
     * <pre>
     *   if (controlSettings == null) {
     *     controlSettings = new FalconPreferences();
     *   }
     *   return controlSettings;
     * </pre>
     *
     * @return the preferences, creating it if necessary
     */
    public BrowserPreferences getPreferences()
    {
        if ( preferences == null )
        {
            preferences = new BrowserPreferences();
        }

        return preferences;
    }

}
