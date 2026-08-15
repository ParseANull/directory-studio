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

// ── CLASS: RestrictOperationDecorator — Darth Vader's Restricted Operations List ─
// Darth Vader walks the corridors of the Death Star and personally signs the
// list of operations that no ordinary officer may perform — only he and the
// Emperor authorize them.  RestrictOperationDecorator manages that list for the
// olcRestrict table: it wires the table to the RestrictOperationDialog, renders
// each restricted operation by name, and keeps the list alphabetically sorted.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the RestrictOperation table in the Options page.
 * It connects the table to {@link RestrictOperationDialog} and renders each
 * {@link RestrictOperationEnum} by its display name, sorted alphabetically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RestrictOperationDecorator extends TableDecorator<RestrictOperationEnum>
{
    // ── Constructor — Opening the Restricted-Operations Registry ──────────────
    // Vader's aide opens the registry and connects it to the dialog where
    // operations can be added to the restrictions list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of RestrictOperationDecorator
     * @param parentShell The parent Shell
     */
    public RestrictOperationDecorator( Shell parentShell )
    {
        setDialog( new RestrictOperationDialog( parentShell ) );
    }


    // ── getText — Read the Restricted Operation Name ───────────────────────────
    // Vader's aide reads the operation off the list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an RestrictOperation.
     *
     * @return the operation's display name, or the default label if the element
     *         is not a {@link RestrictOperationEnum}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof RestrictOperationEnum )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Restrictions List ───────────────────────────
    // The list is text only — no images.
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


    // ── compare — Sort Operations Alphabetically ───────────────────────────────
    // Vader insists the restrictions list stay alphabetical; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( RestrictOperationEnum e1, RestrictOperationEnum e2 )
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
