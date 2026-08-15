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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EmptyValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EmptyValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueModifiedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueRenamedEvent;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;


// ── CLASS: EntryEditorWidgetUniversalListener — Obi-Wan Senses a Disturbance ─
// Aboard the Millennium Falcon, Obi-Wan Kenobi sits quietly with eyes closed,
// his senses stretched across the galaxy. When Alderaan is destroyed he
// instantly reacts: "I felt a great disturbance in the Force." That's us here —
// this class registers itself to hear every LDAP event, every mouse click, every
// preference change, and responds by refreshing the entry editor accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central nervous system for the entry editor widget. We wire up every event
 * source that the editor cares about — LDAP entry changes, keyboard input,
 * mouse gestures, and preference updates — then react to each one to keep the
 * tree viewer in sync with reality.
 *
 * <p>Think of this class as Obi-Wan Kenobi meditating in the Force: always
 * listening, always ready to act the moment something changes anywhere in the
 * LDAP entry being displayed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetUniversalListener implements EntryUpdateListener
{
    /** The tree viewer */
    protected TreeViewer viewer;

    /** The configuration. */
    protected EntryEditorWidgetConfiguration configuration;

    /** The action group. */
    protected EntryEditorWidgetActionGroup actionGroup;

    /** The action used to start the default value editor */
    protected OpenDefaultEditorAction startEditAction;

    /** This listener starts the value editor when pressing enter */
    protected SelectionListener viewerSelectionListener = new SelectionAdapter()
    {
        // ── Obi-Wan Notices a Flicker, Stays Still ──────────────────────────────────
        // A selection event arrives on the Falcon's sensor board — Obi-Wan feels
        // it, registers it, but recognizes there is nothing here requiring action.
        // A plain selection (single click, tab key) is acknowledged and ignored.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Handles a plain widget selection event. We intentionally do nothing here
         * because a single-click selection doesn't start editing — only a confirmed
         * default action (Enter) does that.
         *
         * <p>For example — Obi-Wan senses a faint tremor in the Force but recognizes
         * it as background noise, not a call to action:</p>
         * <pre>
         *   Obi-Wan: "There was a disturbance..."
         *   Luke: "Should we do something?"
         *   Obi-Wan: "No. Not yet."
         * </pre>
         *
         * @param e  The SWT selection event carrying widget state; we don't use it.
         */
        public void widgetSelected( SelectionEvent e )
        {
        }


        // ── Obi-Wan Acts on the Force's Command ─────────────────────────────────────
        // Obi-Wan's voice rings out: "Use the Force, Luke." That's the Enter key —
        // the user pressing Enter in the tree is the galaxy's command to begin editing.
        // We launch the default value editor action if it's currently enabled.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Handles the default selection event, which in an SWT Tree is fired when
         * the user presses Enter on a selected row. We respond by starting the
         * default value editor — but only if it's currently enabled.
         *
         * <p>For example — Obi-Wan hears the Force call clearly and sends Luke into
         * action:</p>
         * <pre>
         *   Obi-Wan (Force ghost): "Let go, Luke."
         *   Luke closes his eyes, hits the trigger → editing begins.
         * </pre>
         *
         * @param e  The SWT selection event; the detail tells us the user pressed Enter.
         */
        public void widgetDefaultSelected( SelectionEvent e )
        {
            if ( startEditAction.isEnabled() )
            {
                startEditAction.run();
            }
        }
    };

    /** This listener starts the value editor or expands/collapses the selected attribute */
    protected MouseListener viewerMouseListener = new MouseAdapter()
    {
        // ── Obi-Wan Senses a Double Pulse, Opens the Way ────────────────────────────
        // Two quick pulses ripple through the Force — Obi-Wan recognizes a double-click.
        // When the selected item is an attribute (not a value), he expands or collapses
        // it, revealing or hiding its children. That's the expand/collapse toggle here.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Handles double-click on a tree row. If the user double-clicked an attribute
         * node (as opposed to a value leaf), we toggle its expanded state — collapse it
         * if it was open, expand it if it was folded. If they double-clicked a value,
         * we let the default action (Enter) handle it instead.
         *
         * <p>For example — Obi-Wan senses two ripples and recognizes the pattern:</p>
         * <pre>
         *   Two pulses detected → attribute selected, not a value leaf.
         *   If open: collapse to level 1 (fold the children away).
         *   If closed: expand to level 1 (reveal the values beneath).
         * </pre>
         *
         * @param e  The SWT mouse event; we don't use it directly — we read the viewer
         *           selection instead.
         */
        public void mouseDoubleClick( MouseEvent e )
        {
            IAttribute[] attributes = BrowserSelectionUtils.getAttributes( viewer.getSelection() );
            IValue[] values = BrowserSelectionUtils.getValues( viewer.getSelection() );

            if ( ( attributes.length == 1 ) && ( values.length == 0 ) )
            {
                if ( viewer.getExpandedState( attributes[0] ) )
                {
                    viewer.collapseToLevel( attributes[0], 1 );
                }
                else
                {
                    viewer.expandToLevel( attributes[0], 1 );
                }
            }
        }


        // ── Obi-Wan Feels the First Contact, Waits ──────────────────────────────────
        // The first touch of a mouse button resonates through the Force like a stone
        // hitting still water. Obi-Wan detects it and remains patient — no action yet.
        // We register the press but do nothing; the interesting part comes on release.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Handles mouse button press. We intentionally do nothing here — the SWT
         * selection machinery handles focus and row highlighting on its own, and we
         * don't need to intercept the press itself.
         *
         * <p>For example — Obi-Wan senses a presence but waits to see what it does:</p>
         * <pre>
         *   Obi-Wan (quietly): "I sense something... a presence I haven't felt since..."
         *   He waits. Nothing happens yet.
         * </pre>
         *
         * @param e  The SWT mouse event; ignored.
         */
        public void mouseDown( MouseEvent e )
        {
        }


        // ── Obi-Wan Watches the Release, Stands Down ────────────────────────────────
        // The mouse button lifts — the disturbance passes. Obi-Wan exhales and returns
        // to stillness. There is nothing to act on at mouse-up time either; we're done.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Handles mouse button release. We intentionally do nothing here — the action
         * we care about (double-click) is handled in {@link #mouseDoubleClick}.
         *
         * <p>For example — Obi-Wan feels the presence retreat and relaxes:</p>
         * <pre>
         *   The ripple in the Force subsides.
         *   Obi-Wan: "It is gone. Stand down."
         * </pre>
         *
         * @param e  The SWT mouse event; ignored.
         */
        public void mouseUp( MouseEvent e )
        {
        }
    };

    /** This listener updates the viewer if an property (e.g. is operational attributes visible) has been changed */
    protected IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener()
    {
        // ── Obi-Wan Senses a Shift in the Force, Refreshes ──────────────────────────
        // A preference changes — "show operational attributes" gets toggled, or the
        // sort order shifts. Obi-Wan feels the realignment immediately and asks the
        // Force to show him the galaxy as it now truly is. We refresh the tree viewer.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Called whenever a plugin preference changes (e.g., "show operational
         * attributes," font size, color scheme). We simply refresh the entire tree
         * viewer so the new preference takes effect immediately without reloading the
         * underlying LDAP entry.
         *
         * <p>For example — Obi-Wan senses the Force shift and updates his vision:</p>
         * <pre>
         *   A new hope stirs in the Force — the preference store changed.
         *   Obi-Wan: "The Force has shifted. Let us see it clearly."
         *   viewer.refresh() → the tree repaints with the new settings.
         * </pre>
         *
         * @param event  The JFace property change event describing what changed and
         *               what the old/new values are; we don't inspect it, we just
         *               refresh everything.
         */
        public void propertyChange( PropertyChangeEvent event )
        {
            if ( viewer != null )
            {
                viewer.refresh();
            }
        }
    };


    // ── Obi-Wan Opens His Senses to the Whole Galaxy ────────────────────────────
    // On the Millennium Falcon's bridge, Obi-Wan Kenobi sits cross-legged and
    // breathes deeply, wiring his consciousness into the Force itself — ready to
    // receive every ripple, every cry, every disturbance across the galaxy.
    // Here we wire up every event listener this class will respond to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the universal listener by registering all event hooks this class
     * needs to keep the entry editor in sync. We attach a selection listener and
     * a mouse listener to the SWT tree, register for LDAP entry-update events
     * from the global EventRegistry, and subscribe to preference-store changes so
     * the display updates immediately when the user tweaks settings.
     *
     * <p>We also add a traverse listener that swallows the Enter key at the tree
     * level, so pressing Enter won't accidentally confirm a parent wizard or dialog
     * while the user is mid-edit.</p>
     *
     * <p>For example — Obi-Wan settles into meditation and opens every sense:</p>
     * <pre>
     *   viewer.tree.addSelectionListener(...)  → listens for Enter key
     *   viewer.tree.addMouseListener(...)      → listens for double-clicks
     *   EventRegistry.addEntryUpdateListener() → hears every LDAP change
     *   preferenceStore.addPropertyChangeListener() → notices setting shifts
     * </pre>
     *
     * @param treeViewer      The SWT TreeViewer we're managing; every update we
     *                        receive will be reflected here.
     * @param configuration   Holds sorters, filters, and preference access for this
     *                        editor instance.
     * @param actionGroup     The set of toolbar/context-menu actions we need to
     *                        inform when the input changes.
     * @param startEditAction The action that opens the default value editor; we
     *                        trigger it on Enter-key press.
     */
    public EntryEditorWidgetUniversalListener( TreeViewer treeViewer, EntryEditorWidgetConfiguration configuration,
        EntryEditorWidgetActionGroup actionGroup, OpenDefaultEditorAction startEditAction )
    {
        this.startEditAction = startEditAction;
        this.viewer = treeViewer;
        this.configuration = configuration;
        this.actionGroup = actionGroup;

        // register listeners
        viewer.getTree().addSelectionListener( viewerSelectionListener );
        viewer.getTree().addMouseListener( viewerMouseListener );
        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        BrowserCommonActivator.getDefault().getPreferenceStore().addPropertyChangeListener( propertyChangeListener );

        // Don't invoke Finish' or 'OK' button when pressing 'Enter' in wizard or dialog
        viewer.getTree().addTraverseListener( new TraverseListener()
        {
            public void keyTraversed( TraverseEvent e )
            {
                if ( e.detail == SWT.TRAVERSE_RETURN )
                {
                    e.doit = false;
                }
            }
        } );
    }


    // ── Obi-Wan Becomes One with the Force ──────────────────────────────────────
    // "If you strike me down, I shall become more powerful than you can possibly
    // imagine." Obi-Wan withdraws his physical presence cleanly, severing his
    // connections. We unregister from every event source and null our references
    // so the garbage collector can reclaim everything.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Tears down all event registrations and clears every reference this listener
     * holds, making it safe to garbage-collect. Call this when the entry editor
     * widget is closing — skipping it would keep this listener alive indefinitely
     * and leak memory.
     *
     * <p>For example — Obi-Wan severs his ties to the living Force before departing:</p>
     * <pre>
     *   EventRegistry.removeEntryUpdateListener(this) → no more LDAP events
     *   preferenceStore.removePropertyChangeListener() → no more pref events
     *   viewer = null; configuration = null; actionGroup = null; → clean slate
     * </pre>
     */
    public void dispose()
    {
        if ( viewer != null )
        {
            EventRegistry.removeEntryUpdateListener( this );
            BrowserCommonActivator.getDefault().getPreferenceStore().removePropertyChangeListener(
                propertyChangeListener );

            startEditAction = null;
            viewer = null;
            configuration = null;
            actionGroup = null;
        }
    }


    // ── Obi-Wan Reacts to a Great Disturbance in the Force ──────────────────────
    // "I felt a great disturbance in the Force, as if millions of voices suddenly
    // cried out in terror." Obi-Wan reacts immediately — refreshing the viewer and
    // then focusing his attention on the specific thing that changed (the added
    // value, the deleted value, the renamed attribute).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the EventRegistry whenever an LDAP entry is modified — values
     * added, deleted, renamed, or modified in bulk. We first bail out fast if the
     * event doesn't apply to the entry currently displayed (wrong entry, or the
     * viewer has already been disposed). Otherwise we cancel any open cell editor,
     * refresh the tree, and then adjust the selection to point at whichever value
     * was most recently affected, so the user's focus stays on the right row.
     *
     * <p>For example — Obi-Wan senses the disturbance and responds precisely:</p>
     * <pre>
     *   ValueAddedEvent   → select the freshly added value row
     *   ValueDeletedEvent → select a sibling value in the same attribute
     *   EmptyValueAdded   → select the empty row and start the editor immediately
     *   ValueModified     → select the newly modified value
     *   ValueRenamed      → select the value under its new name
     * </pre>
     *
     * @param event  The LDAP modification event; its concrete subtype tells us
     *               which value changed and how we should update the selection.
     */
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( ( viewer == null ) ||
             ( viewer.getTree() == null ) ||
             viewer.getTree().isDisposed() ||
             ( viewer.getInput() == null ) ||
             ( ( event.getModifiedEntry() != viewer.getInput() ) && !( event instanceof BulkModificationEvent ) ) )
        {
            return;
        }

        // force closing of cell editors
        if ( viewer.isCellEditorActive() )
        {
            viewer.cancelEditing();
        }

        // refresh
        viewer.refresh();

        // selection value
        if ( event instanceof ValueAddedEvent )
        {
            // select the vadded value
            ValueAddedEvent vaEvent = ( ValueAddedEvent ) event;
            viewer.setSelection( new StructuredSelection( vaEvent.getAddedValue() ), true );
            viewer.refresh();
        }
        else if ( event instanceof ValueDeletedEvent )
        {
            // select another value of the deleted attribute
            ValueDeletedEvent vdEvent = ( ValueDeletedEvent ) event;

            if ( viewer.getSelection().isEmpty() && vdEvent.getDeletedValue().getAttribute().getValueSize() > 0 )
            {
                viewer.setSelection(
                    new StructuredSelection( vdEvent.getDeletedValue().getAttribute().getValues()[0] ), true );
            }
        }
        else if ( event instanceof EmptyValueAddedEvent )
        {
            EmptyValueAddedEvent evaEvent = ( EmptyValueAddedEvent ) event;

            // select the added value and start editing
            viewer.setSelection( new StructuredSelection( evaEvent.getAddedValue() ), true );

            if ( startEditAction.isEnabled() && viewer.getControl().isFocusControl() )
            {
                startEditAction.run();
            }
        }
        else if ( event instanceof EmptyValueDeletedEvent )
        {
            // select another value of the deleted attribute
            EmptyValueDeletedEvent evdEvent = ( EmptyValueDeletedEvent ) event;

            if ( viewer.getSelection().isEmpty() && ( evdEvent.getDeletedValue().getAttribute().getValueSize() > 0 ) )
            {
                viewer.setSelection(
                    new StructuredSelection( evdEvent.getDeletedValue().getAttribute().getValues()[0] ), true );
            }
        }
        else if ( event instanceof ValueModifiedEvent )
        {
            // select the modified value
            ValueModifiedEvent vmEvent = ( ValueModifiedEvent ) event;
            viewer.setSelection( new StructuredSelection( vmEvent.getNewValue() ), true );
        }
        else if ( event instanceof ValueRenamedEvent )
        {
            // select the renamed value
            ValueRenamedEvent vrEvent = ( ValueRenamedEvent ) event;
            viewer.setSelection( new StructuredSelection( vrEvent.getNewValue() ), true );
        }
    }


    // ── Obi-Wan Shifts His Gaze to a New Planet ─────────────────────────────────
    // Obi-Wan's Force awareness sweeps across the galaxy and settles on a new
    // world — he now focuses entirely on this specific LDAP entry. The tree viewer
    // and all action group members are updated to reflect the new entry as their
    // working context, and any folded attributes are automatically expanded.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads a full LDAP entry into the tree viewer so the user can see and edit
     * all of its attributes and values. We also tell the action group about the
     * new entry so toolbar/menu actions stay in sync, and we expand any attributes
     * that the user's preference says should open automatically.
     *
     * <p>For example — Obi-Wan turns his full attention to a new world:</p>
     * <pre>
     *   viewer.setInput(entry)      → display the entry's attributes
     *   actionGroup.setInput(entry) → actions now act on this entry
     *   expandFoldedAttributes()    → reveal children if preference says so
     * </pre>
     *
     * @param entry  The LDAP entry whose attributes we want to display; passing
     *               {@code null} clears the viewer.
     */
    public void setInput( IEntry entry )
    {
        // if ( entry != viewer.getInput() )
        {
            viewer.setInput( entry );
            actionGroup.setInput( entry );
            expandFoldedAttributes();
        }
    }


    // ── Obi-Wan Narrows His Force Gaze to One Attribute Family ──────────────────
    // Rather than watching an entire planet, Obi-Wan focuses his awareness on a
    // single cluster of related attributes — an AttributeHierarchy, which groups
    // several LDAP attribute types that share a common definition (e.g., "cn" and
    // all its aliases). Narrower focus, but the same careful attention.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads an {@link AttributeHierarchy} into the tree viewer instead of a full
     * entry. This is used when the editor is showing only a subset of an entry's
     * attributes — for example in the multi-valued dialog. We update the action
     * group too, and expand folded attributes per the user's preference.
     *
     * <p>For example — Obi-Wan zeroes in on a single cluster in the Force:</p>
     * <pre>
     *   viewer.setInput(attributeHierarchy) → show just this attribute family
     *   actionGroup.setInput(...)           → actions scoped to these attributes
     *   expandFoldedAttributes()            → open children if auto-expand is on
     * </pre>
     *
     * @param attributeHierarchy  The group of related LDAP attributes to display;
     *                            if it's already the current input we skip the
     *                            reload to avoid unnecessary flicker.
     */
    public void setInput( AttributeHierarchy attributeHierarchy )
    {
        if ( attributeHierarchy != viewer.getInput() )
        {
            viewer.setInput( attributeHierarchy );
            actionGroup.setInput( attributeHierarchy );
            expandFoldedAttributes();
        }
    }


    // ── Obi-Wan Expands His Awareness to See Every Detail ───────────────────────
    // When the Force shows Obi-Wan a galaxy full of folded mysteries, he reaches
    // deeper and unfurls every hidden layer — revealing the full picture. Here, if
    // the user's preference says "auto-expand folded attributes," we call
    // expandAll() on the viewer so every multi-valued attribute opens up.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Expands all attribute nodes in the tree viewer if the user's preference
     * for auto-expanding folded attributes is enabled. Multi-valued LDAP attributes
     * are represented as parent nodes with child value nodes; this method makes them
     * all visible at once so the user doesn't have to click each one open manually.
     *
     * <p>For example — Obi-Wan reaches into every fold of the Force:</p>
     * <pre>
     *   if (preferences.isAutoExpandFoldedAttributes()) {
     *     viewer.expandAll() → every attribute parent node opens
     *   }
     *   // Nothing collapses — that's up to the user.
     * </pre>
     */
    protected void expandFoldedAttributes()
    {
        if ( configuration.getPreferences().isAutoExpandFoldedAttributes() )
        {
            viewer.expandAll();
        }
    }
}
