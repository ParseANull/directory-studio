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

package org.apache.directory.studio.entryeditors;


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgressAdapter;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation.State;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenEntryEditorRunnable — CLONE TROOPERS EXECUTING ORDER 66 ───────
// When Palpatine says "Execute Order 66," clone troopers spring into action:
// each squad receives its target Jedi, confirms the target is in range, selects
// the right weapon, and fires — all in one coordinated burst.
// This runnable does the same thing for editors: given an entry/search/bookmark,
// it identifies the target, ensures attributes are loaded, picks the right editor
// extension, builds the input, and fires open the editor on the UI thread.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background job runnable that opens the correct entry editor for a given
 * LDAP entry, search result, or bookmark.
 * We run in a StudioBrowserJob so attribute initialisation can happen off the UI
 * thread; the actual {@code openEditor()} call is then dispatched back to the
 * UI thread via {@link Display#syncExec}.
 * Think of the clone troopers: the planning (which target, which weapon) happens
 * in the field; the final trigger pull happens at the target's location.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEntryEditorRunnable extends StudioConnectionRunnableWithProgressAdapter implements
    StudioConnectionBulkRunnableWithProgress
{
    /** The entries */
    private IEntry[] entries;

    /** The entries found in a search operation */
    private ISearchResult[] searchResults;

    /** The bookmarked elements */
    private IBookmark[] bookmarks;

    /** The extensions, if any */
    private EntryEditorExtension extension;


    // ── The Clone Trooper Squad Receives Its Briefing ───────────────────────────
    // The order arrives: here is your target (entry, search result, or bookmark),
    // here is the weapon to use (extension), and here are your squad mates.
    // We record all that information here so {@code run()} can execute without
    // needing to ask questions.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new runnable that will open an entry editor when executed.
     * Exactly one of {@code entries}, {@code searchResults}, or {@code bookmarks}
     * should have a single element; the others should be empty arrays.
     * If {@code extension} is {@code null} we will pick the best available editor
     * automatically during {@link #run}.
     *
     * @param extension     the editor extension to use, or {@code null} to auto-select
     * @param entries       array of entries to open (usually length 0 or 1)
     * @param searchResults array of search results to open (usually length 0 or 1)
     * @param bookmarks     array of bookmarks to open (usually length 0 or 1)
     */
    public OpenEntryEditorRunnable( EntryEditorExtension extension, IEntry[] entries, ISearchResult[] searchResults,
        IBookmark[] bookmarks )
    {
        super();
        this.extension = extension;
        this.entries = entries;
        this.searchResults = searchResults;
        this.bookmarks = bookmarks;
    }


    // ── The Squad Leader Announces the Operation Code Name ──────────────────────
    // Every Order 66 squad has a designation announced on the comms channel so the
    // progress system knows which operation is running.
    // We return a localised "Open Entry Editor" label for the job progress UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this job, shown in the Eclipse progress view.
     *
     * @return  a localised "Open Entry Editor" label string
     */
    public String getName()
    {
        return Messages.getString( "OpenEntryEditorRunnable.OpenEntryEditor" ); //$NON-NLS-1$
    }


    // ── Identifying Which Jedi Is the Target (Lock Acquired) ────────────────────
    // Before firing, the clone trooper locks onto the specific Jedi — the object
    // that must be exclusively accessed during the operation.
    // We return the underlying IEntry so the job framework can hold the right lock.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be locked during this job's execution.
     * We lock the specific entry so no other job modifies it while we're loading
     * attributes and opening the editor.
     *
     * @return  a one-element array with the target entry, or an empty array if nothing to lock
     */
    public Object[] getLockedObjects()
    {
        if ( entries.length == 1 )
        {
            return new Object[]
                { entries[0] };
        }
        else if ( searchResults.length == 1 )
        {
            return new Object[]
                { searchResults[0].getEntry() };
        }
        else if ( bookmarks.length == 1 )
        {
            return new Object[]
                { bookmarks[0].getEntry() };
        }
        else
        {
            return new Object[0];
        }
    }


    // ── Confirming Which Sector the Target Is In ─────────────────────────────────
    // The clone squad checks which planet the Jedi is on — the connection tells the
    // job framework which server we're talking to so it can route correctly.
    // We return the {@link Connection} for the target entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connections involved in this operation.
     * The job framework uses this to associate the job with the right connection
     * in the progress and error-reporting infrastructure.
     *
     * @return  a one-element array with the target entry's connection, or empty if none found
     */
    public Connection[] getConnections()
    {
        if ( entries.length == 1 )
        {
            return new Connection[]
                { entries[0].getBrowserConnection().getConnection() };
        }
        else if ( searchResults.length == 1 )
        {
            return new Connection[]
                { searchResults[0].getEntry().getBrowserConnection().getConnection() };
        }
        else if ( bookmarks.length == 1 )
        {
            return new Connection[]
                { bookmarks[0].getEntry().getBrowserConnection().getConnection() };
        }
        else
        {
            return new Connection[0];
        }
    }


    // ── The Clone Trooper Executes Order 66: Find, Prepare, Fire ────────────────
    // Order received. Step 1: identify the Jedi (resolve the entry). Step 2: confirm
    // they're in range (ensure attributes are loaded). Step 3: select the right weapon
    // (pick the editor extension). Step 4: pull the trigger (open the editor on the
    // UI thread via syncExec). Execute.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs the open-editor sequence on a background thread.
     * We resolve which entry to open, ensure its attributes are initialised (blocking
     * briefly if needed), auto-select an editor extension if none was provided, build
     * the appropriate {@link EntryEditorInput}, and then switch to the UI thread to
     * call {@link org.eclipse.ui.IWorkbenchPage#openEditor}.
     *
     * @param monitor  the progress monitor we report into; we set a task name but don't subdivide work
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.setTaskName( Messages.getString( "OpenEntryEditorRunnable.OpeningEntryEditor" ) ); //$NON-NLS-1$

        // Getting the entry to open
        IEntry entry = null;

        if ( entries.length == 1 )
        {
            entry = entries[0];
        }
        else if ( searchResults.length == 1 )
        {
            entry = searchResults[0].getEntry();
        }
        else if ( bookmarks.length == 1 )
        {
            entry = bookmarks[0].getEntry();
        }

        if ( entry != null )
        {
            if ( entry instanceof IContinuation )
            {
                IContinuation continuation = ( IContinuation ) entry;

                if ( continuation.getState() == State.UNRESOLVED )
                {
                    continuation.resolve();
                }
            }
            else
            {
                // Making sure attributes are initialized
                if ( !entry.isAttributesInitialized() )
                {
                    InitializeAttributesRunnable.initializeAttributes( entry, monitor );
                }
            }
        }

        // If no entry editor was provided, find the correct one
        if ( extension == null )
        {
            // Looking for the correct entry editor
            for ( EntryEditorExtension entryEditorExtension : BrowserUIPlugin.getDefault().getEntryEditorManager()
                .getSortedEntryEditorExtensions() )
            {
                // Verifying that the editor can handle the entry
                if ( entryEditorExtension.getEditorInstance().canHandle( entry ) )
                {
                    extension = entryEditorExtension;
                    break;
                }
            }
        }

        // Getting the editor's ID and creating the proper editor input
        final String editorId = extension.getEditorId();
        final EntryEditorInput editorInput;

        if ( entries.length == 1 )
        {
            editorInput = new EntryEditorInput( entries[0], extension );
        }
        else if ( searchResults.length == 1 )
        {
            editorInput = new EntryEditorInput( searchResults[0], extension );
        }
        else if ( bookmarks.length == 1 )
        {
            editorInput = new EntryEditorInput( bookmarks[0], extension );
        }
        else
        {
            editorInput = new EntryEditorInput( ( IEntry ) null, extension );
        }

        // Opening the editor
        Display.getDefault().syncExec( new Runnable()
        {
            public void run()
            {
                try
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor( editorInput,
                        editorId, false );
                }
                catch ( PartInitException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } );
    }


    // ── The Squad Checks In After the Operation ─────────────────────────────────
    // After Order 66 executes, the clone squad sends a status ping back to command.
    // In our case there's nothing to report — the editor open is a fire-and-forget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called after {@link #run} completes to send any post-execution notifications.
     * We have nothing to notify here — the editor opening is self-contained.
     *
     * @param monitor  the progress monitor (unused in this implementation)
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        // Nothing to notify
    }
}
