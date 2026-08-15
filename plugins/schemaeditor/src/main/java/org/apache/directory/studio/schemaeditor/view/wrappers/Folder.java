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
package org.apache.directory.studio.schemaeditor.view.wrappers;


import org.eclipse.osgi.util.NLS;


// ── CLASS: Folder — Lando Running Cloud City ──────────────────────────────────
// Lando Calrissian runs Bespin's Cloud City like a well-oiled machine: distinct
// departments — mining operations, security, guest services — each clearly labelled,
// each housed in its own section of the floating station, each with its own
// population of people and droids. He knows which section does what, keeps them
// separate, and can name every one.
// Folder is exactly that: a named, typed container node in the schema tree that
// groups related items together — attribute types here, object classes there,
// errors in the red wing, warnings in the amber wing — so the TreeViewer can
// present an organised, navigable hierarchy instead of one flat pile.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a labelled grouping node in the Schema Editor's JFace TreeViewer.
 * Rather than dumping all attribute types and object classes into one list,
 * we organise them into Folders — an "Attribute Types" folder, an "Object Classes"
 * folder, an "Errors" folder, and a "Warnings" folder — giving the user a clear
 * hierarchy to navigate.
 * Think of Lando's Cloud City departments: each Folder has a type (what kind of
 * items it holds) and a display name (what the TreeViewer labels the row), and
 * it inherits the full parent-child machinery from {@link AbstractTreeNode}.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Folder extends AbstractTreeNode
{
    /**
     * Enumerates the kinds of content a Folder can contain.
     * Lando's departments: the gas-mining wing, security, administration, or
     * something custom. Each type maps to a different display label and icon
     * in the Schema Editor's tree views.
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


    // ── Lando Opens a New Department with a Standard Name ────────────────────
    // When Cloud City expands, Lando opens a new wing and gives it the standard
    // departmental name from the Bespin Corporate Charter — no custom branding,
    // just the canonical label that every station in the Bespin system uses.
    // This constructor creates a Folder with the localised default name for its type.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a Folder with a standard, localised display name derived from its type.
     * The name is looked up from the message bundle — "Attribute Types", "Object Classes",
     * "Errors", or "Warnings" — so it respects the user's locale.
     * For {@link FolderType#NONE} no name is set and it stays as an empty string.
     *
     * <p>For example — Lando opening the gas-mining department under its standard charter name:</p>
     * <pre>
     *   Folder atFolder = new Folder( FolderType.ATTRIBUTE_TYPE, schemaWrapper );
     *   atFolder.getName();  // "Attribute Types" (from messages.properties)
     * </pre>
     *
     * @param type    the kind of content this folder will hold; determines the display label
     * @param parent  the parent {@link TreeNode} in the tree (typically a {@link SchemaWrapper})
     */
    public Folder( FolderType type, TreeNode parent )
    {
        super( parent );
        this.type = type;

        switch ( type )
        {
            case ATTRIBUTE_TYPE:
                name = Messages.getString( "Folder.AttributeTypes" ); //$NON-NLS-1$
                break;
            case OBJECT_CLASS:
                name = Messages.getString( "Folder.ObjectClasses" ); //$NON-NLS-1$
                break;
            case ERROR:
                name = Messages.getString( "Folder.Errors" ); //$NON-NLS-1$
                break;
            case WARNING:
                name = Messages.getString( "Folder.Warnings" ); //$NON-NLS-1$
                break;
            default:
                break;
        }
    }


    // ── Lando Opens a Custom Department with a Hand-Picked Name ──────────────
    // Sometimes Lando opens a special-purpose wing that doesn't fit any standard
    // charter category — maybe an experimental research section — so he gives it
    // a custom name instead of a default one.
    // This constructor creates a Folder with a caller-supplied display name.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a Folder with a specific type and a custom display name.
     * Use this when you need a folder that doesn't use the standard localised label —
     * for example a schema-named folder or a folder whose label changes at runtime.
     *
     * <p>For example — Lando naming a custom wing of Cloud City:</p>
     * <pre>
     *   Folder customFolder = new Folder( FolderType.NONE, "Experimental", parentNode );
     *   customFolder.getName();  // "Experimental"
     * </pre>
     *
     * @param type    the kind of content this folder holds
     * @param name    the display label to show in the TreeViewer row
     * @param parent  the parent {@link TreeNode} in the tree hierarchy
     */
    public Folder( FolderType type, String name, TreeNode parent )
    {
        super( parent );
        this.type = type;
        this.name = name;
    }


    // ── Checking Which Department This Wing Belongs To ────────────────────────
    // A visitor to Cloud City asks: "Which department is this?" Lando points to
    // the door sign — gas mining, security, whatever it is — and the visitor
    // knows exactly what kind of work happens here.
    // getType() tells callers whether this folder holds attribute types, object classes,
    // errors, warnings, or something else entirely.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link FolderType} that classifies what this folder contains.
     * Label providers and content providers use this to decide which icon to show
     * and which child types to expect inside the folder.
     *
     * <p>For example — the visitor reading the department sign in Cloud City:</p>
     * <pre>
     *   FolderType t = folder.getType();  // FolderType.ATTRIBUTE_TYPE
     * </pre>
     *
     * @return  the type of this folder; never {@code null}
     */
    public FolderType getType()
    {
        return type;
    }


    // ── Reading the Wing's Name Plate ─────────────────────────────────────────
    // Every department in Cloud City has a brass name plate on the door:
    // "Gas Mining Operations," "Security," "Guest Services."
    // getName() returns that plate — the string the TreeViewer displays in the row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this folder as it appears in the TreeViewer.
     * This is what the user actually reads in the tree row, so it is localised
     * (for the standard constructor) or set by the caller (for the custom constructor).
     *
     * <p>For example — reading the brass name plate on Cloud City's gas-mining wing:</p>
     * <pre>
     *   String label = folder.getName();  // "Attribute Types"
     * </pre>
     *
     * @return  the display name of this folder; never {@code null}
     */
    public String getName()
    {
        return name;
    }


    // ── Is This the Same Cloud City Department? ───────────────────────────────
    // Two visitors describe "the same department" — but Lando checks: same parent
    // station, same department type, same name plate. All three must match.
    // equals() verifies parent (via getParent()), type, and name in sequence.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a {@link Folder} with the same parent,
     * the same {@link FolderType}, and the same display name.
     * All three must match — a "Warnings" folder in one schema is not the same as
     * a "Warnings" folder in a different schema even if their names look identical.
     *
     * <p>For example — Lando confirming that two wing descriptions refer to the same department:</p>
     * <pre>
     *   folder1.equals( folder2 );
     *   // true only if same parent, same type, same name
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if the folders are structurally identical in the tree
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof Folder )
        {
            Folder folder = ( Folder ) obj;

            if ( ( getParent() != null ) && ( !getParent().equals( folder.getParent() ) ) )
            {
                return false;
            }

            if ( !getType().equals( folder.getType() ) )
            {
                return false;
            }

            if ( ( getName() != null ) && ( !getName().equals( folder.getName() ) ) )
            {
                return false;
            }

            return true;
        }

        return false;
    }


    // ── Cloud City's Unique Wing Identifier ───────────────────────────────────
    // Every wing of Cloud City has a unique identifier in the station's management
    // system — encoded from the wing name and its department type, plus which
    // station it belongs to. hashCode() does the same for Folder nodes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code that factors in the parent's hash (from {@code super.hashCode()}),
     * the folder name, and the folder type.
     * Consistent with {@link #equals}: two equal folders produce the same hash.
     *
     * <p>For example — Cloud City's unique wing identifier, derived from name, type, and station:</p>
     * <pre>
     *   int hash = folder.hashCode();
     *   // Combines parent hash, name hash, and type hash
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = super.hashCode();

        if ( name != null )
        {
            result = 37 * result + name.hashCode();
        }

        if ( type != null )
        {
            result = 37 * result + type.hashCode();
        }

        return result;
    }


    // ── Lando Describes the Wing Over the Comm System ────────────────────────
    // When a ship asks Cloud City traffic control "what department are you routing
    // us to?", the controller reads out the wing type and parent station.
    // toString() gives the same overview for debuggers and log output.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this folder including its type and parent node.
     * Useful for log output and debugging tree structure problems — you can see at a glance
     * what kind of folder this is and where it sits in the hierarchy.
     *
     * <p>For example — the traffic controller identifying the destination wing:</p>
     * <pre>
     *   System.out.println( folder );
     *   // "Folder[ATTRIBUTE_TYPE, parent=coreSchemaWrapper]"
     * </pre>
     *
     * @return  a formatted string describing this folder's type and parent context
     */
    public String toString()
    {
        return NLS.bind( Messages.getString( "Folder.Folder" ), new Object[] { type, fParent } ); //$NON-NLS-1$
    }
}
