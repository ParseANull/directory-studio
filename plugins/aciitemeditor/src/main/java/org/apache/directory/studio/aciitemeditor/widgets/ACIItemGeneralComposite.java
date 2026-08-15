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
package org.apache.directory.studio.aciitemeditor.widgets;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.AuthenticationLevel;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;


// ── CLASS: ACIItemGeneralComposite — GRAND MOFF'S HEADER PANEL ───────────────
// When the Grand Moff composes a new security directive, the first thing he sets
// is the header: a name tag, a priority rank (0–255), the required authentication
// level, and whether the directive is userFirst or itemFirst.
// ACIItemGeneralComposite is that header panel — four controls, one composite.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} that edits the four general properties of an ACI item:
 * identification tag, precedence (0–255), authentication level, and the
 * userFirst / itemFirst radio selection.
 * Lives inside the Visual Editor tab of the ACI item dialog.
 * Think of this as the Grand Moff's directive header panel: name it, rank it,
 * set the auth requirement, pick the form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemGeneralComposite extends Composite
{
    /** The identification tag text field */
    private Text identificationTagText = null;

    /**
     * The combo viewer is attached to authenticationLevelCombo to work with
     * AuthenticationLevel objects rather than Strings
     */
    private ComboViewer authenticationLevelComboViewer = null;

    /** The spinner to select a valid precedence between 0 and 255 */
    private Spinner precedenceSpinner = null;

    /** The user first radio button */
    private Button userFirstRadioButton = null;

    /** The item first radio button */
    private Button itemFirstRadioButton = null;

    /** The list with listers */
    private List<WidgetModifyListener> listenerList = new ArrayList<WidgetModifyListener>();


    // ── CONSTRUCT THE HEADER PANEL ────────────────────────────────────────────
    // The Grand Moff's header panel is created as a standard SWT composite
    // with zero margins so it fits flush inside its parent tab.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACIItemGeneralComposite}.
     * Lays out the four fields (identification tag, precedence, authentication
     * level, userFirst/itemFirst) and wires up their change listeners.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemGeneralComposite( Composite parent, int style )
    {
        super( parent, style );

        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout( layout );

        GridData layoutData = new GridData();
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.verticalAlignment = GridData.CENTER;
        setLayoutData( layoutData );

        createComposite();
    }


    // ── LAY OUT THE FOUR HEADER FIELDS ────────────────────────────────────────
    // The orderly renders the three-column grid: labels in column 1,
    // controls spanning columns 2 and 3.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the inner three-column grid with labels and input controls.
     */
    private void createComposite()
    {

        GridData identificationTagGridData = new GridData();
        identificationTagGridData.grabExcessHorizontalSpace = true;
        identificationTagGridData.verticalAlignment = GridData.CENTER;
        identificationTagGridData.horizontalSpan = 2;
        identificationTagGridData.horizontalAlignment = GridData.FILL;

        GridData precedenceGridData = new GridData();
        precedenceGridData.grabExcessHorizontalSpace = true;
        precedenceGridData.verticalAlignment = GridData.CENTER;
        precedenceGridData.horizontalSpan = 2;
        precedenceGridData.horizontalAlignment = GridData.BEGINNING;
        precedenceGridData.widthHint = 3 * 12;

        GridData authenticationLevelGridData = new GridData();
        authenticationLevelGridData.grabExcessHorizontalSpace = true;
        authenticationLevelGridData.verticalAlignment = GridData.CENTER;
        authenticationLevelGridData.horizontalSpan = 2;
        authenticationLevelGridData.horizontalAlignment = GridData.FILL;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.CENTER;

        Composite composite = new Composite( this, SWT.NONE );
        composite.setLayout( gridLayout );
        composite.setLayoutData( gridData );

        Label identificationTagLabel = new Label( composite, SWT.NONE );
        identificationTagLabel.setText( Messages.getString( "ACIItemGeneralComposite.idTag.label" ) ); //$NON-NLS-1$
        identificationTagText = new Text( composite, SWT.BORDER );
        identificationTagText.setLayoutData( identificationTagGridData );
        identificationTagText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent event )
            {
                fire( event );
            }
        } );

        Label precedenceLabel = new Label( composite, SWT.NONE );
        precedenceLabel.setText( Messages.getString( "ACIItemGeneralComposite.precedence.label" ) ); //$NON-NLS-1$
        precedenceSpinner = new Spinner( composite, SWT.BORDER );
        precedenceSpinner.setMinimum( 0 );
        precedenceSpinner.setMaximum( 255 );
        precedenceSpinner.setDigits( 0 );
        precedenceSpinner.setIncrement( 1 );
        precedenceSpinner.setPageIncrement( 10 );
        precedenceSpinner.setSelection( 0 );
        precedenceSpinner.setLayoutData( precedenceGridData );
        precedenceSpinner.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent event )
            {
                fire( event );
            }
        } );

        Label authenticationLevelLabel = new Label( composite, SWT.NONE );
        authenticationLevelLabel.setText( Messages.getString( "ACIItemGeneralComposite.authLevel.label" ) ); //$NON-NLS-1$

        Combo authenticationLevelCombo = new Combo( composite, SWT.READ_ONLY );
        authenticationLevelCombo.setLayoutData( authenticationLevelGridData );
        AuthenticationLevel[] authenticationLevels = new AuthenticationLevel[3];
        authenticationLevels[0] = AuthenticationLevel.NONE;
        authenticationLevels[1] = AuthenticationLevel.SIMPLE;
        authenticationLevels[2] = AuthenticationLevel.STRONG;
        authenticationLevelComboViewer = new ComboViewer( authenticationLevelCombo );
        authenticationLevelComboViewer.setContentProvider( new ArrayContentProvider() );
        authenticationLevelComboViewer.setLabelProvider( new LabelProvider() );
        authenticationLevelComboViewer.setInput( authenticationLevels );
        authenticationLevelComboViewer.setSelection( new StructuredSelection( AuthenticationLevel.NONE ) );
        authenticationLevelCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent event )
            {
                fire( event );
            }
        } );

        Label userOrItemFirstLabel = new Label( composite, SWT.NONE );
        userOrItemFirstLabel.setText( Messages.getString( "ACIItemGeneralComposite.userOrItemFirst.label" ) ); //$NON-NLS-1$
        userFirstRadioButton = new Button( composite, SWT.RADIO );
        userFirstRadioButton.setText( Messages.getString( "ACIItemGeneralComposite.userFirst.label" ) ); //$NON-NLS-1$
        userFirstRadioButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                fire( event );
            }
        } );
        itemFirstRadioButton = new Button( composite, SWT.RADIO );
        itemFirstRadioButton.setText( Messages.getString( "ACIItemGeneralComposite.itemFirst.label" ) ); //$NON-NLS-1$
        itemFirstRadioButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                fire( event );
            }
        } );

    }


    // ── REGISTER A CHANGE LISTENER ────────────────────────────────────────────
    // Any observer that needs to react when the officer edits the header fields
    // registers here.  The tab-folder composite uses this to keep the source
    // editor in sync.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link WidgetModifyListener} to be notified whenever any
     * header field changes.
     *
     * @param listener  the listener to add; must not be {@code null}
     */
    public void addWidgetModifyListener( WidgetModifyListener listener )
    {
        checkWidget();

        if ( listener == null )
        {
            SWT.error( SWT.ERROR_NULL_ARGUMENT );
        }

        listenerList.add( listener );
    }


    // ── UNREGISTER A CHANGE LISTENER ──────────────────────────────────────────
    /**
     * Removes a previously registered {@link WidgetModifyListener}.
     *
     * @param listener  the listener to remove; must not be {@code null}
     */
    public void removeWidgetModifyListener( WidgetModifyListener listener )
    {
        checkWidget();

        if ( listener == null )
        {
            SWT.error( SWT.ERROR_NULL_ARGUMENT );
        }

        listenerList.remove( listener );
    }


    // ── BROADCAST A CHANGE EVENT ──────────────────────────────────────────────
    // Any widget change fires a WidgetModifyEvent to all registered listeners
    // so the containing composite can update its state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires a {@link WidgetModifyEvent} to all registered listeners.
     *
     * @param event  the original SWT event that triggered this notification
     */
    private void fire( TypedEvent event )
    {
        for ( WidgetModifyListener listener : listenerList )
        {
            listener.widgetModified( new WidgetModifyEvent( this ) );
        }
    }


    // ── READ THE IDENTIFICATION TAG ───────────────────────────────────────────
    /**
     * Returns the current text in the identification tag field.
     *
     * @return the identification tag string
     */
    public String getIdentificationTag()
    {
        return identificationTagText.getText();
    }


    // ── SET THE IDENTIFICATION TAG ────────────────────────────────────────────
    /**
     * Sets the identification tag field to {@code identificationTag}.
     *
     * @param identificationTag  the identification tag to display
     */
    public void setIdentificationTag( String identificationTag )
    {
        identificationTagText.setText( identificationTag );
    }


    // ── READ THE PRECEDENCE ───────────────────────────────────────────────────
    /**
     * Returns the current spinner value as the precedence (0–255).
     *
     * @return the selected precedence
     */
    public int getPrecedence()
    {
        return precedenceSpinner.getSelection();
    }


    // ── SET THE PRECEDENCE ────────────────────────────────────────────────────
    /**
     * Sets the precedence spinner to {@code precedence}.
     *
     * @param precedence  the precedence value to set (0–255)
     */
    public void setPrecedence( int precedence )
    {
        precedenceSpinner.setSelection( precedence );
    }


    // ── READ THE AUTHENTICATION LEVEL ────────────────────────────────────────
    /**
     * Returns the {@link AuthenticationLevel} currently selected in the combo.
     *
     * @return the selected authentication level
     */
    public AuthenticationLevel getAuthenticationLevel()
    {
        IStructuredSelection selection = ( IStructuredSelection ) authenticationLevelComboViewer.getSelection();
        return ( AuthenticationLevel ) selection.getFirstElement();
    }


    // ── SET THE AUTHENTICATION LEVEL ──────────────────────────────────────────
    /**
     * Sets the authentication level combo to {@code authenticationLevel}.
     *
     * @param authenticationLevel  the level to select
     */
    public void setAuthenticationLevel( AuthenticationLevel authenticationLevel )
    {
        IStructuredSelection selection = new StructuredSelection( authenticationLevel );
        authenticationLevelComboViewer.setSelection( selection );
    }


    // ── CHECK IF USERFIRST IS SELECTED ────────────────────────────────────────
    /**
     * Returns {@code true} if the userFirst radio button is selected.
     *
     * @return {@code true} if userFirst is active
     */
    public boolean isUserFirst()
    {
        return userFirstRadioButton.getSelection();
    }


    // ── SELECT USERFIRST ──────────────────────────────────────────────────────
    /**
     * Selects the userFirst radio button and deselects itemFirst.
     */
    public void setUserFirst()
    {
        userFirstRadioButton.setSelection( true );
        itemFirstRadioButton.setSelection( false );
    }


    // ── CHECK IF ITEMFIRST IS SELECTED ────────────────────────────────────────
    /**
     * Returns {@code true} if the itemFirst radio button is selected.
     *
     * @return {@code true} if itemFirst is active
     */
    public boolean isItemFirst()
    {
        return itemFirstRadioButton.getSelection();
    }


    // ── SELECT ITEMFIRST ──────────────────────────────────────────────────────
    /**
     * Selects the itemFirst radio button and deselects userFirst.
     */
    public void setItemFirst()
    {
        itemFirstRadioButton.setSelection( true );
        userFirstRadioButton.setSelection( false );
    }

}
