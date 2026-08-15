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
package org.apache.directory.studio.templateeditor.editor.widgets;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateWidget;
import org.apache.directory.studio.templateeditor.model.widgets.WidgetAlignment;


// ── CLASS: EditorWidget — THE TANTIVE IV CONTROL PANEL BASE ──────────────────────
// Every control panel on the Tantive IV's bridge is built from the same base
// chassis: power connector, mounting bracket, feedback wire back to the ship's
// computer. Some panels show text, some flip toggles, some display images — but
// they all share this foundation. This abstract class is that chassis: it provides
// the shared wiring (editor reference, toolkit, template model) and the common
// attribute operations (add, modify, delete) that every concrete EditorWidget
// subclass needs. The subclass just fills in what shows on the panel's face.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all live editor widgets in the template entry editor.
 * Each concrete subclass ({@link EditorCheckbox}, {@link EditorTextField}, etc.)
 * renders one specific type of template widget ({@link TemplateWidget}) as an
 * interactive SWT control and wires it to the LDAP entry's shared working copy.
 * This base provides the common infrastructure: access to the entry, the toolkit,
 * the template model, and CRUD operations on the LDAP attribute.
 * Think of this as the standard chassis for every Tantive IV control panel.
 *
 * @param <E>  the specific {@link TemplateWidget} model type this editor widget renders
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class EditorWidget<E extends TemplateWidget>
{
    /** The widget*/
    private E widget;

    /** The associated editor */
    private IEntryEditor entryEditor;

    /** The toolkit */
    private FormToolkit toolkit;


    // ── CONSTRUCTOR: MOUNT THE PANEL ON THE BRIDGE ────────────────────────────────
    // The technician installs the panel: clips in the template model (widget),
    // connects the feedback wire (entryEditor), and plugs in the rendering engine
    // (toolkit). From this point, the panel is ready to createWidget().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes this editor widget with its template model, the owning entry
     * editor, and the form toolkit used to create SWT controls.
     *
     * <p>For example — mounting the panel on the Tantive IV bridge:</p>
     * <pre>
     *   new EditorTextField(editor, templateTextField, toolkit);
     *   // "Panel installed. Ready to display text attribute."
     * </pre>
     *
     * @param widget       the template model object describing this widget's configuration
     * @param entryEditor  the entry editor that owns this widget (source of the LDAP entry)
     * @param toolkit      the form toolkit used to create form-aware SWT controls
     */
    public EditorWidget( E widget, IEntryEditor entryEditor, FormToolkit toolkit )
    {
        this.widget = widget;
        this.entryEditor = entryEditor;
        this.toolkit = toolkit;
    }


    // ── GET WIDGET: RETRIEVE THE PANEL'S CONFIGURATION BLUEPRINT ─────────────────
    // The template model object describes how this panel should look and behave:
    // which LDAP attribute it binds to, whether it's enabled, what its alignment is.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the template model object that describes this widget's configuration.
     * Subclasses use this to access settings like attribute type, label, alignment,
     * and enabled state.
     *
     * @return the template model; never {@code null} after construction
     */
    public E getWidget()
    {
        return widget;
    }


    // ── CREATE WIDGET: MATERIALIZE THE PANEL FACE ─────────────────────────────────
    // This is where the panel face appears on-screen. The default implementation is
    // a no-op that just returns the parent (for container widgets like composites).
    // Concrete subclasses override this to create their specific SWT controls.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns the SWT composite that hosts this widget's controls.
     * The default implementation returns the parent unchanged — useful for widgets
     * that don't add their own composite. Subclasses override to create buttons,
     * text fields, images, etc.
     *
     * <p>For example — materializing the panel face:</p>
     * <pre>
     *   Composite composite = initWidget(parent);
     *   updateWidget();  // Fill with LDAP attribute data.
     *   addListeners();  // Wire up the user interaction handlers.
     *   return composite;
     * </pre>
     *
     * @param parent  the SWT composite to build inside
     * @return the composite that hosts this widget (may be {@code parent} itself)
     */
    public Composite createWidget( Composite parent )
    {
        return parent;
    }


    // ── GET EDITOR: RETRIEVE THE FEEDBACK WIRE ───────────────────────────────────
    // The entry editor is the feedback wire from this panel back to the ship's main
    // computer — it's how we read the current LDAP entry and how changes propagate
    // back through the editor's working copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry editor that owns this widget. Used to access the current
     * LDAP entry's shared working copy and to propagate modifications.
     *
     * @return the owning {@link IEntryEditor}; never {@code null} after construction
     */
    public IEntryEditor getEditor()
    {
        return entryEditor;
    }


    // ── GET TOOLKIT: RETRIEVE THE PANEL RENDERING ENGINE ─────────────────────────
    // The FormToolkit is the factory that creates form-aware SWT widgets. Subclasses
    // call this when building their SWT controls so everything renders consistently.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link FormToolkit} used to create form-aware SWT controls.
     * Subclasses use this in their {@code initWidget()} method to produce buttons,
     * labels, and text fields that visually match the parent form.
     *
     * @return the {@link FormToolkit}; never {@code null} after construction
     */
    public FormToolkit getToolkit()
    {
        return toolkit;
    }


    // ── GET ENTRY: GET THE CURRENT LDAP RECORD ───────────────────────────────────
    // The ship's main computer holds the current entry record. We retrieve it through
    // the editor's shared working copy — this is the live, editable version of the
    // LDAP entry that changes are written to before being committed to the server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current LDAP entry from the editor's shared working copy. This is
     * the live, mutable copy that changes are written to. Attribute modifications via
     * {@link #updateAttributeValue(Object)} affect this copy.
     *
     * @return the current {@link IEntry} from the working copy; may be {@code null}
     */
    protected IEntry getEntry()
    {
        EntryEditorInput input = getEditor().getEntryEditorInput();
        return input.getSharedWorkingCopy( getEditor() );
    }


    // ── UPDATE: REFRESH THE PANEL READOUT ────────────────────────────────────────
    // When the ship's data changes, every panel needs to refresh its readout.
    // Subclasses implement this to re-read the attribute value from the working copy
    // and repaint their SWT control to show the current data.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes this widget's display to reflect the current state of the LDAP
     * working copy. Called by {@link org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget#update()}
     * when the entry data changes externally.
     */
    public abstract void update();


    // ── DISPOSE: SHUT DOWN THE PANEL ─────────────────────────────────────────────
    // When the ship is powering down, every panel shuts off and releases its
    // resources. Subclasses implement this to dispose any SWT Image objects or
    // other OS-level resources they hold.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any OS-level resources held by this widget (e.g., SWT {@code Image}
     * objects). Called by {@link org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget#dispose()}
     * when the editor is closing. Always called from the UI thread.
     */
    public abstract void dispose();


    // ── GET GRIDDATA: READ THE PANEL'S MOUNTING SPEC ─────────────────────────────
    // Each panel has a mounting spec that tells the ship's engineer how much space
    // it takes up and how it should be positioned in the bay. We translate the
    // TemplateWidget's alignment/span/size settings into a SWT GridData object that
    // the layout manager can use.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a {@link GridData} layout constraint from the template widget's
     * alignment, span, and size settings. Used by subclasses in their
     * {@code initWidget()} method when setting layout data on the root SWT control.
     *
     * <p>For example — reading the panel's mounting spec:</p>
     * <pre>
     *   GridData gd = getGridata();
     *   textfield.setLayoutData(gd);
     *   // "Panel mounted with correct alignment and span."
     * </pre>
     *
     * @return a configured {@link GridData} for this widget
     */
    protected GridData getGridata()
    {
        // Creating the grid data with alignment and grab excess values
        GridData gd = new GridData( convertWidgetAlignmentToSWTValue( widget.getHorizontalAlignment() ),
            convertWidgetAlignmentToSWTValue( widget.getVerticalAlignment() ), widget.isGrabExcessHorizontalSpace(),
            widget.isGrabExcessVerticalSpace(), widget.getHorizontalSpan(), widget.getVerticalSpan() );

        // Setting width (if needed)
        if ( widget.getImageWidth() != TemplateWidget.DEFAULT_SIZE )
        {
            gd.widthHint = widget.getImageWidth();
        }

        // Setting height (if needed)
        if ( widget.getImageHeight() != TemplateWidget.DEFAULT_SIZE )
        {
            gd.heightHint = widget.getImageHeight();
        }

        return gd;
    }


    // ── CONVERT WIDGET ALIGNMENT TO SWT VALUE: TRANSLATE THE MOUNTING DIRECTION ──
    // The TemplateWidget model uses its own WidgetAlignment enum. SWT uses integer
    // constants. This converter maps one to the other so the GridData is correct.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a {@link WidgetAlignment} enum value to the equivalent SWT integer
     * constant (e.g., {@link SWT#FILL}, {@link SWT#CENTER}). Used by
     * {@link #getGridata()} to build the correct {@link GridData}.
     *
     * @param alignment  the widget alignment to convert
     * @return the corresponding SWT alignment constant; {@link SWT#NONE} if unrecognized
     */
    private static int convertWidgetAlignmentToSWTValue( WidgetAlignment alignment )
    {
        switch ( alignment )
        {
            case NONE:
                return SWT.NONE;
            case BEGINNING:
                return SWT.BEGINNING;
            case CENTER:
                return SWT.CENTER;
            case END:
                return SWT.END;
            case FILL:
                return SWT.FILL;
            default:
                return SWT.NONE;
        }
    }


    // ── GET ATTRIBUTE: PULL THE DATA LINE FROM THE ENTRY ─────────────────────────
    // The panel reads its data from a specific attribute line in the LDAP entry.
    // We look up the attribute by the type name declared in the template model.
    // Returns null if the attribute doesn't exist on the entry yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attribute from the current entry that this widget is bound to.
     * The attribute type comes from {@link TemplateWidget#getAttributeType()}. Returns
     * {@code null} if the attribute does not exist on the entry or the entry is null.
     *
     * @return the bound {@link IAttribute}, or {@code null} if not present
     */
    protected IAttribute getAttribute()
    {
        if ( ( getEntry() != null ) && ( getWidget() != null ) )
        {
            return getEntry().getAttribute( getWidget().getAttributeType() );
        }

        return null;
    }


    // ── UPDATE ATTRIBUTE VALUE: WRITE THE PANEL'S OUTPUT BACK TO THE RECORD ──────
    // The panel operator has changed a value. We need to write it back to the LDAP
    // entry's working copy. If the attribute didn't exist before, we create it.
    // If the new value is empty, we delete the attribute. Otherwise we modify it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the given value to the LDAP attribute this widget is bound to. Handles
     * three cases: attribute doesn't exist yet (creates it), value is empty (deletes
     * attribute), or attribute exists with a non-empty value (modifies it).
     *
     * <p>For example — writing the panel output back to the entry:</p>
     * <pre>
     *   updateAttributeValue("Luke Skywalker");
     *   // "cn attribute modified on working copy."
     * </pre>
     *
     * @param value  the new value to write; empty string signals "delete the attribute"
     */
    protected void updateAttributeValue( Object value )
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute == null ) )
        {
            if ( !"".equals( value ) ) //$NON-NLS-1$
            {
                // Creating a new attribute with the value
                addNewAttribute( value );
            }
        }
        else
        {
            if ( !"".equals( value ) ) //$NON-NLS-1$
            {
                // Modifying the existing attribute
                modifyAttributeValue( value );
            }
            else
            {
                // Deleting the attribute
                deleteAttribute();
            }
        }
    }


    // ── ADD NEW ATTRIBUTE: INSTALL A NEW DATA LINE ON THE RECORD ─────────────────
    // The LDAP entry doesn't have this attribute yet — we create it from scratch
    // using the attribute type declared in the template model and the given value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link Attribute} on the current LDAP entry with the given value.
     * The attribute type comes from {@link TemplateWidget#getAttributeType()}.
     * Used when the attribute does not yet exist on the entry.
     *
     * @param value  the initial value for the new attribute; must not be {@code null}
     */
    protected void addNewAttribute( Object value )
    {
        if ( ( getEntry() != null ) && ( getWidget() != null ) )
        {
            Attribute newAttribute = new Attribute( getEntry(), getWidget().getAttributeType() );
            newAttribute.addValue( new Value( newAttribute, value ) );
            getEntry().addAttribute( newAttribute );
        }
    }


    // ── MODIFY ATTRIBUTE VALUE: UPDATE THE DATA LINE ON THE RECORD ───────────────
    // The attribute already exists. We replace its first value with the new one —
    // delete the old value, add the new one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the first value of the bound LDAP attribute with the given value.
     * Deletes the existing first value, then adds the new one.
     *
     * @param value  the new value to set; must not be {@code null}
     */
    protected void modifyAttributeValue( Object value )
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.getValueSize() > 0 ) )
        {
            attribute.deleteValue( attribute.getValues()[0] );
            attribute.addValue( new Value( attribute, value ) );
        }
    }


    // ── DELETE ATTRIBUTE: REMOVE THE DATA LINE FROM THE RECORD ───────────────────
    // The operator cleared the panel. We remove the entire attribute from the LDAP
    // entry's working copy — when the entry is saved, the attribute will be deleted
    // from the LDAP server too.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deletes the bound LDAP attribute from the current entry's working copy.
     * Called when the user clears a field. On save, the attribute will be removed
     * from the LDAP server.
     */
    protected void deleteAttribute()
    {
        if ( ( getEntry() != null ) && ( getWidget() != null ) && ( getAttribute() != null ) )
        {
            getEntry().deleteAttribute( getAttribute() );
        }
    }


    // ── ADD ATTRIBUTE VALUE: APPEND A NEW VALUE TO A MULTI-VALUED LINE ───────────
    // LDAP allows attributes to have multiple values (e.g., a user can have several
    // "mail" addresses). This method adds another value to the existing attribute's
    // list — or creates the attribute if it doesn't exist yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a string value to the bound LDAP attribute. If the attribute is a
     * string attribute, adds to its value list. If the attribute doesn't exist,
     * creates it with this as the first value.
     *
     * @param value  the string value to append; must not be {@code null}
     */
    protected void addAttributeValue( String value )
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) )
        {
            attribute.addValue( new Value( attribute, value ) );
        }
        else
        {
            addNewAttribute( value );
        }
    }


    // ── DELETE ATTRIBUTE VALUE: REMOVE ONE VALUE FROM A MULTI-VALUED LINE ────────
    // LDAP multi-valued attributes need surgical value removal — we walk the values
    // list, find the one that matches the given string, and delete it without
    // affecting the other values.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific string value from the bound LDAP attribute. Walks all
     * values looking for an exact string match, then deletes that one value. Only
     * applies to string-typed attributes.
     *
     * @param value  the string value to remove; must not be {@code null}
     */
    protected void deleteAttributeValue( String value )
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.isString() ) && ( attribute.getValueSize() > 0 ) )
        {
            for ( IValue attributeValue : attribute.getValues() )
            {
                if ( attributeValue.getStringValue().equals( value ) )
                {
                    attribute.deleteValue( attributeValue );
                    break;
                }
            }
        }
    }
}
