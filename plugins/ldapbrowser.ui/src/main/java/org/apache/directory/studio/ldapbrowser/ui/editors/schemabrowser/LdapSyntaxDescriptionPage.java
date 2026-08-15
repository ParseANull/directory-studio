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


import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;


// ── CLASS: LdapSyntaxDescriptionPage — Death Star Blueprints, Syntax Section ──
// R2-D2 flips to the "Syntaxes" chapter of the blueprints — a catalogue of every
// data format the installation recognises (Integer, DirectoryString, OctetString,
// …).  Each syntax is a wire format specification that says what values are legal
// for attribute types that reference it.  This page lists them all in the master
// table and delegates to a detail page for the full spec.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The schema browser tab that lists all LDAP syntax descriptions from the
 * selected connection's schema.
 * Selecting an entry shows its details in {@link LdapSyntaxDescriptionDetailsPage}.
 * Think of this class as R2's syntax index: every wire format the server speaks,
 * searchable by name or OID, alphabetically ordered, drillable on click.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapSyntaxDescriptionPage extends SchemaPage
{

    // ── R2 Attaches The Syntax Index To The Browser ───────────────────────────────
    // R2 links his syntax catalogue section to the master schema browser.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new syntax description page linked to the given schema browser.
     *
     * @param schemaBrowser  the schema browser editor that hosts this tab
     */
    public LdapSyntaxDescriptionPage( SchemaBrowser schemaBrowser )
    {
        super( schemaBrowser );
    }


    // ── R2 Labels The Syntax Section ──────────────────────────────────────────────
    // The chapter header reads "Syntaxes" so the technician knows which section
    // of the blueprints they are browsing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised title for this page, shown in the section header.
     *
     * @return the page title, e.g. "Syntaxes"
     */
    @Override
    protected String getTitle()
    {
        return Messages.getString( "LdapSyntaxDescriptionPage.Syntaxes" ); //$NON-NLS-1$
    }


    // ── R2 Provides A Filter Hint ─────────────────────────────────────────────────
    // A brief prompt tells the user that the filter field narrows the syntax list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised description shown below the section title.
     *
     * @return the filter description, e.g. "Select a syntax"
     */
    @Override
    protected String getFilterDescription()
    {
        return Messages.getString( "LdapSyntaxDescriptionPage.SelectASyntax" ); //$NON-NLS-1$
    }


    /**
     * Returns the content provider that extracts all syntax descriptions from the schema.
     *
     * @return an {@link LSDContentProvider} instance
     */
    @Override
    protected IStructuredContentProvider getContentProvider()
    {
        return new LSDContentProvider();
    }


    /**
     * Returns the label provider that renders each syntax as a display string.
     *
     * @return an {@link LSDLabelProvider} instance
     */
    @Override
    protected ITableLabelProvider getLabelProvider()
    {
        return new LSDLabelProvider();
    }


    /**
     * Returns the sorter that orders syntax descriptions alphabetically by display name.
     *
     * @return an {@link LSDViewerSorter} instance
     */
    @Override
    protected ViewerSorter getSorter()
    {
        return new LSDViewerSorter();
    }


    /**
     * Returns the viewer filter that hides syntaxes not matching the filter text.
     *
     * @return an {@link LSDViewerFilter} instance
     */
    @Override
    protected ViewerFilter getFilter()
    {
        return new LSDViewerFilter();
    }


    /**
     * Creates and returns the details page for the selected syntax description.
     *
     * @return a new {@link LdapSyntaxDescriptionDetailsPage}
     */
    @Override
    protected SchemaDetailsPage getDetailsPage()
    {
        return new LdapSyntaxDescriptionDetailsPage( this, this.toolkit );
    }

    /**
     * Supplies the table viewer with LDAP syntax descriptions from the schema.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class LSDContentProvider implements IStructuredContentProvider
    {
        // ── R2 Pulls The Syntax List From Storage ──────────────────────────────────
        // R2 reads the syntax definitions from his schema storage and returns them
        // as an array for the table viewer to render.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Returns all LDAP syntax descriptions from the given schema input.
         *
         * @param inputElement  the Schema object; other types yield an empty array
         * @return              array of {@link LdapSyntax} objects, or empty array
         */
        public Object[] getElements( Object inputElement )
        {
            if ( inputElement instanceof Schema )
            {
                Schema schema = ( Schema ) inputElement;
                if ( schema != null && schema.getLdapSyntaxDescriptions() != null )
                {
                    return schema.getLdapSyntaxDescriptions().toArray();
                }
            }
            return new Object[0];
        }


        /** No-op. */
        public void dispose()
        {
        }


        /** No-op. */
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }
    }

    /**
     * Renders each LDAP syntax as a display string.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class LSDLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        /**
         * Returns the display text for the given syntax.
         *
         * @param obj    expected to be an {@link LdapSyntax}
         * @param index  column index (always 0)
         * @return       the formatted syntax name, or {@code obj.toString()} fallback
         */
        public String getColumnText( Object obj, int index )
        {
            if ( obj instanceof LdapSyntax )
            {
                return SchemaUtils.toString( ( LdapSyntax ) obj );
            }
            return obj.toString();
        }


        /** Returns null; text-only rows. */
        public Image getColumnImage( Object obj, int index )
        {
            return null;
        }
    }

    /**
     * Sorts LDAP syntax descriptions alphabetically by display name.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class LSDViewerSorter extends ViewerSorter
    {
        /**
         * Compares two syntaxes by formatted display name.
         *
         * @param viewer  the viewer (not used)
         * @param e1      first element
         * @param e2      second element
         * @return        negative, zero, or positive ordering
         */
        public int compare( Viewer viewer, Object e1, Object e2 )
        {
            if ( e1 instanceof LdapSyntax )
            {
                e1 = SchemaUtils.toString( ( LdapSyntax ) e1 );
            }
            if ( e2 instanceof LdapSyntax )
            {
                e2 = SchemaUtils.toString( ( LdapSyntax ) e2 );
            }
            return e1.toString().compareTo( e2.toString() );
        }
    }

    /**
     * Hides LDAP syntaxes that do not match the filter text (name or OID,
     * case-insensitive).
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class LSDViewerFilter extends ViewerFilter
    {
        /**
         * Returns true if the given syntax matches the filter text by name or OID.
         *
         * @param viewer         the viewer (not used)
         * @param parentElement  the parent element (not used)
         * @param element        the schema element to test
         * @return               true to show, false to hide
         */
        public boolean select( Viewer viewer, Object parentElement, Object element )
        {
            if ( element instanceof LdapSyntax )
            {
                LdapSyntax lsd = ( LdapSyntax ) element;
                boolean matched = Strings.toLowerCase( SchemaUtils.toString( lsd ) )
                    .indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1
                    || Strings.toLowerCase( lsd.getOid() ).indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1;
                return matched;
            }
            return false;
        }
    }

}
