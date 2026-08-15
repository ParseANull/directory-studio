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
package org.apache.directory.studio.openldap.config.acl.widgets;


import java.text.ParseException;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclItem;
import org.apache.directory.studio.openldap.config.acl.model.OpenLdapAclParser;
import org.apache.directory.studio.openldap.config.acl.sourceeditor.OpenLdapAclSourceViewerConfiguration;


// ── CLASS: OpenLdapAclSourceEditorComposite — CASSIAN'S RAW CODE TERMINAL ────
// Cassian sits at a monospaced terminal and reads the raw ACL text in full.
// This composite is that terminal: it wraps a JFace SourceViewer configured
// with syntax colouring, content assist, and the Format pass. The tab folder
// calls refresh() to copy the current model's ACL string into the viewer when
// switching to the Source tab, and calls getInput() to parse and retrieve the
// confirmed string when the user clicks OK. forceSetInput() and forceGetInput()
// bypass the parser for cases where we need unconditional access to the text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT {@link Composite} that hosts a JFace {@link SourceViewer} configured
 * for OpenLDAP ACL syntax colouring, auto-completion, and formatting. This is
 * the Source tab's underlying control in the tab folder.
 *
 * <p>Key operations:</p>
 * <ul>
 *   <li>{@link #refresh()} — copies the model's current ACL string into the viewer
 *       and applies the formatting pass.</li>
 *   <li>{@link #getInput()} — parses the viewer's current text and returns the
 *       canonical ACL string, or throws {@link ParseException} if invalid.</li>
 *   <li>{@link #format()} — triggers the formatting pass on the current text.</li>
 * </ul>
 *
 * <p>Think of this class as Cassian's raw code terminal — full colour syntax
 * display, completions on Ctrl+Space, and a tidy-up button.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclSourceEditorComposite extends Composite
{
    /** The source editor */
    private SourceViewer sourceEditor;

    /** The source editor configuration. */
    private SourceViewerConfiguration configuration;

    /** The ACL context */
    private OpenLdapAclValueWithContext context;

    /** The ACL parser */
    private OpenLdapAclParser parser = new OpenLdapAclParser();

    // ── Constructing the Source Editor Composite ──────────────────────────────
    // Cassian opens the monospaced terminal window, stores the context, and
    // immediately creates and configures the source viewer inside.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new source editor composite. The composite fills its parent
     * (FillLayout) and immediately creates the SourceViewer with syntax colouring,
     * content assist, and formatting configured.
     *
     * <p>For example — the tab folder creating the Source tab content:</p>
     * <pre>
     *   OpenLdapAclSourceEditorComposite source = new OpenLdapAclSourceEditorComposite(
     *       container, context, SWT.NONE);
     * </pre>
     *
     * @param parent   The parent composite.
     * @param context  The ACL context providing the initial value and connection info.
     * @param style    The SWT style bits for this composite.
     */
    public OpenLdapAclSourceEditorComposite( Composite parent, OpenLdapAclValueWithContext context, int style )
    {
        super( parent, style );

        this.context = context;
        setLayout( new FillLayout() );

        createSourceEditor();
    }


    // ── Creating and Configuring the Source Viewer ────────────────────────────
    // Cassian configures the monospaced terminal: SourceViewer with scrollbars,
    // the ACL source viewer configuration (syntax/assist/format), the text font,
    // and an empty document ready to receive content.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the {@link SourceViewer}, applies the {@link OpenLdapAclSourceViewerConfiguration}
     * (syntax colouring, content assist, formatting), sets the monospaced text font,
     * and installs an empty document.
     */
    private void createSourceEditor()
    {
        // create source editor
        sourceEditor = new SourceViewer( this, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );

        // setup basic configuration
        configuration = new OpenLdapAclSourceViewerConfiguration();
        sourceEditor.configure( configuration );

        // set text font
        Font font = JFaceResources.getFont( JFaceResources.TEXT_FONT );
        sourceEditor.getTextWidget().setFont( font );

        // setup document
        IDocument document = new Document();
        sourceEditor.setDocument( document );
    }


    // ── Refreshing the Viewer From the Model ──────────────────────────────────
    // When the tab folder switches to the Source tab, it calls refresh() to push
    // the current model's ACL string into the viewer and apply the format pass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Copies the current model's ACL string (from {@link OpenLdapAclValueWithContext#getAclItem()})
     * into the source viewer and applies the format pass (newlines before "by").
     * Called by the tab folder when switching to the Source tab.
     */
    public void refresh()
    {
        forceSetInput( context.getAclItem().toString() );
    }


    // ── Setting the Viewer Content Unconditionally ────────────────────────────
    // The source tab and the format pass both need to put text into the viewer
    // without running the parser — this method does exactly that, then applies
    // the formatting pass so the text is immediately readable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the source viewer's document content to the given string without
     * running a syntax check. Also triggers the formatting pass to insert
     * newlines before {@code by} clauses.
     *
     * <p>For example — loading the model's ACL text into the viewer:</p>
     * <pre>
     *   forceSetInput("access to * by users read by * none");
     *   // viewer now shows:
     *   // access to *
     *   // by users read
     *   // by * none
     * </pre>
     *
     * @param input  The ACL string to load; may be syntactically invalid.
     */
    public void forceSetInput( String input )
    {
        sourceEditor.getDocument().set( input );

        // format
        IRegion region = new Region( 0, sourceEditor.getDocument().getLength() );
        configuration.getContentFormatter( sourceEditor ).format( sourceEditor.getDocument(), region );
    }


    // ── Parsing and Returning the Canonical ACL String ─────────────────────────
    // When the user clicks OK or switches back to Visual tab, the tab folder
    // calls getInput() to extract and validate the ACL text from the viewer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parsed, canonical ACL string from the source viewer. Strips
     * newlines (added by the formatter) before parsing, runs the ANTLR parser
     * to validate syntax, then returns the model's {@link AclItem#toString()}.
     * Throws {@link ParseException} if the text is syntactically invalid.
     *
     * <p>For example — collecting the ACL string on OK:</p>
     * <pre>
     *   String acl = sourceComposite.getInput();
     *   // → "access to * by users read by * none"
     * </pre>
     *
     * @return  The canonical ACL string with newlines collapsed.
     * @throws ParseException  If the viewer's content fails to parse.
     */
    public String getInput() throws ParseException
    {
        String input = forceGetInput();

        // strip new lines
        input = input.replaceAll( "\\n", " " ); //$NON-NLS-1$ //$NON-NLS-2$
        input = input.replaceAll( "\\r", " " ); //$NON-NLS-1$ //$NON-NLS-2$

        AclItem aclItem = parser.parse( input );

        String acl = "";

        if ( aclItem != null )
        {
            acl = aclItem.toString();
        }

        return acl;
    }


    // ── Returning the Raw Viewer Content Without Parsing ──────────────────────
    // Sometimes we need the raw text as-is — for example when the Format button
    // is pressed. This bypasses the parser entirely.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the source viewer's current document content as-is, without running
     * the syntax parser. The returned string may contain newlines inserted by the
     * formatter and may be syntactically invalid.
     *
     * @return  The raw text in the source viewer.
     */
    public String forceGetInput()
    {
        return sourceEditor.getDocument().get();
    }


    // ── Updating the Context Reference ────────────────────────────────────────
    // If the tab folder needs to swap out the context (e.g. after a value
    // refresh from the dialog) it calls setContext(). Currently a no-op.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the ACL context reference. Currently a no-op — the composite does
     * not re-render when the context changes after construction.
     *
     * @param context  The new ACL context (not currently used).
     */
    public void setContext( OpenLdapAclValueWithContext context )
    {
    }


    // ── Triggering the Format Pass Manually ──────────────────────────────────
    // The tab folder's format() method delegates here so the Format button in
    // the dialog button bar works when the Source tab is active.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Runs the formatting pass on the current viewer content. Inserts newlines
     * before each {@code by} clause (via {@link OpenLdapAclSourceViewerConfiguration#getContentFormatter}).
     * Called by the tab folder when the Format button is pressed while the Source
     * tab is active.
     */
    public void format()
    {
        IRegion region = new Region( 0, sourceEditor.getDocument().getLength() );
        configuration.getContentFormatter( sourceEditor ).format( sourceEditor.getDocument(), region );
    }
}
