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

import org.apache.directory.studio.ldapbrowser.common.dialogs.MultivaluedDialog;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// ── CLASS: MultivaluedValueEditor — C-3PO LISTS ALL SIX MILLION FORMS ────────
// Aboard the Millennium Falcon, a sensor alert announces that the ship has
// entered a region where six different species are broadcasting simultaneously
// in their native languages. C-3PO — fluent in over six million forms — doesn't
// just translate one: he rattles off a comma-separated roll-call of every
// transmission, then opens a dedicated translation dialog where each message
// can be reviewed and edited individually by the right specialist.
// We do the same: this editor handles LDAP attributes that have multiple values.
// In the table it displays a comma-separated summary; when activated it opens
// the {@link MultivaluedDialog} where each individual value can be edited by
// the appropriate single-value editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A special value editor for LDAP attributes that carry multiple values. In the
 * search result or entry editor table it shows a comma-separated list of all
 * values; when the user activates the cell, it opens the
 * {@link MultivaluedDialog} where each value can be inspected and edited
 * individually using the right per-type value editor.
 * Think of this as C-3PO listing all six million forms of communication: one
 * summary line for the table, and a dedicated panel for the detail work.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultivaluedValueEditor extends CellEditor implements IValueEditor
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


    // ── C-3PO POWERS UP AND TAKES HIS POST ───────────────────────────────────
    // C-3PO steps onto the Falcon's bridge and takes up his post at the
    // communications console, storing a reference to the parent control and
    // to the ValueEditorManager so he knows which specialist droid to call on
    // for each individual value when the dialog opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code MultivaluedValueEditor}. We call the parent
     * {@link CellEditor} constructor to register with the SWT widget hierarchy,
     * then store both the parent composite (needed to open dialogs) and the
     * {@link ValueEditorManager} (needed to pick the right per-type editor for
     * each individual value in the dialog).
     *
     * @param parent              the parent SWT composite — used when opening
     *                            the {@link MultivaluedDialog}
     * @param valueEditorManager  the manager we consult to find the right editor
     *                            for each individual attribute value
     */
    public MultivaluedValueEditor( Composite parent, ValueEditorManager valueEditorManager )
    {
        super( parent );
        this.parent = parent;
        this.valueEditorManager = valueEditorManager;
    }


    // ── C-3PO DOESN'T NEED AN INLINE CONSOLE ─────────────────────────────────
    // For multi-value attributes, C-3PO doesn't set up an inline translation
    // widget in the table cell — all the editing happens in the dedicated dialog.
    // We return null so Eclipse knows there is no persistent in-cell control.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this is a dialog-based editor — there is no
     * persistent in-cell control. Eclipse's {@link CellEditor} framework accepts
     * {@code null} here for dialog editors.
     *
     * @param parent  the parent composite (not used)
     * @return        {@code null} always
     */
    protected Control createControl( Composite parent )
    {
        return null;
    }


    // ── C-3PO READS BACK HIS LAST BRIEFING PACKET ────────────────────────────
    // When asked what he last received, C-3PO reads from his memory the exact
    // briefing packet (the attribute hierarchy) that was handed to him earlier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current value stored in this editor — the
     * {@link AttributeHierarchy} that was passed in via {@code doSetValue()}.
     * Eclipse's cell editor framework calls this to retrieve the value after
     * editing is complete.
     *
     * @return  the stored value object
     */
    protected final Object doGetValue()
    {
        return value;
    }


    // ── C-3PO DOESN'T GRAB THE KEYBOARD ──────────────────────────────────────
    // Since C-3PO operates via a dialog and not an inline widget, he doesn't
    // need to move keyboard focus anywhere when the cell is activated.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — this editor uses a dialog, not an inline control, so there is no
     * widget to focus. Eclipse requires this method to be implemented as part of
     * the {@link CellEditor} contract.
     */
    protected void doSetFocus()
    {
    }


    // ── C-3PO MEMORISES THE NEW BRIEFING PACKET ──────────────────────────────
    // When handed a new briefing packet (the attribute hierarchy), C-3PO
    // commits it to memory so he can reference it when the dialog opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the given value — expected to be an {@link AttributeHierarchy} —
     * in this editor's internal field. Eclipse calls this before activating the
     * cell editor so we have the data ready when the dialog opens.
     *
     * @param value  the attribute hierarchy to store
     */
    protected void doSetValue( Object value )
    {
        this.value = value;
    }


    // ── C-3PO OPENS THE FULL TRANSLATION DIALOG ───────────────────────────────
    // When the operator activates the cell, C-3PO opens the full multi-value
    // translation dialog and works through each transmission in turn. When done
    // he fires the "cancel" signal — not because something went wrong, but
    // because multi-value edits are committed inside the dialog, not through
    // the normal cell-editor flow.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link MultivaluedDialog} for the current attribute hierarchy.
     * Editing is committed inside the dialog itself, so after the dialog closes
     * we always call {@code fireCancelEditor()} to prevent Eclipse's cell editor
     * framework from trying to commit a (non-existent) raw value.
     *
     * <p>For example — C-3PO opens the translation session:</p>
     * <pre>
     *   ah = { attribute "mail", values: ["han@falcon.net", "solo@jabba.org"] }
     *   C-3PO opens MultivaluedDialog showing both addresses
     *   User edits "han@falcon.net" → "han@rebellion.org" in the dialog
     *   Dialog closes → change already committed; fireCancelEditor() called
     * </pre>
     */
    public void activate()
    {
        if ( getValue() instanceof AttributeHierarchy )
        {
            AttributeHierarchy ah = ( AttributeHierarchy ) getValue();

            if ( ah != null )
            {
                MultivaluedDialog dialog = new MultivaluedDialog( parent.getShell(), ah );
                dialog.open();
            }
        }

        fireCancelEditor();
    }


    // ── C-3PO CONFIRMS HE IS HIS OWN TRANSLATION UNIT ───────────────────────
    // When asked for his communications unit, C-3PO points to himself — he is
    // both the cell editor and the IValueEditor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns this object as the {@link CellEditor}. Because
     * {@code MultivaluedValueEditor} extends {@link CellEditor} directly, we
     * simply return {@code this}.
     *
     * @return  {@code this}
     */
    public CellEditor getCellEditor()
    {
        return this;
    }


    // ── C-3PO RATTLES OFF THE FULL ROLL-CALL ─────────────────────────────────
    // When asked for a summary of all transmissions, C-3PO lists every value
    // in the attribute hierarchy as a comma-separated roll-call, prefixed with
    // a count badge if there is more than one: "3 values: han@…, leia@…, luke@…"
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a comma-separated display string for all values in the given
     * attribute hierarchy. If there is more than one value we prepend a count
     * badge (e.g. {@code "3 values: …"}) so the user can see at a glance that
     * the cell contains multiple items. Each individual value is formatted by its
     * own per-type value editor.
     *
     * @param attributeHierarchy  the attribute hierarchy containing all values
     *                            to summarise
     * @return                    a comma-separated string of all display values,
     *                            with an optional count prefix
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

            IValueEditor vp = valueEditorManager.getCurrentValueEditor( value.getAttribute().getEntry(), value
                .getAttribute().getDescription() );
            sb.append( vp.getDisplayValue( value ) );
        }

        return sb.toString();
    }


    // ── C-3PO DECLINES TO TRANSLATE A SINGLE LINE ────────────────────────────
    // When asked to translate just one transmission in isolation, C-3PO politely
    // returns an empty string — his multi-value roll-call is designed for the
    // full group, not for individual lines.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty string — it doesn't make sense to use the
     * {@code MultivaluedValueEditor} for a single value, so we just return
     * {@link IValueEditor#EMPTY}. The search result editor will always call
     * {@link #getDisplayValue(AttributeHierarchy)} instead for multi-value
     * attributes.
     *
     * @param value  the single value (not used)
     * @return       {@link IValueEditor#EMPTY} always
     */
    public String getDisplayValue( IValue value )
    {
        return EMPTY; //$NON-NLS-1$
    }


    // ── C-3PO ACCEPTS THE FULL BRIEFING PACKET ───────────────────────────────
    // When the search result editor hands C-3PO the full attribute hierarchy
    // briefing, he accepts it and passes it along as-is — the individual
    // specialist droids in the dialog will handle each value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute hierarchy itself as the raw value — we pass the
     * whole hierarchy through to the {@link MultivaluedDialog} so it can hand
     * each individual value to the correct per-type editor.
     *
     * @param attributeHierarchy  the attribute hierarchy to edit
     * @return                    {@code attributeHierarchy} unchanged
     */
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        return attributeHierarchy;
    }


    // ── C-3PO CONFIRMS THERE IS A TRANSMISSION ───────────────────────────────
    // When asked if there is a live transmission, C-3PO always says no for
    // single-value requests — the multi-value flow doesn't track individual
    // value presence.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — the multi-valued editor doesn't track
     * individual value presence; that responsibility belongs to the per-type
     * editors that handle each value inside the dialog.
     *
     * @param value  the value to check (not used)
     * @return       {@code false} always
     */
    public boolean hasValue( IValue value )
    {
        return false;
    }

    // ── C-3PO DECLINES A SINGLE-VALUE MISSION ────────────────────────────────
    // When handed just one transmission and asked to act as the editor, C-3PO
    // returns null — his mandate is the full group, not individual messages.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — the multi-valued editor does not handle individual
     * single values; it only works with attribute hierarchies. The
     * {@link ValueEditorManager} will only assign this editor when the attribute
     * has multiple values.
     *
     * @param value  the single value (not used)
     * @return       {@code null} always
     */
    public Object getRawValue( IValue value )
    {
        return null;
    }


    // ── C-3PO DEFERS THE COMMIT TO THE SPECIALISTS ───────────────────────────
    // Modifications to individual values are committed inside the dialog by the
    // specialist editors. C-3PO has no further role in the commit — he returns
    // null to signal that nothing needs to be written at the cell-editor level.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — modifications are committed inside the
     * {@link MultivaluedDialog} by the individual per-type value editors.
     * There is no raw value to convert here at the outer level.
     *
     * @param rawValue  the raw value (not used)
     * @return          {@code null} always
     */
    public Object getStringOrBinaryValue( Object rawValue )
    {
        return null;
    }


    // ── C-3PO RECORDS HIS OFFICIAL TITLE ─────────────────────────────────────
    // The Council formally assigns C-3PO his title for the mission register.
    // He stores it so any system that asks can address him correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the display name assigned by the plugin framework during
     * initialisation.
     *
     * @param name  the editor's display name
     */
    public void setValueEditorName( String name )
    {
        this.name = name;
    }


    // ── C-3PO ANNOUNCES HIS OFFICIAL TITLE ───────────────────────────────────
    // When asked to identify himself in the mission register, C-3PO states the
    // title the Council assigned to him at induction.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name previously set by
     * {@link #setValueEditorName(String)}.
     *
     * @return  the editor's display name
     */
    public String getValueEditorName()
    {
        return name;
    }


    // ── C-3PO ACCEPTS HIS HOLOCRON BADGE ─────────────────────────────────────
    // The Council issues C-3PO his icon for the mission badge register — a
    // small holographic image he'll display whenever the UI needs to identify him.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the image descriptor assigned by the plugin framework during
     * initialisation. The image is displayed next to the editor's name in the UI.
     *
     * @param imageDescriptor  the image descriptor for this editor's icon
     */
    public void setValueEditorImageDescriptor( ImageDescriptor imageDescriptor )
    {
        this.imageDescriptor = imageDescriptor;
    }


    // ── C-3PO PRESENTS HIS HOLOCRON BADGE ────────────────────────────────────
    // When asked for identification, C-3PO holds up the holocron badge the
    // Council issued him so any system can display the right icon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor previously set by
     * {@link #setValueEditorImageDescriptor(ImageDescriptor)}.
     *
     * @return  the image descriptor for this editor's icon
     */
    public ImageDescriptor getValueEditorImageDescriptor()
    {
        return imageDescriptor;
    }
}
