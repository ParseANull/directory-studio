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


import org.apache.directory.api.ldap.model.schema.MatchingRule;
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


// ── CLASS: MatchingRuleDescriptionPage — Death Star Blueprints, Matching Rules ─
// R2-D2 opens the "Matching Rules" chapter of the blueprints — a catalogue of
// every comparison algorithm the installation knows (caseExactMatch,
// integerMatch, distinguishedNameMatch, …).  Matching rules define how LDAP
// decides whether two attribute values are equal, or how they sort.  This page
// lists them all and drills into details on click.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The schema browser tab that lists all matching rule descriptions from the
 * selected connection's schema.
 * Selecting an entry shows its details in {@link MatchingRuleDescriptionDetailsPage}.
 * Think of this class as R2's matching-rule index: every comparison algorithm
 * the server understands, searchable, sorted, drillable.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MatchingRuleDescriptionPage extends SchemaPage
{

    // ── R2 Attaches The Matching-Rule Index ───────────────────────────────────────
    // R2 connects his matching-rule catalogue to the parent schema browser.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new matching rule description page linked to the given schema browser.
     *
     * @param schemaBrowser  the schema browser editor that hosts this tab
     */
    public MatchingRuleDescriptionPage( SchemaBrowser schemaBrowser )
    {
        super( schemaBrowser );
    }


    /**
     * Returns the localised title for this page.
     *
     * @return the page title, e.g. "Matching Rules"
     */
    @Override
    protected String getTitle()
    {
        return Messages.getString( "MatchingRuleDescriptionPage.MatchingRules" ); //$NON-NLS-1$
    }


    /**
     * Returns the localised description shown below the section title.
     *
     * @return the filter description, e.g. "Select a matching rule"
     */
    @Override
    protected String getFilterDescription()
    {
        return Messages.getString( "MatchingRuleDescriptionPage.SelectMatchingRule" ); //$NON-NLS-1$
    }


    /**
     * Returns the content provider that extracts all matching rule descriptions.
     *
     * @return an {@link MRDContentProvider} instance
     */
    @Override
    protected IStructuredContentProvider getContentProvider()
    {
        return new MRDContentProvider();
    }


    /**
     * Returns the label provider that renders each matching rule as a display string.
     *
     * @return an {@link MRDLabelProvider} instance
     */
    @Override
    protected ITableLabelProvider getLabelProvider()
    {
        return new MRDLabelProvider();
    }


    /**
     * Returns the sorter that orders matching rules alphabetically.
     *
     * @return an {@link MRDViewerSorter} instance
     */
    @Override
    protected ViewerSorter getSorter()
    {
        return new MRDViewerSorter();
    }


    /**
     * Returns the filter that hides non-matching matching rules.
     *
     * @return an {@link MRDViewerFilter} instance
     */
    @Override
    protected ViewerFilter getFilter()
    {
        return new MRDViewerFilter();
    }


    /**
     * Creates and returns the details page for the selected matching rule.
     *
     * @return a new {@link MatchingRuleDescriptionDetailsPage}
     */
    @Override
    protected SchemaDetailsPage getDetailsPage()
    {
        return new MatchingRuleDescriptionDetailsPage( this, this.toolkit );
    }

    /**
     * Supplies the table viewer with matching rule descriptions from the schema.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRDContentProvider implements IStructuredContentProvider
    {
        // ── R2 Pulls The Matching-Rule List From Storage ───────────────────────────
        // R2 reads every matching rule definition from the schema and returns it
        // as an array for the table viewer.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Returns all matching rule descriptions from the given schema input.
         *
         * @param inputElement  the Schema object; other types yield an empty array
         * @return              array of {@link MatchingRule} objects, or empty array
         */
        public Object[] getElements( Object inputElement )
        {
            if ( inputElement instanceof Schema )
            {
                Schema schema = ( Schema ) inputElement;
                if ( schema != null && schema.getMatchingRuleDescriptions() != null )
                {
                    return schema.getMatchingRuleDescriptions().toArray();
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
     * Renders each matching rule as a display string.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRDLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        /**
         * Returns the display text for the given matching rule.
         *
         * @param obj    expected to be a {@link MatchingRule}
         * @param index  column index (always 0)
         * @return       formatted name, or fallback toString
         */
        public String getColumnText( Object obj, int index )
        {
            if ( obj instanceof MatchingRule )
            {
                return SchemaUtils.toString( ( MatchingRule ) obj );
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
     * Sorts matching rule descriptions alphabetically by display name.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRDViewerSorter extends ViewerSorter
    {
        /**
         * Compares two matching rules by formatted display name.
         *
         * @param viewer  the viewer (not used)
         * @param e1      first element
         * @param e2      second element
         * @return        negative, zero, or positive ordering
         */
        public int compare( Viewer viewer, Object e1, Object e2 )
        {
            if ( e1 instanceof MatchingRule )
            {
                e1 = SchemaUtils.toString( ( MatchingRule ) e1 );
            }
            if ( e2 instanceof MatchingRule )
            {
                e2 = SchemaUtils.toString( ( MatchingRule ) e2 );
            }
            return e1.toString().compareTo( e2.toString() );
        }
    }

    /**
     * Hides matching rules that do not match the filter text (name or OID,
     * case-insensitive).
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRDViewerFilter extends ViewerFilter
    {
        /**
         * Returns true if the given matching rule matches the filter text.
         *
         * @param viewer         the viewer (not used)
         * @param parentElement  the parent element (not used)
         * @param element        the schema element to test
         * @return               true to show, false to hide
         */
        public boolean select( Viewer viewer, Object parentElement, Object element )
        {
            if ( element instanceof MatchingRule )
            {
                MatchingRule mrd = ( MatchingRule ) element;
                boolean matched = Strings.toLowerCase( SchemaUtils.toString( mrd ) )
                    .indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1
                    || Strings.toLowerCase( mrd.getOid() ).indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1;
                return matched;
            }
            return false;
        }
    }

}
