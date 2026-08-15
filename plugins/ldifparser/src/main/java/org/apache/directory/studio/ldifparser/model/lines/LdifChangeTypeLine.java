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


// ── CLASS: LdifChangeTypeLine — REBEL OPERATIONS ORDER TYPE DECLARATION ──────
// Every Rebel operations order declares its type on the second line:
// "changetype: add", "changetype: modify", "changetype: delete", or
// "changetype: moddn".  This single line tells the receiver what kind of
// operation to perform on the named entry.
// LdifChangeTypeLine models that declaration: extends LdifValueLineBase with
// changetype-spec/changetype accessors, operation-type predicates (isAdd,
// isDelete, isModify, isModDn), and factory methods for all four types.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF {@code changetype: <type>} line.
 * Extends {@link LdifValueLineBase} — the {@code "changetype"} keyword is
 * the line-start segment, the separator is {@code ":"}, and the operation
 * type ({@code add}, {@code delete}, {@code modify}, {@code moddn}, or
 * {@code modrdn}) is the value.
 * Factory methods {@link #createAdd()}, {@link #createDelete()},
 * {@link #createModify()}, {@link #createModDn()}, {@link #createModRdn()}
 * build the four standard changetype lines.
 * Think of this as the Rebel operations order type declaration — it tells the
 * receiver which directory operation to execute.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifChangeTypeLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a changetype line with all five raw segments.
     *
     * @param offset              byte offset in the document
     * @param rawChangeTypeSpec   the raw {@code "changetype"} keyword
     * @param rawValueType        the separator ({@code ":"})
     * @param rawChangeType       the raw operation type (e.g. {@code "add"})
     * @param rawNewLine          the raw line-ending characters
     */
    public LdifChangeTypeLine( int offset, String rawChangeTypeSpec, String rawValueType, String rawChangeType,
        String rawNewLine )
    {
        super( offset, rawChangeTypeSpec, rawValueType, rawChangeType, rawNewLine );
    }


    // ── CHANGETYPE SPEC ACCESSORS ─────────────────────────────────────────────
    /**
     * Returns the raw changetype spec keyword ({@code "changetype"}).
     *
     * @return the raw changetype-spec segment
     */
    public String getRawChangeTypeSpec()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded changetype spec keyword.
     *
     * @return the unfolded changetype-spec segment
     */
    public String getUnfoldedChangeTypeSpec()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw operation type string (e.g. {@code "add"}).
     *
     * @return the raw changetype value
     */
    public String getRawChangeType()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded operation type string.
     *
     * @return the unfolded changetype value
     */
    public String getUnfoldedChangeType()
    {
        return super.getUnfoldedValue();
    }


    // ── OPERATION TYPE PREDICATES ─────────────────────────────────────────────
    /**
     * Returns {@code true} if the changetype is {@code "add"}.
     *
     * @return {@code true} for add operations
     */
    public boolean isAdd()
    {
        return getUnfoldedChangeType().equals( "add" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the changetype is {@code "delete"}.
     *
     * @return {@code true} for delete operations
     */
    public boolean isDelete()
    {
        return getUnfoldedChangeType().equals( "delete" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the changetype is {@code "modify"}.
     *
     * @return {@code true} for modify operations
     */
    public boolean isModify()
    {
        return getUnfoldedChangeType().equals( "modify" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the changetype is {@code "moddn"} or
     * {@code "modrdn"} (case-insensitive).
     *
     * @return {@code true} for moddn or modrdn operations
     */
    public boolean isModDn()
    {
        String unfoldedChangeType = getUnfoldedChangeType();

        return "moddn".equalsIgnoreCase( unfoldedChangeType ) || "modrdn".equalsIgnoreCase( unfoldedChangeType ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages for missing changetype spec or missing
     * operation type; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedChangeTypeSpec().length() == 0 )
        {
            return "Missing spec 'changetype'";
        }
        else if ( getUnfoldedChangeType().length() == 0 )
        {
            return "Missing changetype";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHODS ───────────────────────────────────────────────────────
    /**
     * Creates a {@code changetype: delete} line at offset 0.
     *
     * @return a new {@link LdifChangeTypeLine}
     */
    public static LdifChangeTypeLine createDelete()
    {
        return new LdifChangeTypeLine( 0, "changetype", ":", "delete", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    /**
     * Creates a {@code changetype: add} line at offset 0.
     *
     * @return a new {@link LdifChangeTypeLine}
     */
    public static LdifChangeTypeLine createAdd()
    {
        return new LdifChangeTypeLine( 0, "changetype", ":", "add", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    /**
     * Creates a {@code changetype: modify} line at offset 0.
     *
     * @return a new {@link LdifChangeTypeLine}
     */
    public static LdifChangeTypeLine createModify()
    {
        return new LdifChangeTypeLine( 0, "changetype", ":", "modify", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    /**
     * Creates a {@code changetype: moddn} line at offset 0.
     *
     * @return a new {@link LdifChangeTypeLine}
     */
    public static LdifChangeTypeLine createModDn()
    {
        return new LdifChangeTypeLine( 0, "changetype", ":", "moddn", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    /**
     * Creates a {@code changetype: modrdn} line at offset 0.
     *
     * @return a new {@link LdifChangeTypeLine}
     */
    public static LdifChangeTypeLine createModRdn()
    {
        return new LdifChangeTypeLine( 0, "changetype", ":", "modrdn", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
