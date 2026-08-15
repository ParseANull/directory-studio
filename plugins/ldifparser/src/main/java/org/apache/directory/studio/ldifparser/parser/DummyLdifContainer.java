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

package org.apache.directory.studio.ldifparser.parser;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;


// ── CLASS: DummyLdifContainer — REBEL TEMPORARY HOLDING CONTAINER ─────────────
// When a Rebel courier receives partial transmissions that don't yet belong to
// any known record, the dispatcher drops them into a temporary holding bag
// stamped "not valid, assign later."  The bag is always invalid — it exists only
// to give those orphaned fragments a home until the parser figures out where
// they belong.
// DummyLdifContainer is that holding bag: a minimal LdifContainer subclass that
// accepts one LdifPart, exposes any contained comment lines, and always returns
// false from isValid().
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A minimal, always-invalid {@link LdifContainer} used internally by
 * {@link LdifParser} to hold parts that do not yet belong to a proper record
 * during parsing.
 * {@link #isValid()} always returns {@code false}; the container is never
 * promoted into the final {@link org.apache.directory.studio.ldifparser.model.LdifFile} model.
 * Think of this as the Rebel temporary holding bag — a throwaway home for
 * orphaned parts until the parser assigns them a real container.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DummyLdifContainer extends LdifContainer
{
    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────
    // ── Orphaned Part Intake ──────────────────────────────────────────────────
    // The dispatcher stamps the holding bag with the first orphaned fragment
    // and places it on the shelf.
    // We call the parent constructor so the part lands in ldifParts.
    /**
     * Creates a dummy container seeded with the given part.
     *
     * @param part  the initial (and typically only) part to hold
     */
    public DummyLdifContainer( LdifPart part )
    {
        super( part );
    }


    // ── COMMENT EXTRACTION ────────────────────────────────────────────────────
    // ── Retrieve Comment Fragments from the Bag ───────────────────────────────
    // When the dispatcher sorts through the holding bag he extracts anything
    // labelled "comment" and stacks it separately so the parser can attach it
    // to the right record.
    // We filter ldifParts for LdifCommentLine instances and return them as an
    // array.
    /**
     * Returns all {@link LdifCommentLine} parts held in this container.
     *
     * @return an array of comment lines (may be empty, never {@code null})
     */
    public LdifCommentLine[] getComments()
    {
        List<LdifPart> ldifPartList = new ArrayList<LdifPart>();

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ldifPart instanceof LdifCommentLine )
            {
                ldifPartList.add( ldifPart );
            }
        }

        return ( LdifCommentLine[] ) ldifPartList.toArray( new LdifCommentLine[ldifPartList.size()] );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    // ── Holding Bag is Always Invalid ────────────────────────────────────────
    // The holding bag is explicitly marked "NOT a valid record" so no consumer
    // will try to process it as a real LDIF container.
    // We return false unconditionally — this container is temporary by design.
    /**
     * Always returns {@code false} — a dummy container is never a valid LDIF
     * construct; it exists only during parsing.
     *
     * @return {@code false}
     */
    public boolean isValid()
    {
        return false;
    }
}
