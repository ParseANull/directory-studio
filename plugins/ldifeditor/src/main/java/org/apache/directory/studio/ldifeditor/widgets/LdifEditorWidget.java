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

package org.apache.directory.studio.ldifeditor.widgets;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifeditor.editor.LdifDocumentProvider;
import org.apache.directory.studio.ldifeditor.editor.LdifSourceViewerConfiguration;
import org.apache.directory.studio.ldifeditor.editor.NonExistingLdifEditorInput;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// ── CLASS: LdifEditorWidget — PORTABLE REBEL COMMS TERMINAL ──────────────────
// The Rebellion sometimes needs to slot a field-portable comms terminal into
// an existing console panel rather than opening a full cockpit — a widget that
// gives basic LDIF editing (syntax highlighting, content assist) without the
// full editor chrome (toolbar, outline, connection picker).
// LdifEditorWidget is that embedded terminal: it wraps a plain SourceViewer
// (not a ProjectionViewer) inside a Composite, connects a
// LdifDocumentProvider, and wires a LdifSourceViewerConfiguration so the
// embedded text area behaves just like the full LDIF editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT/JFace widget that provides a self-contained LDIF editor panel.
 * Embeds a {@link SourceViewer} pre-configured with
 * {@link LdifSourceViewerConfiguration} for syntax highlighting and optional
 * content assist, and a {@link LdifDocumentProvider} for LDIF model parsing.
 * Use this widget when you need LDIF editing inside a dialog or view rather
 * than as a full Eclipse editor.
 * Think of this as the Rebellion's portable comms terminal — same capability
 * as the full cockpit, just in a smaller form factor.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorWidget extends AbstractWidget implements ILdifEditor, ITextListener
{

    /** The LDAP browser connection used for schema-aware content assist. */
    private IBrowserConnection connection;

    /** The LDIF text to display when the widget is first shown. */
    private String initialLdif;

    /** Whether content assist (Ctrl+Space) is wired up in this widget. */
    private boolean contentAssistEnabled;

    /** Dummy editor input used to key the document provider. */
    private NonExistingLdifEditorInput editorInput;

    /** Parses the document text into an {@link LdifFile} model. */
    private LdifDocumentProvider documentProvider;

    /** The embedded text-area viewer. */
    private SourceViewer sourceViewer;

    /** Configures syntax highlighting, content assist, and hover for the viewer. */
    private LdifSourceViewerConfiguration sourceViewerConfiguration;

    /** The outermost SWT composite that holds the source viewer. */
    private Composite composite;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new LDIF editor widget.
     *
     * @param connection           the LDAP browser connection for schema-aware
     *                             content assist (may be {@code null})
     * @param initialLdif          the LDIF text to pre-populate the widget with
     * @param contentAssistEnabled {@code true} to enable Ctrl+Space content assist
     */
    public LdifEditorWidget( IBrowserConnection connection, String initialLdif, boolean contentAssistEnabled )
    {
        this.connection = connection;
        this.initialLdif = initialLdif;
        this.contentAssistEnabled = contentAssistEnabled;
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────
    // Unhook the text listener and disconnect the document provider to avoid
    // resource leaks after the widget is closed.
    /**
     * Disposes this widget, removing the text listener and disconnecting the
     * {@link LdifDocumentProvider}.
     */
    public void dispose()
    {
        if ( editorInput != null )
        {
            sourceViewer.removeTextListener( this );
            documentProvider.disconnect( editorInput );
            // documentProvider = null;
            editorInput = null;
        }
    }


    // ── BUILD THE UI ──────────────────────────────────────────────────────────
    // Create the composite, wire the source viewer, configure it with syntax
    // highlighting, connect the document provider, and set the initial text.
    /**
     * Creates the widget SWT controls inside {@code parent}.
     * <ol>
     *   <li>Creates a one-column {@link GridLayout} {@link Composite}.</li>
     *   <li>Creates a scrollable {@link SourceViewer} and configures it with a
     *       new {@link LdifSourceViewerConfiguration}.</li>
     *   <li>Sets the monospace text font.</li>
     *   <li>Connects a {@link LdifDocumentProvider} to a
     *       {@link NonExistingLdifEditorInput}, sets the document on the viewer,
     *       and pre-populates it with {@code initialLdif}.</li>
     *   <li>Adds {@code this} as a {@link ITextListener} so change events
     *       propagate to registered widget listeners.</li>
     * </ol>
     *
     * @param parent  the SWT parent composite
     */
    public void createWidget( Composite parent )
    {
        composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        GridLayout layout = new GridLayout( 1, false );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout( layout );

        // create source viewer
        // sourceViewer = new ProjectionViewer(parent, ruler,
        // getOverviewRuler(), true, styles);
        sourceViewer = new SourceViewer( composite, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
        sourceViewer.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // configure
        sourceViewerConfiguration = new LdifSourceViewerConfiguration( this, this.contentAssistEnabled );
        sourceViewer.configure( sourceViewerConfiguration );

        // set font
        Font font = JFaceResources.getFont( JFaceResources.TEXT_FONT );
        sourceViewer.getTextWidget().setFont( font );

        // setup document
        try
        {
            editorInput = new NonExistingLdifEditorInput();
            documentProvider = new LdifDocumentProvider();
            documentProvider.connect( editorInput );

            IDocument document = documentProvider.getDocument( editorInput );
            document.set( initialLdif );
            sourceViewer.setDocument( document );
        }
        catch ( CoreException e )
        {
            e.printStackTrace();
        }

        // listener
        sourceViewer.addTextListener( this );

        // focus
        sourceViewer.getControl().setFocus();
    }


    // ── ILdifEditor: getConnection ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── ILdifEditor: getLdifModel ─────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link LdifDocumentProvider#getLdifModel()}.</p>
     */
    public LdifFile getLdifModel()
    {
        return documentProvider.getLdifModel();
    }


    // ── ILdifEditor: getAdapter ───────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Always returns {@code null} — this widget does not adapt to other
     * Eclipse interfaces.</p>
     */
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── ITextListener: textChanged ────────────────────────────────────────────
    // Relay text-change events to any listeners registered on this widget.
    /**
     * {@inheritDoc}
     *
     * <p>Notifies all registered {@link AbstractWidget} listeners whenever the
     * document content changes.</p>
     */
    public void textChanged( TextEvent event )
    {
        super.notifyListeners();
    }


    // ── getSourceViewer ───────────────────────────────────────────────────────
    /**
     * Returns the embedded {@link SourceViewer}.
     *
     * @return the source viewer
     */
    public SourceViewer getSourceViewer()
    {
        return sourceViewer;
    }


    // ── getSourceViewerConfiguration ─────────────────────────────────────────
    /**
     * Returns the {@link LdifSourceViewerConfiguration} used to configure the
     * source viewer.
     *
     * @return the source viewer configuration
     */
    public LdifSourceViewerConfiguration getSourceViewerConfiguration()
    {
        return sourceViewerConfiguration;
    }


    // ── getControl ────────────────────────────────────────────────────────────
    /**
     * Returns the outermost SWT {@link Composite} that hosts this widget.
     *
     * @return the primary SWT control for this widget
     */
    public Control getControl()
    {
        return composite;
    }
}
