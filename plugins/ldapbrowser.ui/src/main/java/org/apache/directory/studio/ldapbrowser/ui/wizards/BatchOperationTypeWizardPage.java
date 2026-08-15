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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: BatchOperationTypeWizardPage — LUKE CHOOSES HIS WEAPON ─────────────
// Luke stands in the armoury: three choices in front of him — a modification
// (change some attributes), a deletion (remove entries entirely), or an LDIF
// changetype that the user writes from scratch. He picks one and the wizard
// routes him to the right preparation room next. This page presents those three
// radio buttons and validates that exactly one is chosen.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Second page of the batch operation wizard: lets the user pick the operation type.
 * Three options are presented: Modify (change attributes using the GUI),
 * Delete (remove all target entries), or Execute-LDIF (write a raw LDIF fragment).
 * The choice here determines which of the next two pages the wizard shows.
 * Think of Luke choosing his weapon in the armoury: each choice leads down
 * a different corridor to the next stage of preparation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BatchOperationTypeWizardPage extends WizardPage
{

    /** Sentinel value meaning no operation type has been chosen yet. */
    public final static int OPERATION_TYPE_NONE = -1;

    /** Apply a modify changetype to every target entry. */
    public final static int OPERATION_TYPE_MODIFY = 0;

    /** Apply a delete changetype to every target entry. */
    public final static int OPERATION_TYPE_DELETE = 1;

    /** Execute a user-written raw LDIF changetype on every target entry. */
    public final static int OPERATION_TYPE_CREATE_LDIF = 2;

    private final static String[] OPERATION_TYPES =
        {
            Messages.getString( "BatchOperationTypeWizardPage.ModifyEntries" ), Messages.getString( "BatchOperationTypeWizardPage.DeleteEntries" ), Messages.getString( "BatchOperationTypeWizardPage.ExecuteLDIFChangetype" ) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private Button[] operationTypeButtons;


    // ── Luke Reads the Armoury Sign ───────────────────────────────────────────────
    // The sign above the armoury door says which tools are available;
    // the page title and description set those expectations.
    // We start incomplete so the user must make a choice before moving on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BatchOperationTypeWizardPage, starting incomplete so the
     * user must explicitly choose an operation type.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent batch operation wizard (unused here, kept for
     *                  consistency with other pages that need the wizard reference).
     */
    public BatchOperationTypeWizardPage( String pageName, BatchOperationWizard wizard )
    {
        super( pageName );
        super.setTitle( Messages.getString( "BatchOperationTypeWizardPage.SelectOperationType" ) ); //$NON-NLS-1$
        super.setDescription( Messages.getString( "BatchOperationTypeWizardPage.PleaseSelectBatch" ) ); //$NON-NLS-1$
        super.setPageComplete( false );
    }


    private void validate()
    {
        setPageComplete( getOperationType() != OPERATION_TYPE_NONE );
    }


    // ── Luke Surveys the Armoury ─────────────────────────────────────────────────
    // Three radio buttons on the wall, each labelled. Luke picks one and the
    // choice lights up. The first one starts selected by default.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Renders three radio buttons, one per operation type, in a single-column grid.
     * The first button (Modify) is selected by default. Each button triggers
     * validation so the "Next" button activates immediately.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {

        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        operationTypeButtons = new Button[OPERATION_TYPES.length];
        for ( int i = 0; i < operationTypeButtons.length; i++ )
        {
            operationTypeButtons[i] = BaseWidgetUtils.createRadiobutton( composite, OPERATION_TYPES[i], 1 );
            operationTypeButtons[i].addSelectionListener( new SelectionListener()
            {
                public void widgetDefaultSelected( SelectionEvent e )
                {
                    validate();
                }


                public void widgetSelected( SelectionEvent e )
                {
                    validate();
                }
            } );
        }
        operationTypeButtons[0].setSelection( true );

        validate();

        setControl( composite );

    }


    // ── Luke Checks What He Picked ────────────────────────────────────────────────
    // Luke glances back at the armoury to confirm which tool he grabbed —
    // the wizard needs this to route him to the right next page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected operation type constant:
     * {@code OPERATION_TYPE_MODIFY}, {@code OPERATION_TYPE_DELETE},
     * {@code OPERATION_TYPE_CREATE_LDIF}, or {@code OPERATION_TYPE_NONE} if none
     * of the buttons is selected (which shouldn't happen in normal use).
     *
     * @return  the selected operation type constant.
     */
    public int getOperationType()
    {

        for ( int i = 0; i < operationTypeButtons.length; i++ )
        {
            if ( operationTypeButtons[i].getSelection() )
            {
                return i;
            }
        }

        return OPERATION_TYPE_NONE;
    }

}
