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


import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;


// ── CLASS: CopyAttributeDescriptionAction — C-3PO READS THE ATTRIBUTE NAMES ─
// C-3PO is fluent in over six million forms of communication — given a raw
// binary signal from R2, he translates it into plain protocol text that
// organics can read. Here, we take raw LDAP attribute descriptions (things like
// "cn", "mail", "objectClass") and translate them into plain clipboard text
// that the user can paste anywhere.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies the attribute description (i.e., the attribute type name, such as
 * {@code cn} or {@code objectClass}) of all selected attributes and values
 * to the system clipboard as plain text.
 * Useful when you want to reference the exact attribute name in a search filter,
 * an LDIF snippet, or some other tool without typing it by hand.
 * Think of this as C-3PO reading the Jawa dialect aloud so everyone else
 * knows what attribute they're dealing with.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyAttributeDescriptionAction extends BrowserAction
{

    // ── C-3PO Powers Up, Ready to Translate ──────────────────────────────────
    // C-3PO boots up on the Tantive IV — all protocol circuits initialised, no
    // configuration needed beyond what he was built with.
    // We construct with zero arguments; everything we need comes from the
    // selection context at runtime.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopyAttributeDescriptionAction} with default state.
     * No parameters are needed at construction time — the set of attribute names
     * to copy is derived from the current selection when the action fires.
     */
    public CopyAttributeDescriptionAction()
    {
    }


    // ── C-3PO Reads the Attribute Names Into the Commlink ────────────────────
    // R2 beeps a stream of attribute data; C-3PO translates each one into plain
    // Basic and broadcasts them line by line over the comm channel.
    // We collect all attribute description strings, join them with line
    // separators, and push the result to the clipboard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Collects the description strings of all selected attributes and values,
     * then writes them to the system clipboard as a newline-separated plain-text
     * string.
     * Duplicate descriptions are silently deduplicated (we use a
     * {@link LinkedHashSet} internally) and insertion order is preserved.
     */
    public void run()
    {
        StringBuffer text = new StringBuffer();
        boolean isFirst = true;

        for ( String attributeName : getAttributeNameSet() )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                text.append( BrowserCoreConstants.LINE_SEPARATOR );
            }

            text.append( attributeName );
        }

        if ( text.length() > 0 )
        {
            CopyAction.copyToClipboard( new Object[]
                { text.toString() }, new Transfer[]
                { TextTransfer.getInstance() } );
        }
    }


    // ── C-3PO Gathers All the Dialects He Needs to Translate ─────────────────
    // Before C-3PO can speak, he needs to know every language in the room —
    // he scans attribute hierarchies, attributes, and values to build his list.
    // We walk the entire selection and collect the unique attribute descriptions,
    // preserving encounter order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the set of distinct attribute description strings from
     * the current selection, covering attribute hierarchies, attributes, and values.
     * The set preserves insertion order so the clipboard text is deterministic.
     *
     * @return  a non-null, possibly empty ordered set of attribute description strings
     */
    private Set<String> getAttributeNameSet()
    {
        Set<String> attributeNameSet = new LinkedHashSet<String>();

        for ( AttributeHierarchy attributeHierarchy : getSelectedAttributeHierarchies() )
        {
            for ( IAttribute attribute : attributeHierarchy )
            {
                attributeNameSet.add( attribute.getDescription() );
            }
        }

        for ( IAttribute attribute : getSelectedAttributes() )
        {
            attributeNameSet.add( attribute.getDescription() );
        }

        for ( IValue value : getSelectedValues() )
        {
            attributeNameSet.add( value.getAttribute().getDescription() );
        }

        return attributeNameSet;
    }


    // ── C-3PO Announces What He's About to Translate ─────────────────────────
    // "I am fluent in over six million forms of communication" — but first,
    // C-3PO announces whether he'll be translating one description or many.
    // We return the right singular/plural label for the menu item.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action, using the singular form when exactly
     * one attribute description is selected and the plural form otherwise.
     *
     * @return  the localised display name for this action
     */
    public String getText()
    {
        if ( getAttributeNameSet().size() > 1 )
        {
            return Messages.getString( "CopyAttributeDescriptionAction.CopyAttributeDescriptions" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "CopyAttributeDescriptionAction.CopyAttributeDescription" ); //$NON-NLS-1$
        }
    }


    // ── C-3PO Selects His Protocol Badge ─────────────────────────────────────
    // C-3PO reaches for his communication-corps insignia — the icon that tells
    // everyone in the room which kind of translation he's offering.
    // We return the image descriptor for the "copy attribute" toolbar icon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the "copy attribute description" icon
     * shown in menus and toolbars.
     *
     * @return  the icon's {@link ImageDescriptor}; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_ATT );
    }


    // ── C-3PO Checks His Command Registry ────────────────────────────────────
    // C-3PO consults his protocol database for a registered command ID —
    // finding none, he reports back that this action has no bound keyboard shortcut.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action does not have a registered
     * Eclipse command ID and therefore no keyboard shortcut binding.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── C-3PO Confirms There's Something Worth Translating ───────────────────
    // C-3PO won't open his mouth if no one's said anything — he first checks that
    // at least one attribute description is in scope before declaring himself ready.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if at least one attribute description is available
     * in the current selection, meaning the action can actually do something.
     * Eclipse uses this to enable or grey out the menu item.
     *
     * @return  {@code true} if there is at least one attribute description to copy
     */
    public boolean isEnabled()
    {
        return getAttributeNameSet().size() > 0;
    }
}
