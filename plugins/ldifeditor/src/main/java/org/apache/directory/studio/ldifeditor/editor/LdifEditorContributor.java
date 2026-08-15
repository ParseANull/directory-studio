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

package org.apache.directory.studio.ldifeditor.editor;


import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;


// ── CLASS: LdifEditorContributor — REBEL COMMS OFFICER ───────────────────────
// When a Rebel communications officer takes the chair at the console they
// plug in the content-assist shortcut so the operator can press Ctrl+Space
// and get transmission suggestions.  When they leave the chair they unplug it.
// LdifEditorContributor does the same thing for Eclipse's global action bars:
// it wires the content-assist retarget action to the active LDIF editor when
// that editor takes focus, and removes it when focus moves away.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link BasicTextEditorActionContributor} that manages the global
 * content-assist action for the LDIF editor.
 * Registers a {@link RetargetTextEditorAction} bound to the
 * {@code ContentAssistProposal} action definition, and retargets it to the
 * active editor when focus changes.
 * Think of this as the Rebel comms officer plugging in their content-assist
 * shortcut when they take the chair and unplugging it when they leave.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorContributor extends BasicTextEditorActionContributor
{

    /** The content-assist action ID from the constants. */
    private static final String CONTENTASSIST_ACTION = LdifEditorConstants.CONTENTASSIST_ACTION;

    /** The retarget action that delegates to the active editor's content assist. */
    private RetargetTextEditorAction contentAssist;


    // ── CONSTRUCT AND WIRE UP THE RETARGET ACTION ─────────────────────────────
    // The comms officer picks up their content-assist module and configures
    // the keyboard binding before sitting down at the console.
    /**
     * Creates the contributor and initialises the content-assist
     * {@link RetargetTextEditorAction} with its action definition ID.
     */
    public LdifEditorContributor()
    {
        super();

        contentAssist = new RetargetTextEditorAction( LdifEditorActivator.getDefault().getResourceBundle(),
            "ContentAssistProposal." ); //$NON-NLS-1$
        contentAssist.setActionDefinitionId( ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS );
    }


    // ── RETARGET TO THE ACTIVE EDITOR ─────────────────────────────────────────
    // When the officer sits down they plug the retarget action into the
    // incoming editor's actual content-assist handler.
    /**
     * {@inheritDoc}
     *
     * <p>Retargets the content-assist retarget action to the incoming editor's
     * concrete content-assist action handler.</p>
     */
    public void setActiveEditor( IEditorPart part )
    {
        super.setActiveEditor( part );
        ITextEditor editor = ( part instanceof ITextEditor ) ? ( ITextEditor ) part : null;
        contentAssist.setAction( getAction( editor, CONTENTASSIST_ACTION ) );
    }


    // ── REGISTER THE GLOBAL ACTION HANDLER ───────────────────────────────────
    // The officer registers the shortcut with the workbench action bar
    // so it works globally regardless of which toolbar has keyboard focus.
    /**
     * {@inheritDoc}
     *
     * <p>Registers the content-assist retarget action as a global action
     * handler on the workbench action bars.</p>
     */
    public void init( IActionBars bars, IWorkbenchPage page )
    {
        super.init( bars, page );
        bars.setGlobalActionHandler( CONTENTASSIST_ACTION, contentAssist );
    }


    // ── CLEAN UP ──────────────────────────────────────────────────────────────
    // The officer leaves the console and detaches their module.
    /**
     * {@inheritDoc}
     *
     * <p>Deactivates the active editor before delegating to the superclass.</p>
     */
    public void dispose()
    {
        setActiveEditor( null );
        super.dispose();
    }
}
