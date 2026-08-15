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

package org.apache.directory.studio.ldapbrowser.common.dialogs.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: AttributesPreferencePage — MON MOTHMA ISSUES STANDING ORDERS ──────
// Mon Mothma sits at the head of the briefing table on Home One, quietly issuing
// standing orders that govern how every unit in the Alliance presents itself —
// which color insignia for command staff, which font weight for field operatives,
// whether values are shown decorated (with context) or raw (just the numbers).
// Every Rebel follows these orders until Mon Mothma changes them.  This preference
// page works the same way: it sets the colors, fonts, and decoration rules that
// govern how LDAP attributes are rendered throughout the entire browser.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page that controls the visual appearance of LDAP
 * attributes in the browser — colors, bold/italic styles, and whether attribute
 * values are shown in decorated or raw form.
 * Think of this class as Mon Mothma's standing orders for the LDAP browser UI.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button showDecoratedValuesButton;

    private final String[] ATTRIBUTE_TYPES = new String[]
        {
            Messages.getString( "AttributesPreferencePage.ObjectClassAttribute" ), Messages.getString( "AttributesPreferencePage.MustAttributes" ), Messages.getString( "AttributesPreferencePage.MayAttributes" ), Messages.getString( "AttributesPreferencePage.OperationalAttributes" ) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    private final String[] ATTRIBUTE_FONT_CONSTANTS = new String[]
        { BrowserCommonConstants.PREFERENCE_OBJECTCLASS_FONT, BrowserCommonConstants.PREFERENCE_MUSTATTRIBUTE_FONT,
            BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_FONT,
            BrowserCommonConstants.PREFERENCE_OPERATIONALATTRIBUTE_FONT };

    private final String[] ATTRIBUTE_COLOR_CONSTANTS = new String[]
        { BrowserCommonConstants.PREFERENCE_OBJECTCLASS_COLOR, BrowserCommonConstants.PREFERENCE_MUSTATTRIBUTE_COLOR,
            BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_COLOR,
            BrowserCommonConstants.PREFERENCE_OPERATIONALATTRIBUTE_COLOR };

    private Label[] attributeTypeLabels = new Label[ATTRIBUTE_TYPES.length];

    private ColorSelector[] attributeColorSelectors = new ColorSelector[ATTRIBUTE_TYPES.length];

    private Button[] attributeBoldButtons = new Button[ATTRIBUTE_TYPES.length];

    private Button[] attributeItalicButtons = new Button[ATTRIBUTE_TYPES.length];


    // ── MON MOTHMA OPENS THE STANDING-ORDERS BRIEFING ────────────────────────────
    // Mon Mothma calls the briefing to order, names the topic ("Attributes"), and
    // reads out the general agenda ("General settings for how attributes appear").
    // She also points everyone to the right preference store so changes land in
    // the correct configuration file — not the wrong one.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the AttributesPreferencePage with its title and description,
     * and wires it to the correct preference store so our saved values end up
     * in the right place.  Eclipse calls this when the user opens the preference
     * tree and selects the Attributes node.
     *
     * <p>For example — Mon Mothma opens the session:</p>
     * <pre>
     *   title       = "Attributes"
     *   description = "General settings for attribute display"
     *   store       = BrowserCommonActivator preference store
     * </pre>
     */
    public AttributesPreferencePage()
    {
        super( Messages.getString( "AttributesPreferencePage.Attributes" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserCommonActivator.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "AttributesPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── MON MOTHMA ACKNOWLEDGES THE WORKBENCH ─────────────────────────────────────
    // A brief nod to the Eclipse workbench — Mon Mothma acknowledges the presence
    // of the platform framework.  We don't need anything from it here, but the
    // interface contract requires we implement init().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench initialises this preference page.
     * We don't need the workbench reference for anything, so this is intentionally
     * empty — we just satisfy the {@link IWorkbenchPreferencePage} contract.
     *
     * @param workbench  The Eclipse workbench instance — not used here.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── MON MOTHMA POSTS THE STANDING ORDERS ON THE BRIEFING BOARD ───────────────
    // Mon Mothma pins four rows of orders to the briefing board — one row per rank
    // category (objectClass, must, may, operational) — each row showing the unit
    // color swatch, a bold toggle, and an italic toggle.  She also posts the
    // "show decorated values" toggle at the top for everyone to see.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the widgets on the preference page: the decorated-values checkbox
     * at the top, then a group of four rows (one per attribute category) where the
     * user can set color, bold, and italic for each category.
     * This is what the user actually sees and interacts with.
     *
     * <p>For example — Mon Mothma's briefing board:</p>
     * <pre>
     *   [x] Show decorated values
     *   ---Attribute Colors and Fonts---
     *   ObjectClass  [color] [x] Bold  [ ] Italic
     *   Must         [color] [ ] Bold  [ ] Italic
     *   May          [color] [ ] Bold  [x] Italic
     *   Operational  [color] [ ] Bold  [x] Italic
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The composite containing all our preference widgets.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 1, false );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        // Show Decorated Values
        showDecoratedValuesButton = BaseWidgetUtils.createCheckbox( composite, Messages
            .getString( "AttributesPreferencePage.ShowDecoratedValues" ), 1 ); //$NON-NLS-1$
        showDecoratedValuesButton.setSelection( !getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES ) );

        // Attributes Colors And Fonts
        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        Group colorsAndFontsGroup = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "AttributesPreferencePage.AttributeColorsAndFonts" ), 1 ); //$NON-NLS-1$
        colorsAndFontsGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        Composite colorsAndFontsComposite = BaseWidgetUtils.createColumnContainer( colorsAndFontsGroup, 4, 1 );
        for ( int i = 0; i < ATTRIBUTE_TYPES.length; i++ )
        {
            attributeTypeLabels[i] = BaseWidgetUtils.createLabel( colorsAndFontsComposite, ATTRIBUTE_TYPES[i], 1 );
            attributeTypeLabels[i].setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            attributeColorSelectors[i] = new ColorSelector( colorsAndFontsComposite );
            attributeBoldButtons[i] = BaseWidgetUtils.createCheckbox( colorsAndFontsComposite, Messages
                .getString( "AttributesPreferencePage.Bold" ), 1 ); //$NON-NLS-1$
            attributeItalicButtons[i] = BaseWidgetUtils.createCheckbox( colorsAndFontsComposite, Messages
                .getString( "AttributesPreferencePage.Italic" ), 1 ); //$NON-NLS-1$

            FontData[] fontDatas = PreferenceConverter.getFontDataArray( getPreferenceStore(),
                ATTRIBUTE_FONT_CONSTANTS[i] );
            RGB rgb = PreferenceConverter.getColor( getPreferenceStore(), ATTRIBUTE_COLOR_CONSTANTS[i] );
            setColorsAndFonts( i, fontDatas, rgb );
        }

        applyDialogFont( composite );
        return composite;
    }


    private void setColorsAndFonts( int index, FontData[] fontDatas, RGB rgb )
    {
        boolean bold = isBold( fontDatas );
        boolean italic = isItalic( fontDatas );
        attributeColorSelectors[index].setColorValue( rgb );
        attributeBoldButtons[index].setSelection( bold );
        attributeItalicButtons[index].setSelection( italic );
    }


    private void setFontData( FontData[] fontDatas, Button boldButton, Button italicButton )
    {
        for ( FontData fontData : fontDatas )
        {
            int style = SWT.NORMAL;
            if ( boldButton.getSelection() )
            {
                style |= SWT.BOLD;
            }
            if ( italicButton.getSelection() )
            {
                style |= SWT.ITALIC;
            }
            fontData.setStyle( style );
        }
    }


    private boolean isBold( FontData[] fontDatas )
    {
        boolean bold = false;
        for ( FontData fontData : fontDatas )
        {
            if ( ( fontData.getStyle() & SWT.BOLD ) != SWT.NORMAL )
            {
                bold = true;
            }
        }
        return bold;
    }


    private boolean isItalic( FontData[] fontDatas )
    {
        boolean italic = false;
        for ( FontData fontData : fontDatas )
        {
            if ( ( fontData.getStyle() & SWT.ITALIC ) != SWT.NORMAL )
            {
                italic = true;
            }
        }
        return italic;
    }


    // ── MON MOTHMA COUNTERSIGNS AND DISPATCHES THE ORDERS ────────────────────────
    // Mon Mothma reviews the briefing board one last time, countersigns each order,
    // and sends them to the logistics office.  Every color, font weight, and
    // decoration preference gets written to the store here so it persists across
    // Eclipse restarts.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves all the user's choices to the preference store when they click OK or
     * Apply.  We write the decorated-values flag and then loop through all four
     * attribute categories to persist their color and font settings.
     *
     * <p>For example — Mon Mothma signs off the orders:</p>
     * <pre>
     *   store.setValue(SHOW_RAW_VALUES, !showDecoratedValues);
     *   for each category: store color + font (bold/italic flags)
     * </pre>
     *
     * @return  Always true — we have no validation that could prevent saving.
     */
    public boolean performOk()
    {
        // Show Decorated Values
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES,
            !showDecoratedValuesButton.getSelection() );

        // Attributes Colors And Fonts
        for ( int i = 0; i < ATTRIBUTE_TYPES.length; i++ )
        {
            FontData[] fontDatas = PreferenceConverter.getFontDataArray( getPreferenceStore(),
                ATTRIBUTE_FONT_CONSTANTS[i] );
            setFontData( fontDatas, attributeBoldButtons[i], attributeItalicButtons[i] );
            RGB rgb = attributeColorSelectors[i].getColorValue();
            PreferenceConverter.setValue( getPreferenceStore(), ATTRIBUTE_FONT_CONSTANTS[i], fontDatas );
            PreferenceConverter.setValue( getPreferenceStore(), ATTRIBUTE_COLOR_CONSTANTS[i], rgb );
        }

        return true;
    }


    // ── MON MOTHMA REVERTS TO THE ORIGINAL STANDING ORDERS ───────────────────────
    // Mon Mothma pulls out the original Alliance charter and restores every order
    // to its factory-issue wording — the defaults set when the plugin was first
    // installed.  She tears down today's customizations and posts the originals.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resets all widgets on this page to the plugin's default preference values.
     * Eclipse calls this when the user clicks "Restore Defaults."  We read the
     * defaults from the preference store (not the current values) and repopulate
     * every control so the UI reflects what will be saved.
     *
     * <p>For example — Mon Mothma reinstates the original charter:</p>
     * <pre>
     *   showDecoratedValues = default (true)
     *   objectClass color   = default blue
     *   must font           = default bold
     *   // ... and so on for each category
     * </pre>
     */
    protected void performDefaults()
    {
        // Show Decorated Values
        showDecoratedValuesButton.setSelection( !getPreferenceStore().getDefaultBoolean(
            BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES ) );

        // Attributes Colors And Fonts
        for ( int i = 0; i < ATTRIBUTE_TYPES.length; i++ )
        {
            FontData[] fontDatas = PreferenceConverter.getDefaultFontDataArray( getPreferenceStore(),
                ATTRIBUTE_FONT_CONSTANTS[i] );
            getPreferenceStore().setToDefault( ATTRIBUTE_FONT_CONSTANTS[i] );
            RGB rgb = PreferenceConverter.getDefaultColor( getPreferenceStore(), ATTRIBUTE_COLOR_CONSTANTS[i] );
            getPreferenceStore().setToDefault( ATTRIBUTE_COLOR_CONSTANTS[i] );
            setColorsAndFonts( i, fontDatas, rgb );
        }

        super.performDefaults();
    }

}
