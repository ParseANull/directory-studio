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


// ── CLASS: AbstractLdapServerAdapterConfigurationPage — THE ENGINEERING SCHEMATIC BASE PLATE
// Imperial engineers don't start from scratch every time they design a configuration panel.
// They reuse the standard base plate: fields for the panel's title, description, icon,
// error message, and page-completion flag — plus the standard wiring to the modify-listener.
// Concrete adapter configuration pages extend this abstract class and add their own controls,
// calling configurationPageModified() whenever the engineer changes a field.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Base implementation of {@link LdapServerAdapterConfigurationPage}.
 * Handles the standard bookkeeping — id, title, description, image, error message,
 * page-complete flag, and modify-listener notifications — so concrete subclasses
 * only need to implement {@link #createControl}, {@link #loadConfiguration},
 * {@link #saveConfiguration}, and {@link #validate}.
 * Think of it as the standard Imperial engineering schematic base plate.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractLdapServerAdapterConfigurationPage implements LdapServerAdapterConfigurationPage
{
    /** The id */
    protected String id;

    /** The title */
    protected String title;

    /** The description */
    protected String description;

    /** The image descriptor */
    protected ImageDescriptor imageDescriptor;

    /** The error message */
    protected String errorMessage;

    /** The flag for page completion */
    protected boolean pageComplete = true;

    /** The modify listener */
    protected LdapServerAdapterConfigurationPageModifyListener modifyListener;


    // ── Reading The Panel's Description Label ────────────────────────────────────────────────
    // The base plate stores the description text for the configuration panel header.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return description;
    }


    // ── Reading The Panel's Current Error Message ─────────────────────────────────────────────
    // If validation failed, the error message explains why the engineer can't proceed.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }


    // ── Reading The Panel's ID ────────────────────────────────────────────────────────────────
    // The ID uniquely identifies this configuration page within the adapter's page map.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getId()
    {
        return id;
    }


    // ── Reading The Panel's Icon ──────────────────────────────────────────────────────────────
    // The image descriptor provides the icon shown in the wizard or properties dialog header.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return imageDescriptor;
    }


    // ── Reading The Panel's Title ─────────────────────────────────────────────────────────────
    // The title text appears at the top of the configuration panel header.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getTitle()
    {
        return title;
    }


    // ── Checking Whether The Panel Is Complete ────────────────────────────────────────────────
    // The wizard's Finish button only enables when isPageComplete() returns true for every page.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isPageComplete()
    {
        return pageComplete;
    }


    // ── Setting The Description ───────────────────────────────────────────────────────────────
    // Concrete subclasses call this in their constructor to set the description text.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the description displayed in the wizard or properties dialog page header.
     *
     * @param description  the description text
     */
    public void setDescription( String description )
    {
        this.description = description;
    }


    // ── Setting The Error Message And Driving Page Completion ─────────────────────────────────
    // When validate() finds a problem, it calls setErrorMessage() with the human-readable
    // reason.  This also sets pageComplete = (message == null), so valid and complete are
    // always in sync.  Passing null clears the error and re-enables Finish.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the current error message and updates the page-complete flag accordingly.
     * {@code null} clears the error and marks the page complete.
     * Call this from your {@link #validate()} implementation.
     *
     * @param errorMessage  the error text to display, or {@code null} to clear it
     */
    public void setErrorMessage( String errorMessage )
    {
        this.errorMessage = errorMessage;
        setPageComplete( errorMessage == null );
    }


    /**
     * Sets the page's unique ID.
     *
     * @param id  the page ID
     */
    public void setId( String id )
    {
        this.id = id;
    }


    /**
     * Sets the image descriptor for the page header icon.
     *
     * @param imageDescriptor  the image descriptor
     */
    public void setImageDescriptor( ImageDescriptor imageDescriptor )
    {
        this.imageDescriptor = imageDescriptor;
    }


    // ── Registering The Modify Listener ───────────────────────────────────────────────────────
    // The wizard or Properties dialog registers itself as the modify listener so it can
    // react (update the OK/Finish button, show the error banner) whenever a field changes.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setModifyListener( LdapServerAdapterConfigurationPageModifyListener modifyListener )
    {
        this.modifyListener = modifyListener;
    }


    /**
     * Sets the page-complete flag directly.
     * Prefer {@link #setErrorMessage(String)} which keeps this in sync automatically.
     *
     * @param pageComplete  {@code true} if the page is in a valid, saveable state
     */
    public void setPageComplete( boolean pageComplete )
    {
        this.pageComplete = pageComplete;
    }


    /**
     * Sets the page title displayed in the wizard or properties dialog header.
     *
     * @param title  the title text
     */
    public void setTitle( String title )
    {
        this.title = title;
    }


    // ── Coordinating Validation And Listener Notification ─────────────────────────────────────
    // When any input field changes, the subclass calls configurationPageModified().
    // We first run validate() to update the error message and page-complete state,
    // then fire the modify listener so the hosting dialog can react.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Call this from your input-change handlers to trigger the validate → notify cycle.
     * Calls {@link #validate()} (which your subclass implements), then fires the
     * {@link LdapServerAdapterConfigurationPageModifyListener} so the hosting dialog
     * re-checks the error message and page-complete state.
     *
     * <p>For example — an engineer types a new port number:</p>
     * <pre>
     *   portText.addModifyListener(e -> configurationPageModified());
     *   // validate() checks the value, setErrorMessage("") or setErrorMessage(null)
     *   // fireConfigurationPageModified() tells the wizard to update Finish/OK
     * </pre>
     */
    protected final void configurationPageModified()
    {
        validate();
        fireConfigurationPageModified();
    }


    // ── Notifying The Hosting Dialog ──────────────────────────────────────────────────────────
    // After validation, we ping the listener (the Properties dialog or wizard container) so
    // it can update its Finish/OK button and error banner.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires a configuration-page-modified notification to the registered listener (if any).
     * Called by {@link #configurationPageModified()} after {@link #validate()}.
     */
    protected void fireConfigurationPageModified()
    {
        if ( modifyListener != null )
        {
            modifyListener.configurationPageModified();
        }
    }
}
