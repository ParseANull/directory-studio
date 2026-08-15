/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;


// ── CLASS: NewAttributeTypeMatchingRulesWizardPage — Mace Windu Decides How To Judge ──
// In the Chancellor's office, Mace Windu stands before Palpatine and decides how Jedi
// justice will be measured: are two candidates equal in skill? Who ranks higher? Does
// one's name begin with "Sky"? He's setting the rules for comparison.
// This page does exactly that for LDAP attribute types: the user picks how the directory
// server compares values — for equality searches, ordering (greater-than/less-than), and
// substring matches. All three are optional; the server falls back to defaults if unset.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * The third and final wizard page in the New Attribute Type wizard, covering matching rules.
 * We let the user choose equality, ordering, and substring matching rules for the attribute.
 * Matching rules tell the LDAP server how to compare values — for example, whether "Vader"
 * equals "vader" (case-insensitive equality) or whether "Sky" matches "Skywalker" (substring).
 * Think of Mace Windu setting the standards by which Jedi are judged — precise, deliberate,
 * and applicable to every candidate in the order.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewAttributeTypeMatchingRulesWizardPage extends WizardPage
{
    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The LabelProvider */
    private LabelProvider labelProvider = new LabelProvider()
    {
        /**
         * {@inheritDoc}
         */
        public String getText( Object element )
        {
            if ( element instanceof MatchingRule )
            {
                MatchingRule mr = ( MatchingRule ) element;

                String name = mr.getName();
                if ( name != null )
                {
                    return NLS
                        .bind(
                            Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.NameOID" ), new String[] { name, mr.getOid() } ); //$NON-NLS-1$
                }
                else
                {
                    return NLS
                        .bind(
                            Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.NoneOID" ), new String[] { mr.getOid() } ); //$NON-NLS-1$
                }
            }

            return super.getText( element );
        }
    };

    // UI fields
    private ComboViewer equalityComboViewer;
    private ComboViewer orderingComboViewer;
    private ComboViewer substringComboViewer;


    // ── Mace Windu Enters The Chancellor's Office Ready To Judge ─────────────────────
    // Mace Windu strides in, lightsaber at his hip, ready to render judgment — title,
    // description, the icon of authority all on display.
    // We set up the page's title, description, and image here so the user knows what
    // kind of decision this step is asking them to make.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs this wizard page and sets its title, description, and image.
     * We also grab the SchemaHandler here so we can load the matching rules from the
     * currently open schema project when the page is displayed.
     */
    public NewAttributeTypeMatchingRulesWizardPage()
    {
        super( "NewAttributeTypeMatchingRulesWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.MatchingRules" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.PleaseSpecifiyMatchingRules" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_ATTRIBUTE_TYPE_NEW_WIZARD ) );

        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── Mace Windu Draws Three Circles On The Floor ───────────────────────────────────
    // "Equality here. Ordering there. Substring search in the corner." Mace maps out
    // the judgment chambers — three distinct tests, each with its own standard.
    // We build three combo viewers, each populated with the same list of matching rules,
    // so the user can independently set equality, ordering, and substring rules.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widgets for this page — a single group containing three combos:
     * equality, ordering, and substring matching rule selectors.
     * Eclipse calls this once when the page is first shown; we then call {@link #initFields()}
     * to fill the combos with actual matching rule options.
     *
     * @param parent  the parent composite provided by Eclipse — we nest our layout inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Matching Rules Group
        Group matchingRulesGroup = new Group( composite, SWT.NONE );
        matchingRulesGroup.setText( Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.MatchingRules" ) ); //$NON-NLS-1$
        matchingRulesGroup.setLayout( new GridLayout( 2, false ) );
        matchingRulesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // Equality
        Label equalityLabel = new Label( matchingRulesGroup, SWT.NONE );
        equalityLabel.setText( Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.Equality" ) ); //$NON-NLS-1$
        Combo equalityCombo = new Combo( matchingRulesGroup, SWT.READ_ONLY );
        equalityCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        equalityComboViewer = new ComboViewer( equalityCombo );
        equalityComboViewer.setContentProvider( new ArrayContentProvider() );
        equalityComboViewer.setLabelProvider( labelProvider );

        // Ordering
        Label orderingLabel = new Label( matchingRulesGroup, SWT.NONE );
        orderingLabel.setText( Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.Ordering" ) ); //$NON-NLS-1$
        Combo orderingCombo = new Combo( matchingRulesGroup, SWT.READ_ONLY );
        orderingCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        orderingComboViewer = new ComboViewer( orderingCombo );
        orderingComboViewer.setContentProvider( new ArrayContentProvider() );
        orderingComboViewer.setLabelProvider( labelProvider );

        // Substring
        Label substringLabel = new Label( matchingRulesGroup, SWT.NONE );
        substringLabel.setText( Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.Substring" ) ); //$NON-NLS-1$
        Combo substringCombo = new Combo( matchingRulesGroup, SWT.READ_ONLY );
        substringCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        substringComboViewer = new ComboViewer( substringCombo );
        substringComboViewer.setContentProvider( new ArrayContentProvider() );
        substringComboViewer.setLabelProvider( labelProvider );

        initFields();

        setControl( composite );
    }


    // ── Mace Windu Reads The Registry Of All Known Standards ─────────────────────────
    // Before passing judgment, Mace reads the full registry of Jedi Council standards:
    // every matching rule on record, sorted alphabetically for clarity.
    // We load all matching rules from the schema handler, sort them, append a "(None)"
    // option, and feed the same list into all three combo viewers.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates all three matching rule combos (equality, ordering, substring) from the
     * schema handler, sorted alphabetically by first name.
     * We add a "(None)" sentinel at the front and pre-select it in all three combos,
     * because matching rules are completely optional — the LDAP server is fine without them.
     */
    private void initFields()
    {
        if ( schemaHandler != null )
        {
            // Getting the matching rules
            List<Object> matchingRules = new ArrayList<Object>( schemaHandler.getMatchingRules() );
            // Adding the (None) matching rule
            String none = Messages.getString( "NewAttributeTypeMatchingRulesWizardPage.None" ); //$NON-NLS-1$
            matchingRules.add( none );

            // Sorting the matching rules
            Collections.sort( matchingRules, new Comparator<Object>()
            {

                public int compare( Object o1, Object o2 )
                {
                    if ( ( o1 instanceof MatchingRule ) && ( o2 instanceof MatchingRule ) )
                    {
                        List<String> o1Names = ( ( MatchingRule ) o1 ).getNames();
                        List<String> o2Names = ( ( MatchingRule ) o2 ).getNames();

                        // Comparing the First Name
                        if ( ( o1Names != null ) && ( o2Names != null ) )
                        {
                            if ( ( o1Names.size() > 0 ) && ( o2Names.size() > 0 ) )
                            {
                                return o1Names.get( 0 ).compareToIgnoreCase( o2Names.get( 0 ) );
                            }
                            else if ( ( o1Names.size() == 0 ) && ( o2Names.size() > 0 ) )
                            {
                                return "".compareToIgnoreCase( o2Names.get( 0 ) ); //$NON-NLS-1$
                            }
                            else if ( ( o1Names.size() > 0 ) && ( o2Names.size() == 0 ) )
                            {
                                return o1Names.get( 0 ).compareToIgnoreCase( "" ); //$NON-NLS-1$
                            }
                        }
                        else if ( ( o1 instanceof String ) && ( o2 instanceof MatchingRule ) )
                        {
                            return Integer.MIN_VALUE;
                        }
                        else if ( ( o1 instanceof MatchingRule ) && ( o2 instanceof String ) )
                        {
                            return Integer.MAX_VALUE;
                        }
                    }

                    // Default
                    return o1.toString().compareToIgnoreCase( o2.toString() );
                }
            } );

            // Setting the input
            equalityComboViewer.setInput( matchingRules );
            orderingComboViewer.setInput( matchingRules );
            substringComboViewer.setInput( matchingRules );

            // Selecting the None matching rules
            equalityComboViewer.setSelection( new StructuredSelection( none ) );
            orderingComboViewer.setSelection( new StructuredSelection( none ) );
            substringComboViewer.setSelection( new StructuredSelection( none ) );
        }
    }


    // ── Mace Reads The Equality Standard From The Ledger ─────────────────────────────
    // "Are these two candidates equal in the eyes of the Council?" Mace reads the equality
    // standard he's chosen from the register and reports it back.
    // We return the name (or OID if unnamed) of whichever equality matching rule was selected.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name (or OID) of the equality matching rule the user selected.
     * The equality rule tells the LDAP server when two attribute values are considered
     * identical — essential for filter searches like {@code (cn=Skywalker)}.
     * Returns null if the user left it as "(None)".
     *
     * @return  the equality matching rule name or OID string, or null if unset.
     */
    public String getEqualityMatchingRuleValue()
    {
        Object selection = ( ( StructuredSelection ) equalityComboViewer.getSelection() ).getFirstElement();

        if ( selection instanceof MatchingRule )
        {
            MatchingRule mr = ( ( MatchingRule ) selection );

            List<String> names = mr.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return mr.getName();
            }
            else
            {
                return mr.getOid();
            }
        }

        return null;
    }


    // ── Mace Reads The Ordering Standard From The Ledger ──────────────────────────────
    // "Who outranks whom? Which Jedi stands higher in the Council's estimation?"
    // Mace reads back the ordering standard — the rule that determines greater-than/less-than.
    // The ordering rule lets LDAP servers sort results and execute range queries like
    // {@code (rank>=Master)}.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name (or OID) of the ordering matching rule the user selected.
     * The ordering rule defines how values are sorted relative to each other — useful for
     * range filters and sorted search results.
     * Returns null if the user left it as "(None)".
     *
     * @return  the ordering matching rule name or OID string, or null if unset.
     */
    public String getOrderingMatchingRuleValue()
    {
        Object selection = ( ( StructuredSelection ) orderingComboViewer.getSelection() ).getFirstElement();

        if ( selection instanceof MatchingRule )
        {
            MatchingRule mr = ( ( MatchingRule ) selection );

            List<String> names = mr.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return mr.getName();
            }
            else
            {
                return mr.getOid();
            }
        }

        return null;
    }


    // ── Mace Reads The Partial-Match Standard From The Ledger ────────────────────────
    // "Does 'Sky' appear somewhere in this candidate's name?" Mace reads the substring
    // standard that determines whether a partial match qualifies.
    // The substring rule powers LDAP wildcard filters like {@code (cn=Sky*)}.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name (or OID) of the substring matching rule the user selected.
     * The substring rule tells the LDAP server how to evaluate wildcard searches —
     * for example, {@code (cn=*walker)} matching "Skywalker".
     * Returns null if the user left it as "(None)".
     *
     * @return  the substring matching rule name or OID string, or null if unset.
     */
    public String getSubstringMatchingRuleValue()
    {
        Object selection = ( ( StructuredSelection ) substringComboViewer.getSelection() ).getFirstElement();

        if ( selection instanceof MatchingRule )
        {
            MatchingRule mr = ( ( MatchingRule ) selection );

            List<String> names = mr.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return mr.getName();
            }
            else
            {
                return mr.getOid();
            }
        }

        return null;
    }
}
