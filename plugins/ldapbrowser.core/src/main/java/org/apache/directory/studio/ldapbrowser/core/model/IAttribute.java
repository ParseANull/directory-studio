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

package org.apache.directory.studio.ldapbrowser.core.model;


import java.io.Serializable;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.connection.core.ConnectionPropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.AttributePropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.EntryPropertyPageProvider;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: IAttribute — A SINGLE TECHNICAL SPEC LINE IN THE DEATH STAR BLUEPRINT
// Inside the Death Star blueprint each section lists individual specifications:
// "Superlaser — power output: 2.4 × 10^32 W", "Hull — material: quadanium steel."
// Each spec line has a name (attribute description), may repeat multiple times
// (multi-valued attributes), and its data could be a plain string or raw binary.
// IAttribute is that spec line: it belongs to one {@link IEntry} (the section),
// has a type name (e.g. "mail"), and holds one or more {@link IValue}s.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single LDAP attribute within an {@link IEntry}.
 * An attribute has a name (its description/type, e.g. {@code "mail"}), optional
 * schema options (e.g. language tags like {@code "lang-en"}), and one or more
 * {@link IValue}s.  Values may be strings or raw binary data.
 * Think of this as a spec line in the Death Star blueprint: named, typed,
 * potentially repeated, and owned by a specific section (entry).
 */
public interface IAttribute extends Serializable, IAdaptable, AttributePropertyPageProvider, EntryPropertyPageProvider,
    ConnectionPropertyPageProvider
{
    /** The options delimiter used between the attribute type and any options, e.g. {@code "mail;lang-en"}. */
    String OPTION_DELIMITER = ";"; //$NON-NLS-1$

    /** The prefix for language-tag options, e.g. {@code "lang-en"}. */
    String OPTION_LANG_PREFIX = "lang-"; //$NON-NLS-1$


    // ── The Spec Line Points Back To Its Blueprint Section ────────────────────────
    // Every spec line knows which section of the blueprint it belongs to —
    // "Turbolaser spec, section: Weapons Bay, hull number DS-1."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry that owns this attribute.
     * An attribute always belongs to exactly one entry; this lets you navigate
     * from an attribute back up to its entry without keeping a separate reference.
     *
     * @return the owning {@link IEntry}; never {@code null}.
     */
    IEntry getEntry();


    // ── Mace Windu Checks Whether The Spec Is Internally Valid ───────────────────
    // Mace Windu doesn't accept a weapons spec that lists zero cannons or one
    // cannon with a blank power value — both are inconsistent.  He demands at
    // least one real, non-empty value before approving the spec for the record.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute is internally consistent.
     * Consistency requires:
     * <ul>
     *   <li>At least one value present.</li>
     *   <li>No empty (placeholder) values remaining.</li>
     * </ul>
     * An inconsistent attribute should not be written to the LDAP server.
     *
     * @return {@code true} if the attribute passes the consistency check.
     */
    boolean isConsistent();


    // ── The Jedi Archives Say This Spec Is Mandatory ──────────────────────────────
    // The Death Star's design standard mandates certain specs for every weapon
    // system section — you can't submit a weapons spec without a power rating.
    // In LDAP terms, a "must" attribute is required by the entry's objectClass.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute is a MUST attribute according to
     * the entry's schema objectClasses.
     * MUST attributes are mandatory — the entry would be schema-invalid without them.
     *
     * @return {@code true} if this is a mandatory attribute.
     */
    boolean isMustAttribute();


    // ── The Jedi Archives Say This Spec Is Optional ───────────────────────────────
    // Some specs are listed as "if applicable" — a weapons section might optionally
    // include a maintenance schedule.  A "may" attribute is optional in the schema.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute is a MAY attribute according to
     * the entry's schema objectClasses.
     * MAY attributes are optional in the schema but allowed.
     *
     * @return {@code true} if this is an optional (MAY) attribute.
     */
    boolean isMayAttribute();


    // ── The Jedi Archives Mark This Spec As An Internal Maintenance Record ────────
    // Operational attributes are the LDAP server's own housekeeping data —
    // {@code createTimestamp}, {@code modifyTimestamp}, {@code entryDN}.
    // They're generated by the server, not by users.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute is an operational attribute
     * (defined in the schema with USAGE = directoryOperation, distributedOperation,
     * or dSAOperation).
     * Operational attributes are managed by the server rather than users.
     *
     * @return {@code true} if this is an operational attribute.
     */
    boolean isOperationalAttribute();


    // ── The Spec Line Is The Blueprint's Type Registry ────────────────────────────
    // The {@code objectClass} attribute is special — it declares what kind of
    // directory object this entry is.  Everything else flows from it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute is the {@code objectClass} attribute.
     * The objectClass attribute is special: it determines which other attributes
     * are mandatory or optional for the entry.
     *
     * @return {@code true} if this is the objectClass attribute.
     */
    boolean isObjectClassAttribute();


    // ── The Spec Line Holds Text Data ────────────────────────────────────────────
    // Some specs are human-readable text: "Name: Grand Moff Tarkin."
    // Others are raw bytes (e.g. a photo or a certificate).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute's values are string-typed
     * (not binary).  The determination is based on the schema syntax.
     *
     * @return {@code true} if values are strings.
     */
    boolean isString();


    // ── The Spec Line Holds Binary Data ──────────────────────────────────────────
    // "Photo: [binary blob]" — some specs are raw bytes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute's values are binary (byte[]).
     * The determination is based on the schema syntax or explicit binary option.
     *
     * @return {@code true} if values are binary.
     */
    boolean isBinary();


    // ── R2-D2 Opens An Empty Slot In The Spec For A New Value ────────────────────
    // The droid creates a blank entry field in the spec — "value [n+1]: __________"
    // — so the user can type into it.  This is a UI placeholder, not yet on the server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an empty (placeholder) {@link IValue} to this attribute.
     * The empty value represents an editable row in the UI that the user
     * has not yet filled in.  It is not written to the LDAP server.
     */
    void addEmptyValue();


    // ── R2-D2 Removes The Empty Slot If The User Didn't Fill It ──────────────────
    // If the user pressed Escape, the blank field needs to disappear.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes one empty placeholder value from this attribute, if any is present.
     * Called when the user cancels an in-progress value edit.
     */
    void deleteEmptyValue();


    // ── R2-D2 Adds A Real Value Into The Spec ────────────────────────────────────
    // "Value added: han.solo@rebelbase.org."  A real, non-empty value is written
    // into the spec line.  It must belong to this attribute.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends the given value to this attribute's value list.
     *
     * <p>For example — adding a second email address:</p>
     * <pre>
     *   mailAttr.addValue(new Value(mailAttr, "han@falcon.org"));
     * </pre>
     *
     * @param valueToAdd the value to add; must not be {@code null} and its attribute
     *                   must be this attribute.
     * @throws IllegalArgumentException if {@code valueToAdd} is {@code null} or belongs
     *                                  to a different attribute.
     */
    void addValue( IValue valueToAdd ) throws IllegalArgumentException;


    // ── R2-D2 Removes A Value From The Spec ──────────────────────────────────────
    // "Value removed: old.email@empire.gov."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given value from this attribute's value list.
     *
     * @param valueToDelete the value to remove; must not be {@code null} and
     *                      must belong to this attribute.
     * @throws IllegalArgumentException if {@code valueToDelete} is {@code null}
     *                                  or belongs to a different attribute.
     */
    void deleteValue( IValue valueToDelete ) throws IllegalArgumentException;


    // ── R2-D2 Replaces An Existing Value With A New One ──────────────────────────
    // "Superlaser power rating: updated from 2.4 × 10^32 W to 2.4 × 10^33 W."
    // The old value is swapped out in-place for the new one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces {@code oldValue} with {@code newValue} in this attribute.
     * This is used by the value-editing UI to apply an in-place edit without
     * removing and re-adding.
     *
     * @param oldValue the existing value to replace; must belong to this attribute.
     * @param newValue the replacement value; must belong to this attribute.
     * @throws IllegalArgumentException if either value is {@code null} or belongs
     *                                  to a different attribute.
     */
    void modifyValue( IValue oldValue, IValue newValue ) throws IllegalArgumentException;


    // ── R2-D2 Lists All Values In The Spec ───────────────────────────────────────
    // "All turbolaser power values: [2.4e32, 2.4e33, ...]"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all values of this attribute.
     * The array may be empty (e.g. just after {@link #addEmptyValue()}) but is
     * never {@code null}.
     *
     * @return the attribute's values; empty array if none, never {@code null}.
     */
    IValue[] getValues();


    // ── The Spec Reports How Many Values It Has ───────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of values currently held by this attribute,
     * including any empty placeholder values.
     *
     * @return the value count; 0 or greater.
     */
    int getValueSize();


    // ── R2-D2 Reads The Spec Line's Full Label ────────────────────────────────────
    // The description includes the attribute type AND any options:
    // {@code "mail"} or {@code "mail;lang-en"}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full attribute description, including the type and any
     * options (e.g. language tags).  For example: {@code "mail"} or
     * {@code "description;lang-de"}.
     *
     * @return the attribute description string; never {@code null}.
     */
    String getDescription();


    // ── R2-D2 Reads Only The Spec Line's Type Name ────────────────────────────────
    // Just the type, without options: {@code "mail"} rather than {@code "mail;lang-en"}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type without any options.
     * For example, if the description is {@code "mail;lang-en"}, this returns {@code "mail"}.
     *
     * @return the attribute type name; never {@code null}.
     */
    String getType();


    // ── R2-D2 Exports All Values As Raw Bytes ────────────────────────────────────
    // When the spec is a photo or a certificate, the bytes are what matter.
    // String values are UTF-8 encoded before being returned.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all values as binary ({@code byte[]}).
     * String values are converted to bytes using UTF-8 encoding.
     *
     * @return the binary representation of all values; never {@code null}.
     */
    byte[][] getBinaryValues();


    // ── R2-D2 Reads The First String Value From The Spec ─────────────────────────
    // Quick access to a single-valued attribute's text — "just give me the name."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first value of this attribute as a string, or {@code null}
     * if there are no values.
     * Binary values are converted to strings using UTF-8 encoding.
     *
     * @return the first string value, or {@code null} if no values exist.
     */
    String getStringValue();


    // ── R2-D2 Exports All Values As Strings ──────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all values as strings.
     * Binary values are converted using UTF-8 encoding.
     *
     * @return all string values; never {@code null}.
     */
    String[] getStringValues();


    // ── Mace Windu Decides Whether Two Spec Lines Are The Same ───────────────────
    // Two specs are equal if they belong to the same entry and have the same
    // attribute description — regardless of their current values.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given object is an {@link IAttribute} with the
     * same entry and the same attribute description as this one.
     *
     * @param o the object to compare; should be an {@link IAttribute}.
     * @return {@code true} if equal by entry + description.
     */
    boolean equals( Object o );


    // ── The Jedi Archives Look Up The Full Schema Definition ──────────────────────
    // Beyond the spec line's name there's a full schema definition: syntax OID,
    // equality matching rule, single-value flag, etc.  This fetches that record
    // from the schema model (the Jedi Archives).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AttributeType} schema definition for this attribute.
     * Used to determine syntax, matching rules, and other schema properties.
     * May return a default or dummy type if no schema is available.
     *
     * @return the {@link AttributeType}; never {@code null}.
     */
    AttributeType getAttributeTypeDescription();


    // ── The Jedi Archives Return The Parsed Attribute Description ─────────────────
    // The {@link AttributeDescription} is a parsed object wrapping the type name
    // and options, making it easy to compare or manipulate programmatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parsed {@link AttributeDescription} for this attribute.
     * Provides structured access to the type name and any options (e.g. language tags).
     *
     * @return the {@link AttributeDescription}; never {@code null}.
     */
    AttributeDescription getAttributeDescription();
}
