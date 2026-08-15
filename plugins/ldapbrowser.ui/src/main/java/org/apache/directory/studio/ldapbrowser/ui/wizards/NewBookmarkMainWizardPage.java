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
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


// ── CLASS: NewBookmarkMainWizardPage — LEIA RECORDS THE BEACON DETAILS ────────
// Leia's beacon has two pieces of information: a human-readable label
// (the bookmark name) and the exact coordinates (the LDAP DN).
// This page provides a pre-filled text field for the name (defaulting to the
// entry's DN string) and an EntryWidget so the user can navigate to or type
// the target DN. Both fields must be non-empty for the page to complete.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The single page of the new bookmark wizard.
 * Shows a text field for the bookmark name (pre-filled with the selected entry's
 * DN string) and an {@link EntryWidget} for the target DN (pre-loaded with the
 * same DN). The page is complete when both fields are non-empty and the DN
 * resolves to a non-null value from the EntryWidget.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewBookmarkMainWizardPage extends WizardPage implements WidgetModifyListener
{

    /** The entry. */
    private IEntry entry;

    /** The bookmark name text. */
    private Text bookmarkNameText;

    /** The bookmark entry widget. */
    private EntryWidget bookmarkEntryWidget;


    // ── Leia Loads the Pre-filled Beacon Details ──────────────────────────────────
    // Name and DN pre-filled from the selected entry. The page starts incomplete
    // to force user review before accepting the defaults.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewBookmarkMainWizardPage. Pre-populates both fields with the
     * selected entry's DN. Starts incomplete so the user actively confirms the
     * name and DN before clicking Finish.
     *
     * @param pageName  the wizard page name.
     * @param entry     the currently selected LDAP entry (provides the pre-filled DN).
     * @param wizard    the parent bookmark wizard.
     */
    public NewBookmarkMainWizardPage( String pageName, IEntry entry, NewBookmarkWizard wizard )
    {
        super( pageName );
        setTitle( Messages.getString( "NewBookmarkMainWizardPage.NewBookmark" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewBookmarkMainWizardPage.EnterNewBookmark" ) ); //$NON-NLS-1$
        // setImageDescriptor(BrowserUIPlugin.getDefault().getImageDescriptor(BrowserUIConstants.IMG_ATTRIBUTE_WIZARD));
        setPageComplete( false );

        this.entry = entry;
    }


    // ── Leia Disconnects the Beacon Listener ──────────────────────────────────────
    // We registered as a widget listener; we must clean up on dispose to avoid
    // listener leaks after the wizard closes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Removes the WidgetModifyListener from the EntryWidget to prevent listener
     * leaks after the wizard is disposed.
     */
    public void dispose()
    {
        super.dispose();
        bookmarkEntryWidget.removeWidgetModifyListener( this );
    }


    /**
     * Validates this page.
     */
    private void validate()
    {
        if ( bookmarkNameText != null && !bookmarkNameText.isDisposed() )
        {
            setPageComplete( bookmarkEntryWidget.getDn() != null && !"".equals( bookmarkNameText.getText() ) ); //$NON-NLS-1$
        }
    }


    // ── Leia Re-validates When the Page Becomes Visible ──────────────────────────
    // The page may become visible after navigating back from another page;
    // re-validate to ensure the Finish button state is correct.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Triggers validation when the page becomes visible so the Finish button
     * state is always accurate when the user returns to this page.
     *
     * @param visible  whether the page is becoming visible or hidden.
     */
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );
        if ( visible )
        {
            validate();
        }
    }


    // ── Leia Builds the Beacon Panel ──────────────────────────────────────────────
    // Two rows: a text field for the label (focused on open), and an EntryWidget
    // for the target DN (pre-loaded with the selected entry).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI: a single-column outer composite containing a three-column
     * inner composite with a "Bookmark Name" text field (pre-filled with the entry's
     * DN string, focused on open) and a "Bookmark DN" EntryWidget (pre-loaded with
     * the entry's connection and DN). Both modify listeners call validate().
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        Composite innerComposite = BaseWidgetUtils.createColumnContainer( composite, 3, 1 );

        BaseWidgetUtils.createLabel( innerComposite, Messages.getString( "NewBookmarkMainWizardPage.BookmarkName" ), 1 ); //$NON-NLS-1$
        bookmarkNameText = BaseWidgetUtils.createText( innerComposite, entry.getDn().getName(), 2 );
        bookmarkNameText.setFocus();
        bookmarkNameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        BaseWidgetUtils.createLabel( innerComposite, Messages.getString( "NewBookmarkMainWizardPage.BookmarkDN" ), 1 ); //$NON-NLS-1$
        bookmarkEntryWidget = new EntryWidget();
        bookmarkEntryWidget.addWidgetModifyListener( this );
        bookmarkEntryWidget.createWidget( innerComposite );
        bookmarkEntryWidget.setInput( entry.getBrowserConnection(), entry.getDn() );

        setControl( composite );
    }


    // ── Leia Re-validates on DN Change ───────────────────────────────────────────
    // Every time the user changes the target DN, we check the page is still complete.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Called by the EntryWidget whenever the user changes the target DN.
     * Re-validates so the Finish button enables only when both name and DN are valid.
     *
     * @param event  the widget-modify event (unused; we read state from the widget directly).
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        validate();
    }


    // ── Leia Reads the Beacon Coordinates ────────────────────────────────────────
    // The wizard needs the DN to construct the Bookmark object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the target DN selected in the EntryWidget.
     * Used by {@link NewBookmarkWizard#performFinish()} to construct the Bookmark.
     *
     * @return  the selected target Dn.
     */
    public Dn getBookmarkDn()
    {
        return bookmarkEntryWidget.getDn();
    }


    // ── Leia Reads the Beacon Label ───────────────────────────────────────────────
    // The wizard also needs the user's chosen name for the bookmark.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bookmark name typed by the user.
     * Used by {@link NewBookmarkWizard#performFinish()} to construct the Bookmark.
     *
     * @return  the bookmark name string.
     */
    public String getBookmarkName()
    {
        return bookmarkNameText.getText();
    }


    // ── Leia Saves the Last Target for Next Time ──────────────────────────────────
    // The EntryWidget remembers the last DN and connection for the next run.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the EntryWidget's dialog settings (last used DN and connection) so
     * the widget opens in the same state next time.
     */
    public void saveDialogSettings()
    {
        bookmarkEntryWidget.saveDialogSettings();
    }

}
