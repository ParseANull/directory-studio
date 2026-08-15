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
package org.apache.directory.studio.common.ui;


import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: StudioPreferencePage — IMPERIAL CONFIGURATION TERMINAL (ROOT) ─────
// Every Death Star has a root configuration terminal — the top-level panel
// that all the sub-system controls branch off from.  This class is exactly
// that for Apache Directory Studio's preferences: a placeholder root page
// that carries the title and description but leaves the actual controls to
// its child pages.  We suppress the Default and Apply buttons because there
// is nothing to configure at this level.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We implement the root "Apache Directory Studio" preference page in Eclipse's
 * Preferences dialog.  This page is intentionally empty — it serves as a
 * category header so child preference pages appear grouped beneath us.  We
 * hide the Default and Apply buttons because there are no settings to reset
 * or apply at this level.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    // ── CONSTRUCTOR StudioPreferencePage — INITIALIZING THE TERMINAL ─────────
    // We boot up the Imperial terminal with a localized title and description,
    // then immediately suppress the Default and Apply buttons — there is nothing
    // to configure on this root page, so those buttons would only confuse users.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We initialize the root preference page by setting its localized title and
     * description, then calling {@code noDefaultAndApplyButton()} to hide the
     * buttons that have no purpose on a category-only page.
     */
    public StudioPreferencePage()
    {
        super( Messages.getString( "StudioPreferencePage.Title" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "StudioPreferencePage.Description" ) ); //$NON-NLS-1$

        // Removing Default and Apply buttons
        noDefaultAndApplyButton();
    }


    // ── METHOD createContents — LEAVING THE TERMINAL PANEL EMPTY ────────────
    // This Imperial terminal has no switches or dials — it is just a label
    // holder.  We return the parent composite unchanged because there is
    // nothing to add to the page body.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the parent composite without adding any controls, because this
     * root page is a category placeholder with no configurable settings of
     * its own.
     *
     * @param parent the parent composite provided by the preferences framework
     * @return the parent composite, unmodified
     * {@inheritDoc}
     */
    protected Control createContents( Composite parent )
    {
        // Nothing to do
        return parent;
    }


    // ── METHOD init — ACKNOWLEDGING THE WORKBENCH HANDSHAKE ─────────────────
    // The workbench gives us a heads-up that it is ready, but we have nothing
    // to set up on our end.  We accept the handshake silently and move on.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We receive the workbench reference from the preferences framework but
     * have no initialization work to do here.  This page requires no workbench
     * state to function correctly.
     *
     * @param workbench the current workbench instance
     * {@inheritDoc}
     */
    public void init( IWorkbench workbench )
    {
        // Nothing to do
    }
}
