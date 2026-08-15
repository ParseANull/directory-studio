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


import org.apache.directory.api.ldap.model.constants.LdapConstants;
import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.dialogs.FilterDialog;
import org.apache.directory.studio.ldapbrowser.common.filtereditor.FilterContentAssistProcessor;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: FilterWidget — R2-D2 Typing the LDAP Filter Into the Death Star Terminal ──
// In A New Hope, R2-D2 plugs into the Death Star's network terminal and starts keying
// in query strings to locate the tractor beam controls. He types partially, the system
// suggests completions, and he can also pop open the full schematic editor. When he
// pastes a long command string the suggestion popup dismisses itself so it doesn't
// interfere. Once he's happy with the query syntax, he submits it.
// This widget is that terminal interface for LDAP filter strings.
// ────────────────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for entering a valid LDAP search filter. An LDAP filter is a boolean
 * expression that narrows down which directory entries get returned — for example
 * {@code (&(objectClass=person)(cn=Leia*))} means "all person entries whose name
 * starts with Leia."
 * Think of this class as R2-D2 at the Death Star terminal: typing a query, receiving
 * auto-complete suggestions, and optionally opening the full filter editor dialog.
 *
 * <p>The widget contains:</p>
 * <ul>
 *   <li>A combo field with LDAP filter history and content-assist pop-up suggestions.</li>
 *   <li>An auto-edit strategy that auto-closes parentheses and brackets as R2 types.</li>
 *   <li>A "Filter Editor" button that opens the full {@link FilterDialog}.</li>
 * </ul>
 * Used by {@link SearchPageWrapper} as the filter row of the search form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterWidget extends AbstractWidget
{
    /** The filter combo. */
    private Combo filterCombo;

    /** The filter content proposal adapter */
    private ExtendedContentAssistCommandAdapter filterCPA;

    /** The button to open the filter editor. */
    private Button filterEditorButton;

    /** The content assist processor. */
    private FilterContentAssistProcessor contentAssistProcessor;

    /** The connection. */
    private IBrowserConnection browserConnection;

    /** The inital filter. */
    private String initalFilter;

    /** The filter parser. */
    private LdapFilterParser parser;


    // ── R2 Arrives at the Terminal With a Pre-Typed Query String ─────────────────────
    // R2 already has the access code loaded in his memory banks. He walks up to the
    // terminal with the query ready to paste in rather than typing from scratch.
    // We store the initial filter so createWidget() can pre-populate the combo field.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget pre-loaded with a specific LDAP filter string.
     * Use this when opening a search dialog for an existing saved search that already
     * has a filter — the combo field will show it immediately on render.
     *
     * <p>For example — R2 arrives with the tractor beam query ready:</p>
     * <pre>
     *   preloadedQuery = "(objectClass=tractorBeamControl)";
     *   R2.memory.set( preloadedQuery );
     * </pre>
     *
     * @param initalFilter  The LDAP filter string to pre-populate, e.g.
     *                      {@code "(objectClass=*)"}.
     */
    public FilterWidget( String initalFilter )
    {
        this.initalFilter = initalFilter;
    }


    // ── R2 Shows Up at a Brand-New Terminal With No Query Ready ──────────────────────
    // R2 has just been powered on in a completely new sector. No connection established,
    // no query loaded — he'll figure it out once he's plugged in.
    // We start blank; createWidget() will default the filter to (objectClass=*).
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget with no pre-selected connection and no initial filter.
     * The combo will default to {@code (objectClass=*)} — LDAP's "match everything"
     * filter — which is a safe starting point for most searches.
     *
     * <p>For example — R2 boots up with a clean slate:</p>
     * <pre>
     *   R2.connection = null;
     *   R2.query = null; // will default to (objectClass=*)
     * </pre>
     */
    public FilterWidget()
    {
        this.browserConnection = null;
        this.initalFilter = null;
    }


    // ── R2 Wires Up the Full Terminal Interface ───────────────────────────────────────
    // R2 plugs the combo field into the terminal, attaches the auto-complete suggestion
    // engine (content assist), installs the auto-edit strategy for bracket completion,
    // and adds the "Open Schematic Editor" button. He also loads previous queries from
    // the history log and seeds the field with either the stored query or the default.
    // He makes sure pasting a long string shuts the suggestion popup first.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and lays out the SWT controls inside the given parent composite.
     * Creates the filter combo with LDAP content-assist, an auto-edit strategy that
     * auto-closes parentheses, a filter history drop-down, and the Filter Editor button.
     * Call this exactly once after construction.
     *
     * <p>For example — R2 wires up his terminal interface:</p>
     * <pre>
     *   filterCombo = new ComboWithHistory( parent );
     *   filterCombo.addContentAssist( ldapProcessor );
     *   filterCombo.addAutoEdit( bracketCloser );
     *   filterEditorButton = new Button( "Open Schematic Editor" );
     * </pre>
     *
     * @param parent  The SWT composite that will host the filter controls.
     *                Must already have an appropriate layout.
     */
    public void createWidget( final Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        gd.widthHint = 30;
        composite.setLayoutData( gd );

        // filter combo with field decoration and content proposal
        filterCombo = BaseWidgetUtils.createCombo( composite, new String[0], -1, 1 );
        filterCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                notifyListeners();
            }
        } );
        filterCombo.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                // close proposal popup when paste a string

                // either with 3rd mouse button (linux)
                if ( !filterCombo.getText().equals( e.text ) && e.character == 0 && e.start == e.end )
                {
                    filterCPA.closeProposalPopup();
                }

                // or with ctrl+v / command+v
                if ( !filterCombo.getText().equals( e.text ) && e.stateMask == SWT.MOD1 && e.start == e.end )
                {
                    filterCPA.closeProposalPopup();
                }
            }
        } );
        parser = new LdapFilterParser();
        contentAssistProcessor = new FilterContentAssistProcessor( parser );
        filterCPA = new ExtendedContentAssistCommandAdapter( filterCombo, new ComboContentAdapter(),
            contentAssistProcessor, null, null, true );

        // auto edit strategy
        new FilterWidgetAutoEditStrategyAdapter( filterCombo, parser );

        // Filter editor button
        filterEditorButton = BaseWidgetUtils.createButton( parent, Messages
            .getString( "FilterWidget.FilterEditorButton" ), 1 ); //$NON-NLS-1$
        filterEditorButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( browserConnection != null )
                {
                    FilterDialog dialog = new FilterDialog( parent.getShell(), Messages
                        .getString( "FilterWidget.FilterEditor" ), filterCombo.getText(), //$NON-NLS-1$
                        browserConnection );
                    dialog.open();
                    String filter = dialog.getFilter();
                    if ( filter != null )
                    {
                        filterCombo.setText( filter );
                    }
                }
            }
        } );

        // filter history
        String[] history = HistoryUtils.load( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_SEARCH_FILTER_HISTORY );
        filterCombo.setItems( history );

        // initial values
        filterCombo.setText( initalFilter == null ? LdapConstants.OBJECT_CLASS_STAR : initalFilter ); //$NON-NLS-1$
    }


    // ── R2 Validates and Returns the Current Query String ────────────────────────────
    // R2 runs the typed string through the Death Star's syntax checker. If the filter
    // is well-formed, he hands it back to the mission team. If it's malformed — an
    // unclosed parenthesis, a stray asterisk — he beeps an error and returns nothing.
    // We parse the combo text and return null if the model reports it is invalid.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the current combo text as an LDAP filter and returns it if valid.
     * Returns an empty string {@code ""} if the field is empty, and {@code null}
     * if the text is non-empty but syntactically invalid. The search form checks for
     * null to block the user from submitting a broken filter.
     *
     * <p>For example — R2 validates before submitting:</p>
     * <pre>
     *   syntax = deathStarSyntaxChecker.parse( typedString );
     *   return syntax.valid ? typedString : null;
     * </pre>
     *
     * @return  The filter string if it is valid, {@code ""} if empty, or {@code null}
     *          if it fails LDAP filter syntax validation.
     */
    public String getFilter()
    {
        if ( "".equals( filterCombo.getText() ) ) //$NON-NLS-1$
        {
            return ""; //$NON-NLS-1$
        }
        parser.parse( filterCombo.getText() );
        return parser.getModel().isValid() ? filterCombo.getText() : null;
    }


    // ── R2 Overwrites the Terminal With a New Query ───────────────────────────────────
    // The Rebel command centre beams R2 a revised query mid-mission. R2 clears the
    // terminal and types the new string — or, if the interface hasn't been built yet,
    // he just updates his memory banks for when it is built.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the filter string displayed in the combo field. If the widget controls have
     * not been created yet (i.e., {@link #createWidget} hasn't been called), the value
     * is stored and applied later. Otherwise the combo text is updated immediately.
     *
     * <p>For example — R2 replaces his query mid-mission:</p>
     * <pre>
     *   R2.query = "(cn=HanSolo)";
     *   terminalDisplay.setText( R2.query );
     * </pre>
     *
     * @param filter  The LDAP filter string to display. May be any string; invalid ones
     *                will cause {@link #getFilter()} to return null until fixed.
     */
    public void setFilter( String filter )
    {
        if ( filterCombo == null )
        {
            initalFilter = filter;
        }
        else
        {
            filterCombo.setText( filter );
        }
    }


    // ── R2 Switches to a New Server's Schema for Suggestions ─────────────────────────
    // When R2 re-plugs into a different Death Star terminal, the auto-complete engine
    // needs to learn the new terminal's attribute vocabulary. He hands the new schema
    // to the suggestion engine and reconfigures which keypresses trigger the pop-up.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the LDAP server connection, which in turn refreshes the schema used by
     * the content-assist suggestion engine. Call this whenever the user picks a different
     * server in the connection widget above — the attribute names that show up in
     * auto-complete are server-specific.
     *
     * <p>For example — R2 plugs into a new terminal and syncs the vocabulary:</p>
     * <pre>
     *   R2.connection = newTerminal;
     *   suggestions.setVocabulary( newTerminal.schema );
     *   popup.setActivationKeys( newVocabulary.firstChars );
     * </pre>
     *
     * @param browserConnection  The new LDAP server connection whose schema drives
     *                           filter attribute-type suggestions. May be null to clear.
     */
    public void setBrowserConnection( IBrowserConnection browserConnection )
    {
        if ( this.browserConnection != browserConnection )
        {
            this.browserConnection = browserConnection;

            if ( filterCombo != null )
            {
                contentAssistProcessor.setSchema( browserConnection == null ? null : browserConnection.getSchema() );
                filterCPA.setAutoActivationCharacters( contentAssistProcessor
                    .getCompletionProposalAutoActivationCharacters() );
            }
        }
    }


    // ── R2 Commits His Query History to Long-Term Storage ────────────────────────────
    // After the mission succeeds, R2 logs the filter string he used to his permanent
    // memory so it shows up in the history drop-down next time he's at a terminal.
    // We persist the current combo text to the dialog-settings history store.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current filter string to the dialog history so it appears in the combo's
     * drop-down on future openings. Call this when the user confirms the dialog.
     *
     * <p>For example — R2 logs the successful query to his memory banks:</p>
     * <pre>
     *   missionLog.save( currentQueryString );
     * </pre>
     */
    public void saveDialogSettings()
    {
        HistoryUtils.save( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_SEARCH_FILTER_HISTORY, filterCombo.getText() );
    }


    // ── R2 Positions His Cursor at the Terminal ───────────────────────────────────────
    // R2 rolls up to the terminal and the cursor blinks, ready for input.
    // (This method intentionally does nothing right now — see the commented line below.)
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Requests keyboard focus for the filter combo. Currently a no-op — the focus
     * line is commented out — but the method is kept for API compatibility and in case
     * the behaviour needs to be restored in a future release.
     *
     * <p>For example — R2 moves to the terminal and waits:</p>
     * <pre>
     *   terminalCursor.blink(); // cursor placed, awaiting input
     * </pre>
     */
    public void setFocus()
    {
        // filterCombo.setFocus();
    }


    // ── R2 Powers Down His Terminal Controls ─────────────────────────────────────────
    // When the Death Star's power to R2's sector is cut, the combo field greys out
    // and the Filter Editor button goes dark — no input accepted.
    // We propagate the enabled state to both controls.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the widget's interactive controls — the combo field and the
     * Filter Editor button — in one call. Pass {@code false} to grey them out when the
     * form should be read-only.
     *
     * <p>For example — R2's terminal goes dark:</p>
     * <pre>
     *   filterCombo.setEnabled( false );
     *   filterEditorButton.setEnabled( false );
     * </pre>
     *
     * @param b  {@code true} to enable input; {@code false} to disable it.
     */
    public void setEnabled( boolean b )
    {
        filterCombo.setEnabled( b );
        filterEditorButton.setEnabled( b );
    }

}
