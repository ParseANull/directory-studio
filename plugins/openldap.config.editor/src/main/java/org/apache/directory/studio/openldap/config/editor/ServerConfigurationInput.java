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
package org.apache.directory.studio.openldap.config.editor;


import org.apache.directory.studio.openldap.config.jobs.EntryBasedConfigurationPartition;
import org.eclipse.ui.IEditorInput;


// ── CLASS: ServerConfigurationInput — Leia's Hologram Contracts ──────────────
// In the opening of A New Hope, Leia records a desperate message into R2-D2,
// establishing a contract: "Help me, Obi-Wan Kenobi — you're my only hope."
// The message has a defined shape (who's sending it, what it contains) that
// anyone receiving it must honor — just like this interface defines the shape
// every editor input must honor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The contract every server configuration input must fulfill to work with
 * our editor.
 * It extends Eclipse's {@link IEditorInput} and adds the concept of an
 * "original partition" — a snapshot of the config as it was when we first
 * loaded it, which we need to compute diffs on save.
 * Think of this as Leia's hologram protocol: any input that wants to talk to
 * our editor must declare these two abilities — get the snapshot and set it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ServerConfigurationInput extends IEditorInput
{
    // ── Hologram Retrieves The Embedded Message ───────────────────────────────
    // R2-D2 plays Leia's hologram — the data she encoded is now readable to
    // whoever intercepts the droid.
    // Obi-Wan reaches in and extracts exactly what Leia recorded.
    // We retrieve the partition snapshot that was captured when the config
    // was originally loaded, so we can diff it later.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the original configuration partition snapshot we captured when
     * we first loaded this configuration.
     * We keep this around so that on save, we can compare it against the
     * current (potentially modified) state and produce a minimal set of LDAP
     * modifications rather than rewriting everything.
     *
     * @return the original partition, or {@code null} if none has been set yet
     */
    EntryBasedConfigurationPartition getOriginalPartition();


    // ── Hologram Records Leia's Distress Call ─────────────────────────────────
    // Leia's technicians encode her message into R2-D2's memory banks before
    // the ship is boarded.
    // The message is sealed away until someone with the right context unpacks it.
    // We store the partition snapshot here so it can be retrieved at save time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the original configuration partition snapshot for later comparison.
     * This is typically called by the background loading job once it finishes
     * reading the config from the server or directory.
     *
     * @param originalPartition  the partition to remember — we'll diff against
     *                           this on the next save
     */
    void setOriginalPartition( EntryBasedConfigurationPartition originalPartition );
}
