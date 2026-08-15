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

package org.apache.directory.studio.ldifeditor.dialogs.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifeditor.widgets.LdifEditorWidget;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: LdifEditorSyntaxColoringPreferencePage — C-3PO COLOUR CALIBRATION ──
// C-3PO annotates every token of the LDIF communiqué with a colour-coded
// diplomatic tag: comments in grey, DNs in bold, values in the standard hue.
// The syntax-colouring preference page lets the user pick those colours and
// font styles for each of the ten syntax categories, with a live preview pane
// so they can see the result immediately.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link PreferencePage} for configuring the LDIF editor's syntax-colouring.
 * Presents ten {@link SyntaxItem} rows (comments, DN, attribute, value-type, value,
 * keywords, changetype-add, changetype-modify, changetype-delete, changetype-moddn)
 * with colour, bold, italic, strikethrough, and underline controls, plus a live
 * {@link LdifEditorWidget} preview pane.
 * Implements {@link ILdifEditor} because the embedded widget needs an editor context.
 * Think of this as C-3PO's colour-tag calibration panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorSyntaxColoringPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
    ILdifEditor
{

    /** Sample LDIF text shown in the preview pane. */
    private static final String LDIF_INITIAL = "" + "# Content record" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$
        + "dn: cn=content record" + BrowserCoreConstants.LINE_SEPARATOR + "objectClass: person" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "cn: content record" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$
        + "cn;lang-ja:: 5Za25qWt6YOo" + BrowserCoreConstants.LINE_SEPARATOR + "" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$ //$NON-NLS-2$

        + "# Add record with control" + BrowserCoreConstants.LINE_SEPARATOR + "dn: cn=add record" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "control: 1.2.3.4 true: controlValue" //$NON-NLS-1$
        + BrowserCoreConstants.LINE_SEPARATOR + "changetype: add" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$
        + "objectClass: person" + BrowserCoreConstants.LINE_SEPARATOR + "cn: add record" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$

        + "# Modify record" + BrowserCoreConstants.LINE_SEPARATOR + "dn: cn=modify record" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "changetype: modify" + BrowserCoreConstants.LINE_SEPARATOR + "add: cn" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "cn: modify record" + BrowserCoreConstants.LINE_SEPARATOR + "-" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "delete: cn" + BrowserCoreConstants.LINE_SEPARATOR + "-" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "replace: cn" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$
        + "cn: modify record" + BrowserCoreConstants.LINE_SEPARATOR + "-" + BrowserCoreConstants.LINE_SEPARATOR + "" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        + BrowserCoreConstants.LINE_SEPARATOR

        + "# Delete record" + BrowserCoreConstants.LINE_SEPARATOR + "dn: cn=delete record" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "changetype: delete" + BrowserCoreConstants.LINE_SEPARATOR + "" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR

        + "# Modify Dn record" + BrowserCoreConstants.LINE_SEPARATOR + "dn: cn=moddn record" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "changetype: moddn" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$
        + "newrdn: cn=new rdn" + BrowserCoreConstants.LINE_SEPARATOR + "deleteoldrdn: 1" //$NON-NLS-1$ //$NON-NLS-2$
        + BrowserCoreConstants.LINE_SEPARATOR + "newsuperior: cn=new superior" + BrowserCoreConstants.LINE_SEPARATOR //$NON-NLS-1$
        + "" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$

    /** The embedded LDIF editor widget used as the preview pane. */
    private LdifEditorWidget ldifEditorWidget;

    /** The ten syntax category items. */
    private SyntaxItem[] syntaxItems;

    /** Colour picker widget. */
    private ColorSelector colorSelector;

    /** Bold checkbox. */
    private Button boldCheckBox;

    /** Italic checkbox. */
    private Button italicCheckBox;

    /** Underline checkbox. */
    private Button underlineCheckBox;

    /** Strikethrough checkbox. */
    private Button strikethroughCheckBox;

    /** Table viewer listing syntax categories. */
    private TableViewer syntaxItemViewer;

    // ── CLASS: SyntaxItem — ONE COLOUR-CATEGORY ENTRY ────────────────────────
    // C-3PO's annotation index: each entry records the display name, preference
    // key, current colour, and font-style flags for one token category.
    /**
     * DTO representing one syntax-colouring category.
     * Knows how to load, save, and reset its preferences.
     */
    private class SyntaxItem
    {
        /** Human-readable display name shown in the list. */
        String displayName;

        /** Preference key base (suffixes {@code _RGB} and {@code _STYLE} are appended). */
        String key;

        /** Current colour. */
        RGB rgb;

        /** Whether the font style is bold. */
        boolean bold;

        /** Whether the font style is italic. */
        boolean italic;

        /** Whether the font style has strikethrough. */
        boolean strikethrough;

        /** Whether the font style has underline. */
        boolean underline;


        /**
         * Creates a {@code SyntaxItem} and loads its current preferences.
         *
         * @param displayName  the human-readable category name
         * @param key          the preference key base
         */
        SyntaxItem( String displayName, String key )
        {
            this.displayName = displayName;
            this.key = key;
            loadPreferences();
        }


        /**
         * Returns the combined SWT style integer for the current flags.
         *
         * @return the style integer
         */
        int getStyle()
        {
            int style = SWT.NORMAL;
            if ( bold )
                style |= SWT.BOLD;
            if ( italic )
                style |= SWT.ITALIC;
            if ( strikethrough )
                style |= TextAttribute.STRIKETHROUGH;
            if ( underline )
                style |= TextAttribute.UNDERLINE;
            return style;
        }


        /**
         * Decodes the combined SWT style integer into the individual flags.
         *
         * @param style  the style integer
         */
        void setStyle( int style )
        {
            this.bold = ( style & SWT.BOLD ) != SWT.NORMAL;
            this.italic = ( style & SWT.ITALIC ) != SWT.NORMAL;
            this.strikethrough = ( style & TextAttribute.STRIKETHROUGH ) != SWT.NORMAL;
            this.underline = ( style & TextAttribute.UNDERLINE ) != SWT.NORMAL;
        }


        /**
         * Loads the colour and style from the preference store.
         */
        void loadPreferences()
        {
            IPreferenceStore store = getPreferenceStore();
            this.rgb = PreferenceConverter.getColor( store, key
                + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX );
            int style = store.getInt( key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX );
            setStyle( style );
        }


        /**
         * Saves the colour and style to the preference store.
         */
        void savePreferences()
        {
            IPreferenceStore store = getPreferenceStore();
            PreferenceConverter
                .setValue( store, key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX, rgb );
            store.setValue( key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, getStyle() );
        }


        /**
         * Resets the colour and style to their factory defaults.
         */
        void loadDefaultPreferences()
        {
            IPreferenceStore store = getPreferenceStore();
            String colorKey = key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX;
            store.setToDefault( colorKey);
            String styleKey = key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX;
            store.setToDefault( styleKey);
            this.rgb = PreferenceConverter.getDefaultColor( store, colorKey );
            int style = store.getDefaultInt( styleKey );
            setStyle( style );
        }


        /**
         * Returns the display name for the table viewer label provider.
         *
         * @return the display name
         */
        public String toString()
        {
            return displayName;
        }
    }


    // ── CONSTRUCT THE PREFERENCE PAGE ─────────────────────────────────────────
    /**
     * Creates a new syntax-colouring preference page and binds it to the
     * LDIF editor preference store.
     */
    public LdifEditorSyntaxColoringPreferencePage()
    {
        super( Messages.getString( "LdifEditorSyntaxColoringPreferencePage.SyntaxColoring" ) ); //$NON-NLS-1$
        super.setPreferenceStore( LdifEditorActivator.getDefault().getPreferenceStore() );
        // super.setDescription("");
    }


    // ── INITIALISE ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────
    /**
     * Disposes the embedded {@link LdifEditorWidget}.
     */
    public void dispose()
    {
        ldifEditorWidget.dispose();
        super.dispose();
    }


    // ── BUILD THE PAGE CONTENTS ───────────────────────────────────────────────
    // C-3PO's technician wires up the category list on the left, the
    // colour/style controls in the middle, and the live preview pane below.
    /**
     * {@inheritDoc}
     *
     * <p>Creates the syntax category list, colour/style controls, and live
     * preview pane.  Initialises all ten {@link SyntaxItem} rows and selects
     * the first one.</p>
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
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        composite.setLayoutData( gd );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        createSyntaxPage( composite );
        createPreviewer( composite );

        syntaxItems = new SyntaxItem[10];
        syntaxItems[0] = new SyntaxItem(
            Messages.getString( "LdifEditorSyntaxColoringPreferencePage.Comments" ), LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_COMMENT ); //$NON-NLS-1$
        syntaxItems[1] = new SyntaxItem(
            Messages.getString( "LdifEditorSyntaxColoringPreferencePage.DN" ), LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_DN ); //$NON-NLS-1$
        syntaxItems[2] = new SyntaxItem( Messages
            .getString( "LdifEditorSyntaxColoringPreferencePage.AttributeDescriptions" ), //$NON-NLS-1$
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_ATTRIBUTE );
        syntaxItems[3] = new SyntaxItem(
            Messages.getString( "LdifEditorSyntaxColoringPreferencePage.ValueTypes" ), LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUETYPE ); //$NON-NLS-1$
        syntaxItems[4] = new SyntaxItem(
            Messages.getString( "LdifEditorSyntaxColoringPreferencePage.Values" ), LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUE ); //$NON-NLS-1$
        syntaxItems[5] = new SyntaxItem( Messages.getString( "LdifEditorSyntaxColoringPreferencePage.Keywords" ), //$NON-NLS-1$
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_KEYWORD );
        syntaxItems[6] = new SyntaxItem( Messages.getString( "LdifEditorSyntaxColoringPreferencePage.ChangetypeAdd" ), //$NON-NLS-1$
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEADD );
        syntaxItems[7] = new SyntaxItem(
            Messages.getString( "LdifEditorSyntaxColoringPreferencePage.ChangetypeModify" ), //$NON-NLS-1$
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODIFY );
        syntaxItems[8] = new SyntaxItem(
            Messages.getString( "LdifEditorSyntaxColoringPreferencePage.ChangetypeDelete" ), //$NON-NLS-1$
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEDELETE );
        syntaxItems[9] = new SyntaxItem(
            Messages.getString( "LdifEditorSyntaxColoringPreferencePage.ChangetypeModdn" ), //$NON-NLS-1$
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODDN );
        syntaxItemViewer.setInput( syntaxItems );
        syntaxItemViewer.setSelection( new StructuredSelection( syntaxItems[0] ) );

        return composite;
    }


    // ── BUILD THE SYNTAX-ITEM LIST AND CONTROLS ───────────────────────────────
    /**
     * Creates the syntax-category table viewer and the colour/style controls
     * (colour selector, bold, italic, strikethrough, underline checkboxes).
     *
     * @param parent  the parent composite
     */
    private void createSyntaxPage( Composite parent )
    {

        BaseWidgetUtils.createLabel( parent, Messages.getString( "LdifEditorSyntaxColoringPreferencePage.Element" ), 1 ); //$NON-NLS-1$

        Composite editorComposite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        syntaxItemViewer = new TableViewer( editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER
            | SWT.FULL_SELECTION );
        syntaxItemViewer.setLabelProvider( new LabelProvider() );
        syntaxItemViewer.setContentProvider( new ArrayContentProvider() );
        // colorListViewer.setSorter(new WorkbenchViewerSorter());
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.heightHint = convertHeightInCharsToPixels( 5 );
        syntaxItemViewer.getControl().setLayoutData( gd );

        Composite stylesComposite = BaseWidgetUtils.createColumnContainer( editorComposite, 1, 1 );
        stylesComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        Composite colorComposite = BaseWidgetUtils.createColumnContainer( stylesComposite, 2, 1 );
        BaseWidgetUtils.createLabel( colorComposite, Messages
            .getString( "LdifEditorSyntaxColoringPreferencePage.Color" ), 1 ); //$NON-NLS-1$
        colorSelector = new ColorSelector( colorComposite );
        boldCheckBox = BaseWidgetUtils.createCheckbox( stylesComposite, Messages
            .getString( "LdifEditorSyntaxColoringPreferencePage.Bold" ), 1 ); //$NON-NLS-1$
        italicCheckBox = BaseWidgetUtils.createCheckbox( stylesComposite, Messages
            .getString( "LdifEditorSyntaxColoringPreferencePage.Italic" ), 1 ); //$NON-NLS-1$
        strikethroughCheckBox = BaseWidgetUtils.createCheckbox( stylesComposite, Messages
            .getString( "LdifEditorSyntaxColoringPreferencePage.Strikethrough" ), 1 ); //$NON-NLS-1$
        underlineCheckBox = BaseWidgetUtils.createCheckbox( stylesComposite, Messages
            .getString( "LdifEditorSyntaxColoringPreferencePage.Underline" ), 1 ); //$NON-NLS-1$

        syntaxItemViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                handleSyntaxItemViewerSelectionEvent();
            }
        } );
        colorSelector.addListener( new IPropertyChangeListener()
        {
            public void propertyChange( PropertyChangeEvent event )
            {
                handleColorSelectorEvent();
            }
        } );
        boldCheckBox.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                handleBoldSelectionEvent();
            }
        } );
        italicCheckBox.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                handleItalicSelectionEvent();
            }
        } );
        strikethroughCheckBox.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                handleStrikethroughSelectionEvent();
            }
        } );
        underlineCheckBox.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                handleUnderlineSelectionEvent();
            }
        } );

    }


    // ── REACT TO UNDERLINE CHANGE ─────────────────────────────────────────────
    /**
     * Updates the selected syntax item's underline flag and refreshes the preview.
     */
    private void handleUnderlineSelectionEvent()
    {
        SyntaxItem item = getSyntaxItem();
        if ( item != null )
        {
            item.underline = underlineCheckBox.getSelection();
            setTextAttribute( item );
        }
    }


    // ── REACT TO STRIKETHROUGH CHANGE ─────────────────────────────────────────
    /**
     * Updates the selected syntax item's strikethrough flag and refreshes the preview.
     */
    private void handleStrikethroughSelectionEvent()
    {
        SyntaxItem item = getSyntaxItem();
        if ( item != null )
        {
            item.strikethrough = strikethroughCheckBox.getSelection();
            setTextAttribute( item );
        }
    }


    // ── REACT TO ITALIC CHANGE ────────────────────────────────────────────────
    /**
     * Updates the selected syntax item's italic flag and refreshes the preview.
     */
    private void handleItalicSelectionEvent()
    {
        SyntaxItem item = getSyntaxItem();
        if ( item != null )
        {
            item.italic = italicCheckBox.getSelection();
            setTextAttribute( item );
        }
    }


    // ── REACT TO BOLD CHANGE ──────────────────────────────────────────────────
    /**
     * Updates the selected syntax item's bold flag and refreshes the preview.
     */
    private void handleBoldSelectionEvent()
    {
        SyntaxItem item = getSyntaxItem();
        if ( item != null )
        {
            item.bold = boldCheckBox.getSelection();
            setTextAttribute( item );
        }
    }


    // ── REACT TO COLOUR CHANGE ────────────────────────────────────────────────
    /**
     * Updates the selected syntax item's colour and refreshes the preview.
     */
    private void handleColorSelectorEvent()
    {
        SyntaxItem item = getSyntaxItem();
        if ( item != null )
        {
            item.rgb = colorSelector.getColorValue();
            setTextAttribute( item );
        }
    }


    // ── REACT TO LIST SELECTION CHANGE ────────────────────────────────────────
    /**
     * Loads the selected syntax item's colour and style flags into the controls.
     */
    private void handleSyntaxItemViewerSelectionEvent()
    {
        SyntaxItem item = getSyntaxItem();
        if ( item != null )
        {
            colorSelector.setColorValue( item.rgb );
            boldCheckBox.setSelection( item.bold );
            italicCheckBox.setSelection( item.italic );
            strikethroughCheckBox.setSelection( item.strikethrough );
            underlineCheckBox.setSelection( item.underline );
        }
    }


    // ── GET THE SELECTED SYNTAX ITEM ──────────────────────────────────────────
    /**
     * Returns the currently selected {@link SyntaxItem}, or {@code null}.
     *
     * @return the selected syntax item, or {@code null}
     */
    private SyntaxItem getSyntaxItem()
    {
        SyntaxItem item = ( SyntaxItem ) ( ( IStructuredSelection ) syntaxItemViewer.getSelection() ).getFirstElement();
        return item;
    }


    // ── PUSH A TEXT ATTRIBUTE CHANGE TO THE PREVIEW ───────────────────────────
    /**
     * Pushes the updated colour and style for {@code item} to the source-viewer
     * configuration and refreshes the preview document.
     *
     * @param item  the syntax item whose attributes changed
     */
    private void setTextAttribute( SyntaxItem item )
    {
        ldifEditorWidget.getSourceViewerConfiguration().setTextAttribute( item.key, item.rgb, item.getStyle() );

        int topIndex = ldifEditorWidget.getSourceViewer().getTopIndex();
        // ldifEditorWidget.getSourceViewer().getDocument().set("");
        ldifEditorWidget.getSourceViewer().getDocument().set( LDIF_INITIAL );
        ldifEditorWidget.getSourceViewer().setTopIndex( topIndex );
    }


    // ── BUILD THE PREVIEW PANE ────────────────────────────────────────────────
    /**
     * Creates the read-only {@link LdifEditorWidget} preview pane pre-loaded
     * with the sample LDIF text.
     *
     * @param parent  the parent composite
     */
    private void createPreviewer( Composite parent )
    {

        BaseWidgetUtils.createLabel( parent, Messages.getString( "LdifEditorSyntaxColoringPreferencePage.Preview" ), 1 ); //$NON-NLS-1$

        ldifEditorWidget = new LdifEditorWidget( null, LDIF_INITIAL, false );
        ldifEditorWidget.createWidget( parent );
        ldifEditorWidget.getSourceViewer().setEditable( false );

        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertWidthInCharsToPixels( 20 );
        gd.heightHint = convertHeightInCharsToPixels( 5 );
        ldifEditorWidget.getSourceViewer().getControl().setLayoutData( gd );

    }


    // ── ILdifEditor: GET CONNECTION ───────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the embedded {@link LdifEditorWidget}.</p>
     */
    public IBrowserConnection getConnection()
    {
        return ldifEditorWidget.getConnection();
    }


    // ── ILdifEditor: GET LDIF MODEL ───────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the embedded {@link LdifEditorWidget}.</p>
     */
    public LdifFile getLdifModel()
    {
        return ldifEditorWidget.getLdifModel();
    }


    // ── SAVE PREFERENCES ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Persists all ten syntax item colours and styles to the preference store.</p>
     */
    public boolean performOk()
    {
        for ( int i = 0; i < syntaxItems.length; i++ )
        {
            syntaxItems[i].savePreferences();
        }
        return true;
    }


    // ── RESTORE DEFAULTS ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Reloads factory defaults for all ten syntax items and refreshes the
     * controls and preview.</p>
     */
    protected void performDefaults()
    {
        for ( int i = 0; i < syntaxItems.length; i++ )
        {
            syntaxItems[i].loadDefaultPreferences();
            setTextAttribute( syntaxItems[i] );
        }
        handleSyntaxItemViewerSelectionEvent();
        super.performDefaults();
    }


    // ── IAdaptable ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} — no adapters registered.</p>
     */
    public Object getAdapter( Class adapter )
    {
        return null;
    }

}
