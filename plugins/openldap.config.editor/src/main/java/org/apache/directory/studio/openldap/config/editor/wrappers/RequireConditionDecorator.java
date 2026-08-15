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

// ── CLASS: RequireConditionDecorator — Mon Mothma's Entry Requirements ────────
// Mon Mothma draws up the conditions that every visitor must satisfy before
// entering the Rebel base: strong authentication, signed requests, and so on.
// RequireConditionDecorator manages that list for the olcRequire table — it
// connects the table to the RequireConditionDialog, renders each condition by
// name, and sorts them alphabetically.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the RequireCondition table in the Options page.
 * It wires the table to {@link RequireConditionDialog} and renders each
 * {@link RequireConditionEnum} by its display name, sorted alphabetically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RequireConditionDecorator extends TableDecorator<RequireConditionEnum>
{
    // ── Constructor — Opening the Entry Requirements Registry ──────────────────
    // Mon Mothma's aide opens the registry and connects it to the dialog where
    // conditions can be added to the list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of RequireConditionDecorator
     * @param parentShell The parent Shell
     */
    public RequireConditionDecorator( Shell parentShell )
    {
        setDialog( new RequireConditionDialog( parentShell ) );
    }


    // ── getText — Read the Condition Name ─────────────────────────────────────
    // Mon Mothma's aide reads the condition off the registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an RequireCondition.
     *
     * @return the condition's display name, or the default label if the element
     *         is not a {@link RequireConditionEnum}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof RequireConditionEnum )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Registry Entry ───────────────────────────────
    // The registry is text only — no images.
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


    // ── compare — Sort Conditions Alphabetically ───────────────────────────────
    // Mon Mothma keeps the list alphabetical; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( RequireConditionEnum e1, RequireConditionEnum e2 )
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
