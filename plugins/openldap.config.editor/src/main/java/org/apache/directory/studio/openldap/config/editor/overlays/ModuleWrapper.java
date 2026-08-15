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


// ── CLASS: ModuleWrapper — C-3PO Translating R2's Beeps ──────────────────────
// C-3PO stands between R2-D2 and every human in the room, translating raw
// binary whistles into something the audience can actually understand.  Without
// him, R2's critical Death Star schematics would be meaningless noise.
// ModuleWrapper does the same thing: it wraps the raw module data (name, path,
// order, list index) that comes out of the LDAP model and expresses it in a
// form that the Overlays-page UI table can display and sort.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A UI-facing adapter that wraps a single OpenLDAP module entry for display in
 * the Overlays page module list.
 * A module in OpenLDAP terms is a shared library (.so) that the server loads
 * dynamically; each module belongs to a named moduleList, has a file-system
 * path, and carries a load order.  We bundle all four pieces here so the table
 * viewer and sorter have everything they need in one object.
 * Think of this class as C-3PO standing between R2-D2 and the rebel briefing
 * room: raw binary module data becomes a tidy, human-readable row in the UI.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModuleWrapper
{
    /** The moduleList's name */
    private String moduleListName;

    /** The wrapped  moduleList index : we may have more than one moduleList */
    private int moduleListIndex;

    /** The wrapped Module name */
    private String moduleName;

    /** The wrapped module path */
    private String path;

    /** The wrapped module order : we may have many modules for a given path*/
    private int order;


    // ── Default Constructor — C-3PO Powers On ────────────────────────────────
    // On the Tantive IV, C-3PO activates with no memory loaded — he's ready
    // to receive instructions but knows nothing yet.
    // His counterpart R2 will fill in the details momentarily.
    // We create an empty ModuleWrapper the same way: fields start blank and
    // get populated later when the model provides real module data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty ModuleWrapper with no data loaded yet.
     * Use the full constructor when you already know the module details; use
     * this one when you plan to set properties individually afterward.
     */
    public ModuleWrapper()
    {
    }


    // ── Full Constructor — C-3PO Receives the Briefing ───────────────────────
    // C-3PO is handed all the diplomatic context at once before entering the
    // Jabba's-palace negotiation: the list name, the index, the module to
    // translate, the path to the relevant scroll, and the priority order.
    // We load every field in one shot so the wrapper is immediately ready to
    // serve the UI without partial initialization.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully initialized ModuleWrapper with all module details set.
     * We prefer this constructor when we already have all the data from the
     * model, because it avoids a half-built object floating around.
     *
     * <p>For example — C-3PO gets the full briefing:</p>
     * <pre>
     *   ModuleWrapper w = new ModuleWrapper(
     *       "cn=module,cn=config",  // moduleListName
     *       0,                      // moduleListIndex
     *       "back_mdb",             // module
     *       "/usr/lib/ldap",        // path
     *       1);                     // order
     * </pre>
     *
     * @param moduleListName   the name of the moduleList LDAP entry this module belongs to — used for grouping and display
     * @param moduleListIndex  the position of the moduleList among all moduleLists — needed because two lists can share a name
     * @param module           the actual module name (e.g., "back_mdb") — this is what OpenLDAP loads
     * @param path             the file-system directory containing the module — can be null if the server uses its default path
     * @param order            the load order within the moduleList — lower numbers load first
     */
    public ModuleWrapper( String moduleListName, int moduleListIndex, String module, String path, int order )
    {
        this.moduleListName = moduleListName;
        this.moduleName = module;
        this.path = path;
        this.moduleListIndex = moduleListIndex;
        this.order = order;
    }


    // ── Get List Name — C-3PO Names the Delegation ───────────────────────────
    // Arriving at Jabba's court, C-3PO announces which embassy he represents.
    // The moduleList name is our equivalent — it identifies the parent LDAP
    // entry that groups a set of related modules together.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name of the moduleList LDAP entry this module belongs to.
     * We need this so the sorter can group modules by their parent list before
     * comparing individual load orders.
     *
     * @return the moduleList DN fragment (e.g., "cn=module{0},cn=config"), never modified by this class
     */
    public String getModuleListName()
    {
        return moduleListName;
    }


    // ── Get List Index — C-3PO Picks the Right Scroll ────────────────────────
    // Jabba has multiple royal scrolls numbered by court precedence; C-3PO
    // must reference the correct one or deliver the wrong proclamation.
    // The moduleList index is that number — two lists can share the same name
    // but differ by index.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index of the moduleList among all moduleLists with
     * the same name.
     * OpenLDAP allows multiple moduleList entries, so we track the index to
     * distinguish them unambiguously.
     *
     * @return the moduleList index (0-based)
     */
    public int getModuleListIndex()
    {
        return moduleListIndex;
    }


    // ── Get Module Name — C-3PO Reads the Module Aloud ───────────────────────
    // C-3PO reads the name of the visitor from the scroll — this is the actual
    // name that gets announced to Jabba's court.
    // The module name is exactly what OpenLDAP uses to find and load the shared
    // library, so it's the most important single field we carry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bare module name (e.g., "back_mdb", "syncprov") without path
     * or order decoration.
     * This is the value OpenLDAP actually uses to locate the shared library.
     *
     * @return the module name as a plain string
     */
    public String getModuleName()
    {
        return moduleName;
    }


    // ── Get Path — C-3PO Finds the Archive Location ──────────────────────────
    // C-3PO knows that the diplomatic treaty lives in a specific archive chamber
    // — he retrieves that location so everyone can verify authenticity.
    // The path tells OpenLDAP which directory to search for the module file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file-system directory that contains this module.
     * Can be null when OpenLDAP uses its compiled-in default module path.
     *
     * @return the module directory path, or null if unspecified
     */
    public String getPath()
    {
        return path;
    }


    // ── Get Order — C-3PO Reports Load Priority ──────────────────────────────
    // In Jabba's court, protocol demands modules be presented in strict
    // precedence order — C-3PO always reports that rank accurately.
    // OpenLDAP loads modules in order within a list, so this integer tells us
    // where in the sequence this module sits.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the load order of this module within its moduleList.
     * Lower values load first; the sorter uses this to rank entries in the UI
     * table so they match the actual server load sequence.
     *
     * @return the module load order (0-based integer)
     */
    public int getOrder()
    {
        return order;
    }


    // ── Set Module Name — C-3PO Updates His Notes ────────────────────────────
    // Partway through negotiations, a courier arrives with a corrected module
    // name on a new scroll — C-3PO crosses out the old name and writes in the
    // new one before continuing.
    // We do the same when the user edits the module name in the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the module name stored in this wrapper.
     * Call this when the user has edited the module name field in the editor
     * and we need to reflect that change in the model.
     *
     * @param moduleName  the new module name to store
     */
    public void setModuleName( String moduleName )
    {
        this.moduleName = moduleName;
    }


    // ── Set Module Index — C-3PO Corrects the Scroll Number ──────────────────
    // The royal archivist discovers that scroll 0 was mislabeled as scroll 1 —
    // C-3PO updates his reference table before the next court session.
    // We do the same when module list positions shift after an add or remove.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the moduleList index stored in this wrapper.
     * This is typically called when modules are reordered and all indexes need
     * to be recalculated.
     *
     * @param moduleListIndex  the new moduleList index
     */
    public void setModuleIndex( int moduleListIndex )
    {
        this.moduleListIndex = moduleListIndex;
    }


    // ── Set Path — C-3PO Updates the Archive Location ────────────────────────
    // The diplomatic archives have been moved to a new chamber; C-3PO notes the
    // new location so future references point to the right place.
    // We update the path when the user changes where the module lives on disk.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the file-system path stored in this wrapper.
     * Call this when the user has edited the module path field.
     *
     * @param path  the new directory path (null means use the server default)
     */
    public void setPath( String path )
    {
        this.path = path;
    }


    // ── Set List Name — C-3PO Renames the Embassy ────────────────────────────
    // The embassy's official designation changes after a Senate vote; C-3PO
    // updates his records so future announcements use the correct name.
    // We update the moduleList name when it changes in the LDAP tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the moduleList name stored in this wrapper.
     * This reflects changes to the parent moduleList entry's name in the LDAP
     * configuration tree.
     *
     * @param moduleListName  the new moduleList name to store
     */
    public void setModuleListName( String moduleListName )
    {
        this.moduleListName = moduleListName;
    }


    // ── Set Order — C-3PO Adjusts Protocol Precedence ────────────────────────
    // Jabba revises the audience schedule, bumping one delegation ahead of
    // another — C-3PO re-numbers his cue cards accordingly.
    // We do the same when the user drags a module to a different position.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the load order stored in this wrapper.
     * Call this when the user reorders modules in the UI table.
     *
     * @param order  the new load-order value to store
     */
    public void setOrder( int order )
    {
        this.order = order;
    }


    // ── Build Path+Name String — C-3PO Composes the Formal Title ─────────────
    // When announcing a visitor to Jabba, C-3PO combines the chamber location
    // with the visitor's formal rank and name into one authoritative phrase.
    // We do the same: combine path, order, and module name into the string
    // that the label provider shows in the UI table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a display string combining this module's path, load order, and
     * name in the format "path/{order}moduleName" (path omitted when null).
     * This is what the label provider shows in the Overlays page module table
     * so the user can see the full identity of each module at a glance.
     *
     * @return a human-readable path+name string for display in the UI
     */
    public String getModulePathName()
    {
        StringBuilder sb = new StringBuilder();

        if ( path != null )
        {
            sb.append( path ).append( "/" );
        }

        sb.append( "{" ).append( order ).append( '}' ).append( moduleName );

        return sb.toString();
    }


    // ── toString — C-3PO Delivers the Full Diplomatic Identifier ─────────────
    // At a formal inter-system summit, C-3PO announces the complete
    // identification: "Representing moduleList{index}: path/{order}moduleName."
    // This gives every observer enough information to locate and verify the
    // module without any ambiguity.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a fully qualified string representation of this module wrapper,
     * in the format "moduleListName{index}:path/{order}moduleName".
     * Useful for debugging — you can paste this into an LDAP browser and
     * immediately identify which entry the wrapper represents.
     *
     * @return a unique, human-readable identifier for this module wrapper
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( moduleListName ).append( '{').append(  moduleListIndex ).append( '}' );

        sb.append( ':' );

        if ( path != null )
        {
            sb.append( path ).append( "/" );
        }

        sb.append( "{" ).append( order ).append( '}' ).append( moduleName );

        return sb.toString();
    }
}
