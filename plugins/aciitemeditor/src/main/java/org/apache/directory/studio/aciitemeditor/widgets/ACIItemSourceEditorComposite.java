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
package org.apache.directory.studio.aciitemeditor.widgets;


import java.text.ParseException;

import org.apache.directory.api.ldap.aci.ACIItem;
import org.apache.directory.api.ldap.aci.ACIItemParser;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.sourceeditor.ACISourceViewerConfiguration;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ACIItemSourceEditorComposite — CASSIAN'S INTERCEPT CONSOLE ─────────
// Cassian Andor leans over a terminal displaying the raw intercepted Imperial
// transmission — unformatted, verbatim, exactly as the rebels captured it.
// This composite is that console: a syntax-highlighted SourceViewer showing the
// raw ACI item string.  You can load it unchecked (force) or validated (parse).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} wrapping a syntax-highlighted {@link SourceViewer}
 * for editing raw ACI item strings.
 * Provides validated ({@link #setInput}/{@link #getInput}) and unvalidated
 * ({@link #forceSetInput}/{@link #forceGetInput}) access to the editor content.
 * Formatting is applied automatically on load via the content formatter.
 * Think of this as Cassian's intercept console: syntax colour, auto-format,
 * parse-on-demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemSourceEditorComposite extends Composite
{
    /** The source editor */
    private SourceViewer sourceEditor;

    /** The source editor configuration. */
    private SourceViewerConfiguration configuration;


    // ── CONSTRUCT THE SOURCE CONSOLE ──────────────────────────────────────────
    // Cassian's console is assembled: a SourceViewer is created with syntax
    // highlighting configuration, monospace font, and an empty Document.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACIItemSourceEditorComposite}.
     * Builds the {@link SourceViewer}, attaches {@link ACISourceViewerConfiguration},
     * sets the monospace font, and initialises an empty document.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemSourceEditorComposite( Composite parent, int style )
    {
        super( parent, style );
        setLayout( new GridLayout() );

        createSourceEditor();
    }


    // ── BUILD THE SYNTAX-HIGHLIGHTED EDITOR ───────────────────────────────────
    // The SourceViewer is created, configured with ACI syntax highlighting,
    // given the JFace monospace font, and wired to an empty Document.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates and configures the {@link SourceViewer}.
     */
    private void createSourceEditor()
    {
        // create source editor
        sourceEditor = new SourceViewer( this, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
        sourceEditor.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // setup basic configuration
        configuration = new ACISourceViewerConfiguration();
        sourceEditor.configure( configuration );

        // set text font
        Font font = JFaceResources.getFont( JFaceResources.TEXT_FONT );
        sourceEditor.getTextWidget().setFont( font );

        // setup document
        IDocument document = new Document();
        sourceEditor.setDocument( document );
    }


    // ── VALIDATED INPUT SETTER ────────────────────────────────────────────────
    // Cassian verifies the transmission before loading it into the console —
    // if the parser rejects it we throw instead of loading garbage.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates {@code input} by parsing it, then loads it into the source
     * viewer and auto-formats.
     *
     * <p>For example — the tab folder synchronises from visual to source:</p>
     * <pre>
     *   sourceComposite.setInput(visualComposite.getInput());
     *   // parse succeeds, the ACI text appears formatted in the editor
     * </pre>
     *
     * @param input  the ACI string to validate and load
     * @throws ParseException  if {@code input} is not valid ACI syntax
     */
    public void setInput( String input ) throws ParseException
    {
        ACIItemParser parser = Activator.getDefault().getACIItemParser();
        parser.parse( input );

        forceSetInput( input );
    }


    // ── UNVALIDATED INPUT SETTER ──────────────────────────────────────────────
    // When the dialog first opens with whatever string is stored in the attribute,
    // we load it without validation — the user may need to fix bad syntax.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads {@code input} into the source viewer and auto-formats it, without
     * performing a syntax check.
     * Use this when the content may be malformed (e.g., on initial dialog open).
     *
     * @param input  the ACI string to load (may be invalid)
     */
    public void forceSetInput( String input )
    {
        sourceEditor.getDocument().set( input );

        // format
        IRegion region = new Region( 0, sourceEditor.getDocument().getLength() );
        configuration.getContentFormatter( sourceEditor ).format( sourceEditor.getDocument(), region );

    }


    // ── VALIDATED OUTPUT GETTER ───────────────────────────────────────────────
    // Cassian re-parses the transmission before handing it off to confirm it
    // is still valid after any edits; the normalised string is returned.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the normalised ACI string from the source editor after stripping
     * newlines and parsing for validity.
     * Throws {@link ParseException} if the content is invalid.
     *
     * @return the valid normalised ACI string
     * @throws ParseException  if the source editor content fails to parse
     */
    public String getInput() throws ParseException
    {
        String input = forceGetInput();

        // strip new lines
        input = input.replaceAll( "\\n", " " ); //$NON-NLS-1$ //$NON-NLS-2$
        input = input.replaceAll( "\\r", " " ); //$NON-NLS-1$ //$NON-NLS-2$

        ACIItemParser parser = Activator.getDefault().getACIItemParser();
        ACIItem aciItem = parser.parse( input );

        String aci = ""; //$NON-NLS-1$
        if ( aciItem != null )
        {
            aci = aciItem.toString();
        }
        return aci;
    }


    // ── UNVALIDATED OUTPUT GETTER ─────────────────────────────────────────────
    /**
     * Returns the raw text currently in the source editor without any validation.
     *
     * @return the raw editor content, which may be invalid ACI syntax
     */
    public String forceGetInput()
    {
        return sourceEditor.getDocument().get();
    }


    // ── CONTEXT INJECTION ─────────────────────────────────────────────────────
    /**
     * Accepts the connection context; currently unused by the source editor
     * (the source editor does not need schema access directly).
     *
     * @param context  the value context (not currently used)
     */
    public void setContext( ACIItemValueWithContext context )
    {
    }


    // ── FORMAT THE CURRENT DOCUMENT ───────────────────────────────────────────
    /**
     * Runs the content formatter over the entire source editor document,
     * pretty-printing the ACI text.
     */
    public void format()
    {
        IRegion region = new Region( 0, sourceEditor.getDocument().getLength() );
        configuration.getContentFormatter( sourceEditor ).format( sourceEditor.getDocument(), region );
    }

}
