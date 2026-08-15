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


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.widgets.ListContentProposalProvider;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: AttributeTypeWizardPage — YODA QUIZZES LUKE ON THE NATURE OF THE FORCE
// Deep in the Dagobah swamp, Yoda fires a question at Luke: "Name the Force,
// can you?  And which kind — the kind your entry's schema allows, hmm?"
// Luke has to pick the right answer from the list Yoda offers, filter out
// the noise with two checkboxes, and confirm his choice in the preview.
// This wizard page is that quiz: a combo to select an LDAP attribute type,
// two filter checkboxes to narrow the list, and a read-only preview field
// that shows the full attribute description as Luke builds it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The first page of {@link AttributeWizard} — lets the user choose an LDAP
 * attribute type from a filterable combo box.
 * Two checkboxes let the user narrow the list to schema-allowed types only,
 * and optionally hide types that already exist on the entry.
 * A preview field at the bottom shows the current attribute description
 * so the user can see how their choices translate in real time.
 * Think of this page as Yoda's pop quiz on the Dagobah training grounds.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeWizardPage extends WizardPage
{

    /** The parent wizard. */
    private AttributeWizard wizard;

    /** The initial show subschema attributes only. */
    private boolean initialShowSubschemaAttributesOnly;

    /** The initial hide existing attributes. */
    private boolean initialHideExistingAttributes;

    /** The parsed attribute type. */
    private String parsedAttributeType;

    /** The possible attribute types. */
    private String[] possibleAttributeTypes;

    /** The possible attribute types applicable to the entry's schema only. */
    private String[] possibleAttributeTypesSubschemaOnly;

    /** The possible attribute types applicable to the entry's schema only, existing attributes are hidden. */
    private String[] possibleAttributeTypesSubschemaOnlyAndExistingHidden;

    /** The attribute type combo. */
    private Combo attributeTypeCombo;

    /** The show subschem attributes only button. */
    private Button showSubschemAttributesOnlyButton;

    /** The hide existing attributes button. */
    private Button hideExistingAttributesButton;

    /** The preview text. */
    private Text previewText;


    // ── Yoda Prepares Luke's Pop Quiz on Dagobah ─────────────────────────────
    // Yoda gathers all the relevant Force knowledge into three scrolls — every
    // known attribute type, only the ones the entry's schema allows, and that
    // same set minus the attributes Luke has already learned.
    // He also reads Luke's previous answer from the description string, ready
    // to pre-fill the combo when the lesson begins.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code AttributeTypeWizardPage} and pre-computes the three
     * attribute-type lists used by the filter checkboxes.
     * We also parse the initial attribute description to pre-select the right
     * type in the combo when the page first becomes visible.
     *
     * <p>For example — Yoda assembles three training scrolls for Luke:</p>
     * <pre>
     *   // scroll 1: all known attribute types (global schema)
     *   // scroll 2: only types the entry's object classes allow
     *   // scroll 3: scroll 2 minus types already on the entry
     * </pre>
     *
     * @param pageName                          Internal wizard page identifier.
     * @param initialEntry                      The LDAP entry we're adding to;
     *                                          used for schema and existing-attribute lookups.
     * @param initialAttributeDescription       The attribute description to start from
     *                                          (e.g. {@code cn;lang-de}); the type part
     *                                          is parsed out and pre-selected in the combo.
     * @param initialShowSubschemaAttributesOnly When {@code true}, the schema-only
     *                                          filter checkbox starts checked.
     * @param initialHideExistingAttributes     When {@code true}, the hide-existing
     *                                          checkbox starts checked.
     * @param wizard                            The parent {@link AttributeWizard} that
     *                                          coordinates both pages.
     */
    public AttributeTypeWizardPage( String pageName, IEntry initialEntry, String initialAttributeDescription,
        boolean initialShowSubschemaAttributesOnly, boolean initialHideExistingAttributes, AttributeWizard wizard )
    {
        super( pageName );
        super.setTitle( Messages.getString( "AttributeTypeWizardPage.AttributeType" ) ); //$NON-NLS-1$
        super.setDescription( Messages.getString( "AttributeTypeWizardPage.AttributeTypeDescription" ) ); //$NON-NLS-1$
        super.setPageComplete( false );

        this.wizard = wizard;
        this.initialShowSubschemaAttributesOnly = initialShowSubschemaAttributesOnly;
        this.initialHideExistingAttributes = initialHideExistingAttributes;

        Collection<AttributeType> atds = initialEntry.getBrowserConnection().getSchema()
            .getAttributeTypeDescriptions();
        Collection<String> atdNames = SchemaUtils.getNames( atds );
        possibleAttributeTypes = atdNames.toArray( new String[atdNames.size()] );
        Arrays.sort( possibleAttributeTypes );

        Collection<AttributeType> allAtds = SchemaUtils.getAllAttributeTypeDescriptions( initialEntry );
        Collection<String> names = SchemaUtils.getNames( allAtds );
        possibleAttributeTypesSubschemaOnly = names.toArray( new String[0] );
        Arrays.sort( possibleAttributeTypesSubschemaOnly );

        Set<String> set = new HashSet<>( Arrays.asList( possibleAttributeTypesSubschemaOnly ) );
        IAttribute[] existingAttributes = initialEntry.getAttributes();
        for ( int i = 0; existingAttributes != null && i < existingAttributes.length; i++ )
        {
            set.remove( existingAttributes[i].getDescription() );
        }
        possibleAttributeTypesSubschemaOnlyAndExistingHidden = set.toArray( new String[set.size()] );
        Arrays.sort( possibleAttributeTypesSubschemaOnlyAndExistingHidden );

        String attributeDescription = initialAttributeDescription;
        if ( attributeDescription == null )
        {
            attributeDescription = ""; //$NON-NLS-1$
        }
        String[] attributeDescriptionComponents = attributeDescription.split( ";" ); //$NON-NLS-1$
        parsedAttributeType = attributeDescriptionComponents[0];
    }


    // ── Yoda Checks Whether Luke Answered Correctly ───────────────────────────
    // Yoda listens to Luke's answer, glances at the preview board to confirm
    // it reflects what Luke said, then marks the lesson complete or incomplete.
    // If Luke left the combo empty, the lesson isn't done — Yoda waits.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the preview text and enables or disables the Next button based
     * on whether an attribute type has been entered.
     * An empty combo means the page is incomplete; any non-empty value marks it done.
     *
     * <p>For example — Yoda checks whether Luke actually named the Force correctly:</p>
     * <pre>
     *   previewText.setText( wizard.getAttributeDescription() );
     *   setPageComplete( !"".equals( attributeTypeCombo.getText() ) );
     * </pre>
     */
    private void validate()
    {
        previewText.setText( wizard.getAttributeDescription() );
        setPageComplete( !"".equals( attributeTypeCombo.getText() ) ); //$NON-NLS-1$
    }


    // ── Luke Steps Up to the Training Station — Lesson Begins ────────────────
    // Yoda gestures for Luke to approach the podium; as Luke steps forward,
    // Yoda runs a quick re-check of the current answer so the preview stays fresh.
    // When Luke steps back (page hidden), nothing special happens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the wizard dialog whenever this page is shown or hidden.
     * When the page becomes visible we re-validate so the preview and Next-button
     * state are current — important when the user navigates back and returns.
     *
     * <p>For example — Luke approaches Yoda's podium; Yoda re-reads the current answer:</p>
     * <pre>
     *   if ( visible ) { validate(); }
     * </pre>
     *
     * @param visible  {@code true} when this page is being shown, {@code false} when hidden.
     */
    @Override
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        if ( visible )
        {
            validate();
        }
    }


    // ── Yoda Draws the Quiz Layout on a Clearing ─────────────────────────────
    // Yoda scratches a grid into the Dagobah mud: combo box up top for the answer,
    // two checkboxes on the left to filter the question scope, a spacer for
    // breathing room, and a preview slate at the bottom to show what Luke wrote.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT controls for this wizard page.
     * Lays out an attribute-type combo with auto-complete, two filter checkboxes
     * (schema-only and hide-existing), and a read-only preview text field.
     * Listeners on every control call {@link #validate()} so the preview and
     * Next button stay in sync as the user types.
     *
     * <p>For example — Yoda scratches the quiz layout into the training clearing:</p>
     * <pre>
     *   [ AttributeType: [combo▼]                    ]
     *   [   ☑ Show subschema attributes only          ]
     *   [   ☑ Hide existing attributes                ]
     *   [                                             ]
     *   [ Preview: [readonly text]                   ]
     * </pre>
     *
     * @param parent  The parent composite supplied by the wizard dialog.
     */
    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 2, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "AttributeTypeWizardPage.AttributeTypeLabel" ), 1 ); //$NON-NLS-1$

        // attribute combo with field decoration and content proposal
        attributeTypeCombo = BaseWidgetUtils.createCombo( composite, possibleAttributeTypes, -1, 1 );
        attributeTypeCombo.setText( parsedAttributeType );
        new ExtendedContentAssistCommandAdapter( attributeTypeCombo, new ComboContentAdapter(),
            new ListContentProposalProvider( possibleAttributeTypes ), null, null, true );

        BaseWidgetUtils.createSpacer( composite, 1 );
        showSubschemAttributesOnlyButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "AttributeTypeWizardPage.ShowSubschemaAttributesOnly" ), //$NON-NLS-1$
            1 );
        showSubschemAttributesOnlyButton.setSelection( initialShowSubschemaAttributesOnly );

        BaseWidgetUtils.createSpacer( composite, 1 );
        hideExistingAttributesButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "AttributeTypeWizardPage.HideExistingAttributes" ), 1 ); //$NON-NLS-1$
        hideExistingAttributesButton.setSelection( initialHideExistingAttributes );

        Label l = new Label( composite, SWT.NONE );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.horizontalSpan = 2;
        l.setLayoutData( gd );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "AttributeTypeWizardPage.PreviewLabel" ), 1 ); //$NON-NLS-1$
        previewText = BaseWidgetUtils.createReadonlyText( composite, "", 1 ); //$NON-NLS-1$

        // attribute type listener
        attributeTypeCombo.addModifyListener( new ModifyListener()
        {
            @Override
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        // filter listener
        showSubschemAttributesOnlyButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                updateFilter();
                validate();
            }
        } );
        hideExistingAttributesButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                updateFilter();
                validate();
            }
        } );
        updateFilter();

        setControl( composite );
    }


    // ── Yoda Narrows the Question Scope for Luke ──────────────────────────────
    // Depending on Luke's current level, Yoda restricts the question pool:
    // if the schema-only flag is on, only schema-allowed types appear; if
    // hide-existing is also on, types Luke already mastered are removed too.
    // Yoda also greys out the hide-existing checkbox when schema-only is off —
    // there's no point filtering within an unconstrained list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the combo items and enables or disables the filter checkboxes
     * based on the current filter selections.
     * If the schema-only list is empty we force that checkbox off (there is
     * nothing to filter to); similarly for the hide-existing checkbox.
     *
     * <p>For example — Yoda adjusts which questions Luke sees based on his level:</p>
     * <pre>
     *   if ( hideExisting &amp;&amp; subschemaOnly ) → show possibleAttributeTypesSubschemaOnlyAndExistingHidden
     *   if ( subschemaOnly only            ) → show possibleAttributeTypesSubschemaOnly
     *   else                                 → show all possibleAttributeTypes
     * </pre>
     */
    private void updateFilter()
    {
        // enable/disable filter buttons
        hideExistingAttributesButton.setEnabled( showSubschemAttributesOnlyButton.getSelection() );
        if ( possibleAttributeTypesSubschemaOnly.length == 0 )
        {
            showSubschemAttributesOnlyButton.setSelection( false );
            showSubschemAttributesOnlyButton.setEnabled( false );
        }
        if ( possibleAttributeTypesSubschemaOnlyAndExistingHidden.length == 0 )
        {
            hideExistingAttributesButton.setEnabled( false );
            hideExistingAttributesButton.setSelection( false );
        }

        // update combo items and proposals
        String value = attributeTypeCombo.getText();
        if ( hideExistingAttributesButton.getSelection() && showSubschemAttributesOnlyButton.getSelection() )
        {
            attributeTypeCombo.setItems( possibleAttributeTypesSubschemaOnlyAndExistingHidden );
        }
        else if ( showSubschemAttributesOnlyButton.getSelection() )
        {
            attributeTypeCombo.setItems( possibleAttributeTypesSubschemaOnly );
        }
        else
        {
            attributeTypeCombo.setItems( possibleAttributeTypes );
        }
        attributeTypeCombo.setText( value );
    }


    // ── Yoda Retrieves Luke's Final Answer ────────────────────────────────────
    // Yoda checks the answer slate — whatever Luke wrote in the combo, that is
    // the attribute type he chose.  If the combo is gone (wizard disposed), we
    // return an empty string rather than throwing a widget-is-disposed exception.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type currently entered in the combo box.
     * This is the raw text from the combo — not validated against the schema —
     * so callers should treat it as user input.
     * Returns an empty string if the combo has been disposed.
     *
     * <p>For example — Yoda reads back whatever Luke typed on his answer slate:</p>
     * <pre>
     *   String type = typePage.getAttributeType(); // e.g. "cn" or "jpegPhoto"
     *   // combined with options page: "cn;lang-de"
     * </pre>
     *
     * @return  The selected attribute type string, or {@code ""} if disposed.
     */
    String getAttributeType()
    {
        if ( attributeTypeCombo == null || attributeTypeCombo.isDisposed() )
        {
            return ""; //$NON-NLS-1$
        }

        return attributeTypeCombo.getText();
    }

}
