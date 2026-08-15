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
package org.apache.directory.studio.common.ui.wrappers;

// ── CLASS: StringValueWrapper — C-3PO PACKAGING A RAW STRING MESSAGE ─────────
// C-3PO never hands a raw hologram directly to the recipient — he always wraps
// it in a proper diplomatic package so the receiving party knows how to handle
// it.  This class is that package for a plain Java String: it adds identity
// semantics (equals, hashCode, compareTo) and a flag controlling whether the
// comparison is case-sensitive.  The dialogs and table widgets use us to avoid
// duplicate string entries and to keep things sorted.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We wrap a single string value and provide full identity and ordering support
 * so it can live safely inside collections, tables, and sorted lists.  The
 * {@code caseSensitive} flag lets callers control whether equality and ordering
 * ignore case — useful for attribute names, which are case-insensitive in LDAP.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StringValueWrapper implements Cloneable, Comparable<StringValueWrapper>
{
    /** The value */
    private String value;

    /** A flag to tell if the compare should be case sensitive or not */
    private boolean caseSensitive = true;

    // ── CONSTRUCTOR StringValueWrapper — SEALING THE DIPLOMATIC PACKAGE ───────
    // C-3PO seals the package with the raw string inside and stamps it with the
    // case-sensitivity flag so future comparisons know the rules.  The package
    // is immutable in terms of its flag — only the value inside can be changed.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new wrapper around the given string value.  The
     * {@code caseSensitive} flag controls whether {@link #equals} and
     * {@link #compareTo} treat upper and lower case as distinct.
     *
     * @param value         the string to wrap
     * @param caseSensitive {@code true} for case-sensitive comparisons
     */
    public StringValueWrapper( String value, boolean caseSensitive )
    {
        this.value = value;
        this.caseSensitive = caseSensitive;
    }


    // ── METHOD getValue — OPENING THE PACKAGE ────────────────────────────────
    // C-3PO unseals the package and reveals the raw string inside.  Callers
    // get direct access to the wrapped string for display or further processing.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the string value stored inside this wrapper.
     *
     * @return the wrapped string, may be {@code null} if none was set
     */
    public String getValue()
    {
        return value;
    }

    // ── METHOD setValue — REPLACING THE PACKAGE CONTENTS ─────────────────────
    // C-3PO replaces the message inside the existing diplomatic package with a
    // new one.  The case-sensitivity flag stays the same; only the payload
    // changes.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We update the wrapped string value.  The {@code caseSensitive} flag
     * remains unchanged — only the payload string is replaced.
     *
     * @param value the new string value to store
     */
    public void setValue( String value )
    {
        this.value = value;
    }


    // ── METHOD clone — DUPLICATING THE DIPLOMATIC PACKAGE ────────────────────
    // C-3PO runs the package through the duplicator to produce an identical
    // copy.  The clone has the same value and the same case-sensitivity flag,
    // but is a completely independent object so changes to one do not affect
    // the other.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We produce a shallow clone of this wrapper.  Both the original and the
     * clone contain the same string reference, which is safe since strings are
     * immutable in Java.  Returns {@code null} if cloning is unexpectedly
     * unsupported (this should never happen).
     *
     * @return a new {@link StringValueWrapper} with the same value and flags
     */
    public StringValueWrapper clone()
    {
        try
        {
            return (StringValueWrapper)super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            return null;
        }
    }


    // ── METHOD equals — CHECKING WHETHER TWO PACKAGES HOLD THE SAME MESSAGE ──
    // C-3PO compares two packages: if the case-sensitivity flag says case
    // matters, he does a strict comparison; otherwise he ignores case when
    // checking whether the messages are identical.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} when the other object is a {@link StringValueWrapper}
     * whose value equals ours.  If our {@code caseSensitive} flag is set, we use
     * a strict equality check; otherwise we use {@link String#equalsIgnoreCase}.
     *
     * @param that the object to compare against
     * @return {@code true} if both wrappers hold an equal string value
     * @see Object#equals(Object)
     */
    public boolean equals( Object that )
    {
        // Quick test
        if ( this == that )
        {
            return true;
        }

        if ( that instanceof StringValueWrapper )
        {
            StringValueWrapper thatInstance = (StringValueWrapper)that;

            if ( caseSensitive )
            {
                return value.equals( thatInstance.value );
            }
            else
            {
                return value.equalsIgnoreCase( thatInstance.value );
            }
        }
        else
        {
            return false;
        }
    }


    // ── METHOD hashCode — COMPUTING THE PACKAGE'S UNIQUE CODE ────────────────
    // Every diplomatic package gets a unique tracking code computed from its
    // payload.  We fold the string's hash into our computation so equal strings
    // produce equal codes — a requirement for correct hash-based collection
    // behaviour.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We compute a hash code based on the wrapped string value.  Two wrappers
     * that are equal according to {@link #equals} will always produce the same
     * hash code, which is required for correct behaviour in hash maps and sets.
     *
     * @return the hash code for this wrapper
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int h = 37;

        if ( value != null )
        {
            h += h*17 + value.hashCode();
        }

        return h;
    }


    // ── METHOD compareTo — RANKING PACKAGES IN THE DISPATCH ORDER ────────────
    // C-3PO arranges the packages in alphabetical dispatch order, ignoring case
    // so "Alpha" and "alpha" sort to the same position.  Null or empty values
    // sort to the front so they stand out.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We compare this wrapper to another for ordering purposes using
     * case-insensitive string comparison.  A {@code null} argument sorts after
     * us; a null or empty {@code value} in this wrapper sorts before everything
     * else.
     *
     * @param that the other wrapper to compare against
     * @return a negative integer, zero, or a positive integer as per {@link Comparable}
     * @see Comparable#compareTo()
     */
    public int compareTo( StringValueWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        // Check the value
        if ( ( value == null ) || ( value.length() == 0 ) )
        {
            return -1;
        }
        else
        {
            return value.compareToIgnoreCase( that.value );
        }
    }


    // ── METHOD toString — TRANSMITTING THE MESSAGE AS PLAIN TEXT ─────────────
    // C-3PO reads the message aloud in plain text so the table viewer can
    // display it without any extra ceremony.  We simply return the wrapped
    // string, which is all the label provider needs from us.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the wrapped string directly so this wrapper can be displayed
     * in a table or combo without any extra formatting.
     *
     * @return the wrapped string value
     * @see Object#toString()
     */
    public String toString()
    {
        return value;
    }
}
