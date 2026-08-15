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


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: NewAttributeTypeWizard — Luke's Journey From Tatooine To The Death Star ─────
// Luke doesn't become a hero in one step — he starts at the moisture farm (general info),
// trains with Obi-Wan at the cantina (content/syntax), and finally engages in the trench
// run (matching rules) before the Death Star is destroyed and the attribute type exists.
// This wizard orchestrates those same three steps: General page, Content page, Matching
// Rules page — then fires off the finishing blow that registers the new attribute type
// in the schema handler.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * The top-level wizard that guides a user through creating a new LDAP attribute type.
 * It assembles three pages — General (identity), Content (syntax/usage/flags), and
 * Matching Rules — and on Finish it constructs and registers the new {@link AttributeType}.
 * Think of this wizard as Luke's arc from Tatooine to the Death Star: each page is a
 * stage of the journey, and {@code performFinish()} is the proton torpedo going in.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewAttributeTypeWizard extends Wizard implements INewWizard
{
    public static final String ID = PluginConstants.NEW_WIZARD_NEW_ATTRIBUTE_TYPE_WIZARD;

    /** The selected schema */
    private Schema selectedSchema;

    // The pages of the wizards
    private NewAttributeTypeGeneralWizardPage generalPage;
    private NewAttributeTypeContentWizardPage contentPage;
    private NewAttributeTypeMatchingRulesWizardPage matchingRulesPage;


    // ── Luke Lines Up His Squadmates Before The Battle ───────────────────────────────
    // In the briefing room, Luke meets Red Squadron: Biggs, Wedge, and the others — each
    // with their own role to play. Obi-Wan explains the plan, and Luke is ready to fly.
    // We create and register all three wizard pages here, passing the pre-selected schema
    // to the general page so the user doesn't have to pick it manually.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the three wizard pages and adds them in order to this wizard.
     * Eclipse calls this before the wizard dialog opens, so we instantiate all pages here
     * and pass the pre-selected schema (if any) to the General page via its setter.
     */
    @Override
    public void addPages()
    {
        // Creating pages
        generalPage = new NewAttributeTypeGeneralWizardPage();
        generalPage.setSelectedSchema( selectedSchema );
        contentPage = new NewAttributeTypeContentWizardPage();
        matchingRulesPage = new NewAttributeTypeMatchingRulesWizardPage();

        // Adding pages
        addPage( generalPage );
        addPage( contentPage );
        addPage( matchingRulesPage );
    }


    // ── Luke Fires The Proton Torpedo Into The Exhaust Port ───────────────────────────
    // After all three pages are complete, Luke lines up the shot, releases the torpedo,
    // and the Death Star is no more — mission accomplished.
    // We gather every value from the three pages, assemble the AttributeType object,
    // register it with the schema handler, and save the OID to history.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks Finish — builds and registers the new attribute type.
     * We harvest all values from the three pages, construct a new {@link AttributeType},
     * add it to the schema handler (which triggers the editor to refresh), and save the
     * OID into dialog history so it appears in future OID combos.
     *
     * @return  true always — we don't do any validation here since the pages handle that.
     */
    @Override
    public boolean performFinish()
    {
        // Creating the new attribute type
        AttributeType newAT = new AttributeType( generalPage.getOidValue() );
        newAT.setSchemaName( generalPage.getSchemaValue() );
        newAT.setNames( generalPage.getAliasesValue() );
        newAT.setDescription( generalPage.getDescriptionValue() );
        newAT.setSuperiorOid( contentPage.getSuperiorValue() );
        newAT.setUsage( contentPage.getUsageValue() );
        newAT.setSyntaxOid( contentPage.getSyntax() );
        newAT.setSyntaxLength( contentPage.getSyntaxLengthValue() );
        newAT.setObsolete( contentPage.getObsoleteValue() );
        newAT.setSingleValued( contentPage.getSingleValueValue() );
        newAT.setCollective( contentPage.getCollectiveValue() );
        newAT.setUserModifiable( !contentPage.getNoUserModificationValue() );
        newAT.setEqualityOid( matchingRulesPage.getEqualityMatchingRuleValue() );
        newAT.setOrderingOid( matchingRulesPage.getOrderingMatchingRuleValue() );
        newAT.setSubstringOid( matchingRulesPage.getSubstringMatchingRuleValue() );

        // Adding the new attribute type
        Activator.getDefault().getSchemaHandler().addAttributeType( newAT );

        // Saving the Dialog Settings OID History
        PluginUtils.saveDialogSettingsHistory( PluginConstants.DIALOG_SETTINGS_OID_HISTORY, newAT.getOid() );

        return true;
    }


    // ── Luke Gets His Cockpit Briefing Before The Mission Starts ─────────────────────
    // Before Luke climbs into the X-wing, the ground crew gives him a quick systems
    // briefing — but there's nothing specific to set up; the ship is already prepped.
    // Eclipse requires we implement this method; we don't need to do anything here since
    // all initialization happens in {@link #addPages()} and through {@link #setSelectedSchema}.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Eclipse's INewWizard initialization callback — we don't need it.
     * Everything we need comes in through {@link #setSelectedSchema(Schema)} before the
     * wizard opens, so this method intentionally does nothing.
     *
     * @param workbench  the Eclipse workbench — unused.
     * @param selection  the current workbench selection — unused.
     */
    @Override
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // Nothing to do.
    }


    // ── Luke's Schema Home Base Is Set Before The Mission Launches ────────────────────
    // Before Luke takes off, the ground crew programs the target coordinates into
    // the X-wing's navicomputer — so he knows exactly where to go.
    // Callers (e.g. context-menu actions on a schema node) set the target schema here
    // before opening the wizard so the General page starts with it pre-selected.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Pre-sets the schema that the new attribute type will be created in.
     * Call this before opening the wizard if you know from context which schema the user
     * intends to add to — it saves them one combo selection on the first page.
     *
     * @param schema  the Schema to pre-select — may be null if no context is known.
     */
    public void setSelectedSchema( Schema schema )
    {
        selectedSchema = schema;
    }
}
