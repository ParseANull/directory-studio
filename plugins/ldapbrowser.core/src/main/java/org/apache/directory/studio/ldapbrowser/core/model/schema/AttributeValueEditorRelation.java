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


// ── CLASS: AttributeValueEditorRelation — JEDI ARCHIVES EDITOR ASSIGNMENT ────
// The Jedi Archives know that some attribute types need special reading glasses:
// jpegPhoto needs an image viewer, userCertificate needs a certificate reader.
// AttributeValueEditorRelation maps an attribute OID or name to the class name
// of the value editor plugin that handles it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Maps an attribute type (identified by numeric OID or name) to the class name
 * of its value editor.  Used by the schema to look up the correct UI editor
 * component for a given attribute type.
 *
 * <p>Think of this as the Jedi Archives' editor assignment slip — "attribute X
 * gets editor class Y".</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeValueEditorRelation
{

    /** The attribute, either the numeric OID or type. */
    private String attributeNumericOidOrType;

    /** The value editor class name. */
    private String valueEditorClassName;


    // ── Jedi Archivist Creates A Blank Relation Record ───────────────────────────
    // An initiate Archivist stamps a blank assignment slip — no attribute, no editor.
    // The slip sits waiting in the archives until a master fills it in.
    // This no-arg constructor exists so serialization and config frameworks can
    // round-trip the bean without needing to know the field values up front.
    /**
     * Creates a new instance of AttributeValueEditorRelation.
     */
    public AttributeValueEditorRelation()
    {
    }


    // ── Jedi Archivist Records A Fully Assigned Editor Relation ─────────────────
    // A master archivist writes the full assignment slip in one stroke:
    // attribute OID on the left, editor class name on the right.
    // Both fields are stamped immediately, avoiding any partially-filled state.
    // This constructor is the preferred factory when both values are known.
    /**
     * Creates a new instance of AttributeValueEditorRelation.
     *
     * @param attributeNumericOidOrName the attribute numeric OID or name
     * @param valueEditorClassName the value editor class name
     */
    public AttributeValueEditorRelation( String attributeNumericOidOrName, String valueEditorClassName )
    {
        this.attributeNumericOidOrType = attributeNumericOidOrName;
        this.valueEditorClassName = valueEditorClassName;
    }


    // ── Archivist Reads The Attribute Identifier From The Assignment Slip ────────
    // The master queries: "which attribute does this editor assignment cover?"
    // The archivist reads the left column of the slip — numeric OID or type name.
    // The caller uses this to match the relation against an incoming attribute.
    // Returns the raw identifier string exactly as stored; never normalised.
    /**
     * Gets the attribute numeric OID or type.
     *
     * @return the attribute numeric OID or type
     */
    public String getAttributeNumericOidOrType()
    {
        return attributeNumericOidOrType;
    }


    // ── Archivist Updates The Left Column Of The Assignment Slip ─────────────────
    // An archivist corrects the attribute column after a schema reorganisation.
    // The old OID is crossed out and the new identifier is written in its place.
    // All reads via getAttributeNumericOidOrType will now return the new value.
    // This setter supports config-framework round-trips and post-construction init.
    /**
     * Sets the attribute numeric OID or type.
     *
     * @param attributeNumericOidOrType the new attribute numeric OID or type
     */
    public void setAttributeNumericOidOrType( String attributeNumericOidOrType )
    {
        this.attributeNumericOidOrType = attributeNumericOidOrType;
    }


    // ── Archivist Reads The Editor Class Name From The Assignment Slip ────────────
    // The master asks: "which editor class handles this attribute?"
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


    // ── Archivist Updates The Editor Class On The Assignment Slip ────────────────
    // A master archivist swaps the editor class — say, from a generic text editor
    // to a specialised certificate viewer — after a plugin upgrade.
    // The right column of the slip is overwritten with the new class name.
    // All subsequent getValueEditorClassName calls return the updated value.
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
