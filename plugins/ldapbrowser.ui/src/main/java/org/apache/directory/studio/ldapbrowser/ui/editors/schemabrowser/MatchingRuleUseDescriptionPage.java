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


import org.apache.directory.api.ldap.model.schema.MatchingRuleUse;
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


// ── CLASS: MatchingRuleUseDescriptionPage — Death Star Blueprints, MR-Use ──────
// R2-D2 opens the "Matching Rule Use" appendix — a narrower section that tells
// which attribute types each matching rule is actually applied to on this specific
// installation.  It is the cross-reference table that links abstract algorithms
// to the concrete fields that use them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The schema browser tab that lists all matching rule use descriptions from the
 * selected connection's schema.
 * Selecting an entry shows its details in {@link MatchingRuleUseDescriptionDetailsPage}.
 * Think of this class as R2's matching-rule-use appendix: the cross-reference
 * table that says "caseExactMatch applies to these attributes on this server."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MatchingRuleUseDescriptionPage extends SchemaPage
{

    // ── R2 Attaches The Matching-Rule-Use Appendix ────────────────────────────────
    // R2 links his matching-rule-use cross-reference section to the parent browser.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new matching rule use description page linked to the given schema browser.
     *
     * @param schemaBrowser  the schema browser editor that hosts this tab
     */
    public MatchingRuleUseDescriptionPage( SchemaBrowser schemaBrowser )
    {
        super( schemaBrowser );
    }


    /**
     * Returns the localised title for this page.
     *
     * @return the page title, e.g. "Matching Rule Use"
     */
    @Override
    protected String getTitle()
    {
        return Messages.getString( "MatchingRuleUseDescriptionPage.MatchingRule" ); //$NON-NLS-1$
    }


    /**
     * Returns the localised description shown below the section title.
     *
     * @return the filter description, e.g. "Select a matching rule use"
     */
    @Override
    protected String getFilterDescription()
    {
        return Messages.getString( "MatchingRuleUseDescriptionPage.SelectMatchingRule" ); //$NON-NLS-1$
    }


    /**
     * Returns the content provider that extracts all matching rule use descriptions.
     *
     * @return an {@link MRUDContentProvider} instance
     */
    @Override
    protected IStructuredContentProvider getContentProvider()
    {
        return new MRUDContentProvider();
    }


    /**
     * Returns the label provider that renders each matching rule use as a display string.
     *
     * @return an {@link MRUDLabelProvider} instance
     */
    @Override
    protected ITableLabelProvider getLabelProvider()
    {
        return new MRUDLabelProvider();
    }


    /**
     * Returns the sorter that orders matching rule use descriptions alphabetically.
     *
     * @return an {@link MRUDViewerSorter} instance
     */
    @Override
    protected ViewerSorter getSorter()
    {
        return new MRUDViewerSorter();
    }


    /**
     * Returns the filter that hides non-matching entries.
     *
     * @return an {@link MRUDViewerFilter} instance
     */
    @Override
    protected ViewerFilter getFilter()
    {
        return new MRUDViewerFilter();
    }


    /**
     * Creates and returns the details page for the selected matching rule use.
     *
     * @return a new {@link MatchingRuleUseDescriptionDetailsPage}
     */
    @Override
    protected SchemaDetailsPage getDetailsPage()
    {
        return new MatchingRuleUseDescriptionDetailsPage( this, this.toolkit );
    }

    /**
     * Supplies the table viewer with matching rule use descriptions from the schema.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRUDContentProvider implements IStructuredContentProvider
    {
        // ── R2 Pulls The Matching-Rule-Use Cross-Reference From Storage ────────────
        // R2 reads every matching-rule-use entry from the schema and returns them
        // as an array for the table viewer.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Returns all matching rule use descriptions from the given schema input.
         *
         * @param inputElement  the Schema object; other types yield an empty array
         * @return              array of {@link MatchingRuleUse} objects, or empty array
         */
        public Object[] getElements( Object inputElement )
        {
            if ( inputElement instanceof Schema )
            {
                Schema schema = ( Schema ) inputElement;
                if ( schema != null && schema.getMatchingRuleUseDescriptions() != null )
                {
                    return schema.getMatchingRuleUseDescriptions().toArray();
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
     * Renders each matching rule use as a display string.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRUDLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        /**
         * Returns the display text for the given matching rule use.
         *
         * @param obj    expected to be a {@link MatchingRuleUse}
         * @param index  column index (always 0)
         * @return       formatted name, or fallback toString
         */
        public String getColumnText( Object obj, int index )
        {
            if ( obj instanceof MatchingRuleUse )
            {
                return SchemaUtils.toString( ( MatchingRuleUse ) obj );
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
     * Sorts matching rule use descriptions alphabetically by display name.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRUDViewerSorter extends ViewerSorter
    {
        /**
         * Compares two matching rule use descriptions by formatted display name.
         *
         * @param viewer  the viewer (not used)
         * @param e1      first element
         * @param e2      second element
         * @return        negative, zero, or positive ordering
         */
        public int compare( Viewer viewer, Object e1, Object e2 )
        {
            if ( e1 instanceof MatchingRuleUse )
            {
                e1 = SchemaUtils.toString( ( MatchingRuleUse ) e1 );
            }
            if ( e2 instanceof MatchingRuleUse )
            {
                e2 = SchemaUtils.toString( ( MatchingRuleUse ) e2 );
            }
            return e1.toString().compareTo( e2.toString() );
        }
    }

    /**
     * Hides matching rule use descriptions that do not match the filter text
     * (name or OID, case-insensitive).
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class MRUDViewerFilter extends ViewerFilter
    {
        /**
         * Returns true if the given matching rule use matches the filter text.
         *
         * @param viewer         the viewer (not used)
         * @param parentElement  the parent element (not used)
         * @param element        the schema element to test
         * @return               true to show, false to hide
         */
        public boolean select( Viewer viewer, Object parentElement, Object element )
        {
            if ( element instanceof MatchingRuleUse )
            {
                MatchingRuleUse mrud = ( MatchingRuleUse ) element;
                boolean matched = Strings.toLowerCase( SchemaUtils.toString( mrud ) ).indexOf(
                    Strings.toLowerCase( filterText.getText() ) ) != -1
                    || Strings.toLowerCase( mrud.getOid() ).indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1;
                return matched;
            }
            return false;
        }
    }

}
