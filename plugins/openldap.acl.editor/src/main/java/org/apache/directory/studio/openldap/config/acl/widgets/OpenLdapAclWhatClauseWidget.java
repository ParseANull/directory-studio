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
package org.apache.directory.studio.openldap.config.acl.widgets;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClause;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseAttributes;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseDn;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseFilter;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhatClauseAttributesComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhatClauseDnComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhatClauseFilterComposite;


// ── CLASS: OpenLdapAclWhatClauseWidget — GRAND MOFF SELECTING TARGET OBJECT ──
// Grand Moff Tarkin stands at the targeting console choosing the object the
// ACL will protect. He has three exclusive choices: DN (a specific entry),
// Filter (a pattern-matched set of entries), or Attributes (a set of attribute
// types within entries). When he selects a checkbox a new configuration panel
// appears below it; deselecting removes the panel and disposes its widgets.
// The listener for each checkbox calls createXxxComposite() or disposeComposite()
// and then forces the visual editor composite to re-layout so the holotable
// scrolls correctly. refresh() reads the current model and pre-checks the right
// checkbox, creating the appropriate sub-composite.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget representing the "Access to What" section of the visual ACL
 * editor. Provides three optional checkboxes (DN, Filter, Attributes) — when
 * a checkbox is checked a corresponding sub-composite ({@link WhatClauseDnComposite},
 * {@link WhatClauseFilterComposite}, or {@link WhatClauseAttributesComposite}) is
 * dynamically created beneath it; unchecking disposes the sub-composite.
 *
 * <p>Think of this class as Grand Moff Tarkin choosing which Imperial target the
 * ACL will govern — one checkbox for each targeting mode.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclWhatClauseWidget extends AbstractWidget
{
    /** The visual editor composite */
    private OpenLdapAclVisualEditorComposite visualEditorComposite;

    /** The context */
    private OpenLdapAclValueWithContext context;

    // UI widgets
    private Composite composite;
    private Button dnCheckbox;
    private Composite dnComposite;
    private Composite dnSubComposite;
    private Button filterCheckbox;
    private Composite filterComposite;
    private Composite filterSubComposite;
    private Button attributesCheckbox;
    private Composite attributesComposite;
    private Composite attributesSubComposite;

    private WhatClauseAttributesComposite attributesClauseComposite;

    private WhatClauseFilterComposite filterClauseComposite;

    private WhatClauseDnComposite dnClauseComposite;

    // ── Listener: DN Checkbox ─────────────────────────────────────────────────
    // When Tarkin checks DN, a DN-targeting sub-composite appears. When he
    // unchecks it the sub-composite is disposed to free resources.
    // ─────────────────────────────────────────────────────────────────────────
    /** The listener on the DN Checkbox. Creates/disposes the DN sub-composite. */
    private SelectionAdapter dnCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( org.eclipse.swt.events.SelectionEvent e )
        {
            if ( dnCheckbox.getSelection() )
            {
                createDnComposite();
            }
            else
            {
                disposeComposite( dnSubComposite );
            }

            // Refreshing the layout of the whole composite
            visualEditorComposite.layout( true, true );
        }
    };


    // ── Listener: Filter Checkbox ─────────────────────────────────────────────
    // When Tarkin checks Filter, a filter-entry sub-composite appears; uncheck disposes it.
    // ─────────────────────────────────────────────────────────────────────────
    /** The listener on the Filter Checkbox. Creates/disposes the Filter sub-composite. */
    private SelectionAdapter filterCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( org.eclipse.swt.events.SelectionEvent e )
        {
            if ( filterCheckbox.getSelection() )
            {
                createFilterComposite();
            }
            else
            {
                disposeComposite( filterSubComposite );
            }

            // Refreshing the layout of the whole composite
            visualEditorComposite.layout( true, true );
        }
    };


    // ── Listener: Attributes Checkbox ─────────────────────────────────────────
    // When Tarkin checks Attributes, an attribute-list sub-composite appears; uncheck disposes it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * The listener on the Attributes Checkbox. It creates the Attributes composite
     * when selected, dispose it when unchecked.
     */
    private SelectionAdapter attributesCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( org.eclipse.swt.events.SelectionEvent e )
        {
            if ( attributesCheckbox.getSelection() )
            {
                createAttributesComposite();
            }
            else
            {
                disposeComposite( attributesSubComposite );
            }

            // Refreshing the layout of the whole composite
            visualEditorComposite.layout( true, true );
        }
    };


    // ── Constructing the What Clause Widget ───────────────────────────────────
    // Grand Moff Tarkin opens the targeting console, stores the visual editor
    // reference, creates the three checkboxes inside an "Access to What" group,
    // and attaches their listeners. Sub-composites are created on demand.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new What Clause widget with three checkboxes (DN, Filter, Attributes)
     * inside an "Acces to What" group. Sub-composites are created dynamically when
     * the user checks a box.
     *
     * <p>For example — Tarkin opening the targeting console:</p>
     * <pre>
     *   whatClauseWidget = new OpenLdapAclWhatClauseWidget(
     *       visualEditor, parentComposite, context);
     * </pre>
     *
     * @param visualEditorComposite  The parent visual editor composite (used for layout refresh).
     * @param parent                 The composite in which the group will be created.
     * @param context                The ACL context providing the current model.
     */
    public OpenLdapAclWhatClauseWidget( OpenLdapAclVisualEditorComposite visualEditorComposite,
        Composite parent, OpenLdapAclValueWithContext context )
    {
        this.visualEditorComposite = visualEditorComposite;
        this.context = context;

        // Creating the widget base composite
        composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Creating the what group
        Group whatGroup = BaseWidgetUtils.createGroup( parent, "Acces to \"What\"", 1 );
        whatGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // DN
        dnCheckbox = BaseWidgetUtils.createCheckbox( whatGroup, "DN", 1 );
        dnComposite = BaseWidgetUtils.createColumnContainer( whatGroup, 1, 1 );
        dnCheckbox.addSelectionListener( dnCheckboxListener );

        // Filter
        filterCheckbox = BaseWidgetUtils.createCheckbox( whatGroup, "Filter", 1 );
        filterComposite = BaseWidgetUtils.createColumnContainer( whatGroup, 1, 1 );
        filterCheckbox.addSelectionListener( filterCheckboxListener );

        // Attributes
        attributesCheckbox = BaseWidgetUtils.createCheckbox( whatGroup, "Attributes", 1 );
        attributesComposite = BaseWidgetUtils.createColumnContainer( whatGroup, 1, 1 );
        attributesCheckbox.addSelectionListener( attributesCheckboxListener );
    }


    // ── Creating the DN Sub-Composite ─────────────────────────────────────────
    // A group box is created as the sub-composite container, then the DN
    // clause composite is built inside it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a group-box container below the DN checkbox and instantiates a
     * {@link WhatClauseDnComposite} inside it to edit the DN targeting clause.
     */
    private void createDnComposite()
    {
        Group dnGroup = BaseWidgetUtils.createGroup( dnComposite, "", 1 );
        dnGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        dnSubComposite = dnGroup;

        dnClauseComposite = new WhatClauseDnComposite( context, visualEditorComposite );
        dnClauseComposite.createComposite( dnGroup );

        /*
        AclWhatClause whatClause = context.getAclItem().getWhatClause();

        if ( whatClause.getDnClause() != null )
        {
            dnClauseComposite.setClause( whatClause.getDnClause() );
        }

        if ( context != null )
        {
            dnClauseComposite.setConnection( context.getConnection() );
        }
        */
    }


    // ── Creating the Filter Sub-Composite ─────────────────────────────────────
    // A group box is created as the sub-composite container, then the filter
    // clause composite is built inside it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a group-box container below the Filter checkbox and instantiates a
     * {@link WhatClauseFilterComposite} inside it to edit the filter targeting clause.
     */
    private void createFilterComposite()
    {
        Group filterGroup = BaseWidgetUtils.createGroup( filterComposite, "", 1 );
        filterGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        filterSubComposite = filterGroup;

        filterClauseComposite = new WhatClauseFilterComposite( context, visualEditorComposite );
        filterClauseComposite.createComposite( filterGroup );

        /*
        AclWhatClause whatClause = context.getAclItem().getWhatClause();

        if ( whatClause.getFilterClause() != null )
        {
            filterClauseComposite.setClause( whatClause.getFilterClause() );
        }

        if ( context != null )
        {
            filterClauseComposite.setConnection( context.getConnection() );
        }
        */
    }


    // ── Creating the Attributes Sub-Composite ─────────────────────────────────
    // A container composite is created below the Attributes checkbox, then the
    // attributes clause composite is built inside it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a container below the Attributes checkbox and instantiates a
     * {@link WhatClauseAttributesComposite} inside it to edit the attribute-list
     * targeting clause.
     */
    private void createAttributesComposite()
    {
        attributesSubComposite = BaseWidgetUtils.createGroup( attributesComposite, "", 1 );
        attributesSubComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        attributesClauseComposite = new WhatClauseAttributesComposite( visualEditorComposite, attributesSubComposite, context );

        /*
        AclWhatClause whatClause = context.getAclItem().getWhatClause();

        if ( whatClause.getAttributesClause() != null )
        {
            attributesClauseComposite.setClause( whatClause.getAttributesClause() );
        }

        if ( context != null )
        {
            attributesClauseComposite.setConnection( context.getConnection() );
        }
        */
    }


    // ── Disposing a Sub-Composite Safely ──────────────────────────────────────
    // Tarkin removes a targeting panel from the console safely — only if the
    // composite exists and has not already been disposed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the given composite if it is non-null and not already disposed.
     *
     * @param composite  The composite to dispose; no-op if {@code null} or already disposed.
     */
    private void disposeComposite( Composite composite )
    {
        if ( ( composite != null ) && ( !composite.isDisposed() ) )
        {
            composite.dispose();
        }
    }


    // ── Refreshing the What Panel From the Context Model ─────────────────────
    // Called when the Visual tab is shown. Tarkin reads the current what-clause
    // type from the model and checks the corresponding checkbox, creating the
    // appropriate sub-composite.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the WHAT panel from the current ACL context. Reads the model's
     * what-clause type and pre-checks the corresponding checkbox (DN, Filter, or
     * Attributes), creating the sub-composite immediately.
     */
    public void refresh()
    {
        AclWhatClause whatClause = context.getAclItem().getWhatClause();

        if ( whatClause != null )
        {
            // DN clause
            if ( whatClause instanceof AclWhatClauseDn )
            {
                dnCheckbox.setSelection( true );
                createDnComposite();
            }
            else if ( whatClause instanceof AclWhatClauseFilter )
            {
                // Filter clause
                filterCheckbox.setSelection( true );
                createFilterComposite();
            }
            else if ( whatClause instanceof AclWhatClauseAttributes )
            {
                // Attributes clause
                attributesCheckbox.setSelection( true );
                createAttributesComposite();
            }
        }
    }


    // ── Disposing All Created SWT Widgets ─────────────────────────────────────
    // Tarkin powers down the targeting console and releases all SWT resources.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the base composite (and all its children). Should be called when
     * the visual editor composite is itself disposed.
     */
    public void dispose()
    {
        // Composite
        if ( ( composite != null ) && ( !composite.isDisposed() ) )
        {
            composite.dispose();
        }
    }


    // ── Saving Widget Settings Before Close ──────────────────────────────────
    // The visual editor delegates saveWidgetSettings here so the attributes
    // expandable section can persist its expand/collapse state if it is open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves any widget-level settings (e.g. expand/collapse state). Delegates to
     * {@link WhatClauseAttributesComposite#saveWidgetSettings()} if the attributes
     * sub-composite is currently visible.
     */
    public void saveWidgetSettings()
    {
        if ( attributesClauseComposite != null )
        {
            attributesClauseComposite.saveWidgetSettings();
        }
    }
}
