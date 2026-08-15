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


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.MatchingRuleUse;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
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


// ── CLASS: MatchingRuleUseDescriptionDetailsPage — Blueprint: MR-Use Spec ──────
// A technician selects a matching-rule-use entry — "caseIgnoreMatch applies to
// cn, sn, l" — and R2-D2 projects the full cross-reference card: which matching
// rule this is (a hyperlink), whether it is obsolete, and all the attribute types
// it officially applies to on this server.  This class renders that detail panel.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The detail page that displays the full specification of a selected matching
 * rule use description on the right-hand side of the schema browser.
 * It shows OID and a hyperlink to the corresponding matching rule in a "Details"
 * section, the obsolete flag in a "Flags" section, and all applicable attribute
 * types in a collapsible "Applies" section.
 * Think of this class as R2 projecting the matching-rule-use cross-reference card.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MatchingRuleUseDescriptionDetailsPage extends SchemaDetailsPage
{

    /** The main section, contains oid, names and desc */
    private Section mainSection;

    /** The numeric oid field */
    private Text numericOidText;

    /** The name link */
    private Hyperlink nameLink;

    /** The description field */
    private Text descText;

    /** The flag section, contains obsolete */
    private Section flagSection;

    /** The obsolete field */
    private Label isObsoleteText;

    /** The applies section, contains links */
    private Section appliesSection;


    // ── R2 Loads The Matching-Rule-Use Detail Module ───────────────────────────────
    // R2 slots the matching-rule-use cross-reference module into his projection
    // system, linking it to the master page and toolkit.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the matching rule use details page linked to the given master page.
     *
     * @param schemaPage  the master schema page that owns this detail page
     * @param toolkit     the JFace forms toolkit
     */
    public MatchingRuleUseDescriptionDetailsPage( SchemaPage schemaPage, FormToolkit toolkit )
    {
        super( schemaPage, toolkit );
    }


    // ── R2 Builds The Matching-Rule-Use Detail Panel ───────────────────────────────
    // R2 assembles the display: a Details section with OID and rule hyperlink, a
    // Flags section for the obsolete indicator, a collapsible Applies section, and
    // the raw LDIF footer.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT layout for this detail page.
     * Creates the "Details," "Flags," and "Applies" sections along with the
     * standard "Raw Schema Definition" section.
     *
     * @param detailForm  the scrolled form that parents all sections
     */
    @Override
    public void createContents( final ScrolledForm detailForm )
    {

        this.detailForm = detailForm;
        detailForm.getBody().setLayout( new GridLayout() );

        // create main section
        mainSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        mainSection.setText( Messages.getString( "MatchingRuleUseDescriptionDetailsPage.Details" ) ); //$NON-NLS-1$
        mainSection.marginWidth = 0;
        mainSection.marginHeight = 0;
        mainSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( mainSection );

        // create flag section
        flagSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        flagSection.setText( Messages.getString( "MatchingRuleUseDescriptionDetailsPage.Flags" ) ); //$NON-NLS-1$
        flagSection.marginWidth = 0;
        flagSection.marginHeight = 0;
        flagSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( flagSection );

        // create flag content
        Composite flagClient = toolkit.createComposite( flagSection, SWT.WRAP );
        GridLayout flagLayout = new GridLayout();
        flagLayout.numColumns = 1;
        flagLayout.marginWidth = 0;
        flagLayout.marginHeight = 0;
        flagClient.setLayout( flagLayout );
        flagSection.setClient( flagClient );

        isObsoleteText = toolkit.createLabel( flagClient, Messages
            .getString( "MatchingRuleUseDescriptionDetailsPage.Obsolete" ), SWT.CHECK ); //$NON-NLS-1$
        isObsoleteText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        isObsoleteText.setEnabled( false );

        // create applies section
        appliesSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        appliesSection.setText( Messages.getString( "MatchingRuleUseDescriptionDetailsPage.Applies" ) ); //$NON-NLS-1$
        appliesSection.marginWidth = 0;
        appliesSection.marginHeight = 0;
        appliesSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( appliesSection );
        appliesSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create raw section
        super.createRawSection();
    }


    // ── R2 Projects The Full Matching-Rule-Use Card ────────────────────────────────
    // The officer calls out the entry; R2 fills in the OID, the hyperlink to the
    // parent matching rule, the obsolete flag, the applies list, and the raw LDIF.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates all sections to display the given matching rule use description.
     * Rebuilds dynamic sections and reflows the form on every call.
     *
     * <p>For example — R2 projects the caseIgnoreMatch-use card:</p>
     * <pre>
     *   setInput(matchingRuleUse);
     *   // nameLink → caseIgnoreMatch matching rule page
     *   // Applies: cn, sn, l, o, ou, ...
     * </pre>
     *
     * @param input  the {@link MatchingRuleUse} to display; null clears the pane
     */
    @Override
    public void setInput( Object input )
    {
        MatchingRuleUse mrud = null;
        if ( input instanceof MatchingRuleUse )
        {
            mrud = ( MatchingRuleUse ) input;
        }

        // create main content
        this.createMainContent( mrud );

        // set flag
        isObsoleteText.setEnabled( mrud != null && mrud.isObsolete() );

        // create contents of dynamic sections
        this.createAppliesContents( mrud );
        super.createRawContents( mrud );

        this.detailForm.reflow( true );
    }


    // ── R2 Fills The Main Identification Fields ────────────────────────────────────
    // R2 populates the OID, the matching-rule hyperlink, and the description fields,
    // rebuilt fresh each time for proper multi-line description layout.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Details" section content with OID, matching rule hyperlink,
     * and description for the given matching rule use.
     *
     * @param mrud  the matching rule use to display; null leaves the section empty
     */
    private void createMainContent( MatchingRuleUse mrud )
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
        if ( mrud != null )
        {
            toolkit.createLabel( mainClient,
                Messages.getString( "MatchingRuleUseDescriptionDetailsPage.NumericOID" ), SWT.NONE ); //$NON-NLS-1$
            numericOidText = toolkit.createText( mainClient, getNonNullString( mrud.getOid() ), SWT.NONE );
            numericOidText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            numericOidText.setEditable( false );

            toolkit.createLabel( mainClient,
                Messages.getString( "MatchingRuleUseDescriptionDetailsPage.MatchingRules" ), SWT.NONE ); //$NON-NLS-1$
            nameLink = toolkit.createHyperlink( mainClient, "", SWT.WRAP ); //$NON-NLS-1$
            nameLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            nameLink.addHyperlinkListener( this );

            Schema schema = getSchema();
            MatchingRule mrd = schema.hasMatchingRuleDescription( mrud.getOid() ) ? schema
                .getMatchingRuleDescription( mrud.getOid() ) : null;
            nameLink
                .setText( getNonNullString( mrd != null ? SchemaUtils.toString( mrd ) : SchemaUtils.toString( mrud ) ) );
            nameLink.setHref( mrd );
            nameLink.setUnderlined( mrd != null );
            nameLink.setEnabled( mrd != null );

            toolkit.createLabel( mainClient,
                Messages.getString( "MatchingRuleUseDescriptionDetailsPage.Description" ), SWT.NONE ); //$NON-NLS-1$
            descText = toolkit.createText( mainClient, getNonNullString( mrud.getDescription() ), SWT.WRAP | SWT.MULTI );
            GridData gd = new GridData( GridData.FILL_HORIZONTAL );
            gd.widthHint = detailForm.getForm().getSize().x - 100 - 60;
            descText.setLayoutData( gd );
            descText.setEditable( false );
        }

        mainSection.layout();
    }


    // ── R2 Lists Which Attribute Types This Rule Applies To ───────────────────────
    // R2 reads the "APPLIES" list from the matching-rule-use entry and produces
    // clickable hyperlinks to each applicable attribute type, or a dash if empty.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Applies" section with hyperlinks to every attribute type
     * that this matching rule use declares it applies to.
     * Rebuilt on every input change.
     *
     * @param mrud  the matching rule use whose APPLIES list to display; null clears
     */
    private void createAppliesContents( MatchingRuleUse mrud )
    {
        // dispose old content
        if ( appliesSection.getClient() != null )
        {
            appliesSection.getClient().dispose();
        }

        // create new client
        Composite appliesClient = toolkit.createComposite( appliesSection, SWT.WRAP );
        appliesClient.setLayout( new GridLayout() );
        appliesSection.setClient( appliesClient );

        // create content
        if ( mrud != null )
        {
            List<String> names = mrud.getApplicableAttributeOids();
            if ( names != null && !names.isEmpty() )
            {
                appliesSection
                    .setText( NLS
                        .bind(
                            Messages.getString( "MatchingRuleUseDescriptionDetailsPage.AppliesCount" ), new Object[] { names.size() } ) ); //$NON-NLS-1$
                Schema schema = getSchema();
                for ( String name : names )
                {
                    if ( schema.hasAttributeTypeDescription( name ) )
                    {
                        AttributeType appliesAtd = schema.getAttributeTypeDescription( name );
                        Hyperlink appliesLink = toolkit.createHyperlink( appliesClient, SchemaUtils
                            .toString( appliesAtd ), SWT.WRAP );
                        appliesLink.setHref( appliesAtd );
                        appliesLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                        appliesLink.setUnderlined( true );
                        appliesLink.setEnabled( true );
                        appliesLink.addHyperlinkListener( this );
                    }
                    else
                    {
                        Hyperlink appliesLink = toolkit.createHyperlink( appliesClient, name, SWT.WRAP );
                        appliesLink.setHref( null );
                        appliesLink.setUnderlined( false );
                        appliesLink.setEnabled( false );
                    }
                }
            }
            else
            {
                appliesSection.setText( NLS.bind( Messages
                    .getString( "MatchingRuleUseDescriptionDetailsPage.AppliesCount" ), new Object[] { 0 } ) ); //$NON-NLS-1$
                Text usedFromText = toolkit.createText( appliesClient, getNonNullString( null ), SWT.NONE );
                usedFromText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                usedFromText.setEditable( false );
            }
        }
        else
        {
            appliesSection.setText( Messages.getString( "MatchingRuleUseDescriptionDetailsPage.Applies" ) ); //$NON-NLS-1$
        }

        appliesSection.layout();
    }

}
