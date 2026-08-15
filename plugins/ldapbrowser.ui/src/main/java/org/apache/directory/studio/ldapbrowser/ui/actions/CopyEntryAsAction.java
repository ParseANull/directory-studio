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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReadEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;


// ── CLASS: CopyEntryAsAction — YODA LIFTS THE X-WING FROM THE SWAMP ─────────
// On Dagobah, Luke stares at his sunken X-wing and says "It's too big." Yoda
// closes his eyes, reaches out, and transforms impossibility into reality —
// the fighter rises from the muck, fully intact, in a new form. This abstract
// class does the same: given LDAP entries (possibly uninitialized, possibly
// buried in search results or bookmarks), it lifts them out of the directory,
// ensures their attributes are loaded, and hands them to subclasses to
// serialize into whatever output format is needed (LDIF, CSV, etc.).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for actions that copy one or more LDAP entries to the
 * clipboard in a specific serialized format (LDIF, CSV, and so on).
 * We handle the common heavy lifting: resolving entries from bookmarks, fetching
 * uninitialized attributes from the server, and placing the result on the
 * clipboard. Subclasses only need to implement {@link #serialializeEntries} to
 * define the output format.
 * Think of this class as Yoda: we do the Force-heavy work so the subclass
 * (Luke) just has to pick up the X-wing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class CopyEntryAsAction extends BrowserAction
{
    /**
     * Returns Dn only Mode.
     */
    public static final int MODE_DN_ONLY = 1;

    /**
     * Returns Attributes only Mode.
     */
    public static final int MODE_RETURNING_ATTRIBUTES_ONLY = 2;

    /**
     * Normal Mode
     */
    public static final int MODE_NORMAL = 3;

    /**
     * Includes Operational Attributes Mode.
     */
    public static final int MODE_INCLUDE_OPERATIONAL_ATTRIBUTES = 4;

    protected int mode;

    protected String type;

    protected String appendix;


    // ── Yoda Chooses the Depth of the Lift ────────────────────────────────────
    // Before Yoda lifts the X-wing, he decides what form it needs to take:
    // just the hull (DN only), user-visible parts (normal), or everything
    // including the hidden machinery (operational attributes).
    // We store the target format type and mode, and derive the human-readable
    // appendix that appears in the menu label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopyEntryAsAction} configured for a specific output
     * format and copy mode.
     * The {@code mode} constant determines which attributes are included in the
     * serialized output and which menu label appendix is appended.
     *
     * @param type    the human-readable format name (e.g., "LDIF", "CSV") shown in the menu
     * @param mode    one of the {@code MODE_*} constants defined in this class;
     *                controls which entry data is included in the copy
     */
    public CopyEntryAsAction( String type, int mode )
    {
        super();
        this.type = type;
        this.mode = mode;
        if ( this.mode == MODE_DN_ONLY )
        {
            this.appendix = Messages.getString( "CopyEntryAsAction.DNOnly" ); //$NON-NLS-1$
        }
        else if ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY )
        {
            this.appendix = Messages.getString( "CopyEntryAsAction.AttributesOnly" ); //$NON-NLS-1$
        }
        else if ( this.mode == MODE_INCLUDE_OPERATIONAL_ATTRIBUTES )
        {
            this.appendix = Messages.getString( "CopyEntryAsAction.OperationalAttributes" ); //$NON-NLS-1$
        }
        else if ( this.mode == MODE_NORMAL )
        {
            this.appendix = Messages.getString( "CopyEntryAsAction.UserAttributes" ); //$NON-NLS-1$
        }
        else
        {
            appendix = ""; //$NON-NLS-1$
        }
    }


    // ── Yoda Announces What He's About to Lift ────────────────────────────────
    // Yoda looks at the X-wing, then at Luke, and describes what he's going to
    // do — "One X-wing as LDIF (User Attributes)" — so everyone knows the plan.
    // We build the menu label dynamically from the count of selected entries
    // and the configured format type and mode.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the context-sensitive menu label describing what will be copied
     * and in what format (e.g., "Copy 3 Entries as LDIF (User Attributes)").
     * The label changes based on whether entries, search results, bookmarks, or
     * whole searches are selected.
     *
     * @return  the localised display name for this action
     */
    public String getText()
    {
        if ( getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length > 0
            && getSelectedSearches().length == 0 )
        {
            String text = ( getSelectedEntries().length + getSelectedSearchResults().length
                + getSelectedBookmarks().length > 1 ? NLS.bind(
                Messages.getString( "CopyEntryAsAction.CopyEntries" ), new String[] { type } ) //$NON-NLS-1$
                : NLS.bind( Messages.getString( "CopyEntryAsAction.CopyEntry" ), new String[] { type } ) ) //$NON-NLS-1$ //$NON-NLS-2$
                + appendix;
            return text;
        }
        else if ( getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length == 0
            && getSelectedSearches().length == 1 && getSelectedSearches()[0].getSearchResults() != null
            && getSelectedSearches()[0].getSearchResults().length > 0 )
        {
            String text = ( getSelectedSearches()[0].getSearchResults().length > 1 ? NLS.bind( Messages
                .getString( "CopyEntryAsAction.CopyResults" ), new String[] { type } )//$NON-NLS-1$
                : NLS.bind( Messages.getString( "CopyEntryAsAction.CopyResult" ), new String[] { type } ) ) //$NON-NLS-1$
                + appendix;
            return text;
        }

        return NLS.bind( Messages.getString( "CopyEntryAsAction.CopyEntry" ), new String[] { type + appendix } ); //$NON-NLS-1$
    }


    // ── Yoda Has No Need for Command Codes ───────────────────────────────────
    // Yoda acts through the Force, not through keyboard shortcuts — no command
    // ID is registered for this abstract action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID and therefore no keyboard shortcut binding.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Yoda Raises the X-wing From the Swamp ────────────────────────────────
    // Yoda closes his eyes and the X-wing stirs: first he gathers all the pieces
    // (entries, search results, bookmarks), then makes sure their attributes are
    // loaded (the hidden parts under the water), and finally shapes the whole
    // thing into the target form and lifts it to the clipboard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Collects all selected entries (including those behind bookmarks and search
     * results), lazily fetches any uninitialized attributes from the LDAP server,
     * serializes them via {@link #serialializeEntries}, and places the result on
     * the system clipboard.
     * This is the heart of the operation — subclasses define the output format
     * by implementing {@link #serialializeEntries}.
     */
    public void run()
    {
        // entries to copy
        List<IEntry> entryList = new ArrayList<IEntry>();
        for ( int i = 0; i < getSelectedEntries().length; i++ )
        {
            entryList.add( getSelectedEntries()[i] );
        }
        for ( int i = 0; i < getSelectedSearchResults().length; i++ )
        {
            entryList.add( getSelectedSearchResults()[i].getEntry() );
        }
        for ( int i = 0; i < getSelectedBookmarks().length; i++ )
        {
            IEntry entry = getSelectedBookmarks()[0].getBrowserConnection().getEntryFromCache(
                getSelectedBookmarks()[0].getDn() );
            if ( entry == null )
            {
                ReadEntryRunnable runnable = new ReadEntryRunnable( getSelectedBookmarks()[0].getBrowserConnection(),
                    getSelectedBookmarks()[0].getDn() );
                RunnableContextRunner.execute( runnable, null, true );
                entry = runnable.getReadEntry();
            }
            entryList.add( entry );
        }
        if ( getSelectedSearches().length == 1 )
        {
            ISearchResult[] results = getSelectedSearches()[0].getSearchResults();
            for ( int k = 0; k < results.length; k++ )
            {
                entryList.add( results[k].getEntry() );
            }
        }
        IEntry[] entries = ( IEntry[] ) entryList.toArray( new IEntry[entryList.size()] );

        // check uninitialized entries
        List<IEntry> uninitializedEntryList = new ArrayList<IEntry>();
        for ( int i = 0; entries != null && i < entries.length; i++ )
        {
            if ( !entries[i].isAttributesInitialized() )
            {
                uninitializedEntryList.add( entries[i] );
            }
        }
        if ( uninitializedEntryList.size() > 0
            && ( this.mode == MODE_NORMAL || this.mode == MODE_INCLUDE_OPERATIONAL_ATTRIBUTES ) )
        {
            IEntry[] uninitializedEntries = ( IEntry[] ) uninitializedEntryList
                .toArray( new IEntry[uninitializedEntryList.size()] );

            InitializeAttributesRunnable runnable = new InitializeAttributesRunnable( uninitializedEntries );
            RunnableContextRunner.execute( runnable, null, true );

            // SyncInitializeEntryJob job = new
            // SyncInitializeEntryJob(uninitializedEntries,
            // InitializeEntryJob.INIT_ATTRIBUTES_MODE, null);
            // job.execute();
        }

        // serialize
        StringBuffer text = new StringBuffer();
        serialializeEntries( entries, text );
        copyToClipboard( text.toString() );
    }


    // ── Yoda Channels the Force Into the Right Shape ─────────────────────────
    // After the X-wing is clear of the swamp, Yoda shapes it — but he steps back
    // and lets Luke finish the job: the exact form is the subclass's responsibility.
    // Subclasses must implement this to define the serialized output format.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given entries into the target format, appending the result
     * to {@code text}.
     * Subclasses implement this to produce LDIF, CSV, or any other text format.
     * The method is called after attributes have been initialized.
     *
     * @param entries  the fully initialized entries to serialize; may be empty but not null
     * @param text     the buffer to append serialized text to; must not be null
     */
    protected abstract void serialializeEntries( IEntry[] entries, StringBuffer text );


    // ── Yoda Senses Whether the X-wing Is Within Reach ───────────────────────
    // Yoda reaches out with the Force to see if there's something worth lifting —
    // the mode determines which kinds of selections are compatible with this action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the current selection contains entries, search
     * results, bookmarks, or a search whose results are loaded, and the configured
     * mode is compatible with what's selected.
     * Search results selected alone only support returning-attributes, normal,
     * DN-only, and operational-attributes modes.
     *
     * @return  {@code true} if this action can operate on the current selection
     */
    public boolean isEnabled()
    {
        if ( getSelectedSearchResults().length > 0
            && getSelectedEntries().length + getSelectedBookmarks().length + getSelectedSearches().length == 0 )
        {
            return ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY || this.mode == MODE_NORMAL
                || this.mode == MODE_DN_ONLY || this.mode == MODE_INCLUDE_OPERATIONAL_ATTRIBUTES );
        }
        if ( getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length > 0
            && getSelectedSearches().length == 0 )
        {
            return ( this.mode == MODE_NORMAL || this.mode == MODE_DN_ONLY || this.mode == MODE_INCLUDE_OPERATIONAL_ATTRIBUTES );
        }
        if ( getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length == 0
            && getSelectedSearches().length == 1 && getSelectedSearches()[0].getSearchResults() != null
            && getSelectedSearches()[0].getSearchResults().length > 0 )
        {
            return true;
        }
        return false;
    }


    // ── Yoda Sets the X-wing Down on Solid Ground ─────────────────────────────
    // Once the X-wing is in its new form, Yoda places it gently on dry land —
    // the clipboard — so Luke (the user) can pick it up and use it.
    // We create a fresh SWT Clipboard, set the text content, and dispose
    // immediately to avoid leaking the native resource.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Writes {@code text} to the system clipboard as plain text using SWT's
     * {@link Clipboard} API.
     * We create and dispose the clipboard handle in a single call to avoid
     * native resource leaks — always call {@code dispose()} in a finally block.
     *
     * @param text  the serialized entry data to place on the clipboard; must not be null
     */
    protected void copyToClipboard( String text )
    {
        Clipboard clipboard = null;
        try
        {
            clipboard = new Clipboard( Display.getCurrent() );
            clipboard.setContents( new Object[]
                { text }, new Transfer[]
                { TextTransfer.getInstance() } );
        }
        finally
        {
            if ( clipboard != null )
                clipboard.dispose();
        }
    }
}
