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


// ── CLASS: KeywordContentProposal — C-3PO SUGGESTING A BUILT-IN ACL KEYWORD ─
// Among C-3PO's completions are special reserved tokens: "entry" and "children".
// These are not schema-derived — they are fixed ACL keywords that target the
// entry or its children pseudo-attribute. This subclass marks a proposal as a
// keyword so the comparator in the proposal provider can sort keywords before
// attribute types and objectClasses in the dropdown.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A content proposal representing a fixed ACL keyword (currently {@code "entry"}
 * and {@code "children"}) in the attribute widget completion list. Keywords appear
 * before attribute types and objectClasses in the sorted proposal dropdown.
 *
 * <p>Think of this class as C-3PO flagging a completion as a built-in keyword
 * rather than a schema element — so it bubbles to the top of the list.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class KeywordContentProposal extends AttributesWidgetContentProposal
{
    // ── Constructing a Keyword Proposal ────────────────────────────────────────
    // C-3PO creates one entry for a fixed keyword like "entry" or "children".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new keyword content proposal with the given content string.
     *
     * <p>For example — C-3PO offering "entry" as a keyword completion:</p>
     * <pre>
     *   KeywordContentProposal p = new KeywordContentProposal("entry");
     *   p.getContent(); // → "entry"
     * </pre>
     *
     * @param content  The keyword to offer as a completion (e.g. {@code "entry"}).
     */
    public KeywordContentProposal( String content )
    {
        super( content );
    }
}
