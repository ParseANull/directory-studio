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
import java.util.Collection;
import java.util.List;

import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.DialogContentAssistant;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ReturningAttributesWidget — R2 Choosing Which Data Columns to Pull ────────
// At the Death Star terminal in A New Hope, R2-D2 doesn't want everything — he just
// needs specific data columns: maybe the tractor-beam status field and the cell roster.
// He types the column names into the query, separated by commas. The terminal's assistant
// (C-3PO, naturally) whispers completions as he types. Previous queries are available
// from a history drop-down so R2 doesn't have to retype them every mission.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for specifying which LDAP attribute types should be included in the
 * search results — the "returning attributes" list. An LDAP entry can have dozens of
 * attributes; you usually only want a few. An empty list or {@code *} means "all user
 * attributes"; {@code +} means "operational attributes" (internal server metadata).
 * Think of this class as R2-D2 typing column names into the Death Star terminal,
 * with C-3PO ({@link ReturningAttributesContentAssistProcessor}) whispering suggestions.
 *
 * <p>The widget is a single combo field with:</p>
 * <ul>
 *   <li>Content-assist pop-up offering attribute names from the server's schema.</li>
 *   <li>A history drop-down showing previously used returning-attributes strings.</li>
 * </ul>
 * Used by {@link SearchPageWrapper} in the search form's attributes row.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReturningAttributesWidget extends AbstractWidget
{

    /** The returning attributes combo. */
    private Combo returningAttributesCombo;

    /** The content assist processor. */
    private ReturningAttributesContentAssistProcessor contentAssistProcessor;

    /** The connection. */
    private IBrowserConnection browserConnection;

    /** The initial returning attributes. */
    private String[] initialReturningAttributes;


    // ── R2 Arrives With a Pre-Selected Column List and a Live Terminal ─────────────────
    // R2 already knows which columns he needs: "cn, mail, uid". He plugs into the
    // specified terminal and pre-loads the column list so the field is ready immediately.
    // We store both connection and the initial attribute array for createWidget() to use.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget pre-loaded with a connection and a list of attribute names.
     * Use this when editing an existing saved search that already specifies which
     * attributes should be returned.
     *
     * <p>For example — R2 arrives with his column list already loaded:</p>
     * <pre>
     *   R2.terminal = deathStarMainframe;
     *   R2.columns  = [ "cn", "mail", "uid" ];
     * </pre>
     *
     * @param browserConnection          The LDAP server connection, used to load the
     *                                   schema for content-assist suggestions.
     * @param initialReturningAttributes The attribute names to show when the widget
     *                                   first renders. May be null or empty.
     */
    public ReturningAttributesWidget( IBrowserConnection browserConnection, String[] initialReturningAttributes )
    {
        this.browserConnection = browserConnection;
        this.initialReturningAttributes = initialReturningAttributes;
    }


    // ── R2 Boots Up Without a Column List or a Terminal Connection ────────────────────
    // Clean slate: no terminal connection, no predefined columns. The user (or a later
    // setInput call) will supply both before the search runs.
    // We initialise both fields to null; setBrowserConnection() can fill them in later.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a blank widget with no connection and no initial attribute list.
     * The combo will start empty; the user types the attributes they want, or
     * picks from history. Useful for brand-new search dialogs.
     *
     * <p>For example — R2 starts up with no mission parameters:</p>
     * <pre>
     *   R2.terminal = null;
     *   R2.columns  = null;
     * </pre>
     */
    public ReturningAttributesWidget()
    {
        this.browserConnection = null;
        this.initialReturningAttributes = null;
    }


    // ── R2 Sets Up the Column-Selection Terminal Interface ────────────────────────────
    // R2 installs the combo field (with history drop-down), wires C-3PO's auto-complete
    // engine to it, loads the history of past column queries, and seeds the field with
    // either the pre-selected columns or blank. He also listens for every change so the
    // parent form can validate itself.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and lays out the SWT combo and content-assist plumbing inside the given
     * parent composite. Also installs the history drop-down and seeds the field with
     * the initial returning-attributes value. Call this exactly once after construction.
     *
     * <p>For example — R2 sets up his column-selection terminal:</p>
     * <pre>
     *   combo = new ComboWithHistory( parent );
     *   combo.addContentAssist( C3PO.processor );
     *   combo.setText( Utils.arrayToString( initialColumns ) );
     * </pre>
     *
     * @param parent  The SWT composite that will host the combo field. Must have an
     *                appropriate grid layout already applied.
     */
    public void createWidget( Composite parent )
    {
        // Combo
        returningAttributesCombo = BaseWidgetUtils.createCombo( parent, new String[0], -1, 1 );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        gd.widthHint = 200;
        returningAttributesCombo.setLayoutData( gd );

        // Content assist
        contentAssistProcessor = new ReturningAttributesContentAssistProcessor( null );
        DialogContentAssistant raca = new DialogContentAssistant();
        raca.enableAutoInsert( true );
        raca.enableAutoActivation( true );
        raca.setAutoActivationDelay( 500 );
        raca.setContentAssistProcessor( contentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE );
        raca.install( returningAttributesCombo );

        // History
        String[] history = HistoryUtils.load( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_RETURNING_ATTRIBUTES_HISTORY );
        for ( int i = 0; i < history.length; i++ )
        {
            history[i] = Utils.arrayToString( stringToArray( history[i] ) );
        }
        returningAttributesCombo.setItems( history );
        returningAttributesCombo.setText( Utils.arrayToString( this.initialReturningAttributes ) );

        returningAttributesCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                notifyListeners();
            }
        } );

        setBrowserConnection( browserConnection );
    }


    // ── R2 Syncs C-3PO's Vocabulary to the New Terminal's Schema ─────────────────────
    // R2 plugs into a new Death Star terminal. He tells C-3PO: "Load the attribute
    // vocabulary for this section — 'cn', 'uid', '@person', '*', and '+'."
    // C-3PO updates his suggestion list and R2 gets fresh completions for the new server.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the active LDAP connection and refreshes the content-assist proposal list
     * from the new server's schema. Call this whenever the selected connection changes
     * so that attribute-name suggestions stay relevant.
     * If the connection is null, the proposal list is cleared.
     *
     * <p>For example — R2 syncs 3PO to the new terminal's vocabulary:</p>
     * <pre>
     *   C3PO.vocabulary = newTerminal.schema.attributeNames()
     *                   + newTerminal.schema.objectClassNames().map( n -> "@" + n )
     *                   + [ "+", "*" ];
     * </pre>
     *
     * @param browserConnection  The new LDAP server connection whose schema provides
     *                           suggestions. May be null to clear the proposal list.
     */
    public void setBrowserConnection( IBrowserConnection browserConnection )
    {
        this.browserConnection = browserConnection;

        List<String> proposals = new ArrayList<String>();
        if ( browserConnection != null )
        {
            // add attribute types
            proposals.addAll( SchemaUtils.getNames( browserConnection.getSchema().getAttributeTypeDescriptions() ) );

            // add @<object class names>
            Collection<String> ocNames = SchemaUtils.getNames( browserConnection.getSchema()
                .getObjectClassDescriptions() );
            for ( String ocName : ocNames )
            {
                proposals.add( "@" + ocName ); //$NON-NLS-1$
            }

            proposals.add( "+" ); //$NON-NLS-1$
            proposals.add( "*" ); //$NON-NLS-1$
        }

        contentAssistProcessor.setProposals( proposals );
    }


    // ── R2 Loads a New Column List Into the Terminal ──────────────────────────────────
    // Mid-mission, Leia calls in: "We also need 'telephoneNumber'." R2 updates the
    // combo field with the revised column list so the next search picks it up.
    // We update the stored array and push the formatted string into the combo.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the returning-attributes list and updates the combo field.
     * Use this when loading a saved search into the form to restore its attribute list.
     *
     * <p>For example — R2 updates his column list mid-mission:</p>
     * <pre>
     *   R2.columns = [ "cn", "mail", "telephoneNumber" ];
     *   combo.setText( "cn, mail, telephoneNumber" );
     * </pre>
     *
     * @param initialReturningAttributes  The attribute names to display. Null is treated
     *                                    as an empty list (the combo is cleared).
     */
    public void setInitialReturningAttributes( String[] initialReturningAttributes )
    {
        this.initialReturningAttributes = initialReturningAttributes;
        returningAttributesCombo.setText( Utils.arrayToString( initialReturningAttributes ) );
    }


    // ── R2 Powers Down His Column-Selection Input ─────────────────────────────────────
    // When the terminal is locked by the Empire, R2 can no longer change his column
    // selection — the combo greys out. When access is restored it comes back to life.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the combo field. When disabled, the user cannot edit the
     * attribute list. Useful when the form is in read-only or view-only mode.
     *
     * <p>For example — R2's column selector goes read-only:</p>
     * <pre>
     *   columnsCombo.setEnabled( false );
     * </pre>
     *
     * @param b  {@code true} to allow editing; {@code false} to grey the combo out.
     */
    public void setEnabled( boolean b )
    {
        this.returningAttributesCombo.setEnabled( b );
    }


    // ── R2 Reads the Current Column Selection ────────────────────────────────────────
    // "Which columns did we request, R2?" He reads the combo text, splits it on commas
    // and spaces (keeping only valid attribute characters), and returns the array.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current returning-attributes list as an array of individual attribute
     * names, parsed from the combo text. Delegates to {@link #stringToArray} for the
     * parsing, which strips any non-attribute characters (spaces, extra commas, etc.).
     *
     * <p>For example — R2 reads his column selections:</p>
     * <pre>
     *   comboText = "cn, mail, uid";
     *   return [ "cn", "mail", "uid" ];
     * </pre>
     *
     * @return  Array of attribute name strings. May be empty but never null.
     */
    public String[] getReturningAttributes()
    {
        String s = this.returningAttributesCombo.getText();
        return stringToArray( s );
    }


    // ── R2 Commits His Column History to Persistent Memory ───────────────────────────
    // After the mission, R2 logs which columns he queried so the same list appears
    // in the history drop-down next time he opens a similar terminal session.
    // We persist the current combo text to the dialog-settings history store.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current returning-attributes string to the dialog history so it shows
     * up in the combo's drop-down on future openings. Call this when the user confirms
     * the dialog (OK / Search button).
     *
     * <p>For example — R2 logs the column selection to his mission journal:</p>
     * <pre>
     *   missionJournal.save( "cn, mail, uid" );
     * </pre>
     */
    public void saveDialogSettings()
    {
        HistoryUtils.save( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_RETURNING_ATTRIBUTES_HISTORY,
            Utils.arrayToString( getReturningAttributes() ) );
    }


    // ── R2 Positions His Cursor Ready for Column Input ────────────────────────────────
    // R2 rolls up to the terminal and the cursor blinks inside the combo, ready
    // for the user to start typing or picking from the history drop-down.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Requests keyboard focus for the returning-attributes combo field. Call this
     * after the dialog opens if you want the cursor to land here by default.
     *
     * <p>For example — R2 moves to the terminal and waits:</p>
     * <pre>
     *   columnsCombo.requestFocus();
     * </pre>
     */
    public void setFocus()
    {
        returningAttributesCombo.setFocus();

    }


    // ── R2 Parses the Raw Column String Into Individual Names ─────────────────────────
    // The raw text from the combo might be "cn, mail, uid  *" — R2 needs to split this
    // into clean individual tokens, keeping only characters valid in attribute names and
    // special tokens (* + @). He discards spaces, commas, and any other separators.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a comma-separated (or otherwise-delimited) attribute-name string into an
     * array of individual tokens. Only the following characters are kept in each token;
     * anything else acts as a delimiter:
     * <ul>
     *   <li>{@code a-z}, {@code A-Z}, {@code 0-9} — standard identifier characters</li>
     *   <li>{@code -} — allowed in attribute type names</li>
     *   <li>{@code .} — used in numeric OIDs</li>
     *   <li>{@code ;} — attribute option separator</li>
     *   <li>{@code _} — some servers allow underscores</li>
     *   <li>{@code *} — "all user attributes" wildcard</li>
     *   <li>{@code +} — "all operational attributes" wildcard</li>
     *   <li>{@code @} — object-class selector prefix</li>
     *   <li>{@code =} — range option (DIRSTUDIO-985)</li>
     * </ul>
     *
     * <p>For example — R2 parses the raw string:</p>
     * <pre>
     *   input  = "cn, mail; uid  *";
     *   output = [ "cn", "mail", "uid", "*" ];
     * </pre>
     *
     * @param s  The raw string from the combo field. May be null.
     * @return   Array of clean attribute-name tokens, or null if {@code s} is null.
     */
    public static String[] stringToArray( String s )
    {
        if ( s == null )
        {
            return null;
        }
        else
        {
            List<String> attributeList = new ArrayList<String>();

            StringBuffer temp = new StringBuffer();
            for ( int i = 0; i < s.length(); i++ )
            {
                char c = s.charAt( i );

                if ( ( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' ) || ( c >= '0' && c <= '9' ) || c == '-'
                    || c == '.' || c == ';' || c == '_' || c == '*' || c == '+' || c == '@' || c == '=' )
                {
                    temp.append( c );
                }
                else
                {
                    if ( temp.length() > 0 )
                    {
                        attributeList.add( temp.toString() );
                        temp = new StringBuffer();
                    }
                }
            }
            if ( temp.length() > 0 )
            {
                attributeList.add( temp.toString() );
            }

            return ( String[] ) attributeList.toArray( new String[attributeList.size()] );
        }
    }

}
