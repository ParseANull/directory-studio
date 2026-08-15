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

package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: LimitWidget — Han Rationing Fuel for the Hyperspace Jump ──────────────────
// Han Solo at the Millennium Falcon's fuel console in A New Hope: he can't let the
// engines run forever. He sets a maximum entry count — how many results to retrieve —
// and a time cap — how many seconds before the server should give up waiting.
// The two fields mirror Han's two dials: count limit (how many parsecs of fuel) and
// time limit (how long the hyperdrive can run before he cuts it off).
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for configuring the two search-result limits that an LDAP search
 * request can carry: the maximum number of entries the server should return, and the
 * maximum number of seconds the server should spend on the search.
 * Think of this class as Han Solo's fuel console: he dials in a count cap and a
 * time cap to keep the jump from draining the ship dry.
 *
 * <p>Both fields accept only non-negative integers. A value of {@code 0} means
 * "no limit" — the LDAP server default.</p>
 * Used by {@link SearchPageWrapper} in the options section of the search form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LimitWidget extends AbstractWidget
{

    /** The initial count limit. */
    private int initialCountLimit;

    /** The initial time limit. */
    private int initialTimeLimit;

    /** The limit group. */
    private Group limitGroup;

    /** The count limit label. */
    private Label countLimitLabel;

    /** The count limit text. */
    private Text countLimitText;

    /** The time limit label. */
    private Label timeLimitLabel;

    /** The time limit text. */
    private Text timeLimitText;


    // ── Han Sets His Fuel Gauges Before the Jump ──────────────────────────────────────
    // Han knows exactly how far he needs to go: 50 entries max, 30 seconds max.
    // He dials those values into the console before firing up the hyperdrive.
    // We store the caller's specific limits so createWidget() can pre-populate the fields.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget pre-configured with specific count and time limits.
     * Use this when editing an existing saved search that already has limit settings.
     *
     * <p>For example — Han pre-sets the gauges:</p>
     * <pre>
     *   fuelGauge.setMax( 50 );    // countLimit: retrieve at most 50 entries
     *   timerGauge.setMax( 30 );   // timeLimit: give up after 30 seconds
     * </pre>
     *
     * @param initialCountLimit  Maximum number of entries the server should return.
     *                           {@code 0} means no limit.
     * @param initialTimeLimit   Maximum seconds the server should spend on the search.
     *                           {@code 0} means no limit.
     */
    public LimitWidget( int initialCountLimit, int initialTimeLimit )
    {
        this.initialCountLimit = initialCountLimit;
        this.initialTimeLimit = initialTimeLimit;
    }


    // ── Han Leaves the Gauges at Zero — Full Tank, No Timer ──────────────────────────
    // No special mission parameters this time; Han leaves both gauges at zero, meaning
    // the server can return as many results as it wants and take as long as it needs.
    // We default both limits to 0, which is the LDAP "no limit" sentinel value.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget with no limits pre-set (both fields default to {@code 0}).
     * In LDAP, {@code 0} means the server applies its own default limits.
     * Use this for brand-new search dialogs.
     *
     * <p>For example — Han leaves the gauges at full:</p>
     * <pre>
     *   fuelGauge.setMax( 0 );  // no entry-count cap
     *   timerGauge.setMax( 0 ); // no time cap
     * </pre>
     */
    public LimitWidget()
    {
        this.initialCountLimit = 0;
        this.initialTimeLimit = 0;
    }


    // ── Han Installs the Fuel Console Panel on the Dashboard ─────────────────────────
    // Han bolts in a labeled group with two text fields: "Count Limit" and "Time Limit".
    // Each field only accepts digits — non-numeric characters are blocked by a verify
    // listener. Changing either value fires the form's change notification.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and lays out the SWT controls inside the given parent composite.
     * Creates a labeled group box containing two numeric-only text fields: one for the
     * count limit and one for the time limit. Both fields have tooltips explaining what
     * the value means in LDAP terms, and changes to either field notify listeners.
     * Call this exactly once after construction.
     *
     * <p>For example — Han installs the fuel console:</p>
     * <pre>
     *   panel = new Group( dashboard, "Limits" );
     *   panel.addNumericField( "Count Limit", initialCountLimit );
     *   panel.addNumericField( "Time Limit",  initialTimeLimit );
     * </pre>
     *
     * @param parent  The SWT composite that will host this widget's group and fields.
     */
    public void createWidget( Composite parent )
    {

        limitGroup = BaseWidgetUtils.createGroup( parent, Messages.getString( "LimitWidget.Limits" ), 1 ); //$NON-NLS-1$
        GridLayout gl = new GridLayout( 2, false );
        limitGroup.setLayout( gl );

        // Count limit
        String countLimitToolTipText = Messages.getString( "LimitWidget.CountLimitTooltip" ); //$NON-NLS-1$
        countLimitLabel = BaseWidgetUtils.createLabel( limitGroup, Messages.getString( "LimitWidget.CountLimit" ), 1 ); //$NON-NLS-1$
        countLimitLabel.setToolTipText( countLimitToolTipText );
        countLimitText = BaseWidgetUtils.createText( limitGroup, "", 1 ); //$NON-NLS-1$
        countLimitText.setToolTipText( countLimitToolTipText );
        countLimitText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
        countLimitText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                notifyListeners();
            }
        } );

        // Time limit
        String timeLimitToolTipText = Messages.getString( "LimitWidget.TimeLimitToolTip" ); //$NON-NLS-1$
        timeLimitLabel = BaseWidgetUtils.createLabel( limitGroup, Messages.getString( "LimitWidget.TimeLimit" ), 1 ); //$NON-NLS-1$
        timeLimitLabel.setToolTipText( timeLimitToolTipText );
        timeLimitText = BaseWidgetUtils.createText( limitGroup, "", 1 ); //$NON-NLS-1$
        timeLimitText.setToolTipText( timeLimitToolTipText );
        timeLimitText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
        timeLimitText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                notifyListeners();
            }
        } );

        setCountLimit( initialCountLimit );
        setTimeLimit( initialTimeLimit );
    }


    // ── Han Dials In a New Entry-Count Cap ───────────────────────────────────────────
    // Mid-mission, Leia calls over the comm: "Cut the fuel to 100 entries max."
    // Han reaches over and dials the count gauge to 100.
    // We update the stored value and push the new integer into the text field.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the count-limit field to a specific value.
     * Use this when loading a saved search into the form to restore its entry cap.
     * The value is displayed as a plain integer string in the text field.
     *
     * <p>For example — Han adjusts the count gauge:</p>
     * <pre>
     *   countGauge.dial( 100 ); // "100" appears in the field
     * </pre>
     *
     * @param countLimit  The maximum number of entries to request. {@code 0} = no cap.
     */
    public void setCountLimit( int countLimit )
    {
        initialCountLimit = countLimit;
        countLimitText.setText( Integer.toString( initialCountLimit ) );
    }


    // ── Han Dials In a New Time Cap ───────────────────────────────────────────────────
    // "Limit the jump to 60 seconds," Leia insists. Han adjusts the timer dial.
    // We update the stored value and push the new integer into the time-limit field.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the time-limit field to a specific value.
     * Use this when loading a saved search into the form to restore its time cap.
     *
     * <p>For example — Han adjusts the timer dial:</p>
     * <pre>
     *   timerGauge.dial( 60 ); // "60" appears in the field
     * </pre>
     *
     * @param timeLimit  The maximum seconds the server should search. {@code 0} = no cap.
     */
    public void setTimeLimit( int timeLimit )
    {
        initialTimeLimit = timeLimit;
        timeLimitText.setText( Integer.toString( initialTimeLimit ) );
    }


    // ── Han Reads the Current Entry-Count Gauge ───────────────────────────────────────
    // "How many entries are we pulling, Han?" He glances at the count dial and reports.
    // We parse the text field into an integer, defaulting to 0 if the field is blank.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads and returns the current count-limit value from the text field.
     * Parses the field as an integer; returns {@code 0} if the field is empty or
     * contains non-numeric text (which shouldn't happen due to the verify listener,
     * but we guard for it anyway).
     *
     * <p>For example — Han reads the count gauge:</p>
     * <pre>
     *   max = countGauge.read(); // e.g. 100, or 0 for "no limit"
     * </pre>
     *
     * @return  The entry count limit as an integer. {@code 0} means no server-side cap.
     */
    public int getCountLimit()
    {
        int countLimit;
        try
        {
            countLimit = Integer.valueOf( countLimitText.getText() );
        }
        catch ( NumberFormatException e )
        {
            countLimit = 0;
        }
        return countLimit;
    }


    // ── Han Reads the Current Timer Dial ──────────────────────────────────────────────
    // "How many seconds are we giving the server?" Han checks the timer and reports back.
    // We parse the time-limit field as an integer, defaulting to 0 if it's blank.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads and returns the current time-limit value from the text field.
     * Parses the field as an integer; returns {@code 0} if the field is empty or
     * contains non-numeric text.
     *
     * <p>For example — Han reads the timer dial:</p>
     * <pre>
     *   maxSeconds = timerGauge.read(); // e.g. 30, or 0 for "no limit"
     * </pre>
     *
     * @return  The time limit in seconds. {@code 0} means no server-side time cap.
     */
    public int getTimeLimit()
    {
        int timeLimit;
        try
        {
            timeLimit = Integer.valueOf( timeLimitText.getText() );
        }
        catch ( NumberFormatException e )
        {
            timeLimit = 0;
        }
        return timeLimit;
    }


    // ── Han Powers Down the Console Dials ────────────────────────────────────────────
    // When the Falcon is docked and no one's flying, Han flips the console to standby.
    // All the dials grey out and stop accepting input until the ship is flight-ready again.
    // We propagate the enabled state to the group, both labels, and both text fields.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the entire widget in one call. When disabled, the group box,
     * both labels, and both text fields are all greyed out and non-interactive.
     * Useful when the search form enters a read-only or "view only" mode.
     *
     * <p>For example — Han powers down the console:</p>
     * <pre>
     *   consoleGroup.setEnabled( false );
     *   countDial.setEnabled( false );
     *   timerDial.setEnabled( false );
     * </pre>
     *
     * @param b  {@code true} to make the controls interactive; {@code false} to grey them.
     */
    public void setEnabled( boolean b )
    {
        limitGroup.setEnabled( b );
        countLimitLabel.setEnabled( b );
        countLimitText.setEnabled( b );
        timeLimitLabel.setEnabled( b );
        timeLimitText.setEnabled( b );
    }

}
