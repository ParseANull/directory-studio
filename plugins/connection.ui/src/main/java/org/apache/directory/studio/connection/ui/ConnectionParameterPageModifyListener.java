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

package org.apache.directory.studio.connection.ui;


import org.apache.directory.studio.connection.core.ConnectionParameter;


// ── INTERFACE: ConnectionParameterPageModifyListener — CHEWIE'S COPILOT SIGNAL ──
// When Chewie adjusts something in the engine room, he calls out to Han in the
// cockpit so Han can update the status displays.  That's exactly what this
// listener does: whenever any field on a ConnectionParameterPage changes,
// the page calls connectionParameterPageModified() and the host dialog (the
// wizard or property page) updates its OK/Finish button state.
// The second method, getTestConnectionParameters(), lets the page request a
// "test-so-far" connection parameter snapshot (e.g., for the "Check network
// parameters" button) without having to commit the dialog.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface that {@link ConnectionParameterPage} implementations use to
 * notify their host dialog whenever a field value changes.
 *
 * <p>The host dialog (the New Connection Wizard or the connection Properties page)
 * implements this interface and registers itself with each page during
 * {@link ConnectionParameterPage#init}.  When a field changes, the page calls
 * {@link #connectionParameterPageModified()} so the dialog can re-validate all
 * pages and update its OK/Finish button state.</p>
 *
 * <p>The second method, {@link #getTestConnectionParameters()}, allows a page to ask
 * the dialog for a snapshot of the current (possibly incomplete) connection
 * parameters — for example, to run a connectivity test without committing.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ConnectionParameterPageModifyListener
{
    // ── CONNECTION PARAMETER PAGE MODIFIED — CHEWIE'S "HEADS UP" ─────────────────
    /**
     * Called by a {@link ConnectionParameterPage} whenever any of its input fields
     * change value.
     * The host dialog should use this callback to re-check all pages' validity and
     * enable/disable its OK or Finish button accordingly.
     */
    void connectionParameterPageModified();


    // ── GET TEST CONNECTION PARAMETERS — SNAPSHOT FOR CONNECTIVITY TEST ───────────
    /**
     * Returns a {@link ConnectionParameter} snapshot assembled from the current
     * (possibly unsaved) field values across all pages.
     * Pages use this to run a connectivity test (e.g., "Check network parameters")
     * without forcing the user to click Finish first.
     *
     * @return  A {@link ConnectionParameter} reflecting the current UI state.
     */
    ConnectionParameter getTestConnectionParameters();
}
