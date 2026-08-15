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


import java.util.ArrayList;
import java.util.List;


// ── CLASS: AbstractTemplateWidget — C-3PO'S COMMUNIQUÉ BASE DECODER ──────────────
// Every Imperial communiqué C-3PO handles follows the same structure: a parent
// communiqué, a list of nested sub-directives, positioning codes, span counts,
// size hints, and the LDAP attribute it concerns. This abstract class captures
// all of that shared structure so every concrete widget type (checkbox, text field,
// spinner, etc.) only needs to add its own specific fields on top.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all template widget model objects. Provides storage and
 * accessors for the layout properties shared by every widget: alignment, span,
 * size hints, grab-excess flags, the parent-child relationship, and the bound
 * LDAP attribute type. Concrete subclasses add widget-specific fields.
 *
 * <p>Think of this as C-3PO's base communiqué decoder — every message type uses
 * the same header fields, even if the body differs per type:</p>
 * <pre>
 *   AbstractTemplateWidget widget = new TemplateCheckbox( parentWidget );
 *   widget.setAttributeType( "sn" );
 *   widget.setHorizontalAlignment( WidgetAlignment.FILL );
 *   widget.setGrabExcessHorizontalSpace( true );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractTemplateWidget implements TemplateWidget
{
    /** The parent element*/
    private TemplateWidget parent;

    /** The children list */
    private List<TemplateWidget> children;

    /** The attribute type the widget is associated with */
    private String attributeType;

    /** How the widget is positioned horizontally */
    private WidgetAlignment horizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT;

    /** How the widget is positioned vertically */
    private WidgetAlignment verticalAlignment = DEFAULT_VERTICAL_ALIGNMENT;

    /**
     * The flag to know whether the widget will be made wide
     * enough to fit the remaining horizontal space.
     */
    private boolean grabExcessHorizontalSpace = DEFAULT_GRAB_EXCESS_HORIZONTAL_SPACE;

    /**
     * The flag to know whether the widget will be made wide
     * enough to fit the remaining vertical space.
     */
    private boolean grabExcessVerticalSpace = DEFAULT_GRAB_EXCESS_VERTICAL_SPACE;

    /** The number of columns that the widget will take up */
    private int horizontalSpan = DEFAULT_HORIZONTAL_SPAN;

    /** The number of rows that the widget will take up */
    private int verticalSpan = DEFAULT_VERTICAL_SPAN;

    /** The preferred width */
    private int width = DEFAULT_SIZE;

    /** The preferred height*/
    private int height = DEFAULT_SIZE;


    // ── CONSTRUCTOR: REGISTER WITH THE PARENT COMMUNIQUÉ ─────────────────────────
    // C-3PO receives a new sub-communiqué and immediately files it inside the parent
    // message's list of nested directives. That automatic registration keeps the
    // widget tree consistent without callers having to remember to call addChild().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new widget and registers it as a child of the given parent. If
     * {@code parent} is non-null we call {@code parent.addChild(this)} so the
     * widget tree stays consistent automatically.
     *
     * @param parent  the enclosing widget, or {@code null} for the root form widget
     */
    public AbstractTemplateWidget( TemplateWidget parent )
    {
        this.parent = parent;
        children = new ArrayList<TemplateWidget>();

        if ( parent != null )
        {
            parent.addChild( this );
        }
    }


    // ── ADD CHILD: APPEND A NESTED WIDGET ────────────────────────────────────────
    // C-3PO files a new sub-communiqué at the end of this message's nested list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean addChild( TemplateWidget widget )
    {
        return children.add( widget );
    }


    // ── GET ATTRIBUTE TYPE: WHICH LDAP ATTRIBUTE THIS WIDGET EDITS ───────────────
    // C-3PO reads the communiqué header to find the LDAP attribute this directive
    // is about.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getAttributeType()
    {
        return attributeType;
    }


    // ── GET CHILDREN: RETRIEVE ALL NESTED WIDGETS ────────────────────────────────
    // C-3PO returns the full list of sub-communiqués nested inside this message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public List<TemplateWidget> getChildren()
    {
        return children;
    }


    // ── GET IMAGE HEIGHT: RETRIEVE THE PREFERRED HEIGHT ───────────────────────────
    // C-3PO reads the height specification from the communiqué.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int getImageHeight()
    {
        return height;
    }


    // ── GET HORIZONTAL ALIGNMENT ──────────────────────────────────────────────────
    // C-3PO reads the horizontal-positioning directive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public WidgetAlignment getHorizontalAlignment()
    {
        return horizontalAlignment;
    }


    // ── GET HORIZONTAL SPAN ───────────────────────────────────────────────────────
    // C-3PO reads how many grid columns this element occupies.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int getHorizontalSpan()
    {
        return horizontalSpan;
    }


    // ── GET PARENT: RETRIEVE THE ENCLOSING WIDGET ────────────────────────────────
    // C-3PO traces the sub-communiqué back to its enclosing parent message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public TemplateWidget getParent()
    {
        return parent;
    }


    // ── GET VERTICAL ALIGNMENT ────────────────────────────────────────────────────
    // C-3PO reads the vertical-positioning directive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public WidgetAlignment getVerticalAlignment()
    {
        return verticalAlignment;
    }


    // ── GET VERTICAL SPAN ─────────────────────────────────────────────────────────
    // C-3PO reads how many grid rows this element occupies.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int getVerticalSpan()
    {
        return verticalSpan;
    }


    // ── GET IMAGE WIDTH: RETRIEVE THE PREFERRED WIDTH ─────────────────────────────
    // C-3PO reads the width specification from the communiqué.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int getImageWidth()
    {
        return width;
    }


    // ── HAS CHILDREN: CHECK FOR NESTED WIDGETS ───────────────────────────────────
    // C-3PO checks whether this communiqué contains any sub-messages.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean hasChildren()
    {
        return children.size() > 0;
    }


    // ── IS GRAB EXCESS HORIZONTAL SPACE ──────────────────────────────────────────
    // C-3PO checks the "expand horizontally" flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isGrabExcessHorizontalSpace()
    {
        return grabExcessHorizontalSpace;
    }


    // ── IS GRAB EXCESS VERTICAL SPACE ────────────────────────────────────────────
    // C-3PO checks the "expand vertically" flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isGrabExcessVerticalSpace()
    {
        return grabExcessVerticalSpace;
    }


    // ── SET ATTRIBUTE TYPE ────────────────────────────────────────────────────────
    // C-3PO stamps the communiqué header with the LDAP attribute name.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setAttributeType( String attributeType )
    {
        this.attributeType = attributeType;
    }


    // ── SET GRAB EXCESS HORIZONTAL SPACE ─────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setGrabExcessHorizontalSpace( boolean grabExcessHorizontalSpace )
    {
        this.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
    }


    // ── SET GRAB EXCESS VERTICAL SPACE ───────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setGrabExcessVerticalSpace( boolean grabExcessVerticalSpace )
    {
        this.grabExcessVerticalSpace = grabExcessVerticalSpace;
    }


    // ── SET IMAGE HEIGHT ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setImageHeight( int height )
    {
        this.height = height;
    }


    // ── SET HORIZONTAL ALIGNMENT ──────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setHorizontalAlignment( WidgetAlignment horizontalAlignment )
    {
        this.horizontalAlignment = horizontalAlignment;
    }


    // ── SET HORIZONTAL SPAN ───────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setHorizontalSpan( int horizontalSpan )
    {
        this.horizontalSpan = horizontalSpan;
    }


    // ── SET VERTICAL ALIGNMENT ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setVerticalAlignment( WidgetAlignment verticalAlignment )
    {
        this.verticalAlignment = verticalAlignment;
    }


    // ── SET VERTICAL SPAN ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setVerticalSpan( int verticalSpan )
    {
        this.verticalSpan = verticalSpan;
    }


    // ── SET IMAGE WIDTH ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setImageWidth( int width )
    {
        this.width = width;
    }
}
