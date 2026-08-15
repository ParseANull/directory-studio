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
package org.apache.directory.studio.schemaeditor.view.wizards;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: NewSchemaWizardPage — LUKE CONCENTRATING, FEELING THE FORCE ────────
// On Dagobah, Luke closes his eyes, breathes slowly, and reaches inward — he
// has to name the feeling before he can act on it.  Yoda says: "Name what you
// sense."  Until Luke speaks the name, nothing can happen.
// This page is exactly that moment: there's one text field, and the only thing
// the user has to do is type a name for their new schema.  We validate as they
// type — is it empty?  Already taken? — and only let them proceed when the name
// is clean.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The single wizard page for {@link NewSchemaWizard} — collects the name for
 * the new LDAP schema.  A schema name must be non-empty and unique within the
 * current project (duplicate names would confuse the schema handler).
 * Think of this page as Luke's concentration exercise on Dagobah: the only task
 * is to sense and speak one clear name, and Yoda won't let him proceed until
 * it's right.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewSchemaWizardPage extends AbstractWizardPage
{
    /** The ProjectsHandler */
    private SchemaHandler schemaHandler;

    // UI Fields
    private Text nameText;


    // ── Luke Settles Into The Training Pose ──────────────────────────────────
    // Luke sits cross-legged in Yoda's hut, closes his eyes, and prepares
    // himself for the exercise.  Yoda sets the scene: title, description, image —
    // the stage is dressed before the session starts.
    // We set up the page title, description, and image here, and grab the
    // SchemaHandler so we can check for duplicate names when the user types.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of this schema-naming wizard page.
     * Sets the page's title, description, and image, and looks up the
     * {@link SchemaHandler} for duplicate-name validation.
     *
     * <p>For example — Luke settles in and Yoda announces the exercise:</p>
     * <pre>
     *   setTitle( "Create a Schema" );
     *   setDescription( "Please specify a name for the new schema." );
     *   schemaHandler = Activator.getDefault().getSchemaHandler();
     * </pre>
     */
    protected NewSchemaWizardPage()
    {
        super( "NewSchemaWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewSchemaWizardPage.CreateSchema" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewSchemaWizardPage.PleaseSpecifiyName" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMA_NEW_WIZARD ) );
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── Yoda Places The Training Stone In Front Of Luke ──────────────────────
    // Yoda sets a smooth stone on the floor between them — the single object of
    // Luke's focus.  There's a label on it ("Schema Name:") and a space where
    // Luke must write his answer.
    // We build that same setup: a label and a text field, wired up with a
    // ModifyListener so validation fires on every keystroke.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widgets for this page: a label and a text field for the
     * schema name, with a listener that fires {@link #dialogChanged()} on every
     * keystroke.
     * We call {@link #initFields()} after building so the page starts in a
     * known state (empty, incomplete, possibly disabled if no project is open).
     *
     * <p>For example — Yoda presents the naming stone to Luke:</p>
     * <pre>
     *   nameLabel = "Schema Name:"
     *   nameText  = [_________________]  ← Luke must write here
     *   nameText.addModifyListener( e -> dialogChanged() );
     * </pre>
     *
     * @param parent  the SWT container Eclipse provides for our widgets
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );

        // Name
        Label nameLabel = new Label( composite, SWT.NONE );
        nameLabel.setText( Messages.getString( "NewSchemaWizardPage.SchemaName" ) ); //$NON-NLS-1$
        nameText = new Text( composite, SWT.BORDER );
        nameText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        nameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );

        initFields();

        setControl( composite );
    }


    // ── Yoda Checks If Luke Is Even In The Right Room ────────────────────────
    // Before the exercise can begin, Yoda checks that there's actually a project
    // open — you can't train in Force-naming without a universe to name things
    // in.  If there's no open project, the text field stays grey and an error
    // message explains why.
    // We check the SchemaHandler: if it's null there's no open project, so we
    // disable the field and show the error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the initial UI state after the widgets are built.
     * If no schema project is currently open (SchemaHandler is null), we
     * disable the name field and show an error — there's nowhere to put the
     * new schema.  Otherwise we just clear errors and mark the page incomplete
     * so the user knows they still need to type something.
     *
     * <p>For example — Yoda checks the room before the exercise starts:</p>
     * <pre>
     *   if ( schemaHandler == null ) {
     *       nameText.setEnabled( false );
     *       displayErrorMessage( "No schema project is open." );
     *   } else {
     *       displayErrorMessage( null );
     *       setPageComplete( false );
     *   }
     * </pre>
     */
    private void initFields()
    {
        if ( Activator.getDefault().getSchemaHandler() == null )
        {
            nameText.setEnabled( false );
            displayErrorMessage( Messages.getString( "NewSchemaWizardPage.ErrorNoSchemaProjectOpen" ) ); //$NON-NLS-1$
        }
        else
        {
            displayErrorMessage( null );
            setPageComplete( false );
        }
    }


    // ── Yoda Listens As Luke Speaks The Name ─────────────────────────────────
    // Luke concentrates, opens his mouth, and says a name.  Yoda listens
    // carefully: "Empty, the name must not be.  Taken already, it must not be."
    // If Luke says nothing, or repeats a name that already exists, Yoda corrects
    // him — otherwise the Force flows freely.
    // We do the same validation on every keystroke.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current name field value and updates the error banner and
     * page-complete status.  Called on every keystroke via the ModifyListener.
     * We reject empty names and names that are already in use by another schema
     * in this project.
     *
     * <p>For example — Yoda corrects Luke's naming attempts:</p>
     * <pre>
     *   if ( name.isEmpty() )                     error( "No name — speak!" );
     *   if ( schemaHandler.nameTaken( name ) )    error( "That name exists!" );
     *   else                                       clearError();  // "Good. Feel it."
     * </pre>
     */
    private void dialogChanged()
    {
        // Name
        if ( nameText.getText().equals( "" ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "NewSchemaWizardPage.ErrorNoNameSpecified" ) ); //$NON-NLS-1$
            return;
        }
        else if ( schemaHandler.isSchemaNameAlreadyTaken( nameText.getText() ) )
        {
            displayErrorMessage( Messages.getString( "NewSchemaWizardPage.ErrorSchemaNameExists" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── Luke Speaks The Name Clearly ─────────────────────────────────────────
    // Yoda nods and says "Speak the name to me."  Luke opens his eyes and says
    // it plainly — the schema name, exactly as he typed it.
    // We return whatever is in the text field; the wizard's performFinish() uses
    // it to create the Schema object.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema name the user typed into the name field.
     * The wizard's {@code performFinish()} passes this directly to the
     * {@link Schema} constructor.
     *
     * <p>For example — Luke speaks the name aloud to Yoda:</p>
     * <pre>
     *   String name = page.getSchemaName();
     *   Schema schema = new Schema( name );
     *   // "Good. The Force is strong with this schema name."
     * </pre>
     *
     * @return the schema name string; validation ensures it is non-empty and unique
     *         by the time the wizard calls this
     */
    public String getSchemaName()
    {
        return nameText.getText();
    }
}
