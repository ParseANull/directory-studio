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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: OrderedStringValueWrapper — A Numbered Transmission in the Fleet Log ─
// Every fleet transmission is assigned a numbered sequence slot: {0} carries the
// first order, {1} the second, and so on.  OrderedStringValueWrapper stores one
// such numbered string value — a prefix integer and a plain string — and
// formats them as "{n}value" for storage in X-ORDERED LDAP attributes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for an ordered string attribute value.  The value is stored with
 * an integer prefix so that X-ORDERED attributes can be maintained in the
 * correct sequence.  The string form is "{prefix}value".
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OrderedStringValueWrapper implements Cloneable, Comparable<OrderedStringValueWrapper>, OrderedElement
{
    /** The value */
    private String value;

    /** A flag to tell if the compare should be case sensitive or not */
    private boolean caseSensitive = true;

    /** The prefix, used to order the values */
    private Integer prefix;


    // ── Constructor — Assigning a Sequence Number and Value ───────────────────
    // The communications officer assigns this transmission a sequence number
    // and records the message text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of StringValueWrapper.
     *
     * @param prefix the ordering prefix (sequence number)
     * @param value the value
     * @param caseSensitive whether comparisons should be case-sensitive
     */
    public OrderedStringValueWrapper( int prefix, String value, boolean caseSensitive )
    {
        this.value = value;
        this.caseSensitive = caseSensitive;
        this.prefix = prefix;
    }


    // ── getValue — Read the Transmission Text ─────────────────────────────────
    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }


    // ── setValue — Update the Transmission Text ───────────────────────────────
    /**
     * Sets a new value
     *
     * @param value the value to set
     */
    public void setValue( String value )
    {
        this.value = value;
    }


    // ── setPrefix — Update the Sequence Number ────────────────────────────────
    /**
     * Sets a new prefix
     *
     * @param prefix the prefix to set
     */
    public void setPrefix( int prefix )
    {
        this.prefix = prefix;
    }


    // ── getPrefix — Read the Sequence Number ──────────────────────────────────
    /**
     * @return the prefix
     */
    public int getPrefix()
    {
        return prefix;
    }


    // ── decrementPrefix — Move One Slot Earlier in the Sequence ───────────────
    // The officer renumbers a transmission to an earlier slot.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void decrementPrefix()
    {
        prefix--;
    }


    // ── incrementPrefix — Move One Slot Later in the Sequence ─────────────────
    // The officer renumbers a transmission to a later slot.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void incrementPrefix()
    {
        prefix++;
    }


    // ── clone — Duplicate the Log Entry ───────────────────────────────────────
    // The officer makes a carbon copy of the transmission record.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clone the current object
     */
    public OrderedStringValueWrapper clone()
    {
        try
        {
            return (OrderedStringValueWrapper)super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            return null;
        }
    }


    // ── compareTo — Compare Two Entries by Prefix Then Value ──────────────────
    // The officer sorts by sequence number first; ties are broken by value
    // (case-insensitive).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( OrderedStringValueWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        // Check the prefix
        if ( prefix < that.prefix )
        {
            return -1;
        }
        else if ( prefix > that.prefix )
        {
            return 1;
        }

        // Check the value
        if ( Strings.isEmpty( value ) )
        {
            return -1;
        }
        else
        {
            return value.compareToIgnoreCase( that.value );
        }
    }


    // ── equals — Check If Two Entries Are the Same Log Entry ──────────────────
    // Two entries are equal when their prefix and value match (respecting the
    // caseSensitive flag for the value comparison).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#equals(Object)
     */
    public boolean equals( Object that )
    {
        // Quick test
        if ( this == that )
        {
            return true;
        }

        if ( that instanceof OrderedStringValueWrapper )
        {
            OrderedStringValueWrapper thatInstance = (OrderedStringValueWrapper)that;

            if ( ( prefix != null ) && ( prefix != thatInstance.prefix ) )
            {
                return false;
            }

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


    // ── hashCode — Compute a Hash from the Prefix and Value ───────────────────
    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int h = 37;

        h += h*17 + prefix;

        if ( value != null )
        {
            h += h*17 + value.hashCode();
        }

        return h;
    }


    // ── toString — Format the Entry as {n}value ───────────────────────────────
    // The officer stamps the sequence number on the log page in the standard
    // X-ORDERED format: "{n}value".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return '{' + Integer.toString( prefix ) + '}' + value;
    }
}
