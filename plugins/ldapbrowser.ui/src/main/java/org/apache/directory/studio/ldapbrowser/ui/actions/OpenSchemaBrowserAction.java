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


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser.SchemaBrowserManager;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: OpenSchemaBrowserAction — LUKE'S BINARY SUNSET ────────────────────
// Luke stands at the edge of the Tatooine desert, gazing at the twin suns sinking
// below the horizon.  For a moment he can see the whole landscape at once — the
// scope of the world he lives in, the rules that govern it.  An LDAP schema is
// exactly that: the full landscape of object classes, attribute types, matching
// rules, and syntaxes that define what entries can exist and what they can contain.
// OpenSchemaBrowserAction opens the Schema Browser editor so the user can stand at
// that vantage point and take in the whole picture.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Schema Browser editor and optionally navigates it to a specific schema
 * element (object class, attribute type, matching rule, or syntax) based on the
 * current selection and the {@code mode} field.
 * The schema tells you the rules of the directory: what object classes exist, what
 * attributes they require or allow, how values are compared.  This action is the
 * gateway to browsing all of that.
 * Think of this class as Luke's binary sunset moment — we're opening the panoramic
 * view of everything the directory schema defines.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSchemaBrowserAction extends BrowserAction
{
    /**
     * None Mode
     */
    public static final int MODE_NONE = 0;

    /**
     * Object Class Mode
     */
    public static final int MODE_OBJECTCLASS = 10;

    /**
     * Attribute Type Mode
     */
    public static final int MODE_ATTRIBUTETYPE = 20;

    /**
     * Equality Matching Rule Mode
     */
    public static final int MODE_EQUALITYMATCHINGRULE = 30;

    /**
     * Substring Matching Rule Mode
     */
    public static final int MODE_SUBSTRINGMATCHINGRULE = 31;

    /**
     * Ordering Matching Rule Mode
     */
    public static final int MODE_ORDERINGMATCHINGRULE = 32;

    /**
     * Syntax Mode
     */
    public static final int MODE_SYNTAX = 40;

    protected int mode;


    // ── Luke Wanders To The Ridge With No Target ─────────────────────────────────
    // Sometimes Luke just strolled out to the ridge — no specific destination in
    // mind, just to see the sunset.  The no-arg constructor sets mode to MODE_NONE,
    // meaning we open the schema browser without navigating to any specific element.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an OpenSchemaBrowserAction in MODE_NONE.
     * When run, this will open the schema browser on the connected server's schema
     * without pre-selecting any specific element — a general overview.
     */
    public OpenSchemaBrowserAction()
    {
        super();
        this.mode = MODE_NONE;
    }


    // ── Luke Walks Out To See A Specific Constellation ──────────────────────────
    // Some evenings Luke went out specifically to watch a particular constellation
    // rise.  This constructor lets callers pass a mode constant so that when run()
    // fires, the schema browser opens directly to the right element type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an OpenSchemaBrowserAction in a specific display mode.
     * The mode determines which schema element type the browser will navigate to
     * based on the current selection — use one of the {@code MODE_*} constants.
     *
     * @param mode  one of {@link #MODE_NONE}, {@link #MODE_OBJECTCLASS},
     *              {@link #MODE_ATTRIBUTETYPE}, {@link #MODE_EQUALITYMATCHINGRULE},
     *              {@link #MODE_SUBSTRINGMATCHINGRULE}, {@link #MODE_ORDERINGMATCHINGRULE},
     *              or {@link #MODE_SYNTAX}
     */
    public OpenSchemaBrowserAction( int mode )
    {
        super();
        this.mode = mode;
    }


    // ── Luke Takes In The Full Landscape ────────────────────────────────────────
    // Luke's gaze sweeps across the whole horizon — or locks onto one specific
    // constellation depending on his mood.  run() calls SchemaBrowserManager.setInput()
    // with either null (for the general overview) or a specific schema element
    // extracted from the current selection based on our mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Schema Browser and navigates it according to our mode setting.
     * In MODE_NONE we open the browser without focusing on any specific element.
     * In other modes we extract the relevant schema element from the current selection
     * (object class name from a value, attribute type from an attribute, etc.) and
     * pass it to the browser so it opens directly to that item.
     */
    public void run()
    {
        if ( mode == MODE_NONE )
        {
            SchemaBrowserManager.setInput( getConnection(), null );
        }
        else if ( mode == MODE_OBJECTCLASS )
        {
            SchemaBrowserManager.setInput( getConnection(), getOcd() );
        }
        else if ( mode == MODE_ATTRIBUTETYPE )
        {
            SchemaBrowserManager.setInput( getConnection(), getAtd() );
        }
        else if ( mode == MODE_EQUALITYMATCHINGRULE )
        {
            SchemaBrowserManager.setInput( getConnection(), getEmrd() );
        }
        else if ( mode == MODE_SUBSTRINGMATCHINGRULE )
        {
            SchemaBrowserManager.setInput( getConnection(), getSmrd() );
        }
        else if ( mode == MODE_ORDERINGMATCHINGRULE )
        {
            SchemaBrowserManager.setInput( getConnection(), getOmrd() );
        }
        else if ( mode == MODE_SYNTAX )
        {
            SchemaBrowserManager.setInput( getConnection(), getLsd() );
        }
        else
        {
            SchemaBrowserManager.setInput( getConnection(), null );
        }
    }


    // ── Horizon Label Changes With The View ──────────────────────────────────────
    // Luke might describe the scene differently depending on what he's looking at:
    // "just the sunset" vs. "the twin moons rising."  getText() returns a mode-
    // specific label so the menu item clearly describes what it will open.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action, adapted to the current mode.
     * Each mode gets a distinct label — "Open Schema Browser" for the general case,
     * or "Object Class Description", "Attribute Description", etc. for specific modes.
     *
     * @return the localised, mode-specific label string
     */
    public String getText()
    {
        if ( mode == MODE_NONE )
        {
            return Messages.getString( "OpenSchemaBrowserAction.OpenSchemaBrowser" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_OBJECTCLASS )
        {
            return Messages.getString( "OpenSchemaBrowserAction.ObjectDescription" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_ATTRIBUTETYPE )
        {
            return Messages.getString( "OpenSchemaBrowserAction.AttributeDescription" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_EQUALITYMATCHINGRULE )
        {
            return Messages.getString( "OpenSchemaBrowserAction.EqualityDescription" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_SUBSTRINGMATCHINGRULE )
        {
            return Messages.getString( "OpenSchemaBrowserAction.SubstringDescription" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_ORDERINGMATCHINGRULE )
        {
            return Messages.getString( "OpenSchemaBrowserAction.OrderingDescription" ); //$NON-NLS-1$
        }
        else if ( mode == MODE_SYNTAX )
        {
            return Messages.getString( "OpenSchemaBrowserAction.SyntaxDescription" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "OpenSchemaBrowserAction.OpenSchemaBrowser" ); //$NON-NLS-1$
        }
    }


    // ── Each View Has Its Own Icon ───────────────────────────────────────────────
    // The binary sunset looks different from the constellation — different icon per
    // mode so the toolbar buttons are visually distinct and instantly recognisable.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's icon, adapted to the current mode.
     * Each mode maps to a distinct icon in the plugin's image registry (the schema
     * browser icon for MODE_NONE, and type-specific icons for the other modes).
     *
     * @return the mode-specific image descriptor, or the schema browser icon as fallback
     */
    public ImageDescriptor getImageDescriptor()
    {
        if ( mode == MODE_NONE )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_BROWSER_SCHEMABROWSEREDITOR );
        }
        else if ( mode == MODE_OBJECTCLASS )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_OCD );
        }
        else if ( mode == MODE_ATTRIBUTETYPE )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_ATD );
        }
        else if ( mode == MODE_EQUALITYMATCHINGRULE )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_MRD_EQUALITY );
        }
        else if ( mode == MODE_SUBSTRINGMATCHINGRULE )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_MRD_SUBSTRING );
        }
        else if ( mode == MODE_ORDERINGMATCHINGRULE )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_MRD_ORDERING );
        }
        else if ( mode == MODE_SYNTAX )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LSD );
        }
        else
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_BROWSER_SCHEMABROWSEREDITOR );
        }
    }


    // ── No Standard Rebel Broadcast Code ────────────────────────────────────────
    // Luke's private sunsets had no Alliance command code — they were personal moments.
    // This action has no registered Eclipse command ID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * No global command ID is registered for this action.
     *
     * @return always null
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Can Luke See The Sunset From Here ───────────────────────────────────────
    // Luke can only enjoy the binary sunset if he's actually on Tatooine — if he's
    // in hyperspace the view doesn't exist.  isEnabled() checks that the right kind
    // of schema element is available in the current selection for the given mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether this action is available for the current selection.
     * For MODE_NONE we just need a non-null connection; for the element-specific
     * modes we need the corresponding schema element to be derivable from the selection.
     *
     * @return true if the preconditions for the current mode are met
     */
    public boolean isEnabled()
    {

        if ( mode == MODE_NONE )
        {
            return getConnection() != null;
        }
        else if ( mode == MODE_OBJECTCLASS )
        {
            return getOcd() != null;
        }
        else if ( mode == MODE_ATTRIBUTETYPE )
        {
            return getAtd() != null;
        }
        else if ( mode == MODE_EQUALITYMATCHINGRULE )
        {
            return getEmrd() != null;
        }
        else if ( mode == MODE_SUBSTRINGMATCHINGRULE )
        {
            return getSmrd() != null;
        }
        else if ( mode == MODE_ORDERINGMATCHINGRULE )
        {
            return getOmrd() != null;
        }
        else if ( mode == MODE_SYNTAX )
        {
            return getLsd() != null;
        }
        else
        {
            return false;
        }
    }


    // ── Finding The Syntax Star ──────────────────────────────────────────────────
    // Luke knew that the binary stars followed a predictable path; he could trace the
    // path of one star to the other.  getLsd() walks from the selected attribute type
    // to its associated LDAP syntax description via the schema's OID registry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Derives the LDAP syntax description for the attribute type in the current selection.
     * We walk from the selected attribute to its syntax OID (following the inheritance
     * chain transitively) and then look that OID up in the schema.
     *
     * @return the {@link LdapSyntax} for the selected attribute, or null if there is
     *         no connection, no selected attribute, or no matching syntax in the schema
     */
    private LdapSyntax getLsd()
    {
        if ( getConnection() != null )
        {
            Schema schema = getConnection().getSchema();
            AttributeType atd = getAtd();

            if ( atd != null && SchemaUtils.getSyntaxNumericOidTransitive( atd, schema ) != null
                && schema.hasLdapSyntaxDescription( SchemaUtils.getSyntaxNumericOidTransitive( atd, schema ) ) )
            {
                return schema.getLdapSyntaxDescription( SchemaUtils.getSyntaxNumericOidTransitive( atd, schema ) );
            }
        }

        return null;
    }


    // ── Reading The Constellation Name ──────────────────────────────────────────
    // Luke would look up at a constellation and name it — he'd been taught the
    // names as a kid.  getOcd() does the same: it reads the object class name from
    // the selected objectClass attribute value and looks up the matching schema entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Derives the object class description from the current selection.
     * We look for a single selected value on an objectClass attribute, extract its
     * string value (the object class name), and look it up in the server's schema.
     *
     * @return the {@link ObjectClass} for the selected value, or null if the selection
     *         isn't a single objectClass value or the class isn't in the schema
     */
    private ObjectClass getOcd()
    {
        if ( getSelectedAttributes().length == 0 && getSelectedValues().length == 1
            && getSelectedValues()[0].getAttribute().isObjectClassAttribute() )
        {
            String ocdName = getSelectedValues()[0].getStringValue();
            if ( ocdName != null
                && getSelectedValues()[0].getAttribute().getEntry().getBrowserConnection().getSchema()
                    .hasObjectClassDescription( ocdName ) )
            {
                return getSelectedValues()[0].getAttribute().getEntry().getBrowserConnection().getSchema()
                    .getObjectClassDescription( ocdName );
            }
        }

        return null;
    }


    // ── Identifying The Star Type ────────────────────────────────────────────────
    // Luke could tell a white dwarf from a red giant at a glance — he knew the types.
    // getAtd() resolves the attribute type description from whatever is selected:
    // a single value, a single attribute, or a single attribute hierarchy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Derives the attribute type description from the current selection.
     * We handle three selection forms: a single value, a single attribute, and a
     * single attribute hierarchy (all three ultimately lead to one attribute type).
     *
     * @return the {@link AttributeType} for the selection, or null if the selection
     *         doesn't resolve to exactly one attribute type
     */
    private AttributeType getAtd()
    {
        if ( ( getSelectedValues().length + getSelectedAttributes().length ) + getSelectedAttributeHierarchies().length == 1 )
        {
            AttributeType atd = null;
            if ( getSelectedValues().length == 1 )
            {
                atd = getSelectedValues()[0].getAttribute().getAttributeTypeDescription();
            }
            else if ( getSelectedAttributes().length == 1 )
            {
                atd = getSelectedAttributes()[0].getAttributeTypeDescription();
            }
            else if ( getSelectedAttributeHierarchies().length == 1 && getSelectedAttributeHierarchies()[0].size() == 1 )
            {
                atd = getSelectedAttributeHierarchies()[0].getAttribute().getAttributeTypeDescription();
            }

            return atd;
        }

        return null;
    }


    // ── Tracing The Light Back To Its Source ─────────────────────────────────────
    // Luke traced the light of the twin suns back to their source — he knew which
    // star was which.  getConnection() traces the selected object back to its
    // IBrowserConnection, walking through all the possible selection types.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Derives the browser connection from the current selection.
     * We check — in priority order — values, attributes, attribute hierarchies,
     * direct connection selections, entry selections, search-result selections,
     * bookmark selections, and search selections until we find a connection.
     *
     * @return the {@link IBrowserConnection} for the selection, or null if none can be found
     */
    private IBrowserConnection getConnection()
    {
        if ( ( getSelectedValues().length + getSelectedAttributes().length ) + getSelectedAttributeHierarchies().length == 1 )
        {
            IBrowserConnection connection = null;
            if ( getSelectedValues().length == 1 )
            {
                connection = getSelectedValues()[0].getAttribute().getEntry().getBrowserConnection();
            }
            else if ( getSelectedAttributes().length == 1 )
            {
                connection = getSelectedAttributes()[0].getEntry().getBrowserConnection();
            }
            else if ( getSelectedAttributeHierarchies().length == 1 && getSelectedAttributeHierarchies()[0].size() == 1 )
            {
                connection = getSelectedAttributeHierarchies()[0].getAttribute().getEntry().getBrowserConnection();
            }

            return connection;
        }
        else if ( getSelectedConnections().length == 1 )
        {
            Connection connection = getSelectedConnections()[0];
            IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnection( connection );
            return browserConnection;
        }
        else if ( getSelectedEntries().length == 1 )
        {
            return getSelectedEntries()[0].getBrowserConnection();
        }
        else if ( getSelectedSearchResults().length == 1 )
        {
            return getSelectedSearchResults()[0].getEntry().getBrowserConnection();
        }
        else if ( getSelectedBookmarks().length == 1 )
        {
            return getSelectedBookmarks()[0].getBrowserConnection();
        }
        else if ( getSelectedSearches().length == 1 )
        {
            return getSelectedSearches()[0].getBrowserConnection();
        }

        return null;
    }


    // ── Spotting The Equality Star ───────────────────────────────────────────────
    // Some constellations are defined by the equality of their stars' brightness.
    // getEmrd() walks from the attribute type to its equality matching rule using
    // the schema's transitive lookup — following the inheritance chain if needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Derives the equality matching rule description from the selected attribute type.
     * We walk transitively through the attribute type's inheritance chain to find
     * the effective equality matching rule, then look it up in the schema.
     *
     * @return the equality {@link MatchingRule}, or null if none applies to the selection
     */
    private MatchingRule getEmrd()
    {
        if ( getConnection() != null )
        {
            Schema schema = getConnection().getSchema();
            AttributeType atd = getAtd();
            if ( atd != null
                && SchemaUtils.getEqualityMatchingRuleNameOrNumericOidTransitive( atd, schema ) != null
                && schema.hasLdapSyntaxDescription( SchemaUtils.getEqualityMatchingRuleNameOrNumericOidTransitive( atd,
                    schema ) ) )
            {
                return schema.getMatchingRuleDescription( SchemaUtils
                    .getEqualityMatchingRuleNameOrNumericOidTransitive( atd, schema ) );
            }
        }
        return null;
    }


    // ── Spotting The Substring Star ──────────────────────────────────────────────
    // The substring stars are the faint ones — partial matches in the constellation.
    // getSmrd() resolves the substring matching rule for the selected attribute type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Derives the substring matching rule description from the selected attribute type.
     * Substring matching rules define how partial-string searches work for an attribute.
     *
     * @return the substring {@link MatchingRule}, or null if none applies
     */
    private MatchingRule getSmrd()
    {
        if ( getConnection() != null )
        {
            Schema schema = getConnection().getSchema();
            AttributeType atd = getAtd();
            if ( atd != null
                && SchemaUtils.getSubstringMatchingRuleNameOrNumericOidTransitive( atd, schema ) != null
                && schema.hasLdapSyntaxDescription( SchemaUtils.getSubstringMatchingRuleNameOrNumericOidTransitive(
                    atd, schema ) ) )
            {
                return schema.getMatchingRuleDescription( SchemaUtils
                    .getSubstringMatchingRuleNameOrNumericOidTransitive( atd, schema ) );
            }
        }
        return null;
    }


    // ── Spotting The Ordering Star ───────────────────────────────────────────────
    // The ordering star rises before its companion — it defines which comes first.
    // getOmrd() resolves the ordering matching rule for the selected attribute type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Derives the ordering matching rule description from the selected attribute type.
     * Ordering matching rules define how range and sort comparisons work for an attribute.
     *
     * @return the ordering {@link MatchingRule}, or null if none applies
     */
    private MatchingRule getOmrd()
    {
        if ( getConnection() != null )
        {
            Schema schema = getConnection().getSchema();
            AttributeType atd = getAtd();
            if ( atd != null
                && SchemaUtils.getOrderingMatchingRuleNameOrNumericOidTransitive( atd, schema ) != null
                && schema.hasLdapSyntaxDescription( SchemaUtils.getOrderingMatchingRuleNameOrNumericOidTransitive( atd,
                    schema ) ) )
            {
                return schema.getMatchingRuleDescription( SchemaUtils
                    .getOrderingMatchingRuleNameOrNumericOidTransitive( atd, schema ) );
            }
        }
        return null;
    }
}
