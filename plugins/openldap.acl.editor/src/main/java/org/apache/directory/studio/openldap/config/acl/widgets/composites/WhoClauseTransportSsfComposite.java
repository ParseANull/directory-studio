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
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseTransportSsf;


// ── CLASS: WhoClauseTransportSsfComposite — TARKIN REQUIRING TRANSPORT SSF ───
// Grand Moff Tarkin requires the transport-layer security strength factor
// to meet a minimum encryption tier. This composite binds
// AbstractWhoClauseCryptoStrengthComposite to AclWhoClauseTransportSsf and also
// implements the WhoClauseComposite interface. The SSF combo and optional custom
// spinner are inherited from the abstract base.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A clause composite for the {@code transport_ssf} who-clause. Inherits the SSF
 * preset-tier combo and custom spinner from
 * {@link AbstractWhoClauseCryptoStrengthComposite}, and also implements
 * {@link WhoClauseComposite}.
 *
 * <p>Think of this class as Grand Moff Tarkin requiring a minimum transport-layer
 * encryption tier before the rule applies.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WhoClauseTransportSsfComposite extends AbstractWhoClauseCryptoStrengthComposite<AclWhoClauseTransportSsf>
    implements
    WhoClauseComposite<AclWhoClauseTransportSsf>
{
    // ── Constructor With Explicit Clause ──────────────────────────────────────
    /**
     * Creates a new Transport-SSF who-clause composite with an explicit clause.
     *
     * @param context               The ACL context.
     * @param clause                The Transport-SSF clause to edit.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseTransportSsfComposite( OpenLdapAclValueWithContext context, AclWhoClauseTransportSsf clause, Composite visualEditorComposite )
    {
        super( context, clause, visualEditorComposite );
    }


    // ── Constructor Without Explicit Clause ───────────────────────────────────
    /**
     * Creates a new Transport-SSF who-clause composite with a default
     * {@link AclWhoClauseTransportSsf} instance.
     *
     * @param context               The ACL context.
     * @param visualEditorComposite The visual editor composite.
     */
    public WhoClauseTransportSsfComposite( OpenLdapAclValueWithContext context, Composite visualEditorComposite )
    {
        super( context, new AclWhoClauseTransportSsf(), visualEditorComposite );
    }
}
