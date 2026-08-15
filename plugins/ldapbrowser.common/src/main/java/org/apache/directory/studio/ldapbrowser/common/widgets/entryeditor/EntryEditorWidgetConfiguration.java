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


import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: EntryEditorWidgetConfiguration — The Falcon's Pre-Flight Checklist ────────────
// Before every mission, Han and Chewie run through the Falcon's pre-flight checklist in the
// Mos Eisley hangar: hyperdrive primed, shields calibrated, nav computer loaded, comm system
// warm.  Each system is lazily activated — only fired up the first time it's actually needed
// (no point warming up the hyperdrive if you're just doing a short hop inside the atmosphere).
// This class is that checklist: it holds and lazily creates every subsystem the entry editor
// widget needs — content provider, label provider, cell modifier, value editor manager, sorter,
// filter, and preferences — and disposes them all cleanly when the mission ends.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Central configuration and factory for the entry editor widget.
 * Holds the shared instances of every subsystem the viewer needs: content provider, label
 * provider, cell modifier, value editor manager, sorter, filter, and preferences.
 * Each subsystem is created lazily on first access and cached for reuse — no need to
 * create the hyperdrive calibration if the Falcon never leaves the hangar.
 * Dispose this configuration (via {@link #dispose()}) when the entry editor widget is closed
 * to release all SWT resources and Eclipse plugin registrations held by the subsystems.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetConfiguration
{
    /** The disposed flag */
    private boolean disposed = false;

    /** The sorter. */
    protected EntryEditorWidgetSorter sorter;

    /** The filter. */
    protected EntryEditorWidgetFilter filter;

    /** The preferences. */
    protected EntryEditorWidgetPreferences preferences;

    /** The content provider. */
    protected EntryEditorWidgetContentProvider contentProvider;

    /** The label provider. */
    protected EntryEditorWidgetLabelProvider labelProvider;

    /** The cell modifier. */
    protected EntryEditorWidgetCellModifier cellModifier;

    /** The value editor manager. */
    protected ValueEditorManager valueEditorManager;


    // ── Han Boards The Falcon — Empty Checklist ───────────────────────────────────────────
    // Han steps into the cockpit and the checklist is blank — no systems have been activated yet.
    // Everything will be primed on demand when the viewer asks for it.
    // This no-arg constructor lets subclasses or the Eclipse extension mechanism instantiate
    // the configuration before wiring it to a specific viewer.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty configuration.
     * All subsystems start as {@code null} and are created lazily when first requested via
     * the {@code get*} methods.  Call {@link #dispose()} when the owning entry editor widget
     * closes to release all resources.
     */
    public EntryEditorWidgetConfiguration()
    {
    }


    // ── Han Powers Down All Systems At Mission End ────────────────────────────────────────
    // After landing, Han methodically shuts down each of the Falcon's systems in order —
    // nav computer, shields, comm, hyperdrive.  Each system's dispose() is called exactly once,
    // guarded by the `disposed` flag so accidental double-shutdowns don't cause chaos.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all subsystems that have been created, then marks this configuration as disposed.
     * Safe to call multiple times — the {@code disposed} flag prevents double-disposal.
     * Must be called when the owning entry editor widget closes, otherwise SWT resources
     * and Eclipse listener registrations will leak.
     *
     * <p>For example — Han's shutdown sequence:</p>
     * <pre>
     *   sorter.dispose();
     *   filter.dispose();
     *   preferences.dispose();
     *   contentProvider.dispose();
     *   labelProvider.dispose();
     *   cellModifier.dispose();
     *   valueEditorManager.dispose();
     *   disposed = true;
     * </pre>
     */
    public void dispose()
    {
        if ( !disposed )
        {
            if ( sorter != null )
            {
                sorter.dispose();
                sorter = null;
            }

            if ( filter != null )
            {
                filter.dispose();
                filter = null;
            }

            if ( preferences != null )
            {
                preferences.dispose();
                preferences = null;
            }

            if ( contentProvider != null )
            {
                contentProvider.dispose();
                contentProvider = null;
            }

            if ( labelProvider != null )
            {
                labelProvider.dispose();
                labelProvider = null;
            }

            if ( cellModifier != null )
            {
                cellModifier.dispose();
                cellModifier = null;
            }

            if ( valueEditorManager != null )
            {
                valueEditorManager.dispose();
                valueEditorManager = null;
            }

            disposed = true;
        }
    }


    // ── Han Primes The Cargo Manifest System ──────────────────────────────────────────────
    // The content provider is like the cargo manifest — it knows what's in each entry and
    // tells the viewer which child nodes to show.  We create it with the preferences (for
    // sort/filter settings) and the main widget (to get the viewer reference).
    // Lazily created on first call — no need to load the manifest until we're actually
    // browsing the directory.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the content provider for the entry editor's tree viewer, creating it lazily
     * on first call.  The content provider translates LDAP entry attributes into the tree
     * structure the viewer renders.
     *
     * <p>For example — Han loads the cargo manifest:</p>
     * <pre>
     *   if ( contentProvider == null )
     *       contentProvider = new EntryEditorWidgetContentProvider( getPreferences(), mainWidget );
     *   return contentProvider;
     * </pre>
     *
     * @param mainWidget  the entry editor widget, needed to resolve the viewer reference
     * @return the shared {@link EntryEditorWidgetContentProvider} instance
     */
    public EntryEditorWidgetContentProvider getContentProvider( EntryEditorWidget mainWidget )
    {
        if ( contentProvider == null )
        {
            contentProvider = new EntryEditorWidgetContentProvider( getPreferences(), mainWidget );
        }

        return contentProvider;
    }


    // ── Han Activates The Visual Display ─────────────────────────────────────────────────
    // The label provider controls how each row looks in the viewer — what text and icon
    // to show for each attribute value.  It needs the viewer (for font/color context) and
    // the value editor manager (to format values for display).
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label provider for the entry editor's tree viewer, creating it lazily.
     * The label provider formats each {@link org.apache.directory.studio.ldapbrowser.core.model.IValue}
     * for display — choosing the text, icons, and decorations shown in each row.
     *
     * <p>For example — Han powers up the heads-up display:</p>
     * <pre>
     *   if ( labelProvider == null )
     *       labelProvider = new EntryEditorWidgetLabelProvider( viewer, valueEditorManager );
     *   return labelProvider;
     * </pre>
     *
     * @param valueEditorManager  the manager that knows how to format each value type for display
     * @param viewer              the tree viewer, needed for font/color resolution
     * @return the shared {@link EntryEditorWidgetLabelProvider} instance
     */
    public EntryEditorWidgetLabelProvider getLabelProvider( ValueEditorManager valueEditorManager, TreeViewer viewer )
    {
        if ( labelProvider == null )
        {
            labelProvider = new EntryEditorWidgetLabelProvider( viewer, valueEditorManager );
        }

        return labelProvider;
    }


    // ── Han Connects The Manual Override Control ──────────────────────────────────────────
    // The cell modifier is Han's manual controls — it mediates between JFace's table editing
    // protocol and the LDAP value model, checking whether a cell is editable, reading its
    // raw value, and committing edits.  It needs the value editor manager to do all that.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the cell modifier for the entry editor's tree viewer, creating it lazily.
     * The cell modifier implements JFace's {@link org.eclipse.jface.viewers.ICellModifier}
     * — it decides cell editability, provides raw values to cell editors, and commits edits
     * back to the LDAP model via {@link org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification}.
     *
     * <p>For example — Han connects the override lever:</p>
     * <pre>
     *   if ( cellModifier == null )
     *       cellModifier = new EntryEditorWidgetCellModifier( valueEditorManager );
     *   return cellModifier;
     * </pre>
     *
     * @param valueEditorManager  the manager used to look up the right editor for each value type
     * @return the shared {@link EntryEditorWidgetCellModifier} instance
     */
    public EntryEditorWidgetCellModifier getCellModifier( ValueEditorManager valueEditorManager )
    {
        if ( cellModifier == null )
        {
            cellModifier = new EntryEditorWidgetCellModifier( valueEditorManager );
        }

        return cellModifier;
    }


    // ── Han Loads The Value Editor Library ────────────────────────────────────────────────
    // The value editor manager knows which editor plugin to use for each LDAP attribute type —
    // a password editor for userPassword, a DN editor for attributes of DN syntax, etc.
    // It's like the Falcon's library of nav chart modules: one module per coordinate format.
    // We create it once against the viewer's SWT tree widget.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value editor manager, creating it lazily on first call.
     * The {@link ValueEditorManager} discovers all installed value editor plugins and
     * dispatches to the right one based on each attribute's syntax OID.
     * Created against the SWT tree control inside the viewer.
     *
     * <p>For example — Han loads the nav chart library:</p>
     * <pre>
     *   if ( valueEditorManager == null )
     *       valueEditorManager = new ValueEditorManager( viewer.getTree(), false, false );
     *   return valueEditorManager;
     * </pre>
     *
     * @param viewer  the tree viewer whose SWT tree control hosts the inline cell editors
     * @return the shared {@link ValueEditorManager} instance
     */
    public ValueEditorManager getValueEditorManager( TreeViewer viewer )
    {
        if ( valueEditorManager == null )
        {
            valueEditorManager = new ValueEditorManager( viewer.getTree(), false, false );
        }

        return valueEditorManager;
    }


    // ── Han Calibrates The Nav Sort Module ───────────────────────────────────────────────
    // The sorter controls the order in which attribute-value rows appear in the viewer —
    // alphabetically by attribute name, by value, or by a custom comparator.  It reads
    // its current sort configuration from the preferences.  Like the Falcon's nav computer
    // deciding which waypoints to show first.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sorter for the entry editor's tree viewer, creating it lazily.
     * The sorter controls the display order of attribute-value rows and reads its
     * current sort criteria from the shared {@link EntryEditorWidgetPreferences}.
     *
     * <p>For example — Han calibrates the waypoint ordering:</p>
     * <pre>
     *   if ( sorter == null )
     *       sorter = new EntryEditorWidgetSorter( getPreferences() );
     *   return sorter;
     * </pre>
     *
     * @return the shared {@link EntryEditorWidgetSorter} instance
     */
    public EntryEditorWidgetSorter getSorter()
    {
        if ( sorter == null )
        {
            sorter = new EntryEditorWidgetSorter( getPreferences() );
        }

        return sorter;
    }


    // ── Han Activates The Quick-Search Filter ─────────────────────────────────────────────
    // The filter hides rows that don't match the current quick-filter text.  It's like the
    // Falcon's radar narrowing its scan to only show matching blips — the full data is still
    // there, just not displayed.  Created on demand when the user first opens the filter bar.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the viewer filter for the entry editor, creating it lazily.
     * The filter hides attribute-value rows that don't match the current quick-filter
     * text entered by the user.  When no filter text is set, all rows pass through.
     *
     * <p>For example — Han narrows the radar sweep:</p>
     * <pre>
     *   if ( filter == null )
     *       filter = new EntryEditorWidgetFilter();
     *   return filter;
     * </pre>
     *
     * @return the shared {@link EntryEditorWidgetFilter} instance
     */
    public EntryEditorWidgetFilter getFilter()
    {
        if ( filter == null )
        {
            filter = new EntryEditorWidgetFilter();
        }

        return filter;
    }


    // ── Han Checks The Falcon's Saved Settings ────────────────────────────────────────────
    // The preferences object wraps the Eclipse preference store, providing typed access to
    // the entry editor's user-configurable settings (sort order, which columns to show, etc.).
    // Like checking the Falcon's saved mission profiles before departure.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the preferences object for the entry editor, creating it lazily.
     * The preferences wrap Eclipse's preference store and expose settings like sort direction,
     * sort column, and other display options.  Many other subsystems (sorter, content provider)
     * read from this same shared preferences instance.
     *
     * <p>For example — Han checks the saved mission profile:</p>
     * <pre>
     *   if ( preferences == null )
     *       preferences = new EntryEditorWidgetPreferences();
     *   return preferences;
     * </pre>
     *
     * @return the shared {@link EntryEditorWidgetPreferences} instance
     */
    public EntryEditorWidgetPreferences getPreferences()
    {
        if ( preferences == null )
        {
            preferences = new EntryEditorWidgetPreferences();
        }

        return preferences;
    }
}
