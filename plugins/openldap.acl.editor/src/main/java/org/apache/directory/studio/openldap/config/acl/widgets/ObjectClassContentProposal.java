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


// ── CLASS: ObjectClassContentProposal — C-3PO SUGGESTING AN OBJECTCLASS ──────
// C-3PO knows the Empire's full objectClass schema. When the officer types "@"
// or "!" in the attrs= field, C-3PO suggests all objectClasses from the
// connection's schema — each one wrapped in an ObjectClassContentProposal.
// The type is important: the comparator in the proposal provider places objectClass
// proposals after attribute types but before nothing, and the prefix ("@" or "!")
// is already included in the content string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A content proposal representing an LDAP objectClass in the attribute widget
 * completion list. The content string includes the {@code @} or {@code !} prefix
 * (e.g. {@code "@inetOrgPerson"} or {@code "!groupOfNames"}).
 *
 * <p>Think of this class as C-3PO flagging a completion as an objectClass
 * reference so the comparator can group it separately from attribute types.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassContentProposal extends AttributesWidgetContentProposal
{
    // ── Constructing an ObjectClass Proposal ──────────────────────────────────
    // C-3PO creates one entry for each objectClass completion, with the "@" or
    // "!" prefix already embedded in the content string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new objectClass content proposal with the given content string.
     * The content should include any prefix character ({@code @} or {@code !}).
     *
     * <p>For example — C-3PO offering "@inetOrgPerson" as a completion:</p>
     * <pre>
     *   ObjectClassContentProposal p = new ObjectClassContentProposal("@inetOrgPerson");
     *   p.getContent(); // → "@inetOrgPerson"
     * </pre>
     *
     * @param content  The objectClass proposal string including its prefix.
     */
    public ObjectClassContentProposal( String content )
    {
        super( content );
    }
}
