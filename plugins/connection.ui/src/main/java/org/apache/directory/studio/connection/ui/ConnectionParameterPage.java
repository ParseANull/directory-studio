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
package org.apache.directory.studio.connection.ui;


import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Composite;


// ── INTERFACE: ConnectionParameterPage — THE FALCON'S COCKPIT PANEL CONTRACT ─────
// The New Connection Wizard and the connection Properties dialog both show a set
// of "parameter pages" — tab-like sections for network settings, auth settings,
// and so on.  Each section is a ConnectionParameterPage.
// This interface is the contract that every panel must honour: it must know how
// to paint itself (init), read from a ConnectionParameter (loadParameters via
// init), write back to one (saveParameters), report its validity, and sync its
// fields to/from an LdapUrl for copy-paste LDAP URL support.
// Think of it as the spec for each instrument cluster in the Falcon's cockpit.
// The pilot (wizard/property page) can address any cluster through this contract
// without knowing what's inside the panel.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Contract for a single page inside the New Connection Wizard or the connection
 * Properties dialog.
 *
 * <p>Each page is responsible for:</p>
 * <ul>
 *   <li>Drawing its own SWT controls inside a parent {@link Composite}.</li>
 *   <li>Loading from a {@link ConnectionParameter} when editing an existing connection.</li>
 *   <li>Saving its fields back to a {@link ConnectionParameter} when the dialog commits.</li>
 *   <li>Reporting whether its current input is valid (no error message).</li>
 *   <li>Telling the container whether a reconnect is required after changes.</li>
 *   <li>Merging its settings to/from an {@link LdapUrl} for LDAP URL copy-paste support.</li>
 * </ul>
 *
 * <p>Implementations are discovered via the
 * {@code org.apache.directory.studio.connectionparameterpages} Eclipse extension point
 * and instantiated by {@link ConnectionParameterPageManager}.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ConnectionParameterPage
{

    // ── SAVE PARAMETERS — FLUSH FIELDS TO THE MODEL ───────────────────────────────
    /**
     * Reads the current UI field values and writes them into {@code parameter}.
     * Called when the user clicks OK or Finish.
     *
     * @param parameter  The connection parameter object to update.
     */
    void saveParameters( ConnectionParameter parameter );


    // ── IS VALID — CAN WE FLY? ────────────────────────────────────────────────────
    /**
     * Returns {@code true} when all fields on this page have acceptable values
     * (i.e., no error or warning message is currently set).
     *
     * @return {@code true} if the page is in a valid state.
     */
    boolean isValid();


    // ── GET ERROR MESSAGE — HARD BLOCKER ──────────────────────────────────────────
    /**
     * Returns an error string to display in the dialog's error area,
     * or {@code null} to clear the error area.
     * An error prevents the dialog from finishing.
     *
     * @return  The error message, or {@code null}.
     */
    String getErrorMessage();


    // ── GET MESSAGE — SOFT NOTICE ─────────────────────────────────────────────────
    /**
     * Returns a non-error message to display in the dialog's message area,
     * or {@code null} to clear the message area.
     * A message does not block the dialog from finishing.
     *
     * @return  The informational message, or {@code null}.
     */
    String getMessage();


    // ── GET INFO MESSAGE — INFORMATIONAL ──────────────────────────────────────────
    /**
     * Returns an informational message to display,
     * or {@code null} to clear the info area.
     *
     * @return  The info message, or {@code null}.
     */
    String getInfoMessage();


    // ── INIT — PAINT THE PANEL AND LOAD DATA ──────────────────────────────────────
    /**
     * Initialises this page inside the given parent composite.
     * Creates all SWT controls, wires the modify listener, and (if {@code parameter}
     * is not {@code null}) populates the fields from the existing connection parameters.
     *
     * @param parent     The parent SWT composite.
     * @param listener   The listener to notify when any field changes.
     * @param parameter  The connection parameter to pre-populate from, or {@code null}
     *                   for a brand-new connection.
     */
    void init( Composite parent, ConnectionParameterPageModifyListener listener, ConnectionParameter parameter );


    // ── SAVE DIALOG SETTINGS — PERSIST AUTO-COMPLETE HISTORY ──────────────────────
    /**
     * Persists any dialog-specific state (e.g., host/port history combos) to the
     * Eclipse dialog settings so the wizard can restore them next time.
     */
    void saveDialogSettings();


    // ── SET RUNNABLE CONTEXT — ALLOW LONG-RUNNING OPERATIONS ON THE PAGE ──────────
    /**
     * Provides an {@link IRunnableContext} so the page can run long operations
     * (e.g., the "Check network parameters" button on the Network page) with a
     * proper progress dialog.
     *
     * @param runnableContext  The runnable context from the wizard or property page.
     */
    void setRunnableContext( IRunnableContext runnableContext );


    // ── PAGE ID — UNIQUE IDENTIFIER ───────────────────────────────────────────────
    /**
     * Sets the unique page identifier, as defined in the extension point configuration.
     *
     * @param pageId  The page ID string.
     */
    void setPageId( String pageId );


    /**
     * Returns the unique page identifier.
     *
     * @return  The page ID string.
     */
    String getPageId();


    // ── PAGE NAME — DISPLAY LABEL ─────────────────────────────────────────────────
    /**
     * Sets the human-readable page name shown in the wizard step list.
     *
     * @param pageName  The page name string.
     */
    void setPageName( String pageName );


    /**
     * Returns the human-readable page name.
     *
     * @return  The page name string.
     */
    String getPageName();


    // ── PAGE DESCRIPTION — HELP TEXT ──────────────────────────────────────────────
    /**
     * Sets the short description shown below the wizard title banner.
     *
     * @param pageDescription  The description string.
     */
    void setPageDescription( String pageDescription );


    /**
     * Returns the short description shown below the wizard title banner.
     *
     * @return  The description string.
     */
    String getPageDescription();


    // ── PAGE DEPENDS ON ID — ORDERING DEPENDENCY ──────────────────────────────────
    /**
     * Sets the page ID that this page must follow in the wizard.
     * {@link ConnectionParameterPageManager} uses this to sort pages topologically.
     *
     * @param pageDependsOnId  The ID of the page this page depends on.
     */
    void setPageDependsOnId( String pageDependsOnId );


    /**
     * Returns the ID of the page this page depends on.
     *
     * @return  The dependency page ID, or {@code null} if this is the first page.
     */
    String getPageDependsOnId();


    // ── SET FOCUS — DIRECT KEYBOARD FOCUS ─────────────────────────────────────────
    /**
     * Moves keyboard focus to the most appropriate input field on this page.
     */
    void setFocus();


    // ── IS RECONNECTION REQUIRED — DID WE CHANGE ANYTHING CRITICAL? ───────────────
    /**
     * Returns {@code true} if the changes made on this page require the connection
     * to be closed and re-opened before they take effect.
     * The property page uses this to warn the user after saving.
     *
     * @return  {@code true} if a reconnect is needed.
     */
    boolean isReconnectionRequired();


    // ── ARE PARAMETERS MODIFIED — DID ANYTHING CHANGE? ────────────────────────────
    /**
     * Returns {@code true} if any field on this page has been modified since
     * the dialog was opened.
     * The property page uses this to decide whether to enable the Apply button.
     *
     * @return  {@code true} if any parameter was changed.
     */
    boolean areParametersModifed();


    // ── MERGE PARAMETERS TO LDAP URL — WRITE FIELDS INTO A URL ───────────────────
    /**
     * Writes the relevant fields from {@code parameter} into {@code ldapUrl}.
     * Used when the user copies the connection as an LDAP URL.
     *
     * @param parameter  The source connection parameters.
     * @param ldapUrl    The target LDAP URL to populate.
     */
    void mergeParametersToLdapURL( ConnectionParameter parameter, LdapUrl ldapUrl );


    // ── MERGE LDAP URL TO PARAMETERS — READ A URL INTO THE MODEL ─────────────────
    /**
     * Parses relevant fields from {@code ldapUrl} and writes them into {@code parameter}.
     * Used when the user pastes an LDAP URL into the dialog.
     *
     * @param ldapUrl    The source LDAP URL.
     * @param parameter  The target connection parameters to populate.
     */
    void mergeLdapUrlToParameters( LdapUrl ldapUrl, ConnectionParameter parameter );
}
