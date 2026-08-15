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
package org.apache.directory.studio.combinededitor.editor;


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.widgets.LdifEditorWidget;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifInvalidContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.apache.directory.studio.combinededitor.actions.FetchOperationalAttributesAction;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.templateeditor.actions.EditorPagePropertiesAction;
import org.apache.directory.studio.templateeditor.actions.RefreshAction;
import org.apache.directory.studio.templateeditor.actions.SimpleActionProxy;


// ── CLASS: LdifEditorPage — The Tantive IV's Raw Communications Screen ────────
// On the Tantive IV bridge, the comms officer monitors the raw signal stream —
// every byte that comes in or goes out is visible in its unprocessed form on
// the comms screen.  Leia can read it, Antilles can edit it directly, and any
// change to the raw signal immediately flows back to the nav and tactical displays.
// LdifEditorPage is that comms screen: it shows the LDAP entry as raw LDIF text
// (the wire format for LDAP changes), lets the user type directly in that text,
// and pushes every valid edit back into the shared working copy so the other two
// tabs (Template, Table) stay in sync.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "LDIF" tab page in the combined entry editor.
 * Displays the current LDAP entry as a formatted LDIF text record and allows
 * direct text editing.  Valid edits are written back to the shared working copy
 * so they appear in the Template and Table pages as well.
 * LDIF (LDAP Data Interchange Format) is the standard text representation of
 * LDAP entries — think of it as the raw wire protocol made human-readable.
 * Think of this page as the Tantive IV's raw comms screen — full fidelity,
 * no abstractions, every attribute visible.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorPage extends AbstractCombinedEntryEditorPage
{
    /** The LDIF editor widget */
    private LdifEditorWidget ldifEditorWidget;

    /** A count to know if the editor page has updated the shared working copy */
    private int hasUpdatedSharedWorkingCopyCount = 0;

    /** The modify listener for the widget */
    private WidgetModifyListener listener = new WidgetModifyListener()
    {
        public void widgetModified( WidgetModifyEvent event )
        {
            updateSharedWorkingCopy();
        }
    };

    /** The context menu */
    private Menu contextMenu;


    // ── The Comms Officer Reports for Duty on the Tantive IV ──────────────────
    // The comms officer takes her seat at the raw signal console, attaches her
    // headset, and labels her station so Antilles can identify it in the tab rack.
    // Our constructor creates the CTabItem with the right label and icon, then
    // calls setTabItem() to wire up the tab-selection listener.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the LDIF editor page and its tab item in the editor's tab folder.
     * We create the tab now (so it appears immediately in the tab strip) but
     * defer creating the heavy LDIF widget until {@link #init()} is called the
     * first time the user selects this tab.
     *
     * @param editor  the combined editor that owns this page.
     */
    public LdifEditorPage( CombinedEntryEditor editor )
    {
        super( editor );

        // Creating and assigning the tab item
        CTabItem tabItem = new CTabItem( editor.getTabFolder(), SWT.NONE );
        tabItem.setText( Messages.getString( "LdifEditorPage.LDIFEditor" ) ); //$NON-NLS-1$
        tabItem.setImage( LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_BROWSER_LDIFEDITOR ) );
        setTabItem( tabItem );
    }


    // ── The Comms Screen Powers Up — Full LDIF Display Ready ──────────────────
    // The comms officer flips the power switch and the full signal display comes
    // to life: the LDIF widget is created, the context menu is wired up, and
    // the current entry's LDIF text is loaded into the source viewer.
    // init() does all that setup on first use (lazy init — only when needed).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the LDIF editor widget and loads the current entry's LDIF content.
     * Called lazily the first time the user selects this tab.  We create the
     * {@link LdifEditorWidget}, wire up cut/copy/paste action handlers, add a
     * context menu with Refresh and FetchOperationalAttributes, and load the
     * entry's current data into the source viewer.
     */
    public void init()
    {
        super.init();

        ldifEditorWidget = new LdifEditorWidget( null, "", true ); //$NON-NLS-1$
        ldifEditorWidget.createWidget( getEditor().getTabFolder() );

        // Creating a new menu manager
        Control sourceViewerControl = ldifEditorWidget.getSourceViewer().getControl();

        MenuManager menuManager = new MenuManager();
        contextMenu = menuManager.createContextMenu( sourceViewerControl );
        sourceViewerControl.setMenu( contextMenu );

        IEditorSite site = getEditor().getEditorSite();
        IActionBars bars = site.getActionBars();

        Action cutAction = new Action( "Cut" )
        {
            public void run()
            {
                ldifEditorWidget.getSourceViewer().doOperation( SourceViewer.CUT );
            }
        };

        Action copyAction = new Action( "Copy" )
        {
            public void run()
            {
                ldifEditorWidget.getSourceViewer().doOperation( SourceViewer.COPY );
            }
        };

        Action pasteAction = new Action( "Paste" )
        {
            public void run()
            {
                ldifEditorWidget.getSourceViewer().doOperation( SourceViewer.PASTE );
            }
        };

        bars.setGlobalActionHandler( ActionFactory.CUT.getId(), cutAction );
        bars.setGlobalActionHandler( ActionFactory.COPY.getId(), copyAction );
        bars.setGlobalActionHandler( ActionFactory.PASTE.getId(), pasteAction );

        // TODO remove this
        menuManager.add( ActionFactory.CUT.create( PlatformUI.getWorkbench().getActiveWorkbenchWindow() ) );
        menuManager.add( ActionFactory.COPY.create( PlatformUI.getWorkbench().getActiveWorkbenchWindow() ) );
        menuManager.add( ActionFactory.PASTE.create( PlatformUI.getWorkbench().getActiveWorkbenchWindow() ) );

        menuManager.add( new Separator() );
        menuManager.add( new RefreshAction( getEditor() ) );
        menuManager.add( new FetchOperationalAttributesAction( getEditor() ) );
        menuManager.add( new Separator() );
        menuManager.add( new SimpleActionProxy( new EditorPagePropertiesAction( getEditor() ) ) );

        setInput();

        getTabItem().setControl( ldifEditorWidget.getControl() );
    }


    // ── The Comms Officer Tunes In — Start Listening to the Signal ────────────
    // The comms officer puts on her headset and starts processing the incoming
    // signal — from now on any change in the source viewer triggers an update.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the widget modify listener so LDIF text changes propagate to the
     * shared working copy.
     */
    private void addListener()
    {
        ldifEditorWidget.addWidgetModifyListener( listener );
    }


    // ── The Comms Officer Removes Her Headset — Stop Listening Temporarily ────
    // While the comms officer is updating the display herself she removes the
    // headset to avoid an echo — the listener is re-attached once the update
    // is complete.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the widget modify listener to prevent recursive update loops.
     * We remove the listener before programmatically setting the source viewer
     * content (in {@link #setInput()}) so that our own edits don't trigger
     * another working-copy update cycle.
     */
    private void removeListener()
    {
        ldifEditorWidget.removeWidgetModifyListener( listener );
    }


    // ── The Comms Officer Parses the Signal and Updates the Nav Database ───────
    // When the comms officer sees a valid signal she extracts the coordinates
    // and feeds them into the nav database so the whole bridge has the latest data.
    // updateSharedWorkingCopy() parses the LDIF text, validates it, and — if it's
    // a single valid content record — pushes the changes back into the shared
    // working copy so the Template and Table tabs refresh automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the LDIF text in the source viewer and updates the shared working copy.
     * We only update if the LDIF is syntactically valid and contains exactly one
     * content record — partial or invalid LDIF is silently ignored so the user
     * can finish typing before we try to parse.
     * Increments {@link #hasUpdatedSharedWorkingCopyCount} so the next
     * {@link #update()} call knows to skip re-loading (we were the source).
     */
    private void updateSharedWorkingCopy()
    {
        LdifFile ldifModel = ldifEditorWidget.getLdifModel();

        // only continue if the LDIF model is valid
        LdifRecord[] records = ldifModel.getRecords();
        if ( records.length != 1 || !( records[0] instanceof LdifContentRecord ) || !records[0].isValid()
            || !records[0].getDnLine().isValid() )
        {
            return;
        }
        for ( LdifContainer ldifContainer : ldifModel.getContainers() )
        {
            if ( ldifContainer instanceof LdifInvalidContainer )
            {
                return;
            }
        }

        // update shared working copy
        try
        {
            LdifContentRecord modifiedRecord = ( LdifContentRecord ) records[0];
            EntryEditorInput input = getEditor().getEntryEditorInput();
            IEntry sharedWorkingCopyEntry = input.getSharedWorkingCopy( getEditor() );

            IBrowserConnection browserConnection = input.getSharedWorkingCopy( getEditor() ).getBrowserConnection();
            DummyEntry modifiedEntry = ModelConverter.ldifContentRecordToEntry( modifiedRecord, browserConnection );
            ( ( DummyEntry ) sharedWorkingCopyEntry ).setDn( modifiedEntry.getDn() );
            new CompoundModification().replaceAttributes( modifiedEntry, sharedWorkingCopyEntry, this );

            // Increasing the update count
            hasUpdatedSharedWorkingCopyCount++;
        }
        catch ( LdapInvalidDnException e )
        {
            throw new RuntimeException( e );
        }
    }


    // ── The Comms Officer Loads the Current Signal into the Display ────────────
    // The comms officer retrieves the latest data from the working copy, converts
    // it to LDIF text, and loads it into the source viewer — making the display
    // read-only if there's no editable entry, or editable if there is one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads the current shared working copy entry into the LDIF source viewer.
     * If the entry is available we format it as LDIF and make the viewer editable.
     * If there's nothing to show (e.g. the input was cleared) we blank the viewer
     * and make it read-only.  Removes and re-attaches the modify listener around
     * the load so we don't trigger a spurious working-copy update.
     */
    private void setInput()
    {
        removeListener();

        if ( ldifEditorWidget != null )
        {
            SourceViewer sourceViewer = ldifEditorWidget.getSourceViewer();
            IEntry entry = getEditor().getEntryEditorInput().getSharedWorkingCopy( getEditor() );
            if ( entry != null )
            {
                // Making the source viewer editable
                sourceViewer.setEditable( true );

                // Showing the context menu
                sourceViewer.getControl().setMenu( contextMenu );

                // Assigning the content to the source viewer
                sourceViewer.getDocument().set(
                    ModelConverter.entryToLdifContentRecord( entry )
                        .toFormattedString( Utils.getLdifFormatParameters() ) );
            }
            else
            {
                // Making the source viewer non editable
                sourceViewer.setEditable( false );

                // Hiding the context menu
                sourceViewer.getControl().setMenu( null );

                // Assigning a blank content to the source viewer
                sourceViewer.getDocument().set( "" ); //$NON-NLS-1$
            }
        }

        addListener();
    }


    // ── The Bridge Calls for a Status Update — Comms Screen Refreshes ─────────
    // When the nav database changes (because the Template or Table tab was edited)
    // the comms officer refreshes the raw signal display to match — but only if
    // the change didn't come from her own edits (she uses a counter to track that).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the LDIF display from the shared working copy.
     * If we triggered this update ourselves (by editing the LDIF text), we decrement
     * the counter and skip the reload to avoid overwriting the user's in-progress
     * edit.  If the update came from another page, we reload the LDIF text.
     */
    public void update()
    {
        // Checking if the editor page is the source of this update
        if ( hasUpdatedSharedWorkingCopyCount != 0 )
        {
            // Decreasing the number of updates to be discarded
            hasUpdatedSharedWorkingCopyCount--;
        }
        else
        {
            // Reseting the input
            setInput();
        }
    }


    // ── The Comms Officer Steps Back — No Focus Needed ───────────────────────
    // The LDIF source viewer handles its own focus through Eclipse text framework
    // mechanisms; we don't need to do anything extra here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — the LDIF source viewer manages its own focus.
     */
    public void setFocus()
    {
        // Nothing to do.
    }


    // ── New Mission Coordinates — Reload the Comms Display ───────────────────
    // When Antilles locks onto a new target the comms display needs to reload the
    // signal for the new destination — but only if it's already been powered up.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the editor's input switches to a different LDAP entry.
     * If this page has already been initialised we reload the LDIF content
     * from the new entry; if not, we wait until the user selects this tab.
     */
    public void editorInputChanged()
    {
        if ( isInitialized() )
        {
            setInput();
        }
    }
}
