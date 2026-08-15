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
package org.apache.directory.studio.schemaeditor.view.preferences;


import java.util.Comparator;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.io.SchemaConnector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: PluginPreferencePage — LUKE'S BINARY SUNSET ───────────────────────
// Luke Skywalker stands at the edge of the Lars moisture farm at dusk,
// gazing out at the twin suns of Tatooine as they sink below the dunes —
// contemplating all the paths his life might take, all the distant possibilities.
// This preference page offers a similar panoramic view: it lists every
// SchemaConnector plugin installed in Eclipse, lets the administrator survey
// the full roster, and reads each connector's description when selected —
// seeing the big picture of what is available before committing to anything.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The top-level Plugin preference page for the Schema Editor, accessible via
 * Eclipse Preferences → Schema Editor.
 * Its job is to show the administrator which SchemaConnector implementations
 * are currently installed — connectors are the bridge between the Schema
 * Editor and actual LDAP servers or file-based schema sources — and to display
 * a description of whichever connector is selected.
 * Think of this as Luke's binary sunset: a read-only, contemplative overview
 * of everything available, with no changes being made right here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PluginPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    // ── LUKE STEPS OUTSIDE AS THE SUNS BEGIN TO SET ──────────────────────────
    // Luke wanders out from the moisture farm's corridors and pauses at the
    // threshold, ready to take in the view — the twin suns still high enough
    // to illuminate the horizon.
    // We set up the plugin's preference store and write the page description
    // that will appear at the top of the preference panel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link PluginPreferencePage} and wires it to the plugin's
     * preference store.
     * Eclipse instantiates this via the preferences extension point; we call
     * {@code super()} to initialise the JFace preference-page machinery, then
     * attach our plugin's store and set the human-readable description shown
     * at the top of the page.
     *
     * <p>For example — Luke pauses at the farm entrance before the view opens:</p>
     * <pre>
     *   PluginPreferencePage page = new PluginPreferencePage();
     *   // Eclipse wires this into Window → Preferences → Schema Editor
     *   // The page description reads: "General settings for the Schema Editor."
     * </pre>
     */
    public PluginPreferencePage()
    {
        super();
        setPreferenceStore( Activator.getDefault().getPreferenceStore() );
        setDescription( Messages.getString( "PluginPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── LUKE SURVEYS THE HORIZON — ALL PATHS IN VIEW ─────────────────────────
    // Luke gazes at the twin suns and sees every direction Tatooine offers —
    // the route to Anchorhead, the way to Tosche Station, the distant dunes
    // where the Tusken Raiders roam.
    // We build the page content: a table of all available SchemaConnectors
    // on the left, a description panel on the right that fills in when the
    // administrator selects one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI and populates the SchemaConnectors table.
     * Eclipse calls this when the user navigates to this preference page; we
     * create the layout, wire up the table viewer with all registered
     * connectors, and attach a selection listener that populates the
     * description text whenever the user picks a connector.
     * This page is read-only — it doesn't persist anything on Apply/OK.
     *
     * <p>For example — Luke surveys the full horizon of available connectors:</p>
     * <pre>
     *   Installed connectors:
     *     • ApacheDS Online Connector  → "Connects to a running ApacheDS instance"
     *     • Generic LDAP Connector     → "Connects via standard LDAP protocol"
     *   User selects "ApacheDS Online Connector" → description panel updates.
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides as the parent container
     * @return        the outermost control we created, handed back to Eclipse
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // SchemaConnectors Group
        Group schemaConnectorsGroup = new Group( composite, SWT.NONE );
        schemaConnectorsGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        schemaConnectorsGroup.setLayout( new GridLayout( 2, true ) );
        schemaConnectorsGroup.setText( Messages.getString( "PluginPreferencePage.SchemaConnectors" ) ); //$NON-NLS-1$

        // Available Schema Connectors Label
        Label availableSchemaConnectorsLabel = new Label( schemaConnectorsGroup, SWT.NONE );
        availableSchemaConnectorsLabel.setText( Messages.getString( "PluginPreferencePage.AvailableConnectorsColon" ) ); //$NON-NLS-1$

        // Description Label
        Label descriptionLabel = new Label( schemaConnectorsGroup, SWT.NONE );
        descriptionLabel.setText( Messages.getString( "PluginPreferencePage.DescriptionColon" ) ); //$NON-NLS-1$
        // SchemaConnectors TableViewer
        final TableViewer schemaConnectorsTableViewer = new TableViewer( schemaConnectorsGroup, SWT.BORDER | SWT.SINGLE
            | SWT.FULL_SELECTION );
        GridData gridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        gridData.heightHint = 125;
        schemaConnectorsTableViewer.getTable().setLayoutData( gridData );
        schemaConnectorsTableViewer.setContentProvider( new ArrayContentProvider() );
        schemaConnectorsTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                return ( ( SchemaConnector ) element ).getName();
            }


            public Image getImage( Object element )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA_CONNECTOR );
            }
        } );

        schemaConnectorsTableViewer.setComparator( new ViewerComparator( new Comparator<String>()
        {
            public int compare( String o1, String o2 )
            {
                if ( ( o1 != null ) && ( o2 != null ) )
                {
                    return o1.compareToIgnoreCase( o2 );
                }

                // Default
                return 0;
            }
        } ) );

        //      schemaConnectorsTableViewer.setComparator( new ViewerComparator( new Comparator<SchemaConnector>()
        //      {
        //          public int compare( SchemaConnector o1, SchemaConnector o2 )
        //          {
        //              String name1 = o1.getName();
        //              String name2 = o2.getName();
        //
        //              if ( ( name1 != null ) && ( name2 != null ) )
        //              {
        //                  return name1.compareToIgnoreCase( name2 );
        //              }
        //
        //              // Default
        //              return 0;
        //          }
        //      } ) );
        schemaConnectorsTableViewer.setInput( PluginUtils.getSchemaConnectors() );

        // Description Text
        final Text descriptionText = new Text( schemaConnectorsGroup, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY );
        descriptionText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        schemaConnectorsTableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                SchemaConnector schemaConnector = ( SchemaConnector ) ( ( StructuredSelection ) schemaConnectorsTableViewer
                    .getSelection() ).getFirstElement();

                if ( schemaConnector != null )
                {
                    descriptionText.setText( schemaConnector.getDescription() );
                }
            }
        } );

        return parent;
    }


    // ── THE SUNS SET; NOTHING MORE TO DO TONIGHT ─────────────────────────────
    // The twin suns sink below the horizon and Luke heads back inside —
    // no decisions made tonight, just a quiet survey of the possibilities.
    // This page is read-only, so there is nothing to initialise from the
    // workbench at all.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench is initialising this preference page;
     * we have nothing to do here because this page doesn't load any per-workbench
     * state.
     * The SchemaConnector list comes from the plugin registry, not from the
     * workbench, so there is nothing to pull from the {@code workbench} parameter.
     *
     * @param workbench  the current Eclipse workbench instance; not used here
     */
    public void init( IWorkbench workbench )
    {
        // Nothing to do
    }
}
