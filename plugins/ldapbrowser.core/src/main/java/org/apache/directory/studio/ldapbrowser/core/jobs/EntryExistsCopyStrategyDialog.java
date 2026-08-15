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
package org.apache.directory.studio.ldapbrowser.core.jobs;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: EntryExistsCopyStrategyDialog — MACE WINDU CONFRONTS PALPATINE ────
// Mace Windu has discovered Palpatine's identity (a target entry already exists
// at the destination DN).  He must decide: abort the arrest attempt (BREAK), let
// Palpatine walk free this time (IGNORE_AND_CONTINUE), forcibly detain him and
// replace what was there (OVERWRITE_AND_CONTINUE), or use a different approach
// and book him under a different name (RENAME_AND_CONTINUE).
// During a copy operation, if the destination DN already exists, we don't just
// crash — we ask the user via this dialog what to do.  The four strategies map
// directly to those choices.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog interface presented to the user when a copy operation encounters an
 * entry that already exists at the destination DN.
 * Implementations live in the UI layer; this interface keeps the core layer
 * free of SWT/JFace dependencies while still being able to invoke the dialog
 * mid-job.  The four strategies (BREAK, IGNORE, OVERWRITE, RENAME) give the
 * user full control over how to resolve the naming collision.
 * Think of it as Mace Windu deciding how to handle Palpatine when caught.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EntryExistsCopyStrategyDialog
{

    // ── Mace Windu Confirms Palpatine's Identity And Location ────────────────
    // "I know who you are and which office you occupy."  We tell the dialog
    // exactly which entry already exists so it can display the details.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Tells the dialog which entry already exists at the target location.
     * The dialog implementation will typically display this information so the
     * user can make an informed decision about the copy strategy.
     *
     * @param browserConnection the connection to the target LDAP server.
     * @param newLdapDn         the DN of the conflicting existing entry.
     */
    void setExistingEntry( IBrowserConnection browserConnection, Dn newLdapDn );


    // ── Mace Windu Announces His Decision ────────────────────────────────────
    // "We're going with the arrest — here's the strategy."  Returns whichever
    // option the user selected in the dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the copy strategy selected by the user.
     *
     * @return the chosen {@link EntryExistsCopyStrategy}; may be {@code null}
     *         if the user closed the dialog without selecting anything.
     */
    EntryExistsCopyStrategy getStrategy();


    // ── Mace Windu Suggests A New Identity ────────────────────────────────────
    // If the user chose RENAME_AND_CONTINUE, this returns the new RDN they
    // typed in — the alias Palpatine will be booked under.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new RDN chosen by the user when
     * {@link EntryExistsCopyStrategy#RENAME_AND_CONTINUE} was selected.
     * Call this only after {@link #getStrategy()} returns RENAME_AND_CONTINUE.
     *
     * @return the new RDN, or {@code null} if another strategy was selected.
     */
    Rdn getRdn();


    // ── Mace Windu Sets A Standing Order ─────────────────────────────────────
    // "Apply this ruling to all future collisions in this operation."  When
    // true, the copy loop uses this strategy for every subsequent conflict
    // without re-asking the user.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the user wants to remember the selected strategy
     * and apply it to all subsequent conflicts in the current copy operation.
     *
     * @return {@code true} to remember the selection.
     */
    boolean isRememberSelection();


    // ── Mace Windu Steps Into The Chamber ────────────────────────────────────
    // "I need to speak to the Chancellor."  The dialog is displayed; we wait
    // for the user to make a choice before the copy loop continues.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the dialog and blocks until the user makes a selection.
     * Implementations will typically use {@code SWT.OK} or {@code SWT.CANCEL}
     * as the status code.
     *
     * @return the dialog's return code (e.g. {@code Window.OK} or
     *         {@code Window.CANCEL}).
     */
    int open();

    // ── ENUM: EntryExistsCopyStrategy — MACE WINDU'S FOUR OPTIONS ────────────
    /**
     * Enum describing the four strategies available when the destination entry
     * already exists during a copy operation.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum EntryExistsCopyStrategy
    {

        // ── Mace Windu Aborts The Arrest ──────────────────────────────────────
        /** Break strategy: stop the copy operation immediately. */
        BREAK,

        // ── Mace Windu Walks Away This Time ───────────────────────────────────
        /** Ignore the conflicting entry and continue copying the remaining entries. */
        IGNORE_AND_CONTINUE,

        // ── Mace Windu Detains Palpatine In Place ─────────────────────────────
        /** Overwrite the existing entry with the source entry's attributes and continue. */
        OVERWRITE_AND_CONTINUE,

        // ── Mace Windu Books Palpatine Under A Different Name ─────────────────
        /** Rename the copied entry to a new RDN and continue copying. */
        RENAME_AND_CONTINUE;
    }

}
