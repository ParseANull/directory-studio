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

// ── CLASS: SsfDecorator — The Shield Generator's Security Strength Readings ───
// On the Death Star's shield control panel, each security barrier is labeled
// with its strength in bits — TLS requires 128, SASL requires 64, and so on.
// SsfDecorator is that panel for the SSF table: it wires the table to the
// SsfDialog, renders each SsfWrapper as "feature=nbBits", and sorts entries
// by feature name then bit count.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the SSF (Security Strength Factor) table.
 * It connects the table to {@link SsfDialog} and renders each {@link SsfWrapper}
 * as "featureName=nbBits".  Sorting is by feature name then bit count.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SsfDecorator extends TableDecorator<SsfWrapper>
{
    // ── Constructor — Installing the Shield-Strength Panel ────────────────────
    // The shield officer installs the panel and connects it to the SSF editing
    // dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of SsfDecorator
     * @param parentShell The parent Shell
     */
    public SsfDecorator( Shell parentShell )
    {
        setDialog( new SsfDialog( parentShell ) );
    }


    // ── getText — Read the Shield Strength Reading ────────────────────────────
    // The shield officer reads the strength reading off the panel — we delegate
    // to the wrapper's toString.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a SSF. It can be one of :
     *
     * @return the SSF string ("feature=N"), or the default label if the element
     *         is not a {@link SsfWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof SsfWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Shield Panel ────────────────────────────────
    // The panel is numeric — no images.
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


    // ── compare — Sort SSF Entries by Natural Ordering ────────────────────────
    // The shield officer keeps the readings in feature-name order; null entries
    // trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( SsfWrapper e1, SsfWrapper e2 )
    {
        if ( e1 != null )
        {
            return e1.compareTo( e2 );
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
