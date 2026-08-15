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
import org.apache.directory.studio.openldap.config.acl.model.AclWhatClauseStar;


// ── CLASS: WhatClauseStarComposite — TARKIN TARGETING EVERY ENTRY ─────────────
// Grand Moff Tarkin points the targeting system at everything: "access to *".
// The star what-clause requires no additional configuration, so this composite
// is a leaf subclass that merely stores the context and clause reference.
// No SWT controls are created; createComposite() returns the inherited no-op null.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the wildcard ({@code *}) what-clause. Requires no
 * additional UI controls — this composite is a no-op leaf that stores the
 * context and clause reference only.
 *
 * <p>Think of this class as Grand Moff Tarkin targeting every entry in the
 * directory — "access to *" — with nothing further to configure.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhatClauseStarComposite extends AbstractClauseComposite
{
    // ── Constructing the Star Composite ───────────────────────────────────────
    /**
     * Creates a new star what-clause composite. No SWT controls are created.
     *
     * @param context               The ACL context.
     * @param clause                The wildcard what-clause (for future use).
     * @param visualEditorComposite The visual editor composite.
     */
    public WhatClauseStarComposite( OpenLdapAclValueWithContext context, AclWhatClauseStar clause, Composite visualEditorComposite )
    {
        super( context, visualEditorComposite );
    }
}
