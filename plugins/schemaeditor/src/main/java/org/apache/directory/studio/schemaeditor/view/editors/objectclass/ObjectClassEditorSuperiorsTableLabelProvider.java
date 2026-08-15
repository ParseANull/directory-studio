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

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingObjectClass;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ObjectClassEditorSuperiorsTableLabelProvider — C-3PO IN JABBA'S COURT ──
// C-3PO stands in Jabba's throne room, fluent in over six million forms
// of communication, translating the identities and intentions of each
// party in the court into plain Basic so everyone can follow along.
// This class is our C-3PO for the Superiors table: it converts each
// {@link ObjectClass} or {@link NonExistingObjectClass} row object into
// the icon and display string the SWT table column should render.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Label provider for the Superiors table in the {@link ObjectClassEditorOverviewPage}.
 * It translates {@link ObjectClass} and {@link NonExistingObjectClass} objects into
 * the icon and text string that the table column renders for each row.
 * Think of it as C-3PO in Jabba's court: whatever form the superior class takes,
 * it gets translated into something the user can read at a glance.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassEditorSuperiorsTableLabelProvider extends LabelProvider implements ITableLabelProvider
{
    // ── C-3PO Picks the Right Symbol for Each Party ───────────────────────────
    // C-3PO knows which banner to display for each faction in Jabba's court —
    // all recognized parties get the object-class icon so the user knows
    // they're looking at an object class (known or unknown).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the icon to display in the Superiors table column for the given element.
     * Both resolved {@link ObjectClass} objects and unresolved {@link NonExistingObjectClass}
     * stubs get the object-class icon, so the user can see at a glance that each
     * row represents a superior (parent) object class.
     *
     * @param element      the model object for this table row
     * @param columnIndex  the column index (only one column here)
     * @return             the object-class {@link Image}, or {@code null} for unknown types
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        if ( ( element instanceof ObjectClass ) || ( element instanceof NonExistingObjectClass ) )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
        }

        // Default
        return null;
    }


    // ── C-3PO Speaks the Right Introduction ───────────────────────────────────
    // C-3PO announces each party in Jabba's court: "His Excellency, {name} — ({OID})"
    // for known parties, or the raw display name for unknown parties whose records
    // can't be found in his database.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the display string for the given element's table column.
     * For a resolved {@link ObjectClass} with names, we show {@code "alias1, alias2  -  (OID)"};
     * if it has no names, we show {@code "None (OID)"} via the NLS message bundle.
     * For a {@link NonExistingObjectClass} stub, we show its display name (usually the
     * raw OID or name string that couldn't be resolved in the current schema set).
     *
     * @param element      the model object for this table row
     * @param columnIndex  the column index (only one column here)
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
                return NLS
                    .bind(
                        Messages.getString( "ObjectClassEditorSuperiorsTableLabelProvider.None" ), new String[] { oc.getOid() } ); //$NON-NLS-1$
            }
        }
        else if ( element instanceof NonExistingObjectClass )
        {
            return ( ( NonExistingObjectClass ) element ).getDisplayName();
        }

        // Default
        return null;
    }
}
