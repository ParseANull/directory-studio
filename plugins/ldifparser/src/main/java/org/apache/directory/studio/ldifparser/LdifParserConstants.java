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

package org.apache.directory.studio.ldifparser;


// ── CLASS: LdifParserConstants — IMPERIAL COMMS STANDARD MEASUREMENTS ────────
// The Imperial Navy's comms standard says lines shall be no wider than 78
// characters, and specifies the exact byte sequence for a line ending on this
// platform — everything else in the stack reads these constants to stay
// consistent.
// LdifParserConstants is that standard: a non-instantiable constants class
// holding the two values (LINE_WIDTH and LINE_SEPARATOR) that the entire
// LDIF parser/formatter stack relies on.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Shared constants for the LDIF parser and formatter.
 * This class is non-instantiable — all members are {@code public static final}.
 * Think of this as the Imperial comms standard that every other component
 * references for line length and line-ending conventions.
 * Final reference: this class should not be extended.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class LdifParserConstants
{

    /**
     * Private constructor prevents instantiation and extension.
     * Implicit super constructor is not visible for default constructor,
     * but is still self-documenting.
     */
    private LdifParserConstants()
    {
    }

    /** The system-specific line separator ({@code line.separator} property). */
    public static final String LINE_SEPARATOR = System.getProperty( "line.separator" ); //$NON-NLS-1$

    /** The default maximum LDIF line width in characters — value is {@code 78}. */
    public static final int LINE_WIDTH = 78;

}
