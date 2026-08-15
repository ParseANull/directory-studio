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


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: BrowserCategory — CLOUD CITY'S THREE DISTRICT OFFICES ──────────────
// Lando runs Cloud City by dividing it into three distinct administrative
// districts: the mining operations (DIT — actual directory entries), the
// intelligence department (Searches — saved queries), and the VIP registry
// (Bookmarks — pinned entries of interest). Each district has its own name,
// its own data, and its own staff, but they all report to the same city
// administrator (the IBrowserConnection).
// BrowserCategory represents exactly one of those three top-level folders
// that appear in the LDAP browser tree under each connection.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents one of the three top-level category nodes in the browser tree:
 * the DIT (Directory Information Tree), Searches, or Bookmarks.
 * Every LDAP connection in the browser shows these three folders; this class
 * is the model object for those folders.
 * Think of this class as one of Lando's three administrative districts in
 * Cloud City — each has a distinct purpose but shares the same parent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserCategory implements IAdaptable
{

    /** The Constant TYPE_DIT identifies DIT categories. */
    public static final int TYPE_DIT = 0;

    /** The Constant TYPE_SEARCHES identifies searches categories. */
    public static final int TYPE_SEARCHES = 1;

    /** The Constant TYPE_BOOKMARKS identifies bookmark categories. */
    public static final int TYPE_BOOKMARKS = 2;

    /** The title for the DIT categoy */
    public static final String TITLE_DIT = Messages.getString( "BrowserCategory.DIT" ); //$NON-NLS-1$

    /** The title for the searches categoy */
    public static final String TITLE_SEARCHES = Messages.getString( "BrowserCategory.Searches" ); //$NON-NLS-1$

    /** The title for the bookmarks categoy */
    public static final String TITLE_BOOKMARKS = Messages.getString( "BrowserCategory.Bookmarks" ); //$NON-NLS-1$

    /** The category's connection */
    private IBrowserConnection parent;

    /** The category's type */
    private int type;


    // ── CLOUD CITY DISTRICT OFFICE OPENS ─────────────────────────────────────
    // Lando issues a charter for a new district office in Cloud City — signing
    // the papers that officially establish the mining operations bureau and
    // recording which district it is and who the city administrator is.
    // We do the same: record the category type (DIT/Searches/Bookmarks) and
    // the parent connection this category belongs to.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new category node for the browser tree.
     * Each category is one of the three folder types (DIT, Searches, Bookmarks)
     * and is always owned by exactly one LDAP connection.
     *
     * <p>For example — Lando opens a new district office in Cloud City:</p>
     * <pre>
     *   miningDistrict = new District(TYPE_MINING, cityAdministrator);
     *   intelligenceDistrict = new District(TYPE_INTEL, cityAdministrator);
     *   vipRegistry = new District(TYPE_VIP, cityAdministrator);
     * </pre>
     *
     * @param type     the category type — one of {@link #TYPE_DIT}, {@link #TYPE_SEARCHES},
     *                 or {@link #TYPE_BOOKMARKS}
     * @param parent   the LDAP connection this category belongs to; all entries
     *                 under this category are fetched from this connection
     */
    public BrowserCategory( int type, IBrowserConnection parent )
    {
        this.parent = parent;
        this.type = type;
    }


    // ── WHO'S THE CITY ADMINISTRATOR? ────────────────────────────────────────
    // A visiting dignitary asks which Cloud City administrator oversees the
    // mining district — and Lando's aide answers "That would be Administrator
    // Calrissian himself." Every district reports to the same top-level authority.
    // Our category always reports to its parent LDAP connection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection this category belongs to.
     * In the browser tree, a category is always a direct child of a connection —
     * this method walks back up to that parent connection so other code can
     * find out which server owns this category.
     *
     * <p>For example — the visiting dignitary learns who runs Cloud City:</p>
     * <pre>
     *   District miningDistrict = cloudCity.getDistrict(TYPE_MINING);
     *   Administrator admin = miningDistrict.getCityAdministrator();
     *   // admin == Lando Calrissian
     * </pre>
     *
     * @return the parent {@link IBrowserConnection} that owns this category
     */
    public IBrowserConnection getParent()
    {
        return parent;
    }


    // ── WHICH DISTRICT IS THIS? ───────────────────────────────────────────────
    // A Cloud City security guard checks the district badge to know whether
    // to route a visitor to mining, intelligence, or the VIP registry.
    // Callers use our type constant to decide how to handle this category
    // (e.g., the content provider branches on TYPE_DIT vs TYPE_SEARCHES).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the integer type constant for this category.
     * The type is one of {@link #TYPE_DIT} (0), {@link #TYPE_SEARCHES} (1),
     * or {@link #TYPE_BOOKMARKS} (2). The content provider uses this to decide
     * what child data to load when the user expands this category.
     *
     * <p>For example — the Cloud City guard checks which district office this is:</p>
     * <pre>
     *   int badge = district.getType();
     *   if (badge == TYPE_MINING) routeToMiningFloor();
     *   else if (badge == TYPE_INTEL) routeToIntelligenceRoom();
     * </pre>
     *
     * @return the category type constant
     */
    public int getType()
    {
        return type;
    }


    // ── READING THE DISTRICT SIGN ─────────────────────────────────────────────
    // Above each district office door in Cloud City hangs a sign: "Mining
    // Operations", "Intelligence Bureau", or "VIP Registry". Anyone passing
    // by can read the sign to know what's inside without opening the door.
    // The label provider calls this to get the text to display in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display title for this category as shown in the browser tree.
     * The title is an i18n-translated string — "DIT", "Searches", or "Bookmarks" —
     * so it adapts to the user's locale automatically.
     *
     * <p>For example — a visitor reads the sign above the Cloud City district office:</p>
     * <pre>
     *   String sign = district.getTitle();
     *   // "Mining Operations" or "Intelligence Bureau" or "VIP Registry"
     *   treeNode.setLabel(sign);
     * </pre>
     *
     * @return the human-readable category title, never null
     */
    public String getTitle()
    {
        switch ( type )
        {
            case TYPE_DIT:
                return TITLE_DIT;

            case TYPE_SEARCHES:
                return TITLE_SEARCHES;

            case TYPE_BOOKMARKS:
                return TITLE_BOOKMARKS;

            default:
                return "ERROR"; //$NON-NLS-1$
        }
    }


    // ── DOES THIS DISTRICT HAVE A LIAISON? ────────────────────────────────────
    // A visiting diplomat asks Lando if Cloud City's mining district can be
    // adapted to the Empire's administrative framework — Lando checks the
    // district charter and shakes his head: "It doesn't adapt to anything."
    // We return null because BrowserCategory doesn't adapt to other interfaces.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns an adapter object for the given type, or null if this category
     * doesn't support the requested interface.
     * This implements the Eclipse {@link IAdaptable} pattern, which lets generic
     * Eclipse platform code ask "can you give me a {@code Foo} representation of
     * yourself?" We don't support any adapters, so we always return null.
     *
     * <p>For example — Lando checks whether the mining district can adapt to the Empire's framework:</p>
     * <pre>
     *   Object liaison = miningDistrict.getAdapter(EmpireAdminInterface.class);
     *   // liaison == null — we don't do that here
     * </pre>
     *
     * @param adapter   the requested adapter type
     * @return always null — we don't implement any adapters
     */
    public Object getAdapter( Class adapter )
    {
        return null;
    }
}
