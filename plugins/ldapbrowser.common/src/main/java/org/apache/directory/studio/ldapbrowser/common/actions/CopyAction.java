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
import java.util.LinkedHashSet;

import org.apache.directory.studio.ldapbrowser.common.actions.proxy.BrowserActionProxy;
import org.apache.directory.studio.ldapbrowser.common.dnd.EntryTransfer;
import org.apache.directory.studio.ldapbrowser.common.dnd.SearchTransfer;
import org.apache.directory.studio.ldapbrowser.common.dnd.ValuesTransfer;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldifparser.LdifUtils;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: CopyAction — CASSIAN AND JYN COPYING THE DEATH STAR PLANS ─────────
// On Scarif, Cassian and Jyn race through the data vault, pulling up the
// Death Star schematics on the terminal. They don't grab the whole vault —
// they identify exactly what the Alliance needs (entries, searches, or values)
// and transmit just that to the Rebel fleet above. The format depends on what's
// selected: DNs for entries, names for searches, display strings for values.
// CopyAction does the same: it looks at what's selected, decides what flavor
// of copy makes sense, serializes the data to both a typed transfer format
// and a plain-text format, then puts it all on the SWT clipboard.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies the currently selected LDAP objects (entries, searches, or values)
 * to the system clipboard in both a typed transfer format and plain text.
 * The typed format (EntryTransfer, SearchTransfer, ValuesTransfer) is used by
 * the paste action for in-application operations; the plain-text format lets
 * users paste into external tools like text editors.
 * Think of this class as Cassian and Jyn transmitting just the right data
 * from the Scarif data vault to the waiting Rebel fleet.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyAction extends BrowserAction
{
    private BrowserActionProxy pasteActionProxy;

    private ValueEditorManager valueEditorManager;


    // ── JYN PLUGS INTO THE TERMINAL — BASIC COPY CONFIGURED ──────────────────
    // Jyn connects to the data terminal with just the paste-target in mind —
    // she knows where the data is going but doesn't yet know which editor
    // will render the values. This constructor sets up the basic copy path.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a CopyAction linked to the given paste action proxy.
     * After a successful copy, we update the paste action so it knows
     * new data is available on the clipboard.
     * No value editor manager is set — values will be copied as raw strings
     * or base64 rather than via a display renderer.
     *
     * @param pasteActionProxy  the paste action to notify after copy; may be null
     *                          if no paste update is needed.
     */
    public CopyAction( BrowserActionProxy pasteActionProxy )
    {
        super();
        this.pasteActionProxy = pasteActionProxy;
    }


    // ── JYN PLUGS IN WITH FULL TRANSLATION — VALUE EDITOR CONFIGURED ──────────
    // Jyn connects to the terminal and also brings C-3PO to interpret the alien
    // data into a format the Rebel analysts can read. The value editor manager
    // knows how to render each attribute type's value as a display string.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a CopyAction with both a paste action proxy and a value editor
     * manager. The manager is used to render attribute values via their
     * configured display editors (e.g., showing a date instead of raw bytes)
     * when building the plain-text copy string.
     *
     * @param pasteActionProxy    the paste action to notify after copy.
     * @param valueEditorManager  the manager used to look up display renderers
     *                            for each attribute value type.
     */
    public CopyAction( BrowserActionProxy pasteActionProxy, ValueEditorManager valueEditorManager )
    {
        super();
        this.pasteActionProxy = pasteActionProxy;
        this.valueEditorManager = valueEditorManager;
    }


    // ── JYN READS THE TERMINAL DISPLAY — LABEL ADAPTS TO SELECTION ───────────
    // The terminal readout changes depending on what Jyn has selected: "Copy
    // Entry DN" when she's on a single record, "Copy Entries' DNs" for
    // multiple, "Copy Search" for saved query definitions, etc.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action, adapting to the current selection.
     * Shows singular or plural versions depending on how many objects are selected,
     * and picks the right noun (entries, searches, or values) based on what's selected.
     *
     * <p>For example — the terminal readout changing as Jyn selects more data:</p>
     * <pre>
     *   // one entry selected   → "Copy Entry DN"
     *   // multiple entries     → "Copy Entries' DNs"
     *   // one search selected  → "Copy Search"
     *   // multiple values      → "Copy Values"
     * </pre>
     *
     * @return the action label string, localized.
     */
    @Override
    public String getText()
    {
        // entry/searchresult/bookmark
        IEntry[] entries = getEntries();
        if ( entries != null )
        {
            return entries.length > 1 ? Messages.getString( "CopyAction.CopyEntriesDNs" ) : Messages.getString( "CopyAction.CopyEntryDN" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // searches
        ISearch[] searches = getSearches();
        if ( searches != null )
        {
            return searches.length > 1 ? Messages.getString( "CopyAction.CopySearches" ) : Messages.getString( "CopyAction.CopySearch" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // values
        IValue[] values = getValues();
        if ( values != null )
        {
            return values.length > 1 ? Messages.getString( "CopyAction.CopyValues" ) : Messages.getString( "CopyAction.CopyValue" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return Messages.getString( "CopyAction.Copy" ); //$NON-NLS-1$
    }


    // ── JYN HOLDS UP THE COPY INSIGNIA ────────────────────────────────────────
    // The action's icon identifies it visually in menus and toolbars — the
    // standard Eclipse copy icon, same one used everywhere.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the standard Eclipse "copy" icon descriptor from the shared
     * image registry.
     *
     * @return the copy tool image descriptor.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_TOOL_COPY );
    }


    // ── JYN BROADCASTS ON THE COPY CHANNEL ────────────────────────────────────
    // The command ID wires this action to the standard Ctrl+C keyboard shortcut
    // via Eclipse's key-binding system.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for the standard "copy" action, used
     * to bind this action to Ctrl+C (or the platform equivalent).
     *
     * @return the workbench copy command ID.
     */
    @Override
    public String getCommandId()
    {
        return IWorkbenchActionDefinitionIds.COPY;
    }


    // ── CASSIAN AND JYN TRANSMIT THE SELECTED DATA ────────────────────────────
    // Cassian starts the transmission: entries go as DNs (both typed and
    // plain-text), searches go as named search objects, values go with their
    // display string. Each data type gets its own path through the clipboard
    // API. After the copy, the paste action is notified so it can re-evaluate
    // its enabled state.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Executes the copy operation. Determines what's selected (entries, searches,
     * or values), serializes it to the SWT clipboard with both a typed transfer
     * and a plain-text transfer, then notifies the associated paste action to
     * re-evaluate its enabled state.
     * For values, uses the value editor manager (if set) to produce a
     * human-readable display string; falls back to the raw string or base64
     * encoding for binary values.
     *
     * <p>For example — Cassian and Jyn transmitting the Death Star plans:</p>
     * <pre>
     *   copyToClipboard(
     *     new Object[]{ entries, dnText },
     *     new Transfer[]{ EntryTransfer.getInstance(), TextTransfer.getInstance() }
     *   );
     * </pre>
     */
    @Override
    public void run()
    {
        IEntry[] entries = getEntries();
        ISearch[] searches = getSearches();
        IValue[] values = getValues();
        String[] stringProperties = getSelectedProperties();

        // entry/searchresult/bookmark
        if ( entries != null )
        {
            StringBuffer text = new StringBuffer();
            for ( int i = 0; i < entries.length; i++ )
            {
                text.append( entries[i].getDn().getName() );
                if ( i + 1 < entries.length )
                {
                    text.append( BrowserCoreConstants.LINE_SEPARATOR );
                }
            }
            copyToClipboard( new Object[]
                { entries, text.toString() }, new Transfer[]
                { EntryTransfer.getInstance(), TextTransfer.getInstance() } );
        }

        // searches
        if ( searches != null )
        {
            copyToClipboard( new Object[]
                { searches }, new Transfer[]
                { SearchTransfer.getInstance() } );
        }

        // values
        else if ( values != null )
        {
            StringBuffer text = new StringBuffer();

            for ( int i = 0; i < values.length; i++ )
            {
                IValue value = values[i];

                if ( valueEditorManager != null )
                {
                    IValueEditor ve = valueEditorManager.getCurrentValueEditor( value );
                    String displayValue = ve.getDisplayValue( value );
                    text.append( displayValue );
                }
                else if ( values[i].isString() )
                {
                    text.append( values[i].getStringValue() );
                }
                else if ( values[i].isBinary() )
                {
                    text.append( LdifUtils.base64encode( values[i].getBinaryValue() ) );
                }

                if ( i + 1 < values.length )
                {
                    text.append( BrowserCoreConstants.LINE_SEPARATOR );
                }
            }

            copyToClipboard( new Object[]
                { values, text.toString() }, new Transfer[]
                { ValuesTransfer.getInstance(), TextTransfer.getInstance() } );
        }

        // string properties
        else if ( stringProperties != null && stringProperties.length > 0 )
        {
            StringBuffer text = new StringBuffer();

            for ( int i = 0; i < stringProperties.length; i++ )
            {
                text.append( stringProperties[i] );
                if ( i + 1 < stringProperties.length )
                {
                    text.append( BrowserCoreConstants.LINE_SEPARATOR );
                }
            }

            copyToClipboard( new Object[]
                { text.toString() }, new Transfer[]
                { TextTransfer.getInstance() } );
        }

        // update paste action
        if ( this.pasteActionProxy != null )
        {
            this.pasteActionProxy.updateAction();
        }
    }


    // ── CASSIAN FEEDS THE DATA INTO THE TRANSMISSION TERMINAL ─────────────────
    // This static helper creates a fresh Clipboard, loads it with the data
    // arrays (typed objects + transfer agents), then disposes it. The clipboard
    // is a transient SWT resource — create it, use it, dispose it immediately.
    // Silently swallows IllegalArgumentException (e.g., when copying RootDSE)
    // to avoid spurious error dialogs.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Puts the given data arrays onto the SWT system clipboard using the
     * corresponding transfer agents. Each element in {@code data} must have
     * a matching {@link Transfer} in {@code dataTypes} at the same index.
     * The clipboard is created, populated, and disposed within this call.
     * Silently ignores {@link IllegalArgumentException} — this can happen
     * when the data is not suitable for the clipboard (e.g., copying the RootDSE).
     *
     * <p>For example — Cassian feeding data into the transmission terminal:</p>
     * <pre>
     *   CopyAction.copyToClipboard(
     *     new Object[]{ entries, dnText },
     *     new Transfer[]{ EntryTransfer.getInstance(), TextTransfer.getInstance() }
     *   );
     * </pre>
     *
     * @param data       the objects to copy; each must be compatible with the
     *                   corresponding transfer agent.
     * @param dataTypes  the transfer agents that convert each data element
     *                   to a platform-native format.
     */
    public static void copyToClipboard( Object[] data, Transfer[] dataTypes )
    {
        Clipboard clipboard = null;
        try
        {
            clipboard = new Clipboard( Display.getCurrent() );

            try
            {
                clipboard.setContents( data, dataTypes );
            }
            catch ( IllegalArgumentException e )
            {
                // Nothing to do.
                // Preventing an error to be shown in the case of the RootDSE being copied
                // See DIRSTUDIO-773 (IllegalArgumentException thrown when copying the RootDSE)
                // https://issues.apache.org/jira/browse/DIRSTUDIO-773
            }
        }
        finally
        {
            if ( clipboard != null )
                clipboard.dispose();
        }
    }


    // ── JYN CHECKS IF THE TERMINAL HAS DATA TO TRANSMIT ──────────────────────
    // Before Jyn starts the transmission, she confirms there's actually something
    // selected to copy — entries, searches, values, or string properties.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if there is something to copy: entries, searches,
     * values, or string properties. Returns {@code false} when the selection
     * is empty or contains only unsupported types.
     *
     * @return {@code true} if this action is currently copyable.
     */
    @Override
    public boolean isEnabled()
    {
        // entry/searchresult/bookmark
        if ( getEntries() != null )
        {
            return true;
        }

        // searches
        if ( getSearches() != null )
        {
            return true;
        }

        // values
        else if ( getValues() != null )
        {
            return true;
        }

        // string properties
        else if ( getSelectedProperties() != null && getSelectedProperties().length > 0 )
        {
            return true;
        }

        else
        {
            return false;
        }
    }


    // ── JYN IDENTIFIES WHICH ENTRIES ARE IN THE SELECTION ─────────────────────
    // Jyn scans the selected objects to see if they are entry-type targets
    // (IEntry, ISearchResult, or IBookmark). Only returns non-null if the
    // selection is purely entry-type — no searches, attributes, or values mixed in.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entries to copy, or {@code null} if the selection is not
     * purely entry-type (i.e., contains searches, attributes, or values).
     * Normalizes ISearchResult and IBookmark to their underlying IEntry.
     *
     * @return an array of entries to copy, or {@code null} if wrong selection type.
     */
    private IEntry[] getEntries()
    {
        if ( getSelectedConnections().length + getSelectedSearches().length + getSelectedAttributeHierarchies().length
            + getSelectedAttributes().length + getSelectedValues().length == 0
            && getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length > 0 )
        {
            LinkedHashSet<IEntry> entriesSet = new LinkedHashSet<IEntry>();
            for ( IEntry entry : getSelectedEntries() )
            {
                entriesSet.add( entry );
            }
            for ( ISearchResult searchResult : getSelectedSearchResults() )
            {
                entriesSet.add( searchResult.getEntry() );
            }
            for ( IBookmark bookmark : getSelectedBookmarks() )
            {
                entriesSet.add( bookmark.getEntry() );
            }
            return entriesSet.toArray( new IEntry[entriesSet.size()] );
        }
        else
        {
            return null;
        }
    }


    // ── JYN IDENTIFIES WHICH SEARCHES ARE IN THE SELECTION ────────────────────
    // Jyn checks whether the selection is purely saved-search objects. Returns
    // null if anything else (entries, attributes, values) is mixed in.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the searches to copy, or {@code null} if the selection contains
     * non-search objects. Only returns the searches when they are the sole
     * type in the selection.
     *
     * @return the array of selected searches, or {@code null} if the selection
     *         contains other types.
     */
    private ISearch[] getSearches()
    {
        if ( getSelectedConnections().length + getSelectedEntries().length + getSelectedSearchResults().length
            + getSelectedBookmarks().length + getSelectedAttributeHierarchies().length + getSelectedAttributes().length
            + getSelectedValues().length == 0
            && getSelectedSearches().length > 0 )
        {
            LinkedHashSet<ISearch> searchesSet = new LinkedHashSet<ISearch>();
            for ( ISearch search : getSelectedSearches() )
            {
                searchesSet.add( search );
            }
            return searchesSet.toArray( new ISearch[searchesSet.size()] );
        }
        else
        {
            return null;
        }
    }


    // ── JYN IDENTIFIES WHICH VALUES ARE IN THE SELECTION ──────────────────────
    // Jyn checks whether the selection is attribute/value-level objects.
    // Normalizes AttributeHierarchy and IAttribute to their constituent values.
    // Returns null if entries, searches, or connections are mixed in.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute values to copy, or {@code null} if the selection
     * contains non-value types. Expands AttributeHierarchy and IAttribute
     * to their constituent IValue objects, then adds any directly selected values.
     *
     * @return the array of values to copy, or {@code null} if wrong selection type.
     */
    private IValue[] getValues()
    {
        if ( getSelectedConnections().length + getSelectedBookmarks().length + getSelectedEntries().length
            + getSelectedSearches().length == 0
            && getSelectedAttributeHierarchies().length + getSelectedAttributes().length + getSelectedValues().length > 0 )
        {
            LinkedHashSet<IValue> valuesSet = new LinkedHashSet<IValue>();
            for ( AttributeHierarchy ah : getSelectedAttributeHierarchies() )
            {
                for ( IAttribute attribute : ah.getAttributes() )
                {
                    valuesSet.addAll( Arrays.asList( attribute.getValues() ) );
                }
            }
            for ( IAttribute attribute : getSelectedAttributes() )
            {
                valuesSet.addAll( Arrays.asList( attribute.getValues() ) );
            }
            for ( IValue value : getSelectedValues() )
            {
                valuesSet.add( value );
            }
            return valuesSet.toArray( new IValue[valuesSet.size()] );
        }
        else
        {
            return null;
        }
    }
}
