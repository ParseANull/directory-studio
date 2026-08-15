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

package org.apache.directory.studio.ldifparser.model.container;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.LdifParserConstants;
import org.apache.directory.studio.ldifparser.model.LdifInvalidPart;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.lines.LdifLineBase;


// ── CLASS: LdifContainer — DEATH STAR BLUEPRINT SECTION ──────────────────────
// Each major section of the Death Star blueprint — the main reactor, the
// docking bay, the superlaser housing — is a self-contained unit that knows
// where it starts, how long it is, whether all its sub-components are valid,
// and how to serialise itself back to the original text.
// LdifContainer is that section abstraction in the LDIF model: an abstract
// base class holding an ordered list of LdifParts (lines, mod-specs, invalid
// fragments, EOF markers) and implementing getOffset/getLength, raw/formatted
// string serialisation, and offset adjustment as final methods.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for every LDIF model "section": records, comments,
 * separators, mod-specs, version containers, and EOF containers.
 * Maintains an ordered {@link List} of {@link LdifPart} children.  Provides
 * final implementations of {@link #getOffset()}, {@link #getLength()},
 * {@link #toRawString()}, {@link #toFormattedString}, {@link #adjustOffset},
 * and {@link #getParts()}.  Subclasses must implement {@link #isValid()}.
 * Think of this as the abstract blueprint-section class — every section knows
 * its own bounds and can print itself back out.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class LdifContainer implements LdifPart
{
    /** Ordered list of all {@link LdifPart} children in this container. */
    protected List<LdifPart> ldifParts = new ArrayList<LdifPart>();


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a container with {@code part} as its first child.
     *
     * @param part  the initial part (must not be {@code null})
     * @throws IllegalArgumentException if {@code part} is {@code null}
     */
    protected LdifContainer( LdifPart part )
    {
        if ( part == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( part );
    }


    // ── POSITION ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the offset of the first child part.</p>
     */
    public final int getOffset()
    {
        return ldifParts.get( 0 ).getOffset();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Computed as {@code lastPart.offset + lastPart.length - firstPart.offset}.</p>
     */
    public final int getLength()
    {
        LdifPart lastPart = getLastPart();

        return lastPart.getOffset() + lastPart.getLength() - getOffset();
    }


    // ── ADD AN INVALID PART ───────────────────────────────────────────────────
    /**
     * Appends an {@link LdifInvalidPart} to this container's part list.
     * Called by the parser when it encounters unrecognised text inside a
     * partially-recognised container.
     *
     * @param invalid  the invalid part to append (must not be {@code null})
     * @throws IllegalArgumentException if {@code invalid} is {@code null}
     */
    public final void addInvalid( LdifInvalidPart invalid )
    {
        if ( invalid == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( invalid );
    }


    // ── PART ACCESSORS ────────────────────────────────────────────────────────
    /**
     * Returns the last part in the container's part list.
     *
     * @return the last {@link LdifPart}
     */
    public final LdifPart getLastPart()
    {
        return ldifParts.get( ldifParts.size() - 1 );
    }


    /**
     * Returns all parts in this container as an array.
     *
     * @return array of {@link LdifPart} in document order
     */
    public final LdifPart[] getParts()
    {
        return ( LdifPart[] ) ldifParts.toArray( new LdifPart[ldifParts.size()] );
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * Returns a debug-friendly multi-line description showing the class name
     * followed by each child part.
     */
    public final String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( getClass().getSimpleName() );
        sb.append( ":" ); //$NON-NLS-1$
        sb.append( LdifParserConstants.LINE_SEPARATOR );

        for ( LdifPart part : ldifParts )
        {
            sb.append( "    " ); //$NON-NLS-1$
            sb.append( part.toString() );
            sb.append( LdifParserConstants.LINE_SEPARATOR );
        }

        return sb.toString();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Concatenates {@link LdifPart#toRawString()} for each child part.</p>
     */
    public final String toRawString()
    {
        StringBuilder sb = new StringBuilder();

        for ( LdifPart part : ldifParts )
        {
            sb.append( part.toRawString() );
        }

        return sb.toString();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Concatenates {@link LdifPart#toFormattedString} for each child part.</p>
     */
    public final String toFormattedString( LdifFormatParameters formatParameters )
    {
        StringBuilder sb = new StringBuilder();

        for ( LdifPart part : ldifParts )
        {
            sb.append( part.toFormattedString( formatParameters ) );
        }

        return sb.toString();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Subclasses implement their own validity rules and typically delegate
     * to {@link #isAbstractValid()} as their base check.</p>
     */
    public abstract boolean isValid();


    // ── ABSTRACT VALIDITY CHECK ───────────────────────────────────────────────
    /**
     * Returns {@code true} when the container has at least one part, no
     * {@link LdifInvalidPart} children, and all parts report valid — stopping
     * at the first {@link LdifLineBase} found (the container is considered
     * sufficiently well-formed if its first real line is valid).
     *
     * @return {@code true} if the container passes the base validity check
     */
    protected boolean isAbstractValid()
    {
        if ( ldifParts.isEmpty() )
        {
            return false;
        }

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ( ldifPart instanceof LdifInvalidPart ) || ( !ldifPart.isValid() ) )
            {
                return false;
            }

            if ( ldifPart instanceof LdifLineBase )
            {
                return true;
            }
        }

        return false;
    }


    // ── INVALID STRING ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code "Empty Container"} if the part list is empty, or
     * the first child's {@link LdifPart#getInvalidString()} for the first
     * invalid child, or {@code null} if all children are valid.</p>
     */
    public String getInvalidString()
    {
        if ( ldifParts.isEmpty() )
        {
            return "Empty Container";
        }

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( !ldifPart.isValid() )
            {
                return ldifPart.getInvalidString();
            }
        }

        return null;
    }


    // ── OFFSET ADJUSTMENT ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Propagates {@code adjustOffset(adjust)} to all child parts.</p>
     */
    public final void adjustOffset( int adjust )
    {
        for ( LdifPart ldifPart : ldifParts )
        {
            ldifPart.adjustOffset( adjust );
        }
    }
}
