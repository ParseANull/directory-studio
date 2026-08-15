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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.ViewFormWidget;
import org.apache.directory.studio.ldapbrowser.common.dialogs.SelectEntryDialog;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;


// ── CLASS: BrowserWidget — The Rebel Tactical Hologram ───────────────────────
// In Return of the Jedi, Mon Mothma and Admiral Ackbar stand before the great
// holographic projector aboard Home One and show the Rebel fleet everything:
// the Death Star, the surrounding systems, the shield generator on Endor — the
// whole structure at once. Pilots can zoom in, navigate around, and get context
// menus of actions. BrowserWidget is that holographic display: it shows the
// full LDAP directory tree (the DIT), saved searches, and bookmarks, all in one
// scrollable, expandable, actionable JFace TreeViewer — reusable in any view
// or dialog that needs to browse an LDAP directory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A reusable Eclipse widget that displays an LDAP connection's directory
 * information tree (DIT), saved searches, and bookmarks in a JFace
 * {@link TreeViewer}.
 * It is used by the main browser view ({@code BrowserView}) and by
 * {@link SelectEntryDialog}, so both share the same tree rendering logic.
 * The widget assembles a quick-search bar, a virtual SWT {@link Tree}, a
 * {@link TreeViewer}, and wires up the sorter, content provider, label provider
 * and context menu all from a single {@link BrowserConfiguration} object.
 * Think of this widget as the holographic projector: the configuration is the
 * power cell, and {@link #setInput(Object)} is Ackbar pressing the big button.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserWidget extends ViewFormWidget
{

    /** The widget's configuration with the content provider, label provider and menu manager */
    private BrowserConfiguration configuration;

    /** The quick search widget. */
    private BrowserQuickSearchWidget quickSearchWidget;

    /** The action bars. */
    private IActionBars actionBars;

    /** The tree widget used by the tree viewer */
    private Tree tree;

    /** The tree viewer. */
    private TreeViewer viewer;


    // ── MON MOTHMA HANDS OVER THE BRIEFING PACKAGE ────────────────────────────
    // Before the hologram can run, Mon Mothma hands the projector crew two
    // things: the briefing configuration (what data to show, how to show it,
    // what the menus contain) and the action bar controls (where the toolbar
    // and menu live in the surrounding window). We store both so that
    // createWidget() and the manager accessors can do their jobs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new browser widget but does not create any SWT controls yet —
     * call {@link #createWidget(Composite)} after construction to build the UI.
     * We store the configuration (which provides the content/label providers,
     * sorter, and context menu) and the action bars (which provide the toolbar
     * and drop-down menu when running inside a real Eclipse view).
     *
     * @param configuration  Everything the widget needs to render the tree:
     *                       content provider, label provider, sorter, preferences,
     *                       and context menu. Must not be {@code null}.
     * @param actionBars     The Eclipse action bars for the enclosing view, or
     *                       {@code null} when the widget runs inside a dialog
     *                       (in which case {@link ViewFormWidget} creates its own
     *                       toolbar and menu).
     */
    public BrowserWidget( BrowserConfiguration configuration, IActionBars actionBars )
    {
        this.configuration = configuration;
        this.actionBars = actionBars;
    }


    // ── ACKBAR ACTIVATES THE HOLOGRAPHIC PROJECTOR ────────────────────────────
    // "It's a trap!" — well, no, actually Ackbar just presses the button and the
    // hologram springs to life. But how he activates it depends on the context:
    // in the main briefing room (a real Eclipse view with action bars) he uses
    // the room's existing control panel; in a smaller side room (a dialog with
    // no action bars) he uses the projector's built-in controls instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates all SWT controls for this widget inside the given parent composite.
     * When running inside a real Eclipse view (action bars present) we skip the
     * {@link ViewFormWidget} chrome and go straight to {@link #createContent};
     * when running inside a dialog (no action bars) we let the superclass wrap
     * everything in a ViewForm with its own toolbar.
     *
     * @param parent  The SWT composite to build our controls inside.
     */
    @Override
    public void createWidget( Composite parent )
    {
        if ( actionBars == null )
        {
            super.createWidget( parent );
        }
        else
        {
            createContent( parent );
        }
    }


    // ── ACKBAR REACHES FOR THE HOLOGRAM'S TOOLBAR ─────────────────────────────
    // The hologram's toolbar is a row of buttons — zoom in, zoom out, sort.
    // In the main briefing room those buttons live on the room's own control
    // panel (the Eclipse action bars). In a side room we fall back to the
    // ViewFormWidget's built-in toolbar strip. Either way, callers get back
    // the same IToolBarManager interface and never know the difference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the toolbar manager that actions should be contributed to.
     * When action bars are available (real Eclipse view) we use them so our
     * buttons appear in the view's toolbar; otherwise we fall back to the
     * {@link ViewFormWidget} toolbar.
     *
     * @return  The active {@link IToolBarManager}, never {@code null}.
     */
    @Override
    public IToolBarManager getToolBarManager()
    {
        if ( actionBars == null )
        {
            return super.getToolBarManager();
        }
        else
        {
            return actionBars.getToolBarManager();
        }
    }


    // ── ACKBAR OPENS THE HOLOGRAM'S DROP-DOWN MENU ────────────────────────────
    // Beside the toolbar there's a pull-down menu — extra options that don't
    // fit on the toolbar. Same story: main briefing room uses the view's menu,
    // side room uses the ViewFormWidget's own drop-down. Same interface, either way.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the view menu manager for contributing menu items to the
     * browser view's drop-down menu (the downward-pointing triangle).
     * Delegates to the Eclipse action bars when available, falling back to the
     * {@link ViewFormWidget} menu otherwise.
     *
     * @return  The active {@link IMenuManager} for the view menu.
     */
    @Override
    public IMenuManager getMenuManager()
    {
        if ( actionBars == null )
        {
            return super.getMenuManager();
        }
        else
        {
            return actionBars.getMenuManager();
        }
    }


    // ── ACKBAR RIGHT-CLICKS ON THE HOLOGRAM ───────────────────────────────────
    // Right-clicking a node in the hologram pops up a context menu: Open, Copy
    // DN, Delete, Rename… When there are no action bars (dialog mode) the
    // ViewFormWidget manages its own context menu. When there are action bars
    // (view mode) the context menu comes from the BrowserConfiguration, which
    // knows which actions are appropriate for the selected element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the context menu manager that appears when the user right-clicks
     * a node in the tree.
     * In view mode we ask the {@link BrowserConfiguration} because it registers
     * the context menu with the Eclipse workbench, enabling plug-in contribution;
     * in dialog mode we fall back to the {@link ViewFormWidget}'s generic menu.
     *
     * @return  The {@link IMenuManager} for the tree's context menu.
     */
    @Override
    public IMenuManager getContextMenuManager()
    {
        if ( actionBars == null )
        {
            return super.getContextMenuManager();
        }
        else
        {
            return configuration.getContextMenuManager( viewer );
        }
    }


    // ── ACKBAR ASSEMBLES THE HOLOGRAPHIC DISPLAY ──────────────────────────────
    // The crew sets up the projector hardware piece by piece: first the quick-
    // search filter panel on top, then the main holographic grid (the SWT Tree),
    // then the JFace TreeViewer layer that makes it navigable, and finally the
    // sorter, content provider, and label provider that actually populate it.
    // Each piece snaps into place in the right order so the hologram renders
    // correctly the first time Ackbar hits the power button.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT and JFace controls: a {@link BrowserQuickSearchWidget}
     * above the tree, a virtual multi-select {@link Tree}, and a
     * {@link TreeViewer} wired up with the sorter, content provider, and label
     * provider from the configuration.
     * "Virtual" ({@code SWT.VIRTUAL}) means the tree asks for item data lazily
     * rather than loading everything upfront — essential for large directories.
     *
     * <p>For example — Ackbar assembles the briefing display:</p>
     * <pre>
     *   quickSearchWidget.createComposite(composite);  // filter bar
     *   tree = new Tree(composite, VIRTUAL | MULTI | SCROLL | BORDER);
     *   viewer = new TreeViewer(tree);
     *   configuration.getSorter().connect(viewer);
     *   viewer.setContentProvider(...);
     *   viewer.setLabelProvider(...);
     * </pre>
     *
     * @param parent  The SWT composite to build the content inside.
     * @return  The root {@link Tree} control (used by the ViewForm framework
     *          to determine the main content widget).
     */
    @Override
    protected Control createContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
        GridLayout gl = new GridLayout( 1, false );
        gl.marginHeight = gl.marginWidth = 0;
        gl.verticalSpacing = gl.horizontalSpacing = 0;
        composite.setLayout( gl );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        quickSearchWidget = new BrowserQuickSearchWidget( this );
        quickSearchWidget.createComposite( composite );

        // create tree widget and viewer
        tree = new Tree( composite, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        tree.setData( "org.eclipse.e4.ui.css.CssClassName", "studio-browser-tree" );
        GridData data = new GridData( GridData.FILL_BOTH );
        data.widthHint = 450;
        data.heightHint = 250;
        tree.setLayoutData( data );
        viewer = new TreeViewer( tree );
        viewer.setUseHashlookup( true );

        // setup sorter, filter and layout
        configuration.getSorter().connect( viewer );
        configuration.getPreferences().connect( viewer );

        // setup providers
        viewer.setContentProvider( configuration.getContentProvider( this ) );
        viewer.setLabelProvider( configuration.getLabelProvider( viewer ) );

        return tree;
    }


    // ── ACKBAR LOADS NEW INTELLIGENCE INTO THE HOLOGRAM ───────────────────────
    // The rebel scouts bring fresh data — a new set of star charts — and Ackbar
    // loads them into the projector. The hologram rebuilds itself around the new
    // input: a different LDAP connection, a different root node, a different world.
    // We call viewer.setInput() and the JFace content provider does the rest.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the data source for the tree viewer.
     * Typically this is an {@code IBrowserConnection} — the root of a particular
     * LDAP connection's directory tree. Changing the input triggers the content
     * provider to rebuild the tree from scratch.
     *
     * @param input  The new input object (usually an {@code IBrowserConnection}).
     */
    public void setInput( Object input )
    {
        viewer.setInput( input );
    }


    // ── ACKBAR DIRECTS ALL EYES TO THE HOLOGRAM ───────────────────────────────
    // "All wings report in." Ackbar wants everyone looking at the briefing
    // display, not their side conversations. We grab the keyboard focus for
    // the tree so hotkeys (expand, collapse, navigate) work immediately after
    // the view is opened or the user switches to it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the tree widget so the user can immediately use
     * arrow keys, Enter, and other keyboard shortcuts to navigate the directory.
     * Call this when the browser view is activated or brought to the front.
     */
    public void setFocus()
    {
        viewer.getTree().setFocus();
    }


    // ── ACKBAR POWERS DOWN THE PROJECTOR ──────────────────────────────────────
    // The briefing is over. Ackbar powers down the hologram in the right order:
    // first the configuration (unhook menus and listeners), then the quick-
    // search widget, then the tree itself. Skipping the order causes crashes
    // (you can't dispose a parent before its children in SWT). We also null
    // everything out so no code can accidentally use the dead references.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all SWT resources held by this widget in a safe order:
     * configuration first (deregisters listeners and menus), then the
     * quick-search widget, then the tree.
     * We guard with a {@code viewer != null} check so calling dispose twice
     * is harmless — Eclipse sometimes does this on shutdown.
     */
    @Override
    public void dispose()
    {
        if ( this.viewer != null )
        {
            this.configuration.dispose();
            this.configuration = null;

            if ( quickSearchWidget != null )
            {
                quickSearchWidget.dispose();
                quickSearchWidget = null;
            }

            this.tree.dispose();
            this.tree = null;
            this.viewer = null;
        }
    }


    // ── ACKBAR PASSES THE SEARCH PANEL TO A COLLEAGUE ────────────────────────
    // Someone from the fleet asks "where's the quick-search panel?" Ackbar
    // points them at the widget sitting above the holographic grid. Other classes
    // (like ShowQuickSearchAction) need this reference to show/hide the panel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the quick-search widget that sits above the browser tree.
     * Other classes, such as {@link ShowQuickSearchAction}, use this to
     * toggle its visibility.
     *
     * @return  The {@link BrowserQuickSearchWidget}, or {@code null} if the
     *          widget has been disposed.
     */
    public BrowserQuickSearchWidget getQuickSearchWidget()
    {
        return quickSearchWidget;
    }


    // ── ACKBAR PASSES THE HOLOGRAM'S CONTROL HANDLE TO A COLLEAGUE ───────────
    // A navigator from another ship needs direct access to the holographic
    // display to set a selection or expand a node programmatically. Ackbar hands
    // them the TreeViewer reference — the live control handle to the display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying JFace {@link TreeViewer} that renders the directory
     * tree.
     * Useful for classes that need to programmatically set the selection, expand
     * specific nodes, or register additional listeners — like
     * {@link BrowserUniversalListener}.
     *
     * @return  The {@link TreeViewer}, or {@code null} if disposed.
     */
    public TreeViewer getViewer()
    {
        return viewer;
    }
}
