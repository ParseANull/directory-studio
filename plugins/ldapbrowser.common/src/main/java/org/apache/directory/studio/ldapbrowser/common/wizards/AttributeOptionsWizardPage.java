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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: AttributeOptionsWizardPage — OBI-WAN EXPLAINS JEDI PROTOCOL NUANCES
// Aboard the Millennium Falcon, Obi-Wan sits Luke down and works through the
// finer points of Jedi etiquette: which language to speak in each situation
// (language tags), whether a transmission needs binary encoding, and any
// additional ceremonial options.  Each rule can be stacked — you can add more
// language clauses or more custom options with the "+" buttons — and a preview
// pane at the bottom shows the resulting full protocol string in real time.
// This wizard page does exactly that for LDAP attribute descriptions: the user
// layers language tags (lang-de, lang-en-US) and other options (binary, custom)
// onto the attribute type chosen on the previous page.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The second page of {@link AttributeWizard} — lets the user attach options
 * to the attribute type selected on the first page.
 * Options fall into three categories: language tags (e.g. {@code lang-de}),
 * the {@code binary} transfer option, and arbitrary custom option strings.
 * Each category is represented by a dynamic list of rows that can be added or
 * removed with "+" and "−" buttons.
 * A read-only preview field at the bottom shows the assembled attribute
 * description (e.g. {@code cn;binary;lang-de}) as the user edits.
 * Think of this page as Obi-Wan walking Luke through the protocol nuances —
 * every option clause is another rule to layer on top.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeOptionsWizardPage extends WizardPage
{

    /** The wizard. */
    private AttributeWizard wizard;

    /** The shell */
    private Shell shell;

    /** The possible languages. */
    private String[] possibleLanguages;

    /** The possible language to countries map. */
    private Map<String, String[]> possibleLangToCountriesMap;

    /** The parsed lang list. */
    private List<String> parsedLangList;

    /** The parsed option list. */
    private List<String> parsedOptionList;

    /** The parsed binary option. */
    private boolean parsedBinary;

    /** The language group. */
    private Group langGroup;

    /** The lang line list. */
    private ArrayList<LangLine> langLineList;

    /** The options group. */
    private Group optionsGroup;

    /** The option line list. */
    private ArrayList<OptionLine> optionLineList;

    /** The binary option button. */
    private Button binaryOptionButton;

    /** The preview text. */
    private Text previewText;


    // ── Obi-Wan Catalogues All Known Languages and Parses Luke's Prior Training ─
    // Obi-Wan first compiles a master list of every language in the galaxy
    // (from the JVM's Locale table) and maps each language to its known dialects.
    // He then reads Luke's existing attribute description and splits out any
    // language tags, the binary flag, and any other option clauses, ready to
    // pre-fill the UI when the page opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code AttributeOptionsWizardPage}, pre-computing the full
     * language/country lists from {@link Locale#getAvailableLocales()} and
     * parsing any options already present in the initial attribute description.
     * Parsed options are stored in lists and used to pre-fill the rows when
     * {@link #createControl(Composite)} builds the UI.
     *
     * <p>For example — Obi-Wan reads Luke's existing training notes and highlights the options:</p>
     * <pre>
     *   // "cn;binary;lang-de" is split into:
     *   parsedBinary    = true
     *   parsedLangList  = [ "lang-de" ]
     *   parsedOptionList = []   // (binary handled separately)
     * </pre>
     *
     * @param pageName                    Internal wizard page identifier.
     * @param initialAttributeDescription The full attribute description to start from;
     *                                    everything after the first ";" is parsed as options.
     * @param wizard                      The parent {@link AttributeWizard}; used to
     *                                    call back to {@link AttributeWizard#getAttributeDescription()}
     *                                    for the preview.
     */
    public AttributeOptionsWizardPage( String pageName, String initialAttributeDescription, AttributeWizard wizard )
    {
        super( pageName );
        super.setTitle( Messages.getString( "AttributeOptionsWizardPage.Options" ) ); //$NON-NLS-1$
        super.setDescription( Messages.getString( "AttributeOptionsWizardPage.OptionsDescription" ) ); //$NON-NLS-1$
        // super.setImageDescriptor(BrowserUIPlugin.getDefault().getImageDescriptor(BrowserUIConstants.IMG_ATTRIBUTE_WIZARD));
        super.setPageComplete( false );

        this.wizard = wizard;

        // init possible languages and countries
        SortedSet<String> languageSet = new TreeSet<String>();
        Map<String, SortedSet<String>> languageToCountrySetMap = new HashMap<String, SortedSet<String>>();
        Locale[] locales = Locale.getAvailableLocales();
        for ( int i = 0; i < locales.length; i++ )
        {
            Locale locale = locales[i];
            languageSet.add( locale.getLanguage() );
            if ( !languageToCountrySetMap.containsKey( locale.getLanguage() ) )
            {
                languageToCountrySetMap.put( locale.getLanguage(), new TreeSet<String>() );
            }
            SortedSet<String> countrySet = languageToCountrySetMap.get( locale.getLanguage() );
            countrySet.add( locale.getCountry() );
        }
        possibleLanguages = languageSet.toArray( new String[languageSet.size()] );
        possibleLangToCountriesMap = new HashMap<String, String[]>();
        for ( Iterator<String> it = languageToCountrySetMap.keySet().iterator(); it.hasNext(); )
        {
            String language = it.next();
            SortedSet<String> countrySet = languageToCountrySetMap.get( language );
            String[] countries = countrySet.toArray( new String[countrySet.size()] );
            possibleLangToCountriesMap.put( language, countries );
        }

        // parse options
        if ( initialAttributeDescription == null )
        {
            initialAttributeDescription = ""; //$NON-NLS-1$
        }
        String[] attributeDescriptionComponents = initialAttributeDescription.split( ";" ); //$NON-NLS-1$
        parsedLangList = new ArrayList<String>();
        parsedOptionList = new ArrayList<String>();
        parsedBinary = false;
        for ( int i = 1; i < attributeDescriptionComponents.length; i++ )
        {
            if ( attributeDescriptionComponents[i].startsWith( "lang-" ) ) //$NON-NLS-1$
            {
                parsedLangList.add( attributeDescriptionComponents[i] );
            }
            else if ( attributeDescriptionComponents[i].equals( "binary" ) ) //$NON-NLS-1$
            {
                parsedBinary = true;
            }
            else
            {
                parsedOptionList.add( attributeDescriptionComponents[i] );
            }
        }
    }


    // ── Obi-Wan Checks Whether the Protocol Summary Is Complete ──────────────
    // Obi-Wan glances at the preview slate — whatever options Luke has dialled
    // in, the preview reflects them; since this page has no required fields,
    // it's always considered complete once visible.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the preview text with the current attribute description and
     * marks the page as complete.
     * This page has no mandatory fields so it is always complete — any
     * combination of options (including none) is valid.
     *
     * <p>For example — Obi-Wan updates the chalkboard and nods that it's correct:</p>
     * <pre>
     *   previewText.setText( wizard.getAttributeDescription() );
     *   setPageComplete( true );
     * </pre>
     */
    private void validate()
    {
        previewText.setText( wizard.getAttributeDescription() );
        setPageComplete( true );
    }


    // ── Luke Steps Forward for the Protocol Briefing ─────────────────────────
    // Obi-Wan straightens as Luke approaches — it's time to run through the
    // options checklist.  The moment the page is visible we refresh the preview
    // so Luke can see exactly where things stand.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the wizard dialog whenever this page is shown or hidden.
     * When the page becomes visible we re-validate so the preview reflects any
     * changes the user made on the type page before returning here.
     *
     * <p>For example — Luke approaches Obi-Wan; Obi-Wan updates the preview immediately:</p>
     * <pre>
     *   if ( visible ) { validate(); }
     * </pre>
     *
     * @param visible  {@code true} when this page is being shown, {@code false} when hidden.
     */
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );
        if ( visible )
        {
            validate();
        }
    }


    // ── Obi-Wan Arranges the Training Materials on the Table ─────────────────
    // Obi-Wan lays out two sections on the Falcon's briefing table: the language
    // tag section (with combo rows for language and country) and the other-options
    // section (with text rows for arbitrary options and a binary checkbox).
    // A preview pane at the bottom shows the running result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT controls for this page — the language-tag group with
     * dynamic rows, the other-options group with a binary checkbox and dynamic
     * rows, and a read-only preview text at the bottom.
     * Pre-parsed option values from the constructor are used to populate
     * the rows with their initial content.
     *
     * <p>For example — Obi-Wan lays out the briefing table with language and option rows:</p>
     * <pre>
     *   [ Language Tags ]
     *     lang-[combo▼] - [combo▼]  [+] [-]
     *   [ Other Options ]
     *     [text field]  [+] [-]
     *     [x] binary
     *   Preview: [readonly text]
     * </pre>
     *
     * @param parent  The parent composite supplied by the wizard dialog.
     */
    public void createControl( Composite parent )
    {
        shell = parent.getShell();

        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 2, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // Lang group
        langGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "AttributeOptionsWizardPage.LanguageTags" ), 2 ); //$NON-NLS-1$
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        langGroup.setLayoutData( gd );
        Composite langComposite = BaseWidgetUtils.createColumnContainer( langGroup, 6, 1 );
        langLineList = new ArrayList<LangLine>();

        BaseWidgetUtils.createSpacer( composite, 2 );

        // Options group with binary option
        optionsGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "AttributeOptionsWizardPage.OtherOptions" ), 2 ); //$NON-NLS-1$
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        optionsGroup.setLayoutData( gd );
        Composite optionsComposite = BaseWidgetUtils.createColumnContainer( optionsGroup, 3, 1 );
        optionLineList = new ArrayList<OptionLine>();
        Composite binaryComposite = BaseWidgetUtils.createColumnContainer( optionsGroup, 1, 1 );
        binaryOptionButton = BaseWidgetUtils.createCheckbox( binaryComposite, Messages
            .getString( "AttributeOptionsWizardPage.BinaryOption" ), 1 ); //$NON-NLS-1$
        binaryOptionButton.setSelection( parsedBinary );

        Label la = new Label( composite, SWT.NONE );
        gd = new GridData( GridData.GRAB_VERTICAL );
        gd.horizontalSpan = 2;
        la.setLayoutData( gd );

        // Preview text
        BaseWidgetUtils.createLabel( composite, Messages.getString( "AttributeOptionsWizardPage.Preview" ), 1 ); //$NON-NLS-1$
        previewText = BaseWidgetUtils.createReadonlyText( composite, "", 1 ); //$NON-NLS-1$

        // fill lang
        if ( parsedLangList.isEmpty() )
        {
            addLangLine( langComposite, 0 );
        }
        else
        {
            for ( int i = 0; i < parsedLangList.size(); i++ )
            {
                addLangLine( langComposite, i );
                String l = parsedLangList.get( i );
                String[] ls = l.split( "-", 3 ); //$NON-NLS-1$
                if ( ls.length > 1 )
                {
                    langLineList.get( i ).languageCombo.setText( ls[1] );
                }
                if ( ls.length > 2 )
                {
                    langLineList.get( i ).countryCombo.setText( ls[2] );
                }
            }
        }

        // fill options
        if ( parsedOptionList.isEmpty() )
        {
            addOptionLine( optionsComposite, 0 );
        }
        else
        {
            for ( int i = 0; i < parsedOptionList.size(); i++ )
            {
                addOptionLine( optionsComposite, i );
                optionLineList.get( i ).optionText.setText( parsedOptionList.get( i ) );
            }
        }

        // binary listener
        binaryOptionButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                validate();
            }
        } );

        validate();

        setControl( composite );
    }


    // ── Obi-Wan Recites the Full Protocol String Back to Luke ─────────────────
    // Obi-Wan reads every option clause Luke added — language tags first, then
    // binary, then custom options — sorts them alphabetically to ensure a
    // canonical form, and assembles the final protocol string with semicolons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Assembles and returns the options portion of the LDAP attribute description
     * string — everything after the attribute type name.
     * Language tags, the binary flag, and custom options are collected, sorted
     * case-insensitively, de-duplicated, and joined with semicolons.
     * Returns an empty string if the controls have been disposed.
     *
     * <p>For example — Obi-Wan reads the protocol clauses Luke selected:</p>
     * <pre>
     *   // lang row: "de" language, "" country  → ";lang-de"
     *   // binary checked                        → ";binary"
     *   // result sorted alphabetically          → ";binary;lang-de"
     * </pre>
     *
     * @return  The options string starting with ";" for each option, or {@code ""} if none.
     */
    String getAttributeOptions()
    {

        if ( binaryOptionButton == null || binaryOptionButton.isDisposed() )
        {
            return ""; //$NON-NLS-1$
        }

        // attribute type
        StringBuffer sb = new StringBuffer();

        // options
        // sort and unique options
        Comparator<String> comparator = new Comparator<String>()
        {
            public int compare( String s1, String s2 )
            {
                if ( s1 == null || s2 == null )
                {
                    throw new ClassCastException( Messages.getString( "AttributeOptionsWizardPage.MustNotBeNull" ) ); //$NON-NLS-1$
                }
                return s1.compareToIgnoreCase( s2 );
            }
        };
        SortedSet<String> options = new TreeSet<String>( comparator );
        if ( binaryOptionButton.getSelection() )
        {
            options.add( "binary" ); //$NON-NLS-1$
        }
        for ( int i = 0; i < optionLineList.size(); i++ )
        {
            OptionLine optionLine = optionLineList.get( i );
            if ( !"".equals( optionLine.optionText.getText() ) ) //$NON-NLS-1$
            {
                options.add( optionLine.optionText.getText() );
            }

            if ( optionLineList.size() > 1 )
            {
                optionLine.optionDeleteButton.setEnabled( true );
            }
            else
            {
                optionLine.optionDeleteButton.setEnabled( false );
            }
        }
        for ( int i = 0; i < langLineList.size(); i++ )
        {
            LangLine langLine = langLineList.get( i );
            String l = langLine.languageCombo.getText();
            String c = langLine.countryCombo.getText();

            if ( !"".equals( l ) ) //$NON-NLS-1$
            {
                String s = "lang-" + l; //$NON-NLS-1$
                if ( !"".equals( c ) ) //$NON-NLS-1$
                {
                    s += "-" + c; //$NON-NLS-1$
                }
                options.add( s );
            }

            if ( langLineList.size() > 1 )
            {
                langLine.deleteButton.setEnabled( true );
            }
            else
            {
                langLine.deleteButton.setEnabled( false );
            }
        }

        // append options
        for ( Iterator<String> it = options.iterator(); it.hasNext(); )
        {
            String option = it.next();
            sb.append( ';' );
            sb.append( option );
        }

        return sb.toString();
    }


    // ── Obi-Wan Adds Another Custom Protocol Clause to the List ──────────────
    // Obi-Wan slides a new blank rule card into the briefing at the right
    // position, preserving all the existing cards' content by disposing them
    // and recreating them in order so SWT lays them out correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Inserts a new option input row into the other-options group at the given
     * index, re-creating all existing rows to maintain correct SWT layout order.
     * Existing text values are saved before disposal and restored after.
     *
     * <p>For example — Obi-Wan slides a new protocol card into the briefing stack:</p>
     * <pre>
     *   addOptionLine( optionsComposite, 2 );
     *   // existing rows 0,1 recreated; new empty row inserted at position 2
     * </pre>
     *
     * @param optionComposite  The SWT composite that hosts the option rows.
     * @param index            The position at which to insert the new empty row.
     */
    private void addOptionLine( Composite optionComposite, int index )
    {
        OptionLine[] optionLines = optionLineList.toArray( new OptionLine[optionLineList.size()] );

        if ( optionLines.length > 0 )
        {
            for ( int i = 0; i < optionLines.length; i++ )
            {
                OptionLine oldOptionLine = optionLines[i];

                // remember values
                String oldValue = oldOptionLine.optionText.getText();

                // delete old
                oldOptionLine.optionText.dispose();
                oldOptionLine.optionAddButton.dispose();
                oldOptionLine.optionDeleteButton.dispose();
                optionLineList.remove( oldOptionLine );

                // add new
                OptionLine newOptionLine = createOptionLine( optionComposite );
                optionLineList.add( newOptionLine );

                // restore value
                newOptionLine.optionText.setText( oldValue );

                // check
                if ( index == i + 1 )
                {
                    OptionLine optionLine = createOptionLine( optionComposite );
                    optionLineList.add( optionLine );
                }
            }
        }
        else
        {
            OptionLine optionLine = createOptionLine( optionComposite );
            optionLineList.add( optionLine );
        }

        shell.layout( true, true );
    }


    // ── Obi-Wan Scripts One Custom Protocol Entry ─────────────────────────────
    // For each protocol clause row, Obi-Wan prepares a text field for the option
    // value, a "+" button to insert another clause after this one, and a "−"
    // button to remove it — then wires the listeners so changes trigger a preview
    // refresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a single option row widget group: a text field, an add (+) button,
     * and a delete (−) button, all wired to re-validate on any change.
     *
     * <p>For example — Obi-Wan prepares one protocol rule card with edit controls:</p>
     * <pre>
     *   [ [option text field] [+] [-] ]
     * </pre>
     *
     * @param optionComposite  The SWT composite to add the widgets into.
     * @return                 The newly created {@link OptionLine} wrapping all three widgets.
     */
    private OptionLine createOptionLine( final Composite optionComposite )
    {
        OptionLine optionLine = new OptionLine();

        optionLine.optionText = new Text( optionComposite, SWT.BORDER );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL );
        optionLine.optionText.setLayoutData( gd );

        optionLine.optionAddButton = new Button( optionComposite, SWT.PUSH );
        optionLine.optionAddButton.setText( "  +   " ); //$NON-NLS-1$
        optionLine.optionAddButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = optionLineList.size();
                for ( int i = 0; i < optionLineList.size(); i++ )
                {
                    OptionLine optionLine = optionLineList.get( i );
                    if ( optionLine.optionAddButton == e.widget )
                    {
                        index = i + 1;
                    }
                }

                addOptionLine( optionComposite, index );

                validate();
            }
        } );

        optionLine.optionDeleteButton = new Button( optionComposite, SWT.PUSH );
        optionLine.optionDeleteButton.setText( "  −  " ); //$NON-NLS-1$
        optionLine.optionDeleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = 0;
                for ( int i = 0; i < optionLineList.size(); i++ )
                {
                    OptionLine optionLine = optionLineList.get( i );
                    if ( optionLine.optionDeleteButton == e.widget )
                    {
                        index = i;
                    }
                }

                deleteOptionLine( optionComposite, index );

                validate();
            }
        } );

        optionLine.optionText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        return optionLine;
    }


    // ── Obi-Wan Removes a Redundant Protocol Clause ───────────────────────────
    // Obi-Wan pulls a rule card from the stack, disposes of it, and tells the
    // briefing table to reflow — the remaining cards close the gap automatically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the option row at the given index from the list and disposes its
     * SWT widgets, then triggers a shell layout refresh.
     *
     * <p>For example — Obi-Wan discards an unwanted protocol rule card:</p>
     * <pre>
     *   deleteOptionLine( optionsComposite, 1 );
     *   // row at index 1 removed; remaining rows reflow
     * </pre>
     *
     * @param optionComposite  The composite hosting the option rows; used to
     *                         trigger layout after the widgets are disposed.
     * @param index            The zero-based index of the row to remove.
     */
    private void deleteOptionLine( Composite optionComposite, int index )
    {
        OptionLine optionLine = optionLineList.remove( index );
        if ( optionLine != null )
        {
            optionLine.optionText.dispose();
            optionLine.optionAddButton.dispose();
            optionLine.optionDeleteButton.dispose();

            if ( !optionComposite.isDisposed() )
            {
                shell.layout( true, true );
            }
        }
    }

    // ── CLASS: OptionLine — ONE PROTOCOL RULE CARD ────────────────────────────
    // Each protocol clause Obi-Wan adds to the briefing is a single card
    // containing the option text and the two buttons for adding or removing it.
    // This class is that card: a simple data holder for the three SWT widgets
    // that make up one "other option" row in the wizard page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A lightweight holder for the three SWT widgets that form one custom-option
     * row in the other-options section of this page.
     * Created by {@link AttributeOptionsWizardPage#createOptionLine(Composite)}
     * and tracked in {@code optionLineList}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public class OptionLine
    {
        /** The option text. */
        public Text optionText;

        /** The option add button. */
        public Button optionAddButton;

        /** The option delete button. */
        public Button optionDeleteButton;
    }


    // ── Obi-Wan Adds Another Language Clause to the List ─────────────────────
    // Obi-Wan slides a new language row into the briefing at the right position,
    // preserving existing language/country selections by disposing and recreating
    // each row in order before inserting the new blank one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Inserts a new language-tag row into the language group at the given index,
     * re-creating all existing rows to maintain correct SWT layout order.
     * Existing language and country selections are saved and restored.
     *
     * <p>For example — Obi-Wan inserts a new dialect card into the language stack:</p>
     * <pre>
     *   addLangLine( langComposite, 1 );
     *   // existing row 0 recreated; new empty row inserted at position 1
     * </pre>
     *
     * @param langComposite  The SWT composite hosting the language rows.
     * @param index          The position at which to insert the new empty row.
     */
    private void addLangLine( Composite langComposite, int index )
    {
        LangLine[] langLines = langLineList.toArray( new LangLine[langLineList.size()] );

        if ( langLines.length > 0 )
        {
            for ( int i = 0; i < langLines.length; i++ )
            {
                LangLine oldLangLine = langLines[i];

                // remember values
                String oldLanguage = oldLangLine.languageCombo.getText();
                String oldCountry = oldLangLine.countryCombo.getText();

                // delete old
                oldLangLine.langLabel.dispose();
                oldLangLine.languageCombo.dispose();
                oldLangLine.minusLabel.dispose();
                oldLangLine.countryCombo.dispose();
                oldLangLine.addButton.dispose();
                oldLangLine.deleteButton.dispose();
                langLineList.remove( oldLangLine );

                // add new
                LangLine newLangLine = createLangLine( langComposite );
                langLineList.add( newLangLine );

                // restore value
                newLangLine.languageCombo.setText( oldLanguage );
                newLangLine.countryCombo.setText( oldCountry );

                // check
                if ( index == i + 1 )
                {
                    LangLine langLine = createLangLine( langComposite );
                    langLineList.add( langLine );
                }
            }
        }
        else
        {
            LangLine langLine = createLangLine( langComposite );
            langLineList.add( langLine );
        }

        shell.layout( true, true );
    }


    // ── Obi-Wan Drafts One Language Rule Row ─────────────────────────────────
    // Obi-Wan prepares a "lang-" label, a language combo, a dash separator, a
    // country combo (disabled until a language is chosen), and "+"/"−" buttons.
    // When the language combo changes, Obi-Wan updates the country list to match
    // the known dialects for that language.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a single language-tag row: a "lang-" prefix label, a language
     * combo, a "-" separator, a country combo (populated based on the chosen
     * language), and add/delete buttons — all wired to re-validate on change.
     *
     * <p>For example — Obi-Wan creates one language rule slot in the briefing:</p>
     * <pre>
     *   lang-[combo▼] - [combo▼]  [+] [-]
     * </pre>
     *
     * @param langComposite  The SWT composite to place the widgets into.
     * @return               The newly created {@link LangLine} holding all six widgets.
     */
    private LangLine createLangLine( final Composite langComposite )
    {
        final LangLine langLine = new LangLine();

        langLine.langLabel = BaseWidgetUtils.createLabel( langComposite, "lang-", 1 ); //$NON-NLS-1$

        langLine.languageCombo = BaseWidgetUtils.createCombo( langComposite, possibleLanguages, -1, 1 );

        langLine.minusLabel = BaseWidgetUtils.createLabel( langComposite, "-", 1 ); //$NON-NLS-1$

        langLine.countryCombo = BaseWidgetUtils.createCombo( langComposite, new String[0], -1, 1 );
        langLine.countryCombo.setEnabled( false );

        langLine.addButton = new Button( langComposite, SWT.PUSH );
        langLine.addButton.setText( "  +   " ); //$NON-NLS-1$
        langLine.addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = langLineList.size();
                for ( int i = 0; i < langLineList.size(); i++ )
                {
                    LangLine langLine = langLineList.get( i );
                    if ( langLine.addButton == e.widget )
                    {
                        index = i + 1;
                    }
                }

                addLangLine( langComposite, index );

                validate();
            }
        } );

        langLine.deleteButton = new Button( langComposite, SWT.PUSH );
        langLine.deleteButton.setText( "  −  " ); //$NON-NLS-1$
        langLine.deleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = 0;
                for ( int i = 0; i < langLineList.size(); i++ )
                {
                    LangLine langLine = langLineList.get( i );
                    if ( langLine.deleteButton == e.widget )
                    {
                        index = i;
                    }
                }

                deleteLangLine( langComposite, index );

                validate();
            }
        } );

        langLine.languageCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                if ( "".equals( langLine.languageCombo.getText() ) ) //$NON-NLS-1$
                {
                    langLine.countryCombo.setEnabled( false );
                }
                else
                {
                    langLine.countryCombo.setEnabled( true );
                    String oldValue = langLine.countryCombo.getText();
                    if ( possibleLangToCountriesMap.containsKey( langLine.languageCombo.getText() ) )
                    {
                        langLine.countryCombo.setItems( possibleLangToCountriesMap.get( langLine.languageCombo
                            .getText() ) );
                    }
                    else
                    {
                        langLine.countryCombo.setItems( new String[0] );
                    }
                    langLine.countryCombo.setText( oldValue );
                }
                validate();
            }
        } );
        langLine.countryCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        return langLine;
    }


    // ── Obi-Wan Strikes a Language Clause From the Briefing ──────────────────
    // Obi-Wan decides one of the language rule cards is redundant, pulls it from
    // the stack, disposes all its widgets, and refreshes the layout so the
    // remaining cards fill the gap cleanly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the language row at the given index from the list and disposes
     * all six of its SWT widgets, then triggers a shell layout refresh.
     *
     * <p>For example — Obi-Wan discards an unneeded language rule card:</p>
     * <pre>
     *   deleteLangLine( langComposite, 0 );
     *   // row removed; remaining lang rows reflow upward
     * </pre>
     *
     * @param langComposite  The composite hosting the language rows; used to
     *                       check dispose state before triggering layout.
     * @param index          The zero-based index of the row to remove.
     */
    private void deleteLangLine( Composite langComposite, int index )
    {
        LangLine langLine = langLineList.remove( index );
        if ( langLine != null )
        {
            langLine.langLabel.dispose();
            langLine.languageCombo.dispose();
            langLine.minusLabel.dispose();
            langLine.countryCombo.dispose();
            langLine.addButton.dispose();
            langLine.deleteButton.dispose();

            if ( !langComposite.isDisposed() )
            {
                shell.layout( true, true );
            }
        }
    }

    // ── CLASS: LangLine — ONE LANGUAGE PROTOCOL CARD ─────────────────────────
    // Each language clause Obi-Wan adds is a card containing the "lang-" prefix,
    // a language dropdown, a dash, a country dropdown, and add/delete buttons.
    // This class holds all six SWT widgets that make up one language-tag row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A lightweight holder for the six SWT widgets that form one language-tag
     * row in the language section of this page.
     * Created by {@link AttributeOptionsWizardPage#createLangLine(Composite)}
     * and tracked in {@code langLineList}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public class LangLine
    {

        /** The lang label. */
        public Label langLabel;

        /** The language combo. */
        public Combo languageCombo;

        /** The minus label. */
        public Label minusLabel;

        /** The country combo. */
        public Combo countryCombo;

        /** The add button. */
        public Button addButton;

        /** The delete button. */
        public Button deleteButton;
    }

}
