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

package org.apache.directory.studio.ldapbrowser.common.actions.proxy;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;


// ── CLASS: BrowserActionProxy — C-3PO TRANSLATES R2'S SIGNALS ────────────────
// C-3PO's job is to translate R2-D2's beeps and whistles into something the
// human crew can actually act on.  R2 speaks in raw binary; the crew needs
// clear language.  C-3PO stands between them: he listens to everything R2 does
// (selection changes, entry updates, connection updates, bookmark changes), and
// each time something shifts, he re-evaluates what it means and updates what
// he's telling the crew (enabled state, label, icon, checked state).
// BrowserActionProxy does exactly the same: it wraps a real BrowserAction (R2),
// listens to all the relevant events, and keeps the Eclipse Action layer (the
// human crew's UI) in sync with R2's current state.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract proxy that bridges a {@link BrowserAction} (the real logic) with
 * Eclipse's {@link Action} framework (the UI representation).
 *
 * <p>Eclipse menus and toolbars work with {@link Action} instances.  Our actual
 * business logic lives in {@link BrowserAction} implementations, which know
 * about LDAP-specific context.  This proxy sits between them: it holds the
 * real action, listens to all the events that could affect its enabled state
 * or appearance (selection changes, entry/search/bookmark/connection updates),
 * and calls {@link #updateAction()} each time to push the current state into
 * the Eclipse Action so menus stay correct.</p>
 *
 * <p>Think of this class as C-3PO: he always knows what R2 is signaling and
 * makes sure the crew (Eclipse UI) hears it in terms they understand.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class BrowserActionProxy extends Action implements ISelectionChangedListener, EntryUpdateListener,
    SearchUpdateListener, BookmarkUpdateListener, ConnectionUpdateListener
{
    /** The real action we delegate to — R2-D2's actual signal. */
    protected BrowserAction action;

    protected ISelectionProvider selectionProvider;


    // ── C-3PO Takes His Position Beside R2 ───────────────────────────────────
    // C-3PO steps into position next to R2, starts listening on all channels,
    // and immediately reports the first status to the crew.
    // We subscribe to all event sources and call updateAction() right away so
    // the UI is correct from the first render.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a proxy for the given action, using the given selection
     * provider and an explicit style (e.g. {@link Action#AS_PUSH_BUTTON} or
     * {@link Action#AS_CHECK_BOX}).
     *
     * <p>We register as a listener on the selection provider and all relevant
     * event registries, then call {@link #updateAction()} immediately so the
     * Eclipse UI reflects the current state of the real action from the start.</p>
     *
     * @param selectionProvider  the source of selection-change events; must not
     *                           be {@code null}
     * @param action             the real browser action to delegate to; must not
     *                           be {@code null}
     * @param style              the Eclipse Action style constant (push, toggle,
     *                           radio, etc.)
     */
    protected BrowserActionProxy( ISelectionProvider selectionProvider, BrowserAction action, int style )
    {
        super( action.getText(), style );
        this.selectionProvider = selectionProvider;
        this.action = action;

        super.setImageDescriptor( action.getImageDescriptor() );
        super.setActionDefinitionId( action.getCommandId() );

        selectionProvider.addSelectionChangedListener( this );
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);

        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionUIPlugin.getDefault().getEventRunner() );
        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addSearchUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addBookmarkUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );

        updateAction();
    }


    // ── C-3PO Defaults to R2's Preferred Style ────────────────────────────────
    // When no explicit style is specified, C-3PO just adopts whatever mode R2
    // is naturally broadcasting in.
    // We delegate to the full constructor using the action's own style.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a proxy using the real action's own style (obtained via
     * {@link BrowserAction#getStyle()}).  Delegates to the three-argument
     * constructor.
     *
     * @param selectionProvider  the source of selection-change events; must not
     *                           be {@code null}
     * @param action             the real browser action to delegate to; must not
     *                           be {@code null}
     */
    protected BrowserActionProxy( ISelectionProvider selectionProvider, BrowserAction action )
    {
        this( selectionProvider, action, action.getStyle() );
    }


    // ── C-3PO Disconnects from All Channels ───────────────────────────────────
    // When C-3PO is powered down for maintenance, he stops translating — he
    // unregisters from all feeds so no stale signals clutter the channel.
    // We remove all our event listeners, call dispose on the real action, and
    // null it so isDisposed() knows we're done.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tears down this proxy: removes all event listeners from every registry
     * and from the selection provider, then disposes the real action.  After
     * this call, {@link #isDisposed()} returns {@code true} and all other
     * methods become no-ops.
     */
    public void dispose()
    {
        ConnectionEventRegistry.removeConnectionUpdateListener( this );
        EventRegistry.removeEntryUpdateListener( this );
        EventRegistry.removeSearchUpdateListener( this );
        EventRegistry.removeBookmarkUpdateListener( this );
        selectionProvider.removeSelectionChangedListener( this );
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removeSelectionListener(this);

        action.dispose();
        action = null;
    }


    // ── C-3PO Checks Whether He's Been Powered Down ──────────────────────────
    // A simple status check: is C-3PO currently active, or has he been shut
    // off?  We know by checking whether the real action reference is still live.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this proxy has been disposed (i.e. the real
     * action has been nulled out).  Use this to guard all event handler methods
     * against post-dispose callbacks.
     *
     * @return {@code true} if disposed; {@code false} if still active
     */
    public boolean isDisposed()
    {
        return action == null;
    }


    // ── R2 Beeps — An Entry Has Changed ──────────────────────────────────────
    // R2 flashes his lights: something changed in the LDAP entry model.
    // C-3PO hears it and immediately re-evaluates what the crew needs to know.
    // We re-run updateAction() to keep the UI in sync.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event system when an LDAP entry is modified.  We re-sync
     * the Eclipse Action state with the real action's current assessment.
     *
     * @param entryModificationEvent  the event describing what changed; we don't
     *                                inspect it — any change means we re-evaluate
     */
    public final void entryUpdated( EntryModificationEvent entryModificationEvent )
    {
        if ( !isDisposed() )
        {
            updateAction();
        }
    }


    // ── R2 Reports a Search Update ────────────────────────────────────────────
    // R2 signals that a search result set has changed — maybe results arrived,
    // maybe they were cleared.  C-3PO relays the new status.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event system when a saved search is updated.  Re-syncs
     * the Action state by calling {@link #updateAction()}.
     *
     * @param searchUpdateEvent  the event describing which search changed
     */
    public void searchUpdated( SearchUpdateEvent searchUpdateEvent )
    {
        if ( !isDisposed() )
        {
            updateAction();
        }
    }


    // ── R2 Reports a Bookmark Update ─────────────────────────────────────────
    // A bookmark was added, removed, or renamed.  R2 beeps; C-3PO translates;
    // the crew gets the updated status.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event system when a bookmark is updated.  Re-syncs
     * the Action state by calling {@link #updateAction()}.
     *
     * @param bookmarkUpdateEvent  the event describing which bookmark changed
     */
    public void bookmarkUpdated( BookmarkUpdateEvent bookmarkUpdateEvent )
    {
        if ( !isDisposed() )
        {
            updateAction();
        }
    }


    // ── R2 Reports a Connection Update ───────────────────────────────────────
    // The connection state changed — connected, disconnected, credentials
    // updated.  C-3PO relays the relevant action status to the crew.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event system when a connection is modified.  Re-syncs
     * the Action state.  Final so subclasses can't accidentally bypass the
     * disposed check.
     *
     * @param connection  the connection that was updated; may be {@code null}
     *                    in some folder events
     */
    public final void connectionUpdated( Connection connection )
    {
        if ( !isDisposed() )
        {
            updateAction();
        }
    }


    // ── R2 Signals: A New Connection Appeared ────────────────────────────────
    // A new ship (connection) has joined the fleet.  C-3PO notes it and checks
    // whether any actions change state as a result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a new connection is added.  Delegates to
     * {@link #connectionUpdated(Connection)} for uniform handling.
     *
     * @param connection  the newly added connection
     */
    public void connectionAdded( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── R2 Signals: A Connection Has Left the Fleet ───────────────────────────
    // A ship is gone from the fleet.  C-3PO re-evaluates what's available.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection is removed.  Delegates to
     * {@link #connectionUpdated(Connection)} for uniform handling.
     *
     * @param connection  the connection that was removed
     */
    public void connectionRemoved( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── R2 Signals: A Connection Has Opened ───────────────────────────────────
    // A channel is now live.  C-3PO checks whether new commands are available.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection is opened (LDAP bind established).  Delegates
     * to {@link #connectionUpdated(Connection)} for uniform handling.
     *
     * @param connection  the connection that was opened
     */
    public void connectionOpened( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── R2 Signals: A Connection Has Closed ───────────────────────────────────
    // A channel went dark.  C-3PO re-evaluates — some actions may now be
    // disabled because the connection is gone.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection is closed.  Delegates to
     * {@link #connectionUpdated(Connection)} for uniform handling.
     *
     * @param connection  the connection that was closed
     */
    public void connectionClosed( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── R2 Signals: A Connection Folder Changed ───────────────────────────────
    // A connection folder was modified — perhaps connections were moved between
    // folders.  C-3PO relays the update using null since no specific connection
    // object is relevant here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection folder is modified.  Delegates to
     * {@link #connectionUpdated(Connection)} with {@code null} because no
     * specific connection object is relevant for folder changes.
     *
     * @param connectionFolder  the folder that was modified
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }


    // ── R2 Signals: A New Connection Folder Appeared ──────────────────────────
    // The fleet gained a new organizational unit.  C-3PO notes it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection folder is added.  Delegates to
     * {@link #connectionUpdated(Connection)} with {@code null}.
     *
     * @param connectionFolder  the folder that was added
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }


    // ── R2 Signals: A Connection Folder Was Removed ───────────────────────────
    // An organizational unit is gone.  C-3PO re-evaluates the landscape.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection folder is removed.  Delegates to
     * {@link #connectionUpdated(Connection)} with {@code null}.
     *
     * @param connectionFolder  the folder that was removed
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }


    // ── C-3PO Tells R2 What's on the Current Screen ──────────────────────────
    // When the cockpit display changes — different data on screen — C-3PO tells
    // R2 what's loaded now, clears the old selection, and re-evaluates.
    // We set the new input on the real action and fire an empty selection
    // event to trigger a full refresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Notifies the proxy that the editor/viewer input has changed.  We push
     * the new input object into the real action and simulate an empty
     * selection event to force a full state re-evaluation.
     *
     * @param input  the new input object (e.g. an {@link
     *               org.apache.directory.studio.ldapbrowser.core.model.IEntry}
     *               or {@link
     *               org.apache.directory.studio.ldapbrowser.core.model.ISearch})
     */
    public void inputChanged( Object input )
    {
        if ( !this.isDisposed() )
        {
            action.setInput( input );
            selectionChanged( new SelectionChangedEvent( selectionProvider, new StructuredSelection() ) );
            // this.updateAction();
        }
    }


    // ── C-3PO Processes a New Selection Signal from R2 ───────────────────────
    // R2 beeps a new set of selected objects.  C-3PO unpacks the signal —
    // "three entries, one bookmark, no searches" — and tells the real action
    // exactly what is selected so it can decide whether it's enabled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the viewer's selection changes.  We use
     * {@link BrowserSelectionUtils} to unpack the raw JFace selection into
     * typed arrays (entries, attributes, values, searches, etc.) and push them
     * into the real action.  Then we call {@link #updateAction()} to re-sync
     * the Eclipse Action's visible state.
     *
     * @param event  the selection-changed event from the viewer
     */
    public void selectionChanged( SelectionChangedEvent event )
    {
        if ( !isDisposed() )
        {
            ISelection selection = event.getSelection();

            action.setSelectedBrowserViewCategories( BrowserSelectionUtils.getBrowserViewCategories( selection ) );
            action.setSelectedEntries( BrowserSelectionUtils.getEntries( selection ) );
            action.setSelectedBrowserEntryPages( BrowserSelectionUtils.getBrowserEntryPages( selection ) );
            action.setSelectedSearchResults( BrowserSelectionUtils.getSearchResults( selection ) );
            action.setSelectedBrowserSearchResultPages( BrowserSelectionUtils.getBrowserSearchResultPages( selection ) );
            action.setSelectedBookmarks( BrowserSelectionUtils.getBookmarks( selection ) );

            action.setSelectedSearches( BrowserSelectionUtils.getSearches( selection ) );

            action.setSelectedAttributes( BrowserSelectionUtils.getAttributes( selection ) );
            action.setSelectedAttributeHierarchies( BrowserSelectionUtils.getAttributeHierarchie( selection ) );
            action.setSelectedValues( BrowserSelectionUtils.getValues( selection ) );

            action.setSelectedProperties( BrowserSelectionUtils.getProperties( selection ) );

            updateAction();
        }
    }


    // ── C-3PO Reads Out R2's Current Status to the Crew ──────────────────────
    // C-3PO polls R2's current state and announces it clearly: "The action is
    // enabled," "the label has changed to X," "the icon is now Y."  The crew
    // (Eclipse UI) updates what they see based on this report.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Synchronizes the Eclipse {@link Action} state with the current state of
     * the real {@link BrowserAction}.  Pushes the current text, enabled state,
     * image descriptor, and checked state into the Eclipse Action layer so
     * menus and toolbars re-render correctly.
     *
     * <p>Called after every event that might have changed the real action's
     * state — selection changes, data updates, connection events.</p>
     */
    public void updateAction()
    {
        if ( !isDisposed() )
        {
            String text = action.getText();
            setText( text );
            setToolTipText( text );
            setEnabled( action.isEnabled() );
            setImageDescriptor( action.getImageDescriptor() );
            setChecked( action.isChecked() );
        }
    }


    // ── C-3PO Passes the Command to R2 for Execution ─────────────────────────
    // The crew says "do it" — C-3PO relays the command to R2, who actually
    // executes the operation.  C-3PO is just the intermediary.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Runs the real action.  We guard against post-dispose calls and then
     * simply delegate to {@link BrowserAction#run()}.
     */
    public void run()
    {
        if ( !isDisposed() )
        {
            action.run();
        }
    }


    // ── C-3PO Hands Over the R2 Unit If Needed ───────────────────────────────
    // Sometimes the crew needs to talk to R2 directly — C-3PO steps aside and
    // introduces him.  We expose the real action for callers that need direct
    // access (e.g. to call type-specific methods).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the real {@link BrowserAction} that this proxy wraps.  Useful
     * when callers need to interact with the action directly — for example, to
     * call subclass-specific methods that aren't on the {@link Action} base.
     *
     * @return the wrapped real action; {@code null} if this proxy has been disposed
     */
    public BrowserAction getAction()
    {
        return action;
    }

}
