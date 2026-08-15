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

package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaIT;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.ldap.model.message.controls.Subentries;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: SearchPageWrapper — The Rebel Briefing Room Before the Battle of Yavin ────
// In A New Hope, Mon Mothma gathers every key officer into the briefing room: Han at
// the nav console, Luke at the targeting station, R2 at the data terminal, Leia with
// the mission name board. Each officer (sub-widget) handles their own piece, but the
// briefing room (this class) orchestrates the whole operation — it builds the room,
// seats everyone, relays updates between stations, loads the mission brief from storage,
// saves it back, kicks off the actual run, and tells the command whether the mission
// parameters are sound. The style bitmask acts like Mon Mothma's briefing agenda:
// she can mark certain stations as "invisible today" or "read-only for this briefing."
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * A composite SWT widget that assembles all the individual search-parameter sub-widgets
 * into one cohesive form. It is used by the search dialog, the search properties page,
 * the batch-operation wizard, and the export wizards — anywhere a full LDAP search needs
 * to be configured.
 * Think of this class as the Rebel briefing room: every sub-widget (connection picker,
 * filter field, scope radios, etc.) is an officer with a specific role, and this class
 * is the room that seats them all and keeps them talking to each other.
 *
 * <p>The {@code style} bitmask controls which fields are visible and which are read-only.
 * Combine the public static constants with bitwise OR — for example:</p>
 * <pre>
 *   int style = SearchPageWrapper.NAME_INVISIBLE | SearchPageWrapper.CONNECTION_READONLY;
 *   new SearchPageWrapper( style );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchPageWrapper extends AbstractWidget
{

    /** The default style */
    public static final int NONE = 0;

    /** Style for invisible name field */
    public static final int NAME_INVISIBLE = 1 << 1;

    /** Style for read-only name field */
    public static final int NAME_READONLY = 1 << 2;

    /** Style for invisible connection field */
    public static final int CONNECTION_INVISIBLE = 1 << 3;

    /** Style for read-only connection field */
    public static final int CONNECTION_READONLY = 1 << 4;

    /** Style for invisible search base field */
    public static final int SEARCHBASE_INVISIBLE = 1 << 5;

    /** Style for read-only search base field */
    public static final int SEARCHBASE_READONLY = 1 << 6;

    /** Style for invisible filter field */
    public static final int FILTER_INVISIBLE = 1 << 7;

    /** Style for read-only filter field */
    public static final int FILTER_READONLY = 1 << 8;

    /** Style for invisible returning attributes field */
    public static final int RETURNINGATTRIBUTES_INVISIBLE = 1 << 9;

    /** Style for read-only returning attributes field */
    public static final int RETURNINGATTRIBUTES_READONLY = 1 << 10;

    /** Style for visible return Dn checkbox */
    public static final int RETURN_DN_VISIBLE = 1 << 11;

    /** Style for checked return Dn checkbox */
    public static final int RETURN_DN_CHECKED = 1 << 12;

    /** Style for visible return all attributes checkbox */
    public static final int RETURN_ALLATTRIBUTES_VISIBLE = 1 << 13;

    /** Style for checked return all attributes checkbox */
    public static final int RETURN_ALLATTRIBUTES_CHECKED = 1 << 14;

    /** Style for visible return operational attributes checkbox */
    public static final int RETURN_OPERATIONALATTRIBUTES_VISIBLE = 1 << 15;

    /** Style for checked return operational attributes checkbox */
    public static final int RETURN_OPERATIONALATTRIBUTES_CHECKED = 1 << 16;

    /** Style for invisible options */
    public static final int OPTIONS_INVISIBLE = 1 << 21;

    /** Style for read-only scope options */
    public static final int SCOPEOPTIONS_READONLY = 1 << 22;

    /** Style for read-only limit options */
    public static final int LIMITOPTIONS_READONLY = 1 << 23;

    /** Style for read-only alias options */
    public static final int ALIASOPTIONS_READONLY = 1 << 24;

    /** Style for read-only referrals options */
    public static final int REFERRALOPTIONS_READONLY = 1 << 25;

    /** Style for invisible follow referrals manually*/
    public static final int REFERRALOPTIONS_FOLLOW_MANUAL_INVISIBLE = 1 << 26;

    /** Style for invisible controls fields */
    public static final int CONTROLS_INVISIBLE = 1 << 30;

    /** The style. */
    protected int style;

    /** The search name label. */
    protected Label searchNameLabel;

    /** The search name text. */
    protected Text searchNameText;

    /** The connection label. */
    protected Label connectionLabel;

    /** The browser connection widget. */
    protected BrowserConnectionWidget browserConnectionWidget;

    /** The search base label. */
    protected Label searchBaseLabel;

    /** The search base widget. */
    protected EntryWidget searchBaseWidget;

    /** The filter label. */
    protected Label filterLabel;

    /** The filter widget. */
    protected FilterWidget filterWidget;

    /** The returning attributes label. */
    protected Label returningAttributesLabel;

    /** The returning attributes widget. */
    protected ReturningAttributesWidget returningAttributesWidget;

    /** The return dn button. */
    protected Button returnDnButton;

    /** The return all attributes button. */
    protected Button returnAllAttributesButton;

    /** The return operational attributes button. */
    protected Button returnOperationalAttributesButton;

    /** The scope widget. */
    protected ScopeWidget scopeWidget;

    /** The limit widget. */
    protected LimitWidget limitWidget;

    /** The aliases dereferencing widget. */
    protected AliasesDereferencingWidget aliasesDereferencingWidget;

    /** The referrals handling widget. */
    protected ReferralsHandlingWidget referralsHandlingWidget;

    /** The control group. */
    protected Group controlGroup;

    /** The ManageDsaIT control button. */
    protected Button manageDsaItControlButton;

    /** The subentries control button. */
    protected Button subentriesControlButton;

    /** The paged search control button. */
    protected Button pagedSearchControlButton;

    /** The paged search control size label. */
    protected Label pagedSearchControlSizeLabel;

    /** The paged search control size text. */
    protected Text pagedSearchControlSizeText;

    /** The paged search control scroll button. */
    protected Button pagedSearchControlScrollButton;


    // ── Mon Mothma Prepares the Briefing Room With a Style Sheet ─────────────────────
    // Mon Mothma walks into the briefing room with her mission agenda: some stations are
    // invisible today ("not relevant to this operation"), some are read-only ("the
    // parameters are locked in"). The style bitmask is her agenda — she stores it and
    // uses it when calling createContents() to set up the room.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new wrapper with the given style bitmask. The style controls which
     * sub-widgets are visible and which are editable. Combine the class-level style
     * constants with bitwise OR. Call {@link #createContents} afterwards to actually
     * build the SWT controls.
     *
     * <p>For example — Mon Mothma sets the briefing agenda:</p>
     * <pre>
     *   agenda = NAME_INVISIBLE | CONNECTION_READONLY;
     *   briefingRoom = new SearchPageWrapper( agenda );
     * </pre>
     *
     * @param style  Bitmask of style flags controlling widget visibility and editability.
     *               Use {@link #NONE} for the fully-interactive default layout.
     */
    public SearchPageWrapper( int style )
    {
        this.style = style;
    }


    // ── Mon Mothma Opens the Briefing and Seats All Officers ─────────────────────────
    // Mon Mothma opens the session and calls each officer in turn: "Name board, take
    // your station. Connection nav, sit down. Search base, filter, attributes, controls,
    // options — everyone in order." Each officer either takes their seat (widget is
    // created) or Mon Mothma waves them off (invisible style flag is set).
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the sub-widget rows inside the given composite, in the order:
     * search name, connection, search base, filter, returning attributes, LDAP controls,
     * and finally the options panel (scope, limits, aliases, referrals).
     * A sub-widget is skipped entirely if its corresponding INVISIBLE style flag is set.
     * Call this exactly once after constructing the wrapper.
     *
     * <p>For example — Mon Mothma seats the officers one by one:</p>
     * <pre>
     *   createSearchNameLine( composite );    // "Name board" station
     *   createConnectionLine( composite );    // Han's nav console
     *   createSearchBaseLine( composite );    // R2's entry target
     *   createFilterLine( composite );        // R2's query terminal
     *   createReturningAttributesLine( ... ); // R2's column list
     *   createControlComposite( composite );  // mission control flags
     *   createOptionsComposite( composite );  // scope, limits, aliases, referrals
     * </pre>
     *
     * @param composite  The SWT composite that will host all the rows.
     *                   Must already have a three-column grid layout.
     */
    public void createContents( final Composite composite )
    {
        // Search Name
        createSearchNameLine( composite );

        // Connection
        createConnectionLine( composite );

        // Search Base
        createSearchBaseLine( composite );

        // Filter
        createFilterLine( composite );

        // Returning Attributes
        createReturningAttributesLine( composite );

        // control
        createControlComposite( composite );

        // scope, limit, alias, referral
        createOptionsComposite( composite );
    }


    // ── Mon Mothma Checks the Mission Agenda ─────────────────────────────────────────
    // "Is this station on today's agenda?" Mon Mothma holds up the agenda and checks
    // whether a specific flag is set. If the bit is there, the station is active.
    // We use a bitwise AND to test whether the required style bit is present.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Tests whether a specific style flag is active in this wrapper's style bitmask.
     * Used internally by all the {@code create*} methods to decide whether to build
     * a widget or skip it, and whether to make it editable or read-only.
     *
     * <p>For example — Mon Mothma checks the agenda:</p>
     * <pre>
     *   if ( agenda.contains( NAME_INVISIBLE ) ) skip the name station;
     * </pre>
     *
     * @param requiredStyle  The style constant to test, e.g. {@link #NAME_INVISIBLE}.
     * @return               {@code true} if the flag is set; {@code false} otherwise.
     */
    protected boolean isActive( int requiredStyle )
    {
        return ( style & requiredStyle ) != 0;
    }


    // ── Mon Mothma Calls the Mission-Name Board Officer to His Station ─────────────────
    // "Mission name board — front and centre!" If the agenda says this station is
    // invisible, the officer doesn't show up at all. If it's read-only, the board is
    // behind glass. Otherwise the officer takes the editable podium.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the search-name row (label + text field) in the given composite.
     * The field is skipped entirely if {@link #NAME_INVISIBLE} is set.
     * The field is read-only if {@link #NAME_READONLY} is set.
     * Changing the text fires {@link #validate()} to update the form's validity state.
     *
     * <p>For example — Mon Mothma positions the mission-name board:</p>
     * <pre>
     *   if ( NAME_INVISIBLE ) return; // station not on agenda
     *   label = new Label( "Search Name:" );
     *   field = NAME_READONLY ? readonlyText() : editableText();
     * </pre>
     *
     * @param composite  The parent composite to add the row into.
     */
    protected void createSearchNameLine( final Composite composite )
    {
        if ( isActive( NAME_INVISIBLE ) )
        {
            return;
        }

        searchNameLabel = BaseWidgetUtils.createLabel( composite,
            Messages.getString( "SearchPageWrapper.SearchName" ), 1 ); //$NON-NLS-1$
        if ( isActive( NAME_READONLY ) )
        {
            searchNameText = BaseWidgetUtils.createReadonlyText( composite, "", 2 ); //$NON-NLS-1$
        }
        else
        {
            searchNameText = BaseWidgetUtils.createText( composite, "", 2 ); //$NON-NLS-1$
        }
        searchNameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );
        searchNameText.setFocus();

        BaseWidgetUtils.createSpacer( composite, 3 );
    }


    // ── Mon Mothma Calls Han to the Nav Console Station ──────────────────────────────
    // "Han Solo — nav console, please." If the agenda says invisible, Han skips it.
    // If read-only, the console is in display mode only. Otherwise Han takes the live seat.
    // The moment Han picks a connection, the rest of the briefing room adapts.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the connection row (label + {@link BrowserConnectionWidget}) in the
     * given composite. Skipped if {@link #CONNECTION_INVISIBLE} is set.
     * The widget is read-only if {@link #CONNECTION_READONLY} is set.
     * Connection changes fire {@link #validate()}, which propagates the new connection
     * to dependent widgets (search base, filter, etc.).
     *
     * <p>For example — Mon Mothma seats Han at the nav console:</p>
     * <pre>
     *   if ( CONNECTION_INVISIBLE ) return;
     *   label = new Label( "Connection:" );
     *   browserConnectionWidget = new BrowserConnectionWidget();
     * </pre>
     *
     * @param composite  The parent composite to add the row into.
     */
    protected void createConnectionLine( final Composite composite )
    {
        if ( isActive( CONNECTION_INVISIBLE ) )
        {
            return;
        }

        connectionLabel = BaseWidgetUtils.createLabel( composite,
            Messages.getString( "SearchPageWrapper.Connection" ), 1 ); //$NON-NLS-1$
        browserConnectionWidget = new BrowserConnectionWidget();
        browserConnectionWidget.createWidget( composite );
        browserConnectionWidget.setEnabled( !isActive( CONNECTION_READONLY ) );
        browserConnectionWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );
        BaseWidgetUtils.createSpacer( composite, 3 );
    }


    // ── Mon Mothma Calls R2 to the Search-Base Terminal ──────────────────────────────
    // "R2, search-base terminal — over here." If the station is invisible, R2 skips it.
    // If read-only, he can see the target but can't change it. Otherwise R2 takes the
    // live terminal and can navigate the directory tree.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the search-base row (label + {@link EntryWidget}) in the given composite.
     * Skipped if {@link #SEARCHBASE_INVISIBLE} is set. Read-only if
     * {@link #SEARCHBASE_READONLY} is set. Changes fire {@link #validate()}.
     *
     * <p>For example — Mon Mothma seats R2 at the search-base terminal:</p>
     * <pre>
     *   if ( SEARCHBASE_INVISIBLE ) return;
     *   label = new Label( "Search Base:" );
     *   searchBaseWidget = new EntryWidget();
     * </pre>
     *
     * @param composite  The parent composite to add the row into.
     */
    protected void createSearchBaseLine( final Composite composite )
    {
        if ( isActive( SEARCHBASE_INVISIBLE ) )
        {
            return;
        }

        searchBaseLabel = BaseWidgetUtils.createLabel( composite,
            Messages.getString( "SearchPageWrapper.SearchBase" ), 1 ); //$NON-NLS-1$
        searchBaseWidget = new EntryWidget();
        searchBaseWidget.createWidget( composite );
        searchBaseWidget.setEnabled( !isActive( SEARCHBASE_READONLY ) );
        searchBaseWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );
        BaseWidgetUtils.createSpacer( composite, 3 );
    }


    // ── Mon Mothma Calls R2 to the Filter Query Terminal ─────────────────────────────
    // "R2, filter terminal — your LDAP query station." Invisible means R2 skips it;
    // read-only means he can't edit; live means he can type or open the filter editor.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the filter row (label + {@link FilterWidget}) in the given composite.
     * Skipped if {@link #FILTER_INVISIBLE} is set. Read-only if {@link #FILTER_READONLY}.
     * Changes fire {@link #validate()}.
     *
     * <p>For example — Mon Mothma seats R2 at the filter terminal:</p>
     * <pre>
     *   if ( FILTER_INVISIBLE ) return;
     *   label = new Label( "Filter:" );
     *   filterWidget = new FilterWidget();
     * </pre>
     *
     * @param composite  The parent composite to add the row into.
     */
    protected void createFilterLine( final Composite composite )
    {
        if ( isActive( FILTER_INVISIBLE ) )
        {
            return;
        }

        filterLabel = BaseWidgetUtils.createLabel( composite, Messages.getString( "SearchPageWrapper.Filter" ), 1 ); //$NON-NLS-1$
        filterWidget = new FilterWidget();
        filterWidget.createWidget( composite );
        filterWidget.setEnabled( !isActive( FILTER_READONLY ) );
        filterWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );
        BaseWidgetUtils.createSpacer( composite, 3 );
    }


    // ── Mon Mothma Calls R2 to the Returning-Attributes Terminal ─────────────────────
    // "R2, column-selection terminal." If invisible, skipped. If read-only, display only.
    // Optionally, extra checkboxes for "Export DN", "All Attributes", and "Operational
    // Attributes" are placed below the combo when the relevant style flags are set.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the returning-attributes row (label + {@link ReturningAttributesWidget})
     * plus optional shortcut checkboxes for DN export, all user attributes, and
     * operational attributes. Skipped if {@link #RETURNINGATTRIBUTES_INVISIBLE} is set.
     * Changes fire {@link #validate()}.
     *
     * <p>For example — Mon Mothma seats R2 at the attributes station:</p>
     * <pre>
     *   if ( RETURNINGATTRIBUTES_INVISIBLE ) return;
     *   label = new Label( "Returning Attributes:" );
     *   returningAttributesWidget = new ReturningAttributesWidget();
     *   if ( RETURN_DN_VISIBLE )              add returnDnButton;
     *   if ( RETURN_ALLATTRIBUTES_VISIBLE )   add returnAllAttributesButton;
     *   if ( RETURN_OPERATIONALATTRIBUTES_VISIBLE ) add returnOperationalAttributesButton;
     * </pre>
     *
     * @param composite  The parent composite to add the row into.
     */
    protected void createReturningAttributesLine( final Composite composite )
    {
        if ( isActive( RETURNINGATTRIBUTES_INVISIBLE ) )
        {
            return;
        }

        BaseWidgetUtils.createLabel( composite, Messages.getString( "SearchPageWrapper.ReturningAttributes" ), 1 ); //$NON-NLS-1$
        Composite retComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 2 );
        returningAttributesWidget = new ReturningAttributesWidget();
        returningAttributesWidget.createWidget( retComposite );
        returningAttributesWidget.setEnabled( !isActive( RETURNINGATTRIBUTES_READONLY ) );
        returningAttributesWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        // special returning attributes options
        if ( isActive( RETURN_DN_VISIBLE ) || isActive( RETURN_ALLATTRIBUTES_VISIBLE )
            || isActive( RETURN_OPERATIONALATTRIBUTES_VISIBLE ) )
        {
            BaseWidgetUtils.createSpacer( composite, 1 );
            Composite buttonComposite = BaseWidgetUtils.createColumnContainer( composite, 3, 2 );
            if ( isActive( RETURN_DN_VISIBLE ) )
            {
                returnDnButton = BaseWidgetUtils.createCheckbox( buttonComposite, Messages
                    .getString( "SearchPageWrapper.ExportDN" ), 1 ); //$NON-NLS-1$
                returnDnButton.addSelectionListener( new SelectionAdapter()
                {
                    public void widgetSelected( SelectionEvent e )
                    {
                        validate();
                    }
                } );
                returnDnButton.setSelection( isActive( RETURN_DN_CHECKED ) );
            }
            if ( isActive( RETURN_ALLATTRIBUTES_VISIBLE ) )
            {
                returnAllAttributesButton = BaseWidgetUtils.createCheckbox( buttonComposite, Messages
                    .getString( "SearchPageWrapper.AllUserAttributes" ), 1 ); //$NON-NLS-1$
                returnAllAttributesButton.addSelectionListener( new SelectionAdapter()
                {
                    public void widgetSelected( SelectionEvent e )
                    {
                        validate();
                    }
                } );
                returnAllAttributesButton.setSelection( isActive( RETURN_ALLATTRIBUTES_CHECKED ) );
            }
            if ( isActive( RETURN_OPERATIONALATTRIBUTES_VISIBLE ) )
            {
                returnOperationalAttributesButton = BaseWidgetUtils.createCheckbox( buttonComposite, Messages
                    .getString( "SearchPageWrapper.OperationalAttributes" ), 1 ); //$NON-NLS-1$
                returnOperationalAttributesButton.addSelectionListener( new SelectionAdapter()
                {
                    public void widgetSelected( SelectionEvent e )
                    {
                        validate();
                    }
                } );
                returnOperationalAttributesButton.setSelection( isActive( RETURN_OPERATIONALATTRIBUTES_CHECKED ) );
            }
        }

        BaseWidgetUtils.createSpacer( composite, 3 );
    }


    // ── Mon Mothma Deploys the Four Options Officers ──────────────────────────────────
    // "Luke — scope station. Han — limits. 3PO — aliases. R2 — referrals routing."
    // All four sit in a two-column row. If OPTIONS_INVISIBLE is on the agenda, nobody
    // shows up; individual READONLY flags grey out specific stations.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the options composite containing the four advanced-search widgets:
     * {@link ScopeWidget}, {@link LimitWidget}, {@link AliasesDereferencingWidget},
     * and {@link ReferralsHandlingWidget}, arranged in a two-column grid.
     * Skipped entirely if {@link #OPTIONS_INVISIBLE} is set. Individual widgets
     * are read-only if their corresponding READONLY flags are set.
     * Changes to any sub-widget fire {@link #validate()}.
     *
     * <p>For example — Mon Mothma deploys the four options officers:</p>
     * <pre>
     *   if ( OPTIONS_INVISIBLE ) return;
     *   scopeWidget             = new ScopeWidget();
     *   limitWidget             = new LimitWidget();
     *   aliasesDereferencingWidget = new AliasesDereferencingWidget();
     *   referralsHandlingWidget = new ReferralsHandlingWidget();
     * </pre>
     *
     * @param composite  The parent composite to add the options row into.
     */
    protected void createOptionsComposite( final Composite composite )
    {
        if ( isActive( OPTIONS_INVISIBLE ) )
        {
            return;
        }

        Composite optionsComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 3 );

        scopeWidget = new ScopeWidget();
        scopeWidget.createWidget( optionsComposite );
        scopeWidget.setEnabled( !isActive( SCOPEOPTIONS_READONLY ) );
        scopeWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        limitWidget = new LimitWidget();
        limitWidget.createWidget( optionsComposite );
        limitWidget.setEnabled( !isActive( LIMITOPTIONS_READONLY ) );
        limitWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        aliasesDereferencingWidget = new AliasesDereferencingWidget();
        aliasesDereferencingWidget.createWidget( optionsComposite );
        aliasesDereferencingWidget.setEnabled( !isActive( ALIASOPTIONS_READONLY ) );
        aliasesDereferencingWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        referralsHandlingWidget = new ReferralsHandlingWidget();
        referralsHandlingWidget.createWidget( optionsComposite, !isActive( REFERRALOPTIONS_FOLLOW_MANUAL_INVISIBLE ) );
        referralsHandlingWidget.setEnabled( !isActive( REFERRALOPTIONS_READONLY ) );
        referralsHandlingWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );
    }


    // ── Mon Mothma Deploys the LDAP Control Flags Panel ──────────────────────────────
    // "Before we launch: confirm ManageDsaIT, Subentries, and Paged Results flags."
    // These are LDAP protocol-level control extensions that modify search behaviour.
    // If CONTROLS_INVISIBLE is set, the whole panel stays hidden (most UI contexts skip it).
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the LDAP controls panel: checkboxes for ManageDsaIT (treat referrals as
     * normal entries), Subentries (include subschema subentries), and Paged Results
     * (paginate results with a configurable page size and scroll-mode option).
     * Skipped entirely if {@link #CONTROLS_INVISIBLE} is set.
     * Changes fire {@link #validate()}.
     *
     * <p>For example — Mon Mothma activates the mission control flags:</p>
     * <pre>
     *   if ( CONTROLS_INVISIBLE ) return;
     *   manageDsaItControlButton   = new CheckBox( "ManageDsaIT" );
     *   subentriesControlButton    = new CheckBox( "Subentries" );
     *   pagedSearchControlButton   = new CheckBox( "Paged Search" );
     *   pagedSearchControlSizeText = new Text( "100" ); // page size
     * </pre>
     *
     * @param composite  The parent composite to add the controls group into.
     */
    protected void createControlComposite( final Composite composite )
    {
        if ( isActive( CONTROLS_INVISIBLE ) )
        {
            return;
        }

        Composite controlComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 3 );
        controlGroup = BaseWidgetUtils.createGroup( controlComposite,
            Messages.getString( "SearchPageWrapper.Controls" ), 1 ); //$NON-NLS-1$

        // ManageDsaIT control
        manageDsaItControlButton = BaseWidgetUtils.createCheckbox( controlGroup, Messages
            .getString( "SearchPageWrapper.ManageDsaIt" ), 1 ); //$NON-NLS-1$
        manageDsaItControlButton.setToolTipText( Messages.getString( "SearchPageWrapper.ManageDsaItTooltip" ) ); //$NON-NLS-1$
        manageDsaItControlButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                validate();
            }
        } );

        // subentries control
        subentriesControlButton = BaseWidgetUtils.createCheckbox( controlGroup, Messages
            .getString( "SearchPageWrapper.Subentries" ), 1 ); //$NON-NLS-1$
        subentriesControlButton.setToolTipText( Messages.getString( "SearchPageWrapper.SubentriesTooltip" ) ); //$NON-NLS-1$
        subentriesControlButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                validate();
            }
        } );

        // simple paged results control
        Composite sprcComposite = BaseWidgetUtils.createColumnContainer( controlGroup, 4, 1 );
        pagedSearchControlButton = BaseWidgetUtils.createCheckbox( sprcComposite, Messages
            .getString( "SearchPageWrapper.PagedSearch" ), 1 ); //$NON-NLS-1$
        pagedSearchControlButton.setToolTipText( Messages.getString( "SearchPageWrapper.PagedSearchToolTip" ) ); //$NON-NLS-1$
        pagedSearchControlButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                validate();
            }
        } );
        pagedSearchControlSizeLabel = BaseWidgetUtils.createLabel( sprcComposite, Messages
            .getString( "SearchPageWrapper.PageSize" ), 1 ); //$NON-NLS-1$
        pagedSearchControlSizeText = BaseWidgetUtils.createText( sprcComposite, "100", 5, 1 ); //$NON-NLS-1$
        pagedSearchControlSizeText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
        pagedSearchControlSizeText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );
        pagedSearchControlScrollButton = BaseWidgetUtils.createCheckbox( sprcComposite, Messages
            .getString( "SearchPageWrapper.ScrollMode" ), 1 ); //$NON-NLS-1$
        pagedSearchControlScrollButton.setToolTipText( Messages.getString( "SearchPageWrapper.ScrollModeToolTip" ) ); //$NON-NLS-1$
        pagedSearchControlScrollButton.setSelection( true );
        pagedSearchControlScrollButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                validate();
            }
        } );
    }


    // ── Mon Mothma Calls the Room to Order After Each Change ─────────────────────────
    // Whenever any officer updates their station, Mon Mothma stands up and re-checks
    // the whole room: if Han changed his connection, R2's search-base must be updated;
    // if paged search was enabled, the page-size field should light up. Finally she
    // notifies the outer command (the dialog/wizard) that something changed.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Called whenever any sub-widget fires a change event. Re-synchronises inter-widget
     * dependencies (search base tracks the connection; paged-search sub-fields enable
     * only when paged search is checked) and then notifies outer listeners via
     * {@link #notifyListeners()} so the owning dialog can update its OK/Finish button state.
     *
     * <p>For example — Mon Mothma re-checks the briefing room after each update:</p>
     * <pre>
     *   if ( connection changed ) searchBaseWidget.setInput( newConnection, null );
     *   filterWidget.setBrowserConnection( currentConnection );
     *   pagedSearchSizeField.setEnabled( pagedSearchChecked );
     *   notifyListeners();
     * </pre>
     */
    protected void validate()
    {
        if ( browserConnectionWidget.getBrowserConnection() != null )
        {
            if ( searchBaseWidget.getDn() == null
                || searchBaseWidget.getBrowserConnection() != browserConnectionWidget.getBrowserConnection() )
            {
                searchBaseWidget.setInput( browserConnectionWidget.getBrowserConnection(), null );
            }
        }

        filterWidget.setBrowserConnection( browserConnectionWidget.getBrowserConnection() );

        pagedSearchControlSizeLabel.setEnabled( pagedSearchControlButton.getSelection() );
        pagedSearchControlSizeText.setEnabled( pagedSearchControlButton.getSelection() );
        pagedSearchControlScrollButton.setEnabled( pagedSearchControlButton.getSelection() );

        super.notifyListeners();
    }


    // ── Mon Mothma Checks Whether the DN-Export Flag Is Raised ───────────────────────
    // The export wizard asks: "Are we putting DNs in the export file?"
    // Mon Mothma checks the "Export DN" checkbox and reports yes or no.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the "Export DN" checkbox is visible and currently checked.
     * Used by the export wizard to know whether to include the entry's DN as a column
     * in the exported data. Returns {@code false} if the checkbox is not shown.
     *
     * <p>For example — Mon Mothma checks the DN-export flag:</p>
     * <pre>
     *   return exportDnCheckbox != null and exportDnCheckbox.isSelected();
     * </pre>
     *
     * @return  {@code true} if the DN-export checkbox exists and is ticked.
     */
    public boolean isReturnDn()
    {
        return returnDnButton != null && returnDnButton.getSelection();
    }


    // ── Mon Mothma Reads the Mission Brief Into the Briefing Room ─────────────────────
    // The saved mission brief arrives from storage. Mon Mothma reads it aloud and
    // each officer updates their station: Han dials in the connection, R2 sets the
    // search base, Luke adjusts the scope, 3PO notes the alias and referral policies.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates all visible widgets from an existing {@link ISearch} object. Call this
     * when opening the search-properties dialog for a saved search, or when an existing
     * search is being re-run with its stored parameters. Only non-null widgets are
     * updated, so invisible sub-widgets are safely ignored.
     *
     * <p>For example — Mon Mothma reads the mission brief:</p>
     * <pre>
     *   searchNameText.setText( search.getName() );
     *   browserConnectionWidget.setBrowserConnection( search.getBrowserConnection() );
     *   searchBaseWidget.setInput( connection, search.getSearchBase() );
     *   filterWidget.setFilter( search.getFilter() );
     *   // ... and so on for each widget
     * </pre>
     *
     * @param search  The search whose parameters should be loaded into the form.
     *                Must not be null.
     */
    public void loadFromSearch( ISearch search )
    {
        if ( searchNameText != null )
        {
            searchNameText.setText( search.getName() );
        }

        if ( search.getBrowserConnection() != null )
        {
            IBrowserConnection browserConnection = search.getBrowserConnection();
            Dn searchBase = search.getSearchBase();

            if ( browserConnectionWidget != null )
            {
                browserConnectionWidget.setBrowserConnection( browserConnection );
            }

            if ( searchBase != null )
            {
                searchBaseWidget.setInput( browserConnection, searchBase );
            }

            if ( filterWidget != null )
            {
                filterWidget.setBrowserConnection( browserConnection );
                filterWidget.setFilter( search.getFilter() );
            }

            if ( returningAttributesWidget != null )
            {
                returningAttributesWidget.setBrowserConnection( browserConnection );
                returningAttributesWidget.setInitialReturningAttributes( search.getReturningAttributes() );
            }

            if ( scopeWidget != null )
            {
                scopeWidget.setScope( search.getScope() );
            }
            if ( limitWidget != null )
            {
                limitWidget.setCountLimit( search.getCountLimit() );
                limitWidget.setTimeLimit( search.getTimeLimit() );
            }
            if ( aliasesDereferencingWidget != null )
            {
                aliasesDereferencingWidget.setAliasesDereferencingMethod( search.getAliasesDereferencingMethod() );
            }
            if ( referralsHandlingWidget != null )
            {
                referralsHandlingWidget.setReferralsHandlingMethod( search.getReferralsHandlingMethod() );
            }
            if ( subentriesControlButton != null )
            {
                List<Control> searchControls = search.getControls();
                if ( searchControls != null && searchControls.size() > 0 )
                {
                    for ( Control c : searchControls )
                    {
                        if ( c instanceof ManageDsaIT )
                        {
                            manageDsaItControlButton.setSelection( true );
                        }
                        else if ( c instanceof Subentries )
                        {
                            subentriesControlButton.setSelection( true );
                        }
                        else if ( c instanceof PagedResults )
                        {
                            pagedSearchControlButton.setSelection( true );
                            pagedSearchControlSizeText.setText( "" + ( ( PagedResults ) c ).getSize() ); //$NON-NLS-1$
                            pagedSearchControlScrollButton.setSelection( search.isPagedSearchScrollMode() );
                        }
                    }
                }
            }
        }
    }


    // ── Mon Mothma Writes the Updated Brief Back to the Mission Log ──────────────────
    // The briefing is over. Mon Mothma collects each officer's updated parameters and
    // writes them back into the mission brief. She only writes things that actually
    // changed, and returns true if anything was modified (so the caller knows whether
    // to mark the search as "dirty" and trigger a re-run or property save).
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads all visible widget values and writes them back into the given {@link ISearch}
     * object, updating only the fields that actually changed. Returns {@code true} if
     * any parameter was modified, so the caller knows whether to persist the change or
     * schedule a new search run.
     *
     * <p>For example — Mon Mothma debriefs and updates the mission log:</p>
     * <pre>
     *   if ( nameField.changed ) search.setName( newName ); modified = true;
     *   if ( filterWidget.changed ) search.setFilter( newFilter ); modified = true;
     *   // ... and so on
     *   return modified;
     * </pre>
     *
     * @param search  The search object to write values into. Must not be null.
     * @return        {@code true} if at least one parameter was changed; {@code false}
     *                if all widget values matched the existing search parameters.
     */
    public boolean saveToSearch( ISearch search )
    {
        boolean searchModified = false;

        if ( searchNameText != null && !searchNameText.getText().equals( search.getName() ) )
        {
            search.getSearchParameter().setName( searchNameText.getText() );
            searchModified = true;
        }
        if ( browserConnectionWidget != null && browserConnectionWidget.getBrowserConnection() != null
            && browserConnectionWidget.getBrowserConnection() != search.getBrowserConnection() )
        {
            search.setBrowserConnection( browserConnectionWidget.getBrowserConnection() );
            searchModified = true;
        }
        if ( searchBaseWidget != null && searchBaseWidget.getDn() != null
            && !searchBaseWidget.getDn().equals( search.getSearchBase() ) )
        {
            search.getSearchParameter().setSearchBase( searchBaseWidget.getDn() );
            searchModified = true;
            searchBaseWidget.saveDialogSettings();
        }
        if ( filterWidget != null && filterWidget.getFilter() != null )
        {
            if ( !filterWidget.getFilter().equals( search.getFilter() ) )
            {
                search.getSearchParameter().setFilter( filterWidget.getFilter() );
                searchModified = true;
            }
            filterWidget.saveDialogSettings();
        }

        if ( returningAttributesWidget != null )
        {
            if ( !Arrays.equals( returningAttributesWidget.getReturningAttributes(), search.getReturningAttributes() ) )
            {
                search.getSearchParameter().setReturningAttributes( returningAttributesWidget.getReturningAttributes() );
                searchModified = true;
            }
            returningAttributesWidget.saveDialogSettings();

            if ( returnAllAttributesButton != null || returnOperationalAttributesButton != null )
            {
                List<String> raList = new ArrayList<String>();
                raList.addAll( Arrays.asList( search.getReturningAttributes() ) );
                if ( returnAllAttributesButton != null )
                {
                    if ( returnAllAttributesButton.getSelection() )
                    {
                        raList.add( SchemaConstants.ALL_USER_ATTRIBUTES );
                    }
                    if ( returnAllAttributesButton.getSelection() != isActive( RETURN_ALLATTRIBUTES_CHECKED ) )
                    {
                        searchModified = true;
                    }
                }
                if ( returnOperationalAttributesButton != null )
                {
                    if ( returnOperationalAttributesButton.getSelection() )
                    {
                        Collection<AttributeType> opAtds = SchemaUtils
                            .getOperationalAttributeDescriptions( browserConnectionWidget.getBrowserConnection()
                                .getSchema() );
                        Collection<String> opAtdNames = SchemaUtils.getNames( opAtds );
                        raList.addAll( opAtdNames );
                        raList.add( SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES );
                    }
                    if ( returnOperationalAttributesButton.getSelection() != isActive( RETURN_OPERATIONALATTRIBUTES_CHECKED ) )
                    {
                        searchModified = true;
                    }
                }
                String[] returningAttributes = raList.toArray( new String[raList.size()] );
                search.getSearchParameter().setReturningAttributes( returningAttributes );
            }
        }

        if ( scopeWidget != null )
        {
            SearchScope scope = scopeWidget.getScope();
            if ( scope != search.getScope() )
            {
                search.getSearchParameter().setScope( scope );
                searchModified = true;
            }
        }
        if ( limitWidget != null )
        {
            int countLimit = limitWidget.getCountLimit();
            int timeLimit = limitWidget.getTimeLimit();
            if ( countLimit != search.getCountLimit() )
            {
                search.getSearchParameter().setCountLimit( countLimit );
                searchModified = true;
            }
            if ( timeLimit != search.getTimeLimit() )
            {
                search.getSearchParameter().setTimeLimit( timeLimit );
                searchModified = true;
            }
        }
        if ( aliasesDereferencingWidget != null )
        {
            Connection.AliasDereferencingMethod aliasesDereferencingMethod = aliasesDereferencingWidget
                .getAliasesDereferencingMethod();
            if ( aliasesDereferencingMethod != search.getAliasesDereferencingMethod() )
            {
                search.getSearchParameter().setAliasesDereferencingMethod( aliasesDereferencingMethod );
                searchModified = true;
            }
        }
        if ( referralsHandlingWidget != null )
        {
            Connection.ReferralHandlingMethod referralsHandlingMethod = referralsHandlingWidget
                .getReferralsHandlingMethod();
            if ( referralsHandlingMethod != search.getReferralsHandlingMethod() )
            {
                search.getSearchParameter().setReferralsHandlingMethod( referralsHandlingMethod );
                searchModified = true;
            }
        }
        if ( subentriesControlButton != null )
        {
            Set<Control> oldControls = new HashSet<>();
            oldControls.addAll( search.getSearchParameter().getControls() );

            search.getSearchParameter().getControls().clear();

            if ( manageDsaItControlButton.getSelection() )
            {
                search.getSearchParameter().getControls().add( Controls.MANAGEDSAIT_CONTROL );
            }
            if ( subentriesControlButton.getSelection() )
            {
                search.getSearchParameter().getControls().add( Controls.SUBENTRIES_CONTROL );
            }
            if ( pagedSearchControlButton.getSelection() )
            {
                int pageSize;
                try
                {
                    pageSize = Integer.valueOf( pagedSearchControlSizeText.getText() );
                }
                catch ( NumberFormatException e )
                {
                    pageSize = 100;
                }
                boolean isScrollMode = pagedSearchControlScrollButton.getSelection();
                PagedResults control = Controls.newPagedResultsControl(pageSize);
                search.getSearchParameter().getControls().add( control );
                search.getSearchParameter().setPagedSearchScrollMode( isScrollMode );
            }

            Set<Control> newControls = new HashSet<>();
            newControls.addAll( search.getSearchParameter().getControls() );

            if ( !oldControls.equals( newControls ) )
            {
                searchModified = true;
            }
        }

        return searchModified;
    }


    // ── Mon Mothma Gives the "Execute Mission" Order ──────────────────────────────────
    // All the briefing is done. Mon Mothma nods: "Execute." The search job is dispatched
    // asynchronously via a StudioBrowserJob so the UI stays responsive during the run.
    // Returns true if the mission launched successfully, false if no connection is set.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the given search asynchronously by submitting it to the background job
     * framework. Clears any existing results first so the view refreshes cleanly.
     * Returns {@code false} without doing anything if the search has no connection set —
     * you can't fly the mission without knowing where you're going.
     *
     * <p>For example — Mon Mothma gives the execute order:</p>
     * <pre>
     *   search.clearResults();
     *   new StudioBrowserJob( new SearchRunnable( search ) ).execute();
     *   return true;
     * </pre>
     *
     * @param search  The fully-configured {@link ISearch} to execute. Must not be null.
     * @return        {@code true} if the job was submitted; {@code false} if no
     *                connection is available and the search was not started.
     */
    public boolean performSearch( final ISearch search )
    {
        if ( search.getBrowserConnection() != null )
        {
            search.setSearchResults( null );
            new StudioBrowserJob( new SearchRunnable( new ISearch[]
                { search } ) ).execute();
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── Mon Mothma Does a Final Pre-Launch Check ──────────────────────────────────────
    // Before giving the execute order, Mon Mothma runs a quick sanity check:
    // Is there a connection? A valid search base? A search name? A valid filter?
    // If any critical parameter is missing or invalid, the mission can't launch.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if all mandatory search parameters are present and valid.
     * Called by the owning dialog to enable or disable the OK/Search/Finish button.
     * A search is invalid if: no connection is selected, the search base is invalid,
     * the name is empty (if shown), or the filter is syntactically wrong.
     *
     * <p>For example — Mon Mothma's pre-launch checklist:</p>
     * <pre>
     *   if ( no connection )      return false;
     *   if ( no valid base DN )   return false;
     *   if ( empty search name )  return false;
     *   if ( invalid filter )     return false;
     *   return true;
     * </pre>
     *
     * @return  {@code true} if the form is valid and ready to execute.
     */
    public boolean isValid()
    {
        if ( browserConnectionWidget != null && browserConnectionWidget.getBrowserConnection() == null )
        {
            return false;
        }
        if ( searchBaseWidget != null && searchBaseWidget.getDn() == null )
        {
            return false;
        }
        if ( searchNameText != null && "".equals( searchNameText.getText() ) ) //$NON-NLS-1$
        {
            return false;
        }
        if ( filterWidget != null && filterWidget.getFilter() == null )
        {
            return false;
        }
        if ( pagedSearchControlButton != null && pagedSearchControlButton.isEnabled()
            && "".equals( pagedSearchControlButton.getText() ) ) //$NON-NLS-1$
        {
            return false;
        }

        return true;
    }


    // ── Mon Mothma Reports What Is Still Missing ──────────────────────────────────────
    // If the pre-launch check fails, the dialog shows an error message in the status bar.
    // Mon Mothma reads through her checklist in order and returns the first problem she
    // finds — connection missing, then base, then name, then filter.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable error message explaining why {@link #isValid()} returned
     * {@code false}, or {@code null} if the form is valid. The owning dialog displays
     * this message in its status area. Problems are reported in priority order:
     * connection, search base, name, filter.
     *
     * <p>For example — Mon Mothma reads her checklist and finds the first gap:</p>
     * <pre>
     *   if ( no connection ) return "Please select a connection.";
     *   if ( no base DN )    return "Please enter a valid search base.";
     *   if ( empty name )    return "Please enter a search name.";
     *   if ( bad filter )    return "Please enter a valid filter.";
     *   return null;
     * </pre>
     *
     * @return  A localised error message, or {@code null} if the form is complete.
     */
    public String getErrorMessage()
    {
        if ( browserConnectionWidget != null && browserConnectionWidget.getBrowserConnection() == null )
        {
            return Messages.getString( "SearchPageWrapper.SelectConnection" ); //$NON-NLS-1$
        }
        if ( searchBaseWidget != null && searchBaseWidget.getDn() == null )
        {
            return Messages.getString( "SearchPageWrapper.EnterValidSearchBase" ); //$NON-NLS-1$
        }
        if ( searchNameText != null && "".equals( searchNameText.getText() ) ) //$NON-NLS-1$
        {
            return Messages.getString( "SearchPageWrapper.EnterSearchName" ); //$NON-NLS-1$
        }
        if ( filterWidget != null && filterWidget.getFilter() == null )
        {
            return Messages.getString( "SearchPageWrapper.EnterValidFilter" ); //$NON-NLS-1$
        }

        return null;
    }


    // ── Mon Mothma Powers Down or Re-Activates the Whole Briefing Room ────────────────
    // At the end of the debrief, Mon Mothma flips the main switch: every officer's
    // station dims (false) or comes back to life (true). Individual READONLY flags
    // still constrain specific stations even when the room is powered on.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the entire composite — every visible sub-widget — in one call.
     * A {@code false} value makes the whole form non-interactive (useful when, say,
     * a search is already running and we don't want the user changing parameters).
     * Individual sub-widgets that are READONLY in the style remain read-only even
     * when the outer call passes {@code true}.
     *
     * <p>For example — Mon Mothma powers the briefing room down:</p>
     * <pre>
     *   for each officer's station: station.setEnabled( false );
     * </pre>
     *
     * @param b  {@code true} to enable all visible controls; {@code false} to grey them.
     */
    public void setEnabled( boolean b )
    {
        if ( searchNameText != null )
        {
            searchNameLabel.setEnabled( b );
            searchNameText.setEnabled( b );
        }
        if ( browserConnectionWidget != null )
        {
            connectionLabel.setEnabled( b );
            browserConnectionWidget.setEnabled( b && !isActive( CONNECTION_READONLY ) );
        }
        if ( searchBaseWidget != null )
        {
            searchBaseLabel.setEnabled( b );
            searchBaseWidget.setEnabled( b && !isActive( SEARCHBASE_READONLY ) );
        }
        if ( filterWidget != null )
        {
            filterLabel.setEnabled( b );
            filterWidget.setEnabled( b && !isActive( FILTER_READONLY ) );
        }
        if ( returningAttributesWidget != null )
        {
            returningAttributesLabel.setEnabled( b );
            returningAttributesWidget.setEnabled( b && !isActive( RETURNINGATTRIBUTES_READONLY ) );
        }
        if ( returnDnButton != null )
        {
            returnDnButton.setEnabled( b );
        }
        if ( returnAllAttributesButton != null )
        {
            returnAllAttributesButton.setEnabled( b );
        }
        if ( returnOperationalAttributesButton != null )
        {
            returnOperationalAttributesButton.setEnabled( b );
        }
        if ( scopeWidget != null )
        {
            scopeWidget.setEnabled( b && !isActive( SCOPEOPTIONS_READONLY ) );
        }
        if ( limitWidget != null )
        {
            limitWidget.setEnabled( b && !isActive( LIMITOPTIONS_READONLY ) );
        }
        if ( aliasesDereferencingWidget != null )
        {
            aliasesDereferencingWidget.setEnabled( b && !isActive( ALIASOPTIONS_READONLY ) );
        }
        if ( referralsHandlingWidget != null )
        {
            referralsHandlingWidget.setEnabled( b && !isActive( REFERRALOPTIONS_READONLY ) );
        }
        if ( controlGroup != null )
        {
            controlGroup.setEnabled( b );
            manageDsaItControlButton.setEnabled( b );
            subentriesControlButton.setEnabled( b );
        }
    }

}
