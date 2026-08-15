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

import org.apache.directory.studio.ldapbrowser.common.wizards.EditEntryWizard;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// ── CLASS: EntryValueEditor — C-3PO Addresses Jabba as a Full Alien Dignitary ──
// In Return of the Jedi, Luke sends C-3PO into Jabba's palace to address the
// crime lord as a full-fledged diplomatic dignitary — not just to deliver a
// note, but to orchestrate the entire audience: introducing each member of the
// party, delegating the right protocol droid to speak for each topic, and
// coordinating the grand formal encounter. This class does the same for an
// LDAP entry: rather than editing a single attribute value, it treats the whole
// entry as the "dignitary" and opens the EditEntryWizard so the user can
// rearrange all attributes at once.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A special value editor that opens the full {@link EditEntryWizard} so the user
 * can edit an entire LDAP entry off-line (outside a live connection round-trip).
 * Unlike the other value editors that manage a single attribute value, this one
 * treats the whole {@link IEntry} as its "value" — the raw value is the entry
 * itself, and the wizard is the editing surface.
 * Think of this class as C-3PO managing a full dignitary audience with Jabba:
 * he introduces everyone, delegates display to the right specialist, and
 * coordinates the whole encounter.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryValueEditor extends CellEditor implements IValueEditor
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


    // ── C-3PO Arrives at Jabba's Gate with Luke's Full Credentials ────────────
    // Luke hands C-3PO a complete diplomatic dossier and the address of Jabba's
    // palace. C-3PO memorizes the parent shell (where to open dialogs), pockets
    // the value editor manager (his roster of specialist droids), and strides
    // up to the gate ready for the full audience.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new EntryValueEditor wired to the given parent composite and
     * the shared {@link ValueEditorManager}. The manager is how we look up which
     * specialist value editor to use when displaying individual attribute values
     * inside the wizard's summary view.
     *
     * <p>For example — C-3PO receiving his mission briefing:</p>
     * <pre>
     *   super(parent)                              // register with Eclipse
     *   this.parent = parent                       // remember where the shell is
     *   this.valueEditorManager = valueEditorManager  // roster of specialist droids
     * </pre>
     *
     * @param parent              The SWT {@link Composite} that owns the table
     *                            cell — used to find the shell for dialogs.
     * @param valueEditorManager  The manager that knows which value editor to
     *                            use for each attribute type; must not be null.
     */
    public EntryValueEditor( Composite parent, ValueEditorManager valueEditorManager )
    {
        super( parent );
        this.parent = parent;
        this.valueEditorManager = valueEditorManager;
    }


    // ── C-3PO Notes There Is No Dais to Stand On in the Antechamber ──────────
    // The palace antechamber is bare — there is no podium or lectern where
    // C-3PO could set up an inline presence. The real action happens inside
    // the throne room (the wizard). So C-3PO returns null: no widget here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the JFace framework to create an inline editing widget inside
     * the table cell. EntryValueEditor is dialog-based, so there is no inline
     * widget — the entire edit happens in the {@link EditEntryWizard}. We
     * return null to tell JFace not to embed anything in the cell.
     *
     * @param parent  The composite owning the cell — ignored here.
     * @return        Always null; no inline widget is created.
     */
    protected Control createControl( Composite parent )
    {
        return null;
    }


    // ── C-3PO Recalls What He Is Carrying for Jabba ──────────────────────────
    // A Gamorrean guard pats C-3PO down and demands to know what he has on
    // him. C-3PO reaches into his memory bank and reads back whatever Luke
    // packed for him before the mission — the IEntry object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value object currently stored in this editor — typically an
     * {@link IEntry} instance representing the LDAP entry to be edited. This
     * is whatever was last set via {@link #doSetValue(Object)}.
     *
     * @return The currently held value, or null if nothing has been set.
     */
    protected final Object doGetValue()
    {
        return value;
    }


    // ── C-3PO Stands in Protocol Silence Awaiting the Audience ───────────────
    // Protocol dictates that until the audience formally begins, C-3PO does not
    // move, speak, or draw attention. He stands perfectly still. Dialog-based
    // editors have no inline widget to focus, so this method does nothing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the JFace framework to give keyboard focus to the editor widget.
     * Since this is a dialog editor with no inline SWT control, there is nothing
     * to focus and this method intentionally does nothing.
     */
    protected void doSetFocus()
    {
    }


    // ── C-3PO Accepts Luke's Latest Diplomatic Brief ─────────────────────────
    // A messenger droid arrives and hands C-3PO an updated brief from Luke —
    // the IEntry object representing the dignitary (the LDAP entry) C-3PO is
    // about to present to Jabba. C-3PO tucks it away for when the audience opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the given value object so the editor has something to work with
     * when {@link #activate()} is called. For EntryValueEditor this is expected
     * to be an {@link IEntry}; passing anything else means the wizard will not
     * open when activated.
     *
     * @param value  The value to store — should be an {@link IEntry} instance.
     */
    protected void doSetValue( Object value )
    {
        this.value = value;
    }


    // ── C-3PO Throws Open the Audience Chamber Doors ─────────────────────────
    // The moment arrives: C-3PO steps forward, flings open the great doors of
    // Jabba's throne room, and presents the entry dignitary to the court. The
    // EditEntryWizard IS the throne room — it blocks until the user is done,
    // then closes. Win or lose, C-3PO always fires fireCancelEditor() because
    // the wizard handles its own commits; the cell editor framework never needs
    // to do a separate apply.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the JFace framework to start editing. We cast the stored value
     * to an {@link IEntry}, open the {@link EditEntryWizard} in a blocking
     * {@link WizardDialog}, and wait for the user to finish. We always call
     * {@link #fireCancelEditor()} afterwards because the wizard saves changes
     * itself — the cell editor framework does not need to do anything extra on
     * commit.
     *
     * <p>For example — C-3PO opening the throne room:</p>
     * <pre>
     *   IEntry entry = (IEntry) getValue()
     *   new WizardDialog(shell, new EditEntryWizard(entry)).open()
     *   fireCancelEditor()    // wizard handled the save; tell JFace we are done
     * </pre>
     */
    public void activate()
    {
        Object value = getValue();

        if ( value instanceof IEntry )
        {
            IEntry entry = ( IEntry ) value;

            if ( entry != null )
            {
                EditEntryWizard wizard = new EditEntryWizard( entry );
                WizardDialog dialog = new WizardDialog( parent.getShell(), wizard );
                dialog.setBlockOnOpen( true );
                dialog.create();
                dialog.open();
            }
        }

        fireCancelEditor();
    }


    // ── Jabba Points at C-3PO as the Official Diplomatic Channel ─────────────
    // When someone asks who is in charge of the audience, Jabba's tail thumps
    // the dais and he points a stubby finger directly at C-3PO: "That one.
    // He is my channel." C-3PO nods and returns a reference to himself.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link CellEditor} instance the JFace framework should use
     * for this edit. Since this class extends {@link CellEditor} directly,
     * we return {@code this}.
     *
     * @return This editor instance.
     */
    public CellEditor getCellEditor()
    {
        return this;
    }


    // ── C-3PO Introduces All the Visiting Dignitaries to the Court ───────────
    // Jabba's herald asks C-3PO to name every member of Luke's party. C-3PO
    // steps forward and reads them out: if there is more than one he prepends
    // the count ("3 values:"), then lists each one by name, delegating each
    // introduction to the right specialist droid for that dignitary type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a display string summarising all values in an
     * {@link AttributeHierarchy}. If there is more than one value we prepend
     * an "N values:" label; then we list each value's display string
     * (computed by the appropriate specialist value editor) separated by ", ".
     *
     * <p>For example — C-3PO's roll-call:</p>
     * <pre>
     *   3 values: "cn=Luke", "cn=Leia", "cn=Han"
     * </pre>
     *
     * @param attributeHierarchy  The attributes and their values to summarise.
     * @return                    A comma-separated display string, possibly
     *                            prefixed with a count label.
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


    // ── C-3PO Introduces a Single Visiting Dignitary ─────────────────────────
    // One dignitary steps forward. C-3PO consults his roster of specialist
    // protocol droids, picks the one who knows this particular dignitary's
    // customs, and lets that specialist make the formal introduction.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display string for a single {@link IValue} by delegating to
     * whichever specialist value editor is appropriate for that value's
     * attribute type. We use {@link #getValueEditor(IValue)} to look up the
     * right editor, then call its {@link IValueEditor#getDisplayValue(IValue)}.
     *
     * <p>For example — C-3PO delegating a single introduction:</p>
     * <pre>
     *   IValueEditor specialist = getValueEditor(value)
     *   return specialist.getDisplayValue(value)
     * </pre>
     *
     * @param value  The LDAP value whose display string we need.
     * @return       The display string produced by the appropriate specialist
     *               value editor.
     */
    public String getDisplayValue( IValue value )
    {
        IValueEditor vp = getValueEditor( value );

        return vp.getDisplayValue( value );
    }


    // ── C-3PO Finds the Right Protocol Droid for the Dignitary's Customs ─────
    // Not every dignitary speaks the same language. C-3PO consults his droid
    // roster via the ValueEditorManager to find which specialist knows this
    // particular dignitary's customs. If the roster accidentally points back to
    // C-3PO himself (which would cause infinite recursion), he temporarily
    // clears the user override and looks up the default specialist instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the appropriate {@link IValueEditor} for a given {@link IValue}
     * using the {@link ValueEditorManager}. We guard against infinite recursion:
     * if the manager's current editor is another {@link EntryValueEditor} (which
     * could happen when the user has explicitly selected this editor), we
     * temporarily null out the user selection, look up the default, then restore
     * the user selection before returning.
     *
     * <p>For example — C-3PO's recursive-loop guard:</p>
     * <pre>
     *   IValueEditor vp = manager.getCurrentValueEditor(entry, description)
     *   if vp instanceof EntryValueEditor:
     *       // temporarily clear user override to avoid loop
     *       vp = manager.getCurrentValueEditor(entry, description)
     *       // restore override
     *   return vp
     * </pre>
     *
     * @param value  The LDAP value whose attribute type determines which editor
     *               to return.
     * @return       The best available {@link IValueEditor} for this value,
     *               guaranteed not to be another EntryValueEditor.
     */
    private IValueEditor getValueEditor( IValue value )
    {
        IValueEditor vp = valueEditorManager.getCurrentValueEditor( value.getAttribute().getEntry(), value
            .getAttribute().getDescription() );

        // avoid recursion: unset the user selected value editor
        if ( vp instanceof EntryValueEditor )
        {
            IValueEditor userSelectedValueEditor = valueEditorManager.getUserSelectedValueEditor();
            valueEditorManager.setUserSelectedValueEditor( null );
            vp = valueEditorManager.getCurrentValueEditor( value.getAttribute().getEntry(), value.getAttribute()
                .getDescription() );
            valueEditorManager.setUserSelectedValueEditor( userSelectedValueEditor );
        }

        return vp;
    }


    // ── C-3PO Retrieves the Whole Dignitary's Dossier from the Archive ────────
    // The archivist hands C-3PO the complete dossier on the visiting dignitary —
    // not just one attribute but the entire IEntry object that represents the
    // LDAP entry. That entry is what the EditEntryWizard needs to open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw value for this editor from an {@link AttributeHierarchy}.
     * For the entry editor, the "raw value" is the {@link IEntry} object that
     * owns the attributes — we return it directly so {@link #activate()} can
     * pass it to the wizard.
     *
     * @param attributeHierarchy  The attributes whose owning entry we want.
     * @return                    The {@link IEntry} that owns the hierarchy.
     */
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        return attributeHierarchy.getEntry();
    }


    // ── C-3PO Checks Whether the Dignitary Is Actually in Residence ──────────
    // Before announcing a visiting dignitary, C-3PO confirms the entry is real
    // and is actually present — an attribute whose entry is null cannot be
    // introduced, so there is nothing to edit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given {@link IValue}'s owning entry is non-null,
     * meaning there is a real LDAP entry backing this value that the wizard
     * can edit.
     *
     * @param value  The LDAP value to inspect.
     * @return       {@code true} if {@code value.getAttribute().getEntry()}
     *               is not null; {@code false} otherwise.
     */
    public boolean hasValue( IValue value )
    {
        return value.getAttribute().getEntry() != null;
    }


    // ── C-3PO Retrieves One Dignitary's Entry Credentials ────────────────────
    // A guard asks for the credentials of a specific value. C-3PO reaches
    // through the attribute chain and returns the IEntry that ultimately owns
    // that value — the same entry the wizard will open for editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw value for this editor from a single {@link IValue}.
     * Just like the {@link AttributeHierarchy} overload, the "raw value" is
     * the {@link IEntry} that owns the attribute — we walk up the chain:
     * value → attribute → entry.
     *
     * @param value  The LDAP value whose owning entry we want.
     * @return       The {@link IEntry} that owns this value's attribute.
     */
    public Object getRawValue( IValue value )
    {
        return value.getAttribute().getEntry();
    }


    // ── C-3PO Confirms There Is No Written Record to Hand Back ───────────────
    // After the wizard closes, Jabba's secretary asks C-3PO for a written copy
    // of the revised terms. C-3PO shrugs: "The wizard wrote everything directly
    // into the directory — there is no separate document to hand you."
    // The wizard commits changes itself, so we have nothing to return here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts the editor's raw value back to a form the LDAP model can store.
     * For the entry editor this always returns null because the
     * {@link EditEntryWizard} persists any changes directly to the entry —
     * there is no separate commit step driven by the cell editor framework.
     *
     * @param rawValue  Ignored.
     * @return          Always null.
     */
    public Object getStringOrBinaryValue( Object rawValue )
    {
        return null;
    }


    // ── Jabba Assigns C-3PO His Official Court Title ─────────────────────────
    // In a rare moment of generosity, Jabba leans forward and bestows an
    // official title on C-3PO: "You shall be known as the Grand Entry Editor
    // of the Outer Rim." C-3PO bows and records it in his identity registers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the human-readable display name for this value editor. The name
     * appears in the "Open With" context menu and the value editor preference
     * page so users can recognize this editor.
     *
     * @param name  The display name to assign — for example "Entry Editor".
     */
    public void setValueEditorName( String name )
    {
        this.name = name;
    }


    // ── C-3PO Announces His Official Court Title to the Guard ────────────────
    // A Gamorrean guard blocks the corridor and demands identification. C-3PO
    // straightens up and recites his registered court title exactly as Jabba
    // recorded it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display name previously set via
     * {@link #setValueEditorName(String)}. Used by the UI to label this editor
     * in menus and preference pages.
     *
     * @return The editor's display name, or null if none has been set yet.
     */
    public String getValueEditorName()
    {
        return name;
    }


    // ── Jabba Affixes His Official Seal to C-3PO's Credentials ───────────────
    // Jabba presses his signet ring into hot wax and stamps it onto C-3PO's
    // credential card — the visual emblem that will appear next to C-3PO's
    // name whenever his court role is displayed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@link ImageDescriptor} used to represent this value editor
     * visually in the UI — for example, as an icon next to the editor's name
     * in the "Open With" menu.
     *
     * @param imageDescriptor  The image descriptor to use as this editor's
     *                         icon; may be null to clear it.
     */
    public void setValueEditorImageDescriptor( ImageDescriptor imageDescriptor )
    {
        this.imageDescriptor = imageDescriptor;
    }


    // ── C-3PO Displays the Official Court Seal at the Gate ───────────────────
    // The palace gate guard holds out a hand and demands to see the official
    // seal. C-3PO produces the emblem Jabba stamped on him so the guard can
    // verify it and wave him through the checkpoint.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ImageDescriptor} previously set via
     * {@link #setValueEditorImageDescriptor(ImageDescriptor)}. The UI uses this
     * to render the editor's icon in menus and preference pages.
     *
     * @return The editor's image descriptor, or null if none has been set.
     */
    public ImageDescriptor getValueEditorImageDescriptor()
    {
        return imageDescriptor;
    }
}
