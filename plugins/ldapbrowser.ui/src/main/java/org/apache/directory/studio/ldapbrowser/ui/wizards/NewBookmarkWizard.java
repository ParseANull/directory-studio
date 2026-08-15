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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserCategory;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserEntryPage;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserSearchResultPage;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Bookmark;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
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


// ── CLASS: NewBookmarkWizard — LEIA'S HOLOGRAM MARKS A TARGET ────────────────
// Leia's hologram says "Help me, Obi-Wan Kenobi" and pins a location in the
// Rebellion's memory: a name and a place. This wizard does the same for LDAP:
// the user gives a human-readable name and a target DN, and a Bookmark object
// is pinned to the browser connection so the entry is easy to jump back to.
// If no entry was selected when the wizard opened, a dummy page explains
// what's needed instead of showing a broken form.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wizard for creating a new LDAP browser bookmark.
 * Inspects the current workbench selection to find the target entry; if found,
 * shows {@link NewBookmarkMainWizardPage} so the user can name the bookmark and
 * optionally change its target DN. If no entry is selected, a placeholder
 * {@code DummyWizardPage} explains the requirement.
 * On Finish, a {@link Bookmark} is created and registered with the
 * browser connection's bookmark manager.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewBookmarkWizard extends Wizard implements INewWizard
{

    /** The main page. */
    private NewBookmarkMainWizardPage mainPage;

    /** The selected entry. */
    private IEntry selectedEntry;


    // ── Leia Prepares the Hologram Transmitter ────────────────────────────────────
    // The wizard doesn't need a progress monitor (no background work needed).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewBookmarkWizard with the localised "New Bookmark" title.
     * No progress monitor is needed because bookmark creation is instant.
     */
    public NewBookmarkWizard()
    {
        setWindowTitle( Messages.getString( "NewBookmarkWizard.NewBookmark" ) ); //$NON-NLS-1$
        setNeedsProgressMonitor( false );
    }


    // ── Leia Knows the Beacon ID ──────────────────────────────────────────────────
    // The wizard is reachable by constant ID from any action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the new bookmark wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_NEW_BOOKMARK;
    }


    // ── Leia Locks On to the Target ───────────────────────────────────────────────
    // We inspect the first selected element to find an IEntry. Accepted types:
    // IEntry directly, or any model object that carries a reference to one.
    // If none found, selectedEntry stays null and we show the DummyWizardPage.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Derives the target entry from the current workbench selection.
     * Accepts IEntry, ISearchResult, IBookmark, IAttribute, IValue,
     * IBrowserConnection (uses rootDSE), ISearch, BrowserCategory, BrowserSearchResultPage,
     * and BrowserEntryPage. Sets {@code selectedEntry} to null if no match.
     *
     * @param workbench  the current workbench (unused).
     * @param selection  the current structured selection.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // determine the currently selected entry, used
        // to preset the bookmark target Dn
        Object o = selection.getFirstElement();
        if ( o instanceof IEntry )
        {
            selectedEntry = ( ( IEntry ) o );
        }
        else if ( o instanceof ISearchResult )
        {
            selectedEntry = ( ( ISearchResult ) o ).getEntry();
        }
        else if ( o instanceof IBookmark )
        {
            selectedEntry = ( ( IBookmark ) o ).getEntry();
        }
        else if ( o instanceof IAttribute )
        {
            selectedEntry = ( ( IAttribute ) o ).getEntry();
        }
        else if ( o instanceof IValue )
        {
            selectedEntry = ( ( IValue ) o ).getAttribute().getEntry();
        }
        else if ( o instanceof IBrowserConnection )
        {
            selectedEntry = ( ( IBrowserConnection ) o ).getRootDSE();
        }
        else if ( o instanceof ISearch )
        {
            selectedEntry = ( ( ISearch ) o ).getBrowserConnection().getRootDSE();
        }
        else if ( o instanceof BrowserCategory )
        {
            selectedEntry = ( ( BrowserCategory ) o ).getParent().getRootDSE();
        }
        else if ( o instanceof BrowserSearchResultPage )
        {
            selectedEntry = ( ( BrowserSearchResultPage ) o ).getSearch().getBrowserConnection().getRootDSE();
        }
        else if ( o instanceof BrowserEntryPage )
        {
            selectedEntry = ( ( BrowserEntryPage ) o ).getEntry();
        }

        else
        {
            selectedEntry = null;
        }
    }


    // ── Leia Broadcasts the Hologram ──────────────────────────────────────────────
    // If an entry was found, show the real page. Otherwise, show the DummyWizardPage
    // that explains why the wizard can't do anything useful.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds {@link NewBookmarkMainWizardPage} if an entry was selected, or a
     * {@link DummyWizardPage} that explains the requirement if no entry was found.
     */
    public void addPages()
    {
        if ( selectedEntry != null )
        {
            mainPage = new NewBookmarkMainWizardPage( NewBookmarkMainWizardPage.class.getName(), selectedEntry, this );
            addPage( mainPage );
        }
        else
        {
            IWizardPage page = new DummyWizardPage();
            addPage( page );
        }
    }

    // ── CLASS: DummyWizardPage — LEIA'S HOLOGRAM SAYS "SELECT AN ENTRY FIRST" ────
    // When no entry is selected, the hologram can only say: "I need a target."
    // This placeholder page tells the user what they need to do.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * A placeholder page shown when no entry is selected in the browser.
     * Displays "No entry selected" with an instruction message. The page is
     * always complete so the user can Finish immediately (which does nothing useful,
     * but at least doesn't block the wizard close).
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    class DummyWizardPage extends WizardPage
    {

        // ── Leia's Hologram Announces the Target Requirement ──────────────────────
        // Title and description explain that an entry must be selected first.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Creates a new DummyWizardPage with the "no entry selected" title
         * and instruction description.
         */
        protected DummyWizardPage()
        {
            super( "" ); //$NON-NLS-1$
            setTitle( Messages.getString( "NewBookmarkWizard.NoEntrySelected" ) ); //$NON-NLS-1$
            setDescription( Messages.getString( "NewBookmarkWizard.InOrderToUse" ) ); //$NON-NLS-1$
            // setImageDescriptor(BrowserUIPlugin.getDefault().getImageDescriptor(BrowserUIConstants.IMG_ATTRIBUTE_WIZARD));
            setPageComplete( true );
        }


        // ── Leia's Hologram Shows an Empty Frame ──────────────────────────────────
        // No widgets — the description text in the header tells the whole story.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * {@inheritDoc}
         *
         * Creates an empty composite — all information is in the page title and
         * description; no additional widgets are needed.
         *
         * @param parent  the parent composite.
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


    // ── Leia Pins the Beacon ──────────────────────────────────────────────────────
    // If an entry was selected, we create the Bookmark object from the user's
    // name and DN, register it with the connection, and save dialog settings.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates a new {@link Bookmark} from the user's chosen name and DN and
     * registers it with the browser connection's bookmark manager.
     * Does nothing if no entry was selected (DummyWizardPage case), but still
     * calls saveDialogSettings() to persist the entry widget's directory.
     *
     * @return  {@code true} always.
     */
    public boolean performFinish()
    {
        if ( selectedEntry != null )
        {
            String name = mainPage.getBookmarkName();
            Dn dn = mainPage.getBookmarkDn();
            IBookmark bookmark = new Bookmark( selectedEntry.getBrowserConnection(), dn, name );
            selectedEntry.getBrowserConnection().getBookmarkManager().addBookmark( bookmark );
        }
        mainPage.saveDialogSettings();
        return true;
    }

}
