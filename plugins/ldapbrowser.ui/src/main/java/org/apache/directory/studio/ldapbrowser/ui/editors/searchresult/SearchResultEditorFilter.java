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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import java.util.ArrayList;

import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


// ── CLASS: SearchResultEditorFilter — Mace Windu Confronting Palpatine ────────
// Mace Windu doesn't let anyone through unchecked — he stands at the door and
// decides: "Is this person Sith? Out. Is this person the Chancellor? In."
// Every row in the search result table passes through this filter — if its values
// don't match the quick-filter text, it's blocked from the display.  Binary data
// is always blocked because you can't match against bytes with a text search.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link ViewerFilter} that restricts the search result table to rows whose
 * attribute values contain the current quick-filter text.
 * We check every returning attribute on every row (plus the DN if shown), and only
 * pass rows that have at least one string value containing the filter text
 * (case-insensitive).  Binary values are always filtered out.
 * Think of Mace Windu at the Senate door: every row has to pass inspection,
 * and he's not letting anything suspicious through.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorFilter extends ViewerFilter
{

    /** The content provider. */
    protected SearchResultEditorContentProvider contentProvider;

    /** The quick filter value. */
    protected String quickFilterValue;

    /** The show Dn flag. */
    private boolean showDn;


    // ── Mace Windu Takes His Post ─────────────────────────────────────────────
    // Mace walks to the door and takes his post — no filter text yet, so everyone
    // gets through by default.  The filter only becomes active when the user types.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a filter with an empty quick-filter value.
     * An empty value means the filter is inactive — all rows pass through.
     */
    public SearchResultEditorFilter()
    {
        this.quickFilterValue = ""; //$NON-NLS-1$
    }


    // ── Mace Windu Registers With the Event System ────────────────────────────
    // Mace needs a way to trigger a refresh when he decides to tighten the rules.
    // We connect to the content provider so we can call refresh() when the filter
    // text changes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Connects this filter to the content provider so it can trigger table refreshes.
     * Call this after constructing both the filter and the content provider.
     *
     * @param contentProvider the content provider to notify when the filter value changes
     */
    public void connect( SearchResultEditorContentProvider contentProvider )
    {
        this.contentProvider = contentProvider;
    }


    // ── Mace Windu Receives the New Briefing ──────────────────────────────────
    // A new search is selected — Mace updates his checklist: which attributes to
    // inspect, and whether to also check the DN.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the editor's input changes to a new search.
     * We update the DN-visibility flag so we know whether to include the DN in
     * our matching logic.
     *
     * @param newSearch the newly active search (not directly used here; subclasses may need it)
     * @param showDn    whether the DN column is currently visible
     */
    public void inputChanged( ISearch newSearch, boolean showDn )
    {
        this.showDn = showDn;
    }


    // ── Mace Windu Checks If He's On Duty ────────────────────────────────────
    // If there's no filter text, Mace is standing down — everyone gets through.
    // Once the user types something, Mace is on active duty.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the quick filter is currently active (non-empty).
     * When inactive, the parent's {@link #filter} skips the per-element {@link #select}
     * call and returns the original array unchanged.
     *
     * @return {@code true} if there is a non-empty filter string
     */
    public boolean isFiltered()
    {
        return quickFilterValue != null && !"".equals( quickFilterValue ); //$NON-NLS-1$
    }


    // ── Mace Windu Inspects the Crowd ─────────────────────────────────────────
    // Mace stands at the entrance and inspects each arrival — if filtering is active,
    // every element goes through his scrutiny.  If not, the whole crowd passes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Filters the array of elements, returning only those that pass {@link #select}.
     * When the filter is inactive we return the original array directly (fast path).
     * When active we iterate and collect passing elements into a new array.
     *
     * @param viewer   the table viewer (passed to select)
     * @param parent   the parent element (passed to select)
     * @param elements the full array of elements to filter
     * @return the subset of elements that passed inspection; never null
     */
    public Object[] filter( Viewer viewer, Object parent, Object[] elements )
    {
        if ( isFiltered() )
        {
            int size = elements.length;
            ArrayList<Object> out = new ArrayList<Object>( size );
            for ( int i = 0; i < size; ++i )
            {
                Object element = elements[i];
                if ( select( viewer, parent, element ) )
                {
                    out.add( element );
                }
            }

            return out.toArray();
        }
        else
        {
            return elements;
        }
    }


    // ── Mace Windu Scrutinizes One Arrival ────────────────────────────────────
    // One person steps up to Mace — he checks every attribute they're carrying.
    // If any string value contains the filter text, they pass.  Binary data?
    // Blocked.  DN matches?  Let through.  Nothing matches?  Turned away.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given element should be visible in the table.
     * For {@link ISearchResult} elements we check every returning attribute's values
     * for a case-insensitive substring match against the quick filter.  We also check
     * the DN if it's displayed.  Non-search-result elements always pass through.
     *
     * @param viewer        the table viewer (not used directly here)
     * @param parentElement the parent element (not used directly here)
     * @param element       the row element to test
     * @return {@code true} if the row should be shown
     */
    public boolean select( Viewer viewer, Object parentElement, Object element )
    {
        if ( element instanceof ISearchResult )
        {
            ISearchResult searchResult = ( ISearchResult ) element;

            String[] returningAttributes = searchResult.getSearch().getReturningAttributes();
            for ( int r = 0; r < returningAttributes.length; r++ )
            {
                String ra = returningAttributes[r];
                AttributeHierarchy ah = searchResult.getAttributeWithSubtypes( ra );
                if ( ah != null )
                {
                    IAttribute[] attributes = ah.getAttributes();
                    for ( int i = 0; i < attributes.length; i++ )
                    {
                        IValue[] values = attributes[i].getValues();
                        for ( int k = 0; k < values.length; k++ )
                        {
                            if ( this.goesThroughQuickFilter( values[k] ) )
                            {
                                return true;
                            }
                        }
                    }
                }
            }

            if ( showDn
                && searchResult.getDn().getName().toUpperCase().indexOf( quickFilterValue.toUpperCase() ) > -1 )
            {
                return true;
            }

            return false;
        }
        else
        {
            return true;
        }
    }


    // ── Mace Windu Checks a Single Document ───────────────────────────────────
    // A single attribute value is presented — Mace reads it.  Binary data?
    // Automatic fail.  Doesn't contain the filter text?  Fail.  Otherwise pass.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given value passes the quick filter.
     * Binary values always fail.  String values pass if they contain the filter
     * text as a case-insensitive substring.
     *
     * @param value the LDAP attribute value to test
     * @return {@code true} if the value matches the quick-filter text
     */
    private boolean goesThroughQuickFilter( IValue value )
    {
        if ( value.isString() && value.getStringValue().toUpperCase().indexOf( quickFilterValue.toUpperCase() ) == -1 )
        {
            return false;
        }
        else if ( value.isBinary() )
        {
            return false;
        }

        return true;
    }


    // ── Mace Windu Steps Away From the Door ───────────────────────────────────
    // The editor is closing — Mace releases his content provider reference and
    // goes off duty.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the content provider reference.
     * Call this when the filter is no longer needed.
     */
    public void dispose()
    {
        contentProvider = null;
    }


    // ── Mace Windu Reports the Current Passcode ───────────────────────────────
    // Anyone who needs to know what filter is currently active can ask.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current quick-filter text.
     *
     * @return the filter string; empty string if the filter is inactive
     */
    public String getQuickFilterValue()
    {
        return quickFilterValue;
    }


    // ── Mace Windu Updates His Passcode ───────────────────────────────────────
    // The user typed a new filter string — Mace updates his checklist and
    // immediately triggers a refresh so the table reflects the new rules.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the quick-filter text and triggers a content provider refresh.
     * If the new value is the same as the current value, we do nothing — no
     * unnecessary refresh.  Otherwise we update the field and call
     * {@link SearchResultEditorContentProvider#refresh()}.
     *
     * @param quickFilterValue the new filter text; may be empty to deactivate filtering
     */
    public void setQuickFilterValue( String quickFilterValue )
    {
        if ( !this.quickFilterValue.equals( quickFilterValue ) )
        {
            this.quickFilterValue = quickFilterValue;
            if ( contentProvider != null )
            {
                contentProvider.refresh();
            }
        }
    }

}
