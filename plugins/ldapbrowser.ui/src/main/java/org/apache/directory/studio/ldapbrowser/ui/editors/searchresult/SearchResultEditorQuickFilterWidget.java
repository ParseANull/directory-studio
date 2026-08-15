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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


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


// ── CLASS: SearchResultEditorQuickFilterWidget — R2 Accessing a Hidden Panel ──
// When R2-D2 needs to interface with the Death Star's computer, he pops open a
// hidden access panel that wasn't visible before.  The panel has an input port
// (text field) and a reset button.  When R2's done, the panel retracts and
// vanishes back into the wall, zero footprint.
// This widget is that panel: when activated, it slides open a text box and a
// clear button; when deactivated, it collapses to zero height and disposes
// the inner controls entirely.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The quick-filter bar for the search result editor.
 * When active, it shows a text field and a clear button.  Text typed into the
 * field is forwarded to the {@link SearchResultEditorFilter}, which immediately
 * re-filters the table.  The clear button resets the text to empty.
 * When deactivated, the inner composite is destroyed and the outer composite
 * collapses to zero height so it takes up no space in the layout.
 * Think of R2's access panel: slides open when needed, fully gone when not.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorQuickFilterWidget
{

    /** The filter. */
    private SearchResultEditorFilter filter;

    /** The parent. */
    private Composite parent;

    /** The composite. */
    private Composite composite;

    /** The inner composite. */
    private Composite innerComposite;

    /** The quick filter value text. */
    private Text quickFilterValueText;

    /** The clear quick filter button. */
    private Button clearQuickFilterButton;


    // ── R2 Gets His Access Panel ──────────────────────────────────────────────
    // R2 is handed a reference to the filter he'll push keystrokes into.
    // No panel is created yet — that happens in createComposite().
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new quick-filter widget connected to the given filter.
     * The SWT composites are not created until {@link #createComposite(Composite)} is called.
     *
     * @param filter the filter to update as the user types
     */
    public SearchResultEditorQuickFilterWidget( SearchResultEditorFilter filter )
    {
        this.filter = filter;
    }


    // ── R2 Mounts the Access Panel to the Wall ────────────────────────────────
    // R2 installs the outer composite into the parent layout.  Initially it's
    // zero-height (panel is closed) and the inner composite doesn't exist yet.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the outer (always-present) composite inside the given parent.
     * The outer composite starts with zero height so it's invisible.  The inner
     * composite (with the actual controls) is created only when {@link #setActive(boolean)}
     * is called with {@code true}.
     *
     * @param parent the parent composite to embed this widget in
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


    // ── R2 Opens the Access Panel ─────────────────────────────────────────────
    // R2 expands the outer composite to full width, creates the inner composite,
    // wires up the text field to push its value to the filter, and adds the
    // clear button.  The panel is now live.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the inner composite with the text field and clear button.
     * Expands the outer composite to fill the available horizontal space.
     * Called by {@link #setActive(boolean)} when activating.
     */
    private void create()
    {
        // Reseting the layout of the composite to be displayed correctly
        GridData compositeGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        composite.setLayoutData( compositeGridData );

        innerComposite = BaseWidgetUtils.createColumnContainer( this.composite, 2, 1 );

        quickFilterValueText = new Text( innerComposite, SWT.BORDER );
        quickFilterValueText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        quickFilterValueText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                filter.setQuickFilterValue( quickFilterValueText.getText() );
                clearQuickFilterButton.setEnabled( !"".equals( quickFilterValueText.getText() ) ); //$NON-NLS-1$
            }
        } );

        clearQuickFilterButton = new Button( innerComposite, SWT.PUSH );
        clearQuickFilterButton.setToolTipText( Messages
            .getString( "SearchResultEditorQuickFilterWidget.ClearQuickFilterToolTip" ) ); //$NON-NLS-1$
        clearQuickFilterButton.setImage( BrowserCommonActivator.getDefault()
            .getImage( BrowserCommonConstants.IMG_CLEAR ) );
        clearQuickFilterButton.setEnabled( false );
        clearQuickFilterButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( !"".equals( quickFilterValueText.getText() ) ) //$NON-NLS-1$
                    quickFilterValueText.setText( "" ); //$NON-NLS-1$
            }
        } );

        setEnabled( composite.isEnabled() );

        composite.layout( true, true );
        parent.layout( true, true );
    }


    // ── R2 Retracts the Access Panel ─────────────────────────────────────────
    // R2 clears the text (so the filter resets), disposes the inner composite,
    // and collapses the outer composite back to zero height.  Gone without a trace.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Destroys the inner composite (text field + clear button) and collapses
     * the outer composite back to zero height.
     * Called by {@link #setActive(boolean)} when deactivating.
     */
    private void destroy()
    {
        // Reseting the layout of the composite with a width and height set to 0
        GridData compositeGridData = new GridData( SWT.NONE, SWT.NONE, false, false );
        compositeGridData.heightHint = 0;
        compositeGridData.widthHint = 0;
        composite.setLayoutData( compositeGridData );

        if ( !"".equals( quickFilterValueText.getText() ) ) //$NON-NLS-1$
        {
            quickFilterValueText.setText( "" ); //$NON-NLS-1$
        }
        innerComposite.dispose();
        innerComposite = null;

        composite.layout( true, true );
        parent.layout( true, true );
    }


    // ── R2 Disconnects Completely ─────────────────────────────────────────────
    // The editor is closing — R2 fully disconnects from the Death Star terminal:
    // the inner controls, outer composite, and filter reference are all released.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all SWT resources held by this widget.
     * Safe to call if the widget was never fully activated.
     */
    public void dispose()
    {
        if ( innerComposite != null && !innerComposite.isDisposed() )
        {
            quickFilterValueText.dispose();
            quickFilterValueText = null;
            clearQuickFilterButton = null;
            innerComposite = null;
        }
        if ( filter != null )
        {
            composite.dispose();
            composite = null;
            parent = null;
            filter = null;
        }
    }


    // ── R2 Powers the Panel On or Off ─────────────────────────────────────────
    // R2 enables or disables the input controls without hiding them — useful when
    // the editor loses focus or is in a read-only mode.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the text field and clear button without hiding the widget.
     * Used when the editor transitions to a read-only or inactive state.
     *
     * @param enabled {@code true} to enable editing; {@code false} to grey it out
     */
    public void setEnabled( boolean enabled )
    {
        if ( composite != null && !composite.isDisposed() )
        {
            composite.setEnabled( enabled );
        }
        if ( innerComposite != null && !innerComposite.isDisposed() )
        {
            innerComposite.setEnabled( enabled );
            quickFilterValueText.setEnabled( enabled );
            clearQuickFilterButton.setEnabled( enabled );
        }
    }


    // ── R2 Slides the Panel Open or Shut ──────────────────────────────────────
    // The action calls this when the user toggles the "Show Quick Filter" button.
    // Active → panel slides open (create()), focus goes to the text field.
    // Inactive → panel retracts (destroy()), filter clears automatically.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Activates or deactivates the quick-filter widget.
     * When activating, we call {@link #create()} and move keyboard focus to the
     * text field.  When deactivating, we call {@link #destroy()} which also
     * clears the filter text (so the table shows all rows again).
     *
     * @param visible {@code true} to show the widget; {@code false} to hide it
     */
    public void setActive( boolean visible )
    {
        if ( visible && innerComposite == null && composite != null )
        {
            create();
            this.quickFilterValueText.setFocus();
        }
        else if ( !visible && innerComposite != null && composite != null )
        {
            destroy();
        }
    }

}
