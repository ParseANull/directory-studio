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

// ── CLASS: StringValueDecorator — The Protocol Droid Rendering Plain Strings ──
// C-3PO is always ready to read a plain text attribute aloud in whatever
// language is needed.  StringValueDecorator does the same for plain string
// attribute values: it wires the table to the StringValueDialog, renders each
// entry by returning the raw value string, and optionally shows an icon
// alongside it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for a table of plain string attribute values.
 * It connects the table to {@link StringValueDialog} and renders each
 * {@link StringValueWrapper} by returning its raw value.  An optional image
 * can be set via {@link #setImage}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StringValueDecorator extends TableDecorator<StringValueWrapper>
{
    /** The associated image, if any */
    private Image image;


    // ── Constructor — C-3PO Loads the String Dialog ───────────────────────────
    // C-3PO boots up and connects to the string editing dialog for the named
    // attribute type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of StringValueDecorator
     * @param parentShell The parent Shell
     * @param attributeName the name of the attribute which will contain the value
     */
    public StringValueDecorator( Shell parentShell, String attributeName )
    {
        setDialog( new StringValueDialog( parentShell, attributeName ) );
    }


    // ── setImage — Give C-3PO an Icon to Display ──────────────────────────────
    // We hand C-3PO an icon to show next to each string value in the table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds an Image to this decorator
     * @param image The Image
     */
    public void setImage( Image image )
    {
        this.image = image;
    }


    // ── getText — C-3PO Reads the String Aloud ────────────────────────────────
    // C-3PO reads the raw string value from the wrapper.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a String.
     * @param element the Element for which we want the value
     * @return a String representation of the element
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof StringValueWrapper )
        {
            return ( ( StringValueWrapper ) element ).getValue();
        }

        return super.getText( element );
    }


    // ── getImage — Return the Configured Icon ─────────────────────────────────
    // C-3PO holds up the icon we gave him (or nothing if none was set).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get the image. Here, We have none
     *
     * @param element The element for which we want the image
     * @return The associated Image, or Null
     */
    @Override
    public Image getImage( Object element )
    {
        return image;
    }


    // ── compare — Sort Strings by Their Natural Ordering ──────────────────────
    // C-3PO sorts the strings; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( StringValueWrapper e1, StringValueWrapper e2 )
    {
        if ( e1 != null )
        {
            if ( e2 == null )
            {
                return 1;
            }
            else
            {
                return e1.compareTo( e2 );
            }
        }
        else
        {
            if ( e2 == null )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
}
