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


// ── CLASS: BinarySyntax — JEDI ARCHIVES BINARY SYNTAX TAG ───────────────────
// Some LDAP syntaxes are defined as opaque binary blobs — Certificate,
// CertificateList, OctetString — and the Jedi Archives must know not to
// decode their bytes as text.  BinarySyntax records a single syntax OID
// so the schema layer can flag it as binary at runtime.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Bean that stores the numeric OID of an LDAP syntax that must be treated as
 * binary (i.e. values of this syntax are raw {@code byte[]} rather than
 * {@link String}).
 *
 * <p>Think of this as a "BINARY ONLY" tag on a Jedi Archives syntax record —
 * just the OID, nothing more.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BinarySyntax
{

    // ── Jedi Archives Binary Syntax OID Field ────────────────────────────────────
    /** The numeric OID that identifies this binary syntax in the schema. */
    private String syntaxNumericOid;


    // ── Archivist Stamps A Blank Binary Syntax Tag ───────────────────────────────
    // An archivist pulls a blank tag — no OID yet assigned.
    // The tag waits until a master fills in the syntax OID via the setter.
    // This no-arg constructor supports serialization and config-framework use.
    // Always call setSyntaxNumericOid before relying on any getter result.
    /**
     * Creates a new instance of BinarySyntax.
     */
    public BinarySyntax()
    {
    }


    // ── Archivist Stamps A Pre-Filled Syntax Tag In One Stroke ───────────────────
    // A master archivist writes the OID onto the tag immediately — no blank state.
    // The single field is assigned at construction; the tag is ready to file.
    // This constructor is preferred when the syntax OID is known at creation time.
    // No post-construction setter call is needed.
    /**
     * Creates a new instance of BinarySyntax.
     *
     * @param syntaxNumericOid the syntax numeric OID
     */
    public BinarySyntax( String syntaxNumericOid )
    {
        this.syntaxNumericOid = syntaxNumericOid;
    }


    // ── Archivist Reads The Syntax OID From The Tag ──────────────────────────────
    // The schema engine asks: "what syntax OID is on this binary tag?"
    // The archivist reads the single field and returns it to the caller.
    // The caller matches this OID against incoming attribute syntax definitions.
    // Returns the raw OID exactly as stored; never normalised or validated.
    /**
     * Gets the syntax numeric OID.
     *
     * @return the syntax numeric OID
     */
    public String getSyntaxNumericOid()
    {
        return syntaxNumericOid;
    }


    // ── Archivist Overwrites The Syntax OID On The Tag ───────────────────────────
    // A master archivist replaces the OID after a schema re-numbering.
    // The single field is overwritten; the old OID is discarded.
    // Used by config frameworks and post-construction initialisation paths.
    // After this call getSyntaxNumericOid returns the new OID.
    /**
     * Sets the syntax numeric OID.
     *
     * @param syntaxNumericOid the new syntax numeric OID
     */
    public void setSyntaxNumericOid( String syntaxNumericOid )
    {
        this.syntaxNumericOid = syntaxNumericOid;
    }

}
