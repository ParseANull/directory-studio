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
package org.apache.directory.studio.openldap.config.model.widgets;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.config.model.database.OlcBdbConfigLockDetectEnum;


// ── CLASS: LockDetectWidget — Mace Windu Choosing the Right Resolution ────────
// When Mace Windu faces a deadlock situation — conflicting lightsabers,
// Jedi versus Sith, no clear resolution — he chooses the right strategy:
// take out the oldest threat, the most aggressive, the one with fewest allies,
// pick at random, or let the system decide. There's no one-size-fits-all.
// The LockDetectWidget presents the same choice for BDB database lock detection:
// a dropdown that lets the administrator pick which transaction to abort when BDB
// detects a deadlock (oldest, youngest, fewest, random, or default).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT combo widget for selecting the BDB lock detection strategy (olcDbLockDetect).
 * Presents the OlcBdbConfigLockDetectEnum values as human-readable choices in a
 * dropdown — Oldest, Youngest, Fewest, Random, Default, or "(No value)".
 * Think of this as Mace Windu choosing which deadlocked transaction to abort.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LockDetectWidget extends AbstractWidget
{
    /** The combo viewer's values */
    private Object[] comboViewerValues = new Object[]
        {
            new NoneObject(),
            OlcBdbConfigLockDetectEnum.DEFAULT,
            OlcBdbConfigLockDetectEnum.RANDOM,
            OlcBdbConfigLockDetectEnum.OLDEST,
            OlcBdbConfigLockDetectEnum.YOUNGEST,
            OlcBdbConfigLockDetectEnum.FEWEST
    };

    /** The selected value */
    private OlcBdbConfigLockDetectEnum value;

    // UI widgets
    private ComboViewer comboViewer;


    // ── Default Constructor — Mace Windu Stands Ready ─────────────────────────────
    // Mace Windu is ready to evaluate the deadlock situation — no pre-selection yet.
    // We defer all actual UI creation to createWidget() so SWT can supply the parent.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new LockDetectWidget with no value pre-selected.
     * Call {@link #createWidget(Composite)} or {@link #createWidget(Composite, FormToolkit)}
     * to build the UI.
     *
     * <p>For example — Mace stands ready:</p>
     * <pre>
     *   LockDetectWidget widget = new LockDetectWidget();
     *   widget.createWidget( parent );
     * </pre>
     */
    public LockDetectWidget()
    {
    }


    // ── createWidget(Composite) — Mace Windu Takes His Position ──────────────────
    // Mace Windu takes his position (creates the combo) in the editor panel without
    // a FormToolkit — plain SWT mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the lock detect combo widget inside the given SWT composite.
     * Uses plain SWT (no FormToolkit) — suitable for dialogs and property pages.
     *
     * <p>For example — Mace takes his position in plain SWT mode:</p>
     * <pre>
     *   widget.createWidget( parent );
     * </pre>
     *
     * @param parent  the SWT Composite to add this widget's controls to
     */
    public void createWidget( Composite parent )
    {
        createWidget( parent, null );
    }


    // ── createWidget(Composite, FormToolkit) — Mace Takes Position in Form Mode ──
    // Mace takes his position inside a Forms-based editor panel, using the FormToolkit
    // for consistent visual styling with the Eclipse forms look-and-feel.
    // The combo viewer is populated with all lock detection modes and fires
    // notifyListeners() whenever the user changes the selection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the lock detect combo widget inside the given composite, using the
     * optional FormToolkit for Eclipse Forms styling. If toolkit is null, plain SWT is used.
     * The combo is populated with all OlcBdbConfigLockDetectEnum values plus "(No value)".
     *
     * <p>For example — Mace takes position in Forms mode:</p>
     * <pre>
     *   widget.createWidget( parent, toolkit );
     * </pre>
     *
     * @param parent   the SWT Composite to add controls to
     * @param toolkit  the FormToolkit for styling (may be null for plain SWT)
     */
    public void createWidget( Composite parent, FormToolkit toolkit )
    {
        // Combo
        comboViewer = new ComboViewer( parent );
        comboViewer.setContentProvider( new ArrayContentProvider() );
        comboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof NoneObject )
                {
                    return "(No value)";
                }
                else if ( element instanceof OlcBdbConfigLockDetectEnum )
                {
                    OlcBdbConfigLockDetectEnum lockDetect = ( OlcBdbConfigLockDetectEnum ) element;

                    switch ( lockDetect )
                    {
                        case OLDEST:
                            return "Oldest";
                        case YOUNGEST:
                            return "Youngest";
                        case FEWEST:
                            return "Fewest";
                        case RANDOM:
                            return "Random";
                        case DEFAULT:
                            return "Default";
                    }
                }

                return super.getText( element );
            }
        } );
        comboViewer.addSelectionChangedListener( event ->
            {
                value = null;

                StructuredSelection selection = ( StructuredSelection ) comboViewer.getSelection();

                if ( !selection.isEmpty() )
                {
                    Object selectedObject = selection.getFirstElement();

                    if ( selectedObject instanceof OlcBdbConfigLockDetectEnum )
                    {
                        value = ( OlcBdbConfigLockDetectEnum ) selectedObject;
                    }
                }

                notifyListeners();
            } );
        comboViewer.setInput( comboViewerValues );
        comboViewer.setSelection( new StructuredSelection( comboViewerValues[0] ) );
    }


    // ── setValue — Mace Windu Selects the Strategy ────────────────────────────────
    // Mace Windu selects the appropriate deadlock resolution strategy in the combo.
    // Null means "no value" — the combo snaps back to the "(No value)" placeholder.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the current selection in the lock detect combo.
     * If value is null, the combo resets to "(No value)".
     *
     * <p>For example — Mace Windu selects "Oldest":</p>
     * <pre>
     *   widget.setValue( OlcBdbConfigLockDetectEnum.OLDEST );
     * </pre>
     *
     * @param value  the OlcBdbConfigLockDetectEnum to display, or null for no selection
     */
    public void setValue( OlcBdbConfigLockDetectEnum value )
    {
        this.value = value;

        if ( value == null )
        {
            comboViewer.setSelection( new StructuredSelection( comboViewerValues[0] ) );
        }
        else
        {
            comboViewer.setSelection( new StructuredSelection( value ) );
        }
    }


    // ── getValue — Mace Windu Reports His Decision ────────────────────────────────
    // Mace Windu reports which strategy is currently selected — or null if no decision
    // has been made yet (the "(No value)" placeholder is showing).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected lock detection strategy, or null if "(No value)" is selected.
     *
     * <p>For example — Mace Windu reports his decision:</p>
     * <pre>
     *   OlcBdbConfigLockDetectEnum strategy = widget.getValue();
     *   // e.g., OLDEST, or null if no value is selected
     * </pre>
     *
     * @return  the selected OlcBdbConfigLockDetectEnum, or null
     */
    public OlcBdbConfigLockDetectEnum getValue()
    {
        return value;
    }


    // ── getControl — Return the Underlying SWT Control ────────────────────────────
    // We expose the underlying Combo control so layout managers can size and position it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the primary SWT control (the Combo) for this widget.
     * Used by layout code to position and size the control within the editor.
     *
     * <p>For example — get the raw SWT control:</p>
     * <pre>
     *   Control combo = widget.getControl();
     *   combo.setLayoutData( gridData );
     * </pre>
     *
     * @return  the underlying Combo Control
     */
    public Control getControl()
    {
        return comboViewer.getControl();
    }

    class NoneObject
    {
    }
}
