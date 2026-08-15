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

package org.apache.directory.studio.ldapbrowser.ui.views.browser;


import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;


// ── CLASS: BrowserViewLabelDecorator — C-3PO TRANSLATING FOR R2-D2 ───────────
// C-3PO stands next to R2-D2 and adds context: "He says he's found the plans"
// — annotating R2's beeps with a human-readable overlay. R2 has the raw data;
// C-3PO wraps it with meaning. This decorator does the same thing to tree
// entries: the base icon says what the entry is, and we paste on small
// overlay badges (search-result star, filter funnel, referral arrow) to
// communicate additional state without changing the underlying model.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Applies lightweight overlay icons to entries in the LDAP browser tree.
 * JFace label decorators add small badge images on top of existing icons
 * without replacing them — perfect for communicating secondary state like
 * "this entry is a search result" or "its children are filtered."
 * Think of C-3PO layering a translation on top of R2's signal: the raw
 * data is still there, we're just adding a human-readable badge.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserViewLabelDecorator extends LabelProvider implements ILightweightLabelDecorator
{

    // ── C-3PO Reads the Signal and Adds the Annotation ──────────────────────────
    // C-3PO listens to R2's beeps, figures out what they mean, and adds the
    // right verbal annotation for the humans in the room — "search result
    // badge," "filtered badge," "referral badge."
    // We inspect each tree element and paste the appropriate overlay image
    // onto it so the user can see at a glance what kind of thing it is.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds overlay icon badges to a browser tree element based on its state.
     * Search results get a small star in the bottom-right corner; filtered entries
     * get a funnel badge; continuation references (LDAP referrals) get an arrow
     * overlay in the top-left.
     * Note: this method is called by the JFace decorator framework — we don't
     * call it ourselves. It must be fast because it runs for every visible node.
     *
     * @param element    the tree node to decorate — could be an IEntry, ISearchResult, or anything else.
     * @param decoration the mutable decoration object where we add our overlay images.
     */
    @Override
    public void decorate( Object element, IDecoration decoration )
    {
        IEntry entry = null;
        if ( element instanceof ISearchResult )
        {
            entry = ( ( ISearchResult ) element ).getEntry();
            decoration.addOverlay( BrowserUIPlugin.getDefault().getImageDescriptor(
                BrowserUIConstants.IMG_OVR_SEARCHRESULT ), IDecoration.BOTTOM_RIGHT );
        }
        else if ( element instanceof IEntry )
        {
            entry = ( IEntry ) element;
            if ( entry.getChildrenFilter() != null )
            {
                decoration.addOverlay( BrowserUIPlugin.getDefault().getImageDescriptor(
                    BrowserUIConstants.IMG_OVR_FILTERED ), IDecoration.BOTTOM_RIGHT );
            }
        }

        if ( entry instanceof IContinuation || element instanceof IContinuation )
        {
            decoration.addOverlay( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_OVR_REF ),
                IDecoration.TOP_LEFT );
        }
    }

}
