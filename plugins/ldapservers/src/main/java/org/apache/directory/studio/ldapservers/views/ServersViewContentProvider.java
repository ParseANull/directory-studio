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
package org.apache.directory.studio.ldapservers.views;


import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ServersViewContentProvider — LANDO'S CLOUD CITY INVENTORY MANIFEST ────────────
// Lando's operations manifest lists every active facility in Cloud City: the gas extractors,
// the carbon-freezing chamber, the landing pads.  When the control panel asks "what do we have?"
// the manifest hands back the full list.
// That's exactly what this class does: when the JFace TreeViewer asks for its root elements,
// we hand it the complete list of configured LDAP servers from LdapServersManager.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the Servers view's JFace {@link org.eclipse.jface.viewers.TreeViewer} with the
 * list of {@link org.apache.directory.studio.ldapservers.model.LdapServer}s to display.
 * Since servers have no child elements, all tree-navigation methods return null/false.
 * Think of it as Cloud City's operations manifest: one flat list, no hierarchy.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServersViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    // ── Lando Reads The Full Manifest ────────────────────────────────────────────────────────
    // The operations panel asks: "what facilities do we have right now?"
    // Lando reads the manifest and returns the full array — every server in the list.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all configured LDAP servers as the root-level elements of the tree.
     * The {@code inputElement} is the sentinel string "root" set on the viewer during
     * construction — we ignore it and always return the live server list.
     *
     * @param inputElement  the viewer's current input (ignored)
     * @return an array of {@link org.apache.directory.studio.ldapservers.model.LdapServer} objects
     */
    public Object[] getElements( Object inputElement )
    {
        return LdapServersManager.getDefault().getServersList().toArray();
    }


    // ── Closing The Manifest ─────────────────────────────────────────────────────────────────
    // When the panel shuts down, the manifest needs no cleanup.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the viewer when it is disposed. Nothing to release here.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Swapping The Manifest For A New One ──────────────────────────────────────────────────
    // If the viewer gets pointed at a different input, we'd normally update our state.
    // Server list is always pulled live from LdapServersManager, so no update needed.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the viewer's input changes. No state to update here.
     *
     * @param viewer    the viewer
     * @param oldInput  the previous input
     * @param newInput  the new input
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do
    }


    // ── Servers Have No Sub-Facilities ───────────────────────────────────────────────────────
    // Servers are leaf nodes — they don't expand into child elements.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — servers are flat leaf elements with no children.
     *
     * @param parentElement  the parent element (unused)
     * @return {@code null}
     */
    public Object[] getChildren( Object parentElement )
    {
        return null;
    }


    /**
     * Returns {@code null} — servers have no parent in the tree hierarchy.
     *
     * @param element  the element (unused)
     * @return {@code null}
     */
    public Object getParent( Object element )
    {
        return null;
    }


    /**
     * Returns {@code false} — servers are leaf nodes and never have children.
     *
     * @param element  the element (unused)
     * @return {@code false}
     */
    public boolean hasChildren( Object element )
    {
        return false;
    }
}
