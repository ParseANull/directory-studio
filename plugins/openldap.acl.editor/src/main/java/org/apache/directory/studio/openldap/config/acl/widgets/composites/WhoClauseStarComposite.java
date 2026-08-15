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
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseStar;


// ── CLASS: WhoClauseStarComposite — TARKIN GRANTING ACCESS TO ALL REQUESTORS ─
// Grand Moff Tarkin applies the ACL rule to every requestor: "by *". The star
// who-clause requires no additional configuration, so this composite is a no-op
// leaf that stores the clause reference only.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the wildcard ({@code *}) who-clause ("by *"). No SWT
 * controls are required — this is a no-op leaf that stores the clause reference only.
 *
 * <p>Think of this class as Grand Moff Tarkin matching every requestor — the
 * catch-all clause that follows all specific rules.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhoClauseStarComposite extends AbstractWhoClauseComposite<AclWhoClauseStar>
{
    // ── Constructor With Explicit Clause ──────────────────────────────────────
    /**
     * Creates a new star who-clause composite with an explicit clause.
     *
     * @param context               The ACL context.
     * @param clause                The wildcard who-clause to store.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseStarComposite( OpenLdapAclValueWithContext context, AclWhoClauseStar clause, Composite visualEditorComposite )
    {
        super( context, clause, visualEditorComposite );
    }


    // ── Constructor Without Explicit Clause ───────────────────────────────────
    /**
     * Creates a new star who-clause composite with a default {@link AclWhoClauseStar} instance.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseStarComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, new AclWhoClauseStar(), visualEditorComposite );
    }
}
