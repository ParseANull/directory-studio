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

// ── CLASS: DbIndexDecorator — The Imperial Archivist Cataloguing the Index ────
// The Imperial archivist in the data vault doesn't just store the records — he
// renders each one with a clear label so any officer can read the index at a
// glance.  DbIndexDecorator is that archivist: it wires the DbIndex table to
// the DbIndexDialog, renders each index entry through its toString, and orders
// entries by their natural comparison.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the DbIndex table.  It connects the table to
 * {@link DbIndexDialog} and renders each {@link DbIndexWrapper} as its string
 * representation (attribute list + index types), ordering by attribute name.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DbIndexDecorator extends TableDecorator<DbIndexWrapper>
{
    // ── Constructor — Opening the Archive Registry ─────────────────────────────
    // The archivist opens the registry and connects it to the index editing
    // dialog so entries can be added or modified.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of DbIndexDecorator
     * @param parentShell The parent Shell
     * @param browserConnection The LDAP browser connection (passed to the dialog)
     */
    public DbIndexDecorator( Shell parentShell, IBrowserConnection browserConnection )
    {
        setDialog( new DbIndexDialog( parentShell, browserConnection ) );
    }


    // ── getText — Read the Index Label ─────────────────────────────────────────
    // The archivist reads the label off the record card — we delegate to the
    // wrapper's toString which combines the attribute list and index types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an DbIndex.
     *
     * @return the wrapper's string representation, or the default label if the
     *         element is not a {@link DbIndexWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof DbIndexWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Index Card ──────────────────────────────────
    // The archivist keeps the catalog plain — no images are used here.
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


    // ── compare — Sort Index Entries by Their Natural Ordering ─────────────────
    // The archivist files records by attribute name; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( DbIndexWrapper e1, DbIndexWrapper e2 )
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
