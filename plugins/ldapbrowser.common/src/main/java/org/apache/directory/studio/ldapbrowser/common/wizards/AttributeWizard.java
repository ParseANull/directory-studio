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

package org.apache.directory.studio.ldapbrowser.common.wizards;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


// ── CLASS: AttributeWizard — LUKE LEARNING TO FEEL THE FORCE, ONE STEP AT A TIME
// Aboard the Millennium Falcon, Obi-Wan blindfolds Luke and releases a floating
// training remote — Luke has to sense one shot at a time, building the attribute
// (his Force awareness) step by step: first identify the type of attack (the
// attribute type), then fine-tune his stance and options (language tags, binary
// flag).
// This two-page wizard does exactly that: page one picks the attribute type,
// page two layers on the options, and together they produce a finished LDAP
// attribute description like {@code cn;lang-de} or {@code jpegPhoto;binary}.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A two-page JFace wizard for creating a new LDAP attribute or modifying an
 * existing attribute description.
 * Page one ({@link AttributeTypeWizardPage}) lets the user choose the attribute
 * type; page two ({@link AttributeOptionsWizardPage}) layers on options such as
 * language tags and the binary flag.
 * Think of this class as Luke's training session with the remote — each page
 * is one round of the drill, and finishing the wizard produces a fully-formed
 * attribute description string.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeWizard extends Wizard implements INewWizard
{

    /** The type page. */
    private AttributeTypeWizardPage typePage;

    /** The options page. */
    private AttributeOptionsWizardPage optionsPage;

    /** The initial show subschema attributes only. */
    private boolean initialShowSubschemaAttributesOnly;

    /** The initial hide existing attributes. */
    private boolean initialHideExistingAttributes;

    /** The initial attribute description. */
    private String initialAttributeDescription;

    /** The initial entry. */
    private IEntry initialEntry;

    /** The final attribute description. */
    private String finalAttributeDescription = null;


    // ── Luke Picks Up the Saber With No Prior Instructions ───────────────────
    // Obi-Wan hands Luke a lightsaber aboard the Falcon without any briefing
    // yet — Luke holds it, defaults kick in: show only schema attributes,
    // hide those already on the entry, start with an empty description.
    // This no-arg constructor sets exactly those safe defaults so the wizard
    // can be opened without knowing which entry to target yet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code AttributeWizard} with an empty attribute description
     * and safe defaults: schema-only filter on, existing-attributes hidden.
     * Use this when opening the wizard without a pre-selected entry — the entry
     * can be supplied later, or the wizard falls back to the dummy page.
     *
     * <p>For example — Luke grabs the lightsaber before Obi-Wan says a word:</p>
     * <pre>
     *   AttributeWizard wizard = new AttributeWizard();
     *   // defaults: showSubschema=true, hideExisting=true, description=""
     * </pre>
     */
    public AttributeWizard()
    {
        super.setWindowTitle( Messages.getString( "AttributeWizard.NewAttribute" ) ); //$NON-NLS-1$
        super.setNeedsProgressMonitor( false );
        this.initialShowSubschemaAttributesOnly = true;
        this.initialHideExistingAttributes = true;
        this.initialAttributeDescription = ""; //$NON-NLS-1$
        this.initialEntry = null;
    }


    // ── Obi-Wan Briefs Luke on the Specific Training Parameters ──────────────
    // Obi-Wan calls Luke over, adjusts the remote's difficulty settings, hands
    // him the helmet with the blast shield down, and tells him which attribute
    // he's already working on so the session has clear starting conditions.
    // This parameterised constructor lets callers pre-load all those settings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code AttributeWizard} pre-populated with the given
     * initial attribute description and filter flags.
     * Use this when editing an existing attribute — pass its description string
     * and the entry it lives on so the type page can pre-select the right type
     * and the filter buttons start in the correct state.
     *
     * <p>For example — Obi-Wan configures the training remote for Luke's level:</p>
     * <pre>
     *   AttributeWizard wizard = new AttributeWizard(
     *       "Edit Attribute", true, true, "cn;lang-de", selectedEntry );
     *   // type page pre-selects "cn", options page pre-fills "lang-de"
     * </pre>
     *
     * @param title                        Window title shown in the wizard dialog.
     * @param showSubschemaAttributesOnly  When {@code true}, the type page only
     *                                     lists attribute types that the entry's
     *                                     schema allows — keeps the list focused.
     * @param hideExistingAttributes       When {@code true}, attribute types already
     *                                     present on the entry are hidden from the
     *                                     type list — avoids adding duplicates.
     * @param attributeDescription         The full attribute description to edit
     *                                     (e.g. {@code cn;lang-de}); parsed into
     *                                     type + options for the two pages.
     * @param entry                        The LDAP entry the attribute belongs to;
     *                                     used for schema lookups.
     */
    public AttributeWizard( String title, boolean showSubschemaAttributesOnly, boolean hideExistingAttributes,
        String attributeDescription, IEntry entry )
    {
        super.setWindowTitle( title );
        super.setNeedsProgressMonitor( false );
        this.initialShowSubschemaAttributesOnly = showSubschemaAttributesOnly;
        this.initialHideExistingAttributes = hideExistingAttributes;
        this.initialAttributeDescription = attributeDescription;
        this.initialEntry = entry;
    }


    // ── The Training Remote's Model Number ───────────────────────────────────
    // Every piece of Rebel Alliance equipment has a serial number so the
    // supply depot knows exactly what to requisition — the remote, the wizard,
    // everything gets a unique registry ID.
    // This static method hands back the Eclipse wizard registry ID string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard registry ID for this wizard.
     * The platform uses this to look up and launch the wizard from menus,
     * keybindings, or plugin extension points.
     *
     * <p>For example — the Alliance supply clerk checks the remote's serial number:</p>
     * <pre>
     *   String id = AttributeWizard.getId();
     *   // → BrowserCommonConstants.WIZARD_ATTRIBUTE_WIZARD
     * </pre>
     *
     * @return  The unique wizard ID from {@link BrowserCommonConstants}.
     */
    public static String getId()
    {
        return BrowserCommonConstants.WIZARD_ATTRIBUTE_WIZARD;
    }


    // ── Obi-Wan and Luke Take Their Positions in the Falcon's Hold ───────────
    // Obi-Wan watches the workbench selection to see what Luke is already
    // pointing at — he doesn't need to do anything else to get started;
    // the entry reference is captured at construction time, not here.
    // This init() is called by the Eclipse framework but needs no extra setup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the Eclipse workbench when the wizard is first initialised.
     * We don't need to do anything here because the entry and settings were
     * captured in the constructor — this method exists only to satisfy the
     * {@link INewWizard} contract.
     *
     * <p>For example — Obi-Wan nods and Luke readies himself; no extra prep needed:</p>
     * <pre>
     *   wizard.init( workbench, selection ); // no-op in our case
     * </pre>
     *
     * @param workbench  The current workbench; not used.
     * @param selection  The current structured selection; not used.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
    }


    // ── Obi-Wan Lines Up the Two Training Drills ─────────────────────────────
    // Obi-Wan has two exercises ready for Luke: first sense the type of attack
    // (attribute type drill), then refine his defensive stance (options drill).
    // If no entry was selected, Luke can't train — we show a dummy page instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the wizard pages to the dialog.
     * When a valid entry is available we add the type page and the options page.
     * If no entry was set (wizard opened without a target), we add a dummy page
     * that tells the user to select an entry first.
     *
     * <p>For example — Obi-Wan sets up two training stations for Luke:</p>
     * <pre>
     *   // station 1: pick the attribute type
     *   typePage    = new AttributeTypeWizardPage( ... );
     *   // station 2: fine-tune the options
     *   optionsPage = new AttributeOptionsWizardPage( ... );
     * </pre>
     */
    public void addPages()
    {
        if ( initialEntry != null )
        {
            typePage = new AttributeTypeWizardPage( AttributeTypeWizardPage.class.getName(), initialEntry,
                initialAttributeDescription, initialShowSubschemaAttributesOnly, initialHideExistingAttributes, this );
            addPage( typePage );

            optionsPage = new AttributeOptionsWizardPage( AttributeOptionsWizardPage.class.getName(),
                initialAttributeDescription, this );
            addPage( optionsPage );
        }
        else
        {
            IWizardPage page = new DummyWizardPage();
            addPage( page );
        }
    }


    // ── Obi-Wan Marks the Safe Zone for Each Training Station ────────────────
    // Before Luke steps up to each station, Obi-Wan attaches a help placard
    // so Luke knows what assistance is available during each drill.
    // We do the same: after the pages are built we wire their help context IDs
    // so the F1 key opens the right section of the documentation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wires Eclipse help context IDs to each wizard page after the SWT controls
     * are created.
     * This lets users press F1 on any page and land in the right section of the
     * LDAP browser help system.
     *
     * <p>For example — Obi-Wan pins a reference card to each of Luke's training stations:</p>
     * <pre>
     *   PlatformUI.getWorkbench().getHelpSystem()
     *       .setHelp( typePage.getControl(), PLUGIN_ID + ".tools_attribute_wizard" );
     * </pre>
     *
     * @param pageContainer  The SWT composite that hosts all wizard page controls.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem().setHelp( typePage.getControl(),
            BrowserCommonConstants.PLUGIN_ID + "." + "tools_attribute_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( optionsPage.getControl(),
            BrowserCommonConstants.PLUGIN_ID + "." + "tools_attribute_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // ── CLASS: DummyWizardPage — THE TRAINING ROOM WITH NO REMOTE ────────────
    // Luke wanders into the Falcon's cargo hold expecting a training session
    // but Obi-Wan never set up the remote — the room is empty and Luke gets
    // a polite message telling him to come back once there's an entry selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A placeholder page shown when the wizard is opened without a target entry.
     * It simply tells the user that no entry was selected, so there is nothing
     * to do — they should close the wizard and select an entry first.
     * Think of it as Luke arriving at an empty training bay.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class DummyWizardPage extends WizardPage
    {

        // ── Luke Walks Into an Empty Training Bay ─────────────────────────────
        // Luke strolls in ready to learn but the room is bare — Obi-Wan isn't
        // there, there is no remote, and the title board reads "No Entry Selected."
        // This constructor sets that message so the user understands why the
        // wizard has nothing useful to show.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates the dummy page with a title and description explaining that
         * no entry was selected and the wizard cannot proceed without one.
         *
         * <p>For example — Luke reads the sign on the empty training bay door:</p>
         * <pre>
         *   setTitle( "No Entry Selected" );
         *   setDescription( "Please select an entry in the browser first." );
         * </pre>
         */
        protected DummyWizardPage()
        {
            super( "" ); //$NON-NLS-1$
            super.setTitle( Messages.getString( "AttributeWizard.NoEntrySelected" ) ); //$NON-NLS-1$
            super.setDescription( Messages.getString( "AttributeWizard.NoeEntrySelectedDescription" ) ); //$NON-NLS-1$
            // super.setImageDescriptor(BrowserUIPlugin.getDefault().getImageDescriptor(BrowserUIConstants.IMG_ATTRIBUTE_WIZARD));
            super.setPageComplete( true );
        }


        // ── Luke Surveys the Empty Hold — Nothing Here ────────────────────────
        // Luke looks around the empty cargo hold — there are no targets, no
        // equipment, nothing to interact with; all he can do is read the notice
        // and leave.  The UI renders as a blank composite for the same reason.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates the (blank) SWT control for this dummy page.
         * There is nothing to display beyond the title and description already
         * set in the constructor, so we just create an empty composite.
         *
         * <p>For example — Luke stares at bare walls in the empty training bay:</p>
         * <pre>
         *   Composite composite = new Composite( parent, SWT.NONE );
         *   setControl( composite ); // nothing on it — the room is empty
         * </pre>
         *
         * @param parent  The parent composite provided by the wizard dialog framework.
         */
        public void createControl( Composite parent )
        {
            Composite composite = new Composite( parent, SWT.NONE );
            GridLayout gl = new GridLayout( 1, false );
            composite.setLayout( gl );
            composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

            setControl( composite );
        }
    }


    // ── Luke Deflects the Last Bolt — Session Complete ────────────────────────
    // Luke nails the final training shot, flips up the blast shield, and Obi-Wan
    // marks the session done — the attribute description Luke built is locked in.
    // We freeze the current attribute description into {@code finalAttributeDescription}
    // so subsequent calls to {@link #getAttributeDescription()} stay consistent.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks Finish.
     * Captures the current attribute description from the wizard pages into
     * {@code finalAttributeDescription} so it remains stable even if page
     * widgets are disposed after the dialog closes.
     *
     * <p>For example — Obi-Wan records Luke's final result before dismissing him:</p>
     * <pre>
     *   finalAttributeDescription = getAttributeDescription();
     *   // now safe to dispose the dialog; the result is stored
     * </pre>
     *
     * @return  Always {@code true} — finishing always succeeds for this wizard.
     */
    public boolean performFinish()
    {
        finalAttributeDescription = getAttributeDescription();
        return true;
    }


    // ── Reading the Coordinates Luke Locked In ────────────────────────────────
    // After the training session Obi-Wan reads back the coordinates Luke dialled
    // in on the targeting computer — the attribute type plus any options appended.
    // If the session is over and the result was captured, we return that; otherwise
    // we assemble it live from the two page widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the complete LDAP attribute description assembled from the two
     * wizard pages — for example {@code cn;lang-de} or {@code jpegPhoto;binary}.
     * After {@link #performFinish()} is called the cached string is returned so
     * callers can still read the result after the widgets have been disposed.
     *
     * <p>For example — Obi-Wan reads back Luke's final targeting coordinates:</p>
     * <pre>
     *   String desc = wizard.getAttributeDescription();
     *   // before finish: typePage.getAttributeType() + optionsPage.getAttributeOptions()
     *   // after  finish: the captured finalAttributeDescription string
     * </pre>
     *
     * @return  The full attribute description string, never {@code null}.
     */
    public String getAttributeDescription()
    {
        if ( finalAttributeDescription != null )
        {
            return finalAttributeDescription;
        }

        return typePage.getAttributeType() + optionsPage.getAttributeOptions();
    }

}
