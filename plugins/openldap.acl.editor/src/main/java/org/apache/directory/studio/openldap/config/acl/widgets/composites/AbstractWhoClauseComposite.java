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
package org.apache.directory.studio.openldap.config.acl.widgets.composites;


import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: AbstractWhoClauseComposite — GENERIC WHO CLAUSE FORM ──────────────
// Grand Moff Tarkin issues a who-clause form template that all concrete who-clause
// composites extend. Beyond the base directive fields (connection, visual editor,
// context), this template adds a typed who-clause field so each concrete composite
// can read and write its own specific clause type without casting. getClause()
// and setClause() expose that typed field. Concrete subclasses are
// WhoClauseDnComposite, WhoClauseDnAttributeComposite, WhoClauseGroupComposite,
// WhoClauseAnonymousComposite, etc.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Generic abstract base for all who-clause composites. Extends
 * {@link AbstractClauseComposite} with a typed {@code whoClause} field, plus
 * {@link #getClause()} and {@link #setClause(Object)} accessors.
 *
 * <p>Think of this class as Grand Moff Tarkin's generic who-clause form template —
 * concrete subclasses add their own SWT controls on top of this.</p>
 *
 * @param <C>  The concrete who-clause type (e.g. {@code AclWhoClauseDn}).
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractWhoClauseComposite<C> extends AbstractClauseComposite
{
    /** The Who clause */
    protected C whoClause;

    // ── Default Constructor ────────────────────────────────────────────────────
    /**
     * Creates a new instance with no clause pre-set (for subclasses that
     * initialise the clause separately).
     */
    public AbstractWhoClauseComposite()
    {
    }


    // ── Context + Clause + Visual Editor Constructor ──────────────────────────
    // Tarkin hands the form its clause object as well as the context and visual
    // editor composite.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance with the given ACL context, typed clause, and visual
     * editor composite.
     *
     * @param context               The ACL context.
     * @param whoClause             The typed who-clause to edit.
     * @param visualEditorComposite The visual editor composite (for layout refresh).
     */
    public AbstractWhoClauseComposite( OpenLdapAclValueWithContext context, C whoClause, Composite visualEditorComposite )
    {
        super( context, visualEditorComposite );
        this.whoClause = whoClause;
    }


    // ── Returning the Typed Clause ────────────────────────────────────────────
    /**
     * Returns the typed who-clause currently being edited.
     *
     * @return  The typed clause; may be {@code null} if none was set.
     */
    public C getClause()
    {
        return whoClause;
    }


    // ── Setting the Typed Clause ──────────────────────────────────────────────
    /**
     * Sets the typed who-clause to edit. Concrete subclasses that also call
     * {@code setInput()} should override this to refresh their SWT widgets.
     *
     * @param clause  The new who-clause.
     */
    public void setClause( C clause )
    {
        this.whoClause = clause;
    }
}
