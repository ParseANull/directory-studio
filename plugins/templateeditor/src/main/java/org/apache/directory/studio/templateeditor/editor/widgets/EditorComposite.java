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


import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateComposite;


// ── CLASS: EditorComposite — THE TANTIVE IV INSTRUMENT BAY FRAME ─────────────────
// On the Tantive IV, instrument bays are metal frames that group related panels
// together — navigation instruments here, life-support controls there. The frame
// itself has no LDAP attribute; it just arranges child panels in a grid. This class
// is that frame: it creates a GridLayout composite that child EditorWidgets are
// mounted inside. The number of columns and equal-column settings come from the
// template model.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A container editor widget that renders as a plain SWT {@link Composite} with a
 * {@link GridLayout}. It doesn't bind to any LDAP attribute itself — its job is to
 * group child widgets into a multi-column grid. The column count and equal-column
 * flag come from the associated {@link TemplateComposite} model.
 * Think of this as the instrument bay frame on the Tantive IV — a structural
 * container, not a control in its own right.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorComposite extends EditorWidget<TemplateComposite>
{
    // ── CONSTRUCTOR: ASSEMBLE THE FRAME ──────────────────────────────────────────
    // The technician bolts the frame into place. The composite holds no attribute
    // data; it just provides a structured mounting surface for child widgets.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorComposite} that will render as a multi-column grid.
     * The column count and equal-column flag are read from {@code templateComposite}.
     *
     * <p>For example — assembling the instrument bay frame:</p>
     * <pre>
     *   new EditorComposite(editor, twoColumnComposite, toolkit);
     *   // "Frame assembled. Ready for 2-column child layout."
     * </pre>
     *
     * @param editor             the owning entry editor
     * @param templateComposite  the template model for this composite
     * @param toolkit            the form toolkit used to create the SWT composite
     */
    public EditorComposite( IEntryEditor editor, TemplateComposite templateComposite, FormToolkit toolkit )
    {
        super( templateComposite, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE GRID FRAME ──────────────────────────────────────
    // We create a plain SWT composite with a GridLayout configured per the template
    // model's numberOfColumns and equalColumns settings, then return it so the
    // TemplateEditorWidget can mount child widgets inside it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link Composite} with a {@link GridLayout} whose column count and
     * equal-column flag come from the template model. Returns this composite so the
     * template editor widget can place child widgets inside it.
     *
     * <p>For example — building the grid frame:</p>
     * <pre>
     *   Composite composite = toolkit.createComposite(parent);
     *   composite.setLayout(new GridLayout(2, false)); // 2-column grid
     *   return composite; // "Children go in here."
     * </pre>
     *
     * @param parent  the parent composite to build inside
     * @return a new composite that child widgets are placed into
     */
    public Composite createWidget( Composite parent )
    {
        Composite composite = getToolkit().createComposite( parent );
        composite.setLayout( new GridLayout( getWidget().getNumberOfColumns(), getWidget().isEqualColumns() ) );
        composite.setLayoutData( getGridata() );

        return composite;
    }


    // ── UPDATE: NO DATA TO REFRESH ────────────────────────────────────────────────
    /**
     * No-op — composites don't bind to LDAP attributes and have no data to refresh.
     */
    public void update()
    {
        // Nothing to do
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT composite is owned by its parent and disposed with it.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
