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
package org.apache.directory.studio.schemaeditor.model.hierarchy;


import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Schema;


// ── CLASS: HierarchyManager — Lando Running Cloud City ───────────────────────
// Lando Calrissian doesn't just live in Cloud City — he runs it: every resident,
// every platform, every relationship between them is his to manage.  When someone
// new arrives (Han, Leia, Chewie) he has to fit them into the existing social
// hierarchy, and when they leave he has to rewire all the connections they leave
// behind.  That's exactly what we do here: we maintain the parent/child map for
// every attribute type and object class in the loaded schemas.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages the inheritance hierarchy of LDAP schema types — attribute types and
 * object classes — by tracking who is the parent of whom in two internal maps.
 * It lives at the core of the schema editor's hierarchy view, and gets updated
 * any time the loaded schema changes.
 * Think of this class as Lando Calrissian running Cloud City: he knows every
 * resident's place in the pecking order and updates that knowledge whenever
 * someone arrives, leaves, or gets reassigned.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HierarchyManager
{
    /** The parents map is used to store for each element its parents */
    private MultiValuedMap<Object, Object> parentsMap;

    /** The parents map is used to store for each element its children */
    private MultiValuedMap<Object, Object> childrenMap;

    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The RootObject of the Hierarchy */
    private RootObject root;


    // ── Lando Opens the Doors of Cloud City ──────────────────────────────────
    // Lando strides into the control room, activates every system, and pulls up
    // the full population manifest of Cloud City from the archives.
    // He needs to know who lives where before he can manage anything.
    // We spin up both maps and then load the complete current schema into them
    // so the hierarchy is ready before anyone asks for it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new HierarchyManager and immediately populates it from the current schema.
     * We initialise the parents and children maps, grab the SchemaHandler from the
     * Activator, and call {@link #loadSchema()} to fill everything in.
     */
    public HierarchyManager()
    {
        // Initializing the maps
        parentsMap = new ArrayListValuedHashMap<>();
        childrenMap = new ArrayListValuedHashMap<>();

        // Getting the SchemaHandler
        schemaHandler = Activator.getDefault().getSchemaHandler();

        // Loading the complete Schema
        loadSchema();
    }


    // ── Lando Assigns a New Resident a Bunk ──────────────────────────────────
    // A new worker arrives at Cloud City and Lando has to figure out which
    // department they belong to and who their supervisor is.
    // If the supervisor isn't here yet, we park the new arrival in a holding
    // zone (keyed by the supervisor's name string) until the boss shows up.
    // We wire the new attribute type into the parentsMap and childrenMap,
    // handling the "superior exists" and "superior missing" cases separately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers an attribute type in both hierarchy maps.
     * We first resolve any alias/OID placeholders, then link the attribute type
     * to its superior (or directly to root if it has none).
     *
     * @param at  the attribute type to add — must not be null
     */
    private void addAttributeType( AttributeType at )
    {
        // Checking Aliases and OID
        checkAliasesAndOID( at );

        String superiorName = at.getSuperiorOid();
        if ( superiorName != null )
        // The attribute type has a superior
        {
            AttributeType superior = schemaHandler.getAttributeType( superiorName );
            if ( superior != null )
            // The superior attribute type object exists
            {
                parentsMap.put( at, superior );
                childrenMap.put( superior, at );
            }
            else
            // The superior attribute type object does not exist
            {
                // Then, its parent is the name of its superior and
                // it becomes the children of it and the RootObject
                parentsMap.put( at, Strings.toLowerCase( superiorName ) );
                childrenMap.put( Strings.toLowerCase( superiorName ), at );
                childrenMap.put( root, at );
            }
        }
        else
        // The attribute type does not have a superior
        {
            // Then, its parent is the RootObject
            parentsMap.put( at, root );
            childrenMap.put( root, at );
        }
    }


    // ── Lando Slots a New Department into the Org Chart ──────────────────────
    // A new department (object class) arrives; Lando checks the org chart for
    // its declared parent departments and links it in.
    // Object classes can have multiple superiors — Lando can have multiple bosses
    // too (the Emperor AND the consortium that owns the tibanna mines).
    // If no superior is declared and this isn't "top" itself, we link it under
    // the universal "top" object class, which is the ultimate ancestor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers an object class in both hierarchy maps.
     * Object classes can have zero or more superiors; we handle all cases,
     * including the special bootstrap case of the "top" object class (OID 2.5.6.0)
     * which sits at the root of the object-class inheritance tree.
     *
     * @param oc  the object class to add — must not be null
     */
    private void addObjectClass( ObjectClass oc )
    {
        // Checking Aliases and OID
        checkAliasesAndOID( oc );

        List<String> superClasseNames = oc.getSuperiorOids();
        if ( ( superClasseNames != null ) && ( superClasseNames.size() > 0 ) )
        // The object class has one or more superiors
        {
            for ( String superClassName : superClasseNames )
            {
                ObjectClass superClass = schemaHandler.getObjectClass( superClassName );
                if ( superClass == null )
                {
                    parentsMap.put( oc, Strings.toLowerCase( superClassName ) );
                    childrenMap.put( Strings.toLowerCase( superClassName ), oc );
                    childrenMap.put( root, oc );
                }
                else
                {
                    parentsMap.put( oc, superClass );
                    childrenMap.put( superClass, oc );
                }
            }
        }
        else
        // The object class does not have any declared superior
        // Then, it is a child of the "top (2.5.6.0)" object class
        // (Unless it is the "top (2.5.6.0)" object class itself)
        {
            ObjectClass topOC = schemaHandler.getObjectClass( "2.5.6.0" ); //$NON-NLS-1$
            if ( oc.equals( topOC ) )
            // The given object class is the "top (2.5.6.0)" object class
            {
                parentsMap.put( oc, root );
                childrenMap.put( root, oc );
            }
            else
            {
                if ( topOC != null )
                // The "top (2.5.6.0)" object class exists
                {
                    parentsMap.put( oc, topOC );
                    childrenMap.put( topOC, oc );
                }
                else
                // The "top (2.5.6.0)" object class does not exist
                {
                    parentsMap.put( oc, "2.5.6.0" ); //$NON-NLS-1$
                    childrenMap.put( "2.5.6.0", oc ); //$NON-NLS-1$
                    childrenMap.put( root, oc );
                }
            }
        }
    }


    // ── Lando Welcomes a New Mining Engineer ─────────────────────────────────
    // A freshly hired tibanna gas engineer reports for duty and Lando personally
    // adds them to the Cloud City employee roster.
    // This is the public hook the schema-change listener calls when the schema
    // gains a new attribute type — we just delegate to the internal logic.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by schema listeners when a new attribute type is added to the loaded schema.
     * We immediately register it in both hierarchy maps so the hierarchy view stays current.
     *
     * @param at  the newly added attribute type
     */
    public void attributeTypeAdded( AttributeType at )
    {
        addAttributeType( at );
    }


    // ── Lando Reassigns a Resident to a Different Department ─────────────────
    // An engineer transfers from mining to security; Lando cuts them from the
    // mining roll, clears their old supervisor link, and plugs them into the
    // security hierarchy.
    // When an attribute type changes (e.g. its superior OID changes) we rip it
    // out of both maps and re-add it so all parent/child links are correct again.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an existing attribute type is modified.
     * We remove all its current hierarchy links, then re-add it so the maps
     * reflect whatever the type looks like now (superior may have changed, etc.).
     *
     * @param at  the modified attribute type
     */
    public void attributeTypeModified( AttributeType at )
    {
        // Removing the attribute type
        List<Object> parents = getParents( at );
        if ( parents != null )
        {
            for ( Object parent : parents )
            {
                childrenMap.removeMapping( parent, at );
            }

            parentsMap.remove( at );
        }

        // Adding the attribute type again
        addAttributeType( at );
    }


    // ── Lando Strikes a Worker from the Roster ───────────────────────────────
    // A worker leaves Cloud City and Lando removes them from the employee registry,
    // making sure their old subordinates get reassigned so nothing is left dangling.
    // This is the public hook called when an attribute type is deleted from the schema.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an attribute type is removed from the loaded schema.
     * We clean up all its parent/child map entries and promote any orphaned
     * children to root so the tree stays coherent.
     *
     * @param at  the removed attribute type
     */
    public void attributeTypeRemoved( AttributeType at )
    {
        removeAttributeType( at );
    }


    // ── Lando Cross-References Aliases and Badge Numbers ─────────────────────
    // Cloud City workers sometimes go by nicknames; Lando checks the nickname list
    // against the pending-placement queue, resolves who they are, and promotes
    // string-key placeholders to real object references.
    // When a schema object arrives, we check whether any child was already
    // registered under one of its aliases or OID — and if so we upgrade those
    // placeholder string keys to the real object reference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resolves string-keyed placeholder entries in both maps caused by forward
     * references — children that registered before their parent arrived.
     * We look up every alias and the OID of the given object, migrate any
     * waiting children over, and discard the string placeholder keys.
     *
     * @param object  the schema object (attribute type or object class) whose
     *                aliases and OID we should check
     */
    private void checkAliasesAndOID( SchemaObject object )
    {
        // Aliases
        List<String> aliases = object.getNames();
        if ( aliases != null )
        {
            for ( String alias : aliases )
            {
                // Looking for children objects for this alias value
                @SuppressWarnings("unchecked")
                List<Object> children = ( List<Object> ) childrenMap.get( Strings.toLowerCase( alias ) );
                if ( children != null )
                {
                    for ( Object value : children )
                    {
                        childrenMap.put( object, value );
                        parentsMap.removeMapping( value, Strings.toLowerCase( alias ) );
                        parentsMap.put( value, object );
                    }
                    childrenMap.remove( Strings.toLowerCase( alias ) );
                }
            }
        }

        // OID
        String oid = object.getOid();
        if ( oid != null )
        {
            // Looking for children objects for this OID value
            @SuppressWarnings("unchecked")
            List<Object> children = ( List<Object> ) childrenMap.get( Strings.toLowerCase( oid ) );
            if ( children != null )
            {
                for ( Object value : children )
                {
                    childrenMap.put( object, value );
                    if ( oid.equals( "2.5.6.0" ) ) //$NON-NLS-1$
                    {
                        childrenMap.removeMapping( root, value );
                    }
                    parentsMap.removeMapping( value, Strings.toLowerCase( oid ) );
                    parentsMap.put( value, object );

                }
                childrenMap.remove( Strings.toLowerCase( oid ) );
            }
        }
    }


    // ── Lando Reads the Subordinate Roster ───────────────────────────────────
    // Someone asks Lando who reports to a particular department head; he checks
    // the org-chart board and reads off the list of direct reports.
    // This is a simple read from the children map — the caller gets back whoever
    // is directly below the given object in the hierarchy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all direct children of the given schema object (or root) in the hierarchy.
     * The caller uses this to populate tree-view nodes; an empty or null return means
     * the object is a leaf.
     *
     * @param o  the object whose children we want
     * @return   the list of children, or null/empty if it has none
     */
    @SuppressWarnings("unchecked")
    public List<Object> getChildren( Object o )
    {
        return ( List<Object> ) childrenMap.get( o );
    }


    // ── Lando Reads Who a Worker Reports To ──────────────────────────────────
    // Someone asks Lando who a particular worker answers to; he scans the
    // management chart and returns the full list of supervisors.
    // Attribute types have at most one superior; object classes can have many.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all direct parents of the given schema object in the hierarchy.
     * Attribute types have at most one parent; object classes can have multiple.
     *
     * @param o  the object whose parents we want
     * @return   the list of parents, or null/empty if it is a root element
     */
    @SuppressWarnings("unchecked")
    public List<Object> getParents( Object o )
    {
        return ( List<Object> ) parentsMap.get( o );
    }


    // ── Lando Points to the Administrator's Office ───────────────────────────
    // "Where does the buck stop?" — Lando points to the administrator's office
    // at the top of the city's org chart.  Every other entry is below this one.
    // We expose the RootObject so tree-view code can use it as the virtual
    // invisible root of the displayed hierarchy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the virtual root node of the hierarchy tree.
     * The hierarchy view passes this to its content provider as the invisible
     * root; all attribute types and object classes live below it.
     *
     * @return  the RootObject singleton for this manager instance
     */
    public RootObject getRootObject()
    {
        return root;
    }


    // ── Lando Pulls the Complete Population Census ───────────────────────────
    // Before the city opens its doors to visitors, Lando runs a full census:
    // every resident, every department, every supervisor link — all recorded.
    // We iterate over every schema and every type/class within, building the
    // entire hierarchy from scratch into the two maps.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the complete hierarchy from the currently loaded schemas.
     * Called once during construction; creates a fresh RootObject and then
     * walks every schema to register every attribute type and object class.
     */
    private void loadSchema()
    {
        if ( schemaHandler != null )
        {
            // Creating the root element
            root = new RootObject();

            // Looping on the schemas
            for ( Schema schema : schemaHandler.getSchemas() )
            {
                // Looping on the attribute types
                for ( AttributeType at : schema.getAttributeTypes() )
                {
                    addAttributeType( at );
                }

                // Looping on the object classes
                for ( ObjectClass oc : schema.getObjectClasses() )
                {
                    addObjectClass( oc );
                }
            }
        }
    }


    // ── Lando Adds a New Department to the Org Chart ─────────────────────────
    // A new department is established; Lando files the paperwork and plugs it
    // into the management hierarchy.
    // Public hook for schema listeners — delegates to the private logic.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a new object class is added to the loaded schema.
     * We immediately register it in both hierarchy maps.
     *
     * @param oc  the newly added object class
     */
    public void objectClassAdded( ObjectClass oc )
    {
        addObjectClass( oc );
    }


    // ── Lando Reorganises a Department ───────────────────────────────────────
    // A department restructures — its parent division changes — so Lando
    // cuts the old parent link, clears the record, and plugs it into the
    // new position in the org chart.
    // We remove all existing links for the object class and re-add it fresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an existing object class is modified.
     * We remove all its current hierarchy links, then re-add it reflecting
     * whatever changed (new superiors, new OID, etc.).
     *
     * @param oc  the modified object class
     */
    public void objectClassModified( ObjectClass oc )
    {
        // Removing the object class type
        List<Object> parents = getParents( oc );
        if ( parents != null )
        {
            for ( Object parent : parents )
            {
                childrenMap.removeMapping( parent, oc );
            }

            parentsMap.remove( oc );
        }

        // Adding the object class again
        addObjectClass( oc );
    }


    // ── Lando Dissolves a Department ─────────────────────────────────────────
    // A department is shut down; Lando removes it from the org chart and
    // reassigns all its workers so nobody is left without a supervisor.
    // Public hook — delegates to the internal removal logic.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when an object class is removed from the loaded schema.
     * We clean up all its hierarchy entries and re-attach its orphaned children
     * to root so the tree stays coherent.
     *
     * @param oc  the removed object class
     */
    public void objectClassRemoved( ObjectClass oc )
    {
        removeObjectClass( oc );
    }


    // ── Lando Strikes a Worker from the Registry and Reassigns Their Team ────
    // An employee quits; Lando removes them from every management list, then
    // figures out what to do with the people who used to report to them.
    // If the departed worker had subordinates, those subordinates are temporarily
    // promoted to report directly to Lando (root) until a new supervisor is found.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes an attribute type from both hierarchy maps and reassigns any
     * orphaned children directly to root, preserving their superior-name links
     * as placeholder strings in case the superior comes back later.
     *
     * @param at  the attribute type to remove
     */
    private void removeAttributeType( AttributeType at )
    {
        // Removing the attribute type as child of its superior
        String superiorName = at.getSuperiorOid();
        if ( ( superiorName != null ) && ( !"".equals( superiorName ) ) ) //$NON-NLS-1$
        {
            AttributeType superiorAT = schemaHandler.getAttributeType( superiorName );
            if ( superiorAT == null )
            {
                childrenMap.removeMapping( Strings.toLowerCase( superiorName ), at );
            }
            else
            {
                childrenMap.removeMapping( superiorAT, at );
            }
        }
        else
        {
            childrenMap.removeMapping( root, at );
        }

        // Attaching each child (if there are children) to the RootObject
        List<Object> children = getChildren( at );
        if ( children != null )
        {
            for ( Object child : children )
            {
                AttributeType childAT = ( AttributeType ) child;

                parentsMap.removeMapping( child, at );

                parentsMap.put( child, root );
                childrenMap.put( root, child );
                String childSuperiorName = childAT.getSuperiorOid();
                if ( ( childSuperiorName != null ) && ( !"".equals( childSuperiorName ) ) ) //$NON-NLS-1$
                {
                    parentsMap.put( child, Strings.toLowerCase( childSuperiorName ) );
                    childrenMap.put( Strings.toLowerCase( childSuperiorName ), child );
                }
            }
        }

        childrenMap.remove( at );
        parentsMap.remove( at );
    }


    // ── Lando Dissolves a Department and Reassigns Its Staff ─────────────────
    // A whole department is disbanded; Lando removes it from the org chart and
    // makes sure every member of that department gets reassigned, not abandoned.
    // Same pattern as removeAttributeType, but multi-superior capable because
    // object classes can have more than one parent.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes an object class from both hierarchy maps and reassigns any
     * orphaned children directly to root with string-placeholder superior links.
     *
     * @param oc  the object class to remove
     */
    private void removeObjectClass( ObjectClass oc )
    {
        // Removing the object class as child of its superiors
        List<String> superClassesNames = oc.getSuperiorOids();
        if ( ( superClassesNames != null ) && ( superClassesNames.size() > 0 ) )
        {
            for ( String superClassName : superClassesNames )
            {
                if ( !"".equals( superClassName ) ) //$NON-NLS-1$
                {
                    ObjectClass superClassOC = schemaHandler.getObjectClass( superClassName );
                    if ( superClassOC == null )
                    {
                        childrenMap.removeMapping( Strings.toLowerCase( superClassName ), oc );
                        childrenMap.removeMapping( root, oc );
                    }
                    else
                    {
                        childrenMap.removeMapping( superClassOC, oc );
                    }
                }
            }
        }
        else
        {
            if ( oc.getOid().equals( "2.5.6.0" ) ) //$NON-NLS-1$
            // The given object class is the "top (2.5.6.0)" object class
            {
                childrenMap.removeMapping( root, oc );
            }
            else
            {
                ObjectClass topOC = schemaHandler.getObjectClass( "2.5.6.0" ); //$NON-NLS-1$
                if ( topOC != null )
                // The "top (2.5.6.0)" object class exists
                {
                    childrenMap.removeMapping( topOC, oc );
                }
                else
                // The "top (2.5.6.0)" object class does not exist
                {
                    childrenMap.removeMapping( "2.5.6.0", oc ); //$NON-NLS-1$
                }
            }
        }

        // Attaching each child (if there are children) to the RootObject
        List<Object> children = getChildren( oc );
        if ( children != null )
        {
            for ( Object child : children )
            {
                ObjectClass childOC = ( ObjectClass ) child;

                parentsMap.removeMapping( child, oc );

                parentsMap.put( child, root );
                childrenMap.put( root, child );
                List<String> childSuperClassesNames = childOC.getSuperiorOids();
                if ( ( childSuperClassesNames != null ) && ( childSuperClassesNames.size() > 0 ) )
                {
                    String correctSuperClassName = getCorrectSuperClassName( oc, childSuperClassesNames );
                    if ( correctSuperClassName != null )
                    {
                        parentsMap.put( child, Strings.toLowerCase( correctSuperClassName ) );
                        childrenMap.put( Strings.toLowerCase( correctSuperClassName ), child );
                    }
                }
                else
                {
                    parentsMap.put( child, "2.5.6.0" ); //$NON-NLS-1$
                    childrenMap.put( "2.5.6.0", child ); //$NON-NLS-1$
                }
            }
        }

        childrenMap.remove( oc );
        parentsMap.remove( oc );
    }


    // ── Lando Checks Which Department Name Matches the Records ───────────────
    // A child department listed its old parent by an alias; Lando looks through
    // the departing department's known names to find the one that matches.
    // When re-linking orphaned children we need the exact alias string the child
    // used for its superior, not just any alias the superior happens to have.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds which of the child's declared superior names matches an alias of the
     * given (now-removed) object class, so we can preserve the right string key
     * in the maps after the superior is gone.
     *
     * @param oc                    the object class being removed
     * @param childSuperClassesNames  the list of superior names declared by a child OC
     * @return  the matching alias, or null if none found
     */
    private String getCorrectSuperClassName( ObjectClass oc, List<String> childSuperClassesNames )
    {
        if ( childSuperClassesNames != null )
        {
            List<String> aliases = oc.getNames();
            if ( aliases != null )
            {
                for ( String childSuperClassName : childSuperClassesNames )
                {
                    if ( aliases.contains( childSuperClassName ) )
                    {
                        return childSuperClassName;
                    }
                }
            }
        }

        // Default
        return null;
    }
}
