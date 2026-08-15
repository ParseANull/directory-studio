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

package org.apache.directory.studio.ldapbrowser.core.model.schema;


// ── CLASS: SyntaxValueEditorRelation — JEDI ARCHIVES SYNTAX EDITOR ASSIGNMENT ─
// Some LDAP syntaxes need specialist editors — GeneralizedTime needs a calendar,
// OctetString needs a hex viewer.  The Jedi Archives hold assignment slips that
// pair a syntax OID with the editor class name that handles it.
// SyntaxValueEditorRelation is one such slip: OID on the left, class on the right.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Maps an LDAP syntax (identified by numeric OID) to the class name of the
 * value editor that handles it.  Used by the schema layer to look up the
 * correct UI editor component for a given attribute syntax.
 *
 * <p>Think of this as a Jedi Archives editor assignment slip for syntaxes —
 * "syntax OID X gets editor class Y".</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyntaxValueEditorRelation
{

    /** The syntax OID. */
    private String syntaxOID;

    private String valueEditorClassName;


    // ── Archivist Stamps A Blank Syntax Relation Slip ────────────────────────────
    // An archivist pulls a blank assignment slip — no syntax OID, no editor yet.
    // The slip waits until a master fills in both fields via the setters.
    // This no-arg constructor supports serialization and config-framework use.
    // Always populate both fields before relying on any getter result.
    /**
     * Creates a new instance of SyntaxValueEditorRelation.
     */
    public SyntaxValueEditorRelation()
    {
    }


    // ── Archivist Files A Fully Completed Syntax Relation Slip ──────────────────
    // A master archivist writes OID and editor class onto the slip in one stroke.
    // Both fields are assigned immediately; the slip is ready for the index.
    // This two-arg constructor is preferred when both values are known at creation.
    // No post-construction setter calls are needed.
    /**
     * Creates a new instance of SyntaxValueEditorRelation.
     *
     * @param syntaxOID the syntax OID
     * @param valueEditorClassName the value editor class name
     */
    public SyntaxValueEditorRelation( String syntaxOID, String valueEditorClassName )
    {
        this.syntaxOID = syntaxOID;
        this.valueEditorClassName = valueEditorClassName;
    }


    // ── Archivist Reads The Syntax OID From The Assignment Slip ─────────────────
    // The schema engine queries: "which syntax OID does this relation cover?"
    // The archivist reads the left column of the slip and returns the raw OID.
    // The caller matches this OID against an attribute's syntax at runtime.
    // Returns the OID exactly as stored; never normalised or dereferenced.
    /**
     * Gets the syntax OID.
     *
     * @return the syntax OID
     */
    public String getSyntaxOID()
    {
        return syntaxOID;
    }


    // ── Archivist Updates The Left Column Of The Syntax Relation Slip ────────────
    // A master archivist corrects the OID after a schema re-numbering.
    // The old OID is crossed out and the new value is written in its place.
    // Used by config frameworks and post-construction initialisation paths.
    // After this call getSyntaxOID returns the updated OID.
    /**
     * Sets the syntax OID.
     *
     * @param syntaxOID the new syntax OID
     */
    public void setSyntaxOID( String syntaxOID )
    {
        this.syntaxOID = syntaxOID;
    }


    // ── Archivist Reads The Editor Class From The Syntax Relation Slip ───────────
    // The schema engine queries: "which editor class handles this syntax?"
    // The archivist reads the right column — the fully-qualified class name.
    // The UI uses this string to instantiate the correct editor via reflection.
    // Returns the raw class name exactly as stored; never resolved to a Class.
    /**
     * Gets the value editor class name.
     *
     * @return the value editor class name
     */
    public String getValueEditorClassName()
    {
        return valueEditorClassName;
    }


    // ── Archivist Swaps The Editor Class On The Syntax Relation Slip ────────────
    // A master archivist upgrades the editor class — say after a plugin update.
    // The old class name is discarded and the new fully-qualified name is stored.
    // All subsequent getValueEditorClassName calls return the updated value.
    // Config frameworks use this setter during post-construction initialisation.
    /**
     * Sets the value editor class name.
     *
     * @param valueEditorClassName the new value editor class name
     */
    public void setValueEditorClassName( String valueEditorClassName )
    {
        this.valueEditorClassName = valueEditorClassName;
    }

}
