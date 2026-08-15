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
package org.apache.directory.studio.apacheds.configuration.editor;


// ── CLASS: AttributeValueObject — IMPERIAL VAULT DATA CRYSTAL ────────────────────────────────────
// Deep inside Palpatine's vaults on Coruscant, every data crystal is labelled and sealed:
// one label identifies the category (attribute), the inscription holds the actual secret (value).
// You pick up the crystal, read the label, read the inscription — that's it, nothing more to it.
// This class is exactly that: a tiny, labelled data carrier pairing an LDAP attribute name
// with its corresponding string value, used in the partition details table on the config editor.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A simple data-transfer object that pairs an LDAP attribute name with its
 * string value. We use it to back the rows in the index/attribute table inside
 * {@link PartitionDetailsPage} — each row is one {@code AttributeValueObject}.
 * Think of it as a single labelled data crystal in Palpatine's vault: one label,
 * one inscription, nothing else.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeValueObject
{
    /** The attribute */
    private String attribute;

    /** The value */
    private String value;


    // ── SEALING A FRESH DATA CRYSTAL ─────────────────────────────────────────────────────────────
    // Palpatine's archivist engraves a fresh crystal: first the category label, then the secret.
    // Once sealed, the crystal sits in the vault table ready to be read or replaced.
    // We do the same: take the attribute name and the value, store them, done.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new attribute-value pair. Both sides of the pair are required
     * up front — we do not support half-initialised objects here.
     *
     * <p>For example — the archivist seals a fresh crystal:</p>
     * <pre>
     *   AttributeValueObject crystal = new AttributeValueObject("objectClass", "inetOrgPerson");
     *   // crystal is now ready to sit in the partition index table
     * </pre>
     *
     * @param attribute  The LDAP attribute name (e.g., {@code "objectClass"}).
     * @param value      The string value associated with that attribute.
     */
    public AttributeValueObject( String attribute, String value )
    {
        this.attribute = attribute;
        this.value = value;
    }


    // ── READING THE CRYSTAL'S LABEL ───────────────────────────────────────────────────────────────
    // The archivist holds up the crystal and reads aloud its category label.
    // "This one is stamped 'objectClass'." That's all getAttribute does.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attribute name stored in this pair.
     * This is the "key" side of the name-value pair — for example,
     * {@code "objectClass"} or {@code "cn"}.
     *
     * <p>For example — reading the crystal label:</p>
     * <pre>
     *   String label = crystal.getAttribute();  // "objectClass"
     * </pre>
     *
     * @return  The attribute name; never {@code null} if the object was properly constructed.
     */
    public String getAttribute()
    {
        return attribute;
    }


    // ── RE-ENGRAVING THE CRYSTAL'S LABEL ─────────────────────────────────────────────────────────
    // The archivist scratches out the old label and stamps a new category name onto the crystal.
    // The inscription (value) stays the same; only the label changes.
    // We do the same: replace the attribute name without touching the value.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the attribute name stored in this pair. Note that the method is
     * named {@code setId} for historical reasons — it still sets the attribute
     * name, not some separate id field.
     *
     * <p>For example — re-engraving the label:</p>
     * <pre>
     *   crystal.setId("cn");  // was "objectClass", now "cn"
     * </pre>
     *
     * @param attribute  The new attribute name to store.
     */
    public void setId( String attribute )
    {
        this.attribute = attribute;
    }


    // ── READING THE CRYSTAL'S INSCRIPTION ────────────────────────────────────────────────────────
    // Flip the crystal over and read the actual secret engraved on the back.
    // "The inscription reads: 'inetOrgPerson'." That's getValue.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string value stored in this pair. This is the "data" side —
     * for example, {@code "inetOrgPerson"} or {@code "example.com"}.
     *
     * <p>For example — reading the inscription:</p>
     * <pre>
     *   String inscription = crystal.getValue();  // "inetOrgPerson"
     * </pre>
     *
     * @return  The value string; never {@code null} if the object was properly constructed.
     */
    public String getValue()
    {
        return value;
    }


    // ── RE-ENGRAVING THE CRYSTAL'S INSCRIPTION ───────────────────────────────────────────────────
    // The archivist replaces the secret on the back of the crystal while keeping the label intact.
    // Old inscription is gone; the new value is now sealed.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the value stored in this pair. The attribute name is unchanged —
     * only the value side of the pair is updated.
     *
     * <p>For example — re-engraving the inscription:</p>
     * <pre>
     *   crystal.setValue("top");  // was "inetOrgPerson", now "top"
     * </pre>
     *
     * @param value  The new value string to store.
     */
    public void setValue( String value )
    {
        this.value = value;
    }


    // ── ANNOUNCING THE CRYSTAL'S CONTENTS ALOUD ──────────────────────────────────────────────────
    // The archivist reads both sides of the crystal into the log: label first, then the inscription.
    // "Attribute='objectClass', Value='inetOrgPerson'." Useful for diagnostics.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable summary of this attribute-value pair, in the
     * format {@code Attribute="name", Value="value"}. Handy for debugging
     * and for displaying in table cell tooltips.
     *
     * <p>For example — announcing the crystal:</p>
     * <pre>
     *   // prints: Attribute="objectClass", Value="inetOrgPerson"
     *   System.out.println(crystal.toString());
     * </pre>
     *
     * @return  A formatted string combining the attribute name and value.
     */
    public String toString()
    {
        return "Attribute=\"" + attribute + "\", Value=\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
