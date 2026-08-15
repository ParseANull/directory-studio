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

package org.apache.directory.studio.ldapbrowser.common.wizards;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReadEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: NewEntryTypeWizardPage — YODA ASKS "FROM SCRATCH OR FROM TEMPLATE?" ─
// At the very start of Luke's training on Dagobah, Yoda faces him with the
// first real decision: "Build your skills from nothing, will you?  Or model
// them after a Jedi who came before?"  Depending on Luke's answer, Yoda either
// conjures a fresh prototype or goes and fetches an existing Jedi's record to
// clone.
// This wizard page presents the same fork to the user: create a new LDAP entry
// from scratch (guided by schema alone) or copy an existing entry as a template
// and then modify it.  The user selects via radio buttons; the template path
// also requires pointing at a specific entry in the browser.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The first page of {@link NewEntryWizard} — lets the user choose between
 * creating a new LDAP entry from scratch or using an existing entry as a template.
 * "From scratch" creates an empty {@link DummyEntry} and lets the schema
 * guide the process; "use template" fetches a real entry from the server,
 * strips non-modifiable attributes, and uses that as the starting point.
 * Think of this page as Yoda's opening question at the start of Luke's training —
 * the path you choose here shapes everything that follows.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewEntryTypeWizardPage extends WizardPage implements WidgetModifyListener, SelectionListener
{

    /** The Constant PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY. */
    public static final String PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY = NewEntryTypeWizardPage.class
        .getName()
        + ".preferredEntryCreationMethod"; //$NON-NLS-1$

    /** The wizard. */
    private NewEntryWizard wizard;

    /** The schema button. */
    private Button schemaButton;

    /** The template button. */
    private Button templateButton;

    /** The entry widget to select the template entry. */
    private EntryWidget entryWidget;


    // ── Yoda Prepares the First Training Question ─────────────────────────────
    // Yoda settles onto his walking stick and gets ready to pose the opening
    // question to Luke — the page title and description are set, and the
    // "incomplete" state is established so the Next button stays grey until
    // Luke actually makes a choice.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code NewEntryTypeWizardPage} with its title, description,
     * and icon.  The page starts incomplete — the user must choose a radio
     * button before Next becomes available.
     *
     * <p>For example — Yoda composes his opening question and waits for Luke's answer:</p>
     * <pre>
     *   setTitle( "Entry Creation Method" );
     *   setDescription( "Please select the entry creation method" );
     *   setPageComplete( false ); // Next is disabled until Luke picks one
     * </pre>
     *
     * @param pageName  Internal wizard page identifier.
     * @param wizard    The parent {@link NewEntryWizard} that coordinates all pages.
     */
    public NewEntryTypeWizardPage( String pageName, NewEntryWizard wizard )
    {
        super( pageName );
        setTitle( Messages.getString( "NewEntryTypeWizardPage.EntryCreationMethod" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewEntryTypeWizardPage.EntryCreationMethodDescription" ) ); //$NON-NLS-1$
        setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
            BrowserCommonConstants.IMG_ENTRY_WIZARD ) );
        setPageComplete( false );

        this.wizard = wizard;
    }


    // ── Yoda Checks Whether Luke Has Actually Made a Choice ───────────────────
    // Yoda peers at Luke — has he answered "from scratch" or pointed to a
    // specific Jedi record as a template?  If neither radio is selected, or
    // the template radio is on but no entry has been pointed to, Yoda waits.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the Next button based on the current radio selection.
     * "From scratch" is always valid once selected.  "Template" is valid only
     * when both a connection and a DN have been set in the entry widget.
     *
     * <p>For example — Yoda waits until Luke gives a complete answer:</p>
     * <pre>
     *   if ( schemaButton.getSelection() )      → setPageComplete( true )
     *   if ( templateButton.getSelection() )    → valid only if connection + DN set
     *   else                                    → setPageComplete( false )
     * </pre>
     */
    private void validate()
    {
        if ( schemaButton.getSelection() )
        {
            setPageComplete( true );
        }
        else if ( templateButton.getSelection() )
        {
            setPageComplete( entryWidget.getBrowserConnection() != null && entryWidget.getDn() != null );
        }
        else
        {
            setPageComplete( false );
        }
    }


    // ── Yoda Checks: Has Luke Actually Answered? Can We Move On? ─────────────
    // Before leading Luke to the next trial, Yoda checks whether Luke has
    // given a complete answer — he doesn't pre-fetch any template yet, he
    // just confirms the page is done.  This avoids unnecessary server reads
    // if the user is still mulling their choice.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} only when the page is complete, without triggering
     * the heavier {@link #getNextPage()} logic.
     * We override this to avoid creating a new prototype entry prematurely —
     * that work only happens in {@link #getNextPage()} when the user actually
     * clicks Next.
     *
     * <p>For example — Yoda confirms Luke answered before moving to the next trial:</p>
     * <pre>
     *   return isPageComplete(); // fast check only — no server calls
     * </pre>
     *
     * @return  {@code true} if the page is complete and Next can be pressed.
     */
    public boolean canFlipToNextPage()
    {
        return isPageComplete();
    }


    // ── Yoda Builds Luke's Prototype Before the Next Trial ────────────────────
    // When Luke presses Next, Yoda either creates a blank training record
    // (from scratch) or fetches an existing Jedi's record, strips out the
    // non-transferable attributes, and hands Luke a clean copy to work from.
    // If the template entry can't be found, Yoda shows Luke an error and stays.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the prototype {@link DummyEntry} for the wizard before advancing
     * to the next page.
     * For "from scratch" we create an empty entry; for "template" we read the
     * chosen entry from the server, clone it via an LDIF round-trip, drop
     * any non-modifiable attributes, and install the clone as the prototype.
     * Error dialogs are shown if the connection or DN is missing, or if the
     * template entry doesn't exist on the server.
     *
     * <p>For example — Yoda fetches a Jedi record and strips out the classified parts:</p>
     * <pre>
     *   LdifContentRecord record = ModelConverter.entryToLdifContentRecord( templateEntry );
     *   DummyEntry proto = ModelConverter.ldifContentRecordToEntry( record, connection );
     *   // remove non-modifiable attributes from proto
     *   wizard.setPrototypeEntry( proto );
     * </pre>
     *
     * @return  The next {@link IWizardPage}, or {@code null} if there is an error.
     */
    public IWizardPage getNextPage()
    {
        if ( templateButton.getSelection() )
        {
            final IBrowserConnection browserConnection = entryWidget.getBrowserConnection();
            final Dn dn = entryWidget.getDn();
            IEntry templateEntry = null;

            if ( browserConnection == null )
            {
                getShell().getDisplay().syncExec( new Runnable()
                {
                    public void run()
                    {
                        MessageDialog
                            .openError(
                                getShell(),
                                Messages.getString( "NewEntryTypeWizardPage.Error" ), Messages.getString( "NewEntryTypeWizardPage.NoConnection" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } );
                return null;
            }
            if ( dn == null )
            {
                getShell().getDisplay().syncExec( new Runnable()
                {
                    public void run()
                    {
                        MessageDialog
                            .openError(
                                getShell(),
                                Messages.getString( "NewEntryTypeWizardPage.Error" ), Messages.getString( "NewEntryTypeWizardPage.NoDN" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } );
                return null;
            }

            // check if selected Dn exists
            ReadEntryRunnable readEntryRunnable = new ReadEntryRunnable( browserConnection, dn );
            RunnableContextRunner.execute( readEntryRunnable, getContainer(), false );
            templateEntry = readEntryRunnable.getReadEntry();
            if ( templateEntry == null )
            {
                getShell().getDisplay().syncExec( new Runnable()
                {
                    public void run()
                    {
                        MessageDialog
                            .openError(
                                getShell(),
                                Messages.getString( "NewEntryTypeWizardPage.Error" ), NLS.bind( Messages.getString( "NewEntryTypeWizardPage.EntryDoesNotExist" ), dn.toString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } );
                return null;
            }

            // init attributes
            if ( !templateEntry.isAttributesInitialized() )
            {
                InitializeAttributesRunnable runnable = new InitializeAttributesRunnable( templateEntry );
                RunnableContextRunner.execute( runnable, getContainer(), true );
            }

            // clone entry and remove non-modifiable attributes
            try
            {
                EventRegistry.suspendEventFiringInCurrentThread();

                LdifContentRecord record = ModelConverter.entryToLdifContentRecord( templateEntry );
                DummyEntry prototypeEntry = ModelConverter.ldifContentRecordToEntry( record, browserConnection );
                IAttribute[] attributes = prototypeEntry.getAttributes();
                for ( int i = 0; i < attributes.length; i++ )
                {
                    if ( !SchemaUtils.isModifiable( attributes[i].getAttributeTypeDescription() ) )
                    {
                        prototypeEntry.deleteAttribute( attributes[i] );
                    }
                }
                wizard.setPrototypeEntry( prototypeEntry );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                EventRegistry.resumeEventFiringInCurrentThread();
            }
        }
        else
        {
            wizard.setPrototypeEntry( new DummyEntry( new Dn(), wizard.getSelectedConnection() ) );
        }

        return super.getNextPage();
    }


    // ── Yoda Lays Out the Two Training Options on the Ground ─────────────────
    // Yoda scratches two circles in the Dagobah mud: one for "from scratch"
    // and one for "from template."  Below the template circle he places an
    // entry-picker widget so Luke can point to the specific Jedi to emulate.
    // The last-used preference is recalled from dialog settings so Luke's
    // usual choice is pre-selected each time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT controls for this page — two radio buttons and an
     * {@link EntryWidget} for specifying the template entry.
     * The entry widget is enabled only when the template radio is selected.
     * The previously saved preference (from dialog settings) is used to
     * pre-select the right radio on first open.
     *
     * <p>For example — Yoda draws the two training paths on the ground for Luke:</p>
     * <pre>
     *   (o) Create entry from scratch
     *   ( ) Use existing entry as template
     *        [------entry picker------] [Browse]
     * </pre>
     *
     * @param parent  The parent composite supplied by the wizard dialog.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        schemaButton = BaseWidgetUtils.createRadiobutton( composite, Messages
            .getString( "NewEntryTypeWizardPage.CreateEntryFromScratch" ), 1 ); //$NON-NLS-1$
        schemaButton.addSelectionListener( this );
        templateButton = BaseWidgetUtils.createRadiobutton( composite, Messages
            .getString( "NewEntryTypeWizardPage.UseExistingEntryAsTemplate" ), 1 ); //$NON-NLS-1$
        templateButton.addSelectionListener( this );

        Composite entryComposite = BaseWidgetUtils.createColumnContainer( composite, 3, 1 );
        BaseWidgetUtils.createRadioIndent( entryComposite, 1 );
        entryWidget = new EntryWidget( wizard.getSelectedConnection(), wizard.getSelectedEntry() != null ? wizard
            .getSelectedEntry().getDn() : null );
        entryWidget.createWidget( entryComposite );
        entryWidget.addWidgetModifyListener( this );

        if ( BrowserCommonActivator.getDefault().getDialogSettings().get(
            PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY ) == null )
        {
            BrowserCommonActivator.getDefault().getDialogSettings().put(
                PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY, true );
        }
        schemaButton.setSelection( BrowserCommonActivator.getDefault().getDialogSettings().getBoolean(
            PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY ) );
        templateButton.setSelection( !BrowserCommonActivator.getDefault().getDialogSettings().getBoolean(
            PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY ) );
        widgetSelected( null );

        setControl( composite );
    }


    // ── Luke Changes the Template Entry — Yoda Re-Evaluates ──────────────────
    // Luke fiddles with the entry picker widget while the template radio is
    // selected — every time it changes, Yoda checks whether Luke has now
    // pointed at a valid entry, and updates the Next button accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fired when the {@link EntryWidget} content changes (user typed a DN
     * or browsed to an entry).
     * Re-validates the page so the Next button reflects the current state.
     *
     * <p>For example — Luke points at a new entry; Yoda checks whether it's valid:</p>
     * <pre>
     *   public void widgetModified( WidgetModifyEvent event ) { validate(); }
     * </pre>
     *
     * @param event  The widget-modify event from the entry picker; not used directly.
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        validate();
    }


    // ── Luke Double-Clicks a Radio Button — Yoda Remains Patient ─────────────
    // Luke double-clicks a radio by accident; Yoda doesn't flinch because
    // the default-selected event carries no extra meaning in this context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op implementation required by the {@link SelectionListener} interface.
     * Double-clicking a radio button in SWT fires this event; we treat it the
     * same as a single click (handled by {@link #widgetSelected(SelectionEvent)}).
     *
     * <p>For example — Luke taps the button twice; Yoda ignores the second tap:</p>
     * <pre>
     *   public void widgetDefaultSelected( SelectionEvent e ) { /* no-op *&#47; }
     * </pre>
     *
     * @param e  The selection event; ignored.
     */
    public void widgetDefaultSelected( SelectionEvent e )
    {
    }


    // ── Luke Picks a Radio Button — Yoda Adjusts the Training Setup ──────────
    // Luke points at "from scratch" or "from template" — Yoda immediately
    // enables or disables the entry-picker widget and re-checks whether the
    // page is complete.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fired when either radio button is selected.
     * Enables the entry-picker widget only when the template radio is active,
     * then re-validates the page.
     *
     * <p>For example — Luke selects "template"; Yoda activates the entry picker:</p>
     * <pre>
     *   entryWidget.setEnabled( templateButton.getSelection() );
     *   validate();
     * </pre>
     *
     * @param e  The selection event from the radio button; not used directly.
     */
    public void widgetSelected( SelectionEvent e )
    {
        entryWidget.setEnabled( templateButton.getSelection() );
        validate();
    }


    // ── Yoda Notes Down Luke's Preferred Training Approach ───────────────────
    // As the session ends, Yoda scratches a note about whether Luke prefers
    // to start from scratch or from a template — next time they meet, Yoda
    // will pre-select Luke's usual choice so they don't repeat themselves.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the currently selected entry creation method to Eclipse dialog
     * settings so the choice is remembered across wizard invocations.
     * Called by {@link NewEntryWizard#performFinish()}.
     *
     * <p>For example — Yoda writes down Luke's preference for next time:</p>
     * <pre>
     *   BrowserCommonActivator.getDefault().getDialogSettings()
     *       .put( PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY,
     *             schemaButton.getSelection() );
     * </pre>
     */
    public void saveDialogSettings()
    {
        BrowserCommonActivator.getDefault().getDialogSettings().put( PREFERRED_ENTRY_CREATION_METHOD_DIALOGSETTING_KEY,
            schemaButton.getSelection() );
    }

}
