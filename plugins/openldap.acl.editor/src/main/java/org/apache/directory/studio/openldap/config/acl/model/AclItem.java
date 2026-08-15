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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


// ── CLASS: AclItem — DEATH STAR CLEARANCE MANIFEST: ONE COMPLETE RULE ────────
// Each page on Tarkin's clearance manifest is one complete access rule: what
// resource is being protected (the what-clause) and a list of who gets what
// level of access (the who-clauses). Together they form a single OpenLDAP ACL
// rule: "access to [what] by [who1] [level1] by [who2] [level2] ...". This
// class is that one page — one AclWhatClause plus a list of AclWhoClauses.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The top-level model object representing one complete OpenLDAP ACL rule.
 * An ACL rule has exactly one "what" clause (identifying the resource) and a
 * list of one-or-more "by" clauses (identifying who gets what access). The
 * three {@code toString} overloads let us produce the rule in a compact
 * single-line form or a pretty-printed multi-line form, with or without the
 * leading {@code "access"} keyword.
 * Think of this class as one page of Tarkin's Death Star clearance manifest —
 * resource on the top, access assignments below, all sealed into one item.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclItem
{
    /** The {@link AclWhatClause} element */
    private AclWhatClause whatClause;

    /** The {@link AclWhoClause} elements */
    private List<AclWhoClause> whoClauses = new ArrayList<AclWhoClause>();


    // ── Creating a Blank Manifest Page ────────────────────────────────────────
    // The parser creates an empty AclItem and then fills in the what-clause
    // and who-clauses one by one as it reads tokens from the ACL string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty ACL item with no what-clause and an empty who-clauses
     * list. The parser uses this to build an item incrementally, and the visual
     * editor uses it when creating a new rule from scratch.
     */
    public AclItem()
    {
    }


    // ── Creating a Pre-Populated Manifest Page ────────────────────────────────
    // When we already have a what-clause and a list of who-clauses we can
    // package them into an AclItem in one constructor call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an ACL item pre-populated with a what-clause and a list of
     * who-clauses. Useful in tests or when converting from another representation.
     *
     * <p>For example — Tarkin filling in a complete manifest page at once:</p>
     * <pre>
     *   AclItem item = new AclItem(whatClause, whoClauses);
     *   item.toString(); // → "to * by users read"
     * </pre>
     *
     * @param whatClause  The resource-selector clause.
     * @param whoClauses  The list of subject-access clauses.
     */
    public AclItem( AclWhatClause whatClause, List<AclWhoClause> whoClauses )
    {
        this.whatClause = whatClause;
        this.whoClauses = whoClauses;
    }


    // ── Reading the What-Clause ────────────────────────────────────────────────
    // The adjutant reads which resource is being protected from the manifest page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the what-clause that identifies the protected resource. This is
     * always present on a well-formed ACL item; {@code null} indicates an
     * incomplete item being built by the parser.
     *
     * <p>For example — reading which resource Tarkin is protecting:</p>
     * <pre>
     *   AclWhatClause what = item.getWhatClause();
     *   what.toString(); // → "dn.subtree=\"ou=Rebels,dc=galaxy,dc=far\""
     * </pre>
     *
     * @return  The {@link AclWhatClause}; may be {@code null} if not yet set.
     */
    public AclWhatClause getWhatClause()
    {
        return whatClause;
    }


    // ── Reading the Who-Clauses List ──────────────────────────────────────────
    // The adjutant reads the ordered list of who-clauses — the access
    // assignments for each category of subject.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of who-clauses defining subject-access pairs. The order
     * matters: OpenLDAP evaluates them top-to-bottom and applies the first one
     * that matches.
     *
     * <p>For example — reading Tarkin's access assignments:</p>
     * <pre>
     *   List&lt;AclWhoClause&gt; whos = item.getWhoClauses();
     *   // [AclWhoClauseUsers(read), AclWhoClauseStar(none)]
     * </pre>
     *
     * @return  The list of {@link AclWhoClause}; never {@code null}.
     */
    public List<AclWhoClause> getWhoClauses()
    {
        return whoClauses;
    }


    // ── Stamping the What-Clause ───────────────────────────────────────────────
    // The parser stamps the what-clause onto the manifest page after parsing the
    // "to [what]" portion of the ACL rule.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the what-clause on this ACL item. Called by the ANTLR parser after it
     * has fully parsed the "access to [what]" portion of the rule.
     *
     * @param whatClause  The resource-selector clause to apply.
     */
    public void setWhatClause( AclWhatClause whatClause )
    {
        this.whatClause = whatClause;
    }


    // ── Appending One Who-Clause ───────────────────────────────────────────────
    // The parser appends who-clauses one at a time as it reads each "by [who]
    // [level]" segment from the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single who-clause to this ACL item's list. Called by the parser
     * as it finishes parsing each "by" clause in the rule.
     *
     * <p>For example — the parser adding a "by users read" clause:</p>
     * <pre>
     *   item.addWhoClause(usersClause);
     * </pre>
     *
     * @param c  The {@link AclWhoClause} to append.
     */
    public void addWhoClause( AclWhoClause c )
    {
        whoClauses.add( c );
    }


    // ── Bulk-Adding Who-Clauses ────────────────────────────────────────────────
    // When we have a pre-built collection of who-clauses we can add them all
    // in one call rather than iterating manually.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds all elements from the given collection to the who-clauses list.
     *
     * @param c  Collection of {@link AclWhoClause} elements to add.
     */
    public void addAllWhoClause( Collection<? extends AclWhoClause> c )
    {
        whoClauses.addAll( c );
    }


    // ── Clearing the Who-Clauses List ─────────────────────────────────────────
    // When rebuilding the who-clauses from a fresh model (e.g. after a UI
    // refresh) we clear the list first to avoid duplicates.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes all who-clauses from this ACL item. Useful when the visual editor
     * needs to rebuild the clause list from scratch after the user has made
     * changes.
     */
    public void clearWhoClause()
    {
        whoClauses.clear();
    }


    // ── Rendering the Rule: Compact Form Without "access" Prefix ─────────────
    // Tarkin's adjutant writes the rule in the standard compact form —
    // "to [what] by [who1] [level1] by [who2] [level2]..." on one line.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the compact single-line ACL rule text without the leading
     * {@code "access"} keyword. Equivalent to {@code toString(false)}.
     *
     * <p>For example — default form Tarkin writes to the config file:</p>
     * <pre>
     *   item.toString()
     *   // → "to * by users read by * none"
     * </pre>
     *
     * @return  The ACL rule text.
     */
    public String toString()
    {
        return toString( false );
    }


    // ── Rendering the Rule: Optionally Prepend "access" Keyword ──────────────
    // Some contexts (like writing to an LDIF entry) require the full
    // "access to ..." form; others just want "to ...". This overload chooses.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACL rule text, optionally prefixed with {@code "access"}.
     * Some OpenLDAP configuration formats require the leading keyword;
     * others omit it.
     *
     * <p>For example — choosing the "access" prefix:</p>
     * <pre>
     *   item.toString(true)  // → "access to * by users read"
     *   item.toString(false) // → "to * by users read"
     * </pre>
     *
     * @param prependAccess  {@code true} to include the leading {@code "access"} keyword.
     * @return               The ACL rule text.
     */
    public String toString( boolean prependAccess )
    {
        return toString( prependAccess, false );
    }


    // ── Rendering the Rule: Full Control Over Format ───────────────────────────
    // For the source-editor pretty-printer we want newlines before each "by"
    // clause so the rule is easier to read. This overload adds them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACL rule text with full control over the "access" prefix and
     * pretty-printing. When {@code prettyPrint} is {@code true}, each "by" clause
     * is placed on its own line — useful for the source editor's formatter.
     *
     * <p>For example — pretty-printed output:</p>
     * <pre>
     *   item.toString(true, true)
     *   // → "access to *\nby users read\nby * none"
     * </pre>
     *
     * @param prependAccess  {@code true} to include the leading {@code "access"} keyword.
     * @param prettyPrint    {@code true} to put each "by" clause on its own line.
     * @return               The formatted ACL rule text.
     */
    public String toString( boolean prependAccess, boolean prettyPrint )
    {

        StringBuilder sb = new StringBuilder();

        // Access (if needed)
        if ( prependAccess )
        {
            sb.append( "access " );
        }

        // To
        sb.append( "to " );

        // What Clause
        if ( whatClause != null )
        {
            sb.append( whatClause.toString() );
        }

        // Who Clauses
        if ( ( whoClauses != null ) && ( whoClauses.size() > 0 ) )
        {
            for ( AclWhoClause whoClause : whoClauses )
            {
                if ( prettyPrint )
                {
                    sb.append( "\n" );
                }
                else
                {
                    sb.append( " " );
                }

                sb.append( "by " );
                sb.append( whoClause.toString() );
            }
        }

        return sb.toString();
    }
}
