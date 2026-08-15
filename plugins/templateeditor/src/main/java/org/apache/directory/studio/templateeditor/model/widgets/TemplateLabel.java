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


// ── CLASS: TemplateLabel — C-3PO DECODING A STATIC-TEXT COMMUNIQUÉ ───────────────
// In the Imperial communiqué, a label directive is the simplest kind: pure display
// text with no editing interaction. C-3PO reads the value string, how many rows it
// should occupy, and whether a dollar sign in the value should be rendered as a
// newline — a compact convention for multi-line labels in the XML.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template label widget. Stores the static display text,
 * the number of rows it should occupy, and whether a {@code $} character in the
 * value should be interpreted as a newline (a compact multi-line convention).
 * Labels may also bind to an LDAP attribute, in which case the live attribute
 * value is displayed instead of the static text.
 *
 * <p>Think of this as a C-3PO-decoded static-text communiqué directive:</p>
 * <pre>
 *   label.setValue( "Full Name:$Department:" ); // "$" becomes newline
 *   label.setDollarSignIsNewLine( true );
 *   label.setNumberOfRows( 2 );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateLabel extends AbstractTemplateWidget
{
    /** The default value — {@code null} means no static text; use the LDAP attribute value. */
    public static String DEFAULT_VALUE = null;

    /** The default number of display rows — single-line by default. */
    public static int DEFAULT_NUMBER_OF_ROWS = 1;

    /** The default dollar-sign-is-newline flag — {@code $} is a literal character by default. */
    public static boolean DEFAULT_DOLLAR_SIGN_IS_NEW_LINE = false;

    /** The label value */
    private String value = DEFAULT_VALUE;

    /** The number of rows */
    private int numberOfRows = DEFAULT_NUMBER_OF_ROWS;

    /** The flag which indicates if dollar sign ('$') is to be interpreted as a new line */
    private boolean dollarSignIsNewLine = DEFAULT_DOLLAR_SIGN_IS_NEW_LINE;


    // ── CONSTRUCTOR: REGISTER THE LABEL COMMUNIQUÉ ───────────────────────────────
    // C-3PO receives a new static-text directive and files it inside the parent
    // communiqué. The template parser sets the value, rows, and newline flag from
    // the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateLabel} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateLabel( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET NUMBER OF ROWS: HOW MANY LINES THE LABEL OCCUPIES ────────────────────
    // C-3PO reads the row-count field — a multi-line label needs more vertical space
    // so the layout can reserve enough room for it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of display rows this label should occupy. Multi-line
     * labels need a value greater than {@code 1} so the layout reserves enough
     * vertical space.
     *
     * @return row count; defaults to {@code 1}
     */
    public int getNumberOfRows()
    {
        return numberOfRows;
    }


    // ── GET VALUE: THE STATIC TEXT TO DISPLAY ────────────────────────────────────
    // C-3PO reads the static text from the communiqué. If this is null, the editor
    // falls back to displaying the live LDAP attribute value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the static text string to display, or {@code null} if the label
     * should display the bound LDAP attribute's live value.
     *
     * @return the static value string, or {@code null}
     */
    public String getValue()
    {
        return value;
    }


    // ── IS DOLLAR SIGN IS NEW LINE: THE MULTI-LINE CONVENTION ────────────────────
    // C-3PO checks whether the communiqué uses the compact convention where a
    // dollar sign in the value string means "start a new line here."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if a {@code $} character in the value string should be
     * rendered as a newline, enabling compact multi-line labels in the XML.
     *
     * @return the dollar-sign-is-newline flag; defaults to {@code false}
     */
    public boolean isDollarSignIsNewLine()
    {
        return dollarSignIsNewLine;
    }


    // ── SET DOLLAR SIGN IS NEW LINE ───────────────────────────────────────────────
    /**
     * Sets whether {@code $} in the value string is treated as a newline character.
     *
     * @param dollarSignIsNewLine  {@code true} to treat {@code $} as newline; {@code false} for literal
     */
    public void setDollarSignIsNewLine( boolean dollarSignIsNewLine )
    {
        this.dollarSignIsNewLine = dollarSignIsNewLine;
    }


    // ── SET NUMBER OF ROWS ────────────────────────────────────────────────────────
    /**
     * Sets the number of display rows this label should occupy.
     *
     * @param numberOfRows  row count (must be &gt;= 1)
     */
    public void setNumberOfRows( int numberOfRows )
    {
        this.numberOfRows = numberOfRows;
    }


    // ── SET VALUE ─────────────────────────────────────────────────────────────────
    /**
     * Sets the static text to display. Pass {@code null} to display the bound
     * LDAP attribute's live value instead.
     *
     * @param value  the static label text, or {@code null}
     */
    public void setValue( String value )
    {
        this.value = value;
    }
}
