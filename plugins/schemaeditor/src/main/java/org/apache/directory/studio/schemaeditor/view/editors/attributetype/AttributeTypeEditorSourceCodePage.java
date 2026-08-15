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

package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import java.text.ParseException;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
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


// ── CLASS: AttributeTypeEditorSourceCodePage — C-3PO READS THE JAWA DIALECT ─────────
// C-3PO is one of the few beings in the galaxy who can both understand the Jawas'
// rapid, high-pitched dialect AND translate it back into something the crew can act on.
// He listens carefully, decodes each utterance as it comes in, and warns immediately
// if the Jawas say something garbled or contradictory — "I'm afraid that grammar does
// not parse, sir."
// This page is C-3PO for OpenLDAP schema syntax.  It shows the attribute type as raw
// schema text in a monospaced editor, listens for every keystroke, tries to parse each
// change using OpenLdapSchemaParser, and sets a "canLeaveThePage" flag so the Overview
// page knows whether the current source is valid before allowing a tab switch.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Source Code" tab page of the Attribute Type Editor.
 * It presents the attribute type's definition in OpenLDAP schema format (the same
 * format used in .schema files) inside a syntax-highlighted source viewer.  Any edit
 * the user makes is immediately parsed; if the parse succeeds we update the working-copy
 * attribute type; if it fails we set a flag so switching away from this tab shows an
 * error rather than silently discarding the bad source.
 * Think of this as C-3PO reading the Jawa dialect: fluent, real-time, and vocal about
 * parse failures.
 */
public class AttributeTypeEditorSourceCodePage extends AbstractAttributeTypeEditorPage
{
    /** The page ID */
    public static final String ID = AttributeTypeEditor.ID + "sourceCodePage"; //$NON-NLS-1$

    /** The Schema Source Viewer */
    private SchemaSourceViewer schemaSourceViewer;

    /** The flag to indicate if the user can leave the Source Code page */
    private boolean canLeaveThePage = true;

    /** The listener of the Schema Source Editor Widget */
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
                List<?> attributeTypes = parser.getAttributeTypes();

                if ( attributeTypes.size() != 1 )
                {
                    // Throw an exception and return
                }
                else
                {
                    updateAttributeType( ( AttributeType ) attributeTypes.get( 0 ) );
                }
            }
            catch ( ParseException e1 )
            {
                canLeaveThePage = false;
            }
        }
    };


    // ── C-3PO Powers Up and Registers His Language Modules ───────────────────────────
    // C-3PO activates his Jawa-dialect module and announces himself to the crew, ready
    // to translate incoming schema text.  He's wired to the specific editor (the bridge)
    // that will coordinate across all pages.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new Source Code page and associates it with its parent editor.
     * We pass the stable page ID and a localised tab title to the superclass; Eclipse
     * uses the ID for page management and the title for the tab label.
     *
     * <p>For example — C-3PO joins the bridge crew:</p>
     * <pre>
     *   // The editor creates us; we register with it so we can reach the modified AT.
     *   new AttributeTypeEditorSourceCodePage( attributeTypeEditor );
     * </pre>
     *
     * @param editor  the {@link AttributeTypeEditor} that owns this page; provides
     *                access to the original and modified attribute type objects
     */
    public AttributeTypeEditorSourceCodePage( AttributeTypeEditor editor )
    {
        super( editor, ID, Messages.getString( "AttributeTypeEditorSourceCodePage.SourceCode" ) ); //$NON-NLS-1$
    }


    // ── C-3PO Sets Up His Translation Console ────────────────────────────────────────
    // C-3PO arranges his translation console: a syntax-highlighted text area where the
    // Jawa dialect will appear in a legible monospaced font, scrollable horizontally and
    // vertically.  He also loads the initial text (the current attribute type's schema
    // definition) so there's something to read from the start.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the source-code page UI inside the given Eclipse managed form.
     * We create a {@link SchemaSourceViewer} (a specialised JFace text viewer with
     * OpenLDAP schema syntax highlighting), set it to a monospaced font, populate it
     * with the current attribute type's source, attach the modify listener, and register
     * a help context for the dynamic-help system.
     * The {@code initialized} flag is set at the end so {@link #refreshUI()} knows the
     * form is ready.
     *
     * <p>For example — C-3PO's console comes online:</p>
     * <pre>
     *   // Result: a scrollable, syntax-highlighted source editor pre-filled with:
     *   //   attributetype ( 2.5.4.3 NAME 'cn' SUP name EQUALITY ... )
     * </pre>
     *
     * @param managedForm  the Eclipse-managed form container for this page
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

        // Initialization from the "input" attribute type
        fillInUiFields();

        // Listeners initialization
        addListeners();

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( form,
            PluginConstants.PLUGIN_ID + "." + "attribute_type_editor" ); //$NON-NLS-1$ //$NON-NLS-2$

        initialized = true;
    }


    // ── C-3PO Plugs In His Headset ────────────────────────────────────────────────────
    // C-3PO plugs in his translation headset to start listening for incoming Jawa text.
    // We only wire the listener if the viewer exists — the page might call addListeners
    // before the viewer is created in edge cases.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the modify listener to the source viewer's text widget.
     * The listener parses the content on every keystroke and updates the working-copy
     * attribute type or sets the canLeaveThePage flag to false on parse failure.
     * We guard with a null check because the abstract parent may call this before the
     * viewer is constructed.
     */
    protected void addListeners()
    {
        if ( schemaSourceViewer != null )
        {
            addModifyListener( schemaSourceViewer.getTextWidget(), schemaSourceViewerListener );
        }
    }


    // ── C-3PO Unplugs His Headset ─────────────────────────────────────────────────────
    // C-3PO unplugs his headset before the refresh so that the programmatic setText
    // operation doesn't fire the parse listener — we don't want to re-parse content
    // we've just written ourselves.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the modify listener from the source viewer's text widget.
     * Called before {@link #fillInUiFields()} during a refresh so that setting the
     * document text programmatically doesn't trigger the schema parser listener.
     * Guarded with a null check for the same reason as {@link #addListeners()}.
     */
    protected void removeListeners()
    {
        if ( schemaSourceViewer != null )
        {
            removeModifyListener( schemaSourceViewer.getTextWidget(), schemaSourceViewerListener );
        }
    }


    // ── C-3PO Reads the Current Mission Briefing Aloud ───────────────────────────────
    // C-3PO reads the current attribute type's schema definition into the source viewer
    // so the user can see the raw OpenLDAP schema text.  We use the exporter to produce
    // the correct OpenLDAP-format string from the in-memory attribute type.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the source viewer with the OpenLDAP schema-format representation of the
     * current working-copy attribute type.
     * We use {@link OpenLdapSchemaFileExporter#toSourceCode} to generate the canonical
     * text; the viewer's document is updated so the user sees the current state.
     * Called on page creation and on every UI refresh cycle.
     */
    protected void fillInUiFields()
    {
        schemaSourceViewer.getDocument().set( OpenLdapSchemaFileExporter.toSourceCode( getModifiedAttributeType() ) );
    }


    // ── C-3PO Reports Whether He Understood the Last Sentence ────────────────────────
    // After each incoming Jawa phrase, C-3PO either nods (parsed successfully) or
    // raises a hand (parse error).  This method lets the bridge captain check whether
    // the last thing C-3PO heard made sense before allowing a tab switch.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the current source-code content can be parsed without errors.
     * The {@link AttributeTypeEditor} calls this before switching to the Overview tab
     * and before saving: if it returns false, the editor shows an error dialog and
     * prevents the switch or save.
     * This flag starts as true (no errors) and is updated by the modify listener on
     * every keystroke.
     *
     * @return  {@code true} if the source text is valid OpenLDAP schema, {@code false}
     *          if the last parse attempt failed
     */
    public boolean canLeaveThePage()
    {
        return canLeaveThePage;
    }


    // ── C-3PO Updates the Ship's Logs from the Translated Text ───────────────────────
    // Once C-3PO has decoded the Jawa message, he updates all relevant ship's logs with
    // the new information — field by field, making sure every detail is transferred
    // correctly from the parsed structure into the working-copy attribute type.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Applies all fields from a freshly parsed {@link AttributeType} onto the editor's
     * mutable working copy.
     * We copy every property individually rather than replacing the whole object so that
     * the existing object reference in the editor stays valid (other parts of the code
     * hold that reference).  This method is called from the modify listener each time
     * the source text parses successfully.
     *
     * <p>For example — C-3PO updates the logs field by field:</p>
     * <pre>
     *   // Parsed from source: collective=false, description="Common Name", oid="2.5.4.3", ...
     *   // Applied to modifiedAttributeType: each field set individually.
     * </pre>
     *
     * @param atl  the freshly parsed AttributeType whose field values we copy across
     *             into the editor's working-copy attribute type
     */
    private void updateAttributeType( AttributeType atl )
    {
        AttributeType modifiedAttributeType = getModifiedAttributeType();

        modifiedAttributeType.setCollective( atl.isCollective() );
        modifiedAttributeType.setDescription( atl.getDescription() );
        modifiedAttributeType.setEqualityOid( atl.getEqualityOid() );
        modifiedAttributeType.setSyntaxLength( atl.getSyntaxLength() );
        modifiedAttributeType.setNames( atl.getNames() );
        modifiedAttributeType.setObsolete( atl.isObsolete() );
        modifiedAttributeType.setOid( atl.getOid() );
        modifiedAttributeType.setOrderingOid( atl.getOrderingOid() );
        modifiedAttributeType.setSingleValued( atl.isSingleValued() );
        modifiedAttributeType.setSubstringOid( atl.getSubstringOid() );
        modifiedAttributeType.setSuperiorOid( atl.getSuperiorOid() );
        modifiedAttributeType.setSyntaxOid( atl.getSyntaxOid() );
        modifiedAttributeType.setUsage( atl.getUsage() );
        modifiedAttributeType.setUserModifiable( atl.isUserModifiable() );
    }
}
