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


// ── CLASS: ServerConfigurationEditorPage — DEATH STAR WORKSTATION BASE PLATE ─────────────
// Each engineering workstation inside the Death Star's command center (the FormEditor) is
// built on the same base frame: it knows which director to report changes to, how to mark
// itself dirty, how to build validated port/address/thread-count text boxes, and how to
// add the Import/Export toolbar buttons that every tab shares.
// Concrete pages (OverviewPage, LdapLdapsServersPage, PartitionsPage, ...) extend this class
// and inherit all those common services, filling in only the content specific to their tab.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all tabs in the {@link ServerConfigurationEditor}.
 * Provides shared infrastructure: dirty-listener wiring, access to the config/directory-service
 * beans, validated input factory methods (port, address, thread count, backlog), section
 * builders, and safe widget-update helpers that check for disposal before touching the UI.
 * Think of it as the base frame every Death Star engineering workstation is bolted onto.
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


    // ── Wiring A Page Into Its Parent Editor ──────────────────────────────────────────────────
    // Each page reports dirty state and fetches config beans through the parent editor.
    // This constructor gives Eclipse the id and title it needs to manage the tab.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new page and registers it with the given editor.
     *
     * @param editor  the parent {@link ServerConfigurationEditor} that hosts this page
     * @param id      a unique string identifier for this page (used by Eclipse internally)
     * @param title   the tab title shown in the editor
     */
    public ServerConfigurationEditorPage( ServerConfigurationEditor editor, String id, String title )
    {
        super( editor, id, title );
    }


    // ── Getting A Reference Back To The Parent Editor ─────────────────────────────────────────
    // Subclasses need to call editor-level operations (setDirty, getConfiguration).
    // This helper casts the generic FormPage editor reference to the concrete type.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ServerConfigurationEditor} that hosts this page.
     * Convenience cast of {@link FormPage#getEditor()}.
     *
     * @return the parent editor
     */
    public ServerConfigurationEditor getServerConfigurationEditor()
    {
        return ( ServerConfigurationEditor ) getEditor();
    }


    // ── Marking The Editor As Having Unsaved Changes ──────────────────────────────────────────
    // When the user changes any widget value, we need the editor's title bar asterisk to appear
    // and the Save button to become enabled.  This delegates to the editor's setDirty method.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent editor as dirty (unsaved changes pending).
     * Call this from any widget listener that the user can change.
     */
    protected void setEditorDirty()
    {
        getServerConfigurationEditor().setDirty( true );
    }


    // ── Fetching The Root Configuration Bean ──────────────────────────────────────────────────
    // All pages work off the same ConfigBean stored in the editor.  If none exists yet,
    // we create a default one so the page can populate its widgets without NPEs.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConfigBean} from the editor's current {@link Configuration}.
     * Creates and registers a new empty {@code Configuration} if none exists yet.
     *
     * @return the current config bean; never {@code null}
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


    // ── Fetching The Directory Service Bean ───────────────────────────────────────────────────
    // Most pages configure the DirectoryService: replication, interceptors, partitions, etc.
    // This helper unwraps it from the ConfigBean, creating a default one if needed.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link DirectoryServiceBean} from the current config bean.
     * Creates and registers a default bean if none is present.
     *
     * @return the current directory-service bean; never {@code null}
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


    // ── Fetching The Live LDAP Connection (If Any) ────────────────────────────────────────────
    // When the editor is backed by a live server connection, pages can call live API operations.
    // If the editor was opened from a file, this returns null and pages operate offline.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Connection} associated with this editor input, or {@code null}
     * if the editor was opened from a file rather than a live server connection.
     *
     * @return the connection, or {@code null}
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


    // ── Building The Page's Scrolled Form And Adding The Toolbar ──────────────────────────────
    // Eclipse calls this when the page's tab is first activated.  We set up the title, add
    // the Import/Export toolbar buttons that every page shares, then delegate to the subclass
    // to build its specific form content.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to build the page's scrolled form.
     * Sets the form title, adds the Import/Export toolbar buttons, calls the subclass
     * {@link #createFormContent(Composite, FormToolkit)} method, and sets the initialized flag.
     *
     * @param managedForm  the managed form provided by the Eclipse Forms framework
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


    // ── Subclass Builds Its Specific Form Content ─────────────────────────────────────────────
    // Each concrete page overrides this to lay out its own sections, labels, and controls.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Subclasses implement this to build their page-specific form content.
     *
     * @param parent   the parent composite inside the scrolled form body
     * @param toolkit  the form toolkit for creating styled widgets
     */
    protected abstract void createFormContent( Composite parent, FormToolkit toolkit );


    // ── Refreshing The UI From The Current Model State ────────────────────────────────────────
    // After a configuration load or reset, all pages need to repopulate their widgets.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Subclasses implement this to refresh all their widgets from the current model state.
     * Called after a configuration is loaded or reset.
     */
    protected abstract void refreshUI();


    // ── Checking Whether The Page Has Been Initialised ────────────────────────────────────────
    // refreshUI() is unsafe to call before createFormContent() has run — controls don't exist.
    // The editor checks this flag before calling refreshUI() on a page.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this page's form has been built and is safe to call {@link #refreshUI()} on.
     *
     * @return {@code true} if the form has been initialised; {@code false} otherwise
     */
    public boolean isInitialized()
    {
        return isInitialized;
    }


    // ── Creating A Validated Port Number Text Box ─────────────────────────────────────────────
    // Port numbers must be 0-65535.  We attach a VerifyListener that rejects non-digit input
    // before it reaches the text, and limit the field to 5 characters.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a port-number {@link Text} widget that only accepts digits (0–65535).
     * Fixed width of 42px; limited to 5 characters.
     *
     * @param toolkit  the form toolkit
     * @param parent   the parent composite
     * @return a ready-to-use port text widget
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


    // ── Creating A Validated IP Address Text Box ──────────────────────────────────────────────
    // We can't block bad input keystroke-by-keystroke (addresses have non-numeric characters),
    // so instead we turn the text red while the address is invalid and restore it when it
    // parses correctly.  Width 200px; limit 256 characters.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an IP address {@link Text} widget that turns red when the entered address is invalid.
     * Uses {@link InetAddress#getAllByName(String)} to validate on every keystroke.
     * Fixed width 200px; limited to 256 characters.
     *
     * @param toolkit  the form toolkit
     * @param parent   the parent composite
     * @return a ready-to-use address text widget with live validation colouring
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


    // ── Creating A Validated Thread-Count Text Box ────────────────────────────────────────────
    // Thread counts must be 0-999.  Digits only; limited to 3 characters.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a thread-count {@link Text} widget that only accepts digits (0–999).
     * Fixed width of 42px; limited to 3 characters.
     *
     * @param toolkit  the form toolkit
     * @param parent   the parent composite
     * @return a ready-to-use thread-count text widget
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


    // ── Creating A Validated Backlog-Size Text Box ────────────────────────────────────────────
    // Backlog sizes must be 0-99999.  Digits only; limited to 5 characters.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a connection-backlog-size {@link Text} widget that only accepts digits (0–99999).
     * Fixed width of 42px; limited to 5 characters.
     *
     * @param toolkit  the form toolkit
     * @param parent   the parent composite
     * @return a ready-to-use backlog-size text widget
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


    // ── Creating A Coloured Default-Value Label ────────────────────────────────────────────────
    // Some fields show a "(default: X)" hint in the keyword colour to help the engineer know
    // what the out-of-the-box value is without having to look it up.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a label showing the default value for a setting, styled in the keyword colour.
     * The text is formatted as the localised "Default: {value}" pattern.
     *
     * @param toolkit  the form toolkit
     * @param parent   the parent composite
     * @param text     the default value to display
     * @return the styled default-value label
     */
    protected Label createDefaultValueLabel( FormToolkit toolkit, Composite parent, String text )
    {
        Label label = toolkit.createLabel( parent,
            NLS.bind( Messages.getString( "ServerConfigurationEditorPage.DefaultWithValue" ), text ), SWT.WRAP ); //$NON-NLS-1$
        label.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.KEYWORD_1_COLOR ) );

        return label;
    }


    // ── Making A Label Bold ────────────────────────────────────────────────────────────────────
    // Section header labels or important hints get bolded by swapping in the JFace bold font.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the given label's font to bold and returns it.
     * Looks up the bold variant of the label's current font via the JFace font registry.
     *
     * @param label  the label to embolden
     * @return the same label, now with a bold font
     */
    protected Label setBold( Label label )
    {
        FontData fontData = label.getFont().getFontData()[0];
        Font boldFont = JFaceResources.getFontRegistry().getBold( fontData.getName() );
        label.setFont( boldFont );

        return label;
    }


    // ── Safely Adding A Modify Listener ───────────────────────────────────────────────────────
    // Widget disposal races are common in Eclipse editors; we check before adding to avoid NPEs.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link ModifyListener} to the given {@link Text}, guarding against null or disposed widgets.
     *
     * @param text      the text widget
     * @param listener  the listener to add
     */
    protected void addModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.addModifyListener( listener );
        }
    }


    // ── Safely Adding A Selection-Changed Listener ────────────────────────────────────────────
    // Same disposal-safe guard for viewers.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an {@link ISelectionChangedListener} to the given {@link Viewer}, guarding against
     * null or disposed controls.
     *
     * @param viewer    the viewer
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


    // ── Safely Adding A Double-Click Listener ─────────────────────────────────────────────────
    // Guard pattern for double-click listeners on structured viewers.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an {@link IDoubleClickListener} to the given {@link StructuredViewer}, guarding against
     * null or disposed controls.
     *
     * @param viewer    the structured viewer
     * @param listener  the listener to add
     */
    protected void addDoubleClickListener( StructuredViewer viewer, IDoubleClickListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.addDoubleClickListener( listener );
        }
    }


    // ── Safely Adding A Selection Listener To A Button ────────────────────────────────────────
    // Guard pattern for button selection listeners.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link SelectionListener} to the given {@link Button}, guarding against null or
     * disposed controls.
     *
     * @param button    the button
     * @param listener  the listener to add
     */
    protected void addSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.addSelectionListener( listener );
        }
    }


    // ── Safely Removing A Modify Listener ─────────────────────────────────────────────────────
    // Guard pattern for removing modify listeners — needed when refreshUI() temporarily
    // detaches dirty listeners while repopulating widgets.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a {@link ModifyListener} from the given {@link Text}, guarding against null or
     * disposed controls.
     *
     * @param text      the text widget
     * @param listener  the listener to remove
     */
    protected void removeModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.removeModifyListener( listener );
        }
    }


    // ── Safely Removing A Selection-Changed Listener ──────────────────────────────────────────
    // Guard pattern for viewer selection-changed listeners.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes an {@link ISelectionChangedListener} from the given {@link Viewer}, guarding against
     * null or disposed controls.
     *
     * @param viewer    the viewer
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


    // ── Safely Removing A Double-Click Listener ───────────────────────────────────────────────
    // Guard pattern for double-click listener removal.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes an {@link IDoubleClickListener} from the given {@link StructuredViewer}, guarding
     * against null or disposed controls.
     *
     * @param viewer    the structured viewer
     * @param listener  the listener to remove
     */
    protected void removeDoubleClickListener( StructuredViewer viewer, IDoubleClickListener listener )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() )
            && ( listener != null ) )
        {
            viewer.removeDoubleClickListener( listener );
        }
    }


    // ── Safely Removing A Selection Listener From A Button ────────────────────────────────────
    // Guard pattern for button selection listener removal.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a {@link SelectionListener} from the given {@link Button}, guarding against null or
     * disposed controls.
     *
     * @param button    the button
     * @param listener  the listener to remove
     */
    protected void removeSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.removeSelectionListener( listener );
        }
    }


    // ── Attaching The Shared Dirty Listener To A Text Widget ──────────────────────────────────
    // Delegates to addModifyListener with the shared dirtyModifyListener instance.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty modify listener to the given {@link Text}.
     * Any change to this text will mark the editor dirty.
     *
     * @param text  the text widget to watch
     */
    protected void addDirtyListener( Text text )
    {
        addModifyListener( text, dirtyModifyListener );
    }


    // ── Attaching The Shared Dirty Listener To A Button ───────────────────────────────────────
    // Delegates to addSelectionListener with the shared dirtySelectionListener instance.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty selection listener to the given {@link Button}.
     * Any click on this button will mark the editor dirty.
     *
     * @param button  the button to watch
     */
    protected void addDirtyListener( Button button )
    {
        addSelectionListener( button, dirtySelectionListener );
    }


    // ── Attaching The Shared Dirty Listener To A Viewer ───────────────────────────────────────
    // Delegates to addSelectionChangedListener with the shared dirtySelectionChangedListener.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty selection-changed listener to the given {@link Viewer}.
     * Any selection change will mark the editor dirty.
     *
     * @param viewer  the viewer to watch
     */
    protected void addDirtyListener( Viewer viewer )
    {
        addSelectionChangedListener( viewer, dirtySelectionChangedListener );
    }


    // ── Detaching The Shared Dirty Listener From A Text Widget ────────────────────────────────
    // Used during refreshUI() to prevent repopulation from triggering dirty.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the shared dirty modify listener from the given {@link Text}.
     *
     * @param text  the text widget to stop watching
     */
    protected void removeDirtyListener( Text text )
    {
        removeModifyListener( text, dirtyModifyListener );
    }


    // ── Detaching The Shared Dirty Listener From A Button ────────────────────────────────────
    // Used during refreshUI() to silence selection events triggered by programmatic updates.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the shared dirty selection listener from the given {@link Button}.
     *
     * @param button  the button to stop watching
     */
    protected void removeDirtyListener( Button button )
    {
        removeSelectionListener( button, dirtySelectionListener );
    }


    // ── Detaching The Shared Dirty Listener From A Viewer ────────────────────────────────────
    // Used during refreshUI() to silence selection events triggered by programmatic selection.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the shared dirty selection-changed listener from the given {@link Viewer}.
     *
     * @param viewer  the viewer to stop watching
     */
    protected void removeDirtyListener( Viewer viewer )
    {
        removeSelectionChangedListener( viewer, dirtySelectionChangedListener );
    }


    // ── Safely Setting A Button's Selected State ──────────────────────────────────────────────
    // Used in refreshUI() when repopulating checkboxes/radio buttons from model state.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the selection state of a {@link Button}, guarding against null or disposed controls.
     *
     * @param button    the button to update
     * @param selected  the new selection state
     */
    protected void setSelection( Button button, boolean selected )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) )
        {
            button.setSelection( selected );
        }
    }


    // ── Safely Setting A Viewer's Selected Item ────────────────────────────────────────────────
    // Used in refreshUI() when repopulating viewers from model state.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the selected item in a {@link Viewer}, guarding against null or disposed controls.
     * Wraps the value in a {@link StructuredSelection}.
     *
     * @param viewer     the viewer to update
     * @param selection  the object to select
     */
    protected void setSelection( Viewer viewer, Object selection )
    {
        if ( ( viewer != null ) && ( viewer.getControl() != null ) && ( !viewer.getControl().isDisposed() ) )
        {
            viewer.setSelection( new StructuredSelection( selection ) );
        }
    }


    // ── Safely Setting A Text Widget's Content ────────────────────────────────────────────────
    // Used in refreshUI() when populating text fields from model state.
    // Null model values become empty strings so the widget doesn't blow up.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the content of a {@link Text} widget, guarding against null or disposed controls.
     * A null string value is treated as an empty string.
     *
     * @param text    the text widget to update
     * @param string  the new text (may be {@code null})
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


    // ── Safely Focusing A Control ─────────────────────────────────────────────────────────────
    // Used when directing keyboard focus to a specific field after opening a section.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Requests focus for the given {@link Control}, guarding against null or disposed controls.
     *
     * @param control  the control to focus
     */
    protected void setFocus( Control control )
    {
        if ( ( control != null ) && ( !control.isDisposed() ) )
        {
            control.setFocus();
        }
    }


    // ── Safely Enabling Or Disabling A Control ────────────────────────────────────────────────
    // Used when a checkbox state changes and dependent fields need to be enabled/disabled.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the enabled state of a {@link Control}, guarding against null or disposed controls.
     *
     * @param control  the control to enable or disable
     * @param enabled  {@code true} to enable; {@code false} to disable
     */
    protected void setEnabled( Control control, boolean enabled )
    {
        if ( ( control != null ) && ( !control.isDisposed() ) )
        {
            control.setEnabled( enabled );
        }
    }


    // ── Applying A GridData With A Standard Default Width ─────────────────────────────────────
    // A convenience method for controls that should use a 50px width hint.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Applies the given {@link GridData} to the control and sets the width hint to 50px.
     *
     * @param control  the control to configure
     * @param gd       the grid data to apply (width hint will be set to 50)
     */
    protected void setGridDataWithDefaultWidth( Control control, GridData gd )
    {
        gd.widthHint = 50;
        control.setLayoutData( gd );
    }


    // ── Creating A Titled Section With A Grid Layout Inside ───────────────────────────────────
    // Almost every page section follows the same pattern: a titled Section widget, a Composite
    // inside with a GridLayout, and a bit of border painting.  This factory does it all.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a titled {@link Section} with a grid-layout composite inside it.
     * The section title is looked up via {@link Messages#getString(String)}, so pass the
     * message key rather than the raw string.
     *
     * @param toolkit     the form toolkit
     * @param parent      the parent composite to attach the section to
     * @param title       the message key for the section title
     * @param nbColumns   number of columns in the section's inner GridLayout
     * @param style       section style flags (e.g. {@code Section.TITLE_BAR | Section.EXPANDED})
     * @return the inner composite that page content should be added to
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
