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

package org.apache.directory.studio.ldifparser.model.lines;


import org.apache.directory.studio.ldifparser.LdifParserConstants;


// ── CLASS: LdifSepLine — REBEL COMMS BLANK SEPARATOR LINE ────────────────────
// A blank line in the Rebel transmission marks the end of one record and the
// start of the next — just a newline character with no content, the most
// minimal possible separator.
// LdifSepLine is that blank line in the LDIF model: a LdifLineBase with no
// content field other than the newline, and a factory that creates one using
// the platform line separator.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF separator line — a blank line (newline only, no content) that
 * terminates a record or separates top-level containers.
 * Extends {@link LdifLineBase} directly.  Use {@link #create()} to build a
 * separator programmatically.
 * Think of this as the Rebel comms blank-line divider between records.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifSepLine extends LdifLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a separator line at {@code offset} with the given raw newline.
     *
     * @param offset      byte offset of this line in the document
     * @param rawNewLine  the raw line-ending characters
     */
    public LdifSepLine( int offset, String rawNewLine )
    {
        super( offset, rawNewLine );
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new separator line at offset 0 using the platform
     * {@link LdifParserConstants#LINE_SEPARATOR}.
     *
     * @return a new {@link LdifSepLine}
     */
    public static LdifSepLine create()
    {
        return new LdifSepLine( 0, LdifParserConstants.LINE_SEPARATOR );
    }

}
