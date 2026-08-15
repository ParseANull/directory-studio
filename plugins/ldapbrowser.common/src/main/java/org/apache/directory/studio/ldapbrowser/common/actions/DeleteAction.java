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

package org.apache.directory.studio.ldapbrowser.common.actions;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.connection.core.StudioControl;
import org.apache.directory.studio.ldapbrowser.common.dialogs.DeleteDialog;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.jobs.DeleteEntriesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: DeleteAction — VADER FORCE-CHOKING AN OFFICER ON THE EXECUTOR ──────
// Darth Vader stands on the Executor's bridge. An officer delivers bad news.
// Vader decides what must be removed — not rashly, but with deliberate precision.
// He warns the rest of the bridge crew first ("delete this entry along with all
// its children — are you sure?"), then executes the order when confirmed.
// DeleteAction does the same: gathers what's targeted (entries, searches,
// bookmarks, or attribute values), assembles a warning dialog listing exactly
// what will be destroyed, then dispatches to the appropriate deletion back-end
// (LDAP delete job, search manager, bookmark manager, or CompoundModification).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Deletes whatever is currently selected in the browser: LDAP entries (with
 * optional tree-delete control), saved searches, bookmarks, or attribute values.
 * Presents a confirmation dialog listing the targets before any destructive
 * operation takes place. Subclasses override the {@code get*()} methods to
 * change which objects are targeted (e.g., {@link DeleteAllAction} targets all
 * children of the selected entry rather than just the selected entry itself).
 * Think of this class as Vader deciding who gets Force-choked — and then
 * actually doing it after a brief announcement to the bridge.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteAction extends BrowserAction
{
    // ── VADER ANNOUNCES WHAT'S ABOUT TO BE REMOVED ────────────────────────────
    // Vader's announcement adapts to the target: "Delete Entry" for one entry,
    // "Delete Entries" for many, "Delete Search" for a saved query, etc. The
    // label changes based on what's selected so the user always knows exactly
    // what the Delete key will do.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action, adapted to the current selection.
     * Shows the singular or plural form of "Delete Entry/Entries",
     * "Delete Search/Searches", "Delete Bookmark/Bookmarks", or
     * "Delete Value/Values" depending on what's selected and how many.
     * Falls back to plain "Delete" if the selection is mixed or unrecognized.
     *
     * <p>For example — Vader's announcement adapts to the situation:</p>
     * <pre>
     *   // 1 entry selected    → "Delete Entry"
     *   // 3 entries selected  → "Delete Entries"
     *   // 1 search selected   → "Delete Search"
     *   // 2 values selected   → "Delete Values"
     * </pre>
     *
     * @return the localized action label string.
     */
    public String getText()
    {
        try
        {
            Collection<IEntry> entries = getEntries();
            ISearch[] searches = getSearches();
            IBookmark[] bookmarks = getBookmarks();
            Collection<IValue> values = getValues();

            if ( entries.size() > 0 && searches.length == 0 && bookmarks.length == 0 && values.size() == 0 )
            {
                return entries.size() > 1 ? Messages.getString( "DeleteAction.DeleteEntries" ) : Messages.getString( "DeleteAction.DeleteEntry" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if ( searches.length > 0 && entries.size() == 0 && bookmarks.length == 0 && values.size() == 0 )
            {
                return searches.length > 1 ? Messages.getString( "DeleteAction.DeleteSearches" ) : Messages.getString( "DeleteAction.DeleteSearch" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if ( bookmarks.length > 0 && entries.size() == 0 && searches.length == 0 && values.size() == 0 )
            {
                return bookmarks.length > 1 ? Messages.getString( "DeleteAction.DeleteBookmarks" ) : Messages.getString( "DeleteAction.DeleteBookmark" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if ( values.size() > 0 && entries.size() == 0 && searches.length == 0 && bookmarks.length == 0 )
            {
                return values.size() > 1 ? Messages.getString( "DeleteAction.DeleteValues" ) : Messages.getString( "DeleteAction.DeleteValue" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch ( Exception e )
        {
        }

        return Messages.getString( "DeleteAction.Delete" ); //$NON-NLS-1$
    }


    // ── VADER HOLDS UP THE DELETE INSIGNIA ────────────────────────────────────
    // The standard Eclipse delete icon marks this action in menus and toolbars.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the standard Eclipse "delete" icon from the shared image registry.
     * Used in menus and toolbars to visually identify the delete action.
     *
     * @return the delete image descriptor.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_TOOL_DELETE );
    }


    // ── VADER BINDS THE ORDER TO THE STANDARD DELETE CHANNEL ──────────────────
    // The Eclipse Delete key binding is wired through this command ID so that
    // pressing Delete on the keyboard triggers exactly this action.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for the standard "delete" workbench action,
     * used to bind this action to the Delete key on the keyboard.
     *
     * @return the workbench delete command ID.
     */
    public String getCommandId()
    {
        return IWorkbenchActionDefinitionIds.DELETE;
    }


    // ── VADER EXECUTES THE ORDER ───────────────────────────────────────────────
    // Vader gathers each category of target, builds the warning message ("Are
    // you sure you want to destroy Alderaan — I mean, this entry and all its
    // children?"), opens the confirmation dialog, and — if confirmed — dispatches
    // the appropriate deletion back-end for each target type. If the tree-delete
    // LDAP control is supported on the server, the dialog also offers to use it.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Executes the delete operation. Collects all targeted objects, assembles
     * a warning message (with RDN conflicts, objectClass warnings, MUST attribute
     * warnings, non-modifiable attribute notices), opens a {@link DeleteDialog}
     * for confirmation, and then dispatches:
     * <ul>
     *   <li>Entries → {@link StudioBrowserJob} with {@link DeleteEntriesRunnable}</li>
     *   <li>Searches → removed from the connection's search manager</li>
     *   <li>Bookmarks → removed from the connection's bookmark manager</li>
     *   <li>Values → {@link CompoundModification#deleteValues}</li>
     * </ul>
     * Empty-only value sets (placeholder values) skip the confirmation dialog.
     * Silently swallows exceptions to avoid crashing the UI on unexpected states.
     *
     * <p>For example — Vader executing the order after the warning is given:</p>
     * <pre>
     *   if ( dialog.open() == DeleteDialog.OK ) {
     *     deleteEntries( entries, dialog.isUseTreeDeleteControl() );
     *   }
     * </pre>
     */
    public void run()
    {
        try
        {
            Collection<IEntry> entries = getEntries();
            ISearch[] searches = getSearches();
            IBookmark[] bookmarks = getBookmarks();
            Collection<IValue> values = getValues();

            StringBuffer message = new StringBuffer();
            boolean askForTreeDeleteControl = false;

            if ( entries.size() > 0 )
            {
                appendEntriesWarnMessage( message, entries );

                if ( entries.iterator().next().getBrowserConnection().getRootDSE()
                    .isControlSupported( StudioControl.TREEDELETE_CONTROL.getOid() ) )
                {
                    askForTreeDeleteControl = true;
                }
            }

            if ( searches.length > 0 )
            {
                appendSearchesWarnMessage( message, searches );
            }

            if ( bookmarks.length > 0 )
            {
                appendBookmarsWarnMessage( message, bookmarks );
            }

            if ( values.size() > 0 )
            {
                boolean emptyValuesOnly = true;
                for ( IValue value : values )
                {
                    if ( !value.isEmpty() )
                    {
                        emptyValuesOnly = false;
                    }
                }
                if ( !emptyValuesOnly )
                {
                    appendValuesWarnMessage( message, values );
                }
            }

            DeleteDialog dialog = new DeleteDialog( getShell(), getText(), message.toString(), askForTreeDeleteControl );
            if ( message.length() == 0 || dialog.open() == DeleteDialog.OK )
            {
                if ( entries.size() > 0 )
                {
                    deleteEntries( entries, dialog.isUseTreeDeleteControl() );
                }
                if ( searches.length > 0 )
                {
                    deleteSearches( searches );
                }
                if ( bookmarks.length > 0 )
                {
                    deleteBookmarks( bookmarks );
                }
                if ( values.size() > 0 )
                {
                    deleteValues( values );
                }
            }
        }
        catch ( Exception e )
        {
        }
    }


    // ── VADER CHECKS IF THERE IS ANYTHING TO CHOKE ────────────────────────────
    // Before Vader can Force-choke anyone, there has to be someone there.
    // This method returns true if any targeted objects exist in the selection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if there is at least one entry, search, bookmark,
     * or value currently targeted for deletion. Returns {@code false} when
     * nothing in the selection can be deleted.
     *
     * @return {@code true} if there is something to delete; {@code false} otherwise.
     */
    public boolean isEnabled()
    {
        try
        {
            Collection<IEntry> entries = getEntries();
            ISearch[] searches = getSearches();
            IBookmark[] bookmarks = getBookmarks();
            Collection<IValue> values = getValues();

            return entries.size() + searches.length + bookmarks.length + values.size() > 0;

        }
        catch ( Exception e )
        {
            //e.printStackTrace();
            return false;
        }
    }


    // ── VADER SELECTS HIS TARGETS — ENTRIES ────────────────────────────────────
    // Vader picks the entries to eliminate. Selected entries and search results
    // are both candidates. To avoid redundant deletions, if a parent entry is
    // already targeted we remove any of its children — Vader doesn't double-choke.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP entries to delete. Includes both directly selected entries
     * and the underlying entries of any selected search results. If both a parent
     * and its child are selected, the child is removed to avoid double-deletion
     * (the parent deletion would cascade). Subclasses override this to change
     * which entries are targeted.
     *
     * @return a collection of entries to delete; never null, may be empty.
     * @throws Exception  if anything goes wrong inspecting the selection.
     */
    protected Collection<IEntry> getEntries()
    {
        LinkedHashSet<IEntry> entriesSet = new LinkedHashSet<IEntry>();
        for ( IEntry entry : getSelectedEntries() )
        {
            entriesSet.add( entry );
        }
        for ( ISearchResult sr : getSelectedSearchResults() )
        {
            entriesSet.add( sr.getEntry() );
        }

        Iterator<IEntry> iterator = entriesSet.iterator();
        while ( iterator.hasNext() )
        {
            IEntry entry = iterator.next();
            if ( entriesSet.contains( entry.getParententry() ) )
            {
                iterator.remove();
            }
        }

        return entriesSet;
    }


    // ── VADER ISSUES THE ENTRY DELETION WARNING ────────────────────────────────
    // Before the order is final, Vader lists the targets on the briefing screen.
    // If the RootDSE is somehow in the list, an extra warning is prepended.
    // For five or fewer targets, each DN is listed; for more, a generic message
    // covers it. This all gets appended to the shared warning StringBuffer.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Appends an entry-deletion warning to {@code message}. Lists each entry's
     * DN if there are five or fewer; for more than five, uses a generic
     * "delete selected entries" message. Prepends a RootDSE warning if the
     * root DSE is among the targets.
     *
     * @param message  the buffer to append to.
     * @param entries  the entries that will be listed in the warning.
     */
    protected void appendEntriesWarnMessage( StringBuffer message, Collection<IEntry> entries )
    {
        for ( IEntry entry : entries )
        {
            if ( entry instanceof IRootDSE )
            {
                message.append( Messages.getString( "DeleteAction.DeleteRootDSE" ) ); //$NON-NLS-1$
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
            }
        }

        if ( entries.size() <= 5 )
        {
            message.append( entries.size() == 1 ? Messages.getString( "DeleteAction.DeleteEntryQuestion" ) //$NON-NLS-1$
                : Messages.getString( "DeleteAction.DeleteEntriesQuestion" ) ); //$NON-NLS-1$
            for ( IEntry entry : entries )
            {
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( "  - " ); //$NON-NLS-1$
                message.append( entry.getDn().getName() );
            }
        }
        else
        {
            message.append( Messages.getString( "DeleteAction.DeleteSelectedEntriesQuestion" ) ); //$NON-NLS-1$
        }
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
    }


    // ── VADER DISPATCHES THE ENTRY DELETION ORDER ─────────────────────────────
    // Once the targeting data is confirmed, Vader issues the final command:
    // spin up a StudioBrowserJob with a DeleteEntriesRunnable. That job runs
    // on a background thread so the UI stays responsive while LDAP deletes happen.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches the background job that deletes LDAP entries. Wraps a
     * {@link DeleteEntriesRunnable} in a {@link StudioBrowserJob} and calls
     * {@code execute()} — the job runs asynchronously on a worker thread.
     * If the server supports the tree-delete control and the user opted in,
     * the runnable uses it to delete entries recursively in one LDAP operation.
     *
     * @param entries               the entries to delete.
     * @param useTreeDeleteControl  {@code true} to request the server-side
     *                              tree-delete LDAP control.
     */
    protected void deleteEntries( Collection<IEntry> entries, boolean useTreeDeleteControl )
    {
        new StudioBrowserJob( new DeleteEntriesRunnable( entries, useTreeDeleteControl ) ).execute();
    }


    // ── VADER SELECTS HIS TARGETS — SEARCHES ───────────────────────────────────
    // Saved searches are just the ones currently selected in the view. No
    // parent-child filtering needed — searches don't nest.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the searches to delete. By default returns all currently
     * selected searches. Subclasses override this to expand the scope
     * (e.g., all searches on the connection).
     *
     * @return the array of selected searches; never null, may be empty.
     * @throws Exception  if anything goes wrong inspecting the selection.
     */
    protected ISearch[] getSearches()
    {
        return getSelectedSearches();
    }


    // ── VADER ISSUES THE SEARCH DELETION WARNING ──────────────────────────────
    // Lists the names of searches to be removed. Five or fewer are listed by
    // name; more than five gets a generic message.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Appends a search-deletion warning to {@code message}. Lists each search
     * by name if there are five or fewer; for more than five, uses a generic
     * "delete selected searches" message.
     *
     * @param message  the buffer to append to.
     * @param searches  the searches that will be listed in the warning.
     */
    protected void appendSearchesWarnMessage( StringBuffer message, ISearch[] searches )
    {
        if ( searches.length <= 5 )
        {
            message.append( searches.length == 1 ? Messages.getString( "DeleteAction.DeleteSearchQuestion" ) //$NON-NLS-1$
                : Messages.getString( "DeleteAction.DeleteSearchesQuestion" ) ); //$NON-NLS-1$
            for ( int i = 0; i < searches.length; i++ )
            {
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( "  - " ); //$NON-NLS-1$
                message.append( searches[i].getName() );
            }
        }
        else
        {
            message.append( Messages.getString( "DeleteAction.DeleteSelectedSearchesQuestion" ) ); //$NON-NLS-1$
        }
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
    }


    // ── VADER WIPES THE SAVED SEARCH RECORDS ──────────────────────────────────
    // Searches are removed from the connection's search manager — no LDAP
    // operation needed, just in-memory removal from the search registry.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Removes each search from its connection's search manager.
     * This is a local in-memory operation — no LDAP request is sent to the server.
     *
     * @param searches  the searches to remove.
     */
    protected void deleteSearches( ISearch[] searches )
    {
        for ( ISearch search : searches )
        {
            search.getBrowserConnection().getSearchManager().removeSearch( search );
        }
    }


    // ── VADER SELECTS HIS TARGETS — BOOKMARKS ──────────────────────────────────
    // Bookmarks to delete are just the currently selected ones.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bookmarks to delete. By default returns all currently
     * selected bookmarks. Subclasses override this to expand the scope.
     *
     * @return the array of selected bookmarks; never null, may be empty.
     * @throws Exception  if anything goes wrong inspecting the selection.
     */
    protected IBookmark[] getBookmarks()
    {
        return getSelectedBookmarks();
    }


    // ── VADER ISSUES THE BOOKMARK DELETION WARNING ─────────────────────────────
    // Lists the names of bookmarks to be removed. Five or fewer are listed by
    // name; more than five gets a generic message.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Appends a bookmark-deletion warning to {@code message}. Lists each bookmark
     * by name if there are five or fewer; for more than five, uses a generic
     * "delete selected bookmarks" message.
     *
     * @param message    the buffer to append to.
     * @param bookmarks  the bookmarks that will be listed in the warning.
     */
    protected void appendBookmarsWarnMessage( StringBuffer message, IBookmark[] bookmarks )
    {
        if ( bookmarks.length <= 5 )
        {
            message.append( bookmarks.length == 1 ? Messages.getString( "DeleteAction.DeleteBookmarkQuestion" ) //$NON-NLS-1$
                : Messages.getString( "DeleteAction.DeleteBookmarksQuestion" ) ); //$NON-NLS-1$
            for ( int i = 0; i < bookmarks.length; i++ )
            {
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( "  - " ); //$NON-NLS-1$
                message.append( bookmarks[i].getName() );
            }
        }
        else
        {
            message.append( Messages.getString( "DeleteAction.DeleteSelectedBookmarksQuestion" ) ); //$NON-NLS-1$
        }
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
    }


    // ── VADER WIPES THE BOOKMARK RECORDS ──────────────────────────────────────
    // Bookmarks are removed from the connection's bookmark manager — again,
    // a local in-memory operation with no LDAP request.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Removes each bookmark from its connection's bookmark manager.
     * This is a local in-memory operation — no LDAP request is sent to the server.
     *
     * @param bookmarks  the bookmarks to remove.
     */
    protected void deleteBookmarks( IBookmark[] bookmarks )
    {
        for ( IBookmark bookmark : bookmarks )
        {
            bookmark.getBrowserConnection().getBookmarkManager().removeBookmark( bookmark );
        }
    }


    // ── VADER SELECTS HIS TARGETS — VALUES ─────────────────────────────────────
    // Values come from three sources in priority order: selected attributes
    // (all their values), selected attribute hierarchies (all values of all
    // attributes in the hierarchy), and directly selected individual values.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute values to delete. Collects values from three
     * sources: directly selected {@link IAttribute}s (all values), selected
     * {@link AttributeHierarchy} objects (all values of all attributes),
     * and directly selected {@link IValue}s. Uses a {@link LinkedHashSet} to
     * deduplicate while preserving insertion order.
     *
     * @return a collection of values to delete; never null, may be empty.
     * @throws Exception  if anything goes wrong inspecting the selection.
     */
    protected Collection<IValue> getValues() throws Exception
    {
        Set<IValue> valueList = new LinkedHashSet<IValue>();

        // add selected attributes
        for ( IAttribute attribute : getSelectedAttributes() )
        {
            if ( attribute != null && attribute.getValueSize() > 0 )
            {
                valueList.addAll( Arrays.asList( attribute.getValues() ) );
            }
        }

        // add selected hierarchies
        for ( AttributeHierarchy ah : getSelectedAttributeHierarchies() )
        {
            for ( IAttribute attribute : ah )
            {
                if ( attribute != null && attribute.getValueSize() > 0 )
                {
                    valueList.addAll( Arrays.asList( attribute.getValues() ) );
                }
            }
        }

        // add selected values, but not if there attributes are also selected
        for ( IValue value : getSelectedValues() )
        {
            valueList.add( value );
        }

        return valueList;
    }


    // ── VADER ISSUES THE VALUE DELETION WARNING ────────────────────────────────
    // Values carry extra risk: they might be part of the RDN (deleting them
    // would break the entry's path), required by the schema (MUST attributes),
    // non-modifiable (operational attributes), or the sole remaining objectClass.
    // We check all of these and append appropriate warnings to the message buffer.
    // The final section lists up to five values by name, or a generic message for
    // larger selections.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Appends value-deletion warnings to {@code message}. For each value, checks:
     * <ul>
     *   <li>Is it part of the RDN? (would break the entry's DN)</li>
     *   <li>Is it the last objectClass value? (would invalidate the entry)</li>
     *   <li>Is it the last value of a MUST attribute?</li>
     *   <li>Is it non-modifiable (operational)?</li>
     *   <li>Would removing this objectClass make other attributes orphaned?</li>
     * </ul>
     * Then lists the values by name (up to five) or a generic summary for more.
     *
     * @param message  the buffer to append warnings to.
     * @param values   the values that will be deleted.
     */
    protected void appendValuesWarnMessage( StringBuffer message, Collection<IValue> values )
    {
        Map<AttributeType, Integer> attributeNameToSelectedValuesCountMap = new HashMap<AttributeType, Integer>();
        Set<ObjectClass> selectedObjectClasses = new HashSet<ObjectClass>();
        for ( IValue value : values )
        {
            String type = value.getAttribute().getType();
            AttributeType atd = value.getAttribute().getAttributeTypeDescription();
            AttributeHierarchy ah = value.getAttribute().getEntry().getAttributeWithSubtypes( type );

            // check if (part of) Rdn is selected
            if ( value.isRdnPart() )
            {
                message.append( NLS.bind( Messages.getString( "DeleteAction.DeletePartOfRDN" ), value.toString() ) ); //$NON-NLS-1$
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
            }

            // check if a required objectClass is selected
            if ( value.getAttribute().isObjectClassAttribute() )
            {
                selectedObjectClasses.add( value.getAttribute().getEntry().getBrowserConnection().getSchema()
                    .getObjectClassDescription( value.getStringValue() ) );
            }

            // check if ALL values of objectClass or a MUST attribute are selected
            if ( !attributeNameToSelectedValuesCountMap.containsKey( atd ) )
            {
                attributeNameToSelectedValuesCountMap.put( atd, Integer.valueOf(  0 ) );
            }
            int count = ( attributeNameToSelectedValuesCountMap.get( atd ) ).intValue() + 1;
            attributeNameToSelectedValuesCountMap.put( atd, Integer.valueOf( count ) );
            if ( value.getAttribute().isObjectClassAttribute() && count >= ah.getValueSize() )
            {
                message.append( Messages.getString( "DeleteAction.DeleteObjectClass" ) ); //$NON-NLS-1$
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                continue;
            }
            else if ( value.getAttribute().isMustAttribute() && count >= ah.getValueSize() )
            {
                message.append( NLS.bind( Messages.getString( "DeleteAction.DeleteMust" ), type ) ); //$NON-NLS-1$
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
            }

            // check if a value of an operational attribute is selected
            if ( !SchemaUtils.isModifiable( atd ) )
            {
                message.append( NLS.bind( Messages.getString( "DeleteAction.DeleteNonModifiable" ), type ) ); //$NON-NLS-1$
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                continue;
            }
        }

        // check if a required objectClass is selected
        if ( values.size() > 0 && !selectedObjectClasses.isEmpty() )
        {
            IEntry entry = values.iterator().next().getAttribute().getEntry();
            Schema schema = entry.getBrowserConnection().getSchema();
            // get remaining attributes
            Collection<ObjectClass> remainingObjectClasses = entry.getObjectClassDescriptions();
            remainingObjectClasses.removeAll( selectedObjectClasses );
            Set<AttributeType> remainingAttributeSet = new HashSet<AttributeType>();
            for ( ObjectClass ocd : remainingObjectClasses )
            {
                {
                    Collection<String> mustAttrs = SchemaUtils.getMustAttributeTypeDescriptionNamesTransitive( ocd,
                        schema );
                    for ( String mustAttr : mustAttrs )
                    {
                        AttributeType atd = entry.getBrowserConnection().getSchema()
                            .getAttributeTypeDescription( mustAttr );
                        remainingAttributeSet.add( atd );
                    }
                    Collection<String> mayAttrs = SchemaUtils.getMayAttributeTypeDescriptionNamesTransitive( ocd,
                        schema );
                    for ( String mayAttr : mayAttrs )
                    {
                        AttributeType atd = entry.getBrowserConnection().getSchema()
                            .getAttributeTypeDescription( mayAttr );
                        remainingAttributeSet.add( atd );
                    }
                }
            }
            // check against attributes
            IAttribute[] attributes = entry.getAttributes();
            for ( IAttribute attribute : attributes )
            {
                if ( attribute.isMayAttribute() || attribute.isMustAttribute() )
                {
                    if ( !remainingAttributeSet.contains( attribute.getAttributeTypeDescription() ) )
                    {
                        message.append( NLS.bind(
                            Messages.getString( "DeleteAction.DeleteNeededObjectClass" ), attribute.getDescription() ) ); //$NON-NLS-1$
                        message.append( BrowserCoreConstants.LINE_SEPARATOR );
                        message.append( BrowserCoreConstants.LINE_SEPARATOR );
                    }
                }
            }
        }

        if ( values.size() <= 5 )
        {
            message.append( values.size() == 1 ? Messages.getString( "DeleteAction.DeleteAttributeQuestion" ) //$NON-NLS-1$
                : Messages.getString( "DeleteAction.DeleteAttributesQuestion" ) ); //$NON-NLS-1$
            for ( IValue value : values )
            {
                message.append( BrowserCoreConstants.LINE_SEPARATOR );
                message.append( "  - " ); //$NON-NLS-1$
                message.append( value.toString() );
            }
        }
        else
        {
            message.append( Messages.getString( "DeleteAction.DeleteSelectedAttributesQuestion" ) ); //$NON-NLS-1$
        }
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
        message.append( BrowserCoreConstants.LINE_SEPARATOR );
    }


    // ── VADER WIPES THE ATTRIBUTE VALUES ──────────────────────────────────────
    // Values are removed through CompoundModification, which batches the LDAP
    // MODIFY operations into the fewest possible round trips. Unlike entry
    // deletion, this does not use a separate background job — the compound
    // modification handles its own execution.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Deletes the given attribute values by calling
     * {@link CompoundModification#deleteValues}. This batches the underlying
     * LDAP MODIFY operations for efficiency.
     *
     * @param values  the values to delete.
     */
    protected void deleteValues( Collection<IValue> values )
    {
        new CompoundModification().deleteValues( values );
    }

}
