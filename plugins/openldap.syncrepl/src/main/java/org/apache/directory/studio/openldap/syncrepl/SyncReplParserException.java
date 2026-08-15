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
package org.apache.directory.studio.openldap.syncrepl;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


// ── CLASS: SyncReplParserException — The Sector Command's Incident Report ─────
// When the sector command receives a garbled intelligence package from Imperial
// HQ it doesn't stop processing after the first error — it collects all the
// garbled tokens into a single incident report and sends it back to the
// intelligence officer at once.  This way the officer can fix every problem
// in the configuration string in one pass instead of discovering issues one
// by one.
// SyncReplParserException is that incident report: it accumulates all
// ParseException objects raised during a syncrepl directive parse and throws
// them in one batch at the end.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An aggregating exception for errors encountered while parsing a syncrepl directive.
 * Instead of aborting on the first bad token, {@link SyncReplParser} collects
 * all {@link ParseException} objects into this exception and throws it once
 * parsing is complete.  Callers can then inspect all failures at once and give
 * a comprehensive error message.
 * Think of this as the sector command's batch incident report: every garbled
 * token collected and delivered to the intelligence officer in one go.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyncReplParserException extends Exception
{
    private static final long serialVersionUID = 1L;

    /** The list of exceptions*/
    private List<ParseException> exceptions = new ArrayList<ParseException>();


    // ── Blank Incident Report Created — Ready to Collect Problems ─────────────
    // The parser creates this exception at the start of parsing; it's only
    // thrown if at least one ParseException was added to it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty SyncReplParserException.
     * The parser creates one of these at the start of a parse, adds errors
     * as it finds them, and throws it at the end if any errors were collected.
     */
    public SyncReplParserException()
    {
        super();
    }


    // ── Add One or More Garbled Tokens to the Incident Report ────────────────
    // Each time the parser finds a bad token it files a ParseException here.
    // Multiple exceptions can be added in a single call (varargs).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more {@link ParseException} objects to the accumulated list.
     * If the argument array is {@code null} or any individual element is {@code null},
     * those are silently skipped.
     *
     * @param parseExceptions  one or more parse exceptions to record.
     */
    public void addParseException( ParseException... parseExceptions )
    {
        if ( parseExceptions != null )
        {
            for ( ParseException parseException : parseExceptions )
            {
                exceptions.add( parseException );
            }
        }
    }


    // ── How Many Incidents Were Recorded? ────────────────────────────────────
    // The parser checks this after the parse loop — if greater than zero,
    // it throws this exception so the caller sees all the errors.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of {@link ParseException} objects collected so far.
     * The caller uses this to decide whether to throw the exception.
     *
     * @return  the number of accumulated parse errors.
     */
    public int size()
    {
        return exceptions.size();
    }


    // ── Print the Full Incident Report ────────────────────────────────────────
    // We format all accumulated exceptions in a bracketed comma-separated list
    // so the intelligence officer sees every problem at a glance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a string representation of all accumulated parse exceptions.
     * Format: {@code "[exception1, exception2, ...]"}.
     *
     * @return  the formatted incident report string.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( '[' );

        for ( int i = 0; i < exceptions.size(); i++ )
        {
            sb.append( exceptions.get( i ).toString() );

            if ( i != ( exceptions.size() - 1 ) )
            {
                sb.append( ", " );

            }
        }

        sb.append( ']' );

        return sb.toString();
    }
}
