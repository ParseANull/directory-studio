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
package org.apache.directory.studio.connection.ui.widgets;


import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;


// ── CLASS: ExtendedContentAssistCommandAdapter — R2-D2 OPENS THE PROPOSAL HATCH ──
// When C-3PO needs to access the ship's computer he normally has to wait for the
// hatch to open automatically — but sometimes you need to force it open or slam it
// shut from the outside.  ContentAssistCommandAdapter provides content-assist popups
// but its open/close methods are package-private, so no external code can control
// the popup programmatically.
// ExtendedContentAssistCommandAdapter extends that class and promotes
// closeProposalPopup() and openProposalPopup() to public so callers can manage the
// popup lifecycle themselves.  The constructor also pins the preferred settings used
// throughout the connection UI: PROPOSAL_REPLACE, FILTER_NONE, no auto-activation
// characters, and zero delay.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Extension of {@link ContentAssistCommandAdapter} that exposes
 * {@link #closeProposalPopup()} and {@link #openProposalPopup()} as public
 * methods, allowing callers to control the proposal-popup lifecycle
 * programmatically.
 *
 * <p>The constructor also configures the following defaults:</p>
 * <ul>
 *   <li>{@link ContentProposalAdapter#PROPOSAL_REPLACE} — selecting a proposal
 *       replaces the entire field content.</li>
 *   <li>{@link ContentProposalAdapter#FILTER_NONE} — the adapter does not filter
 *       proposals; the provider is responsible for filtering.</li>
 *   <li>Auto-activation characters: {@code null} (disabled).</li>
 *   <li>Auto-activation delay: {@code 0} ms.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtendedContentAssistCommandAdapter extends ContentAssistCommandAdapter
{
    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ExtendedContentAssistCommandAdapter} with the
     * connection-UI defaults wired in.
     *
     * <p>After the super-constructor returns we apply:</p>
     * <ul>
     *   <li>PROPOSAL_REPLACE — accepts overwrite the field text.</li>
     *   <li>FILTER_NONE — no client-side filtering; the provider filters.</li>
     *   <li>Auto-activation chars = {@code null} — popup only via the keybinding.</li>
     *   <li>Auto-activation delay = {@code 0} ms — no delay when triggered.</li>
     * </ul>
     *
     * @param control                  The SWT control to attach content-assist to.
     * @param controlContentAdapter    Adapter that bridges between the control and
     *                                 the proposal framework.
     * @param proposalProvider         Source of completion proposals.
     * @param commandId                Eclipse command ID that triggers content-assist
     *                                 (e.g. {@code ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS}).
     * @param autoActivationCharacters Characters that auto-open the popup, or
     *                                 {@code null} to disable auto-activation.
     * @param installDecoration        Whether to install the lightweight decoration
     *                                 (the small content-assist hint image).
     */
    public ExtendedContentAssistCommandAdapter( Control control, IControlContentAdapter controlContentAdapter,
        IContentProposalProvider proposalProvider, String commandId, char[] autoActivationCharacters,
        boolean installDecoration )
    {
        super( control, controlContentAdapter, proposalProvider, commandId, autoActivationCharacters,
            installDecoration );

        // ── APPLY DEFAULT SETTINGS ────────────────────────────────────────────────
        // Replace the whole field when a proposal is accepted, do no client-side
        // filtering, disable character-based auto-activation, and open immediately.
        // ──────────────────────────────────────────────────────────────────────────
        setProposalAcceptanceStyle( ContentProposalAdapter.PROPOSAL_REPLACE );
        setFilterStyle( ContentProposalAdapter.FILTER_NONE );
        setAutoActivationCharacters( null );
        setAutoActivationDelay( 0 );
    }


    // ── CLOSE PROPOSAL POPUP ─────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Promoted to {@code public} so external code can close the proposal popup
     * programmatically — for example when the host dialog is about to hide its
     * content.</p>
     */
    @Override
    public void closeProposalPopup()
    {
        super.closeProposalPopup();
    }


    // ── OPEN PROPOSAL POPUP ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Promoted to {@code public} so external code can open the proposal popup
     * programmatically — for example when a field gains focus after a programmatic
     * value change.</p>
     */
    @Override
    public void openProposalPopup()
    {
        super.openProposalPopup();
    }
}
