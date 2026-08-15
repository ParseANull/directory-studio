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

// ── CLASS: PasswordHashDecorator — The Imperial Cipher Officer's Algorithm List ─
// The Imperial cipher officer keeps a registry of approved hashing algorithms —
// {SHA}, {SSHA}, {MD5}, etc. — and lists them in alphabetical order on the
// security manifest.  PasswordHashDecorator does the same for the
// olcPasswordHash table: it wires the table to the PasswordHashDialog and
// renders each PasswordHashEnum value by name, alphabetically sorted.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the PasswordHash table in the Security page.
 * It connects the table to {@link PasswordHashDialog} and renders each
 * {@link PasswordHashEnum} by its display name, sorted alphabetically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordHashDecorator extends TableDecorator<PasswordHashEnum>
{
    // ── Constructor — Opening the Algorithm Registry ───────────────────────────
    // The cipher officer opens the registry and connects it to the algorithm
    // selection dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of PasswordHashDecorator
     * @param parentShell The parent Shell
     */
    public PasswordHashDecorator( Shell parentShell )
    {
        setDialog( new PasswordHashDialog( parentShell ) );
    }


    // ── getText — Read the Algorithm Name ─────────────────────────────────────
    // The cipher officer reads the algorithm name off the registry entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a PasswordHash.
     *
     * @return the algorithm's display name, or the default label if the element
     *         is not a {@link PasswordHashEnum}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof PasswordHashEnum )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Algorithm Entry ──────────────────────────────
    // The registry is text-only — no images.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get the image. We have none (may be we could add one for URLs ?)
     *
     * @return always {@code null}
     */
    @Override
    public Image getImage( Object element )
    {
        return null;
    }


    // ── compare — Sort Algorithms Alphabetically ───────────────────────────────
    // The cipher officer keeps the list alphabetical; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( PasswordHashEnum e1, PasswordHashEnum e2 )
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
