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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateListbox;
import org.apache.directory.studio.templateeditor.model.widgets.ValueItem;


// ── CLASS: EditorListbox — THE TANTIVE IV SELECTOR PANEL ─────────────────────────
// On the Tantive IV's mission panel, certain settings are chosen from a fixed list:
// "Combat", "Patrol", "Diplomatic Escort" — the crew picks one (or several) from
// the selector panel, and the choice is broadcast to the ship's computer. This
// widget renders that selector: a JFace {@link ListViewer} pre-populated with the
// template's configured {@link ValueItem} list. When the operator selects one or
// more items, the corresponding LDAP attribute values are updated — the old values
// are cleared first, then the newly selected ones are added.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A list-based selection widget bound to a multi-valued (or single-valued) LDAP
 * attribute. Displays the template's configured list of {@link ValueItem}s and
 * writes the selected items' values back to the LDAP attribute on selection change.
 * Supports single or multiple selection mode per the template model.
 * Think of this as the Tantive IV mission selector panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorListbox extends EditorWidget<TemplateListbox>
{
    /** The list viewer */
    private ListViewer listViewer;

    /** The selection listener */
    private ISelectionChangedListener selectionListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            StructuredSelection selection = ( StructuredSelection ) listViewer.getSelection();
            if ( !selection.isEmpty() )
            {
                // Deleting the old attribute
                deleteAttribute();

                // Re-creating the attribute with the selected values
                Iterator<?> iterator = selection.iterator();
                while ( iterator.hasNext() )
                {
                    ValueItem item = ( ValueItem ) iterator.next();
                    addAttributeValue( ( String ) item.getValue() );
                }
            }
        }
    };


    // ── CONSTRUCTOR: INSTALL THE SELECTOR PANEL ───────────────────────────────────
    // The technician installs the selector panel. It displays the items configured
    // in the template model and binds to the LDAP attribute type declared there.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorListbox} bound to the given template listbox model.
     *
     * @param editor            the owning entry editor
     * @param templateListbox   the template model specifying items, multi-select flag, etc.
     * @param toolkit           the form toolkit
     */
    public EditorListbox( IEntryEditor editor, TemplateListbox templateListbox, FormToolkit toolkit )
    {
        super( templateListbox, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE SELECTOR LIST ────────────────────────────────────
    /**
     * Creates the list viewer, fills it with the template's items, highlights
     * the currently selected LDAP attribute value(s), and attaches the selection
     * listener.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    public Composite createWidget( Composite parent )
    {
        // Creating and initializing the widget UI
        Composite composite = initWidget( parent );

        // Updating the widget's content
        updateWidget();

        // Adding the listeners
        addListeners();

        return composite;
    }


    // ── INIT WIDGET: BUILD THE LIST ───────────────────────────────────────────────
    // We create the SWT List with single or multiple selection style, wrap it in a
    // JFace ListViewer with a label provider that shows each ValueItem's label,
    // and set the template's item list as the viewer input.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT {@link List} widget wrapped in a JFace {@link ListViewer},
     * configured for single or multiple selection per the template model.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    private Composite initWidget( Composite parent )
    {
        // Getting the style of the listbox
        int style = SWT.BORDER /*| SWT.V_SCROLL | SWT.H_SCROLL*/;
        if ( getWidget().isMultipleSelection() )
        {
            style |= SWT.MULTI;
        }
        else
        {
            style |= SWT.SINGLE;
        }

        // Creating the list
        List list = new List( parent, style );
        list.setLayoutData( getGridata() );

        // Creating the associated viewer
        listViewer = new ListViewer( list );
        listViewer.getList().setEnabled( getWidget().isEnabled() );
        listViewer.setContentProvider( new ArrayContentProvider() );
        listViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                return ( ( ValueItem ) element ).getLabel();
            }
        } );

        listViewer.setInput( getWidget().getItems() );

        return parent;
    }


    // ── UPDATE WIDGET: SELECT THE CURRENT ATTRIBUTE VALUES ───────────────────────
    // We build a map of ValueItems by their value strings, then walk the LDAP
    // attribute's current values and find the matching ValueItems. We highlight
    // those items in the list viewer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current LDAP attribute values and highlights the matching
     * {@link ValueItem}s in the list viewer. Clears the selection if the
     * attribute has no values.
     */
    private void updateWidget()
    {
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.getValueSize() > 0 ) )
        {
            // Registering the values of the items in a map for easy
            // access
            Map<Object, ValueItem> itemsMap = new HashMap<Object, ValueItem>();
            for ( ValueItem valueItem : getWidget().getItems() )
            {
                itemsMap.put( valueItem.getValue(), valueItem );
            }

            // Creating a list of the selected objects
            java.util.List<ValueItem> selectedList = new ArrayList<ValueItem>();

            // Checking each value
            for ( IValue value : attribute.getValues() )
            {
                ValueItem valueItem = itemsMap.get( value.getRawValue() );
                if ( valueItem != null )
                {
                    selectedList.add( valueItem );
                }
            }

            // Setting the selection to the viewer
            if ( selectedList.size() > 0 )
            {
                listViewer.setSelection( new StructuredSelection( selectedList.toArray() ) );
            }
        }
        else
        {
            listViewer.setSelection( null );
        }
    }


    // ── ADD LISTENERS: WIRE THE SELECTION HANDLER ─────────────────────────────────
    /**
     * Attaches the selection changed listener to the list viewer.
     */
    private void addListeners()
    {
        listViewer.addSelectionChangedListener( selectionListener );
    }


    // ── REMOVE LISTENERS: DETACH DURING PROGRAMMATIC UPDATES ─────────────────────
    /**
     * Removes the selection changed listener to prevent feedback loops during
     * programmatic selection updates.
     */
    private void removeListeners()
    {
        listViewer.removeSelectionChangedListener( selectionListener );
    }


    // ── UPDATE: REFRESH WITHOUT TRIGGERING LISTENER ───────────────────────────────
    /**
     * Removes the selection listener, refreshes the selection from the LDAP
     * attribute, then re-attaches the listener.
     */
    public void update()
    {
        removeListeners();
        updateWidget();
        addListeners();
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT List and its viewer are disposed by their parent composite.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
