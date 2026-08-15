/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.model;

// ── CLASS: AclWhatClause — DEATH STAR MANIFEST: WHAT IS BEING PROTECTED ─────
// Tarkin's clearance manifest has a "what" section that identifies the resource
// being protected. A what-clause can name just about anything: a DN (or pattern
// of DNs), an LDAP filter that dynamically selects entries, a list of
// attributes, or the universal star. This base class stores the filter and
// attributes sub-clauses and provides constructors for the common combinations.
// Specific leaf types (AclWhatClauseStar, AclWhatClauseDn) extend it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The base class for the "access to [what]" portion of an OpenLDAP ACL rule.
 * In its base form it holds an optional {@link AclWhatClauseFilter} and/or
 * {@link AclWhatClauseAttributes}. Concrete subclasses (star, dn, filter,
 * attributes) represent the different selectors. Think of this class as the
 * resource-description section of Tarkin's clearance form — before you say who
 * gets access, you first describe what is being guarded.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhatClause
{
    /** The filter clause */
    private AclWhatClauseFilter filterClause;

    /** The attributes clause */
    private AclWhatClauseAttributes attributesClause;


    // ── Default Constructor: Blank What-Clause ────────────────────────────────
    // An empty manifest entry for the resource section — no filter, no
    // attributes yet. The parser or UI fills in the details later.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty what-clause with no filter and no attributes. Used when
     * the parser is building a clause incrementally or when the UI creates a
     * new rule from scratch.
     */
    public AclWhatClause()
    {
    }


    // ── Full Constructor: All Possible Sub-Clauses ────────────────────────────
    // Tarkin specifies everything at once on the manifest — star, DN, filter,
    // and attributes — in one go. This constructor captures all of them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a what-clause pre-populated with both a filter and an attributes
     * sub-clause. The starClause and dnClause parameters are present for
     * signature symmetry but are not stored here — they are handled by their own
     * concrete subclasses.
     *
     * <p>For example — Tarkin filling out the full resource section at once:</p>
     * <pre>
     *   AclWhatClause wc = new AclWhatClause(null, null, filterClause, attrClause);
     * </pre>
     *
     * @param starClause        Unused in this base class (present for symmetry).
     * @param dnClause          Unused in this base class (present for symmetry).
     * @param filterClause      The LDAP filter that selects target entries.
     * @param attributesClause  The attribute selector for the target entries.
     */
    public AclWhatClause( AclWhatClauseStar starClause, AclWhatClauseDn dnClause, AclWhatClauseFilter filterClause,
        AclWhatClauseAttributes attributesClause )
    {
        this.filterClause = filterClause;
        this.attributesClause = attributesClause;
    }


    // ── Filter-Only Constructor ────────────────────────────────────────────────
    // Tarkin uses just the filter section when only a search-expression selector
    // is needed, without an attributes sub-clause.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a what-clause with only a filter sub-clause. Use this when the
     * rule targets entries matching an LDAP filter expression but does not
     * restrict to specific attributes.
     *
     * <p>For example — a filter-only what-clause:</p>
     * <pre>
     *   new AclWhatClause(filterClause); // → "filter=(uid=luke*)"
     * </pre>
     *
     * @param filterClause  The filter sub-clause to store.
     */
    public AclWhatClause( AclWhatClauseFilter filterClause )
    {
        this.filterClause = filterClause;
    }


    // ── Attributes-Only Constructor ───────────────────────────────────────────
    // Tarkin uses just the attributes section when the rule targets a specific
    // set of attributes without a filter qualifier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a what-clause with only an attributes sub-clause. Use this when
     * the rule specifies which attributes are covered without an LDAP filter
     * to narrow the entry selection.
     *
     * <p>For example — an attributes-only what-clause:</p>
     * <pre>
     *   new AclWhatClause(attrClause); // → "attrs=userPassword"
     * </pre>
     *
     * @param attributesClause  The attributes sub-clause to store.
     */
    public AclWhatClause( AclWhatClauseAttributes attributesClause )
    {
        this.attributesClause = attributesClause;
    }


    // ── Reading the Attributes Sub-Clause ─────────────────────────────────────
    // Tarkin's adjutant reads the attributes section from the manifest entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attributes sub-clause stored in this what-clause, or
     * {@code null} if no attributes restriction was specified.
     *
     * <p>For example — reading the attribute selector:</p>
     * <pre>
     *   AclWhatClauseAttributes attrs = clause.getAttributesClause();
     *   // attrs.toString() → "attrs=cn,sn"
     * </pre>
     *
     * @return  The {@link AclWhatClauseAttributes}, or {@code null}.
     */
    public AclWhatClauseAttributes getAttributesClause()
    {
        return attributesClause;
    }


    // ── Reading the Filter Sub-Clause ─────────────────────────────────────────
    // Tarkin's adjutant reads the filter section from the manifest entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the filter sub-clause stored in this what-clause, or {@code null}
     * if no filter was specified.
     *
     * <p>For example — reading the filter selector:</p>
     * <pre>
     *   AclWhatClauseFilter f = clause.getFilterClause();
     *   // f.getFilter() → "(objectClass=person)"
     * </pre>
     *
     * @return  The {@link AclWhatClauseFilter}, or {@code null}.
     */
    public AclWhatClauseFilter getFilterClause()
    {
        return filterClause;
    }


    // ── Stamping the Attributes Sub-Clause ────────────────────────────────────
    // Tarkin's parser or UI stamps an attributes sub-clause onto the manifest
    // entry when the ACL text or the user specifies one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the attributes sub-clause on this what-clause. Called by the parser
     * after recognising an {@code attrs=} token, or by the UI when the user
     * selects attribute-based targeting.
     *
     * @param attributesClause  The {@link AclWhatClauseAttributes} to store.
     */
    public void setAttributesClause( AclWhatClauseAttributes attributesClause )
    {
        this.attributesClause = attributesClause;
    }


    // ── Stamping the Filter Sub-Clause ────────────────────────────────────────
    // The parser stamps the filter sub-clause after recognising "filter=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the filter sub-clause on this what-clause. Called by the parser after
     * recognising a {@code filter=} token in the ACL text.
     *
     * @param filterClause  The {@link AclWhatClauseFilter} to store.
     */
    public void setFilterClause( AclWhatClauseFilter filterClause )
    {
        this.filterClause = filterClause;
    }
}
