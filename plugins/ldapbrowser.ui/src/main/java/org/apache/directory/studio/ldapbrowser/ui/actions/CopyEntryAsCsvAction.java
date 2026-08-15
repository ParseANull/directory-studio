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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.AttributeComparator;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: CopyEntryAsCsvAction — YODA LIFTS THE X-WING INTO SPREADSHEET FORM
// Yoda raises Luke's X-wing from the Dagobah swamp and reshapes it into a
// neat, tabular form that any Rebel navigator can read — rows and columns,
// clean and flat. That's exactly what we do here: take LDAP entries (which are
// hierarchical and attribute-rich) and flatten them into CSV rows so they can
// be opened in a spreadsheet or imported into another tool.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies one or more LDAP entries to the clipboard as comma-separated values
 * (CSV), with configurable delimiters and encoding pulled from preferences.
 * Extends {@link CopyEntryAsAction} and implements the {@code serialializeEntries}
 * method to produce CSV output; also adds {@code MODE_TABLE} for copying the
 * entire search-result table as currently displayed.
 * Think of this as Yoda lifting the X-wing into a flat, tabular configuration
 * — the same data, just reshaped for a different consumer.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyEntryAsCsvAction extends CopyEntryAsAction
{
    /**
     * Table Mode.
     */
    public static final int MODE_TABLE = 5;


    // ── Yoda Selects the CSV Configuration ───────────────────────────────────
    // Yoda decides on the exact shape the X-wing will take before he lifts it —
    // DN-only? User attributes? The full table? He sets the parameters and lets
    // the parent class handle the rest.
    // We pass the "CSV" type label and the mode to the parent constructor,
    // which sets up the menu label appendix accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopyEntryAsCsvAction} configured for the given mode.
     * Passes the "CSV" format label to the parent so menu items read like
     * "Copy Entry as CSV (User Attributes)".
     *
     * @param mode  one of the {@code MODE_*} constants from {@link CopyEntryAsAction}
     *              or {@link #MODE_TABLE}; controls which attributes are included
     */
    public CopyEntryAsCsvAction( int mode )
    {
        super( Messages.getString( "CopyEntryAsCsvAction.CSV" ), mode ); //$NON-NLS-1$
    }


    // ── Yoda Selects the Right Symbol for the Transformation ─────────────────
    // Yoda knows which kind of lift this is — there's a different sigil for a
    // full table copy versus a DN-only copy versus an operational-attributes copy.
    // We return the correct icon based on the configured mode.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the icon representing this CSV copy mode.
     * Each mode has a distinct icon so the user can tell them apart in menus.
     *
     * @return  the appropriate {@link ImageDescriptor} for this mode; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        if ( this.mode == MODE_DN_ONLY )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_CSV );
        }
        else if ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_CSV_SEARCHRESULT );
        }
        else if ( this.mode == MODE_INCLUDE_OPERATIONAL_ATTRIBUTES )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_CSV_OPERATIONAL );
        }
        else if ( this.mode == MODE_NORMAL )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_CSV_USER );
        }
        else if ( this.mode == MODE_TABLE )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_TABLE );
        }
        else
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_CSV );
        }
    }


    // ── Yoda Names the Lift for the Table Case ────────────────────────────────
    // For the special table-copy mode, Yoda uses a unique incantation —
    // "Copy Table" — rather than the usual "Copy Entry as CSV" label.
    // All other modes fall back to the parent's context-sensitive label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns "Copy Table" when in {@code MODE_TABLE}, delegating to the parent
     * for all other modes.
     * The table mode copies the entire search-result editor content as a CSV
     * snapshot, which is conceptually different from copying individual entries.
     *
     * @return  the localised display name for this action
     */
    public String getText()
    {
        if ( this.mode == MODE_TABLE )
        {
            return Messages.getString( "CopyEntryAsCsvAction.CopyTable" ); //$NON-NLS-1$
        }

        return super.getText();
    }


    // ── Yoda Checks Whether the Table Is Ready to Lift ───────────────────────
    // In table mode, Yoda can only act if the search has results to show —
    // no results, no lift. For all other modes, the parent's logic applies.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the action can execute.
     * For {@code MODE_TABLE}, requires that the current input is a search with
     * at least one result; all other modes delegate to the parent.
     *
     * @return  {@code true} if this action is applicable in the current context
     */
    public boolean isEnabled()
    {
        if ( this.mode == MODE_TABLE )
        {
            return getInput() instanceof ISearch
                && ( ( ISearch ) getInput() ).getSearchResults() != null
                && ( ( ISearch ) getInput() ).getSearchResults().length > 0;
        }

        return super.isEnabled();
    }


    // ── Yoda Lifts the Table Into CSV Form ────────────────────────────────────
    // In table mode, Yoda reaches directly into the search input and lifts every
    // visible result row; for all other modes, he delegates the heavy lifting to
    // the parent's {@code run()} — which handles bookmarks, uninitialized entries,
    // and the general serialisation pipeline.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the copy operation.
     * For {@code MODE_TABLE}, bypasses the parent and directly serializes all
     * entries from the search input so the output mirrors the table view exactly.
     * All other modes delegate to {@link CopyEntryAsAction#run()}.
     */
    public void run()
    {

        if ( this.mode == MODE_TABLE )
        {
            if ( getInput() instanceof ISearch
                && ( ( ISearch ) getInput() ).getSearchResults() != null
                && ( ( ISearch ) getInput() ).getSearchResults().length > 0 )
            {
                List<IEntry> entryList = new ArrayList<IEntry>();
                ISearchResult[] results = ( ( ISearch ) getInput() ).getSearchResults();
                for ( int k = 0; k < results.length; k++ )
                {
                    entryList.add( results[k].getEntry() );
                }
                IEntry[] entries = ( IEntry[] ) entryList.toArray( new IEntry[entryList.size()] );

                StringBuffer text = new StringBuffer();
                serialializeEntries( entries, text );
                copyToClipboard( text.toString() );
            }
        }
        else
        {
            super.run();
        }

    }


    // ── Yoda Shapes the Entries Into CSV Rows ────────────────────────────────
    // The X-wing is airborne; now Yoda reshapes its form into something flat and
    // tabular — a header row of column names, then one row per entry, each cell
    // quoted and delimited according to the user's preferences.
    // This is the core CSV serialization: pull preferences, build the header,
    // then iterate entries and attributes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given entries into CSV format, appending the result to
     * {@code text}.
     * Format parameters (delimiter, quote character, line separator, binary
     * encoding) are read from preferences. The column set is determined by
     * the mode — DN-only, returning-attributes, table, or all user/operational
     * attributes discovered across all entries.
     *
     * @param entries  the entries to serialize; may be empty but not null
     * @param text     the buffer to append CSV output to; must not be null
     */
    public void serialializeEntries( IEntry[] entries, StringBuffer text )
    {

        String attributeDelimiter = BrowserCommonActivator.getDefault().getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_ATTRIBUTEDELIMITER );
        String valueDelimiter = BrowserCommonActivator.getDefault().getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_VALUEDELIMITER );
        String quoteCharacter = BrowserCommonActivator.getDefault().getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_QUOTECHARACTER );
        String lineSeparator = BrowserCommonActivator.getDefault().getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_LINESEPARATOR );
        int binaryEncoding = BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_BINARYENCODING );

        String[] returningAttributes = null;
        if ( this.mode == MODE_DN_ONLY )
        {
            returningAttributes = new String[0];
        }
        else if ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY && getSelectedSearchResults().length > 0
            && getSelectedEntries().length + getSelectedBookmarks().length + getSelectedSearches().length == 0 )
        {
            returningAttributes = getSelectedSearchResults()[0].getSearch().getReturningAttributes();
        }
        else if ( ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY || this.mode == MODE_TABLE )
            && getSelectedSearches().length == 1 )
        {
            returningAttributes = getSelectedSearches()[0].getReturningAttributes();
        }
        else if ( ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY || this.mode == MODE_TABLE )
            && ( getInput() instanceof ISearch ) )
        {
            returningAttributes = ( ( ISearch ) ( getInput() ) ).getReturningAttributes();
        }
        else
        {
            Map<String, IAttribute> attributeMap = new HashMap<String, IAttribute>();
            for ( int e = 0; entries != null && e < entries.length; e++ )
            {
                IAttribute[] attributes = entries[e].getAttributes();
                for ( int a = 0; attributes != null && a < attributes.length; a++ )
                {

                    if ( attributes[a].isOperationalAttribute() && this.mode != MODE_INCLUDE_OPERATIONAL_ATTRIBUTES )
                        continue;

                    if ( !attributeMap.containsKey( attributes[a].getDescription() ) )
                    {
                        attributeMap.put( attributes[a].getDescription(), attributes[a] );
                    }
                }
            }
            IAttribute[] attributes = ( IAttribute[] ) attributeMap.values().toArray(
                new IAttribute[attributeMap.size()] );

            if ( attributes.length > 0 )
            {
                AttributeComparator comparator = new AttributeComparator();
                Arrays.sort( attributes, comparator );
            }

            returningAttributes = new String[attributes.length];
            for ( int i = 0; i < attributes.length; i++ )
            {
                returningAttributes[i] = attributes[i].getDescription();
            }
        }

        // header
        if ( this.mode != MODE_TABLE
            || BrowserUIPlugin.getDefault().getPreferenceStore().getBoolean(
                BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN ) )
        {
            text.append( quoteCharacter );
            text.append( "Dn" ); //$NON-NLS-1$
            text.append( quoteCharacter );
            text.append( attributeDelimiter );
        }
        for ( int a = 0; returningAttributes != null && a < returningAttributes.length; a++ )
        {
            text.append( quoteCharacter );
            text.append( returningAttributes[a] );
            text.append( quoteCharacter );
            if ( a + 1 < returningAttributes.length )
            {
                text.append( attributeDelimiter );
            }
        }
        text.append( lineSeparator );

        for ( int e = 0; entries != null && e < entries.length; e++ )
        {

            if ( this.mode != MODE_TABLE
                || BrowserUIPlugin.getDefault().getPreferenceStore().getBoolean(
                    BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN ) )
            {
                text.append( quoteCharacter );
                text.append( entries[e].getDn().getName() );
                text.append( quoteCharacter );
                text.append( attributeDelimiter );

            }
            for ( int a = 0; returningAttributes != null && a < returningAttributes.length; a++ )
            {

                AttributeComparator comparator = new AttributeComparator();
                AttributeHierarchy ah = entries[e].getAttributeWithSubtypes( returningAttributes[a] );
                if ( ah != null )
                {

                    StringBuffer valueSB = new StringBuffer();

                    for ( Iterator it = ah.iterator(); it.hasNext(); )
                    {
                        IAttribute attribute = ( IAttribute ) it.next();
                        if ( attribute != null )
                        {

                            IValue[] values = attribute.getValues();
                            Arrays.sort( values, comparator );

                            for ( int v = 0; v < values.length; v++ )
                            {
                                String val = ModelConverter.getStringValue( values[v], binaryEncoding );
                                valueSB.append( val );
                                if ( v + 1 < values.length )
                                {
                                    valueSB.append( valueDelimiter );
                                }
                            }
                        }

                        if ( it.hasNext() )
                        {
                            valueSB.append( valueDelimiter );
                        }
                    }

                    String value = valueSB.toString().replaceAll( quoteCharacter, quoteCharacter + quoteCharacter );
                    text.append( quoteCharacter );
                    text.append( value );
                    text.append( quoteCharacter );

                }

                // IAttribute attribute =
                // entries[e].getAttribute(returningAttributes[a]);
                // if (attribute != null) {
                //
                // IValue[] values = attribute.getValues();
                // Arrays.sort(values, comparator);
                //
                // StringBuffer valueSB = new StringBuffer();
                // for (int v = 0; v < values.length; v++) {
                // String val = LdifUtils.getStringValue(values[v],
                // binaryEncoding);
                // valueSB.append(val);
                // if (v + 1 < values.length) {
                // valueSB.append(valueDelimiter);
                // ;
                // }
                // }
                //
                // String value = valueSB.toString().replaceAll(quoteCharacter,
                // quoteCharacter + quoteCharacter);
                // text.append(quoteCharacter);
                // text.append(value);
                // text.append(quoteCharacter);
                //
                // }

                if ( a + 1 < returningAttributes.length )
                {
                    text.append( attributeDelimiter );
                }
            }

            if ( e < entries.length )
            {
                text.append( lineSeparator );
            }
        }
    }
}
