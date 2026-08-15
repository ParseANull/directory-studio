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
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: NewSchemaWizard — YODA LIFTING LUKE'S X-WING FROM THE SWAMP ────────
// On Dagobah, Luke stares at his X-wing buried in the swamp — convinced it's
// impossible to lift.  Yoda closes his eyes, reaches out with the Force, and
// the entire ship rises from the water and sets down on solid ground.  Something
// that didn't exist in usable form a moment ago is now fully present and ready.
// We do exactly the same thing: in a single page and a single finish step, we
// conjure a brand-new {@link Schema} object out of nothing but a name, attach it
// to the open project, and register it with the schema handler.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wizard that creates a new, empty LDAP schema within the currently open project.
 * A schema is the namespace container for attribute types and object classes —
 * think of it as a named package.  This wizard is intentionally minimal: one
 * page, one text field, one action.
 * Think of this wizard as Yoda lifting Luke's X-wing: it takes something that
 * seems like it needs a lot of work (setting up a schema) and accomplishes it
 * in one calm, decisive move.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewSchemaWizard extends Wizard implements INewWizard
{
    public static final String ID = PluginConstants.NEW_WIZARD_NEW_SCHEMA_WIZARD;

    // The pages of the wizard
    private NewSchemaWizardPage page;


    // ── Yoda Reaches Out Toward The Swamp ────────────────────────────────────
    // Before Yoda can lift the X-wing, he extends his hand toward the water —
    // establishing contact with the ship that will shortly rise.
    // We do the same: create the single wizard page that will collect the
    // schema name, and register it with the wizard framework.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers the single page this wizard uses — the
     * {@link NewSchemaWizardPage} that asks for the schema name.
     * We only need one page because a new schema has exactly one required
     * attribute: its name.
     *
     * <p>For example — Yoda extends his hand toward the submerged X-wing:</p>
     * <pre>
     *   page = new NewSchemaWizardPage();
     *   addPage( page );
     *   // "Ready to lift, the schema is."
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        page = new NewSchemaWizardPage();

        // Adding pages
        addPage( page );
    }


    // ── The X-Wing Rises From The Swamp ──────────────────────────────────────
    // With a low hum, the X-wing breaks the surface, water streaming off its
    // wings, and Yoda sets it gently on the ground — fully formed, ready to fly.
    // We do the same: construct a {@link Schema} from the name the user typed,
    // bind it to the currently open project, and register it with the handler.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks "Finish" — creates the new {@link Schema},
     * associates it with the currently open project, and registers it with the
     * schema handler so it appears immediately in the Schema view.
     * The schema starts empty — no attribute types, no object classes — ready
     * for the user to populate.
     *
     * <p>For example — the X-wing emerges from the swamp, fully intact:</p>
     * <pre>
     *   Schema schema = new Schema( page.getSchemaName() );
     *   schema.setProject( openProject );
     *   schemaHandler.addSchema( schema );
     *   return true;  // "That is why you fail... but I did not fail."
     * </pre>
     *
     * @return {@code true} always — failure surfaces through Eclipse error dialogs, not here
     */
    public boolean performFinish()
    {
        Schema schema = new Schema( page.getSchemaName() );
        schema.setProject( Activator.getDefault().getProjectsHandler().getOpenProject() );
        Activator.getDefault().getSchemaHandler().addSchema( schema );

        return true;
    }


    // ── Luke Watches In Stunned Silence ──────────────────────────────────────
    // After the X-wing lands safely, Luke just stares — there's nothing to say.
    // The wizard initializes similarly: there's nothing to configure from the
    // workbench or the current selection, so we do nothing here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes this wizard from the current Eclipse workbench context.
     * We don't need anything from the workbench or the selection, so this is
     * intentionally a no-op — just like Luke speechless after the X-wing rises.
     *
     * <p>For example — Luke stares, mouth open, nothing to add:</p>
     * <pre>
     *   // "I don't... I don't believe it."
     *   // "That is why you fail." (but we still return nothing)
     * </pre>
     *
     * @param workbench  the active Eclipse workbench — not used
     * @param selection  the current UI selection — not used
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // Nothing to do.
    }
}
