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

package org.apache.directory.studio.ldapbrowser.common.widgets;


import java.util.Arrays;

import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.dialogs.preferences.TextFormatsPreferencePage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


// ── CLASS: OptionsInput — THE MILLENNIUM FALCON'S MODULAR COCKPIT CONSOLE ────
// The Falcon's cockpit has two navigation modes: a pre-plotted hyperspace route
// (the default button, one click and you're set) or a custom course you dial in
// yourself on the nav computer (the "Other" radio + combo box with all known routes).
// OptionsInput is that same two-mode panel: one radio for the sensible default,
// one radio that unlocks a drop-down for any other choice.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A reusable "pick one option" widget used throughout the text-format preference pages.
 * It renders two radio buttons:
 * <ol>
 *   <li>A <em>default</em> radio that selects a single pre-determined value (e.g. the
 *       platform's charset or line separator).</li>
 *   <li>An <em>other</em> radio that, when selected, enables a drop-down combo so the
 *       user can choose from a list of alternatives (or type a custom value if allowed).</li>
 * </ol>
 * Every subclass ({@link BinaryEncodingInput}, {@link FileEncodingInput},
 * {@link LineSeparatorInput}) pre-populates the constructor with a fixed set of choices.
 * Think of this as the Falcon's cockpit: Han can take the pre-plotted route (default radio),
 * or dial in a custom course on the nav computer (other radio + combo).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OptionsInput extends AbstractWidget
{

    /** The option's title */
    private String title;

    /** The group, only used when asGroup is true */
    private Group titleGroup;

    /** The default raw value */
    private String defaultRawValue;

    /** The default display value */
    private String defaultDisplayValue;

    /** The radio button to select the default value */
    private Button defaultButton;

    /** The other raw values */
    private String[] otherRawValues;

    /** The other display values */
    private String[] otherDisplayValues;

    /** The radio button to select a value from drop-down list */
    private Button otherButton;

    /** The combo with the other values */
    private Combo otherCombo;

    /** The initial raw value */
    private String initialRawValue;

    /** If true the options are aggregated in a group widget */
    private boolean asGroup;

    /** If true it is possible to enter a custom value into the combo field */
    private boolean allowCustomInput;


    // ── HAN CONFIGURES THE NAV CONSOLE BEFORE DEPARTURE ──────────────────────────
    // Han sits in the cockpit and loads the pre-plotted route, the nav computer's full list
    // of alternative destinations, and which destination to pre-select from last time.
    // We store all configuration parameters so createWidget() can build the SWT controls.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Configures an OptionsInput with all the data it needs to render itself.
     * No SWT widgets are built yet — call {@link #createWidget(Composite)} to render them.
     * The {@code initialRawValue} determines which state the widget starts in:
     * if it matches the default, the default radio is checked; otherwise, the "Other" radio
     * is checked and the combo is pre-selected to the matching entry.
     *
     * <p>For example — Han loads the nav console:</p>
     * <pre>
     *   title             = "File Encoding"
     *   defaultDisplay    = "UTF-8 (platform default)"
     *   defaultRaw        = "UTF-8"
     *   otherDisplayValues= {"ISO-8859-1", "UTF-16", ...}
     *   otherRawValues    = {"ISO-8859-1", "UTF-16", ...}
     *   initialRawValue   = "UTF-8"  → default radio pre-selected
     * </pre>
     *
     * @param title               The label shown above or beside the two radio buttons.
     * @param defaultDisplayValue The text shown next to the default radio button.
     * @param defaultRawValue     The internal value returned when the default radio is selected.
     * @param otherDisplayValues  The human-readable labels for the drop-down list options.
     * @param otherRawValues      The internal values corresponding to each drop-down label;
     *                            must be the same length and order as {@code otherDisplayValues}.
     * @param initialRawValue     The raw value to pre-select when the widget is first shown.
     * @param asGroup             When {@code true}, wraps everything in a labeled SWT Group border.
     * @param allowCustomInput    When {@code true}, the combo allows free-form text entry in
     *                            addition to the drop-down choices.
     */
    public OptionsInput( String title, String defaultDisplayValue, String defaultRawValue, String[] otherDisplayValues,
        String[] otherRawValues, String initialRawValue, boolean asGroup, boolean allowCustomInput )
    {
        super();
        this.title = title;
        this.defaultDisplayValue = defaultDisplayValue;
        this.defaultRawValue = defaultRawValue;
        this.otherDisplayValues = otherDisplayValues;
        this.otherRawValues = otherRawValues;
        this.initialRawValue = initialRawValue;
        this.asGroup = asGroup;
        this.allowCustomInput = allowCustomInput;
    }


    // ── HAN INSTALLS THE COCKPIT PANEL ────────────────────────────────────────────
    // Han bolts the nav console into the cockpit, wires up the two route buttons,
    // connects the nav computer drop-down, and sets it to the last known heading.
    // We build the SWT radio buttons and combo, attach listeners, and call setRawValue().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and attaches the widget's SWT controls to the given parent composite.
     * Creates:
     * <ul>
     *   <li>Optionally a titled SWT Group (if {@code asGroup} was {@code true})</li>
     *   <li>A default radio button labeled with the default display value</li>
     *   <li>An "Other:" radio button that enables the combo when selected</li>
     *   <li>A drop-down combo populated with the other display values</li>
     * </ul>
     * After building, calls {@link #setRawValue(String)} with the initial value to
     * put the widget in the right starting state.
     *
     * <p>For example — Han installs the panel:</p>
     * <pre>
     *   [o] UTF-8 (platform default)       ← defaultButton — one click, done
     *   [o] Other: [ ISO-8859-1     ▼ ]   ← otherButton + otherCombo
     * </pre>
     *
     * @param parent  The SWT composite to add our controls to; must not be {@code null}.
     */
    public void createWidget( Composite parent )
    {

        Composite composite;
        if ( asGroup )
        {
            titleGroup = BaseWidgetUtils.createGroup( parent, title, 1 );
            composite = BaseWidgetUtils.createColumnContainer( titleGroup, 1, 1 );
        }
        else
        {
            composite = parent;
            Composite labelComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );
            BaseWidgetUtils.createLabel( labelComposite, title + ":", 1 ); //$NON-NLS-1$
        }

        Composite defaultComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );
        defaultButton = BaseWidgetUtils.createRadiobutton( defaultComposite, defaultDisplayValue, 1 );
        defaultButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                otherButton.setSelection( false );
                otherCombo.setEnabled( false );
                notifyListeners();
            }
        } );

        Composite otherComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        otherButton = BaseWidgetUtils.createRadiobutton( otherComposite, Messages.getString( "OptionsInput.Other" ), 1 ); //$NON-NLS-1$
        otherButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                defaultButton.setSelection( false );
                otherCombo.setEnabled( true );
                notifyListeners();
            }
        } );

        if ( allowCustomInput )
        {
            otherCombo = BaseWidgetUtils.createCombo( otherComposite, otherDisplayValues, 0, 1 );
        }
        else
        {
            otherCombo = BaseWidgetUtils.createReadonlyCombo( otherComposite, otherDisplayValues, 0, 1 );
        }
        otherCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                notifyListeners();
            }
        } );

        setRawValue( initialRawValue );
    }


    // ── HAN READS THE CURRENT HEADING FROM THE DISPLAY ───────────────────────────
    // The co-pilot asks: "What's our current course?" Han glances at the console.
    // If the default route is selected, he reads the pre-plotted coordinates.
    // If "Other" is selected, he reads the chosen destination from the nav computer.
    // We return the default raw value or translate the combo's display value back to raw.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw (internal) value currently selected by the widget.
     * If the default radio is selected, {@code defaultRawValue} is returned directly.
     * If the "Other" radio is selected, the combo's current display text is mapped back
     * to its corresponding raw value via the {@code otherDisplayValues} / {@code otherRawValues}
     * arrays. If the display text doesn't match any known entry (only possible when
     * {@code allowCustomInput} is {@code true}), the raw text from the combo is returned as-is.
     *
     * <p>For example — Han reads the console:</p>
     * <pre>
     *   defaultButton.getSelection() → true  → return "UTF-8"
     *   otherCombo.getText()         → "ISO-8859-1" → return "ISO-8859-1" (raw value)
     * </pre>
     *
     * @return  The current raw value as a String; never {@code null}.
     */
    public String getRawValue()
    {
        if ( defaultButton.getSelection() )
        {
            return defaultRawValue;
        }
        else
        {
            String t = otherCombo.getText();
            for ( int i = 0; i < otherDisplayValues.length; i++ )
            {
                if ( t.equals( otherDisplayValues[i] ) )
                {
                    return otherRawValues[i];
                }
            }
            return t;
        }
    }


    // ── HAN PUNCHES IN A NEW HEADING ──────────────────────────────────────────────
    // Mission control transmits new coordinates — Han punches them into the nav console.
    // If it's the default route, he hits the default button. If it's something else,
    // he switches to "Other" and dials in the matching destination on the nav computer.
    // We translate the raw value back to the right UI state (radio + combo selection).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the widget's state to reflect the given raw value.
     * If the raw value matches {@code defaultRawValue}, the default radio is selected
     * and the combo is disabled. Otherwise, the "Other" radio is selected, the combo is
     * enabled, and the combo is scrolled to the matching display entry (or has its text
     * set directly if no match is found in the arrays — only meaningful with custom input).
     *
     * <p>For example — Han punches in new coordinates:</p>
     * <pre>
     *   setRawValue("UTF-8")       → defaultButton selected, combo disabled
     *   setRawValue("ISO-8859-1")  → otherButton selected, combo shows "ISO-8859-1"
     *   setRawValue("X-CUSTOM")    → otherButton selected, combo text = "X-CUSTOM"
     * </pre>
     *
     * @param rawValue  The raw value to select; must not be {@code null}.
     */
    public void setRawValue( String rawValue )
    {
        int index = Arrays.asList( otherRawValues ).indexOf( rawValue );
        if ( index == -1 )
        {
            index = Arrays.asList( otherDisplayValues ).indexOf( rawValue );
        }

        if ( defaultRawValue.equals( rawValue ) )
        {
            defaultButton.setSelection( true );
            otherButton.setSelection( false );
            otherCombo.setEnabled( false );
            otherCombo.select( index );
        }
        else if ( index > -1 )
        {
            defaultButton.setSelection( false );
            otherButton.setSelection( true );
            otherCombo.setEnabled( true );
            otherCombo.select( index );
        }
        else
        {
            defaultButton.setSelection( false );
            otherButton.setSelection( true );
            otherCombo.setEnabled( true );
            otherCombo.setText( rawValue );
        }
    }

}
