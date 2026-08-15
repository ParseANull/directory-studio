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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


// -- CLASS: EntryEditorWidgetQuickFilterWidget -- R2-D2 QUERIES THE DEATH STAR SYSTEMS --
// In the Death Star's detention block, R2-D2 rolls up to the central terminal and plugs in.
// He brings up two search fields on the screen -- attribute type and value -- and a "Clear"
// button so the Rebels can reset the query if they need to.  As Han types "Leia" into the
// value box, R2 instantly narrows the results to matching cells.
// This class IS that terminal panel: two text boxes plus a Clear button that live above the
// entry editor table and drive {@link EntryEditorWidgetFilter} in real time.
// ---------------------------------------------------------------------------------
/**
 * The quick-filter bar that sits above the entry editor table and lets users narrow down
 * visible rows by typing an attribute name fragment and/or a value fragment.
 * The bar can be shown or hidden (it's hidden by default and toggled by an action).
 * When shown, it looks like this:
 * <pre>
 * +----------------------------------------------------------------+
 * | [(attribute)] [(Value)                             ] (X Clear) |
 * +----------------------------------------------------------------+
 * </pre>
 * Think of this class as R2-D2's Death Star search interface: plug in, type a query,
 * get instant matching results.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetQuickFilterWidget
{
    /** The filter to propagate the entered filter phrases. */
    private EntryEditorWidgetFilter filter;

    /** The entry editor widget. */
    private EntryEditorWidget entryEditorWidget;

    /** The parent, used to create the composite. */
    private Composite parent;

    /** The outer composite. */
    private Composite composite;

    /** The inner composite, it is created/destroyed when showing/hiding the quick filter. */
    private Composite innerComposite;

    /** The quick filter attribute text. */
    private Text quickFilterAttributeText;

    /** The quick filter value text. */
    private Text quickFilterValueText;

    /** The clear quick filter button. */
    private Button clearQuickFilterButton;

    /**
     * The Listener that reacts on any text entered into the quick Attribute filter text widget
     */
    private ModifyListener quickFilterAttributeTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            filter.setQuickFilterAttribute( quickFilterAttributeText.getText() );
            clearQuickFilterButton.setEnabled( !Strings.isEmpty( quickFilterAttributeText.getText() ) //$NON-NLS-1$
                || !Strings.isEmpty( quickFilterValueText.getText() ) ); //$NON-NLS-1$
        }
    };


    /**
     * The Listener that reacts on any text entered into the quick Value filter text widget
     */
    private ModifyListener quickFilterValueTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            filter.setQuickFilterValue( quickFilterValueText.getText() );
            clearQuickFilterButton.setEnabled( !Strings.isEmpty( quickFilterAttributeText.getText() ) //$NON-NLS-1$
                || !Strings.isEmpty( quickFilterValueText.getText() ) ); //$NON-NLS-1$
        }
    };


    /**
     * The listener associated with teh Clear button. It will reset the attribute and value Texts
     */
    public SelectionAdapter clearQuickFilterButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            quickFilterAttributeText.setText( "" ); //$NON-NLS-1$
            quickFilterValueText.setText( "" ); //$NON-NLS-1$
        }
    };


    // -- R2 INITIALIZES HIS SEARCH PARAMETERS ----------------------------------
    // R2-D2 powers up and stores references to two things: the filter object he'll
    // feed search strings into as the user types, and the parent entry editor widget
    // so he knows where to return focus when the panel is closed.
    // ---------------------------------------------------------------------------------
    /**
     * Creates a new quick-filter widget wired to the given filter and entry editor.
     * You still need to call {@link #createComposite(Composite)} to actually build the SWT controls.
     *
     * <p>For example -- R2 initializes before approaching the terminal:</p>
     * <pre>
     *   R2: "Filter reference: loaded. Entry editor widget: loaded.
     *        Approaching Death Star terminal... interface ready."
     * </pre>
     *
     * @param filter             the filter object that will receive attribute/value search strings
     * @param entryEditorWidget  the parent entry editor -- used to return focus to the table on close
     */
    public EntryEditorWidgetQuickFilterWidget( EntryEditorWidgetFilter filter, EntryEditorWidget entryEditorWidget )
    {
        this.filter = filter;
        this.entryEditorWidget = entryEditorWidget;
    }


    // -- R2 LOCATES THE TERMINAL PANEL IN THE WALL -----------------------------
    // R2 rolls to the panel location and creates the outer frame -- a zero-height
    // placeholder composite. The search fields themselves don't exist yet; they're
    // created on demand when the user activates the quick filter via setActive(true).
    // The zero-height trick means the panel takes up no visible space when hidden.
    // ---------------------------------------------------------------------------------
    /**
     * Builds the invisible outer container that will eventually hold the quick-filter bar.
     * The outer composite starts with zero width and height, so it takes up no space.
     * The actual input fields are only created when {@link #setActive(boolean)} is called
     * with {@code true}. Call this during the entry editor's layout setup.
     * <pre>
     * +----------------------------------------------------------+
     * |                                                          |
     * +----------------------------------------------------------+
     * </pre>
     *
     * <p>For example -- R2 claims his spot at the wall terminal:</p>
     * <pre>
     *   R2 rolls into position. The panel slot is there but blank.
     *   "BWOOP." (Terminal frame registered. Awaiting activation.)
     * </pre>
     *
     * @param parent  the SWT composite to build the outer frame inside
     */
    public void createComposite( Composite parent )
    {
        this.parent = parent;

        composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
        GridLayout gl = new GridLayout();
        gl.marginHeight = 2;
        gl.marginWidth = 2;
        composite.setLayout( gl );
        // Setting the default width and height of the composite to 0
        GridData compositeGridData = new GridData( SWT.NONE, SWT.NONE, false, false );
        compositeGridData.heightHint = 0;
        compositeGridData.widthHint = 0;
        composite.setLayoutData( compositeGridData );

        innerComposite = null;
    }


    // -- R2 PULLS UP THE FULL SEARCH INTERFACE ---------------------------------
    // R2 activates the terminal: the screen lights up showing two text boxes (attribute
    // filter, value filter) and the Clear button. He repositions the layout so the
    // panel now takes up visible real estate, and focuses the attribute field so the
    // user can start typing immediately.
    // ---------------------------------------------------------------------------------
    /**
     * Creates the inner composite with the three search controls: attribute text field,
     * value text field, and Clear button. Resizes the outer composite from zero to full-width
     * so the bar becomes visible, then triggers a layout pass to push the table down.
     * <pre>
     * [          ] [                                     ] (X)
     * </pre>
     *
     * <p>For example -- R2 activates the search screen:</p>
     * <pre>
     *   Panel lights up. Two text boxes and a Clear button appear.
     *   R2: "BWEEP." (Ready for query input.)
     * </pre>
     */
    private void createFilterView()
    {
        // Reseting the layout of the composite to be displayed correctly
        GridData compositeGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        composite.setLayoutData( compositeGridData );

        innerComposite = BaseWidgetUtils.createColumnContainer( composite, 3, 1 );

        // The QuickFilterAttribute Text
        quickFilterAttributeText = new Text( innerComposite, SWT.BORDER );
        quickFilterAttributeText.setLayoutData( new GridData( 200 - 14, SWT.DEFAULT ) );

        quickFilterAttributeText.addModifyListener( quickFilterAttributeTextListener );

        // The QuickFilterValue Text
        quickFilterValueText = new Text( innerComposite, SWT.BORDER );
        quickFilterValueText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        quickFilterValueText.addModifyListener( quickFilterValueTextListener );

        // The QuickFilter Button
        clearQuickFilterButton = new Button( innerComposite, SWT.PUSH );
        clearQuickFilterButton.setToolTipText( Messages
            .getString( "EntryEditorWidgetQuickFilterWidget.ClearQuickFilter" ) ); //$NON-NLS-1$
        clearQuickFilterButton.setImage( BrowserCommonActivator.getDefault()
            .getImage( BrowserCommonConstants.IMG_CLEAR ) );
        clearQuickFilterButton.setEnabled( false );
        clearQuickFilterButton.addSelectionListener( clearQuickFilterButtonListener );

        setEnabled( composite.isEnabled() );

        composite.layout( true, true );
        parent.layout( true, true );
    }


    // -- R2 RETRACTS THE SEARCH INTERFACE --------------------------------------
    // The user has closed the quick-filter bar. R2 clears both text fields (which
    // fires the modify listeners and resets the filter to show everything), disposes
    // the inner composite, then collapses the outer frame back to zero size so no
    // space is wasted. Focus returns to the main entry table.
    // ---------------------------------------------------------------------------------
    /**
     * Tears down the inner composite and collapses the outer frame back to zero height.
     * Clearing the text fields before disposal fires the modify listeners, which resets
     * the filter to show all attributes again. After this, the quick-filter bar is invisible.
     *
     * <p>For example -- R2 retracts the search panel:</p>
     * <pre>
     *   R2 clears the text fields: filter is reset.
     *   He collapses the screen back into the wall.
     *   "BWOOP." (Terminal stowed. Back to standby.)
     * </pre>
     */
    private void destroy()
    {
        // Reseting the layout of the composite with a width and height set to 0
        GridData compositeGridData = new GridData( SWT.NONE, SWT.NONE, false, false );
        compositeGridData.heightHint = 0;
        compositeGridData.widthHint = 0;
        composite.setLayoutData( compositeGridData );
        quickFilterAttributeText.setText( "" ); //$NON-NLS-1$
        quickFilterValueText.setText( "" ); //$NON-NLS-1$
        innerComposite.dispose();
        innerComposite = null;

        composite.layout( true, true );
        parent.layout( true, true );
    }


    // -- R2 FULLY DISCONNECTS FROM THE TERMINAL --------------------------------
    // The mission is over and R2 unplugs everything: text fields, buttons, composites,
    // and finally the filter reference itself. After this, the object is inert.
    // ---------------------------------------------------------------------------------
    /**
     * Fully disposes this widget and releases all SWT resources.
     * Safe to call even if the widget was never fully shown (the inner composite may be null).
     * After disposal, all fields are null and the object must not be used again.
     *
     * <p>For example -- R2 fully disconnects from the Death Star:</p>
     * <pre>
     *   R2 retracts his probe. All panel references are cleared.
     *   "BEEEEP." (All systems offline. Ready for transport to the Falcon.)
     * </pre>
     */
    public void dispose()
    {
        if ( filter != null )
        {
            quickFilterAttributeText = null;
            quickFilterValueText = null;
            clearQuickFilterButton = null;
            innerComposite = null;
            composite.dispose();
            composite = null;
            parent = null;
            filter = null;
        }
    }


    // -- R2 TOGGLES THE TERMINAL ACTIVE STATE ----------------------------------
    // Han tells R2: "disable the terminal -- we don't want to accidentally query
    // while no entry is selected."  Or: "enable it -- we have a live entry."
    // We propagate the enabled state down through both composites and all three
    // child controls so they visually grey out or come back to life together.
    // ---------------------------------------------------------------------------------
    /**
     * Enables or disables all controls in the quick-filter bar.
     * When disabled (e.g., no LDAP entry is selected), the text fields and button
     * grey out so the user can't interact with them.
     *
     * <p>For example -- Han tells R2 to go standby:</p>
     * <pre>
     *   Han: "No entry selected, R2. Disable the search panel."
     *   R2 greys out all three controls. They can't be clicked.
     *   Han selects an entry: "OK R2, re-enable." Controls come back.
     * </pre>
     *
     * @param enabled  {@code true} to enable all controls, {@code false} to grey them out
     */
    public void setEnabled( boolean enabled )
    {
        if ( ( composite != null ) && !composite.isDisposed() )
        {
            composite.setEnabled( enabled );
        }

        if ( ( innerComposite != null ) && !innerComposite.isDisposed() )
        {
            innerComposite.setEnabled( enabled );
            quickFilterAttributeText.setEnabled( enabled );
            quickFilterValueText.setEnabled( enabled );
            clearQuickFilterButton.setEnabled( enabled );
        }
    }


    // -- R2 SHOWS OR HIDES THE SEARCH PANEL ------------------------------------
    // The user has pressed the "Quick Filter" toolbar button. If the panel isn't
    // showing, R2 calls createFilterView() to build it and focuses the attribute
    // text field. If the panel IS showing, R2 calls destroy() to collapse it and
    // returns focus to the entry table so keyboard navigation keeps working.
    // ---------------------------------------------------------------------------------
    /**
     * Activates or deactivates the quick-filter bar.
     * Activating builds the inner composite and focuses the attribute text field.
     * Deactivating clears search criteria (resetting the filter to show everything),
     * tears down the inner composite, and returns focus to the entry editor table.
     *
     * <p>For example -- R2 toggles the search panel on the user's command:</p>
     * <pre>
     *   User presses "Quick Filter" button (visible=true):
     *     R2 opens the panel and places the cursor in the attribute field.
     *   User presses it again (visible=false):
     *     R2 clears the fields, collapses the panel, returns focus to table.
     * </pre>
     *
     * @param visible  {@code true} to show the quick-filter bar, {@code false} to hide it
     */
    public void setActive( boolean visible )
    {
        if ( visible && ( innerComposite == null ) && ( composite != null ) )
        {
            createFilterView();
            quickFilterAttributeText.setFocus();
        }
        else if ( !visible && ( innerComposite != null ) && ( composite != null ) )
        {
            destroy();
            entryEditorWidget.getViewer().getTree().setFocus();
        }
    }
}
