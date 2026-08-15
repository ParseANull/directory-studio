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

package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.filtereditor.FilterAutoEditStrategy;
import org.apache.directory.studio.ldapbrowser.common.filtereditor.FilterAutoEditStrategy.AutoEditParameters;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;


// ── CLASS: FilterWidgetAutoEditStrategyAdapter — R2 Auto-Correcting His Access Codes ──
// In A New Hope, R2-D2 is typing an access code into the Death Star terminal fast.
// As he types an opening parenthesis, the terminal's assistant sub-system instantly
// auto-inserts the matching closing parenthesis. He typed one character; the system
// applied the correction and repositioned the cursor between the brackets automatically.
// That "type one char, get corrected result" loop is exactly what this adapter does for
// the LDAP filter combo: it intercepts each keystroke before and after it lands,
// runs FilterAutoEditStrategy to compute the corrected text, and patches the combo.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * Bridges {@link FilterAutoEditStrategy} — a JFace text editor feature — into an
 * SWT {@link Combo} field. JFace auto-edit strategies normally only work inside
 * full JFace text viewers; this adapter translates the combo's verify/modify event
 * pair into the same mechanism so the combo gets bracket auto-completion too.
 * Think of this class as R2-D2's terminal assistant sub-system that fixes your
 * keystroke before the Death Star's access-control system even sees it.
 *
 * <p>Usage: just construct it — the constructor wires the listeners automatically:</p>
 * <pre>
 *   new FilterWidgetAutoEditStrategyAdapter( filterCombo, parser );
 * </pre>
 * There is no public "start" or "stop" — the listeners live as long as the combo does.
 * Used internally by {@link FilterWidget}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterWidgetAutoEditStrategyAdapter
{

    /** The auto edit strategy. */
    private FilterAutoEditStrategy autoEditStrategy;

    /** The combo. */
    private Combo combo;

    /** The old texts. */
    private List<String> oldTexts;

    /** The verify events. */
    private List<VerifyEvent> verifyEvents;

    /** The in apply combo customization flag. */
    private boolean inApplyComboCustomization;


    // ── R2 Plugs in His Auto-Correction Sub-System ───────────────────────────────────
    // R2 connects his bracket-completion co-processor to the Death Star terminal.
    // He wires a "before" listener to snapshot the text before the keystroke lands,
    // and an "after" listener to compute and apply the corrected result.
    // We install a VerifyListener (captures the old text + keystroke) and a
    // ModifyListener (applies the auto-edit strategy after the combo updates itself).
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the adapter and immediately attaches it to the given combo.
     * After this constructor returns, every keystroke in the combo will go through
     * the auto-edit pipeline: snapshot the old text in the verify phase, compute
     * the corrected text in the modify phase, and patch the combo if the strategy
     * changed anything.
     *
     * <p>For example — R2 wires up his correction module:</p>
     * <pre>
     *   terminal.addBefore( e -> snapshot( currentText, keystroke ) );
     *   terminal.addAfter(  e -> applyCorrection( snapshot, keystroke ) );
     * </pre>
     *
     * @param combo   The SWT Combo field that the filter string is typed into.
     *                Must not be null.
     * @param parser  The LDAP filter parser used by {@link FilterAutoEditStrategy}
     *                to understand the current filter structure. Must not be null.
     */
    public FilterWidgetAutoEditStrategyAdapter( Combo combo, LdapFilterParser parser )
    {
        this.combo = combo;

        this.oldTexts = new ArrayList<String>();
        this.verifyEvents = new ArrayList<VerifyEvent>();
        this.inApplyComboCustomization = false;

        this.autoEditStrategy = new FilterAutoEditStrategy( parser );
        combo.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                prepareComboCustomization( e );
            }
        } );
        combo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                applyComboCustomization( e );
            }
        } );
    }


    // ── R2 Snapshots the Terminal Before the Keystroke Lands ─────────────────────────
    // R2's co-processor captures a photo of the screen just before the user's finger
    // hits the key — it needs to know what was there before the change so it can compute
    // the right corrected version once the change has been applied.
    // We stash the pre-edit text and the verify event for the apply phase to consume.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the combo's {@link VerifyListener} just before a keystroke modifies the
     * text. We record the current (pre-change) text and the verify event — which carries
     * the characters being inserted and the selection range being replaced — so that
     * {@link #applyComboCustomization} has all the information it needs.
     * We skip this step if we ourselves are in the middle of applying a correction,
     * to avoid an infinite re-entry loop.
     *
     * <p>For example — R2 takes a screenshot before the key lands:</p>
     * <pre>
     *   if ( not self-applying ) {
     *       screenshot.save( currentText );
     *       keystrokeLog.append( event );
     *   }
     * </pre>
     *
     * @param e  The SWT verify event carrying the inserted text, start offset,
     *           and end offset of the replaced range.
     */
    public void prepareComboCustomization( VerifyEvent e )
    {
        if ( !inApplyComboCustomization )
        {
            String oldText = combo.getText();
            //parser.parse( oldText );

            if ( !oldTexts.isEmpty() )
            {
                oldTexts.clear();
                verifyEvents.clear();
            }
            oldTexts.add( oldText );
            verifyEvents.add( e );
        }
    }


    // ── R2 Applies the Corrected Version After the Terminal Updates ───────────────────
    // The keystroke has landed — the combo text now reflects the raw edit. R2's
    // co-processor wakes up, replays the correction algorithm on the before/after pair,
    // computes the final corrected string and cursor position, and patches the combo.
    // We guard with inApplyComboCustomization so our own setText() call doesn't
    // trigger another correction cycle.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the combo's {@link ModifyListener} immediately after a keystroke
     * has been applied. If we have a snapshot from {@link #prepareComboCustomization},
     * we run the auto-edit strategy to compute the corrected text and cursor position,
     * then push both back into the combo — effectively replacing the raw edit with
     * the strategy's version (e.g. adding a closing parenthesis automatically).
     *
     * <p>For example — R2 applies his correction and repositions the cursor:</p>
     * <pre>
     *   corrected = autoEdit.fixBrackets( beforeSnapshot, rawKeystroke );
     *   terminal.setText( corrected.text );
     *   terminal.setCursor( corrected.caretPosition );
     * </pre>
     *
     * @param e  The SWT modify event. We don't actually use the event's data directly;
     *           we rely on the state captured in {@link #prepareComboCustomization}.
     */
    public void applyComboCustomization( ModifyEvent e )
    {
        if ( !inApplyComboCustomization && !verifyEvents.isEmpty() )
        {
            String oldText = oldTexts.remove( 0 );
            VerifyEvent verifyEvent = verifyEvents.remove( 0 );
            inApplyComboCustomization = true;

            // extract modification details
            String text = verifyEvent.text;
            int offset = verifyEvent.start <= verifyEvent.end ? verifyEvent.start : verifyEvent.end;
            int length = verifyEvent.start <= verifyEvent.end ? verifyEvent.end - verifyEvent.start : verifyEvent.start
                - verifyEvent.end;

            // apply auto edit strategy
            AutoEditParameters autoEditParameters = new AutoEditParameters( text, offset, length, -1, true );
            autoEditStrategy.customizeAutoEditParameters( oldText, autoEditParameters );

            // get current selection
            Point oldSelection = combo.getSelection();

            // compose new text
            String newText = ""; //$NON-NLS-1$
            newText += oldText.substring( 0, autoEditParameters.offset );
            newText += autoEditParameters.text;
            newText += oldText.substring( autoEditParameters.offset + autoEditParameters.length, oldText.length() );

            // determine new cursor position
            Point newSelection;
            if ( autoEditParameters.caretOffset != -1 )
            {
                int x = autoEditParameters.caretOffset;
                newSelection = new Point( x, x );
            }
            else
            {
                newSelection = new Point( oldSelection.x, oldSelection.y );
            }

            // set new text and cursor position
            if ( verifyEvents.isEmpty() )
            {
                combo.setText( newText );
                combo.setSelection( newSelection );
            }

            inApplyComboCustomization = false;
        }

    }
}
