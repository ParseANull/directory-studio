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
package org.apache.directory.studio.templateeditor.model.widgets;


// ── CLASS: TemplateSection — C-3PO DECODING A GROUPED COMMUNIQUÉ SECTION ─────────
// In the Imperial communiqué, a section directive groups a set of related
// sub-directives under a titled heading. C-3PO notes the title, the optional
// description, how many columns to use inside the section, whether all columns
// must be equal width, and whether the section can be collapsed by the user
// (expandable/expanded flags). Sections map to Eclipse Forms API Section widgets.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template section widget. Sections are titled, collapsible
 * containers that group related widgets under a heading in the editor form.
 * They store a title, an optional description, layout flags (column count and
 * equal-width columns), and expand/collapse state.
 *
 * <p>Think of this as a C-3PO-decoded grouped communiqué section:</p>
 * <pre>
 *   section.setTitle( "Identity" );
 *   section.setNumberOfColumns( 2 );
 *   section.setExpandable( true );
 *   section.setExpanded( true );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateSection extends AbstractTemplateWidget
{
    /** The default number of columns — single-column layout by default. */
    public static int DEFAULT_NUMBER_OF_COLUMNS = 1;

    /** The default equal-columns flag — natural column widths by default. */
    public static boolean DEFAULT_EQUAL_COLUMNS = false;

    /** The default expandable flag — sections are not collapsible by default. */
    public static boolean DEFAULT_EXPANDABLE = false;

    /** The default expanded flag — sections start fully open by default. */
    public static boolean DEFAULT_EXPANDED = true;

    /** The default title — {@code null} means no title is shown. */
    public static String DEFAULT_TITLE = null;

    /** The default description — {@code null} means no description is shown. */
    public static String DEFAULT_DESCRIPTION = null;

    /** The number of columns of the layout */
    private int numberOfColumns = DEFAULT_NUMBER_OF_COLUMNS;

    /** The flag indicating if all columns are equal in width size */
    private boolean equalColumns = DEFAULT_EQUAL_COLUMNS;

    /** The flag indicating if the section is expandable */
    private boolean expandable = DEFAULT_EXPANDABLE;

    /** The flag indicating if the section is expanded */
    private boolean expanded = DEFAULT_EXPANDED;

    /** The title */
    private String title = DEFAULT_TITLE;

    /** The description */
    private String description = DEFAULT_DESCRIPTION;


    // ── CONSTRUCTOR: REGISTER THE SECTION COMMUNIQUÉ ──────────────────────────────
    // C-3PO receives a new grouped directive and files it inside the parent
    // communiqué. The template parser sets title, description, and flags from XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateSection} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (form or composite)
     */
    public TemplateSection( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET DESCRIPTION: THE SECTION'S SUBTITLE TEXT ──────────────────────────────
    // C-3PO reads the description field — an optional paragraph shown below the
    // section title to explain what the group contains.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the optional description text shown below the section's title.
     *
     * @return the description, or {@code null} if not set
     */
    public String getDescription()
    {
        return description;
    }


    // ── GET NUMBER OF COLUMNS: HOW MANY COLUMNS INSIDE THE SECTION ────────────────
    // C-3PO reads the column-count field — the grid layout used inside this section
    // to arrange its child widgets.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of grid columns inside this section's content area.
     *
     * @return column count; defaults to {@code 1}
     */
    public int getNumberOfColumns()
    {
        return numberOfColumns;
    }


    // ── GET TITLE: THE SECTION HEADING ────────────────────────────────────────────
    // C-3PO reads the title field — the bold heading displayed at the top of the
    // section widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the section's title heading text.
     *
     * @return the title, or {@code null} if not set
     */
    public String getTitle()
    {
        return title;
    }


    // ── IS EQUAL COLUMNS: UNIFORM COLUMN WIDTHS ───────────────────────────────────
    // C-3PO checks whether all columns inside the section must be the same width.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if all columns inside this section should be forced to
     * the same width.
     *
     * @return the equal-columns flag; defaults to {@code false}
     */
    public boolean isEqualColumns()
    {
        return equalColumns;
    }


    // ── IS EXPANDABLE: CAN THE SECTION COLLAPSE ───────────────────────────────────
    // C-3PO checks whether the communiqué allows the user to collapse and expand
    // the section by clicking its title bar.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the user can collapse and expand this section by
     * clicking its title bar.
     *
     * @return the expandable flag; defaults to {@code false}
     */
    public boolean isExpandable()
    {
        return expandable;
    }


    // ── IS EXPANDED: CURRENT EXPAND STATE ────────────────────────────────────────
    // C-3PO checks the current expand state — whether the section body is visible.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this section should start in the expanded (open) state.
     * Only meaningful when {@link #isExpandable()} is {@code true}.
     *
     * @return the expanded flag; defaults to {@code true}
     */
    public boolean isExpanded()
    {
        return expanded;
    }


    // ── SET DESCRIPTION ───────────────────────────────────────────────────────────
    /**
     * Sets the optional description text shown below the section title.
     *
     * @param description  the description text, or {@code null} for none
     */
    public void setDescription( String description )
    {
        this.description = description;
    }


    // ── SET EQUAL COLUMNS ─────────────────────────────────────────────────────────
    /**
     * Sets whether all columns inside the section should be forced to equal widths.
     *
     * @param equalColumns  {@code true} for uniform columns; {@code false} for natural widths
     */
    public void setEqualColumns( boolean equalColumns )
    {
        this.equalColumns = equalColumns;
    }


    // ── SET EXPANDABLE ────────────────────────────────────────────────────────────
    /**
     * Sets whether the user can collapse and expand this section.
     *
     * @param expandable  {@code true} to allow collapsing; {@code false} for always-open
     */
    public void setExpandable( boolean expandable )
    {
        this.expandable = expandable;
    }


    // ── SET EXPANDED ──────────────────────────────────────────────────────────────
    /**
     * Sets the initial expand state. Only meaningful when {@link #isExpandable()} is
     * {@code true}.
     *
     * @param expanded  {@code true} to start expanded; {@code false} to start collapsed
     */
    public void setExpanded( boolean expanded )
    {
        this.expanded = expanded;
    }


    // ── SET NUMBER OF COLUMNS ─────────────────────────────────────────────────────
    /**
     * Sets the number of grid columns inside this section's content area.
     *
     * @param numberOfColumns  the column count (must be &gt;= 1)
     */
    public void setNumberOfColumns( int numberOfColumns )
    {
        this.numberOfColumns = numberOfColumns;
    }


    // ── SET TITLE ─────────────────────────────────────────────────────────────────
    /**
     * Sets the section's title heading text.
     *
     * @param title  the title text, or {@code null} for no title
     */
    public void setTitle( String title )
    {
        this.title = title;
    }
}
