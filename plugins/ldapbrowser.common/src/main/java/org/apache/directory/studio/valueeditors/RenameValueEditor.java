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

package org.apache.directory.studio.valueeditors;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.ldapbrowser.common.dialogs.RenameEntryDialog;
import org.apache.directory.studio.ldapbrowser.common.dialogs.SimulateRenameDialogImpl;
import org.apache.directory.studio.ldapbrowser.core.jobs.RenameEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// ── CLASS: RenameValueEditor — C-3PO UPDATES THE DIPLOMATIC REGISTER ─────────
// In Cloud City, Lando's deal with Vader is constantly changing. C-3PO rushes
// to update the official diplomatic register every time a name, title, or entry
// changes — cross-referencing the old record, confirming the new designation,
// then filing the revised entry. That's exactly what this editor does: when a
// user clicks on an RDN attribute (the part of the DN that names the entry), we
// pop open a rename dialog, collect the new RDN, and fire off a background job
// to execute the LDAP modrdn operation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A special value editor that handles renaming an LDAP entry via its RDN (Relative
 * Distinguished Name). Rather than letting the user type a new value inline, this
 * editor pops open the {@link RenameEntryDialog}, which knows how to construct a
 * valid new RDN and then run the modrdn operation as a background job.
 * Think of this class as C-3PO frantically updating the Cloud City diplomatic
 * register every time Lando's deal with Vader changes — it doesn't edit values
 * in-place, it opens a proper form and fires a background rename operation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameValueEditor extends CellEditor implements IValueEditor
{
    /** The value to handle */
    private Object value;

    /** The parent composite, used to instantiate a new control */
    private Composite parent;

    /** The name of this value editor */
    private String name;

    /** The image of this value editor */
    private ImageDescriptor imageDescriptor;

    /** The value editor manager, used to get proper value editors */
    protected ValueEditorManager valueEditorManager;


    // ── C-3PO IS BRIEFED AND GIVEN HIS TOOLS ────────────────────────────────────
    // Lando hands C-3PO the official Cloud City register and introduces him to the
    // central records office that coordinates all the different clerks.
    // We save those references so every subsequent operation has what it needs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of RenameValueEditor, wiring it to the parent SWT
     * composite and the shared manager that knows about all available editors.
     * We need the parent composite because SWT requires a parent for any widget
     * we might eventually create (even though this dialog editor doesn't create one).
     *
     * <p>For example — C-3PO reports to the records office:</p>
     * <pre>
     *   C-3PO: "I am C-3PO, human–cyborg relations. You must be the records master."
     *   Lando: "Here's the register and the coordination droid. Keep them close."
     *   C-3PO: "Oh, I see. I shall maintain both references diligently."
     * </pre>
     *
     * @param parent              the parent composite that owns any SWT controls we create
     * @param valueEditorManager  the shared manager we ask when we need to delegate display
     *                            formatting to the right specialist editor
     */
    public RenameValueEditor( Composite parent, ValueEditorManager valueEditorManager )
    {
        super( parent );
        this.parent = parent;
        this.valueEditorManager = valueEditorManager;
    }


    // ── C-3PO PREPARES HIS DESK — BUT THERE IS NOTHING TO DRAW ─────────────────
    // C-3PO opens the register on his desk, ready to write, but quickly realises
    // that the rename process is entirely handled by the dialog that appears later.
    // There is no inline widget to embed in the cell — we return null on purpose.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * This is a dialog-based editor, so we don't embed any SWT control directly
     * into the table cell. Returning {@code null} here tells the JFace cell
     * editor framework that there is no inline widget to render.
     *
     * <p>For example — C-3PO prepares for the rename dialog:</p>
     * <pre>
     *   C-3PO: "Shall I prepare an ink quill for the table, Master Lando?"
     *   Lando: "No, 3PO — the rename dialog handles all that. Just stand by."
     *   C-3PO: "No inline control necessary. Understood."
     * </pre>
     *
     * @param parent  the parent composite (ignored — we create no control here)
     * @return        always {@code null} because this editor uses a dialog instead
     */
    protected Control createControl( Composite parent )
    {
        return null;
    }


    // ── C-3PO READS THE CURRENT REGISTER ENTRY ──────────────────────────────────
    // C-3PO glances at the page he's been holding ever since doSetValue was called,
    // and reads back whatever was recorded there — an IEntry, typically.
    // This is how the CellEditor framework retrieves the current value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whatever object was last stored via {@link #doSetValue(Object)}.
     * JFace calls this when it wants to know the editor's current value — for us
     * that is the {@link IEntry} we're about to rename.
     *
     * <p>For example — C-3PO consults his notes:</p>
     * <pre>
     *   C-3PO: "The current register entry reads: 'cn=Han Solo, ou=rebels, dc=galaxy'."
     *   C-3PO: "That is precisely what was recorded when the cell was activated."
     * </pre>
     *
     * @return the value object stored in the member field (usually an {@link IEntry})
     */
    protected final Object doGetValue()
    {
        return value;
    }


    // ── C-3PO STANDS BY — FOCUS IS NOT APPLICABLE HERE ──────────────────────────
    // When the register is a modal dialog, there is no cursor to position in a text
    // field. C-3PO straightens up and waits; the dialog will manage its own focus.
    // This no-op satisfies the abstract CellEditor contract without side effects.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op: since this editor delegates to a dialog, there is no inline widget
     * to focus. The dialog manages its own keyboard focus when it opens.
     *
     * <p>For example — C-3PO holds his position:</p>
     * <pre>
     *   C-3PO: "Where shall I place the cursor, Master Lando?"
     *   Lando: "Nowhere, 3PO — the dialog will handle that when it opens."
     *   C-3PO: "Very well. Standing by."
     * </pre>
     */
    protected void doSetFocus()
    {
    }


    // ── C-3PO RECORDS THE ENTRY IN HIS REGISTER ─────────────────────────────────
    // Lando hands C-3PO the Cloud City entry card (an IEntry object) and tells him
    // to hold on to it. C-3PO tucks it safely into his memory banks.
    // When activate() fires later, this is the entry we'll rename.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the given value so we can use it later in {@link #activate()}. The
     * JFace cell editor framework calls this before it activates the editor, passing
     * in whatever model object the table cell represents — for us, an {@link IEntry}.
     *
     * <p>For example — C-3PO logs the incoming entry:</p>
     * <pre>
     *   Lando: "3PO, here is the entry we need to rename: cn=Lobot."
     *   C-3PO: "Recording 'cn=Lobot' to memory. Ready for activation, sir."
     * </pre>
     *
     * @param value  the value to store — expected to be an {@link IEntry} by the time
     *               {@link #activate()} is called
     */
    protected void doSetValue( Object value )
    {
        this.value = value;
    }


    // ── C-3PO OPENS THE RENAME FORM AND SUBMITS THE REVISED ENTRY ───────────────
    // The diplomat arrives, the entry is on the desk, and C-3PO opens the official
    // rename form (RenameEntryDialog). When the clerk approves the new name, C-3PO
    // dispatches a runner (StudioBrowserJob) to update the permanent record in the
    // directory. He then closes the ink pot — fireCancelEditor() — because there is
    // nothing more for the cell to do.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * The main event: opens the {@link RenameEntryDialog} and, if the user confirms
     * a new RDN, fires a {@link StudioBrowserJob} to execute the LDAP modrdn
     * operation as a background task. We always call {@code fireCancelEditor()}
     * afterwards because the rename happens asynchronously — there is no modified
     * cell value to commit back through JFace.
     *
     * <p>For example — C-3PO processes the rename request:</p>
     * <pre>
     *   C-3PO: "I've opened the official rename form for 'cn=Lobot'."
     *   Lando: [types new name] "Change it to 'cn=Lobot, Chief of Operations'."
     *   C-3PO: "Confirmed. Dispatching the update runner to the LDAP server now."
     *   C-3PO: "Ink pot closed. The cell editor is dismissed."
     * </pre>
     */
    public void activate()
    {
        if ( getValue() instanceof IEntry )
        {
            IEntry entry = ( IEntry ) getValue();

            RenameEntryDialog renameDialog = new RenameEntryDialog( parent.getShell(), entry );

            if ( renameDialog.open() == Dialog.OK )
            {
                Rdn newRdn = renameDialog.getRdn();

                if ( ( newRdn != null ) && !newRdn.equals( entry.getRdn() ) )
                {
                    IEntry originalEntry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );
                    new StudioBrowserJob( new RenameEntryRunnable( originalEntry, newRdn,
                        new SimulateRenameDialogImpl( parent.getShell() ) ) ).execute();
                }
            }
        }

        fireCancelEditor();
    }


    // ── C-3PO IDENTIFIES HIMSELF AS THE RESPONSIBLE DROID ───────────────────────
    // When asked "who is the cell editor for this cell?", C-3PO points to himself.
    // The IValueEditor interface separates the display concerns from the CellEditor
    // mechanics; this bridge method hands the JFace table the actual CellEditor it
    // needs to activate the editor session.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code this} as the {@link CellEditor} to use for this value.
     * We implement both {@link IValueEditor} and {@link CellEditor} in one class,
     * so the answer is always ourselves — no wrapping needed.
     *
     * <p>For example — C-3PO steps forward:</p>
     * <pre>
     *   Lando: "Which droid manages the rename cell?"
     *   C-3PO: "That would be me, sir. I am both the editor and the cell editor."
     * </pre>
     *
     * @return this instance, cast as a {@link CellEditor}
     */
    public CellEditor getCellEditor()
    {
        return this;
    }


    // ── C-3PO READS ALL VALUES FROM THE REGISTER HIERARCHY ──────────────────────
    // C-3PO has a stack of diplomatic cards (an AttributeHierarchy). He reads each
    // one aloud, delegating the exact pronunciation of each value to the specialist
    // clerk who knows that particular attribute's format.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Produces a human-readable display string for all values across an entire
     * attribute hierarchy. We iterate every attribute and every value, ask the
     * appropriate specialist editor how to display each one, and join them
     * with commas. If there are multiple values, we prefix the count.
     *
     * <p>For example — C-3PO reads the full hierarchy:</p>
     * <pre>
     *   C-3PO: "This entry has 3 values: 'Han Solo', 'Smuggler', 'Millennium Falcon pilot'."
     *   C-3PO: "I delegated the exact rendering of each to the relevant specialist clerk."
     * </pre>
     *
     * @param attributeHierarchy  the set of attributes and values to display
     * @return                    a comma-separated string of display values, or a count prefix
     *                            when there are multiple values
     */
    public String getDisplayValue( AttributeHierarchy attributeHierarchy )
    {
        List<IValue> valueList = new ArrayList<IValue>();

        for ( IAttribute attribute : attributeHierarchy )
        {
            valueList.addAll( Arrays.asList( attribute.getValues() ) );
        }

        StringBuffer sb = new StringBuffer();

        if ( valueList.size() > 1 )
        {
            sb.append( NLS.bind( Messages.getString( "EntryValueEditor.n_values" ), valueList.size() ) ); //$NON-NLS-1$
        }

        boolean isFirst = true;

        for ( IValue value : valueList )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            IValueEditor vp = getValueEditor( value );
            sb.append( vp.getDisplayValue( value ) );
        }

        return sb.toString();
    }


    // ── C-3PO READS A SINGLE VALUE FROM THE REGISTER ────────────────────────────
    // One diplomatic card, one reading. C-3PO passes the card to the appropriate
    // specialist clerk and relays whatever that clerk announces back to the audience.
    // We don't format it ourselves — we trust the right editor to know the format.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Produces a human-readable display string for a single {@link IValue} by
     * delegating to whatever specialist editor is most appropriate for that value's
     * attribute type. We do not format it here directly.
     *
     * <p>For example — C-3PO delegates the reading:</p>
     * <pre>
     *   C-3PO: "The value card reads 'jpegPhoto'. I shall pass this to the image clerk."
     *   Image clerk: [returns "Binary data, 4096 bytes"]
     *   C-3PO: "Very good. I shall relay that to the table cell."
     * </pre>
     *
     * @param value  the single LDAP value to render as a display string
     * @return       the display string produced by the appropriate specialist editor
     */
    public String getDisplayValue( IValue value )
    {
        IValueEditor vp = getValueEditor( value );

        return vp.getDisplayValue( value );
    }


    // ── C-3PO FINDS THE RIGHT CLERK WITHOUT LOOPING BACK TO HIMSELF ─────────────
    // C-3PO checks the records office (ValueEditorManager) for the best clerk, but
    // if the office sends him back to himself — infinite loop! — he resets the
    // user's preference momentarily, picks a different clerk, then restores the
    // preference. This avoids a stack overflow when the user has explicitly selected
    // RenameValueEditor as their preferred editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the most appropriate {@link IValueEditor} for a given value,
     * specifically guarding against the case where the manager would hand us back
     * a {@code RenameValueEditor} (causing infinite recursion). When that loop is
     * detected, we temporarily clear the user-selected editor, pick the fallback,
     * then restore the user's preference.
     *
     * <p>For example — C-3PO avoids the recursive loop:</p>
     * <pre>
     *   C-3PO: "The records office says I should handle this value myself... that's circular!"
     *   C-3PO: [sets user preference aside] "Let me ask again, without my name in the way."
     *   Records office: "In that case, use the plain text clerk."
     *   C-3PO: [restores preference] "Excellent. I'll use the plain text clerk."
     * </pre>
     *
     * @param value  the value whose display we need to delegate
     * @return       a non-recursive {@link IValueEditor} appropriate for this value
     */
    private IValueEditor getValueEditor( IValue value )
    {
        IValueEditor vp = valueEditorManager.getCurrentValueEditor( value.getAttribute().getEntry(), value
            .getAttribute().getDescription() );

        // avoid recursion: unset the user selected value editor
        if ( vp instanceof RenameValueEditor )
        {
            IValueEditor userSelectedValueEditor = valueEditorManager.getUserSelectedValueEditor();
            valueEditorManager.setUserSelectedValueEditor( null );
            vp = valueEditorManager.getCurrentValueEditor( value.getAttribute().getEntry(), value.getAttribute()
                .getDescription() );
            valueEditorManager.setUserSelectedValueEditor( userSelectedValueEditor );
        }

        return vp;
    }


    // ── C-3PO RETRIEVES THE FULL ENTRY RECORD FROM THE HIERARCHY ────────────────
    // The records office asks C-3PO: "What entry does this hierarchy belong to?"
    // C-3PO simply looks at the hierarchy's entry field — it is the parent entry
    // that owns all of these attributes. This is what activate() will rename.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IEntry} that the given attribute hierarchy belongs to.
     * This is the "raw value" that we work with — the entry itself, not any
     * individual attribute value. {@code activate()} will cast this back to
     * {@link IEntry} when opening the rename dialog.
     *
     * <p>For example — C-3PO locates the parent entry:</p>
     * <pre>
     *   Records office: "Which entry do all these attribute cards belong to?"
     *   C-3PO: "They all belong to 'cn=Lobot, ou=cloudcity, dc=galaxy'. Here it is."
     * </pre>
     *
     * @param attributeHierarchy  the hierarchy from which we extract the owning entry
     * @return                    the {@link IEntry} that owns these attributes
     */
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        return attributeHierarchy.getEntry();
    }


    // ── C-3PO CHECKS THE REGISTER IS NOT BLANK ──────────────────────────────────
    // Before anything else, C-3PO makes sure the entry's card actually exists in
    // the directory. An entry without a parent entry would be an orphaned record —
    // nonsensical and un-renameable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the value's owning entry exists (is non-null), meaning
     * there is something meaningful to rename. If the entry reference is null this
     * value editor has nothing to work with.
     *
     * <p>For example — C-3PO checks for a valid parent record:</p>
     * <pre>
     *   C-3PO: "Does this value card have a parent entry? Yes — 'cn=Lobot' exists."
     *   C-3PO: "We may proceed with the rename operation."
     * </pre>
     *
     * @param value  the LDAP value to inspect
     * @return       {@code true} if the value's attribute has a non-null parent entry
     */
    public boolean hasValue( IValue value )
    {
        return value.getAttribute().getEntry() != null;
    }


    // ── C-3PO RETRIEVES THE ENTRY FOR A SINGLE VALUE CARD ───────────────────────
    // Given one attribute value card, C-3PO walks up the chain: value → attribute →
    // entry. That entry reference is the raw object we need to pass to activate().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IEntry} that owns the attribute this value belongs to.
     * We navigate value → attribute → entry to get the raw object that
     * {@link #activate()} needs when it opens the rename dialog.
     *
     * <p>For example — C-3PO traces the value back to its entry:</p>
     * <pre>
     *   C-3PO: "This 'cn' value card belongs to the 'cn' attribute..."
     *   C-3PO: "...which belongs to the entry 'cn=Lobot, ou=cloudcity, dc=galaxy'."
     *   C-3PO: "That entry is our raw value for the rename operation."
     * </pre>
     *
     * @param value  the LDAP value whose owning entry we want
     * @return       the {@link IEntry} that owns this value's attribute
     */
    public Object getRawValue( IValue value )
    {
        return value.getAttribute().getEntry();
    }


    // ── C-3PO DECLINES TO PRODUCE A STRING — THE WIZARD HANDLES IT ──────────────
    // Lando asks C-3PO: "Can you give me the rename in plain text for the journal?"
    // C-3PO replies: "No, sir — the rename wizard handles the journal. I contribute
    // nothing here." The rename operation is asynchronous and dialog-driven; there
    // is no string or binary value to return from this editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} always. The rename operation is dispatched as a background
     * job by {@link #activate()} — there is no string or binary to hand back through
     * the normal value editor pipeline. Callers should not expect a value here.
     *
     * <p>For example — C-3PO defers to the rename wizard:</p>
     * <pre>
     *   Lando: "Give me the renamed value as a string, 3PO."
     *   C-3PO: "I'm afraid that's not how this works, sir. The wizard handles it."
     *   C-3PO: "I shall return null. The background job has already been dispatched."
     * </pre>
     *
     * @param rawValue  the raw value (not used — we always return null)
     * @return          always {@code null}; the rename runs as a background job
     */
    public Object getStringOrBinaryValue( Object rawValue )
    {
        return null;
    }


    // ── C-3PO UPDATES HIS OFFICIAL DESIGNATION ──────────────────────────────────
    // Lando formally gives C-3PO a title in the Cloud City register: "Rename Editor."
    // The name is used in the UI, for example in context menus that let the user
    // choose a different editor for a cell.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the display name for this value editor, used in menus and preference
     * pages where users can choose which editor handles a given attribute type.
     *
     * <p>For example — C-3PO receives his official title:</p>
     * <pre>
     *   Lando: "From now on your designation is 'Rename Editor', 3PO."
     *   C-3PO: "Duly noted, sir. I've updated my official registry entry."
     * </pre>
     *
     * @param name  the human-readable name to assign to this editor
     */
    public void setValueEditorName( String name )
    {
        this.name = name;
    }


    // ── C-3PO REPORTS HIS CURRENT OFFICIAL DESIGNATION ──────────────────────────
    // When asked, C-3PO reads his title from the register — the same name that
    // was recorded when setValueEditorName was called. This is the label shown
    // in context menus like "Open with > Rename Editor".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this value editor as previously set by
     * {@link #setValueEditorName(String)}. Used wherever the UI needs to label
     * this editor — for example in "Open with ▶" context menu entries.
     *
     * <p>For example — C-3PO states his designation:</p>
     * <pre>
     *   Records keeper: "What is your designation, droid?"
     *   C-3PO: "I am the Rename Editor, sir. Pleased to make your acquaintance."
     * </pre>
     *
     * @return  the editor's display name
     */
    public String getValueEditorName()
    {
        return name;
    }


    // ── C-3PO ATTACHES HIS PORTRAIT TO THE REGISTER ─────────────────────────────
    // Every entry in the Cloud City diplomatic register has a small portrait (icon)
    // next to the name. C-3PO files his portrait so the UI can display it next to
    // "Rename Editor" in menus and preference pages.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the {@link ImageDescriptor} used as this editor's icon in the UI —
     * menus, toolbars, and preference pages that list available value editors all
     * use this image to visually identify the rename editor.
     *
     * <p>For example — C-3PO files his portrait:</p>
     * <pre>
     *   Lando: "Here's your portrait for the register, 3PO — a small rename icon."
     *   C-3PO: "I've filed it under my official entry. It will display in the menus."
     * </pre>
     *
     * @param imageDescriptor  the icon descriptor to associate with this editor
     */
    public void setValueEditorImageDescriptor( ImageDescriptor imageDescriptor )
    {
        this.imageDescriptor = imageDescriptor;
    }


    // ── C-3PO RETRIEVES HIS PORTRAIT FROM THE REGISTER ──────────────────────────
    // The UI asks C-3PO for his portrait so it can render the icon next to his name
    // in the "Open with" menu. C-3PO fetches the descriptor he filed earlier.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ImageDescriptor} previously stored via
     * {@link #setValueEditorImageDescriptor(ImageDescriptor)}. The framework uses
     * this to render the editor's icon in menus, toolbars, and preference tables.
     *
     * <p>For example — C-3PO presents his portrait:</p>
     * <pre>
     *   UI menu builder: "Show me your icon, 3PO — I need to render it next to your name."
     *   C-3PO: "Here you are — my official portrait from the Cloud City register."
     * </pre>
     *
     * @return  the icon {@link ImageDescriptor} for this editor, or {@code null} if
     *          none was set
     */
    public ImageDescriptor getValueEditorImageDescriptor()
    {
        return imageDescriptor;
    }
}
