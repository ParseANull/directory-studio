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

package org.apache.directory.studio.connection.ui.actions;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.wizards.NewConnectionWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewConnectionAction — HAN REGISTERS A NEW SHIP ─────────────────────────
// Every time the Rebel Alliance acquires a new ship, someone has to register it
// with fleet command: give it a name, log its network coordinates, and set up
// the authentication codes.  NewConnectionAction does exactly that: it opens the
// New Connection Wizard so the user can add a brand-new LDAP connection.
// The current selection (folders + connections) is passed as the initial selection
// so the wizard can suggest a default parent folder.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Opens the New Connection Wizard to create a new LDAP connection.
 *
 * <p>Passes the current selection (folders and connections) to the wizard as an
 * initial selection so it can default to the right parent folder.</p>
 *
 * <p>Always enabled — you can always create a new connection.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewConnectionAction extends StudioAction
{
    // ── RUN — OPEN THE NEW CONNECTION WIZARD ──────────────────────────────────────
    /**
     * {@inheritDoc}
     * Opens the {@link NewConnectionWizard} in a blocking {@link WizardDialog}.
     */
    public void run()
    {
        // ── BUILD THE INITIAL SELECTION FROM CURRENT VIEW SELECTION ───────────────
        List<Object> selectedObjects = new ArrayList<>();
        selectedObjects.addAll( Arrays.asList( getSelectedConnectionFolders() ) );
        selectedObjects.addAll( Arrays.asList( getSelectedConnections() ) );

        NewConnectionWizard wizard = new NewConnectionWizard();
        wizard.init( PlatformUI.getWorkbench(), new StructuredSelection( selectedObjects ) );
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }


    // ── GET TEXT ──────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getText()
    {
        return Messages.getString( "NewConnectionAction.NewConnection" ); //$NON-NLS-1$
    }


    // ── GET IMAGE DESCRIPTOR — THE "ADD CONNECTION" ICON ──────────────────────────
    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ConnectionUIPlugin.getDefault().getImageDescriptor( ConnectionUIConstants.IMG_CONNECTION_ADD );
    }


    // ── GET COMMAND ID — NO GLOBAL KEY BINDING ────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return null;
    }


    // ── IS ENABLED — ALWAYS ───────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Always returns {@code true} — creating a new connection is always possible.
     */
    public boolean isEnabled()
    {
        return true;
    }
}
