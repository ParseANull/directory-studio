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


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.contentassist.ComboContentAssistSubjectAdapter;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.contentassist.TextContentAssistSubjectAdapter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;


// ── CLASS: DialogContentAssistant — R2-D2 PLUGGING INTO ANY PANEL ─────────────
// R2-D2 can plug his data cable into different types of socket: a standard
// text-entry panel, a drop-down combo, or a full SourceViewer terminal.
// He also registers a keyboard shortcut so the user can summon him with Ctrl+Space,
// and he disconnects cleanly when the panel loses focus.
// DialogContentAssistant is that adaptable plug: it wraps a
// SubjectControlContentAssistant so it can attach to Text, Combo, or ITextViewer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link SubjectControlContentAssistant} that can be installed on
 * {@link Text} widgets, {@link Combo} widgets, or a full {@link ITextViewer}.
 * Manages a Ctrl+Space keyboard handler binding that is activated when the
 * target control gains focus and deactivated when it loses focus.
 * Think of this class as R2-D2 with a universal data cable: he plugs into
 * whatever panel is active, registers his Ctrl+Space shortcut, and disconnects
 * politely when the panel goes dark.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DialogContentAssistant extends SubjectControlContentAssistant implements FocusListener
{
    private Control control;

    private IHandlerActivation handlerActivation;

    private boolean possibleCompletionsVisible;


    // ── INITIALISE R2-D2'S DATA CABLE ────────────────────────────────────────
    // R2-D2 powers up with his completion popup flag cleared — no proposals
    // are showing yet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code DialogContentAssistant} with the proposal popup
     * initially hidden.
     */
    public DialogContentAssistant()
    {
        super();
        this.possibleCompletionsVisible = false;
    }


    // ── PLUG INTO A TEXT WIDGET ───────────────────────────────────────────────
    // R2-D2 inserts his data cable into a standard Text panel, registers the
    // focus listener, and wraps the widget in a TextContentAssistSubjectAdapter
    // so the superclass can talk to it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Installs content-assist support on the given {@link Text} widget.
     * Adds this instance as a focus listener and wraps {@code text} in a
     * {@link TextContentAssistSubjectAdapter} before delegating to the superclass.
     *
     * <p>For example — attaching to a DN text field in a dialog:</p>
     * <pre>
     *   DialogContentAssistant assistant = new DialogContentAssistant();
     *   assistant.install(dnTextField);
     * </pre>
     *
     * @param text  the text widget that will host the content-assist popup
     */
    public void install( Text text )
    {
        this.control = text;
        this.control.addFocusListener( this );
        super.install( new TextContentAssistSubjectAdapter( text ) );
    }


    // ── PLUG INTO A COMBO WIDGET ──────────────────────────────────────────────
    // R2-D2 inserts his data cable into a Combo drop-down panel — same pattern
    // as Text but with a ComboContentAssistSubjectAdapter.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Installs content-assist support on the given {@link Combo} widget.
     * Adds this instance as a focus listener and wraps {@code combo} in a
     * {@link ComboContentAssistSubjectAdapter}.
     *
     * <p>For example — attaching to an attribute-type combo:</p>
     * <pre>
     *   DialogContentAssistant assistant = new DialogContentAssistant();
     *   assistant.install(attributeTypeCombo);
     * </pre>
     *
     * @param combo  the combo widget that will host the content-assist popup
     */
    public void install( Combo combo )
    {
        this.control = combo;
        this.control.addFocusListener( this );
        super.install( new ComboContentAssistSubjectAdapter( combo ) );
    }


    // ── PLUG INTO A FULL TEXT VIEWER ──────────────────────────────────────────
    // R2-D2 connects to the full SourceViewer terminal. He also installs a
    // TraverseListener so ESC dismisses the popup without closing the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void install( ITextViewer viewer )
    {
        this.control = viewer.getTextWidget();
        this.control.addFocusListener( this );

        // stop traversal (ESC) if popup is shown
        this.control.addTraverseListener( new TraverseListener()
        {
            public void keyTraversed( TraverseEvent e )
            {
                if ( possibleCompletionsVisible )
                {
                    e.doit = false;
                }
            }
        } );

        super.install( viewer );
    }


    // ── DISCONNECT CLEANLY ────────────────────────────────────────────────────
    // When the panel shuts down, R2-D2 deactivates the Ctrl+Space handler,
    // removes the focus listener, and delegates the rest to the superclass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void uninstall()
    {
        if ( this.handlerActivation != null )
        {
            IHandlerService handlerService = ( IHandlerService ) PlatformUI.getWorkbench().getAdapter(
                IHandlerService.class );
            handlerService.deactivateHandler( this.handlerActivation );
            this.handlerActivation = null;
        }

        if ( this.control != null )
        {
            this.control.removeFocusListener( this );
        }

        super.uninstall();
    }


    // ── TRACK POPUP VISIBILITY (RESTORE SIZE) ────────────────────────────────
    // When the proposal popup re-appears, R2-D2 marks it as visible so the
    // TraverseListener knows to intercept ESC.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Point restoreCompletionProposalPopupSize()
    {
        possibleCompletionsVisible = true;
        return super.restoreCompletionProposalPopupSize();
    }


    // ── SHOW THE PROPOSALS POPUP ──────────────────────────────────────────────
    // R2-D2 beeps and shows his list of suggestions; we mark the popup as
    // visible before delegating to the superclass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String showPossibleCompletions()
    {
        possibleCompletionsVisible = true;
        return super.showPossibleCompletions();
    }


    // ── TRACK POPUP CLOSURE ───────────────────────────────────────────────────
    // When the proposal list closes, R2-D2 clears the visibility flag so ESC
    // resumes normal traversal.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void possibleCompletionsClosed()
    {
        this.possibleCompletionsVisible = false;
        super.possibleCompletionsClosed();
    }


    // ── REGISTER THE CTRL+SPACE SHORTCUT ON FOCUS GAIN ───────────────────────
    // R2-D2 activates his Ctrl+Space handler the moment the panel gains focus
    // so the user can invoke proposals at any time while editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void focusGained( FocusEvent e )
    {
        IHandlerService handlerService = ( IHandlerService ) PlatformUI.getWorkbench().getAdapter(
            IHandlerService.class );
        if ( handlerService != null )
        {
            IHandler handler = new org.eclipse.core.commands.AbstractHandler()
            {
                public Object execute( ExecutionEvent event ) throws org.eclipse.core.commands.ExecutionException
                {
                    showPossibleCompletions();
                    return null;
                }
            };
            this.handlerActivation = handlerService.activateHandler(
                ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler );
        }
    }


    // ── DEREGISTER THE SHORTCUT ON FOCUS LOSS ────────────────────────────────
    // When the panel goes dark, R2-D2 deactivates his Ctrl+Space handler so it
    // does not fire for other panels that gain focus.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void focusLost( FocusEvent e )
    {
        if ( this.handlerActivation != null )
        {
            IHandlerService handlerService = ( IHandlerService ) PlatformUI.getWorkbench().getAdapter(
                IHandlerService.class );
            handlerService.deactivateHandler( this.handlerActivation );
            this.handlerActivation = null;
        }
    }
}
