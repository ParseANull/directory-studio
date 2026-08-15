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

package org.apache.directory.studio.ldapbrowser.core.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;


// ── CLASS: SearchParameter — C-3PO'S MISSION BRIEFING DOSSIER ────────────────
// Before C-3PO translates a Jawa dialect, he runs through his pre-mission
// checklist: "Which base camp are we starting from?  What filter phrase do we
// use?  How many entries should we read before stopping?  Do we follow alias
// pointers?  Do we chase referrals?"  All of that goes into a dossier he carries
// on every mission.
// SearchParameter is that dossier: a plain, serializable bean that holds every
// tuning knob for a single LDAP search — base DN, filter, scope, time limit,
// count limit, alias handling, referral handling, and controls.  An ISearch
// delegates all those settings to one of these, and we also serialize it to
// XML when persisting saved searches.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A serializable bean that holds all parameters for a single LDAP search.
 * Used both at run-time (as the backing store for {@link ISearch}) and when
 * persisting saved searches to XML.  All fields have sensible defaults:
 * one-level scope, {@code (objectClass=*)} filter, no limits, always
 * dereference aliases, follow referrals.
 * Think of this as C-3PO's pre-mission briefing card — every detail the
 * droid needs before diving into the directory.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchParameter implements Serializable
{

    /** The serialVersionUID. */
    private static final long serialVersionUID = 2447490121520960805L;

    /** The symbolic name. */
    private String name;

    /** The search base. */
    private Dn searchBase;

    /** The filter. */
    private String filter;

    /** The returning attributes. */
    private String[] returningAttributes;

    /** The search scope. */
    private SearchScope scope;

    /** The time limit in seconds, 0 means no limit. */
    private int timeLimit;

    /** The count limit, 0 means no limit. */
    private int countLimit;

    /** The alias dereferencing method. */
    private AliasDereferencingMethod aliasesDereferencingMethod;

    /** The referrals handling method. */
    private ReferralHandlingMethod referralsHandlingMethod;

    /** The controls */
    private List<Control> controls;

    /** The response controls */
    private List<Control> responseControls;

    /** The paged search scroll mode flag. */
    protected boolean pagedSearchScrollModeFlag;

    /** Flag indicating weather the hasChildren flag of IEntry should be initialized */
    private boolean initHasChildrenFlag;


    // ── C-3PO Prepares A Default Briefing Card ────────────────────────────────────
    // "Defaults set: one-level scan, objectClass=* filter, no limits, always
    // dereference aliases, follow all referrals.  Ready for mission assignment."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of SearchParameter with default search parameters:
     * <ul>
     * <li>null search name
     * <li>null search base
     * <li>default filter (objectClass=*)
     * <li>no returning attributes
     * <li>search scope one level
     * <li>no count limit
     * <li>no time limit
     * <li>always dereference aliases
     * <li>follow referrals
     * <li>no initialization of hasChildren flag
     * <li>no initialization of isAlias and isReferral flag
     * <li>no controls
     * <li>no response controls
     * </ul>
     */
    public SearchParameter()
    {
        name = null;
        searchBase = null;
        filter = ISearch.FILTER_TRUE;
        returningAttributes = ISearch.NO_ATTRIBUTES;
        scope = SearchScope.ONELEVEL;
        timeLimit = 0;
        countLimit = 0;
        aliasesDereferencingMethod = AliasDereferencingMethod.ALWAYS;
        referralsHandlingMethod = ReferralHandlingMethod.FOLLOW;
        controls = new ArrayList<>();
        responseControls = new ArrayList<>();
        pagedSearchScrollModeFlag = true;
        initHasChildrenFlag = false;
    }


    // ── C-3PO Reads The Entry Limit From The Briefing Card ───────────────────────
    // "Maximum entries: 0 means no limit — scan the whole section if needed."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the count limit, 0 means no limit.
     *
     * @return the count limit
     */
    public int getCountLimit()
    {
        return countLimit;
    }


    // ── C-3PO Updates The Entry Limit On The Briefing Card ───────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the count limit, 0 means no limit.
     *
     * @param countLimit the count limit
     */
    public void setCountLimit( int countLimit )
    {
        this.countLimit = countLimit;
    }


    // ── C-3PO Reads The Filter Phrase ────────────────────────────────────────────
    // "Filter phrase: '(objectClass=*)'.  Match everyone in this section."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter()
    {
        return filter;
    }


    // ── C-3PO Updates The Filter Phrase ──────────────────────────────────────────
    // Null or empty filters are automatically promoted to (objectClass=*) — the
    // catch-all that matches every entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the filter, a null or empty filter will be
     * transformed to (objectClass=*).
     *
     * @param filter the filter
     */
    public void setFilter( String filter )
    {
        if ( filter == null || "".equals( filter ) ) //$NON-NLS-1$
        {
            filter = ISearch.FILTER_TRUE;
        }
        this.filter = filter;
    }


    // ── C-3PO Reads The Mission's Symbolic Name ───────────────────────────────────
    // "Mission codename: 'Find all droids in Sector 7G'."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the symbolic name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }


    // ── C-3PO Updates The Mission's Symbolic Name ────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the symbolic name.
     *
     * @param name the name
     */
    public void setName( String name )
    {
        this.name = name;
    }


    // ── C-3PO Reads Which Attributes To Return ───────────────────────────────────
    // "Return only: cn, sn, mail — everything else stays on the server."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the returning attributes.
     *
     * @return the returning attributes
     */
    public String[] getReturningAttributes()
    {
        return returningAttributes;
    }


    // ── C-3PO Updates The Attribute Return List ───────────────────────────────────
    // Null is treated as "return all user attributes" (*).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the returning attributes, an empty array indicates none,
     * null will be transformed to '*' (all user attributes).
     *
     * @param returningAttributes the returning attributes
     */
    public void setReturningAttributes( String[] returningAttributes )
    {
        if ( returningAttributes == null )
        {
            returningAttributes = new String[]
                { SchemaConstants.ALL_USER_ATTRIBUTES };
        }
        this.returningAttributes = returningAttributes;
    }


    // ── C-3PO Reads How Deep The Scan Should Go ───────────────────────────────────
    // "Scope: ONE_LEVEL — scan this room only, not the whole Death Star."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the scope.
     *
     * @return the scope
     */
    public SearchScope getScope()
    {
        return scope;
    }


    // ── C-3PO Updates The Scan Depth ──────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the scope.
     *
     * @param scope the scope
     */
    public void setScope( SearchScope scope )
    {
        this.scope = scope;
    }


    // ── C-3PO Reads The Alias Handling Strategy ───────────────────────────────────
    // "Alias pointers: ALWAYS follow them — we want the real target."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the aliases dereferencing method.
     *
     * @return the aliases dereferencing method
     */
    public AliasDereferencingMethod getAliasesDereferencingMethod()
    {
        return aliasesDereferencingMethod;
    }


    // ── C-3PO Updates The Alias Handling Strategy ─────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the aliases dereferencing method.
     *
     * @param aliasesDereferencingMethod the aliases dereferencing method
     */
    public void setAliasesDereferencingMethod( AliasDereferencingMethod aliasesDereferencingMethod )
    {
        this.aliasesDereferencingMethod = aliasesDereferencingMethod;
    }


    // ── C-3PO Reads The Referral Handling Strategy ────────────────────────────────
    // "Referral pointers: FOLLOW — chase them to the next server."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the referrals handling method.
     *
     * @return the referrals handling method
     */
    public ReferralHandlingMethod getReferralsHandlingMethod()
    {
        return referralsHandlingMethod;
    }


    // ── C-3PO Updates The Referral Handling Strategy ─────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the referrals handling method.
     *
     * @param referralsHandlingMethod the referrals handling method
     */
    public void setReferralsHandlingMethod( ReferralHandlingMethod referralsHandlingMethod )
    {
        this.referralsHandlingMethod = referralsHandlingMethod;
    }


    // ── C-3PO Reads The Starting Location ────────────────────────────────────────
    // "Base camp: dc=empire,dc=net — we start scanning from here."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the search base.
     *
     * @return the search base
     */
    public Dn getSearchBase()
    {
        return searchBase;
    }


    // ── C-3PO Updates The Starting Location ──────────────────────────────────────
    // Null base is not allowed — C-3PO needs to know where to start.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the search base, a null search base is not allowed.
     *
     * @param searchBase the search base
     */
    public void setSearchBase( Dn searchBase )
    {
        assert searchBase != null;
        this.searchBase = searchBase;
    }


    // ── C-3PO Reads The Time Budget For This Mission ──────────────────────────────
    // "Time limit: 30 seconds.  If the scan isn't done by then, abort."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the time limit in seconds, 0 means no limit.
     *
     * @return the time limit
     */
    public int getTimeLimit()
    {
        return timeLimit;
    }


    // ── C-3PO Updates The Time Budget ────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the time limit in seconds, 0 means no limit.
     *
     * @param timeLimit the time limit
     */
    public void setTimeLimit( int timeLimit )
    {
        this.timeLimit = timeLimit;
    }


    // ── C-3PO Duplicates The Briefing Card For A New Mission ─────────────────────
    // "Copying all parameters to a fresh card — same briefing, separate mission."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        SearchParameter clone = new SearchParameter();
        clone.setName( getName() );
        clone.setSearchBase( getSearchBase() );
        clone.setFilter( getFilter() );
        clone.setReturningAttributes( getReturningAttributes() );
        clone.setScope( getScope() );
        clone.setTimeLimit( getTimeLimit() );
        clone.setCountLimit( getCountLimit() );
        clone.setAliasesDereferencingMethod( getAliasesDereferencingMethod() );
        clone.setReferralsHandlingMethod( getReferralsHandlingMethod() );
        clone.setInitHasChildrenFlag( isInitHasChildrenFlag() );
        clone.getControls().addAll( getControls() );
        clone.getResponseControls().addAll( getResponseControls() );
        return clone;
    }


    // ── C-3PO Checks Whether To Probe Each Entry's Child Count ──────────────────
    // "Should we run a sub-scan on each result to see if it has children?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the hasChildren flag of IEntry should be initialized.
     *
     * @return true, if the hasChildren flag of IEntry should be initialized
     */
    public boolean isInitHasChildrenFlag()
    {
        return initHasChildrenFlag;
    }


    // ── C-3PO Sets The Child-Probe Flag ──────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets if the hasChildren flag of IEntry should be initialized.
     *
     * @param initHasChildrenFlag the init hasChildren flag
     */
    public void setInitHasChildrenFlag( boolean initHasChildrenFlag )
    {
        this.initHasChildrenFlag = initHasChildrenFlag;
    }


    // ── C-3PO Reads The Request Controls ────────────────────────────────────────
    // "Special control packets to attach to the outgoing search request."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the request controls.
     *
     * @return the request controls
     */
    public List<Control> getControls()
    {
        return controls;
    }


    // ── C-3PO Reads The Response Controls ───────────────────────────────────────
    // "Controls that came back with the server's response."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the response controls.
     *
     * @return the response controls
     */
    public List<Control> getResponseControls()
    {
        return responseControls;
    }


    // ── C-3PO Checks Whether Paged Scroll Mode Is Active ────────────────────────
    // "Are we fetching results in scrollable pages rather than one big batch?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if paged-search scroll mode is enabled — results
     * will be fetched one page at a time with an explicit "next page" action.
     *
     * @return {@code true} if scroll mode is active.
     */
    public boolean isPagedSearchScrollMode()
    {
        return pagedSearchScrollModeFlag;
    }


    // ── C-3PO Sets The Paged Scroll Mode Flag ────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether paged-search scroll mode is enabled.
     *
     * @param pagedSearchScrollModeFlag {@code true} to enable scroll mode.
     */
    public void setPagedSearchScrollMode( boolean pagedSearchScrollModeFlag )
    {
        this.pagedSearchScrollModeFlag = pagedSearchScrollModeFlag;
    }

}
