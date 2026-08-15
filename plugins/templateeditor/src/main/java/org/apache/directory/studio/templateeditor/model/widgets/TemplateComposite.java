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


// ── CLASS: TemplateComposite — C-3PO DECODING A GRID-LAYOUT COMMUNIQUÉ ───────────
// In the Imperial communiqué, a composite directive tells C-3PO to arrange a group
// of nested sub-directives in a grid of columns. He notes how many columns to use
// and whether all columns must be the same width. The rest is layout-only: no
// direct LDAP attribute binding.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template composite widget. A composite is a simple container
 * that arranges its children in a grid layout with a configurable number of columns.
 * It has no LDAP attribute binding of its own — its purpose is purely layout.
 *
 * <p>Think of this as a C-3PO-decoded grid-layout directive:</p>
 * <pre>
 *   composite.setNumberOfColumns( 3 );
 *   composite.setEqualColumns( true );
 *   // child widgets are nested inside during parsing
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateComposite extends AbstractTemplateWidget
{
    /** The default number of columns — a single-column layout unless overridden. */
    public static int DEFAULT_NUMBER_OF_COLUMNS = 1;

    /** The default equal-columns flag — columns have natural widths by default. */
    public static boolean DEFAULT_EQUAL_COLUMNS = false;

    /** The number of columns of the layout */
    private int numberOfColumns = DEFAULT_NUMBER_OF_COLUMNS;

    /** The flag indicating if all columns are equal in width size */
    private boolean equalColumns = DEFAULT_EQUAL_COLUMNS;


    // ── CONSTRUCTOR: REGISTER THE COMPOSITE COMMUNIQUÉ ───────────────────────────
    // C-3PO receives a new grid-layout directive and files it inside the parent
    // communiqué. Child widgets are added later during XML parsing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateComposite} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (section, form, or another composite)
     */
    public TemplateComposite( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET NUMBER OF COLUMNS: HOW MANY COLUMNS IN THE GRID ──────────────────────
    // C-3PO reads the column-count field from the communiqué — this is the number
    // of side-by-side cells the child widgets are arranged into.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of columns in this composite's grid layout.
     *
     * @return column count; defaults to {@code 1}
     */
    public int getNumberOfColumns()
    {
        return numberOfColumns;
    }


    // ── IS EQUAL COLUMNS: ARE ALL COLUMNS THE SAME WIDTH ─────────────────────────
    // C-3PO checks whether the communiqué mandates uniform column widths. When
     // true, SWT makes every column as wide as the widest cell in the grid.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if all columns should be forced to the same width,
     * {@code false} to allow natural (content-driven) column widths.
     *
     * @return the equal-columns flag; defaults to {@code false}
     */
    public boolean isEqualColumns()
    {
        return equalColumns;
    }


    // ── SET EQUAL COLUMNS ─────────────────────────────────────────────────────────
    /**
     * Sets whether all columns in the grid should be forced to an equal width.
     *
     * @param equalColumns  {@code true} for uniform columns; {@code false} for natural widths
     */
    public void setEqualColumns( boolean equalColumns )
    {
        this.equalColumns = equalColumns;
    }


    // ── SET NUMBER OF COLUMNS ─────────────────────────────────────────────────────
    /**
     * Sets the number of columns for this composite's grid layout.
     *
     * @param numberOfColumns  the column count (must be &gt;= 1)
     */
    public void setNumberOfColumns( int numberOfColumns )
    {
        this.numberOfColumns = numberOfColumns;
    }
}
