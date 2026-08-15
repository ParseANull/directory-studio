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


import org.apache.directory.studio.ldifparser.model.LdifInvalidPart;


// ── CLASS: LdifInvalidContainer — HAN'S "BAD FEELING" CONTAINER ───────────────
// When the Rebel archivist encounters a fragment of the transmission that
// she simply cannot parse — it's corrupted, out of place, or just garbage —
// she bags it in an invalid container rather than discarding it, so the error
// can be shown in the UI without losing any of the surrounding valid data.
// LdifInvalidContainer is that bag: a top-level container wrapping a single
// LdifInvalidPart, always reporting isValid() = false.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for an unrecognised or invalid top-level fragment.
 * Wraps a single {@link LdifInvalidPart} and always returns {@code false}
 * from {@link #isValid()}.  Preserves the original text so error annotations
 * can be shown in the editor.
 * Think of this as Han Solo's "bad feeling" bag — something's wrong, but we
 * keep the evidence rather than throwing it away.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifInvalidContainer extends LdifContainer
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates an invalid container wrapping {@code invalid}.
     *
     * @param invalid  the invalid part to wrap (must not be {@code null})
     */
    public LdifInvalidContainer( LdifInvalidPart invalid )
    {
        super( invalid );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return always {@code false} — this container is by definition invalid
     */
    public boolean isValid()
    {
        return false;
    }
}
