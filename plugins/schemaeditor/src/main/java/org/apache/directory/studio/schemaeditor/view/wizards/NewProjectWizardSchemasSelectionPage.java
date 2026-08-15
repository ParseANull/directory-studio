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
package org.apache.directory.studio.schemaeditor.view.wizards;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.widget.CoreSchemasSelectionWidget;
import org.apache.directory.studio.schemaeditor.view.widget.CoreSchemasSelectionWidget.ServerTypeEnum;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: NewProjectWizardSchemasSelectionPage — REBEL BRIEFING ROOM HOLOGRAM ─
// In the Yavin briefing room, the holographic projection of the Death Star hangs
// in the air while General Dodonna highlights the specific systems the attack
// will target — the exhaust port, the trench, the thermal oscillator.  The pilots
// don't get everything; they choose their targets.
// This page works the same way: a holographic tree of available core schemas is
// displayed and the user checks off exactly the ones they want bundled into their
// new offline project.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wizard page where the user picks which core LDAP schemas to include in a new
 * offline project.  Core schemas (like {@code core}, {@code inetOrgPerson},
 * {@code cosine}) are the standard building blocks bundled with every LDAP server.
 * Think of this page as the Rebel briefing room hologram: all the available
 * schemas are projected for inspection and the user picks the ones relevant to
 * their mission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewProjectWizardSchemasSelectionPage extends WizardPage
{
    // UI Fields
    private CoreSchemasSelectionWidget coreSchemaSelectionWidget;


    // ── General Dodonna Powers On The Briefing Hologram ──────────────────────
    // General Dodonna steps to the hologram projector in the Yavin briefing room,
    // switches it on, and annotates the projected Death Star with the target
    // systems — ready for the pilots to study.
    // We set the page title and description here so the user knows what they're
    // looking at before the schema tree even loads.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of this schemas-selection wizard page.
     * Sets the page's title, description, and image so the user understands
     * they're about to pick the core schemas for their offline project.
     *
     * <p>For example — Dodonna activates the briefing room display:</p>
     * <pre>
     *   setTitle( "Create Schema Project" );
     *   setDescription( "Select the core schemas to include in your project." );
     *   // hologram flickers to life — all known systems visible
     * </pre>
     */
    protected NewProjectWizardSchemasSelectionPage()
    {
        super( "NewProjectWizardSchemasSelectionPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewProjectWizardSchemasSelectionPage.CreateSchemaProject" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewProjectWizardSchemasSelectionPage.PleaseSelectCoreSchemaForInclude" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_NEW_WIZARD ) );
    }


    // ── The Hologram Renders The Full Target Map ──────────────────────────────
    // The briefing-room projector renders the full three-dimensional hologram of
    // available systems — each one a labelled node the pilots can reach out and
    // check off.  The display defaults to showing Apache DS schemas since that's
    // the most common server type.
    // We build the {@link CoreSchemasSelectionWidget} here and initialize it to
    // the ApacheDS server type so the user sees sensible defaults.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widgets for this page — specifically the
     * {@link CoreSchemasSelectionWidget} that displays a checkable tree of
     * bundled core schemas.
     * We initialize it for {@link ServerTypeEnum#APACHE_DS} because that's our
     * most common deployment target; the user can switch server type via the
     * widget's own controls.
     *
     * <p>For example — Dodonna zooms the hologram in on the Apache DS system list:</p>
     * <pre>
     *   coreSchemaSelectionWidget = new CoreSchemasSelectionWidget();
     *   coreSchemaSelectionWidget.init( ServerTypeEnum.APACHE_DS );
     *   // pilots can now check: [ ] core  [x] inetOrgPerson  [ ] cosine ...
     * </pre>
     *
     * @param parent  the SWT container Eclipse provides for our widgets
     */
    public void createControl( Composite parent )
    {
        coreSchemaSelectionWidget = new CoreSchemasSelectionWidget();
        Composite composite = coreSchemaSelectionWidget.createWidget( parent );
        coreSchemaSelectionWidget.init( ServerTypeEnum.APACHE_DS );

        setControl( composite );
    }


    // ── Dodonna Reads Out The Checked Target Systems ──────────────────────────
    // After the pilots have checked off their attack vectors on the hologram,
    // Dodonna reads the list aloud: "Port 2, Trench Run, Thermal Oscillator."
    // We do the same: ask the widget which schemas have been checked and return
    // their names as a string array.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the names of the core schemas the user checked in the widget.
     * The wizard's {@code performFinish()} iterates this list to load and
     * register each selected schema into the new project.
     *
     * <p>For example — Dodonna collects the pilots' target selections:</p>
     * <pre>
     *   String[] targets = schemasSelectionPage.getSelectedSchemas();
     *   // targets = ["core", "inetOrgPerson", "cosine"]
     * </pre>
     *
     * @return array of selected core-schema names; may be empty if the user
     *         checked nothing; never {@code null}
     */
    public String[] getSelectedSchemas()
    {
        return coreSchemaSelectionWidget.getCheckedCoreSchemas();
    }


    // ── Dodonna Notes Which Fleet The Targets Belong To ──────────────────────
    // Different Imperial battle groups use different weapon systems — the
    // Death Star's schematics vary by fleet designation.  Dodonna notes which
    // Imperial fleet they're studying so the pilots read the right manuals.
    // We return the server type for the same reason: different LDAP servers
    // package their core schemas differently.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server type the user selected in the widget (e.g., Apache DS,
     * OpenLDAP).  Different servers ship with different core schema variants,
     * so we need this alongside the schema names to load the right files.
     *
     * <p>For example — Dodonna notes which fleet's schematics are on the hologram:</p>
     * <pre>
     *   ServerTypeEnum fleet = schemasSelectionPage.getServerType();
     *   // fleet = APACHE_DS — load the ApacheDS core schema files
     * </pre>
     *
     * @return the selected {@link ServerTypeEnum}; matches whatever the user
     *         chose in the {@link CoreSchemasSelectionWidget}
     */
    public ServerTypeEnum getServerType()
    {
        return coreSchemaSelectionWidget.getServerType();
    }
}
