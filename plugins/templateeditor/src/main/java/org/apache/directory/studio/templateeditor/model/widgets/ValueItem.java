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
package org.apache.directory.studio.templateeditor.model.widgets;


// ── CLASS: ValueItem — C-3PO PAIRING A DISPLAY LABEL WITH A STORED VALUE ─────────
// In the Imperial communiqué, a value-item entry pairs two things: the human-
// readable label C-3PO reads aloud ("Active") and the raw value stored in the
// LDAP attribute ("TRUE"). Listboxes, radio-button groups, and similar widgets
// need this pairing so the user sees friendly text while the directory stores
// the machine-readable value.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A simple label-value pair used by listbox and radio-button widgets. The
 * {@code label} is the human-readable text shown in the UI; the {@code value}
 * is the corresponding LDAP attribute value stored in the directory. Either
 * field may be {@code null} if not needed.
 *
 * <p>Think of each entry as a C-3PO translation table row:</p>
 * <pre>
 *   new ValueItem( "Active",   "TRUE"  );  // user sees "Active", stores "TRUE"
 *   new ValueItem( "Inactive", "FALSE" );  // user sees "Inactive", stores "FALSE"
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueItem
{
    /** The label */
    private String label;

    /** The value */
    private Object value;


    // ── CONSTRUCTOR: EMPTY VALUE ITEM ─────────────────────────────────────────────
    // C-3PO prepares a blank translation entry — both label and value will be set
    // separately via the setters after construction.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new empty {@code ValueItem}. Both {@code label} and {@code value}
     * are {@code null} until set via {@link #setLabel(String)} and
     * {@link #setValue(Object)}.
     */
    public ValueItem()
    {
    }


    // ── CONSTRUCTOR: LABEL ONLY ───────────────────────────────────────────────────
    // C-3PO records only the display label — the stored value will be set
    // separately or defaults to null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ValueItem} with a display label and no stored value.
     *
     * @param label  the human-readable text to show in the UI
     */
    public ValueItem( String label )
    {
        this.label = label;
    }


    // ── CONSTRUCTOR: VALUE ONLY ───────────────────────────────────────────────────
    // C-3PO records only the stored value — the display label will be set
    // separately or defaults to null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ValueItem} with a stored value and no display label.
     *
     * @param value  the LDAP attribute value to store
     */
    public ValueItem( Object value )
    {
        this.value = value;
    }


    // ── CONSTRUCTOR: LABEL AND VALUE ──────────────────────────────────────────────
    // C-3PO records the full translation entry: user sees the label, directory
    // stores the value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ValueItem} with both a display label and a stored value.
     *
     * @param label  the human-readable text to show in the UI
     * @param value  the corresponding LDAP attribute value to store
     */
    public ValueItem( String label, Object value )
    {
        this.label = label;
        this.value = value;
    }


    // ── GET LABEL: THE DISPLAY TEXT ───────────────────────────────────────────────
    // C-3PO reads the human-readable side of the translation entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable label shown in the UI widget.
     *
     * @return the label string, or {@code null} if not set
     */
    public String getLabel()
    {
        return label;
    }


    // ── SET LABEL ─────────────────────────────────────────────────────────────────
    /**
     * Sets the human-readable display label.
     *
     * @param label  the label text
     */
    public void setLabel( String label )
    {
        this.label = label;
    }


    // ── GET VALUE: THE STORED LDAP VALUE ──────────────────────────────────────────
    // C-3PO reads the machine-readable side of the translation entry — what gets
    // written to the LDAP attribute when this item is selected.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attribute value corresponding to this item.
     *
     * @return the stored value, or {@code null} if not set
     */
    public Object getValue()
    {
        return value;
    }


    // ── SET VALUE ─────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP attribute value stored when this item is selected.
     *
     * @param value  the stored value
     */
    public void setValue( Object value )
    {
        this.value = value;
    }


    // ── EQUALS: COMPARE TWO VALUE ITEMS ──────────────────────────────────────────
    // C-3PO checks whether two translation entries match — both the label and the
    // value must agree for the items to be considered equal.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ValueItem )
        {
            ValueItem comparisonObject = ( ValueItem ) obj;

            // Comparing the label
            if ( ( getLabel() != null ) && ( comparisonObject.getLabel() != null ) )
            {
                if ( !getLabel().equals( comparisonObject.getLabel() ) )
                {
                    return false;
                }
            }

            // Comparing the value
            if ( ( getValue() != null ) && ( comparisonObject.getValue() != null ) )
            {
                if ( !getValue().equals( comparisonObject.getValue() ) )
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }


    // ── HASH CODE: STABLE HASH FOR COLLECTIONS ────────────────────────────────────
    // C-3PO combines the label and value hashes using the classic 37-times-17
    // polynomial — consistent with equals() so ValueItems work correctly in
    // hash-based collections like HashSet and HashMap.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        int result = 17;

        // The label
        if ( getLabel() != null )
        {
            result = 37 * result + getLabel().hashCode();
        }

        // The value
        if ( getValue() != null )
        {
            result = 37 * result + getValue().hashCode();
        }

        return result;
    }
}
