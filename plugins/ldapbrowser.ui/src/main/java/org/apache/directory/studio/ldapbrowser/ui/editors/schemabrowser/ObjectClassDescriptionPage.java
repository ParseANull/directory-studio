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


import org.apache.directory.api.ldap.model.schema.ObjectClass;
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


// ── CLASS: ObjectClassDescriptionPage — Death Star Blueprints, Object Classes ──
// R2-D2 opens the first and most important chapter of the blueprints: "Object
// Classes."  Every LDAP entry must declare its object classes — they define the
// entry's type (person, organizationalUnit, device, …) and determine which
// attribute types are required or permitted.  This is the default tab when the
// schema browser opens, because object classes are the natural starting point for
// understanding a directory's structure.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The schema browser tab that lists all object class descriptions from the
 * selected connection's schema.
 * This is the first tab shown when the schema browser opens.
 * Selecting an entry shows its details in {@link ObjectClassDescriptionDetailsPage}.
 * Think of this class as R2's object-class index: the master list of every
 * entry type the server knows, searchable, sorted, drillable on click.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassDescriptionPage extends SchemaPage
{

    // ── R2 Attaches The Object-Class Index To The Browser ─────────────────────────
    // R2 links his object-class catalogue — the first and primary blueprint section —
    // to the parent schema browser.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new object class description page linked to the given schema browser.
     *
     * @param schemaBrowser  the schema browser editor that hosts this tab
     */
    public ObjectClassDescriptionPage( SchemaBrowser schemaBrowser )
    {
        super( schemaBrowser );
    }


    /**
     * Returns the localised title for this page.
     *
     * @return the page title, e.g. "Object Classes"
     */
    @Override
    protected String getTitle()
    {
        return Messages.getString( "ObjectClassDescriptionPage.ObjectClasses" ); //$NON-NLS-1$
    }


    /**
     * Returns the localised description shown below the section title.
     *
     * @return the filter description, e.g. "Select an object class"
     */
    @Override
    protected String getFilterDescription()
    {
        return Messages.getString( "ObjectClassDescriptionPage.SelectObjectClass" ); //$NON-NLS-1$
    }


    /**
     * Returns the content provider that extracts all object class descriptions.
     *
     * @return an {@link OCDContentProvider} instance
     */
    @Override
    protected IStructuredContentProvider getContentProvider()
    {
        return new OCDContentProvider();
    }


    /**
     * Returns the label provider that renders each object class as a display string.
     *
     * @return an {@link OCDLabelProvider} instance
     */
    @Override
    protected ITableLabelProvider getLabelProvider()
    {
        return new OCDLabelProvider();
    }


    /**
     * Returns the sorter that orders object classes alphabetically.
     *
     * @return an {@link OCDViewerSorter} instance
     */
    @Override
    protected ViewerSorter getSorter()
    {
        return new OCDViewerSorter();
    }


    /**
     * Returns the filter that hides non-matching object classes.
     *
     * @return an {@link OCDViewerFilter} instance
     */
    @Override
    protected ViewerFilter getFilter()
    {
        return new OCDViewerFilter();
    }


    /**
     * Creates and returns the details page for the selected object class.
     *
     * @return a new {@link ObjectClassDescriptionDetailsPage}
     */
    @Override
    protected SchemaDetailsPage getDetailsPage()
    {
        return new ObjectClassDescriptionDetailsPage( this, this.toolkit );
    }

    /**
     * Supplies the table viewer with object class descriptions from the schema.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class OCDContentProvider implements IStructuredContentProvider
    {
        // ── R2 Pulls The Object-Class List From The Schema ─────────────────────────
        // R2 reads every object class definition from the schema and converts the
        // collection to an array the table viewer can iterate.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Returns all object class descriptions from the given schema input.
         *
         * @param inputElement  the Schema object; other types yield an empty array
         * @return              array of {@link ObjectClass} objects, or empty array
         */
        public Object[] getElements( Object inputElement )
        {
            if ( inputElement instanceof Schema )
            {
                Schema schema = ( Schema ) inputElement;
                if ( schema != null )
                {
                    return schema.getObjectClassDescriptions().toArray();
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
     * Renders each object class as a display string.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class OCDLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        /**
         * Returns the display text for the given object class.
         *
         * @param obj    expected to be an {@link ObjectClass}
         * @param index  column index (always 0)
         * @return       formatted name, or fallback toString
         */
        public String getColumnText( Object obj, int index )
        {
            if ( obj instanceof ObjectClass )
            {
                return SchemaUtils.toString( ( ObjectClass ) obj );
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
     * Sorts object class descriptions alphabetically by display name.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class OCDViewerSorter extends ViewerSorter
    {
        /**
         * Compares two object classes by formatted display name.
         *
         * @param viewer  the viewer (not used)
         * @param e1      first element
         * @param e2      second element
         * @return        negative, zero, or positive ordering
         */
        public int compare( Viewer viewer, Object e1, Object e2 )
        {
            if ( e1 instanceof ObjectClass )
            {
                e1 = SchemaUtils.toString( ( ObjectClass ) e1 );
            }
            if ( e2 instanceof ObjectClass )
            {
                e2 = SchemaUtils.toString( ( ObjectClass ) e2 );
            }
            return e1.toString().compareTo( e2.toString() );
        }
    }

    /**
     * Hides object classes that do not match the filter text (name or OID,
     * case-insensitive).
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class OCDViewerFilter extends ViewerFilter
    {
        /**
         * Returns true if the given object class matches the filter text.
         *
         * @param viewer         the viewer (not used)
         * @param parentElement  the parent element (not used)
         * @param element        the schema element to test
         * @return               true to show, false to hide
         */
        public boolean select( Viewer viewer, Object parentElement, Object element )
        {
            if ( element instanceof ObjectClass )
            {
                ObjectClass ocd = ( ObjectClass ) element;
                boolean matched = Strings.toLowerCase( SchemaUtils.toString( ocd ) )
                    .indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1
                    || Strings.toLowerCase( ocd.getOid() ).indexOf( Strings.toLowerCase( filterText.getText() ) ) != -1;
                return matched;
            }
            return false;
        }
    }

}
