/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.apache.directory.studio.connection.core;


import java.util.List;


// ── CLASS: IReferralHandler — C-3PO REDIRECTS HAN THROUGH THE RIGHT CHANNEL ───
// An LDAP referral is like a diplomatic redirect: the server says "what you need
// is actually over at this other address."  Someone has to decide which of those
// addresses to follow and which saved connection to use to get there.
// C-3PO is the perfect choice — he knows every diplomatic protocol and can
// select the right communication channel for the situation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface through which the connection core asks a higher-level layer
 * (typically the UI plugin) which connection to use when following an LDAP referral.
 * An LDAP referral is a server response that says "the data you requested is actually
 * at this other LDAP URL — go ask there."  This interface decouples the core
 * referral-chasing logic from the UI dialog that lets users pick a connection.
 * Think of this interface as C-3PO's diplomatic routing protocol: the core asks
 * "which channel do we use for this redirect?" and C-3PO selects the right one.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IReferralHandler
{
    // ── GET REFERRAL CONNECTION — C-3PO PICKS THE RIGHT DIPLOMATIC CHANNEL ────────
    // The server hands C-3PO a list of possible addresses for the referral.
    // C-3PO consults the crew and selects the saved connection that matches.
    // Returning null means "don't chase this referral — abort the operation."
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the saved {@link Connection} to use when following an LDAP referral.
     * The implementation may show the user a dialog listing the referral URLs and
     * asking which saved connection to route through.
     * Returning {@code null} cancels referral chasing for this operation.
     *
     * <p>For example — C-3PO selects the channel:</p>
     * <pre>
     *   List&lt;String&gt; urls = Arrays.asList("ldap://dc2.example.com/dc=example,dc=com");
     *   Connection target = handler.getReferralConnection(urls);
     *   if (target == null) return; // user chose to ignore the referral
     *   // ... re-issue the request on target
     * </pre>
     *
     * @param referralUrls  The list of LDAP URLs the server provided in the referral response.
     * @return  The {@link Connection} to use for following the referral,
     *          or {@code null} to cancel referral chasing.
     */
    Connection getReferralConnection( List<String> referralUrls );
}
