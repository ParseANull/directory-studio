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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.apache.directory.studio.openldap.config.editor.wrappers.TimeLimitWrapper;


// Like Mace Windu confronting Palpatine and demanding the server prove
// it has legitimate time boundaries before he lets operations proceed,
// we present a focused dialog that forces the administrator to declare
// soft, hard, and global time limits — refusing to close until the
// values are all legal and consistent.
/**
 * The TimeLimitDialog is used to edit the TimeLimit parameter. The TimeLimit
 * grammar is:
 * <pre>
 * time      ::= 'time' timeLimit time-e
 * time-e    ::= 'time' timeLimit time-e | e
 * timeLimit ::= '.soft=' limit | '.hard=' hardLimit | '=' limit
 * limit     ::= 'unlimited' | 'none' | INT
 * hardLimit ::= 'soft' | limit
 * </pre>
 *
 * <p>The dialog overlay is like:
 * <pre>
 * +-------------------------------------------------------+
 * | Time Limit                                            |
 * | .---------------------------------------------------. |
 * | | Soft Limit :  [----------]  [] Unlimited          | |
 * | |                                                   | |
 * | | Hard Limit :  [----------]  [] Unlimited  [] Soft | |
 * | |                                                   | |
 * | | Global :      [----------]  [] Unlimited          | |
 * | '---------------------------------------------------' |
 * | Resulting Time Limit                                  |
 * | .---------------------------------------------------. |
 * | | Time Limit  : &lt;/////////////////////////////////&gt; | |
 * | '---------------------------------------------------' |
 * |                                                       |
 * |  (Cancel)                                      (OK)   |
 * +-------------------------------------------------------+
 * </pre>
 *
 * <p>A few rules:
 * <ul>
 * <li>When the global limit is set, the soft and hard limits are not used</li>
 * <li>When the Unlimited button is checked, the integer value is discarded</li>
 * <li>When the Soft checkbox for the hard limit is checked, the Global value is used</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TimeLimitDialog extends AbstractLimitDialog<TimeLimitWrapper>
{
    // Like Mace Windu arriving at the Senate chamber with no pre-formed
    // judgment and a clean slate so the confrontation starts from a neutral
    // baseline, we create the dialog with no pre-loaded time limit data.
    /**
     * Creates a new TimeLimitDialog with no pre-loaded time limit value.
     * The RESIZE style lets the operator expand the dialog if needed.
     *
     * @param parentShell the parent shell
     */
    public TimeLimitDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like Mace Windu arriving at the confrontation already briefed on the
    // current situation so he can interrogate the existing limit values
    // rather than starting from scratch, we parse the provided time limit
    // string into a wrapper and pre-populate the dialog fields.
    /**
     * Creates a new TimeLimitDialog pre-populated with the given time limit
     * string, which is parsed into a {@link TimeLimitWrapper} for editing.
     *
     * @param parentShell the parent shell
     * @param timeLimitStr the string encoding of the time limit to edit
     */
    public TimeLimitDialog( Shell parentShell, String timeLimitStr )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );

        setEditedElement( new TimeLimitWrapper( timeLimitStr ) );
    }


    // Like Mace Windu naming the confrontation so everyone in the Senate
    // chamber understands what they're witnessing, we stamp the dialog
    // shell with the "Time Limit" title.
    /**
     * Configures the dialog shell by setting its title to "Time Limit".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Time Limit" );
    }


    // Like Mace Windu laying out the terms of the confrontation —
    // soft limit, hard limit, global limit — so Palpatine must address
    // each one before the hearing can close, we build the full dialog
    // area with both the input group and the result display.
    /**
     * Creates the dialog content area with a time limit input group and
     * a read-only result group. After building the UI we initialize the
     * fields from the edited element and wire up the listeners.
     *
     * <pre>
     * +-------------------------------------------------------+
     * | Time Limit                                            |
     * | .---------------------------------------------------. |
     * | | Soft Limit :  [----------]  [] Unlimited          | |
     * | |                                                   | |
     * | | Hard Limit :  [----------]  [] Unlimited  [] Soft | |
     * | |                                                   | |
     * | | Global :      [----------]  [] Unlimited          | |
     * | '---------------------------------------------------' |
     * | Resulting Time Limit                                  |
     * | .---------------------------------------------------. |
     * | | Time Limit  : &lt;/////////////////////////////////&gt; | |
     * | '---------------------------------------------------' |
     * |                                                       |
     * |  (Cancel)                                      (OK)   |
     * +-------------------------------------------------------+
     * </pre>
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        createTimeLimitEditGroup( composite );
        createTimeLimitShowGroup( composite );

        initDialog();
        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like Mace Windu's three-part interrogation — presenting each limit
    // category (soft, hard, global) as a distinct line of questioning —
    // we build the input group with a separate row per limit type.
    /**
     * Creates the TimeLimit input group where the operator enters soft,
     * hard, and global limit values with their associated Unlimited
     * (and Soft) checkboxes.
     *
     * <pre>
     * TcpBuffer Input
     * .---------------------------------------------------.
     * | Soft Limit :  [----------]  [] Unlimited          |
     * |                                                   |
     * | Hard Limit :  [----------]  [] Unlimited  [] Soft |
     * |                                                   |
     * | Global :      [----------]  [] Unlimited          |
     * '---------------------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the input group to
     */
    private void createTimeLimitEditGroup( Composite parent )
    {
        // TimeLimit Group
        Group timeLimitGroup = BaseWidgetUtils.createGroup( parent, "Time Limit input", 1 );
        GridLayout timeLimitGridLayout = new GridLayout( 6, false );
        timeLimitGroup.setLayout( timeLimitGridLayout );
        timeLimitGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // SoftLimit Text
        BaseWidgetUtils.createLabel( timeLimitGroup, "Soft Limit :", 1 );
        softLimitText = BaseWidgetUtils.createText( timeLimitGroup, "", 1 );

        // Soft Limit unlimited checkbox Button
        softUnlimitedCheckbox = BaseWidgetUtils.createCheckbox( timeLimitGroup, "Unlimited", 2 );

        // 2 tabs to fill the line
        BaseWidgetUtils.createLabel( timeLimitGroup, "", 2 );

        // HardLimit Text
        BaseWidgetUtils.createLabel( timeLimitGroup, "Hard Limit :", 1 );
        hardLimitText = BaseWidgetUtils.createText( timeLimitGroup, "", 1 );

        // Hard Limit unlimited checkbox Button
        hardUnlimitedCheckbox = BaseWidgetUtils.createCheckbox( timeLimitGroup, "Unlimited", 2 );

        // HardLimit soft checkbox Button
        hardSoftCheckbox = BaseWidgetUtils.createCheckbox( timeLimitGroup, "Soft", 2 );

        // GlobalLimit Text
        BaseWidgetUtils.createLabel( timeLimitGroup, "Global Limit :", 1 );
        globalLimitText = BaseWidgetUtils.createText( timeLimitGroup, "", 1 );

        // GLobal Limit unlimited checkbox Button
        globalUnlimitedCheckbox = BaseWidgetUtils.createCheckbox( timeLimitGroup, "Unlimited", 2 );

        // 2 tabs to fill the line
        BaseWidgetUtils.createLabel( timeLimitGroup, "", 2 );
    }


    // Like the Senate clerk displaying the official verdict of the confrontation
    // so everyone can read the resulting judgment in one place, we create
    // the read-only result group that shows the combined time limit string.
    /**
     * Creates the TimeLimit display group, which shows the resulting time
     * limit string (or an error indicator) as the operator adjusts values.
     *
     * <pre>
     * Resulting Time Limit
     * .------------------------------------.
     * | Time Limit : &lt;///////////////////&gt; |
     * '------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the display group to
     */
    private void createTimeLimitShowGroup( Composite parent )
    {
        // TimeLimit Group
        Group timeLimitGroup = BaseWidgetUtils.createGroup( parent, "Resulting Time Limit", 1 );
        GridLayout timeLimitGroupGridLayout = new GridLayout( 2, false );
        timeLimitGroup.setLayout( timeLimitGroupGridLayout );
        timeLimitGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // TimeLimit Text
        limitText = BaseWidgetUtils.createText( timeLimitGroup, "", 1 );
        limitText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        limitText.setEditable( false );
    }


    // Like Mace Windu wiring the listening devices so every word spoken
    // in the confrontation is captured and feeds the live verdict display,
    // we attach modify and selection listeners to every limit input widget.
    /**
     * Attaches modify and selection listeners to all limit input widgets
     * so the dialog validates input and updates the result display on
     * every change the operator makes.
     */
    private void addListeners()
    {
        softLimitText.addModifyListener( softLimitTextListener );
        softUnlimitedCheckbox.addSelectionListener( softUnlimitedCheckboxSelectionListener );
        hardLimitText.addModifyListener( hardLimitTextListener );
        hardUnlimitedCheckbox.addSelectionListener( hardUnlimitedCheckboxSelectionListener );
        hardSoftCheckbox.addSelectionListener( hardSoftCheckboxSelectionListener );
        globalLimitText.addModifyListener( globalLimitTextListener );
        globalUnlimitedCheckbox.addSelectionListener( globalUnlimitedCheckboxSelectionListener );
    }


    // Like Mace Windu starting the confrontation from a clean slate when
    // no prior verdict exists, we seed the edited element with an empty
    // TimeLimitWrapper so the operator fills in a fresh time limit entry.
    /**
     * Seeds the dialog with a new empty {@link TimeLimitWrapper} when the
     * operator is adding a brand-new time limit value.
     */
    @Override
    public void addNewElement()
    {
        setEditedElement( new TimeLimitWrapper( "" ) );
    }
}
