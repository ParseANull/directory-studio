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

package org.apache.directory.studio.ldifparser.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;


// ── CLASS: LdifFile — REBEL DATA SMUGGLING MANIFEST ──────────────────────────
// The Rebellion's data-smuggling network keeps a manifest of every communiqué
// being carried: you can look up a record by its byte offset, splice new
// records in, or ask the manifest to print itself in formatted or raw form.
// LdifFile is that manifest: a serialisable list of LdifContainers (version,
// comment, content record, change record, separator, EOF) with static helpers
// for position-based container/part lookup and in-place container replacement.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The in-memory model of a parsed LDIF document.
 * Holds an ordered list of {@link LdifContainer} objects (version, comments,
 * content records, change records, separators, and EOF markers) and exposes:
 * <ul>
 *   <li>type queries ({@link #isContentType()}, {@link #isChangeType()})</li>
 *   <li>static position-based lookups ({@link #getContainer},
 *       {@link #getContainerContent}, {@link #getParts})</li>
 *   <li>in-place replacement ({@link #replace}) for editor-driven splicing</li>
 *   <li>full serialisation ({@link #toRawString()},
 *       {@link #toFormattedString})</li>
 * </ul>
 * Think of this as the Rebel data-smuggling manifest — every transmitted
 * record is listed, locatable by offset, and replaceable in place.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifFile implements Serializable
{
    /** The serialVersionUID */
    private static final long serialVersionUID = 846864138240517008L;

    /** Ordered list of all parsed containers (including separators, EOF, etc.). */
    private List<LdifContainer> containerList = new ArrayList<LdifContainer>();

    /**
     * Set to {@code true} when a {@link LdifChangeRecord} is added, marking
     * this as a change-type LDIF file.
     */
    private boolean hasChanges = false;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates an empty LDIF file model with no containers.
     */
    public LdifFile()
    {
    }


    // ── TYPE QUERIES ─────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this file contains only content records (no
     * changetype operations).
     *
     * @return {@code true} if no change records have been added
     */
    public boolean isContentType()
    {
        return !hasChanges;
    }


    /**
     * Returns {@code true} if this file contains at least one changetype
     * operation (add, modify, delete, or moddn).
     *
     * @return {@code true} if any change record has been added
     */
    public boolean isChangeType()
    {
        return hasChanges;
    }


    // ── ADD A CONTAINER ───────────────────────────────────────────────────────
    /**
     * Appends {@code container} to the end of the container list.
     * If {@code container} is a {@link LdifChangeRecord}, also sets the
     * {@link #hasChanges} flag.
     *
     * @param container  the container to add
     */
    public void addContainer( LdifContainer container )
    {
        containerList.add( container );

        if ( container instanceof LdifChangeRecord )
        {
            hasChanges = true;
        }
    }


    // ── GET ALL CONTAINERS ────────────────────────────────────────────────────
    /**
     * Returns the full ordered list of containers (version headers, comments,
     * records, separators, and EOF markers).
     *
     * @return the live container list
     */
    public List<LdifContainer> getContainers()
    {
        return containerList;
    }


    // ── GET RECORDS ONLY ──────────────────────────────────────────────────────
    /**
     * Returns an array of all {@link LdifRecord} containers (both valid and
     * invalid records), excluding version headers, comments, and separators.
     *
     * @return array of {@link LdifRecord} instances in document order
     */
    public LdifRecord[] getRecords()
    {
        List<LdifRecord> recordList = new ArrayList<LdifRecord>();

        for ( LdifContainer container : containerList )
        {
            if ( container instanceof LdifRecord )
            {
                recordList.add( ( LdifRecord ) container );
            }
        }

        return recordList.toArray( new LdifRecord[recordList.size()] );
    }


    // ── GET THE LAST CONTAINER ────────────────────────────────────────────────
    /**
     * Returns the last container in the list, or {@code null} if the list is
     * empty.
     *
     * @return the last {@link LdifContainer}, or {@code null}
     */
    public LdifContainer getLastContainer()
    {
        if ( containerList.isEmpty() )
        {
            return null;
        }
        else
        {
            return containerList.get( containerList.size() - 1 );
        }
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * Returns the raw (unformatted) LDIF text of all containers concatenated.
     *
     * @return the full raw LDIF string
     */
    public String toRawString()
    {
        StringBuilder sb = new StringBuilder();

        for ( LdifContainer container : containerList )
        {
            sb.append( container.toRawString() );
        }

        return sb.toString();
    }


    /**
     * Returns the formatted LDIF text of all containers concatenated using
     * {@code formatParameters}.
     *
     * @param formatParameters  the line-width, space-after-colon, and
     *                          line-separator settings to apply
     * @return the full formatted LDIF string
     */
    public String toFormattedString( LdifFormatParameters formatParameters )
    {
        StringBuilder sb = new StringBuilder();

        for ( LdifContainer ldifContainer : containerList )
        {
            sb.append( ldifContainer.toFormattedString( formatParameters ) );
        }

        return sb.toString();
    }


    /**
     * Returns a debug-friendly string listing all containers.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for ( LdifContainer ldifContainer : containerList )
        {
            sb.append( ldifContainer );
        }

        return sb.toString();
    }


    // ── STATIC LOOKUP: CONTAINER AT OFFSET ────────────────────────────────────
    /**
     * Returns the {@link LdifContainer} in {@code model} whose range covers
     * {@code offset}, or {@code null} if none does.
     *
     * @param model   the LDIF file to search
     * @param offset  the byte offset to look up
     * @return the container at {@code offset}, or {@code null}
     */
    public static LdifContainer getContainer( LdifFile model, int offset )
    {
        if ( ( model == null ) || ( offset < 0 ) )
        {
            return null;
        }

        List<LdifContainer> containers = model.getContainers();

        if ( containers.size() > 0 )
        {
            for ( LdifContainer ldifContainer : containers )
            {
                if ( ( ldifContainer.getOffset() <= offset ) &&
                    ( offset < ldifContainer.getOffset() + ldifContainer.getLength() ) )
                {
                    return ldifContainer;
                }
            }
        }

        return null;
    }


    // ── STATIC LOOKUP: INNER MODSPEC AT OFFSET ────────────────────────────────
    /**
     * Returns the {@link LdifModSpec} part inside {@code container} whose range
     * covers {@code offset}, or {@code null} if none does.
     *
     * @param container  the containing {@link LdifContainer}
     * @param offset     the byte offset to look up
     * @return the nested {@link LdifModSpec}, or {@code null}
     */
    public static LdifModSpec getInnerContainer( LdifContainer container, int offset )
    {
        if ( ( container == null ) ||
            ( offset < container.getOffset() ) ||
            ( offset > container.getOffset() + container.getLength() ) )
        {
            return null;
        }

        LdifModSpec innerContainer = null;
        LdifPart[] parts = container.getParts();

        if ( parts.length > 0 )
        {
            for ( LdifPart ldifPart : parts )
            {
                int start = ldifPart.getOffset();
                int end = ldifPart.getOffset() + ldifPart.getLength();

                if ( ( start <= offset ) && ( offset < end ) && ( ldifPart instanceof LdifModSpec ) )
                {
                    innerContainer = ( LdifModSpec ) ldifPart;
                    break;
                }
            }
        }

        return innerContainer;
    }


    // ── STATIC LOOKUP: CONTAINERS OVERLAPPING A RANGE ─────────────────────────
    /**
     * Returns all {@link LdifContainer}s in {@code model} whose ranges overlap
     * the region {@code [offset, offset+length)}.
     *
     * @param model   the LDIF file to search
     * @param offset  the start of the region
     * @param length  the length of the region
     * @return array of overlapping containers, or {@code null} on bad input
     */
    public static LdifContainer[] getContainers( LdifFile model, int offset, int length )
    {
        if ( ( model == null ) || ( offset < 0 ) )
        {
            return null;
        }

        List<LdifContainer> containerList = new ArrayList<LdifContainer>();
        List<LdifContainer> containers = model.getContainers();

        if ( containers.size() > 0 )
        {
            for ( LdifContainer container : containers )
            {
                int containerOffset = container.getOffset();

                if ( ( offset < containerOffset + container.getLength() ) &&
                    ( offset + length > containerOffset ) )
                {
                    containerList.add( container );
                }
            }
        }

        return containerList.toArray( new LdifContainer[containerList.size()] );
    }


    // ── STATIC LOOKUP: PARTS OVERLAPPING A RANGE (from model) ─────────────────
    /**
     * Returns all {@link LdifPart}s in {@code model} that overlap the region
     * {@code [offset, offset+length)}.  Recurses into {@link LdifModSpec}
     * containers.  For consecutive invalid parts the preceding valid part is
     * substituted.
     *
     * @param model   the LDIF file to search
     * @param offset  the start of the region
     * @param length  the length of the region
     * @return array of overlapping parts, or {@code null} on bad input
     */
    public static LdifPart[] getParts( LdifFile model, int offset, int length )
    {
        if ( ( model == null ) || ( offset < 0 ) )
        {
            return null;
        }

        List<LdifContainer> containers = model.getContainers();

        return getParts( containers, offset, length );

    }


    // ── STATIC LOOKUP: PARTS OVERLAPPING A RANGE (from list) ──────────────────
    /**
     * Returns all {@link LdifPart}s in {@code containers} that overlap the
     * region {@code [offset, offset+length)}.  Recurses into
     * {@link LdifModSpec} parts.
     *
     * @param containers  the container list to search
     * @param offset      the start of the region
     * @param length      the length of the region
     * @return array of overlapping parts, or {@code null} on bad input
     */
    public static LdifPart[] getParts( List<LdifContainer> containers, int offset, int length )
    {
        if ( ( containers == null ) || ( offset < 0 ) )
        {
            return null;
        }

        List<LdifPart> partList = new ArrayList<LdifPart>();

        for ( LdifContainer ldifContainer : containers )
        {
            int ldifContainerOffset = ldifContainer.getOffset();

            if ( ( offset < ldifContainerOffset + ldifContainer.getLength() )
                && ( offset + length >= ldifContainerOffset ) )
            {
                LdifPart[] ldifParts = ldifContainer.getParts();
                LdifPart previousLdifPart = null;

                for ( LdifPart ldifPart : ldifParts )
                {
                    int ldifPartOffset = ldifPart.getOffset();

                    if ( ( offset < ldifPartOffset + ldifPart.getLength() ) && ( offset + length >= ldifPartOffset ) )
                    {
                        if ( ldifPart instanceof LdifModSpec )
                        {
                            LdifModSpec spec = ( LdifModSpec ) ldifPart;
                            List<LdifContainer> newLdifContainer = new ArrayList<LdifContainer>();
                            newLdifContainer.add( spec );

                            partList.addAll( Arrays.asList( getParts( newLdifContainer, offset, length ) ) );
                        }
                        else
                        {
                            if ( ( ldifPart instanceof LdifInvalidPart ) && ( previousLdifPart != null ) )
                            {
                                ldifPart = previousLdifPart;
                            }

                            partList.add( ldifPart );
                        }

                        previousLdifPart = ldifPart;
                    }

                }
            }
        }

        return partList.toArray( new LdifPart[partList.size()] );
    }


    // ── STATIC LOOKUP: PART AT OFFSET WITHIN CONTAINER ────────────────────────
    /**
     * Returns the {@link LdifPart} inside {@code container} whose range covers
     * {@code offset}, recursing into {@link LdifModSpec} containers.
     * Returns {@code null} if {@code offset} is outside the container's range.
     *
     * @param container  the container to search
     * @param offset     the byte offset to look up
     * @return the part at {@code offset}, or {@code null}
     */
    public static LdifPart getContainerContent( LdifContainer container, int offset )
    {
        int containerOffset = container.getOffset();

        if ( ( container == null ) || ( offset < containerOffset ) ||
            ( offset > containerOffset + container.getLength() ) )
        {
            return null;
        }

        LdifPart part = null;
        LdifPart[] parts = container.getParts();

        if ( parts.length > 0 )
        {
            for ( LdifPart ldifPart : parts )
            {
                int start = ldifPart.getOffset();
                int end = ldifPart.getOffset() + ldifPart.getLength();

                if ( ( start <= offset ) && ( offset < end ) )
                {
                    if ( ldifPart instanceof LdifModSpec )
                    {
                        part = getContainerContent( ( LdifModSpec ) ldifPart, offset );
                    }

                    break;
                }
            }
        }

        return part;
    }


    // ── IN-PLACE CONTAINER REPLACEMENT ───────────────────────────────────────
    // Remove old containers, insert new ones at the same position, then
    // shift the offsets of all subsequent containers to reflect the delta.
    /**
     * Replaces {@code oldContainers} in the container list with
     * {@code newContainers}, adjusting the absolute offsets of all containers
     * that follow the replaced region.
     *
     * <p>The first element of {@code oldContainers} determines the insertion
     * index.  All new containers are positioned starting from the offset of
     * the first removed container.</p>
     *
     * @param oldContainers  the containers to remove (must be in the list)
     * @param newContainers  the replacement containers (offsets will be
     *                       adjusted to the insertion point)
     */
    public void replace( LdifContainer[] oldContainers, List<LdifContainer> newContainers )
    {
        // find index
        int index = 0;

        if ( oldContainers.length > 0 )
        {
            index = containerList.indexOf( oldContainers[0] );
        }

        // remove old containers
        int removeLength = 0;
        int removeOffset = 0;

        if ( oldContainers.length > 0 )
        {
            removeOffset = oldContainers[0].getOffset();

            for ( int i = 0; i < oldContainers.length; i++ )
            {
                containerList.remove( index );
                removeLength += oldContainers[i].getLength();
            }
        }

        // add new containers
        int insertLength = 0;
        int pos = 0;

        for ( LdifContainer ldifContainer : newContainers )
        {
            ldifContainer.adjustOffset( removeOffset );
            insertLength += ldifContainer.getLength();
            containerList.add( index + pos, ldifContainer );
            pos++;
        }

        // adjust offset of following containers
        int adjust = insertLength - removeLength;

        for ( int i = index + newContainers.size(); i < containerList.size(); i++ )
        {
            LdifContainer container = containerList.get( i );
            container.adjustOffset( adjust );
        }
    }
}
