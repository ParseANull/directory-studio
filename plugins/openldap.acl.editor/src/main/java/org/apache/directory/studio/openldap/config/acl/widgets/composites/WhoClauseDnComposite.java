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
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseDnTypeEnum;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseDn;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseDnTypeEnum;


// ── CLASS: WhoClauseDnComposite — TARKIN IDENTIFYING REQUESTOR BY DN ─────────
// Grand Moff Tarkin points a who-clause at a specific requesting principal (DN).
// This composite renders a DN picker (EntryWidget) and a Type combo (base/exact/
// one/subtree/children/regex). The modify listener writes the picked DN back into
// the model's AclWhoClauseDn pattern field. setClause() and setConnection() call
// setInput() to keep the EntryWidget in sync.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the {@code dn} who-clause. Renders a DN picker
 * ({@link EntryWidget}) and a Type combo ({@link AclWhoClauseDnTypeEnum} values).
 * The EntryWidget modify listener writes the picked DN pattern back into the model.
 *
 * <p>Think of this class as Grand Moff Tarkin identifying the requesting principal
 * by their Distinguished Name and scope type.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhoClauseDnComposite extends AbstractWhoClauseComposite<AclWhoClauseDn>
{
    /** The array of DN who clause types */
    private static final AclWhoClauseDnTypeEnum[] aclWhoClauseDnTypes = new AclWhoClauseDnTypeEnum[]
        {
            AclWhoClauseDnTypeEnum.BASE,
            AclWhoClauseDnTypeEnum.EXACT,
            AclWhoClauseDnTypeEnum.ONE,
            AclWhoClauseDnTypeEnum.SUBTREE,
            AclWhoClauseDnTypeEnum.CHILDREN,
            AclWhoClauseDnTypeEnum.REGEX
    };

    /** The entry widget */
    private EntryWidget entryWidget;

    // ── Listener: Entry Widget Changed ─────────────────────────────────────────
    // When Tarkin picks a DN the listener writes it back as the clause pattern.
    // ─────────────────────────────────────────────────────────────────────────
    /** Modify listener — writes the picked DN into the who-clause model. */
    private WidgetModifyListener modifyListener = new WidgetModifyListener()
    {
        public void widgetModified( WidgetModifyEvent event )
        {
            getClause().setPattern( entryWidget.getDn().toString() );
        }
    };


    // ── Constructor With Explicit Clause ──────────────────────────────────────
    /**
     * Creates a new DN who-clause composite with an explicit clause.
     *
     * @param context               The ACL context.
     * @param clause                The DN clause to edit.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseDnComposite( OpenLdapAclValueWithContext context, AclWhoClauseDn clause, Composite visualEditorComposite )
    {
        super( context, clause, visualEditorComposite );
    }


    // ── Constructor Without Explicit Clause ───────────────────────────────────
    /**
     * Creates a new DN who-clause composite with a default {@link AclWhoClauseDn} instance.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseDnComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, new AclWhoClauseDn(), visualEditorComposite );
    }


    // ── Building the DN Form ──────────────────────────────────────────────────
    // Creates a three-column composite: "DN:" label + EntryWidget + "Type:" label
    // + Type combo spanning the remaining columns.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the DN who-clause form: a "DN:" label, an {@link EntryWidget}, a
     * "Type:" label, and a {@link AclWhoClauseDnTypeEnum} combo viewer.
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
                    AclWhatClauseDnTypeEnum value = ( AclWhatClauseDnTypeEnum ) element;
                    switch ( value )
                    {
                        case BASE:
                            return "Base";
                        case EXACT:
                            return "Exact";
                        case ONE:
                            return "One";
                        case SUBTREE:
                            return "Subtree";
                        case CHILDREN:
                            return "Children";
                        case REGEX:
                            return "Regex";
                    }
                }

                return super.getText( element );
            }
        } );
        whatClauseDnTypeComboViewer.setInput( aclWhoClauseDnTypes );

        return composite;
    }


    // ── Setting the Clause and Refreshing the Entry Widget ────────────────────
    /**
     * Sets the DN who-clause and refreshes the {@link EntryWidget} with the
     * clause's current pattern.
     *
     * {@inheritDoc}
     *
     * @param clause  The new DN clause.
     */
    public void setClause( AclWhoClauseDn clause )
    {
        super.setClause( clause );
        setInput();
    }


    // ── Setting the Connection and Refreshing the Entry Widget ────────────────
    /**
     * Sets the LDAP browser connection and refreshes the {@link EntryWidget}.
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
     * Passes the current connection and DN pattern to the {@link EntryWidget}.
     * Called after a clause or connection change.
     */
    private void setInput()
    {
        if ( entryWidget != null )
        {
            if ( whoClause != null )
            {
                try
                {
                    entryWidget.setInput( connection, new Dn( whoClause.getPattern() ) );
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
