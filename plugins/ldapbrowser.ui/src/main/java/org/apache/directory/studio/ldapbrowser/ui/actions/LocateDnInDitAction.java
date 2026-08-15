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


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: LocateDnInDitAction — R2-D2 READS THE DN FROM AN ATTRIBUTE VALUE ─
// R2-D2 is inside the entry editor, scanning attribute values. He spots a value
// that looks like a DN — say, a "seeAlso" attribute pointing to
// "ou=Droids,dc=rebels,dc=org". R2 reads it, verifies it's a valid address,
// and immediately navigates the browser to that entry. This action does exactly
// that: it reads the selected attribute value, checks if it's a valid DN, and
// if so, tells the parent class to scroll the browser to that entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Locates and opens the LDAP entry whose DN appears as the selected attribute
 * value in the entry editor or search result editor.
 * For example: if you select a "seeAlso" attribute whose value is
 * "ou=Droids,dc=rebels,dc=org", this action navigates the browser directly to
 * that entry. It also works when a search-result row is selected (navigates
 * to the row's own DN).
 * Think of R2-D2 reading a DN reference embedded inside an attribute and
 * immediately querying the directory for that address.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LocateDnInDitAction extends LocateInDitAction
{
    // ── R2 Boots His Locate-DN-in-Value Mode ─────────────────────────────────
    // R2 initialises his DN-reading subroutine — nothing to configure at
    // construction time, he reads the target from the live selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code LocateDnInDitAction} with default state.
     * The DN to navigate to is read from the current attribute value selection
     * when the action fires.
     */
    public LocateDnInDitAction()
    {
    }


    // ── R2 Announces His Function ────────────────────────────────────────────
    // "Locate DN" — R2 beeps the label for this navigation command so the user
    // knows exactly what will happen when they click it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name "Locate DN" for this action.
     *
     * @return  the menu label; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "LocateDnInDitAction.LocateDN" ); //$NON-NLS-1$
    }


    // ── R2 Selects the Locate-DN Indicator ───────────────────────────────────
    // R2 lights up the locate-DN indicator on his panel — the correct icon
    // for this type of navigation action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the "locate DN in DIT" icon.
     *
     * @return  the {@link ImageDescriptor} for the icon; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LOCATE_DN_IN_DIT );
    }


    // ── R2 Reads and Validates the DN From the Selected Value ─────────────────
    // R2 scans the selected attribute value: is it a string? Is it a valid DN
    // format? If so, he packages it with the entry's connection and hands it
    // back to {@link LocateInDitAction#run()} to do the actual navigation.
    // He also handles the fallback case: a selected search result with no
    // attribute selected — in that case, the search result's own DN is used.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link ConnectionAndDn} bundle if the selected attribute value
     * or search result contains a valid LDAP DN to navigate to.
     * Checks three cases in order:
     * (1) A single attribute hierarchy with one value that is a valid DN string,
     *     combined with a single search result (entry editor context).
     * (2) A single selected value that is a valid DN string.
     * (3) A single selected search result with no attribute selected
     *     (the result's own DN is used).
     * Returns {@code null} if none of the above produce a valid DN.
     *
     * @return  the connection and DN to navigate to, or {@code null} if not applicable
     */
    protected ConnectionAndDn getConnectionAndDn()
    {

        if ( getSelectedAttributeHierarchies().length == 1
            && getSelectedAttributeHierarchies()[0].getAttribute().getValueSize() == 1
            && getSelectedSearchResults().length == 1 )
        {
            try
            {
                IValue value = getSelectedAttributeHierarchies()[0].getAttribute().getValues()[0];
                if ( value.isString() && Dn.isValid( value.getStringValue() ) )
                {
                    return new ConnectionAndDn( value.getAttribute().getEntry().getBrowserConnection(), new Dn(
                        value.getStringValue() ) );
                }
            }
            catch ( LdapInvalidDnException e )
            {
                // no valid Dn
            }
        }

        if ( getSelectedValues().length == 1 && getSelectedAttributes().length == 0 )
        {
            try
            {
                IValue value = getSelectedValues()[0];
                if ( value.isString() && Dn.isValid( value.getStringValue() ) )
                {
                    return new ConnectionAndDn( value.getAttribute().getEntry().getBrowserConnection(), new Dn(
                        value.getStringValue() ) );
                }
            }
            catch ( LdapInvalidDnException e )
            {
                // no valid Dn
            }
        }

        if ( getSelectedSearchResults().length == 1 && getSelectedAttributeHierarchies().length == 0 )
        {
            ISearchResult result = getSelectedSearchResults()[0];
            return new ConnectionAndDn( result.getEntry().getBrowserConnection(), result.getEntry().getDn() );
        }

        return null;
    }
}
