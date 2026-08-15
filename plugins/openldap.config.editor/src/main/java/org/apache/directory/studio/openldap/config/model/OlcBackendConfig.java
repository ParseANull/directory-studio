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
package org.apache.directory.studio.openldap.config.model;


// ── CLASS: OlcBackendConfig — THE DEATH STAR ENGINEERING SECTION BLUEPRINT ───
// Grand Moff Tarkin reviews the section of the Death Star plans that covers the
// superlaser power coupling — just the backend hardware type, not the full station.
// This class is exactly that: the single-field blueprint for an OpenLDAP backend
// type declaration (e.g. "bdb", "mdb", "ldap"), which sits under cn=config as its
// own LDAP entry before any databases hang off it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcBackendConfig} LDAP object class — it models the
 * top-level backend-type declaration entry in OpenLDAP's cn=config tree.
 * Think of it as the schematic page that says "we're using the MDB backend" before
 * any specific databases are configured.
 * Used by ConfigurationReader to deserialize the backend entry, and by the editor
 * pages to let users pick and inspect the backend type.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcBackendConfig extends OlcConfig
{
    /**
     * Field for the 'olcBackend' attribute.
     */
    @ConfigurationElement(attributeType = "olcBackend", isOptional = false, version="2.4.0")
    private String olcBackend;


    // ── Empty Blueprint Ready For Assembly ───────────────────────────────────────
    // The Death Star construction crew lays out a fresh set of schematics — nothing
    // filled in yet, just the blank form for a new backend type entry.
    // We need this no-arg constructor so the I/O layer can instantiate the bean
    // reflectively and then populate it field by field from the LDAP entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty OlcBackendConfig instance with no backend type set yet.
     * The I/O layer calls this before calling setters to populate from LDAP.
     *
     * <p>For example — the construction crew unrolls a blank schematic:</p>
     * <pre>
     *   Officer: "Start with a clean sheet — backend type TBD."
     *   new OlcBackendConfig()  // all fields null until the reader fills them in
     * </pre>
     */
    public OlcBackendConfig()
    {
    }


    // ── Copying The Blueprint Before Modification ─────────────────────────────────
    // The Rebel spies make a duplicate of the Death Star schematics before handing
    // the original to Leia, so both sides have an independent copy to work from.
    // We do the same here — produce a fully independent copy so callers can edit
    // one instance without accidentally mutating the other.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Copy constructor — creates a new OlcBackendConfig that's an independent clone
     * of the given instance. Changes to the copy won't affect the original.
     *
     * <p>For example — Rebel spies duplicate the schematics:</p>
     * <pre>
     *   OlcBackendConfig original = reader.readBackend(entry);
     *   OlcBackendConfig editCopy = new OlcBackendConfig(original);
     *   editCopy.setOlcBackend("mdb");  // original is untouched
     * </pre>
     *
     * @param o  the instance to copy from — its olcBackend value is copied verbatim
     */
    public OlcBackendConfig( OlcBackendConfig o )
    {
        olcBackend = o.olcBackend;
    }


    // ── Reading The Backend Type From The Schematic ───────────────────────────────
    // Tarkin reads off the power-coupling spec label from the schematic — it just
    // says "turbolaser array", nothing more. That label is what drives all downstream
    // decisions about what hardware to order and how to wire it up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the backend type string stored in the {@code olcBackend} attribute —
     * something like "bdb", "mdb", "ldap", or "monitor".
     * This is the single required attribute that distinguishes one backend-type entry
     * from another.
     *
     * <p>For example — Tarkin reads the section label:</p>
     * <pre>
     *   String type = config.getOlcBackend();  // "mdb"
     *   // Now we know: this backend section is for the MDB storage engine.
     * </pre>
     *
     * @return  the backend type string, or {@code null} if not yet set
     */
    public String getOlcBackend()
    {
        return olcBackend;
    }


    // ── Stamping The Backend Type Onto The Blueprint ──────────────────────────────
    // The chief engineer stamps the official backend designation on the schematic
    // cover — "MDB STORAGE ENGINE" — so every construction team knows what they're
    // building. Without this stamp, the blueprint is incomplete.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the backend type string, which maps to the {@code olcBackend} LDAP attribute.
     * This is the single required field on this bean — the reader sets it from the
     * entry's RDN attribute value after instantiating the bean.
     *
     * <p>For example — the engineer stamps the designation:</p>
     * <pre>
     *   OlcBackendConfig cfg = new OlcBackendConfig();
     *   cfg.setOlcBackend("mdb");  // now the entry's type is declared
     * </pre>
     *
     * @param olcBackend  the backend type string to set, e.g. "mdb" or "bdb"
     */
    public void setOlcBackend( String olcBackend )
    {
        this.olcBackend = olcBackend;
    }


    // ── Duplicating The Schematic For Safe Keeping ────────────────────────────────
    // Before the Rebels transmit the Death Star plans, they make a verified copy to
    // keep in the archive — just in case R2's mission goes sideways.
    // Our copy() method does the same: produces a clean independent clone of this
    // config bean for use in undo/redo stacks or diff comparisons.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns an independent copy of this OlcBackendConfig.
     * Useful for the editor's undo stack, or anywhere we need a snapshot of the
     * current state before making changes.
     *
     * <p>For example — the archive clerk duplicates the schematic:</p>
     * <pre>
     *   OlcBackendConfig snapshot = config.copy();
     *   // snapshot is independent — edits to config don't affect it
     * </pre>
     *
     * @return  a new OlcBackendConfig with the same field values as this instance
     */
    public OlcBackendConfig copy()
    {
        return new OlcBackendConfig( this );
    }
}
