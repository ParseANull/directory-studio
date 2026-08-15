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

package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ATEUsedByTablesLabelProvider — C-3PO ANNOUNCES THE GUEST LIST ─────────────
// After R2-D2 collects the list of object classes, C-3PO takes over: he stands at the
// door of the conference room and announces each guest as they enter — "Ah yes,
// 'organizationalPerson', arriving with the standard object-class icon and the name
// 'organizationalPerson'."  He knows both how to show an image (the icon) and how to
// read a name aloud (the text).
// This label provider does that: it provides the object-class icon and the concatenated
// alias string for each row in the "Used As Mandatory" and "Used As Optional" tables on
// the Attribute Type Editor's "Used By" page.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * JFace ITableLabelProvider that renders rows in the "Used By" tables of the Attribute
 * Type Editor.
 * Each row is an {@link ObjectClass}; we provide an object-class icon for the image
 * column and the concatenated aliases for the text column.  Both tables share this
 * same provider because they show the same kind of data in the same format.
 * Think of C-3PO: he knows how to introduce each guest — icon first, then name.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATEUsedByTablesLabelProvider extends LabelProvider implements ITableLabelProvider
{
    // ── C-3PO Fetches the Guest's Portrait ──────────────────────────────────────────
    // Before announcing each guest by name, C-3PO holds up their portrait for the room
    // to see — the standard object-class icon from the plugin's image registry.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image for a table row.
     * We always return the standard object-class icon for {@link ObjectClass} elements;
     * any other element type gets null (no image), which is the JFace default.
     * The {@code columnIndex} parameter is ignored here because we only have one logical
     * column for images.
     *
     * <p>For example — C-3PO holds up the portrait:</p>
     * <pre>
     *   // element = ObjectClass "person" → returns the object-class icon image
     *   // element = something weird → returns null
     * </pre>
     *
     * @param element      the table row object — expected to be an {@link ObjectClass}
     * @param columnIndex  the column index (unused — we show the same icon regardless)
     * @return             the object-class icon Image, or null if element is not an
     *                     ObjectClass
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        if ( element instanceof ObjectClass )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
        }

        // Default
        return null;
    }


    // ── C-3PO Announces the Guest's Name ─────────────────────────────────────────────
    // C-3PO reads out the guest's full name — all their aliases concatenated — so
    // the room knows exactly who has arrived.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for a table row.
     * For an {@link ObjectClass} we concatenate all its aliases using
     * {@link ViewUtils#concateAliases} (which joins them with commas, e.g.
     * "person, Person").  Any other element type gets null.
     *
     * <p>For example — C-3PO announces the name:</p>
     * <pre>
     *   // element = ObjectClass names=["person", "Person"]
     *   //   → "person, Person"
     * </pre>
     *
     * @param element      the table row object — expected to be an {@link ObjectClass}
     * @param columnIndex  the column index (unused — only one text column)
     * @return             the concatenated alias string, or null if element is not an
     *                     ObjectClass
     */
    public String getColumnText( Object element, int columnIndex )
    {
        if ( element instanceof ObjectClass )
        {
            return ViewUtils.concateAliases( ( ( ObjectClass ) element ).getNames() );
        }

        // Default
        return null;
    }
}
