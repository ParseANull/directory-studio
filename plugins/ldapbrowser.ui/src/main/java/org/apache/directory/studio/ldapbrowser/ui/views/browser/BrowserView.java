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

package org.apache.directory.studio.ldapbrowser.ui.views.browser;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserConfiguration;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: BrowserView — LUKE GAZES AT THE BINARY SUNSET ─────────────────────
// Luke Skywalker steps outside the Lars homestead and stares at the two suns
// setting over Tatooine's desert — a vast, branching landscape stretching to
// every horizon, full of depth he hasn't explored yet.
// The browser view is that landscape: a navigable tree of LDAP entries,
// searches, and bookmarks, where the user can see everything the directory
// server contains and explore it in any direction.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main LDAP browser view — an Eclipse ViewPart that shows the DIT (Directory
 * Information Tree), saved searches, and bookmarks in a navigable tree widget.
 * This is the central navigation panel of Directory Studio: everything the user
 * does starts here — expanding entries, triggering editors, running searches.
 * Think of Luke gazing at the twin suns: a vast landscape laid out before him,
 * ready to explore.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserView extends ViewPart
{

    /** The configuration */
    private BrowserConfiguration configuration;

    /** The listeners */
    private BrowserViewUniversalListener universalListener;

    /** The actions */
    private BrowserViewActionGroup actionGroup;

    /** The browser's main widget */
    private BrowserWidget mainWidget;


    // private DragAction dragAction;
    // private DropAction dropAction;

    // ── Luke Knows Where Home Is ─────────────────────────────────────────────────
    // Even standing out on the dune, Luke knows exactly which homestead he's
    // looking out from — he has a fixed identity in the landscape.
    // This method returns the stable Eclipse view ID used to look up this
    // view from other parts of the application.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse view ID for the browser view.
     * Other parts of the application use this ID to programmatically open,
     * find, or reference the browser view without hardcoding a string literal.
     *
     * @return  the view ID constant from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.VIEW_BROWSER_VIEW;
    }


    // ── Luke Takes His First Step Outside ────────────────────────────────────────
    // Luke walks out of the homestead — no luggage, no equipment yet, just
    // present and ready. The view is constructed but not yet initialised.
    // We have a no-arg constructor because Eclipse's extension registry
    // instantiates view parts reflectively without arguments.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, uninitialised BrowserView instance.
     * Eclipse's plugin framework calls this via reflection before invoking
     * {@link #createPartControl(Composite)} — so don't do real work here.
     */
    public BrowserView()
    {
    }


    // ── Luke Returns His Gaze to the Homestead ───────────────────────────────────
    // After staring at the sunset, Luke turns back inside, glances around the
    // workshop, and focuses on the task at hand — present, attentive.
    // We direct keyboard focus to the tree widget so the user can immediately
    // start navigating with arrow keys.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Gives keyboard focus to the browser tree so the user can navigate
     * with arrow keys right away after clicking the view tab.
     */
    @Override
    public void setFocus()
    {
        mainWidget.getViewer().getControl().setFocus();
    }


    // ── The Twin Suns Set and Luke Goes Back Inside ───────────────────────────────
    // The suns dip below the horizon, the moment passes, and Luke goes indoors —
    // the landscape is still there but Luke's connection to it is released.
    // We tear down all listeners, actions, and widgets in the right order so
    // there are no memory leaks when the view is closed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Cleans up all resources held by this view when it is closed.
     * We null out fields after disposal so any stray references to this view
     * don't accidentally keep the heavy widget tree alive.
     * The null-check on {@code configuration} acts as a "already disposed" guard.
     */
    @Override
    public void dispose()
    {
        if ( configuration != null )
        {
            actionGroup.dispose();
            actionGroup = null;
            universalListener.dispose();
            universalListener = null;
            configuration.dispose();
            configuration = null;
            mainWidget.dispose();
            mainWidget = null;
            getSite().setSelectionProvider( null );
        }

        super.dispose();
    }


    // ── Luke Sees the Full Landscape for the First Time ──────────────────────────
    // Luke steps out and the view snaps into focus: he sees the desert, the
    // horizon, the twin suns — everything assembled at once, ready to explore.
    // We build the SWT composite, configure the browser widget, wire up all
    // actions and listeners, and register the viewer as the selection provider
    // so the rest of Eclipse reacts to what the user selects here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates and lays out all SWT widgets for the browser view, then wires up
     * actions, context menus, and event listeners.
     * This is where everything the user sees gets assembled — the tree widget,
     * its toolbar, its context menu, and the listener that reacts to selections.
     * Called once by Eclipse after the view is instantiated; don't call manually.
     *
     * @param parent  the parent SWT composite provided by the Eclipse workbench.
     */
    @Override
    public void createPartControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout( layout );

        PlatformUI.getWorkbench().getHelpSystem().setHelp( composite,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_browser_view" ); //$NON-NLS-1$ //$NON-NLS-2$

        // create configuration
        configuration = new BrowserConfiguration();

        // create main widget
        mainWidget = new BrowserWidget( configuration, getViewSite().getActionBars() );
        mainWidget.createWidget( composite );
        mainWidget.setInput( getSite() );

        // create actions and context menu (and register global actions)
        actionGroup = new BrowserViewActionGroup( this );
        actionGroup.fillToolBar( mainWidget.getToolBarManager() );
        actionGroup.fillMenu( mainWidget.getMenuManager() );
        actionGroup.enableGlobalActionHandlers( getViewSite().getActionBars() );
        IMenuManager contextMenuManager = mainWidget.getContextMenuManager();
        actionGroup.fillContextMenu( contextMenuManager );
        getSite().registerContextMenu( ( MenuManager ) contextMenuManager, mainWidget.getViewer() );

        // create the listener
        getSite().setSelectionProvider( mainWidget.getViewer() );
        universalListener = new BrowserViewUniversalListener( this );

        // DND support
        // int ops = DND.DROP_COPY | DND.DROP_MOVE;
        // viewer.addDragSupport(ops, new Transfer[]{TextTransfer.getInstance(),
        // BrowserTransfer.getInstance()}, this.dragAction);
        // viewer.addDropSupport(ops, new
        // Transfer[]{BrowserTransfer.getInstance()}, this.dropAction);
    }


    // ── Luke Walks Toward What He Sees ───────────────────────────────────────────
    // When Luke spots something interesting on the horizon — a farm, a speeder
    // track, a distant shape — he walks toward it, bringing it into focus.
    // We expand the tree path to the given object and set the selection so
    // the user sees exactly the item they need, no matter how deep in the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Navigates the browser tree to select the given object, expanding the
     * path to it if necessary.
     * This is used by "link with editor" and "show in browser" to keep the
     * tree in sync with whatever the user is looking at elsewhere.
     * The object must be one of: {@link IEntry}, {@link ISearch},
     * {@link ISearchResult}, or {@link IBookmark} — anything else is silently ignored.
     * For entries, we walk the parent chain and force-expand each ancestor so
     * the JFace tree model is aware of them before we try to select.
     *
     * @param obj  the LDAP model object to select in the tree.
     */
    public void select( Object obj )
    {
        Object objectToSelect = null;

        if ( obj instanceof ISearch )
        {
            ISearch search = ( ISearch ) obj;

            universalListener.setInput( search.getBrowserConnection() );

            mainWidget.getViewer().expandToLevel( search, 0 );

            objectToSelect = search;
        }
        if ( obj instanceof ISearchResult )
        {
            ISearchResult searchResult = ( ISearchResult ) obj;
            ISearch search = searchResult.getSearch();

            universalListener.setInput( search.getBrowserConnection() );

            mainWidget.getViewer().expandToLevel( search, 1 );

            objectToSelect = searchResult;
        }
        if ( obj instanceof IBookmark )
        {
            IBookmark bookmark = ( IBookmark ) obj;

            universalListener.setInput( bookmark.getBrowserConnection() );

            mainWidget.getViewer().expandToLevel( bookmark, 0 );

            objectToSelect = bookmark;
        }
        if ( obj instanceof IEntry )
        {
            IEntry entry = ( IEntry ) obj;

            universalListener.setInput( entry.getBrowserConnection() );

            List<IEntry> entryList = new ArrayList<IEntry>();
            IEntry tempEntry = entry;
            while ( tempEntry.getParententry() != null )
            {
                IEntry parentEntry = tempEntry.getParententry();
                entryList.add( 0, tempEntry );
                tempEntry = parentEntry;
            }

            for ( IEntry childEntry : entryList )
            {
                IEntry parentEntry = childEntry.getParententry();
                if ( !parentEntry.isChildrenInitialized() )
                {
                    parentEntry.setChildrenInitialized( true );
                    parentEntry.setHasMoreChildren( true );
                }
                if ( !Arrays.asList( parentEntry.getChildren() ).contains( childEntry ) )
                {
                    parentEntry.addChild( childEntry );
                }

                // force refresh of each parent, beginning from the root
                // if the entry to select was lazy initialized then the
                // JFace model has no knowledge about it so we must
                // refresh the JFace model from the browser model
                mainWidget.getViewer().refresh( parentEntry, true );
            }

            objectToSelect = entry;
        }

        if ( objectToSelect != null )
        {
            mainWidget.getViewer().reveal( objectToSelect );
            mainWidget.getViewer().refresh( objectToSelect, true );
            mainWidget.getViewer().setSelection( new StructuredSelection( objectToSelect ), true );
        }
    }


    // ── The Landscape Meets the Map ──────────────────────────────────────────────
    // Luke's view of Tatooine is also navigable from a map — you can pinpoint
    // a location on the map and Luke knows where it is on the ground. The
    // IShowInTarget adapter is that map-to-ground bridge: another view (e.g.
    // the entry editor) can ask us to "show" a particular entry, and we navigate.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns an {@link IShowInTarget} adapter so the browser view can participate
     * in Eclipse's "Show In" navigation gesture.
     * When the user right-clicks an attribute value in the entry editor and chooses
     * "Show In &gt; Browser," Eclipse calls this adapter to tell us what to select.
     * We resolve the entry from the connection's cache (bypassing any stale clones)
     * before calling {@link #select(Object)}.
     *
     * @param required  the adapter interface being requested.
     * @return          an {@link IShowInTarget} if requested, otherwise {@code null}.
     */
    @Override
    public Object getAdapter( Class required )
    {
        if ( IShowInTarget.class.equals( required ) )
        {
            return new IShowInTarget()
            {
                public boolean show( ShowInContext context )
                {
                    StructuredSelection selection = ( StructuredSelection ) context.getSelection();
                    Object obj = selection.getFirstElement();
                    if ( obj instanceof IValue )
                    {
                        IValue value = ( IValue ) obj;
                        IEntry entry = value.getAttribute().getEntry();
                        // The entry may be a clone, lookup original entry from entry cache.
                        // The result may be null, in that case the selection won't change.
                        entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );
                        select( entry );
                    }
                    else if ( obj instanceof IAttribute )
                    {
                        IAttribute attribute = ( IAttribute ) obj;
                        IEntry entry = attribute.getEntry();
                        // The entry may be a clone, lookup original entry from entry cache.
                        // The result may be null, in that case the selection won't change.
                        entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );
                        select( entry );
                    }
                    else if ( obj instanceof ISearchResult )
                    {
                        ISearchResult sr = ( ISearchResult ) obj;
                        ISearch search = sr.getSearch();
                        // The search may be a clone, lookup original search from search manager.
                        // The result may be null, in that case the selection won't change.
                        search = search.getBrowserConnection().getSearchManager().getSearch( search.getName() );
                        select( search );
                    }
                    return true;
                }
            };
        }

        return null;
    }


    // ── Luke's Companions Wait for His Signal ────────────────────────────────────
    // Han, Leia, Chewie — they all watch Luke and follow his lead; they need
    // to be able to reach him through a known reference.
    // The universal listener and the action group both need access to the view's
    // action group so they can call activate/deactivate handlers.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the action group that manages all actions in this browser view.
     * Used by the universal listener to activate and deactivate global action
     * handlers when the view gains or loses focus.
     *
     * @return  the {@link BrowserViewActionGroup} for this view.
     */
    public BrowserViewActionGroup getActionGroup()
    {
        return actionGroup;
    }


    // ── Luke Checks His Gear ─────────────────────────────────────────────────────
    // Before heading out, Luke checks his equipment — he needs his landspeeder
    // configured correctly to navigate Tatooine's terrain.
    // The configuration object controls how the browser tree sorts, filters, and
    // displays entries; other classes need it to adjust the view's behaviour.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser widget's configuration object.
     * The configuration controls sorting, filtering, and rendering preferences
     * for the tree. The action group uses it to connect actions to the correct
     * preference keys.
     *
     * @return  the {@link BrowserConfiguration} for the main widget.
     */
    public BrowserConfiguration getConfiguration()
    {
        return configuration;
    }


    // ── Luke's View of the Landscape ─────────────────────────────────────────────
    // The main widget is the actual physical vantage point — the window frame
    // through which Luke sees the binary sunset. Everything else just references
    // or controls this central widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the main browser widget containing the tree viewer, toolbar,
     * and quick-search bar.
     * Most other classes in this package use this to get direct access to the
     * tree viewer, the context menu, or the toolbar manager.
     *
     * @return  the {@link BrowserWidget} that is the root of this view's UI.
     */
    public BrowserWidget getMainWidget()
    {
        return mainWidget;
    }


    // ── Luke's Companion Who Watches the Horizon ─────────────────────────────────
    // While Luke looks at the sunset, C-3PO keeps watch — listening for any
    // change in the situation and notifying Luke when something shifts.
    // The universal listener is exactly that: it monitors all the event buses
    // and keeps the tree in sync with the underlying model.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the universal listener that keeps this view in sync with
     * the LDAP model and with the active connection.
     * Used by the action group to set the current connection input when
     * the user selects a different connection.
     *
     * @return  the {@link BrowserViewUniversalListener} for this view.
     */
    public BrowserViewUniversalListener getUniversalListener()
    {
        return universalListener;
    }

}
