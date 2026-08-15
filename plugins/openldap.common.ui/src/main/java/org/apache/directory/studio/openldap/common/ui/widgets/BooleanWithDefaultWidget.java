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
package org.apache.directory.studio.openldap.common.ui.widgets;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: BooleanWithDefaultWidget — REBELS CHOOSING TO FIGHT OR YIELD ──────
// Picture a Rebel squad at a mission briefing: every member chooses to fight
// (TRUE), yield to the Empire (FALSE), or defer to Alliance command's standing
// order (DEFAULT). This widget presents that three-way choice as a combo box
// so the user can pick an explicit yes/no or let the server default decide. The
// inner BooleanValue enum maps the three choices to a type-safe constant set.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We provide a three-state combo widget for OpenLDAP boolean configuration
 * options that support an explicit {@code true}, an explicit {@code false}, or
 * a server default (represented as {@code null} in our value field). We extend
 * {@link AbstractWidget} so listeners can be notified of changes.
 */
public class BooleanWithDefaultWidget extends AbstractWidget
{
    /** The combo viewer's values */
    private Object[] comboViewerValues = new Object[]
        {
            BooleanValue.DEFAULT,
            BooleanValue.TRUE,
            BooleanValue.FALSE
    };

    // The default value
    private Boolean defaultValue;

    // The value
    private Boolean value;

    // UI widgets
    private ComboViewer comboViewer;


    // ── CONSTRUCTOR: BooleanWithDefaultWidget() — BLANK MISSION ORDERS ────────
    // We create a widget with no pre-set default. The combo will show "Default
    // value" without parenthesized hint text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link BooleanWithDefaultWidget} with no pre-configured
     * default value. The DEFAULT entry in the combo shows plain {@code "Default value"}.
     */
    public BooleanWithDefaultWidget()
    {
    }


    // ── CONSTRUCTOR: BooleanWithDefaultWidget(boolean) — STANDING ORDERS ──────
    // We create a widget pre-loaded with the server's standing order (the
    // default). When DEFAULT is selected the combo shows "Default value (true)"
    // or "Default value (false)" to help the user understand what omitting the
    // setting means.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link BooleanWithDefaultWidget} with the given
     * {@code boolean} default. The DEFAULT entry will show
     * {@code "Default value (true)"} or {@code "Default value (false)"}.
     *
     * @param defaultValue  the server default boolean value
     */
    public BooleanWithDefaultWidget( boolean defaultValue )
    {
        this.defaultValue = defaultValue;
    }


    // ── CONSTRUCTOR: BooleanWithDefaultWidget(Boolean) — BOXED ORDERS ─────────
    // We accept a {@code Boolean} object so callers can pass {@code null} to
    // indicate "no known default", resulting in plain "Default value" label text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link BooleanWithDefaultWidget} with the given
     * {@link Boolean} default. A {@code null} argument is equivalent to calling
     * the no-arg constructor.
     *
     * @param defaultValue  the server default value, or {@code null}
     */
    public BooleanWithDefaultWidget( Boolean defaultValue )
    {
        this.defaultValue = defaultValue;
    }


    // ── METHOD: create(Composite) — DEPLOYING THE SQUAD (NO TOOLKIT) ──────────
    // We delegate to the toolkit-aware overload with {@code null} toolkit so we
    // always go through a single code path.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the widget's SWT controls inside the given parent, without a
     * {@link FormToolkit}. Delegates to {@link #create(Composite, FormToolkit)}.
     *
     * @param parent  the parent {@link Composite}
     */
    public void create( Composite parent )
    {
        create( parent, null );
    }


    // ── METHOD: create(Composite, FormToolkit) — DEPLOYING THE SQUAD ──────────
    // We build the combo viewer, wire up its label provider (which prints the
    // human-readable choice text), and attach a selection listener that updates
    // our {@code value} field and fires our change listeners.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the widget's {@link ComboViewer} inside the given parent,
     * optionally adapting it with the supplied {@link FormToolkit} for use in
     * Eclipse Forms pages.
     *
     * @param parent   the parent {@link Composite}
     * @param toolkit  the form toolkit, or {@code null} for plain SWT
     */
    public void create( Composite parent, FormToolkit toolkit )
    {
        comboViewer = new ComboViewer( parent );
        comboViewer.setContentProvider( new ArrayContentProvider() );
        comboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof BooleanValue )
                {
                    BooleanValue booleanValue = ( BooleanValue ) element;

                    switch ( booleanValue )
                    {
                        case DEFAULT:
                            if ( defaultValue != null )
                            {
                                if ( defaultValue.booleanValue() )
                                {
                                    return NLS.bind( "Default value ({0})", "true" );
                                }
                                else
                                {
                                    return NLS.bind( "Default value ({0})", "false" );
                                }
                            }
                            else
                            {
                                return "Default value";
                            }
                        case TRUE:
                            return "True";
                        case FALSE:
                            return "False";
                    }
                }

                return super.getText( element );
            }
        } );
        comboViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                value = null;

                StructuredSelection selection = ( StructuredSelection ) comboViewer.getSelection();

                if ( !selection.isEmpty() )
                {
                    BooleanValue booleanValue = ( BooleanValue ) selection.getFirstElement();

                    switch ( booleanValue )
                    {
                        case DEFAULT:
                            value = null;
                            break;
                        case TRUE:
                            value = new Boolean( true );
                            break;
                        case FALSE:
                            value = new Boolean( false );
                            break;
                    }
                }

                notifyListeners();
            }
        } );
        comboViewer.setInput( comboViewerValues );
        comboViewer.setSelection( new StructuredSelection( comboViewerValues[0] ) );
    }


    // ── METHOD: getControl — HANDING OVER THE SQUAD'S RADIO ──────────────────
    // We return the underlying SWT control so the parent layout can position
    // it within the form or dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the primary SWT {@link Control} backing this widget (the combo
     * box), so the parent layout can size and position it.
     *
     * @return the combo's underlying {@link Control}
     */
    public Control getControl()
    {
        return comboViewer.getControl();
    }


    // ── METHOD: setValue — ISSUING NEW ORDERS TO THE SQUAD ───────────────────
    // We update both our internal field and the combo's selection. A {@code null}
    // value selects DEFAULT; {@code true} selects TRUE; {@code false} selects FALSE.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We programmatically set the widget's current value and synchronize the
     * combo selection. Pass {@code null} to select the DEFAULT entry.
     *
     * @param value  the new value, or {@code null} for DEFAULT
     */
    public void setValue( Boolean value )
    {
        this.value = value;

        if ( value != null )
        {
            if ( value.booleanValue() )
            {
                comboViewer.setSelection( new StructuredSelection( comboViewerValues[1] ) );
            }
            else
            {
                comboViewer.setSelection( new StructuredSelection( comboViewerValues[2] ) );
            }
        }
        else
        {
            comboViewer.setSelection( new StructuredSelection( comboViewerValues[0] ) );
        }
    }


    // ── METHOD: getValue — READING THE SQUAD'S CURRENT ORDERS ────────────────
    // We return the current value: {@code Boolean.TRUE} for fight, {@code
    // Boolean.FALSE} for yield, or {@code null} for "defer to command default".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the current widget value. Returns {@code Boolean.TRUE},
     * {@code Boolean.FALSE}, or {@code null} if DEFAULT is selected.
     *
     * @return the current value, or {@code null} for DEFAULT
     */
    public Boolean getValue()
    {
        return value;
    }


    // ── METHOD: dispose — STANDING DOWN THE SQUAD ─────────────────────────────
    // We release the SWT resources used by the combo control to avoid UI
    // memory leaks when the parent dialog or editor is closed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We dispose the underlying SWT combo control if it has not already been
     * disposed, freeing all associated UI resources.
     */
    public void dispose()
    {
        if ( ( comboViewer != null ) && ( comboViewer.getControl() != null )
            && ( !comboViewer.getControl().isDisposed() ) )
        {
            comboViewer.getControl().dispose();
        }
    }


    // ── METHOD: setEnabled — ACTIVATING OR STANDING DOWN THE CONTROL ──────────
    // We enable or disable the combo so the parent form can lock this field
    // when the surrounding context makes a manual choice inappropriate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We enable or disable the underlying combo control. When disabled, the user
     * cannot change the selection.
     *
     * @param enabled  {@code true} to enable the widget, {@code false} to disable it
     */
    public void setEnabled( boolean enabled )
    {
        if ( ( comboViewer != null ) && ( comboViewer.getControl() != null )
            && ( !comboViewer.getControl().isDisposed() ) )
        {
            comboViewer.getControl().setEnabled( enabled );
        }
    }

    // ── ENUM: BooleanValue — THE THREE STANDING ORDERS ───────────────────────
    // DEFAULT defers to the server configuration, TRUE is an explicit yes, and
    // FALSE is an explicit no. These are internal to the widget and never
    // exposed directly through the public API.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We define the three possible internal states for this widget: a server
     * default, an explicit true, and an explicit false.
     */
    enum BooleanValue
    {
        DEFAULT, TRUE, FALSE
    }
}
