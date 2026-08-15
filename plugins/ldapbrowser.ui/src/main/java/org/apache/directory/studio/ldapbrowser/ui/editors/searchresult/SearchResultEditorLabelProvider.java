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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;


// ── CLASS: SearchResultEditorLabelProvider — Luke Watching the Binary Sunset ──
// Luke stands at the Lars homestead looking out at the two suns setting over
// Tatooine — he sees everything laid out before him, the vast landscape rendered
// clearly.  He doesn't interact with it yet; he just observes and understands.
// This label provider is that vista: given a row and column, it computes what
// text and font to show.  It doesn't change anything — it just renders clearly
// so the user can see the big picture of their search results.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The label provider for the search result editor's table.
 * For each cell it computes the display text (via the value editor's
 * {@code getDisplayValue}) and the font (based on attribute type — must, may,
 * operational, objectClass each get their own font preference).
 * No images are provided; no colors are differentiated (both return null/null).
 * Think of Luke's binary sunset: we're rendering a clear, readable picture of
 * the LDAP entries without changing anything.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider,
    ITableColorProvider
{

    /** The value editor manager. */
    private ValueEditorManager valueEditorManager;

    /** The search. */
    private ISearch search;

    /** The show Dn flag. */
    private boolean showDn;


    // ── Luke Gets a Clear View ────────────────────────────────────────────────
    // Luke needs to be able to see the full landscape — we hand him the value
    // editor manager so he can translate raw attribute values into readable text.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the label provider with the value editor manager it will use to
     * format attribute values for display.
     *
     * @param valueEditorManager the manager that maps attribute types to display formatters
     */
    public SearchResultEditorLabelProvider( ValueEditorManager valueEditorManager )
    {
        this.valueEditorManager = valueEditorManager;
    }


    // ── Luke's View Updates to the New Horizon ────────────────────────────────
    // A different search is now in scope — Luke turns to look at the new landscape.
    // We update the search and DN-flag so subsequent column-text calls use the
    // correct returning attributes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the current search context.
     * Call this whenever the editor switches to a different search so the label
     * provider uses the correct returning-attributes list when computing column text.
     *
     * @param newSearch the newly active search
     * @param showDn    {@code true} if the DN column is being shown as the first column
     */
    public void inputChanged( ISearch newSearch, boolean showDn )
    {
        this.search = newSearch;
        this.showDn = showDn;
    }


    // ── Luke Reads the Landscape at a Specific Point ──────────────────────────
    // Luke looks at a specific spot on the horizon — row X, column Y — and
    // reports what he sees.  If the coordinates are out of bounds (an "invisible"
    // column), he reports nothing.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for the cell at the given row and column.
     * For the DN column we return the full DN name.  For other columns we ask the
     * current value editor to format the attribute value.  Long values are truncated
     * to 50 characters.  We return empty string for invisible (out-of-bounds) columns.
     *
     * @param obj   the row element, expected to be an {@link ISearchResult}
     * @param index the zero-based column index
     * @return the display string; never null
     */
    public final String getColumnText( Object obj, int index )
    {
        if ( obj instanceof ISearchResult )
        {
            String property;
            try
            {
                ISearchResult result = ( ISearchResult ) obj;

                if ( showDn && index == 0 )
                {
                    property = BrowserUIConstants.DN;
                }
                else if ( showDn && index > 0 )
                {
                    property = search.getReturningAttributes()[index - 1];
                }
                else
                {
                    property = search.getReturningAttributes()[index];
                }

                if ( property == BrowserUIConstants.DN )
                {
                    return result.getDn().getName();
                }
                else
                {
                    AttributeHierarchy ah = result.getAttributeWithSubtypes( property );
                    return getDisplayValue( ah );
                }

            }
            catch ( ArrayIndexOutOfBoundsException aioobe )
            {
                // occurs on "invisible" columns
                return ""; //$NON-NLS-1$
            }

        }
        else if ( obj != null )
        {
            return obj.toString();
        }
        else
        {
            return ""; //$NON-NLS-1$
        }
    }


    // ── Luke Sees No Images, Only Text ───────────────────────────────────────
    // The binary sunset is pure light — no icons, just the text of the landscape.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — the search result table uses text only, no per-cell icons.
     *
     * @param obj   the row element
     * @param index the column index
     * @return {@code null}
     */
    public final Image getColumnImage( Object obj, int index )
    {
        return null;
    }


    // ── Luke Reads the Fine Print ────────────────────────────────────────────
    // Luke squints to read the detailed value — we ask the value editor to format
    // it, then cap it at 50 characters to keep the cell readable.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Formats an attribute hierarchy's value for display, capped at 50 characters.
     * We ask the current value editor to produce the display string, then append
     * "..." if it's too long.  Returns empty string if the attribute is absent or
     * has no value editor.
     *
     * @param ah the attribute hierarchy to format, or {@code null}
     * @return the formatted display string; never null, at most 50 chars visible
     */
    private String getDisplayValue( AttributeHierarchy ah )
    {
        IValueEditor vp = valueEditorManager.getCurrentValueEditor( ah );
        if ( vp == null )
        {
            return ""; //$NON-NLS-1$
        }

        String value = vp.getDisplayValue( ah );
        if ( value.length() > 50 )
        {
            value = value.substring( 0, 47 ) + "..."; //$NON-NLS-1$
        }
        return value;
    }


    // ── Luke Distinguishes the Important From the Background ─────────────────
    // Not all points in the landscape look the same — must-attributes stand out
    // in bold, operational attributes in a different style, objectClass different
    // again.  Luke's eye distinguishes them from the regular "may" attributes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display font for the cell based on the attribute's schema role.
     * Must attributes, objectClass attributes, and operational attributes each get
     * their own configured font (from preferences); everything else gets the "may"
     * attribute font.  Returns {@code null} for the DN column.
     *
     * @param element the row element
     * @param index   the zero-based column index
     * @return the font to use, or {@code null} to use the default table font
     */
    public Font getFont( Object element, int index )
    {
        if ( element instanceof ISearchResult )
        {
            ISearchResult result = ( ISearchResult ) element;
            String property = null;

            if ( showDn && index == 0 )
            {
                property = BrowserUIConstants.DN;
            }
            else if ( showDn && index > 0 && index - 1 < result.getSearch().getReturningAttributes().length )
            {
                property = result.getSearch().getReturningAttributes()[index - 1];
            }
            else if ( index < result.getSearch().getReturningAttributes().length )
            {
                property = result.getSearch().getReturningAttributes()[index];
            }

            if ( property != null && property == BrowserUIConstants.DN )
            {
                return null;
            }
            else if ( property != null )
            {
                AttributeHierarchy ah = result.getAttributeWithSubtypes( property );
                if ( ah != null )
                {
                    for ( int i = 0; i < ah.getAttributes().length; i++ )
                    {
                        IAttribute attribute = ah.getAttributes()[i];
                        if ( attribute.isObjectClassAttribute() )
                        {
                            FontData[] fontData = PreferenceConverter.getFontDataArray( BrowserCommonActivator
                                .getDefault().getPreferenceStore(), BrowserCommonConstants.PREFERENCE_OBJECTCLASS_FONT );
                            return BrowserCommonActivator.getDefault().getFont( fontData );
                        }
                        else if ( attribute.isMustAttribute() )
                        {
                            FontData[] fontData = PreferenceConverter.getFontDataArray( BrowserCommonActivator
                                .getDefault().getPreferenceStore(),
                                BrowserCommonConstants.PREFERENCE_MUSTATTRIBUTE_FONT );
                            return BrowserCommonActivator.getDefault().getFont( fontData );
                        }
                        else if ( attribute.isOperationalAttribute() )
                        {
                            FontData[] fontData = PreferenceConverter.getFontDataArray( BrowserCommonActivator
                                .getDefault().getPreferenceStore(),
                                BrowserCommonConstants.PREFERENCE_OPERATIONALATTRIBUTE_FONT );
                            return BrowserCommonActivator.getDefault().getFont( fontData );
                        }
                        else
                        {
                            FontData[] fontData = PreferenceConverter
                                .getFontDataArray( BrowserCommonActivator.getDefault().getPreferenceStore(),
                                    BrowserCommonConstants.PREFERENCE_MAYATTRIBUTE_FONT );
                            return BrowserCommonActivator.getDefault().getFont( fontData );
                        }
                    }
                }
            }
        }

        return null;
    }


    // ── Luke Sees No Tinted Glass ────────────────────────────────────────────
    // The sunset is in natural colors — no per-cell foreground tinting.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — we use the default foreground color for all cells.
     *
     * @param element the row element
     * @param index   the column index
     * @return {@code null}
     */
    public Color getForeground( Object element, int index )
    {
        return null;
    }


    // ── Luke Sees No Highlighted Cells ───────────────────────────────────────
    // The horizon is uniform — no per-cell background color highlighting.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — we use the default background color for all cells.
     *
     * @param element the row element
     * @param index   the column index
     * @return {@code null}
     */
    public Color getBackground( Object element, int index )
    {
        return null;
    }

}
