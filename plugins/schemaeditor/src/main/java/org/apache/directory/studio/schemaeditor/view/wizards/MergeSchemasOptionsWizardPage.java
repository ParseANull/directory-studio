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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: MergeSchemasOptionsWizardPage — Han Solo At The Cantina Weighing His Options ─
// Han Solo sits at the bar in the Mos Eisley Cantina, weighing three choices: take the
// job, take half the job, or walk out entirely.  Each option changes how the mission
// plays out.  We give the user a similar set of merge-behaviour toggles: replace unknown
// syntaxes, pull in dependencies, promote attributes — all checked by default because
// those are the sensible choices, but the user can override them.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * The second page of {@link MergeSchemasWizard} where the user chooses behavioural
 * options that control how the merge is executed.
 * The three checkboxes map to: replacing unknown syntaxes with Directory String,
 * pulling in schema dependencies automatically, and promoting attributes from
 * super-classes that already exist in the target project.
 * Think of this page as Han Solo at the Mos Eisley Cantina: three yes/no choices,
 * all pre-selected to the most useful defaults, but the user can clear any of them
 * before pulling the trigger.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MergeSchemasOptionsWizardPage extends WizardPage
{
    // UI Fields
    private Button replaceUnknowNSyntaxButton;
    private Button mergeDependenciesButton;
    private Button pullUpAttributesButton;


    // ── Han Walks Into The Cantina And Finds His Booth ───────────────────────
    // Han strolls through the Mos Eisley Cantina, picks a booth in the back,
    // and before the conversation even starts he's already got a default plan:
    // take the job, bring Chewie, negotiate for more.  We set the page title,
    // description, and image, pre-selecting the safest defaults.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the options page and sets its title, description, and header image.
     * We pass a unique page-ID string to the superclass so Eclipse can manage page
     * ordering in the wizard dialog.
     */
    protected MergeSchemasOptionsWizardPage()
    {
        super( "MergeSchemasOptionsWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "MergeSchemasSelectionWizardPage.ImportSchemasFromProjects" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "MergeSchemasSelectionWizardPage.SelectOptions" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT_WIZARD ) );
    }


    // ── Han Lays The Three Choices On The Table ───────────────────────────────
    // Greedo slides three credit chips across the table, each representing an option:
    // "Fix broken syntax?", "Bring the whole crew along?", "Move the hand-me-down gear up?"
    // Han taps all three — sure, why not, the defaults are good — but he can always
    // push one back if he changes his mind.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the three option checkboxes and lays them out in the wizard page.
     * All three are selected by default because those defaults cover the most common
     * merge scenario; the user can uncheck any of them before clicking Finish.
     * This page has no validation — all combinations are valid, so the Finish
     * button stays enabled once the previous page is complete.
     *
     * <p>For example — Han sizes up the three options at the cantina:</p>
     * <pre>
     *   Option 1: Replace unknown syntaxes with Directory String — Han taps it. "Makes sense."
     *   Option 2: Merge dependencies automatically — Han taps it. "Bring the whole crew."
     *   Option 3: Pull up attributes from super-classes — Han taps it. "Sure, grab what we need."
     * </pre>
     *
     * @param parent  the parent composite from the wizard framework; we embed our widgets inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        replaceUnknowNSyntaxButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "MergeSchemasOptionsWizardPage.ReplaceUnknownSyntax" ), 1 ); //$NON-NLS-1$
        replaceUnknowNSyntaxButton.setToolTipText( Messages
            .getString( "MergeSchemasOptionsWizardPage.ReplaceUnknownSyntaxTooltip" ) ); //$NON-NLS-1$
        replaceUnknowNSyntaxButton.setSelection( true );

        mergeDependenciesButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "MergeSchemasOptionsWizardPage.MergeDependencies" ), 1 ); //$NON-NLS-1$
        mergeDependenciesButton.setToolTipText( Messages
            .getString( "MergeSchemasOptionsWizardPage.MergeDependenciesTooltip" ) ); //$NON-NLS-1$
        mergeDependenciesButton.setSelection( true );

        pullUpAttributesButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "MergeSchemasOptionsWizardPage.PullUpAttributes" ), 1 ); //$NON-NLS-1$
        pullUpAttributesButton.setToolTipText( Messages
            .getString( "MergeSchemasOptionsWizardPage.PullUpAttributesTooltip" ) ); //$NON-NLS-1$
        pullUpAttributesButton.setSelection( true );

        setControl( composite );
    }


    // ── Han Confirms His First Choice ─────────────────────────────────────────
    // Han confirms whether he's willing to swap out any syntax he doesn't recognise
    // for the standard Directory String — "If it's weird, just make it readable."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user has opted to replace unrecognised attribute syntaxes
     * with the Directory String syntax (OID 1.3.6.1.4.1.1466.115.121.1.15) during
     * the merge.
     * This is useful when merging schemas from external servers that reference syntaxes
     * the target project doesn't know about — without this flag, such attributes would
     * fail validation.
     *
     * @return  {@code true} if the replace-unknown-syntax checkbox is checked.
     */
    public boolean isReplaceUnknownSyntax()
    {
        return replaceUnknowNSyntaxButton.getSelection();
    }


    // ── Han Confirms His Second Choice ───────────────────────────────────────
    // "Do I bring the whole crew, or just the people the client asked for?"
    // Han confirms whether dependent schema objects (super-types, referenced
    // attribute types) should be dragged along for the ride automatically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user has opted to automatically include schema objects
     * that the selected items depend on (e.g., super attribute types and super object classes).
     * When {@code true}, the wizard recursively pulls in any referenced schema element
     * that doesn't already exist in the target project.
     *
     * @return  {@code true} if the merge-dependencies checkbox is checked.
     */
    public boolean isMergeDependencies()
    {
        return mergeDependenciesButton.getSelection();
    }


    // ── Han Confirms His Third Choice ────────────────────────────────────────
    // "If the boss already has some of my crew's skills, do I bring those skills
    // along anyway or just the people?"  Han agrees to pull up attributes from
    // super-classes that are already in the target, so nothing gets lost in translation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user has opted to promote must/may attribute references
     * from a source super-class onto the merged object class when that super-class
     * already exists in the target project.
     * This prevents silent attribute loss: if the target has a trimmed version of a
     * super-class, the extra attributes it no longer carries get added directly to
     * the merged object class instead.
     *
     * @return  {@code true} if the pull-up-attributes checkbox is checked.
     */
    public boolean isPullUpAttributes()
    {
        return pullUpAttributesButton.getSelection();
    }

}
