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

import org.apache.directory.studio.connection.core.ConnectionPropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.AttributePropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.EntryPropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.ValuePropertyPageProvider;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: IValue — A SINGLE DATA POINT IN THE DEATH STAR BLUEPRINT ──────────
// Zooming further into the Death Star blueprint: each technical spec line
// (IAttribute) can have multiple data points listed next to it —
// "Turbolaser power: [2.4e32 W]  [2.4e33 W (enhanced)]".
// IValue is one of those data points.  It knows its parent spec line
// (the attribute), its raw content (string or binary), and whether it's
// actually filled in or just an empty editing placeholder.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single value held by an {@link IAttribute}.
 * An attribute may have multiple values (e.g. a user with three email addresses).
 * Each value is an {@code IValue}: it holds the raw data (string or binary),
 * knows its owning attribute, and can report whether it is empty (a placeholder
 * the user hasn't typed into yet) or whether it forms part of the entry's RDN.
 * Think of this as one data-point cell inside a spec line of the Death Star blueprint.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IValue extends Serializable, IAdaptable, ValuePropertyPageProvider, AttributePropertyPageProvider,
    EntryPropertyPageProvider, ConnectionPropertyPageProvider
{
    // ── CLASS: EmptyValue — THE BLANK FIELD WAITING FOR INPUT ────────────────────
    // R2-D2 opens a new empty slot in the schematic — the connector is seated,
    // the label is ready, but no data has been transferred yet.  The slot
    // reports itself as empty (string or binary type) and returns "" or new byte[0].
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Marker interface for empty placeholder values.
     * An empty value exists only in the in-memory model to give the UI an
     * editable row; it is never written to the LDAP server.
     * Two singletons cover the two common cases:
     * {@link IValue#EMPTY_STRING_VALUE} and {@link IValue#EMPTY_BINARY_VALUE}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    interface EmptyValue
    {
        /**
         * Returns the empty string value — always {@code ""}.
         *
         * @return the string form of this empty value.
         */
        String getStringValue();


        /**
         * Returns the empty binary value — always {@code new byte[0]}.
         *
         * @return the binary form of this empty value.
         */
        byte[] getBinaryValue();


        /**
         * Returns {@code true} if this is a string-typed empty value.
         *
         * @return {@code true} for string-typed placeholders.
         */
        boolean isString();


        /**
         * Returns {@code true} if this is a binary-typed empty value.
         *
         * @return {@code true} for binary-typed placeholders.
         */
        boolean isBinary();
    }

    /**
     * Singleton empty-string placeholder.
     * Used when the attribute's syntax is string-based and the user has added
     * a new value row but not yet typed anything.
     */
    EmptyValue EMPTY_STRING_VALUE = new EmptyValue()
    {

        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return BrowserCoreMessages.model__empty_string_value;
        }


        /**
         * {@inheritDoc}
         */
        public boolean isString()
        {
            return true;
        }


        /**
         * {@inheritDoc}
         */
        public boolean isBinary()
        {
            return false;
        }


        /**
         * {@inheritDoc}
         */
        public byte[] getBinaryValue()
        {
            return new byte[0];
        }


        /**
         * {@inheritDoc}
         */
        public String getStringValue()
        {
            return ""; //$NON-NLS-1$
        }
    };

    /**
     * Singleton empty-binary placeholder.
     * Used when the attribute's syntax is binary and the user has added
     * a new value row but not yet provided any data.
     */
    EmptyValue EMPTY_BINARY_VALUE = new EmptyValue()
    {

        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return BrowserCoreMessages.model__empty_binary_value;
        }


        /**
         * {@inheritDoc}
         */
        public boolean isString()
        {
            return false;
        }


        /**
         * {@inheritDoc}
         */
        public boolean isBinary()
        {
            return true;
        }


        /**
         * {@inheritDoc}
         */
        public byte[] getBinaryValue()
        {
            return new byte[0];
        }


        /**
         * {@inheritDoc}
         */
        public String getStringValue()
        {
            return ""; //$NON-NLS-1$
        }
    };


    // ── The Data Point Identifies Its Parent Spec Line ────────────────────────────
    // Every data point in the blueprint says "I belong to the turbolaser spec
    // on the weapons section."  This navigates back to that parent attribute.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute that owns this value.
     * Navigation path: value → attribute → entry → connection.
     *
     * @return the owning {@link IAttribute}; never {@code null}.
     */
    IAttribute getAttribute();


    // ── R2-D2 Returns The Raw Data Object ────────────────────────────────────────
    // The raw value is either a String, a byte[], or an {@link EmptyValue} sentinel.
    // Don't call this if you just want the string or binary representation — use
    // {@link #getStringValue()} or {@link #getBinaryValue()} instead.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw stored value: a {@code String}, a {@code byte[]}, or an
     * {@link EmptyValue} instance if this is a placeholder.
     * Prefer {@link #getStringValue()} or {@link #getBinaryValue()} for display.
     *
     * @return the raw value object; never {@code null}.
     */
    Object getRawValue();


    // ── R2-D2 Delivers The Value As A Human-Readable String ──────────────────────
    // "Grand Moff Tarkin."  Even binary values get decoded to a UTF-8 string
    // so the UI can always render something.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation of this value.
     * If the value is binary it is decoded as UTF-8.
     * If the value is an empty placeholder, returns {@code ""}.
     *
     * @return the string form; never {@code null}.
     */
    String getStringValue();


    // ── R2-D2 Delivers The Value As Raw Bytes ────────────────────────────────────
    // "Certificate or photo — raw bytes, please."  String values are UTF-8 encoded.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the binary representation of this value.
     * If the value is a string it is encoded as UTF-8 bytes.
     * If the value is an empty placeholder, returns {@code new byte[0]}.
     *
     * @return the binary form; never {@code null}.
     */
    byte[] getBinaryValue();


    // ── Mace Windu Checks Whether The Slot Is Still Unfilled ─────────────────────
    // "This spec field is blank — we can't submit the blueprint with empty entries."
    // An empty value is a placeholder that hasn't been typed into yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this value is an empty placeholder (not yet filled in).
     * An empty value's raw value is one of the {@link EmptyValue} singletons.
     * Empty values must not be submitted to the LDAP server.
     *
     * @return {@code true} if this is an unfilled placeholder.
     */
    boolean isEmpty();


    // ── The Data Point Reports Its String/Binary Nature ───────────────────────────
    // Delegates to the owning attribute's {@link IAttribute#isString()}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Convenience method — returns {@code getAttribute().isString()}.
     *
     * @return {@code true} if this value's attribute is string-typed.
     */
    boolean isString();


    // ── The Data Point Reports Its Binary Nature ──────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Convenience method — returns {@code getAttribute().isBinary()}.
     *
     * @return {@code true} if this value's attribute is binary-typed.
     */
    boolean isBinary();


    // ── Mace Windu Checks Whether The Value Is Part Of The Entry's Identity ───────
    // The RDN (relative distinguished name) is built from one or more attribute
    // values — e.g. {@code cn=Han Solo} means the value "Han Solo" is an RDN part.
    // Deleting an RDN part would change the entry's DN, which requires a rename op.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this value contributes to its entry's relative
     * distinguished name (RDN).
     * RDN parts may not be deleted without performing a rename operation.
     *
     * @return {@code true} if this value is an RDN component.
     */
    boolean isRdnPart();


    // ── Mace Windu Decides Whether Two Data Points Match ─────────────────────────
    // Two values are equal if they belong to the same entry, the same attribute,
    // and carry the same raw data (string equality or byte-array equality).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given object is an {@link IValue} with the same
     * entry, attribute, and raw value as this one.
     *
     * @param o the object to compare; should be an {@link IValue}.
     * @return {@code true} if entry, attribute, and raw value all match.
     */
    boolean equals( Object o );
}
