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
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.FilterWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseFilter;


// ── CLASS: WhatClauseFilterComposite — TARKIN ENTERING THE ENTRY FILTER ──────
// Grand Moff Tarkin types a LDAP search filter to target a set of entries.
// This composite renders a FilterWidget (a text field with filter syntax
// validation). The modify listener writes the filter string back into the
// model's AclWhatClauseFilter. setConnection() passes the live connection to
// the FilterWidget so the filter builder can suggest attribute types.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the Filter what-clause. Embeds a {@link FilterWidget}
 * and writes filter changes back into the model via a modify listener.
 *
 * <p>Think of this class as Grand Moff Tarkin keying in an LDAP search filter
 * to target a set of directory entries by predicate.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhatClauseFilterComposite extends AbstractClauseComposite
{
    /** The filter widget */
    private FilterWidget filterWidget;

    // ── Listener: Filter Widget Changed ───────────────────────────────────────
    // When Tarkin types a new filter string the listener writes it back into
    // the model's AclWhatClauseFilter.
    // ─────────────────────────────────────────────────────────────────────────
    /** Modify listener — writes the typed filter string into the what-clause model. */
    private WidgetModifyListener modifyListener = new WidgetModifyListener()
    {
        public void widgetModified( WidgetModifyEvent event )
        {
            context.getAclItem().getWhatClause().getFilterClause().setFilter( filterWidget.getFilter() );
        }
    };


    // ── Constructing the Filter Composite ─────────────────────────────────────
    /**
     * Creates a new Filter what-clause composite. SWT controls are deferred to
     * {@link #createComposite(Composite)}.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhatClauseFilterComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, visualEditorComposite );
    }


    // ── Building the Filter Form ──────────────────────────────────────────────
    // Creates a three-column composite: "Filter:" label + FilterWidget spanning
    // the remaining columns.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the filter form composite: a "Filter:" label and a {@link FilterWidget}.
     *
     * @param parent  The parent composite.
     * @return        The created composite.
     */
    public Composite createComposite( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        BaseWidgetUtils.createLabel( composite, "Filter:", 1 );
        filterWidget = new FilterWidget();
        filterWidget.createWidget( composite );
        filterWidget.addWidgetModifyListener( modifyListener );

        return composite;
    }


    // ── Updating the Connection and Refreshing the Filter Widget ──────────────
    /**
     * Sets the LDAP browser connection and calls {@code setInput()} to pass the
     * connection to the {@link FilterWidget}.
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


    // ── Populating the Filter Widget From the Model ───────────────────────────
    /**
     * Passes the current connection and filter string from the model to the
     * {@link FilterWidget}. Called after a connection change.
     */
    private void setInput()
    {
        if ( filterWidget != null )
        {
            filterWidget.setBrowserConnection( connection );
            AclWhatClauseFilter aclWhatClauseFilter = context.getAclItem().getWhatClause().getFilterClause();

            if ( aclWhatClauseFilter != null )
            {
                String filter = aclWhatClauseFilter.getFilter();
                filterWidget.setFilter( ( filter != null ) ? filter : "" );
            }
            else
            {
                filterWidget.setFilter( "" );
            }
        }
    }
}
