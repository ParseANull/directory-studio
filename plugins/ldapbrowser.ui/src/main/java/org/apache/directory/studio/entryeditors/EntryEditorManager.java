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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateAdapter;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueModifiedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueMultiModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueRenamedEvent;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.jobs.UpdateEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;


// ── CLASS: EntryEditorManager — LANDO CALRISSIAN RUNNING CLOUD CITY ──────────
// Lando is the administrator of Cloud City: he manages the guest registry,
// allocates rooms (working copies) to each visitor, handles the awkward moment
// when the Empire shows up and changes the deal, cleans up after guests leave,
// and keeps the whole operation running smoothly behind the scenes.
// This class does the same for entry editors: it discovers every registered editor
// extension, manages reference and working copies for open editors, listens to
// server-side changes, and closes editors when their connection goes away.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages the lifecycle of all registered entry editor extensions and their
 * shared working copies.
 * On startup we scan the Eclipse extension registry for any plugin that contributed
 * to the {@code org.apache.directory.studio.entryeditors} extension point and
 * build a sorted registry of {@link EntryEditorExtension} beans.
 * At runtime we maintain two sets of copy pairs (reference + working) — one for
 * manual-save (open-save-close, "OSC") editors and one for auto-save editors —
 * and we propagate server-side changes and connection lifecycle events to all
 * open editor panels.
 * Think of Lando: charming, well-organised, responsive to unexpected guests
 * (server-side updates), and always ready to apologise when the deal changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorManager
{
    private static final String ID_ATTR = "id"; //$NON-NLS-1$
    private static final String NAME_ATTR = "name"; //$NON-NLS-1$
    private static final String DESCRIPTION_ATTR = "description"; //$NON-NLS-1$
    private static final String ICON_ATTR = "icon"; //$NON-NLS-1$
    private static final String CLASS_ATTR = "class"; //$NON-NLS-1$
    private static final String EDITOR_ID_ATTR = "editorId"; //$NON-NLS-1$
    private static final String MULTI_WINDOW_ATTR = "multiWindow"; //$NON-NLS-1$
    private static final String PRIORITY_ATTR = "priority"; //$NON-NLS-1$

    /** The priorities separator */
    public static final String PRIORITIES_SEPARATOR = ","; //$NON-NLS-1$

    /** The list of entry editors */
    private Map<String, EntryEditorExtension> entryEditorExtensions = new HashMap<>();

    /** The shared reference copies for open-save-close editors; original entry -> reference copy */
    private Map<IEntry, IEntry> oscSharedReferenceCopies = new HashMap<>();

    /** The shared working copies for open-save-close editors; original entry -> working copy */
    private Map<IEntry, IEntry> oscSharedWorkingCopies = new HashMap<>();

    /** The shared reference copies for auto-save editors; original entry -> reference copy */
    private Map<IEntry, IEntry> autoSaveSharedReferenceCopies = new HashMap<>();

    /** The shared working copies for auto-save editors; original entry -> working copy */
    private Map<IEntry, IEntry> autoSaveSharedWorkingCopies = new HashMap<>();

    /** The comparator for entry editors */
    private Comparator<EntryEditorExtension> entryEditorComparator = new Comparator<EntryEditorExtension>()
    {
        @Override
        public int compare( EntryEditorExtension o1, EntryEditorExtension o2 )
        {
            if ( o1 == null )
            {
                return ( o2 == null ) ? 0 : -1;
            }

            if ( o2 == null )
            {
                return 1;
            }

            // Getting priorities
            int o1Priority = o1.getPriority();
            int o2Priority = o2.getPriority();

            if ( o1Priority != o2Priority )
            {
                return ( o1Priority > o2Priority ) ? -1 : 1;
            }

            // Getting names
            String o1Name = o1.getName();
            String o2Name = o2.getName();

            if ( o1Name == null )
            {
                return ( o2Name == null ) ? 0 : -1;
            }

            return o1Name.compareTo( o2Name );
        }
    };


    /** The listener for workbench part update */
    private IPartListener2 partListener = new IPartListener2()
    {
        @Override
        public void partActivated( IWorkbenchPartReference partRef )
        {
            cleanupCopies( partRef.getPage() );

            IEntryEditor editor = getEntryEditor( partRef );

            if ( editor != null )
            {
                EntryEditorInput eei = editor.getEntryEditorInput();
                IEntry originalEntry = eei.getResolvedEntry();
                IEntry oscSharedReferenceCopy = oscSharedReferenceCopies.get( originalEntry );
                IEntry oscSharedWorkingCopy = oscSharedWorkingCopies.get( originalEntry );

                if ( editor.isAutoSave() )
                {
                    // check if the same entry is used in an OSC editor and is dirty -> should save first?
                    if ( oscSharedReferenceCopy != null && oscSharedWorkingCopy != null )
                    {
                        LdifFile diff = Utils.computeDiff( oscSharedReferenceCopy, oscSharedWorkingCopy );

                        if ( diff != null )
                        {
                            MessageDialog dialog = new MessageDialog( partRef.getPart( false ).getSite().getShell(),
                                Messages.getString( "EntryEditorManager.SaveChanges" ), null,//$NON-NLS-1$
                                Messages.getString( "EntryEditorManager.SaveChangesDescription" ), //$NON-NLS-1$
                                MessageDialog.QUESTION, new String[]
                                    { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0 );
                            int result = dialog.open();

                            if ( result == 0 )
                            {
                                saveSharedWorkingCopy( originalEntry, true, null );
                            }
                        }
                    }
                }
                else
                {
                    // check if original entry was updated
                    if ( ( oscSharedReferenceCopy != null ) && ( oscSharedWorkingCopy != null ) )
                    {
                        LdifFile refDiff = Utils.computeDiff( originalEntry, oscSharedReferenceCopy );

                        if ( refDiff != null )
                        {
                            // check if we could just update the working copy
                            LdifFile workDiff = Utils.computeDiff( oscSharedReferenceCopy, oscSharedWorkingCopy );

                            if ( workDiff != null )
                            {
                                askUpdateSharedWorkingCopy( partRef, originalEntry, oscSharedWorkingCopy, null );
                            }
                        }
                    }
                }
            }
        }


        @Override
        public void partOpened( IWorkbenchPartReference partRef )
        {
        }


        @Override
        public void partClosed( IWorkbenchPartReference partRef )
        {
            cleanupCopies( partRef.getPage() );
        }


        @Override
        public void partInputChanged( IWorkbenchPartReference partRef )
        {
            cleanupCopies( partRef.getPage() );
        }


        @Override
        public void partHidden( IWorkbenchPartReference partRef )
        {
        }


        @Override
        public void partDeactivated( IWorkbenchPartReference partRef )
        {
        }


        @Override
        public void partBroughtToTop( IWorkbenchPartReference partRef )
        {
        }


        @Override
        public void partVisible( IWorkbenchPartReference partRef )
        {
        }
    };

    /** The listener for entry update */
    private EntryUpdateListener entryUpdateListener = new EntryUpdateListener()
    {
        @Override
        public void entryUpdated( EntryModificationEvent event )
        {
            IEntry modifiedEntry = event.getModifiedEntry();
            IBrowserConnection browserConnection = modifiedEntry.getBrowserConnection();
            IEntry originalEntry = browserConnection.getEntryFromCache( modifiedEntry.getDn() );

            if ( modifiedEntry == originalEntry )
            {
                // an original entry has been modified, check if we could update the editors

                // if the OSC editor is not dirty we could update the working copy
                IEntry oscSharedReferenceCopy = oscSharedReferenceCopies.get( originalEntry );
                IEntry oscSharedWorkingCopy = oscSharedWorkingCopies.get( originalEntry );

                if ( ( oscSharedReferenceCopy != null ) && ( oscSharedWorkingCopy != null ) )
                {
                    LdifFile refDiff = Utils.computeDiff( originalEntry, oscSharedReferenceCopy );

                    if ( refDiff != null )
                    {
                        // diff between original entry and reference copy
                        LdifFile workDiff = Utils.computeDiff( oscSharedReferenceCopy, oscSharedWorkingCopy );

                        if ( workDiff == null )
                        {
                            // no changes on working copy, update
                            updateOscSharedReferenceCopy( originalEntry );
                            updateOscSharedWorkingCopy( originalEntry );

                            // inform all OSC editors
                            List<IEntryEditor> oscEditors = getOscEditors( oscSharedWorkingCopy );

                            for ( IEntryEditor editor : oscEditors )
                            {
                                editor.workingCopyModified( event.getSource() );
                            }
                        }
                        else
                        {
                            // changes on working copy, ask before update
                            IWorkbenchPartReference reference = getActivePartRef( getOscEditors( oscSharedWorkingCopy ) );

                            if ( reference != null )
                            {
                                askUpdateSharedWorkingCopy( reference, originalEntry, oscSharedWorkingCopy, event
                                    .getSource() );
                            }
                        }
                    }
                    else
                    {
                        // no diff betweeen original entry and reference copy, check if editor is dirty
                        LdifFile workDiff = Utils.computeDiff( oscSharedReferenceCopy, oscSharedWorkingCopy );

                        if ( workDiff != null )
                        {
                            // changes on working copy, ask before update
                            IWorkbenchPartReference reference = getActivePartRef( getOscEditors( oscSharedWorkingCopy ) );
                            if ( reference != null )
                            {
                                askUpdateSharedWorkingCopy( reference, originalEntry, oscSharedWorkingCopy, event
                                    .getSource() );
                            }
                        }
                    }
                }

                // always update auto-save working copies, if necessary
                IEntry autoSaveSharedReferenceCopy = autoSaveSharedReferenceCopies.get( originalEntry );
                IEntry autoSaveSharedWorkingCopy = autoSaveSharedWorkingCopies.get( originalEntry );

                if ( ( autoSaveSharedReferenceCopy != null ) && ( autoSaveSharedWorkingCopy != null ) )
                {
                    LdifFile diff = Utils.computeDiff( originalEntry, autoSaveSharedReferenceCopy );

                    if ( diff != null )
                    {
                        updateAutoSaveSharedReferenceCopy( originalEntry );
                        updateAutoSaveSharedWorkingCopy( originalEntry );
                        List<IEntryEditor> editors = getAutoSaveEditors( autoSaveSharedWorkingCopy );

                        for ( IEntryEditor editor : editors )
                        {
                            editor.workingCopyModified( event.getSource() );
                        }
                    }
                }

                // check all editors: if the input does not exist any more then close the editor
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                // Collecting editor references to close
                List<IEditorReference> editorReferences = new ArrayList<>();

                for ( IEditorReference ref : activePage.getEditorReferences() )
                {
                    IEntryEditor editor = getEntryEditor( ref );
                    if ( editor != null )
                    {
                        EntryEditorInput entryEditorInput = editor.getEntryEditorInput();

                        if ( entryEditorInput != null )
                        {
                            IEntry resolvedEntry = entryEditorInput.getResolvedEntry();

                            if ( ( editor != null ) && ( resolvedEntry != null ) )
                            {
                                IBrowserConnection bc = resolvedEntry.getBrowserConnection();
                                Dn dn = resolvedEntry.getDn();

                                if ( bc.getEntryFromCache( dn ) == null )
                                {
                                    editorReferences.add( ref );
                                }
                            }
                        }
                    }
                }

                // Closing the corresponding editor references
                if ( !editorReferences.isEmpty() )
                {
                    activePage.closeEditors( editorReferences.toArray( new IEditorReference[0] ), false );
                }
            }

            else if ( oscSharedWorkingCopies.containsKey( originalEntry )
                && ( oscSharedWorkingCopies.get( originalEntry ) == modifiedEntry ) )
            {
                // OSC working copy has been modified: inform OSC editors
                IEntry oscSharedWorkingCopy = oscSharedWorkingCopies.get( originalEntry );
                List<IEntryEditor> oscEditors = getOscEditors( oscSharedWorkingCopy );

                for ( IEntryEditor editor : oscEditors )
                {
                    editor.workingCopyModified( event.getSource() );
                }
            }

            else if ( autoSaveSharedWorkingCopies.containsValue( originalEntry )
                && ( autoSaveSharedWorkingCopies.get( originalEntry ) == modifiedEntry ) )
            {
                // auto-save working copy has been modified: save and inform all auto-save editors
                IEntry autoSaveSharedReferenceCopy = autoSaveSharedReferenceCopies.get( originalEntry );
                IEntry autoSaveSharedWorkingCopy = autoSaveSharedWorkingCopies.get( originalEntry );

                // sanity check: never save if event source is the EntryEditorManager
                if ( event.getSource() instanceof EntryEditorManager )
                {
                    return;
                }

                // only save if we receive a real value modification event
                if ( !( ( event instanceof ValueAddedEvent ) ||
                        ( event instanceof ValueDeletedEvent ) ||
                        ( event instanceof ValueModifiedEvent ) ||
                        ( event instanceof ValueRenamedEvent ) ||
                        ( event instanceof ValueMultiModificationEvent ) ) )
                {
                    return;
                }

                // consistency check: don't save if there is an empty value, silently return in that case
                for ( IAttribute attribute : autoSaveSharedWorkingCopy.getAttributes() )
                {
                    for ( IValue value : attribute.getValues() )
                    {
                        if ( value.isEmpty() )
                        {
                            return;
                        }
                    }
                }

                LdifFile diff = Utils.computeDiff( autoSaveSharedReferenceCopy, autoSaveSharedWorkingCopy );

                if ( diff != null )
                {
                    // remove entry from map, reduces number of fired events
                    autoSaveSharedReferenceCopies.remove( originalEntry );
                    autoSaveSharedWorkingCopies.remove( originalEntry );
                    UpdateEntryRunnable runnable = new UpdateEntryRunnable( originalEntry, diff
                        .toFormattedString( LdifFormatParameters.DEFAULT ) );
                    new StudioBrowserJob( runnable ).execute();
                    // put entry back to map
                    autoSaveSharedReferenceCopies.put( originalEntry, autoSaveSharedReferenceCopy );
                    autoSaveSharedWorkingCopies.put( originalEntry, autoSaveSharedWorkingCopy );

                    // don't care if status is ok or not: always update
                    updateAutoSaveSharedReferenceCopy( originalEntry );
                    updateAutoSaveSharedWorkingCopy( originalEntry );
                    List<IEntryEditor> editors = getAutoSaveEditors( autoSaveSharedWorkingCopy );

                    for ( IEntryEditor editor : editors )
                    {
                        editor.workingCopyModified( event.getSource() );
                    }
                }
            }
        }
    };

    /** The listener for connection update */
    private ConnectionUpdateListener connectionUpdateListener = new ConnectionUpdateAdapter()
    {
        @Override
        public void connectionClosed( Connection connection )
        {
            closeEditorsBelongingToConnection( connection );
        }


        @Override
        public void connectionRemoved( Connection connection )
        {
            closeEditorsBelongingToConnection( connection );
        }
    };


    // ── Lando Opens Cloud City for Business ──────────────────────────────────────
    // When Cloud City opens its doors, Lando's first job is to scan the guest registry
    // (extension registry), add part listeners to know when guests arrive and leave,
    // and hook into the city-wide alert system for external events.
    // We do exactly that in this constructor: init extensions, register listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance and immediately wires up the manager if a workbench window
     * is already active.
     * This gets called once during plugin startup. If the workbench isn't up yet (e.g.,
     * during headless tests), we skip the listener registration — {@link #getEditorManager()}
     * can be called later when the UI is ready.
     */
    public EntryEditorManager()
    {
        if ( PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null )
        {
            getEditorManager();
        }
    }


    // ── Lando Staffs Up Cloud City Before the Guests Arrive ─────────────────────
    // Lando briefs the staff: "Here's who's registered to visit, here's how to handle
    // arrivals and departures, and here's the channel for emergency alerts."
    // We initialise the extension registry, register the part listener, and hook
    // the entry-update and connection-update listeners so we react to all events.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the manager fully: scans entry editor extensions, registers the
     * workbench part listener, and subscribes to entry and connection update events.
     * Extracted from the constructor so it can be called later if the workbench
     * window wasn't available at construction time.
     */
    public void getEditorManager()
    {
        initEntryEditorExtensions();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener( partListener );
        EventRegistry
            .addEntryUpdateListener( entryUpdateListener, BrowserCommonActivator.getDefault().getEventRunner() );
        ConnectionEventRegistry.addConnectionUpdateListener( connectionUpdateListener, ConnectionUIPlugin.getDefault()
            .getEventRunner() );
    }


    // ── Lando Reads the Official Guest Manifest from the City Records ────────────
    // Before opening day, Lando checks the official city registry for every approved
    // business partner — who they are, what services they provide, their priority.
    // We scan the Eclipse extension point and populate our extension map the same way.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scans the Eclipse extension registry for all contributions to the entry editors
     * extension point and populates the internal {@code entryEditorExtensions} map.
     * Called once at startup. Each contributing plugin gets one {@link EntryEditorExtension}
     * bean, pre-loaded with icon, class name, priority, and a live editor instance.
     */
    private void initEntryEditorExtensions()
    {
        entryEditorExtensions = new HashMap<>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint( BrowserUIConstants.ENTRY_EDITOR_EXTENSION_POINT );
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();

        // For each extension:
        for ( IConfigurationElement member : members )
        {
            EntryEditorExtension bean = new EntryEditorExtension();

            IExtension extension = member.getDeclaringExtension();
            String extendingPluginId = extension.getNamespaceIdentifier();

            bean.setId( member.getAttribute( ID_ATTR ) );
            bean.setName( member.getAttribute( NAME_ATTR ) );
            bean.setDescription( member.getAttribute( DESCRIPTION_ATTR ) );
            String iconPath = member.getAttribute( ICON_ATTR );
            ImageDescriptor icon = AbstractUIPlugin.imageDescriptorFromPlugin( extendingPluginId, iconPath );

            if ( icon == null )
            {
                icon = ImageDescriptor.getMissingImageDescriptor();
            }

            bean.setIcon( icon );
            bean.setClassName( member.getAttribute( CLASS_ATTR ) );
            bean.setEditorId( member.getAttribute( EDITOR_ID_ATTR ) );
            bean.setMultiWindow( "true".equalsIgnoreCase( member.getAttribute( MULTI_WINDOW_ATTR ) ) ); //$NON-NLS-1$
            bean.setPriority( Integer.parseInt( member.getAttribute( PRIORITY_ATTR ) ) );

            try
            {
                bean.setEditorInstance( ( IEntryEditor ) member.createExecutableExtension( CLASS_ATTR ) );
            }
            catch ( CoreException e )
            {
                // Will never happen
            }

            entryEditorExtensions.put( bean.getId(), bean );
        }
    }


    // ── Lando Closes Cloud City and Pays Off the Staff ──────────────────────────
    // When the Empire moves in and the evacuation starts, Lando shuts down operations:
    // he removes the part listener and disconnects from the city-wide alert system.
    // We do the same here — deregister everything so we don't leak listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up all listeners registered by this manager.
     * Call this when the plugin is stopping. Failing to call this leaks the part
     * listener and the entry-update listener, which can cause NPEs after shutdown.
     */
    public void dispose()
    {
        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if ( ww != null )
        {
            ww.getPartService().removePartListener( partListener );
            EventRegistry.removeEntryUpdateListener( entryUpdateListener );
        }
    }


    // ── Lando Hands Over the Full Guest Manifest ─────────────────────────────────
    // Any department head who asks can get the complete list of approved business
    // partners — not sorted, just the raw approved roster.
    // We return all registered {@link EntryEditorExtension} values unordered.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all registered entry editor extensions, in no particular order.
     * Use {@link #getSortedEntryEditorExtensions()} if you need them in priority
     * order (e.g., to find the best editor for an entry).
     *
     * @return  an unordered collection of all known {@link EntryEditorExtension} instances
     */
    public Collection<EntryEditorExtension> getEntryEditorExtensions()
    {
        return entryEditorExtensions.values();
    }


    // ── Lando Looks Up a Specific Business Partner by Their Badge ID ─────────────
    // A visitor at the security desk presents their badge and Lando checks the registry:
    // "Yes, you're approved — here's your full profile."
    // We look up and return the one extension that has the given ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry editor extension with the given ID, or {@code null} if not found.
     * The ID comes from the {@code id} attribute in the extension point XML.
     *
     * @param id  the extension ID to look up
     * @return    the matching {@link EntryEditorExtension}, or {@code null}
     */
    public EntryEditorExtension getEntryEditorExtension( String id )
    {
        return entryEditorExtensions.get( id );
    }


    // ── Lando Ranks Business Partners Before the VIP Reception ──────────────────
    // Before the big reception, Lando decides who gets introduced first — VIPs by
    // rank, using the user's personal guest list if they supplied one, otherwise
    // defaulting to the official priority order.
    // We do the same: check the preference flag and delegate to the right sorter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all registered entry editor extensions sorted by the active priority strategy.
     * If the user has configured a personal priority order in preferences, that order wins.
     * Otherwise we fall back to the default priority from the plugin XML.
     * We iterate this list in order when auto-selecting the best editor for an entry.
     *
     * @return  a sorted collection of {@link EntryEditorExtension} instances, highest priority first
     */
    public Collection<EntryEditorExtension> getSortedEntryEditorExtensions()
    {
        boolean useUserPriority = BrowserUIPlugin.getDefault().getPluginPreferences().getBoolean(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USE_USER_PRIORITIES );

        if ( useUserPriority )
        {
            return getEntryEditorExtensionsSortedByUserPriority();
        }
        else
        {
            return getEntryEditorExtensionsSortedByDefaultPriority();
        }
    }


    // ── Lando Reads the Official Protocol Handbook for Priority Order ────────────
    // When no personal preference exists, Lando follows the official protocol handbook:
    // highest-ranking partner gets introduced first, ties broken alphabetically.
    // We sort extensions by the priority value in their plugin XML metadata.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all entry editor extensions sorted by the priority declared in each
     * plugin's extension point XML.
     * Higher priority value means earlier in the list. Ties are broken alphabetically
     * by editor name. Called when user-priority mode is disabled.
     *
     * @return  extensions sorted by their declared default priority, highest first
     */
    public Collection<EntryEditorExtension> getEntryEditorExtensionsSortedByDefaultPriority()
    {
        // Getting all entry editors
        Collection<EntryEditorExtension> entryEditorExtensions = getEntryEditorExtensions();

        // Creating the sorted entry editors list
        ArrayList<EntryEditorExtension> sortedEntryEditorsList = new ArrayList<>(
            entryEditorExtensions.size() );

        // Adding the remaining entry editors
        for ( EntryEditorExtension entryEditorExtension : entryEditorExtensions )
        {
            sortedEntryEditorsList.add( entryEditorExtension );
        }

        // Sorting the remaining entry editors based on their priority
        Collections.sort( sortedEntryEditorsList, entryEditorComparator );

        return sortedEntryEditorsList;
    }


    // ── Lando Uses the Baron Administrator's Personal Guest List ────────────────
    // When the user hands Lando their personal introduction order, he follows it —
    // even if it differs from the official protocol. Any editors that weren't on the
    // personal list (new plugins added since the list was made) go at the end.
    // We honour the user's stored priority string from preferences the same way.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all entry editor extensions sorted by the priority order the user
     * has saved in the preferences page.
     * The preference stores a comma-separated list of editor IDs. We process them
     * in order, then append any newly-installed editors that weren't in the saved
     * list (sorted by their default priority) so new plugins don't get ignored.
     *
     * @return  extensions in user-defined priority order, with any new ones appended at the end
     */
    public Collection<EntryEditorExtension> getEntryEditorExtensionsSortedByUserPriority()
    {
        // Getting all entry editors
        Collection<EntryEditorExtension> entryEditorExtensions = BrowserUIPlugin.getDefault().getEntryEditorManager()
            .getEntryEditorExtensions();

        // Creating the sorted entry editors list
        Collection<EntryEditorExtension> sortedEntryEditorsList = new ArrayList<>(
            entryEditorExtensions.size() );

        // Getting the user's priorities
        String userPriorities = BrowserUIPlugin.getDefault().getPluginPreferences().getString(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USER_PRIORITIES );

        if ( ( userPriorities != null ) && ( !"".equals( userPriorities ) ) ) //$NON-NLS-1$
        {
            String[] splittedUserPriorities = userPriorities.split( PRIORITIES_SEPARATOR );

            if ( ( splittedUserPriorities != null ) && ( splittedUserPriorities.length > 0 ) )
            {

                // Creating a map where entry editors are accessible via their ID
                Map<String, EntryEditorExtension> entryEditorsMap = new HashMap<>();

                for ( EntryEditorExtension entryEditorExtension : entryEditorExtensions )
                {
                    entryEditorsMap.put( entryEditorExtension.getId(), entryEditorExtension );
                }

                // Adding the entry editors according to the user's priority
                for ( String entryEditorId : splittedUserPriorities )
                {
                    // Verifying the entry editor is present in the map
                    if ( entryEditorsMap.containsKey( entryEditorId ) )
                    {
                        // Adding it to the sorted list
                        sortedEntryEditorsList.add( entryEditorsMap.get( entryEditorId ) );
                    }
                }
            }

            // If some new plugins have been added recently, their new
            // entry editors may not be present in the string stored in
            // the preferences.
            // We are then adding them at the end of the sorted list.

            // Creating a list of remaining entry editors
            List<EntryEditorExtension> remainingEntryEditors = new ArrayList<>();

            for ( EntryEditorExtension entryEditorExtension : entryEditorExtensions )
            {
                // Verifying the entry editor is present in the sorted list
                if ( !sortedEntryEditorsList.contains( entryEditorExtension ) )
                {
                    // Adding it to the remaining list
                    remainingEntryEditors.add( entryEditorExtension );
                }
            }

            // Sorting the remaining entry editors based on their priority
            Collections.sort( remainingEntryEditors, entryEditorComparator );

            // Adding the remaining entry editors
            for ( EntryEditorExtension entryEditorExtension : remainingEntryEditors )
            {
                sortedEntryEditorsList.add( entryEditorExtension );
            }
        }

        return sortedEntryEditorsList;
    }


    // ── Lando Closes All the Rooms Booked Under an Imperial Account ──────────────
    // When the Empire's deal goes south and the Imperials are expelled, Lando has to
    // walk through Cloud City and close every room that was on their tab — quickly.
    // We do the same: close all open editor tabs whose entry belongs to the given connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Closes all open entry editors that display entries from the given connection.
     * Called when the connection is closed or removed — it makes no sense to leave
     * editors open pointing at a server we're no longer connected to.
     *
     * @param connection  the connection that was closed or removed
     */
    private void closeEditorsBelongingToConnection( Connection connection )
    {
        if ( connection != null )
        {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

            // Collecting editor references to close
            List<IEditorReference> editorReferences = new ArrayList<>();

            for ( IEditorReference ref : activePage.getEditorReferences() )
            {
                IEntryEditor editor = getEntryEditor( ref );

                if ( ( editor != null ) && ( editor.getEntryEditorInput().getResolvedEntry() != null ) )
                {
                    IBrowserConnection bc = editor.getEntryEditorInput().getResolvedEntry().getBrowserConnection();

                    if ( connection.equals( bc.getConnection() ) )
                    {
                        editorReferences.add( ref );
                    }
                }
            }

            // Closing the corresponding editor references
            if ( !editorReferences.isEmpty() )
            {
                activePage.closeEditors( editorReferences.toArray( new IEditorReference[0] ), false );
            }
        }
    }


    // ── Lando Escorts a VIP to Their Assigned Suite with a Specific Host ─────────
    // When a special guest arrives with a preference for a particular host, Lando
    // hands them directly to that host's suite — no need to go through the normal
    // desk assignment process.
    // We open the editor using the specified extension, bypassing auto-selection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens an entry editor for the given entries/results/bookmarks using a specific
     * extension rather than auto-selecting one.
     * Spawns a {@link StudioBrowserJob} at interactive priority so attribute loading
     * happens in the background and the editor opens as soon as it's ready.
     *
     * @param extension     the specific editor extension to use; must not be {@code null}
     * @param entries       the entries to open (usually length 0 or 1)
     * @param searchResults the search results to open (usually length 0 or 1)
     * @param bookmarks     the bookmarks to open (usually length 0 or 1)
     */
    public void openEntryEditor( EntryEditorExtension extension, IEntry[] entries, ISearchResult[] searchResults,
        IBookmark[] bookmarks )
    {
        OpenEntryEditorRunnable runnable = new OpenEntryEditorRunnable( extension, entries, searchResults, bookmarks );
        StudioBrowserJob job = new StudioBrowserJob( runnable );
        job.setPriority( Job.INTERACTIVE ); // Highest priority (just in case)
        job.execute();
    }


    // ── Lando Assigns the Next Available Suite with No Preference Given ──────────
    // When a guest arrives without a host preference, Lando walks them to the best
    // available suite based on the priority manifest — whoever's ranked first gets the call.
    // We auto-select the best editor extension via {@link OpenEntryEditorRunnable}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens an entry editor for the given entries/results/bookmarks, auto-selecting
     * the best available editor extension by priority.
     * Delegates to {@link #openEntryEditor(EntryEditorExtension, IEntry[], ISearchResult[], IBookmark[])}
     * with a {@code null} extension — the runnable will pick the right one.
     *
     * @param entries       the entries to open (usually length 0 or 1)
     * @param searchResults the search results to open (usually length 0 or 1)
     * @param bookmarks     the bookmarks to open (usually length 0 or 1)
     */
    public void openEntryEditor( IEntry[] entries, ISearchResult[] searchResults, IBookmark[] bookmarks )
    {
        openEntryEditor( null, entries, searchResults, bookmarks );
    }


    // ── Refreshing the Suite's Reference Inventory from the Master Stock Room ────
    // When the official stock changes, Lando's crew updates the reference copy of the
    // room's inventory — removing the old snapshot and replacing it with the current
    // state from the master stock room, without disturbing the guest's own working list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the OSC reference copy of the given entry to reflect its current
     * server state, suspending event firing during the attribute replacement so we
     * don't trigger a cascade of redundant change notifications.
     *
     * @param entry  the original entry whose OSC reference copy should be refreshed
     */
    private void updateOscSharedReferenceCopy( IEntry entry )
    {
        IEntry referenceCopy = oscSharedReferenceCopies.remove( entry );

        if ( referenceCopy != null )
        {
            EventRegistry.suspendEventFiringInCurrentThread();
            EntryEditorUtils.ensureAttributesInitialized( entry );
            new CompoundModification().replaceAttributes( entry, referenceCopy, this );
            EventRegistry.resumeEventFiringInCurrentThread();
            oscSharedReferenceCopies.put( entry, referenceCopy );
        }
    }


    // ── Refreshing the Guest's Own Working Checklist from the Updated Reference ──
    // After the reference inventory is updated, the guest's own working copy is
    // brought in line — Lando's crew copies the new reference into the guest's folder.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the OSC working copy of the given entry to match the current server
     * state, typically called right after {@link #updateOscSharedReferenceCopy}.
     *
     * @param entry  the original entry whose OSC working copy should be refreshed
     */
    private void updateOscSharedWorkingCopy( IEntry entry )
    {
        IEntry workingCopy = oscSharedWorkingCopies.get( entry );

        if ( workingCopy != null )
        {
            EntryEditorUtils.ensureAttributesInitialized( entry );
            new CompoundModification().replaceAttributes( entry, workingCopy, this );
        }
    }


    // ── Silently Updating the Auto-Save Suite's Reference Behind the Scenes ──────
    // For suites on auto-pilot, the reference stock is updated quietly without
    // bothering the guest — events are suppressed during the refresh.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the auto-save reference copy of the given entry to match its current
     * server state, with event firing suppressed to avoid feedback loops.
     *
     * @param entry  the original entry whose auto-save reference copy should be refreshed
     */
    private void updateAutoSaveSharedReferenceCopy( IEntry entry )
    {
        EventRegistry.suspendEventFiringInCurrentThread();
        EntryEditorUtils.ensureAttributesInitialized( entry );
        IEntry workingCopy = autoSaveSharedReferenceCopies.get( entry );
        new CompoundModification().replaceAttributes( entry, workingCopy, this );
        EventRegistry.resumeEventFiringInCurrentThread();
    }


    // ── Refreshing the Auto-Save Guest's Working Copy Too ───────────────────────
    // The guest's own working copy on the auto-pilot suite is also updated — they
    // always see the latest state without needing to request a refresh.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the auto-save working copy of the given entry to match its current
     * server state. Called after {@link #updateAutoSaveSharedReferenceCopy} so both
     * copies stay in sync.
     *
     * @param entry  the original entry whose auto-save working copy should be refreshed
     */
    private void updateAutoSaveSharedWorkingCopy( IEntry entry )
    {
        EntryEditorUtils.ensureAttributesInitialized( entry );
        IEntry workingCopy = autoSaveSharedWorkingCopies.get( entry );
        new CompoundModification().replaceAttributes( entry, workingCopy, this );
    }


    // ── Finding All Rooms That Are on Manual-Checkout Mode ──────────────────────
    // Lando's staff walks through Cloud City and lists every suite where the guest
    // has to formally check out (manual-save) and is currently viewing a specific entry.
    // We collect all open OSC (non-auto-save) editors that hold the given working copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all currently open OSC (manual-save) entry editors that are using the
     * given working copy.
     * We scan all editor references on the active page and include any non-auto-save
     * editor whose shared working copy matches.
     *
     * @param workingCopy  the working copy entry to match against, or {@code null} to include all OSC editors
     * @return             a list of matching {@link IEntryEditor} instances
     */
    private List<IEntryEditor> getOscEditors( IEntry workingCopy )
    {
        List<IEntryEditor> oscEditors = new ArrayList<>();
        IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .getEditorReferences();

        for ( IEditorReference ref : editorReferences )
        {
            IEntryEditor editor = getEntryEditor( ref );

            if ( ( editor != null ) && !editor.isAutoSave()
                && ((  workingCopy == null ) || ( editor.getEntryEditorInput().getSharedWorkingCopy( editor ) == workingCopy ) ) )
            {
                oscEditors.add( editor );
            }
        }

        return oscEditors;
    }


    // ── Finding All Auto-Pilot Suites Viewing a Specific Entry ──────────────────
    // Similarly, Lando lists every suite on auto-pilot (auto-save) that is currently
    // watching the same entry, so changes can be broadcast to all of them at once.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all currently open auto-save entry editors that are using the given
     * working copy.
     * We scan all editor references on the active page and include any auto-save
     * editor whose shared working copy identity matches.
     *
     * @param workingCopy  the working copy entry to match against
     * @return             a list of matching auto-save {@link IEntryEditor} instances
     */
    private List<IEntryEditor> getAutoSaveEditors( IEntry workingCopy )
    {
        List<IEntryEditor> autoSaveEditors = new ArrayList<>();
        IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .getEditorReferences();

        for ( IEditorReference ref : editorReferences )
        {
            IEntryEditor editor = getEntryEditor( ref );

            if ( ( editor != null ) && editor.isAutoSave()
                && ( editor.getEntryEditorInput().getSharedWorkingCopy( editor ) == workingCopy ) )
            {
                autoSaveEditors.add( editor );
            }
        }

        return autoSaveEditors;
    }


    // ── Checking Whether a Workbench Part Is Actually an Entry Editor ─────────────
    // Not everyone in Cloud City is a guest in Lando's program — some are maintenance
    // crew, some are Imperials. We check whether a given room is an entry editor suite.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Casts the workbench part behind the given reference to {@link IEntryEditor}, or
     * returns {@code null} if the part is not an entry editor.
     * Uses {@code getPart(false)} to avoid triggering lazy part initialisation.
     *
     * @param partRef  the workbench part reference to inspect
     * @return         the part as an {@link IEntryEditor}, or {@code null}
     */
    private IEntryEditor getEntryEditor( IWorkbenchPartReference partRef )
    {
        IWorkbenchPart part = partRef.getPart( false );

        if ( part instanceof IEntryEditor )
        {
            return ( IEntryEditor ) part;
        }

        return null;
    }


    // ── Finding Which Suite Is Currently Receiving Lando's Direct Attention ──────
    // Of all the entry editors in the list, Lando wants to know which one is the
    // active suite right now — the one currently in focus on the workbench page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the workbench part reference for whichever editor in the list is
     * currently the active editor on its page, or {@code null} if none of them is active.
     * Used to find the right shell to parent a dialog on.
     *
     * @param editors  the list of entry editors to search through
     * @return         the {@link IWorkbenchPartReference} of the active editor, or {@code null}
     */
    private IWorkbenchPartReference getActivePartRef( List<IEntryEditor> editors )
    {
        for ( IEntryEditor editor : editors )
        {
            IWorkbenchPart part = ( IWorkbenchPart ) editor;
            IEditorPart activeEditor = part.getSite().getPage().getActiveEditor();

            if ( part == activeEditor )
            {
                return part.getSite().getPage().getReference( part );
            }
        }

        return null;
    }


    // ── Issuing a Room Key to the Guest — Create If It Doesn't Exist ────────────
    // When a guest checks in, Lando checks the key box. If there's already a key for
    // that room it's handed over; if not, a new one is cut, the reference copy is
    // made, and the working copy is cloned from it. Auto-save and OSC rooms are
    // tracked in separate key boxes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared working copy for the given original entry and editor.
     * If this is the first time this entry is being opened in an editor of this type,
     * we create both the reference copy and the working copy via {@link CompoundModification#cloneEntry}.
     * Subsequent calls for the same entry reuse the existing copies.
     *
     * @param originalEntry  the live entry from the connection cache
     * @param editor         the editor requesting the working copy (used to determine auto-save vs OSC)
     * @return               the shared working copy {@link IEntry} for this editor
     */
    IEntry getSharedWorkingCopy( IEntry originalEntry, IEntryEditor editor )
    {
        cleanupCopies( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() );

        EntryEditorUtils.ensureAttributesInitialized( originalEntry );

        if ( editor.isAutoSave() )
        {
            if ( !autoSaveSharedReferenceCopies.containsKey( originalEntry ) )
            {
                autoSaveSharedReferenceCopies
                    .put( originalEntry, new CompoundModification().cloneEntry( originalEntry ) );
            }

            if ( !autoSaveSharedWorkingCopies.containsKey( originalEntry ) )
            {
                IEntry referenceCopy = autoSaveSharedReferenceCopies.get( originalEntry );
                autoSaveSharedWorkingCopies.put( originalEntry, new CompoundModification().cloneEntry( referenceCopy ) );
            }

            return autoSaveSharedWorkingCopies.get( originalEntry );
        }
        else
        {
            if ( !oscSharedReferenceCopies.containsKey( originalEntry ) )
            {
                oscSharedReferenceCopies.put( originalEntry, new CompoundModification().cloneEntry( originalEntry ) );
            }

            if ( !oscSharedWorkingCopies.containsKey( originalEntry ) )
            {
                IEntry referenceCopy = oscSharedReferenceCopies.get( originalEntry );
                oscSharedWorkingCopies.put( originalEntry, new CompoundModification().cloneEntry( referenceCopy ) );
            }

            return oscSharedWorkingCopies.get( originalEntry );
        }
    }


    // ── Checking Whether the Guest Has Made Any Unauthorised Modifications ───────
    // Lando's staff compares the current state of the suite against the reference
    // inventory — any differences mean the guest has been "creative."
    // We diff the reference copy against the working copy and report dirty/clean.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the OSC working copy differs from its reference copy (i.e., has
     * unsaved changes).
     * Auto-save editors are never dirty by definition — they save immediately.
     * For OSC editors, we compute an LDIF diff; if the diff is non-null there are changes.
     *
     * @param originalEntry  the live entry from the connection cache
     * @param editor         the editor asking about its dirty state
     * @return               {@code true} if the working copy has uncommitted changes; always {@code false} for auto-save
     */
    boolean isSharedWorkingCopyDirty( IEntry originalEntry, IEntryEditor editor )
    {
        if ( editor.isAutoSave() )
        {
            return false;
        }
        else
        {
            IEntry referenceCopy = oscSharedReferenceCopies.get( originalEntry );
            IEntry workingCopy = oscSharedWorkingCopies.get( originalEntry );

            if ( ( referenceCopy != null ) && ( workingCopy != null ) )
            {
                LdifFile diff = Utils.computeDiff( referenceCopy, workingCopy );
                return diff != null;
            }

            return false;
        }
    }


    // ── Lando Submits the Final Invoice to the Empire Before Checkout ─────────────
    // When the guest is ready to leave, Lando computes the bill (LDIF diff), removes
    // the entry from the pending map to reduce event noise during the transaction,
    // submits it, then puts the entry back in the map and refreshes both copies.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the OSC working copy by computing an LDIF diff against the reference copy
     * and executing an {@link UpdateEntryRunnable} to write the changes to the server.
     * We temporarily remove the entry from the copy maps during execution to prevent
     * redundant change events from firing. On success both copies are refreshed.
     * Throws a {@link RuntimeException} if any attribute still has an empty value —
     * we refuse to save partial data.
     *
     * @param originalEntry  the live entry whose working copy should be saved
     * @param handleError    if {@code true}, show a dialog on failure
     * @param editor         the editor initiating the save, or {@code null} to force OSC save
     * @return               an {@link IStatus} with the save outcome, or {@code null} if nothing to save
     */
    IStatus saveSharedWorkingCopy( IEntry originalEntry, boolean handleError, IEntryEditor editor )
    {
        if ( ( editor == null ) || !editor.isAutoSave() )
        {
            IEntry referenceCopy = oscSharedReferenceCopies.get( originalEntry );
            IEntry workingCopy = oscSharedWorkingCopies.get( originalEntry );

            if ( ( referenceCopy != null ) && ( workingCopy != null ) )
            {
                // consistency check: don't save if there is an empty value, throw an exception as the user pressed 'save'
                for ( IAttribute attribute : workingCopy.getAttributes() )
                {
                    for ( IValue value : attribute.getValues() )
                    {
                        if ( value.isEmpty() )
                        {
                            throw new RuntimeException( NLS.bind( Messages
                                .getString( "EntryEditorManager.EmptyValueInAttribute" ), attribute.getDescription() ) ); //$NON-NLS-1$
                        }
                    }
                }

                LdifFile diff = Utils.computeDiff( referenceCopy, workingCopy );

                if ( diff != null )
                {
                    // remove entry from map, reduces number of fired events
                    oscSharedReferenceCopies.remove( originalEntry );
                    oscSharedWorkingCopies.remove( originalEntry );
                    // save by executing the LDIF
                    UpdateEntryRunnable runnable = new UpdateEntryRunnable( originalEntry, diff
                        .toFormattedString( LdifFormatParameters.DEFAULT ) );
                    IStatus status = RunnableContextRunner.execute( runnable, null, handleError );
                    // put entry back to map
                    oscSharedReferenceCopies.put( originalEntry, referenceCopy );
                    oscSharedWorkingCopies.put( originalEntry, workingCopy );

                    if ( status.isOK() )
                    {
                        updateOscSharedReferenceCopy( originalEntry );
                        updateOscSharedWorkingCopy( originalEntry );
                    }

                    return status;
                }
            }
        }

        return null;
    }


    // ── Lando Resets the Suite to the Original Inventory After Checkout ──────────
    // When the guest clicks "Revert," Lando discards their working changes and
    // restores both the reference and working copies to the current server state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Resets the OSC working copy and reference copy to the current server state,
     * discarding any unsaved changes.
     * Called when the user clicks Revert or when the editor is closing without saving.
     *
     * @param originalEntry  the live entry whose copies should be reset
     * @param editor         the editor requesting the reset, or {@code null}
     */
    void resetSharedWorkingCopy( IEntry originalEntry, IEntryEditor editor )
    {
        if ( ( editor == null ) || !editor.isAutoSave() )
        {
            IEntry referenceCopy = oscSharedReferenceCopies.get( originalEntry );
            IEntry workingCopy = oscSharedWorkingCopies.get( originalEntry );

            if ((  referenceCopy != null ) && ( workingCopy != null ) )
            {
                updateOscSharedReferenceCopy( originalEntry );
                updateOscSharedWorkingCopy( originalEntry );
            }
        }
    }


    // ── Lando Knocks and Asks the Guest: "New Inventory Arrived — Update Yours?" ─
    // Cloud City received a delivery that changes the suite's reference stock.
    // If the guest has made local modifications, Lando knocks politely and asks
    // whether they want to incorporate the new stock or keep their current list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shows a "The entry was changed externally — update your working copy?" dialog.
     * If the user says Yes, we refresh both copies and notify all OSC editors.
     * If they say No, the working copy keeps its unsaved changes and the stale
     * reference copy remains — the user accepted the risk of a conflict on save.
     *
     * @param partRef              the active editor part, used to parent the dialog
     * @param originalEntry        the live entry that was changed on the server
     * @param oscSharedWorkingCopy the current working copy for OSC editors
     * @param source               the event source that triggered this call
     */
    private void askUpdateSharedWorkingCopy( IWorkbenchPartReference partRef, IEntry originalEntry,
        IEntry oscSharedWorkingCopy, Object source )
    {
        MessageDialog dialog = new MessageDialog( partRef.getPart( false ).getSite().getShell(), Messages
            .getString( "EntryEditorManager.EntryChanged" ), null, Messages //$NON-NLS-1$
            .getString( "EntryEditorManager.EntryChangedDescription" ), MessageDialog.QUESTION, new String[] //$NON-NLS-1$
            { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0 );
        int result = dialog.open();

        if ( result == 0 )
        {
            // update reference copy and working copy
            updateOscSharedReferenceCopy( originalEntry );
            updateOscSharedWorkingCopy( originalEntry );

            // inform all OSC editors
            List<IEntryEditor> oscEditors = getOscEditors( oscSharedWorkingCopy );

            for ( IEntryEditor oscEditor : oscEditors )
            {
                oscEditor.workingCopyModified( source );
            }
        }
    }


    // ── Lando Sweeps Through Cloud City and Evicts Unclaimed Rooms ──────────────
    // Periodically, Lando's staff walks the corridors and clears out any copy pairs
    // for entries that no longer have an open editor — no guest means no need to
    // keep the working and reference copies in memory.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes copy pairs (reference + working) for any entry that is no longer open
     * in any editor on the given page.
     * Called whenever an editor is activated, closed, or its input changes — basically
     * whenever we might have leftover copies that are no longer needed.
     * This is a pure memory management step; no data is saved or discarded.
     *
     * @param page  the workbench page whose editor list we compare against our copy maps
     */
    private void cleanupCopies( IWorkbenchPage page )
    {
        // cleanup unused copies (OSC + auto-save)
        Set<IEntry> oscEntries = new HashSet<>();
        Set<IEntry> autoSaveEntries = new HashSet<>();
        IEditorReference[] editorReferences = page.getEditorReferences();

        for ( IEditorReference ref : editorReferences )
        {
            IEntryEditor editor = getEntryEditor( ref );

            if ( editor != null )
            {
                EntryEditorInput input = editor.getEntryEditorInput();

                if ( ( input != null ) && ( input.getResolvedEntry() != null ) )
                {
                    IEntry entry = input.getResolvedEntry();

                    if ( editor.isAutoSave() )
                    {
                        autoSaveEntries.add( entry );
                    }
                    else
                    {
                        oscEntries.add( entry );
                    }
                }
            }
        }

        for ( Iterator<IEntry> it = oscSharedReferenceCopies.keySet().iterator(); it.hasNext(); )
        {
            IEntry entry = it.next();

            if ( !oscEntries.contains( entry ) )
            {
                it.remove();
                oscSharedWorkingCopies.remove( entry );
            }
        }

        for ( Iterator<IEntry> it = oscSharedWorkingCopies.keySet().iterator(); it.hasNext(); )
        {
            IEntry entry = it.next();

            if ( !oscEntries.contains( entry ) )
            {
                it.remove();
            }
        }

        for ( Iterator<IEntry> it = autoSaveSharedReferenceCopies.keySet().iterator(); it.hasNext(); )
        {
            IEntry entry = it.next();

            if ( !autoSaveEntries.contains( entry ) )
            {
                it.remove();
            }
        }

        for ( Iterator<IEntry> it = autoSaveSharedWorkingCopies.keySet().iterator(); it.hasNext(); )
        {
            IEntry entry = it.next();

            if ( !autoSaveEntries.contains( entry ) )
            {
                it.remove();
            }
        }
    }
}
