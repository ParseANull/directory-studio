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


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClause;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseDn;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseDnTypeEnum;


// ── CLASS: WhatClauseDnComposite — TARKIN POINTING AT THE TARGET ENTRY ────────
// Grand Moff Tarkin uses the DN targeting panel to point the ACL at a specific
// LDAP entry or subtree. This composite renders a DN picker (EntryWidget) and
// a Type combo (base/exact/one/subtree/children/regex). The EntryWidget's modify
// listener writes the DN pattern back into the model's AclWhatClauseDn. If no
// what-clause exists yet, the constructor creates one. setConnection() passes the
// live connection to the EntryWidget so the DN browser button works.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the DN what-clause. Provides a DN picker
 * ({@link EntryWidget}) and a Type combo ({@link AclWhatClauseDnTypeEnum} values).
 * The EntryWidget modify listener writes the selected DN back into the model.
 *
 * <p>Think of this class as Grand Moff Tarkin pointing the targeting crosshair
 * at a specific LDAP entry using the DN picker.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhatClauseDnComposite extends AbstractClauseComposite
{
    /** The entry widget */
    private EntryWidget entryWidget;

    // ── Listener: DN Entry Widget Changed ─────────────────────────────────────
    // When Tarkin picks a DN from the browser the listener writes the string
    // representation back into the model's AclWhatClauseDn pattern field.
    // ─────────────────────────────────────────────────────────────────────────
    /** Modify listener — writes the picked DN into the what-clause model. */
    private WidgetModifyListener modifyListener = new WidgetModifyListener()
    {
        public void widgetModified( WidgetModifyEvent event )
        {
            ((AclWhatClauseDn)context.getAclItem().getWhatClause()).setPattern( entryWidget.getDn().toString() );
        }
    };


    // ── Constructing the DN Composite ─────────────────────────────────────────
    // Tarkin initialises the context and ensures the model has an AclWhatClauseDn.
    // The SWT controls are deferred to createComposite().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DN what-clause composite. If the model has no what-clause
     * yet an {@link AclWhatClauseDn} is installed. SWT controls are deferred to
     * {@link #createComposite(Composite)}.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhatClauseDnComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, visualEditorComposite );
        AclWhatClause whatClause = context.getAclItem().getWhatClause();

        if ( whatClause == null )
        {
            context.getAclItem().setWhatClause( new AclWhatClauseDn() );
        }
    }


    // ── Building the DN Form ──────────────────────────────────────────────────
    // Creates a three-column composite: "DN:" label + EntryWidget + "Type:" label
    // + Type combo (spanning the remaining columns).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the DN form composite: a "DN:" label, an {@link EntryWidget}, a
     * "Type:" label, and a {@link AclWhatClauseDnTypeEnum} combo viewer.
     *
     * @param parent  The parent composite.
     * @return        The created composite.
     */
    public Composite createComposite( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        // DN
        BaseWidgetUtils.createLabel( composite, "DN:", 1 );
        entryWidget = new EntryWidget();
        entryWidget.createWidget( composite );
        entryWidget.addWidgetModifyListener( modifyListener );

        // Type
        BaseWidgetUtils.createLabel( composite, "Type:", 1 );
        ComboViewer whatClauseDnTypeComboViewer = new ComboViewer( BaseWidgetUtils.createReadonlyCombo( composite,
            new String[0], -1, 1 ) );
        whatClauseDnTypeComboViewer.getCombo().setLayoutData( new GridData( SWT.NONE, SWT.NONE, false, false, 2, 1 ) );
        whatClauseDnTypeComboViewer.setContentProvider( new ArrayContentProvider() );

        whatClauseDnTypeComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof AclWhatClauseDnTypeEnum )
                {
                    return (( AclWhatClauseDnTypeEnum ) element).getName();
                }

                return super.getText( element );
            }
        } );

        whatClauseDnTypeComboViewer.setInput( AclWhatClauseDnTypeEnum.values() );

        return composite;
    }


    // ── Updating the Connection and Refreshing the Entry Widget ───────────────
    // When Tarkin connects to an LDAP server, the DN browser button in the
    // EntryWidget becomes functional.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP browser connection on this composite and calls {@code setInput()}
     * to pass the live connection to the {@link EntryWidget}.
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


    // ── Populating the Entry Widget From the Model ────────────────────────────
    /**
     * Passes the current connection and DN pattern from the model to the
     * {@link EntryWidget}. Called after a connection change.
     */
    private void setInput()
    {
        if ( entryWidget != null )
        {
            if ( context.getAclItem().getWhatClause() != null )
            {
                try
                {
                    entryWidget.setInput( connection, new Dn( ((AclWhatClauseDn)context.getAclItem().getWhatClause()).getPattern() ) );
                }
                catch ( LdapInvalidDnException e )
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else
            {
                entryWidget.setInput( connection, null );
            }
        }
    }
}
