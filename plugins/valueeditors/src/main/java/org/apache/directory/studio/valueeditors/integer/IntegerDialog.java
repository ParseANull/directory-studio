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

package org.apache.directory.studio.valueeditors.integer;


import java.math.BigDecimal;

import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: IntegerDialog — C-3PO Calculating Asteroid Field Odds ──────────────
// In the asteroid field, C-3PO calculates probabilities with relentless precision:
// "Sir, the possibility of successfully navigating an asteroid field is 3,720 to 1!"
// This dialog is C-3PO's number-entry console: it validates input, adjusts the value
// with + / - controls, and refuses anything that is not a proper integer.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * A small dialog that lets the user enter or adjust an integer LDAP attribute value.
 * It wraps a text field with plus and minus buttons, keyboard arrow-key support, and a
 * verify listener that blocks non-numeric input before it can reach the field.
 * Think of this class as C-3PO's calculation console: it accepts only clean integers,
 * complains loudly about anything else, and reports the final number with precision.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class IntegerDialog extends Dialog
{
    /** The initial value */
    private BigDecimal initialValue;

    /** The value */
    private BigDecimal value;

    /** The text */
    private Text text;


    // ── C-3PO Initializes His Calculation Unit ────────────────────────────────────
    // C-3PO powers up and loads the current mission odds into his calculation register.
    // He notes the starting value so he can later tell whether anything actually changed.
    // We store the initial value, clone it into the live value field, and set the shell resizable.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new IntegerDialog pre-loaded with the current LDAP integer value.
     * We store the initial value separately so we can detect whether the user actually
     * changed anything (see {@link #isDirty()}), and we clone it into {@code value} so
     * edits do not accidentally mutate the original.
     *
     * <p>For example — C-3PO loads the current odds into his calculation register:</p>
     * <pre>
     *   C-3PO: "Current probability on record: 3,720. Noted. Standing by for adjustment."
     * </pre>
     *
     * @param parentShell  the SWT shell that owns this dialog
     * @param initialValue the current attribute value to pre-populate the editor with
     */
    public IntegerDialog( Shell parentShell, BigDecimal initialValue )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.initialValue = initialValue;
        this.value = new BigDecimal( initialValue.toString() );
    }


    // ── C-3PO Labels The Calculation Console ─────────────────────────────────────
    // C-3PO announces the purpose of the session before touching any buttons.
    // He sets the window title and icon so everyone knows this is the integer editor.
    // We configure the shell text and image for the LDAP integer editor.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell with the integer editor's window title and icon.
     * Called by the JFace framework before the dialog opens so the window is clearly
     * identified before the user starts typing.
     *
     * <p>For example — C-3PO announces the purpose of the console session:</p>
     * <pre>
     *   C-3PO: "This is the Integer Calculation Console. Precision is paramount."
     * </pre>
     *
     * @param shell the shell to configure; we set its text and image
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "IntegerDialog.IntegerEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_INTEGEREDITOR ) );
    }


    // ── C-3PO Installs The Confirm And Abort Controls ─────────────────────────────
    // C-3PO wires up the OK and Cancel levers before accepting any input.
    // OK confirms the final probability figure; Cancel aborts and discards changes.
    // We create the standard JFace OK and Cancel buttons in the button bar.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons for the dialog's button bar.
     * OK is the default button so the user can hit Enter to confirm; Cancel discards any edits.
     *
     * <p>For example — C-3PO installs confirm and abort levers on the console:</p>
     * <pre>
     *   C-3PO: "OK lever — confirms the probability. Cancel lever — reverts. Both installed."
     * </pre>
     *
     * @param parent the composite that hosts the button bar
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── C-3PO Locks In The Final Probability Figure ───────────────────────────────
    // When Han Solo finally says "confirmed," C-3PO registers the current number as final.
    // He hands control back to the framework to close the dialog.
    // We call super.okPressed() to propagate the OK action through the JFace dialog chain.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Handles the OK button press, locking in the current value and closing the dialog.
     * The value has already been kept up to date by the modify and verify listeners,
     * so we simply delegate to the superclass to trigger the close sequence.
     *
     * <p>For example — C-3PO locks in the confirmed probability and steps back:</p>
     * <pre>
     *   Han: "Lock it in, Threepio."
     *   C-3PO: "3,720 confirmed. Closing calculation console."
     * </pre>
     */
    protected void okPressed()
    {
        //        returnValue = spinner.getSelection();
        super.okPressed();
    }


    // ── C-3PO Assembles The Calculation Console Interface ─────────────────────────
    // C-3PO extends his input panel: a minus button, the number field, and a plus button.
    // He wires listeners that block non-numeric input and respond to arrow-key adjustments.
    // We build the three-widget layout and hook up modify, verify, and key listeners.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content area: a minus button, a text field, and a plus button arranged
     * in a single row.
     * The text field carries three listeners:
     * <ul>
     *   <li>A {@link ModifyListener} that keeps {@code value} in sync as the user types.</li>
     *   <li>A {@link VerifyListener} that blocks any character that would produce a non-integer.</li>
     *   <li>A {@link KeyAdapter} that handles up/down/page-up/page-down for quick adjustments.</li>
     * </ul>
     *
     * <p>For example — C-3PO extends his full calculation input panel:</p>
     * <pre>
     *   [-] [ 3720 ] [+]
     *   C-3PO: "Arrow keys adjust by 1. Page keys adjust by 100. Non-digits are rejected."
     * </pre>
     *
     * @param parent the parent composite provided by the JFace dialog framework
     * @return the top-level control we built, handed back to JFace
     */
    protected Control createDialogArea( Composite parent )
    {
        // Composite
        Composite composite = ( Composite ) super.createDialogArea( parent );
        composite.setLayout( new GridLayout( 3, false ) );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        // - Button
        Button minusButton = new Button( composite, SWT.PUSH );
        minusButton.setText( "-" ); //$NON-NLS-1$
        minusButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addToValue( -1 );
                text.selectAll();
            }
        } );

        // Text
        text = new Text( composite, SWT.BORDER );
        text.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        text.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                updateValueFromText();
            }
        } );
        text.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                // Prevent the user from entering anything but an integer
                if ( !e.text.matches( "(-)?([0-9])*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
        text.addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent e )
            {
                if ( e.keyCode == SWT.ARROW_UP )
                {
                    addToValue( 1 );
                    e.doit = false;
                    text.selectAll();
                }
                else if ( e.keyCode == SWT.ARROW_DOWN )
                {
                    addToValue( -1 );
                    e.doit = false;
                    text.selectAll();
                }
                else if ( e.keyCode == SWT.PAGE_UP )
                {
                    addToValue( 100 );
                    e.doit = false;
                    text.selectAll();
                }
                else if ( e.keyCode == SWT.PAGE_DOWN )
                {
                    addToValue( -100 );
                    e.doit = false;
                    text.selectAll();
                }
            }
        } );

        // + Button
        Button plusButton = new Button( composite, SWT.PUSH );
        plusButton.setText( "+" ); //$NON-NLS-1$
        plusButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addToValue( 1 );
                text.selectAll();
            }
        } );

        updateTextValue();

        // Setting focus
        text.setFocus();

        applyDialogFont( composite );
        return composite;
    }


    // ── C-3PO Reads The Current Value Aloud ──────────────────────────────────────
    // After every adjustment C-3PO recites the updated probability to the crew.
    // He sets the text field's content to reflect the in-memory value precisely.
    // We push the current BigDecimal value into the SWT Text widget.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the current in-memory {@code value} into the text field.
     * We call this after every programmatic change (button click, arrow key) so the
     * displayed number always matches what we will return if the user clicks OK.
     *
     * <p>For example — C-3PO reads the updated probability to the crew:</p>
     * <pre>
     *   C-3PO: "Recalculating... The odds are now 3,719 to 1."
     *   He updates the display accordingly.
     * </pre>
     */
    private void updateTextValue()
    {
        text.setText( value.toString() );
    }


    // ── C-3PO Adjusts The Probability By A Delta ─────────────────────────────────
    // Han punches a button and C-3PO recalculates: "That changes the odds by exactly one."
    // C-3PO updates his register and immediately reads the new figure back.
    // We add the delta to the BigDecimal value and refresh the text field.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the given integer delta to the current value and updates the text field to match.
     * Called by the plus/minus buttons and the up/down/page arrow key handlers.
     * Using {@link BigDecimal#add} keeps us safe from integer overflow, which matters if
     * someone is editing a very large LDAP integer attribute.
     *
     * <p>For example — C-3PO adjusts his odds by the commanded delta:</p>
     * <pre>
     *   Han presses [+1]. C-3PO: "Adjustment accepted. New odds: 3,721 to 1."
     *   Han presses [-100]. C-3PO: "New odds: 3,621 to 1. Still not good, sir."
     * </pre>
     *
     * @param i the integer to add; may be negative (from the minus button or down-arrow key)
     */
    private void addToValue( int i )
    {
        value = value.add( new BigDecimal( i ) );

        updateTextValue();
    }


    // ── C-3PO Reads The Text Field And Updates His Register ──────────────────────
    // As the user types a new number, C-3PO monitors each keystroke and updates his register.
    // If the current text is not a valid number yet he quietly waits without complaining.
    // We try to parse the text as a BigDecimal and silently ignore NumberFormatException.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the text field's current content and updates the in-memory {@code value} if
     * the text is a valid integer.
     * Called by the modify listener on every keystroke.
     * We silently ignore {@link NumberFormatException} because intermediate typing states
     * (e.g. just "-" with no digits yet) are expected and harmless.
     *
     * <p>For example — C-3PO reads each keystroke and updates his register quietly:</p>
     * <pre>
     *   User types "3", "37", "372", "3720" — C-3PO updates silently at each step.
     *   User types "-" alone — C-3PO: "Not valid yet. I will wait."
     * </pre>
     */
    private void updateValueFromText()
    {
        try
        {
            BigDecimal newValue = new BigDecimal( text.getText() );
            value = newValue;
        }
        catch ( NumberFormatException e )
        {
            // Nothing to do
        }
    }


    // ── C-3PO Reports The Final Probability Figure ────────────────────────────────
    // After all adjustments, C-3PO announces the final number to whoever is asking.
    // He hands over the value cleanly so the caller can write it to the LDAP attribute.
    // We return the current BigDecimal value from our internal register.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the integer value currently displayed in the dialog.
     * The caller uses this after the dialog closes with OK to get the number to write back
     * to the LDAP attribute.
     *
     * <p>For example — C-3PO reports the final probability:</p>
     * <pre>
     *   C-3PO: "The confirmed figure is 3,720. Do with it what you will, sir."
     * </pre>
     *
     * @return the current value as a {@link BigDecimal}
     */
    public BigDecimal getInteger()
    {
        return value;
    }


    // ── C-3PO Checks Whether The Number Actually Changed ─────────────────────────
    // C-3PO compares the final figure against what it was when the session opened.
    // If Han typed the same number back in, C-3PO knows nothing really changed.
    // We compare value against initialValue so the caller can skip a pointless write.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Indicates whether the user changed the value from what it was when the dialog opened.
     * The caller checks this to avoid writing back to the LDAP directory when nothing actually
     * changed — a pointless round-trip that could trigger unnecessary modify operations.
     *
     * <p>For example — C-3PO checks whether Han actually changed the probability:</p>
     * <pre>
     *   Han types "3720" — same as before. C-3PO: "No change detected. Saving unnecessary."
     *   Han types "3721" — C-3PO: "Value changed. Dirty flag: true."
     * </pre>
     *
     * @return {@code true} if the current value differs from the initial value, {@code false} otherwise
     */
    public boolean isDirty()
    {
        return !initialValue.equals( value );
    }
}
