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
package org.apache.directory.studio.common.ui.colorschemes;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: ColorSchemePreferencePage — IMPERIAL CONFIGURATION TERMINAL (COLORS)
// Deep in the Death Star's control room there is a dedicated color-scheme
// terminal where Imperial technicians can choose which visual palette to flash
// onto every display at once.  This preference page is exactly that terminal:
// the operator picks a named Base16 scheme from the drop-down, previews all
// fifteen color swatches in the panel below, and hits OK to apply the palette
// to the preference store so every editor and tree view across Directory Studio
// immediately uses the new colors.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We implement the "Color Schemes (Base16)" Eclipse preference page, located
 * under Window &gt; Preferences &gt; Apache Directory Studio &gt; Color Schemes.
 * Selecting a scheme and clicking OK writes fifteen color preferences to the
 * store.  Selecting "(none)" resets every key to its default so the active
 * Eclipse theme's CSS takes over.
 *
 * Located under: Window &gt; Preferences &gt; Apache Directory Studio &gt; Color Schemes (Base16)
 *
 * Selecting a scheme and clicking OK (or Apply) writes the Studio semantic color
 * preferences directly to the preference store.  Those values persist until the
 * user switches Eclipse themes (Window &gt; Appearance), which causes the CSS engine
 * to overwrite them with the new theme's defaults.  To restore a scheme after a
 * theme switch, re-open this page and click OK again.
 */
public class ColorSchemePreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private static final String PREF_SELECTED_SCHEME = CommonUIConstants.COLOR_SCHEME_ID;

    private static final String[] COLOR_LABELS = {
        "Default text", "Disabled", "Error",
        "Comment", "Keyword (primary)", "Keyword (secondary)",
        "Object class", "Attribute type", "Value",
        "OID", "Separator", "Add",
        "Delete", "Modify", "Rename"
    };

    private Combo combo;
    private Canvas[] swatches;
    private final List<Color> swatchColors = new ArrayList<>();
    private ColorScheme selected;


    // ── CONSTRUCTOR ColorSchemePreferencePage — BOOTING THE COLOR TERMINAL ────
    // The color terminal boots up with its title and a helpful description that
    // reminds the operator what this panel does and how to recover after an
    // Eclipse theme switch.  No Default or Apply suppression here — both buttons
    // are useful for previewing and resetting the color scheme.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We initialize the preference page with its title and a multi-line
     * description explaining the purpose of the color scheme picker and
     * the tip about re-applying after theme switches.
     */
    public ColorSchemePreferencePage()
    {
        super( "Color Schemes (Base16)" );
        setDescription(
            "Select a Base16 color scheme to apply to the LDAP browser.\n" +
            "These colors control how LDAP elements (keywords, values, object\n" +
            "classes, diff markers) are displayed in editors and tree views.\n\n" +
            "Tip: if you change the Eclipse theme (Window → Appearance), re-open\n" +
            "this page and click OK to restore the Base16 scheme." );
    }


    // ── METHOD createContents — ASSEMBLING THE TERMINAL DISPLAY PANELS ────────
    // We assemble the two-panel terminal display: the scheme-picker panel on
    // top and the color-swatch preview panel below.  We also restore the last
    // saved scheme from the preference store so the terminal shows the current
    // state rather than always starting blank.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the preference page content: a scheme-picker group at the top
     * and a 15-swatch preview group below it.  If a scheme was previously saved
     * to the preference store, we restore the combo selection and update the
     * swatches so the page opens showing the current state.
     *
     * @param parent the parent composite provided by the preferences framework
     * @return the root composite of the page's content area
     * {@inheritDoc}
     */
    @Override
    protected Control createContents( Composite parent )
    {
        Composite root = new Composite( parent, SWT.NONE );
        root.setLayout( new GridLayout( 1, false ) );
        root.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        createSchemeGroup( root );
        createPreviewGroup( root );

        // Restore previously applied scheme, if any
        String savedId = CommonUIPlugin.getDefault().getPreferenceStore()
            .getString( PREF_SELECTED_SCHEME );
        ColorScheme saved = ColorSchemes.findById( savedId );
        if ( saved != null )
        {
            selected = saved;
            combo.select( ColorSchemes.ALL.indexOf( saved ) + 1 ); // +1 for the "(none)" item
            updateSwatches();
        }

        return root;
    }


    // ── METHOD createSchemeGroup (private) — BUILDING THE SCHEME SELECTOR PANEL
    // We build the scheme-selector panel: a label on the left and a drop-down
    // combo on the right populated with "(none)" at the top followed by every
    // known Base16 scheme.  A selection listener fires updateSwatches whenever
    // the operator changes the combo choice.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the "Scheme" group containing the color-scheme combo box.  The
     * combo starts with a "(none)" option followed by all entries from
     * {@link ColorSchemes#ALL}.  Changing the selection immediately updates
     * the preview swatches.
     *
     * @param parent the composite to add the group into
     */
    private void createSchemeGroup( Composite parent )
    {
        Group group = new Group( parent, SWT.NONE );
        group.setText( "Scheme" );
        group.setLayout( new GridLayout( 2, false ) );
        group.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        Label lbl = new Label( group, SWT.NONE );
        lbl.setText( "Color scheme:" );

        combo = new Combo( group, SWT.DROP_DOWN | SWT.READ_ONLY );
        combo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        combo.add( "(none — use Eclipse theme defaults)" );
        for ( ColorScheme scheme : ColorSchemes.ALL )
        {
            combo.add( scheme.label );
        }
        combo.select( 0 );

        combo.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                int idx = combo.getSelectionIndex();
                selected = idx > 0 ? ColorSchemes.ALL.get( idx - 1 ) : null;
                updateSwatches();
            }
        } );
    }


    // ── METHOD createPreviewGroup (private) — BUILDING THE SWATCH PANEL ───────
    // We build the swatch panel with fifteen color boxes arranged in a three-
    // column grid.  Each row has a colored canvas swatch on the left and a
    // text label naming the semantic role on the right.  Swatches start empty
    // until the operator picks a scheme.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the "Preview" group containing fifteen color-swatch canvas
     * widgets laid out in a three-column grid.  Each swatch is paired with
     * a label describing the semantic role of that color.  The swatches are
     * blank until {@link #updateSwatches} is called.
     *
     * @param parent the composite to add the group into
     */
    private void createPreviewGroup( Composite parent )
    {
        Group group = new Group( parent, SWT.NONE );
        group.setText( "Preview" );
        // 3 scheme items per row; each item is a [swatch canvas] + [label] = 6 columns
        group.setLayout( new GridLayout( 6, false ) );
        group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        swatches = new Canvas[COLOR_LABELS.length];
        for ( int i = 0; i < COLOR_LABELS.length; i++ )
        {
            Canvas swatch = new Canvas( group, SWT.BORDER );
            GridData sgd = new GridData( SWT.FILL, SWT.CENTER, false, false );
            sgd.widthHint = 28;
            sgd.heightHint = 14;
            swatch.setLayoutData( sgd );
            swatches[i] = swatch;

            Label lbl = new Label( group, SWT.NONE );
            lbl.setText( COLOR_LABELS[i] );
            lbl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        }
    }


    // ── METHOD updateSwatches (private) — REPAINTING THE SWATCH PANEL ─────────
    // The operator has chosen a new scheme on the terminal and we need to
    // repaint all fifteen swatches to preview it.  We dispose the old Color
    // objects first to avoid leaking native handles, then create new ones from
    // the selected scheme's values and assign them to each canvas.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We refresh all fifteen preview swatches to reflect the currently selected
     * scheme.  Existing SWT Color objects are disposed before new ones are
     * created.  If {@code selected} is null (the "(none)" option), we reset
     * each swatch to the system widget background.
     */
    private void updateSwatches()
    {
        if ( swatches == null )
        {
            return;
        }

        // Dispose previously created swatch colors
        for ( Color c : swatchColors )
        {
            if ( !c.isDisposed() )
            {
                c.dispose();
            }
        }
        swatchColors.clear();

        String[] values = selected != null ? selected.getValues() : null;

        for ( int i = 0; i < swatches.length; i++ )
        {
            Canvas swatch = swatches[i];
            if ( values != null )
            {
                RGB rgb = parseRgb( values[i] );
                if ( rgb != null )
                {
                    Color color = new Color( swatch.getDisplay(), rgb );
                    swatchColors.add( color );
                    swatch.setBackground( color );
                    swatch.setToolTipText( values[i] );
                }
            }
            else
            {
                swatch.setBackground( swatch.getDisplay().getSystemColor( SWT.COLOR_WIDGET_BACKGROUND ) );
                swatch.setToolTipText( "" );
            }
            swatch.redraw();
        }
    }


    // ── METHOD parseRgb (private) — DECODING THE COLOR CODE ──────────────────
    // The Imperial decoder reads a comma-separated "R,G,B" string from the
    // archive and converts it into an SWT RGB object.  Malformed or null input
    // returns null — no crashing the terminal over a bad color string.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We parse a {@code "R,G,B"} comma-separated string into an SWT {@link RGB}.
     * Returns {@code null} for null input, wrong number of components, or
     * non-integer component values.
     *
     * @param csv the comma-separated color string, e.g. {@code "220,50,47"}
     * @return the parsed RGB, or {@code null} on any parse failure
     */
    private RGB parseRgb( String csv )
    {
        if ( csv == null )
        {
            return null;
        }
        String[] parts = csv.split( "," );
        if ( parts.length != 3 )
        {
            return null;
        }
        try
        {
            return new RGB(
                Integer.parseInt( parts[0].trim() ),
                Integer.parseInt( parts[1].trim() ),
                Integer.parseInt( parts[2].trim() ) );
        }
        catch ( NumberFormatException ex )
        {
            return null;
        }
    }


    // ── METHOD performOk — WRITING THE CHOSEN PALETTE TO THE STORE ───────────
    // The operator presses OK and the chosen color scheme is written to the
    // preference store.  If no scheme was selected, we reset all color keys to
    // their defaults and clear the saved-scheme ID so the CSS theme takes over.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We apply the currently selected scheme to the preference store when the
     * user clicks OK.  If a scheme is selected we write its fifteen colors and
     * save its id.  If "(none)" is selected we reset every color key to its
     * default and clear the saved id so the Eclipse theme's CSS rules take
     * over.
     *
     * @return {@code true} always, indicating OK handling was successful
     * {@inheritDoc}
     */
    @Override
    public boolean performOk()
    {
        IPreferenceStore store = CommonUIPlugin.getDefault().getPreferenceStore();
        if ( selected != null )
        {
            selected.applyTo( store );
            store.setValue( PREF_SELECTED_SCHEME, selected.id );
        }
        else
        {
            // Reset all color preferences to defaults so the Eclipse theme CSS takes over
            for ( String key : ColorScheme.KEYS )
            {
                store.setToDefault( key );
            }
            store.setValue( PREF_SELECTED_SCHEME, "" );
        }
        return true;
    }


    // ── METHOD performDefaults — RESETTING THE TERMINAL TO FACTORY STATE ──────
    // The operator hits the "Restore Defaults" button and the terminal reverts
    // to its factory state: the combo snaps back to "(none)" and the swatches
    // clear to show that the Eclipse theme will be in charge of colors.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We reset the combo to its first item (none) and clear the selected scheme
     * reference, then refresh the swatches.  The parent's {@code performDefaults}
     * is called last so the standard Eclipse reset logic can run.
     * {@inheritDoc}
     */
    @Override
    protected void performDefaults()
    {
        combo.select( 0 );
        selected = null;
        updateSwatches();
        super.performDefaults();
    }


    // ── METHOD dispose — POWERING DOWN THE COLOR TERMINAL ─────────────────────
    // The color terminal shuts down: we dispose every SWT Color object we
    // created for the swatches to release the native graphics handles before
    // the parent's dispose logic runs.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We dispose all SWT {@link Color} objects we created for the preview
     * swatches to prevent native handle leaks, then delegate to the parent's
     * dispose method.
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        for ( Color c : swatchColors )
        {
            if ( !c.isDisposed() )
            {
                c.dispose();
            }
        }
        swatchColors.clear();
        super.dispose();
    }


    // ── METHOD init — ACKNOWLEDGING THE WORKBENCH HANDSHAKE ──────────────────
    // The workbench gives us a heads-up that it is ready, but we have no
    // workbench-level initialization to perform here — everything we need is
    // handled in createContents.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We receive the workbench reference from the preferences framework but
     * perform no initialization here — all setup happens in
     * {@link #createContents}.
     *
     * @param workbench the current workbench instance
     * {@inheritDoc}
     */
    @Override
    public void init( IWorkbench workbench )
    {
        // nothing
    }
}
