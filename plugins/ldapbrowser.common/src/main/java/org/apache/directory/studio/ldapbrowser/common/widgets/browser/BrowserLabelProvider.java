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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import java.util.Collection;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation.State;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.BaseDNEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DirectoryMetadataEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.schema.ObjectClassIconPair;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


// ── CLASS: BrowserLabelProvider — C-3PO TRANSLATING FOR THE REBEL CREW ────────
// C-3PO stands in the Rebel briefing room. As each data packet arrives —
// a droid report, a Wookiee cry, a Huttese contract — he translates it into
// clear Basic for the crew: a text label and the right icon so everyone
// instantly recognizes what kind of thing they're looking at.
// BrowserLabelProvider does exactly this for the browser tree: given any
// model object (IEntry, ISearch, IBookmark, BrowserEntryPage, etc.), it
// produces the text and icon for that tree row, respecting user preferences
// for how labels should be displayed (full DN, RDN, or RDN value).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The JFace label provider for the LDAP browser tree widget.
 * For every possible node type in the tree, this class provides the display text
 * and the icon image to show. It also implements {@link IFontProvider} and
 * {@link IColorProvider} — though those currently return null (default styling).
 * Think of this class as C-3PO in the Rebel briefing room: every strange object
 * gets translated into something the human crew can read at a glance.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserLabelProvider extends LabelProvider implements IFontProvider, IColorProvider
{

    /** The preferences. */
    private BrowserPreferences preferences;


    // ── C-3PO RECEIVES HIS TRANSLATION PREFERENCES ───────────────────────────
    // Before C-3PO starts translating in the Rebel briefing room, Leia hands him
    // a settings card: "Show full diplomatic titles, abbreviate when over 20 chars,
    // put the most important details first." He keeps this card throughout the session.
    // We receive the BrowserPreferences and store them — every getText() call
    // will consult them to decide how to format labels.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new label provider configured with the given display preferences.
     * The preferences control things like: show full DN vs. just the RDN, abbreviate
     * long labels, show the children count suffix, etc.
     *
     * <p>For example — Leia hands C-3PO his translation preferences card:</p>
     * <pre>
     *   C3PO.setPreferences(showFullDiplomaticTitle=true, maxLength=30);
     * </pre>
     *
     * @param preferences   the browser display preferences — never null
     */
    public BrowserLabelProvider( BrowserPreferences preferences )
    {
        this.preferences = preferences;
    }


    // ── C-3PO TRANSLATES EACH OBJECT INTO READABLE TEXT ──────────────────────
    // An entry arrives: C-3PO reads its LDAP type, checks the translation settings,
    // and produces the right text. An IEntry might become "cn=Luke Skywalker (3)",
    // a BrowserEntryPage becomes "[1...50]", an ISearch becomes "My Search (42 results)".
    // JFace calls this for every visible row in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for a tree node.
     * We handle every possible node type differently. For entries, we respect the
     * user's label preference (full DN, RDN, or RDN value) and append a children
     * count like "(5)" if children are loaded. For pages, we show the range "[1...50]".
     * For searches, we append the result count. Strings are abbreviated if the user
     * has set an abbreviation limit.
     *
     * <p>For example — C-3PO translates a Rebel entry into readable Basic:</p>
     * <pre>
     *   getText(lukeEntry);        // "Skywalker (3)" — RDN value + child count
     *   getText(entryPage);        // "[51...100]"
     *   getText(mySearch);         // "Jedi Search (7)"
     *   getText(bookmarkNode);     // "Luke's home planet"
     * </pre>
     *
     * @param obj   the tree node object to translate into text
     * @return the display string; empty string for null input
     */
    public String getText( Object obj )
    {
        if ( obj instanceof IEntry )
        {
            IEntry entry = ( IEntry ) obj;

            StringBuffer append = new StringBuffer();

            if ( entry.isChildrenInitialized() && ( entry.getChildrenCount() > 0 ) || entry.getChildrenFilter() != null )
            {
                append.append( " (" ).append( entry.getChildrenCount() ); //$NON-NLS-1$
                if ( entry.hasMoreChildren() )
                {
                    append.append( "+" ); //$NON-NLS-1$
                }
                if ( entry.getChildrenFilter() != null )
                {
                    append.append( ", filtered" ); //$NON-NLS-1$
                }
                append.append( ")" ); //$NON-NLS-1$
            }

            if ( entry instanceof IRootDSE )
            {
                return "Root DSE" + append.toString(); //$NON-NLS-1$
            }
            else if ( entry instanceof IContinuation )
            {
                return entry.getUrl().toString() + append.toString();
            }
            else if ( entry instanceof BaseDNEntry )
            {
                return entry.getDn().getName() + append.toString();
            }
            else if ( entry.hasParententry() )
            {
                String label = ""; //$NON-NLS-1$
                if ( preferences.getEntryLabel() == BrowserCommonConstants.SHOW_DN )
                {
                    label = entry.getDn().getName();
                }
                else if ( preferences.getEntryLabel() == BrowserCommonConstants.SHOW_RDN )
                {
                    label = entry.getRdn().getName();
                }
                else if ( preferences.getEntryLabel() == BrowserCommonConstants.SHOW_RDN_VALUE )
                {
                    label = ( String ) entry.getRdn().getName();
                }

                label += append.toString();

                if ( preferences.isEntryAbbreviate() && label.length() > preferences.getEntryAbbreviateMaxLength() )
                {
                    label = Utils.shorten( label, preferences.getEntryAbbreviateMaxLength() );
                }

                return label;
            }
            else
            {
                return entry.getDn().getName() + append.toString();
            }
        }
        else if ( obj instanceof SearchContinuation )
        {
            SearchContinuation sc = ( SearchContinuation ) obj;
            return sc.getUrl().toString();
        }
        else if ( obj instanceof BrowserEntryPage )
        {
            BrowserEntryPage container = ( BrowserEntryPage ) obj;
            return "[" + ( container.getFirst() + 1 ) + "..." + ( container.getLast() + 1 ) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else if ( obj instanceof BrowserSearchResultPage )
        {
            BrowserSearchResultPage container = ( BrowserSearchResultPage ) obj;
            return "[" + ( container.getFirst() + 1 ) + "..." + ( container.getLast() + 1 ) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else if ( obj instanceof ISearch )
        {
            ISearch search = ( ISearch ) obj;
            ISearchResult[] results = search.getSearchResults();
            SearchContinuation[] scs = search.getSearchContinuations();
            StringBuffer append = new StringBuffer( search.getName() );
            if ( results != null && scs != null )
            {
                append.append( " (" ).append( results.length + scs.length ); //$NON-NLS-1$
                if ( search.isCountLimitExceeded() )
                {
                    append.append( "+" ); //$NON-NLS-1$
                }
                append.append( ")" ); //$NON-NLS-1$
            }
            return append.toString();
        }
        else if ( obj instanceof IBookmark )
        {
            IBookmark bookmark = ( IBookmark ) obj;
            return bookmark.getName();
        }
        else if ( obj instanceof ISearchResult )
        {
            ISearchResult sr = ( ISearchResult ) obj;

            if ( sr.getEntry() instanceof IContinuation )
            {
                return sr.getEntry().getUrl().toString();
            }
            else if ( sr.getEntry().hasParententry() || sr.getEntry() instanceof IRootDSE )
            {
                String label = ""; //$NON-NLS-1$
                if ( sr.getEntry() instanceof IRootDSE )
                {
                    label = "Root DSE"; //$NON-NLS-1$
                }
                else if ( preferences.getSearchResultLabel() == BrowserCommonConstants.SHOW_DN )
                {
                    label = sr.getEntry().getDn().getName();
                }
                else if ( preferences.getSearchResultLabel() == BrowserCommonConstants.SHOW_RDN )
                {
                    label = sr.getEntry().getRdn().getName();
                }
                else if ( preferences.getSearchResultLabel() == BrowserCommonConstants.SHOW_RDN_VALUE )
                {
                    label = ( String ) sr.getEntry().getRdn().getName();
                }

                if ( preferences.isSearchResultAbbreviate()
                    && label.length() > preferences.getSearchResultAbbreviateMaxLength() )
                {
                    label = Utils.shorten( label, preferences.getSearchResultAbbreviateMaxLength() );
                }

                return label;
            }
            else
            {
                return sr.getEntry().getDn().getName();
            }

        }
        else if ( obj instanceof StudioConnectionRunnableWithProgress )
        {
            StudioConnectionRunnableWithProgress runnable = ( StudioConnectionRunnableWithProgress ) obj;
            for ( Object lockedObject : runnable.getLockedObjects() )
            {
                if ( lockedObject instanceof ISearch )
                {
                    ISearch search = ( ISearch ) lockedObject;
                    if ( obj == search.getTopSearchRunnable() )
                    {
                        return Messages.getString( "BrowserLabelProvider.TopPage" ); //$NON-NLS-1$
                    }
                    else if ( obj == search.getNextSearchRunnable() )
                    {
                        return Messages.getString( "BrowserLabelProvider.NextPage" ); //$NON-NLS-1$
                    }
                }
                else if ( lockedObject instanceof IEntry )
                {
                    IEntry entry = ( IEntry ) lockedObject;
                    if ( obj == entry.getTopPageChildrenRunnable() )
                    {
                        return Messages.getString( "BrowserLabelProvider.TopPage" ); //$NON-NLS-1$
                    }
                    else if ( obj == entry.getNextPageChildrenRunnable() )
                    {
                        return Messages.getString( "BrowserLabelProvider.NextPage" ); //$NON-NLS-1$
                    }
                }
            }
            return obj.toString();
        }
        else if ( obj instanceof BrowserCategory )
        {
            BrowserCategory category = ( BrowserCategory ) obj;
            return category.getTitle();
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


    // ── C-3PO HANDS THE CREW THE RIGHT ID BADGE ICON ─────────────────────────
    // C-3PO has a badge printer that produces the correct icon for each visitor:
    // a Jedi badge for Luke, an Imperial crest for Vader, a folder icon for a
    // section directory. For LDAP entries, he picks the icon by checking what
    // objectClasses the entry has — a "person" gets a person icon, a group gets
    // a group icon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image for a tree node.
     * Each node type gets its own icon. For LDAP entries we pick the icon based
     * on the entry's structural objectClass (by weight-scoring configured pairs).
     * The special "Root DSE" gets a root icon, schema entries get a schema icon,
     * page nodes get a folder icon, and so on.
     *
     * <p>For example — C-3PO prints the right ID badge for each Rebel visitor:</p>
     * <pre>
     *   getImage(lukeEntry);        // person icon (structural objectClass = inetOrgPerson)
     *   getImage(rebelGroupEntry);  // group icon (structural objectClass = groupOfNames)
     *   getImage(entryPage);        // folder icon
     *   getImage(bookmarkNode);     // bookmark icon
     * </pre>
     *
     * @param obj   the tree node object that needs an icon
     * @return the SWT {@link Image} to display, or null for unknown types
     */
    public Image getImage( Object obj )
    {
        if ( obj instanceof IEntry )
        {
            IEntry entry = ( IEntry ) obj;
            if ( entry instanceof IRootDSE )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_ENTRY_ROOT );
            }
            else if ( entry instanceof DirectoryMetadataEntry && ( ( DirectoryMetadataEntry ) entry ).isSchemaEntry() )
            {
                return BrowserCommonActivator.getDefault().getImage(
                    BrowserCommonConstants.IMG_BROWSER_SCHEMABROWSEREDITOR );
            }
            else if ( entry.getDn().equals( entry.getBrowserConnection().getSchema().getDn() ) )
            {
                return BrowserCommonActivator.getDefault().getImage(
                    BrowserCommonConstants.IMG_BROWSER_SCHEMABROWSEREDITOR );
            }
            else
            {
                return BrowserLabelProvider.getImageByObjectClass( entry );
            }
        }
        else if ( obj instanceof BrowserEntryPage )
        {
            return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJ_FOLDER );
        }
        else if ( obj instanceof BrowserSearchResultPage )
        {
            return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJ_FOLDER );
        }
        else if ( obj instanceof IQuickSearch )
        {
            return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_QUICKSEARCH );
        }
        else if ( obj instanceof ISearch )
        {
            ISearch search = ( ISearch ) obj;
            if ( search instanceof IContinuation && ( ( IContinuation ) search ).getState() != State.RESOLVED )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_SEARCH_UNPERFORMED );
            }
            else if ( search.getSearchResults() != null )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_SEARCH );
            }
            else
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_SEARCH_UNPERFORMED );
            }
        }
        else if ( obj instanceof IBookmark )
        {
            return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_BOOKMARK );
        }
        else if ( obj instanceof ISearchResult )
        {
            ISearchResult sr = ( ISearchResult ) obj;
            IEntry entry = sr.getEntry();
            return BrowserLabelProvider.getImageByObjectClass( entry );
        }
        else if ( obj instanceof StudioConnectionRunnableWithProgress )
        {
            StudioConnectionRunnableWithProgress runnable = ( StudioConnectionRunnableWithProgress ) obj;
            for ( Object lockedObject : runnable.getLockedObjects() )
            {
                if ( lockedObject instanceof ISearch )
                {
                    ISearch search = ( ISearch ) lockedObject;
                    if ( obj == search.getTopSearchRunnable() )
                    {
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_TOP );
                    }
                    else if ( obj == search.getNextSearchRunnable() )
                    {
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_NEXT );
                    }
                }
                else if ( lockedObject instanceof IEntry )
                {
                    IEntry entry = ( IEntry ) lockedObject;
                    if ( obj == entry.getTopPageChildrenRunnable() )
                    {
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_TOP );
                    }
                    else if ( obj == entry.getNextPageChildrenRunnable() )
                    {
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_NEXT );
                    }
                }
            }
            return null;
        }
        else if ( obj instanceof BrowserCategory )
        {
            BrowserCategory category = ( BrowserCategory ) obj;
            if ( category.getType() == BrowserCategory.TYPE_DIT )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_DIT );
            }
            else if ( category.getType() == BrowserCategory.TYPE_SEARCHES )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_SEARCHES );
            }
            else if ( category.getType() == BrowserCategory.TYPE_BOOKMARKS )
            {
                return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_BOOKMARKS );
            }
            else
            {
                return null;
            }
        }
        else
        {
            // return
            // Activator.getDefault().getImage("icons/sandglass.gif");
            return null;
        }
    }


    // ── C-3PO PICKS THE CORRECT BADGE BY RANK AND AFFILIATION ────────────────
    // C-3PO consults the Rebel insignia guide: if the visitor is a Jedi Master
    // (structural AUXILIARY + weight 3) they get the highest-weight badge. If they're
    // just a Padawan (structural STRUCTURAL + weight 2) they get a different one.
    // We score each objectClass pair against the entry's objectClasses and pick
    // the icon pair with the highest score — this gives "inetOrgPerson" priority
    // over "top" for person entries, for example.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Selects and returns the icon for an LDAP entry based on its objectClass attribute.
     * We compare the entry's objectClass OIDs against a user-configured list of
     * objectClass-to-icon mappings (configured in Directory Studio preferences).
     * Each matching structural objectClass scores 3 points, each auxiliary scores 2.
     * The mapping with the highest total score wins and provides the icon.
     * Falls back to the generic entry icon if no configured mapping matches.
     *
     * <p>For example — C-3PO picks the right Rebel badge by checking rank and affiliation:</p>
     * <pre>
     *   int jediScore = countMatchingOCs(entry, JEDI_OCS);   // 5 — Jedi Master
     *   int rebelScore = countMatchingOCs(entry, REBEL_OCS); // 3 — Alliance member
     *   return jediScore > rebelScore ? JEDI_BADGE : REBEL_BADGE;
     * </pre>
     *
     * @param entry   the LDAP entry whose objectClass should determine the icon
     * @return the best-matching icon image, or the generic entry icon if none match
     */
    public static Image getImageByObjectClass( IEntry entry )
    {
        Schema schema = entry.getBrowserConnection().getSchema();
        Collection<ObjectClass> ocds = entry.getObjectClassDescriptions();
        if ( ocds != null )
        {
            Collection<String> numericOids = SchemaUtils.getNumericOids( ocds );
            ObjectClassIconPair[] objectClassIcons = BrowserCorePlugin.getDefault().getCorePreferences()
                .getObjectClassIcons();
            int maxWeight = 0;
            ObjectClassIconPair maxObjectClassIconPair = null;
            for ( ObjectClassIconPair objectClassIconPair : objectClassIcons )
            {
                int weight = 0;
                String[] ocNumericOids = objectClassIconPair.getOcNumericOids();
                for ( String ocNumericOid : ocNumericOids )
                {
                    if ( numericOids.contains( ocNumericOid ) )
                    {
                        ObjectClass ocd = schema.getObjectClassDescription( ocNumericOid );
                        if ( ocd.getType() == ObjectClassTypeEnum.STRUCTURAL )
                        {
                            weight += 3;
                        }
                        else if ( ocd.getType() == ObjectClassTypeEnum.AUXILIARY )
                        {
                            weight += 2;
                        }
                    }
                }
                if ( weight > maxWeight )
                {
                    maxObjectClassIconPair = objectClassIconPair;
                }
            }

            if ( maxObjectClassIconPair != null )
            {
                return BrowserCommonActivator.getDefault().getImage( maxObjectClassIconPair.getIconPath() );
            }
        }

        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_ENTRY );
    }


    // ── C-3PO REPORTS: NO SPECIAL FONT FOR THIS ONE ──────────────────────────
    // A Rebel officer asks C-3PO if this particular visitor should be displayed
    // in bold or italic — C-3PO checks his notes and shakes his head: "Standard
    // formatting will do for everyone, sir."
    // We return null, which tells JFace to use the tree's default font.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns null to use the tree's default font for all nodes.
     * We implement {@link IFontProvider} but don't currently differentiate nodes
     * by font. Returning null tells JFace to use its default.
     *
     * <p>For example — C-3PO advises standard formatting for all Rebel personnel:</p>
     * <pre>
     *   Font font = C3PO.getFont(lukeEntry); // null — standard formatting
     * </pre>
     *
     * @param element   the tree node (unused)
     * @return always null
     */
    public Font getFont( Object element )
    {
        return null;
    }


    // ── C-3PO REPORTS: NO SPECIAL TEXT COLOR FOR THIS ONE ────────────────────
    // The same officer asks about foreground color — C-3PO again shakes his
    // head: "All entries display in the standard color, sir."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns null to use the tree's default foreground (text) color for all nodes.
     * We implement {@link IColorProvider} but don't currently apply custom colors.
     *
     * <p>For example — C-3PO advises standard text color for all entries:</p>
     * <pre>
     *   Color fg = C3PO.getForeground(lukeEntry); // null — default color
     * </pre>
     *
     * @param element   the tree node (unused)
     * @return always null
     */
    public Color getForeground( Object element )
    {
        return null;
    }


    // ── C-3PO REPORTS: NO SPECIAL BACKGROUND COLOR FOR THIS ONE ─────────────
    // And the background color? "Standard, sir. We don't highlight individuals."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns null to use the tree's default background color for all nodes.
     * We implement {@link IColorProvider} but don't currently apply custom backgrounds.
     *
     * <p>For example — C-3PO confirms standard background for all entries:</p>
     * <pre>
     *   Color bg = C3PO.getBackground(lukeEntry); // null — default background
     * </pre>
     *
     * @param element   the tree node (unused)
     * @return always null
     */
    public Color getBackground( Object element )
    {
        return null;
    }

}
