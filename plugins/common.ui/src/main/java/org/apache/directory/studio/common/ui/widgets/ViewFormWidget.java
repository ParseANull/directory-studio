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

package org.apache.directory.studio.common.ui.widgets;


import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


// ── CLASS: ViewFormWidget — REBEL COMMAND CENTER BATTLE STATION DISPLAY ───────
// The Rebel command center's main battle station display has three zones at the
// top — a status text readout on the left, an action toolbar in the center, and
// a pull-down menu button on the right — and a large main content area below.
// This widget assembles exactly that layout using a SWT ViewForm, giving every
// view in Directory Studio a consistent header and context-menu infrastructure.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We provide a reusable composite widget that wraps a SWT {@link ViewForm} to
 * deliver a standard view layout: an info text label top-left, an action
 * toolbar top-center, a pull-down menu button top-right, and an abstract main
 * content area that subclasses fill in.  A context menu is automatically
 * attached to the content control.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class ViewFormWidget
{
    /** The view form control */
    protected ViewForm control;

    /** The info text, positioned at the top left */
    protected Text infoText;

    /** The action tool bar */
    protected ToolBar actionToolBar;

    /** The action tool bar manager */
    protected IToolBarManager actionToolBarManager;

    /** The menu tool bar. */
    protected ToolBar menuToolBar;

    /** The menu manager. */
    protected MenuManager menuManager;

    /** The context menu manager. */
    protected MenuManager contextMenuManager;


    // ── METHOD createWidget — POWERING UP THE BATTLE STATION DISPLAY ──────────
    // We switch on the battle station display: we wire up the ViewForm, attach
    // the status text readout, install the action toolbar, add the pull-down
    // menu button, call the subclass to fill in the main content area, and
    // hook up a context menu to whatever control the subclass hands back.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the complete ViewForm widget inside the given parent composite.
     * This includes the info text label, action toolbar, pull-down menu button,
     * the subclass-defined content area, and a context menu on the content.
     * Call this once during your view or editor setup.
     *
     * @param parent the parent composite to build the widget inside
     */
    public void createWidget( Composite parent )
    {
        control = new ViewForm( parent, SWT.NONE );
        // control.marginWidth = 0;
        // control.marginHeight = 0;
        // control.horizontalSpacing = 0;
        // control.verticalSpacing = 0;
        control.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // infoText = BaseWidgetUtils.createLabeledText(control, "", 1);
        Composite infoTextControl = BaseWidgetUtils.createColumnContainer( control, 1, 1 );
        infoTextControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        infoText = BaseWidgetUtils.createLabeledText( infoTextControl, "", 1 ); //$NON-NLS-1$
        infoText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, true ) );
        control.setTopLeft( infoTextControl );

        // tool bar
        actionToolBar = new ToolBar( control, SWT.FLAT | SWT.RIGHT );
        actionToolBar.setLayoutData( new GridData( SWT.END, SWT.NONE, true, false ) );
        actionToolBarManager = new ToolBarManager( actionToolBar );
        control.setTopCenter( actionToolBar );

        // local menu
        this.menuManager = new MenuManager();
        menuToolBar = new ToolBar( control, SWT.FLAT | SWT.RIGHT );
        ToolItem ti = new ToolItem( menuToolBar, SWT.PUSH, 0 );
        ti.setImage( CommonUIPlugin.getDefault().getImage( CommonUIConstants.IMG_PULLDOWN ) );
        ti.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                showViewMenu();
            }
        } );
        control.setTopRight( menuToolBar );

        // content
        Composite composite = BaseWidgetUtils.createColumnContainer( control, 1, 1 );
        GridLayout gl = new GridLayout();
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        composite.setLayout( gl );
        Control childControl = this.createContent( composite );
        control.setContent( composite );

        // context menu
        this.contextMenuManager = new MenuManager();
        Menu menu = this.contextMenuManager.createContextMenu( childControl );
        childControl.setMenu( menu );
    }


    // ── METHOD createContent — INSTALLING THE MAIN VIEWSCREEN ────────────────
    // Subclasses install whatever they need on the main viewscreen — a table,
    // a tree, a text editor.  We call this during createWidget so the content
    // is in place before we attach the context menu.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We call this template method to let subclasses build their specific
     * content area inside the given composite.  The returned control receives
     * the context menu that we attach automatically after this call.
     *
     * @param control the composite to add content controls into
     * @return the primary content control that should host the context menu
     */
    protected abstract Control createContent( Composite control );


    // ── METHOD showViewMenu — DEPLOYING THE PULL-DOWN MENU ───────────────────
    // The operator presses the pull-down button on the battle station console
    // and the local view menu drops down from the toolbar, positioned right
    // below the button so it feels natural.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We compute the screen position of the pull-down menu button and show
     * the local view menu at that location.  This is triggered when the user
     * clicks the pull-down toolbar item in the top-right corner.
     */
    private void showViewMenu()
    {
        Menu aMenu = menuManager.createContextMenu( control );
        Point topLeft = new Point( 0, 0 );
        topLeft.y += menuToolBar.getBounds().height;
        topLeft = menuToolBar.toDisplay( topLeft );
        aMenu.setLocation( topLeft.x, topLeft.y );
        aMenu.setVisible( true );
    }


    // ── METHOD dispose — POWERING DOWN THE BATTLE STATION ────────────────────
    // The Rebel base powers down: we systematically shut off every manager,
    // toolbar, and text widget in the reverse order they were created, nulling
    // each reference so nothing can accidentally be used after disposal.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We tear down this widget by disposing every manager and SWT control we
     * own.  After this call, all our field references are set to {@code null}
     * and the widget should no longer be used.
     */
    public void dispose()
    {
        if ( control != null )
        {

            if ( contextMenuManager != null )
            {
                contextMenuManager.removeAll();
                contextMenuManager.dispose();
                contextMenuManager = null;
            }
            if ( menuToolBar != null )
            {
                menuToolBar.dispose();
                menuToolBar = null;
                menuManager.dispose();
                menuManager = null;
            }
            if ( actionToolBar != null )
            {
                actionToolBar.dispose();
                actionToolBar = null;
                actionToolBarManager.removeAll();
                actionToolBarManager = null;
            }

            if ( infoText != null )
            {
                infoText.dispose();
                infoText = null;
            }

            control.dispose();
            control = null;
        }
    }


    // ── METHOD getInfoText — READING THE STATUS READOUT ───────────────────────
    // We hand over the status readout text field so callers can update the
    // status message displayed in the top-left corner of the battle station.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link Text} control that displays the info label in the
     * top-left of the ViewForm.  Callers can set its text to provide status
     * information relevant to the current view.
     *
     * @return the info text control
     */
    public Text getInfoText()
    {
        return infoText;
    }


    // ── METHOD getToolBarManager — ACCESSING THE ACTION PANEL ─────────────────
    // We hand over the action panel manager so callers can add, remove, or
    // update the action buttons shown in the top-center toolbar of the
    // battle station.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link IToolBarManager} for the action toolbar at the top
     * of the ViewForm.  Use this to contribute action buttons specific to your
     * view.
     *
     * @return the action toolbar manager
     */
    public IToolBarManager getToolBarManager()
    {
        return this.actionToolBarManager;
    }


    // ── METHOD getMenuManager — ACCESSING THE PULL-DOWN MENU PANEL ────────────
    // We hand over the pull-down menu manager so callers can add menu items
    // that appear when the user clicks the pull-down button in the top-right
    // corner of the battle station.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link IMenuManager} for the local pull-down view menu.
     * Add your view-specific actions here so they appear when the user clicks
     * the pull-down toolbar button.
     *
     * @return the view menu manager
     */
    public IMenuManager getMenuManager()
    {
        return menuManager;
    }


    // ── METHOD getContextMenuManager — ACCESSING THE RIGHT-CLICK PANEL ────────
    // We hand over the context menu manager so callers can contribute actions
    // that appear when the user right-clicks anywhere in the content area of
    // the battle station's main viewscreen.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link IMenuManager} for the context menu that is attached
     * to the main content control.  Contribute actions here to populate the
     * right-click menu for your view's content.
     *
     * @return the context menu manager
     */
    public IMenuManager getContextMenuManager()
    {
        return this.contextMenuManager;
    }


    // ── METHOD getControl — RETRIEVING THE BATTLE STATION HULL ───────────────
    // We return the outermost SWT control — the ViewForm hull — so layout
    // managers and parent composites can treat this entire widget as a single
    // unit.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the primary SWT {@link Control} — the underlying
     * {@link ViewForm} — so callers can lay it out inside a parent composite.
     *
     * @return the ViewForm control that is the root of this widget
     */
    public Control getControl()
    {
        return control;
    }
}
