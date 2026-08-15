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
package org.apache.directory.studio.schemaeditor.view.views;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.ProblemsViewController;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaChecker;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProblemsViewRoot;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: ProblemsView — Mace Windu Confronting Palpatine ───────────────────
// In Revenge of the Sith, Mace Windu walks into the Chancellor's office with
// three other Jedi Masters and lays out what they know: the corruption, the
// deception, the danger. He's not guessing — he's surfacing concrete evidence
// and making it impossible to ignore. The confrontation is structured, blunt,
// and visible to everyone present.
// ProblemsView is that confrontation: it surfaces all schema validation errors
// and warnings in a structured two-column tree (Description + Resource), with
// a running tally at the top so you can see at a glance how bad things are.
// You can't pretend the problems aren't there once this view is open.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Problems View — an Eclipse ViewPart that surfaces schema validation errors and
 * warnings produced by the {@link SchemaChecker}. The view shows a two-column tree
 * (Description | Resource) grouped by error type and warning type, with a header label
 * counting how many of each there are. Think of it as Mace Windu's confrontation: calm,
 * structured, and impossible to dismiss. The schema can't hide its flaws here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProblemsView extends ViewPart
{
    /** The ID of the View */
    public static final String ID = PluginConstants.VIEW_PROBLEMS_VIEW_ID;

    /** The viewer */
    private TreeViewer treeViewer;

    /** The content provider of the viewer */
    private ProblemsViewContentProvider contentProvider;

    /** The overview label */
    private Label overviewLabel;

    /** The SchemaChecker */
    private SchemaChecker schemaChecker;

    /** The Controller */
    private ProblemsViewController controller;


    // ── Mace Windu Calls the Council to Session ──────────────────────────────
    // Mace doesn't just burst in — he sets the stage. He positions his team,
    // establishes the gravity of the moment, lays out the structure of the
    // confrontation. Here, createPartControl does the same: it builds the header
    // label (the tally), a visual separator, then the tree, and finally installs
    // the controller that will update everything when schema events occur.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Problems View UI — called by Eclipse when this view is first opened.
     * We lay out a zero-margin GridLayout so the tree fills the entire view, add an
     * overview label (the "X errors, Y warnings" summary at the top), a separator line,
     * then the tree viewer itself, and finally wire up the {@link ProblemsViewController}.
     *
     * <p>For example — Mace opens the session:</p>
     * <pre>
     *   createPartControl(parent)
     *     → setHelp("problems_view")
     *     → overviewLabel.setText("0 errors, 0 warnings")
     *     → initViewer(parent)
     *     → new ProblemsViewController(this)
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides for us to draw into
     */
    @Override
    public void createPartControl( Composite parent )
    {
        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent, PluginConstants.PLUGIN_ID + "." + "problems_view" ); //$NON-NLS-1$ //$NON-NLS-2$

        GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 0;
        parent.setLayout( gridLayout );

        // Overview Label
        overviewLabel = new Label( parent, SWT.NULL );
        setErrorsAndWarningsCount( 0, 0 );
        overviewLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Separator Label
        Label separatorLabel = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        separatorLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Viewer
        initViewer( parent );

        // Adding the controller
        controller = new ProblemsViewController( this );
    }


    // ── Mace Sets Up the Evidence Board ─────────────────────────────────────
    // Before the confrontation happens, you need the evidence organized — not
    // scattered across the room, but laid out in columns that make sense:
    // what went wrong, and which schema object caused it.
    // initViewer sets up exactly that: two named columns ("Description" and
    // "Resource"), a tree with visible headers and gridlines, and both the
    // content and label providers that know how to populate and render each row.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures the {@link TreeViewer} that displays validation results.
     * We set up two columns: "Description" (the human-readable error/warning message)
     * and "Resource" (the schema object that caused the problem). Full-selection mode
     * means clicking anywhere on a row selects the whole row, not just a cell.
     *
     * <p>For example — the evidence board is ready:</p>
     * <pre>
     *   Description column (500px wide) | Resource column (100px wide)
     *   "Attribute type has no syntax"  | "myCustomAT"
     *   "OID already registered"        | "anotherAT"
     * </pre>
     *
     * @param parent  the SWT composite to embed the tree into
     */
    private void initViewer( Composite parent )
    {
        treeViewer = new TreeViewer( parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL );
        Tree tree = treeViewer.getTree();
        tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        tree.setHeaderVisible( true );
        tree.setLinesVisible( true );
        TreeColumn descriptionColumn = new TreeColumn( tree, SWT.LEFT );
        descriptionColumn.setText( Messages.getString( "ProblemsView.Description" ) ); //$NON-NLS-1$
        descriptionColumn.setWidth( 500 );
        TreeColumn resourceColumn = new TreeColumn( tree, SWT.LEFT );
        resourceColumn.setText( Messages.getString( "ProblemsView.Resource" ) ); //$NON-NLS-1$
        resourceColumn.setWidth( 100 );
        contentProvider = new ProblemsViewContentProvider();
        treeViewer.setContentProvider( contentProvider );
        treeViewer.setLabelProvider( new ProblemsViewLabelProvider() );
    }


    // ── Mace Keeps His Eyes on the Chancellor ───────────────────────────────
    // In that office, nobody's attention wanders. Mace keeps his gaze on
    // Palpatine — focused, immovable. In UI terms, "setFocus" means keyboard
    // events go to our widget. We route focus to the tree so users can
    // immediately navigate results with the keyboard.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Gives keyboard focus to the problems tree.
     * Eclipse calls this when the user switches to our view. Routing focus to
     * the tree means arrow keys and Enter work immediately without an extra click.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        treeViewer.getTree().setFocus();
    }


    // ── Mace Hands the Evidence File to the Council ──────────────────────────
    // The other Jedi Masters need direct access to what Mace has assembled.
    // He doesn't summarize — he hands over the actual dossier. Similarly,
    // our controller needs the raw TreeViewer to register listeners and
    // trigger refreshes, so we hand it over directly.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link TreeViewer} for this view.
     * The {@link ProblemsViewController} uses this to register listeners, drive refreshes,
     * and respond to user selections in the tree.
     *
     * @return  the {@link TreeViewer} displaying errors and warnings
     */
    public TreeViewer getViewer()
    {
        return treeViewer;
    }


    // ── Mace Updates the Tally of Evidence ──────────────────────────────────
    // As the confrontation unfolds, new evidence surfaces: more counts, more
    // charges. The tally on the evidence board updates in real time.
    // This method rebuilds the summary label whenever the SchemaChecker runs
    // a new check and finds a different count of errors and warnings.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the tree with fresh schema checker results and updates the summary label.
     * We create a new {@link ProblemsViewRoot} (which triggers the content provider
     * to re-query the {@link SchemaChecker}), expand all rows so nothing is hidden,
     * and then update the "X errors, Y warnings" header label.
     *
     * <p>For example — new evidence surfaces:</p>
     * <pre>
     *   schemaChecker reports: 3 errors, 1 warning
     *   reloadViewer() → treeViewer.setInput(new ProblemsViewRoot())
     *                  → overviewLabel.setText("3 errors, 1 warning")
     * </pre>
     */
    public void reloadViewer()
    {
        treeViewer.setInput( new ProblemsViewRoot() );
        treeViewer.expandAll();

        schemaChecker = Activator.getDefault().getSchemaChecker();
        if ( schemaChecker != null )
        {
            setErrorsAndWarningsCount( schemaChecker.getErrors().size(), schemaChecker.getWarnings().size() );
        }
        else
        {
            setErrorsAndWarningsCount( 0, 0 );
        }
    }


    // ── Mace Reads the Final Charge Count Aloud ─────────────────────────────
    // "You are under arrest, Lord Sidious." The count matters — one accusation
    // is serious; dozens is catastrophic. Mace states it clearly.
    // This method builds a grammatically correct "X error(s), Y warning(s)"
    // string and puts it in the overview label at the top of the view.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Updates the overview label at the top of the view with a human-readable summary.
     * We handle singular vs. plural correctly — "1 error" not "1 errors" — because
     * that matters to users who are counting. Called every time {@link #reloadViewer()}
     * runs.
     *
     * <p>For example — reading the count:</p>
     * <pre>
     *   setErrorsAndWarningsCount(1, 3) → "1 error, 3 warnings"
     *   setErrorsAndWarningsCount(2, 1) → "2 errors, 1 warning"
     *   setErrorsAndWarningsCount(0, 0) → "0 errors, 0 warnings"
     * </pre>
     *
     * @param errors    the number of schema errors currently found
     * @param warnings  the number of schema warnings currently found
     */
    public void setErrorsAndWarningsCount( int errors, int warnings )
    {
        StringBuffer sb = new StringBuffer();

        sb.append( errors );
        sb.append( " " ); //$NON-NLS-1$
        if ( errors > 1 )
        {
            sb.append( Messages.getString( "ProblemsView.Errors" ) ); //$NON-NLS-1$
        }
        else
        {
            sb.append( Messages.getString( "ProblemsView.Error" ) ); //$NON-NLS-1$
        }

        sb.append( ", " ); //$NON-NLS-1$

        sb.append( warnings );
        sb.append( " " ); //$NON-NLS-1$
        if ( warnings > 1 )
        {
            sb.append( Messages.getString( "ProblemsView.Warnings" ) ); //$NON-NLS-1$
        }
        else
        {
            sb.append( Messages.getString( "ProblemsView.Warning" ) ); //$NON-NLS-1$
        }

        overviewLabel.setText( sb.toString() );
    }


    // ── The Confrontation Ends — Stand Down ──────────────────────────────────
    // The scene ends. The Jedi Masters take their leave; the room is dismissed.
    // In Eclipse terms, when the view is closed, we tell the controller to
    // clean up its listeners so we don't leak event subscriptions.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this view is being closed or the workbench is shutting down.
     * We delegate to the controller's {@code dispose()} so it can unregister any event
     * listeners it registered against the schema checker or schema handler. Forgetting
     * this would cause listener leaks that trigger callbacks on a dead view.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
        controller.dispose();
    }
}
