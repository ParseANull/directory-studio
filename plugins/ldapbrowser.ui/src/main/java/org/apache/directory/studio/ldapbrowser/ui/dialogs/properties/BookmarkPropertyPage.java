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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.properties;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: BookmarkPropertyPage — LUKE'S BINARY SUNSET ON TATOOINE ────────────
// Luke watches the twin suns dip below the Tatooine horizon — both orbs fully
// visible, the whole picture laid out before him: name of the planet, location
// in the galaxy, and exactly where it sits in his journey.
// A bookmark in Directory Studio is the same kind of orientation marker: a named
// pointer to a specific DN in the directory tree.  This property page shows you
// the full picture — the bookmark's name and the DN it points to — and lets you
// edit both fields if needed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse property page for viewing and editing a directory bookmark.
 * A bookmark is just a named shortcut to a specific DN; this page shows both
 * the name and the target DN and lets the user change them, subject to basic
 * validation (name non-empty, DN resolvable, name unique).
 * Think of this page as Luke's binary sunset — the complete, clear picture of
 * where a bookmark points before you commit to using it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BookmarkPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

    /** The bookmark. */
    private IBookmark bookmark;

    /** The bookmark name text. */
    private Text bookmarkNameText;

    /** The bookmark entry widget. */
    private EntryWidget bookmarkEntryWidget;


    // ── LUKE STEPS INTO HIS VIEWING SPOT ─────────────────────────────────────
    // Luke doesn't need Apply or Defaults on this particular hillside — he's
    // going to see the sunset, take it in, and decide.  We suppress the default
    // and apply buttons because changes here are saved via performOk only.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the property page and hides the Default and Apply buttons.
     * The page uses a single OK action (via {@link #performOk()} called by the
     * dialog framework) rather than an Apply button, because bookmark edits are
     * low-risk and don't need incremental saves.
     *
     * <p>For example — Luke settles in without distractions:</p>
     * <pre>
     *   no "Apply" button → no "Restore Defaults" → just view/edit and confirm
     * </pre>
     */
    public BookmarkPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── LUKE SEES THE HORIZON — BOTH SUNS, FULL PICTURE ───────────────────────
    // Luke gazes at the full sunset: both suns clearly visible, horizon stretching
    // from left to right.  We build the equivalent view: the bookmark's name
    // at the top, its target DN below it, both editable, both wired to validate
    // so the OK button only enables when both fields are valid.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the property page UI: a name text field and an entry-widget DN picker.
     * If the selection element adapts to {@link IBookmark} we pre-populate both
     * fields from the existing bookmark; otherwise we leave them blank.
     * Modify listeners on both fields call {@link #validate()} immediately so the
     * page's validity tracks user input in real time.
     *
     * <p>For example — Luke sees the full bookmark picture:</p>
     * <pre>
     *   Bookmark Name: "My Admin Entry"
     *   Bookmark DN:   cn=admin,dc=example,dc=com
     *   → both correct → OK enabled
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's property dialog.
     * @return        The inner composite we built.
     */
    protected Control createContents( Composite parent )
    {

        if ( getElement() instanceof IAdaptable )
        {
            bookmark = ( IBookmark ) ( ( IAdaptable ) getElement() ).getAdapter( IBookmark.class );
            super
                .setMessage( Messages.getString( "BookmarkPropertyPage.Bookmark" ) + Utils.shorten( bookmark.getName(), 30 ) ); //$NON-NLS-1$
        }
        else
        {
            bookmark = null;
        }

        Composite innerComposite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        BaseWidgetUtils.createLabel( innerComposite, Messages.getString( "BookmarkPropertyPage.BookmarkName" ), 1 ); //$NON-NLS-1$
        bookmarkNameText = BaseWidgetUtils.createText( innerComposite, bookmark != null ? bookmark.getName() : "", 2 ); //$NON-NLS-1$
        bookmarkNameText.setFocus();
        bookmarkNameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        BaseWidgetUtils.createLabel( innerComposite, Messages.getString( "BookmarkPropertyPage.BookmarkDN" ), 1 ); //$NON-NLS-1$
        bookmarkEntryWidget = new EntryWidget();
        bookmarkEntryWidget.createWidget( innerComposite );
        if ( bookmark != null )
        {
            bookmarkEntryWidget.setInput( bookmark.getBrowserConnection(), bookmark.getDn() );
        }
        bookmarkEntryWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        return innerComposite;
    }


    // ── LUKE DECIDES TO STAY OR LEAVE ─────────────────────────────────────────
    // If the picture is complete and makes sense, Luke can commit; if it's broken
    // — a sun missing, the horizon wrong — he hesitates.
    // performOk is where we actually persist the changes back to the bookmark
    // object, and only if the bookmark is non-null (i.e. we had a valid selection).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves the edited name and DN back to the underlying {@link IBookmark} object
     * and persists the entry widget's dialog settings for future use.
     * Does nothing if {@code bookmark} is null (can happen if the selection element
     * didn't adapt to {@link IBookmark}).
     *
     * <p>For example — Luke commits to staying on Tatooine (for now):</p>
     * <pre>
     *   OK clicked → bookmark.setName("New Name") → bookmark.setDn(cn=admin,…) →
     *   bookmarkEntryWidget.saveDialogSettings() → changes persisted
     * </pre>
     *
     * @return  Always {@code true}.
     */
    public boolean performOk()
    {
        if ( bookmark != null )
        {
            bookmark.setName( bookmarkNameText.getText() );
            bookmark.setDn( bookmarkEntryWidget.getDn() );
            bookmarkEntryWidget.saveDialogSettings();
        }

        return true;
    }


    // ── LUKE CHECKS THE SUNSET IS ACTUALLY COMPLETE ───────────────────────────
    // If only one sun appears — or neither — the binary sunset isn't the full
    // picture, and Luke knows something is off.  We run three distinct validation
    // checks and surface a specific error message for each failure case.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the name and DN fields and enables/disables the page (and OK button)
     * accordingly.
     * Three specific checks in priority order: DN must be non-null, name must be
     * non-empty, and the name must be unique among existing bookmarks (unless we're
     * keeping the original name).
     * Called by modify listeners on both input fields.
     *
     * <p>For example — Luke checks both suns are visible:</p>
     * <pre>
     *   DN = null           → setValid(false), error "Please enter a DN"
     *   name = ""           → setValid(false), error "Please enter a bookmark name"
     *   name already exists → setValid(false), error "Bookmark already exists"
     *   all clear           → setValid(true), error cleared
     * </pre>
     */
    private void validate()
    {
        setValid( bookmarkEntryWidget.getDn() != null && !"".equals( bookmarkNameText.getText() ) ); //$NON-NLS-1$

        if ( bookmark != null )
        {
            if ( bookmarkEntryWidget.getDn() == null )
            {
                setValid( false );
                setErrorMessage( Messages.getString( "BookmarkPropertyPage.EnterDN" ) ); //$NON-NLS-1$
            }
            else if ( "".equals( bookmarkNameText.getText() ) ) //$NON-NLS-1$
            {
                setValid( false );
                setErrorMessage( Messages.getString( "BookmarkPropertyPage.EnterName" ) ); //$NON-NLS-1$
            }
            else if ( !bookmark.getName().equals( bookmarkNameText.getText() )
                && bookmark.getBrowserConnection().getBookmarkManager().getBookmark( bookmarkNameText.getText() ) != null )
            {
                setValid( false );
                setErrorMessage( Messages.getString( "BookmarkPropertyPage.ErrorBookmarkExists" ) ); //$NON-NLS-1$
            }
            else
            {
                setValid( true );
                setErrorMessage( null );
            }
        }
        else
        {
            setValid( false );
        }
    }

}
