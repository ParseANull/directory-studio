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


/**
 * Preference page for selecting a Base16 color scheme.
 *
 * Located under: Window > Preferences > Apache Directory Studio > Color Schemes (Base16)
 *
 * Selecting a scheme and clicking OK (or Apply) writes the Studio semantic color
 * preferences directly to the preference store.  Those values persist until the
 * user switches Eclipse themes (Window > Appearance), which causes the CSS engine
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


    @Override
    protected void performDefaults()
    {
        combo.select( 0 );
        selected = null;
        updateSwatches();
        super.performDefaults();
    }


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


    @Override
    public void init( IWorkbench workbench )
    {
        // nothing
    }
}
