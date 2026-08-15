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


import java.util.ArrayList;
import java.util.List;


// ── CLASS: ATESyntaxComboInput — MILLENNIUM FALCON HIDDEN COMPARTMENTS ───────────────
// The Millennium Falcon has hidden cargo compartments under the floor panels — neutral
// storage that holds whatever you put in, no questions asked.  Han used them for spice
// runs; the Rebellion used them to hide heroes from Imperial scanners.
// This class is those compartments for the "Syntax" combo: a plain lazy-initialised
// list that the content provider fills with LdapSyntax objects and NonExistingSyntax
// placeholders, which the ComboViewer then displays for the user to choose from.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Simple container that acts as the JFace input object for the "Syntax" combo box on
 * the Attribute Type Editor's Overview page.
 * The content provider ({@link ATESyntaxComboContentProvider}) populates this object's
 * children list on the first call to {@code getElements} and returns the same list on
 * subsequent calls.  This object is the memory between those calls.
 * Think of this as the Falcon's cargo hold: neutral storage, lazily loaded, holds
 * whatever the content provider puts in it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATESyntaxComboInput
{
    /** The children */
    private List<Object> children;


    // ── Han Drops Another Crate Into the Hold ────────────────────────────────────────
    // Han opens the floor hatch and stashes another item — the hold is lazy-initialised,
    // so the first item triggers the list creation.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single item to the children list.
     * We lazy-initialise the list on the first call so no ArrayList is allocated if
     * this input object is created but the combo tab is never opened.
     *
     * <p>For example — loading the syntax cargo:</p>
     * <pre>
     *   input.addChild( new NonExistingSyntax( NonExistingSyntax.NONE ) );
     *   input.addChild( someKnownSyntax );
     * </pre>
     *
     * @param child  the item to add — either a
     *               {@link org.apache.directory.api.ldap.model.schema.LdapSyntax} or a
     *               {@link org.apache.directory.studio.schemaeditor.view.editors.NonExistingSyntax};
     *               must not be null
     */
    public void addChild( Object child )
    {
        if ( children == null )
        {
            children = new ArrayList<Object>();
        }

        children.add( child );
    }


    // ── Han Opens the Hatch and Lists the Contents ───────────────────────────────────
    // Han lifts the floor plate and reads out the inventory.  An empty hold means the
    // content provider should go fill it — it's the trigger for lazy population.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the mutable list of children currently held in this input.
     * An empty list signals the content provider to populate it now.  The caller may
     * add extra items (e.g. an unresolvable syntax OID as a NonExistingSyntax) to
     * the returned list.
     *
     * @return  the live children list; never null, may be empty
     */
    public List<Object> getChildren()
    {
        if ( children == null )
        {
            children = new ArrayList<Object>();
        }

        return children;
    }
}
