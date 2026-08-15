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
package org.apache.directory.studio.apacheds.configuration.editor;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.directory.server.config.beans.ConfigBean;
import org.apache.directory.server.config.beans.DirectoryServiceBean;
import org.apache.directory.studio.apacheds.configuration.actions.EditorExportConfigurationAction;
import org.apache.directory.studio.apacheds.configuration.actions.EditorImportConfigurationAction;
import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.connection.core.Connection;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: ServerConfigurationEditorPage — THE IMPERIAL ENGINEERING SCHEMATIC BASE PLATE ─────────
// Every wing of the Death Star follows the same engineering protocol: shared tools, shared
// controls, shared wiring diagrams.  The weapons team, the vault architects, the comms
// engineers — all of them use the same port-field widget, the same address validator,
// the same "mark the schematics dirty" callback.
// This abstract class is that shared protocol: the standard base plate from which every
// specialist tab in the ServerConfigurationEditor is stamped.  Subclasses add their own
// controls and implement createFormContent + refreshUI; this class handles the shared toolkit.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all tab pages in the {@link ServerConfigurationEditor}.
 * Provides the shared scaffolding: convenience methods for creating validated text fields
 * (port, address, thread count, backlog size), safe add/remove listener helpers that guard
 * against disposed controls, and a standard "dirty" listener trio that marks the editor
 * as needing save whenever any field changes.
 * Think of it as the Imperial engineering schematic base plate: every specialist department
 * stamps its own section onto this shared template.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class ServerConfigurationEditorPage extends FormPage
{
    /** The default LDAP port */
    protected static final int DEFAULT_PORT_LDAP = 10389;
    
    /** The default LDAPS port */
    protected static final int DEFAULT_PORT_LDAPS = 10636;
    
    /** The default Kerberos port */
    protected static final int DEFAULT_PORT_KERBEROS = 60088;
    
    /** The default LDAPS port */
    protected static final int DEFAULT_PORT_CHANGE_PASSWORD = 60464;
    
    /** The default IPV4 address for servers */
    protected static final String DEFAULT_ADDRESS = "0.0.0.0"; //$NON-NLS-1$
    
    protected static final String TABULATION = "      "; //$NON-NLS-1$
    
    /** A flag to indicate if the page is initialized */
    protected boolean isInitialized = false;

    // Dirty listeners
    private ModifyListener dirtyModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            setEditorDirty();
        }
    };
    
    
    private SelectionListener dirtySelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            setEditorDirty();
        }
    };
    
    
    private ISelectionChangedListener dirtySelectionChangedListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            setEditorDirty();
        }
    };


    // ── Stamping This Page Into The Editor ────────────────────────────────────────────────────
    // Each specialist department registers itself: "I am the Kerberos wing, ID=KerberosPage,
    // title='Kerberos Server'."  The editor receives all three and wires this page into the tab strip.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new editor page and associates it with the given editor.
     * Passes the id and title to the Eclipse forms framework.
     *
     * @param editor  the parent {@link ServerConfigurationEditor}
     * @param id      the unique page identifier (typically the class name)
     * @param title   the tab title shown in the editor
     */
    public ServerConfigurationEditorPage( ServerConfigurationEditor editor, String id, String title )
    {
        super( editor, id, title );
    }


    // ── Looking Up The Parent Engineering Control Room ────────────────────────────────────────
    // Every wing has a direct line back to the main control room (the ServerConfigurationEditor).
    // This method returns that control room reference.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ServerConfigurationEditor} that hosts this page.
     *
     * @return the parent editor, cast from the Eclipse FormEditor
     */
    public ServerConfigurationEditor getServerConfigurationEditor()
    {
        return ( ServerConfigurationEditor ) getEditor();
    }


    // ── Marking The Schematics As Modified ────────────────────────────────────────────────────
    // When an engineer changes any field on the schematic, the control room logs that the
    // document has unsaved changes — the "dirty" flag lights up on the editor tab.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent {@link ServerConfigurationEditor} as having unsaved changes.
     * Called by the shared dirty listeners whenever any widget value changes.
     */
    protected void setEditorDirty()
    {
        getServerConfigurationEditor().setDirty( true );
    }


    // ── Retrieving The Full Death Star Specification ───────────────────────────────────────────
    // The control room keeps the master spec document ({@link ConfigBean}).  If it doesn't exist
    // yet, we create a fresh empty one and register it before returning.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConfigBean} holding the full server configuration in memory.
     * Creates and registers an empty bean if one does not yet exist.
     *
     * @return the live configuration bean; never {@code null}
     */
    public ConfigBean getConfigBean()
    {
        Configuration configuration = getServerConfigurationEditor().getConfiguration();

        if ( configuration == null )
        {
            configuration = new Configuration( new ConfigBean(), null );
            getServerConfigurationEditor().setConfiguration( configuration );
        }

        return configuration.getConfigBean();
    }


    // ── Retrieving The Directory Service Specification ────────────────────────────────────────
    // The directory service section of the spec covers partitions, interceptors, and general
    // server behaviour.  We create it on-demand if it hasn't been initialised yet.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link DirectoryServiceBean} from the current configuration.
     * Creates and registers a fresh one if none exists yet.
     *
     * @return the directory service bean; never {@code null}
     */
    public DirectoryServiceBean getDirectoryServiceBean()
    {
        DirectoryServiceBean directoryServiceBean = getConfigBean().getDirectoryServiceBean();

        if ( directoryServiceBean == null )
        {
            directoryServiceBean = new DirectoryServiceBean();
            getConfigBean().addDirectoryService( directoryServiceBean );
        }

        return directoryServiceBean;
    }


    // ── Retrieving The Live LDAP Connection ───────────────────────────────────────────────────
    // If the editor was opened against a live server (not a file), there's a Connection object
    // embedded in the editor input.  This method extracts it; returns null for file-based inputs.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live LDAP {@link Connection} backing this editor, if any.
     * Returns {@code null} if the editor is backed by a file rather than a connection.
     *
     * @return the connection, or {@code null} for file-based editor inputs
     */
    public Connection getConnection()
    {
        IEditorInput editorInput = getEditorInput();

        if ( editorInput instanceof ConnectionServerConfigurationInput )
        {
            return ( ( ConnectionServerConfigurationInput ) editorInput ).getConnection();
        }

        return null;
    }


    // ── Building The Standard Page Frame ─────────────────────────────────────────────────────
    // Each page starts with the same standard frame: title, toolbar (import/export), a grid
    // layout — then delegates to the subclass to fill in its own controls.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the standard page frame: sets the title, adds the Import/Export toolbar actions,
     * lays out the body with a single-column grid, then delegates to
     * {@link #createFormContent(Composite, FormToolkit)} for subclass-specific controls.
     * Sets {@link #isInitialized} to {@code true} at the end.
     *
     * @param managedForm  the managed form provided by the Eclipse forms framework
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        ScrolledForm form = managedForm.getForm();
        form.setText( getTitle() );

        Composite parent = form.getBody();
        parent.setLayout( new GridLayout() );

        FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading( form.getForm() );

        ServerConfigurationEditor editor = ( ServerConfigurationEditor ) getEditor();

        IToolBarManager toolbarManager = form.getToolBarManager();
        toolbarManager.add( new EditorImportConfigurationAction( editor ) );
        toolbarManager.add( new Separator() );
        toolbarManager.add( new EditorExportConfigurationAction( editor ) );

        toolbarManager.update( true );

        createFormContent( parent, toolkit );

        isInitialized = true;
    }


    // ── The Subclass Fills In Its Own Specialist Controls ─────────────────────────────────────
    // The weapons team fills in the superlaser settings; the comms team fills in port fields.
    // Each subclass implements this to build its own widget tree inside the shared frame.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Subclasses must implement this to build the page's SWT widget tree inside the
     * shared form frame created by {@link #createFormContent(IManagedForm)}.
     *
     * @param parent   the parent composite to add widgets into
     * @param toolkit  the form toolkit for creating themed widgets
     */
    protected abstract void createFormContent( Composite parent, FormToolkit toolkit );


    // ── Refreshing The Page After The Config Changes ──────────────────────────────────────────
    // When the configuration object is replaced (e.g., after import), every page must
    // re-read the new values and repaint its widgets.  This abstract method enforces that.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Subclasses must implement this to re-read all values from the current configuration
     * and update the page's widgets accordingly.
     * Called by the editor after the configuration is loaded or replaced.
     */
    protected abstract void refreshUI();


    // ── Checking Whether The Page Has Been Initialised ────────────────────────────────────────
    // The editor must not call refreshUI() before the page's widgets are built.
    // isInitialized gates that call: true once createFormContent has finished.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this page's widgets have been fully initialised
     * (i.e., {@link #createFormContent(IManagedForm)} has completed).
     *
     * @return {@code true} if the page is ready for refreshUI() calls
     */
    public boolean isInitialized()
    {
        return isInitialized;
    }


    // ── Creating A Validated Port Number Field ────────────────────────────────────────────────
    // Port numbers are integers between 0 and 65535.  The field rejects any non-digit keystroke.
    // Maximum 5 characters keeps it within the valid range.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns a port-number text widget that accepts only digits (0–65535),
     * limited to 5 characters.
     *
     * @param toolkit  the form toolkit for theming
     * @param parent   the parent composite
     * @return the configured port text widget
     */
    protected Text createPortText( FormToolkit toolkit, Composite parent )
    {
        Text portText = toolkit.createText( parent, "" ); //$NON-NLS-1$
        GridData gd = new GridData( SWT.NONE, SWT.NONE, false, false );
        gd.widthHint = 42;
        portText.setLayoutData( gd );
        
        portText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                // Check that it's a valid port. It should be
                // any value between 0 and 65535
                // Skip spaces on both sides
                char[] port = e.text.trim().toCharArray();

                if ( port.length > 0 )
                {
                    for ( char c : port )
                    {
                        if ( ( c < '0' ) || ( c > '9' ) )
                        {
                            // This is an error
                            e.doit = false;
                            break;
                        }
                    }
                }
            }
        } );
        
        
        // the port can only have 5 chars max
        portText.setTextLimit( 5 );

        return portText;
    }


    // ── Creating An Address Field With Live Validation ────────────────────────────────────────
    // If the engineer types a bad IP/hostname, the field turns red immediately — no waiting
    // until they click Save to find out something is wrong.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns a host-address text widget that turns the text red when the
     * entered value fails a DNS/IP resolution check, and reverts to the default colour
     * when the address becomes valid again.
     * Maximum 256 characters.
     *
     * @param toolkit  the form toolkit for theming
     * @param parent   the parent composite
     * @return the configured address text widget
     */
    protected Text createAddressText( FormToolkit toolkit, Composite parent )
    {
        final Text addressText = toolkit.createText( parent, "" ); //$NON-NLS-1$
        GridData gd = new GridData( SWT.NONE, SWT.NONE, false, false );
        gd.widthHint = 200;
        addressText.setLayoutData( gd );
        
        addressText.addModifyListener( new ModifyListener()
        {
            Display display = addressText.getDisplay();

            // Check that the address is valid
            public void modifyText( ModifyEvent e )
            {
                Text addressText = (Text)e.widget;
                String address = addressText.getText();
                
                try
                {
                    InetAddress.getAllByName( address );
                    addressText.setForeground( null );
                }
                catch ( UnknownHostException uhe )
                {
                    addressText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                }
            }
        } );
        
        // An address can be fairly long...
        addressText.setTextLimit( 256 );

        return addressText;
    }


    // ── Creating A Thread-Count Field ─────────────────────────────────────────────────────────
    // The thread pool size is an integer 0–999.  Three digits max keeps it within range.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns a thread-count text widget that accepts only digits (0–999),
     * limited to 3 characters.
     *
     * @param toolkit  the form toolkit for theming
     * @param parent   the parent composite
     * @return the configured thread-count text widget
     */
    protected Text createNbThreadsText( FormToolkit toolkit, Composite parent )
    {
        Text nbThreadsText = toolkit.createText( parent, "" ); //$NON-NLS-1$
        GridData gd = new GridData( SWT.NONE, SWT.NONE, false, false );
        gd.widthHint = 42;
        nbThreadsText.setLayoutData( gd );
        
        nbThreadsText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                // Check that it's a valid number of threads. It should be
                // any value between 0 and 999
                // Skip spaces on both sides
                char[] nbThreads = e.text.trim().toCharArray();

                if ( nbThreads.length > 0 )
                {
                    for ( char c : nbThreads )
                    {
                        if ( ( c < '0' ) || ( c > '9' ) )
                        {
                            // This is an error
                            e.doit = false;
                            break;
                        }
                    }
                }
            }
        } );
        
        
        // We can't have more than 999 threads
        nbThreadsText.setTextLimit( 3 );

        return nbThreadsText;
    }


    // ── Creating A Backlog-Size Field ─────────────────────────────────────────────────────────
    // The TCP accept backlog is an integer 0–99999.  Five digits max keeps it in range.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns a TCP accept-backlog-size text widget that accepts only digits
     * (0–99999), limited to 5 characters.
     *
     * @param toolkit  the form toolkit for theming
     * @param parent   the parent composite
     * @return the configured backlog-size text widget
     */
    protected Text createBackLogSizeText( FormToolkit toolkit, Composite parent )
    {
        Text backLogSizetText = toolkit.createText( parent, "" ); //$NON-NLS-1$
        GridData gd = new GridData( SWT.NONE, SWT.NONE, false, false );
        gd.widthHint = 42;
        backLogSizetText.setLayoutData( gd );
        
        backLogSizetText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                // Check that it's a valid size. It should be
                // any value between 0 and 99999
                // Skip spaces on both sides
                char[] backlogSize = e.text.trim().toCharArray();

                if ( backlogSize.length > 0 )
                {
                    for ( char c : backlogSize )
                    {
                        if ( ( c < '0' ) || ( c > '9' ) )
                        {
                            // This is an error
                            e.doit = false;
                            break;
                        }
                    }
                }
            }
        } );
        
        
        // the backlog size can only have 5 chars max
        backLogSizetText.setTextLimit( 5 );

        return backLogSizetText;
    }


    // ── Creating A "Default Value" Hint Label ─────────────────────────────────────────────────
    // Below a port field we often show "Default: 10389" in the engineering-spec colour so the
    // engineer knows what value they're overriding.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a styled "Default: {value}" hint label below a field.
     * Uses the Studio keyword colour so it stands out from regular labels.
     *
     * @param toolkit  the form toolkit for theming
     * @param parent   the parent composite
     * @param text     the default value string to embed in the "Default: {text}" message
     * @return the styled hint label
     */
    protected Label createDefaultValueLabel( FormToolkit toolkit, Composite parent, String text )
    {
        Label label = toolkit.createLabel( parent,
            NLS.bind( Messages.getString( "ServerConfigurationEditorPage.DefaultWithValue" ), text ), SWT.WRAP ); //$NON-NLS-1$
        label.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.KEYWORD_1_COLOR ) );

        return label;
    }


    // ── Making A Section Header Bold ──────────────────────────────────────────────────────────
    // Section headers in the Imperial schematics are in bold so they're easy to scan.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Applies a bold font to the given label and returns it.
     * Uses JFace's font registry to find the bold variant of the label's current font.
     *
     * @param label  the label to make bold
     * @return the same label, now with a bold font applied
     */
    protected Label setBold( Label label )
    {
        FontData fontData = label.getFont().getFontData()[0];
        Font boldFont = JFaceResources.getFontRegistry().getBold( fontData.getName() );
        label.setFont( boldFont );

        return label;
    }


    // ── Safely Adding A Modify Listener ───────────────────────────────────────────────────────
    // We guard against null controls and disposed widgets — a disposed SWT widget will throw
    // if you try to add a listener to it.  These helpers make all callers safe.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link ModifyListener} to a Text widget only if the widget is non-null and not disposed.
     *
     * @param text      the Text control to listen on
     * @param listener  the listener to add
     */
    protected void addModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.addModifyListener( listener );
        }
    }


    /**
     * Adds an {@link ISelectionChangedListener} to a Viewer only if it is non-null and not disposed.
     *
     * @param viewer    the viewer to listen on
     * @param listener  the listener to add
     */
    protected void addSelectionChangedListener( Viewer viewer, ISelectionChangedListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.addSelectionChangedListener( listener );
        }
    }


    /**
     * Adds an {@link IDoubleClickListener} to a StructuredViewer only if it is non-null and not disposed.
     *
     * @param viewer    the structured viewer to listen on
     * @param listener  the double-click listener to add
     */
    protected void addDoubleClickListener( StructuredViewer viewer, IDoubleClickListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.addDoubleClickListener( listener );
        }
    }


    /**
     * Adds a {@link SelectionListener} to a Button only if it is non-null and not disposed.
     *
     * @param button    the button to listen on
     * @param listener  the selection listener to add
     */
    protected void addSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.addSelectionListener( listener );
        }
    }


    // ── Safely Removing Listeners ─────────────────────────────────────────────────────────────
    // The page is refreshed by first removing all dirty listeners, updating widgets silently,
    // then re-adding them.  These remove helpers guard against disposed controls.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a {@link ModifyListener} from a Text widget only if non-null and not disposed.
     *
     * @param text      the Text control
     * @param listener  the listener to remove
     */
    protected void removeModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.removeModifyListener( listener );
        }
    }


    /**
     * Removes an {@link ISelectionChangedListener} from a Viewer only if non-null and not disposed.
     *
     * @param viewer    the viewer control
     * @param listener  the listener to remove
     */
    protected void removeSelectionChangedListener( Viewer viewer, ISelectionChangedListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.removeSelectionChangedListener( listener );
        }
    }


    /**
     * Removes an {@link IDoubleClickListener} from a StructuredViewer only if non-null and not disposed.
     *
     * @param viewer    the structured viewer
     * @param listener  the double-click listener to remove
     */
    protected void removeDoubleClickListener( StructuredViewer viewer, IDoubleClickListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.removeDoubleClickListener( listener );
        }
    }


    /**
     * Removes a {@link SelectionListener} from a Button only if non-null and not disposed.
     *
     * @param button    the button control
     * @param listener  the selection listener to remove
     */
    protected void removeSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.removeSelectionListener( listener );
        }
    }


    // ── Wiring A Control Into The Unsaved-Changes Alarm ──────────────────────────────────────
    // Whenever an engineer touches a field on the Death Star schematics, the control room's
    // "unsaved changes" alarm trips.  These helpers wire the shared dirty listeners into Text,
    // Button, and Viewer controls so that any change automatically marks the editor dirty.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty {@link ModifyListener} to a Text widget so that any text
     * change immediately marks the editor as having unsaved changes.
     *
     * @param text  the Text control to monitor for changes
     */
    protected void addDirtyListener( Text text )
    {
        addModifyListener( text, dirtyModifyListener );
    }


    /**
     * Attaches the shared dirty {@link SelectionListener} to a Button widget so that any
     * selection change immediately marks the editor as having unsaved changes.
     *
     * @param button  the Button control to monitor for changes
     */
    protected void addDirtyListener( Button button )
    {
        addSelectionListener( button, dirtySelectionListener );
    }


    /**
     * Attaches the shared dirty {@link ISelectionChangedListener} to a Viewer so that any
     * selection change immediately marks the editor as having unsaved changes.
     *
     * @param viewer  the Viewer to monitor for selection changes
     */
    protected void addDirtyListener( Viewer viewer )
    {
        addSelectionChangedListener( viewer, dirtySelectionChangedListener );
    }


    // ── Silencing The Alarm Before A Silent Update ────────────────────────────────────────────
    // When refreshUI() reloads config values into the widgets, we don't want every setText()
    // call to trip the "unsaved changes" alarm.  These helpers detach the shared dirty listeners
    // so we can update controls silently, then re-attach them once the refresh is done.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the shared dirty {@link ModifyListener} from a Text widget so that the
     * next programmatic text change does not mark the editor dirty.
     *
     * @param text  the Text control to stop monitoring
     */
    protected void removeDirtyListener( Text text )
    {
        removeModifyListener( text, dirtyModifyListener );
    }


    /**
     * Detaches the shared dirty {@link SelectionListener} from a Button so that the
     * next programmatic selection change does not mark the editor dirty.
     *
     * @param button  the Button control to stop monitoring
     */
    protected void removeDirtyListener( Button button )
    {
        removeSelectionListener( button, dirtySelectionListener );
    }


    /**
     * Detaches the shared dirty {@link ISelectionChangedListener} from a Viewer so that
     * the next programmatic selection change does not mark the editor dirty.
     *
     * @param viewer  the Viewer to stop monitoring
     */
    protected void removeDirtyListener( Viewer viewer )
    {
        removeSelectionChangedListener( viewer, dirtySelectionChangedListener );
    }


    // ── Moving A Control's Indicator To A New Position ───────────────────────────────────────
    // When refreshUI() reloads the config, each checkbox and list must be positioned to reflect
    // the current setting.  These helpers move the Button or Viewer selection safely, guarding
    // against disposed controls so a partially-built page never throws.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the checked/selected state of a Button widget only if it is non-null and not disposed.
     * Safe to call during refreshUI() even if the widget hasn't been created yet.
     *
     * @param button    the Button control (checkbox or radio)
     * @param selected  the new selection state to apply
     */
    protected void setSelection( Button button, boolean selected )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) )
        {
            button.setSelection( selected );
        }
    }


    /**
     * Sets the selection of a Viewer to the given object, wrapping it in a
     * {@link StructuredSelection}.  Only applied if the viewer and its control are non-null
     * and not disposed.
     *
     * @param viewer     the Viewer to update
     * @param selection  the object to select
     */
    protected void setSelection( Viewer viewer, Object selection )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() ) )
        {
            viewer.setSelection( new StructuredSelection( selection ) );
        }
    }


    // ── Writing A New Value Into A Schematic Field ────────────────────────────────────────────
    // When the config is refreshed, each text field gets the latest value written into it.
    // A null string is treated as empty so the field is never left in an undefined state.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the text content of a Text widget only if it is non-null and not disposed.
     * A {@code null} string is normalised to an empty string before assignment.
     *
     * @param text    the Text control to update
     * @param string  the new value to write; {@code null} is treated as {@code ""}
     */
    protected void setText( Text text, String string )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) )
        {
            if ( string == null )
            {
                string = ""; //$NON-NLS-1$
            }

            text.setText( string );
        }
    }


    // ── Directing The Engineer's Attention To A Specific Control ─────────────────────────────
    // On the Death Star bridge, when a fault is detected the duty officer directs all eyes to
    // the relevant panel.  setFocus() does the same: keyboard focus lands on the given widget.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the given control only if it is non-null and not disposed.
     * Safe to call during page initialisation before all widgets exist.
     *
     * @param control  the control that should receive keyboard focus
     */
    protected void setFocus( Control control )
    {
        if ( ( control != null ) && ( !control.isDisposed() ) )
        {
            control.setFocus();
        }
    }


    // ── Powering A System On Or Off ───────────────────────────────────────────────────────────
    // When Kerberos is disabled in the config, the Kerberos port field greys out — the system
    // is powered down.  When it's re-enabled, the field lights up again.  setEnabled() handles
    // this safely: if the control is gone we simply do nothing.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the enabled state of a control only if it is non-null and not disposed.
     * Disabled controls are greyed out and not interactive.
     *
     * @param control  the control to enable or disable
     * @param enabled  {@code true} to enable the control; {@code false} to grey it out
     */
    protected void setEnabled( Control control, boolean enabled )
    {
        if ( ( control != null ) && ( !control.isDisposed() ) )
        {
            control.setEnabled( enabled );
        }
    }


    // ── Fitting A Control Into A Standardised Grid Slot ──────────────────────────────────────
    // The schematic template mandates a standard minimum width (50 pixels) for all data fields
    // so the page looks uniform regardless of which specialist filled it in.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Applies the given {@link GridData} to a control and sets its width hint to the standard
     * default of 50 pixels before attaching it.
     * Ensures a consistent minimum field width across all tab pages.
     *
     * @param control  the control to lay out
     * @param gd       the GridData to configure and attach (its widthHint is overwritten to 50)
     */
    protected void setGridDataWithDefaultWidth( Control control, GridData gd )
    {
        gd.widthHint = 50;
        control.setLayoutData( gd );
    }
    
    
    // ── Stamping A New Wing Onto The Schematic Page ───────────────────────────────────────────
    // Each tab page is divided into named sections: "LDAP Server", "LDAPS Server", "Advanced".
    // This helper stamps a new section block onto the page with a standard grid layout inside,
    // ready for the specialist team to fill in their controls.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a titled, bordered {@link Section} and returns the inner {@link Composite}
     * laid out with an {@code nbColumns}-column {@link GridLayout}.
     * All tab pages use this helper to build their named sub-sections uniformly.
     *
     * <p>For example — the Kerberos page stamps its server section:</p>
     * <pre>
     *   Composite kerberosSection = createSection(toolkit, parent,
     *       "KerberosServerPage.ServerSection", 2, Section.TITLE_BAR);
     *   // then adds port label + field into kerberosSection
     * </pre>
     *
     * @param toolkit    the form toolkit for themed widget creation
     * @param parent     the composite to add the section into
     * @param title      i18n key for the section heading (looked up via {@link Messages})
     * @param nbColumns  number of columns in the inner grid
     * @param style      SWT/Section style flags (e.g. {@code Section.TITLE_BAR | Section.TWISTIE})
     * @return the inner composite, ready for child controls
     */
    protected Composite createSection( FormToolkit toolkit, Composite parent, String title, int nbColumns, int style )
    {
        Section section = toolkit.createSection( parent, style );
        section.setText( Messages.getString( title ) );
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( nbColumns, false );
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        return composite;
    }
}
