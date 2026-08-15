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


// ── CLASS: LdifModSpecTypeLine — REBEL FIELD MODIFICATION TYPE DECLARATION ────
// The first line of every Rebel field-modification order declares the operation
// and the target attribute: "add: mail", "replace: sn", "delete: cn".
// The operation must be exactly one of those three keywords — anything else
// fails validation.
// LdifModSpecTypeLine models that opening declaration: extends LdifValueLineBase
// with getRawModType/getRawAttributeDescription accessors, operation predicates
// (isAdd, isReplace, isDelete), and three factory methods.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF mod-spec type line — the opening line of a modification specification:
 * {@code add: <attr>}, {@code replace: <attr>}, or {@code delete: <attr>}.
 * Extends {@link LdifValueLineBase} — the operation keyword is the line-start
 * segment, the separator is {@code ":"}, and the attribute description is the
 * value.
 * Valid when the superclass check passes and the operation is exactly
 * {@code "add"}, {@code "replace"}, or {@code "delete"}.
 * Use {@link #createAdd}, {@link #createReplace}, or {@link #createDelete} to
 * build standard lines.
 * Think of this as the Rebel field modification type declaration — one word
 * that tells the server which kind of attribute operation follows.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifModSpecTypeLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a mod-spec type line with all five raw segments.
     *
     * @param offset                   byte offset in the document
     * @param rawModType               the raw operation keyword ({@code "add"}, etc.)
     * @param rawValueType             the separator ({@code ":"})
     * @param rawAttributeDescription  the raw attribute name
     * @param rawNewLine               the raw line-ending characters
     */
    public LdifModSpecTypeLine( int offset, String rawModType, String rawValueType, String rawAttributeDescription,
        String rawNewLine )
    {
        super( offset, rawModType, rawValueType, rawAttributeDescription, rawNewLine );
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    /**
     * Returns the raw operation keyword ({@code "add"}, {@code "replace"}, or
     * {@code "delete"}).
     *
     * @return the raw mod-type segment
     */
    public String getRawModType()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded operation keyword.
     *
     * @return the unfolded mod-type segment
     */
    public String getUnfoldedModType()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw attribute description (attribute name).
     *
     * @return the raw attribute description
     */
    public String getRawAttributeDescription()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded attribute description.
     *
     * @return the unfolded attribute description
     */
    public String getUnfoldedAttributeDescription()
    {
        return super.getUnfoldedValue();
    }


    // ── OPERATION TYPE PREDICATES ─────────────────────────────────────────────
    /**
     * Returns {@code true} if the mod type is {@code "add"}.
     *
     * @return {@code true} for add
     */
    public boolean isAdd()
    {
        return getUnfoldedModType().equals( "add" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the mod type is {@code "replace"}.
     *
     * @return {@code true} for replace
     */
    public boolean isReplace()
    {
        return getUnfoldedModType().equals( "replace" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the mod type is {@code "delete"}.
     *
     * @return {@code true} for delete
     */
    public boolean isDelete()
    {
        return getUnfoldedModType().equals( "delete" ); //$NON-NLS-1$
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass check passes and the mod type is one of
     * {@code "add"}, {@code "replace"}, or {@code "delete"}.</p>
     */
    public boolean isValid()
    {
        return super.isValid() && ( isAdd() || isReplace() || isDelete() );
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages for missing or invalid mod type and
     * missing attribute description.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedModType().length() == 0 )
        {
            return "Missing modification type 'add', 'replace' or 'delete'";
        }
        else if ( !isAdd() && !isReplace() && !isDelete() )
        {
            return "Invalid modification type, expected 'add', 'replace' or 'delete'";
        }
        else if ( getUnfoldedAttributeDescription().length() == 0 )
        {
            return "Missing attribute";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHODS ───────────────────────────────────────────────────────
    /**
     * Creates an {@code add: <attributeName>} mod-spec type line at offset 0.
     *
     * @param attributeName  the attribute to add
     * @return a new {@link LdifModSpecTypeLine}
     */
    public static LdifModSpecTypeLine createAdd( String attributeName )
    {
        return new LdifModSpecTypeLine( 0, "add", ":", attributeName, LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * Creates a {@code replace: <attributeName>} mod-spec type line at offset 0.
     *
     * @param attributeName  the attribute to replace
     * @return a new {@link LdifModSpecTypeLine}
     */
    public static LdifModSpecTypeLine createReplace( String attributeName )
    {
        return new LdifModSpecTypeLine( 0, "replace", ":", attributeName, LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * Creates a {@code delete: <attributeName>} mod-spec type line at offset 0.
     *
     * @param attributeName  the attribute to delete
     * @return a new {@link LdifModSpecTypeLine}
     */
    public static LdifModSpecTypeLine createDelete( String attributeName )
    {
        return new LdifModSpecTypeLine( 0, "delete", ":", attributeName, LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
