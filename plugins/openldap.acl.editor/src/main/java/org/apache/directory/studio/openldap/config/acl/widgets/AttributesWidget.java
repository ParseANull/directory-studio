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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.TableWidget;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPlugin;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPluginConstants;
import org.apache.directory.studio.openldap.config.acl.model.AclAttribute;
import org.apache.directory.studio.openldap.config.acl.model.AclAttributeStyleEnum;
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseAttributes;
import org.apache.directory.studio.openldap.config.acl.wrapper.AclAttributeDecorator;
import org.apache.directory.studio.openldap.config.acl.wrapper.AclAttributeWrapper;


// ── CLASS: AttributesWidget — GRAND MOFF LISTING ATTRIBUTES TO CONTROL ───────
// Grand Moff Tarkin assembles the attribute manifest: a scrollable table of
// attribute names (possibly with "!" exclusion prefixes or "@objectClass" refs),
// a Val checkbox that unlocks a matching-rule filter and value text field, and
// a Style combo (base/exact/one/subtree/children/regex). This widget renders
// all of that. The table uses TableWidget<AclAttributeWrapper> backed by
// AclAttributeDecorator for display and inline editing. The Val checkbox enables
// the matching-rule checkbox, style combo, and value text. The listeners keep
// the AclWhatClauseAttributes model up to date as the user edits. getAttributes()
// extracts the final list of AclAttribute objects from the table.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for editing an {@link AclWhatClauseAttributes}: a table of
 * attribute names/exclusions plus optional Val filter controls (matching rule,
 * style, value text).
 *
 * <p>Think of this class as Grand Moff Tarkin's attribute manifest — listing
 * which Imperial attribute types the ACL rule will govern, with optional
 * value-matching constraints.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributesWidget extends AbstractWidget
{
    /** The Attributes table */
    private TableWidget<AclAttributeWrapper> attributeTable;

    /** The WhatAttributes clause */
    private AclWhatClauseAttributes aclWhatClauseAttributes;

    /** The checkbox for the Val */
    private Button valButton;

    /** The checkbox for the matchingrule */
    private Button matchingRuleButton;

    /** The style combo */
    private Combo styleCombo;

    /** The Value Text */
    private Text valueText;

    /** The initial attributes. */
    private String[] initialAttributes;

    /** The proposal provider */
    private AttributesWidgetContentProposalProvider proposalProvider;

    /** The proposal adapter*/
    private ContentProposalAdapter proposalAdapter;

    // ── Label Provider for the Proposal Popup ────────────────────────────────
    // Decorates each proposal with its appropriate icon (attribute type, objectClass,
    // or keyword) in the content-assist dropdown.
    // ─────────────────────────────────────────────────────────────────────────
    /** Label provider for the proposal adapter popup. */
    private LabelProvider labelProvider = new LabelProvider()
    {
        public String getText( Object element )
        {
            if ( element instanceof IContentProposal )
            {
                IContentProposal proposal = ( IContentProposal ) element;
                return proposal.getLabel() == null ? proposal.getContent() : proposal.getLabel();
            }

            return super.getText( element );
        };


        public Image getImage( Object element )
        {
            if ( element instanceof AttributeTypeContentProposal )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_ATD );
            }
            else if ( element instanceof ObjectClassContentProposal )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_OCD );
            }
            else if ( element instanceof KeywordContentProposal )
            {
                return OpenLdapAclEditorPlugin.getDefault().getImage( OpenLdapAclEditorPluginConstants.IMG_KEYWORD );
            }

            return super.getImage( element );
        }
    };

    // ── Verify Listener: Reject Whitespace in Attribute Names ─────────────────
    // ACL attribute names cannot contain whitespace; this listener vetoes any
    // keystrokes that would insert a space or tab.
    // ─────────────────────────────────────────────────────────────────────────
    /** Verify listener — rejects whitespace characters in the text field. */
    private VerifyListener verifyListener = new VerifyListener()
    {
        public void verifyText( VerifyEvent e )
        {
            // Not allowing white spaces
            if ( Character.isWhitespace( e.character ) )
            {
                e.doit = false;
            }
        }
    };


    // ── Modify Listener: Notify on Any Text Change ────────────────────────────
    // Fires a modify notification whenever the value text changes so higher-level
    // listeners can react.
    // ─────────────────────────────────────────────────────────────────────────
    /** Modify listener — fires notifyListeners() on text change. */
    private ModifyListener modifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            notifyListeners();
        }
    };


    // ── Listener: Val Checkbox ────────────────────────────────────────────────
    // Enabling Val unlocks the matching-rule checkbox, style combo, and value
    // text; disabling locks them again.
    // ─────────────────────────────────────────────────────────────────────────
    /** Val checkbox listener — enables/disables matching-rule, style, and value controls. */
    private SelectionAdapter valButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent event )
        {
            // If the Val Button is selected, then the MatchingRule Button,
            // the Style Combo and the value Text must be enabled
            boolean valSelected = valButton.getSelection();

            matchingRuleButton.setEnabled( valSelected );
            styleCombo.setEnabled( valSelected );
            valueText.setEnabled( valSelected );
            aclWhatClauseAttributes.setVal( valSelected );

            // TODO : disable the OK button if Val is set and there is no value
        }
    };


    // ── Listener: MatchingRule Checkbox ──────────────────────────────────────
    /** MatchingRule checkbox listener — updates the clause's matchingRule flag. */
    private SelectionAdapter matchingRuleButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent event )
        {
            aclWhatClauseAttributes.setMatchingRule( matchingRuleButton.getSelection() );
        }
    };


    // ── Listener: Style Combo ─────────────────────────────────────────────────
    /** Style combo listener — updates the clause's style from the selected name. */
    private SelectionAdapter styleComboListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent event )
        {
            aclWhatClauseAttributes.setStyle( AclAttributeStyleEnum.getStyle( styleCombo.getText() ) );
        }
    };


    // ── Creating the Attribute Widget UI ─────────────────────────────────────
    // Tarkin creates the attribute table (with Add/Edit/Delete buttons), the Val
    // checkbox, matching-rule checkbox, style combo, and value text field. All
    // are laid out in a four-column composite and initialised from the clause.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the full attribute widget UI inside the given parent.
     *
     * <p>For example — creating the widget in the attributes sub-composite:</p>
     * <pre>
     *   AttributesWidget widget = new AttributesWidget();
     *   widget.createWidget(composite, connection, aclWhatClauseAttributes);
     * </pre>
     *
     * @param parent      The parent composite.
     * @param connection  The LDAP browser connection (for attribute proposals).
     * @param clause      The attributes clause to edit.
     */
    public void createWidget( Composite parent, IBrowserConnection connection, AclWhatClauseAttributes clause )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 4, 1 );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        gd.widthHint = 30;
        composite.setLayoutData( gd );

        // The Attribute table
        BaseWidgetUtils.createLabel( composite, "Attributes list :", 4 );
        AclAttributeDecorator decorator = new AclAttributeDecorator( composite.getShell(), connection );
        attributeTable = new TableWidget<AclAttributeWrapper>( decorator );
        attributeTable.createWidgetWithEdit( composite, null );
        attributeTable.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 4, 3 ) );
        //attributeTable.addWidgetModifyListener( attributeTableListener );

        // The Val
        valButton = BaseWidgetUtils.createCheckbox( composite, "Val", 1 );
        valButton.addSelectionListener( valButtonListener );

        // The MatchingRule
        matchingRuleButton = BaseWidgetUtils.createCheckbox( composite, "MatchingRule", 1 );
        matchingRuleButton.setEnabled( false );
        matchingRuleButton.addSelectionListener( matchingRuleButtonListener );

        // The style
        BaseWidgetUtils.createLabel( composite, "Style :", 1 );
        styleCombo = BaseWidgetUtils.createCombo( composite, AclAttributeStyleEnum.getNames(), 9, 1 );
        styleCombo.setEnabled( false );
        styleCombo.addSelectionListener( styleComboListener );

        // The value
        BaseWidgetUtils.createLabel( composite, "Value :", 1 );
        valueText = BaseWidgetUtils.createText( composite, "", 3 );
        valueText.setEnabled( false );
        //valueText.addModifyListener( valueTextListener );

        initWidget( clause );
    }


    // ── Initialising the Widget From the Clause ───────────────────────────────
    // Tarkin pre-fills the table with the clause's existing attributes and
    // configures the Val / matching-rule / style / value controls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the attribute table and Val controls from the given clause.
     *
     * @param clause  The attributes clause to initialise from.
     */
    private void initWidget( AclWhatClauseAttributes clause )
    {
        aclWhatClauseAttributes = clause;

        // Update the table
        setAttributes( clause.getAttributes() );

        // The Val button is always enabled
        valButton.setEnabled( true );

        if ( clause.hasVal() )
        {
            matchingRuleButton.setEnabled( clause.hasMatchingRule() );
            styleCombo.setEnabled( false );
            styleCombo.setText( clause.getStyle().getName() );
            valueText.setEnabled( false );
            valueText.setText( CommonUIUtils.getTextValue( clause.getValue() ) );
        }
        else
        {
            matchingRuleButton.setEnabled( false );
            styleCombo.setEnabled( false );
            valueText.setEnabled( false );
        }
    }


    // ── Populating the Attribute Table ────────────────────────────────────────
    // Converts each AclAttribute to an AclAttributeWrapper and loads them into
    // the TableWidget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wraps each {@link AclAttribute} in an {@link AclAttributeWrapper} and loads
     * the wrappers into the attribute table.
     *
     * @param aclAttributes  The list of attributes from the clause.
     */
    private void setAttributes( List<AclAttribute> aclAttributes )
    {
        List<AclAttributeWrapper> aclAttributeWrappers = new ArrayList<AclAttributeWrapper>( aclAttributes.size() );

        for ( AclAttribute aclAttribute: aclAttributes )
        {
            AclAttributeWrapper aclAttributeWrapper = new AclAttributeWrapper( aclAttribute );
            aclAttributeWrappers.add( aclAttributeWrapper );
        }

        attributeTable.setElements( aclAttributeWrappers );
    }


    // ── Enabling/Disabling the Widget ─────────────────────────────────────────
    /**
     * Sets the enabled state of the widget (currently a no-op; sub-controls have
     * their own enable logic).
     *
     * @param b  {@code true} to enable, {@code false} to disable.
     */
    public void setEnabled( boolean b )
    {
    }


    // ── Extracting the Final Attribute List ───────────────────────────────────
    // Tarkin reads the table back out, unwrapping each AclAttributeWrapper to
    // return the underlying list of AclAttribute objects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of {@link AclAttribute} objects currently shown in the table.
     * Unwraps each {@link AclAttributeWrapper} to its underlying {@link AclAttribute}.
     *
     * @return  The current attribute list; never {@code null}.
     */
    public List<AclAttribute> getAttributes()
    {
        List<AclAttributeWrapper> elementList = attributeTable.getElements();

        List<AclAttribute> result = new ArrayList<AclAttribute>( elementList.size() );

        for ( AclAttributeWrapper element : elementList )
        {
            result.add( element.getAclAttribute() );
        }

        return result;
    }
}
