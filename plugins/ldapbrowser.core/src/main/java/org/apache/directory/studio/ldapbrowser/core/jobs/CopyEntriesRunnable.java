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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.EntryExistsCopyStrategyDialog.EntryExistsCopyStrategy;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;


// ── CLASS: CopyEntriesRunnable — THE CLONE ARMY REPLICATES ACROSS ALL PLANETS ─
// The Kaminoans start the cloning vats: for each Jango Fett template (source
// entry), they create an exact copy at the target location.  If the clone
// already exists in the barracks (entry-already-exists), they ask Mace Windu
// how to handle the conflict (BREAK/IGNORE/OVERWRITE/RENAME).  For a full
// subtree copy, each clone's children are also recursively cloned (SUBTREE_SCOPE).
// This runnable copies one or more entries to a new parent DN.  It reads the
// source entry's attributes, adjusts the RDN on the new copy, and creates the
// entry at the destination.  A dialog handles naming conflicts.  Fires a
// {@link BulkModificationEvent} after completion.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that copies one or more LDAP entries to a new parent
 * location.  The copy scope controls depth: OBJECT (top entry only), ONELEVEL
 * (top entry + its direct children), or SUBTREE (full recursive copy).
 * If the destination entry already exists, an
 * {@link EntryExistsCopyStrategyDialog} asks the user how to handle the
 * conflict (stop, ignore, overwrite attributes, or rename the copy).
 * After copying, the parent entry's children are marked as uninitialized so the
 * tree refreshes on next expand.  A {@link BulkModificationEvent} notifies the
 * UI.
 * Think of it as the Kaminoans cloning the template across the galaxy.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyEntriesRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The parent entry. */
    private IEntry parent;

    /** The entries to copy. */
    private IEntry[] entriesToCopy;

    /** The copy scope */
    private SearchScope scope;

    /** The dialog to ask for the strategy */
    private EntryExistsCopyStrategyDialog dialog;


    // ── The Kaminoans Prepare The Cloning Vats ────────────────────────────────
    // Stores parent, source entries, copy scope, and the conflict dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new CopyEntriesRunnable.
     *
     * <p>For example — copy a single entry (OBJECT scope):</p>
     * <pre>
     *   new StudioBrowserJob(new CopyEntriesRunnable(
     *       newParent, new IEntry[]{sourceEntry}, SearchScope.OBJECT, dialog)).execute();
     * </pre>
     *
     * @param parent        the destination parent entry.
     * @param entriesToCopy the source entries to copy.
     * @param scope         the copy depth (OBJECT, ONELEVEL, or SUBTREE).
     * @param dialog        the dialog to ask for conflict resolution; pass
     *                      {@code null} to report name-conflict errors directly.
     */
    public CopyEntriesRunnable( final IEntry parent, final IEntry[] entriesToCopy, SearchScope scope,
        EntryExistsCopyStrategyDialog dialog )
    {
        this.parent = parent;
        this.entriesToCopy = entriesToCopy;
        this.scope = scope;
        this.dialog = dialog;
    }


    // ── One Cloning Bay, One Connection ───────────────────────────────────────
    // All copies go to the same destination connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the connection for the destination parent entry.
     *
     * @return single-element array with the underlying {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { parent.getBrowserConnection().getConnection() };
    }


    // ── The Job Name For The Progress Bar ─────────────────────────────────────
    // "Copy entry" (singular) or "Copy entries" (plural).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Copy entry" or "Copy entries" label.
     */
    public String getName()
    {
        return entriesToCopy.length == 1 ? BrowserCoreMessages.jobs__copy_entries_name_1
            : BrowserCoreMessages.jobs__copy_entries_name_n;
    }


    // ── Lock Parent And Sources During Cloning ────────────────────────────────
    // Prevents the parent's children list from changing while we're adding copies.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent and all source entries as locked objects.
     *
     * @return the lock list.
     */
    public Object[] getLockedObjects()
    {
        List<IEntry> l = new ArrayList<>();
        l.add( parent );
        l.addAll( Arrays.asList( entriesToCopy ) );
        return l.toArray();
    }


    // ── If Cloning Fails ──────────────────────────────────────────────────────
    // Singular vs plural error message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown if copying fails.
     *
     * @return a localised error string.
     */
    public String getErrorMessage()
    {
        return entriesToCopy.length == 1 ? BrowserCoreMessages.jobs__copy_entries_error_1
            : BrowserCoreMessages.jobs__copy_entries_error_n;
    }


    // ── The Kaminoans Clone Each Template ─────────────────────────────────────
    // Validates that the source and target are not the same subtree.
    // Calls static copyEntry per source entry.
    // After copying, marks the parent's children as uninitialized.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Copies each source entry to the parent DN.  Entries that are ancestors of
     * the target parent are skipped with an error (you can't copy an entry into
     * its own subtree for ONELEVEL/SUBTREE scope).
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask(
            entriesToCopy.length == 1 ? BrowserCoreMessages.bind( BrowserCoreMessages.jobs__copy_entries_task_1,
                new String[]
                    { entriesToCopy[0].getDn().getName(), parent.getDn().getName() } ) : BrowserCoreMessages.bind(
                BrowserCoreMessages.jobs__copy_entries_task_n, new String[]
                    { Integer.toString( entriesToCopy.length ), parent.getDn().getName() } ),
            2 + entriesToCopy.length );

        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        if ( scope == SearchScope.OBJECT || scope == SearchScope.ONELEVEL || scope == SearchScope.SUBTREE )
        {
            StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );
            int copyScope = scope == SearchScope.SUBTREE ? SearchControls.SUBTREE_SCOPE
                : scope == SearchScope.ONELEVEL ? SearchControls.ONELEVEL_SCOPE : SearchControls.OBJECT_SCOPE;

            int num = 0;
            for ( int i = 0; !monitor.isCanceled() && i < entriesToCopy.length; i++ )
            {
                IEntry entryToCopy = entriesToCopy[i];

                if ( scope == SearchScope.OBJECT
                    || !parent.getDn().getNormName().endsWith( entryToCopy.getDn().getNormName() ) )
                {
                    dummyMonitor.reset();
                    num = copyEntry( entryToCopy, parent, null, copyScope, num, dialog, dummyMonitor, monitor );
                }
                else
                {
                    monitor.reportError( BrowserCoreMessages.jobs__copy_entries_source_and_target_are_equal );
                }
            }

            parent.setChildrenInitialized( false );
            parent.setHasChildrenHint( true );
        }
    }


    // ── The Kaminoans Announce The Clone Batch Is Ready ───────────────────────
    // Fires a BulkModificationEvent so the tree refreshes the parent node.
    // Individual EntryAddedEvents are not fired — there may be thousands of clones.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires a {@link BulkModificationEvent} to notify the browser tree that an
     * unknown number of entries were added.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        // don't fire an EntryCreatedEvent for each created entry
        // that would cause massive UI updates
        // instead we fire a BulkModificationEvent
        EventRegistry.fireEntryUpdated( new BulkModificationEvent( parent.getBrowserConnection() ), this );
    }


    // ── Copy A Single Template Entry To The Destination ───────────────────────
    // Reads the source entry's attributes with an OBJECT-scope search.
    // Handles referral and subentry entries by requesting the right attributes.
    // Then delegates to copyEntryRecursive with the search result enumeration.
    // Static so RenameEntryRunnable can call it for simulated renames.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Copies a single entry (and optionally its children, based on scope) to
     * the destination parent.  Reads the source entry's attributes first, then
     * calls {@link #copyEntryRecursive}.
     * Static so that {@link RenameEntryRunnable} can reuse it for copy+delete
     * simulated renames.
     *
     * @param entryToCopy          the source entry.
     * @param parent               the destination parent.
     * @param newRdn               the RDN for the copy; {@code null} to keep
     *                             the source RDN.
     * @param scope                {@link SearchControls} scope constant.
     * @param numberOfCopiedEntries running count for progress reporting.
     * @param dialog               the conflict dialog; {@code null} to report
     *                             conflicts directly to the monitor.
     * @param dummyMonitor         absorbs per-entry errors for retry.
     * @param monitor              the real progress monitor.
     * @return the updated count of copied entries.
     */
    static int copyEntry( IEntry entryToCopy, IEntry parent, Rdn newRdn, int scope, int numberOfCopiedEntries,
        EntryExistsCopyStrategyDialog dialog, StudioProgressMonitor dummyMonitor, StudioProgressMonitor monitor )
    {
        SearchControls searchControls = new SearchControls();
        searchControls.setCountLimit( 1 );
        searchControls.setReturningAttributes( new String[]
            { SchemaConstants.ALL_USER_ATTRIBUTES } );
        searchControls.setSearchScope( SearchControls.OBJECT_SCOPE );

        // handle special entries
        org.apache.directory.api.ldap.model.message.Control[] controls = null;
        if ( entryToCopy.isReferral() )
        {
            controls = new org.apache.directory.api.ldap.model.message.Control[]
                { Controls.MANAGEDSAIT_CONTROL };
            searchControls.setReturningAttributes( new String[]
                { SchemaConstants.ALL_USER_ATTRIBUTES, SchemaConstants.REF_AT } );
        }
        if ( entryToCopy.isSubentry() )
        {
            searchControls.setReturningAttributes( new String[]
                { SchemaConstants.ALL_USER_ATTRIBUTES, SchemaConstants.SUBTREE_SPECIFICATION_AT } );
        }

        StudioSearchResultEnumeration result = entryToCopy
            .getBrowserConnection()
            .getConnection()
            .getConnectionWrapper()
            .search( entryToCopy.getDn().getName(), ISearch.FILTER_TRUE, searchControls,
                AliasDereferencingMethod.NEVER, ReferralHandlingMethod.IGNORE, controls, monitor, null );

        // In case the parent is the RootDSE: use the parent Dn of the old entry
        Dn parentDn = parent.getDn();
        if ( parentDn.isEmpty() )
        {
            parentDn = entryToCopy.getDn().getParent();
        }
        numberOfCopiedEntries = copyEntryRecursive( entryToCopy.getBrowserConnection(), result,
            parent.getBrowserConnection(), parentDn, newRdn, scope, numberOfCopiedEntries, dialog, dummyMonitor,
            monitor );

        return numberOfCopiedEntries;
    }


    // ── The Kaminoans Clone Each Entry And All Its Children ───────────────────
    // Iterates through the search result enumeration.  For each entry:
    //   1. Compose the new DN by applying the parent DN and any forced new RDN.
    //   2. Apply the new RDN to the entry's attribute values (oldRdn out, newRdn in).
    //   3. Try to create the entry.
    //   4. If it already exists, ask the dialog for a strategy and react.
    //   5. If scope is ONELEVEL or SUBTREE, recursively copy children.
    // Static so other runnables can reuse it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Recursively copies entries from the source enumeration to the target
     * location.  Handles naming conflicts by consulting the
     * {@link EntryExistsCopyStrategyDialog}.
     *
     * @param sourceBrowserConnection  the source connection.
     * @param entries                  the source search result enumeration.
     * @param targetBrowserConnection  the target connection.
     * @param parentDn                 the target parent DN.
     * @param forceNewRdn              the RDN override, or {@code null} to keep
     *                                 the original.
     * @param scope                    {@link SearchControls} scope constant.
     * @param numberOfCopiedEntries    running count.
     * @param dialog                   the conflict dialog; {@code null} to report.
     * @param dummyMonitor             absorbs per-entry errors.
     * @param monitor                  the real progress monitor.
     * @return the updated count of copied entries.
     */
    static int copyEntryRecursive( IBrowserConnection sourceBrowserConnection, StudioSearchResultEnumeration entries,
        IBrowserConnection targetBrowserConnection, Dn parentDn, Rdn forceNewRdn, int scope,
        int numberOfCopiedEntries, EntryExistsCopyStrategyDialog dialog, StudioProgressMonitor dummyMonitor,
        StudioProgressMonitor monitor )
    {
        try
        {
            while ( !monitor.isCanceled() && entries.hasMore() )
            {
                // get next entry to copy
                Entry entry = entries.next().getEntry();
                Dn oldLdapDn = entry.getDn();
                Rdn oldRdn = oldLdapDn.getRdn();

                // compose new Dn
                Rdn newRdn = oldLdapDn.getRdn();
                if ( forceNewRdn != null )
                {
                    newRdn = forceNewRdn;
                }
                Dn newLdapDn = parentDn.add( newRdn );
                entry.setDn( newLdapDn );

                // apply new Rdn to the attributes
                applyNewRdn( entry, oldRdn, newRdn );

                // ManageDsaIT control
                Control[] controls = null;
                if ( entry.hasObjectClass( SchemaConstants.REFERRAL_OC ) )
                {
                    controls = new Control[]
                        { Controls.MANAGEDSAIT_CONTROL };
                }

                // create entry
                targetBrowserConnection.getConnection().getConnectionWrapper()
                    .createEntry( entry, controls, dummyMonitor, null );

                while ( dummyMonitor.errorsReported() )
                {
                    if ( dialog != null
                        && StudioLdapException.isEntryAlreadyExistsException( dummyMonitor.getException() ) )
                    {
                        // open dialog
                        dialog.setExistingEntry( targetBrowserConnection, newLdapDn );
                        dialog.open();
                        EntryExistsCopyStrategy strategy = dialog.getStrategy();

                        if ( strategy != null )
                        {
                            dummyMonitor.reset();

                            switch ( strategy )
                            {
                                case BREAK:
                                    monitor.setCanceled( true );
                                    break;

                                case IGNORE_AND_CONTINUE:
                                    break;

                                case OVERWRITE_AND_CONTINUE:
                                    // create modifications
                                    Collection<Modification> modifications = ModelConverter
                                        .toReplaceModifications( entry );

                                    // modify entry
                                    targetBrowserConnection
                                        .getConnection()
                                        .getConnectionWrapper()
                                        .modifyEntry( newLdapDn, modifications, null, dummyMonitor, null );

                                    // force reload of attributes
                                    IEntry newEntry = targetBrowserConnection.getEntryFromCache( newLdapDn );
                                    if ( newEntry != null )
                                    {
                                        newEntry.setAttributesInitialized( false );
                                    }

                                    break;

                                case RENAME_AND_CONTINUE:
                                    Rdn renamedRdn = dialog.getRdn();

                                    // apply renamed Rdn to the attributes
                                    applyNewRdn( entry, newRdn, renamedRdn );

                                    // compose new Dn
                                    newLdapDn = parentDn.add( renamedRdn );
                                    entry.setDn( newLdapDn );

                                    // create entry
                                    targetBrowserConnection.getConnection().getConnectionWrapper()
                                        .createEntry( entry, null, dummyMonitor, null );

                                    break;
                            }
                        }
                        else
                        {
                            monitor.reportError( dummyMonitor.getException() );
                            break;
                        }
                    }
                    else
                    {
                        monitor.reportError( dummyMonitor.getException() );
                        break;
                    }
                }

                if ( !monitor.isCanceled() && !monitor.errorsReported() )
                {
                    numberOfCopiedEntries++;

                    monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.model__copied_n_entries,
                        new String[]
                            { Integer.toString( numberOfCopiedEntries ) } ) ); //$NON-NLS-1$

                    // copy recursively
                    if ( scope == SearchControls.ONELEVEL_SCOPE || scope == SearchControls.SUBTREE_SCOPE )
                    {
                        SearchControls searchControls = new SearchControls();
                        searchControls.setCountLimit( 0 );
                        searchControls.setReturningAttributes( new String[]
                            { SchemaConstants.ALL_USER_ATTRIBUTES, SchemaConstants.REF_AT } );
                        searchControls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
                        StudioSearchResultEnumeration childEntries = sourceBrowserConnection
                            .getConnection()
                            .getConnectionWrapper()
                            .search( oldLdapDn.getName(), ISearch.FILTER_TRUE, searchControls,
                                AliasDereferencingMethod.NEVER, ReferralHandlingMethod.IGNORE, null, monitor, null );

                        if ( scope == SearchControls.ONELEVEL_SCOPE )
                        {
                            scope = SearchControls.OBJECT_SCOPE;
                        }

                        numberOfCopiedEntries = copyEntryRecursive( sourceBrowserConnection, childEntries,
                            targetBrowserConnection, newLdapDn, null, scope, numberOfCopiedEntries, dialog,
                            dummyMonitor, monitor );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }

        return numberOfCopiedEntries;
    }


    // ── Swap The Clone's RDN Attributes ───────────────────────────────────────
    // When we give the clone a new RDN (e.g. rename from "cn=Fett" to "cn=Rex"),
    // we remove the old RDN attribute value and add the new one so the entry's
    // attributes stay consistent with its DN.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adjusts an entry's attributes to match a new RDN.  Removes the old RDN
     * attribute values and adds the new ones.  This keeps the entry's attribute
     * set consistent with its DN after a copy-with-rename.
     *
     * @param entry  the entry (Apache LDAP API) to modify in place.
     * @param oldRdn the old RDN (values to remove).
     * @param newRdn the new RDN (values to add).
     * @throws LdapException if the attribute manipulation fails.
     */
    private static void applyNewRdn( Entry entry, Rdn oldRdn, Rdn newRdn ) throws LdapException
    {
        // remove old Rdn attributes and values
        for ( Ava atav : oldRdn )
        {
            entry.remove( atav.getType(), atav.getValue() );
        }

        // add new Rdn attributes and values
        for ( Ava atav : newRdn )
        {
            entry.add( atav.getType(), atav.getValue() );
        }
    }
}
