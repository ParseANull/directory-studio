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


import org.apache.directory.api.ldap.model.schema.AbstractSchemaObject;
import org.apache.directory.api.ldap.model.schema.AttributeType;
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


// ── CLASS: AttributeTypeDescriptionPage — Death Star Blueprints, Attribute Types ─
// R2-D2 opens the "Attribute Types" section of the Death Star blueprints — a
// master index of every named data field the installation can hold (cn, mail,
// objectClass, …), searchable and alphabetically sorted.  Click any entry and the
// right-hand panel lights up with that field's full specification.  This class is
// exactly that: the "Attribute Types" tab of the schema browser, with an inner
// content provider pulling the full list from the schema, a label provider turning
// each AttributeType into a readable string, a sorter keeping the list alphabetical,
// and a filter narrowing the list as the user types.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The schema browser tab that lists all attribute type descriptions from the
 * selected LDAP connection's schema.
 * Selecting an entry in the list shows its details in the right-hand
 * {@link AttributeTypeDescriptionDetailsPage}.
 * Think of this class as R2 indexing the attribute-type section of the Death Star
 * blueprints: every named field the server knows about, filter-searchable,
 * alphabetically sorted, with a click-to-drill-down detail panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeDescriptionPage extends SchemaPage
{

    // ── R2 Attaches The Attribute-Type Index To The Browser ───────────────────────
    // R2 connects his attribute-type blueprint section to the main schema browser
    // so the two stay coordinated.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new attribute type page linked to the given schema browser.
     *
     * @param schemaBrowser  the schema browser editor that hosts this tab
     */
    public AttributeTypeDescriptionPage( SchemaBrowser schemaBrowser )
    {
        super( schemaBrowser );
    }


    // ── R2 Labels The Attribute-Type Section ──────────────────────────────────────
    // The blueprint section header reads "Attribute Types" so technicians know
    // which index they are browsing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised title shown in the section header and form title.
     *
     * @return the page title, e.g. "Attribute Types"
     */
    @Override
    protected String getTitle()
    {
        return Messages.getString( "AttributeTypeDescriptionPage.AttributeTypes" ); //$NON-NLS-1$
    }


    // ── R2 Provides A Filter Hint For The Index ───────────────────────────────────
    // Below the title R2 shows a brief instruction so the user knows what the
    // filter field is for.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised description shown below the section title,
     * hinting that the filter field narrows the attribute type list.
     *
     * @return the filter description, e.g. "Select an attribute type"
     */
    @Override
    protected String getFilterDescription()
    {
        return Messages.getString( "AttributeTypeDescriptionPage.SelectAttributeType" ); //$NON-NLS-1$
    }


    // ── R2 Supplies The Attribute-Type Catalogue ──────────────────────────────────
    // The content provider is the mechanism that pulls the full list of attribute
    // types out of the Schema object and hands them to the table viewer.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the content provider that extracts all attribute type descriptions
     * from the schema and supplies them to the table viewer.
     *
     * @return an {@link ATDContentProvider} instance
     */
    @Override
    protected IStructuredContentProvider getContentProvider()
    {
        return new ATDContentProvider();
    }


    // ── R2 Supplies The Display Labels ────────────────────────────────────────────
    // The label provider turns each AttributeType object into the display text
    // shown in the table row.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label provider that renders each attribute type as a display
     * string in the table viewer.
     *
     * @return an {@link ATDLabelProvider} instance
     */
    @Override
    protected ITableLabelProvider getLabelProvider()
    {
        return new ATDLabelProvider();
    }


    // ── R2 Sorts The Attribute-Type Catalogue Alphabetically ──────────────────────
    // The catalogue is sorted A-to-Z so the technician can find any attribute
    // quickly without scrolling through an arbitrary ordering.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sorter that orders attribute types alphabetically by display name.
     *
     * @return an {@link ATDViewerSorter} instance
     */
    @Override
    protected ViewerSorter getSorter()
    {
        return new ATDViewerSorter();
    }


    // ── R2 Narrows The Catalogue By The Filter Text ───────────────────────────────
    // Only attribute types whose name or OID contains the filter text remain
    // visible in the list when the user is typing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the viewer filter that hides attribute types not matching the text
     * the user typed in the filter field (matched against name and OID).
     *
     * @return an {@link ATDViewerFilter} instance
     */
    @Override
    protected ViewerFilter getFilter()
    {
        return new ATDViewerFilter();
    }


    // ── R2 Creates The Detail Readout For The Selected Attribute Type ──────────────
    // When the user clicks an entry in the index, the right-hand readout panel
    // shows the full specification of that attribute type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns the details page that shows the full specification of a
     * selected attribute type on the right-hand side of the master/detail split.
     *
     * @return a new {@link AttributeTypeDescriptionDetailsPage}
     */
    @Override
    protected SchemaDetailsPage getDetailsPage()
    {
        return new AttributeTypeDescriptionDetailsPage( this, this.toolkit );
    }

    // ── R2 Reads The Attribute-Type Index From The Schema ─────────────────────────
    // R2 opens the schema storage and extracts every attribute type description
    // into an array that the table viewer can iterate over.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Supplies the table viewer with attribute type descriptions from the Schema.
     * Returns an empty array if the input is not a Schema or is null.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class ATDContentProvider implements IStructuredContentProvider
    {
        // ── R2 Extracts The Attribute-Type Entries ─────────────────────────────────
        // R2 reaches into the schema storage and pulls out the full collection of
        // attribute type descriptions, converting it to an array for the viewer.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Returns all attribute type descriptions from the given schema input.
         *
         * <p>For example — R2 pulls the attribute list:</p>
         * <pre>
         *   getElements(schema); // [ cn, mail, objectClass, ... ]
         * </pre>
         *
         * @param inputElement  the Schema object set on the viewer; other types yield empty array
         * @return              array of {@link AttributeType} objects, or empty array
         */
        public Object[] getElements( Object inputElement )
        {
            if ( inputElement instanceof Schema )
            {
                Schema schema = ( Schema ) inputElement;
                if ( schema != null )
                {
                    return schema.getAttributeTypeDescriptions().toArray();
                }
            }
            return new Object[0];
        }


        /**
         * No-op: we hold no resources.
         */
        public void dispose()
        {
        }


        /**
         * No-op: we do not need to react to input changes.
         *
         * @param viewer    the viewer
         * @param oldInput  the previous input
         * @param newInput  the new input
         */
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }
    }

    // ── R2 Renders Each Attribute-Type Name For Display ───────────────────────────
    // The label provider reads each AttributeType object and returns the string
    // that will appear in the table row — typically the human-readable name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Renders each attribute type as a display string in the table viewer.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class ATDLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        // ── R2 Reads The Name Off Each Attribute-Type Entry ────────────────────────
        // R2 reads the entry label from his catalogue — the formatted name or OID
        // of the attribute type — for rendering in the table cell.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Returns the display text for the given attribute type.
         *
         * @param obj    the schema object; expected to be an {@link AttributeType}
         * @param index  the column index (only one column, so always 0)
         * @return       the formatted attribute type name, or {@code obj.toString()} fallback
         */
        public String getColumnText( Object obj, int index )
        {
            if ( obj instanceof AttributeType )
            {
                return SchemaUtils.toString( ( AbstractSchemaObject ) obj );
            }
            return obj.toString();
        }


        /**
         * Returns null; we use text-only rows in this table.
         *
         * @param obj    the schema object
         * @param index  the column index
         * @return       null always
         */
        public Image getColumnImage( Object obj, int index )
        {
            return null;
        }
    }

    // ── R2 Sorts The Attribute-Type Index A-to-Z ──────────────────────────────────
    // R2 sorts the catalogue entries alphabetically by their display name so the
    // technician can scan the index in a predictable order.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sorts attribute type descriptions alphabetically by their display name.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class ATDViewerSorter extends ViewerSorter
    {
        // ── R2 Compares Two Attribute-Type Names ────────────────────────────────────
        // R2 compares two entries from the catalogue — "cn" vs "mail" — and returns
        // the one that comes first alphabetically.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Compares two attribute types by their formatted display name (case-sensitive
         * string comparison via {@link String#compareTo}).
         *
         * @param viewer  the viewer (not used)
         * @param e1      the first element; converted to display string if AttributeType
         * @param e2      the second element; converted to display string if AttributeType
         * @return        negative, zero, or positive as e1 is less than, equal to, or greater than e2
         */
        public int compare( Viewer viewer, Object e1, Object e2 )
        {
            if ( e1 instanceof AttributeType )
            {
                e1 = SchemaUtils.toString( ( AbstractSchemaObject ) e1 );
            }
            if ( e2 instanceof AttributeType )
            {
                e2 = SchemaUtils.toString( ( AbstractSchemaObject ) e2 );
            }
            return e1.toString().compareTo( e2.toString() );
        }
    }

    // ── R2 Hides Non-Matching Entries ─────────────────────────────────────────────
    // R2 checks each catalogue entry against the filter text the user typed and
    // hides any that do not match by name or OID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Hides attribute types that do not match the text in the filter field.
     * We match against both the human-readable name and the numeric OID,
     * case-insensitively, so the user can filter either way.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class ATDViewerFilter extends ViewerFilter
    {
        // ── R2 Checks Whether An Entry Matches The Filter ──────────────────────────
        // R2 checks the entry's name and OID against the filter text — if neither
        // contains the typed string, the entry is hidden from the index.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Returns true if the given attribute type matches the filter text.
         * Matching is case-insensitive and checks both the display name and the OID.
         *
         * @param viewer         the viewer (not used directly)
         * @param parentElement  the parent element (not used)
         * @param element        the schema element to test
         * @return               true if the element should be shown, false to hide it
         */
        public boolean select( Viewer viewer, Object parentElement, Object element )
        {
            if ( element instanceof AttributeType )
            {
                AttributeType atd = ( AttributeType ) element;
                boolean matched = Strings.toLowerCase( SchemaUtils.toString( atd ) )
                    .indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1
                    || Strings.toLowerCase( atd.getOid() ).indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1;
                return matched;
            }
            return false;
        }
    }

}
