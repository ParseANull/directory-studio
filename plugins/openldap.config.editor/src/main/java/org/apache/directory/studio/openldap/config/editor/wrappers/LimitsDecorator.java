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

// ── CLASS: LimitsDecorator — The Command Structure Manifest ───────────────────
// The Death Star's command structure has many limits: who can access what, when,
// and how much.  LimitsDecorator manages that manifest for the olcLimits table
// — it wires the Limits table to the LimitsDialog, renders each LimitsWrapper
// entry as its string form (prefix, selector, limits list), and sorts entries
// by their natural ordering (prefix first).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the Limits table.  It connects the table to
 * {@link LimitsDialog} and renders each {@link LimitsWrapper} as its string
 * representation.  Ordering is by the wrapper's prefix integer (the {n} that
 * appears in the LDAP attribute value).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LimitsDecorator extends TableDecorator<LimitsWrapper>
{
    // ── Constructor — Opening the Command Manifest ─────────────────────────────
    // The command structure clerk opens the manifest and connects it to the
    // limits editing dialog.  The title parameter is reserved for future use
    // and is currently passed through but not used.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of LimitDecorator
     * @param parentShell The parent Shell
     * @param title The title (reserved for future use)
     */
    public LimitsDecorator( Shell parentShell, String title )
    {
        setDialog( new LimitsDialog( parentShell ) );
    }


    // ── getText — Read the Limits Entry ───────────────────────────────────────
    // The clerk reads the entry aloud — we delegate to the wrapper's toString
    // which formats the selector and limit list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a Limit.
     *
     * @return the wrapper's string representation, or the default label if the
     *         element is not a {@link LimitWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof LimitWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Manifest ────────────────────────────────────
    // The manifest is text only — no images are needed.
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


    // ── compare — Sort Limits by Their Natural Ordering ───────────────────────
    // The manifest is kept in prefix order — we delegate to compareTo.
    // Null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( LimitsWrapper e1, LimitsWrapper e2 )
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
