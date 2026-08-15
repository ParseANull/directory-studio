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
package org.apache.directory.studio.openldap.config.acl.widgets.composites;


import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseAnonymous;


// ── CLASS: WhoClauseAnonymousComposite — TARKIN GRANTING ANONYMOUS ACCESS ─────
// Grand Moff Tarkin signs the directive that allows unauthenticated (anonymous)
// requestors to be matched by this who-clause. No additional parameters are
// needed — anonymous is identified simply by the keyword "anonymous" in the ACL.
// This composite is therefore a no-op leaf that stores the clause reference only.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the {@code anonymous} who-clause. No SWT controls are
 * required — this is a no-op leaf that stores the context and clause reference.
 *
 * <p>Think of this class as Grand Moff Tarkin marking a who-clause row as
 * "anonymous" — no configuration beyond the keyword is needed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhoClauseAnonymousComposite extends AbstractWhoClauseComposite<AclWhoClauseAnonymous>
{
    // ── Constructor With Explicit Clause ──────────────────────────────────────
    /**
     * Creates a new anonymous who-clause composite with an explicit clause.
     *
     * @param context               The ACL context.
     * @param clause                The anonymous clause to store.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseAnonymousComposite( OpenLdapAclValueWithContext context, AclWhoClauseAnonymous clause, Composite visualEditorComposite )
    {
        super( context, clause, visualEditorComposite );
    }


    // ── Constructor Without Explicit Clause ───────────────────────────────────
    /**
     * Creates a new anonymous who-clause composite with a default
     * {@link AclWhoClauseAnonymous} instance.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseAnonymousComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, new AclWhoClauseAnonymous(), visualEditorComposite );
    }
}
