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
package org.apache.directory.studio.ldapservers.model;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// ── CLASS: LdapServerAdapterConfigurationPage — THE DEATH STAR ENGINEERING SCHEMATIC PAGE ─
// Each section of the Death Star has its own engineering schematic page — the shield emitter
// page, the superlaser page, the reactor cooling page.  Every page has a title, description,
// icon, and a way to load/save its settings.
// This interface is the contract for one configuration page tab in the "New Server" wizard
// or server properties dialog — the adapter supplies its own implementation.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The contract for a single configuration page contributed by an LDAP server adapter.
 * Adapter plugins implement this interface to provide a custom SWT UI for their server's
 * settings (ports, paths, etc.) that appears as a tab in the "New Server" wizard or the
 * server's Properties dialog.
 * Think of each implementation as one schematic page in the Death Star blueprint.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface LdapServerAdapterConfigurationPage
{
    // ── The Engineer Draws The Schematic Panel ───────────────────────────────────────────────
    // Each schematic page has a physical panel — widgets, labels, input fields — that the
    // engineer can read and modify.  This method creates that panel inside the parent container.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns the SWT {@link Control} that represents this configuration page.
     * Implementations build their form widgets (text fields, checkboxes, etc.) inside
     * the given {@code parent} composite and return the top-level control.
     *
     * @param parent  the SWT composite to build the page inside
     * @return the top-level SWT control for this page
     */
    Control createControl( Composite parent );


    // ── Reading The Schematic Page's Description Placard ────────────────────────────────────
    // Each engineering panel has a small placard explaining what it controls.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a short description of what this configuration page controls.
     * Displayed as subtitle text in the wizard or properties dialog.
     *
     * @return the description string
     */
    String getDescription();


    // ── Checking The Panel's Error Indicator Light ───────────────────────────────────────────
    // If a value is out of range or invalid, the panel's error light turns on with a message.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current validation error message for this page, or {@code null} if there are none.
     * Used by the wizard container to display error text at the top of the dialog.
     *
     * @return the current error message, or {@code null} if the page is valid
     */
    String getErrorMessage();


    // ── Reading The Panel's Section Identifier ───────────────────────────────────────────────
    // Each schematic page has a unique section ID so the engineering system can reference it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this page's unique identifier string.
     *
     * @return the page ID
     */
    String getId();


    // ── Looking Up The Panel's Section Icon ─────────────────────────────────────────────────
    // Each engineering section has its own icon on the master schematic index.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ImageDescriptor} for this page's icon, used in the wizard/dialog header.
     *
     * @return the image descriptor
     */
    ImageDescriptor getImageDescriptor();


    // ── Reading The Panel's Title Banner ────────────────────────────────────────────────────
    // Each engineering section has a title printed in large text at the top of its panel.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display title for this configuration page.
     * Shown in the wizard/dialog header area.
     *
     * @return the page title
     */
    String getTitle();


    // ── Checking Whether The Panel Sign-Off Is Possible ──────────────────────────────────────
    // An engineer can only sign off on a schematic page once all required fields are filled in
    // and validated.  Until then, the "Next" / "Finish" button stays greyed out.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether all required fields on this page have been filled in correctly.
     * The wizard container uses this to enable or disable the "Next" / "Finish" button.
     *
     * @return {@code true} if the page has no validation errors and is ready to proceed;
     *         {@code false} otherwise
     */
    boolean isPageComplete();


    // ── Loading The Current Configuration Onto The Panel ────────────────────────────────────
    // The engineer opens a server's schematic page and the current settings are pre-filled
    // from the server's configuration record.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates this page's UI fields from the given server's persisted configuration parameters.
     * Called when the page is opened for an existing server (e.g., in the Properties dialog)
     * so the user sees the current settings rather than blanks.
     *
     * @param ldapServer  the server whose configuration to load into the UI
     */
    void loadConfiguration( LdapServer ldapServer );


    // ── Writing The Panel's Settings Back To The Configuration Record ────────────────────────
    // After the engineer makes changes and signs off, the settings are written back to the
    // server's official configuration record.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the UI field values and saves them to the given server's configuration parameters.
     * Called when the user clicks "Finish" (wizard) or "OK" (properties dialog) to commit changes.
     *
     * @param ldapServer  the server whose configuration to update
     */
    void saveConfiguration( LdapServer ldapServer );


    // ── Plugging In The Control Room Notification Wire ──────────────────────────────────────
    // The control room wants to know the moment any value on this panel changes so the wizard
    // can re-validate and update the "Finish" button state.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a listener that is called whenever any field on this page changes.
     * The wizard container supplies this so it can re-run validation and update the "Finish"
     * button state whenever the user types something.
     *
     * @param modifyListener  the listener to notify on any field modification
     */
    void setModifyListener( LdapServerAdapterConfigurationPageModifyListener modifyListener );


    // ── Running The Engineering Sign-Off Checklist ───────────────────────────────────────────
    // The engineer runs through the sign-off checklist — all values in range? all required fields
    // filled? — and posts the result to the error indicator.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Runs validation on all fields and updates the page's error message accordingly.
     * Must be called after any field change so {@link #getErrorMessage()} and
     * {@link #isPageComplete()} reflect the current state.
     */
    void validate();
}
