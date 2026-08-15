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
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseTlsSsf;


// ── CLASS: WhoClauseTlsSsfComposite — TARKIN REQUIRING TLS ENCRYPTION TIER ───
// Grand Moff Tarkin requires the TLS layer security strength factor to meet a
// minimum encryption tier. This composite binds
// AbstractWhoClauseCryptoStrengthComposite to AclWhoClauseTlsSsf. The SSF combo
// and optional custom spinner are inherited from the abstract base.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the {@code tls_ssf} who-clause. Inherits the SSF
 * preset-tier combo and custom spinner from
 * {@link AbstractWhoClauseCryptoStrengthComposite}.
 *
 * <p>Think of this class as Grand Moff Tarkin requiring a minimum TLS
 * encryption tier before the rule applies.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhoClauseTlsSsfComposite extends AbstractWhoClauseCryptoStrengthComposite<AclWhoClauseTlsSsf>
{
    // ── Constructor With Explicit Clause ──────────────────────────────────────
    /**
     * Creates a new TLS-SSF who-clause composite with an explicit clause.
     *
     * @param context               The ACL context.
     * @param clause                The TLS-SSF clause to edit.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseTlsSsfComposite( OpenLdapAclValueWithContext context, AclWhoClauseTlsSsf clause, Composite visualEditorComposite )
    {
        super( context, clause, visualEditorComposite );
    }


    // ── Constructor Without Explicit Clause ───────────────────────────────────
    /**
     * Creates a new TLS-SSF who-clause composite with a default
     * {@link AclWhoClauseTlsSsf} instance.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseTlsSsfComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, new AclWhoClauseTlsSsf(), visualEditorComposite );
    }
}
