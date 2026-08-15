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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.properties;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.SearchPageWrapper;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: SearchPropertyPage — R2-D2 PLUGGING INTO THE DEATH STAR COMPUTER ──
// R2-D2 rolls up to the Death Star computer terminal, jacks in his interface arm,
// and immediately starts reading configuration data — base DN, filter, scope,
// size limit.  He can't change which Death Star he's connected to (CONNECTION_READONLY),
// but he can tweak every other search parameter and re-run the query.
// When Artoo validates a data port and confirms the signal is clean, the OK
// button lights up.  When he hits commit, the updated search parameters are
// persisted and the search is re-executed to fetch fresh results.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse property page for viewing and editing an {@link ISearch} configuration.
 * Wraps a {@link SearchPageWrapper} in read-only-connection mode, meaning the
 * target LDAP connection cannot be changed from this page — all other search
 * parameters (base DN, filter, scope, size/time limits, return attributes) are
 * editable.
 * On OK, any changes are saved back to the {@link ISearch} object, a
 * {@link SearchUpdateEvent} fires to persist the new parameters, and the search
 * re-executes.
 * Think of this page as R2-D2 plugging into the Death Star terminal — reading
 * and patching the mission parameters before extracting the data.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchPropertyPage extends PropertyPage implements IWorkbenchPropertyPage, WidgetModifyListener
{

    /** The search. */
    private ISearch search;

    /** The search page wrapper. */
    private SearchPageWrapper spw;


    // ── R2-D2 POWERS UP HIS INTERFACE ARM ─────────────────────────────────────
    // Artoo initialises before approaching the terminal — no Apply button needed
    // here because changes are saved all-at-once when the user hits OK.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the property page and suppresses the Default and Apply buttons.
     * Search parameter edits are committed together when the user clicks OK,
     * so we don't need incremental Apply semantics here.
     *
     * <p>For example — Artoo powers up, ready to plug in:</p>
     * <pre>
     *   noDefaultAndApplyButton() → one-shot commit on OK, no incremental Apply
     * </pre>
     */
    public SearchPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── R2-D2 DISCONNECTS CLEANLY FROM THE TERMINAL ───────────────────────────
    // When the property dialog closes we need to remove Artoo's modify listener
    // from the search page wrapper, otherwise events fired after the dialog is
    // gone could try to update disposed widgets and crash.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes this page from the {@link SearchPageWrapper}'s widget-modify listener
     * list before delegating to the superclass {@code dispose()}.
     * This prevents stale event callbacks from reaching the disposed widgets.
     *
     * <p>For example — Artoo retracts his interface arm and rolls away:</p>
     * <pre>
     *   dialog closed → spw.removeWidgetModifyListener(this) → super.dispose()
     * </pre>
     */
    public void dispose()
    {
        spw.removeWidgetModifyListener( this );
        super.dispose();
    }


    // ── R2-D2 JACKS IN AND READS THE MISSION PARAMETERS ──────────────────────
    // Artoo plugs into the terminal, identifies the target search, loads all of
    // its current parameters (base DN, filter, scope, etc.) into the display,
    // then wires himself up to get notified whenever a parameter changes so he
    // can update the valid/invalid signal immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the property page UI using a {@link SearchPageWrapper} in
     * {@link SearchPageWrapper#CONNECTION_READONLY} mode (the connection field
     * is shown but not editable), populates it from the current {@link ISearch},
     * registers this page as a {@link WidgetModifyListener}, and fires an initial
     * validation pass.
     *
     * <p>For example — Artoo plugs in and reads the mission parameters:</p>
     * <pre>
     *   ISearch element → spw.loadFromSearch(search) populates all fields
     *   any field change → widgetModified fires → OK button enabled/disabled
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's property dialog.
     * @return        The top-level composite we built.
     */
    protected Control createContents( Composite parent )
    {
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_search_properties" ); //$NON-NLS-1$ //$NON-NLS-2$

        // declare search
        ISearch search = ( ISearch ) getElement();
        if ( search != null )
        {
            this.search = search;
        }
        else
        {
            this.search = new Search();
        }

        super.setMessage( Messages.getString( "SearchPropertyPage.Search" ) + Utils.shorten( search.getName(), 30 ) ); //$NON-NLS-1$

        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        spw = new SearchPageWrapper( SearchPageWrapper.CONNECTION_READONLY );
        spw.createContents( composite );
        spw.loadFromSearch( search );
        spw.addWidgetModifyListener( this );

        widgetModified( new WidgetModifyEvent( this ) );

        return composite;
    }


    // ── R2-D2 TRANSMITS THE UPDATED MISSION PARAMETERS ────────────────────────
    // Artoo has finished editing; now he saves everything to the search object,
    // fires a SearchUpdateEvent so the rest of the application knows the search
    // parameters changed, and then re-runs the search to pull fresh results.
    // If nothing changed, he simply returns true and lets the dialog close quietly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves any modified search parameters back to the {@link ISearch} via
     * {@link SearchPageWrapper#saveToSearch(ISearch)}.
     * If any parameter changed and the search has a live connection, fires a
     * {@link SearchUpdateEvent} and re-runs the search via
     * {@link SearchPageWrapper#performSearch(ISearch)}.
     * Returns {@code true} if the dialog may close, {@code false} if the
     * re-run fails validation (which is unlikely given the prior {@code isValid()} check).
     *
     * <p>For example — Artoo transmits the updated mission parameters:</p>
     * <pre>
     *   filter changed → spw.saveToSearch(search) → SearchUpdateEvent fired →
     *   spw.performSearch(search) → fresh results loaded → dialog closes
     *   nothing changed → returns true immediately
     * </pre>
     *
     * @return  {@code true} if the dialog can close; result of
     *          {@link SearchPageWrapper#performSearch(ISearch)} when a re-run occurs.
     */
    public boolean performOk()
    {
        boolean modified = spw.saveToSearch( search );
        if ( modified && search.getBrowserConnection() != null )
        {
            // send update event to force saving of new search parameters.
            EventRegistry.fireSearchUpdated( new SearchUpdateEvent( search,
                SearchUpdateEvent.EventDetail.SEARCH_PARAMETER_UPDATED ), this );

            return spw.performSearch( search );
        }

        return true;
    }


    // ── R2-D2 CHECKS THE DATA PORT SIGNAL ─────────────────────────────────────
    // Every time a parameter changes, Artoo checks whether the current settings
    // constitute a valid, executable search query.  If the signal is clean he
    // illuminates the OK button; if there's an error he relays the error message
    // and dims the button.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates valid/invalid state and the current error message from the
     * {@link SearchPageWrapper} to the property page framework.
     * Called by the wrapper whenever any search parameter widget changes value.
     *
     * <p>For example — Artoo checks the data port signal:</p>
     * <pre>
     *   field change → widgetModified fires →
     *   setValid(spw.isValid()) → setErrorMessage(spw.getErrorMessage())
     * </pre>
     *
     * @param event  The widget-modify event from the {@link SearchPageWrapper};
     *               its source is the specific widget that changed.
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        setValid( spw.isValid() );
        setErrorMessage( spw.getErrorMessage() );
    }

}
