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


import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: NewObjectClassWizard — CONSTRUCTION OF THE SECOND DEATH STAR ──────
// The Emperor oversees the construction of the second Death Star, assembling it
// section by section — superlaser, hull, command bridge — each piece added in a
// deliberate sequence until the whole structure is complete and ready to fire.
// We do the same thing here: we walk the user through page after page collecting
// OID, aliases, content settings, mandatory attrs, and optional attrs, then bolt
// the finished ObjectClass together and register it with the schema.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wizard that guides the user through creating a brand-new LDAP ObjectClass.
 * An ObjectClass is the blueprint for an LDAP entry — it declares what
 * attributes an entry must or may carry (like a Java class declaring fields).
 * Think of this wizard as the Emperor's construction droid: it collects every
 * structural piece across four pages, then assembles the final ObjectClass in
 * one decisive {@code performFinish()} call.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewObjectClassWizard extends Wizard implements INewWizard
{
    public static final String ID = PluginConstants.NEW_WIZARD_NEW_OBJECT_CLASS_WIZARD;

    /** The selected schema */
    private Schema selectedSchema;

    // The pages of the wizards
    private NewObjectClassGeneralPageWizardPage generalPage;
    private NewObjectClassContentWizardPage contentPage;
    private NewObjectClassMandatoryAttributesPage mandatoryAttributesPage;
    private NewObjectClassOptionalAttributesPage optionalAttributesPage;


    // ── Emperor Orders The Wing Sections Built ───────────────────────────────
    // The Emperor issues construction orders to his engineers on the Death Star
    // II, each team responsible for a different section of the station's frame.
    // The construction foreman lines them all up in sequence — general hull,
    // interior layout, mandatory systems, optional systems — ready to be filled.
    // We do the same: instantiate each wizard page and register it in order so
    // the user steps through them one at a time.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers all four wizard pages that collect ObjectClass data.
     * We need four separate pages because there's a lot of information to gather
     * and cramming it on one screen would be overwhelming for the user.
     * The pages are added in presentation order: general info first, then
     * content settings, then mandatory attributes, then optional attributes.
     *
     * <p>For example — the Emperor's engineers line up the construction modules:</p>
     * <pre>
     *   "Section Alpha: primary hull geometry."
     *   "Section Beta: interior compartments."
     *   "Section Gamma: life support — mandatory."
     *   "Section Delta: turbolasers — optional."
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        generalPage = new NewObjectClassGeneralPageWizardPage();
        generalPage.setSelectedSchema( selectedSchema );
        contentPage = new NewObjectClassContentWizardPage();
        mandatoryAttributesPage = new NewObjectClassMandatoryAttributesPage();
        optionalAttributesPage = new NewObjectClassOptionalAttributesPage();

        // Adding pages
        addPage( generalPage );
        addPage( contentPage );
        addPage( mandatoryAttributesPage );
        addPage( optionalAttributesPage );
    }


    // ── The Death Star Fires Its Superlaser ──────────────────────────────────
    // The Emperor watches the second Death Star's superlaser charge up and
    // fire — every piece of the construction effort converging into a single,
    // decisive moment of completion.
    // We do exactly that here: we pull data from all four pages, assemble the
    // ObjectClass object, and commit it to the schema — the wizard's payoff.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks "Finish" — assembles all collected data into
     * a real {@link ObjectClass} and registers it with the schema handler.
     * This is where all four pages' worth of input collapses into a single
     * concrete schema element that the LDAP server will understand.
     *
     * <p>For example — the Emperor's final order activates the station:</p>
     * <pre>
     *   ObjectClass newStation = new ObjectClass( "Death Star II" );
     *   newStation.setSchemaName( "Imperial Fleet" );
     *   newStation.setNames( ["Battle Station", "DS-2"] );
     *   schemaHandler.addObjectClass( newStation );
     * </pre>
     *
     * @return {@code true} always — we always succeed (exceptions surface via Eclipse error dialogs)
     */
    public boolean performFinish()
    {
        // Creating the new object class
        ObjectClass newOC = new ObjectClass( generalPage.getOidValue() );
        newOC.setSchemaName( generalPage.getSchemaValue() );
        newOC.setNames( generalPage.getAliasesValue() );
        newOC.setDescription( generalPage.getDescriptionValue() );
        newOC.setSuperiorOids( contentPage.getSuperiorsNameValue() );
        newOC.setType( contentPage.getClassTypeValue() );
        newOC.setObsolete( contentPage.getObsoleteValue() );
        newOC.setMustAttributeTypeOids( mandatoryAttributesPage.getMandatoryAttributeTypesNames() );
        newOC.setMayAttributeTypeOids( optionalAttributesPage.getOptionalAttributeTypesNames() );

        // Adding the new object class
        Activator.getDefault().getSchemaHandler().addObjectClass( newOC );

        // Saving the Dialog Settings OID History
        PluginUtils.saveDialogSettingsHistory( PluginConstants.DIALOG_SETTINGS_OID_HISTORY, newOC.getOid() );

        return true;
    }


    // ── Construction Crew Awaits Their Orders ────────────────────────────────
    // Before the Emperor issues his construction directives, the crew stands at
    // attention — no setup needed yet, just ready acknowledgment that the
    // station-building operation is about to begin.
    // This init is similarly empty: Eclipse calls it as part of wizard startup,
    // but we have nothing to pre-configure from the workbench or selection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes this wizard from the current Eclipse workbench context.
     * We don't actually need anything from the workbench or the current selection
     * here, so this is intentionally a no-op.
     *
     * <p>For example — the Emperor's construction crew snaps to attention:</p>
     * <pre>
     *   "All sections report ready, my lord."
     *   // (The Emperor nods and says nothing — construction begins on addPages.)
     * </pre>
     *
     * @param workbench  the active Eclipse workbench — we ignore it here
     * @param selection  whatever the user had selected when they opened the wizard — also ignored
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // Nothing to do.
    }


    // ── The Emperor Designates The Construction Site ─────────────────────────
    // Before any section can be built, the Emperor specifies which quadrant of
    // the galaxy the station will be anchored in — the schema is that anchor.
    // We record the target schema so the general page can pre-select it, saving
    // the user from having to pick it manually.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Tells the wizard which schema the new ObjectClass should land in.
     * The general page uses this to pre-populate the schema dropdown so the
     * user doesn't have to hunt for it themselves.
     *
     * <p>For example — the Emperor points to the Endor system on the holotable:</p>
     * <pre>
     *   "The station will orbit Endor — mark it on all construction charts."
     *   wizard.setSelectedSchema( endorSchema );
     * </pre>
     *
     * @param schema  the Schema we want the new ObjectClass to belong to; may be {@code null}
     *                if no schema was pre-selected in the UI
     */
    public void setSelectedSchema( Schema schema )
    {
        selectedSchema = schema;
    }
}
