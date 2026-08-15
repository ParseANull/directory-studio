/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.widgets;


import org.eclipse.jface.fieldassist.ContentProposal;


// ── CLASS: AttributesWidgetContentProposal — C-3PO'S GENERIC PHRASE TEMPLATE ─
// Before C-3PO can suggest a specific phrase (attribute type, objectClass, or
// keyword) he needs a general completion wrapper that knows where the user's
// partial input starts — so he can insert only the missing tail of the phrase
// rather than the whole string. This abstract class extends JFace ContentProposal
// with a startPosition field and a getContent() override that returns only the
// suffix starting at startPosition. Concrete subclasses (AttributeTypeContentProposal,
// ObjectClassContentProposal, KeywordContentProposal) differ only by type.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base for all attribute-widget completion proposals. Extends
 * {@link ContentProposal} with a {@code startPosition} field that tells the
 * framework how many leading characters the user has already typed. The
 * overridden {@link #getContent()} returns only the suffix starting at
 * {@code startPosition}, so the framework inserts only the missing part.
 *
 * <p>Think of this class as C-3PO's generic phrase template — concrete sub-
 * classes specialise it for attribute types, objectClasses, and keywords.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AttributesWidgetContentProposal extends ContentProposal
{
    /** The start position */
    private int startPosition = 0;


    // ── Constructing a Proposal With a Full Content String ─────────────────────
    // C-3PO memorises the full phrase (e.g. "inetOrgPerson") and will trim it
    // to the suffix when the framework calls getContent().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new proposal with the given full content string. The display
     * label is the same as the content (inherited from {@link ContentProposal}).
     *
     * @param content  The full completion string; must not be {@code null}.
     */
    public AttributesWidgetContentProposal( String content )
    {
        super( content );
    }


    // ── Reading the Start Position ─────────────────────────────────────────────
    // The proposal provider sets this after it determines how many characters
    // the user has already typed, so getContent() can trim correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of already-typed characters that should be skipped
     * when inserting this proposal. Set by the proposal provider to match the
     * length of the user's current partial input.
     *
     * @return  The start position offset; defaults to {@code 0}.
     */
    public int getStartPosition()
    {
        return startPosition;
    }


    // ── Setting the Start Position ─────────────────────────────────────────────
    // The proposal provider calls this with the length of what the user has typed
    // so the proposal knows how much to trim before inserting.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the start position — the number of leading characters to skip when
     * the proposal is inserted. The provider sets this equal to the length of
     * the user's current partial input.
     *
     * @param startPosition  The offset from the beginning of the full content string.
     */
    public void setStartPosition( int startPosition )
    {
        this.startPosition = startPosition;
    }


    // ── Returning Only the Suffix After What Was Already Typed ────────────────
    // C-3PO returns only the missing part of the phrase — if the user has typed
    // "inet" he inserts "OrgPerson" rather than the full "inetOrgPerson".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the insertion content: the full string from the super-class, trimmed
     * to the suffix starting at {@link #startPosition}. This ensures the framework
     * inserts only the part the user has not already typed.
     *
     * <p>For example — completing after the user typed "inet":</p>
     * <pre>
     *   proposal.setStartPosition(4); // "inet" already typed
     *   proposal.getContent();        // → "OrgPerson" (not "inetOrgPerson")
     * </pre>
     *
     * {@inheritDoc}
     *
     * @return  The insertion suffix, or {@code null} if the super-class content is null.
     */
    public String getContent()
    {
        String content = super.getContent();
        if ( content != null )
        {
            return content.substring( startPosition );
        }

        return null;
    }
}
