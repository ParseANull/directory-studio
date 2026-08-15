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


// ── CLASS: TemplateSpinner — C-3PO DECODING A NUMERIC-RANGE COMMUNIQUÉ ───────────
// In the Imperial communiqué, a spinner directive constrains an integer LDAP
// attribute to a numeric range. C-3PO reads the minimum, maximum, increment (how
// much each up/down click changes the value), page increment (how much PgUp/PgDn
// changes it), and the number of decimal digits to display. Defaults cover the
// full int range so the spinner works out of the box without extra configuration.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template spinner widget. Stores the numeric range
 * ({@code minimum}, {@code maximum}), step sizes ({@code increment},
 * {@code pageIncrement}), and the number of decimal places ({@code digits}).
 *
 * <p>Think of this as a C-3PO-decoded numeric-range communiqué directive:</p>
 * <pre>
 *   spinner.setMinimum( 0 );
 *   spinner.setMaximum( 65535 );
 *   spinner.setIncrement( 1 );
 *   spinner.setPageIncrement( 256 );
 *   spinner.setDigits( 0 );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateSpinner extends AbstractTemplateWidget
{
    /** The default minimum — {@link Integer#MIN_VALUE} means no lower bound imposed. */
    public static int DEFAULT_MINIMUM = Integer.MIN_VALUE;

    /** The default maximum — {@link Integer#MAX_VALUE} means no upper bound imposed. */
    public static int DEFAULT_MAXIMUM = Integer.MAX_VALUE;

    /** The default increment — one unit per click. */
    public static int DEFAULT_INCREMENT = 1;

    /** The default page increment — ten units per PgUp/PgDn. */
    public static int DEFAULT_PAGE_INCREMENT = 10;

    /** The default digits — integer display (no decimal places). */
    public static int DEFAULT_DIGITS = 0;

    /** The minimum value */
    private int minimum = DEFAULT_MINIMUM;

    /** The maximum value */
    private int maximum = DEFAULT_MAXIMUM;

    /** The increment */
    private int increment = DEFAULT_INCREMENT;

    /** The page increment */
    private int pageIncrement = DEFAULT_PAGE_INCREMENT;

    /** The number of decimal places */
    private int digits = DEFAULT_DIGITS;


    // ── CONSTRUCTOR: REGISTER THE SPINNER COMMUNIQUÉ ──────────────────────────────
    // C-3PO receives a new numeric-range directive and files it inside the parent
    // communiqué. The template parser overrides defaults from the XML if specified.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateSpinner} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateSpinner( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET INCREMENT: THE PER-CLICK STEP SIZE ────────────────────────────────────
    // C-3PO reads the increment field — how much the value changes each time the
    // user clicks the up or down arrow.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the amount added or subtracted from the value each time the user
     * clicks the spinner's up or down arrow.
     *
     * @return the increment; defaults to {@code 1}
     */
    public int getIncrement()
    {
        return increment;
    }


    // ── GET MAXIMUM: THE UPPER BOUND ──────────────────────────────────────────────
    // C-3PO reads the upper-bound field from the communiqué.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum value the spinner will allow.
     *
     * @return the maximum; defaults to {@link Integer#MAX_VALUE}
     */
    public int getMaximum()
    {
        return maximum;
    }


    // ── GET MINIMUM: THE LOWER BOUND ──────────────────────────────────────────────
    // C-3PO reads the lower-bound field from the communiqué.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the minimum value the spinner will allow.
     *
     * @return the minimum; defaults to {@link Integer#MIN_VALUE}
     */
    public int getMinimum()
    {
        return minimum;
    }


    // ── GET PAGE INCREMENT: THE PGUP/PGDN STEP SIZE ───────────────────────────────
    // C-3PO reads the page-increment field — the larger step used when the user
    // presses PgUp or PgDn inside the spinner.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the larger step size applied when the user presses PgUp or PgDn.
     *
     * @return the page increment; defaults to {@code 10}
     */
    public int getPageIncrement()
    {
        return pageIncrement;
    }


    // ── GET DIGITS: DECIMAL PLACES ────────────────────────────────────────────────
    // C-3PO reads the digits field — how many decimal places to show. Zero means
    // pure integer display; greater values shift the decimal point left.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of decimal places displayed. {@code 0} means the value
     * is shown as a plain integer; {@code 2} shows e.g. {@code 3.14} when the
     * raw value is {@code 314}.
     *
     * @return the digit count; defaults to {@code 0}
     */
    public int getDigits()
    {
        return digits;
    }


    // ── SET INCREMENT ─────────────────────────────────────────────────────────────
    /**
     * Sets the per-click step size.
     *
     * @param increment  the amount to add/subtract per arrow click (must be &gt; 0)
     */
    public void setIncrement( int increment )
    {
        this.increment = increment;
    }


    // ── SET MAXIMUM ───────────────────────────────────────────────────────────────
    /**
     * Sets the upper bound for the spinner's value.
     *
     * @param maximum  the maximum allowed value
     */
    public void setMaximum( int maximum )
    {
        this.maximum = maximum;
    }


    // ── SET MINIMUM ───────────────────────────────────────────────────────────────
    /**
     * Sets the lower bound for the spinner's value.
     *
     * @param minimum  the minimum allowed value
     */
    public void setMinimum( int minimum )
    {
        this.minimum = minimum;
    }


    // ── SET PAGE INCREMENT ────────────────────────────────────────────────────────
    /**
     * Sets the PgUp/PgDn step size.
     *
     * @param pageIncrement  the amount to add/subtract per page key press (must be &gt; 0)
     */
    public void setPageIncrement( int pageIncrement )
    {
        this.pageIncrement = pageIncrement;
    }


    // ── SET DIGITS ────────────────────────────────────────────────────────────────
    /**
     * Sets the number of decimal places displayed.
     *
     * @param digits  decimal place count (0 for integer display)
     */
    public void setDigits( int digits )
    {
        this.digits = digits;
    }
}
