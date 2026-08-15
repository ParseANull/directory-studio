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


import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.directory.api.ldap.model.exception.LdapLoopDetectedException;
import org.apache.directory.api.ldap.model.message.Referral;


// ── CLASS: ReferralsInfo — C-3PO'S DIPLOMATIC ROUTING LOG ────────────────────
// When C-3PO is following up diplomatic referrals across the galaxy, he keeps
// a careful log: a queue of leads still to follow up, and a record of every
// address he's already visited.  If he spots an address he's already been to,
// he knows he's caught in a diplomatic loop — time to abort before the
// Millennium Falcon runs out of fuel circling the same spaceport.
// This class is that log: a queue of LDAP referrals still to process, and a
// set of already-visited URLs to detect (and optionally break) referral loops.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Tracks pending and already-processed LDAP referrals during a single search operation.
 * When an LDAP server returns a referral response, it's saying "go look over there."
 * We queue those "look over there" referrals and process them one at a time.
 * To avoid following the same referral twice (which would cause an infinite loop),
 * we record every URL we've already chased.
 * If a loop is detected, we can either silently drop the duplicate or throw
 * {@link LdapLoopDetectedException} — controlled by the constructor flag.
 * Think of this as C-3PO's referral routing log: queue up leads, mark ones as visited,
 * and scream (or quietly discard) if you see one you've already been to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReferralsInfo
{
    /** Queue of referrals we still need to follow. */
    private LinkedList<Referral> referralsToProcess = new LinkedList<Referral>();

    /** Set of all URLs we've already visited — used to detect loops. */
    private Set<String> processedUrls = new HashSet<String>();

    /** If true, throw an exception on loop detection; if false, silently discard. */
    private boolean throwExceptionOnLoop;


    // ── CONSTRUCTOR — C-3PO OPENS A FRESH ROUTING LOG ────────────────────────────
    // C-3PO opens a fresh diplomatic routing log for this round of referral chasing.
    // The throwExceptionOnLoop flag controls whether he panics (throws) or just
    // quietly crosses out the repeated address and moves on.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ReferralsInfo} for tracking referrals during one operation.
     *
     * @param throwExceptionOnLoop  If {@code true}, throw {@link LdapLoopDetectedException}
     *                              when a referral loop is detected; if {@code false}, silently
     *                              drop the duplicate referral.
     */
    public ReferralsInfo( boolean throwExceptionOnLoop )
    {
        this.throwExceptionOnLoop = throwExceptionOnLoop;
    }


    // ── ADD REFERRAL — C-3PO ADDS A NEW LEAD TO THE QUEUE ────────────────────────
    // C-3PO receives a referral from the current server and adds it to the
    // bottom of his "leads to follow" stack.
    // We append the referral at the tail of our processing queue.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a referral to the end of the pending-processing queue.
     * Call this each time the server returns a referral response during a search.
     *
     * @param referral  The referral to queue for processing.
     */
    public void addReferral( Referral referral )
    {
        referralsToProcess.addLast( referral );
    }


    // ── GET NEXT REFERRAL — C-3PO PICKS UP THE NEXT LEAD ─────────────────────────
    // C-3PO checks the queue, skips any already-visited addresses, then picks up
    // the next fresh lead.  Returns null if the queue is empty.
    // If a loop is detected and we're set to throw, he panics.
    // We remove the head of the queue, mark its URLs as visited, and return it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns and removes the next unvisited referral from the queue.
     * Before returning, we filter out any referrals whose URLs we've already chased.
     * The returned referral's URLs are added to the visited set.
     * Returns {@code null} when the queue is empty (all referrals processed).
     *
     * @return  The next referral to process, or {@code null} if none remain.
     * @throws LdapLoopDetectedException  If a referral loop is detected and
     *                                    {@code throwExceptionOnLoop} was {@code true}.
     */
    public Referral getNextReferral() throws LdapLoopDetectedException
    {
        handleAlreadyProcessedUrls();
        if ( !referralsToProcess.isEmpty() )
        {
            Referral referral = referralsToProcess.removeFirst();
            processedUrls.addAll( referral.getLdapUrls() );
            return referral;
        }
        else
        {
            return null;
        }
    }


    // ── HAS MORE REFERRALS — C-3PO CHECKS IF THERE ARE MORE LEADS ────────────────
    // C-3PO scans the remaining leads and skips any already-visited ones, then
    // reports whether there's at least one fresh lead left.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if there are more unvisited referrals to process.
     * We filter out any already-visited URLs before answering.
     *
     * @return  {@code true} if at least one more referral remains; {@code false} otherwise.
     * @throws LdapLoopDetectedException  If a referral loop is detected and
     *                                    {@code throwExceptionOnLoop} was {@code true}.
     */
    public boolean hasMoreReferrals() throws LdapLoopDetectedException
    {
        handleAlreadyProcessedUrls();
        return !referralsToProcess.isEmpty();
    }


    // ── HANDLE ALREADY PROCESSED URLS — C-3PO FILTERS THE DUPLICATE LEADS ────────
    // C-3PO scans the front of the queue, dropping any referrals pointing at addresses
    // he's already visited.  If he finds one and the panic flag is set, he throws;
    // otherwise he just crosses it out and moves to the next one.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal helper: removes any referrals at the head of the queue whose URLs
     * overlap with the already-visited set.
     * We scan from the front and drop or throw for each duplicate, stopping when
     * we reach a referral with all-new URLs.
     *
     * @throws LdapLoopDetectedException  If {@code throwExceptionOnLoop} is {@code true}
     *                                    and a duplicate URL is found.
     */
    private void handleAlreadyProcessedUrls() throws LdapLoopDetectedException
    {
        while ( !referralsToProcess.isEmpty() )
        {
            Referral referral = referralsToProcess.getFirst();
            boolean alreadyProcessed = referral.getLdapUrls().stream().anyMatch( url -> processedUrls.contains( url ) );
            if ( alreadyProcessed )
            {
                // yes, already processed, remove the current referral and continue with filtering
                if ( throwExceptionOnLoop )
                {
                    throw new LdapLoopDetectedException( "Referral " + referral.getLdapUrls() + " already processed" );
                }
                else
                {
                    referralsToProcess.removeFirst();
                }
            }
            else
            {
                // no, not yet processed, done with filtering
                return;
            }
        }
    }

}
