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


import java.text.ParseException;

import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.common.ui.dialogs.UnixPermissionsDialog;
import org.apache.directory.studio.openldap.common.ui.model.UnixPermissions;


// ── CLASS: UnixPermissionsWidget — IMPERIAL SECURITY CHECKPOINT WIDGET ────────
// Picture the Imperial checkpoint terminal mounted at a base entrance: it
// displays the current permission value in readable form (symbolic notation
// and octal side by side) on a read-only screen, and offers an "Edit
// Permissions..." button that opens the full {@link UnixPermissionsDialog}
// where the officer can adjust the permission bits. After the dialog closes
// with OK we update the displayed value and fire our change listeners so the
// parent form knows the checkpoint rules have been updated.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We provide a compact Unix permissions display-and-edit widget consisting of a
 * read-only text label (showing both symbolic and octal notation) and an
 * "Edit Permissions..." button that opens a {@link UnixPermissionsDialog}. We
 * extend {@link AbstractWidget} so change listeners are notified when the
 * permission value changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UnixPermissionsWidget extends AbstractWidget
{
    // The value
    private String value;

    // UI widgets
    private Composite composite;
    private Text label;
    private Button editButton;

    // Listeners
    private SelectionListener editButtonSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            // Creating and opening a UNIX permission dialog
            UnixPermissionsDialog dialog = new UnixPermissionsDialog( editButton.getShell(), value );

            if ( UnixPermissionsDialog.OK == dialog.open() )
            {
                setValue( dialog.getDecimalValue() );
                notifyListeners();
            }
        }
    };


    // ── METHOD: create(Composite) — INSTALLING THE CHECKPOINT (NO TOOLKIT) ────
    // We delegate to the toolkit-aware overload with {@code null} so there is
    // always one code path to maintain.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the widget's SWT controls inside the given parent without a
     * {@link FormToolkit}. Delegates to {@link #create(Composite, FormToolkit)}.
     *
     * @param parent  the parent {@link Composite}
     */
    public void create( Composite parent )
    {
        create( parent, null );
    }


    // ── METHOD: create(Composite, FormToolkit) — INSTALLING THE CHECKPOINT ────
    // We build the composite holding the read-only permission label and the
    // "Edit Permissions..." button. We attach the edit-button selection listener
    // so clicking it opens the full permissions dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create all SWT controls for this widget inside the given parent,
     * optionally adapting them with a {@link FormToolkit} for Eclipse Forms pages.
     *
     * @param parent   the parent {@link Composite}
     * @param toolkit  the form toolkit, or {@code null} for plain SWT
     */
    public void create( Composite parent, FormToolkit toolkit )
    {
        // Creating the widget base composite
        if ( toolkit != null )
        {
            composite = toolkit.createComposite( parent );
        }
        else
        {
            composite = new Composite( parent, SWT.NONE );
        }

        GridLayout compositeGridLayout = new GridLayout( 2, false );
        compositeGridLayout.marginHeight = compositeGridLayout.marginWidth = 0;
        compositeGridLayout.verticalSpacing = 0;
        composite.setLayout( compositeGridLayout );

        // Label
        if ( toolkit != null )
        {
            label = toolkit.createText( composite, "" );
        }
        else
        {
            label = BaseWidgetUtils.createText( composite, "", 1 );
        }

        label.setEditable( false );
        label.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Edit Button
        if ( toolkit != null )
        {
            editButton = toolkit.createButton( composite, "Edit Permissions...", SWT.PUSH );
        }
        else
        {
            editButton = BaseWidgetUtils.createButton( composite, "Edit Permissions...", 1 );
        }

        editButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false ) );

        // Adding the listeners to the UI widgets
        addListeners();
    }


    // ── METHOD: getControl — HANDING OVER THE CHECKPOINT TERMINAL HANDLE ──────
    // We return the top-level composite so the parent layout can size and
    // position the entire permissions widget as a unit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the top-level {@link Control} (a {@link Composite}) for this
     * widget so the parent layout can size and position it.
     *
     * @return the primary composite control
     */
    public Control getControl()
    {
        return composite;
    }


    // ── METHOD: addListeners — ACTIVATING THE CHECKPOINT SENSOR ──────────────
    // We attach our pre-built selection listener to the Edit button so it opens
    // the {@link UnixPermissionsDialog} when clicked.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We attach our pre-built selection listener to the "Edit Permissions..."
     * button.
     */
    private void addListeners()
    {
        editButton.addSelectionListener( editButtonSelectionListener );
    }


    // ── METHOD: setValue — UPDATING THE CHECKPOINT SCREEN ────────────────────
    // We store the new raw value, parse it into a {@link UnixPermissions}
    // object (falling back to all-denied on parse failure), and update the
    // label to show both the symbolic and octal representations.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the widget's current permission value, parse it into a
     * {@link UnixPermissions} object, and update the display label to show
     * the symbolic and octal representations.
     *
     * @param s  the permission value string (decimal, octal, or symbolic)
     */
    public void setValue( String s )
    {
        value = s;

        UnixPermissions perm = null;

        try
        {
            perm = new UnixPermissions( s );
        }
        catch ( ParseException e )
        {
            perm = new UnixPermissions();
        }

        label.setText( NLS.bind( "{0} ({1})", perm.getSymbolicValue(), perm.getOctalValue() ) );
    }


    // ── METHOD: getValue — READING THE CHECKPOINT TERMINAL VALUE ──────────────
    // We return the raw value string that was last passed to setValue(), which
    // may be in decimal, octal, or symbolic format.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the raw permission value string that was last set via
     * {@link #setValue(String)}.
     *
     * @return the raw permission value string
     */
    public String getValue()
    {
        return value;
    }


    // ── METHOD: dispose — DECOMMISSIONING THE CHECKPOINT ─────────────────────
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
