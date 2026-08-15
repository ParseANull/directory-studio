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
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserCategory;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserEntryPage;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserSearchResultPage;
import org.apache.directory.studio.ldapbrowser.core.jobs.CreateEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewEntryWizard — LUKE ASSEMBLES HIS LIGHTSABER IN RETURN OF THE JEDI ─
// In a quiet corner of Tatooine, Luke lays out all the components on his
// workbench: first he decides the blade's origin (from scratch or from a
// template Jedi's saber), then he picks the kyber crystal (object class),
// then he inscribes where this lightsaber belongs in the Force (the DN), and
// finally he fills in every specification on the data plate (the attributes).
// One step at a time, each page in sequence, each correct before moving on —
// and when he ignites it for the first time, the entry is created on the server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main JFace wizard for creating a new LDAP entry from scratch or by
 * cloning an existing entry as a template.
 * Coordinates four sequential pages:
 * <ol>
 *   <li>{@link NewEntryTypeWizardPage}       — choose the creation method</li>
 *   <li>{@link NewEntryObjectclassWizardPage} — pick object classes</li>
 *   <li>{@link NewEntryDnWizardPage}          — compose the DN</li>
 *   <li>{@link NewEntryAttributesWizardPage}  — fill in attribute values</li>
 * </ol>
 * On finish, calls {@link CreateEntryRunnable} to write the new entry to the
 * server.  The connection is temporarily set read-only during the wizard to
 * prevent concurrent modifications.
 * Think of this class as Luke's lightsaber assembly session — every step in
 * order, every component verified before the next one is added.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewEntryWizard extends Wizard implements INewWizard
{

    /** The type page. */
    protected NewEntryTypeWizardPage typePage;

    /** The object class page. */
    protected NewEntryObjectclassWizardPage ocPage;

    /** The dn page. */
    protected NewEntryDnWizardPage dnPage;

    /** The attributes page. */
    protected NewEntryAttributesWizardPage attributePage;

    /** The selected entry. */
    protected IEntry selectedEntry;

    /** The selected connection. */
    protected IBrowserConnection selectedConnection;

    /** The read only flag of the selected connection. */
    protected boolean originalReadOnlyFlag;

    /** The prototype entry. */
    protected DummyEntry prototypeEntry;


    // ── Luke Lays Out His Workbench ────────────────────────────────────────────
    // Luke spreads a cloth over the workbench and enables the progress monitor
    // so Obi-Wan can see how the assembly is going in the background.
    // No components are placed yet — that happens in init() once we know which
    // forge (connection) we'll be working with.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code NewEntryWizard} with a progress monitor enabled.
     * The selected entry and connection are {@code null} until {@link #init}
     * is called by the Eclipse workbench.
     *
     * <p>For example — Luke spreads the workbench cloth, nothing on it yet:</p>
     * <pre>
     *   NewEntryWizard wizard = new NewEntryWizard();
     *   // selectedConnection = null; prototypeEntry = null;
     *   // init() will fill them in
     * </pre>
     */
    public NewEntryWizard()
    {
        setNeedsProgressMonitor( true );
    }


    // ── The Lightsaber's Registry Serial Number ────────────────────────────────
    // Every lightsaber in the Jedi Archives has a serial number that the Order
    // uses to look it up in the registry — this static method returns the
    // Eclipse wizard-registry ID so the platform can find and launch this wizard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard registry ID for this wizard.
     * The platform uses this string to look up and launch the wizard from
     * menu contributions, keybindings, or plugin extension points.
     *
     * <p>For example — the Archives clerk looks up the serial number of Luke's lightsaber:</p>
     * <pre>
     *   String id = NewEntryWizard.getId();
     *   // → BrowserCommonConstants.WIZARD_NEW_ENTRY_WIZARD
     * </pre>
     *
     * @return  The unique wizard ID from {@link BrowserCommonConstants}.
     */
    public static String getId()
    {
        return BrowserCommonConstants.WIZARD_NEW_ENTRY_WIZARD;
    }

    // ── Luke Identifies His Workbench and Gathers the Right Materials ─────────
    // Luke walks into the workshop and figures out which forge he'll use and
    // which template saber (if any) is already laid out on the bench.
    // The workbench selection can be an entry, a search result, a bookmark —
    // we need to dig out the connection from whatever was selected so we know
    // where to send the final entry.  The connection is immediately set
    // read-only to prevent concurrent changes while the wizard is open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the Eclipse workbench to initialise the wizard.
     * Determines the selected entry and connection from the current workbench
     * selection — handles {@link IEntry}, {@link ISearchResult}, {@link IBookmark},
     * {@link IAttribute}, {@link IValue}, {@link ISearch}, {@link IBrowserConnection},
     * {@link BrowserCategory}, {@link BrowserSearchResultPage}, and
     * {@link BrowserEntryPage}.
     * The connection is temporarily set read-only during the wizard to guard
     * against concurrent modifications, and a blank {@link DummyEntry} prototype
     * is created.
     *
     * <p>For example — Luke surveys the workshop and grabs the connection to the right forge:</p>
     * <pre>
     *   selectedEntry      = ... (from workbench selection)
     *   selectedConnection = ... (from the entry's browser connection)
     *   selectedConnection.getConnection().setReadOnly( true ); // lock forge
     *   prototypeEntry     = new DummyEntry( new Dn(), selectedConnection );
     * </pre>
     *
     * @param workbench  The current workbench; not used directly.
     * @param selection  The current structured selection; used to extract the
     *                   target entry and connection.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // determine the currently selected entry
        Object selected = selection.getFirstElement();

        if ( isNewContextEntry() )
        {
            setWindowTitle( Messages.getString( "NewEntryWizard.NewContextEntry" ) ); //$NON-NLS-1$
        }
        else
        {
            setWindowTitle( Messages.getString( "NewEntryWizard.NewEntry" ) ); //$NON-NLS-1$
        }

        if ( selected instanceof IEntry )
        {
            selectedEntry = ( ( IEntry ) selected );
            selectedConnection = selectedEntry.getBrowserConnection();
        }
        else if ( selected instanceof ISearchResult )
        {
            selectedEntry = ( ( ISearchResult ) selected ).getEntry();
            selectedConnection = selectedEntry.getBrowserConnection();
        }
        else if ( selected instanceof IBookmark )
        {
            selectedEntry = ( ( IBookmark ) selected ).getEntry();
            selectedConnection = selectedEntry.getBrowserConnection();
        }
        else if ( selected instanceof IAttribute )
        {
            selectedEntry = ( ( IAttribute ) selected ).getEntry();
            selectedConnection = selectedEntry.getBrowserConnection();
        }
        else if ( selected instanceof IValue )
        {
            selectedEntry = ( ( IValue ) selected ).getAttribute().getEntry();
            selectedConnection = selectedEntry.getBrowserConnection();
        }
        else if ( selected instanceof ISearch )
        {
            selectedEntry = null;
            selectedConnection = ( ( ISearch ) selected ).getBrowserConnection();
        }
        else if ( selected instanceof IBrowserConnection )
        {
            selectedEntry = null;
            selectedConnection = ( IBrowserConnection ) selected;
        }
        else if ( selected instanceof BrowserCategory )
        {
            selectedEntry = null;
            selectedConnection = ( ( BrowserCategory ) selected ).getParent();
        }
        else if ( selected instanceof BrowserSearchResultPage )
        {
            selectedEntry = null;
            selectedConnection = ( ( BrowserSearchResultPage ) selected ).getSearch().getBrowserConnection();
        }
        else if ( selected instanceof BrowserEntryPage )
        {
            selectedEntry = null;
            selectedConnection = ( ( BrowserEntryPage ) selected ).getEntry().getBrowserConnection();
        }
        else
        {
            selectedEntry = null;
            selectedConnection = null;
        }

        if ( selectedConnection != null )
        {
            if ( selectedConnection.getConnection() != null )
            {
                originalReadOnlyFlag = selectedConnection.getConnection().isReadOnly();
                selectedConnection.getConnection().setReadOnly( true );
            }

            prototypeEntry = new DummyEntry( new Dn(), selectedConnection );
        }
    }


    // ── Luke Lines Up the Four Assembly Stages on the Workbench ──────────────
    // Luke sets out four labelled stations on the bench: creation method,
    // crystal selection (object class), position inscription (DN), and data plate
    // (attributes).  If no connection was found, he replaces them all with a
    // single "wrong workbench" notice.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the four wizard pages when a connection is available, or a single
     * dummy page when no connection was determined from the workbench selection.
     *
     * <p>For example — Luke lines up his four assembly stations:</p>
     * <pre>
     *   typePage      = new NewEntryTypeWizardPage( ... );      // station 1
     *   ocPage        = new NewEntryObjectclassWizardPage( ... ); // station 2
     *   dnPage        = new NewEntryDnWizardPage( ... );         // station 3
     *   attributePage = new NewEntryAttributesWizardPage( ... ); // station 4
     * </pre>
     */
    public void addPages()
    {
        if ( selectedConnection != null )
        {
            typePage = new NewEntryTypeWizardPage( NewEntryTypeWizardPage.class.getName(), this );
            addPage( typePage );

            ocPage = new NewEntryObjectclassWizardPage( NewEntryObjectclassWizardPage.class.getName(), this );
            addPage( ocPage );

            dnPage = new NewEntryDnWizardPage( NewEntryDnWizardPage.class.getName(), this );
            addPage( dnPage );

            attributePage = new NewEntryAttributesWizardPage( NewEntryAttributesWizardPage.class.getName(), this );
            addPage( attributePage );
        }
        else
        {
            IWizardPage page = new DummyWizardPage();
            addPage( page );
        }
    }


    // ── Luke Attaches the Reference Cards to Each Station ─────────────────────
    // Once the stations are built, Luke pins a reference card (help context ID)
    // to each one so that pressing F1 at any stage opens the right section of
    // the Jedi training manual — the Eclipse help system.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wires Eclipse help context IDs to each wizard page after the SWT controls
     * are created.
     * Pressing F1 on any page opens the {@code tools_newentry_wizard} section
     * of the LDAP browser help system.
     *
     * <p>For example — Luke pins a Jedi manual reference card to each workbench station:</p>
     * <pre>
     *   PlatformUI.getWorkbench().getHelpSystem()
     *       .setHelp( typePage.getControl(), PLUGIN_ID + ".tools_newentry_wizard" );
     * </pre>
     *
     * @param pageContainer  The SWT composite that hosts all wizard page controls.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        if ( selectedConnection != null )
        {
            if ( typePage != null )
            {
                PlatformUI.getWorkbench().getHelpSystem().setHelp( typePage.getControl(),
                    BrowserCommonConstants.PLUGIN_ID + "." + "tools_newentry_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if ( ocPage != null )
            {
                PlatformUI.getWorkbench().getHelpSystem().setHelp( ocPage.getControl(),
                    BrowserCommonConstants.PLUGIN_ID + "." + "tools_newentry_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if ( dnPage != null )
            {
                PlatformUI.getWorkbench().getHelpSystem().setHelp( dnPage.getControl(),
                    BrowserCommonConstants.PLUGIN_ID + "." + "tools_newentry_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if ( attributePage != null )
            {
                PlatformUI.getWorkbench().getHelpSystem().setHelp( attributePage.getControl(),
                    BrowserCommonConstants.PLUGIN_ID + "." + "tools_newentry_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    // ── CLASS: DummyWizardPage — THE WRONG WORKBENCH, NO PARTS TO ASSEMBLE ───
    // Luke wanders into the wrong workshop — there's no forge here, no connection
    // to a server, and the workbench is completely bare.  A notice on the board
    // tells him "No Connection Selected" so he knows to go find the right workshop.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A placeholder page shown when the wizard is opened without a valid LDAP
     * connection in the workbench selection.
     * Tells the user they need to select a connection before the wizard can proceed.
     * Think of it as Luke arriving at an empty workshop with no forge.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class DummyWizardPage extends WizardPage
    {

        // ── Luke Reads the "Wrong Workshop" Notice ────────────────────────────
        // Luke glances at the board: "No Connection Selected — please choose a
        // connection in the LDAP Browser first."  The page is marked complete
        // so Cancel is available but the four real pages are never shown.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates the dummy page with a title and description explaining that
         * no connection was found in the workbench selection.
         *
         * <p>For example — Luke reads the notice on the empty workshop door:</p>
         * <pre>
         *   setTitle( "No Connection Selected" );
         *   setDescription( "Select a connection in the LDAP Browser and try again." );
         * </pre>
         */
        protected DummyWizardPage()
        {
            super( "" ); //$NON-NLS-1$
            setTitle( Messages.getString( "NewEntryWizard.NoConnectonSelected" ) ); //$NON-NLS-1$
            setDescription( Messages.getString( "NewEntryWizard.NoConnectonSelectedDescription" ) ); //$NON-NLS-1$
            setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
                BrowserCommonConstants.IMG_ENTRY_WIZARD ) );
            setPageComplete( true );
        }


        // ── Luke Surveys the Empty Workshop — Nothing to Build ────────────────
        // The workshop is bare — no components, no stations, nothing to interact
        // with.  The UI renders an empty composite for the same reason.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates the (blank) SWT control for this dummy page.
         * There is nothing useful to display beyond the title and description, so
         * we create an empty composite.
         *
         * <p>For example — Luke stares at the bare walls of the wrong workshop:</p>
         * <pre>
         *   Composite composite = new Composite( parent, SWT.NONE );
         *   setControl( composite ); // empty — no forge, no parts
         * </pre>
         *
         * @param parent  The parent composite provided by the wizard dialog framework.
         */
        public void createControl( Composite parent )
        {
            Composite composite = new Composite( parent, SWT.NONE );
            GridLayout gl = new GridLayout( 1, false );
            composite.setLayout( gl );
            composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

            setControl( composite );
        }
    }


    // ── Luke Sets Down the Parts Unfinished — Abort Assembly ──────────────────
    // Luke puts the components back in their box and restores the forge to its
    // original state — the connection read-only flag is put back to whatever it
    // was before the wizard opened, so the rest of the application keeps working.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user cancels the wizard.
     * Restores the connection's read-only flag to its original value so other
     * operations against the server can proceed normally after the wizard closes.
     *
     * <p>For example — Luke returns the forge to its pre-assembly state:</p>
     * <pre>
     *   selectedConnection.getConnection().setReadOnly( originalReadOnlyFlag );
     * </pre>
     *
     * @return  Always {@code true} — cancellation always succeeds.
     */
    public boolean performCancel()
    {
        if ( selectedConnection != null && selectedConnection.getConnection() != null )
        {
            selectedConnection.getConnection().setReadOnly( originalReadOnlyFlag );
        }

        return true;
    }


    // ── Luke Ignites the Completed Lightsaber for the First Time ─────────────
    // Luke presses the activator and the blade extends — the prototype is sent
    // to the LDAP server via a CreateEntryRunnable, the connection read-only
    // flag is restored, and dialog settings are saved for next time.
    // If the server rejects the entry, the flag stays read-only and we return
    // false so the wizard stays open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks Finish.
     * Restores the connection read-only flag, saves dialog settings from the type
     * and DN pages, then executes a {@link CreateEntryRunnable} to write the
     * prototype entry to the server.
     * Returns {@code false} (keeping the wizard open) if the server call fails
     * — in that case the connection is re-locked read-only.
     *
     * <p>For example — Luke ignites the completed lightsaber; it either works or he tries again:</p>
     * <pre>
     *   selectedConnection.getConnection().setReadOnly( originalReadOnlyFlag );
     *   CreateEntryRunnable runnable = new CreateEntryRunnable( prototypeEntry, connection );
     *   IStatus status = RunnableContextRunner.execute( runnable, getContainer(), true );
     *   if ( !status.isOK() ) { setReadOnly( true ); return false; }
     * </pre>
     *
     * @return  {@code true} if the entry was created successfully; {@code false} on failure.
     */
    public boolean performFinish()
    {
        try
        {
            if ( selectedConnection != null && selectedConnection.getConnection() != null )
            {
                selectedConnection.getConnection().setReadOnly( originalReadOnlyFlag );

                typePage.saveDialogSettings();
                dnPage.saveDialogSettings();

                CreateEntryRunnable runnable = new CreateEntryRunnable( prototypeEntry, selectedConnection );
                IStatus status = RunnableContextRunner.execute( runnable, getContainer(), true );

                if ( !status.isOK() )
                {
                    selectedConnection.getConnection().setReadOnly( true );

                    return false;
                }
                else
                {
                    return true;
                }
            }
            else
            {
                return true;
            }
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            return false;
        }
    }


    // ── Luke Checks Which Component He Started From ────────────────────────────
    // Luke looks at his workbench to see which entry was laid out as the starting
    // point — this might be a template saber he's cloning, or it might be null
    // if he's building from scratch.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP entry that was selected when the wizard was opened.
     * Used as the suggested parent DN on the DN page when creating regular
     * (non-context) entries.  May be {@code null} if nothing was selected or
     * if the selection was a connection-level object rather than an entry.
     *
     * <p>For example — Luke checks which saber template is on his workbench:</p>
     * <pre>
     *   IEntry start = wizard.getSelectedEntry(); // may be null
     * </pre>
     *
     * @return  The selected entry, or {@code null} if none was determined.
     */
    public IEntry getSelectedEntry()
    {
        return selectedEntry;
    }


    // ── Luke Confirms Which Forge He's Using ───────────────────────────────────
    // Luke checks the forge's identification plate — which LDAP connection are
    // we creating the entry on?  Every page needs this to perform schema lookups
    // and eventually to send the new entry to the right server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection that the new entry will be created on.
     * Used by all wizard pages for schema lookups and by {@link #performFinish()}
     * to know where to send the {@link CreateEntryRunnable}.
     *
     * <p>For example — Luke checks the identification plate on the forge:</p>
     * <pre>
     *   IBrowserConnection conn = wizard.getSelectedConnection();
     *   conn.getSchema(); // used to validate object classes and attributes
     * </pre>
     *
     * @return  The connection hosting the new entry, or {@code null} if not set.
     */
    public IBrowserConnection getSelectedConnection()
    {
        return selectedConnection;
    }


    // ── Luke Checks the In-Progress Lightsaber Assembly ───────────────────────
    // Luke peers at the workbench to see how the prototype is shaping up —
    // each page modifies this same DummyEntry in memory, and at the end it's
    // what gets sent to the server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the in-memory prototype {@link DummyEntry} that wizard pages
     * build up step by step.
     * All pages read and write to this same instance; on finish it is submitted
     * to the server via {@link CreateEntryRunnable}.
     *
     * <p>For example — Luke looks at the half-assembled lightsaber on the bench:</p>
     * <pre>
     *   DummyEntry proto = wizard.getPrototypeEntry();
     *   // pages modify proto's objectClass, DN, and attributes
     * </pre>
     *
     * @return  The prototype entry being built up through the wizard pages.
     */
    public DummyEntry getPrototypeEntry()
    {
        return prototypeEntry;
    }


    // ── Luke Swaps In a New Draft Blade ───────────────────────────────────────
    // Mid-assembly, Luke decides the prototype needs to be replaced with a
    // freshly cloned template — he sets the new draft on the workbench and
    // subsequent pages pick it up automatically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the current prototype entry with the given one.
     * Called by {@link NewEntryTypeWizardPage#getNextPage()} when the "use
     * template" path clones a server entry and installs the clone as the new
     * prototype.
     *
     * <p>For example — Luke sets a freshly cloned saber template on the workbench:</p>
     * <pre>
     *   wizard.setPrototypeEntry( clonedEntry );
     *   // subsequent pages now see the new prototype
     * </pre>
     *
     * @param getPrototypeEntry  The new prototype to use; subsequent pages will
     *                           read and write to this instance.
     */
    public void setPrototypeEntry( DummyEntry getPrototypeEntry )
    {
        this.prototypeEntry = getPrototypeEntry;
    }


    // ── Is This the Root Component or Just a Blade? ───────────────────────────
    // Luke asks: "Is this saber the master template — the one everything else
    // references — or is it just a regular blade?"  For most entries, it's just
    // a regular entry with a parent.  Subclasses like NewContextEntryWizard
    // override this to say "yes, this is the root."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this wizard creates a context (root) entry.
     * The base implementation always returns {@code false}; overridden by
     * {@link NewContextEntryWizard} to return {@code true}.
     * The value controls how the DN page renders (full-DN combo vs. RDN+parent
     * picker) and how {@link #init} sets the window title.
     *
     * <p>For example — Luke checks: root master template or just a regular blade?</p>
     * <pre>
     *   if ( wizard.isNewContextEntry() ) {
     *     setWindowTitle( "New Context Entry" ); // root of a naming context
     *   }
     * </pre>
     *
     * @return  {@code false} for regular entries; {@code true} for context entries.
     */
    public boolean isNewContextEntry()
    {
        return false;
    }

}
