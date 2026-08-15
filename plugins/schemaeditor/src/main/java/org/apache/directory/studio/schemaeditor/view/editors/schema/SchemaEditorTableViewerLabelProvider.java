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

package org.apache.directory.studio.schemaeditor.view.editors.schema;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;


// ── CLASS: SchemaEditorTableViewerLabelProvider — C-3PO AT THE EWOK BONFIRE ──
// C-3PO stands at the center of the Ewok celebration bonfire, narrating the
// story of the Rebellion to the tribe — translating each hero's identity into
// something the Ewoks can understand: "This one fought the Empire — she is called
// Leia (ID: 2.5.4.41)." For each character who shows up, C-3PO knows the right
// icon and the right words to use.
// This class does the same for the Schema Editor tables: it knows whether an
// element is an {@link ObjectClass} or an {@link AttributeType}, picks the
// correct plugin icon, and formats the display string as
// "alias1, alias2  -  (OID)" — or falls back to "None (OID)" when no aliases exist.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Label provider for the attribute types and object classes table viewers in the
 * {@link SchemaEditorOverviewPage}.
 * It translates {@link AttributeType} and {@link ObjectClass} model objects into
 * the icon and display string that each table row renders.
 * Think of it as C-3PO at the Ewok bonfire: for every character that appears,
 * he chooses the right emblem and announces the right name.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditorTableViewerLabelProvider extends LabelProvider implements ITableLabelProvider
{
    // ── C-3PO Selects the Right Emblem for Each Hero ──────────────────────────
    // C-3PO knows the Rebel insignia from the Ewok icon — he picks the correct
    // banner for each character stepping into the firelight.
    // Object classes get the object-class icon; attribute types get the
    // attribute-type icon; anything else gets nothing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the icon for the given element's table column.
     * {@link ObjectClass} elements get the object-class icon; {@link AttributeType}
     * elements get the attribute-type icon. Unknown types return {@code null}.
     *
     * @param element      the model object for this table row
     * @param columnIndex  the column index (only one column in these tables)
     * @return             the appropriate plugin {@link Image}, or {@code null} for unknown types
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        if ( element instanceof ObjectClass )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
        }
        else if ( element instanceof AttributeType )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
        }

        // Default
        return null;
    }


    // ── C-3PO Announces Each Hero by Name and Registry Number ─────────────────
    // "This is Luke Skywalker — registration (2.5.4.3)." C-3PO formats each
    // hero's introduction as "aliases  -  (OID)" so the Ewoks know both the
    // common name and the unique identifier. If the hero has no name, C-3PO
    // falls back to "None (OID)" so the slot isn't blank.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the display string for the given element's table column.
     * For an {@link ObjectClass} or {@link AttributeType} with names, we format
     * {@code "alias1, alias2  -  (OID)"}; if the element has no names at all,
     * we fall back to {@code "None (OID)"} via the NLS message bundle.
     *
     * @param element      the model object for this table row
     * @param columnIndex  the column index (only one column in these tables)
     * @return             the human-readable display string, or {@code null} for unknown types
     */
    public String getColumnText( Object element, int columnIndex )
    {
        if ( element instanceof ObjectClass )
        {
            ObjectClass oc = ( ObjectClass ) element;

            List<String> names = oc.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return ViewUtils.concateAliases( names ) + "  -  (" + oc.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                return NLS.bind(
                    Messages.getString( "SchemaEditorTableViewerLabelProvider.None" ), new String[] { oc.getOid() } ); //$NON-NLS-1$
            }
        }
        else if ( element instanceof AttributeType )
        {
            AttributeType at = ( AttributeType ) element;

            List<String> names = at.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return ViewUtils.concateAliases( names ) + "  -  (" + at.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                return NLS.bind(
                    Messages.getString( "SchemaEditorTableViewerLabelProvider.None" ), new String[] { at.getOid() } ); //$NON-NLS-1$
            }
        }

        // Default
        return null;
    }
}
