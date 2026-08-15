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
 */
package org.apache.directory.studio;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: StudioAppearancePage — Ackbar Adjusts the Tactical Hologram Display ──
// In the Home One briefing room, Admiral Ackbar notices that the standard
// holographic display includes a "Color Palette Selector" control that the
// Rebel engineers superseded with the new CSS theme console — it's confusing
// and clutters the tactical readout.  Ackbar has his crew physically hide that
// one row while keeping everything else exactly as it was.
// StudioAppearancePage does the same: it delegates all preference logic to
// Eclipse's built-in ViewsPreferencePage but hides the now-redundant
// "Color and Font theme" combo before the page is shown.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Replacement for the built-in Eclipse Appearance preference page that omits
 * the legacy "Color and font theme" row (superseded by the CSS theme selector).
 * All preference logic is delegated to the original {@code ViewsPreferencePage} via
 * reflection so that tab, font, and scaling settings continue to work normally.
 * Think of this as Ackbar's crew hiding one confusing panel on the Home One
 * tactical display while leaving every other control fully functional.
 */
public class StudioAppearancePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private static final String VIEWS_PAGE_CLASS = "org.eclipse.ui.internal.dialogs.ViewsPreferencePage";

    private PreferencePage delegate;


    // ── Ackbar Names the Display Panel — "Appearance" ────────────────────────
    // Ackbar labels the patched tactical display "Appearance" so officers
    // navigating the control room know exactly which panel they're looking at.
    // Our constructor just sets the title that appears in the Preferences tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the page with the title "Appearance" and no delegate yet.
     * The delegate is created lazily in {@link #init(IWorkbench)} when Eclipse
     * provides us with a workbench reference.
     */
    public StudioAppearancePage()
    {
        setTitle( "Appearance" );
    }


    // ── Ackbar's Engineer Loads the Original Panel Firmware via Backdoor ──────
    // The engineer can't access the source code of the original panel, so she
    // uses a maintenance backdoor (reflection) to load the standard firmware
    // into a shadow instance and then patches it at runtime.
    // init() does exactly that: we reflectively instantiate Eclipse's internal
    // ViewsPreferencePage and initialise it so it's ready to create its widgets.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the delegate preference page via reflection.
     * We load Eclipse's internal {@code ViewsPreferencePage} class, instantiate
     * it, and call its {@code init} so it's fully wired up.  If reflection fails
     * (e.g. Eclipse changed the internal class) we set {@code delegate = null}
     * and show an empty page rather than crashing.
     *
     * @param workbench  the workbench — passed through to the delegate's {@code init}.
     */
    @Override
    public void init( IWorkbench workbench )
    {
        try
        {
            delegate = ( PreferencePage ) Class.forName( VIEWS_PAGE_CLASS )
                .getDeclaredConstructor().newInstance();
            ( ( IWorkbenchPreferencePage ) delegate ).init( workbench );
        }
        catch ( ReflectiveOperationException e )
        {
            delegate = null;
        }
    }


    // ── The Engineer Builds the Panel, Then Hides the Offending Row ───────────
    // The engineer instantiates the full original panel display (delegates to
    // standard createContents) and then physically hides the "Color Palette
    // Selector" row by calling hideColorsAndFontsThemeRow() — the row is gone
    // from the layout but all other controls remain fully wired and functional.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the page contents by delegating to the internal Eclipse page, then
     * hides the "Color and Font theme" combo row.
     * If the delegate is unavailable we return an empty label so the page still
     * opens without an error.
     *
     * @param parent  the SWT container provided by the Preferences dialog.
     * @return        the root widget of the delegate's content, or an empty
     *                {@link Label} if the delegate could not be loaded.
     */
    @Override
    protected Control createContents( Composite parent )
    {
        if ( delegate == null )
        {
            return new Label( parent, SWT.NONE );
        }
        try
        {
            Method m = findMethod( delegate.getClass(), "createContents", Composite.class );
            m.setAccessible( true );
            Control content = ( Control ) m.invoke( delegate, parent );
            hideColorsAndFontsThemeRow();
            return content;
        }
        catch ( ReflectiveOperationException e )
        {
            return new Label( parent, SWT.NONE );
        }
    }


    // ── The Engineer Cuts Power to the Offending Selector ────────────────────
    // The engineer locates the "colorsAndFontsThemeCombo" relay on the panel,
    // cuts power to the entire row (parent composite), and removes it from the
    // layout grid so the display reflows cleanly without it.
    // hideColorsAndFontsThemeRow() does the same via reflection: find the field,
    // get its parent composite, exclude it from the GridLayout.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Hides the "Color and Font theme" row from the delegate's widget tree.
     * We reach into the delegate via reflection to find the {@code colorsAndFontsThemeCombo}
     * field, then hide and exclude the entire parent row from the GridLayout.
     * If the field doesn't exist (Eclipse changed it) we silently do nothing —
     * the combo stays visible but that's better than a crash.
     */
    private void hideColorsAndFontsThemeRow()
    {
        try
        {
            Field f = delegate.getClass().getDeclaredField( "colorsAndFontsThemeCombo" );
            f.setAccessible( true );
            ComboViewer viewer = ( ComboViewer ) f.get( delegate );
            if ( viewer == null )
            {
                return;
            }
            Composite row = viewer.getCombo().getParent();
            excludeFromLayout( row );
            row.getParent().layout( true, true );
        }
        catch ( ReflectiveOperationException e )
        {
            // Silently accept — the combo stays visible but functionality is unaffected.
        }
    }


    // ── The Engineer Removes the Row from the Grid ────────────────────────────
    // The engineer sets each child widget's visibility to false and tells the
    // GridLayout to exclude those cells from the flow so the remaining rows
    // reflow upward, leaving no ugly blank space.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Hides all children of the composite and excludes it from its GridLayout.
     * Setting {@code exclude = true} on a {@link GridData} tells the GridLayout
     * to act as if that widget doesn't exist — it takes up no space, so the
     * remaining rows fill the gap cleanly.
     *
     * @param composite  the row composite to hide and exclude from layout.
     */
    private static void excludeFromLayout( Composite composite )
    {
        for ( Control child : composite.getChildren() )
        {
            child.setVisible( false );
            Object ld = child.getLayoutData();
            if ( ld instanceof GridData )
            {
                ( ( GridData ) ld ).exclude = true;
            }
        }
        Object ld = composite.getLayoutData();
        if ( ld instanceof GridData )
        {
            ( ( GridData ) ld ).exclude = true;
        }
        composite.setVisible( false );
    }


    // ── Ackbar Commits the Configuration Changes ──────────────────────────────
    // Ackbar approves the patched display configuration and the engineer commits
    // the changes to the ship's systems — all the real work goes through the
    // original panel's commit logic, which we haven't touched.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the preference page's settings by delegating to the wrapped page.
     * All the real preference logic lives in the delegate; we just forward the
     * OK signal to it.
     *
     * @return  the delegate's result, or {@code true} if there is no delegate.
     */
    @Override
    public boolean performOk()
    {
        return delegate != null ? delegate.performOk() : true;
    }


    // ── Ackbar Resets the Display to Its Pre-Battle Default Configuration ──────
    // Ackbar orders the engineer to reset all non-hidden controls to their factory
    // defaults — the hidden row stays hidden, but everything else snaps back.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Restores all settings to their defaults by delegating to the wrapped page.
     * We call the delegate's {@code performDefaults} via reflection; if that fails
     * we fall back to the parent class's default implementation.
     */
    @Override
    protected void performDefaults()
    {
        if ( delegate == null )
        {
            return;
        }
        try
        {
            Method m = findMethod( delegate.getClass(), "performDefaults" );
            m.setAccessible( true );
            m.invoke( delegate );
        }
        catch ( ReflectiveOperationException e )
        {
            super.performDefaults();
        }
    }


    // ── The Engineer Climbs the Class Hierarchy to Find the Right Panel ───────
    // The engineer can't find the maintenance hatch on the surface panel, so she
    // opens the access panel behind it, then the one behind that — climbing the
    // superclass hierarchy until she finds the right method to call.
    // findMethod() does the same: it walks up the class hierarchy looking for the
    // named method, which may be declared on a superclass of the delegate.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Finds a declared method by name and parameter types, walking up the class hierarchy.
     * Eclipse's internal classes sometimes declare the method on a parent class, so
     * we recurse up the hierarchy rather than failing immediately if the method isn't
     * on the exact class we start from.
     *
     * @param clazz   the class to start searching from — we walk up via
     *                {@link Class#getSuperclass()} if the method isn't found here.
     * @param name    the method name to look for.
     * @param params  the parameter types of the method signature.
     * @return        the {@link Method}, accessible from any class.
     * @throws NoSuchMethodException  if the method is not found anywhere in the hierarchy.
     */
    private static Method findMethod( Class<?> clazz, String name, Class<?>... params ) throws NoSuchMethodException
    {
        try
        {
            return clazz.getDeclaredMethod( name, params );
        }
        catch ( NoSuchMethodException e )
        {
            if ( clazz.getSuperclass() != null )
            {
                return findMethod( clazz.getSuperclass(), name, params );
            }
            throw e;
        }
    }
}
