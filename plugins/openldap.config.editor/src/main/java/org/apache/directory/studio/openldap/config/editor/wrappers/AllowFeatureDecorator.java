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

// ── CLASS: AllowFeatureDecorator — Tarkin's Permitted-Features Manifest ───────
// Grand Moff Tarkin keeps a tightly controlled permit list — only named features
// may be enabled on the Death Star.  AllowFeatureDecorator is his manifest: it
// connects the AllowFeature table to the AllowFeatureDialog picker, renders each
// permitted feature by name, and sorts them alphabetically so the list is
// readable at a glance.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the AllowFeature table in the Options page.
 * It wires the table to {@link AllowFeatureDialog} so the user can pick from
 * the available {@link AllowFeatureEnum} values, and it renders and sorts each
 * entry by its display name.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AllowFeatureDecorator extends TableDecorator<AllowFeatureEnum>
{
    // ── Constructor — Opening the Permit Registry ──────────────────────────────
    // Tarkin opens the permit registry and connects it to the approval dialog
    // where new features can be authorized.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of AllowFeatureDecorator
     * @param parentShell The parent Shell
     */
    public AllowFeatureDecorator( Shell parentShell )
    {
        setDialog( new AllowFeatureDialog( parentShell ) );
    }


    // ── getText — Read the Feature's Name from the Permit ─────────────────────
    // Tarkin reads the name printed on each permit — if the entry is an
    // AllowFeatureEnum, we return its display name; otherwise we fall through
    // to the default rendering.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an AllowFeature.
     *
     * @return the feature's display name, or the default label if the element
     *         is not an {@link AllowFeatureEnum}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof AllowFeatureEnum )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Permit ──────────────────────────────────────
    // Tarkin keeps the manifest plain text — no icons are used for this table.
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


    // ── compare — Alphabetically Sort the Permit List ─────────────────────────
    // Tarkin insists the permit list stay in alphabetical order for easy audit.
    // We compare by name, with null entries always trailing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( AllowFeatureEnum e1, AllowFeatureEnum e2 )
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
