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


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


// ── CLASS: AliasesDereferencingWidget — C-3PO Decoding Alias Names on Cloud City ──
// C-3PO on Cloud City is trying to figure out which name to use for Lando's contact:
// "Calrissian" or "Baron Administrator"? Both point to the same person — one is an alias.
// He must decide whether to resolve aliases while finding the base entry, during the
// search itself, or in both phases — exactly what this widget controls.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * A small SWT panel that lets the user choose how the LDAP client should handle
 * alias entries it encounters during a search. Aliases in LDAP are a bit like
 * symbolic links — an entry that just points somewhere else.
 * Think of this class as C-3PO on Cloud City sorting out which contact name to
 * actually follow when two names point to the same person.
 *
 * <p>The panel contains a labeled group with two checkboxes:</p>
 * <ul>
 *   <li><b>Finding base DN</b> — dereference aliases while locating the search base</li>
 *   <li><b>Search</b> — dereference aliases encountered during the actual search</li>
 * </ul>
 * Both checked = {@code ALWAYS}; neither = {@code NEVER}.
 * Used by {@link SearchPageWrapper} as part of the full search-options panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AliasesDereferencingWidget extends AbstractWidget
{

    /** The initial aliases dereferencing method */
    private Connection.AliasDereferencingMethod initialAliasesDereferencingMethod;

    /** The group. */
    private Group group;

    /** The finding button. */
    private Button findingButton;

    /** The search button. */
    private Button searchButton;


    // ── 3PO Receives His Orders Before Cloud City ────────────────────────────────────
    // Lando hands C-3PO a briefing card specifying exactly which alias policy to use.
    // 3PO memorises it so he is ready the moment the conversation starts.
    // We store the caller's chosen dereferencing method so createWidget() can apply it.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new widget pre-configured with a specific alias dereferencing method.
     * Use this when the caller already knows what the setting should be — for example,
     * when opening the properties dialog for an existing saved search.
     *
     * <p>For example — C-3PO receives his initial briefing card:</p>
     * <pre>
     *   briefingCard = AliasDereferencingMethod.FINDING;
     *   C3PO.memorise( briefingCard );
     * </pre>
     *
     * @param initialAliasesDereferencingMethod  The dereferencing policy to show when the
     *                                           widget is first rendered. One of ALWAYS,
     *                                           FINDING, SEARCH, or NEVER.
     */
    public AliasesDereferencingWidget( Connection.AliasDereferencingMethod initialAliasesDereferencingMethod )
    {
        this.initialAliasesDereferencingMethod = initialAliasesDereferencingMethod;
    }


    // ── 3PO Shows Up Without a Briefing Card ─────────────────────────────────────────
    // C-3PO arrives at the Cloud City meeting without any prior instructions.
    // He falls back to the safest default: resolve aliases in every situation.
    // We default to ALWAYS so new searches work sensibly right out of the box.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new widget with the default alias dereferencing method of
     * {@link Connection.AliasDereferencingMethod#ALWAYS}.
     * Good for fresh search dialogs where no previous setting exists yet.
     *
     * <p>For example — 3PO defaults to resolving all aliases when nobody told him otherwise:</p>
     * <pre>
     *   C3PO.setPolicy( AliasDereferencingMethod.ALWAYS ); // safe default
     * </pre>
     */
    public AliasesDereferencingWidget()
    {
        this.initialAliasesDereferencingMethod = Connection.AliasDereferencingMethod.ALWAYS;
    }


    // ── 3PO Sets Up His Translation Station ──────────────────────────────────────────
    // C-3PO arranges his dossiers on the Cloud City table: two folders, one labeled
    // "Finding base DN" and one "Search", so Lando can tick whichever phases he wants.
    // We build the SWT group with two checkboxes and attach listeners that fire
    // notifyListeners() whenever a checkbox changes so the parent form can react.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and lays out the SWT controls inside the given parent composite.
     * Call this exactly once, after constructing the widget and before showing the dialog.
     * It creates a labeled group containing "Finding base DN" and "Search" checkboxes,
     * then seeds them from the initial dereferencing method passed at construction time.
     *
     * <p>For example — 3PO prepares two folders on the table:</p>
     * <pre>
     *   group = new Group( parent, "Aliases Dereferencing" );
     *   findingButton = new CheckBox( "Finding base DN" );
     *   searchButton  = new CheckBox( "Search" );
     *   setAliasesDereferencingMethod( initialMethod );
     * </pre>
     *
     * @param parent  The SWT composite that will contain this widget's controls.
     *                Must already have an appropriate layout attached.
     */
    public void createWidget( Composite parent )
    {

        group = BaseWidgetUtils.createGroup( parent, Messages
            .getString( "AliasesDereferencingWidget.AliasesDereferencing" ), 1 ); //$NON-NLS-1$
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group, 1, 1 );

        findingButton = BaseWidgetUtils.createCheckbox( groupComposite, Messages
            .getString( "AliasesDereferencingWidget.FindingBaseDN" ), 1 ); //$NON-NLS-1$
        findingButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                notifyListeners();
            }
        } );

        searchButton = BaseWidgetUtils.createCheckbox( groupComposite, Messages
            .getString( "AliasesDereferencingWidget.Search" ), 1 ); //$NON-NLS-1$
        searchButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                notifyListeners();
            }
        } );

        setAliasesDereferencingMethod( initialAliasesDereferencingMethod );
    }


    // ── 3PO Updates His Folders Mid-Meeting ──────────────────────────────────────────
    // Lando hands C-3PO a revised briefing mid-session: "Ignore aliases during search."
    // 3PO opens the right folder, ticks or unticks the boxes, and sits back down.
    // We translate the enum value into the correct checkbox states so the UI matches.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the alias dereferencing mode and updates the checkbox UI
     * to match. Call this when loading a saved search into the dialog or when
     * another part of the form forces a specific value.
     *
     * <p>For example — 3PO revises his notes mid-meeting:</p>
     * <pre>
     *   method = AliasDereferencingMethod.FINDING;
     *   findingCheckbox.setSelected( true );
     *   searchCheckbox.setSelected( false );
     * </pre>
     *
     * @param aliasesDereferencingMethod  The policy to apply. ALWAYS ticks both boxes;
     *                                   FINDING ticks only the first; SEARCH ticks only
     *                                   the second; NEVER leaves both unchecked.
     */
    public void setAliasesDereferencingMethod( Connection.AliasDereferencingMethod aliasesDereferencingMethod )
    {
        initialAliasesDereferencingMethod = aliasesDereferencingMethod;
        findingButton.setSelection( initialAliasesDereferencingMethod == Connection.AliasDereferencingMethod.FINDING
            || initialAliasesDereferencingMethod == Connection.AliasDereferencingMethod.ALWAYS );
        searchButton.setSelection( initialAliasesDereferencingMethod == Connection.AliasDereferencingMethod.SEARCH
            || initialAliasesDereferencingMethod == Connection.AliasDereferencingMethod.ALWAYS );
    }


    // ── 3PO Reports Which Folders Are Ticked ─────────────────────────────────────────
    // Lando asks: "So which alias phases did we agree on?" C-3PO checks his two folders
    // and reports back: both ticked, just Finding, just Search, or neither.
    // We read the checkbox states and map them back to the correct enum constant.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current checkbox states and returns the matching
     * {@link Connection.AliasDereferencingMethod} enum value.
     * The search code calls this to find out what to put in the LDAP search request.
     *
     * <p>For example — 3PO checks both folders and declares the result:</p>
     * <pre>
     *   if ( findingTicked and searchTicked ) return ALWAYS;
     *   if ( findingTicked )                 return FINDING;
     *   if ( searchTicked  )                 return SEARCH;
     *   else                                 return NEVER;
     * </pre>
     *
     * @return  The dereferencing method that reflects what the user has ticked.
     *          Never null.
     */
    public Connection.AliasDereferencingMethod getAliasesDereferencingMethod()
    {
        if ( findingButton.getSelection() && searchButton.getSelection() )
        {
            return Connection.AliasDereferencingMethod.ALWAYS;
        }
        else if ( findingButton.getSelection() )
        {
            return Connection.AliasDereferencingMethod.FINDING;
        }
        else if ( searchButton.getSelection() )
        {
            return Connection.AliasDereferencingMethod.SEARCH;
        }
        else
        {
            return Connection.AliasDereferencingMethod.NEVER;
        }
    }


    // ── 3PO Packs Up or Unpacks His Folders ──────────────────────────────────────────
    // When the Empire locks down Cloud City, 3PO can no longer access his files — the
    // administrator disables everything. When the coast is clear, it all opens up again.
    // We propagate the enabled/disabled state to the group container and both checkboxes.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the entire widget — the surrounding group box and both
     * checkboxes — in one call. Useful when, say, the connection field is empty and
     * we don't want the user touching the search options yet.
     *
     * <p>For example — 3PO locks all his folders when the Empire occupies Cloud City:</p>
     * <pre>
     *   group.setEnabled( false );
     *   findingFolder.setEnabled( false );
     *   searchFolder.setEnabled( false );
     * </pre>
     *
     * @param b  Pass {@code true} to enable all controls, {@code false} to grey them out.
     */
    public void setEnabled( boolean b )
    {
        group.setEnabled( b );
        findingButton.setEnabled( b );
        searchButton.setEnabled( b );
    }

}
