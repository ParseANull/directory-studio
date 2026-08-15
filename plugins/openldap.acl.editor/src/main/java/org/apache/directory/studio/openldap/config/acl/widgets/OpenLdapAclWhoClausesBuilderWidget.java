/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.widgets;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClause;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseStar;


// ── CLASS: OpenLdapAclWhoClausesBuilderWidget — LANDO MANAGING CLOUD CITY ────
// Lando Calrissian runs Cloud City's service roster: he can add a new worker
// row, delete one, move a row up or down, and ensure there is always at least
// one row in the queue. This widget manages the list of WHO clause rows in the
// visual ACL editor — exactly that roster. Each row is an OpenLdapAclWhoClauseWidget.
// A separator label is placed between consecutive rows. refreshWhoClauseWidgets()
// disposes the old rows and creates new ones from the current model, disabling
// move-up on the first row and move-down on the last. The builder widget also
// exposes addNewClause(), deleteClause(), moveUpClause(), and moveDownClause()
// so that individual row widgets can call back to modify the list.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages the list of {@link OpenLdapAclWhoClauseWidget} rows inside the "Acces by
 * Who" group of the visual ACL editor. Provides add, delete, move-up, and move-down
 * operations, and ensures there is always at least one default {@link AclWhoClauseStar}
 * row.
 *
 * <p>Think of this class as Lando Calrissian running Cloud City's personnel roster —
 * adding, removing, and reordering workers as directives change.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclWhoClausesBuilderWidget
{
    /** The ACL context */
    private OpenLdapAclValueWithContext context;

    /** The visual editor composite */
    protected OpenLdapAclVisualEditorComposite visualEditorComposite;

    /** The list of clause widgets */
    private List<OpenLdapAclWhoClauseWidget> clauseWidgets = new ArrayList<OpenLdapAclWhoClauseWidget>();

    /** The list of separators */
    private List<Label> separatorWidgets = new ArrayList<Label>();

    // UI widgets
    private Group whoGroup;


    // ── Listener: Clause Row Modified ─────────────────────────────────────────
    // When a row widget fires a modify event (e.g. the user changes the clause
    // type or access level), Lando replaces the corresponding entry in the model.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A listener for the WhoClause widget. Replaces the clause at the widget's
     * index in the model and refreshes the visual editor layout.
     */
    private WidgetModifyListener whoClauseModifyListener = new WidgetModifyListener()
    {
        public void widgetModified( WidgetModifyEvent event )
        {
            // Getting the source widget
            OpenLdapAclWhoClauseWidget widget = ( OpenLdapAclWhoClauseWidget ) event.getSource();
            List<AclWhoClause> whoClauses = context.getAclItem().getWhoClauses();

            // Updating the clause
            whoClauses.remove( widget.getIndex() );
            whoClauses.add( widget.getIndex(), widget.getClause() );

            // Adjusting the layout of the visual editor composite
            visualEditorComposite.layout( true, true );
        }
    };


    // ── Constructing the Builder Widget ───────────────────────────────────────
    // Lando opens Cloud City's command centre, stores the visual editor
    // composite reference (needed for layout refresh), and stores the context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new builder widget. Stores the visual editor composite and the
     * ACL context for use by all operations.
     *
     * @param visualEditorComposite  The parent visual editor composite for layout refresh.
     * @param context                The shared ACL context.
     */
    public OpenLdapAclWhoClausesBuilderWidget( OpenLdapAclVisualEditorComposite visualEditorComposite, OpenLdapAclValueWithContext context )
    {
        this.visualEditorComposite = visualEditorComposite;
        this.context = context;
    }


    // ── Creating the Who Group Container ─────────────────────────────────────
    // Lando sets up the group box that will contain the clause rows. The rows
    // themselves are created later by createClauseWidgets().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Acces by Who" {@link Group} and adds it to the given parent
     * composite. The group container fills horizontally and is the parent for all
     * clause row widgets.
     *
     * @param parent  The composite in which the group will be created.
     */
    public void create( Composite parent )
    {
        // Creating the who group
        whoGroup = BaseWidgetUtils.createGroup( parent, "Acces by \"Who\"", 1 );
        whoGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Disposing All Row Widgets ─────────────────────────────────────────────
    // Lando clears the roster: disposes every row widget and every separator
    // label, removing them from their respective lists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all existing clause row widgets and separator labels, removing them
     * from the internal lists. Called before recreating the rows from the model.
     */
    private void disposeClausesWidgets()
    {
        // Disposing and removing clause widgets
        for ( OpenLdapAclWhoClauseWidget clauseWidget : clauseWidgets.toArray( new OpenLdapAclWhoClauseWidget[0] ) )
        {
            clauseWidget.dispose();
            clauseWidgets.remove( clauseWidget );
        }

        // Disposing and removing separators
        for ( Label separator : separatorWidgets.toArray( new Label[0] ) )
        {
            separator.dispose();
            separatorWidgets.remove( separator );
        }
    }


    // ── Creating Row Widgets From the Model ───────────────────────────────────
    // Lando walks the who-clause list and creates one row widget per clause,
    // placing a horizontal separator between rows. If the list is empty he adds
    // a default "*" clause so there is always at least one row. After creating
    // all rows he updates the enabled state of Move Up/Down and Delete buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates one {@link OpenLdapAclWhoClauseWidget} per who-clause in the model.
     * If the model has no clauses a default {@link AclWhoClauseStar} is added first.
     * Separator labels are placed between rows. Button enabled states are adjusted
     * for the first row (Move Up disabled) and last row (Move Down disabled), and
     * Delete is disabled when there is only one row.
     */
    private void createClauseWidgets()
    {
        // Checking the clauses
        List<AclWhoClause> whoClauses = context.getAclItem().getWhoClauses();

        if ( whoClauses.size() == 0 )
        {
            // Adding at least one default clause
            AclWhoClauseStar whoClause = new AclWhoClauseStar();

            whoClauses.add( whoClause );
        }

        // Creating a widget for each clause
        boolean isFirst = true;
        int pos = 0;

        for ( AclWhoClause whoClause : whoClauses )
        {
            // Creating a separator (except for the first row)
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                Label separator = new Label( whoGroup, SWT.SEPARATOR | SWT.HORIZONTAL );
                separator.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
                separatorWidgets.add( separator );
            }

            // Creating the clause widget
            OpenLdapAclWhoClauseWidget clauseWidget = new OpenLdapAclWhoClauseWidget( this, context, whoClause, pos );
            clauseWidget.create( whoGroup );

            clauseWidget.addWidgetModifyListener( whoClauseModifyListener );
            clauseWidgets.add( clauseWidget );
            pos++;
        }

        // Updating button states for specific rows (first, last and the case where there's only one row)
        if ( clauseWidgets.size() == 1 )
        {
            // There's only one row
            OpenLdapAclWhoClauseWidget widget = clauseWidgets.get( 0 );
            widget.getDeleteButton().setEnabled( false );
            widget.getMoveUpButton().setEnabled( false );
            widget.getMoveDownButton().setEnabled( false );
        }
        else
        {
            // There are more than 1 row
            OpenLdapAclWhoClauseWidget firstWidget = clauseWidgets.get( 0 );
            firstWidget.getMoveUpButton().setEnabled( false );
            OpenLdapAclWhoClauseWidget lastWidget = clauseWidgets.get( clauseWidgets.size() - 1 );
            lastWidget.getMoveDownButton().setEnabled( false );
        }
    }


    // ── Refreshing All Row Widgets From the Model ─────────────────────────────
    // Lando tears down the old roster and builds a fresh one from the model,
    // then triggers a layout refresh on the visual editor composite.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the existing clause rows and recreates them from the current model.
     * Triggers a layout refresh on the visual editor composite.
     */
    private void refreshWhoClauseWidgets()
    {
        // Disposing previous widgets and creating new ones
        disposeClausesWidgets();
        createClauseWidgets();

        // Adjusting the layout of the visual editor composite
        visualEditorComposite.layout( true, true );
    }


    // ── Adding a New Clause Row ────────────────────────────────────────────────
    // A row widget calls this when the user presses the Add button. Lando appends
    // a new default "*" clause to the model and refreshes the rows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by a {@link OpenLdapAclWhoClauseWidget} when the user presses Add.
     * Appends a new default {@link AclWhoClauseStar} to the model and refreshes
     * all clause rows.
     *
     * @param widget  The row widget that initiated the add.
     */
    protected void addNewClause( OpenLdapAclWhoClauseWidget widget )
    {
        // Adding a new clause underneath the selected widget
        AclWhoClauseStar whoClause = new AclWhoClauseStar();
        context.getAclItem().getWhoClauses().add( whoClause );

        // Refreshing clauses widgets
        refreshWhoClauseWidgets();
    }


    // ── Deleting a Clause Row ─────────────────────────────────────────────────
    // A row widget calls this when the user presses Delete. Lando removes the
    // clause at the widget's index and refreshes the rows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by a {@link OpenLdapAclWhoClauseWidget} when the user presses Delete.
     * Removes the clause at the widget's index from the model and refreshes all rows.
     *
     * @param widget  The row widget that initiated the delete.
     */
    protected void deleteClause( OpenLdapAclWhoClauseWidget widget )
    {
        int deletedIndex = widget.getIndex();

        // Deleting the selected widget
        context.getAclItem().getWhoClauses().remove( deletedIndex );

        // Refreshing clauses widgets
        refreshWhoClauseWidgets();
    }


    // ── Moving a Clause Row Up ────────────────────────────────────────────────
    // A row widget calls this when the user presses Move Up. Lando swaps the
    // clause with its predecessor and refreshes the rows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by a {@link OpenLdapAclWhoClauseWidget} when the user presses Move Up.
     * Swaps the clause at the widget's index with the one above it and refreshes
     * all rows.
     *
     * @param widget  The row widget that initiated the move-up.
     */
    protected void moveUpClause( OpenLdapAclWhoClauseWidget widget )
    {
        // Swapping clauses
        int index = widget.getIndex();
        swapClauseIndexes( index, index - 1 );

        // Refreshing clauses widgets
        refreshWhoClauseWidgets();
    }


    // ── Moving a Clause Row Down ───────────────────────────────────────────────
    // A row widget calls this when the user presses Move Down. Lando swaps the
    // clause with its successor and refreshes the rows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by a {@link OpenLdapAclWhoClauseWidget} when the user presses Move Down.
     * Swaps the clause at the widget's index with the one below it and refreshes
     * all rows.
     *
     * @param widget  The row widget that initiated the move-down.
     */
    protected void moveDownClause( OpenLdapAclWhoClauseWidget widget )
    {
        // Swapping clauses
        int index = widget.getIndex();
        swapClauseIndexes( index, index + 1 );

        // Refreshing clauses widgets
        refreshWhoClauseWidgets();
    }


    // ── Swapping Two Clauses in the Model ─────────────────────────────────────
    // Lando exchanges the two roster entries at the given positions in the
    // model's who-clause list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Swaps the who-clauses at the two given indexes in the model.
     *
     * @param sourceIndex       The index of the clause to move.
     * @param destinationIndex  The index it should swap with.
     */
    private void swapClauseIndexes( int sourceIndex, int destinationIndex )
    {
        // Getting clauses
        List<AclWhoClause> whoClauses = context.getAclItem().getWhoClauses();
        AclWhoClause sourceClause = whoClauses.get( sourceIndex );
        AclWhoClause destinationClause = whoClauses.get( destinationIndex );

        // Swapping clauses
        whoClauses.remove( sourceIndex );
        whoClauses.add( sourceIndex, destinationClause );

        whoClauses.remove( destinationIndex );
        whoClauses.add( destinationIndex, sourceClause );
    }


    // ── Refreshing All Row Widgets (Public Entry Point) ───────────────────────
    // Called by the visual editor when the tab switches to Visual. Lando
    // rebuilds the full roster from the current model.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Public entry point: disposes existing clause rows and recreates them from
     * the current model. Called by {@link OpenLdapAclVisualEditorComposite#refresh()}.
     */
    public void refresh()
    {
        refreshWhoClauseWidgets();
    }


    // ── Disposing the Group and All Row Widgets ───────────────────────────────
    // Lando shuts down Cloud City — disposes the who group and all rows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the who group composite and all clause row widgets. Should be called
     * when the visual editor composite is itself disposed.
     */
    public void dispose()
    {
        // Disposing the who group
        if ( ( whoGroup != null ) && ( !whoGroup.isDisposed() ) )
        {
            whoGroup.dispose();
        }

        // Disposing the clause widgets
        disposeClausesWidgets();
    }
}
