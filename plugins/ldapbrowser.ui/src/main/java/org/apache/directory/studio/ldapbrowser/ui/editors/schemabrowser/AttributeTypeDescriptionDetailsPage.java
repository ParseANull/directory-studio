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

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import java.util.Collection;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: AttributeTypeDescriptionDetailsPage — The Most Detailed Blueprint ─────
// This is the longest chapter in the Death Star manual.  Every attribute type
// carries an OID, human names, a description, a usage category, four boolean
// flags (single-valued, read-only, collective, obsolete), a wire syntax with
// optional max length, three matching-rule hyperlinks, extra matching rules,
// the object classes that require or permit it, a supertype, and derived subtypes.
// R2-D2 projects all of it in one tall scrollable panel whenever an attribute
// type entry is selected in the left-hand list.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The detail page that displays the full specification of a selected attribute
 * type description on the right-hand side of the schema browser.
 * It is the most comprehensive detail page in the schema browser, covering:
 * OID, names, description, and usage in a fixed "Details" section;
 * four colored flag labels (single-valued, read-only, collective, obsolete);
 * a fixed "Syntax" section with OID hyperlink, description, and max length;
 * a fixed "Matching Rules" section with equality, substring, and ordering links;
 * and six collapsible sections for other matching rules, used-as-must object classes,
 * used-as-may object classes, supertype, subtypes, and the raw LDIF footer.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeDescriptionDetailsPage extends SchemaDetailsPage
{
    /** The main section, contains oid, names, desc and usage */
    private Section mainSection;

    /** The numeric oid field */
    private Text numericOidText;

    /** The names field */
    private Text namesText;

    /** The description field */
    private Text descText;

    /** The usage field */
    private Text usageText;

    /** The flag section, contains sv, obsolete, collective and read-only */
    private Section flagSection;

    /** The single-valued label */
    private Label singleValuedLabel;

    /** The obsolete label */
    private Label isObsoleteLabel;

    /** The collective label */
    private Label collectiveLabel;

    /** The no-user-modification label */
    private Label noUserModificationLabel;

    /** The syntax section, contains syntax description, lenth and a link to the syntax */
    private Section syntaxSection;

    /** The syntax description field */
    private Text syntaxDescText;

    /** The syntax length field */
    private Text lengthText;

    /** The link to the syntax */
    private Hyperlink syntaxLink;

    /** The matching rules section, contains links to matching rules */
    private Section matchingRulesSection;

    /** The link to the equality matching rule */
    private Hyperlink equalityLink;

    /** The link to the substring matching rule */
    private Hyperlink substringLink;

    /** The link to the ordering matching rule */
    private Hyperlink orderingLink;

    /** The section with other matching rules */
    private Section otherMatchSection;

    /** The section with links to object classes using the selected attribute as must */
    private Section usedAsMustSection;

    /** The section with links to object classes using the selected attribute as may */
    private Section usedAsMaySection;

    /** The section with a link to the superior attribute type */
    private Section supertypeSection;

    /** The section with links to the derived attribute types */
    private Section subtypesSection;


    // ── R2 Loads The Attribute-Type Blueprint Module ───────────────────────────────
    // R2 slots the attribute-type detail module into his projection system, linking
    // it to the master page and toolkit so it can build controls when asked.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the attribute type details page linked to the given master schema page.
     *
     * @param schemaPage  the master schema page that owns this detail page
     * @param toolkit     the JFace forms toolkit used to create controls
     */
    public AttributeTypeDescriptionDetailsPage( SchemaPage schemaPage, FormToolkit toolkit )
    {
        super( schemaPage, toolkit );
    }


    // ── R2 Assembles The Full Attribute-Type Blueprint Panel ───────────────────────
    // R2 builds the complete display: a fixed Details section, four flag labels in
    // a Flags row, a fixed Syntax section, a fixed Matching Rules section, and then
    // six collapsible appendices — other matching rules, used-as-must, used-as-may,
    // supertype, subtypes — capped by the standard raw LDIF footer.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT layout for this detail page inside the given form.
     * Creates all fixed and collapsible sections.
     * The flag labels are created here once; their foreground color is updated on
     * every {@link #setInput(Object)} call to indicate enabled/disabled state.
     *
     * @param detailForm  the scrolled form that parents all sections
     */
    protected void createContents( final ScrolledForm detailForm )
    {

        this.detailForm = detailForm;
        detailForm.getBody().setLayout( new GridLayout() );

        // create main section
        mainSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        mainSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.Details" ) ); //$NON-NLS-1$
        mainSection.marginWidth = 0;
        mainSection.marginHeight = 0;
        mainSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( mainSection );

        // create flag section
        flagSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        flagSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.Flags" ) ); //$NON-NLS-1$
        flagSection.marginWidth = 0;
        flagSection.marginHeight = 0;
        flagSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( flagSection );

        // create flags content
        Composite flagClient = toolkit.createComposite( flagSection, SWT.WRAP );
        GridLayout flagLayout = new GridLayout();
        flagLayout.numColumns = 4;
        flagLayout.marginWidth = 0;
        flagLayout.marginHeight = 0;
        flagClient.setLayout( flagLayout );
        flagSection.setClient( flagClient );

        singleValuedLabel = toolkit.createLabel( flagClient, Messages
            .getString( "AttributeTypeDescriptionDetailsPage.SingleValued" ), SWT.CHECK ); //$NON-NLS-1$
        singleValuedLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        noUserModificationLabel = toolkit.createLabel( flagClient, Messages
            .getString( "AttributeTypeDescriptionDetailsPage.ReadOnly" ), SWT.CHECK ); //$NON-NLS-1$
        noUserModificationLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        collectiveLabel = toolkit.createLabel( flagClient, Messages
            .getString( "AttributeTypeDescriptionDetailsPage.Collective" ), SWT.CHECK ); //$NON-NLS-1$
        collectiveLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        isObsoleteLabel = toolkit.createLabel( flagClient, Messages
            .getString( "AttributeTypeDescriptionDetailsPage.Obsolete" ), SWT.CHECK ); //$NON-NLS-1$
        isObsoleteLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        // create syntax section
        syntaxSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        syntaxSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.Syntax" ) ); //$NON-NLS-1$
        syntaxSection.marginWidth = 0;
        syntaxSection.marginHeight = 0;
        syntaxSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( syntaxSection );

        // create syntax content
        Composite syntaxClient = toolkit.createComposite( syntaxSection, SWT.WRAP );
        GridLayout syntaxLayout = new GridLayout();
        syntaxLayout.numColumns = 2;
        syntaxLayout.marginWidth = 0;
        syntaxLayout.marginHeight = 0;
        syntaxClient.setLayout( syntaxLayout );
        syntaxSection.setClient( syntaxClient );

        toolkit.createLabel( syntaxClient,
            Messages.getString( "AttributeTypeDescriptionDetailsPage.SyntaxOID" ), SWT.NONE ); //$NON-NLS-1$
        syntaxLink = toolkit.createHyperlink( syntaxClient, "", SWT.WRAP ); //$NON-NLS-1$
        syntaxLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        syntaxLink.addHyperlinkListener( this );

        toolkit.createLabel( syntaxClient,
            Messages.getString( "AttributeTypeDescriptionDetailsPage.SyntaxDescription" ), SWT.NONE ); //$NON-NLS-1$
        syntaxDescText = toolkit.createText( syntaxClient, "", SWT.NONE ); //$NON-NLS-1$
        syntaxDescText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        syntaxDescText.setEditable( false );

        toolkit
            .createLabel( syntaxClient, Messages.getString( "AttributeTypeDescriptionDetailsPage.Length" ), SWT.NONE ); //$NON-NLS-1$
        lengthText = toolkit.createText( syntaxClient, "", SWT.NONE ); //$NON-NLS-1$
        lengthText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        lengthText.setEditable( false );

        // create matching rules section
        matchingRulesSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        matchingRulesSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.MatchingRules" ) ); //$NON-NLS-1$
        matchingRulesSection.marginWidth = 0;
        matchingRulesSection.marginHeight = 0;
        matchingRulesSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( matchingRulesSection );

        // create matching rules content
        Composite matchClient = toolkit.createComposite( matchingRulesSection, SWT.WRAP );
        GridLayout matchLayout = new GridLayout();
        matchLayout.numColumns = 2;
        matchLayout.marginWidth = 0;
        matchLayout.marginHeight = 0;
        matchClient.setLayout( matchLayout );
        matchingRulesSection.setClient( matchClient );

        toolkit.createLabel( matchClient,
            Messages.getString( "AttributeTypeDescriptionDetailsPage.EqualityMatch" ), SWT.NONE ); //$NON-NLS-1$
        equalityLink = toolkit.createHyperlink( matchClient, "", SWT.WRAP ); //$NON-NLS-1$
        equalityLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        equalityLink.addHyperlinkListener( this );

        toolkit.createLabel( matchClient,
            Messages.getString( "AttributeTypeDescriptionDetailsPage.SubstringMatch" ), SWT.NONE ); //$NON-NLS-1$
        substringLink = toolkit.createHyperlink( matchClient, "", SWT.WRAP ); //$NON-NLS-1$
        substringLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        substringLink.addHyperlinkListener( this );

        toolkit.createLabel( matchClient,
            Messages.getString( "AttributeTypeDescriptionDetailsPage.OrderingMatch" ), SWT.NONE ); //$NON-NLS-1$
        orderingLink = toolkit.createHyperlink( matchClient, "", SWT.WRAP ); //$NON-NLS-1$
        orderingLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        orderingLink.addHyperlinkListener( this );

        // create other matching rules section
        otherMatchSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        otherMatchSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.OtherMatchingRules" ) ); //$NON-NLS-1$
        otherMatchSection.marginWidth = 0;
        otherMatchSection.marginHeight = 0;
        otherMatchSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( otherMatchSection );
        otherMatchSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create used as must section
        usedAsMustSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        usedAsMustSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.UsedAsMust" ) ); //$NON-NLS-1$
        usedAsMustSection.marginWidth = 0;
        usedAsMustSection.marginHeight = 0;
        usedAsMustSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( usedAsMustSection );
        usedAsMustSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create used as may section
        usedAsMaySection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        usedAsMaySection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.UsedAsMay" ) ); //$NON-NLS-1$
        usedAsMaySection.marginWidth = 0;
        usedAsMaySection.marginHeight = 0;
        usedAsMaySection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( usedAsMaySection );
        usedAsMaySection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create supertype section
        supertypeSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        supertypeSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.Supertype" ) ); //$NON-NLS-1$
        supertypeSection.marginWidth = 0;
        supertypeSection.marginHeight = 0;
        supertypeSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( supertypeSection );
        supertypeSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create subtypes section
        subtypesSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        subtypesSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.Subtypes" ) ); //$NON-NLS-1$
        subtypesSection.marginWidth = 0;
        subtypesSection.marginHeight = 0;
        subtypesSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( subtypesSection );
        subtypesSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create raw section
        createRawSection();
    }


    // ── R2 Projects The Full Attribute-Type Specification ─────────────────────────
    // "Show everything about 'cn'."  R2 fills in all eleven sections: the header
    // fields, the four colored flag indicators, syntax OID and length, the three
    // matching-rule hyperlinks, and rebuilds the six collapsible appendices.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates all sections to display the given attribute type description.
     * Rebuilds dynamic sections and reflowing the form on every call.
     * Fixed-content sections (flags, syntax, matching rules) are updated in-place;
     * dynamic sections are fully disposed and recreated.
     *
     * <p>For example — R2 projects the full "cn" attribute spec:</p>
     * <pre>
     *   setInput(cnAttributeType);
     *   // Details: OID, "cn" / "commonName", userApplications
     *   // Flags: single-valued=disabled, read-only=disabled, ...
     *   // Syntax: DirectoryString (hyperlink), length = 32768
     *   // Matching Rules: caseIgnoreMatch, caseIgnoreSubstringsMatch, -
     * </pre>
     *
     * @param input  the {@link AttributeType} to display; null clears the pane
     */
    public void setInput( Object input )
    {
        AttributeType atd = null;

        if ( input instanceof AttributeType )
        {
            atd = ( AttributeType ) input;
        }

        // create main content
        createMainContent( atd );

        // set flags
        if ( ( atd != null ) && ( atd.isSingleValued() ) )
        {
            singleValuedLabel.setForeground( getColor( CommonUIConstants.DEFAULT_COLOR ) );
        }
        else
        {
            singleValuedLabel.setForeground( getColor( CommonUIConstants.DISABLED_COLOR ) );
        }

        if ( atd != null && atd.isObsolete() )
        {
            isObsoleteLabel.setForeground( getColor( CommonUIConstants.DEFAULT_COLOR ) );
        }
        else
        {
            isObsoleteLabel.setForeground( getColor( CommonUIConstants.DISABLED_COLOR ) );
        }

        if ( atd != null && atd.isCollective() )
        {
            collectiveLabel.setForeground( getColor( CommonUIConstants.DEFAULT_COLOR ) );
        }
        else
        {
            collectiveLabel.setForeground( getColor( CommonUIConstants.DISABLED_COLOR ) );
        }

        if ( atd != null && !atd.isUserModifiable() )
        {
            noUserModificationLabel.setForeground( getColor( CommonUIConstants.DEFAULT_COLOR ) );
        }
        else
        {
            noUserModificationLabel.setForeground( getColor( CommonUIConstants.DISABLED_COLOR ) );
        }

        flagSection.layout();

        // set syntax content
        String lsdOid = null;
        LdapSyntax lsd = null;
        long lsdLength = 0;

        if ( atd != null )
        {
            lsdOid = SchemaUtils.getSyntaxNumericOidTransitive( atd, getSchema() );

            if ( lsdOid != null && getSchema().hasLdapSyntaxDescription( lsdOid ) )
            {
                lsd = getSchema().getLdapSyntaxDescription( lsdOid );
            }

            lsdLength = SchemaUtils.getSyntaxLengthTransitive( atd, getSchema() );
        }

        syntaxLink.setText( getNonNullString( lsd != null ? lsd.getOid() : lsdOid ) );
        syntaxLink.setHref( lsd );
        syntaxLink.setUnderlined( lsd != null );
        syntaxLink.setEnabled( lsd != null );
        syntaxDescText.setText( getNonNullString( lsd != null ? lsd.getDescription() : null ) );
        lengthText.setText( getNonNullString( lsdLength > 0 ? Long.toString( lsdLength ) : null ) );
        syntaxSection.layout();

        // set matching rules content
        String emrOid = null;
        MatchingRule emr = null;

        if ( atd != null )
        {
            emrOid = SchemaUtils.getEqualityMatchingRuleNameOrNumericOidTransitive( atd, getSchema() );

            if ( emrOid != null && getSchema().hasMatchingRuleDescription( emrOid ) )
            {
                emr = getSchema().getMatchingRuleDescription( emrOid );
            }
        }

        equalityLink.setText( getNonNullString( emr != null ? SchemaUtils.toString( emr ) : emrOid ) );
        equalityLink.setHref( emr );
        equalityLink.setUnderlined( emr != null );
        equalityLink.setEnabled( emr != null );

        String smrOid = null;
        MatchingRule smr = null;

        if ( atd != null )
        {
            smrOid = SchemaUtils.getSubstringMatchingRuleNameOrNumericOidTransitive( atd, getSchema() );

            if ( smrOid != null && getSchema().hasMatchingRuleDescription( smrOid ) )
            {
                smr = getSchema().getMatchingRuleDescription( smrOid );
            }
        }

        substringLink.setText( getNonNullString( smr != null ? SchemaUtils.toString( smr ) : smrOid ) );
        substringLink.setHref( smr );
        substringLink.setUnderlined( smr != null );
        substringLink.setEnabled( smr != null );

        String omrOid = null;
        MatchingRule omr = null;

        if ( atd != null )
        {
            omrOid = SchemaUtils.getOrderingMatchingRuleNameOrNumericOidTransitive( atd, getSchema() );

            if ( omrOid != null && getSchema().hasMatchingRuleDescription( omrOid ) )
            {
                omr = getSchema().getMatchingRuleDescription( omrOid );
            }
        }

        orderingLink.setText( getNonNullString( omr != null ? SchemaUtils.toString( omr ) : omrOid ) );
        orderingLink.setHref( omr );
        orderingLink.setUnderlined( omr != null );
        orderingLink.setEnabled( omr != null );
        matchingRulesSection.layout();

        // create contents of dynamic sections
        createOtherMatchContent( atd );
        createUsedAsMustContent( atd );
        createUsedAsMayContent( atd );
        createSupertypeContent( atd );
        createSubtypesContent( atd );
        createRawContents( atd );

        detailForm.reflow( true );
    }


    // ── R2 Looks Up A Named Color ──────────────────────────────────────────────────
    // R2 translates a symbolic color name (DEFAULT_COLOR or DISABLED_COLOR) into
    // an SWT Color object by asking the shared plugin registry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT Color registered under the given symbolic name.
     * Used to paint flag labels enabled (dark) or disabled (grey).
     *
     * @param color  a color constant from {@link CommonUIConstants}
     * @return       the registered Color object
     */
    private Color getColor( String color )
    {
        return CommonUIPlugin.getDefault().getColor( color );
    }


    // ── R2 Fills The Header Identification Fields ──────────────────────────────────
    // R2 populates the top section of the attribute blueprint: OID, human display
    // names, a potentially multi-line description, and the usage category
    // (userApplications, directoryOperation, etc.).  Rebuilt fresh every time
    // so multi-line descriptions get proper layout space.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Details" section content with OID, names, description, and
     * usage fields for the given attribute type.
     * Disposing and recreating on every call ensures multi-line descriptions
     * properly resize the section.
     *
     * @param atd  the attribute type to display; null leaves the section empty
     */
    private void createMainContent( AttributeType atd )
    {
        // dispose old content
        if ( mainSection.getClient() != null )
        {
            mainSection.getClient().dispose();
        }

        // create new client
        Composite mainClient = toolkit.createComposite( mainSection, SWT.WRAP );
        GridLayout mainLayout = new GridLayout( 2, false );
        mainClient.setLayout( mainLayout );
        mainSection.setClient( mainClient );

        // create new content
        if ( atd != null )
        {
            toolkit.createLabel( mainClient,
                Messages.getString( "AttributeTypeDescriptionDetailsPage.NumericOID" ), SWT.NONE ); //$NON-NLS-1$
            numericOidText = toolkit.createText( mainClient, getNonNullString( atd.getOid() ), SWT.NONE );
            numericOidText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            numericOidText.setEditable( false );

            toolkit.createLabel( mainClient,
                Messages.getString( "AttributeTypeDescriptionDetailsPage.AttributeNames" ), SWT.NONE ); //$NON-NLS-1$
            namesText = toolkit.createText( mainClient, getNonNullString( SchemaUtils.toString( atd ) ), SWT.NONE );
            namesText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            namesText.setEditable( false );

            toolkit.createLabel( mainClient,
                Messages.getString( "AttributeTypeDescriptionDetailsPage.Description" ), SWT.WRAP ); //$NON-NLS-1$
            descText = toolkit.createText( mainClient, getNonNullString( atd.getDescription() ), SWT.WRAP | SWT.MULTI );
            GridData gd = new GridData( GridData.FILL_HORIZONTAL );
            gd.widthHint = detailForm.getForm().getSize().x - 100 - 60;
            descText.setLayoutData( gd );
            descText.setEditable( false );

            toolkit.createLabel( mainClient,
                Messages.getString( "AttributeTypeDescriptionDetailsPage.Usage" ), SWT.NONE ); //$NON-NLS-1$
            usageText = toolkit.createText( mainClient, getNonNullString( UsageEnum.render( atd.getUsage() ) ),
                SWT.NONE );
            usageText.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL ) );
            usageText.setEditable( false );

        }

        mainSection.layout();
    }


    // ── R2 Lists Matching Rules Beyond The Main Three ─────────────────────────────
    // Besides equality, substring, and ordering, an attribute type may declare
    // additional matching rules.  R2 collects those extras and lists them as
    // hyperlinks in this collapsible section — a dash if there are none.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Other Matching Rules" section with hyperlinks to every
     * matching rule beyond equality, substring, and ordering.
     * Rebuilt on every input change.
     *
     * @param atd  the attribute type whose extra matching rules to list; null clears
     */
    private void createOtherMatchContent( AttributeType atd )
    {
        // dispose old content
        if ( otherMatchSection.getClient() != null )
        {
            otherMatchSection.getClient().dispose();
        }

        // create new client
        Composite otherMatchClient = toolkit.createComposite( otherMatchSection, SWT.WRAP );
        otherMatchClient.setLayout( new GridLayout() );
        otherMatchSection.setClient( otherMatchClient );

        // create new content, either links to other matching rules
        // or a dash if no other matching rules exist.
        if ( atd != null )
        {
            Collection<String> otherMrdNames = SchemaUtils.getOtherMatchingRuleDescriptionNames( atd, getSchema() );
            if ( otherMrdNames != null && otherMrdNames.size() > 0 )
            {
                otherMatchSection
                    .setText( NLS
                        .bind(
                            Messages.getString( "AttributeTypeDescriptionDetailsPage.OtherMatchingRulesCount" ), new Object[] { otherMrdNames.size() } ) ); //$NON-NLS-1$
                for ( String mrdName : otherMrdNames )
                {
                    if ( getSchema().hasMatchingRuleDescription( mrdName ) )
                    {
                        MatchingRule mrd = getSchema().getMatchingRuleDescription( mrdName );
                        Hyperlink otherMatchLink = toolkit.createHyperlink( otherMatchClient, SchemaUtils
                            .toString( mrd ), SWT.WRAP );
                        otherMatchLink.setHref( mrd );
                        otherMatchLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                        otherMatchLink.setUnderlined( true );
                        otherMatchLink.setEnabled( true );
                        otherMatchLink.addHyperlinkListener( this );
                    }
                    else
                    {
                        Hyperlink otherMatchLink = toolkit.createHyperlink( otherMatchClient, mrdName, SWT.WRAP );
                        otherMatchLink.setHref( null );
                        otherMatchLink.setUnderlined( false );
                        otherMatchLink.setEnabled( false );
                    }
                }
            }
            else
            {
                otherMatchSection.setText( NLS.bind( Messages
                    .getString( "AttributeTypeDescriptionDetailsPage.OtherMatchingRulesCount" ), new Object[] { 0 } ) ); //$NON-NLS-1$
                Text otherText = toolkit.createText( otherMatchClient, getNonNullString( null ), SWT.NONE );
                otherText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                otherText.setEditable( false );
            }
        }
        else
        {
            otherMatchSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.OtherMatchingRules" ) ); //$NON-NLS-1$
        }

        otherMatchSection.layout();
    }


    // ── R2 Follows The Supertype Chain Upward ────────────────────────────────────
    // R2 checks whether this attribute type declares a SUP (superior) attribute —
    // its parent in the inheritance hierarchy — and if so, renders a single
    // clickable hyperlink to it so the user can navigate up the chain.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Supertype" section with a hyperlink to this attribute type's
     * declared superior, or a dash if it has no supertype.
     * Rebuilt on every input change.
     *
     * @param atd  the attribute type whose supertype to display; null clears
     */
    private void createSupertypeContent( AttributeType atd )
    {
        // dispose old content
        if ( supertypeSection.getClient() != null )
        {
            supertypeSection.getClient().dispose();
        }

        // create new client
        Composite superClient = toolkit.createComposite( supertypeSection, SWT.WRAP );
        superClient.setLayout( new GridLayout() );
        supertypeSection.setClient( superClient );

        // create new content, either a link to the superior attribute type
        // or a dash if no supertype exists.
        if ( atd != null )
        {
            String superType = atd.getSuperiorOid();
            if ( superType != null )
            {
                supertypeSection.setText( NLS.bind( Messages
                    .getString( "AttributeTypeDescriptionDetailsPage.SupertypeCount" ), new Object[] { 1 } ) ); //$NON-NLS-1$
                if ( getSchema().hasAttributeTypeDescription( superType ) )
                {
                    AttributeType supAtd = getSchema().getAttributeTypeDescription( superType );
                    Hyperlink superLink = toolkit.createHyperlink( superClient, SchemaUtils.toString( supAtd ),
                        SWT.WRAP );
                    superLink.setHref( supAtd );
                    superLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                    superLink.setUnderlined( true );
                    superLink.setEnabled( true );
                    superLink.addHyperlinkListener( this );
                }
                else
                {
                    Hyperlink superLink = toolkit.createHyperlink( superClient, superType, SWT.WRAP );
                    superLink.setHref( null );
                    superLink.setUnderlined( false );
                    superLink.setEnabled( false );
                }
            }
            else
            {
                supertypeSection.setText( NLS.bind( Messages
                    .getString( "AttributeTypeDescriptionDetailsPage.SupertypeCount" ), new Object[] { 0 } ) ); //$NON-NLS-1$
                Text supText = toolkit.createText( superClient, getNonNullString( null ), SWT.NONE );
                supText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                supText.setEditable( false );
            }
        }
        else
        {
            supertypeSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.Supertype" ) ); //$NON-NLS-1$
        }

        supertypeSection.layout();
    }


    // ── R2 Lists All Derived Attribute Types ─────────────────────────────────────
    // R2 scans every attribute type in the schema and collects those that declare
    // the current type as their SUP — the derived subtypes — then renders them as
    // clickable hyperlinks so the user can navigate down the inheritance tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Subtypes" section with hyperlinks to every attribute type
     * that declares this one as its superior.
     * Rebuilt on every input change.
     *
     * @param atd  the attribute type whose subtypes to list; null clears
     */
    private void createSubtypesContent( AttributeType atd )
    {
        // dispose old content
        if ( subtypesSection.getClient() != null )
        {
            subtypesSection.getClient().dispose();
        }

        // create new client
        Composite subClient = toolkit.createComposite( subtypesSection, SWT.WRAP );
        subClient.setLayout( new GridLayout() );
        subtypesSection.setClient( subClient );

        // create new content, either links to subtypes or a dash if no subtypes exist.
        if ( atd != null )
        {
            Collection<AttributeType> derivedAtds = SchemaUtils.getDerivedAttributeTypeDescriptions( atd,
                getSchema() );
            if ( derivedAtds != null && derivedAtds.size() > 0 )
            {
                subtypesSection
                    .setText( NLS
                        .bind(
                            Messages.getString( "AttributeTypeDescriptionDetailsPage.SubtypesCount" ), new Object[] { derivedAtds.size() } ) ); //$NON-NLS-1$
                for ( AttributeType derivedAtd : derivedAtds )
                {
                    Hyperlink subAttributeTypeLink = toolkit.createHyperlink( subClient, SchemaUtils
                        .toString( derivedAtd ), SWT.WRAP );
                    subAttributeTypeLink.setHref( derivedAtd );
                    subAttributeTypeLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                    subAttributeTypeLink.setUnderlined( true );
                    subAttributeTypeLink.setEnabled( true );
                    subAttributeTypeLink.addHyperlinkListener( this );
                }
            }
            else
            {
                subtypesSection.setText( NLS.bind( Messages
                    .getString( "AttributeTypeDescriptionDetailsPage.SubtypesCount" ), new Object[] { 0 } ) ); //$NON-NLS-1$
                Text subText = toolkit.createText( subClient, getNonNullString( null ), SWT.NONE );
                subText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                subText.setEditable( false );
            }
        }
        else
        {
            subtypesSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.Subtypes" ) ); //$NON-NLS-1$
        }

        subtypesSection.layout();

    }


    // ── R2 Lists Object Classes That Require This Attribute ───────────────────────
    // R2 scans the schema for every object class whose MUST list includes this
    // attribute type (either directly or via superclass inheritance) and renders
    // them as clickable hyperlinks — the "required by" cross-reference.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Used As Must" section with hyperlinks to every object class
     * that requires this attribute type.
     * Rebuilt on every input change.
     *
     * @param atd  the attribute type to cross-reference; null clears
     */
    private void createUsedAsMustContent( AttributeType atd )
    {
        // dispose old content
        if ( usedAsMustSection.getClient() != null )
        {
            usedAsMustSection.getClient().dispose();
        }

        // create new client
        Composite mustClient = toolkit.createComposite( usedAsMustSection, SWT.WRAP );
        mustClient.setLayout( new GridLayout() );
        usedAsMustSection.setClient( mustClient );

        // create new content, either links to objectclasses or a dash
        if ( atd != null )
        {
            Collection<ObjectClass> usedAsMusts = SchemaUtils.getUsedAsMust( atd, getSchema() );
            if ( usedAsMusts != null && usedAsMusts.size() > 0 )
            {
                usedAsMustSection
                    .setText( NLS
                        .bind(
                            Messages.getString( "AttributeTypeDescriptionDetailsPage.UsedAsMustCount" ), new Object[] { usedAsMusts.size() } ) ); //$NON-NLS-1$
                for ( ObjectClass ocd : usedAsMusts )
                {
                    Hyperlink usedAsMustLink = toolkit.createHyperlink( mustClient, SchemaUtils.toString( ocd ),
                        SWT.WRAP );
                    usedAsMustLink.setHref( ocd );
                    usedAsMustLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                    usedAsMustLink.setUnderlined( true );
                    usedAsMustLink.setEnabled( true );
                    usedAsMustLink.addHyperlinkListener( this );
                }
            }
            else
            {
                usedAsMustSection.setText( NLS.bind( Messages
                    .getString( "AttributeTypeDescriptionDetailsPage.UsedAsMustCount" ), new Object[] { 0 } ) ); //$NON-NLS-1$
                Text mustText = toolkit.createText( mustClient, getNonNullString( null ), SWT.NONE );
                mustText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                mustText.setEditable( false );
            }
        }
        else
        {
            usedAsMustSection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.UsedAsMust" ) ); //$NON-NLS-1$
        }

        usedAsMustSection.layout();

    }


    // ── R2 Lists Object Classes That Permit This Attribute ────────────────────────
    // R2 scans the schema for every object class whose MAY list includes this
    // attribute type (directly or via superclass inheritance) and renders them
    // as clickable hyperlinks — the "permitted by" cross-reference.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Used As May" section with hyperlinks to every object class
     * that optionally permits this attribute type.
     * Rebuilt on every input change.
     *
     * @param atd  the attribute type to cross-reference; null clears
     */
    private void createUsedAsMayContent( AttributeType atd )
    {
        // dispose old content
        if ( usedAsMaySection.getClient() != null )
        {
            usedAsMaySection.getClient().dispose();
        }

        // create new client
        Composite mayClient = toolkit.createComposite( usedAsMaySection, SWT.WRAP );
        mayClient.setLayout( new GridLayout() );
        usedAsMaySection.setClient( mayClient );

        // create new content, either links to objectclasses or a dash
        if ( atd != null )
        {
            Collection<ObjectClass> usedAsMays = SchemaUtils.getUsedAsMay( atd, getSchema() );
            if ( usedAsMays != null && usedAsMays.size() > 0 )
            {
                usedAsMaySection
                    .setText( NLS
                        .bind(
                            Messages.getString( "AttributeTypeDescriptionDetailsPage.UsedAsMayCount" ), new Object[] { usedAsMays.size() } ) ); //$NON-NLS-1$
                for ( ObjectClass ocd : usedAsMays )
                {
                    Hyperlink usedAsMayLink = toolkit
                        .createHyperlink( mayClient, SchemaUtils.toString( ocd ), SWT.WRAP );
                    usedAsMayLink.setHref( ocd );
                    usedAsMayLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                    usedAsMayLink.setUnderlined( true );
                    usedAsMayLink.setEnabled( true );
                    usedAsMayLink.addHyperlinkListener( this );
                }
            }
            else
            {
                usedAsMaySection.setText( NLS.bind( Messages
                    .getString( "AttributeTypeDescriptionDetailsPage.UsedAsMayCount" ), new Object[] { 0 } ) ); //$NON-NLS-1$
                Text mayText = toolkit.createText( mayClient, getNonNullString( null ), SWT.NONE );
                mayText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                mayText.setEditable( false );
            }
        }
        else
        {
            usedAsMaySection.setText( Messages.getString( "AttributeTypeDescriptionDetailsPage.UsedAsMay" ) ); //$NON-NLS-1$
        }

        usedAsMaySection.layout();
    }

}
