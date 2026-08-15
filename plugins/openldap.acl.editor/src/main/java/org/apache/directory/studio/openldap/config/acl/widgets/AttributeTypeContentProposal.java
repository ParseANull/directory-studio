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


// ── CLASS: AttributeTypeContentProposal — C-3PO SUGGESTING AN ATTRIBUTE TYPE ─
// When the officer starts typing an attribute name in the attrs= field, C-3PO
// offers a list of schema attribute types as completions. Each one is wrapped
// in an AttributeTypeContentProposal — a thin subclass that marks the proposal
// as an attribute type (rather than an objectClass or a keyword) so the
// completion provider can group and label proposals correctly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A content proposal representing a schema attribute type (e.g. {@code uid},
 * {@code cn}, {@code mail}) in the ACL attributes widget completion list.
 * Extends {@link AttributesWidgetContentProposal} without adding any new behaviour —
 * the type distinction is handled by the class type alone.
 *
 * <p>Think of this class as C-3PO labelling a completion entry as "attribute type"
 * so the proposal provider knows what prefix (if any) to apply.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeContentProposal extends AttributesWidgetContentProposal
{
    // ── Constructing an Attribute Type Proposal ────────────────────────────────
    // C-3PO creates one entry for an attribute type completion, carrying just
    // the attribute name string as the completion content.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new attribute type content proposal with the given content string.
     *
     * <p>For example — C-3PO offering "uid" as an attribute type completion:</p>
     * <pre>
     *   AttributeTypeContentProposal p = new AttributeTypeContentProposal("uid");
     *   p.getContent(); // → "uid"
     * </pre>
     *
     * @param content  The attribute type name to offer as a completion.
     */
    public AttributeTypeContentProposal( String content )
    {
        super( content );
    }
}
