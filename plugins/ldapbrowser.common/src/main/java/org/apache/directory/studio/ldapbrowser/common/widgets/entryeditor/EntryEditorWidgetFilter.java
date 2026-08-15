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


import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


// -- CLASS: EntryEditorWidgetFilter -- R2-D2 AT THE DEATH STAR DETENTION TERMINAL ----
// R2-D2 has just plugged into the Death Star's central computer in the detention block.
// He runs targeted queries -- filtering thousands of prisoner records down to just the
// ones that match: "Princess Leia, cell block AA-23."
// This class does exactly that: it sits in the JFace viewer pipeline and hides every
// attribute/value row that doesn't match what the user typed in the quick-filter bar.
// ---------------------------------------------------------------------------------
/**
 * Filters the entry editor's tree table to show only rows that match the user's quick-filter
 * text. JFace's {@link ViewerFilter} contract means we simply say yes/no for each element
 * and the viewer handles hiding the rest.
 * Think of this class as R2-D2 at the Death Star terminal: he queries by attribute name and
 * value text, and only passes back the records that match both criteria.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetFilter extends ViewerFilter
{
    /** The viewer to filter. */
    protected TreeViewer viewer;

    /** The quick filter attribute. */
    protected String quickFilterAttribute;

    /** The quick filter value. */
    protected String quickFilterValue;


    // -- R2 POWERS UP THE TERMINAL WITH EMPTY SEARCH CRITERIA ------------------
    // R2-D2 connects the interface probe but hasn't typed anything yet.
    // Both search fields start blank, meaning every record passes through
    // until the user gives us something to narrow it down.
    // ---------------------------------------------------------------------------------
    /**
     * Creates a new filter instance with empty (pass-all) search strings.
     * Until the user types something in the quick-filter bar, every row will be visible.
     *
     * <p>For example -- R2 boots the terminal:</p>
     * <pre>
     *   R2 connects probe. Search fields are blank.
     *   All prisoner records pass through unfiltered.
     *   "Bweeep." (Ready and waiting for search criteria.)
     * </pre>
     */
    public EntryEditorWidgetFilter()
    {
        quickFilterAttribute = ""; //$NON-NLS-1$
        quickFilterValue = ""; //$NON-NLS-1$
    }


    // -- R2 HOOKS HIS PROBE INTO THE STATION NETWORK ----------------------------
    // R2 physically connects his interface probe to the detention block terminal
    // and registers himself as the active query processor for that panel.
    // We attach this filter to the JFace viewer so it participates in every refresh.
    // ---------------------------------------------------------------------------------
    /**
     * Registers this filter with the given {@link TreeViewer} so it is applied on every refresh.
     * You must call this once after creating the filter; without it the viewer won't know
     * we exist and no filtering will happen.
     *
     * <p>For example -- R2 registers with the terminal:</p>
     * <pre>
     *   R2 inserts his interface probe into the panel socket.
     *   The station network acknowledges: "External query processor registered."
     *   From here on, every record request passes through R2's filter logic.
     * </pre>
     *
     * @param viewer  the tree viewer we should filter; stored and used when we fire property changes
     */
    public void connect( TreeViewer viewer )
    {
        this.viewer = viewer;
        viewer.addFilter( this );
    }


    // -- R2 RUNS THE QUERY: DOES THIS RECORD MAKE THE CUT? ---------------------
    // R2 evaluates each detention record against the search criteria.
    // For attribute-group rows, he checks if at least one child value passes.
    // For individual value rows, he checks the value itself directly.
    // Records that don't match are quietly dropped from the results.
    // ---------------------------------------------------------------------------------
    /**
     * Decides whether a given element should be visible in the entry editor.
     * JFace calls this for every row during a viewer refresh.
     * For {@link IAttribute} group rows we return true if at least one child value
     * passes the quick filter; for {@link IValue} rows we check the value directly.
     *
     * <p>For example -- R2 evaluates a record:</p>
     * <pre>
     *   Record: attribute "mail", value "leia@alderaan.gov"
     *   Quick filter attribute: "mail"    -> "mail".contains("mail") = true
     *   Quick filter value:     "alderaan" -> "leia@alderaan.gov".contains("alderaan") = true
     *   Result: record passes. Show it.
     * </pre>
     *
     * @param viewer         the viewer requesting the filter decision
     * @param parentElement  the parent node (used for context, not directly checked here)
     * @param element        the element being tested -- an {@link IAttribute} or {@link IValue}
     * @return               {@code true} to show the element, {@code false} to hide it
     */
    public boolean select( Viewer viewer, Object parentElement, Object element )
    {
        if ( element instanceof IAttribute )
        {
            // check if one of the values goes through the quick filter
            IValue[] values = ( ( IAttribute ) element).getValues();

            for ( IValue value : values )
            {
                if ( goesThroughQuickFilter( value ) )
                {
                    return true;
                }
            }

            return false;
        }
        else if ( element instanceof IValue )
        {
            // check quick filter
            return goesThroughQuickFilter( ( IValue ) element );
        }

        return true;
    }


    // -- R2 APPLIES THE DUAL-CRITERION CHECK ------------------------------------
    // R2 runs two checks in sequence: first the attribute-name filter, then the
    // value-text filter. If either fails, the record is suppressed. Binary values
    // are always hidden when a value filter is active, since we can't do a
    // meaningful text match on raw bytes.
    // ---------------------------------------------------------------------------------
    /**
     * The core filter predicate: returns true if the given {@link IValue} matches both
     * the quick-filter attribute pattern and the quick-filter value pattern.
     * Matching is case-insensitive substring matching. Binary values are always hidden
     * when a value filter string is active, because we can't compare bytes to text.
     *
     * <p>For example -- R2's dual check:</p>
     * <pre>
     *   Attribute filter "cn"  -> only keep values whose attribute description contains "cn"
     *   Value filter "skywalker" -> only keep values whose string content contains "skywalker"
     *   Binary blob             -> hidden whenever a value filter is active
     * </pre>
     *
     * @param value  the individual LDAP value to test against both filter strings
     * @return       {@code true} if the value passes both criteria (or criteria are empty)
     */
    private boolean goesThroughQuickFilter( IValue value )
    {
        // filter attribute description
        if ( !Strings.isEmpty( quickFilterAttribute ) )
        {
            if ( value.getAttribute().getDescription().toUpperCase().indexOf( quickFilterAttribute.toUpperCase() ) == -1 )
            {
                return false;
            }
        }

        // filter value
        if ( !Strings.isEmpty( quickFilterValue ) )
        {
            if ( value.isString()
                && value.getStringValue().toUpperCase().indexOf( quickFilterValue.toUpperCase() ) == -1 )
            {
                return false;
            }
            else if ( value.isBinary() )
            {
                return false;
            }
        }

        return true;
    }


    // -- R2 RETRACTS HIS PROBE AND GOES DARK ------------------------------------
    // The mission is over; R2 disconnects from the terminal and clears his
    // reference to the viewer so the GC can reclaim the memory.
    // ---------------------------------------------------------------------------------
    /**
     * Cleans up this filter when the viewer is being torn down.
     * After this call we hold no references and should not be used again.
     *
     * <p>For example -- R2 disconnects:</p>
     * <pre>
     *   R2 retracts his interface probe from the terminal socket.
     *   All query state is cleared. The panel is free for other uses.
     * </pre>
     */
    public void dispose()
    {
        viewer = null;
    }


    // -- R2 REPORTS THE CURRENT ATTRIBUTE SEARCH TERM ---------------------------
    // Han leans over and asks "R2, what attribute are you filtering on right now?"
    // R2 chirps back the current quickFilterAttribute string.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the attribute-name substring currently used by the quick filter.
     * An empty string means no attribute filtering is active (all attributes pass).
     *
     * <p>For example -- R2 displays the current attribute criterion:</p>
     * <pre>
     *   Han: "What are you filtering by?"
     *   R2: "BWOOP" -> "mail"
     * </pre>
     *
     * @return  the current attribute quick-filter string; never null, may be empty
     */
    public String getQuickFilterAttribute()
    {
        return quickFilterAttribute;
    }


    // -- R2 UPDATES THE ATTRIBUTE SEARCH TERM -----------------------------------
    // The user has typed something new in the "Attribute" text box.
    // R2 updates his internal criterion and fires a property-change event so
    // any interested listeners (like the quick-filter widget itself) know the state changed.
    // ---------------------------------------------------------------------------------
    /**
     * Updates the attribute-name substring used by the quick filter.
     * Only does work if the new value actually differs from the current one,
     * and fires a preference property-change event so listeners can react.
     *
     * <p>For example -- R2 reprograms his search term:</p>
     * <pre>
     *   Old criterion: ""  (no filter)
     *   User types: "cn"
     *   R2 updates his registers and broadcasts: "QuickFilterAttributeChanged"
     * </pre>
     *
     * @param quickFilterAttribute  the new attribute substring to filter on; empty string = no filter
     */
    public void setQuickFilterAttribute( String quickFilterAttribute )
    {
        if ( !this.quickFilterAttribute.equals( quickFilterAttribute ) )
        {
            String oldValue = this.quickFilterAttribute;
            this.quickFilterAttribute = quickFilterAttribute;
            BrowserCommonActivator.getDefault().getPreferenceStore()
                .firePropertyChangeEvent( "QuickFilterAttributeChanged", oldValue, quickFilterAttribute ); //$NON-NLS-1$
        }
    }


    // -- R2 REPORTS THE CURRENT VALUE SEARCH TERM -------------------------------
    // Han asks "what value text are you matching against?"
    // R2 chirps back the current quickFilterValue string.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the value-text substring currently used by the quick filter.
     * An empty string means no value filtering is active (all values pass the value check).
     *
     * <p>For example -- R2 displays the current value criterion:</p>
     * <pre>
     *   Han: "And the value filter?"
     *   R2: "BWOOP" -> "skywalker"
     * </pre>
     *
     * @return  the current value quick-filter string; never null, may be empty
     */
    public String getQuickFilterValue()
    {
        return quickFilterValue;
    }


    // -- R2 UPDATES THE VALUE SEARCH TERM ---------------------------------------
    // The user has typed something new in the "Value" text box.
    // R2 updates his value criterion and fires the matching property-change event.
    // ---------------------------------------------------------------------------------
    /**
     * Updates the value-text substring used by the quick filter.
     * Only does work if the new value actually differs from the current one,
     * and fires a preference property-change event so listeners can react.
     *
     * <p>For example -- R2 adjusts the value criterion:</p>
     * <pre>
     *   Old criterion: ""  (no filter)
     *   User types: "alderaan"
     *   R2 updates and broadcasts: "QuickFilterValueChanged"
     * </pre>
     *
     * @param quickFilterValue  the new value substring to filter on; empty string = no filter
     */
    public void setQuickFilterValue( String quickFilterValue )
    {
        if ( !this.quickFilterValue.equals( quickFilterValue ) )
        {
            String oldValue = this.quickFilterValue;
            this.quickFilterValue = quickFilterValue;
            BrowserCommonActivator.getDefault().getPreferenceStore()
                .firePropertyChangeEvent( "QuickFilterValueChanged", oldValue, quickFilterAttribute ); //$NON-NLS-1$
        }
    }
}
