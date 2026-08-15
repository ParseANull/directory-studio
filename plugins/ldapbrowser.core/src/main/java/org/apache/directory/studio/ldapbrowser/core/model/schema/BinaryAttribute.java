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


// ── CLASS: BinaryAttribute — JEDI ARCHIVES BINARY DATA TAG ──────────────────
// The Jedi Archives flag certain holorecord fields as binary-only:
// userCertificate, jpegPhoto, audio — raw bytes, not readable text.
// BinaryAttribute records a single OID or name that should be treated as
// binary so the schema layer never tries to decode it as a UTF-8 string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Bean that stores the numeric OID or name of an attribute type that must be
 * treated as binary (i.e. its values are raw {@code byte[]} rather than
 * {@link String}).
 *
 * <p>Think of this as a "BINARY ONLY" tag on a Jedi Archives holorecord field —
 * just the identifier, nothing more.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BinaryAttribute
{

    private String attributeNumericOidOrName;


    // ── Archivist Stamps A Blank Binary Tag ──────────────────────────────────────
    // An archivist pulls a blank tag from the shelf — no OID yet written.
    // The tag waits in the archives until a master fills in the identifier.
    // This no-arg constructor supports serialization and config-framework use.
    // Fields default to null; always call setAttributeNumericOidOrName before use.
    /**
     * Creates a new instance of BinaryAttribute.
     */
    public BinaryAttribute()
    {
    }


    // ── Archivist Stamps A Pre-Filled Binary Tag In One Stroke ──────────────────
    // A master archivist writes the OID directly onto the tag without leaving it
    // blank — the preferred factory when the identifier is known at creation time.
    // The single field is set immediately; the tag is ready to file in the index.
    // No post-construction setter call is needed.
    /**
     * Creates a new instance of BinaryAttribute.
     *
     * @param attributeNumericOidOrName the attribute numeric OID or name
     */
    public BinaryAttribute( String attributeNumericOidOrName )
    {
        this.attributeNumericOidOrName = attributeNumericOidOrName;
    }


    // ── Archivist Reads The Binary Tag Identifier ────────────────────────────────
    // The schema engine asks: "what attribute OID is on this binary tag?"
    // The archivist reads the single field — numeric OID or human-readable name.
    // The caller matches this against incoming attribute descriptions at runtime.
    // Returns the raw identifier exactly as stored; never normalised or resolved.
    /**
     * Gets the attribute numeric OID or name.
     *
     * @return the attribute numeric OID or name
     */
    public String getAttributeNumericOidOrName()
    {
        return attributeNumericOidOrName;
    }


    // ── Archivist Overwrites The Binary Tag Identifier ──────────────────────────
    // A master archivist replaces the OID on the tag after a schema re-numbering.
    // The single field is overwritten; the old identifier is discarded.
    // Used by config frameworks and post-construction initialisation paths.
    // After this call getAttributeNumericOidOrName returns the new identifier.
    /**
     * Sets the attribute numeric OID or name.
     *
     * @param attributeNumericOidOrName the new attribute numeric OID or name
     */
    public void setAttributeNumericOidOrName( String attributeNumericOidOrName )
    {
        this.attributeNumericOidOrName = attributeNumericOidOrName;
    }

}
