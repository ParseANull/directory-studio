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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserLabelProvider;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;


// ── CLASS: EntryEditorOutlinePage — LUKE'S BINARY SUNSET ON TATOOINE ─────────
// Luke stands on the ridge at dusk, watching two suns dip below the horizon.
// For the first time he sees the whole landscape at once — not the farm chores,
// not the garage, but the full sweep of the desert world stretching to the horizon.
// EntryEditorOutlinePage is that ridge: it gives the user a high-level bird's-eye
// view of the LDAP entry — entry → attributes → values — alongside the detail editor,
// so they can see the big picture and jump straight to any attribute they care about.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse Outline view page that accompanies the entry editor.
 * While the main editor shows a detailed table of attributes and values,
 * this outline page shows a compact tree — entry at the root, attributes as
 * children, values nested under each attribute — so the user can see the whole
 * entry structure and click to jump to any attribute quickly.
 * Think of this as Luke's binary sunset view: pull back, see the whole landscape,
 * then zero in on the part that matters.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorOutlinePage extends ContentOutlinePage
{
    /** The editor it is attached to */
    private EntryEditor entryEditor;

    /** This listener updates the viewer if an property (e.g. is operational attributes visible) has been changed */
    protected IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener()
    {
        public void propertyChange( PropertyChangeEvent event )
        {
            refresh();
        }
    };

    private Composite noOutlineComposite;

    private Composite composite;

    private Composite fakeComposite;


    // ── LUKE CLIMBS TO THE RIDGE FOR THE FIRST TIME ───────────────────────────
    // Young Luke scrambles up to the ridge to watch the binary sunset, and for
    // the first time really sees Tatooine stretching out around him.
    // We store a reference to the entry editor and subscribe to preference changes
    // so this outline page can refresh whenever display settings change.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new outline page attached to the given entry editor.
     * We immediately subscribe to preference change events so the outline refreshes
     * when the user toggles operational-attributes visibility or other display options.
     *
     * @param entryEditor  The entry editor whose content we're providing an outline for.
     */
    public EntryEditorOutlinePage( EntryEditor entryEditor )
    {
        this.entryEditor = entryEditor;
        BrowserCommonActivator.getDefault().getPreferenceStore().addPropertyChangeListener( propertyChangeListener );
    }


    // ── LUKE LOOKS DOWN AT THE RIDGE ITSELF ──────────────────────────────────
    // Luke's vantage point — the ridge — is the physical thing he's standing on.
    // We return the composite control that acts as our outline page's container.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the root SWT control of this outline page.
     * Eclipse checks this to detect whether the page has been created and
     * whether it's still alive (not disposed).
     *
     * @return the root composite, or {@code null} if not yet created.
     */
    public Control getControl()
    {
        return composite;
    }


    // ── LUKE'S VIEW TAKES SHAPE AS HE REACHES THE SUMMIT ────────────────────
    // As Luke crests the ridge, the full panorama assembles before him — two suns,
    // the flat desert, the homestead below — all snapping into place at once.
    // We build the outline's two states: a message panel for when there's no entry,
    // and a tree viewer for when there is one, then wire up selection sync so
    // clicking in the outline highlights the matching row in the main editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the outline page's SWT UI inside the given parent composite.
     * Eclipse calls this once when the Outline view first shows our editor's page.
     * We create two mutually exclusive sub-composites — one tree viewer and one
     * "no outline" message — and swap between them based on whether an entry is loaded.
     * Selection in the outline tree drives selection in the main attribute-table viewer.
     *
     * @param parent  The SWT composite that Eclipse provides as our container.
     */
    public void createControl( Composite parent )
    {
        // Creating the composite and fake composite
        this.composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new FillLayout() );
        this.fakeComposite = new Composite( parent, SWT.NONE );

        // Creating the No Outline composite
        noOutlineComposite = new Composite( composite, SWT.NONE );
        noOutlineComposite.setLayout( new FillLayout() );

        Label label = new Label( noOutlineComposite, SWT.WRAP );
        label.setText( Messages.getString( "EntryEditorOutlinePage.NoOutline" ) ); //$NON-NLS-1$

        // Creating the Outline tree viewer
        super.createControl( parent );

        final TreeViewer treeViewer = getTreeViewer();
        treeViewer.setLabelProvider( new EntryEditorOutlineLabelProvider() );
        treeViewer.setContentProvider( new EntryEditorOutlineContentProvider() );

        treeViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                if ( !event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection )
                {
                    if ( getEntryEditorWidgetTreeViewerInput() != null )
                    {
                        List<Object> selectionList = new ArrayList<Object>();

                        for ( Object element : ( ( IStructuredSelection ) event.getSelection() ).toArray() )
                        {
                            if ( element instanceof IValue )
                            {
                                // select the value
                                IValue value = ( IValue ) element;
                                selectionList.add( value );
                            }
                            else if ( element instanceof IAttribute )
                            {
                                // select attribute and all values
                                IAttribute attribute = ( IAttribute ) element;
                                selectionList.add( attribute );
                                selectionList.addAll( Arrays.asList( attribute.getValues() ) );
                            }
                            else if ( element instanceof EntryWrapper )
                            {
                                // select all attributes and values
                                IEntry entry = ( ( EntryWrapper ) element ).entry;
                                for ( IAttribute attribute : entry.getAttributes() )
                                {
                                    selectionList.add( attribute );
                                    selectionList.addAll( Arrays.asList( attribute.getValues() ) );
                                }
                            }
                        }

                        IStructuredSelection selection = new StructuredSelection( selectionList );
                        TreeViewer entryEditorWidgetTreeViewer = getEntryEditorWidgetTreeViewer();
                        if ( entryEditorWidgetTreeViewer != null )
                        {
                            entryEditorWidgetTreeViewer.setSelection( selection );
                        }
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
                    {
                        treeViewer.collapseToLevel( obj, 1 );
                    }
                    else if ( ( ( ITreeContentProvider ) treeViewer.getContentProvider() ).hasChildren( obj ) )
                    {
                        treeViewer.expandToLevel( obj, 1 );
                    }
                }
            }
        } );

        this.refresh();
    }


    // ── LUKE FOCUSES ON ONE SPOT OF THE LANDSCAPE ────────────────────────────
    // Luke zooms in on a specific dune or structure in the panorama — not
    // rebuilding the whole view, just refreshing the piece that changed.
    // We sync filters from the main viewer and then refresh just the given element.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes a single element in the outline tree without rebuilding everything.
     * We first sync the outline's filters with the main entry editor widget so
     * operational attributes hide/show consistently, then ask the tree to repaint
     * just the subtree rooted at the given element.
     *
     * @param element  The model object whose corresponding tree node should be refreshed.
     */
    public void refresh( Object element )
    {
        final TreeViewer treeViewer = getTreeViewer();
        if ( treeViewer != null && treeViewer.getTree() != null && !treeViewer.getTree().isDisposed() )
        {
            TreeViewer entryEditorWidgetTreeViewer = getEntryEditorWidgetTreeViewer();
            if ( entryEditorWidgetTreeViewer != null )
            {
                treeViewer.setFilters( entryEditorWidgetTreeViewer.getFilters() );
            }
            treeViewer.refresh( element );
        }
    }


    // ── LUKE STEPS BACK AND SEES THE WHOLE HORIZON AGAIN ─────────────────────
    // Luke takes his eyes off one dune and sweeps his gaze across the whole
    // twin-sunset panorama — everything fresh, nothing stale from the old view.
    // We swap the visible composite based on whether there's an entry to show,
    // sync filters, and rebuild the full tree from the current model input.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fully rebuilds the outline tree from the current entry editor state.
     * Called when the editor input changes or preferences change — we swap between
     * the tree viewer and the "no outline" message, re-apply filters, and reload
     * the tree structure from the entry model.
     */
    public void refresh()
    {
        TreeViewer treeViewer = getTreeViewer();

        if ( treeViewer != null && composite != null )
        {
            if ( hasAnOutline() )
            {
                Control treeViewerControl = treeViewer.getControl();

                if ( ( treeViewerControl != null ) && ( !treeViewerControl.isDisposed() ) )
                {
                    treeViewerControl.setParent( composite );
                }

                noOutlineComposite.setParent( fakeComposite );
            }
            else
            {
                Control treeViewerControl = treeViewer.getControl();

                if ( ( treeViewerControl != null ) && ( !treeViewerControl.isDisposed() ) )
                {
                    treeViewerControl.setParent( fakeComposite );
                }

                noOutlineComposite.setParent( composite );
            }

            composite.layout();

            if ( treeViewer.getTree() != null && !treeViewer.getTree().isDisposed() )
            {
                TreeViewer entryEditorWidgetTreeViewer = getEntryEditorWidgetTreeViewer();
                if ( entryEditorWidgetTreeViewer != null )
                {
                    treeViewer.setFilters( entryEditorWidgetTreeViewer.getFilters() );
                }
                if ( !treeViewer.getTree().isEnabled() )
                {
                    treeViewer.getTree().setEnabled( true );
                }

                IEntry entry = getEntryEditorWidgetTreeViewerInput();
                if ( entry == null )
                {
                    treeViewer.setInput( null );
                    treeViewer.getTree().setEnabled( false );
                }
                else
                {
                    treeViewer.setInput( entry );
                    treeViewer.expandToLevel( 2 );
                }

                treeViewer.refresh();
            }
        }
    }


    // ── LUKE LEAVES THE RIDGE AS THE SUNS SET ────────────────────────────────
    // The suns finally disappear; Luke turns and heads back to the homestead.
    // He won't watch from this ridge again — we unsubscribe from preferences
    // and null out the editor reference to let GC reclaim everything cleanly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up this outline page when the editor is closed.
     * We remove our preference listener (so the editor isn't kept alive by the
     * preference store) and null the editor reference before delegating to super.
     */
    public void dispose()
    {
        super.dispose();
        if ( entryEditor != null )
        {
            BrowserCommonActivator.getDefault().getPreferenceStore().removePropertyChangeListener(
                propertyChangeListener );
            entryEditor = null;
        }
    }

    // ── CLASS: EntryEditorOutlineContentProvider — MAPPING THE DESERT TERRAIN ─
    // Luke scans the terrain and maps it: homestead at the top, outbuildings beneath,
    // sand crawler tracks further down. The content provider does the same for LDAP:
    // entry → attributes → values, three levels, no shortcuts.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * The JFace content provider that maps an {@link IEntry} to the tree hierarchy.
     * Returns entry → attribute → value children in that order so the outline
     * tree always shows a consistent three-level structure.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private static class EntryEditorOutlineContentProvider implements ITreeContentProvider
    {
        // ── READING THE TERRAIN LAYER BY LAYER ───────────────────────────────
        // Luke reads the map layer by layer: homestead on the surface, tunnels below,
        // individual rooms at the deepest level. The outline does the same for LDAP.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns the children of the given tree element in the outline hierarchy.
         * An {@link IEntry} produces a single {@link EntryWrapper} root node; an
         * {@link EntryWrapper} produces the entry's attributes; an {@link IAttribute}
         * produces that attribute's values; anything else returns an empty array.
         *
         * @param element  The parent element whose children we need.
         * @return array of child objects, never {@code null}.
         */
        public Object[] getChildren( Object element )
        {
            // entry -> entry wrapper
            // the entry is the input and is not visible,
            // so we use an wrapper around to make it visible as root element
            if ( element instanceof IEntry )
            {
                IEntry entry = ( IEntry ) element;
                return new EntryWrapper[]
                    { new EntryWrapper( entry ) };
            }

            // entry wrapper -> attribute
            if ( element instanceof EntryWrapper )
            {
                EntryWrapper entryWrapper = ( EntryWrapper ) element;
                return entryWrapper.entry.getAttributes();
            }

            // attribute -> values
            else if ( element instanceof IAttribute )
            {
                IAttribute attribute = ( IAttribute ) element;
                return attribute.getValues();
            }

            else
            {
                return new Object[0];
            }
        }


        // ── FINDING THE HOMESTEAD FROM ANY SPOT IN THE DESERT ────────────────
        // Given any spot in the desert, Luke theoretically could walk back to
        // the homestead — but we don't actually implement that reverse lookup here.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns the parent of the given element — not implemented, returns {@code null}.
         * We don't need reverse navigation for the outline tree to function correctly.
         *
         * @param element  The element whose parent is requested.
         * @return always {@code null}.
         */
        public Object getParent( Object element )
        {
            return null;
        }


        // ── CHECKING IF THERE'S MORE TERRAIN BENEATH ─────────────────────────
        // Luke peers over a dune to see if there's more desert hidden behind it —
        // this method answers whether a node has any children to expand.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given element has any children.
         * Used by the tree viewer to decide whether to render an expand arrow.
         *
         * @param element  The element to test.
         * @return {@code true} if {@link #getChildren(Object)} would return a non-empty array.
         */
        public boolean hasChildren( Object element )
        {
            return getChildren( element ) != null && getChildren( element ).length > 0;
        }


        // ── STARTING THE SCAN FROM THE HIGHEST POINT ─────────────────────────
        // Luke starts his survey from the very top of the ridge — the same as
        // asking for the root-level children of the whole tree input.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns the root elements for the given tree input — delegates to {@link #getChildren(Object)}.
         * For us, the tree input is the {@link IEntry}, so this returns a single {@link EntryWrapper}.
         *
         * @param inputElement  The tree input; expected to be an {@link IEntry}.
         * @return the top-level nodes for the outline tree.
         */
        public Object[] getElements( Object inputElement )
        {
            return getChildren( inputElement );
        }


        // ── PACKING UP THE SURVEY EQUIPMENT ──────────────────────────────────
        // Luke folds up his macrobinoculars and heads home — nothing to clean up here.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * No-op — this content provider holds no resources to release.
         */
        public void dispose()
        {
        }


        // ── SWAPPING OUT THE MAP WHEN THE ENTRY CHANGES ──────────────────────
        // Luke gets a new set of terrain maps for a completely different planet —
        // the old ones are discarded and he starts fresh with the new ones.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Called when the viewer's input changes — we don't cache anything so this is a no-op.
         *
         * @param viewer    The tree viewer whose input changed.
         * @param oldInput  The previous input object.
         * @param newInput  The new input object.
         */
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }
    }

    // ── CLASS: EntryEditorOutlineLabelProvider — LABELING THE TERRAIN FEATURES ─
    // Luke labels each feature on his map — "two suns set here", "vapor farm here",
    // "sandcrawler tracks there" — so any rebel soldier can read the landscape at a glance.
    // The label provider does the same: entry → DN name, attribute → description + count,
    // value → shortened string, all with appropriate icons.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * The JFace label provider that renders text and icons for each outline tree node.
     * Entries show their DN (or "Root DSE"), attributes show their description and value
     * count, and values show a shortened string representation with matching icons.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private static class EntryEditorOutlineLabelProvider extends LabelProvider
    {
        // ── LUKE READS THE LABEL OFF EACH TERRAIN MARKER ─────────────────────
        // Each stake in the ground has a hand-written label — Luke reads it and
        // reports it back to the team so they know what they're looking at.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns the display text for the given outline tree element.
         * Entry wrappers show the DN (or "Root DSE" for the root), attributes show
         * their description plus value count, and values show a 20-char truncated string.
         *
         * @param element  The tree node to label.
         * @return the label string; empty string if the element type is unrecognized.
         */
        public String getText( Object element )
        {
            // Entry
            if ( element instanceof EntryWrapper )
            {
                IEntry entry = ( ( EntryWrapper ) element ).entry;

                // Checking the Root DSE
                if ( entry.getDn() != null && "".equals( entry.getDn().toString() ) ) //$NON-NLS-1$
                {
                    // Root DSE
                    return "Root DSE"; //$NON-NLS-1$
                }
                else
                {
                    // Any other case
                    return entry.getDn().getName();
                }
            }

            // Attribute
            else if ( element instanceof IAttribute )
            {
                IAttribute attribute = ( IAttribute ) element;
                return attribute.getDescription() + " (" + attribute.getValueSize() + ")"; //$NON-NLS-1$  //$NON-NLS-2$
            }

            // Value
            else if ( element instanceof IValue )
            {
                IValue value = ( IValue ) element;
                return Utils.getShortenedString( value.getStringValue(), 20 );
            }

            else
            {
                return ""; //$NON-NLS-1$
            }
        }


        // ── LUKE PICKS THE RIGHT FLAG COLOR FOR EACH MARKER ──────────────────
        // Different marker flags in the desert — red for the homestead, blue for
        // the well, yellow for the sand crawler tracks. Each type gets its own icon.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns the icon image for the given outline tree element.
         * Entry wrappers get a root-entry or object-class-based icon; attributes get the
         * LDIF attribute icon; values get the LDIF value icon.
         *
         * @param element  The tree node to provide an image for.
         * @return the image, or {@code null} if the element type is unrecognized.
         */
        public Image getImage( Object element )
        {
            // Entry
            if ( element instanceof EntryWrapper )
            {
                IEntry entry = ( ( EntryWrapper ) element ).entry;

                // Checking the Root DSE
                if ( entry.getDn() != null && "".equals( entry.getDn().toString() ) ) //$NON-NLS-1$
                {
                    // Root DSE
                    return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_ENTRY_ROOT );
                }
                else
                {
                    // Any other case
                    return BrowserLabelProvider.getImageByObjectClass( entry );
                }
            }

            // Attribute
            else if ( element instanceof IAttribute )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_ATTRIBUTE );
            }

            // Value
            else if ( element instanceof IValue )
            {
                return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_VALUE );
            }

            else
            {
                return null;
            }
        }
    }

    // ── CLASS: EntryWrapper — THE HOMESTEAD PERIMETER MARKER ─────────────────
    // The homestead is the root of Luke's map, but to show it as a visible node
    // in the tree (not just the invisible input object), we wrap it in a marker flag.
    // EntryWrapper does exactly that — it makes the IEntry visible as the tree root.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * A thin wrapper around {@link IEntry} so the entry itself appears as a visible
     * root node in the outline tree. JFace tree viewers treat the input object as an
     * invisible container; wrapping it lets us make the entry show up at the top level.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private static class EntryWrapper
    {
        IEntry entry;


        // ── PLANTING THE HOMESTEAD FLAG ───────────────────────────────────────
        // Luke stakes a flag at the homestead location so it appears on the map.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Creates a wrapper for the given LDAP entry.
         *
         * @param entry  The entry to wrap and make visible as a tree root node.
         */
        public EntryWrapper( IEntry entry )
        {
            super();
            this.entry = entry;
        }


        // ── COMPUTING THE FLAG'S UNIQUE COORDINATES ───────────────────────────
        // Each flag has a unique grid coordinate so it can be found on the map.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns a hash code based on the wrapped entry, for use in collections.
         *
         * @return the hash code.
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( entry == null ) ? 0 : entry.hashCode() );
            return result;
        }


        // ── CHECKING IF TWO FLAGS MARK THE SAME SPOT ─────────────────────────
        // Two flags might look the same from a distance — this check confirms
        // whether they're actually planted at exactly the same coordinates.
        // ──────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if this wrapper wraps the same entry as the other wrapper.
         *
         * @param obj  The object to compare against.
         * @return {@code true} if both wrap equal {@link IEntry} instances.
         */
        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            EntryWrapper other = ( EntryWrapper ) obj;
            if ( entry == null )
            {
                if ( other.entry != null )
                    return false;
            }
            else if ( !entry.equals( other.entry ) )
                return false;
            return true;
        }
    }


    // ── CHECKING IF THE SUNSET IS VISIBLE FROM HERE ───────────────────────────
    // Luke checks: is there actually a sunset to watch, or is it already dark?
    // We check whether there's an entry loaded in the main widget to show in the outline.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if there is an entry currently loaded in the main editor widget.
     * Used to decide whether to show the tree viewer or the "no outline" message.
     *
     * @return {@code true} if the main widget's tree viewer has an {@link IEntry} as input.
     */
    private boolean hasAnOutline()
    {
        return getEntryEditorWidgetTreeViewerInput() != null;
    }


    // ── FINDING THE MAIN VIEWPORT THROUGH THE HOMESTEAD WINDOW ──────────────
    // Luke peers through the homestead window to find the main viewfinder he
    // uses to look at the horizon — the entry editor widget's tree viewer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tree viewer from the entry editor's main widget, or {@code null}.
     * We navigate through the editor → widget → viewer chain defensively, returning
     * null at each step if anything is missing or disposed.
     *
     * @return the main entry editor tree viewer, or {@code null} if unavailable.
     */
    private TreeViewer getEntryEditorWidgetTreeViewer()
    {
        if ( entryEditor != null )
        {
            EntryEditorWidget mainWidget = entryEditor.getMainWidget();
            if ( mainWidget != null )
            {
                TreeViewer viewer = mainWidget.getViewer();
                return viewer;
            }
        }
        return null;
    }


    // ── READING WHAT'S IN THE MAIN VIEWFINDER RIGHT NOW ──────────────────────
    // Luke looks through the viewfinder — if an entry is displayed there, he
    // reports its type back. If it's empty or showing something else, null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IEntry} currently loaded in the main editor widget's viewer, or {@code null}.
     * We check the viewer's input and cast it only if it's actually an {@link IEntry} —
     * during initialization or after a close the input might be null or a different type.
     *
     * @return the currently displayed entry, or {@code null} if none is loaded.
     */
    private IEntry getEntryEditorWidgetTreeViewerInput()
    {
        TreeViewer viewer = getEntryEditorWidgetTreeViewer();
        if ( viewer != null )
        {
            Object o = viewer.getInput();
            if (  o instanceof IEntry )
            {
                return ( IEntry ) o;
            }
        }
        return null;
    }
}
