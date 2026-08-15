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

// ── CLASS: DnDecorator — R2-D2 Reading Destination Coordinates ───────────────
// R2-D2 stores and reads navigation coordinates (DN values) precisely and
// without error.  DnDecorator does the same job for the DN table: it connects
// the table to the DnValueDialog, renders each DnWrapper entry as a plain
// string, and orders entries by their natural DN comparison.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the DN table.  It connects the table to
 * {@link DnValueDialog} so the user can pick a DN, and it renders and orders
 * each {@link DnWrapper} by its natural comparison (descendant-before-ancestor
 * then alphabetical by RDN).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnDecorator extends TableDecorator<DnWrapper>
{
    // ── Constructor — Loading the Navigation Database ─────────────────────────
    // R2-D2 boots his navigation database and links it to the DN picker dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of DnDecorator
     * @param parentShell The parent Shell
     */
    public DnDecorator( Shell parentShell )
    {
        setDialog( new DnValueDialog( parentShell ) );
    }


    // ── getText — Recite the Destination Coordinate ───────────────────────────
    // R2-D2 beeps out the DN string — we return the wrapper's toString.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an DisallowFeature.
     *
     * @return the wrapper's string representation, or the default label if the
     *         element is not a {@link DisallowFeatureEnum}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof DisallowFeatureEnum )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon for the Coordinate ─────────────────────────────────
    // R2-D2 works in text — no images here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get the image. We have none
     *
     * {@inheritDoc}
     */
    @Override
    public Image getImage( Object element )
    {
        return null;
    }


    // ── compare — Sort DNs by Their Natural Tree Ordering ─────────────────────
    // R2-D2 orders the coordinate set so descendants come before ancestors;
    // null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( DnWrapper e1, DnWrapper e2 )
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
