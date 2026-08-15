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

package org.apache.directory.studio.ldapbrowser.ui.views.searchlogs;


import java.io.File;
import java.io.FileReader;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.io.api.LdifSearchLogger;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.BrowserConnectionUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.BrowserConnectionUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.views.connection.ConnectionView;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IWorkbenchPart;


// ── CLASS: SearchLogsViewUniversalListener — OBI-WAN SENSES A DISTURBANCE ────
// Obi-Wan sits quietly, but he's aware of everything: a connection changes,
// a search completes, an entry is refreshed — every ripple in the Force reaches
// him. He responds instantly but efficiently: he won't react twice to the same
// disturbance within one second. This listener is Obi-Wan: it subscribes to
// three event buses simultaneously and drives the search logs view whenever
// anything meaningful changes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The universal listener for the search logs view.
 * Subscribes to three Eclipse event sources — entry updates, search updates,
 * and browser-connection updates — and also watches the connection view's
 * selection so the displayed log file tracks whichever connection the user picks.
 * When a relevant event fires, we re-read the log file from disk and update the
 * LDIF viewer, throttled to at most once per second to avoid flooding the UI.
 * Think of Obi-Wan sensing every disturbance in the Force: calm, attentive,
 * and always ready to respond — but wise enough not to overreact.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchLogsViewUniversalListener implements BrowserConnectionUpdateListener, SearchUpdateListener,
    EntryUpdateListener
{

    /** The search log view. */
    private SearchLogsView view;

    /** The current input */
    private SearchLogsViewInput input;

    /** The last refresh timestamp. */
    private long lastRefreshTimestamp;

    /** Listener that listens for selections of connections */
    private INullSelectionListener connectionSelectionListener = new INullSelectionListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation sets the input when another connection was selected.
         */
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            if ( view != null && part != null )
            {
                if ( view.getSite().getWorkbenchWindow() == part.getSite().getWorkbenchWindow() )
                {
                    Connection[] connections = BrowserSelectionUtils.getConnections( selection );
                    if ( connections.length == 1 )
                    {
                        IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                            .getBrowserConnectionById( connections[0].getId() );
                        SearchLogsViewInput input = new SearchLogsViewInput( connection, 0 );
                        setInput( input );
                        scrollToNewest();
                    }
                }
            }
        }
    };


    // ── Obi-Wan Opens Himself to the Force ───────────────────────────────────────
    // Obi-Wan lowers his mental shields and lets the Force flow through him:
    // every event in the galaxy now reaches his awareness.
    // We register with three event buses and attach the connection-selection
    // listener so no relevant event can slip past unnoticed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SearchLogsViewUniversalListener and registers it with all
     * relevant event sources.
     * We subscribe to: entry updates, search updates, browser-connection updates
     * (via the shared EventRegistry), and connection-view selection changes
     * (via the workbench selection service).
     *
     * @param view  the search logs view that this listener drives.
     */
    public SearchLogsViewUniversalListener( SearchLogsView view )
    {
        this.view = view;
        this.input = null;

        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addSearchUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addBrowserConnectionUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        view.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener( ConnectionView.getId(),
            connectionSelectionListener );
    }


    // ── Obi-Wan Returns to the Force ─────────────────────────────────────────────
    // When Obi-Wan's time is done, he releases his connection to the world
    // so others can move freely without his presence interfering.
    // We unregister from all three event buses and remove the selection listener
    // before releasing the view reference.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters from all event sources and releases the view reference.
     * Must be called when the view is disposed to prevent memory leaks from
     * lingering event-bus registrations.
     */
    public void dispose()
    {
        if ( view != null )
        {
            view.getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(
                ConnectionView.getId(), connectionSelectionListener );

            EventRegistry.removeEntryUpdateListener( this );
            EventRegistry.removeSearchUpdateListener( this );
            EventRegistry.removeBrowserConnectionUpdateListener( this );
            view = null;
        }
    }


    // ── Obi-Wan Re-Reads the Same Scroll ─────────────────────────────────────────
    // Obi-Wan picks up the same data-card he was reading and reads it again
    // from the start — maybe something new was appended since he last looked.
    // We null out the current input so setInput() doesn't skip it due to the
    // "only update when input changes" guard, then reload the same file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Forces a re-read of the currently displayed log file.
     * We temporarily clear the stored input reference so that {@link #setInput}
     * doesn't skip the reload (it normally skips if the input hasn't changed).
     * This is the method called by {@link RefreshAction} and by {@link #updateInput()}.
     */
    void refreshInput()
    {
        SearchLogsViewInput newInput = input;
        input = null;
        setInput( newInput );
    }


    // ── Obi-Wan Switches His Attention to a New Scroll ───────────────────────────
    // A new data-card arrives: Obi-Wan sets aside the old one and reads the new
    // one, loading its contents into memory for display.
    // We guard against redundant loads (same input, same connection), then read
    // the log file identified by the input's connection+index pair.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads the log file identified by the given input into the LDIF viewer.
     * We skip the load if the new input is the same object as the current one
     * (it's already displayed). We also skip if the connection is disconnected
     * (null connection means no log files to read).
     * The file is read in 4 KB chunks to handle large log files efficiently.
     * After loading, we push the input to the action group so all buttons
     * update their enabled state.
     *
     * @param input  the new search logs view input identifying connection + file index.
     */
    void setInput( SearchLogsViewInput input )
    {
        // only if another connection is selected
        if ( this.input != input && input.getBrowserConnection().getConnection() != null )
        {
            this.input = input;

            LdifSearchLogger searchLogger = ConnectionCorePlugin.getDefault().getLdifSearchLogger();

            if ( ( input != null ) && ( input.getBrowserConnection() != null )
                && ( input.getBrowserConnection().getConnection() != null ) && ( searchLogger != null ) )
            {
                // load file %u %g
                StringBuffer sb = new StringBuffer();
                File[] files = searchLogger.getFiles( input.getBrowserConnection().getConnection() );
                int i = input.getIndex();
                if ( 0 <= i && i < files.length && files[i] != null && files[i].exists() && files[i].canRead() )
                {
                    try ( FileReader fr = new FileReader( files[i] ) )
                    {
                        char[] cbuf = new char[4096];
                        for ( int length = fr.read( cbuf ); length > 0; length = fr.read( cbuf ) )
                        {
                            sb.append( cbuf, 0, length );
                        }
                    }
                    catch ( Exception e )
                    {
                        sb.append( e.getMessage() );
                    }
                }

                // change input
                view.getMainWidget().getSourceViewer().getDocument().set( sb.toString() );
                view.getActionGroup().setInput( input );
            }
        }
    }


    // ── Obi-Wan Notices an Entry Changed ─────────────────────────────────────────
    // A tremor: an entry's attributes were loaded or its children were expanded.
    // That often means a search just completed and new log data was written.
    // We react only to AttributesInitializedEvent and ChildrenInitializedEvent —
    // both signal the end of a server round-trip that might have logged something.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Reacts to entry modification events by refreshing the log view.
     * We only refresh for {@link AttributesInitializedEvent} and
     * {@link ChildrenInitializedEvent} because those are the events that follow
     * a completed LDAP search — the kind that writes a new log entry.
     *
     * @param event  the entry modification event fired by the browser core.
     */
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( event instanceof AttributesInitializedEvent || event instanceof ChildrenInitializedEvent )
        {
            updateInput();
        }
    }


    // ── Obi-Wan Notices a Search Completed ───────────────────────────────────────
    // The search request went out, the results came back, the log was written.
    // Obi-Wan senses the completion and re-reads the scroll to capture the new entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Reacts to search update events by refreshing the log view.
     * We only refresh when the event detail is {@code SEARCH_PERFORMED} because
     * that's when the connection layer finishes writing the search log entry.
     * Other search events (e.g., search renamed) don't produce new log data.
     *
     * @param searchUpdateEvent  the search update event from the browser core.
     */
    public void searchUpdated( SearchUpdateEvent searchUpdateEvent )
    {
        if ( searchUpdateEvent.getDetail() == SearchUpdateEvent.EventDetail.SEARCH_PERFORMED )
        {
            updateInput();
        }
    }


    // ── Obi-Wan Notices a Connection Changed ─────────────────────────────────────
    // A new connection opened or the schema was refreshed — either event can
    // produce new log data or change which logs are valid.
    // Obi-Wan notices and triggers a re-read.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Reacts to browser-connection update events by refreshing the log view.
     * We refresh for {@code BROWSER_CONNECTION_OPENED} (a new connection is ready)
     * and {@code SCHEMA_UPDATED} (a schema refresh triggers server round-trips
     * that may produce search log entries).
     *
     * @param browserConnectionUpdateEvent  the event from the browser core.
     */
    public void browserConnectionUpdated( BrowserConnectionUpdateEvent browserConnectionUpdateEvent )
    {
        if ( browserConnectionUpdateEvent.getDetail() == BrowserConnectionUpdateEvent.Detail.BROWSER_CONNECTION_OPENED
            || browserConnectionUpdateEvent.getDetail() == BrowserConnectionUpdateEvent.Detail.SCHEMA_UPDATED )
        {
            updateInput();
        }
    }


    // ── Obi-Wan Responds, But Not Too Quickly ────────────────────────────────────
    // Obi-Wan is calm: he doesn't thrash. If multiple disturbances hit within
    // one second, he waits for the storm to pass and responds once.
    // We check the last refresh timestamp; if less than 1000 ms has elapsed
    // since the last refresh, we skip to avoid flooding the UI with repaints.
    // ────────────────────────────────────────────────────────────────────────────
    private void updateInput()
    {
        // performance optimization: refresh only once per second
        long now = System.currentTimeMillis();
        if ( lastRefreshTimestamp + 1000 < now )
        {
            refreshInput();
            scrollToNewest();
            lastRefreshTimestamp = now;
        }
    }


    // ── Obi-Wan Reads From the Beginning ─────────────────────────────────────────
    // Sometimes you need to see where a log file started — Obi-Wan scrolls to
    // the very first line so the oldest entry is at the top of the display.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scrolls the LDIF viewer to the very first line (top of file / oldest entry).
     * Called by {@link NewerAction} after loading a newer file so the user sees
     * the beginning of that file, where the oldest entries in it live.
     */
    public void scrollToOldest()
    {
        view.getMainWidget().getSourceViewer().setTopIndex( 0 );
    }


    // ── Obi-Wan Reads to the End ─────────────────────────────────────────────────
    // Obi-Wan skips to the most recent entry — the last lines of the scroll —
    // because that's where the latest intelligence is.
    // We find the last LDIF container's offset, convert to a line number, and
    // scroll there with a small back-offset so there's context above it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scrolls the LDIF viewer to the most recently written log entry (end of file).
     * We locate the last {@link LdifContainer} in the LDIF model, find its line
     * offset, and scroll to 3 lines before it so the entry is fully visible.
     * Any exception (e.g., empty document, model not yet parsed) is silently
     * swallowed — scrolling failure is not fatal.
     */
    public void scrollToNewest()
    {
        try
        {
            LdifContainer record = view.getMainWidget().getLdifModel().getLastContainer();
            int offset = record.getOffset();
            int line = view.getMainWidget().getSourceViewer().getDocument().getLineOfOffset( offset );
            if ( line > 3 )
                line -= 3;
            view.getMainWidget().getSourceViewer().setTopIndex( line );
        }
        catch ( Exception e )
        {
        }
    }


    // ── Obi-Wan Erases the Records ───────────────────────────────────────────────
    // When the mission is over, the records are destroyed so they can't be used
    // against the Rebellion. We dispose the log files and blank the display.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deletes all search log files for the current connection and clears the viewer.
     * We call the logger's {@code dispose()} method which removes all rotation files,
     * then blank the document and reset the scroll position.
     * Only operates if the current connection is live (null connection = no files).
     */
    public void clearInput()
    {
        if ( input.getBrowserConnection().getConnection() != null )
        {
            LdifSearchLogger searchLogger = ConnectionCorePlugin.getDefault().getLdifSearchLogger();
            searchLogger.dispose( input.getBrowserConnection().getConnection() );
            view.getMainWidget().getSourceViewer().setTopIndex( 0 );
            view.getMainWidget().getSourceViewer().getDocument().set( "" );
        }
    }

}
