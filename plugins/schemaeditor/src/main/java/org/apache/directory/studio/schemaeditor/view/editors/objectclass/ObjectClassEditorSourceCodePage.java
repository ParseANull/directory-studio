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

package org.apache.directory.studio.schemaeditor.view.editors.objectclass;


import java.text.ParseException;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.parsers.OpenLdapSchemaParser;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.io.OpenLdapSchemaFileExporter;
import org.apache.directory.studio.schemaeditor.view.widget.SchemaSourceViewer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;


// ── CLASS: ObjectClassEditorSourceCodePage — C-3PO READING THE JAWA DIALECT ──
// C-3PO crouches in the Jawa sandcrawler, decoding the rapid-fire Jawa
// dialect as each droid rolls off the conveyor — parsing the raw symbols
// and immediately flagging anything he can't understand as a translation error.
// This page is our C-3PO: it displays the object class as raw OpenLDAP schema
// syntax in a syntax-highlighted text widget, lets the user edit it directly,
// and re-parses on every keystroke — flagging parse failures so the user can't
// accidentally save garbled schema definitions.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Source Code page inside the {@link ObjectClassEditor}.
 * It presents the object class definition as raw OpenLDAP schema syntax text
 * that the user can view and edit directly. A {@link ModifyListener} re-parses
 * the text on every change; if parsing fails, the page blocks navigation away
 * from it until the syntax is fixed.
 * Think of it as C-3PO reading Jawa: any symbol he can't parse raises an immediate
 * flag, and we don't move on until it's resolved.
 */
public class ObjectClassEditorSourceCodePage extends AbstractObjectClassEditorPage
{
    /** The page ID */
    public static final String ID = ObjectClassEditor.ID + "sourceCodePage"; //$NON-NLS-1$

    /** The Schema Source Viewer */
    private SchemaSourceViewer schemaSourceViewer;

    /** The flag to indicate if the user can leave the Source Code page */
    private boolean canLeaveThePage = true;

    /** The listener of the Schema Source Viewer Widget */
    private ModifyListener schemaSourceViewerListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            canLeaveThePage = true;
            try
            {
                getEditor().setDirty( true );
                OpenLdapSchemaParser parser = new OpenLdapSchemaParser();
                parser.parse( schemaSourceViewer.getTextWidget().getText() );

                List<?> objectclasses = parser.getObjectClasses();
                if ( objectclasses.size() != 1 )
                {
                    // TODO Throw an exception and return
                }
                else
                {
                    updateObjectClass( ( ObjectClass ) objectclasses.get( 0 ) );
                }
            }
            catch ( ParseException exception )
            {
                canLeaveThePage = false;
            }
        }
    };


    // ── C-3PO Takes His Position at the Conveyor ──────────────────────────────
    // C-3PO positions himself at the Jawa conveyor belt, ready to decode whatever
    // dialect rolls toward him. This constructor registers the page with the
    // parent editor and gives it the "Source Code" tab label so the user knows
    // they're looking at raw schema text rather than the GUI form view.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Source Code page and registers it with the parent editor.
     * The page's tab label is loaded from the NLS message bundle under the key
     * {@code "ObjectClassEditorSourceCodePage.SourceCode"}.
     *
     * @param editor  the parent {@link ObjectClassEditor} that owns this page
     */
    public ObjectClassEditorSourceCodePage( ObjectClassEditor editor )
    {
        super( editor, ID, Messages.getString( "ObjectClassEditorSourceCodePage.SourceCode" ) ); //$NON-NLS-1$
    }


    // ── C-3PO Sets Up His Translation Station ─────────────────────────────────
    // C-3PO arranges his workspace in the sandcrawler: he lays out his
    // translation panels, sets up the text font so the characters are legible,
    // loads the current schema definition as a starting document, attaches his
    // listening ear, and registers the help context so the Rebels know where
    // to look for guidance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the source code editing UI: a full-page {@link SchemaSourceViewer}
     * with monospace font, pre-populated with the current object class definition
     * in OpenLDAP schema format, and a modify listener that re-parses on every change.
     *
     * @param managedForm  the Eclipse Forms managed form that hosts this page
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        super.createFormContent( managedForm );

        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        form.getBody().setLayout( layout );
        toolkit.paintBordersFor( form.getBody() );

        // SOURCE CODE Field
        schemaSourceViewer = new SchemaSourceViewer( form.getBody(), null, null, false, SWT.BORDER | SWT.H_SCROLL
            | SWT.V_SCROLL );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        gd.heightHint = 10;
        schemaSourceViewer.getTextWidget().setLayoutData( gd );

        // set text font
        Font font = JFaceResources.getFont( JFaceResources.TEXT_FONT );
        schemaSourceViewer.getTextWidget().setFont( font );

        IDocument document = new Document();
        schemaSourceViewer.setDocument( document );

        // Initialization from the "input" object class
        fillInUiFields();

        // Listeners initialization
        addListeners();

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( form,
            PluginConstants.PLUGIN_ID + "." + "object_class_editor" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── C-3PO Starts Listening to the Conveyor ────────────────────────────────
    // C-3PO activates his auditory sensors and begins monitoring the conveyor —
    // every symbol that changes triggers his translation circuit.
    // We attach our modify listener to the text widget so every keystroke
    // kicks off a re-parse of the schema source.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Attaches the modify listener to the source viewer text widget.
     * After this call, every edit in the text area triggers a re-parse
     * and updates the working object class (or sets the "cannot leave" flag
     * if the text contains a syntax error).
     */
    protected void addListeners()
    {
        if ( schemaSourceViewer != null )
        {
            schemaSourceViewer.getTextWidget().addModifyListener( schemaSourceViewerListener );
        }
    }


    // ── C-3PO Deactivates His Sensors ─────────────────────────────────────────
    // When C-3PO is done at the conveyor, he deactivates his sensors so he
    // stops reacting to changes — important to avoid processing stale events
    // after the page has been torn down.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Detaches the modify listener from the source viewer text widget.
     * We call this before refreshing the UI to prevent the listener from
     * treating our own programmatic text update as a user edit.
     */
    protected void removeListeners()
    {
        if ( schemaSourceViewer != null )
        {
            schemaSourceViewer.getTextWidget().removeModifyListener( schemaSourceViewerListener );
        }
    }


    // ── C-3PO Reads Back the Current Text ─────────────────────────────────────
    // C-3PO looks at his current translation panel and reads out the most
    // up-to-date schema text — converting the working object class back to
    // OpenLDAP source format and loading it into the text widget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Refreshes the source code text widget from the current working object class.
     * We export the modified object class to OpenLDAP schema format using
     * {@link OpenLdapSchemaFileExporter#toSourceCode} and push the result into
     * the document. Listeners are temporarily removed to avoid a spurious re-parse loop.
     */
    protected void fillInUiFields()
    {
        schemaSourceViewer.getDocument().set( OpenLdapSchemaFileExporter.toSourceCode( getModifiedObjectClass() ) );
    }


    // ── Can C-3PO Leave the Station? ──────────────────────────────────────────
    // If C-3PO is mid-translation and the text is still garbled, he should not
    // leave — the Jawas won't know what they're dealing with. We return false
    // if the last parse attempt failed, blocking the editor from switching
    // away to the Overview page or saving.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Reports whether the user is allowed to navigate away from this page.
     * Returns {@code false} if the source text currently contains a parse error —
     * the parent editor uses this to block tab switches and save operations
     * until the syntax is corrected.
     *
     * @return  {@code true} if the current source text parses cleanly; {@code false} otherwise
     */
    public boolean canLeaveThePage()
    {
        return canLeaveThePage;
    }


    // ── C-3PO Updates the Shared Translation ──────────────────────────────────
    // Once C-3PO has successfully parsed a new set of Jawa symbols, he
    // propagates the result to the shared mission briefing — updating every
    // field of the working object class so the Overview page stays in sync.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Applies all fields from a freshly parsed {@link ObjectClass} literal to the
     * working copy held by the parent editor.
     * After the user types valid schema syntax, we parse it into a temporary
     * {@code ObjectClass} object and then copy each field over to the persistent
     * working copy — keeping the two pages synchronized.
     *
     * @param ocl  the parsed {@link ObjectClass} literal; all its fields replace those
     *             in the current working copy
     */
    private void updateObjectClass( ObjectClass ocl )
    {
        ObjectClass modifiedObjectClass = getModifiedObjectClass();

        modifiedObjectClass.setDescription( ocl.getDescription() );
        modifiedObjectClass.setMayAttributeTypeOids( ocl.getMayAttributeTypeOids() );
        modifiedObjectClass.setMustAttributeTypeOids( ocl.getMustAttributeTypeOids() );
        modifiedObjectClass.setNames( ocl.getNames() );
        modifiedObjectClass.setObsolete( ocl.isObsolete() );
        modifiedObjectClass.setOid( ocl.getOid() );
        modifiedObjectClass.setSuperiorOids( ocl.getSuperiorOids() );
        modifiedObjectClass.setType( ocl.getType() );
    }
}
