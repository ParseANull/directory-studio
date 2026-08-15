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


// ── CLASS: TemplateTextField — C-3PO DECODING A FREE-TEXT COMMUNIQUÉ ─────────────
// In the Imperial communiqué, a text-field directive is the most common kind: a
// free-form text entry for an LDAP attribute. C-3PO reads three settings: how
// many rows the field should occupy (1 = single-line, > 1 = multi-line), an
// optional maximum character limit, and whether a dollar sign in the initial value
// should be treated as a newline — the same compact convention used by TemplateLabel.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template text-field widget. Stores the number of display rows
 * (single-line vs. multi-line), an optional maximum character limit, and the
 * dollar-sign-is-newline flag for compact multi-line initial values in the XML.
 *
 * <p>Think of this as a C-3PO-decoded free-text communiqué directive:</p>
 * <pre>
 *   textField.setNumberOfRows( 3 );
 *   textField.setCharactersLimit( 256 );
 *   textField.setDollarSignIsNewLine( false );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateTextField extends AbstractTemplateWidget
{
    /** The default number of rows — single-line text field unless overridden. */
    public static int DEFAULT_NUMBER_OF_ROWS = 1;

    /** The default character limit — {@code -1} means no limit enforced. */
    public static int DEFAULT_CHARACTERS_LIMIT = -1;

    /** The default dollar-sign-is-newline flag — {@code $} is a literal character. */
    public static boolean DEFAULT_DOLLAR_SIGN_IS_NEW_LINE = false;

    /** The number of rows */
    private int numberOfRows = DEFAULT_NUMBER_OF_ROWS;

    /** The characters limit */
    private int charactersLimit = DEFAULT_CHARACTERS_LIMIT;

    /** The flag which indicates if dollar sign ('$') is to be interpreted as a new line */
    private boolean dollarSignIsNewLine = DEFAULT_DOLLAR_SIGN_IS_NEW_LINE;


    // ── CONSTRUCTOR: REGISTER THE TEXT-FIELD COMMUNIQUÉ ──────────────────────────
    // C-3PO receives a new free-text directive and files it inside the parent
    // communiqué. The template parser sets row count, character limit, and the
    // dollar-sign flag from the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateTextField} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateTextField( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET CHARACTERS LIMIT: THE MAX INPUT LENGTH ────────────────────────────────
    // C-3PO reads the character-limit field from the communiqué — if set, the SWT
    // Text widget will refuse to accept more characters than this.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of characters the user may enter, or
     * {@link #DEFAULT_CHARACTERS_LIMIT} ({@code -1}) if no limit is imposed.
     *
     * @return the character limit, or {@code -1} for unlimited input
     */
    public int getCharactersLimit()
    {
        return charactersLimit;
    }


    // ── GET NUMBER OF ROWS: SINGLE OR MULTI-LINE ──────────────────────────────────
    // C-3PO reads the row-count field — a value greater than 1 causes the editor
    // to render a multi-line SWT Text widget (SWT.MULTI | SWT.WRAP).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of rows the text field should display. {@code 1} means a
     * standard single-line input; greater values produce a multi-line editor.
     *
     * @return row count; defaults to {@code 1}
     */
    public int getNumberOfRows()
    {
        return numberOfRows;
    }


    // ── IS DOLLAR SIGN IS NEW LINE: THE MULTI-LINE CONVENTION ────────────────────
    // C-3PO checks whether the communiqué uses the compact convention where a
    // dollar sign in the initial value means "start a new line here."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if a {@code $} character in the field's initial value
     * should be rendered as a newline — the same compact convention used in
     * {@link TemplateLabel}.
     *
     * @return the dollar-sign-is-newline flag; defaults to {@code false}
     */
    public boolean isDollarSignIsNewLine()
    {
        return dollarSignIsNewLine;
    }


    // ── SET CHARACTERS LIMIT ──────────────────────────────────────────────────────
    /**
     * Sets the maximum number of characters the user may enter. Pass {@code -1} for
     * no limit.
     *
     * @param charactersLimit  the character limit, or {@code -1} for unlimited
     */
    public void setCharactersLimit( int charactersLimit )
    {
        this.charactersLimit = charactersLimit;
    }


    // ── SET DOLLAR SIGN IS NEW LINE ───────────────────────────────────────────────
    /**
     * Sets whether {@code $} in the initial value string is treated as a newline.
     *
     * @param dollarSignIsNewLine  {@code true} to treat {@code $} as newline; {@code false} for literal
     */
    public void setDollarSignIsNewLine( boolean dollarSignIsNewLine )
    {
        this.dollarSignIsNewLine = dollarSignIsNewLine;
    }


    // ── SET NUMBER OF ROWS ────────────────────────────────────────────────────────
    /**
     * Sets the number of display rows. Use {@code 1} for single-line; use a higher
     * value to get a multi-line (SWT.MULTI) text widget.
     *
     * @param numberOfRows  row count (must be &gt;= 1)
     */
    public void setNumberOfRows( int numberOfRows )
    {
        this.numberOfRows = numberOfRows;
    }
}
