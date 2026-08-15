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


import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.LdapFilterUtils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;


// ── CLASS: CopySearchFilterAction — R2-D2 QUERIES THE DEATH STAR TERMINAL ───
// In the Death Star docking bay, R2-D2 rolls up to an Imperial terminal, plugs
// in, and extracts exactly the data he needs — not everything, just the right
// query string that will unlock the next step. Here, we inspect the selected
// attribute values, build an LDAP search filter expression (like
// "(cn=Luke Skywalker)" or "(&(objectClass=person)(mail=luke@rebels.org))"),
// and copy it to the clipboard so the user can paste it anywhere.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies an LDAP search filter derived from the currently selected attribute
 * values to the system clipboard as plain text.
 * The filter type (equals, NOT, AND, OR) is set at construction time via a
 * mode constant. For example, selecting a {@code cn} attribute with value
 * "Luke" in AND mode produces {@code (&(cn=Luke))}.
 * Think of this as R2-D2 formulating and extracting the exact query he needs
 * from the Death Star's computer — precise, purposeful, and immediately usable.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopySearchFilterAction extends BrowserAction
{

    /**
     * Equals Mode.
     */
    public static final int MODE_EQUALS = 0;

    /**
     * Not Mode.
     */
    public static final int MODE_NOT = 1;

    /**
     * And Mode.
     */
    public static final int MODE_AND = 2;

    /**
     * Or Mode.
     */
    public static final int MODE_OR = 3;

    private int mode;


    // ── R2 Configures His Query Terminal Mode ────────────────────────────────
    // R2 plugs into the terminal and selects which kind of query he'll be
    // running: a simple equality check, a negation, an AND, or an OR.
    // We store the mode and use it at runtime to shape the filter expression.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopySearchFilterAction} for the given filter type mode.
     * Each instance is permanently bound to one filter operator; create separate
     * instances for each operator you want to offer.
     *
     * @param mode  one of {@link #MODE_EQUALS}, {@link #MODE_NOT},
     *              {@link #MODE_AND}, or {@link #MODE_OR}
     */
    public CopySearchFilterAction( int mode )
    {
        this.mode = mode;
    }


    // ── R2 Announces Which Terminal He's Using ───────────────────────────────
    // R2 beeps the label of the query type he's about to run — "Copy Search
    // Filter" for equals, "Copy NOT Filter" for negation, and so on.
    // The label appears in menus so the user can pick the right operator.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised menu label describing the filter operator this
     * instance produces — one of "Copy Search Filter", "Copy NOT Search Filter",
     * "Copy AND Search Filter", or "Copy OR Search Filter".
     *
     * @return  the display name for this action; never {@code null}
     */
    public String getText()
    {
        if ( mode == MODE_EQUALS )
        {
            return Messages.getString( "CopySearchFilterAction.CopySearchFilter" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_NOT )
        {
            return Messages.getString( "CopySearchFilterAction.CopyNotSearchFilter" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_AND )
        {
            return Messages.getString( "CopySearchFilterAction.CopyAndSearchFilter" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_OR )
        {
            return Messages.getString( "CopySearchFilterAction.CopyOrSearchFilter" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "CopySearchFilterAction.CopySearchFilter" ); //$NON-NLS-1$
        }
    }


    // ── R2 Selects the Right Data Port Icon ──────────────────────────────────
    // R2 knows which port icon corresponds to which query type — equals has its
    // own symbol, NOT has another, AND and OR each have theirs.
    // We return the matching image descriptor so each menu item is visually distinct.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the icon representing this filter operator.
     * Equals, NOT, AND, and OR each have a distinct visual icon in the plugin's
     * image registry.
     *
     * @return  the appropriate {@link ImageDescriptor}; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        if ( mode == MODE_EQUALS )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_FILTER_EQUALS );
        }
        else if ( mode == MODE_NOT )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_FILTER_NOT );
        }
        else if ( mode == MODE_AND )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_FILTER_AND );
        }
        else if ( mode == MODE_OR )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_FILTER_OR );
        }
        else
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_FILTER_EQUALS );
        }
    }


    // ── R2 Checks His Command Port Number ────────────────────────────────────
    // R2 has no registered keyboard shortcut for this particular query type —
    // returning null tells Eclipse not to look for a key binding.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID and therefore no keyboard shortcut.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── R2 Verifies the Terminal Is Active ───────────────────────────────────
    // R2 doesn't plug in unless there's something to query — for equals/NOT he
    // needs exactly one value (to produce an unambiguous filter); for AND/OR he
    // needs at least one value (to wrap in a composite).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the current selection satisfies the requirements
     * of this filter mode.
     * Equals and NOT require exactly one attribute value so the filter is
     * unambiguous. AND and OR just need at least one value to wrap.
     *
     * @return  {@code true} if this action is applicable in the current selection
     */
    public boolean isEnabled()
    {
        if ( mode == MODE_EQUALS || mode == MODE_NOT )
        {
            return getSelectedAttributeHierarchies().length + getSelectedAttributes().length
                + getSelectedValues().length == 1
                && ( getSelectedValues().length == 1
                    || ( getSelectedAttributes().length == 1 && getSelectedAttributes()[0].getValueSize() == 1 ) || ( getSelectedAttributeHierarchies().length == 1
                    && getSelectedAttributeHierarchies()[0].size() == 1 && getSelectedAttributeHierarchies()[0]
                    .getAttribute().getValueSize() == 1 ) );
        }
        else if ( mode == MODE_AND || mode == MODE_OR )
        {
            return getSelectedAttributeHierarchies().length + getSelectedAttributes().length
                + getSelectedValues().length > 0;
        }
        else
        {
            return false;
        }
    }


    // ── R2 Extracts the Filter String and Copies It ───────────────────────────
    // R2 runs his query, gets back the filter string, and transmits it straight
    // to the comm channel (the clipboard) so the mission team can use it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the LDAP search filter string for the current selection and copies
     * it to the system clipboard as plain text.
     * Delegates filter construction to {@link #getFilter(String)} with the
     * appropriate operator token for this mode.
     */
    public void run()
    {

        String filter = null;

        if ( mode == MODE_EQUALS )
        {
            filter = getFilter( null );
        }
        else if ( mode == MODE_NOT )
        {
            filter = getFilter( "!" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_AND )
        {
            filter = getFilter( "&" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_OR )
        {
            filter = getFilter( "|" ); //$NON-NLS-1$
        }

        if ( filter != null && filter.length() > 0 )
        {
            CopyAction.copyToClipboard( new Object[]
                { filter }, new Transfer[]
                { TextTransfer.getInstance() } );
        }

    }


    // ── R2 Builds the Query String From Selected Values ───────────────────────
    // R2 scans each selected attribute and value, generates an LDAP filter
    // assertion for each one using {@link LdapFilterUtils}, then wraps them
    // in the appropriate operator (or returns the single assertion bare for equals).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs an LDAP filter expression from the selected attributes and values.
     * For {@code null} filter type (equals), a single {@code (attr=value)} filter
     * is returned. For {@code "!"}, {@code "&"}, or {@code "|"}, the individual
     * assertions are wrapped in a composite like {@code (&(cn=Luke)(mail=...))}.
     *
     * @param filterType  {@code "!"}, {@code "&"}, {@code "|"}, or {@code null}
     *                    for a bare equals assertion
     * @return  the LDAP filter string; may be empty if the selection yields nothing
     */
    private String getFilter( String filterType )
    {
        Set filterSet = new LinkedHashSet();
        for ( int i = 0; i < getSelectedAttributeHierarchies().length; i++ )
        {
            for ( Iterator it = getSelectedAttributeHierarchies()[i].iterator(); it.hasNext(); )
            {
                IAttribute att = ( IAttribute ) it.next();
                IValue[] values = att.getValues();
                for ( int v = 0; v < values.length; v++ )
                {
                    filterSet.add( LdapFilterUtils.getFilter( values[v] ) );
                }
            }
        }
        for ( int a = 0; a < getSelectedAttributes().length; a++ )
        {
            IValue[] values = getSelectedAttributes()[a].getValues();
            for ( int v = 0; v < values.length; v++ )
            {
                filterSet.add( LdapFilterUtils.getFilter( values[v] ) );
            }
        }
        for ( int v = 0; v < getSelectedValues().length; v++ )
        {
            filterSet.add( LdapFilterUtils.getFilter( getSelectedValues()[v] ) );
        }

        StringBuffer filter = new StringBuffer();
        if ( filterType != null )
        {
            filter.append( "(" ); //$NON-NLS-1$
            filter.append( filterType );
            for ( Iterator filterIterator = filterSet.iterator(); filterIterator.hasNext(); )
            {
                filter.append( filterIterator.next() );
            }
            filter.append( ")" ); //$NON-NLS-1$
        }
        else if ( filterSet.size() == 1 )
        {
            filter.append( filterSet.toArray()[0] );
        }

        return filter.toString();
    }
}
