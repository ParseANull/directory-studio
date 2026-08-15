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

package org.apache.directory.studio.schemaeditor.view.dialogs;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ObjectClassSelectionDialogLabelProvider — C-3PO READS THE OC ARCHIVE ──
// After R2 pulls the object-class records out of the Death Star's second filing cabinet,
// C-3PO steps in again to translate the raw data for the humans.
// "Oh my — that record is identified as 'person, organizationalPerson' with registry
// number 2.5.6.6, sir." He picks the right icon (the object-class badge, not the
// attribute-type badge) and assembles the display text from the class's names and OID.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Label provider for the {@link ObjectClassSelectionDialog}'s table viewer.
 * It converts each {@link ObjectClass} object in the list into a display icon and
 * a text string that combines all the class's names with its OID.
 * Think of this class as C-3PO translating R2's object-class data: same technique as
 * he uses for attribute types, just with a different icon and a different source record.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassSelectionDialogLabelProvider extends LabelProvider
{
    // ── Threepio Points to the Object-Class Status Light ─────────────────────
    // "That indicator there — the hexagonal one — that's the object class symbol, sir.
    // Quite distinct from the attribute-type light on the panel next to it."
    // We return the standard object-class icon for any {@link ObjectClass} element,
    // and null for anything we don't recognise.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image to display next to the object class's name in the table.
     * We return the standard object-class icon for any {@link ObjectClass} element,
     * and {@code null} for anything else (which tells JFace to show no image).
     *
     * @param element  the object in the viewer's list; we only handle {@link ObjectClass} instances
     * @return         the object-class {@link Image} from our plugin's image registry, or {@code null}
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof ObjectClass )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
        }

        // Default
        return null;
    }


    // ── Threepio Reads the Object-Class Record Aloud ──────────────────────────
    // "This record is filed under 'person, organizationalPerson' — registry number
    // 2.5.6.6. Shall I read it in full, Master Luke?" C-3PO composes the display
    // string from all available names and the OID, falling back to a "(none) - (OID)"
    // format for nameless records so the human at the keyboard always gets something
    // useful rather than an empty row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for an object class row in the selection table.
     * If the class has one or more names we concatenate them with "/" separators and
     * append the OID in parentheses: {@code "person / organizationalPerson  -  (2.5.6.6)"}.
     * If the class has no names at all, we fall back to a localised "(none)  -  (OID)" format.
     *
     * @param element  the object in the viewer's list; we only handle {@link ObjectClass} instances
     * @return         a human-readable label string, or {@code null} if the element isn't an object class
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof ObjectClass )
        {
            ObjectClass oc = ( ObjectClass ) element;

            List<String> names = oc.getNames();
            if ( ( names != null ) && ( names.size() > 0 ) )
            {
                return ViewUtils.concateAliases( names ) + "  -  (" + oc.getOid() + ")"; //$NON-NLS-1$//$NON-NLS-2$
            }
            else
            {
                return NLS.bind(
                    Messages.getString( "ObjectClassSelectionDialogLabelProvider.None" ), new String[] { oc.getOid() } ); //$NON-NLS-1$
            }
        }

        // Default
        return null;
    }
}
