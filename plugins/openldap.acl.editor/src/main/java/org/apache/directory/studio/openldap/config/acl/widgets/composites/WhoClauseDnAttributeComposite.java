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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseDnAttr;


// ── CLASS: WhoClauseDnAttributeComposite — TARKIN MATCHING BY DN ATTRIBUTE ────
// Grand Moff Tarkin targets a who-clause by the DN stored in a specific attribute
// of the requesting entry (e.g. "dnattr=manager"). This composite renders a
// "DN Attribute:" label and a combo box for the attribute name. setClause() and
// setConnection() both call setInput() to keep the combo in sync with the model.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the {@code dnattr} who-clause. Renders a "DN Attribute:"
 * label and a combo box for the attribute name whose value is the requestor's DN.
 *
 * <p>Think of this class as Grand Moff Tarkin matching who-clauses by the DN stored
 * in a named attribute of the requesting entry.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhoClauseDnAttributeComposite extends AbstractWhoClauseComposite<AclWhoClauseDnAttr>
{
    /** The DN attribute combo */
    private Combo dnAttributeCombo;


    // ── Constructor With Explicit Clause ──────────────────────────────────────
    /**
     * Creates a new DN-attribute who-clause composite with an explicit clause.
     *
     * @param context               The ACL context.
     * @param clause                The DN-in-attribute clause to edit.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseDnAttributeComposite( OpenLdapAclValueWithContext context, AclWhoClauseDnAttr clause, Composite visualEditorComposite )
    {
        super( context, clause, visualEditorComposite );
    }


    // ── Constructor Without Explicit Clause ───────────────────────────────────
    /**
     * Creates a new DN-attribute who-clause composite with a default
     * {@link AclWhoClauseDnAttr} instance.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseDnAttributeComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, new AclWhoClauseDnAttr(), visualEditorComposite );
    }


    // ── Building the DN-Attribute Form ────────────────────────────────────────
    // Creates a two-column composite: "DN Attribute:" label + editable combo.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the DN-attribute form composite: a "DN Attribute:" label and a
     * combo box (200 px wide) for the attribute name.
     *
     * @param parent  The parent composite.
     * @return        The created composite.
     */
    public Composite createComposite( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        // DN
        BaseWidgetUtils.createLabel( composite, "DN Attribute:", 1 );
        dnAttributeCombo = BaseWidgetUtils.createCombo( composite, new String[0], -1, 1 );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        gd.widthHint = 200;
        dnAttributeCombo.setLayoutData( gd );

        return composite;
    }


    // ── Setting the Clause and Refreshing the Combo ───────────────────────────
    /**
     * Sets the DN-attribute clause and refreshes the combo with the clause's
     * attribute name.
     *
     * {@inheritDoc}
     *
     * @param clause  The new DN-attribute clause.
     */
    public void setClause( AclWhoClauseDnAttr clause )
    {
        super.setClause( clause );
        setInput();
    }


    // ── Setting the Connection and Refreshing the Combo ───────────────────────
    /**
     * Sets the LDAP browser connection and refreshes the combo.
     *
     * {@inheritDoc}
     *
     * @param connection  The new LDAP browser connection.
     */
    public void setConnection( IBrowserConnection connection )
    {
        super.setConnection( connection );
        setInput();
    }


    // ── Populating the Combo From the Model ───────────────────────────────────
    /**
     * Writes the clause's attribute name into the combo, or clears it when the
     * clause is {@code null}. Called after a clause or connection change.
     */
    private void setInput()
    {
        if ( dnAttributeCombo != null )
        {
            if ( whoClause != null )
            {
                dnAttributeCombo.setText( whoClause.getAttribute() );
            }
            else
            {
                dnAttributeCombo.setText( "" );
            }
        }
    }
}
