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


// ── CLASS: LdifFormatParameters — IMPERIAL TRANSMISSION STYLE GUIDE ──────────
// Before the Imperial communications officer sends a communiqué, she checks
// the style guide: how wide is each line, does the colon need a trailing space,
// and which line-ending convention does the network expect?
// LdifFormatParameters is that style guide: a small value object carrying the
// three formatting decisions (space after colon, line width, line separator)
// that control how an LDIF model renders itself as text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds the three formatting parameters used when rendering an LDIF model
 * to text: whether to add a space after the colon separator, the maximum
 * line width before folding, and the line-separator string.
 * Use {@link #DEFAULT} for the standard settings (space after colon, 78-char
 * line width, system line separator).
 * Think of this as the Imperial communications style guide passed to every
 * serialiser method.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifFormatParameters
{

    /**
     * The default LDIF format parameters: space after colon, 78-char line
     * width, and the platform line separator.
     */
    public static final LdifFormatParameters DEFAULT = new LdifFormatParameters();

    /** Whether a space is added between the colon/double-colon and the value. */
    private boolean spaceAfterColon;

    /** Maximum character width of a single LDIF line before line-folding. */
    private int lineWidth;

    /** The line-ending string to append at the end of each LDIF line. */
    private String lineSeparator;


    // ── DEFAULT CONSTRUCTOR ───────────────────────────────────────────────────
    /**
     * Creates the default format parameters (space after colon, 78-char width,
     * system line separator).  Private so callers use {@link #DEFAULT}.
     */
    private LdifFormatParameters()
    {
        this.spaceAfterColon = true;
        this.lineWidth = LdifParserConstants.LINE_WIDTH;
        this.lineSeparator = LdifParserConstants.LINE_SEPARATOR;
    }


    // ── CUSTOM CONSTRUCTOR ────────────────────────────────────────────────────
    /**
     * Creates format parameters with explicit values.
     *
     * @param spaceAfterColon  {@code true} to emit a space after {@code :} or {@code ::}
     * @param lineWidth        maximum line length before folding
     * @param lineSeparator    the line-ending string (e.g. {@code "\n"} or {@code "\r\n"})
     */
    public LdifFormatParameters( boolean spaceAfterColon, int lineWidth, String lineSeparator )
    {
        this.spaceAfterColon = spaceAfterColon;
        this.lineWidth = lineWidth;
        this.lineSeparator = lineSeparator;
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if a space should be inserted between the colon and
     * the value in output LDIF lines.
     *
     * @return whether space-after-colon formatting is enabled
     */
    public boolean isSpaceAfterColon()
    {
        return spaceAfterColon;
    }


    /**
     * Sets whether a space is placed after the colon separator.
     *
     * @param spaceAfterColon  {@code true} to enable, {@code false} to disable
     */
    public void setSpaceAfterColon( boolean spaceAfterColon )
    {
        this.spaceAfterColon = spaceAfterColon;
    }


    /**
     * Returns the maximum line width before LDIF line-folding kicks in.
     *
     * @return the configured line width in characters
     */
    public int getLineWidth()
    {
        return lineWidth;
    }


    /**
     * Sets the maximum LDIF line width.
     *
     * @param lineWidth  the new maximum line width in characters
     */
    public void setLineWidth( int lineWidth )
    {
        this.lineWidth = lineWidth;
    }


    /**
     * Returns the line-separator string appended at the end of each LDIF line.
     *
     * @return the line separator (e.g. {@code "\n"} or {@code "\r\n"})
     */
    public String getLineSeparator()
    {
        return lineSeparator;
    }


    /**
     * Sets the line-separator string.
     *
     * @param lineSeparator  the new line-separator string
     */
    public void setLineSeparator( String lineSeparator )
    {
        this.lineSeparator = lineSeparator;
    }

}
