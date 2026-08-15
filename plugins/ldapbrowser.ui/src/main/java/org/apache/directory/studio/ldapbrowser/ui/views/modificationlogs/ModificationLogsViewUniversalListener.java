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

package org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs;


import java.io.File;
import java.io.FileReader;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.io.api.LdifModificationLogger;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.views.connection.ConnectionView;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IWorkbenchPart;


// ── CLASS: ModificationLogsViewUniversalListener — OBI-WAN SENSES A DISTURBANCE
// Obi-Wan meditates in the Force, fully open to whatever ripples reach him —
// a connection selected in the fleet, an entry modified on a remote world.
// When he senses it, he reaches out and updates the record: loads the right
// log file, scrolls to the newest entry, keeps the vault display current.
// This listener does exactly that: it watches for connection selections and
// entry-modification events, then reloads the log view accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages all events that drive what the modification logs view displays.
 * It listens for two kinds of signals: connection selection changes (to switch
 * to a different connection's log files) and LDAP entry-modification events
 * (to reload the log after a write operation completes).
 * Think of this as Obi-Wan's Force awareness — every meaningful event in the
 * workbench is sensed and translated into an update to the view's content.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModificationLogsViewUniversalListener implements EntryUpdateListener
{

    /** The modification log view. */
    private ModificationLogsView view;

    /** The current input */
    private ModificationLogsViewInput input;

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
                        ModificationLogsViewInput input = new ModificationLogsViewInput( connection, 0 );
                        setInput( input );
                        scrollToNewest();
                    }
                }
            }
        }
    };


    // ── Constructor: Obi-Wan Opens His Force Senses ───────────────────────────
    // Obi-Wan settles into meditation, opens his awareness to the Force, and
    // registers his presence with the Rebel Alliance's event network.
    // We register on both the EventRegistry (for entry-modification events) and
    // the workbench selection service (for connection-selection changes).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this listener and registers it with the event registry and
     * the workbench selection service.
     * We listen specifically on the connection view's selection so we only
     * react when a connection (not a random selection) changes.
     *
     * <p>For example — Obi-Wan opens himself to Force events across the galaxy:</p>
     * <pre>
     *   EventRegistry.addEntryUpdateListener( this, eventRunner );
     *   selectionService.addPostSelectionListener( ConnectionView.getId(), ... );
     * </pre>
     *
     * @param view  the modification logs view to update when events arrive
     */
    public ModificationLogsViewUniversalListener( ModificationLogsView view )
    {
        this.view = view;
        this.input = null;

        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        view.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener( ConnectionView.getId(),
            connectionSelectionListener );
    }


    // ── dispose: Obi-Wan Releases His Force Connection ────────────────────────
    // When Obi-Wan allows Vader to strike him down, his Force presence fades —
    // he stops sensing events, stops reaching out, and lets go gracefully.
    // We remove ourselves from the selection service and EventRegistry, then
    // null the view reference so no late-arriving events can act on a dead view.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters all event listeners and releases the view reference.
     * After this call, no further events will cause view updates.
     * Guard: if view is already null we skip (handles double-dispose).
     *
     * <p>For example — Obi-Wan releases his presence as Vader's saber falls:</p>
     * <pre>
     *   selectionService.removePostSelectionListener( ConnectionView.getId(), listener );
     *   EventRegistry.removeEntryUpdateListener( this );
     *   view = null; // no more Force connections
     * </pre>
     */
    public void dispose()
    {
        if ( view != null )
        {
            view.getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(
                ConnectionView.getId(), connectionSelectionListener );

            EventRegistry.removeEntryUpdateListener( this );
            view = null;
        }
    }


    // ── refreshInput: Obi-Wan Re-Reads the Disturbance ───────────────────────
    // Obi-Wan senses the disturbance has shifted — he closes his eyes again,
    // re-reads the Force, and updates his understanding of the situation.
    // We null the current input and re-set it, which forces the view to
    // reload the log file from disk and redisplay the content.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Forces a reload of the current log file from disk.
     * We do this by clearing the stored input reference and re-calling
     * {@link #setInput(ModificationLogsViewInput)}, which triggers a file re-read.
     *
     * <p>For example — Obi-Wan re-reads the Force after a disturbance:</p>
     * <pre>
     *   ModificationLogsViewInput newInput = input;
     *   input = null;             // clear cached state
     *   setInput( newInput );     // force full reload from disk
     * </pre>
     */
    void refreshInput()
    {
        ModificationLogsViewInput newInput = input;
        input = null;
        setInput( newInput );
    }


    // ── setInput: Obi-Wan Updates His Picture of the Galaxy ──────────────────
    // Obi-Wan receives new intelligence about where Luke is and what just
    // happened — he updates his internal map of events and ensures the
    // holographic display reflects the current reality.
    // We read the log file at the given index into a string buffer and set
    // it as the document content, then update the action group's state.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Loads the log file identified by the given input object and displays it.
     * We read the file at {@code input.getIndex()} in the connection's rotating
     * log-file array into a StringBuilder and push it to the LDIF editor widget.
     * We guard against loading the same input twice (to avoid flicker) and skip
     * null connections (which have no log files).
     *
     * <p>For example — Obi-Wan consults the holographic galaxy map:</p>
     * <pre>
     *   File logFile = modificationLogger.getFiles( conn.getConnection() )[ input.getIndex() ];
     *   // read logFile into sb
     *   view.getMainWidget().getSourceViewer().getDocument().set( sb.toString() );
     *   view.getActionGroup().setInput( input );
     * </pre>
     *
     * @param input  the new input specifying which connection and which log-file index to display
     */
    void setInput( ModificationLogsViewInput input )
    {
        // only if another connection is selected
        if ( this.input != input && input.getBrowserConnection().getConnection() != null )
        {
            this.input = input;

            LdifModificationLogger modificationLogger = ConnectionCorePlugin.getDefault().getLdifModificationLogger();

            if ( ( input != null ) && ( input.getBrowserConnection() != null )
                && ( input.getBrowserConnection().getConnection() != null ) && ( modificationLogger != null ) )
            {
                // load file %u %g
                StringBuffer sb = new StringBuffer();
                File[] files = modificationLogger.getFiles( input.getBrowserConnection().getConnection() );
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


    // ── entryUpdated: Obi-Wan Senses the Latest Disturbance ──────────────────
    // Somewhere in the galaxy, an entry has just been modified — Obi-Wan feels
    // it immediately through the Force and updates his awareness of what happened.
    // We reload the log and scroll to newest, but skip pure initialization events
    // (attribute/children loads) since those don't produce modification log entries.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to LDAP entry modification events by refreshing the log display.
     * We skip {@link AttributesInitializedEvent} and {@link ChildrenInitializedEvent}
     * because those are read operations that don't generate modification log entries.
     * All other modification events (add, delete, modify) cause a reload and scroll.
     *
     * <p>For example — Obi-Wan feels the disturbance and checks the latest records:</p>
     * <pre>
     *   // attribute-loaded events are ignored — no log entry for those
     *   refreshInput();    // reload from disk
     *   scrollToNewest();  // bring the new entry into view
     * </pre>
     *
     * @param event  the entry modification event from the LDAP browser's event bus
     */
    @Override
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( !( event instanceof AttributesInitializedEvent ) && !( event instanceof ChildrenInitializedEvent ) )
        {
            refreshInput();
            scrollToNewest();
        }
    }


    // ── scrollToOldest: Obi-Wan Rewinds to the Beginning of the Record ───────
    // Obi-Wan wants to read the Imperial record from the very first entry —
    // he scrolls the holographic display back to line zero.
    // We set the source viewer's top index to 0 to show the oldest log entry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Scrolls the LDIF editor to show the oldest (top) log entry.
     * Called by {@link NewerAction} after switching to a newer log file, so
     * we start reading from the oldest entry in that file.
     *
     * <p>For example — Obi-Wan rewinds the holographic record to the beginning:</p>
     * <pre>
     *   view.getMainWidget().getSourceViewer().setTopIndex( 0 );
     * </pre>
     */
    public void scrollToOldest()
    {
        view.getMainWidget().getSourceViewer().setTopIndex( 0 );
    }


    // ── scrollToNewest: Obi-Wan Fast-Forwards to the Latest Entry ────────────
    // Obi-Wan wants to see what just happened — he fast-forwards the holographic
    // record to the most-recent event and pauses there.
    // We find the last LDIF container in the model, compute its line number,
    // and set the source viewer's top index so it's visible.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Scrolls the LDIF editor to show the most-recent (bottom) log entry.
     * We locate the last {@link LdifContainer} in the parsed model, compute its
     * offset, and scroll the viewer to that line minus a small margin so there's
     * context above it. Exceptions are silently swallowed (e.g., when the log is empty).
     *
     * <p>For example — Obi-Wan fast-forwards to the latest Imperial record:</p>
     * <pre>
     *   LdifContainer last = view.getMainWidget().getLdifModel().getLastContainer();
     *   int line = document.getLineOfOffset( last.getOffset() );
     *   viewer.setTopIndex( line - 3 ); // small margin above the entry
     * </pre>
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


    // ── clearInput: Obi-Wan Wipes the Holographic Record ─────────────────────
    // Obi-Wan decides the old records must be purged — he reaches into the
    // data core, invokes the logger's dispose routine, and resets the display
    // to blank. The records are gone; the slate is clean.
    // We call the modification logger's dispose (which deletes the log files),
    // then reset the viewer's scroll position and clear its document text.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Deletes all on-disk modification log files for the current connection
     * and clears the LDIF editor display.
     * We call {@link LdifModificationLogger#dispose(Connection)} which removes
     * the rotating log files, then reset the viewer to an empty document at
     * line zero. Only operates if the connection is non-null.
     *
     * <p>For example — Obi-Wan purges the holographic data core:</p>
     * <pre>
     *   modificationLogger.dispose( connection ); // log files deleted from disk
     *   viewer.setTopIndex( 0 );                  // scroll to top
     *   viewer.getDocument().set( "" );            // blank the display
     * </pre>
     */
    public void clearInput()
    {
        if ( input.getBrowserConnection().getConnection() != null )
        {
            LdifModificationLogger modificationLogger = ConnectionCorePlugin.getDefault().getLdifModificationLogger();
            modificationLogger.dispose( input.getBrowserConnection().getConnection() );
            view.getMainWidget().getSourceViewer().setTopIndex( 0 );
            view.getMainWidget().getSourceViewer().getDocument().set( "" );
        }
    }

}
