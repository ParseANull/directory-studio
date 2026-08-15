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

package org.apache.directory.studio.ldapbrowser.core.model;


import org.apache.directory.api.ldap.model.url.LdapUrl;


// ── CLASS: IContinuation — POE DAMERON FOLLOWING A DISTRESS SIGNAL ────────────
// Poe Dameron's X-Wing picks up a distress signal: coordinates are there, but
// the signal could be from Resistance territory or an Imperial trap.  Poe has to
// decide: ignore it (CANCELED), investigate the coordinates and get a green
// light from base (RESOLVED), or sit with the unresolved signal for now
// (UNRESOLVED).  Each state is a phase in that decision.
// An IContinuation is an LDAP referral URL encountered during a search — the
// server said "more results over there at this other URL."  We need to decide
// whether to follow it (RESOLVED), skip it (CANCELED), or defer the decision
// (UNRESOLVED).  The user picks a matching connection and we follow the referral.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP search continuation — a referral URL returned by the server
 * during a search that points to additional results on another server or subtree.
 * The continuation moves through three states: UNRESOLVED (we haven't asked the
 * user which connection to use), CANCELED (the user declined), or RESOLVED (a
 * connection was selected and we can follow the URL).
 * Think of this as Poe Dameron intercepting a distress signal: the coordinates
 * are there but we need to decide whether and how to act on them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IContinuation
{

    // ── ENUM: State — THE SIGNAL'S CURRENT STATUS ─────────────────────────────────
    // Poe's three possible statuses for any incoming distress signal.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * The lifecycle states of a search continuation (referral URL).
     */
    enum State
    {
        /** The referral URL has been received but not yet acted on. */
        UNRESOLVED,

        /** The user explicitly declined to follow this referral. */
        CANCELED,

        /** A suitable connection was selected and the referral can be followed. */
        RESOLVED
    }


    // ── Poe Checks Whether The Signal Has Been Resolved Yet ───────────────────────
    // "Is this signal still pending, already cancelled, or fully cleared for approach?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current resolve state of this continuation.
     *
     * <p>For example — checking before following:</p>
     * <pre>
     *   if (continuation.getState() == State.RESOLVED) { follow(); }
     * </pre>
     *
     * @return the {@link State}; never {@code null}.
     */
    State getState();


    // ── Poe Opens Comms To Ask Base For A Go/No-Go ────────────────────────────────
    // Poe patches through to base and asks: "I've got a signal at these coords —
    // which of our ships should I assign to follow it?"  The user picks a connection,
    // which moves the state to RESOLVED (or CANCELED if they decline).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Asks the user (via a dialog) to select a connection for following this
     * referral URL.  On success, state moves to {@link State#RESOLVED};
     * if the user cancels, state moves to {@link State#CANCELED}.
     */
    void resolve();


    // ── Poe Reads The Signal Coordinates ──────────────────────────────────────────
    // "The beacon is broadcasting from ldap://other-server/dc=branch,dc=org."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP URL this continuation points to.
     * The URL encodes the target server and the subtree to search.
     *
     * @return the referral {@link LdapUrl}; never {@code null}.
     */
    LdapUrl getUrl();
}
