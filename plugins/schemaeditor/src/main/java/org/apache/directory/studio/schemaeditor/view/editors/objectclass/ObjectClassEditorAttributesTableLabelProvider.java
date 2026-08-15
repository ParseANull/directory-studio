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

package org.apache.directory.studio.schemaeditor.view.editors.objectclass;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingAttributeType;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ObjectClassEditorAttributesTableLabelProvider — C-3PO AT BRIGHT TREE VILLAGE ──
// C-3PO stands before the Ewoks at Bright Tree Village, translating Luke's
// mission details — complicated technical concepts — into the Ewok dialect so
// the audience actually understands what's being discussed.
// This class is our C-3PO: given an {@link AttributeType} or a
// {@link NonExistingAttributeType} object, it produces the icon and the
// human-readable string that the attributes table column should display.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Label provider for the Mandatory and Optional Attributes tables in the
 * {@link ObjectClassEditorOverviewPage}.
 * It translates {@link AttributeType} and {@link NonExistingAttributeType} objects
 * into the icon and text that the SWT table column renders.
 * Think of it as C-3PO at Bright Tree Village — taking technical schema objects
 * and presenting them in a language the user can read at a glance.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassEditorAttributesTableLabelProvider extends LabelProvider implements ITableLabelProvider
{
    // ── C-3PO Selects the Right Emblem ────────────────────────────────────────
    // C-3PO knows which symbol to display for each type of entity — a Rebel
    // insignia for known allies, a question mark for unknowns.
    // We return the attribute-type icon for known and unknown attribute types,
    // and null for anything we don't recognize (the table shows nothing then).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the icon to display in the table column for the given element.
     * Both resolved {@link AttributeType} objects and unresolved
     * {@link NonExistingAttributeType} stubs get the attribute-type icon,
     * so the user can tell at a glance that each row represents an attribute type.
     *
     * @param element      the model object for this table row
     * @param columnIndex  the column index (only one column, so always 0 here)
     * @return             the attribute-type {@link Image}, or {@code null} for unknown types
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        if ( ( element instanceof AttributeType ) || ( element instanceof NonExistingAttributeType ) )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
        }

        // Default
        return null;
    }


    // ── C-3PO Speaks the Right Phrase ─────────────────────────────────────────
    // C-3PO finds the right phrase in Ewok: "Aliases - (OID)" for known
    // attribute types, the display name for unresolvable stubs.
    // If an AttributeType has no names at all, C-3PO admits it — "None (OID)".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the display string for the given element's table column.
     * For a fully resolved {@link AttributeType} with names, we show
     * {@code "alias1, alias2  -  (OID)"}; if it has no names, we show
     * {@code "None (OID)"} via the NLS message bundle.
     * For a {@link NonExistingAttributeType} stub, we show its display name
     * (which is usually the raw OID or name that couldn't be resolved).
     *
     * @param element      the model object for this table row
     * @param columnIndex  the column index (only one column here)
     * @return             the human-readable display string, or {@code null} for unknown types
     */
    public String getColumnText( Object element, int columnIndex )
    {
        if ( element instanceof AttributeType )
        {
            AttributeType at = ( AttributeType ) element;

            List<String> names = at.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return ViewUtils.concateAliases( names ) + "  -  (" + at.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                return NLS
                    .bind(
                        Messages.getString( "ObjectClassEditorAttributesTableLabelProvider.none" ), new String[] { at.getOid() } ); //$NON-NLS-1$
            }
        }
        else if ( element instanceof NonExistingAttributeType )
        {
            return ( ( NonExistingAttributeType ) element ).getDisplayName();
        }

        // Default
        return null;
    }
}
