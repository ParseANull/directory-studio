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

package org.apache.directory.studio.ldifeditor.editor;


import java.io.File;
import java.util.ResourceBundle;

import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.filesystem.PathEditorInput;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.ValueEditorPreferencesAction;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.BrowserConnectionWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.actions.EditLdifAttributeAction;
import org.apache.directory.studio.ldifeditor.editor.actions.EditLdifRecordAction;
import org.apache.directory.studio.ldifeditor.editor.actions.FormatLdifDocumentAction;
import org.apache.directory.studio.ldifeditor.editor.actions.FormatLdifRecordAction;
import org.apache.directory.studio.ldifeditor.editor.actions.OpenBestValueEditorAction;
import org.apache.directory.studio.ldifeditor.editor.actions.OpenDefaultValueEditorAction;
import org.apache.directory.studio.ldifeditor.editor.actions.OpenValueEditorAction;
import org.apache.directory.studio.ldifeditor.editor.text.LdifPartitionScanner;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.utils.ActionUtils;
import org.apache.directory.studio.valueeditors.AbstractDialogValueEditor;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


// ── CLASS: LdifEditor — REBEL TRANSMISSION CONSOLE ───────────────────────────
// In the Rebel base on Yavin 4 the communications console is the beating heart
// of the operation: it lets officers compose, edit, and transmit data bundles
// to the fleet, view them in an indexed outline, and send them directly over
// an authenticated connection.
// LdifEditor is that console: a full Eclipse text editor for LDIF files that
// adds a connection selector, execute action, syntax highlighting, content
// assist, code folding, and an outline view — all wired to the LDAP directory
// connection the officer picks from the toolbar.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link TextEditor} for LDIF files.
 * Extends the standard text editor with a toolbar that hosts a
 * {@link BrowserConnectionWidget} (for selecting the LDAP connection) and an
 * execute button, syntax highlighting via {@link LdifSourceViewerConfiguration},
 * code folding via {@link ProjectionSupport}, an outline page via
 * {@link LdifOutlinePage}, and context-menu actions for editing attributes,
 * records, and formatting.
 * Think of this as the Rebel transmission console: compose, preview, and send
 * LDIF operations to the directory in one place.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditor extends TextEditor implements ILdifEditor, ConnectionUpdateListener, IPartListener2
{
    /** The ViewForm that contains the toolbar and the editor area. */
    private ViewForm control;

    /** Toolbar widget for selecting the LDAP browser connection. */
    private BrowserConnectionWidget browserConnectionWidget;

    /** The SWT toolbar that hosts execute and other action buttons. */
    private ToolBar actionToolBar;

    /** JFace manager for the action toolbar. */
    private IToolBarManager actionToolBarManager;

    /** The currently selected LDAP browser connection. */
    private IBrowserConnection browserConnection;

    /** Eclipse folding infrastructure installed on the ProjectionViewer. */
    private ProjectionSupport projectionSupport;

    /** The LDIF outline page, created lazily via getAdapter(). */
    protected LdifOutlinePage outlinePage;

    /** Manages the full set of value-editor extensions for this editor. */
    private ValueEditorManager valueEditorManager;

    /** The "open best matching value editor" action. */
    private OpenBestValueEditorAction openBestValueEditorAction;

    /** One action per registered value-editor extension. */
    private OpenValueEditorAction[] openValueEditorActions;

    /** Action that opens the value-editor preferences page. */
    private ValueEditorPreferencesAction valueEditorPreferencesAction;

    /** Whether the connection + execute toolbar strip should be shown. */
    protected boolean showToolBar = true;


    // ── CONSTRUCT AND CONFIGURE THE EDITOR ───────────────────────────────────
    // The officer sits down at the console, loads the LDIF source-viewer
    // configuration (syntax + content assist), chains together the LDIF and
    // Eclipse preference stores, and sets the help context.
    /**
     * Creates a new LDIF editor instance.
     * Installs {@link LdifSourceViewerConfiguration} and
     * {@link LdifDocumentProvider}, then chains the LDIF plugin preference store
     * in front of the Eclipse editors preference store so that LDIF-specific
     * settings override the generic ones.
     */
    public LdifEditor()
    {
        super();

        setSourceViewerConfiguration( new LdifSourceViewerConfiguration( this, true ) );
        setDocumentProvider( new LdifDocumentProvider() );

        IPreferenceStore editorStore = EditorsUI.getPreferenceStore();
        IPreferenceStore browserStore = LdifEditorActivator.getDefault().getPreferenceStore();
        IPreferenceStore combinedStore = new ChainedPreferenceStore( new IPreferenceStore[]
            { browserStore, editorStore } );
        setPreferenceStore( combinedStore );

        setHelpContextId( LdifEditorConstants.PLUGIN_ID + "." + "tools_ldif_editor" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── REACT TO PREFERENCE CHANGES ───────────────────────────────────────────
    // When the officer adjusts a syntax-colour setting the console re-renders
    // the current document so the change is visible immediately.
    /**
     * {@inheritDoc}
     *
     * <p>Forces a full document re-render before delegating to the superclass so
     * that syntax-colour preference changes take effect immediately.</p>
     */
    protected void handlePreferenceStoreChanged( PropertyChangeEvent event )
    {
        try
        {

            ISourceViewer sourceViewer = getSourceViewer();
            if ( sourceViewer == null )
            {
                return;
            }

            int topIndex = getSourceViewer().getTopIndex();
            getSourceViewer().getDocument().set( getSourceViewer().getDocument().get() );
            getSourceViewer().setTopIndex( topIndex );

        }
        finally
        {
            super.handlePreferenceStoreChanged( event );
        }
    }


    // ── INJECT LDIF PREFERENCE PAGES INTO CONTEXT MENU ───────────────────────
    // The officer adds LDIF-specific settings pages to the context menu so
    // they can tweak the editor without leaving the console.
    /**
     * {@inheritDoc}
     *
     * <p>Prepends the four LDIF preference page IDs (main, content assist, syntax
     * colouring, templates) in front of the generic text-editor preference
     * pages.</p>
     */
    protected String[] collectContextMenuPreferencePages()
    {
        String[] ids = super.collectContextMenuPreferencePages();
        String[] more = new String[ids.length + 4];
        more[0] = LdifEditorConstants.PREFERENCEPAGEID_LDIFEDITOR;
        more[1] = LdifEditorConstants.PREFERENCEPAGEID_LDIFEDITOR_CONTENTASSIST;
        more[2] = LdifEditorConstants.PREFERENCEPAGEID_LDIFEDITOR_SYNTAXCOLORING;
        more[3] = LdifEditorConstants.PREFERENCEPAGEID_LDIFEDITOR_TEMPLATES;
        System.arraycopy( ids, 0, more, 4, ids.length );
        return more;
    }


    // ── RETURN THE STATIC EDITOR ID ───────────────────────────────────────────
    /**
     * Returns the Eclipse editor-part ID for the LDIF editor, as registered in
     * the plugin.xml extension point.
     *
     * @return the LDIF editor ID string
     */
    public static String getId()
    {
        return LdifEditorConstants.EDITOR_LDIF_EDITOR;
    }


    // ── INITIALISE THE EDITOR PART ───────────────────────────────────────────
    // The officer takes their seat, checks that the file is not too large to
    // edit, subscribes to connection events, and sets up the value-editor
    // manager.
    /**
     * {@inheritDoc}
     *
     * <p>Refuses to open files larger than 1 MB by substituting a
     * {@link NonExistingLdifEditorInput}.  Registers as a
     * {@link ConnectionUpdateListener} and an {@link IPartListener2}.</p>
     *
     * @param site   the editor site
     * @param input  the editor input
     * @throws PartInitException if the superclass init fails
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        String className = input.getClass().getName();
        File file = null;
        if ( input instanceof IPathEditorInput )
        {
            IPathEditorInput pei = ( IPathEditorInput ) input;
            IPath path = pei.getPath();
            file = path.toFile();
        }
        else if ( className.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" ) //$NON-NLS-1$
            || className.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) ) //$NON-NLS-1$
        // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
        // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
        // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
        // opening a file from the menu File > Open... in Eclipse 3.3.x
        {
            file = new File( input.getToolTipText() );
        }
        if ( file != null )
        {
            long fileLength = file.length();
            if ( fileLength > ( 1 * 1024 * 1024 ) )
            {
                MessageDialog.openError( site.getShell(), Messages.getString( "LdifEditor.LDIFFileIsTooBig" ), //$NON-NLS-1$
                    Messages.getString( "LdifEditor.LDIFFileIsTooBigDescription" ) ); //$NON-NLS-1$
                input = new NonExistingLdifEditorInput();
            }
        }

        super.init( site, input );

        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionUIPlugin.getDefault().getEventRunner() );
        getSite().getPage().addPartListener( this );

        this.valueEditorManager = new ValueEditorManager( getSite().getShell(), false, false );
    }


    // ── TEAR DOWN THE EDITOR ─────────────────────────────────────────────────
    // The officer leaves the console, deregisters their action handlers and
    // connection subscription, and releases the value-editor manager.
    /**
     * {@inheritDoc}
     *
     * <p>Disposes the value-editor manager, deactivates global action handlers,
     * and unregisters the connection and part listeners before delegating to the
     * superclass.</p>
     */
    public void dispose()
    {
        valueEditorManager.dispose();

        deactivateGlobalActionHandlers();

        ConnectionEventRegistry.removeConnectionUpdateListener( this );
        getSite().getPage().removePartListener( this );

        super.dispose();
    }


    // ── ADAPT TO REQUIRED INTERFACES ─────────────────────────────────────────
    // The operator can ask the console for the outline page, source viewer,
    // annotation hover, text hover, or content-assist processor without
    // knowing the concrete class.
    /**
     * {@inheritDoc}
     *
     * <p>Handles {@link IShowInTargetList} (Navigator), {@link IContentOutlinePage}
     * (lazily creates {@link LdifOutlinePage}), {@link ISourceViewer},
     * {@link IAnnotationHover}, {@link ITextHover}, and
     * {@link IContentAssistProcessor} adaptations before falling back to the
     * projection support and the superclass.</p>
     */
    public Object getAdapter( Class required )
    {
        if ( IShowInTargetList.class.equals( required ) )
        {
            return new IShowInTargetList()
            {
                public String[] getShowInTargetIds()
                {
                    return new String[]
                        { "org.eclipse.ui.views.ResourceNavigator" };
                }
            };
        }
        if ( IContentOutlinePage.class.equals( required ) )
        {
            if ( outlinePage == null || outlinePage.getControl() == null || outlinePage.getControl().isDisposed() )
            {
                outlinePage = new LdifOutlinePage( this );
            }
            return outlinePage;
        }
        if ( ISourceViewer.class.equals( required ) )
        {
            return getSourceViewer();
        }
        if ( IAnnotationHover.class.equals( required ) )
        {
            if ( getSourceViewerConfiguration() != null && getSourceViewer() != null )
                return getSourceViewerConfiguration().getAnnotationHover( getSourceViewer() );
        }
        if ( ITextHover.class.equals( required ) )
        {
            if ( getSourceViewerConfiguration() != null && getSourceViewer() != null )
                return getSourceViewerConfiguration().getTextHover( getSourceViewer(), null );
        }
        if ( IContentAssistProcessor.class.equals( required ) )
        {
            if ( getSourceViewerConfiguration() != null && getSourceViewer() != null )
                return getSourceViewerConfiguration().getContentAssistant( getSourceViewer() )
                    .getContentAssistProcessor( LdifPartitionScanner.LDIF_RECORD );
        }
        if ( projectionSupport != null )
        {
            Object adapter = projectionSupport.getAdapter( getSourceViewer(), required );
            if ( adapter != null )
                return adapter;
        }
        return super.getAdapter( required );
    }


    // ── CUSTOMISE THE CONTEXT MENU ────────────────────────────────────────────
    // The officer removes the shift-left/shift-right items (they don't apply to
    // LDIF) and adds LDIF-specific edit, "edit value with", and format groups.
    /**
     * {@inheritDoc}
     *
     * <p>Removes the text-indent shift actions, then adds LDIF attribute, value,
     * "edit value with" sub-menu, record, and format sub-menu items.</p>
     */
    protected void editorContextMenuAboutToShow( IMenuManager menu )
    {
        super.editorContextMenuAboutToShow( menu );

        IContributionItem[] items = menu.getItems();
        for ( int i = 0; i < items.length; i++ )
        {
            if ( items[i] instanceof ActionContributionItem )
            {
                ActionContributionItem aci = ( ActionContributionItem ) items[i];
                if ( aci.getAction() == getAction( ITextEditorActionConstants.SHIFT_LEFT ) )
                {
                    menu.remove( items[i] );
                }
                if ( aci.getAction() == getAction( ITextEditorActionConstants.SHIFT_RIGHT ) )
                {
                    menu.remove( items[i] );
                }
            }
        }

        // add Edit actions
        addAction( menu, ITextEditorActionConstants.GROUP_EDIT,
            LdifEditorConstants.ACTION_ID_EDIT_ATTRIBUTE_DESCRIPTION );
        addAction( menu, ITextEditorActionConstants.GROUP_EDIT, BrowserCommonConstants.ACTION_ID_EDIT_VALUE );

        MenuManager valueEditorMenuManager = new MenuManager( Messages.getString( "LdifEditor.EditValueWith" ) ); //$NON-NLS-1$
        if ( this.openBestValueEditorAction.isEnabled() )
        {
            valueEditorMenuManager.add( this.openBestValueEditorAction );
            valueEditorMenuManager.add( new Separator() );
        }
        for ( int i = 0; i < this.openValueEditorActions.length; i++ )
        {
            this.openValueEditorActions[i].update();
            if ( this.openValueEditorActions[i].isEnabled()
                && this.openValueEditorActions[i].getValueEditor().getClass() != this.openBestValueEditorAction
                    .getValueEditor().getClass()
                && this.openValueEditorActions[i].getValueEditor() instanceof AbstractDialogValueEditor )
            {
                valueEditorMenuManager.add( this.openValueEditorActions[i] );
            }
        }
        valueEditorMenuManager.add( new Separator() );
        valueEditorMenuManager.add( this.valueEditorPreferencesAction );
        menu.appendToGroup( ITextEditorActionConstants.GROUP_EDIT, valueEditorMenuManager );

        addAction( menu, ITextEditorActionConstants.GROUP_EDIT, LdifEditorConstants.ACTION_ID_EDIT_RECORD );

        // add Format actions
        MenuManager formatMenuManager = new MenuManager( Messages.getString( "LdifEditor.Format" ) ); //$NON-NLS-1$
        addAction( formatMenuManager, LdifEditorConstants.ACTION_ID_FORMAT_LDIF_DOCUMENT );
        addAction( formatMenuManager, LdifEditorConstants.ACTION_ID_FORMAT_LDIF_RECORD );
        menu.appendToGroup( ITextEditorActionConstants.GROUP_EDIT, formatMenuManager );
    }


    // ── REGISTER ALL EDITOR ACTIONS ───────────────────────────────────────────
    // The officer plugs in every module: content assist, execute, attribute edit,
    // value editors, record edit, format, and standard cut/copy/paste with icons.
    /**
     * {@inheritDoc}
     *
     * <p>Registers the content-assist action, execute action (on the toolbar),
     * attribute-edit action, open-best-value-editor action, all per-extension
     * value-editor actions, default value-editor action, record-edit action,
     * format-document and format-record actions, and refreshes the cut/copy/paste
     * icons.  Finally activates global action handlers.</p>
     */
    protected void createActions()
    {
        super.createActions();

        // add content assistant
        ResourceBundle bundle = LdifEditorActivator.getDefault().getResourceBundle();
        IAction action = new ContentAssistAction( bundle, "ldifeditor__contentassistproposal_", this ); //$NON-NLS-1$
        action.setActionDefinitionId( ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS );
        setAction( "ContentAssistProposal", action ); //$NON-NLS-1$

        // add execute action (for tool bar)
        if ( actionToolBarManager != null )
        {
            ExecuteLdifAction executeLdifAction = new ExecuteLdifAction( this );
            actionToolBarManager.add( executeLdifAction );
            setAction( LdifEditorConstants.ACTION_ID_EXECUTE_LDIF, executeLdifAction );
            actionToolBarManager.update( true );
        }

        // add context menu edit actions
        EditLdifAttributeAction editLdifAttributeAction = new EditLdifAttributeAction( this );
        setAction( BrowserCommonConstants.ACTION_ID_EDIT_ATTRIBUTE_DESCRIPTION, editLdifAttributeAction );

        openBestValueEditorAction = new OpenBestValueEditorAction( this );
        IValueEditor[] valueEditors = valueEditorManager.getAllValueEditors();
        openValueEditorActions = new OpenValueEditorAction[valueEditors.length];
        for ( int i = 0; i < this.openValueEditorActions.length; i++ )
        {
            openValueEditorActions[i] = new OpenValueEditorAction( this, valueEditors[i] );
        }
        valueEditorPreferencesAction = new ValueEditorPreferencesAction();

        OpenDefaultValueEditorAction openDefaultValueEditorAction = new OpenDefaultValueEditorAction( this,
            openBestValueEditorAction );
        setAction( BrowserCommonConstants.ACTION_ID_EDIT_VALUE, openDefaultValueEditorAction );

        EditLdifRecordAction editRecordAction = new EditLdifRecordAction( this );
        setAction( LdifEditorConstants.ACTION_ID_EDIT_RECORD, editRecordAction );

        // add context menu format actions
        FormatLdifDocumentAction formatDocumentAction = new FormatLdifDocumentAction( this );
        setAction( LdifEditorConstants.ACTION_ID_FORMAT_LDIF_DOCUMENT, formatDocumentAction );
        FormatLdifRecordAction formatRecordAction = new FormatLdifRecordAction( this );
        setAction( LdifEditorConstants.ACTION_ID_FORMAT_LDIF_RECORD, formatRecordAction );

        // update cut, copy, paste
        IAction cutAction = getAction( ITextEditorActionConstants.CUT );
        if ( cutAction != null )
        {
            cutAction.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_CUT ) );
        }
        IAction copyAction = getAction( ITextEditorActionConstants.COPY );
        if ( copyAction != null )
        {
            copyAction.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_COPY ) );
        }
        IAction pasteAction = getAction( ITextEditorActionConstants.PASTE );
        if ( pasteAction != null )
        {
            pasteAction.setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_PASTE ) );
        }

        activateGlobalActionHandlers();
    }


    // ── BUILD THE EDITOR WIDGET HIERARCHY ────────────────────────────────────
    // The console is assembled: connection selector at the top, execute button
    // on the right, and the full-screen text editor in the body.  Code folding
    // is switched on immediately.
    /**
     * {@inheritDoc}
     *
     * <p>If {@link #showToolBar} is {@code true}, wraps the editor in a
     * {@link ViewForm} with a {@link BrowserConnectionWidget} and action toolbar.
     * Regardless, installs a {@link ProjectionSupport} and toggles code folding
     * on.</p>
     */
    public void createPartControl( Composite parent )
    {
        setHelpContextId( LdifEditorConstants.PLUGIN_ID + "." + "tools_ldif_editor" ); //$NON-NLS-1$ //$NON-NLS-2$

        if ( showToolBar )
        {
            // create the toolbar (including connection widget and execute button) on top of the editor
            Composite composite = new Composite( parent, SWT.NONE );
            GridLayout layout = new GridLayout();
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            composite.setLayout( layout );

            control = new ViewForm( composite, SWT.NONE );
            control.setLayoutData( new GridData( GridData.FILL_BOTH ) );

            Composite browserConnectionWidgetControl = BaseWidgetUtils.createColumnContainer( control, 2, 1 );
            browserConnectionWidget = new BrowserConnectionWidget();
            browserConnectionWidget.createWidget( browserConnectionWidgetControl );
            connectionUpdated( null );
            browserConnectionWidget.addWidgetModifyListener( new WidgetModifyListener()
            {
                public void widgetModified( WidgetModifyEvent event )
                {
                    IBrowserConnection browserConnection = browserConnectionWidget.getBrowserConnection();
                    setConnection( browserConnection );
                }
            } );
            control.setTopLeft( browserConnectionWidgetControl );

            // tool bar
            actionToolBar = new ToolBar( control, SWT.FLAT | SWT.RIGHT );
            actionToolBar.setLayoutData( new GridData( SWT.END, SWT.NONE, true, false ) );
            actionToolBarManager = new ToolBarManager( actionToolBar );
            control.setTopCenter( actionToolBar );

            // local menu
            control.setTopRight( null );

            // content
            Composite editorComposite = new Composite( control, SWT.NONE );
            editorComposite.setLayout( new FillLayout() );
            GridData data = new GridData( GridData.FILL_BOTH );
            data.widthHint = 450;
            data.heightHint = 250;
            editorComposite.setLayoutData( data );
            super.createPartControl( editorComposite );
            control.setContent( editorComposite );
        }
        else
        {
            super.createPartControl( parent );
        }

        ProjectionViewer projectionViewer = ( ProjectionViewer ) getSourceViewer();
        projectionSupport = new ProjectionSupport( projectionViewer, getAnnotationAccess(), getSharedColors() );
        projectionSupport.install();
        projectionViewer.doOperation( ProjectionViewer.TOGGLE );
    }


    // ── CREATE THE SOURCE VIEWER ──────────────────────────────────────────────
    // The console screen is a ProjectionViewer so code folding works natively.
    /**
     * {@inheritDoc}
     *
     * <p>Returns a {@link ProjectionViewer} so that
     * {@link ProjectionSupport} can drive code folding.</p>
     */
    protected ISourceViewer createSourceViewer( Composite parent, IVerticalRuler ruler, int styles )
    {
        getAnnotationAccess();
        getOverviewRuler();
        ISourceViewer viewer = new ProjectionViewer( parent, ruler, getOverviewRuler(), true, styles );
        getSourceViewerDecorationSupport( viewer );

        return viewer;
    }


    // ── CONFIGURE DECORATION SUPPORT ─────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates entirely to the superclass — no additional decorations needed.</p>
     */
    protected void configureSourceViewerDecorationSupport( SourceViewerDecorationSupport support )
    {
        super.configureSourceViewerDecorationSupport( support );
    }


    // ── RETURN THE PARSED LDIF MODEL ─────────────────────────────────────────
    // The operator reads the current parse result without touching the document
    // directly.
    /**
     * {@inheritDoc}
     *
     * <p>Retrieves the {@link LdifFile} from the {@link LdifDocumentProvider}.
     * Returns {@code null} if the provider is not a {@link LdifDocumentProvider}.</p>
     */
    public LdifFile getLdifModel()
    {
        IDocumentProvider provider = getDocumentProvider();
        if ( provider instanceof LdifDocumentProvider )
        {
            return ( ( LdifDocumentProvider ) provider ).getLdifModel();
        }
        else
        {
            return null;
        }
    }


    // ── HANDLE OUTLINE PAGE CLOSURE ───────────────────────────────────────────
    // When the archivist closes the index pad the editor releases the folding
    // support.
    /**
     * Called by {@link LdifOutlinePage#dispose()} when the outline page is
     * closed.  Disposes the projection support and nulls the reference.
     */
    public void outlinePageClosed()
    {
        projectionSupport.dispose();
        outlinePage = null;
    }


    // ── RETURN THE CURRENT CONNECTION ─────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the currently selected {@link IBrowserConnection}.</p>
     */
    public IBrowserConnection getConnection()
    {
        return browserConnection;
    }


    // ── SET THE CONNECTION ────────────────────────────────────────────────────
    // The officer selects a new connection from the toolbar dropdown.
    /**
     * Sets the browser connection without updating the toolbar widget.
     *
     * @param browserConnection  the new connection (may be {@code null})
     */
    protected void setConnection( IBrowserConnection browserConnection )
    {
        setConnection( browserConnection, false );
    }


    // ── SET THE CONNECTION AND OPTIONALLY SYNC THE WIDGET ─────────────────────
    /**
     * Sets the browser connection, optionally updating the
     * {@link BrowserConnectionWidget} to reflect the change.
     *
     * @param browserConnection              the new connection (may be {@code null})
     * @param updateBrowserConnectionWidget  {@code true} to sync the toolbar widget
     */
    protected void setConnection( IBrowserConnection browserConnection, boolean updateBrowserConnectionWidget )
    {
        this.browserConnection = browserConnection;

        if ( updateBrowserConnectionWidget && ( browserConnectionWidget != null ) )
        {
            browserConnectionWidget.setBrowserConnection( browserConnection );
        }
    }


    // ── REACT TO CONNECTION UPDATES ───────────────────────────────────────────
    // The Rebel comm channel went up or down; the console refreshes its
    // connection state.
    /**
     * {@inheritDoc}
     *
     * <p>If the updated connection matches the one selected in the toolbar widget,
     * re-applies it so the local reference stays consistent.</p>
     */
    public final void connectionUpdated( Connection connection )
    {
        if ( browserConnectionWidget != null )
        {
            IBrowserConnection browserConnection = browserConnectionWidget.getBrowserConnection();
            if ( browserConnection != null && browserConnection.getConnection().equals( connection ) )
            {
                setConnection( browserConnection );
                browserConnectionWidget.setBrowserConnection( browserConnection );
            }
        }
    }


    // ── CONNECTION ADDED ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #connectionUpdated(Connection)}.</p>
     */
    public void connectionAdded( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── CONNECTION REMOVED ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #connectionUpdated(Connection)}.</p>
     */
    public void connectionRemoved( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── CONNECTION OPENED ─────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #connectionUpdated(Connection)}.</p>
     */
    public void connectionOpened( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── CONNECTION CLOSED ─────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #connectionUpdated(Connection)}.</p>
     */
    public void connectionClosed( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── CONNECTION FOLDER MODIFIED ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — folder changes do not affect this editor.</p>
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
    }


    // ── CONNECTION FOLDER ADDED ───────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — folder changes do not affect this editor.</p>
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
    }


    // ── CONNECTION FOLDER REMOVED ─────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — folder changes do not affect this editor.</p>
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
    }


    // ── SAVE — REDIRECT NEW FILES TO SAVE-AS ─────────────────────────────────
    // If the file has never been saved the officer is prompted for a location
    // before saving.
    /**
     * {@inheritDoc}
     *
     * <p>If the current input is a {@link NonExistingLdifEditorInput} (file not
     * yet saved to disk), delegates to {@code doSaveAs()} to prompt for a name
     * before saving.</p>
     */
    public void doSave( IProgressMonitor progressMonitor )
    {
        final IEditorInput input = getEditorInput();
        if ( input instanceof NonExistingLdifEditorInput )
        {
            super.doSaveAs();
            return;
        }

        super.doSave( progressMonitor );
    }


    // ── SAVE AS ───────────────────────────────────────────────────────────────
    // In IDE mode the standard Eclipse save-as dialog appears; in RCP mode a
    // plain SWT FileDialog is shown instead.
    /**
     * {@inheritDoc}
     *
     * <p>In IDE mode delegates to the superclass.  In RCP mode shows an SWT
     * {@link FileDialog} and writes the document to the chosen path, prompting
     * before overwriting an existing file.</p>
     *
     * <p>Supported input types:
     * <ul>
     *   <li>{@link NonExistingLdifEditorInput} — new, unsaved file</li>
     *   <li>{@code PathEditorInput} — opened via "Open File…" action</li>
     *   <li>{@code FileEditorInput} — workspace file</li>
     *   <li>{@code JavaFileEditorInput} / {@code FileStoreEditorInput} — Eclipse 3.2/3.3 file open</li>
     * </ul>
     * </p>
     */
    protected void performSaveAs( IProgressMonitor progressMonitor )
    {
        // detect IDE or RCP:
        // check if perspective org.eclipse.ui.resourcePerspective is available
        boolean isIDE = CommonUIUtils.isIDEEnvironment();

        if ( isIDE )
        {
            // Just call super implementation for now
            IPreferenceStore store = EditorsUI.getPreferenceStore();
            String key = getEditorSite().getId() + ".internal.delegateSaveAs"; // $NON-NLS-1$ //$NON-NLS-1$
            store.setValue( key, true );
            super.performSaveAs( progressMonitor );
        }
        else
        {
            // Open FileDialog
            Shell shell = getSite().getShell();
            final IEditorInput input = getEditorInput();

            IDocumentProvider provider = getDocumentProvider();
            final IEditorInput newInput;

            FileDialog dialog = new FileDialog( shell, SWT.SAVE );

            String path = dialog.open();
            if ( path == null )
            {
                if ( progressMonitor != null )
                {
                    progressMonitor.setCanceled( true );
                }
                return;
            }

            // Check whether file exists and if so, confirm overwrite
            final File externalFile = new File( path );
            if ( externalFile.exists() )
            {
                MessageDialog overwriteDialog = new MessageDialog(
                    shell,
                    Messages.getString( "LdifEditor.Overwrite" ), null, Messages.getString( "LdifEditor.OverwriteQuestion" ), //$NON-NLS-1$ //$NON-NLS-2$
                    MessageDialog.WARNING, new String[]
                        { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 1 ); // 'No' is the default
                if ( overwriteDialog.open() != Window.OK )
                {
                    if ( progressMonitor != null )
                    {
                        progressMonitor.setCanceled( true );
                        return;
                    }
                }
            }

            IPath iPath = new Path( path );
            newInput = new PathEditorInput( iPath );

            boolean success = false;
            try
            {
                provider.aboutToChange( newInput );
                provider.saveDocument( progressMonitor, newInput, provider.getDocument( input ), true );
                success = true;
            }
            catch ( CoreException x )
            {
                final IStatus status = x.getStatus();
                if ( status == null || status.getSeverity() != IStatus.CANCEL )
                {
                    String title = Messages.getString( "LdifEditor.ErrorInSaveAs" ); //$NON-NLS-1$
                    String msg = Messages.getString( "LdifEditor.ErrorInSaveAs" ) + x.getMessage(); //$NON-NLS-1$
                    MessageDialog.openError( shell, title, msg );
                }
            }
            finally
            {
                provider.changed( newInput );
                if ( success )
                {
                    setInput( newInput );
                }
            }

            if ( progressMonitor != null )
            {
                progressMonitor.setCanceled( !success );
            }
        }

    }

    /** The keyboard-context activation token (non-null while editor is active). */
    private IContextActivation contextActivation;


    // ── PART DEACTIVATED ─────────────────────────────────────────────────────
    // The officer leaves the console; global action handlers and keyboard
    // context are released.
    /**
     * {@inheritDoc}
     *
     * <p>Deactivates global action handlers and the LDIF keyboard context when
     * this editor part loses focus.</p>
     */
    public void partDeactivated( IWorkbenchPartReference partRef )
    {
        if ( partRef.getPart( false ) == this && contextActivation != null )
        {
            deactivateGlobalActionHandlers();

            IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                IContextService.class );
            contextService.deactivateContext( contextActivation );
            contextActivation = null;
        }
    }


    // ── PART ACTIVATED ───────────────────────────────────────────────────────
    // The officer sits back down; global action handlers and keyboard context
    // are activated again.
    /**
     * {@inheritDoc}
     *
     * <p>Activates the browser-windows keyboard context and the global action
     * handlers when this editor gains focus.</p>
     */
    public void partActivated( IWorkbenchPartReference partRef )
    {
        if ( partRef.getPart( false ) == this )
        {
            IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                IContextService.class );
            contextActivation = contextService.activateContext( BrowserCommonConstants.CONTEXT_WINDOWS );

            activateGlobalActionHandlers();
        }
    }


    // ── PART BROUGHT TO TOP ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op.</p>
     */
    public void partBroughtToTop( IWorkbenchPartReference partRef )
    {
    }


    // ── PART CLOSED ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op.</p>
     */
    public void partClosed( IWorkbenchPartReference partRef )
    {
    }


    // ── PART HIDDEN ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op.</p>
     */
    public void partHidden( IWorkbenchPartReference partRef )
    {
    }


    // ── PART INPUT CHANGED ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op.</p>
     */
    public void partInputChanged( IWorkbenchPartReference partRef )
    {
    }


    // ── PART OPENED ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op.</p>
     */
    public void partOpened( IWorkbenchPartReference partRef )
    {
    }


    // ── PART VISIBLE ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op.</p>
     */
    public void partVisible( IWorkbenchPartReference partRef )
    {
    }


    // ── ACTIVATE GLOBAL ACTION HANDLERS ──────────────────────────────────────
    // The officer plugs in the attribute-edit, value-edit, and record-edit
    // shortcuts so they respond to keyboard triggers.
    /**
     * Registers the edit-attribute, edit-value, and edit-record actions as
     * global handlers so keyboard shortcuts work regardless of focus within the
     * editor.
     */
    public void activateGlobalActionHandlers()
    {
        IAction elaa = getAction( BrowserCommonConstants.ACTION_ID_EDIT_ATTRIBUTE_DESCRIPTION );
        ActionUtils.activateActionHandler( elaa );
        IAction elva = getAction( BrowserCommonConstants.ACTION_ID_EDIT_VALUE );
        ActionUtils.activateActionHandler( elva );
        IAction elra = getAction( LdifEditorConstants.ACTION_ID_EDIT_RECORD );
        ActionUtils.activateActionHandler( elra );
    }


    // ── DEACTIVATE GLOBAL ACTION HANDLERS ────────────────────────────────────
    // The officer unplugs the shortcuts before leaving the console.
    /**
     * Deregisters the edit-attribute, edit-value, and edit-record global action
     * handlers.
     */
    public void deactivateGlobalActionHandlers()
    {
        IAction elaa = getAction( BrowserCommonConstants.ACTION_ID_EDIT_ATTRIBUTE_DESCRIPTION );
        ActionUtils.deactivateActionHandler( elaa );
        IAction elva = getAction( BrowserCommonConstants.ACTION_ID_EDIT_VALUE );
        ActionUtils.deactivateActionHandler( elva );
        IAction elra = getAction( LdifEditorConstants.ACTION_ID_EDIT_RECORD );
        ActionUtils.deactivateActionHandler( elra );
    }


    // ── RETURN THE VALUE EDITOR MANAGER ──────────────────────────────────────
    /**
     * Returns the {@link ValueEditorManager} for this editor, which provides
     * all registered value-editor extensions.
     *
     * @return the value-editor manager
     */
    public ValueEditorManager getValueEditorManager()
    {
        return valueEditorManager;
    }

}
