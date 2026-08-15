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
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: LdapSyntaxDescriptionDetailsPage — Death Star Blueprint: Syntax Spec ─
// A technician selects "DirectoryString" from the syntax index; R2-D2 projects
// the full specification panel: the numeric OID, the human description, and a
// cross-reference list of every attribute type that uses this wire format.
// This class builds exactly that detail panel for a selected LDAP syntax
// description — OID and description in the main section, attribute-type
// cross-references in a collapsible "Used From" section, and the raw LDIF line
// at the bottom.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The detail page that displays the full specification of a selected LDAP
 * syntax description on the right-hand side of the schema browser.
 * It shows the syntax's numeric OID and description in a fixed section, and
 * all attribute types that reference this syntax in a collapsible "Used From" section.
 * Think of this class as R2 projecting a single syntax blueprint page: OID,
 * human description, and the list of fields that speak this wire format.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapSyntaxDescriptionDetailsPage extends SchemaDetailsPage
{

    /** The main section, contains oid and desc */
    private Section mainSection;

    /** The used from section, contains links to attribute types */
    private Section usedFromSection;


    // ── R2 Loads The Syntax Blueprint Module ──────────────────────────────────────
    // R2 slots the syntax-detail module into his projection system — linking it
    // to the master page and toolkit so it can build controls when asked.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the syntax details page linked to the given master schema page and toolkit.
     *
     * <p>For example — R2 loads the syntax module:</p>
     * <pre>
     *   new LdapSyntaxDescriptionDetailsPage(schemaPage, toolkit);
     *   // ready for createContents() to be called
     * </pre>
     *
     * @param schemaPage  the master schema page that owns this detail page
     * @param toolkit     the JFace forms toolkit used to create controls
     */
    public LdapSyntaxDescriptionDetailsPage( SchemaPage schemaPage, FormToolkit toolkit )
    {
        super( schemaPage, toolkit );
    }


    // ── R2 Builds The Syntax Detail Panel ─────────────────────────────────────────
    // R2 assembles the holographic display panel for a syntax: a fixed "Details"
    // section at the top, a collapsible "Used From" cross-reference below, and
    // the raw LDIF footer.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT layout for this detail page inside the given form.
     * Creates a fixed "Details" section for OID and description, a collapsible
     * "Used From" section for attribute-type cross-references, and the standard
     * "Raw Schema Definition" section inherited from {@link SchemaDetailsPage}.
     *
     * <p>For example — R2 projects the syntax panel layout:</p>
     * <pre>
     *   createContents(detailForm);
     *   // mainSection + usedFromSection (collapsible) + rawSection visible
     * </pre>
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
        mainSection.setText( Messages.getString( "LdapSyntaxDescriptionDetailsPage.Details" ) ); //$NON-NLS-1$
        mainSection.marginWidth = 0;
        mainSection.marginHeight = 0;
        mainSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( mainSection );

        // create used from section
        usedFromSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        usedFromSection.setText( Messages.getString( "LdapSyntaxDescriptionDetailsPage.UsedFrom" ) ); //$NON-NLS-1$
        usedFromSection.marginWidth = 0;
        usedFromSection.marginHeight = 0;
        usedFromSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( usedFromSection );
        usedFromSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create raw section
        createRawSection();
    }


    // ── R2 Projects A Specific Syntax Blueprint ────────────────────────────────────
    // The officer calls out "Show the DirectoryString syntax" and R2 loads that
    // definition from his storage, populates the main OID/description fields, fills
    // the "Used From" cross-reference, and reflowing the display.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates all sections of the detail panel to display the given syntax description.
     * Rebuilds the main and "Used From" sections so they always reflect the current
     * selection; clears everything if the input is null.
     *
     * <p>For example — R2 projects the DirectoryString spec:</p>
     * <pre>
     *   setInput(directoryStringSyntax);
     *   // OID = "1.3.6.1.4.1.1466.115.121.1.15"
     *   // description = "Directory String"
     *   // Used From: cn, sn, description, ...
     * </pre>
     *
     * @param input  the {@link LdapSyntax} to display; null clears the pane
     */
    @Override
    public void setInput( Object input )
    {
        LdapSyntax lsd = null;
        if ( input instanceof LdapSyntax )
        {
            lsd = ( LdapSyntax ) input;
        }

        createMainContent( lsd );
        createUsedFromContents( lsd );
        createRawContents( lsd );

        detailForm.reflow( true );
    }


    // ── R2 Fills The Main Specification Fields ────────────────────────────────────
    // R2 populates the top panel of the syntax blueprint: OID in one field,
    // human-readable description in another — rebuilt fresh each time so multi-line
    // descriptions resize the layout correctly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the main section's content with OID and description fields for
     * the given syntax.
     * We dispose and recreate the section client on every call so multi-line
     * descriptions force a proper layout resize.
     *
     * <p>For example — R2 fills in the syntax's header fields:</p>
     * <pre>
     *   createMainContent(lsd);
     *   // Numeric OID: "1.3.6.1.4.1.1466.115.121.1.15"
     *   // Description: "Directory String"
     * </pre>
     *
     * @param lsd  the syntax to display; null leaves the section empty
     */
    private void createMainContent( LdapSyntax lsd )
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
        if ( lsd != null )
        {
            toolkit.createLabel( mainClient,
                Messages.getString( "LdapSyntaxDescriptionDetailsPage.NumericOID" ), SWT.NONE ); //$NON-NLS-1$
            Text numericOidText = toolkit.createText( mainClient, getNonNullString( lsd.getOid() ), SWT.NONE );
            numericOidText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            numericOidText.setEditable( false );

            toolkit.createLabel( mainClient,
                Messages.getString( "LdapSyntaxDescriptionDetailsPage.Description" ), SWT.NONE ); //$NON-NLS-1$
            Text descText = toolkit.createText( mainClient, getNonNullString( lsd.getDescription() ), SWT.WRAP
                | SWT.MULTI );
            GridData gd = new GridData( GridData.FILL_HORIZONTAL );
            gd.widthHint = detailForm.getForm().getSize().x - 100 - 60;
            descText.setLayoutData( gd );
            descText.setEditable( false );
        }

        mainSection.layout();
    }


    // ── R2 Lists Which Attribute Types Use This Syntax ────────────────────────────
    // R2 scans his blueprint cross-references and produces a list of every attribute
    // type that declares this syntax as its wire format — clickable hyperlinks so
    // the user can jump directly to any of them.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recreates the "Used From" section with hyperlinks to every attribute type
     * that references this syntax.
     * We rebuild on every input change because the list is dynamic.
     * Each hyperlink navigates to the corresponding attribute type details page.
     *
     * <p>For example — R2 cross-references which fields use this syntax:</p>
     * <pre>
     *   createUsedFromContents(lsd);
     *   // Used From (12): cn, sn, l, st, o, ou, description, ...
     * </pre>
     *
     * @param lsd  the syntax whose attribute-type cross-references to display; null clears
     */
    private void createUsedFromContents( LdapSyntax lsd )
    {
        // dispose old content
        if ( usedFromSection.getClient() != null && !usedFromSection.getClient().isDisposed() )
        {
            usedFromSection.getClient().dispose();
        }

        // create new client
        Composite usedFromClient = toolkit.createComposite( usedFromSection, SWT.WRAP );
        usedFromClient.setLayout( new GridLayout() );
        usedFromSection.setClient( usedFromClient );

        // create content
        if ( lsd != null )
        {
            Collection<AttributeType> usedFromATDs = SchemaUtils.getUsedFromAttributeTypeDescriptions( lsd,
                getSchema() );
            if ( usedFromATDs != null && !usedFromATDs.isEmpty() )
            {
                usedFromSection
                    .setText( NLS
                        .bind(
                            Messages.getString( "LdapSyntaxDescriptionDetailsPage.UsedFromCount" ), new Object[] { usedFromATDs.size() } ) ); //$NON-NLS-1$
                for ( AttributeType atd : usedFromATDs )
                {
                    Hyperlink usedFromLink = toolkit.createHyperlink( usedFromClient, SchemaUtils.toString( atd ),
                        SWT.WRAP );
                    usedFromLink.setHref( atd );
                    usedFromLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                    usedFromLink.setUnderlined( true );
                    usedFromLink.setEnabled( true );
                    usedFromLink.addHyperlinkListener( this );
                }
            }
            else
            {
                usedFromSection.setText( NLS.bind( Messages
                    .getString( "LdapSyntaxDescriptionDetailsPage.UsedFromCount" ), new Object[] { 0 } ) ); //$NON-NLS-1$
                Text usedFromText = toolkit.createText( usedFromClient, getNonNullString( null ), SWT.NONE );
                usedFromText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                usedFromText.setEditable( false );
            }
        }
        else
        {
            usedFromSection.setText( Messages.getString( "LdapSyntaxDescriptionDetailsPage.UsedFrom" ) ); //$NON-NLS-1$
        }

        usedFromSection.layout();
    }

}
