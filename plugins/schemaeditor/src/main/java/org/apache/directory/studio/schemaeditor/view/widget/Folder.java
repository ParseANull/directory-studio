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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.directory.studio.schemaeditor.model.difference.Difference;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;


// ── CLASS: Folder — LANDO RUNS A DISTRICT IN CLOUD CITY ──────────────────────
// Cloud City is divided into administrative districts, each managed by Lando
// Calrissian: the landing pads sector, the carbonite chamber sector, the
// residential sector. Each district is a named container that holds its own
// set of residents and activities. Our Folder does the same thing in the schema
// differences tree: it is a named grouping node (Attribute Types, Object Classes)
// that contains a collection of child Difference objects.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A simple container node used in the schema-differences {@link org.eclipse.jface.viewers.TreeViewer}.
 * A Folder groups related {@link Difference} items under a typed, named heading —
 * for example, "Attribute Types (12)" or "Object Classes (3)" — making it easier
 * for the user to navigate a large set of schema changes.
 * Think of it as one of Lando's districts in Cloud City: a named area that collects
 * its own population of items and presents them as a coherent unit.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Folder
{
    /** The children */
    protected List<Difference> children;

    // ── CLASS: FolderType — THE DISTRICT CLASSIFICATION ──────────────────────────
    // Cloud City has different kinds of districts: industrial, residential, secure.
    // Lando knows which set of rules applies in each zone. FolderType tells our
    // Folder which kind of schema objects it holds, so the label provider can pick
    // the right icon and name.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Classifies the kind of schema elements this folder groups together. The label
     * provider uses this to pick the right folder icon and display name. {@code NONE}
     * is the generic untyped fallback; the others map to specific schema object categories.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum FolderType
    {
        NONE, ATTRIBUTE_TYPE, OBJECT_CLASS, ERROR, WARNING
    }

    /** The type of the Folder */
    private FolderType type = FolderType.NONE;

    /** The name of the Folder */
    private String name = ""; //$NON-NLS-1$


    // ── LANDO OPENS A NEW DISTRICT: CONSTRUCTING THE FOLDER ──────────────────────
    // Lando declares a new district open and gives it a name based on its type.
    // An ATTRIBUTE_TYPE district gets the "Attribute Types" sign; an OBJECT_CLASS
    // district gets "Object Classes." The children list starts empty — residents
    // move in later via addChild() and addAllChildren().
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new Folder of the given type and sets its display name accordingly.
     * The localised name is looked up from the messages bundle (e.g. "Attribute Types"
     * for {@link FolderType#ATTRIBUTE_TYPE}). Unknown or {@code NONE} types get no
     * display name — the label provider can decide what to show.
     *
     * <p>For example — Lando opens a new district:</p>
     * <pre>
     *   new Folder(FolderType.ATTRIBUTE_TYPE)  →  name = "Attribute Types", children empty
     *   new Folder(FolderType.OBJECT_CLASS)    →  name = "Object Classes",  children empty
     * </pre>
     *
     * @param type  the kind of schema objects this folder will hold; use
     *              {@link FolderType#NONE} for a generic untyped folder
     */
    public Folder( FolderType type )
    {
        this.type = type;

        switch ( type )
        {
            case ATTRIBUTE_TYPE:
                name = Messages.getString( "Folder.AttributeTypes" ); //$NON-NLS-1$
                break;
            case OBJECT_CLASS:
                name = Messages.getString( "Folder.ObjectClasses" ); //$NON-NLS-1$
                break;
            default:
                break;
        }
    }


    // ── READING THE DISTRICT CLASSIFICATION BADGE ─────────────────────────────────
    // Every district in Cloud City has a classification badge on the door: industrial,
    // residential, secure. We expose ours so the label provider and content provider
    // can use the type to pick the right icon and decide how to present the folder.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link FolderType} that classifies what kind of schema objects this
     * folder holds. The content provider and label provider both use this to decide
     * how to display and expand the folder node in the tree.
     *
     * @return  the folder type, never {@code null}
     */
    public FolderType getType()
    {
        return type;
    }


    // ── READING THE DISTRICT SIGN ────────────────────────────────────────────────
    // Every district has a name painted on the entryway so visitors know where they
    // are. We return the localised name here; the label provider appends the child
    // count in parentheses before showing it in the tree.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this folder (e.g. "Attribute Types", "Object Classes").
     * The label provider appends a child count to this before rendering, so the tree
     * shows something like "Attribute Types (12)". The name is set in the constructor
     * based on the folder type.
     *
     * @return  the localised display name; may be an empty string for {@link FolderType#NONE}
     */
    public String getName()
    {
        return name;
    }


    // ── IS ANYONE LIVING IN THIS DISTRICT? ───────────────────────────────────────
    // Before Lando leads a tour, he checks whether the district actually has any
    // residents — no point showing an empty sector. We return false if the children
    // list is null or empty, so the tree can decide whether to show an expand arrow.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this folder contains at least one child difference.
     * The tree content provider uses this to decide whether to show the expand triangle
     * next to the folder node. An uninitialised or empty children list returns false.
     *
     * @return  {@code true} if there is at least one child; {@code false} if empty or null
     */
    public boolean hasChildren()
    {
        if ( children == null )
        {
            return false;
        }

        return !children.isEmpty();
    }


    // ── LISTING THE DISTRICT'S RESIDENTS ─────────────────────────────────────────
    // Lando can produce a roster of everyone in the district on demand. We lazily
    // initialise the list the first time this is called so we never hand out a null
    // to callers — an empty list is much safer to iterate over than null.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of child {@link Difference} objects held by this folder.
     * We lazily initialise the list on first access, so callers always get a non-null
     * result. Mutating the returned list directly is supported but prefer
     * {@link #addChild} and {@link #addAllChildren} for consistency.
     *
     * @return  the mutable list of children; never {@code null}, possibly empty
     */
    public List<Difference> getChildren()
    {
        if ( children == null )
        {
            children = new ArrayList<Difference>();
        }

        return children;
    }


    // ── A NEW RESIDENT MOVES INTO THE DISTRICT ────────────────────────────────────
    // Lando processes a new arrival: if this person is not already on the roster
    // they get added; duplicates are silently ignored to keep the list clean.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single {@link Difference} to this folder's children list, ignoring
     * duplicates. We lazily initialise the list if this is the first addition.
     * The duplicate check uses {@link List#contains(Object)}, which relies on the
     * difference object's {@code equals()} implementation.
     *
     * @param diff  the difference to add; must not be {@code null}
     */
    public void addChild( Difference diff )
    {
        if ( children == null )
        {
            children = new ArrayList<Difference>();
        }

        if ( !children.contains( diff ) )
        {
            children.add( diff );
        }
    }


    // ── A RESIDENT IS EVICTED FROM THE DISTRICT ───────────────────────────────────
    // Lando occasionally has to remove someone from the roster — an eviction, a
    // transfer. We remove the matching node from the children list; if the list is
    // null we silently do nothing since there is nothing to remove.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a {@link TreeNode} from this folder's children list. If the children
     * list has not been initialised yet, we do nothing — there is nothing to remove.
     * Note the parameter type is {@link TreeNode}, not {@link Difference}, which means
     * this method only works for tree-node subtypes of Difference.
     *
     * @param node  the tree node to remove; if not present, the call is a no-op
     */
    public void removeChild( TreeNode node )
    {
        if ( children != null )
        {
            children.remove( node );
        }
    }


    // ── AN ENTIRE CONVOY MOVES INTO THE DISTRICT ─────────────────────────────────
    // Sometimes whole batches of new residents arrive at once. Lando fast-tracks
    // them all in one go. We lazily initialise the list and then delegate to
    // List.addAll() which handles the batch efficiently.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Appends all elements from the given collection to this folder's children list.
     * We lazily initialise the list if needed. This is the preferred way to populate
     * a folder in bulk — for example when the content provider hands us all the
     * attribute-type differences for a schema in one shot.
     *
     * @param c  the collection of differences to add; must not be {@code null}
     * @return   {@code true} if the children list changed as a result of the call
     */
    public boolean addAllChildren( Collection<? extends Difference> c )
    {
        if ( children == null )
        {
            children = new ArrayList<Difference>();
        }

        return children.addAll( c );
    }
}
