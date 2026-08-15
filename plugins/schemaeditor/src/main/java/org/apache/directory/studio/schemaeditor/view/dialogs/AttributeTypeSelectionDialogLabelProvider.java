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

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;


// ── CLASS: AttributeTypeSelectionDialogLabelProvider — C-3PO TRANSLATING FOR R2 ─
// After R2-D2 extracts raw binary data from the Death Star's computer, C-3PO steps
// in and translates it into something the humans on the Tantive IV can actually read.
// R2 hands over machine-readable records; Threepio converts them to protocol-standard
// speech: names, identifiers, a friendly icon on the wall panel.
// We play Threepio here: the content provider hands us raw {@link AttributeType} objects,
// and we convert each one into the human-readable label ("cn, commonName  -  (2.5.4.3)")
// and the right icon that the dialog's table viewer can render on screen.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Label provider for the {@link AttributeTypeSelectionDialog}'s table viewer.
 * It converts each {@link AttributeType} object in the list into a display icon
 * and a text string that combines all the type's names with its OID.
 * Think of this class as C-3PO: it takes whatever the content provider digs up
 * and translates it into a language the viewer — and the human behind the keyboard — can understand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeSelectionDialogLabelProvider extends LabelProvider
{
    // ── Threepio Points to the Right Icon on the Panel ───────────────────────
    // C-3PO gestures to the correct status light on the wall: "That blinking indicator
    // there — that's the attribute type symbol, sir." He always returns the same
    // icon for attribute types, and null for anything he doesn't recognise.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image to display next to the attribute type's name in the table.
     * We return the standard attribute-type icon for any {@link AttributeType} element,
     * and {@code null} for anything else (which tells JFace to show no image).
     *
     * @param element  the object in the viewer's list; we only handle {@link AttributeType} instances
     * @return         the attribute-type {@link Image} from our plugin's image registry, or {@code null}
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof AttributeType )
        {
            return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
        }

        // Default
        return null;
    }


    // ── Threepio Reads the File Header Aloud ─────────────────────────────────
    // "I'm fluent in over six million forms of communication." C-3PO reads the
    // attribute type record and announces its names and its numeric OID identifier
    // in a format the crew can parse at a glance: "cn, commonName  -  (2.5.4.3)".
    // If the record has no names at all, he announces it by OID only, noting
    // that no aliases are on file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for an attribute type row in the selection table.
     * If the type has one or more names we concatenate them with "/" separators and
     * append the OID in parentheses: {@code "cn / commonName  -  (2.5.4.3)"}.
     * If the type has no names at all — which can happen with schema elements defined
     * only by OID — we fall back to a localised "(none)  -  (OID)" format.
     *
     * @param element  the object in the viewer's list; we only handle {@link AttributeType} instances
     * @return         a human-readable label string, or {@code null} if the element isn't an attribute type
     */
    @Override
    public String getText( Object element )
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
                        Messages.getString( "AttributeTypeSelectionDialogLabelProvider.None" ), new String[] { at.getOid() } ); //$NON-NLS-1$
            }
        }

        // Default
        return null;
    }
}
