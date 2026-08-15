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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.apache.directory.studio.templateeditor.model.widgets.TemplateSection;


// ── CLASS: EditorSection — THE TANTIVE IV LABELED INSTRUMENT PANEL COVER ─────────
// On the Tantive IV, major instrument groups have a labeled cover that can be
// opened or latched shut — "Navigation Systems" or "Hyperdrive Controls" stamped on
// the top, with a twistie arrow to expand or collapse the contents. Eclipse Forms
// provides exactly this with its {@link Section} widget: a collapsible panel with
// a title bar, optional description, and a grid of child controls inside. This class
// wraps that Section and configures it from the template model's settings.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A container editor widget that renders as an Eclipse Forms {@link Section} —
 * a titled, optionally collapsible panel that groups child widgets visually.
 * The section's title, description, expandable/expanded flags, and column count
 * all come from the associated {@link TemplateSection} model. Like
 * {@link EditorComposite}, it doesn't bind to an LDAP attribute directly; it
 * just provides structure.
 * Think of this as the labeled instrument panel cover on the Tantive IV.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorSection extends EditorWidget<TemplateSection>
{
    // ── CONSTRUCTOR: BOLT THE PANEL COVER ON ─────────────────────────────────────
    // The technician bolts the labeled panel cover into place. The section provides
    // the title bar and optional expand/collapse twistie for the child controls below.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorSection} that will render as a Forms {@link Section}.
     * The section's title, description, and collapse settings are read from
     * {@code templateSection}.
     *
     * <p>For example — bolting the panel cover on:</p>
     * <pre>
     *   new EditorSection(editor, personalInfoSection, toolkit);
     *   // "Section cover installed. Title: 'Personal Information'."
     * </pre>
     *
     * @param editor           the owning entry editor
     * @param templateSection  the template model for this section
     * @param toolkit          the form toolkit used to create the SWT section
     */
    public EditorSection( IEntryEditor editor, TemplateSection templateSection, FormToolkit toolkit )
    {
        super( templateSection, editor, toolkit );
    }


    // ── CREATE WIDGET: BUILD THE LABELED PANEL COVER ──────────────────────────────
    // We calculate the Section style from the template flags (title bar, description,
    // twistie, expanded), create the Section, set its title and description, and
    // create the client composite inside it. The client composite is what we return —
    // child widgets go inside it, not directly in the Section itself.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a Forms {@link Section} with a title bar, optional description, and
     * optional twistie (collapse toggle). Returns the client composite inside the
     * section — that's where child widgets are placed.
     *
     * <p>For example — building the labeled panel cover:</p>
     * <pre>
     *   Section section = toolkit.createSection(parent, TITLE_BAR | TWISTIE | EXPANDED);
     *   Composite clientComposite = toolkit.createComposite(section);
     *   section.setClient(clientComposite);
     *   return clientComposite; // "Child widgets go in here."
     * </pre>
     *
     * @param parent  the parent composite to build inside
     * @return the client composite inside the section (child widgets go here)
     */
    public Composite createWidget( Composite parent )
    {
        // Calculating the style
        int style = Section.TITLE_BAR;
        if ( getWidget().getDescription() != null )
        {
            style |= Section.DESCRIPTION;
        }
        if ( getWidget().isExpandable() )
        {
            style |= Section.TWISTIE;
        }
        if ( getWidget().isExpanded() )
        {
            style |= Section.EXPANDED;
        }

        // Creating the section
        Section section = getToolkit().createSection( parent, style );
        section.setLayoutData( getGridata() );

        // Creating the client composite
        Composite clientComposite = getToolkit().createComposite( section );
        section.setClient( clientComposite );

        // Setting the layout for the client composite
        clientComposite.setLayout( new GridLayout( getWidget().getNumberOfColumns(), getWidget().isEqualColumns() ) );
        clientComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Title
        if ( ( getWidget().getTitle() != null ) && ( !"".equals( getWidget().getTitle() ) ) ) //$NON-NLS-1$
        {
            section.setText( getWidget().getTitle() );
        }

        // Description
        if ( ( getWidget().getDescription() != null ) && ( !"".equals( getWidget().getDescription() ) ) ) //$NON-NLS-1$
        {
            section.setDescription( getWidget().getDescription() );
        }

        return clientComposite;
    }


    // ── UPDATE: NO DATA TO REFRESH ────────────────────────────────────────────────
    /**
     * No-op — sections don't bind to LDAP attributes and have no data to refresh.
     */
    public void update()
    {
        // Nothing to do
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT section and client composite are disposed by their parent.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
