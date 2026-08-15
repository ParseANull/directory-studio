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
package org.apache.directory.studio.openldap.config.editor.pages;


import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.common.ui.widgets.TableWidget;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.openldap.config.actions.EditorExportConfigurationAction;
import org.apache.directory.studio.openldap.config.actions.EditorImportConfigurationAction;
import org.apache.directory.studio.openldap.config.editor.Messages;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: OpenLDAPServerConfigurationEditorPage — Bridge of a Star Destroyer ──
// The bridge of a Star Destroyer has multiple workstations, but they all share
// the same infrastructure: the same power grid, the same communication network,
// the same command hierarchy.  OpenLDAPServerConfigurationEditorPage is that
// shared infrastructure for every editor tab: it provides common services
// (reaching the configuration model, marking the editor dirty, creating
// standard widgets and sections, managing listener registration) that each
// specific page tab inherits and builds on.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all tab pages in the OpenLDAP server configuration
 * editor.
 * Each concrete page (Overview, Databases, Security, etc.) extends this class
 * and inherits helpers for creating common UI elements, wiring dirty listeners,
 * and accessing the shared {@link OpenLdapConfiguration} model.
 * Think of it as the Star Destroyer bridge infrastructure — every workstation
 * plugs into the same power, comms, and command grid.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class OpenLDAPServerConfigurationEditorPage extends FormPage
{
    protected static final String TABULATION = "      ";

    /** A flag to indicate if the page is initialized */
    protected boolean isInitialized = false;

    /**
     * A listener used to set the dirty flag when a Text is updated
     */
    protected ModifyListener dirtyModifyListener = event -> setEditorDirty();

    /**
     * A listener used to set the dirty flag when a widget is selected
     */
    private SelectionListener dirtySelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEditorDirty();
        }
    };

    /**
     * A listener used to set the dirty flag when a widget is updated
     */
    protected WidgetModifyListener dirtyWidgetModifyListener = event -> setEditorDirty();


    // ── Constructor — A New Workstation Comes Online ───────────────────────────
    // A new bridge workstation is installed and connected to the Star Destroyer's
    // central command computer (the editor).  It needs an ID (so it can be
    // addressed) and a title (so it's labeled on the tab strip).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new editor page and registers it with the given editor.
     * Subclasses call this from their own constructors with their specific
     * page IDs and titles.
     *
     * @param editor  the OpenLdapServerConfigurationEditor that owns this page
     * @param id      the unique page ID (typically the fully qualified class name)
     * @param title   the tab label shown in the editor's tab strip
     */
    public OpenLDAPServerConfigurationEditorPage( OpenLdapServerConfigurationEditor editor, String id, String title )
    {
        super( editor, id, title );
    }


    // ── getServerConfigurationEditor — Bridge Contacts Command Central ─────────
    // Any workstation on the bridge can call up the captain (the editor) at any
    // time by referencing the command terminal — that's what this method gives us.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLdapServerConfigurationEditor that owns this page.
     * We use this to reach shared state like the configuration model and
     * the dirty flag.
     *
     * @return the parent configuration editor, cast from the generic FormEditor
     */
    public OpenLdapServerConfigurationEditor getServerConfigurationEditor()
    {
        return ( OpenLdapServerConfigurationEditor ) getEditor();
    }


    // ── setEditorDirty — Workstation Signals an Unsaved Change ────────────────
    // When a crew member changes a setting at their workstation, they press the
    // "unsaved change" indicator so the captain knows a save is pending.
    // We tell the editor to mark itself dirty, which in turn enables the Save
    // action in Eclipse's toolbar.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent editor as dirty, indicating there are unsaved changes.
     * Called by the various dirty listeners whenever the user modifies any
     * widget on this page.
     */
    private void setEditorDirty()
    {
        getServerConfigurationEditor().setDirty( true );
    }


    // ── getConfiguration — Bridge Fetches the Current Config ──────────────────
    // When a workstation needs to read or write the ship's current configuration,
    // it calls up the configuration from the captain's computer.  If no
    // configuration has been loaded yet, we create an empty one and hand it back
    // rather than returning null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current {@link OpenLdapConfiguration} from the editor.
     * If the editor has no configuration yet (e.g., during initial setup), we
     * create a new empty one and store it before returning — so callers never
     * get null back.
     *
     * @return the current configuration, guaranteed non-null
     */
    public OpenLdapConfiguration getConfiguration()
    {
        OpenLdapConfiguration configuration = getServerConfigurationEditor().getConfiguration();

        if ( configuration == null )
        {
            configuration = new OpenLdapConfiguration();
            getServerConfigurationEditor().setConfiguration( configuration );
        }

        return configuration;
    }


    // ── createFormContent — Bridge Assembles the Workstation Console ──────────
    // The Star Destroyer's engineering team assembles the workstation console:
    // they install the form heading, wire up the import/export toolbar actions
    // that every tab shares, and then hand off to the page-specific buildout.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the common page framework (form heading, toolbar actions) and
     * then delegates to {@link #createFormContent(Composite, FormToolkit)}
     * for page-specific content.
     * The import and export toolbar actions are added here because they apply
     * to every page in the editor.
     *
     * @param managedForm  the managed form context provided by Eclipse Forms
     */
    @Override
    protected void createFormContent( IManagedForm managedForm )
    {
        ScrolledForm form = managedForm.getForm();
        form.setText( getTitle() );

        Composite parent = form.getBody();
        parent.setLayout( new GridLayout() );

        FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading( form.getForm() );

        OpenLdapServerConfigurationEditor editor = ( OpenLdapServerConfigurationEditor ) getEditor();

        IToolBarManager toolbarManager = form.getToolBarManager();
        toolbarManager.add( new EditorImportConfigurationAction( editor ) );
        toolbarManager.add( new Separator() );
        toolbarManager.add( new EditorExportConfigurationAction( editor ) );

        toolbarManager.update( true );

        createFormContent( parent, toolkit );

        isInitialized = true;
    }


    // ── createFormContent (abstract) — Each Station Builds Its Own Panel ──────
    // After the common infrastructure is in place, each bridge station builds
    // its own specific control panel — weapons displays look different from
    // navigation charts.  Subclasses implement this to add their page-specific
    // sections and widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Subclasses implement this to create their page-specific form content.
     * Called from {@link #createFormContent(IManagedForm)} after the common
     * framework (heading, toolbar) has been set up.
     *
     * @param parent   the SWT composite that should receive the page's widgets
     * @param toolkit  the Eclipse Forms toolkit for creating styled widgets
     */
    protected abstract void createFormContent( Composite parent, FormToolkit toolkit );


    // ── refreshUI (abstract) — Each Station Refreshes Its Own Readouts ────────
    // When new sensor data arrives, each station operator refreshes their own
    // displays independently.  Subclasses implement this to reload their widgets
    // from the current model state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Subclasses implement this to refresh their UI widgets from the current
     * configuration model.
     * Called whenever the editor's data changes — e.g., after a background
     * job completes loading from the server.
     */
    public abstract void refreshUI();


    // ── isInitialized — Bridge Checks If the Station Is Ready ─────────────────
    // Before sending data to a workstation, the captain checks whether it has
    // finished booting up.  We set the initialized flag at the end of
    // createFormContent so callers know it's safe to interact with the page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this page has completed its initialization sequence.
     * Calling refreshUI() before initialization is a no-op in most pages, so
     * checking this first avoids NullPointerExceptions during editor startup.
     *
     * @return true if the page's widgets have been created and are ready to use
     */
    public boolean isInitialized()
    {
        return isInitialized;
    }


    // ── createPortText — Workstation Adds a Port Number Field ─────────────────
    // Installing a port-number input at a workstation is a common task: we want
    // a fixed-width field that only accepts digits 0-9 and refuses anything else.
    // This method creates that field with the right width hint and a verifier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a Text widget pre-configured for port number entry: fixed width,
     * digits only (verified on every keystroke), max 5 characters (65535 is the
     * largest valid port).
     *
     * <p>For example — a navigator sets the LDAP port:</p>
     * <pre>
     *   Text portField = createPortText(toolkit, parent);
     *   portField.setText("389");   // only digits, max 5 chars
     * </pre>
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the composite to add the Text widget to
     * @return         the configured port number Text widget
     */
    protected Text createPortText( FormToolkit toolkit, Composite parent )
    {
        Text portText = toolkit.createText( parent, "" ); //$NON-NLS-1$
        GridData gd = new GridData( SWT.NONE, SWT.NONE, false, false );
        gd.widthHint = 42;
        portText.setLayoutData( gd );

        portText.addVerifyListener( event ->
            {
                if ( !event.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    event.doit = false;
                }
            } );

        portText.setTextLimit( 5 );

        return portText;
    }


    // ── createSection (with style) — Workstation Installs a Named Panel ───────
    // Each workstation on the bridge organizes its controls into labeled panels
    // (sections).  This method creates a titled, bordered section using a grid
    // layout with a configurable number of columns.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an Eclipse Forms Section with a title and a GridLayout composite
     * inside it, ready to receive widgets.
     * The section title is looked up from the messages bundle via the given key.
     * Returns the inner composite (not the Section itself) so callers can add
     * widgets directly.
     *
     * @param toolkit    the Eclipse Forms toolkit
     * @param parent     the composite that contains the section
     * @param title      the message-bundle key for the section title
     * @param nbColumns  how many columns the inner GridLayout should have
     * @param style      the Section style flags (e.g., Section.TITLE_BAR)
     * @return           the inner composite inside the section
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


    // ── createDefaultValueLabel — Workstation Shows the Default Hint ──────────
    // When a crew member is uncertain what value to enter, a small annotation
    // next to the field shows the default: "(Default: 389)".  It's rendered in
    // the keyword color so it stands out as informational, not instructional.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a "(Default: value)" label in the keyword-highlight color.
     * We use a distinct color so the user can easily distinguish default
     * hints from regular field labels.
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the composite to add the label to
     * @param text     the default value to display (e.g., "389", "none")
     * @return         the configured Label widget
     */
    protected Label createDefaultValueLabel( FormToolkit toolkit, Composite parent, String text )
    {
        Label label = toolkit.createLabel( parent, NLS.bind( "(Default: {0})", text ) );
        label.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.KEYWORD_1_COLOR ) );

        return label;
    }


    // ── addModifyListener (Text) — Workstation Wires the Text Monitor ─────────
    // Before going live, each workstation connects its text inputs to the
    // monitoring system so any keystrokes trigger the appropriate listener.
    // We guard against null and disposed controls because widget lifecycle
    // in Eclipse can be tricky during page switches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a ModifyListener to the given Text widget if the widget is non-null
     * and not disposed.
     * The null and disposed checks prevent NPEs when listeners are registered
     * before widgets are created or after they've been destroyed.
     *
     * @param text      the Text widget to listen to — may be null
     * @param listener  the ModifyListener to attach — may be null
     */
    protected void addModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.addModifyListener( listener );
        }
    }


    // ── addSelectionListener (Button) — Workstation Wires the Button Monitor ──
    // Same guard pattern as addModifyListener, applied to Button widgets —
    // connecting the workstation's toggle switches to the monitoring system.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a SelectionListener to the given Button widget if it's non-null and
     * not disposed.
     *
     * @param button    the Button widget to listen to — may be null
     * @param listener  the SelectionListener to attach — may be null
     */
    protected void addSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.addSelectionListener( listener );
        }
    }


    // ── addSelectionListener (Combo) — Workstation Wires the Dropdown Monitor ─
    // Same guard pattern applied to Combo dropdowns — connecting the workstation's
    // selection menus to the monitoring system.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a SelectionListener to the given Combo widget if it's non-null and
     * not disposed.
     *
     * @param combo     the Combo widget to listen to — may be null
     * @param listener  the SelectionListener to attach — may be null
     */
    protected void addSelectionListener( Combo combo, SelectionListener listener )
    {
        if ( ( combo != null ) && ( !combo.isDisposed() ) && ( listener != null ) )
        {
            combo.addSelectionListener( listener );
        }
    }


    // ── addModifyListener (TableWidget) — Workstation Wires the Table Monitor ─
    // Same guard pattern for table widgets — connecting the workstation's data
    // tables to the monitoring system.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a WidgetModifyListener to the given TableWidget if both are non-null.
     *
     * @param tableWidget  the TableWidget to listen to — may be null
     * @param listener     the WidgetModifyListener to attach — may be null
     */
    protected void addModifyListener( TableWidget<?> tableWidget, WidgetModifyListener listener )
    {
        if ( ( tableWidget != null ) && ( listener != null ) )
        {
            tableWidget.addWidgetModifyListener( listener );
        }
    }


    // ── removeModifyListener (Text) — Workstation Disconnects the Text Monitor ─
    // Before refreshing a Text field's content programmatically (not via user
    // input), we disconnect the listener so the programmatic change doesn't
    // accidentally mark the editor dirty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a ModifyListener from the given Text widget if it's non-null and
     * not disposed.
     * We call this before updating Text fields from the model during a UI
     * refresh, then re-add the listener afterward.
     *
     * @param text      the Text widget — may be null
     * @param listener  the ModifyListener to remove — may be null
     */
    protected void removeModifyListener( Text text, ModifyListener listener )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) && ( listener != null ) )
        {
            text.removeModifyListener( listener );
        }
    }


    // ── removeSelectionListener (Button) — Workstation Disconnects the Button Monitor ─
    // Same pattern as removeModifyListener, for Button widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a SelectionListener from the given Button widget if it's non-null
     * and not disposed.
     *
     * @param button    the Button widget — may be null
     * @param listener  the SelectionListener to remove — may be null
     */
    protected void removeSelectionListener( Button button, SelectionListener listener )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) && ( listener != null ) )
        {
            button.removeSelectionListener( listener );
        }
    }


    // ── removeSelectionListener (Combo) — Workstation Disconnects the Dropdown Monitor ─
    // Same pattern as removeModifyListener, for Combo widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a SelectionListener from the given Combo widget if it's non-null
     * and not disposed.
     *
     * @param combo     the Combo widget — may be null
     * @param listener  the SelectionListener to remove — may be null
     */
    protected void removeSelectionListener( Combo combo, SelectionListener listener )
    {
        if ( ( combo != null ) && ( !combo.isDisposed() ) && ( listener != null ) )
        {
            combo.removeSelectionListener( listener );
        }
    }


    // ── removeModifyListener (TableWidget) — Workstation Disconnects the Table Monitor ─
    // Same pattern as removeModifyListener, for TableWidget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a WidgetModifyListener from the given TableWidget if both are
     * non-null.
     *
     * @param tableWidget  the TableWidget — may be null
     * @param listener     the WidgetModifyListener to remove — may be null
     */
    protected void removeModifyListener( TableWidget<?> tableWidget, WidgetModifyListener listener )
    {
        if ( ( tableWidget != null ) && ( listener != null ) )
        {
            tableWidget.removeWidgetModifyListener( listener );
        }
    }


    // ── addDirtyListener (Text) — Connect Text to the Dirty Flag ──────────────
    // Shortcut: connect this Text directly to the shared dirty listener so any
    // user keystroke automatically marks the editor as having unsaved changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty ModifyListener to the given Text widget.
     * Equivalent to {@code addModifyListener(text, dirtyModifyListener)}.
     *
     * @param text  the Text widget to monitor for changes
     */
    protected void addDirtyListener( Text text )
    {
        addModifyListener( text, dirtyModifyListener );
    }


    // ── addDirtyListener (Button) — Connect Button to the Dirty Flag ──────────
    // Same shortcut for Button widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty SelectionListener to the given Button widget.
     * Equivalent to {@code addSelectionListener(button, dirtySelectionListener)}.
     *
     * @param button  the Button widget to monitor for selection changes
     */
    protected void addDirtyListener( Button button )
    {
        addSelectionListener( button, dirtySelectionListener );
    }


    // ── addDirtyListener (Combo) — Connect Combo to the Dirty Flag ────────────
    // Same shortcut for Combo widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty SelectionListener to the given Combo widget.
     * Equivalent to {@code addSelectionListener(combo, dirtySelectionListener)}.
     *
     * @param button  the Combo widget to monitor (misleadingly named — it's actually a Combo)
     */
    protected void addDirtyListener( Combo combo )
    {
        addSelectionListener( combo, dirtySelectionListener );
    }


    // ── addDirtyListener (TableWidget) — Connect Table to the Dirty Flag ──────
    // Same shortcut for TableWidget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the shared dirty WidgetModifyListener to the given TableWidget.
     * Equivalent to {@code addModifyListener(tableWidget, dirtyWidgetModifyListener)}.
     *
     * @param tableWidget  the TableWidget to monitor for changes
     */
    protected void addDirtyListener( TableWidget<?> tableWidget )
    {
        addModifyListener( tableWidget, dirtyWidgetModifyListener );
    }


    // ── removeDirtyListener (Text) — Disconnect Text from the Dirty Flag ──────
    // During a programmatic UI refresh, we temporarily disconnect the dirty
    // listener so that populating fields from the model doesn't accidentally
    // mark the editor dirty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the shared dirty ModifyListener from the given Text widget.
     * Call this before updating Text content from the model during refreshUI().
     *
     * @param text  the Text widget to stop monitoring
     */
    protected void removeDirtyListener( Text text )
    {
        removeModifyListener( text, dirtyModifyListener );
    }


    // ── removeDirtyListener (Button) — Disconnect Button from the Dirty Flag ──
    // Same pattern for Button widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the shared dirty SelectionListener from the given Button widget.
     *
     * @param button  the Button widget to stop monitoring
     */
    protected void removeDirtyListener( Button button )
    {
        removeSelectionListener( button, dirtySelectionListener );
    }


    // ── removeDirtyListener (Combo) — Disconnect Combo from the Dirty Flag ────
    // Same pattern for Combo widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the shared dirty SelectionListener from the given Combo widget.
     *
     * @param combo  the Combo widget to stop monitoring
     */
    protected void removeDirtyListener( Combo combo )
    {
        removeSelectionListener( combo, dirtySelectionListener );
    }


    // ── removeDirtyListener (TableWidget) — Disconnect Table from the Dirty Flag ─
    // Same pattern for TableWidget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the shared dirty WidgetModifyListener from the given TableWidget.
     *
     * @param tableWidget  the TableWidget to stop monitoring
     */
    protected void removeDirtyListener( TableWidget<?> tableWidget )
    {
        removeModifyListener( tableWidget, dirtyWidgetModifyListener );
    }


    // ── setSelection — Workstation Sets a Button State Safely ─────────────────
    // Before pressing a toggle on the workstation, the operator checks that the
    // switch actually exists and hasn't been locked out.  We do the same null
    // and disposed check before calling setSelection on a Button.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the selection state of a Button widget safely, guarding against
     * null and disposed widgets.
     * Useful during refreshUI() when populating checkboxes and radio buttons
     * from the model without worrying about widget lifecycle.
     *
     * @param button    the Button to update — may be null
     * @param selected  the desired selection state
     */
    protected void setSelection( Button button, boolean selected )
    {
        if ( ( button != null ) && ( !button.isDisposed() ) )
        {
            button.setSelection( selected );
        }
    }


    // ── setText — Workstation Updates a Text Field Safely ─────────────────────
    // Before typing into a field on the workstation, the operator checks that
    // the terminal is powered on and not locked.  We do the same before calling
    // setText on a Text widget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the text content of a Text widget safely, guarding against null and
     * disposed widgets.
     * Useful during refreshUI() when populating text fields from the model
     * without worrying about widget lifecycle.
     *
     * @param text    the Text widget to update — may be null
     * @param string  the new text to set
     */
    protected void setText( Text text, String string )
    {
        if ( ( text != null ) && ( !text.isDisposed() ) )
        {
            text.setText( string );
        }
    }


    // ── setFocus — Workstation Passes Keyboard Focus to a Control ─────────────
    // The bridge operator taps a control to hand it keyboard focus, but first
    // checks the control is live.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the given Control safely, guarding against null
     * and disposed controls.
     *
     * @param control  the Control to focus — may be null
     */
    protected void setFocus( Control control )
    {
        if ( ( control != null ) && ( !control.isDisposed() ) )
        {
            control.setFocus();
        }
    }


    // ── createSection (expandable) — Workstation Installs a Collapsible Panel ─
    // Some workstation panels can be collapsed to save space — the operator
    // clicks the twistie and the section folds up.  This overload creates an
    // expandable section with a title (no message-bundle lookup — raw string).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an expandable, collapsible Eclipse Forms Section with a title.
     * The section starts expanded.  Use this when you want the user to be able
     * to collapse a panel to reduce visual clutter.
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the composite that contains the section
     * @param title    the section title as a raw string (not a bundle key)
     * @return         the Section widget — call createSectionComposite() next to add an inner composite
     */
    protected Section createSection( FormToolkit toolkit, Composite parent, String title )
    {
        Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText( title ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        return section;
    }


    // ── createSectionComposite — Workstation Adds the Inner Panel Grid ────────
    // After installing a section frame, the engineering crew bolts in the inner
    // composite with its grid columns — the actual area where controls live.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures the inner composite for a Section widget.
     * Call this after {@link #createSection(FormToolkit, Composite, String)}
     * to get the composite where you add your actual widgets.
     *
     * @param toolkit                 the Eclipse Forms toolkit
     * @param section                 the Section that will contain the composite
     * @param numColumns              how many columns the GridLayout should have
     * @param makeColumnsEqualWidth   whether all columns should be the same width
     * @return                        the inner composite, ready to receive widgets
     */
    protected Composite createSectionComposite( FormToolkit toolkit, Section section, int numColumns,
        boolean makeColumnsEqualWidth )
    {
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( numColumns, makeColumnsEqualWidth );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        return composite;
    }
}
