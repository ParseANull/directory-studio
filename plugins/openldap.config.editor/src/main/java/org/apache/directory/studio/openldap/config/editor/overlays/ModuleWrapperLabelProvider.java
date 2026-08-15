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
package org.apache.directory.studio.openldap.config.editor.overlays;

import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

// ── CLASS: ModuleWrapperLabelProvider — Luke Sees the Big Picture ──────────
// Luke stands at the edge of the Tatooine desert at sunset, two suns dropping
// below the horizon, and takes in the whole landscape at once.  That sweeping
// view is what a JFace TableViewer needs too: given any object in its input,
// it must quickly render a readable label and an icon so the user can scan the
// list at a glance.  This label provider is Luke's vantage point — it takes
// a raw ModuleWrapper and turns it into the text and image the viewer
// paints in each row.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace LabelProvider that converts ModuleWrapper objects into the text and
 * icon displayed in the Overlays page module table viewer.
 * Without a label provider the viewer would just call toString() on every
 * element and show no icons; this class makes the table readable.
 * Think of it as Luke's moment of clarity at sunset — it gives the UI the
 * panoramic view of each module that the user needs to identify entries quickly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModuleWrapperLabelProvider extends LabelProvider
{
    // ── getText — Luke Reads the Horizon ──────────────────────────────────────
    // Luke stands at the Lars homestead, shading his eyes, and reads the whole
    // landscape — he can name every feature from the distant mesa to the nearby
    // moisture vaporators.  The viewer calls getText on every row to decide what
    // to print; we hand back the module's full path+name string so the user can
    // read it just as easily as Luke reads Tatooine's skyline.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for a module row in the table viewer.
     * We delegate to {@link ModuleWrapper#getModulePathName()} to get a
     * "path/{order}name" string; for any non-ModuleWrapper element we fall
     * back to the parent LabelProvider's default.
     *
     * <p>For example — Luke takes in the full view:</p>
     * <pre>
     *   element = ModuleWrapper("back_mdb", path="/usr/lib/ldap", order=0)
     *   getText(element) → "/usr/lib/ldap/{0}back_mdb"
     * </pre>
     *
     * @param element  the viewer row object, expected to be a ModuleWrapper
     * @return         the text string to paint in the table cell
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof ModuleWrapper )
        {
            return ( ( ModuleWrapper ) element ).getModulePathName();
        }

        return super.getText( element );
    }


    // ── getImage — Luke Spots the Familiar Silhouette ─────────────────────────
    // At sunset Luke recognizes the silhouette of the homestead by its shape
    // alone — even before reading the label.  Icons work the same way for users
    // scanning a long list.  We return the database icon for every ModuleWrapper
    // so the row is identifiable at a glance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image for a module row in the table viewer.
     * We use the database image from the plugin's image registry because
     * modules represent loadable database back-ends or overlay libraries.
     * For anything that's not a ModuleWrapper we defer to the parent class.
     *
     * @param element  the viewer row object, expected to be a ModuleWrapper
     * @return         the SWT Image to paint next to the row's text label
     */
    @Override
    public Image getImage( Object element )
    {
        if ( element instanceof ModuleWrapper )
        {
            return OpenLdapConfigurationPlugin.getDefault().getImage(
                OpenLdapConfigurationPluginConstants.IMG_DATABASE );
        }

        return super.getImage( element );
    }
}
