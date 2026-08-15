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

package org.apache.directory.studio.schemaeditor.view.editors;


import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;


// ── CLASS: AbstractSchemaObjectEditorPage — TANTIVE IV BRIDGE, ALL STATIONS ──────────
// The Tantive IV's bridge is a flurry of coordinated activity: comms officers, helm
// operators, and weapons stations are all wired into the ship's event bus, snapping to
// action when something changes and going silent when the crisis passes.  Every concrete
// editor page is a different bridge crew — they each own different widgets — but the
// coordination protocol (wire up → fill UI → refresh → unwire → clean up) is the same
// for all of them, and that protocol lives right here.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all schema-object editor pages in the Schema Editor.
 * We extend Eclipse's FormPage here, which gives us a scrollable JFace Forms page
 * with titled sections and toolkit-styled widgets; on top of that we add the listener
 * lifecycle skeleton that every concrete subclass needs.
 * Think of this class as the Tantive IV bridge: each subclass (attribute type, object
 * class, matching rule ...) brings its own crew and consoles, but the "all hands to
 * stations / stand down" rhythm is inherited from here.
 */
public abstract class AbstractSchemaObjectEditorPage<E extends FormEditor> extends FormPage
{
    /** The flag to indicate if the page has been initialized */
    protected boolean initialized = false;


    // ── Antilles Commissions a New Bridge Station ────────────────────────────────────
    // Captain Antilles is standing on the Tantive IV bridge as a fresh crew member takes
    // their post.  He tells them which ship they are on, where their console sits, and
    // what their station is called.
    // This constructor does the same: it hands the new page its parent editor, a unique
    // ID string, and the title that will appear on the tab.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Wires a new editor page into its parent FormEditor.
     * We pass these three arguments straight up to FormPage so Eclipse can manage the
     * page's tab placement and lifecycle; nothing fancy happens here.
     *
     * <p>For example — Captain Antilles assigns a new officer their post:</p>
     * <pre>
     *   Antilles: "You're on the bridge of the Tantive IV.
     *              Your station ID is 'helm-3', and your title is 'Navigation Officer'."
     *   The officer takes their seat and waits for their first event.
     * </pre>
     *
     * @param editor  the FormEditor that owns this page — we need it to navigate back
     *                and to call editor-level operations like setDirty
     * @param id      a unique string that identifies this page within the editor; Eclipse
     *                uses it for page switching
     * @param title   the human-readable label shown on the tab at the bottom of the editor
     */
    public AbstractSchemaObjectEditorPage( E editor, String id, String title )
    {
        super( editor, id, title );
    }


    // ── Requesting the Ship's Current Captain ────────────────────────────────────────
    // A crew member on the Tantive IV wants to report to the commanding officer, so they
    // ask the bridge: "Who is in charge here?" The bridge returns a typed reference to
    // the captain, not just an anonymous person.
    // This method does the same: it fetches the parent editor with the correct concrete
    // type so callers don't have to cast it themselves.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent FormEditor cast to the concrete editor type E.
     * The base FormPage only knows the editor as a raw FormEditor, but our subclasses
     * need the specific type (e.g. AttributeTypeEditor) to call domain-specific methods.
     * The unchecked cast is safe because the constructor enforces that E matches.
     *
     * <p>For example — the crew member gets the right officer:</p>
     * <pre>
     *   // Instead of a generic "person" reference, you get back "Captain Antilles":
     *   AttributeTypeEditor editor = getEditor();
     *   editor.setDirty( true );  // only works with the real type
     * </pre>
     *
     * @return  the parent editor typed as E — ready to use without any casting on the
     *          caller's side
     */
    @SuppressWarnings("unchecked")
    public E getEditor()
    {
        return ( E ) super.getEditor();
    }


    // ── Bridge Goes Live for the First Time ──────────────────────────────────────────
    // The Tantive IV's bridge powers up and every console blinks to life.  The crew
    // confirms readiness, and the bridge is now "initialized" — from this point on,
    // incoming status updates will trigger meaningful responses.
    // Subclasses override this to actually build their UI, but they must call super
    // first so the initialized flag gets set.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the page as initialized when Eclipse calls us to build the form content.
     * We set {@code initialized = true} here so that {@link #refreshUI()} can safely
     * skip a refresh if the form was never constructed (which would cause NPEs on all
     * the widget fields).
     * Subclasses must call {@code super.createFormContent(managedForm)} and then build
     * their actual SWT layout on top.
     *
     * <p>For example — the bridge comes online:</p>
     * <pre>
     *   // Without this flag, refreshUI() would try to update widgets that don't exist yet.
     *   // With it, we're safe: "bridge is live, commence operations."
     * </pre>
     *
     * @param managedForm  the Eclipse-managed scrollable form container that we will
     *                     populate with sections and toolkit-styled widgets
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        initialized = true;
    }


    // ── All Hands to Stations ────────────────────────────────────────────────────────
    // The Tantive IV comes under attack and the captain shouts "All hands to stations!"
    // Every crew member snaps to their console and starts monitoring incoming signals.
    // Subclasses override this to attach their specific listeners to their widgets.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Hook for subclasses to attach all their event listeners to their UI widgets.
     * We call this as part of the refresh cycle: after filling fields with fresh data
     * we re-arm all listeners so the user's next edit is captured.
     * The default implementation does nothing — every meaningful page overrides this.
     *
     * <p>For example — the Tantive IV crew mans their stations:</p>
     * <pre>
     *   // Subclass does:
     *   addModifyListener( aliasesText, aliasesTextModifyListener );
     *   addSelectionChangedListener( supComboViewer, supComboViewerListener );
     *   // ... and so on for every widget that can fire events.
     * </pre>
     */
    protected void addListeners()
    {
    }


    // ── Stand Down from Battle Stations ─────────────────────────────────────────────
    // The attack is over and the captain calls "stand down."  Each crew member
    // disengages from their console so the ship can undergo a refit without stray
    // signals triggering alarms.
    // We do the same before re-populating the UI: we remove all listeners first so
    // that programmatic field updates don't fire "user edited me" events.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Hook for subclasses to detach all their event listeners from their UI widgets.
     * We must remove listeners before calling {@link #fillInUiFields()} during a refresh
     * because setting widget text/selections programmatically would otherwise trigger
     * the very listeners we just wired, causing dirty-flag spam or infinite loops.
     * The default implementation does nothing — every meaningful page overrides this.
     *
     * <p>For example — the crew stands down:</p>
     * <pre>
     *   // Subclass does:
     *   removeModifyListener( aliasesText, aliasesTextModifyListener );
     *   removeSelectionChangedListener( supComboViewer, supComboViewerListener );
     *   // Safe to rewrite widget content without triggering events now.
     * </pre>
     */
    protected void removeListeners()
    {
    }


    // ── Updating All Consoles from Fresh Telemetry ───────────────────────────────────
    // New sensor data arrives on the Tantive IV bridge and each console operator updates
    // their display from the ship's current state — not from memory of the last reading.
    // Subclasses override this to read the current model object and push its values into
    // every widget on the form.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Hook for subclasses to populate every UI widget from the current model state.
     * We call this both on initial form creation and during a refresh so the UI always
     * reflects the live schema object, not stale in-memory widget content.
     * The default implementation does nothing — every meaningful page overrides this.
     *
     * <p>For example — consoles update from fresh telemetry:</p>
     * <pre>
     *   // Subclass does:
     *   aliasesText.setText( ViewUtils.concateAliases( modifiedAT.getNames() ) );
     *   oidText.setText( modifiedAT.getOid() );
     *   // ... one widget per attribute property.
     * </pre>
     */
    protected void fillInUiFields()
    {
    }


    // ── Powering Down the Bridge at Mission's End ────────────────────────────────────
    // The Tantive IV's mission is over.  The captain orders all stations shut down in
    // the correct sequence: listeners first, then hardware, to prevent dangling signals
    // from crashing anything.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Tears down this editor page cleanly, removing all listeners before delegating
     * to FormPage's own dispose so Eclipse can reclaim SWT resources.
     * If we skipped removeListeners here, a listener holding a reference to a disposed
     * widget could fire and throw a SWTException — so we always clean up first.
     *
     * <p>For example — the bridge shuts down in the right order:</p>
     * <pre>
     *   // Wrong: super.dispose() first, then listeners try to fire on dead widgets.
     *   // Right: removeListeners() first, then super.dispose() — safe every time.
     * </pre>
     */
    public void dispose()
    {
        removeListeners();

        super.dispose();
    }


    // ── Emergency Refit Between Skirmishes ───────────────────────────────────────────
    // Between two battles the Tantive IV docks briefly for a refit.  The crew stands
    // down (listeners off), the engineering team swaps in fresh components (fillInUiFields),
    // and then everyone returns to stations (listeners back on) — all in one tight sequence.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes all UI widgets from the current model state in a single coordinated cycle.
     * We guard with the {@code initialized} flag to avoid NPEs if the form hasn't been
     * built yet (e.g. when a schema change event fires before the tab is first opened).
     * The three-step pattern — remove listeners, fill fields, add listeners — ensures
     * that programmatic field updates don't trigger user-edit events mid-refresh.
     *
     * <p>For example — the refit cycle runs:</p>
     * <pre>
     *   removeListeners();   // stand down — no spurious events
     *   fillInUiFields();    // swap in fresh data from the model
     *   addListeners();      // all hands back to stations
     * </pre>
     */
    public void refreshUI()
    {
        if ( initialized )
        {
            removeListeners();
            fillInUiFields();
            addListeners();
        }
    }


    // ── Comms Officer Takes the Incoming Signal ──────────────────────────────────────
    // The comms officer on the Tantive IV plugs in their headset only after confirming
    // the console exists and hasn't been blown out by a power surge.
    // We do the same: we only attach a listener to a Text widget if it's non-null and
    // hasn't been disposed by SWT.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches a ModifyListener to a Text widget.
     * We check that neither the widget nor the listener is null, and that the widget
     * hasn't been disposed, before calling addModifyListener — skipping a null or
     * disposed widget silently rather than throwing an NPE or SWTException.
     *
     * <p>For example — the officer only puts on a working headset:</p>
     * <pre>
     *   // Console is there and live → hook up the listener.
     *   // Console was destroyed in the firefight → skip it, no crash.
     * </pre>
     *
     * @param text      the Text SWT control to watch; if null or disposed we do nothing
     * @param listener  the ModifyListener to attach; if null we do nothing
     */
    protected void addModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.addModifyListener( listener );
        }
    }


    // ── StyledText Comms Officer Takes the Incoming Signal ───────────────────────────
    // Same scene as above: the comms officer confirms the styled-text console is live
    // before plugging in their headset.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches a ModifyListener to a StyledText widget (used for the source-code
     * page's schema viewer, which is a StyledText rather than a plain Text).
     * The same null/disposed guard applies as for the plain Text overload.
     *
     * <p>For example — the officer checks the styled console before connecting:</p>
     * <pre>
     *   // StyledText is the rich-text variant used in the source-code editor tab.
     *   // Guard keeps us from crashing if the tab was never opened.
     * </pre>
     *
     * @param text      the StyledText control to watch; if null or disposed we do nothing
     * @param listener  the ModifyListener to attach; if null we do nothing
     */
    protected void addModifyListener( StyledText text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.addModifyListener( listener );
        }
    }


    // ── Combo Comms Officer Takes the Incoming Signal ────────────────────────────────
    // Third crew member at the comms bank, this one watching the drop-down selector
    // console for changes — same safety check, different hardware.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches a ModifyListener to a Combo widget (the drop-down selectors on
     * the Overview page, e.g. for Usage or Syntax).
     * Same null/disposed guard as the other addModifyListener overloads.
     *
     * <p>For example — the third console operator connects to the selector bank:</p>
     * <pre>
     *   // addModifyListener( usageCombo, usageComboListener );
     *   // If usageCombo was never created (page not yet open), this is a no-op.
     * </pre>
     *
     * @param combo     the Combo SWT control to watch; if null or disposed we do nothing
     * @param listener  the ModifyListener to attach; if null we do nothing
     */
    protected void addModifyListener( Combo combo, ModifyListener listener )
    {
        if ( ( combo != null ) && ( !combo.isDisposed() ) && ( listener != null ) )
        {
            combo.addModifyListener( listener );
        }
    }


    // ── OID Validator Takes Their Post ───────────────────────────────────────────────
    // A specialist crew member whose only job is to intercept incoming characters before
    // they reach the OID display — they verify each character is a digit or dot before
    // letting it through.  Same safety check before hooking up.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches a VerifyListener to a Text widget.
     * VerifyListeners fire before the text actually changes, letting us reject individual
     * keystrokes that would make the content invalid (e.g. non-OID characters in the OID
     * field).  We check null/disposed before wiring, same as the ModifyListener overloads.
     *
     * <p>For example — the OID specialist intercepts bad characters:</p>
     * <pre>
     *   // addVerifyListener( oidText, oidTextVerifyListener );
     *   // The verifyListener rejects anything that doesn't match "([0-9]*\\.?)*".
     * </pre>
     *
     * @param text      the Text control whose input we want to pre-validate
     * @param listener  the VerifyListener to attach; if null or text is disposed, no-op
     */
    protected void addVerifyListener( Text text, VerifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.addVerifyListener( listener );
        }
    }


    // ── Navigation Officer Monitors the Ship's Sensor Array ─────────────────────────
    // The navigation officer watches the sensor viewer — whenever the view's selection
    // changes (someone picked a new destination), they get notified.
    // JFace Viewers are a higher-level abstraction over raw SWT tables/lists; we need to
    // check both the viewer and its underlying control before wiring.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches an ISelectionChangedListener to a JFace Viewer (e.g. a ComboViewer
     * or TableViewer).
     * We need to verify that the viewer, its underlying SWT control, and the listener
     * are all non-null and that the control hasn't been disposed — more checks than a
     * plain widget because Viewer is a JFace wrapper that can exist without a control.
     *
     * <p>For example — the officer monitors the navigation array:</p>
     * <pre>
     *   // addSelectionChangedListener( supComboViewer, supComboViewerListener );
     *   // When the user picks a different "superior type", the listener fires.
     * </pre>
     *
     * @param viewer    the JFace Viewer to monitor; if null or its control is disposed,
     *                  we do nothing
     * @param listener  the ISelectionChangedListener to attach; if null, we do nothing
     */
    protected void addSelectionChangedListener( Viewer viewer, ISelectionChangedListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.addSelectionChangedListener( listener );
        }
    }


    // ── Hyperlink Navigation Officer Goes on Watch ───────────────────────────────────
    // A Tantive IV crew member takes up position next to the hyperspace console — a
    // clickable link widget that, when activated, jumps to another editor (like a schema
    // link jumping to the Schema Editor).  Same null-check before wiring.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches an IHyperlinkListener to a JFace Forms Hyperlink widget.
     * Hyperlinks on the Overview page (e.g. the schema name link and the "Superior Type"
     * label) open other editors when clicked; the listener handles that navigation.
     * We guard against null/disposed before wiring.
     *
     * <p>For example — the hyperspace console gets a navigator:</p>
     * <pre>
     *   // addHyperlinkListener( schemaLink, schemaLinkListener );
     *   // Clicking "inetorgperson" schema link opens the schema editor for that schema.
     * </pre>
     *
     * @param hyperLink  the Forms Hyperlink widget to listen on; null/disposed → no-op
     * @param listener   the IHyperlinkListener to attach; null → no-op
     */
    protected void addHyperlinkListener( Hyperlink hyperLink, IHyperlinkListener listener )
    {
        if ( ( hyperLink != null ) && ( !hyperLink.isDisposed() ) && ( listener != null ) )
        {
            hyperLink.addHyperlinkListener( listener );
        }
    }


    // ── Button Operator Takes the Controls ───────────────────────────────────────────
    // Another crew member posts up at the button bank — "Edit Aliases," "Obsolete" check-
    // boxes, and so on.  They watch for widget-selected events and react accordingly.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches a SelectionListener to a Button widget (push buttons and checkboxes).
     * Buttons on the Overview page include "Edit Aliases" and the boolean property
     * checkboxes (single-value, obsolete, collective, no-user-modification).
     * We guard null/disposed before wiring.
     *
     * <p>For example — the button operator takes the controls:</p>
     * <pre>
     *   // addSelectionListener( aliasesButton, aliasesButtonListener );
     *   // Clicking "Edit Aliases" opens a dialog to manage the names list.
     * </pre>
     *
     * @param button    the Button SWT control to watch; null/disposed → no-op
     * @param listener  the SelectionListener to attach; null → no-op
     */
    protected void addSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.addSelectionListener( listener );
        }
    }


    // ── Table Crew Member Mans the Double-Click Console ─────────────────────────────
    // On the Tantive IV's bridge there's a tactical table showing a grid of contacts.
    // A crew member watches it for double-clicks so they can open a new view on a
    // selected contact.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely attaches a MouseListener to a Table widget.
     * Tables on the "Used By" page fire mouse events; we listen for double-clicks to
     * open the ObjectClass editor for the selected row.
     * We guard null/disposed before wiring.
     *
     * <p>For example — the tactical table crew member watches for double-clicks:</p>
     * <pre>
     *   // addMouseListener( mandatoryAttributeTable, mandatoryAttributeTableListener );
     *   // Double-clicking an object class row opens that class's editor.
     * </pre>
     *
     * @param table     the Table SWT control to watch; null/disposed → no-op
     * @param listener  the MouseListener to attach; null → no-op
     */
    protected void addMouseListener( Table table, MouseListener listener )
    {
        if ( ( table != null ) && ( !table.isDisposed() ) && ( listener != null ) )
        {
            table.addMouseListener( listener );
        }
    }


    // ── Comms Officer Unplugs the Text Headset ───────────────────────────────────────
    // The battle is over; the comms officer on the Text console unplugs their headset
    // before the refit begins so incoming signals don't cause spurious alerts.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches a ModifyListener from a Text widget.
     * Called during {@link #removeListeners()} before we repopulate the form so that
     * programmatic setText() calls don't trigger user-edit handlers.
     * We guard null/disposed before calling removeModifyListener.
     *
     * <p>For example — the comms officer unplugs before refit:</p>
     * <pre>
     *   removeModifyListener( aliasesText, aliasesTextModifyListener );
     *   // Now we can set aliasesText.setText("...") without firing the alias parser.
     * </pre>
     *
     * @param text      the Text control to detach from; null/disposed → no-op
     * @param listener  the ModifyListener to remove; null → no-op
     */
    protected void removeModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.removeModifyListener( listener );
        }
    }


    // ── StyledText Comms Officer Unplugs Their Headset ──────────────────────────────
    // Same scene, styled-text variant: the source-code console comms officer disconnects
    // before the engineering team repopulates the schema source viewer.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches a ModifyListener from a StyledText widget.
     * Mirror of the addModifyListener(StyledText, ...) overload; called during
     * removeListeners() for the source-code page's schema viewer.
     *
     * <p>For example — the styled-console officer disconnects before the refit:</p>
     * <pre>
     *   removeModifyListener( schemaSourceViewer.getTextWidget(), schemaSourceViewerListener );
     * </pre>
     *
     * @param text      the StyledText control to detach from; null/disposed → no-op
     * @param listener  the ModifyListener to remove; null → no-op
     */
    protected void removeModifyListener( StyledText text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.removeModifyListener( listener );
        }
    }


    // ── Combo Comms Officer Unplugs Their Headset ────────────────────────────────────
    // The drop-down selector console comms officer disconnects before the engineering
    // team repopulates the combo with fresh model data.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches a ModifyListener from a Combo widget.
     * Mirror of the addModifyListener(Combo, ...) overload; called during removeListeners()
     * for the Usage combo and similar drop-downs.
     *
     * <p>For example — the combo console officer disconnects before the refit:</p>
     * <pre>
     *   removeModifyListener( usageCombo, usageComboListener );
     *   // Now safe to call usageCombo.select(index) without spurious events.
     * </pre>
     *
     * @param combo     the Combo control to detach from; null/disposed → no-op
     * @param listener  the ModifyListener to remove; null → no-op
     */
    protected void removeModifyListener( Combo combo, ModifyListener listener )
    {
        if ( ( combo != null ) && ( !combo.isDisposed() ) && ( listener != null ) )
        {
            combo.removeModifyListener( listener );
        }
    }


    // ── OID Validator Stands Down ────────────────────────────────────────────────────
    // The OID-character-interceptor crew member stands down from the Text console before
    // we programmatically set the OID field — otherwise they'd try to "verify" our own
    // valid content and possibly interfere.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches a VerifyListener from a Text widget.
     * Mirror of addVerifyListener; called during removeListeners() to prevent the verify
     * callback from intercepting programmatic setText() calls during a UI refresh.
     *
     * <p>For example — the OID validator stands down:</p>
     * <pre>
     *   removeVerifyListener( oidText, oidTextVerifyListener );
     *   oidText.setText( modifiedAT.getOid() );  // no interception, no accidents
     * </pre>
     *
     * @param text      the Text control to detach from; null/disposed → no-op
     * @param listener  the VerifyListener to remove; null → no-op
     */
    protected void removeVerifyListener( Text text, VerifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.removeVerifyListener( listener );
        }
    }


    // ── Navigation Officer Logs Off the Sensor Array ─────────────────────────────────
    // The navigation officer signs off the sensor viewer before the system performs
    // a scheduled recalibration — otherwise their listener might fire on stale data
    // mid-update and write a corrupted course correction.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches an ISelectionChangedListener from a JFace Viewer.
     * Mirror of addSelectionChangedListener; called during removeListeners() to prevent
     * combo-selection events from firing while we programmatically reset the selection
     * during a UI refresh.
     *
     * <p>For example — the navigation officer logs off before recalibration:</p>
     * <pre>
     *   removeSelectionChangedListener( supComboViewer, supComboViewerListener );
     *   supComboViewer.setSelection( new StructuredSelection( supAT ), true );
     *   // No spurious "user changed the superior type" event fired.
     * </pre>
     *
     * @param viewer    the JFace Viewer to detach from; null or disposed control → no-op
     * @param listener  the ISelectionChangedListener to remove; null → no-op
     */
    protected void removeSelectionChangedListener( Viewer viewer, ISelectionChangedListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.removeSelectionChangedListener( listener );
        }
    }


    // ── Hyperlink Navigator Logs Off the Hyperspace Console ─────────────────────────
    // The hyperspace console navigator steps away before the engineering team updates
    // the displayed schema name — otherwise clicking the link mid-update could navigate
    // to a stale schema.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches an IHyperlinkListener from a JFace Forms Hyperlink widget.
     * Mirror of addHyperlinkListener; called during removeListeners() before refreshing
     * the UI so hyperlink callbacks don't fire on stale references.
     *
     * <p>For example — the navigator steps away from the hyperspace console:</p>
     * <pre>
     *   removeHyperlinkListener( schemaLink, schemaLinkListener );
     *   schemaLabel.setText( newSchemaName );  // safe — no click events active
     * </pre>
     *
     * @param hyperLink  the Hyperlink widget to detach from; null/disposed → no-op
     * @param listener   the IHyperlinkListener to remove; null → no-op
     */
    protected void removeHyperlinkListener( Hyperlink hyperLink, IHyperlinkListener listener )
    {
        if ( ( hyperLink != null ) && ( !hyperLink.isDisposed() ) && ( listener != null ) )
        {
            hyperLink.removeHyperlinkListener( listener );
        }
    }


    // ── Button Operator Steps Away from the Controls ─────────────────────────────────
    // The button-bank operator steps back from the controls so the engineering team can
    // reset checkbox states without triggering the "user toggled a flag" handler.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches a SelectionListener from a Button widget.
     * Mirror of addSelectionListener; called during removeListeners() before the UI
     * refresh so setSelection() calls on checkboxes don't fire the edit-capture callbacks.
     *
     * <p>For example — the operator steps away before the refit:</p>
     * <pre>
     *   removeSelectionListener( obsoleteCheckbox, obsoleteCheckboxListener );
     *   obsoleteCheckbox.setSelection( modifiedAT.isObsolete() );  // no event fired
     * </pre>
     *
     * @param button    the Button control to detach from; null/disposed → no-op
     * @param listener  the SelectionListener to remove; null → no-op
     */
    protected void removeSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.removeSelectionListener( listener );
        }
    }


    // ── Tactical Table Crew Member Logs Off ──────────────────────────────────────────
    // The tactical-table crew member steps back from the double-click console before
    // the table gets refreshed with new object-class data — no phantom navigation events
    // should fire while the content provider reloads.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Safely detaches a MouseListener from a Table widget.
     * Mirror of addMouseListener; called during removeListeners() before the table content
     * providers reload so no double-click navigation fires mid-refresh.
     *
     * <p>For example — the tactical crew member logs off before the reload:</p>
     * <pre>
     *   removeMouseListener( mandatoryAttributeTable, mandatoryAttributeTableListener );
     *   mandatoryAttributeTableViewer.setInput( modifiedAT );  // reload without events
     * </pre>
     *
     * @param table     the Table control to detach from; null/disposed → no-op
     * @param listener  the MouseListener to remove; null → no-op
     */
    protected void removeMouseListener( Table table, MouseListener listener )
    {
        if ( ( table != null ) && ( !table.isDisposed() ) && ( listener != null ) )
        {
            table.removeMouseListener( listener );
        }
    }
}
