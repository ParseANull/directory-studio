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


// ── CLASS: AclWhatClauseFilter — DEATH STAR MANIFEST: FILTERED TARGET SET ────
// Tarkin's manifest doesn't always target entries by DN. Sometimes he writes
// a search filter — "protect all entries where (objectClass=inetOrgPerson)."
// That is the LDAP filter what-clause: you give an LDAP search expression and
// OpenLDAP applies the rule to all entries that match it. This class stores
// that filter string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete what-clause that targets entries matching an LDAP search filter.
 * Written as {@code filter=(objectClass=inetOrgPerson)}, the filter is a
 * standard LDAP search filter expression applied to every entry to decide
 * if this rule's access controls apply.
 * Think of this class as a smart selector on Tarkin's manifest — instead of
 * naming every individual entry, you write a query and protect everything
 * that the query matches.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhatClauseFilter extends AclWhatClause
{
    /** The filter */
    private String filter;


    // ── Reading the Filter Expression ─────────────────────────────────────────
    // Tarkin's adjutant reads the search expression back from the manifest so
    // the UI can display it in the filter text box for editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP search filter string stored in this what-clause.
     * This is the raw filter expression, for example {@code (objectClass=person)},
     * exactly as it should appear in the ACL text.
     *
     * <p>For example — reading Tarkin's query back from the manifest:</p>
     * <pre>
     *   String f = clause.getFilter();
     *   // f == "(objectClass=inetOrgPerson)"
     * </pre>
     *
     * @return  The LDAP filter expression string; may be {@code null} if not set.
     */
    public String getFilter()
    {
        return filter;
    }


    // ── Writing the Filter Expression ─────────────────────────────────────────
    // Tarkin stamps the new search expression onto the manifest entry —
    // either from the parser reading an existing ACL or from the UI when
    // the user types a new filter.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP search filter expression for this what-clause. Called by the
     * ANTLR parser when it finds a {@code filter=} token, and by the UI when the
     * user edits the filter text field.
     *
     * <p>For example — stamping a new filter expression onto the manifest:</p>
     * <pre>
     *   clause.setFilter("(objectClass=groupOfNames)");
     *   clause.toString(); // → "filter=(objectClass=groupOfNames)"
     * </pre>
     *
     * @param filter  The LDAP filter expression to store.
     */
    public void setFilter( String filter )
    {
        this.filter = filter;
    }


    // ── Rendering the Filter Clause as ACL Text ────────────────────────────────
    // The adjutant writes "filter=" followed by the expression, which is exactly
    // what OpenLDAP needs to see in the ACL rule text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string: {@code filter=EXPR}
     * where EXPR is the stored LDAP search filter expression.
     *
     * <p>For example — serialising to ACL text:</p>
     * <pre>
     *   clause.setFilter("(uid=luke)");
     *   clause.toString(); // → "filter=(uid=luke)"
     * </pre>
     *
     * @return  The ACL text fragment for this filter what-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "filter=" );
        sb.append( filter );

        return sb.toString();
    }
}
