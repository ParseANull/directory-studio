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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;


// -- CLASS: EntryEditorWidgetLabelProvider -- C-3PO READS THE REBEL BRIEFING ROSTER --
// In the Rebel briefing room on Yavin IV, C-3PO stands at the podium reading out each
// pilot's call sign and stats -- adjusting his voice and color-coded annotations depending
// on whether they're a Red Leader (must-have), a reserve pilot (may-have), or an
// intelligence asset (operational).
// This class does the same: for every row JFace wants to draw, we supply the text for
// each column, the font (bold for important attributes, italic for inconsistent ones),
// and the foreground color (red for errors, custom per attribute type).
// ---------------------------------------------------------------------------------
/**
 * Provides the display text, fonts, and colors for every row in the entry editor table.
 * JFace calls us through three interfaces: {@link ITableLabelProvider} for text/images,
 * {@link IFontProvider} for per-row fonts, and {@link IColorProvider} for per-row colors.
 * Think of this class as C-3PO at the Rebel briefing podium: he reads out names and values
 * and adjusts his annotation style based on how important each attribute is.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider,
    IColorProvider
{
    /** The viewer */
    private TreeViewer viewer;

    /** The value editor manager. */
    private ValueEditorManager valueEditorManager;


    // -- C-3PO TAKES HIS PLACE AT THE BRIEFING PODIUM --------------------------
    // Before the briefing starts, C-3PO is handed two things: a reference to the
    // display screen (the viewer) so he can query back into the content provider
    // when counting values, and a roster of value-editor translators so he can
    // render each data type in a human-friendly way.
    // ---------------------------------------------------------------------------------
    /**
     * Creates a new label provider wired to the given viewer and value-editor manager.
     * We need the {@code viewer} reference to look up the content provider and active filter
     * when computing how many child values are visible for a folded attribute group.
     * The {@code valueEditorManager} knows which display format to use for each value type.
     *
     * <p>For example -- C-3PO steps up to the podium:</p>
     * <pre>
     *   C-3PO is handed the display screen remote and the pilot roster.
     *   He acknowledges both: "I am ready to begin the briefing at your convenience."
     * </pre>
     *
     * @param viewer               the JFace tree viewer we're labelling; used to reach the content provider
     * @param valueEditorManager   locates the right value renderer for each data type
     */
    public EntryEditorWidgetLabelProvider( TreeViewer viewer, ValueEditorManager valueEditorManager )
    {
        this.viewer = viewer;
        this.valueEditorManager = valueEditorManager;
    }


    // -- C-3PO HANDS BACK HIS PODIUM NOTES AND EXITS ---------------------------
    // The briefing is over. C-3PO nulls out the valueEditorManager reference so
    // the GC can reclaim it, and lets the LabelProvider superclass clean itself up too.
    // ---------------------------------------------------------------------------------
    /**
     * Releases resources when the viewer is torn down.
     * Delegates to the superclass and clears our own reference to the value editor manager.
     *
     * <p>For example -- C-3PO wraps up the briefing:</p>
     * <pre>
     *   C-3PO steps down from the podium and hands back the roster.
     *   "Mission briefing complete. All materials returned."
     * </pre>
     */
    public void dispose()
    {
        super.dispose();
        valueEditorManager = null;
    }


    // -- C-3PO READS OUT A CELL'S TEXT ------------------------------------------
    // For each cell in the table, C-3PO announces the appropriate content.
    // Column 0 (Key): the attribute description -- or for a folded group, the name
    // plus a "(N values)" count showing how many children pass the current filter.
    // Column 1 (Value): the human-friendly display string from the value editor.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the text to display in a specific table column for a given element.
     * For an {@link IValue} in the Key column we return the attribute description;
     * in the Value column we ask the {@link ValueEditorManager} for the best display string.
     * For a folded {@link IAttribute} row we return "description (N values)" in the Key column.
     *
     * <p>For example -- C-3PO announces a row:</p>
     * <pre>
     *   Column 0: "mail"               (the attribute name)
     *   Column 1: "leia@alderaan.gov"  (rendered by the value editor)
     *   Folded:   "memberOf (42 values)" (group header with visible-child count)
     * </pre>
     *
     * @param obj    the element being labelled -- an {@link IValue} or {@link IAttribute}
     * @param index  the column index; 0 = attribute name, 1 = value
     * @return       the display string for that cell; never null, may be empty
     */
    public final String getColumnText( Object obj, int index )
    {
        if ( obj instanceof IValue )
        {
            IValue value = ( IValue ) obj;
            switch ( index )
            {
                case EntryEditorWidgetTableMetadata.KEY_COLUMN_INDEX:
                    return value.getAttribute().getDescription();
                case EntryEditorWidgetTableMetadata.VALUE_COLUMN_INDEX:
                    IValueEditor vp = this.valueEditorManager.getCurrentValueEditor( value );
                    String dv = vp.getDisplayValue( value );
                    return dv;
                default:
                    return ""; //$NON-NLS-1$
            }
        }
        else if ( obj instanceof IAttribute )
        {
            IAttribute attribute = ( IAttribute ) obj;
            if ( index == EntryEditorWidgetTableMetadata.KEY_COLUMN_INDEX )
            {
                return NLS
                    .bind(
                        Messages.getString( "EntryEditorWidgetLabelProvider.AttributeLabel" ), //$NON-NLS-1$
                        attribute.getDescription(), getNumberOfValues( attribute ) );
            }
            else
            {
                return ""; //$NON-NLS-1$
            }
        }
        else
        {
            return ""; //$NON-NLS-1$
        }
    }


    // -- C-3PO COUNTS VISIBLE PILOTS ON THE ROSTER -----------------------------
    // When announcing a folded group header, C-3PO needs to say how many entries
    // are actually visible after the quick filter has done its work -- not the raw
    // total. He asks the content provider for all children, then checks each one
    // against the active filter.
    // ---------------------------------------------------------------------------------
    /**
     * Counts how many child values of a folded attribute group are currently visible
     * after the active filter has been applied. We ask the content provider for all
     * children and then run each one through the filter's {@code select()} method.
     *
     * <p>For example -- C-3PO counts surviving candidates:</p>
     * <pre>
     *   "memberOf" has 500 values. Quick filter: "rebel".
     *   C-3PO tallies: 12 values survive the filter.
     *   He announces: "memberOf (12 values)"
     * </pre>
     *
     * @param attribute  the folded attribute whose visible-child count we need
     * @return           the number of child values that pass the current quick filter
     */
    private int getNumberOfValues( IAttribute attribute )
    {
        EntryEditorWidgetContentProvider contentProvider = ( EntryEditorWidgetContentProvider ) viewer
            .getContentProvider();
        EntryEditorWidgetFilter filter = ( EntryEditorWidgetFilter ) viewer.getFilters()[0];

        int count = 0;

        for ( Object child : contentProvider.getChildren( attribute ) )
        {
            if ( filter.select( viewer, attribute, child ) )
            {
                count++;
            }
        }

        return count;
    }


    // -- C-3PO PROVIDES NO ICONS -- JUST WORDS ----------------------------------
    // Unlike a file browser, the entry editor table doesn't use column icons.
    // C-3PO has been asked for an image and he simply hands back nothing.
    // Returning null here is the correct JFace contract for "no image."
    // ---------------------------------------------------------------------------------
    /**
     * Returns null for all columns -- we don't show per-cell images in the entry editor.
     * JFace requires this method; we satisfy the contract by always returning null.
     *
     * <p>For example -- C-3PO is asked for a picture:</p>
     * <pre>
     *   General Dodonna: "Do you have a visual for that attribute?"
     *   C-3PO: "I'm afraid not, sir. Text only for the entry editor."
     * </pre>
     *
     * @param element  the element being labelled (ignored)
     * @param index    the column index (ignored)
     * @return         always null
     */
    public final Image getColumnImage( Object element, int index )
    {
        return null;
    }


    // -- C-3PO MODULATES HIS DELIVERY FOR ATTRIBUTE IMPORTANCE -----------------
    // At the Rebel briefing, C-3PO emphasises critical pilots (must-attributes) in
    // bold, operational intelligence officers in a separate style, and flags any
    // inconsistent or empty records in bold-italic so the commanders notice immediately.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the font to use for a given element in the entry editor.
     * Empty values and inconsistent attributes get bold-italic (a visual warning).
     * Other attributes get a font from preferences based on their type:
     * objectClass attributes, must-attributes, operational attributes, and may-attributes
     * each have their own configurable font.
     *
     * <p>For example -- C-3PO adjusts his vocal style per pilot rank:</p>
     * <pre>
     *   Red Leader (must-attribute)    -> bold delivery
     *   Intelligence asset (operational) -> subdued style
     *   Missing flight data (empty value) -> bold-italic warning
     *   Standard reserve pilot (may)   -> normal delivery
     * </pre>
     *
     * @param element  the {@link IValue} or {@link IAttribute} being rendered
     * @return         the {@link Font} to use, or null to fall back to the system default
     */
    public Font getFont( Object element )
    {
        IAttribute attribute = null;
        IValue value = null;
        if ( element instanceof IAttribute )
        {
            attribute = ( IAttribute ) element;
        }
        else if ( element instanceof IValue )
        {
            value = ( IValue ) element;
            attribute = value.getAttribute();
        }

        // inconsistent attributes and values
        if ( value != null )
        {
            if ( value.isEmpty() )
            {
                FontData[] fontData = Display.getDefault().getSystemFont().getFontData();
                FontData fontDataBoldItalic = new FontData( fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD
                    | SWT.ITALIC );
                return BrowserCommonActivator.getDefault().getFont( new FontData[]
                    { fontDataBoldItalic } );
            }
        }
        if ( attribute != null && value == null )
        {
            if ( !attribute.isConsistent() )
            {
                FontData[] fontData = Display.getDefault().getSystemFont().getFontData();
                FontData fontDataBoldItalic = new FontData( fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD
                    | SWT.ITALIC );
                return BrowserCommonActivator.getDefault().getFont( new FontData[]
                    { fontDataBoldItalic } );
            }
        }

        // attribute type
        if ( attribute != null )
        {
            if ( attribute.isObjectClassAttribute() )
            {
                FontData[] fontData = PreferenceConverter.getFontDataArray( BrowserCommonActivator.getDefault()
                    .getPreferenceStore(), BrowserCommonConstants.PREFERENCE_OBJECTCLASS_FONT );
                return BrowserCommonActivator.getDefault().getFont( fontData );
            }
            else if ( attribute.isMustAttribute() )
            {
                FontData[] fontData = PreferenceConverter.getFontDataArray( BrowserCommonActivator.getDefault()
                    .getPreferenceStore(), BrowserCommonConstants.PREFERENCE_MUSTATTRIBUTE_FONT );
                return BrowserCommonActivator.getDefault().getFont( fontData );
            }
            else if ( attribute.isOperationalAttribute() )
            {
                FontData[] fontData = PreferenceConverter.getFontDataArray( BrowserCommonActivator.getDefault()
                    .getPreferenceStore(), BrowserCommonConstants.PREFERENCE_OPERATIONALATTRIBUTE_FONT );
                return BrowserCommonActivator.getDefault().getFont( fontData );
            }
            else
            {
                FontData[] fontData = PreferenceConverter.getFontDataArray( BrowserCommonActivator.getDefault()
                    .getPreferenceStore(), BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_FONT );
                return BrowserCommonActivator.getDefault().getFont( fontData );
            }
        }
        else
        {
            return null;
        }
    }


    // -- C-3PO COLOR-CODES EACH ROW IN THE BRIEFING SLIDES ---------------------
    // The Rebel briefing uses color-coded annotations: red for problem entries,
    // attribute-type colors for everything else. C-3PO checks the error conditions
    // first (empty values, inconsistent attributes) and falls back to the
    // preference-configured color per attribute category.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the foreground text color for a given element.
     * Empty values and inconsistent attribute groups render in the error color (red by default).
     * Other elements use the per-attribute-type color from the preference store;
     * if the user hasn't customized the color, we return null and let the system default apply.
     *
     * <p>For example -- C-3PO applies color annotations to the briefing slides:</p>
     * <pre>
     *   Empty value "mail: (empty)"        -> error red
     *   Inconsistent attribute group        -> error red
     *   objectClass attribute               -> objectClass color from prefs
     *   Must attribute                      -> must-attribute color from prefs
     *   Normal may attribute                -> may-attribute color from prefs
     * </pre>
     *
     * @param element  the {@link IValue} or {@link IAttribute} being rendered
     * @return         the foreground {@link Color}, or null to use the system default
     */
    public Color getForeground( Object element )
    {
        IAttribute attribute = null;
        IValue value = null;
        if ( element instanceof IAttribute )
        {
            attribute = ( IAttribute ) element;
        }
        else if ( element instanceof IValue )
        {
            value = ( IValue ) element;
            attribute = value.getAttribute();
        }

        // inconsistent attributes and values
        if ( value != null )
        {
            if ( value.isEmpty() )
            {
                return CommonUIPlugin.getDefault().getColor( CommonUIConstants.ERROR_COLOR );
            }
        }

        if ( attribute != null && value == null )
        {
            if ( !attribute.isConsistent() )
            {
                return CommonUIPlugin.getDefault().getColor( CommonUIConstants.ERROR_COLOR );
            }
        }

        // attribute type
        if ( attribute != null )
        {
            if ( attribute.isObjectClassAttribute() )
            {
                return getColorIfNotDefaultElseNull( BrowserCommonConstants.PREFERENCE_OBJECTCLASS_COLOR );
            }
            else if ( attribute.isMustAttribute() )
            {
                return getColorIfNotDefaultElseNull( BrowserCommonConstants.PREFERENCE_MUSTATTRIBUTE_COLOR );
            }
            else if ( attribute.isOperationalAttribute() )
            {
                return getColorIfNotDefaultElseNull( BrowserCommonConstants.PREFERENCE_OPERATIONALATTRIBUTE_COLOR );
            }
            else
            {
                return getColorIfNotDefaultElseNull( BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_COLOR );
            }
        }
        else
        {
            return null;
        }
    }


    // -- C-3PO LOOKS UP A COLOR AND ONLY USES IT IF CUSTOMIZED -----------------
    // Before announcing a colored annotation, C-3PO checks whether the user has
    // actually set a custom color for that attribute category. If they're still
    // using the factory default, he returns null and lets the OS pick the color
    // naturally -- this avoids overriding the system theme unnecessarily.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the {@link Color} registered under the given preference key,
     * but only if the user has actually customized it away from the default.
     * If the preference is still at its factory default, we return null so
     * the system theme's foreground color takes effect instead.
     *
     * <p>For example -- C-3PO checks the color card:</p>
     * <pre>
     *   C-3PO: "Has someone customized the must-attribute color?"
     *   Yes -> return that custom color (e.g., dark blue)
     *   No  -> return null, let the OS choose
     * </pre>
     *
     * @param color  the preference key naming the color to look up
     * @return       the custom {@link Color}, or null if the preference is still default
     */
    private Color getColorIfNotDefaultElseNull( String color )
    {
        BrowserCommonActivator plugin = BrowserCommonActivator.getDefault();
        IPreferenceStore preferenceStore = plugin.getPreferenceStore();

        if ( preferenceStore.isDefault( color ) )
        {
            return null;
        }
        RGB rgb = PreferenceConverter.getColor( preferenceStore, color );
        return plugin.getColor( rgb );
    }


    // -- C-3PO HAS NO BACKGROUND COLOR OPINION ---------------------------------
    // General Dodonna asks C-3PO what background color each row should have.
    // C-3PO has no opinion -- we let the table widget handle background painting
    // entirely on its own, which means alternating row shading and selection
    // highlighting work out-of-the-box.
    // ---------------------------------------------------------------------------------
    /**
     * Returns null for all elements, meaning we do not override row background colors.
     * JFace and SWT handle background painting (selection highlight, alternating rows)
     * without our help.
     *
     * <p>For example -- C-3PO defers on background:</p>
     * <pre>
     *   General Dodonna: "What background color for that row?"
     *   C-3PO: "I defer to the system defaults, General. Null it is."
     * </pre>
     *
     * @param element  the element being rendered (ignored)
     * @return         always null
     */
    public Color getBackground( Object element )
    {
        return null;
    }

}
