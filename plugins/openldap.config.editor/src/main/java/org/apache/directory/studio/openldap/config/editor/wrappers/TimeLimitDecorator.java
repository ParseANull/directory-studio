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

// ── CLASS: TimeLimitDecorator — The Star Destroyer's Mission Timer ────────────
// Every mission on a Star Destroyer runs on a countdown: how long the shields
// can hold, how long before reinforcements arrive.  TimeLimitDecorator manages
// the time-limit table for the LDAP server — it wires the table to the
// TimeLimitDialog and renders each TimeLimitWrapper as its configuration string
// (global, hard, or soft time limit).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the TimeLimit table in the Tuning page.
 * It connects the table to {@link TimeLimitDialog} and renders each
 * {@link TimeLimitWrapper} as its OpenLDAP configuration string.
 * Sorting is by the wrapper's natural ordering (string comparison).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TimeLimitDecorator extends TableDecorator<TimeLimitWrapper>
{
    // ── Constructor — Installing the Mission Timer ─────────────────────────────
    // The mission controller installs the timer panel and connects it to the
    // time-limit editing dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of TImeLimitDecorator
     * @param parentShell The parent Shell
     */
    public TimeLimitDecorator( Shell parentShell )
    {
        setDialog( new TimeLimitDialog( parentShell ) );
    }


    // ── getText — Read the Current Time Limit ─────────────────────────────────
    // The mission controller reads the timer value — we delegate to the
    // wrapper's toString.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a TimeLimit. It can be one of :
     *
     * @return the time limit's configuration string, or the default label if the
     *         element is not a {@link TimeLimitWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof TimeLimitWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Timer Panel ─────────────────────────────────
    // The timer panel is numeric — no images.
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


    // ── compare — Sort Time Limits by Natural Ordering ────────────────────────
    // The mission controller sorts by configuration string; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( TimeLimitWrapper e1, TimeLimitWrapper e2 )
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
