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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.AttributeComparator;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: CopyEntryAsLdifAction — YODA LIFTS THE X-WING INTO LDIF FORM ─────
// Yoda pulls the X-wing from the Dagobah swamp and reshapes it into the
// canonical textual form that every LDAP tool understands — LDIF (LDAP Data
// Interchange Format). LDIF is the lingua franca of directory data: one
// "dn:" line, then one "attributeName: value" line per attribute value.
// This action does exactly that transformation: from in-memory LDAP entry
// objects to a clipboard-ready LDIF text block.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies one or more LDAP entries to the clipboard as LDIF-formatted text.
 * LDIF (LDAP Data Interchange Format) is the standard human-readable format
 * for LDAP entries — most LDAP command-line tools, import wizards, and config
 * files accept it, so this action is great for grabbing entries to paste
 * elsewhere.
 * Extends {@link CopyEntryAsAction} and implements {@code serialializeEntries}
 * to produce LDIF output. Think of this as Yoda lifting the X-wing into
 * the one shape that every Rebel technician knows how to read.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyEntryAsLdifAction extends CopyEntryAsAction
{

    // ── Yoda Configures the LDIF Lift ─────────────────────────────────────────
    // Yoda sets his intention — "LDIF, this shall become" — and passes the
    // format label and mode up to the parent constructor, which sets the right
    // menu appendix (DN only, user attributes, operational attributes, etc.).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopyEntryAsLdifAction} configured for the given mode.
     * Passes the "LDIF" format label to the parent so menu items read like
     * "Copy Entry as LDIF (User Attributes)".
     *
     * @param mode  one of the {@code MODE_*} constants from {@link CopyEntryAsAction};
     *              controls which attributes are included in the LDIF output
     */
    public CopyEntryAsLdifAction( int mode )
    {
        super( Messages.getString( "CopyEntryAsLdifAction.LDIF" ), mode ); //$NON-NLS-1$
    }


    // ── Yoda Selects the Right LDIF Icon ──────────────────────────────────────
    // Each mode of the lift gets a distinctive sigil — Yoda knows at a glance
    // whether this is a DN-only lift, a search-result lift, or an
    // operational-attributes lift, and selects the matching symbol.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the icon representing this LDIF copy mode.
     * DN-only, returning-attributes, operational-attributes, and normal modes
     * each have a distinct icon to tell them apart in menus.
     *
     * @return  the appropriate {@link ImageDescriptor} for this mode; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        if ( this.mode == MODE_DN_ONLY )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_LDIF );
        }
        else if ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_LDIF_SEARCHRESULT );
        }
        else if ( this.mode == MODE_INCLUDE_OPERATIONAL_ATTRIBUTES )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_LDIF_OPERATIONAL );
        }
        else
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_LDIF_USER );
        }
    }


    // ── Yoda Shapes Each Entry Into LDIF Block Form ───────────────────────────
    // Yoda lifts each entry out of the swamp and reshapes it methodically:
    // first the DN line, then each attribute value on its own line, separated
    // by blank lines between entries — exactly the LDIF spec.
    // We respect the returning-attributes filter, skip operational attributes
    // unless the mode requests them, and sort values for deterministic output.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given entries into LDIF format, appending the result to
     * {@code text}.
     * Each entry becomes a block starting with a {@code dn:} line, followed by
     * one line per attribute value, separated from the next entry by a blank line
     * (the LDIF record separator). The line separator character is read from
     * preferences.
     *
     * @param entries  the entries to serialize; may be empty but not null
     * @param text     the buffer to append LDIF output to; must not be null
     */
    public void serialializeEntries( IEntry[] entries, StringBuffer text )
    {
        String lineSeparator = BrowserCorePlugin.getDefault().getPluginPreferences().getString(
            BrowserCoreConstants.PREFERENCE_LDIF_LINE_SEPARATOR );

        Set<String> returningAttributesSet = null;

        if ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY && getSelectedSearchResults().length > 0
            && getSelectedEntries().length + getSelectedBookmarks().length + getSelectedSearches().length == 0 )
        {
            returningAttributesSet = new HashSet<String>( Arrays.asList( getSelectedSearchResults()[0].getSearch()
                .getReturningAttributes() ) );
        }
        else if ( this.mode == MODE_RETURNING_ATTRIBUTES_ONLY && getSelectedSearches().length == 1 )
        {
            returningAttributesSet = new HashSet<String>( Arrays.asList( getSelectedSearches()[0].getReturningAttributes() ) );
        }

        boolean isFirst = true;

        for ( IEntry entry : entries )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                text.append( lineSeparator );
            }

            serializeDn( entry.getDn(), text );

            if ( this.mode != MODE_DN_ONLY )
            {
                List<IValue> valueList = new ArrayList<IValue>();
                IAttribute[] attributes = entry.getAttributes();

                if ( attributes != null )
                {
                    for ( IAttribute attribute : attributes )
                    {
                        if ( ( returningAttributesSet != null ) && !returningAttributesSet.contains( attribute.getType() ) )
                        {
                            continue;
                        }

                        if ((  attribute.isOperationalAttribute() ) && ( this.mode != MODE_INCLUDE_OPERATIONAL_ATTRIBUTES ) )
                        {
                            continue;
                        }

                        for ( IValue value : attribute.getValues() )
                        {
                            valueList.add( value );
                        }
                    }
                }

                IValue[] values = ( IValue[] ) valueList.toArray( new IValue[valueList.size()] );

                AttributeComparator comparator = new AttributeComparator();
                Arrays.sort( values, comparator );

                for ( IValue value : values )
                {
                    serializeValue( value, text );
                }
            }
        }
    }


    // ── Yoda Encodes One Attribute Value ─────────────────────────────────────
    // Each attribute value is a single particle of the X-wing — Yoda lifts it
    // carefully and sets it down as one LDIF attribute-value line.
    // We delegate to {@link ModelConverter} which handles binary encoding and
    // line folding per the LDIF spec.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serializes a single attribute value as an LDIF {@code attribute: value}
     * line and appends it to {@code text}.
     * Binary values are base64-encoded (using {@code attribute:: value} syntax)
     * and long lines are folded according to the current LDIF format preferences.
     *
     * @param value  the attribute value to serialize; must not be null
     * @param text   the buffer to append the serialized line to; must not be null
     */
    protected void serializeValue( IValue value, StringBuffer text )
    {
        text
            .append( ModelConverter.valueToLdifAttrValLine( value ).toFormattedString( Utils.getLdifFormatParameters() ) );
    }


    // ── Yoda Encodes the DN Line First ───────────────────────────────────────
    // Every LDIF block starts with the DN — it's the anchor point, the first
    // thing Yoda lowers into place before anything else.
    // We format the DN as an LDIF {@code dn:} line respecting line-folding rules.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serializes a {@link Dn} as the opening {@code dn:} line of an LDIF record
     * and appends it to {@code text}.
     * The DN is formatted according to the current LDIF format preferences
     * (e.g., line-folding width).
     *
     * @param dn    the distinguished name to serialize; must not be null
     * @param text  the buffer to append the {@code dn:} line to; must not be null
     */
    protected void serializeDn( Dn dn, StringBuffer text )
    {
        text.append( ModelConverter.dnToLdifDnLine( dn ).toFormattedString( Utils.getLdifFormatParameters() ) );
    }
}
