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

// ── CLASS: OrderedStringValueDecorator — The Fleet Communication Log ──────────
// The fleet communication officer logs every transmission in numbered order —
// each message gets a prefix like {0}, {1}, {2}.  OrderedStringValueDecorator
// does the same for ordered string attribute values: it wires the table to the
// OrderedStringValueDialog and renders each {n}value entry using toString.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for a table of ordered string attribute values.
 * It connects the table to {@link OrderedStringValueDialog} and renders each
 * {@link OrderedStringValueWrapper} as "{prefix}value" using toString.
 * Sorting is by prefix integer, then by value.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OrderedStringValueDecorator extends TableDecorator<OrderedStringValueWrapper>
{
    // ── Constructor — Opening the Numbered Log ────────────────────────────────
    // The communication officer opens the log book and connects it to the
    // dialog where new entries can be added or edited.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of StringValueDecorator
     * @param parentShell The parent Shell
     * @param attributeName The name of the attribute for the dialog title
     */
    public OrderedStringValueDecorator( Shell parentShell, String attributeName )
    {
        setDialog( new OrderedStringValueDialog( parentShell, attributeName ) );
    }


    // ── getText — Read the Log Entry Label ────────────────────────────────────
    // The officer reads the log entry aloud — we return the wrapper's toString
    // which formats it as "{n}value".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a String.
     *
     * @return the wrapper's "{prefix}value" string, or the default label if
     *         the element is not a {@link StringValueWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof StringValueWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon in the Log ─────────────────────────────────────────
    // The log is plain text — no images are used.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get the image. We have none
     *
     * @return always {@code null}
     */
    @Override
    public Image getImage( Object element )
    {
        return null;
    }


    // ── compare — Sort Entries by Prefix Then Value ───────────────────────────
    // The officer keeps the log in prefix order; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( OrderedStringValueWrapper e1, OrderedStringValueWrapper e2 )
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
