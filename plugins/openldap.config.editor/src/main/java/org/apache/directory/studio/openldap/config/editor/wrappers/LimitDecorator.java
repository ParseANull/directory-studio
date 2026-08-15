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

// ── CLASS: LimitDecorator — The Hyperdrive Engineer Setting the Speed Cap ─────
// The chief engineer on the Star Destroyer keeps a panel labeled with the
// maximum hyperdrive speed — you can see the current limit at a glance.
// LimitDecorator does the same for a single size-or-time limit: it wires the
// limit table to the SizeTimeLimitDialog and renders each LimitWrapper as its
// string representation.  Ordering is a stub (always returns 0) because limits
// in this context don't need sorting.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for a single {@link LimitWrapper} (size or time).
 * It connects the table to {@link SizeTimeLimitDialog} and renders each entry
 * as its string form.  The compare method is a stub — it always returns 0.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LimitDecorator extends TableDecorator<LimitWrapper>
{
    // ── Constructor — Hanging the Speed-Cap Panel ──────────────────────────────
    // The engineer installs the panel and connects it to the dialog where the
    // limit value can be changed.  The title parameter is reserved for future
    // use and is currently ignored.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of LimitDecorator
     * @param parentShell The parent Shell
     * @param title The title (reserved for future use)
     */
    public LimitDecorator( Shell parentShell, String title )
    {
        setDialog( new SizeTimeLimitDialog( parentShell ) );
    }


    // ── getText — Read the Current Limit Value ─────────────────────────────────
    // The engineer reads the number off the panel — we delegate to the
    // LimitWrapper's toString.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a TimeLimit. It can be one of :
     *
     * @return the limit's string representation, or the default label if the
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


    // ── getImage — No Icon on the Speed-Cap Panel ─────────────────────────────
    // The panel is numeric only — no image is needed.
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


    // ── compare — Ordering Not Yet Implemented ────────────────────────────────
    // The engineer hasn't been given ranking orders yet — this always returns 0.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( LimitWrapper e1, LimitWrapper e2 )
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
