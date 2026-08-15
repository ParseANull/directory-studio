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


import java.util.List;


// ── INTERFACE: TemplateWidget — C-3PO'S COMMUNIQUÉ CONTRACT ──────────────────────
// In the Imperial communications centre, every communiqué — whether it describes
// a checkbox, a label, or a full section — must follow the same Imperial protocol.
// C-3PO insists on it: every message specifies its position, its span, its size,
// and which attribute it concerns. This interface is that protocol: the contract
// every template widget model object must satisfy so that the editor and the XML
// parser know how to handle any widget uniformly.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Contract for all template widget model objects. A template widget is a data
 * object parsed from the template XML that describes one UI control — a checkbox,
 * a text field, a section, etc. — including its layout constraints (alignment,
 * span, size) and the LDAP attribute type it edits.
 *
 * <p>Think of each widget as a C-3PO-decoded Imperial communiqué that specifies
 * exactly how and where a control should appear:</p>
 * <pre>
 *   communiqué.setHorizontalAlignment(FILL);
 *   communiqué.setHorizontalSpan(2);
 *   communiqué.setAttributeType("cn");
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface TemplateWidget
{
    /** The default horizontal alignment — no explicit alignment specified. */
    public static WidgetAlignment DEFAULT_HORIZONTAL_ALIGNMENT = WidgetAlignment.NONE;

    /** The default vertical alignment — no explicit alignment specified. */
    WidgetAlignment DEFAULT_VERTICAL_ALIGNMENT = WidgetAlignment.NONE;

    /** The default value for grab-excess-horizontal-space — don't grab by default. */
    boolean DEFAULT_GRAB_EXCESS_HORIZONTAL_SPACE = false;

    /** The default value for grab-excess-vertical-space — don't grab by default. */
    boolean DEFAULT_GRAB_EXCESS_VERTICAL_SPACE = false;

    /** The default number of grid columns the widget occupies — just one. */
    int DEFAULT_HORIZONTAL_SPAN = 1;

    /** The default number of grid rows the widget occupies — just one. */
    int DEFAULT_VERTICAL_SPAN = 1;

    /** Sentinel value meaning "no preferred size set" — {@code -1} tells SWT to use its own default. */
    int DEFAULT_SIZE = -1;


    // ── ADD CHILD: ATTACH A NESTED WIDGET ────────────────────────────────────────
    // C-3PO receives a nested communiqué — a child directive inside the parent
    // message — and appends it to the parent's list of sub-messages.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a child widget to this widget's children list. Container widgets
     * (composites, sections, the form itself) use this to build the widget tree.
     *
     * @param widget  the child widget to add
     * @return {@code true} as per {@link java.util.Collection#add(Object)}
     */
    boolean addChild( TemplateWidget widget );


    // ── GET ATTRIBUTE TYPE: WHICH LDAP ATTRIBUTE THIS WIDGET EDITS ───────────────
    // C-3PO reads the communiqué header: "this directive concerns attribute 'cn'."
    // We need to know which LDAP attribute to read from and write back to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name of the LDAP attribute type this widget is bound to. The
     * editor reads the attribute's current value from the LDAP entry and displays
     * it; when the user edits, the new value is written back to the same attribute.
     *
     * @return the attribute type name (e.g. {@code "cn"}, {@code "mail"}), or
     *         {@code null} for layout-only widgets like labels or composites
     */
    String getAttributeType();


    // ── GET CHILDREN: RETRIEVE ALL NESTED WIDGETS ────────────────────────────────
    // C-3PO returns the full list of sub-communiqués nested inside this one —
    // used by container widgets (composites, sections) to iterate their children.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of child widgets nested inside this widget. Only container
     * widgets (composites, sections, the form) normally have children; leaf widgets
     * return an empty list.
     *
     * @return the (possibly empty) list of child {@link TemplateWidget}s
     */
    List<TemplateWidget> getChildren();


    // ── GET IMAGE HEIGHT: RETRIEVE THE PREFERRED HEIGHT ───────────────────────────
    // C-3PO checks the communiqué's height specification — only meaningful for
    // image widgets; other widgets default to {@code DEFAULT_SIZE} (-1).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the preferred height for this widget in pixels, or {@link #DEFAULT_SIZE}
     * ({@code -1}) if no explicit height is set. Primarily used by image widgets.
     *
     * @return preferred height in pixels, or {@code -1} for SWT default
     */
    int getImageHeight();


    // ── GET HORIZONTAL ALIGNMENT: HOW THE WIDGET SITS HORIZONTALLY ───────────────
    // C-3PO reads the horizontal-positioning directive from the communiqué and
    // returns it so the editor widget can set up its SWT GridData accordingly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this widget's horizontal alignment within its grid cell.
     *
     * @return one of the {@link WidgetAlignment} values; defaults to
     *         {@link WidgetAlignment#NONE}
     */
    WidgetAlignment getHorizontalAlignment();


    // ── GET HORIZONTAL SPAN: HOW MANY COLUMNS THE WIDGET OCCUPIES ────────────────
    // C-3PO reads the column-span count — how many grid columns this communiqué
    // element should occupy. Wide controls (like a text area) might span 2 or more.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of grid columns this widget should occupy.
     *
     * @return column span; defaults to {@link #DEFAULT_HORIZONTAL_SPAN} (1)
     */
    int getHorizontalSpan();


    // ── GET PARENT: RETRIEVE THE ENCLOSING WIDGET ────────────────────────────────
    // C-3PO traces the communiqué back to its enclosing message — the parent
    // widget that contains this one in the widget tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent widget that contains this widget in the template tree.
     * The root form widget returns {@code null} here because it has no parent.
     *
     * @return the parent widget, or {@code null} for the root form
     */
    TemplateWidget getParent();


    // ── GET VERTICAL ALIGNMENT: HOW THE WIDGET SITS VERTICALLY ───────────────────
    // C-3PO reads the vertical-positioning directive — top, centre, bottom, or fill.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this widget's vertical alignment within its grid cell.
     *
     * @return one of the {@link WidgetAlignment} values; defaults to
     *         {@link WidgetAlignment#NONE}
     */
    WidgetAlignment getVerticalAlignment();


    // ── GET VERTICAL SPAN: HOW MANY ROWS THE WIDGET OCCUPIES ─────────────────────
    // C-3PO reads the row-span count — most widgets occupy exactly one row, but
    // tall multi-line controls may span more.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of grid rows this widget should occupy.
     *
     * @return row span; defaults to {@link #DEFAULT_VERTICAL_SPAN} (1)
     */
    int getVerticalSpan();


    // ── GET IMAGE WIDTH: RETRIEVE THE PREFERRED WIDTH ─────────────────────────────
    // C-3PO checks the width specification from the communiqué — again, primarily
    // meaningful for image widgets.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the preferred width for this widget in pixels, or {@link #DEFAULT_SIZE}
     * ({@code -1}) if no explicit width is set. Primarily used by image widgets.
     *
     * @return preferred width in pixels, or {@code -1} for SWT default
     */
    int getImageWidth();


    // ── HAS CHILDREN: CHECK FOR NESTED WIDGETS ───────────────────────────────────
    // A quick check: does this communiqué contain any sub-messages? If yes,
    // the widget is a container; if no, it's a leaf.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this widget has at least one child widget.
     *
     * @return {@code true} if children are present, {@code false} otherwise
     */
    boolean hasChildren();


    // ── IS GRAB EXCESS HORIZONTAL SPACE: CONSUME LEFTOVER WIDTH ─────────────────
    // C-3PO checks whether the communiqué says "fill the rest of the row" — if so,
    // SWT will make this widget as wide as the remaining horizontal space.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this widget should stretch horizontally to consume
     * any leftover space in its row. Useful for text fields and tables that should
     * always use all available width.
     *
     * @return {@code true} to grab excess horizontal space; defaults to {@code false}
     */
    boolean isGrabExcessHorizontalSpace();


    // ── IS GRAB EXCESS VERTICAL SPACE: CONSUME LEFTOVER HEIGHT ──────────────────
    // C-3PO checks whether the communiqué says "fill the rest of the column" —
    // used for widgets that should grow taller when the window is resized.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this widget should stretch vertically to consume
     * any leftover space in its column.
     *
     * @return {@code true} to grab excess vertical space; defaults to {@code false}
     */
    boolean isGrabExcessVerticalSpace();


    // ── SET ATTRIBUTE TYPE: BIND TO AN LDAP ATTRIBUTE ────────────────────────────
    // C-3PO stamps the communiqué header: "this directive concerns attribute X."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP attribute type this widget is bound to.
     *
     * @param attributeType  the attribute type name (e.g. {@code "sn"}, {@code "mail"})
     */
    void setAttributeType( String attributeType );


    // ── SET GRAB EXCESS HORIZONTAL SPACE ─────────────────────────────────────────
    /**
     * Sets whether this widget should stretch to consume leftover horizontal space.
     *
     * @param grabExcessHorizontalSpace  {@code true} to grab excess horizontal space
     */
    void setGrabExcessHorizontalSpace( boolean grabExcessHorizontalSpace );


    // ── SET GRAB EXCESS VERTICAL SPACE ───────────────────────────────────────────
    /**
     * Sets whether this widget should stretch to consume leftover vertical space.
     *
     * @param grabExcessVerticalSpace  {@code true} to grab excess vertical space
     */
    void setGrabExcessVerticalSpace( boolean grabExcessVerticalSpace );


    // ── SET IMAGE HEIGHT ──────────────────────────────────────────────────────────
    /**
     * Sets the preferred height for this widget. Use {@link #DEFAULT_SIZE} ({@code -1})
     * to leave the height at the SWT default.
     *
     * @param height  preferred height in pixels, or {@code -1} for no preference
     */
    void setImageHeight( int height );


    // ── SET HORIZONTAL ALIGNMENT ──────────────────────────────────────────────────
    /**
     * Sets how this widget is positioned horizontally within its grid cell.
     *
     * @param horizontalAlignment  one of the {@link WidgetAlignment} values
     */
    void setHorizontalAlignment( WidgetAlignment horizontalAlignment );


    // ── SET HORIZONTAL SPAN ───────────────────────────────────────────────────────
    /**
     * Sets the number of grid columns this widget should occupy.
     *
     * @param horizontalSpan  column count (must be &gt;= 1)
     */
    void setHorizontalSpan( int horizontalSpan );


    // ── SET VERTICAL ALIGNMENT ────────────────────────────────────────────────────
    /**
     * Sets how this widget is positioned vertically within its grid cell.
     *
     * @param verticalAlignment  one of the {@link WidgetAlignment} values
     */
    void setVerticalAlignment( WidgetAlignment verticalAlignment );


    // ── SET VERTICAL SPAN ─────────────────────────────────────────────────────────
    /**
     * Sets the number of grid rows this widget should occupy.
     *
     * @param verticalSpan  row count (must be &gt;= 1)
     */
    void setVerticalSpan( int verticalSpan );


    // ── SET IMAGE WIDTH ───────────────────────────────────────────────────────────
    /**
     * Sets the preferred width for this widget. Use {@link #DEFAULT_SIZE} ({@code -1})
     * to leave the width at the SWT default.
     *
     * @param width  preferred width in pixels, or {@code -1} for no preference
     */
    void setImageWidth( int width );
}
