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


import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: AbstractConnectionParameterPage — THE FALCON'S DEFAULT COCKPIT BASE ──
// The ConnectionParameterPage interface (the spec) says every cockpit panel must
// know its page ID, name, description, dependency, messages, and how to initialise
// itself.  Most of that bookkeeping is identical across every panel.
// Rather than duplicating it in NetworkParameterPage, AuthenticationParameterPage,
// and every other concrete panel, we put it here once.
// Concrete pages only need to implement the three abstract methods:
//   createComposite() — paint the SWT widgets
//   validate()        — check field values and set message/errorMessage
//   loadParameters()  — populate fields from a ConnectionParameter
//   initListeners()   — attach SWT listeners to fire connectionPageModified()
// Think of this as the Falcon's standard instrument panel skeleton — every
// cockpit section bolts onto the same frame; only the dials inside differ.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all {@link ConnectionParameterPage} implementations.
 * Handles the boilerplate: page identity fields, message fields, modify listener
 * wiring, and the {@link #init} / {@link #connectionPageModified} lifecycle.
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #createComposite(Composite)} — build SWT controls.</li>
 *   <li>{@link #validate()} — check field values; set {@link #message} or
 *       {@link #errorMessage}.</li>
 *   <li>{@link #loadParameters(ConnectionParameter)} — populate fields from an
 *       existing connection parameter.</li>
 *   <li>{@link #initListeners()} — attach SWT modify/selection listeners that call
 *       {@link #connectionPageModified()}.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractConnectionParameterPage implements ConnectionParameterPage
{
    /** The unique page ID set by the extension point registry. */
    protected String pageId;

    /** The human-readable page name shown in the wizard step list. */
    protected String pageName;

    /** The short description shown below the wizard title banner. */
    protected String pageDescription;

    /** The ID of the page this page must follow in the wizard. */
    protected String pageDependsOnId;

    /**
     * The runnable context provided by the host wizard or property page,
     * used to run long-running operations (e.g., "Check network parameters").
     */
    protected IRunnableContext runnableContext;

    /** The listener that the host dialog registers to be notified of field changes. */
    protected ConnectionParameterPageModifyListener connectionParameterPageModifyListener;

    /**
     * A non-blocking informational message; {@code null} means no message.
     * Set by {@link #validate()}.
     */
    protected String message;

    /**
     * A blocking error message; {@code null} means no error.
     * Set by {@link #validate()}.  An error prevents the dialog from finishing.
     */
    protected String errorMessage;

    /**
     * An informational notice message; {@code null} means no notice.
     * Set by {@link #validate()}.
     */
    protected String infoMessage;

    /**
     * The connection parameter the page is currently displaying/editing.
     * Populated by {@link #init} when editing an existing connection.
     */
    protected ConnectionParameter connectionParameter;


    // ── SET MODIFY LISTENER — REGISTER THE HOST DIALOG'S LISTENER ────────────────
    /**
     * Registers the listener that will be notified whenever a field on this page
     * changes.  Called during {@link #init}.
     *
     * @param listener  The modify listener from the host wizard or property page.
     */
    public void setConnectionParameterPageModifyListener( ConnectionParameterPageModifyListener listener )
    {
        this.connectionParameterPageModifyListener = listener;
    }


    // ── FIRE CONNECTION PAGE MODIFIED — NOTIFY THE HOST ───────────────────────────
    // Called by subclasses after any field change.  The host dialog uses the
    // notification to update its OK/Finish button state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies the registered modify listener that a field on this page has changed.
     * Subclasses should call this (indirectly, via {@link #connectionPageModified()})
     * inside every SWT listener.
     */
    protected void fireConnectionPageModified()
    {
        connectionParameterPageModifyListener.connectionParameterPageModified();
    }


    // ── SET RUNNABLE CONTEXT ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setRunnableContext( IRunnableContext runnableContext )
    {
        this.runnableContext = runnableContext;
    }


    // ── GET / SET PAGE ID ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getPageId()
    {
        return pageId;
    }


    /**
     * {@inheritDoc}
     */
    public void setPageId( String pageId )
    {
        this.pageId = pageId;
    }


    // ── GET / SET PAGE NAME ───────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getPageName()
    {
        return pageName;
    }


    /**
     * {@inheritDoc}
     */
    public void setPageName( String pageName )
    {
        this.pageName = pageName;
    }


    // ── GET / SET PAGE DESCRIPTION ────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getPageDescription()
    {
        return pageDescription;
    }


    /**
     * {@inheritDoc}
     */
    public void setPageDescription( String pageDescription )
    {
        this.pageDescription = pageDescription;
    }


    // ── GET / SET PAGE DEPENDS ON ID ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getPageDependsOnId()
    {
        return pageDependsOnId;
    }


    /**
     * {@inheritDoc}
     */
    public void setPageDependsOnId( String pageDependsOnId )
    {
        this.pageDependsOnId = pageDependsOnId;
    }


    // ── GET ERROR MESSAGE ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }


    // ── GET MESSAGE ───────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getMessage()
    {
        return message;
    }


    // ── IS VALID — NO MESSAGE AND NO ERROR MESSAGE ────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code true} when neither {@link #message} nor {@link #errorMessage}
     * is set.
     */
    public boolean isValid()
    {
        return message == null && errorMessage == null;
    }


    // ── GET INFO MESSAGE ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getInfoMessage()
    {
        return infoMessage;
    }


    // ── INIT — THE PAGE LIFECYCLE ENTRY POINT ─────────────────────────────────────
    // We call createComposite() first so the SWT controls exist, then wire the
    // modify listener, then load any existing connection parameters, then attach
    // SWT listeners, and finally run a first validation pass so the page starts
    // in the correct state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Creates SWT controls, loads parameters, wires listeners, and runs an initial
     * validation pass — all in the correct order.
     */
    public final void init( Composite parent, ConnectionParameterPageModifyListener listener,
        ConnectionParameter parameter )
    {
        createComposite( parent );

        if ( listener != null )
        {
            setConnectionParameterPageModifyListener( listener );
        }

        if ( parameter != null )
        {
            loadParameters( parameter );
        }

        initListeners();
        connectionPageModified();
    }


    // ── CONNECTION PAGE MODIFIED — VALIDATE THEN NOTIFY ──────────────────────────
    // Called by each SWT listener (via subclass).  We validate first so that
    // message/errorMessage are current before we notify the host dialog.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called internally whenever any input field changes.
     * Runs {@link #validate()} to refresh message state, then fires the modify
     * listener to let the host dialog update its button states.
     */
    protected final void connectionPageModified()
    {
        validate();
        fireConnectionPageModified();
    }


    // ── ABSTRACT — SUBCLASS RESPONSIBILITIES ──────────────────────────────────────

    /**
     * Builds and lays out all SWT controls for this page inside the given parent.
     * Called once during {@link #init}.
     *
     * @param parent  The parent SWT composite to add controls to.
     */
    protected abstract void createComposite( Composite parent );


    /**
     * Inspects all field values and updates {@link #message} and {@link #errorMessage}
     * accordingly.  {@code null} means no message/error.
     * Called after every field change.
     */
    protected abstract void validate();


    /**
     * Populates the SWT fields from the given {@link ConnectionParameter}.
     * Called during {@link #init} when editing an existing connection.
     *
     * @param parameter  The connection parameters to read from.
     */
    protected abstract void loadParameters( ConnectionParameter parameter );


    /**
     * Attaches SWT modify/selection listeners to all editable fields.
     * Each listener should call {@link #connectionPageModified()} when triggered.
     * Called once during {@link #init}, after fields are populated.
     */
    protected abstract void initListeners();
}
