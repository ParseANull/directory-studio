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
package org.apache.directory.studio.openldap.config.acl.sourceeditor;


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


// ── CLASS: DialogContentAssistant — C-3PO OFFERING PHRASE COMPLETIONS AT THE TERMINAL
// C-3PO stands at Cassian's terminal, ready to suggest completions the moment
// the officer starts typing. When the control gains focus C-3PO registers the
// Ctrl+Space handler so the officer can invoke completions at any time. When the
// control loses focus C-3PO deregisters the handler so other parts of the UI
// can claim the keyboard shortcut. He also tracks whether the completion popup
// is currently showing — if it is, he swallows the ESC key so it closes the
// popup instead of dismissing the dialog.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link SubjectControlContentAssistant} that registers and deregisters the
 * Ctrl+Space content-assist keyboard handler when the associated control gains
 * or loses focus. Can be installed on {@link Text} widgets, {@link Combo}
 * widgets, or an {@link ITextViewer}. Also swallows ESC key traversal while
 * the completion popup is visible to prevent accidentally closing the dialog.
 *
 * <p>Think of this class as C-3PO's completion service: he activates it when
 * you focus the ACL text field and steps back when you tab away.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DialogContentAssistant extends SubjectControlContentAssistant implements FocusListener
{
    private Control control;

    private IHandlerActivation handlerActivation;

    private boolean possibleCompletionsVisible;


    // ── Constructing the Content Assistant ────────────────────────────────────
    // C-3PO powers up his phrase database and sets the popup-visible flag to false
    // because no completion popup is open yet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DialogContentAssistant with the completion popup initially
     * hidden and no handler registered.
     */
    public DialogContentAssistant()
    {
        super();
        this.possibleCompletionsVisible = false;
    }


    // ── Installing on a Text Widget ────────────────────────────────────────────
    // C-3PO plugs into a Text widget: he stores the reference, adds himself as
    // a focus listener, and delegates to the super-class using a TextContentAssistSubjectAdapter.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Installs content assist support on the given {@link Text} widget. Registers
     * this instance as a focus listener so the Ctrl+Space handler is activated
     * only when the text field has focus.
     *
     * @param text  The text widget to install content assist on.
     */
    public void install( Text text )
    {
        this.control = text;
        this.control.addFocusListener( this );
        super.install( new TextContentAssistSubjectAdapter( text ) );
    }


    // ── Installing on a Combo Widget ──────────────────────────────────────────
    // C-3PO plugs into a Combo widget in the same way as a Text widget, but
    // uses a ComboContentAssistSubjectAdapter to wrap it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Installs content assist support on the given {@link Combo} widget. Registers
     * this instance as a focus listener and wraps the combo in a
     * {@link ComboContentAssistSubjectAdapter}.
     *
     * @param combo  The combo widget to install content assist on.
     */
    public void install( Combo combo )
    {
        this.control = combo;
        this.control.addFocusListener( this );
        super.install( new ComboContentAssistSubjectAdapter( combo ) );
    }


    // ── Installing on a Source Viewer ─────────────────────────────────────────
    // C-3PO plugs into a source viewer's underlying text widget and also adds a
    // traverse listener that swallows ESC while the completion popup is open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Installs content assist support on the given {@link ITextViewer}. In addition
     * to focus handling, adds a {@link TraverseListener} that intercepts ESC key
     * traversal while the completion popup is visible — preventing ESC from closing
     * the parent dialog while the user is navigating completion proposals.
     *
     * {@inheritDoc}
     *
     * @param viewer  The text viewer to install content assist on.
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


    // ── Uninstalling and Cleaning Up ──────────────────────────────────────────
    // C-3PO powers down: deregisters the keyboard handler, removes the focus
    // listener, and calls the super-class uninstall.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Uninstalls content assist support. Deregisters the Ctrl+Space handler
     * activation (if still active), removes the focus listener, then delegates
     * to the super-class.
     *
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


    // ── Tracking the Popup Opening (for ESC suppression) ─────────────────────
    // C-3PO notes that the completion popup is now visible so the traverse
    // listener knows to swallow ESC key events.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code possibleCompletionsVisible} flag to {@code true} then
     * delegates to the super-class to restore the popup size.
     *
     * {@inheritDoc}
     *
     * @return  The restored popup size.
     */
    protected Point restoreCompletionProposalPopupSize()
    {
        possibleCompletionsVisible = true;
        return super.restoreCompletionProposalPopupSize();
    }


    // ── Showing the Completion Popup ──────────────────────────────────────────
    // C-3PO opens the phrase suggestion popup and notes that it is now visible.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Shows the possible completions popup and marks it as visible so ESC
     * traversal is swallowed while it remains open.
     *
     * {@inheritDoc}
     *
     * @return  An error message or {@code null} on success.
     */
    public String showPossibleCompletions()
    {
        possibleCompletionsVisible = true;
        return super.showPossibleCompletions();
    }


    // ── Tracking the Popup Closing ─────────────────────────────────────────────
    // C-3PO notes that the popup has closed so ESC traversal is no longer
    // swallowed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clears the {@code possibleCompletionsVisible} flag then delegates to the
     * super-class. Called automatically by the framework when the popup is dismissed.
     *
     * {@inheritDoc}
     */
    protected void possibleCompletionsClosed()
    {
        this.possibleCompletionsVisible = false;
        super.possibleCompletionsClosed();
    }


    // ── Registering the Ctrl+Space Handler on Focus Gained ────────────────────
    // When the officer clicks into the ACL text field, C-3PO registers a handler
    // for Ctrl+Space so the officer can invoke completions from the keyboard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the control gains focus. Registers a keyboard handler for the
     * {@link ITextEditorActionDefinitionIds#CONTENT_ASSIST_PROPOSALS} command
     * (Ctrl+Space) so the user can trigger completions from the keyboard.
     *
     * {@inheritDoc}
     *
     * @param e  The focus event.
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


    // ── Deregistering the Ctrl+Space Handler on Focus Lost ────────────────────
    // When the officer tabs away from the field, C-3PO deregisters the handler
    // so other widgets can reclaim Ctrl+Space.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the control loses focus. Deregisters the Ctrl+Space keyboard
     * handler that was activated in {@link #focusGained(FocusEvent)}.
     *
     * {@inheritDoc}
     *
     * @param e  The focus event.
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
