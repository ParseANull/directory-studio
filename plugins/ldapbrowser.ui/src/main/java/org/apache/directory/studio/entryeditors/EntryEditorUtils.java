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


import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;


// ── CLASS: EntryEditorUtils — YODA LIFTING LUKE'S X-WING FROM THE SWAMP ─────
// On Dagobah, Luke stares at his sunken X-wing and says "It's too big."
// Yoda closes his eyes, reaches out with the Force, and lifts the whole ship
// clear of the water — transforming something stuck and unusable into something
// ready to fly. This utility class does exactly that: it takes entries that
// aren't ready (uninitialized attributes, wrong wrapper type) and makes them fly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static helper utilities for entry editor plumbing.
 * We use these to ensure entries have their attributes loaded before an editor
 * opens, to safely cast generic {@link IEditorInput} objects into
 * {@link EntryEditorInput}, to build navigation history labels, and to prompt
 * the user before discarding unsaved changes.
 * Think of Yoda lifting Luke's X-wing: the entry looks stuck and unusable,
 * and we're the Force that gets it out of the swamp and into the air.
 */
public class EntryEditorUtils
{

    // ── Yoda Reaches Out and Lifts the X-wing Clear of the Water ────────────────
    // Luke's X-wing is half-submerged; Yoda closes his eyes and initialises it —
    // every system comes online, every attribute becomes readable.
    // We do the same: if the entry's attributes haven't been fetched from the server,
    // we fire an initialisation job right now before the editor tries to display them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Ensures the given entry has its LDAP attributes loaded from the server.
     * If the entry already has attributes, we do nothing — no wasted round-trips.
     * If not, we block the current thread briefly and run an initialise-attributes
     * job so the editor doesn't open on an empty shell.
     *
     * @param entry  the LDAP entry we need to have fully loaded before proceeding
     */
    public static void ensureAttributesInitialized( IEntry entry )
    {
        if ( !entry.isAttributesInitialized() )
        {
            InitializeAttributesRunnable runnable = new InitializeAttributesRunnable( entry );
            RunnableContextRunner.execute( runnable, null, true );
        }
    }


    // ── Recognising the X-wing Under the Swamp Water ─────────────────────────────
    // Yoda doesn't see murky water — he sees an X-wing that just needs lifting.
    // This method looks past the generic IEditorInput wrapper and returns the real
    // EntryEditorInput that is hiding inside, throwing if the cast would be wrong.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Unwraps a generic {@link IEditorInput} into our specific {@link EntryEditorInput}.
     * Every editor in Directory Studio receives an {@link IEditorInput} from Eclipse;
     * this is the safe, loud cast that fails fast with a helpful message if the wrong
     * type was somehow passed in.
     *
     * @param input  the raw Eclipse editor input to cast
     * @return       the same object typed as {@link EntryEditorInput}
     * @throws IllegalArgumentException  if {@code input} is not an {@link EntryEditorInput}
     */
    public static EntryEditorInput getEntryEditorInput( IEditorInput input )
    {
        if ( input instanceof EntryEditorInput )
        {
            EntryEditorInput eei = ( EntryEditorInput ) input;
            return eei;
        }
        else
        {
            throw new IllegalArgumentException( "Expected an EntryEditorInput" ); //$NON-NLS-1$
        }
    }


    // ── Reading the X-wing's Flight Log for the Navigation Computer ─────────────
    // After Yoda lands the X-wing, Luke checks the flight log to see where it's
    // been: "Entry at Tatooine, search result from Mos Eisley, bookmark for Dagobah."
    // We build a human-readable string for Eclipse's navigation history list the same way.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the text shown for this input in Eclipse's editor navigation history.
     * The history drop-down (the back/forward arrows in the editor toolbar) calls
     * this to label each entry. We include the DN, the root DSE label if applicable,
     * and the connection name — so "cn=Luke,dc=rebels — MyServer" is unambiguous.
     *
     * @param input  the editor input to describe; may be {@code null}
     * @return       a localised, human-readable navigation label, or {@code null} if {@code input} is null
     */
    public static String getHistoryNavigationText( EntryEditorInput input )
    {
        if ( input != null )
        {
            if ( input.getEntryInput() != null )
            {
                String connectionName = input.getEntryInput().getBrowserConnection().getConnection() == null ? "" //$NON-NLS-1$
                    : " - " + input.getEntryInput().getBrowserConnection().getConnection().getName(); //$NON-NLS-1$
                if ( input.getEntryInput() instanceof IRootDSE )
                {
                    return Messages.getString( "EntryEditorNavigationLocation.RootDSE" ) + connectionName; //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    return NLS.bind( Messages.getString( "EntryEditorNavigationLocation.Entry" ), //$NON-NLS-1$
                        input.getEntryInput().getDn().getName() ) + connectionName;
                }
            }
            else if ( input.getSearchResultInput() != null )
            {
                String connectionName = input.getSearchResultInput().getEntry().getBrowserConnection().getConnection() == null ? "" //$NON-NLS-1$
                    : " - " + input.getSearchResultInput().getEntry().getBrowserConnection().getConnection().getName(); //$NON-NLS-1$
                if ( input.getSearchResultInput() instanceof IRootDSE )
                {
                    return Messages.getString( "EntryEditorNavigationLocation.RootDSE" ) + connectionName; //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    return NLS.bind( Messages.getString( "EntryEditorNavigationLocation.SearchResult" ), //$NON-NLS-1$
                        input.getSearchResultInput().getDn().getName() ) + connectionName; //$NON-NLS-1$
                }
            }
            else if ( input.getBookmarkInput() != null )
            {
                String connectionName = input.getBookmarkInput().getBrowserConnection().getConnection() == null ? "" //$NON-NLS-1$
                    : " - " + input.getBookmarkInput().getBrowserConnection().getConnection().getName(); //$NON-NLS-1$
                if ( input.getBookmarkInput() instanceof IRootDSE )
                {
                    return Messages.getString( "EntryEditorNavigationLocation.RootDSE" ) + connectionName; //$NON-NLS-1$
                }
                else
                {
                    return NLS.bind( Messages.getString( "EntryEditorNavigationLocation.Bookmark" ), //$NON-NLS-1$
                        input.getBookmarkInput().getDn().getName() ) + connectionName;
                }
            }
            else
            {
                return Messages.getString( "EntryEditorUtils.NoEntrySelected" ); //$NON-NLS-1$
            }
        }

        return null;
    }


    // ── Yoda Pauses Mid-Lift and Asks "Ready, are you?" ─────────────────────────
    // Just before the X-wing clears the water, Yoda turns to Luke: "Your old ship —
    // modifications pending. Save them before I set her down elsewhere?"
    // We do the same: when the editor is about to switch entries, we ask the user
    // whether to save the current working-copy changes first.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shows a "Save changes?" dialog and optionally saves before the editor switches input.
     * Call this inside {@code setInput()} when the working copy is dirty and we are
     * about to replace it with a new entry.
     * If the user clicks Yes and the save fails, we return {@code false} so the caller
     * knows to abort the input switch — the dirty data stays in the editor.
     *
     * @param editor  the entry editor about to switch to a new input
     * @return        {@code true} if it is safe to switch inputs (user said Yes and save succeeded,
     *                or user said No); {@code false} if the save failed and we should abort
     */
    public static boolean askSaveSharedWorkingCopyBeforeInputChange( IEntryEditor editor )
    {
        // Asking for saving the modifications
        MessageDialog dialog = new MessageDialog( Display.getCurrent().getActiveShell(), Messages
            .getString( "EntryEditorUtils.SaveChanges" ), null, Messages //$NON-NLS-1$
            .getString( "EntryEditorUtils.SaveChangesDescription" ), MessageDialog.QUESTION, new String[] //$NON-NLS-1$
            { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0 );
        int result = dialog.open();
        if ( result == 0 )
        {
            // Saving the modifications
            EntryEditorInput eei = editor.getEntryEditorInput();
            IStatus status = eei.saveSharedWorkingCopy( true, editor );

            if ( ( status == null ) || !status.isOK() )
            {
                // If save failed, let's keep the modifications in the editor and return false
                return false;
            }
        }

        return true;
    }
}
