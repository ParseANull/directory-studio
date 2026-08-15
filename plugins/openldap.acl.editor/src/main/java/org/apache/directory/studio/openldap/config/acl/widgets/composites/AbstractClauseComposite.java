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


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: AbstractClauseComposite — BASE IMPERIAL DIRECTIVE FORM ────────────
// Grand Moff Tarkin issues a standard base form to all directive panels in the
// visual editor: every panel shares the same three fields (visualEditorComposite,
// connection, context). This abstract class holds those fields and provides the
// default (no-op) implementations of createComposite() and saveWidgetSettings()
// so concrete subclasses only need to override what they actually implement.
// Concrete subclasses (WhatClauseDnComposite, WhoClauseDnComposite, etc.) extend
// this and override createComposite() to build their SWT widgets.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base implementing {@link ClauseComposite}. Stores the visual editor
 * composite, the LDAP browser connection, and the ACL context. Provides
 * default no-op implementations of {@link #createComposite(Composite)} and
 * {@link #saveWidgetSettings()}.
 *
 * <p>Think of this class as Grand Moff Tarkin's standard directive form —
 * all concrete clause panels extend this base and fill in their own SWT widgets.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractClauseComposite implements ClauseComposite
{
    /** The visual editor composite */
    protected Composite visualEditorComposite;

    /** The connection */
    protected IBrowserConnection connection;

    /** The ACL context in use */
    protected OpenLdapAclValueWithContext context;


    // ── Default Constructor ────────────────────────────────────────────────────
    /**
     * Creates a new instance of AbstractClauseComposite with no-arg (for
     * subclasses that set fields directly).
     */
    public AbstractClauseComposite()
    {
    }


    // ── Context + Visual Editor Constructor ───────────────────────────────────
    // Tarkin hands the form its context and visual editor reference on creation.
    // The connection is extracted from the context at construction time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance with the given ACL context and visual editor composite.
     * Extracts the LDAP connection from the context.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite (for layout refresh).
     */
    public AbstractClauseComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        this.context = context;
        this.visualEditorComposite = visualEditorComposite;
        connection = context.getConnection();
    }


    // ── Default createComposite (no-op) ──────────────────────────────────────
    /**
     * Default implementation — returns {@code null}. Concrete subclasses
     * override this to build their SWT widgets.
     *
     * {@inheritDoc}
     *
     * @param parent  The parent composite.
     * @return        {@code null} by default.
     */
    public Composite createComposite( Composite parent )
    {
        return null;
    }


    // ── Visual Editor Composite Accessor ──────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return  The visual editor composite.
     */
    public Composite getVisualEditorComposite()
    {
        return visualEditorComposite;
    }


    // ── Visual Editor Composite Mutator ───────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @param visualEditorComposite  The new visual editor composite.
     */
    public void setVisualEditorComposite( Composite visualEditorComposite )
    {
        this.visualEditorComposite = visualEditorComposite;
    }


    // ── Connection Accessor ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return  The current LDAP browser connection; may be {@code null}.
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── Connection Mutator ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @param connection  The new LDAP browser connection.
     */
    public void setConnection( IBrowserConnection connection )
    {
        this.connection = connection;
    }


    // ── Default saveWidgetSettings (no-op) ───────────────────────────────────
    /**
     * Default implementation — no-op. Concrete subclasses override to persist
     * widget state (e.g. expandable section expand/collapse).
     *
     * {@inheritDoc}
     */
    public void saveWidgetSettings()
    {
    }


    // ── Context Accessor ──────────────────────────────────────────────────────
    /**
     * Returns the ACL context in use.
     *
     * @return  The ACL context.
     */
    public OpenLdapAclValueWithContext getContext()
    {
        return context;
    }


    // ── Context Mutator ───────────────────────────────────────────────────────
    /**
     * Sets the ACL context in use.
     *
     * @param context  The new ACL context.
     */
    public void setContext( OpenLdapAclValueWithContext context )
    {
        this.context = context;
    }
}
