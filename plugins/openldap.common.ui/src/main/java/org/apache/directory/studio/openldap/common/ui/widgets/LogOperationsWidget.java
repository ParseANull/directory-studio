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
package org.apache.directory.studio.openldap.common.ui.widgets;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.openldap.common.ui.model.LogOperationEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: LogOperationsWidget — REBEL INTELLIGENCE MONITORING TRANSMISSIONS ──
// Picture Rebel Intelligence's monitoring console at Echo Base. The console has
// three channel groups: Write operations (add, delete, modify, modify-RDN),
// Read operations (compare, search), and Session operations (abandon, bind,
// unbind). An "All operations" master switch covers everything at once. Each
// group header checkbox uses a grayed tri-state when only some of its sub-
// operations are checked. We fire change listeners whenever any checkbox
// changes so the parent form stays informed of which transmissions to intercept.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We provide a hierarchical checkbox widget for selecting which LDAP access-log
 * operations to monitor. We organise the thirteen individual checkboxes into
 * three groups (Write, Read, Session) with a master "All operations" checkbox,
 * and we expose the selection as a {@link List} of {@link LogOperationEnum}
 * constants. We extend {@link AbstractWidget} so change listeners can be
 * registered.
 */
public class LogOperationsWidget extends AbstractWidget
{
    // UI widgets
    private Composite composite;
    private Composite writeOperationsComposite;
    private Composite readOperationsComposite;
    private Composite sessionOperationsComposite;
    private Button allOperationsCheckbox;
    private Button writeOperationsCheckbox;
    private Button addOperationCheckbox;
    private Button deleteOperationCheckbox;
    private Button modifyOperationCheckbox;
    private Button modifyRdnOperationCheckbox;
    private Button readOperationsCheckbox;
    private Button compareOperationCheckbox;
    private Button searchOperationCheckbox;
    private Button sessionOperationsCheckbox;
    private Button abandonOperationCheckbox;
    private Button bindOperationCheckbox;
    private Button unbindOperationCheckbox;

    // Listeners
    private SelectionAdapter allOperationsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            allOperationsCheckboxesSetSelection( allOperationsCheckbox.getSelection() );
            notifyListeners();
        }
    };
    private SelectionAdapter writeOperationsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            writeOperationsCheckboxesSetSelection( writeOperationsCheckbox.getSelection() );
            checkAllOperationsCheckboxSelectionState();
            notifyListeners();
        }
    };
    private SelectionAdapter writeOperationCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            checkWriteOperationsCheckboxSelectionState();
            checkAllOperationsCheckboxSelectionState();
            notifyListeners();
        }
    };
    private SelectionAdapter readOperationsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            readOperationsCheckboxesSetSelection( readOperationsCheckbox.getSelection() );
            checkAllOperationsCheckboxSelectionState();
            notifyListeners();
        }
    };
    private SelectionAdapter readOperationCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            checkReadOperationsCheckboxSelectionState();
            checkAllOperationsCheckboxSelectionState();
            notifyListeners();
        }
    };
    private SelectionAdapter sessionOperationsCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            sessionOperationsCheckboxesSetSelection( sessionOperationsCheckbox.getSelection() );
            checkAllOperationsCheckboxSelectionState();
            notifyListeners();
        }
    };
    private SelectionAdapter sessionOperationCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            checkSessionOperationsCheckboxSelectionState();
            checkAllOperationsCheckboxSelectionState();
            notifyListeners();
        }
    };


    // ── METHOD: create — BRINGING THE MONITORING CONSOLE ONLINE ──────────────
    // We build the three-column composite, create all thirteen checkboxes in
    // their correct groups, and wire up all the listeners so the console is
    // ready to track which transmissions the administrator wants to intercept.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create all SWT controls for this widget inside the given parent. We lay
     * out a master "All operations" row and three group columns (Write, Read,
     * Session), each with their own indented sub-operation checkboxes, then
     * attach all listeners.
     *
     * @param parent  the parent {@link Composite}
     */
    public void create( Composite parent )
    {
        // Creating the widget base composite
        composite = new Composite( parent, SWT.NONE );
        GridLayout compositeGridLayout = new GridLayout( 3, true );
        compositeGridLayout.marginHeight = compositeGridLayout.marginWidth = 0;
        compositeGridLayout.verticalSpacing = compositeGridLayout.horizontalSpacing = 0;
        composite.setLayout( compositeGridLayout );

        // All Operations Checkbox
        allOperationsCheckbox = BaseWidgetUtils.createCheckbox( composite, "All operations", 3 );

        // Write Operations Checkbox
        writeOperationsCheckbox = BaseWidgetUtils.createCheckbox( composite, "Write operations", 1 );

        // Read Operations Checkbox
        readOperationsCheckbox = BaseWidgetUtils.createCheckbox( composite, "Read operations", 1 );

        // Session Operations Checkbox
        sessionOperationsCheckbox = BaseWidgetUtils.createCheckbox( composite, "Session operations", 1 );

        // Write Operations Composite
        writeOperationsComposite = new Composite( composite, SWT.NONE );
        GridLayout writeOperationsCompositeGridLayout = new GridLayout( 2, false );
        writeOperationsCompositeGridLayout.marginHeight = writeOperationsCompositeGridLayout.marginWidth = 0;
        writeOperationsCompositeGridLayout.verticalSpacing = writeOperationsCompositeGridLayout.horizontalSpacing = 0;
        writeOperationsComposite.setLayout( writeOperationsCompositeGridLayout );
        writeOperationsComposite.setLayoutData( new GridData( SWT.NONE, SWT.NONE, false, false ) );

        // Read Operations Composite
        readOperationsComposite = new Composite( composite, SWT.NONE );
        GridLayout readOperationsCompositeGridLayout = new GridLayout( 2, false );
        readOperationsCompositeGridLayout.marginHeight = readOperationsCompositeGridLayout.marginWidth = 0;
        readOperationsCompositeGridLayout.verticalSpacing = readOperationsCompositeGridLayout.horizontalSpacing = 0;
        readOperationsComposite.setLayout( readOperationsCompositeGridLayout );
        readOperationsComposite.setLayoutData( new GridData( SWT.NONE, SWT.NONE, false, false ) );

        // Session Operations Composite
        sessionOperationsComposite = new Composite( composite, SWT.NONE );
        GridLayout sessionOperationsCompositeGridLayout = new GridLayout( 2, false );
        sessionOperationsCompositeGridLayout.marginHeight = sessionOperationsCompositeGridLayout.marginWidth = 0;
        sessionOperationsCompositeGridLayout.verticalSpacing = sessionOperationsCompositeGridLayout.horizontalSpacing = 0;
        sessionOperationsComposite.setLayout( sessionOperationsCompositeGridLayout );
        sessionOperationsComposite.setLayoutData( new GridData( SWT.NONE, SWT.NONE, false, false ) );

        // Add Operation Checkbox
        BaseWidgetUtils.createRadioIndent( writeOperationsComposite, 1 );
        addOperationCheckbox = BaseWidgetUtils.createCheckbox( writeOperationsComposite, "Add", 1 );

        // Delete Operation Checkbox
        BaseWidgetUtils.createRadioIndent( writeOperationsComposite, 1 );
        deleteOperationCheckbox = BaseWidgetUtils.createCheckbox( writeOperationsComposite, "Delete", 1 );

        // Modify Operation Checkbox
        BaseWidgetUtils.createRadioIndent( writeOperationsComposite, 1 );
        modifyOperationCheckbox = BaseWidgetUtils.createCheckbox( writeOperationsComposite, "Modify", 1 );

        // Modify RDN Operation Checkbox
        BaseWidgetUtils.createRadioIndent( writeOperationsComposite, 1 );
        modifyRdnOperationCheckbox = BaseWidgetUtils.createCheckbox( writeOperationsComposite, "Modify RDN", 1 );

        // Compare Operation Checkbox
        BaseWidgetUtils.createRadioIndent( readOperationsComposite, 1 );
        compareOperationCheckbox = BaseWidgetUtils.createCheckbox( readOperationsComposite, "Compare", 1 );

        // Search Operation Checkbox
        BaseWidgetUtils.createRadioIndent( readOperationsComposite, 1 );
        searchOperationCheckbox = BaseWidgetUtils.createCheckbox( readOperationsComposite, "Search", 1 );

        // Abandon Operation Checkbox
        BaseWidgetUtils.createRadioIndent( sessionOperationsComposite, 1 );
        abandonOperationCheckbox = BaseWidgetUtils.createCheckbox( sessionOperationsComposite, "Abandon", 1 );

        // Bind Operation Checkbox
        BaseWidgetUtils.createRadioIndent( sessionOperationsComposite, 1 );
        bindOperationCheckbox = BaseWidgetUtils.createCheckbox( sessionOperationsComposite, "Bind", 1 );

        // Unbind Operation Checkbox
        BaseWidgetUtils.createRadioIndent( sessionOperationsComposite, 1 );
        unbindOperationCheckbox = BaseWidgetUtils.createCheckbox( sessionOperationsComposite, "Unbind", 1 );

        // Adding the listeners to the UI widgets
        addListeners();
    }


    // ── METHOD: adapt — PAINTING THE CONSOLE IN ECLIPSE FORMS COLORS ──────────
    // We adapt all composites to the supplied {@link FormToolkit} so they render
    // correctly inside Eclipse Forms pages without a white-on-gray background mismatch.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We adapt all internal composites to the given {@link FormToolkit} so the
     * widget renders correctly inside Eclipse Forms pages.
     *
     * @param toolkit  the form toolkit to adapt with, or {@code null} to skip
     */
    public void adapt( FormToolkit toolkit )
    {
        if ( toolkit != null )
        {
            toolkit.adapt( composite );
            toolkit.adapt( writeOperationsComposite );
            toolkit.adapt( readOperationsComposite );
            toolkit.adapt( sessionOperationsComposite );
        }
    }


    // ── METHOD: getControl — HANDING OVER THE CONSOLE PANEL ──────────────────
    // We return the top-level composite so the parent layout can size and
    // position the entire monitoring console as a unit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the top-level {@link Control} (the outer composite) for this
     * widget so the parent layout can size and position it.
     *
     * @return the primary composite control
     */
    public Control getControl()
    {
        return composite;
    }


    // ── METHOD: addListeners — ACTIVATING ALL CHANNEL MONITORS ───────────────
    // We attach the pre-built listener instances to every checkbox so every
    // user interaction triggers the correct propagation of state through the
    // hierarchy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We attach our pre-built selection listeners to all thirteen checkboxes.
     */
    private void addListeners()
    {
        allOperationsCheckbox.addSelectionListener( allOperationsCheckboxListener );
        writeOperationsCheckbox.addSelectionListener( writeOperationsCheckboxListener );
        addOperationCheckbox.addSelectionListener( writeOperationCheckboxListener );
        deleteOperationCheckbox.addSelectionListener( writeOperationCheckboxListener );
        modifyOperationCheckbox.addSelectionListener( writeOperationCheckboxListener );
        modifyRdnOperationCheckbox.addSelectionListener( writeOperationCheckboxListener );
        readOperationsCheckbox.addSelectionListener( readOperationsCheckboxListener );
        compareOperationCheckbox.addSelectionListener( readOperationCheckboxListener );
        searchOperationCheckbox.addSelectionListener( readOperationCheckboxListener );
        sessionOperationsCheckbox.addSelectionListener( sessionOperationsCheckboxListener );
        abandonOperationCheckbox.addSelectionListener( sessionOperationCheckboxListener );
        bindOperationCheckbox.addSelectionListener( sessionOperationCheckboxListener );
        unbindOperationCheckbox.addSelectionListener( sessionOperationCheckboxListener );
    }


    // ── METHOD: allOperationsCheckboxesSetSelection — SWEEPING ALL CHANNELS ───
    // When the master "All operations" switch is thrown we propagate the same
    // selection value to every group and sub-operation checkbox so the entire
    // console flips together.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the selection of every checkbox in the widget to the given value,
     * clearing the grayed state on the master checkbox first.
     *
     * @param selection  {@code true} to check all, {@code false} to uncheck all
     */
    private void allOperationsCheckboxesSetSelection( boolean selection )
    {
        allOperationsCheckbox.setGrayed( false );
        allOperationsCheckbox.setSelection( selection );
        writeOperationsCheckboxesSetSelection( selection );
        readOperationsCheckboxesSetSelection( selection );
        sessionOperationsCheckboxesSetSelection( selection );
    }


    // ── METHOD: writeOperationsCheckboxesSetSelection — SWITCHING WRITE CHANNEL
    // We set the Write group header and all four write sub-operation checkboxes
    // to the given selection value, clearing the grayed state first.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the selection of the Write group header and all four write
     * sub-operation checkboxes (Add, Delete, Modify, ModifyRDN).
     *
     * @param selection  {@code true} to check all Write checkboxes, {@code false} to uncheck them
     */
    private void writeOperationsCheckboxesSetSelection( boolean selection )
    {
        writeOperationsCheckbox.setGrayed( false );
        writeOperationsCheckbox.setSelection( selection );
        addOperationCheckbox.setSelection( selection );
        deleteOperationCheckbox.setSelection( selection );
        modifyOperationCheckbox.setSelection( selection );
        modifyRdnOperationCheckbox.setSelection( selection );
    }


    // ── METHOD: readOperationsCheckboxesSetSelection — SWITCHING READ CHANNEL ─
    // We set the Read group header and both read sub-operation checkboxes
    // to the given selection value, clearing the grayed state first.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the selection of the Read group header and both read sub-operation
     * checkboxes (Compare, Search).
     *
     * @param selection  {@code true} to check all Read checkboxes, {@code false} to uncheck them
     */
    private void readOperationsCheckboxesSetSelection( boolean selection )
    {
        readOperationsCheckbox.setGrayed( false );
        readOperationsCheckbox.setSelection( selection );
        compareOperationCheckbox.setSelection( selection );
        searchOperationCheckbox.setSelection( selection );
    }


    // ── METHOD: sessionOperationsCheckboxesSetSelection — SWITCHING SESSION CHANNEL
    // We set the Session group header and all three session sub-operation
    // checkboxes to the given selection value, clearing the grayed state first.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the selection of the Session group header and all three session
     * sub-operation checkboxes (Abandon, Bind, Unbind).
     *
     * @param selection  {@code true} to check all Session checkboxes, {@code false} to uncheck them
     */
    private void sessionOperationsCheckboxesSetSelection( boolean selection )
    {
        sessionOperationsCheckbox.setGrayed( false );
        sessionOperationsCheckbox.setSelection( selection );
        abandonOperationCheckbox.setSelection( selection );
        bindOperationCheckbox.setSelection( selection );
        unbindOperationCheckbox.setSelection( selection );
    }


    // ── METHOD: checkAllOperationsCheckboxSelectionState — AUDITING THE MASTER SWITCH
    // After any sub-operation change we recompute whether all nine leaf checkboxes
    // are checked (full), some are checked (grayed), or none are (unchecked) and
    // update the master "All operations" checkbox accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We recompute the tri-state of the master "All operations" checkbox based
     * on whether all, some, or none of the nine leaf checkboxes are currently
     * checked.
     */
    private void checkAllOperationsCheckboxSelectionState()
    {
        boolean atLeastOneSelected = addOperationCheckbox.getSelection()
            || deleteOperationCheckbox.getSelection() || modifyOperationCheckbox.getSelection()
            || modifyRdnOperationCheckbox.getSelection() || compareOperationCheckbox.getSelection()
            || searchOperationCheckbox.getSelection() || abandonOperationCheckbox.getSelection()
            || bindOperationCheckbox.getSelection() || unbindOperationCheckbox.getSelection();
        boolean allSelected = addOperationCheckbox.getSelection()
            && deleteOperationCheckbox.getSelection() && modifyOperationCheckbox.getSelection()
            && modifyRdnOperationCheckbox.getSelection() && compareOperationCheckbox.getSelection()
            && searchOperationCheckbox.getSelection() && abandonOperationCheckbox.getSelection()
            && bindOperationCheckbox.getSelection() && unbindOperationCheckbox.getSelection();
        allOperationsCheckbox.setGrayed( atLeastOneSelected && !allSelected );
        allOperationsCheckbox.setSelection( atLeastOneSelected );
    }


    // ── METHOD: checkWriteOperationsCheckboxSelectionState — AUDITING WRITE CHANNEL
    // After any write sub-operation change we recompute the Write group header
    // tri-state to reflect partial vs. full vs. no selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We recompute the tri-state of the "Write operations" group header based
     * on whether all, some, or none of the four write sub-operation checkboxes
     * are currently checked (and not grayed).
     */
    private void checkWriteOperationsCheckboxSelectionState()
    {
        boolean atLeastOneSelected = isChecked( addOperationCheckbox )
            || isChecked( deleteOperationCheckbox ) || isChecked( modifyOperationCheckbox )
            || isChecked( modifyRdnOperationCheckbox );
        boolean allSelected = isChecked( addOperationCheckbox )
            && isChecked( deleteOperationCheckbox ) && isChecked( modifyOperationCheckbox )
            && isChecked( modifyRdnOperationCheckbox );
        writeOperationsCheckbox.setGrayed( atLeastOneSelected && !allSelected );
        writeOperationsCheckbox.setSelection( atLeastOneSelected );
    }


    // ── METHOD: checkReadOperationsCheckboxSelectionState — AUDITING READ CHANNEL
    // After any read sub-operation change we recompute the Read group header
    // tri-state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We recompute the tri-state of the "Read operations" group header based on
     * whether all, some, or none of the two read sub-operation checkboxes are
     * currently checked.
     */
    private void checkReadOperationsCheckboxSelectionState()
    {
        boolean atLeastOneSelected = isChecked( compareOperationCheckbox )
            || isChecked( searchOperationCheckbox );
        boolean allSelected = isChecked( compareOperationCheckbox )
            && isChecked( searchOperationCheckbox );
        readOperationsCheckbox.setGrayed( atLeastOneSelected && !allSelected );
        readOperationsCheckbox.setSelection( atLeastOneSelected );
    }


    // ── METHOD: checkSessionOperationsCheckboxSelectionState — AUDITING SESSION CHANNEL
    // After any session sub-operation change we recompute the Session group
    // header tri-state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We recompute the tri-state of the "Session operations" group header based
     * on whether all, some, or none of the three session sub-operation checkboxes
     * are currently checked.
     */
    private void checkSessionOperationsCheckboxSelectionState()
    {
        boolean atLeastOneSelected = isChecked( abandonOperationCheckbox )
            || isChecked( bindOperationCheckbox ) || isChecked( unbindOperationCheckbox );
        boolean allSelected = isChecked( abandonOperationCheckbox )
            && isChecked( bindOperationCheckbox ) && isChecked( unbindOperationCheckbox );
        sessionOperationsCheckbox.setGrayed( atLeastOneSelected && !allSelected );
        sessionOperationsCheckbox.setSelection( atLeastOneSelected );
    }


    // ── METHOD: setInput — LOADING A PREVIOUS INTERCEPT PLAN ─────────────────
    // We reset all checkboxes and then select the ones that match the supplied
    // list of operations. After that we recompute all group header tri-states so
    // the console accurately reflects the loaded configuration.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We reset all checkboxes and then select the ones corresponding to the
     * supplied list of {@link LogOperationEnum} values. We finish by
     * recomputing all group-level tri-states.
     *
     * @param operationsList  the list of operations to select, or {@code null} to clear all
     */
    public void setInput( List<LogOperationEnum> operationsList )
    {
        // Reset all checkboxes
        resetAllCheckboxes();

        // Select checkboxes according to the log operations list
        if ( operationsList != null )
        {
            for ( LogOperationEnum logOperation : operationsList )
            {
                switch ( logOperation )
                {
                    case ALL:
                        allOperationsCheckbox.setSelection( true );
                        allOperationsCheckboxesSetSelection( true );
                        break;
                    case WRITES:
                        writeOperationsCheckbox.setSelection( true );
                        writeOperationsCheckboxesSetSelection( true );
                        break;
                    case ADD:
                        addOperationCheckbox.setSelection( true );
                        break;
                    case DELETE:
                        deleteOperationCheckbox.setSelection( true );
                        break;
                    case MODIFY:
                        modifyOperationCheckbox.setSelection( true );
                        break;
                    case MODIFY_RDN:
                        modifyRdnOperationCheckbox.setSelection( true );
                        break;
                    case READS:
                        readOperationsCheckbox.setSelection( true );
                        readOperationsCheckboxesSetSelection( true );
                        break;
                    case COMPARE:
                        compareOperationCheckbox.setSelection( true );
                        break;
                    case SEARCH:
                        searchOperationCheckbox.setSelection( true );
                        break;
                    case SESSION:
                        sessionOperationsCheckbox.setSelection( true );
                        sessionOperationsCheckboxesSetSelection( true );
                        break;
                    case ABANDON:
                        abandonOperationCheckbox.setSelection( true );
                        break;
                    case BIND:
                        bindOperationCheckbox.setSelection( true );
                        break;
                    case UNBIND:
                        unbindOperationCheckbox.setSelection( true );
                        break;
                }
            }
        }

        // Check hierarchical checkboxes
        checkWriteOperationsCheckboxSelectionState();
        checkReadOperationsCheckboxSelectionState();
        checkSessionOperationsCheckboxSelectionState();
        checkAllOperationsCheckboxSelectionState();
    }


    // ── METHOD: resetAllCheckboxes — CLEARING THE CONSOLE ────────────────────
    // We clear and un-gray every checkbox so the console returns to a fully
    // blank state before we load a new intercept plan.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We clear the selection and grayed state of all thirteen checkboxes,
     * returning the widget to a fully unchecked state.
     */
    private void resetAllCheckboxes()
    {
        allOperationsCheckbox.setSelection( false );
        allOperationsCheckbox.setGrayed( false );
        writeOperationsCheckbox.setSelection( false );
        writeOperationsCheckbox.setGrayed( false );
        addOperationCheckbox.setSelection( false );
        addOperationCheckbox.setGrayed( false );
        deleteOperationCheckbox.setSelection( false );
        deleteOperationCheckbox.setGrayed( false );
        modifyOperationCheckbox.setSelection( false );
        modifyOperationCheckbox.setGrayed( false );
        readOperationsCheckbox.setSelection( false );
        readOperationsCheckbox.setGrayed( false );
        compareOperationCheckbox.setSelection( false );
        compareOperationCheckbox.setGrayed( false );
        searchOperationCheckbox.setSelection( false );
        searchOperationCheckbox.setGrayed( false );
        sessionOperationsCheckbox.setSelection( false );
        sessionOperationsCheckbox.setGrayed( false );
        abandonOperationCheckbox.setSelection( false );
        abandonOperationCheckbox.setGrayed( false );
        bindOperationCheckbox.setSelection( false );
        bindOperationCheckbox.setGrayed( false );
        unbindOperationCheckbox.setSelection( false );
        unbindOperationCheckbox.setGrayed( false );
    }


    // ── METHOD: getSelectedOperationsList — READING THE ACTIVE INTERCEPTS ─────
    // We walk the checkbox hierarchy from the most general (ALL) down to the most
    // specific sub-operations, emitting the most specific matching constant for
    // each channel. A group header that is fully and non-grayedly checked emits
    // its group constant instead of individual sub-constants.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build and return a list of {@link LogOperationEnum} constants that
     * correspond to the currently checked (non-grayed) checkboxes. Group
     * constants (ALL, WRITES, READS, SESSION) are preferred over individual
     * sub-operation constants when all sub-operations within the group are
     * selected.
     *
     * @return the list of selected {@link LogOperationEnum} values
     */
    public List<LogOperationEnum> getSelectedOperationsList()
    {
        List<LogOperationEnum> logOperations = new ArrayList<LogOperationEnum>();

        // All operations
        if ( isChecked( allOperationsCheckbox ) )
        {
            logOperations.add( LogOperationEnum.ALL );
        }
        else
        {
            // Write operations
            if ( isChecked( writeOperationsCheckbox ) )
            {
                logOperations.add( LogOperationEnum.WRITES );
            }
            else
            {
                // Add operation
                if ( isChecked( addOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.ADD );
                }

                // Delete operation
                if ( isChecked( deleteOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.DELETE );
                }

                // Modify operation
                if ( isChecked( modifyOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.MODIFY );
                }

                // Modify RDN operation
                if ( isChecked( modifyRdnOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.MODIFY_RDN );
                }
            }

            // Read operations
            if ( isChecked( readOperationsCheckbox ) )
            {
                logOperations.add( LogOperationEnum.READS );
            }
            else
            {
                // Compare operation
                if ( isChecked( compareOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.COMPARE );
                }

                // Search operation
                if ( isChecked( searchOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.SEARCH );
                }
            }

            // Session operations
            if ( isChecked( sessionOperationsCheckbox ) )
            {
                logOperations.add( LogOperationEnum.SESSION );
            }
            else
            {
                // Abandon operation
                if ( isChecked( abandonOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.ABANDON );
                }

                // Bind operation
                if ( isChecked( bindOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.BIND );
                }

                // Unbind operation
                if ( isChecked( unbindOperationCheckbox ) )
                {
                    logOperations.add( LogOperationEnum.UNBIND );
                }
            }
        }

        return logOperations;
    }


    // ── METHOD: isChecked — VERIFYING A CHECKBOX IS TRULY ON ─────────────────
    // A checkbox can be selected but also grayed (indeterminate), which is
    // visually checked but logically partial. We only return true if the button
    // is non-null, not disposed, selected, and not grayed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} only if the given checkbox is non-null, not
     * disposed, selected, and not in the grayed (indeterminate) state.
     *
     * @param checkbox  the {@link Button} checkbox to inspect
     * @return          {@code true} if the checkbox is fully checked
     */
    private boolean isChecked( Button checkbox )
    {
        return ( ( checkbox != null ) && ( !checkbox.isDisposed() ) && ( checkbox.getSelection() ) && ( !checkbox
            .getGrayed() ) );
    }


    // ── METHOD: dispose — SHUTTING DOWN THE CONSOLE ───────────────────────────
    // We dispose the top-level composite and all its children to free SWT
    // resources when the parent dialog or editor is closed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We dispose the top-level composite (and all its child controls) if it has
     * not already been disposed.
     */
    public void dispose()
    {
        // Composite
        if ( ( composite != null ) && ( !composite.isDisposed() ) )
        {
            composite.dispose();
        }
    }
}
