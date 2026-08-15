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
package org.apache.directory.studio.ldapservers.wizards;


import org.apache.directory.studio.ldapservers.LdapServersPlugin;
import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;


// ── CLASS: LdapServerAdapterExtensionsLabelProvider — THE PARTS CATALOGUE DISPLAY CARD ───
// In the parts catalogue, each vendor section has a folder icon and a name.
// Each part under it has a server icon and a label like "ApacheDS 2.0".
// This label provider supplies exactly that — the right icon and name string for each node
// in the adapter-selection tree in the New Server Wizard.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Label provider for the adapter-selection {@link TreeViewer} in the New Server Wizard.
 * Vendor strings get a folder icon and their name; {@link LdapServerAdapterExtension}s get
 * a server icon and "Name Version" (or just "Name" if the version is blank).
 * Think of it as the display card for each row in the Imperial parts catalogue.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServerAdapterExtensionsLabelProvider extends LabelProvider
{
    // ── Picking The Right Icon For Each Row ──────────────────────────────────────────────────
    // Vendor rows get a folder icon (they're sections); extension rows get the server icon.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for the given tree element.
     * Vendor strings get a folder icon; {@link LdapServerAdapterExtension}s get the server icon.
     *
     * @param element  the tree element (either a vendor String or an LdapServerAdapterExtension)
     * @return the icon image, or {@code null} if the element type is unknown
     */
    public Image getImage( Object element )
    {
        if ( element instanceof String )
        {
            return LdapServersPlugin.getDefault().getImage( LdapServersPluginConstants.IMG_FOLDER );
        }
        else if ( element instanceof LdapServerAdapterExtension )
        {
            return LdapServersPlugin.getDefault().getImage( LdapServersPluginConstants.IMG_SERVER );
        }

        return null;
    }


    // ── Generating The Display Label For Each Row ─────────────────────────────────────────────
    // Vendor rows display as-is: "Apache Software Foundation".
    // Extension rows display "Name Version" (e.g., "ApacheDS 2.0") if a version is present,
    // or just "Name" if the version is blank.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for the given tree element.
     * Vendor strings return themselves; {@link LdapServerAdapterExtension}s return
     * "Name Version" (if version is non-empty) or just "Name".
     *
     * <p>For example — the catalogue row for ApacheDS 2.0:</p>
     * <pre>
     *   extension.getName()    → "ApacheDS"
     *   extension.getVersion() → "2.0"
     *   getText(extension)     → "ApacheDS 2.0"
     * </pre>
     *
     * @param element  the tree element
     * @return the label string
     */
    public String getText( Object element )
    {
        if ( element instanceof String )
        {
            return ( String ) element;

        }
        else if ( element instanceof LdapServerAdapterExtension )
        {
            LdapServerAdapterExtension extension = ( LdapServerAdapterExtension ) element;

            String version = extension.getVersion();

            if ( ( version != null ) && ( !version.equals( "" ) ) ) //$NON-NLS-1$
            {

                return extension.getName() + " " + version; //$NON-NLS-1$
            }
            else
            {
                return extension.getName();
            }
        }

        return super.getText( element );
    }
}
