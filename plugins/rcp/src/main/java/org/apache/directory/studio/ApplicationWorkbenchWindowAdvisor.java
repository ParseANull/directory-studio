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


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;


// ── CLASS: ApplicationWorkbenchWindowAdvisor — The Bridge of Home One ────────
// Home One is Admiral Ackbar's command cruiser — its bridge is where every
// important display, control surface, and status indicator lives.  The bridge
// crew keeps the tactical readout (title bar) up to date as the battle shifts:
// which ship is in focus, which perspective is active, which engagement is live.
// ApplicationWorkbenchWindowAdvisor is our bridge crew: it configures the window
// dimensions, toolbar visibility, and keeps the OS title bar text accurate as
// the user switches editors and perspectives.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Configures each Eclipse workbench window and keeps its title bar up to date.
 * Eclipse calls this advisor once per window (we normally only have one window).
 * We set the initial size, enable the cool bar and perspective bar, and wire up
 * a set of listeners that recompute the window title whenever the user switches
 * editors or perspectives.
 * Think of this class as the crew of Home One's bridge — they keep all displays
 * current as the tactical situation evolves.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
{
    private IEditorPart lastActiveEditor = null;
    private IPerspectiveDescriptor lastPerspective = null;
    private IWorkbenchPage lastActivePage;
    private String lastEditorTitle = ""; //$NON-NLS-1$
    private IAdaptable lastInput;
    private IPropertyListener editorPropertyListener = new IPropertyListener()
    {
        public void propertyChanged( Object source, int propId )
        {
            if ( propId == IWorkbenchPartConstants.PROP_TITLE )
            {
                if ( lastActiveEditor != null )
                {
                    String newTitle = lastActiveEditor.getTitle();
                    if ( !lastEditorTitle.equals( newTitle ) )
                    {
                        recomputeTitle();
                    }
                }
            }
        }
    };


    // ── A New Ship Joins the Fleet — Bridge Crew Reports for Duty ─────────────
    // When a new capital ship joins Ackbar's fleet its bridge crew comes aboard,
    // takes the configuration manual from fleet command, and stands ready to act.
    // Our constructor does the same: we accept Eclipse's configurer handle and
    // pass it to the parent so it's available to all our lifecycle methods.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the window advisor with the given window configurer.
     * Eclipse passes us this configurer handle so we can later call methods like
     * {@code setInitialSize} and {@code setShowCoolBar} during {@link #preWindowOpen()}.
     *
     * @param configurer  Eclipse's per-window configuration object — we store it
     *                    via the parent class for use in subsequent lifecycle methods.
     */
    public ApplicationWorkbenchWindowAdvisor( IWorkbenchWindowConfigurer configurer )
    {
        super( configurer );
    }


    // ── Ackbar Assigns the Tactical Officer ───────────────────────────────────
    // Ackbar hands the menu-and-toolbar briefing document to the tactical officer
    // who is responsible for populating all the weapons controls and comm channels.
    // createActionBarAdvisor() instantiates our ApplicationActionBarAdvisor which
    // handles exactly that: building every menu and toolbar button on the window.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action bar advisor that builds the window's menus and toolbars.
     * Eclipse calls this early in the window's lifecycle, before the window is
     * shown, so we have a chance to register all actions and populate the menu bar.
     *
     * @param configurer  Eclipse's action bar configuration handle — passed through
     *                    to our {@link ApplicationActionBarAdvisor}.
     * @return            a new {@link ApplicationActionBarAdvisor} for this window.
     */
    public ActionBarAdvisor createActionBarAdvisor( IActionBarConfigurer configurer )
    {
        return new ApplicationActionBarAdvisor( configurer );
    }


    // ── Home One's Bridge Is Configured Before Battle Stations ───────────────
    // Before the battle begins the bridge crew configures every display: tactical
    // readout size (950×708), the cool bar of mission controls, the perspective
    // selector, and the progress indicator for long-running operations.
    // preWindowOpen() does all of that, then hooks the listeners that will keep
    // the OS title bar in sync with whatever the user is doing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the workbench window before its SWT shell is created.
     * We set the initial window size, show the cool bar (the main toolbar area),
     * hide the status line (we don't use it), show the perspective bar, and
     * enable the progress indicator.  We also hook up the title-update listeners
     * so the window title reflects the active editor and perspective.
     */
    public void preWindowOpen()
    {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize( new Point( 950, 708 ) );
        configurer.setShowCoolBar( true );
        configurer.setShowStatusLine( false );
        configurer.setShowPerspectiveBar( true );
        configurer.setShowProgressIndicator( true );
        // setShowFastViewBars removed in Eclipse 4 — fast view bars no longer exist

        // hopk up the listeners to update the window title
        // adapted from org.eclipse.ui.internal.ide.application.IDEWorkbenchWindowAdvisor
        // http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.ui.ide.application/src/org/eclipse/ui/internal/ide/application/IDEWorkbenchWindowAdvisor.java?view=markup
        hookTitleUpdateListeners( configurer );
    }


    // ── Bridge Crew Monitors All Channels for Status Changes ──────────────────
    // The bridge crew on Home One monitors every tactical channel: page status,
    // perspective changes, and which ship is currently in focus.  Whenever
    // something changes they update the main tactical display.
    // hookTitleUpdateListeners() does exactly that — it attaches listeners to
    // pages, perspectives, and parts so we can recompute the window title when
    // anything relevant changes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the listeners that keep the window title bar up to date.
     * We listen for page activation/closure, perspective switches, and editor
     * activation/hiding — any of these can change what should appear in the title.
     * This approach is adapted from the Eclipse IDE workbench advisor.
     *
     * @param configurer  the window configurer — we use it to get the window
     *                    object so we can attach our listeners to it.
     */
    private void hookTitleUpdateListeners( IWorkbenchWindowConfigurer configurer )
    {
        configurer.getWindow().addPageListener( new IPageListener()
        {
            public void pageActivated( IWorkbenchPage page )
            {
                updateTitle( false );
            }


            public void pageClosed( IWorkbenchPage page )
            {
                updateTitle( false );
            }


            public void pageOpened( IWorkbenchPage page )
            {
                // do nothing
            }
        } );
        configurer.getWindow().addPerspectiveListener( new PerspectiveAdapter()
        {
            public void perspectiveActivated( IWorkbenchPage page, IPerspectiveDescriptor perspective )
            {
                updateTitle( false );
            }


            public void perspectiveSavedAs( IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
                IPerspectiveDescriptor newPerspective )
            {
                updateTitle( false );
            }


            public void perspectiveDeactivated( IWorkbenchPage page, IPerspectiveDescriptor perspective )
            {
                updateTitle( false );
            }
        } );
        configurer.getWindow().getPartService().addPartListener( new IPartListener2()
        {
            public void partActivated( IWorkbenchPartReference ref )
            {
                if ( ref instanceof IEditorReference )
                {
                    updateTitle( false );
                }
            }


            public void partBroughtToTop( IWorkbenchPartReference ref )
            {
                if ( ref instanceof IEditorReference )
                {
                    updateTitle( false );
                }
            }


            public void partClosed( IWorkbenchPartReference ref )
            {
                updateTitle( false );
            }


            public void partDeactivated( IWorkbenchPartReference ref )
            {
                // do nothing
            }


            public void partOpened( IWorkbenchPartReference ref )
            {
                // do nothing
            }


            public void partHidden( IWorkbenchPartReference ref )
            {
                if ( ref.getPart( false ) == lastActiveEditor && lastActiveEditor != null )
                {
                    updateTitle( true );
                }
            }


            public void partVisible( IWorkbenchPartReference ref )
            {
                if ( ref.getPart( false ) == lastActiveEditor && lastActiveEditor != null )
                {
                    updateTitle( false );
                }
            }


            public void partInputChanged( IWorkbenchPartReference ref )
            {
                // do nothing
            }
        } );

    }


    // ── The Navigator Plots the Current Readout String ───────────────────────
    // Home One's navigator combines ship name, current sector, and active
    // engagement into a single status string for the main tactical display.
    // computeTitle() builds that string: product name, active perspective label,
    // and the active editor's tool-tip joined with dashes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the window title string from the current workbench state.
     * The title has the format: {@code [editorPath - ] [perspectiveLabel - ] ProductName}.
     * We use the editor's tool-tip text (which shows the full LDAP DN for an
     * entry editor) rather than the short title, because it's more informative.
     *
     * @return  the computed title string — never {@code null}, may be empty.
     */
    private String computeTitle()
    {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        IWorkbenchPage currentPage = configurer.getWindow().getActivePage();
        IEditorPart activeEditor = null;
        if ( currentPage != null )
        {
            activeEditor = lastActiveEditor;
        }

        String title = null;
        IProduct product = Platform.getProduct();
        if ( product != null )
        {
            title = product.getName();
        }
        if ( title == null )
        {
            title = ""; //$NON-NLS-1$
        }

        if ( currentPage != null )
        {
            if ( activeEditor != null )
            {
                lastEditorTitle = activeEditor.getTitleToolTip();
                title = NLS.bind( "{0} - {1}", lastEditorTitle, title ); //$NON-NLS-1$
            }
            IPerspectiveDescriptor persp = currentPage.getPerspective();
            String label = ""; //$NON-NLS-1$
            if ( persp != null )
            {
                label = persp.getLabel();
            }
            IAdaptable input = currentPage.getInput();
            if ( input != null )
            {
                label = currentPage.getLabel();
            }
            if ( label != null && !label.equals( "" ) ) { //$NON-NLS-1$
                title = NLS.bind( "{0} - {1}", label, title ); //$NON-NLS-1$
            }
        }

        return title;
    }


    // ── Navigator Updates the Tactical Display if It Has Changed ─────────────
    // The navigator on Home One only refreshes the main tactical screen when the
    // situation actually changes — constant redraws would be distracting.
    // recomputeTitle() applies the same logic: compare old and new title strings
    // and only push the new one to the OS window title if they differ.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Recomputes the window title and applies it only if it has changed.
     * We avoid setting the title to the same string it already has because
     * some OS window managers flicker or re-sort the taskbar on every title set.
     */
    private void recomputeTitle()
    {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        String oldTitle = configurer.getTitle();
        String newTitle = computeTitle();
        if ( !newTitle.equals( oldTitle ) )
        {
            configurer.setTitle( newTitle );
        }
    }


    // ── Bridge Crew Detects a Status Change — Tactical Display Refresh ────────
    // When a new engagement begins or a ship breaks off, the bridge crew on
    // Home One checks whether the tactical picture has genuinely changed before
    // updating the main display — avoiding unnecessary redraws.
    // updateTitle() does the same: it detects whether anything meaningful has
    // changed (editor, page, perspective, or input) and calls recomputeTitle()
    // only when needed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the workbench state has changed and updates the title if so.
     * We track the last-known editor, page, perspective, and page input — if none
     * of those have changed we skip the recomputation entirely.  If the editor is
     * hidden (e.g. another part is on top) we treat it as "no active editor" for
     * title purposes by nulling the active editor reference.
     *
     * @param editorHidden  {@code true} when this call is triggered because the
     *                      active editor became hidden (another part was brought to
     *                      the top) — we should not include it in the title in that case.
     */
    private void updateTitle( boolean editorHidden )
    {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        IWorkbenchWindow window = configurer.getWindow();
        IEditorPart activeEditor = null;
        IWorkbenchPage currentPage = window.getActivePage();
        IPerspectiveDescriptor persp = null;
        IAdaptable input = null;

        if ( currentPage != null )
        {
            activeEditor = currentPage.getActiveEditor();
            persp = currentPage.getPerspective();
            input = currentPage.getInput();
        }

        if ( editorHidden )
        {
            activeEditor = null;
        }

        // Nothing to do if the editor hasn't changed
        if ( activeEditor == lastActiveEditor && currentPage == lastActivePage && persp == lastPerspective
            && input == lastInput )
        {
            return;
        }

        if ( lastActiveEditor != null )
        {
            lastActiveEditor.removePropertyListener( editorPropertyListener );
        }

        lastActiveEditor = activeEditor;
        lastActivePage = currentPage;
        lastPerspective = persp;
        lastInput = input;

        if ( activeEditor != null )
        {
            activeEditor.addPropertyListener( editorPropertyListener );
        }

        recomputeTitle();
    }
}
