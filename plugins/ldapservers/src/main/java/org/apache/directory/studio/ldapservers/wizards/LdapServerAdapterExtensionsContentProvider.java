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


import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.studio.ldapservers.LdapServerAdapterExtensionsManager;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: LdapServerAdapterExtensionsContentProvider — THE IMPERIAL PARTS CATALOGUE ORGANISER
// When Han and Chewie need a replacement motivator, they browse the Falcon's parts catalogue.
// The parts are grouped by vendor: "Corellian Engineering", "Kuat Drive Yards", etc.
// Under each vendor heading sit the specific models (ApacheDS 2.0, ApacheDS 2.1).
// This class is that organiser: it groups all registered LDAP server adapter extensions by
// vendor (the tree's top-level nodes) and supplies the individual extensions as children.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Content provider for the adapter-selection {@link TreeViewer} in the New Server Wizard.
 * Builds a two-level tree: top level = vendor strings; children = the {@link LdapServerAdapterExtension}s
 * supplied by that vendor.
 * Populated from {@link LdapServerAdapterExtensionsManager#getLdapServerAdapterExtensions()} at construction time.
 * Think of it as the Imperial parts catalogue organiser.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServerAdapterExtensionsContentProvider implements ITreeContentProvider
{
    /** The {@link MultiValuedMap} used to store LDAP Server Adapter Extensions and order them by vendor (used as key) */
    private MultiValuedMap<String, LdapServerAdapterExtension> ldapServerAdapterExtensionsMap = new ArrayListValuedHashMap<>();


    // ── Building The Parts Catalogue ─────────────────────────────────────────────────────────
    // At startup we scan the registry and file every extension under its vendor heading.
    // The multimap lets us look up all extensions for a given vendor in one call.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the content provider and populates the vendor-to-extensions multimap
     * from the live extension registry.
     *
     * <p>For example — building the catalogue:</p>
     * <pre>
     *   "Apache Software Foundation" → [ApacheDS 2.0, ApacheDS 2.1]
     *   "Some Vendor" → [OtherDS 1.0]
     * </pre>
     */
    public LdapServerAdapterExtensionsContentProvider()
    {
        for ( LdapServerAdapterExtension extension : LdapServerAdapterExtensionsManager.getDefault()
            .getLdapServerAdapterExtensions() )
        {
            ldapServerAdapterExtensionsMap.put( extension.getVendor(), extension );
        }
    }


    // ── Listing The Vendor Headings ───────────────────────────────────────────────────────────
    // The top level of the tree shows vendor names — the catalogue sections.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the set of vendor strings as the tree's top-level elements.
     *
     * @param inputElement  the tree's input (ignored)
     * @return array of vendor name strings
     */
    public Object[] getElements( Object inputElement )
    {
        return ldapServerAdapterExtensionsMap.keySet().toArray();
    }


    // ── Listing The Extensions Under A Vendor Heading ─────────────────────────────────────────
    // When the user expands a vendor node, we return the extensions grouped under that vendor.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapServerAdapterExtension}s for the given vendor string parent node.
     * Returns {@code null} if the element has no children.
     *
     * @param parentElement  the vendor string (top-level node)
     * @return array of child extensions, or {@code null}
     */
    public Object[] getChildren( Object parentElement )
    {
        Object children = ldapServerAdapterExtensionsMap.get( (String) parentElement );
        if ( children != null )
        {
            if ( children instanceof List )
            {
                return ( ( List<?> ) children ).toArray();
            }
            else
            {
                return new Object[]
                    { children };
            }
        }

        return null;
    }


    // ── Checking Whether A Node Can Be Expanded ───────────────────────────────────────────────
    // Vendor strings are expandable (they have extension children); extension objects are leaves.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} for vendor-string top-level nodes (they have extension children),
     * {@code false} for {@link LdapServerAdapterExtension} leaf nodes.
     *
     * @param element  the element to check
     * @return {@code true} if the element is a vendor string
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof String )
        {
            return true;
        }

        return false;
    }


    /**
     * Nothing to dispose.
     */
    public void dispose()
    {
        // Nothing to do
    }


    /**
     * Input changes are not relevant — the catalogue is built at construction time.
     *
     * @param viewer    the viewer
     * @param oldInput  the previous input
     * @param newInput  the new input
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do
    }


    /**
     * Returns {@code null} — navigation is one-directional (vendor → extension only).
     *
     * @param element  the element
     * @return {@code null}
     */
    public Object getParent( Object element )
    {
        // Hierarchy is only descending.
        // Should not be used.
        return null;
    }
}
