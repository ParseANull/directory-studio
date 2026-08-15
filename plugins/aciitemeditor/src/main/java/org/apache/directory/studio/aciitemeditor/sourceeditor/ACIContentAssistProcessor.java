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
package org.apache.directory.studio.aciitemeditor.sourceeditor;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.aciitemeditor.ACIITemConstants;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ACIContentAssistProcessor — R2-D2 SUGGESTING ACI ESCAPE ROUTES ────
// R2-D2 plugs into the Death Star's data port and, as the Rebels edit their
// escape plan, he pops up a list of valid next moves: "Turn left here", "Open
// this door", "These are the droids you're looking for."
// ACIContentAssistProcessor does the same for ACI text: when the user presses
// Ctrl+Space it offers template snippets for whatever comes next in the ACI syntax.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Content-assist processor for the ACI source editor.
 * Extends {@link TemplateCompletionProcessor} to serve snippet proposals from
 * the ACI template store ({@link ACIITemConstants#ACI_ITEM_TEMPLATE_ID}) whenever
 * the user invokes Ctrl+Space or types a letter in the editor.
 * Think of this class as R2-D2 plugging into the data port and popping up a
 * list of valid next moves: it offers ready-made ACI clause snippets so the
 * user does not have to remember the full syntax.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIContentAssistProcessor extends TemplateCompletionProcessor
{
    // ── COLLECT AND RETURN PROPOSALS ──────────────────────────────────────────
    // R2-D2 scans the available escape routes at the current position and returns
    // the full list in order of relevance.
    // We delegate to the superclass for template proposals and merge any future
    // non-template proposals (keyword completions, etc.) into the same list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public ICompletionProposal[] computeCompletionProposals( ITextViewer viewer, int offset )
    {
        List<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>();

        // Add context dependend template proposals
        ICompletionProposal[] templateProposals = super.computeCompletionProposals( viewer, offset );

        if ( templateProposals != null )
        {
            proposalList.addAll( Arrays.asList( templateProposals ) );
        }

        return ( ICompletionProposal[] ) proposalList.toArray( new ICompletionProposal[proposalList.size()] );
    }


    // ── CONTEXT INFORMATION (NOT USED) ────────────────────────────────────────
    // R2-D2 does not display a parameter pop-up for ACI constructs; context
    // information is not meaningful here, so we return null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public IContextInformation[] computeContextInformation( ITextViewer viewer, int offset )
    {
        return null;
    }


    // ── AUTO-ACTIVATE ON EVERY LETTER ────────────────────────────────────────
    // R2-D2 starts beeping at the first keystroke — we activate the proposal
    // popup automatically on any letter so the user sees options right away.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {

        char[] chars = new char[52];
        for ( int i = 0; i < 26; i++ )
        {
            chars[i] = ( char ) ( 'a' + i );
        }
        for ( int i = 0; i < 26; i++ )
        {
            chars[i + 26] = ( char ) ( 'A' + i );
        }

        return chars;
    }


    // ── NO CONTEXT-INFO AUTO-ACTIVATION ──────────────────────────────────────
    // ACI has no parameter-info pop-up, so there is nothing to auto-activate
    // for context information.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }


    // ── NO CONTEXT-INFO VALIDATOR ─────────────────────────────────────────────
    // Without context information there is nothing to validate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }


    // ── NO ERROR MESSAGE ──────────────────────────────────────────────────────
    // R2-D2 does not complain when no proposals are found — the list simply
    // stays empty and the popup closes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return null;
    }


    // ── RESOLVE THE ACI CONTEXT TYPE ─────────────────────────────────────────
    // R2-D2 checks which section of the Death Star he is plugged into so he
    // knows which category of routes to suggest.
    // We always return the single ACI_ITEM context type — there is only one
    // template context for the whole ACI language.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected TemplateContextType getContextType( ITextViewer viewer, IRegion region )
    {
        return Activator.getDefault().getAciTemplateContextTypeRegistry().getContextType(
            ACIITemConstants.ACI_ITEM_TEMPLATE_ID );
    }


    // ── PROVIDE A TEMPLATE ICON (NOT USED) ───────────────────────────────────
    // R2-D2's proposals don't have individual icons in the pop-up list;
    // returning null is fine here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Image getImage( Template template )
    {
        return null;
    }


    // ── FETCH TEMPLATES FROM THE STORE ───────────────────────────────────────
    // R2-D2 retrieves the list of known escape routes from the ship's memory
    // bank (the template store) filtered by the current context type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Template[] getTemplates( String contextTypeId )
    {
        return Activator.getDefault().getAciTemplateStore().getTemplates( contextTypeId );
    }
}
