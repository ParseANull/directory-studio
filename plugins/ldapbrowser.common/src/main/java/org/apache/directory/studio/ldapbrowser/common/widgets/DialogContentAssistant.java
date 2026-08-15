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

package org.apache.directory.studio.ldapbrowser.common.widgets;


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


// ── CLASS: DialogContentAssistant — OBI-WAN SENSING A DISTURBANCE IN THE FORCE ─
// Obi-Wan meditates quietly until he feels a ripple — someone has started typing
// in a dialog field, and the Force (the keyboard handler) awakens around them.
// When the popup appears, Obi-Wan also blocks the ESC exit so the dialog can't flee.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides content-assist (autocomplete) pop-ups for SWT {@link Text}, {@link Combo},
 * and {@link ITextViewer} controls embedded inside dialogs.
 * Without this class, pressing ESC to dismiss the suggestion popup would also close the
 * whole dialog — that's a bad experience. We intercept that keystroke and block it while
 * the popup is visible, then clean up keyboard handlers when focus moves away.
 * Think of this class as Obi-Wan: always watching quietly, ready to deflect an ill-timed
 * keystroke and guide the user toward a valid LDAP value.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DialogContentAssistant extends SubjectControlContentAssistant implements FocusListener
{

    /** The control */
    private Control control;

    /** The handler activation. */
    private IHandlerActivation handlerActivation;

    /** The possible completions visible. */
    private boolean possibleCompletionsVisible;


    // ── OBI-WAN ENTERS MEDITATION ─────────────────────────────────────────────────
    // Obi-Wan sits cross-legged on the Falcon's cargo floor, eyes closed, sensing nothing yet.
    // The proposal popup is nowhere to be seen — the field is quiet and unfocused.
    // We start with possibleCompletionsVisible = false, ready to react when the disturbance comes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DialogContentAssistant in its idle state.
     * No pop-up is visible and no keyboard handlers are registered yet — everything
     * activates later when the user actually focuses on a text control.
     *
     * <p>For example — Obi-Wan prepares:</p>
     * <pre>
     *   "I felt a great disturbance in the Force... but not yet. Stand by."
     *   // possibleCompletionsVisible = false — popup is dark, handler is null
     * </pre>
     */
    public DialogContentAssistant()
    {
        this.possibleCompletionsVisible = false;
    }


    // ── OBI-WAN ATTACHES TO A TEXT FIELD ─────────────────────────────────────────
    // Obi-Wan places his hand on Luke's shoulder in the gun turret — "I'm watching your back."
    // He registers as a focus listener so he knows the moment Luke (the Text widget) engages.
    // We wire up content-assist on the SWT Text control so proposals flow when typing begins.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Installs content-assist support on the given SWT {@link Text} control.
     * We register as a focus listener so the keyboard shortcut handler (Ctrl+Space) is
     * activated/deactivated as the user tabs in and out of the field.
     *
     * <p>For example — Obi-Wan attaches to the text field:</p>
     * <pre>
     *   control = text;
     *   control.addFocusListener(this);  // "I'll sense when you're active."
     *   super.install(new TextContentAssistSubjectAdapter(text));
     * </pre>
     *
     * @param text  The SWT Text control to attach content-assist to; must not be {@code null}.
     */
    public void install( Text text )
    {
        control = text;
        control.addFocusListener( this );
        super.install( new TextContentAssistSubjectAdapter( text ) );
    }


    // ── OBI-WAN ATTACHES TO A COMBO ───────────────────────────────────────────────
    // Same watchful presence, different turret — this time Obi-Wan guards the Combo dropdown.
    // He hooks in as a focus listener so the Force flows in and out with focus.
    // We install content-assist on a Combo widget using a Combo-specific adapter.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Installs content-assist support on the given SWT {@link Combo} control.
     * Works the same way as {@link #install(Text)} but uses the appropriate JFace adapter
     * so proposals correctly replace the combo's editable text portion.
     *
     * <p>For example — Obi-Wan guards the dropdown:</p>
     * <pre>
     *   control = combo;
     *   control.addFocusListener(this);
     *   super.install(new ComboContentAssistSubjectAdapter(combo));
     * </pre>
     *
     * @param combo  The SWT Combo control to attach content-assist to; must not be {@code null}.
     */
    public void install( Combo combo )
    {
        control = combo;
        control.addFocusListener( this );
        super.install( new ComboContentAssistSubjectAdapter( combo ) );
    }


    // ── OBI-WAN GUARDS THE TEXT VIEWER AND BLOCKS THE ESCAPE ROUTE ───────────────
    // When the proposal popup is visible, any ESC keystroke would slam the whole dialog shut.
    // Obi-Wan steps into the doorway: "You cannot pass!" — he eats the ESC event.
    // We hook a TraverseListener that swallows traversal (ESC) while completions are visible.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Installs content-assist support on the given {@link ITextViewer} and adds special
     * ESC-blocking logic: while the proposal popup is open, ESC keystrokes are swallowed
     * here rather than propagating up to close the parent dialog.
     * This is the most feature-rich install variant, used for editors embedded in dialogs.
     *
     * <p>For example — Obi-Wan blocks the escape route:</p>
     * <pre>
     *   if (possibleCompletionsVisible) {
     *       e.doit = false;  // "The door is sealed. Choose your proposal."
     *   }
     * </pre>
     *
     * @param viewer  The text viewer to attach content-assist to; must not be {@code null}.
     */
    public void install( ITextViewer viewer )
    {
        control = viewer.getTextWidget();
        control.addFocusListener( this );

        // stop traversal (ESC) if popup is shown
        control.addTraverseListener( new TraverseListener()
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


    // ── OBI-WAN WITHDRAWS INTO THE FORCE ──────────────────────────────────────────
    // When the widget is going away, Obi-Wan releases his watch — like his final surrender
    // on the Death Star before Vader strikes him down. Clean, deliberate, no loose ends.
    // We deregister the keyboard handler and focus listener so nothing leaks.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Tears down all content-assist infrastructure attached to the control.
     * Deactivates the Ctrl+Space keyboard handler if it was still registered,
     * removes the focus listener, and delegates to the parent uninstall.
     * Call this when the dialog or widget is being disposed so we don't leave
     * dangling references in the Eclipse handler service.
     *
     * <p>For example — Obi-Wan steps aside:</p>
     * <pre>
     *   handlerService.deactivateHandler(handlerActivation);  // release the shortcut
     *   control.removeFocusListener(this);                    // stop listening
     *   super.uninstall();
     * </pre>
     */
    public void uninstall()
    {
        if ( handlerActivation != null )
        {
            IHandlerService handlerService = ( IHandlerService ) PlatformUI.getWorkbench().getAdapter(
                IHandlerService.class );
            handlerService.deactivateHandler( handlerActivation );
            handlerActivation = null;
        }

        if ( control != null )
        {
            control.removeFocusListener( this );
        }

        super.uninstall();
    }


    // ── OBI-WAN SENSES THE POPUP AWAKENING ───────────────────────────────────────
    // Just before the proposal popup's size is restored from preferences, Obi-Wan
    // feels it — the Force shifts, the popup is about to appear.
    // We set possibleCompletionsVisible = true before delegating so ESC-blocking kicks in.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the parent class just before the completion proposal popup is restored
     * from its saved size. We use this hook to flip {@code possibleCompletionsVisible}
     * to {@code true} before the popup appears, so ESC is blocked from that moment on.
     *
     * <p>For example — Obi-Wan feels the shift:</p>
     * <pre>
     *   "I sense the popup is coming." → possibleCompletionsVisible = true
     *   → super.restoreCompletionProposalPopupSize();
     * </pre>
     *
     * @return  The saved {@link Point} size of the popup, as returned by the parent class.
     */
    protected Point restoreCompletionProposalPopupSize()
    {
        possibleCompletionsVisible = true;
        return super.restoreCompletionProposalPopupSize();
    }


    // ── OBI-WAN RAISES THE SHIELD ─────────────────────────────────────────────────
    // Obi-Wan ignites his saber and holds position — the popup is now fully visible.
    // From this moment, any ESC attempt will be caught and the dialog stays put.
    // We mark the popup as visible and let the parent do the actual display work.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Triggers the proposal popup to appear and marks it as visible so our ESC-blocking
     * logic in the traverse listener takes effect. Overrides the parent to set the
     * {@code possibleCompletionsVisible} flag before the popup shows.
     *
     * <p>For example — Obi-Wan holds the line:</p>
     * <pre>
     *   possibleCompletionsVisible = true;  // "The door is locked. Pick a proposal."
     *   return super.showPossibleCompletions();
     * </pre>
     *
     * @return  A status message from the parent, or {@code null} if all went well.
     */
    public String showPossibleCompletions()
    {
        possibleCompletionsVisible = true;
        return super.showPossibleCompletions();
    }


    // ── OBI-WAN LOWERS HIS GUARD, THE DOOR REOPENS ───────────────────────────────
    // The user picked a proposal — or dismissed the popup on purpose. Obi-Wan relaxes.
    // The popup is gone, so ESC can now propagate normally if the user wants to cancel.
    // We flip possibleCompletionsVisible back to false and let the parent clean up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the parent class when the proposal popup has been closed.
     * We use this hook to reset {@code possibleCompletionsVisible} to {@code false}
     * so ESC keystrokes can propagate normally again (e.g., to close the parent dialog).
     *
     * <p>For example — Obi-Wan steps aside:</p>
     * <pre>
     *   possibleCompletionsVisible = false;  // "The popup is gone — ESC is free."
     *   super.possibleCompletionsClosed();
     * </pre>
     */
    protected void possibleCompletionsClosed()
    {
        possibleCompletionsVisible = false;
        super.possibleCompletionsClosed();
    }


    // ── OBI-WAN ACTIVATES AS THE FIELD RECEIVES FOCUS ────────────────────────────
    // Luke steps into the cockpit and Obi-Wan's voice comes through clearly: "Use the Force."
    // Obi-Wan registers the Ctrl+Space shortcut so Luke can call for proposals any time.
    // We activate the content-assist keyboard handler in the Eclipse handler service.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to focus gained on the attached control by registering the Ctrl+Space
     * (content-assist proposals) keyboard handler in Eclipse's handler service.
     * This lets users trigger the completion popup via the standard keybinding
     * while this specific field has focus.
     *
     * <p>For example — Obi-Wan's voice when the field gains focus:</p>
     * <pre>
     *   "The Force is with you. Press Ctrl+Space — I'll show you the possibilities."
     *   handlerService.activateHandler(CONTENT_ASSIST_PROPOSALS, handler);
     * </pre>
     *
     * @param e  The focus event from SWT; we don't use its details directly.
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
            handlerActivation = handlerService.activateHandler(
                ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler );
        }
    }


    // ── OBI-WAN GOES QUIET WHEN FOCUS LEAVES ─────────────────────────────────────
    // Luke leaves the cockpit, and Obi-Wan's voice fades — no point holding the shortcut open.
    // He deactivates cleanly so the Ctrl+Space handler doesn't fire in the wrong context.
    // We deregister the handler activation from Eclipse's handler service on focus loss.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to focus lost on the attached control by deregistering the Ctrl+Space
     * keyboard handler from Eclipse's handler service.
     * This prevents the content-assist shortcut from triggering when some other control
     * (or another field with its own assistant) has focus.
     *
     * <p>For example — Obi-Wan goes quiet:</p>
     * <pre>
     *   "The field is no longer focused. I'll step back."
     *   handlerService.deactivateHandler(handlerActivation);
     *   handlerActivation = null;
     * </pre>
     *
     * @param e  The focus event from SWT; we don't use its details directly.
     */
    public void focusLost( FocusEvent e )
    {
        if ( handlerActivation != null )
        {
            IHandlerService handlerService = ( IHandlerService ) PlatformUI.getWorkbench().getAdapter(
                IHandlerService.class );
            handlerService.deactivateHandler( handlerActivation );
            handlerActivation = null;
        }
    }

}
