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

package org.apache.directory.studio.schemaeditor.view.editors.schema;


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerAdapter;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.io.OpenLdapSchemaFileExporter;
import org.apache.directory.studio.schemaeditor.view.widget.SchemaSourceViewer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;


// ── CLASS: SchemaEditorSourceCodePage — OBI-WAN'S DEATH STAR HOLOGRAM ─────────
// Aboard the Millennium Falcon, R2-D2 projects Obi-Wan's holographic recording
// of the Death Star's technical schematics — the raw engineering blueprints of
// the entire station, rendered in read-only blue light. Obi-Wan can only study
// them; he cannot reach in and edit the hologram.
// This page is that hologram: it displays the entire schema's raw OpenLDAP
// source code in a read-only syntax-highlighted text viewer, automatically
// regenerated whenever any schema element changes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Source Code page inside the {@link SchemaEditor}.
 * It displays the entire schema as raw OpenLDAP schema syntax in a read-only
 * {@link SchemaSourceViewer}. Unlike the object class source code page, this page
 * is view-only — the user cannot edit schema source here directly.
 * The page registers a {@link SchemaHandlerListener} so it regenerates the source
 * text whenever any attribute type, object class, syntax, or matching rule in the
 * schema changes.
 * Think of it as Obi-Wan's Death Star hologram: the full technical blueprints
 * rendered for study, but you can only look — not touch.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditorSourceCodePage extends FormPage
{
    /** The page ID */
    public static final String ID = SchemaEditor.ID + "sourceCode"; //$NON-NLS-1$

    /** The flag to indicate if the page has been initialized */
    private boolean initialized = false;

    /** The associated schema */
    private Schema schema;

    // UI Field
    private SchemaSourceViewer schemaSourceViewer;

    // Listerner
    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerAdapter()
    {
        /**
         * {@inheritDoc}
         */
        public void attributeTypeAdded( AttributeType at )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void attributeTypeModified( AttributeType at )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void attributeTypeRemoved( AttributeType at )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void matchingRuleAdded( MatchingRule mr )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void matchingRuleModified( MatchingRule mr )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void matchingRuleRemoved( MatchingRule mr )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassAdded( ObjectClass oc )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassModified( ObjectClass oc )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassRemoved( ObjectClass oc )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void syntaxAdded( LdapSyntax syntax )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void syntaxModified( LdapSyntax syntax )
        {
            refreshUI();
        }


        /**
         * {@inheritDoc}
         */
        public void syntaxRemoved( LdapSyntax syntax )
        {
            refreshUI();
        }
    };


    // ── R2 Cues Up the Hologram Projector ─────────────────────────────────────
    // R2 loads the hologram recording into his projector, connects to the
    // live ship's sensor feed, and gets ready to display the schematics as
    // soon as Obi-Wan asks for them.
    // We do the same: register the page with the parent editor, set the tab
    // label from the NLS bundle, and hook into the schema handler so we refresh
    // when the live schema changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Source Code page and registers it with the parent editor and schema handler.
     * We attach a {@link SchemaHandlerListener} here so the page regenerates its source
     * text automatically whenever any schema element changes.
     *
     * @param editor  the parent {@link SchemaEditor} that owns this page
     */
    public SchemaEditorSourceCodePage( FormEditor editor )
    {
        super( editor, ID, Messages.getString( "SchemaEditorSourceCodePage.SourceCode" ) ); //$NON-NLS-1$
        Activator.getDefault().getSchemaHandler().addListener( schemaHandlerListener );
    }


    // ── R2 Projects the Hologram ──────────────────────────────────────────────
    // R2 activates the projector: the hologram blooms to life in the cabin —
    // a read-only rendering of the Death Star's full technical schematics
    // in a monospace font, with scroll bars so Obi-Wan can pan across the design.
    // We build the source code viewer here: monospace font, read-only mode,
    // and an initial population of the schema's OpenLDAP source text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the Source Code page UI: a full-page read-only {@link SchemaSourceViewer}
     * in monospace font, pre-populated with the schema's full OpenLDAP source representation.
     *
     * @param managedForm  the Eclipse Forms managed form hosting this page
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        schema = ( ( SchemaEditor ) getEditor() ).getSchema();

        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        GridLayout layout = new GridLayout();
        form.getBody().setLayout( layout );
        toolkit.paintBordersFor( form.getBody() );

        // SOURCE CODE Field
        schemaSourceViewer = new SchemaSourceViewer( form.getBody(), null, null, false, SWT.BORDER | SWT.H_SCROLL
            | SWT.V_SCROLL );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        gd.heightHint = 10;
        schemaSourceViewer.getTextWidget().setLayoutData( gd );
        schemaSourceViewer.getTextWidget().setEditable( false );

        // set text font
        Font font = JFaceResources.getFont( JFaceResources.TEXT_FONT );
        schemaSourceViewer.getTextWidget().setFont( font );

        IDocument document = new Document();
        schemaSourceViewer.setDocument( document );

        // Initializes the UI from the schema
        fillInUiFields();

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( form, PluginConstants.PLUGIN_ID + "." + "schema_editor" ); //$NON-NLS-1$ //$NON-NLS-2$

        initialized = true;
    }


    // ── R2 Regenerates the Hologram From the Current Plans ────────────────────
    // When mission intelligence updates — a new defense system added, an old
    // one removed — R2 regenerates the hologram from the latest plans so
    // Obi-Wan is always studying current blueprints, not stale ones.
    // We export the schema to OpenLDAP format and push it into the document.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the source code viewer from the current schema state.
     * We export the entire schema to OpenLDAP schema format using
     * {@link OpenLdapSchemaFileExporter#toSourceCode} and set the result as the
     * viewer's document text.
     */
    private void fillInUiFields()
    {
        schemaSourceViewer.getDocument().set( OpenLdapSchemaFileExporter.toSourceCode( schema ) );
    }


    // ── R2 Powers Down the Projector ─────────────────────────────────────────
    // When the Falcon lands and Obi-Wan no longer needs the hologram, R2
    // powers down the projector and disconnects from the ship's sensor feed —
    // no lingering connections, no memory of the blueprints.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Cleans up this page when it is disposed.
     * We remove our {@link SchemaHandlerListener} from the schema handler to
     * prevent memory leaks and stale callbacks after the editor tab closes.
     */
    public void dispose()
    {
        Activator.getDefault().getSchemaHandler().removeListener( schemaHandlerListener );

        super.dispose();
    }


    // ── R2 Updates the Hologram When Intelligence Changes ─────────────────────
    // When new Imperial intelligence comes in, R2 waits until the projector
    // is fully initialized before refreshing the hologram — no point updating
    // before the display is ready to receive new data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the source code text from the current schema state.
     * We guard with the {@code initialized} flag so we don't attempt to write
     * to the source viewer before it has been fully set up by {@link #createFormContent}.
     */
    public void refreshUI()
    {
        if ( initialized )
        {
            fillInUiFields();
        }
    }
}
