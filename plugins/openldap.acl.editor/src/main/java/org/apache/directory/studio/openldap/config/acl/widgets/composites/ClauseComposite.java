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


// ── CLASS: ClauseComposite — IMPERIAL DIRECTIVE FORM CONTRACT ─────────────────
// Grand Moff Tarkin demands that every security-directive form implement the same
// interface: it must be able to create its SWT composite on demand, expose its
// visual editor parent, accept a live LDAP connection for DN browsing, and save
// its own widget settings before closing. This interface is that Imperial contract.
// Any Who or What clause composite that can be embedded in the visual ACL editor
// must implement it so the widget builder can treat them uniformly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Contract for every clause composite embedded in the visual ACL editor. Defines
 * the lifecycle operations: create the SWT composite, get/set the visual editor
 * parent, get/set the LDAP connection, save widget settings, and access the ACL
 * context.
 *
 * <p>Think of this interface as Grand Moff Tarkin's standard form specification —
 * every security-directive form that appears in the editor must conform.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ClauseComposite
{
    // ── Creating the SWT Composite ────────────────────────────────────────────
    /**
     * Creates and returns the SWT {@link Composite} for this clause inside the
     * given parent.
     *
     * @param parent  The parent composite.
     * @return        The newly created composite.
     */
    Composite createComposite( Composite parent );


    // ── Accessing the Visual Editor Composite ────────────────────────────────
    /**
     * Returns the visual editor composite (used for layout refresh calls).
     *
     * @return  The visual editor composite.
     */
    Composite getVisualEditorComposite();


    // ── Updating the Visual Editor Composite Reference ───────────────────────
    /**
     * Sets the visual editor composite reference. Called when the parent changes.
     *
     * @param visualEditorComposite  The new visual editor composite.
     */
    void setVisualEditorComposite( Composite visualEditorComposite );


    // ── Accessing the LDAP Connection ─────────────────────────────────────────
    /**
     * Returns the current LDAP browser connection used for DN/attribute browsing.
     *
     * @return  The current connection; may be {@code null}.
     */
    IBrowserConnection getConnection();


    // ── Updating the LDAP Connection ──────────────────────────────────────────
    /**
     * Sets the LDAP browser connection. Implementations should call {@code setInput()}
     * to refresh any entry widgets after the connection changes.
     *
     * @param connection  The new connection.
     */
    void setConnection( IBrowserConnection connection );


    // ── Saving Widget Settings ────────────────────────────────────────────────
    /**
     * Persists any widget-level settings (e.g. expand/collapse state of expandable
     * sections). Called by the visual editor before it closes.
     */
    void saveWidgetSettings();


    // ── Accessing the ACL Context ─────────────────────────────────────────────
    /**
     * Returns the ACL context in use.
     *
     * @return  The ACL context.
     */
    OpenLdapAclValueWithContext getContext();


    // ── Updating the ACL Context ──────────────────────────────────────────────
    /**
     * Sets the ACL context in use.
     *
     * @param context  The new ACL context.
     */
    void setContext( OpenLdapAclValueWithContext context );
}
