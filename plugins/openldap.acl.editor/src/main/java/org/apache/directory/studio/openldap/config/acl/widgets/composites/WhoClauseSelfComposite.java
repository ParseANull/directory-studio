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
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseSelf;


// ── CLASS: WhoClauseSelfComposite — TARKIN GRANTING SELF-ACCESS ───────────────
// Grand Moff Tarkin permits an LDAP entry to access its own attributes by
// matching the "self" keyword. No additional parameters are required. This
// composite is a no-op leaf that stores the clause reference only.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the {@code self} who-clause. No SWT controls are
 * required — this is a no-op leaf that stores the clause reference only.
 *
 * <p>Think of this class as Grand Moff Tarkin permitting an entry to read
 * its own attributes — self-service access, no configuration needed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhoClauseSelfComposite extends AbstractWhoClauseComposite<AclWhoClauseSelf>
{
    // ── Constructor With Explicit Clause ──────────────────────────────────────
    /**
     * Creates a new self who-clause composite with an explicit clause.
     *
     * @param context               The ACL context.
     * @param clause                The self clause to store.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseSelfComposite( OpenLdapAclValueWithContext context, AclWhoClauseSelf clause, Composite visualEditorComposite )
    {
        super( context, clause, visualEditorComposite );
    }


    // ── Constructor Without Explicit Clause ───────────────────────────────────
    /**
     * Creates a new self who-clause composite with a default {@link AclWhoClauseSelf} instance.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseSelfComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, new AclWhoClauseSelf(), visualEditorComposite );
    }
}
