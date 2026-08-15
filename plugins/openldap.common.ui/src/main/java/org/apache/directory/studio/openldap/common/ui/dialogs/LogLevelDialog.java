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
package org.apache.directory.studio.openldap.common.ui.dialogs;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.openldap.common.ui.model.LogLevelEnum;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: LogLevelDialog — DEATH STAR THREAT ALERT CONSOLE ──────────────────
// Think of Grand Moff Tarkin's control room where each alert switch corresponds
// to a different threat category: trace traffic, BER packets, ACL violations,
// and so on. The operator flips any combination of alert switches, and the
// console immediately computes and displays the resulting threat-level integer.
// "None" disarms everything; "Any" lights up every indicator at once.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We present a dialog for configuring the OpenLDAP log level. Each logging
 * category is represented as a checkbox; we compute the resulting integer by
 * OR-ing the bit values of the selected categories. The possible values are:
 *
 * <ul>
 * <li>none        0</li>
 * <li>trace       1</li>
 * <li>packets     2</li>
 * <li>args        4</li>
 * <li>conns       8</li>
 * <li>BER        16</li>
 * <li>filter     32</li>
 * <li>config     64</li>
 * <li>ACL       128</li>
 * <li>stats     256</li>
 * <li>stats2    512</li>
 * <li>shell    1024</li>
 * <li>parse    2048</li>
 * <li>sync    16384</li>
 * <li>any       -1</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LogLevelDialog extends Dialog
{
    /** The logLevel value */
    private int logLevelValue;

    // UI widgets
    private Button noneCheckbox;
    private Button traceCheckbox;
    private Button packetsCheckbox;
    private Button argsCheckbox;
    private Button connsCheckbox;
    private Button berCheckbox;
    private Button filterCheckbox;
    private Button configCheckbox;
    private Button aclCheckbox;
    private Button statsCheckbox;
    private Button stats2Checkbox;
    private Button shellCheckbox;
    private Button parseCheckbox;
    private Button syncCheckbox;
    private Button anyCheckbox;

    /** An array of all the checkboxes */
    private Button[] buttons = new Button[13];

    // The resulting integer
    private Text logLevelText;

    // An empty space
    protected static final String TABULATION = " ";

    /**
     * The listener in charge of exposing the changes when some buttons are checked
     */
    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            Object object = e.getSource();

            if ( object instanceof Button )
            {
                Button selectedButton = (Button)object;

                if ( selectedButton.equals( noneCheckbox ) )
                {
                    // None, we have to uncheck all the other checkbox
                    for ( Button button : buttons )
                    {
                        button.setSelection( false );
                    }

                    // reset the Any button
                    anyCheckbox.setSelection( false );

                    // set the None button
                    noneCheckbox.setSelection( true );
                }
                else if ( selectedButton.equals( anyCheckbox ) )
                {
                    // Any, we have to check all the buttons
                    for ( Button button : buttons )
                    {
                        button.setSelection( true );
                    }

                    // reset the None button
                    noneCheckbox.setSelection( false );

                    // set the Any button
                    anyCheckbox.setSelection( true );
                }
                else
                {
                    // deselect the any and none button, unless we don't have any more
                    // selected button or all the button selected
                    int count = 0;
                    for ( Button button : buttons )
                    {
                        if ( button.getSelection() )
                        {
                            count++;
                        }
                    }

                    if ( count == 0 )
                    {
                        anyCheckbox.setSelection( false );
                        noneCheckbox.setSelection( true );
                    }
                    else if ( count == buttons.length )
                    {
                        anyCheckbox.setSelection( true );
                        noneCheckbox.setSelection( false );
                    }
                    else
                    {
                        anyCheckbox.setSelection( false );
                        noneCheckbox.setSelection( false );
                    }
                }
            }

            computeLogValue();
            setLogLevelText();
        }
    };


    // ── CONSTRUCTOR: LogLevelDialog(Shell) — OPENING THE ALERT CONSOLE ────────
    // The operator arrives at a blank console with no alerts pre-set. We make
    // the shell resizable so the operator can expand it for better readability,
    // and the initial log-level integer stays at zero.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a LogLevelDialog with an initial log-level value of zero (none).
     * The shell is made resizable so users can adjust the layout.
     *
     * @param parentShell  the parent shell
     */
    public LogLevelDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // ── CONSTRUCTOR: LogLevelDialog(Shell, int) — RESTORING A SAVED ALERT STATE
    // The operator returns to a console that was previously configured: the
    // pre-existing alert bitmask is supplied so the checkboxes can be
    // initialized to match it when the dialog opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a LogLevelDialog pre-initialized with an existing log-level
     * value. The checkboxes will reflect the bit pattern of {@code value} when
     * the dialog opens.
     *
     * @param parentShell  the parent shell
     * @param value        the initial log-level bitmask to pre-select
     */
    public LogLevelDialog( Shell parentShell, int value )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.logLevelValue = value;
    }


    // ── METHOD: configureShell — LABELLING THE ALERT CONSOLE ─────────────────
    // Before the control room lights up we stamp the title on the console
    // header so every officer knows exactly what system they are operating.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "OpenLDAP LogLevel" );
    }


    // ── METHOD: okPressed — LOCKING IN THE THREAT LEVEL ──────────────────────
    // Before we seal the hatch and hand control back to the caller, we run one
    // final computation to make sure the integer reflects the current checkbox
    // state — no stale readings leave the control room.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        computeLogValue();
        super.okPressed();
    }


    // ── METHOD: createDialogArea — CONSTRUCTING THE CONTROL ROOM LAYOUT ───────
    // We build the full interior: the checkbox grid for selecting log categories,
    // the read-only numeric display, and the listeners. After construction we
    // call setCheckboxesValue() to reflect any pre-existing bitmask.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        createLogLevelArea( composite );
        createLogLevelValueArea( composite );
        setCheckboxesValue();
        addListeners();
        applyDialogFont( composite );

        return composite;
    }


    // ── METHOD: setCheckboxesValue — SYNCING SWITCH STATES TO THE BITMASK ─────
    // The stored integer bitmask tells us which alert switches should be lit.
    // We test each bit in turn and set the corresponding checkbox. The None
    // and Any checkboxes use equality rather than bitwise AND because they
    // represent sentinel values (0 and -1).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We synchronize all checkbox selections to match the current
     * {@code logLevelValue} bitmask. Each checkbox is set by testing whether
     * its corresponding bit is set in the stored integer.
     *
     * @param perm  the Unix permissions (parameter name inherited from copy-paste; ignored here)
     */
    private void setCheckboxesValue()
    {
        noneCheckbox.setSelection( logLevelValue == LogLevelEnum.NONE.getValue() );
        traceCheckbox.setSelection( ( logLevelValue & LogLevelEnum.TRACE.getValue() ) != 0 );
        packetsCheckbox.setSelection( ( logLevelValue & LogLevelEnum.PACKETS.getValue() ) != 0 );
        argsCheckbox.setSelection( ( logLevelValue & LogLevelEnum.ARGS.getValue() ) != 0 );
        connsCheckbox.setSelection( ( logLevelValue & LogLevelEnum.CONNS.getValue() ) != 0 );
        berCheckbox.setSelection( ( logLevelValue & LogLevelEnum.BER.getValue() ) != 0 );
        filterCheckbox.setSelection( ( logLevelValue & LogLevelEnum.FILTER.getValue() ) != 0 );
        configCheckbox.setSelection( ( logLevelValue & LogLevelEnum.CONFIG.getValue() ) != 0 );
        aclCheckbox.setSelection( ( logLevelValue & LogLevelEnum.ACL.getValue() ) != 0 );
        statsCheckbox.setSelection( ( logLevelValue & LogLevelEnum.STATS.getValue() ) != 0 );
        stats2Checkbox.setSelection( ( logLevelValue & LogLevelEnum.STATS2.getValue() ) != 0 );
        shellCheckbox.setSelection( ( logLevelValue & LogLevelEnum.SHELL.getValue() ) != 0 );
        parseCheckbox.setSelection( ( logLevelValue & LogLevelEnum.PARSE.getValue() ) != 0 );
        syncCheckbox.setSelection( ( logLevelValue & LogLevelEnum.SYNC.getValue() ) != 0 );
        anyCheckbox.setSelection( logLevelValue == LogLevelEnum.ANY.getValue() );
    }


    // ── METHOD: setLogLevelText — UPDATING THE NUMERIC READOUT ───────────────
    // After any change to the checkboxes we push the current integer value into
    // the read-only text widget so the operator always sees the exact numeric
    // representation of the current alert state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We update the read-only log-level text field to display the current value
     * of {@code logLevelValue} as a decimal string.
     */
    private void setLogLevelText()
    {
        logLevelText.setText( Integer.toString( logLevelValue ) );
    }


    // ── METHOD: createLogLevelArea — WIRING UP THE ALERT SWITCH PANEL ─────────
    // We lay out all thirteen category checkboxes in a five-column grid inside a
    // labeled group. None and Any occupy the top row as special controls;
    // the remaining checkboxes fill two rows of five, with three on the bottom
    // row centered by spacer labels on each side.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build the checkbox panel containing all log-level categories. The
     * layout mirrors the OpenLDAP documentation grouping: None/Any at the top,
     * alphabetical categories in the middle, and the remaining three at the
     * bottom.
     *
     * @param parent  the parent composite to attach the group to
     */
    private void createLogLevelArea( Composite parent )
    {
        Group logLevelGroup = BaseWidgetUtils.createGroup( parent, "Log Levels", 1 );
        logLevelGroup.setLayout( new GridLayout( 5, false ) );
        int pos = 0;

        // None and any, centered
        BaseWidgetUtils.createLabel( logLevelGroup, TABULATION, 1 );
        noneCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "None", 1 );
        BaseWidgetUtils.createLabel( logLevelGroup, TABULATION, 1 );
        anyCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Any", 1 );
        BaseWidgetUtils.createLabel( logLevelGroup, TABULATION, 1 );

        // The first 5 options
        aclCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "ACL", 1 );
        buttons[pos++] = aclCheckbox;
        argsCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Args", 1 );
        buttons[pos++] = argsCheckbox;
        berCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "BER", 1 );
        buttons[pos++] = berCheckbox;
        configCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Config", 1 );
        buttons[pos++] = configCheckbox;
        connsCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Conns", 1 );
        buttons[pos++] = connsCheckbox;

        // The next 5 options
        filterCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Filter", 1 );
        buttons[pos++] = filterCheckbox;
        packetsCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Packets", 1 );
        buttons[pos++] = packetsCheckbox;
        parseCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Parses", 1 );
        buttons[pos++] = parseCheckbox;
        shellCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Shell", 1 );
        buttons[pos++] = shellCheckbox;
        statsCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Stats", 1 );
        buttons[pos++] = statsCheckbox;

        // The last 3 options, centered
        BaseWidgetUtils.createLabel( logLevelGroup, TABULATION, 1 );
        stats2Checkbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Stats2", 1 );
        buttons[pos++] = stats2Checkbox;
        traceCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Trace", 1 );
        buttons[pos++] = traceCheckbox;
        syncCheckbox = BaseWidgetUtils.createCheckbox( logLevelGroup, "Sync", 1 );
        buttons[pos++] = syncCheckbox;
        BaseWidgetUtils.createLabel( logLevelGroup, TABULATION, 1 );
    }


    // ── METHOD: createLogLevelValueArea — INSTALLING THE NUMERIC READOUT ──────
    // Beneath the switch panel we add a read-only text field that always shows
    // the current integer representation of the selected alert state. The field
    // is limited to five characters because the maximum useful value is 16383.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build the read-only numeric display area below the checkbox panel.
     * The text field is not editable — it simply reflects the computed integer
     * value of the current checkbox combination.
     *
     * @param parent  the parent composite to attach the group to
     */
    private void createLogLevelValueArea( Composite parent )
    {
        Group logLevelValueGroup = BaseWidgetUtils.createGroup( parent, "LogLevel Value", 1 );
        logLevelText = BaseWidgetUtils.createText( logLevelValueGroup, Integer.toString( logLevelValue ), 1 );
        logLevelText.setTextLimit( 5 );
        logLevelText.setEditable( false );
    }


    // ── METHOD: addListeners — CONNECTING THE ALERT SWITCHES ─────────────────
    // Each checkbox gets wired to the shared selection listener so that any
    // toggle immediately recomputes the integer and refreshes the readout.
    // None and Any get the same listener but live outside the buttons array.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We attach the shared {@code checkboxSelectionListener} to every checkbox,
     * including the special None and Any controls that live outside the main
     * buttons array.
     */
    private void addListeners()
    {
        noneCheckbox.addSelectionListener( checkboxSelectionListener );

        for ( Button button : buttons )
        {
            button.addSelectionListener( checkboxSelectionListener );
        }

        anyCheckbox.addSelectionListener( checkboxSelectionListener );
    }


    // ── METHOD: computeLogValue — CALCULATING THE THREAT LEVEL INTEGER ────────
    // We inspect each checkbox and OR its bit value into the running total.
    // None forces the result to 0; Any forces it to -1. For individual
    // selections we also handle the edge case where we are cancelling an
    // existing ANY selection (resetting from -1 to 0 first).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We recompute {@code logLevelValue} from the current checkbox states. If
     * None is checked the result is 0; if Any is checked the result is -1;
     * otherwise we OR together the bit values of every checked category checkbox.
     */
    private void computeLogValue()
    {
        if ( noneCheckbox.getSelection() )
        {
            logLevelValue = 0;
        }
        else if ( anyCheckbox.getSelection() )
        {
            logLevelValue = -1;
        }
        else
        {
            if ( logLevelValue == LogLevelEnum.ANY.getValue() )
            {
                // We cancel the ANY selection, so we have to set the LogLevelValue
                // to 0, as it's currently -1
                logLevelValue = 0;
            }

            // Now, check all the checkBox selections
            if ( aclCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.ACL.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.ACL.getValue();
            }

            if ( argsCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.ARGS.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.ARGS.getValue();
            }

            if ( berCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.BER.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.BER.getValue();
            }

            if ( configCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.CONFIG.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.CONFIG.getValue();
            }

            if ( connsCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.CONNS.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.CONNS.getValue();
            }

            if ( filterCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.FILTER.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.FILTER.getValue();
            }

            if ( packetsCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.PACKETS.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.PACKETS.getValue();
            }

            if ( parseCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.PARSE.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.PARSE.getValue();
            }

            if ( shellCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.SHELL.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.SHELL.getValue();
            }

            if ( statsCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.STATS.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.STATS.getValue();
            }

            if ( stats2Checkbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.STATS2.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.STATS2.getValue();
            }

            if ( syncCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.SYNC.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.SYNC.getValue();
            }

            if ( traceCheckbox.getSelection() )
            {
                logLevelValue |= LogLevelEnum.TRACE.getValue();
            }
            else
            {
                logLevelValue &= ~LogLevelEnum.TRACE.getValue();
            }
        }
    }


    // ── METHOD: getLogLevelValue — READING THE CONSOLE OUTPUT ────────────────
    // After the operator confirms the settings, any component that needs the
    // resulting integer can call this to retrieve the computed alert level.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the integer bitmask that encodes the currently selected log
     * level categories. This value is computed fresh when OK is pressed and
     * also updated live as checkboxes are toggled.
     *
     * @return  the computed integer that codes for the selected LogLevels
     */
    public int getLogLevelValue()
    {
        return logLevelValue;
    }
}
