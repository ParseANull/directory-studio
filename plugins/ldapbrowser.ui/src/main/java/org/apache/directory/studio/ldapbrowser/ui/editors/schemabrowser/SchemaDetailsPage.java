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

import org.apache.directory.api.ldap.model.schema.AbstractSchemaObject;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: SchemaDetailsPage — The Death Star Blueprints R2 Is Carrying ──────
// R2-D2 carries the Death Star blueprints inside him; any technician who plugs
// in and asks for a particular system diagram gets a detailed readout of that
// section — power conduits, structural specs, cross-references to adjacent
// sections.  This abstract class is the common blueprint reader: it provides
// the shared machinery (raw section, hyperlink handling, schema access) that
// all the concrete "system diagram" pages (attribute type, object class, etc.)
// inherit and build on top of.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all schema detail pages shown on the right-hand side
 * of the schema browser's master/detail split.
 * Subclasses fill in the specific content for one schema element type (attribute
 * type, object class, syntax, matching rule, matching rule use) while we provide
 * the shared raw-section display, hyperlink navigation, and schema accessor.
 * Think of this class as the blueprint reader that knows how to project any
 * section of R2's plans — the specific plan section is supplied by the subclass.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class SchemaDetailsPage implements IHyperlinkListener
{

    /** The raw section, displays the schema attibute value */
    protected Section rawSection;

    /** The text with the schema attribute value */
    protected Text rawText;

    /** The toolkit used to create controls */
    protected FormToolkit toolkit;

    /** The master schema page */
    protected SchemaPage schemaPage;

    /** The detail page form */
    protected ScrolledForm detailForm;


    // ── R2 Boots Up His Blueprint Reader ──────────────────────────────────────────
    // R2 powers on the holoprojector and links it to the master schema page so
    // that any blueprint he projects stays coordinated with the list view.
    // We store the page and toolkit references so subclasses can use them when
    // building their specific UI sections.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new detail page linked to the given schema page and toolkit.
     * Subclasses call this via {@code super()} as the first thing in their own
     * constructors.
     *
     * <p>For example — R2 initialises his projector:</p>
     * <pre>
     *   SchemaDetailsPage page =
     *       new AttributeTypeDescriptionDetailsPage(schemaPage, toolkit);
     *   // page is ready to have createContents() called next
     * </pre>
     *
     * @param schemaPage  the master page that owns this detail page
     * @param toolkit     the JFace forms toolkit used to create all SWT controls
     */
    protected SchemaDetailsPage( SchemaPage schemaPage, FormToolkit toolkit )
    {
        this.schemaPage = schemaPage;
        this.toolkit = toolkit;
    }


    // ── The Projector Powers Down ─────────────────────────────────────────────────
    // After the briefing R2 retracts his holoprojector and the room goes dark —
    // the blueprint is no longer needed and resources are released.
    // This is a hook for subclasses that hold resources; the base class has nothing
    // to clean up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes any resources held by this detail page.
     * The base implementation does nothing; subclasses that allocate SWT resources
     * should override this and release them here.
     *
     * <p>For example — R2 retracts the holoprojector:</p>
     * <pre>
     *   detailsPage.dispose();
     *   // page is now inert; do not call setInput again
     * </pre>
     */
    public void dispose()
    {
    }


    // ── The Crew Follows A Blueprint Cross-Reference ───────────────────────────────
    // A technician studying the blueprints spots a cross-reference hyperlink to
    // another section — "See reactor coolant diagram."  They click it and R2
    // immediately projects the referenced section.
    // We handle the hyperlink activation here by constructing a new SchemaBrowserInput
    // for the linked schema object and setting it on the browser.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Handles a hyperlink click within the detail page by navigating the schema
     * browser to the linked schema element.
     * The hyperlink's href is expected to be an {@link AbstractSchemaObject}; if it
     * is, we wrap it in a new SchemaBrowserInput and push it to the browser.
     *
     * <p>For example — the crew jumps to the referenced diagram:</p>
     * <pre>
     *   // user clicks "equalityMatchingRule" hyperlink
     *   linkActivated(event);
     *   // schemaBrowser.setInput(new SchemaBrowserInput(conn, matchingRule))
     * </pre>
     *
     * @param e  the hyperlink event carrying the href object
     */
    @Override
    public void linkActivated( HyperlinkEvent e )
    {
        Object obj = e.getHref();
        if ( obj instanceof AbstractSchemaObject )
        {
            schemaPage.getSchemaBrowser().setInput(
                new SchemaBrowserInput( schemaPage.getConnection(), ( AbstractSchemaObject ) obj ) );
        }
    }


    // ── Cursor Hovers Over A Blueprint Cross-Reference ────────────────────────────
    // A technician moves their finger toward a cross-reference but hasn't clicked
    // yet — nothing needs to happen in response.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user's mouse enters a hyperlink area; no action needed here.
     *
     * @param e  the hyperlink event
     */
    @Override
    public void linkEntered( HyperlinkEvent e )
    {
    }


    // ── Cursor Leaves The Cross-Reference ─────────────────────────────────────────
    // The technician's finger moves away from the cross-reference back to the
    // main diagram — still no action required.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user's mouse leaves a hyperlink area; no action needed here.
     *
     * @param e  the hyperlink event
     */
    @Override
    public void linkExited( HyperlinkEvent e )
    {
    }


    // ── R2 Projects A New Blueprint Section ───────────────────────────────────────
    // The technician names the section they want — "attribute type: cn" — and R2
    // locates it in his storage and projects the full detail view.
    // Subclasses implement this to populate their specific UI sections.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the detail page to display the given schema element.
     * Called by the master page whenever the user selects a different element in
     * the list; subclasses populate all their sections from the given input object.
     * Pass null to clear the detail pane.
     *
     * @param input  the schema element to display, or null to clear the pane
     */
    public abstract void setInput( Object input );


    // ── R2 Constructs The Holographic Display ─────────────────────────────────────
    // R2 builds the physical layout of the holoprojector readout — the frames,
    // the panel dividers — before any actual blueprint content is filled in.
    // Subclasses implement this to lay out their SWT sections inside the form.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT control layout for this detail page inside the given form.
     * Called once, right after construction, before any input is set.
     * Subclasses create their sections and widgets here.
     *
     * @param detailForm  the scrolled form that acts as the parent container
     */
    protected abstract void createContents( final ScrolledForm detailForm );


    // ── R2 Creates The Raw Data Panel ─────────────────────────────────────────────
    // In addition to the formatted blueprint view, R2 can display the raw binary
    // data from the storage medium — the exact bytes as they were recorded.
    // We create the collapsible "Raw Schema Definition" section that shows the LDIF
    // line representation of whatever schema element is selected.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the collapsible "Raw Schema Definition" section to the detail form.
     * This section shows the raw LDIF encoding of the selected schema element,
     * which is useful for debugging and for users who want to see exactly what
     * the server returned.
     * Call this once from {@link #createContents(ScrolledForm)}, after all other
     * sections are created.
     *
     * <p>For example — R2 adds the raw data panel to the display:</p>
     * <pre>
     *   createRawSection(); // adds collapsed "Raw Schema Definition" twistie
     *   // user expands it to see: "( 2.5.4.3 NAME 'cn' ... )"
     * </pre>
     */
    protected void createRawSection()
    {
        rawSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        rawSection.setText( Messages.getString( "SchemaDetailsPage.RawSchemaDefinition" ) ); //$NON-NLS-1$
        rawSection.marginWidth = 0;
        rawSection.marginHeight = 0;
        rawSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( rawSection );
        rawSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );
    }


    // ── R2 Fills In The Raw Data Panel ────────────────────────────────────────────
    // When the technician expands the raw data panel R2 reads the binary data from
    // storage and displays it verbatim — exactly as it was encoded by the server.
    // We dispose the old content and create fresh text widgets so the display
    // always matches the currently selected schema element.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the raw section with the LDIF representation of the given schema
     * element, or clears the section if the element is null.
     * We dispose and recreate the section's content on every call so there is no
     * stale data visible from a previous selection.
     *
     * <p>For example — R2 renders the raw blueprint bytes:</p>
     * <pre>
     *   createRawContents(attributeType);
     *   // rawText displays:
     *   // "( 2.5.4.3 NAME ( 'cn' 'commonName' ) SUP name )"
     * </pre>
     *
     * @param asd  the schema object whose LDIF line to display; null clears the section
     */
    protected void createRawContents( AbstractSchemaObject asd )
    {

        if ( rawSection.getClient() != null && !rawSection.getClient().isDisposed() )
        {
            rawSection.getClient().dispose();
        }

        Composite client = toolkit.createComposite( rawSection, SWT.WRAP );
        client.setLayout( new GridLayout() );
        rawSection.setClient( client );

        if ( asd != null )
        {
            rawText = toolkit.createText( client, getNonNullString( SchemaUtils.getLdifLine( asd ) ), SWT.WRAP
                | SWT.MULTI );
            GridData gd2 = new GridData( GridData.FILL_HORIZONTAL );
            gd2.widthHint = detailForm.getForm().getSize().x - 100 - 60;
            // detailForm.getForm().getVerticalBar().getSize().x
            // gd2.widthHint = 10;
            rawText.setLayoutData( gd2 );
            rawText.setEditable( false );
        }

        rawSection.layout();

    }


    // ── R2 Identifies Which Death Star We Are Inspecting ──────────────────────────
    // Before projecting blueprints R2 verifies which Death Star installation we are
    // querying — the schema that belongs to the currently selected connection.
    // We delegate to the schema page to get the Schema object for the active
    // LDAP connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema for the currently selected LDAP connection.
     * All detail page subclasses call this to resolve cross-references (e.g. to
     * find the matching rule that corresponds to an OID string).
     *
     * @return the Schema object for the active connection; never null in practice
     *         because the page is disabled when no connection is selected
     */
    protected Schema getSchema()
    {
        return schemaPage.getConnection().getSchema();
    }


    // ── R2 Substitutes A Dash For Missing Data ────────────────────────────────────
    // If a blueprint section is blank R2 shows a dash rather than an empty field
    // so the viewer knows the data is intentionally absent, not a rendering error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the given string, or a dash {@code "-"} if it is null.
     * We use this everywhere in detail pages so text fields always show something
     * rather than being blank, which would look like a layout bug.
     *
     * @param s  the string to return, or null
     * @return   {@code s} if non-null, otherwise {@code "-"}
     */
    protected String getNonNullString( String s )
    {
        return s == null ? "-" : s; //$NON-NLS-1$
    }


    // ── R2 Substitutes A Dash For An Empty List ───────────────────────────────────
    // If a blueprint section has no entries R2 still shows a dash for that field
    // so the viewer is not confused by an empty panel.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first element of the list as a string, or {@code "-"} if the
     * list is null or empty.
     * We use this variant where a schema attribute that normally has one value
     * is stored as a list.
     *
     * @param s  a list of strings to pick from, or null
     * @return   the first list element, or {@code "-"} if none exists
     */
    private String getNonNullString( List<String> s )
    {
        if ( s == null || s.isEmpty() )
        {
            return "-"; //$NON-NLS-1$
        }

        return s.get( 0 );
    }

}
