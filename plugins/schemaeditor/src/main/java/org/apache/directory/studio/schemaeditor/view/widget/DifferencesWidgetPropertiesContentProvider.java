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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.Collections;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.difference.PropertyDifference;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: DifferencesWidgetPropertiesContentProvider — YODA LIFTS THE X-WING ──
// On Dagobah, Yoda takes Luke's sunken X-wing — a heavy, disordered mess — and
// lifts it cleanly out of the swamp, transforming raw chaos into something useful.
// This class does the same: it receives an unsorted list of PropertyDifference
// objects and transforms it into a cleanly ordered array the table viewer can render.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link IStructuredContentProvider} for the right-panel table inside
 * {@link DifferencesWidget}. It receives a {@code List<PropertyDifference>} as
 * input, sorts it according to the active grouping preference (by property name or
 * by change type), and returns the sorted array for the table to display.
 * Think of it as Yoda lifting the X-wing: the raw, disordered input goes in,
 * a neatly ordered array comes out, ready to be shown to the user.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DifferencesWidgetPropertiesContentProvider implements IStructuredContentProvider
{
    /** The PropertySorter */
    private PropertySorter propertySorter;

    /** The TypeSorter */
    private TypeSorter typeSorter;

    /** The PreferenceStore */
    private IPreferenceStore store;


    // ── YODA CENTRES HIMSELF BEFORE LIFTING ──────────────────────────────────────
    // Before Yoda can lift the X-wing, he has to be ready: eyes closed, the Force
    // flowing through him. Here we pre-create both sorter strategies so we never
    // allocate them during the hot getElements() call.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new content provider, initialising both sorting strategies and
     * grabbing a reference to the plugin's preference store. We prepare the sorters
     * up front so {@link #getElements(Object)} never has to allocate mid-call.
     */
    public DifferencesWidgetPropertiesContentProvider()
    {
        propertySorter = new PropertySorter();
        typeSorter = new TypeSorter();

        store = Activator.getDefault().getPreferenceStore();
    }


    // ── YODA LIFTS AND SETS THE X-WING DOWN NEATLY ───────────────────────────────
    // Yoda does not just grab the X-wing and drop it randomly on the bank — he sets
    // it down in exactly the right place. We sort the incoming list in-place according
    // to the current grouping preference and return it as a clean Object array.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the rows for the property-differences table. If the input is a
     * {@code List<PropertyDifference>}, we sort it in-place (by property name or by
     * change type, depending on the user's preference) and return it as an array.
     * Any other input type returns {@code null} and the table shows nothing.
     *
     * <p>For example — Yoda arranges the X-wing parts before Luke can use them:</p>
     * <pre>
     *   input: [SyntaxDiff, AliasDiff, DescriptionDiff]  (unsorted)
     *   pref = GROUP_BY_PROPERTY  →  sorted by property weight via PropertySorter
     *   pref = GROUP_BY_TYPE      →  sorted by change-type weight via TypeSorter
     *   returns: Object[] ready for the table rows
     * </pre>
     *
     * @param inputElement  the list of {@link PropertyDifference} objects from the
     *                      tree selection; anything else yields {@code null}
     * @return              sorted array of differences, or {@code null} if input is wrong type
     */
    @SuppressWarnings("unchecked")
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof List )
        {
            List<PropertyDifference> differences = ( List<PropertyDifference> ) inputElement;

            int prefValue = store.getInt( PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING );
            if ( prefValue == PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING_PROPERTY )
            {
                Collections.sort( differences, propertySorter );
            }
            else if ( prefValue == PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING_TYPE )
            {
                Collections.sort( differences, typeSorter );
            }

            return differences.toArray();
        }

        // Default
        return null;
    }


    // ── YODA LETS THE FORCE SETTLE BACK ──────────────────────────────────────────
    // After Yoda lifts the X-wing, the exertion fades and he rests. Our provider
    // holds no SWT resources, so there is genuinely nothing to release here.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the table viewer is disposed or its content provider
     * is replaced. We hold no SWT resources, so there is nothing to clean up here.
     *
     * {@inheritDoc}
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── THE INPUT CHANGES: YODA ADJUSTS HIS GRIP ─────────────────────────────────
    // When the tree selection changes, the table gets a new input list. Yoda adjusts —
    // the next call to getElements() will sort and return the new list. We do not need
    // to do anything special here because getElements() reads the store fresh each time.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the input to the table viewer changes. We do not need to
     * cache any viewer reference or react to old/new input transitions — the next call
     * to {@link #getElements(Object)} will pick up whatever the new input is.
     *
     * @param viewer    the table viewer whose input just changed
     * @param oldInput  the previous input object
     * @param newInput  the new input object
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing do to
    }
}
