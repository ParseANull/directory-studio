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

// ── CLASS: DisallowFeatureDecorator — The Emperor's List of Forbidden Things ──
// The Emperor keeps a strict list of capabilities that no one — not even Grand
// Admirals — may enable.  DisallowFeatureDecorator is that list for the LDAP
// server: it connects the DisallowFeature table to the DisallowFeatureDialog,
// renders each prohibited feature by name, and sorts them alphabetically.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the DisallowFeature table in the Options page.
 * It wires the table to {@link DisallowFeatureDialog} so the user can select
 * from the available {@link DisallowFeatureEnum} values, and it renders and
 * sorts each entry by its display name.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DisallowFeatureDecorator extends TableDecorator<DisallowFeatureEnum>
{
    // ── Constructor — Opening the Forbidden-Features Registry ─────────────────
    // The Emperor's clerk opens the registry and connects it to the selection
    // dialog where features can be added to the prohibition list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of DisallowFeatureDecorator
     * @param parentShell The parent Shell
     */
    public DisallowFeatureDecorator( Shell parentShell )
    {
        setDialog( new DisallowFeatureDialog( parentShell ) );
    }


    // ── getText — Read the Prohibited Feature's Name ───────────────────────────
    // The clerk reads the name of the forbidden capability — we return the
    // enum's display name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an DisallowFeature.
     *
     * @return the feature's display name, or the default label if the element
     *         is not a {@link DisallowFeatureEnum}
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


    // ── getImage — No Icon on the Prohibition Scroll ──────────────────────────
    // The scroll is plain text — no images are needed.
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


    // ── compare — Alphabetically Sort the Prohibition List ────────────────────
    // The Emperor insists the prohibition list stay in alphabetical order.
    // We compare by name, with null entries always trailing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( DisallowFeatureEnum e1, DisallowFeatureEnum e2 )
    {
        if ( e1 != null )
        {
            if ( e2 == null )
            {
                return 1;
            }
            else
            {
                return e1.getName().compareTo( e2.getName() );
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
