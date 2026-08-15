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

package org.apache.directory.studio.ldifeditor.editor;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserLabelProvider;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeDeleteRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModDnRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;


// ── CLASS: LdifOutlinePage — REBEL TRANSMISSION INDEX ─────────────────────────
// Alliance archivists paste a brief index on the front cover of each
// transmission bundle: one line per record showing the target entry's DN and
// record type.  Clicking a line in the index jumps to that record in the full
// text.
// LdifOutlinePage is that index: a tree view of LDIF records, attributes, and
// values that stays in sync with the editor and lets the user navigate by
// clicking.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link ContentOutlinePage} for the LDIF editor.
 * Presents the parsed {@link LdifFile} as a tree of records, attribute groups,
 * and individual values with type-specific icons.
 * Clicking a record, attribute group, or value reveals and selects the
 * corresponding text range in the editor.
 * Think of this as the Alliance transmission index: a quick overview of all
 * records in the file with click-to-navigate.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifOutlinePage extends ContentOutlinePage
{
    /** The editor it is attached to */
    private LdifEditor ldifEditor;

    /** Whether or not the outline page is linked to an entry in the LDAP Browser view*/
    private boolean isLinkedToLdapBrowser = false;


    // ── CONSTRUCT STANDALONE ──────────────────────────────────────────────────
    // The archivist picks up their index pad and ties it to the editor.
    /**
     * Creates a new outline page attached to {@code ldifEditor}.
     *
     * @param ldifEditor  the LDIF editor to reflect
     */
    public LdifOutlinePage( LdifEditor ldifEditor )
    {
        this.ldifEditor = ldifEditor;
    }


    // ── CONSTRUCT LINKED TO LDAP BROWSER ──────────────────────────────────────
    // The archivist can also work in linked mode where record icons are
    // resolved from the live directory connection.
    /**
     * Creates a new outline page attached to {@code ldifEditor}, optionally
     * linked to the LDAP Browser view for richer icons.
     *
     * @param ldifEditor            the LDIF editor to reflect
     * @param isLinkedToLdapBrowser {@code true} to resolve entry icons from
     *                              the browser connection
     */
    public LdifOutlinePage( LdifEditor ldifEditor, boolean isLinkedToLdapBrowser )
    {
        this.ldifEditor = ldifEditor;
        this.isLinkedToLdapBrowser = isLinkedToLdapBrowser;
    }


    // ── CREATE THE OUTLINE TREE ───────────────────────────────────────────────
    // The archivist opens the index pad, wires in the record label provider
    // and content provider, and registers the selection and double-click handlers.
    /**
     * {@inheritDoc}
     *
     * <p>Configures the tree viewer with {@link LdifLabelProvider} and
     * {@link LdifContentProvider}, then wires selection-changed and
     * double-click listeners before triggering the first full refresh.</p>
     */
    public void createControl( Composite parent )
    {
        super.createControl( parent );

        final TreeViewer treeViewer = getTreeViewer();
        treeViewer.setLabelProvider( new LdifLabelProvider( ldifEditor, isLinkedToLdapBrowser ) );
        treeViewer.setContentProvider( new LdifContentProvider() );

        if ( isLinkedToLdapBrowser )
        {
            treeViewer.setAutoExpandLevel( 2 );
        }

        treeViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                if ( !event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection )
                {
                    Object element = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();

                    if ( element instanceof LdifRecord )
                    {
                        LdifRecord ldifRecord = ( LdifRecord ) element;
                        ldifEditor.selectAndReveal( ldifRecord.getDnLine().getOffset(), ldifRecord.getDnLine()
                            .getLength() );
                    }
                    else if ( element instanceof List )
                    {
                        List<?> list = ( List<?> ) element;

                        if ( !list.isEmpty() && list.get( 0 ) instanceof LdifAttrValLine )
                        {
                            LdifAttrValLine line = ( LdifAttrValLine ) list.get( 0 );
                            ldifEditor.selectAndReveal( line.getOffset(), line.getRawAttributeDescription().length() );
                        }
                    }
                    else if ( element instanceof LdifAttrValLine )
                    {
                        LdifAttrValLine line = ( LdifAttrValLine ) element;
                        ldifEditor.selectAndReveal( line.getOffset() + line.getRawAttributeDescription().length()
                            + line.getRawValueType().length(), line.getRawValue().length() );
                    }
                    else if ( element instanceof LdifModSpec )
                    {
                        LdifModSpec modSpec = ( LdifModSpec ) element;
                        ldifEditor.selectAndReveal( modSpec.getOffset(), modSpec.getModSpecType().getLength() );
                    }
                }
            }
        } );

        treeViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                if ( event.getSelection() instanceof IStructuredSelection )
                {
                    Object obj = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();
                    if ( treeViewer.getExpandedState( obj ) )
                        treeViewer.collapseToLevel( obj, 1 );
                    else if ( ( ( ITreeContentProvider ) treeViewer.getContentProvider() ).hasChildren( obj ) )
                        treeViewer.expandToLevel( obj, 1 );
                }
            }
        } );

        this.refresh();
    }


    // ── PARTIAL REFRESH ───────────────────────────────────────────────────────
    // When a single record changes the archivist updates only that page of
    // the index.
    /**
     * Refreshes the tree starting from {@code element}, if the tree is not disposed.
     *
     * @param element  the tree element to refresh from
     */
    public void refresh( Object element )
    {
        final TreeViewer treeViewer = getTreeViewer();
        if ( treeViewer != null && treeViewer.getTree() != null && !treeViewer.getTree().isDisposed() )
        {
            treeViewer.refresh( element );
        }
    }


    // ── FULL REFRESH ──────────────────────────────────────────────────────────
    // The archivist re-stamps the entire index from the current model.
    /**
     * Refreshes the entire outline tree from the editor's current LDIF model.
     * No-ops if the tree is disposed.
     */
    public void refresh()
    {
        final TreeViewer treeViewer = getTreeViewer();

        if ( treeViewer != null && treeViewer.getTree() != null && !treeViewer.getTree().isDisposed() )
        {
            // ISelection selection = treeViewer.getSelection();
            // Object[] expandedElements = treeViewer.getExpandedElements();

            if ( !treeViewer.getTree().isEnabled() )
            {
                treeViewer.getTree().setEnabled( true );
            }

            if ( ldifEditor != null )
            {
                if ( treeViewer.getInput() != ldifEditor.getLdifModel() )
                {
                    treeViewer.setInput( ldifEditor.getLdifModel() );
                }
            }

            treeViewer.refresh();

            if ( isLinkedToLdapBrowser )
            {
                treeViewer.setAutoExpandLevel( 2 );
            }

            // treeViewer.setSelection(selection);
            // treeViewer.setExpandedElements(expandedElements);
        }
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────
    // The archivist closes the index pad and notifies the editor that the
    // outline page is gone.
    /**
     * {@inheritDoc}
     *
     * <p>Notifies the editor that the outline page has closed, then releases
     * the reference.</p>
     */
    public void dispose()
    {
        super.dispose();
        if ( ldifEditor != null )
        {
            ldifEditor.outlinePageClosed();
            ldifEditor = null;
        }
    }

    // ── CLASS: LdifContentProvider — TREE STRUCTURE ───────────────────────────
    // The content provider maps the parsed LdifFile tree (file → records →
    // attribute groups → individual values) to tree nodes.
    /**
     * {@link ITreeContentProvider} that maps a {@link LdifFile} to a tree of
     * records, attribute groups (lists of {@link LdifAttrValLine}), and individual
     * attribute-value lines or mod-spec items.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private static class LdifContentProvider implements ITreeContentProvider
    {
        /**
         * {@inheritDoc}
         */
        public Object[] getChildren( Object element )
        {
            // file --> records
            if ( element instanceof LdifFile )
            {
                LdifFile ldifFile = ( LdifFile ) element;
                return ldifFile.getRecords();
            }

            // record --> Array of List of AttrValLine
            else if ( element instanceof LdifContentRecord )
            {
                LdifContentRecord record = ( LdifContentRecord ) element;
                return getUniqueAttrValLineArray( record.getAttrVals() );
            }
            else if ( element instanceof LdifChangeAddRecord )
            {
                LdifChangeAddRecord record = ( LdifChangeAddRecord ) element;
                return getUniqueAttrValLineArray( record.getAttrVals() );
            }
            else if ( element instanceof LdifChangeModifyRecord )
            {
                LdifChangeModifyRecord record = ( LdifChangeModifyRecord ) element;
                return record.getModSpecs();
            }
            else if ( element instanceof LdifChangeModDnRecord )
            {
                return new Object[0];
            }
            else if ( element instanceof LdifChangeDeleteRecord )
            {
                return new Object[0];
            }

            // List of AttrValLine --> Array of AttrValLine
            else if ( element instanceof List && ( ( List<?> ) element ).get( 0 ) instanceof LdifAttrValLine )
            {
                List<?> list = ( List<?> ) element;
                return list.toArray();
            }
            else if ( element instanceof LdifModSpec )
            {
                LdifModSpec modSpec = ( LdifModSpec ) element;
                return modSpec.getAttrVals();
            }

            else
            {
                return new Object[0];
            }
        }


        /**
         * Groups {@code lines} by attribute description (preserving insertion order)
         * and returns an array of {@link List} instances, one per unique attribute.
         *
         * @param lines  the attribute-value lines to group
         * @return       an array of attribute-value line lists
         */
        private Object[] getUniqueAttrValLineArray( LdifAttrValLine[] lines )
        {
            Map<String, List<LdifAttrValLine>> uniqueAttrMap = new LinkedHashMap<String, List<LdifAttrValLine>>();

            for ( LdifAttrValLine ldifAttrValLine : lines )
            {
                String key = ldifAttrValLine.getUnfoldedAttributeDescription();
                List<LdifAttrValLine> listLdifAttrValLine = uniqueAttrMap.get( key );

                if ( listLdifAttrValLine == null )
                {
                    listLdifAttrValLine = new ArrayList<LdifAttrValLine>();
                    uniqueAttrMap.put( key, listLdifAttrValLine );
                }

                listLdifAttrValLine.add( ldifAttrValLine );
            }

            return uniqueAttrMap.values().toArray();
        }


        /**
         * {@inheritDoc}
         */
        public Object getParent( Object element )
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        public boolean hasChildren( Object element )
        {
            return getChildren( element ) != null && getChildren( element ).length > 0;
        }


        /**
         * {@inheritDoc}
         */
        public Object[] getElements( Object inputElement )
        {
            return getChildren( inputElement );
        }


        /**
         * {@inheritDoc}
         */
        public void dispose()
        {
        }


        /**
         * {@inheritDoc}
         */
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }
    }

    // ── CLASS: LdifLabelProvider — RECORD / ATTRIBUTE / VALUE ICONS ───────────
    // Each tree node gets a label and an icon matching its LDIF record type
    // or attribute role.
    /**
     * {@link LabelProvider} for the outline tree.
     * Returns DN strings for records, attribute-group summaries ("cn (3)"),
     * shortened value strings for individual lines, and type-appropriate icons.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private static class LdifLabelProvider extends LabelProvider
    {
        /** The editor it is attached to */
        private LdifEditor ldifEditor;

        /** Whether or not the outline page is linked to an entry in the LDAP Browser view*/
        private boolean isLinkedToLdapBrowser = false;


        /**
         * Creates a label provider for the outline tree.
         *
         * @param ldifEditor            the attached editor
         * @param isLinkedToLdapBrowser whether to resolve entry icons from the browser
         */
        public LdifLabelProvider( LdifEditor ldifEditor, boolean isLinkedToLdapBrowser )
        {
            super();
            this.ldifEditor = ldifEditor;
            this.isLinkedToLdapBrowser = isLinkedToLdapBrowser;
        }


        /**
         * {@inheritDoc}
         */
        public String getText( Object element )
        {
            // Record
            if ( element instanceof LdifRecord )
            {
                LdifRecord ldifRecord = ( LdifRecord ) element;
                return ldifRecord.getDnLine().getValueAsString();
            }

            // List of AttrValLine
            else if ( element instanceof List && ( ( List<?> ) element ).get( 0 ) instanceof LdifAttrValLine )
            {
                List<?> list = ( List<?> ) element;
                return ( ( LdifAttrValLine ) list.get( 0 ) ).getUnfoldedAttributeDescription() + " (" + list.size() //$NON-NLS-1$
                    + ")"; //$NON-NLS-1$
            }
            else if ( element instanceof LdifModSpec )
            {
                LdifModSpec modSpec = ( LdifModSpec ) element;
                return modSpec.getModSpecType().getUnfoldedAttributeDescription() + " (" + modSpec.getAttrVals().length //$NON-NLS-1$
                    + ")"; //$NON-NLS-1$
            }

            // AttrValLine
            else if ( element instanceof LdifAttrValLine )
            {
                LdifAttrValLine line = ( LdifAttrValLine ) element;
                return Utils.getShortenedString( line.getValueAsString(), 20 );
            }

            else
            {
                return ""; //$NON-NLS-1$
            }
        }


        /**
         * {@inheritDoc}
         */
        public Image getImage( Object element )
        {

            // Record
            if ( element instanceof LdifContentRecord )
            {
                if ( isLinkedToLdapBrowser )
                {
                    LdifContentRecord record = ( LdifContentRecord ) element;

                    LdifDnLine dnLine = record.getDnLine();

                    if ( dnLine != null )
                    {
                        String dn = dnLine.getUnfoldedDn();

                        if ( ( dn != null ) && ( dn.length() == 0 ) ) //$NON-NLS-1$
                        {
                            // Root DSE
                            return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_ENTRY_ROOT );
                        }
                        else
                        {
                            // Any other case
                            try
                            {
                                return BrowserLabelProvider.getImageByObjectClass( ldifEditor.getConnection()
                                    .getEntryFromCache( new Dn( dn ) ) );
                            }
                            catch ( LdapInvalidDnException e )
                            {
                                // Will never occur
                            }
                        }
                    }
                }

                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_ENTRY );
            }
            else if ( element instanceof LdifChangeAddRecord )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_ADD );
            }
            else if ( element instanceof LdifChangeModifyRecord )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MODIFY );
            }
            else if ( element instanceof LdifChangeDeleteRecord )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_DELETE );
            }
            else if ( element instanceof LdifChangeModDnRecord )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_RENAME );
            }
            // List of AttrValLine
            else if ( element instanceof List && ( ( List ) element ).get( 0 ) instanceof LdifAttrValLine )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_ATTRIBUTE );
            }
            else if ( element instanceof LdifModSpec )
            {
                LdifModSpec modSpec = ( LdifModSpec ) element;

                if ( modSpec.isAdd() )
                {
                    return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MOD_ADD );
                }
                else if ( modSpec.isReplace() )
                {
                    return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MOD_REPLACE );
                }
                else if ( modSpec.isDelete() )
                {
                    return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MOD_DELETE );
                }
                else
                {
                    return null;
                }
            }

            // AttrValLine
            else if ( element instanceof LdifAttrValLine )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_VALUE );
            }
            else
            {
                return null;
            }
        }
    }
}
