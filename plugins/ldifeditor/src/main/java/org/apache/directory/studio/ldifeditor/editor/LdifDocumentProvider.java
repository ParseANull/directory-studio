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

package org.apache.directory.studio.ldifeditor.editor;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.text.LdifExternalAnnotationModel;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.apache.directory.studio.ldifparser.parser.LdifParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;


// ── CLASS: LdifDocumentProvider — REBEL TRANSMISSION RELAY ───────────────────
// The Alliance communications centre keeps a single live copy of each
// intercepted transmission.  When a character is typed the relay station
// re-parses only the portion of the message that changed, updates the
// structured model, and propagates the change to all listeners.
// LdifDocumentProvider is that relay station: it owns the IDocument, wires
// the LDIF partition scanner onto it, drives incremental re-parsing on every
// DocumentEvent, and writes the file back to disk on save.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link AbstractDocumentProvider} for LDIF files.
 * Owns the {@link IDocument}, sets up LDIF partitioning via
 * {@link LdifDocumentSetupParticipant}, performs initial full parsing, and
 * then drives incremental re-parsing on every {@link DocumentEvent} by
 * implementing {@link IDocumentListener}.
 * Think of this as the Alliance relay station: keeps one live copy of the
 * transmission and updates only the changed portion of the model on each
 * keystroke.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifDocumentProvider extends AbstractDocumentProvider implements IDocumentListener
{

    /** The LDIF parser used for both full and incremental parsing. */
    private final LdifParser ldifParser;

    /** Sets up LDIF partitioning on a document. */
    private final LdifDocumentSetupParticipant ldifDocumentSetupParticipant;

    /** The current parsed model of the document. */
    private LdifFile ldifModel;


    // ── CONSTRUCT THE DOCUMENT PROVIDER ───────────────────────────────────────
    // The relay station comes online: the parser and partitioner are ready.
    /**
     * Creates a new {@code LdifDocumentProvider} with fresh
     * {@link LdifParser} and {@link LdifDocumentSetupParticipant} instances.
     */
    public LdifDocumentProvider()
    {
        super();
        this.ldifParser = new LdifParser();
        this.ldifDocumentSetupParticipant = new LdifDocumentSetupParticipant();
    }


    // ── GET THE DOCUMENT ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the superclass document cache.</p>
     */
    public IDocument getDocument( Object element )
    {
        IDocument document = super.getDocument( element );
        return document;
    }


    // ── GET THE PARSED MODEL ──────────────────────────────────────────────────
    // Hand back the structured LDIF model so actions and outline pages can use it.
    /**
     * Returns the current parsed {@link LdifFile} model.
     *
     * @return the LDIF model
     */
    public LdifFile getLdifModel()
    {
        return ldifModel;
    }


    // ── PRE-CHANGE HOOK ───────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — we do not need to act before the change.</p>
     */
    public void documentAboutToBeChanged( DocumentEvent event )
    {
    }


    // ── INCREMENTAL RE-PARSE ON DOCUMENT CHANGE ───────────────────────────────
    // C-3PO intercepts the updated fragment of the transmission, re-parses
    // only the affected region, and splices the new containers into the model.
    /**
     * {@inheritDoc}
     *
     * <p>Computes the set of {@link LdifContainer} objects that overlap the
     * change region (expanded by one line separator on each side to capture
     * partial record boundaries), re-parses the corresponding document text,
     * and calls {@link LdifFile#replace} to update the model in-place.</p>
     */
    public void documentChanged( DocumentEvent event )
    {
        try
        {
            int changeOffset = event.getOffset();
            int replacedTextLength = event.getLength();
            int insertedTextLength = event.getText() != null ? event.getText().length() : 0;
            IDocument document = event.getDocument();
            // Region changeRegion = new Region(changeOffset,
            // replacedTextLength);
            Region changeRegion = new Region( changeOffset - BrowserCoreConstants.LINE_SEPARATOR.length(),
                replacedTextLength + ( 2 * BrowserCoreConstants.LINE_SEPARATOR.length() ) );

            // get containers to replace (from changeOffset till
            // changeOffset+replacedTextLength, check end of record)
            List<LdifContainer> oldContainerList = new ArrayList<LdifContainer>();
            List<LdifContainer> containers = ldifModel.getContainers();

            for ( int i = 0; i < containers.size(); i++ )
            {
                LdifContainer ldifContainer = containers.get( i );

                Region containerRegion = new Region( containers.get( i ).getOffset(), containers.get( i ).getLength() );

                boolean changeOffsetAtEOF = i == containers.size() - 1
                    && changeOffset >= containerRegion.getOffset() + containerRegion.getLength();

                if ( TextUtilities.overlaps( containerRegion, changeRegion ) || changeOffsetAtEOF )
                {
                    // remember index
                    int index = i;

                    // add invalid containers and non-records before overlap
                    i--;
                    for ( ; i >= 0; i-- )
                    {
                        ldifContainer = containers.get( i );

                        if ( !ldifContainer.isValid() || !( ldifContainer instanceof LdifRecord ) )
                        {
                            oldContainerList.add( 0, ldifContainer );
                        }
                        else
                        {
                            break;
                        }
                    }

                    // add all overlapping containers
                    i = index;
                    for ( ; i < containers.size(); i++ )
                    {
                        ldifContainer = containers.get( i );
                        containerRegion = new Region( ldifContainer.getOffset(), ldifContainer.getLength() );

                        if ( TextUtilities.overlaps( containerRegion, changeRegion ) || changeOffsetAtEOF )
                        {
                            oldContainerList.add( ldifContainer );
                        }
                        else
                        {
                            break;
                        }
                    }

                    // add invalid containers and non-records after overlap
                    for ( ; i < containers.size(); i++ )
                    {
                        ldifContainer = containers.get( i );

                        if ( !ldifContainer.isValid() || !( ldifContainer instanceof LdifRecord )
                            || !( oldContainerList.get( oldContainerList.size() - 1 ) instanceof LdifRecord ) )
                        {
                            oldContainerList.add( ldifContainer );
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }

            LdifContainer[] oldContainers = ( LdifContainer[] ) oldContainerList
                .toArray( new LdifContainer[oldContainerList.size()] );
            int oldCount = oldContainers.length;
            int oldOffset = oldCount > 0 ? oldContainers[0].getOffset() : 0;
            int oldLength = oldCount > 0 ? ( oldContainers[oldContainers.length - 1].getOffset()
                + oldContainers[oldContainers.length - 1].getLength() - oldContainers[0].getOffset() ) : 0;

            // get new content
            int newOffset = oldOffset;
            int newLength = oldLength - replacedTextLength + insertedTextLength;
            String textToParse = document.get( newOffset, newLength );

            // parse partion content to containers (offset=0)
            LdifFile newModel = this.ldifParser.parse( textToParse );
            List<LdifContainer> newContainers = newModel.getContainers();

            // replace old containers with new containers
            // must adjust offsets of all following containers in model
            ldifModel.replace( oldContainers, newContainers );

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

    }


    // ── CREATE THE ANNOTATION MODEL ───────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns a new {@link LdifExternalAnnotationModel}.</p>
     */
    protected IAnnotationModel createAnnotationModel( Object element ) throws CoreException
    {
        return new LdifExternalAnnotationModel();
    }


    // ── LOAD FILE CONTENT INTO DOCUMENT ───────────────────────────────────────
    // The relay operator loads the file from disk into the document buffer,
    // handling different editor input types (IPathEditorInput, JavaFileEditorInput,
    // FileStoreEditorInput).
    /**
     * Reads the file pointed to by {@code input} (if it is an
     * {@link IPathEditorInput} or a known file-editor input type) into
     * {@code document}.
     * Returns {@code true} on success or if the file does not yet exist
     * (a new file), {@code false} if the input type is unsupported.
     *
     * @param document  the document to fill
     * @param input     the editor input
     * @return          {@code true} if the document was set successfully or the
     *                  file does not exist yet
     * @throws CoreException  if reading fails
     */
    private boolean setDocumentContent( IDocument document, IEditorInput input ) throws CoreException
    {
        // TODO: handle encoding
        Reader reader;
        try
        {
            String inputClassName = input.getClass().getName();
            if ( input instanceof IPathEditorInput )
            {
                reader = new FileReader( ( ( IPathEditorInput ) input ).getPath().toFile() );
            }
            else if ( inputClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" ) //$NON-NLS-1$
                || inputClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) ) //$NON-NLS-1$
            // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
            // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
            // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
            // opening a file from the menu File > Open... in Eclipse 3.3.x
            {
                reader = new FileReader( new File( input.getToolTipText() ) );
            }
            else
            {
                return false;
            }
        }
        catch ( FileNotFoundException e )
        {
            // return empty document and save later
            return true;
        }

        try
        {
            setDocumentContent( document, reader );
            return true;
        }
        catch ( IOException e )
        {
            throw new CoreException( new Status( IStatus.ERROR, LdifEditorConstants.PLUGIN_ID, IStatus.OK,
                "error reading file", e ) ); //$NON-NLS-1$
        }
    }


    // ── READ FROM A READER INTO THE DOCUMENT ──────────────────────────────────
    /**
     * Reads all characters from {@code reader} into {@code document}.
     *
     * @param document  the document to fill
     * @param reader    the source reader
     * @throws IOException  if reading fails
     */
    private void setDocumentContent( IDocument document, Reader reader ) throws IOException
    {
        Reader in = new BufferedReader( reader );
        try
        {
            StringBuffer buffer = new StringBuffer( 512 );
            char[] readBuffer = new char[512];
            int n = in.read( readBuffer );
            while ( n > 0 )
            {
                buffer.append( readBuffer, 0, n );
                n = in.read( readBuffer );
            }

            document.set( buffer.toString() );

        }
        finally
        {
            in.close();
        }
    }


    // ── SETUP PARTITIONING AND INITIAL PARSING ────────────────────────────────
    // Before the relay station accepts live traffic the operator installs
    // the partitioner, runs a full initial parse, and registers the change listener.
    /**
     * Sets up LDIF partitioning, performs the initial full parse, and
     * registers this provider as a document listener for incremental updates.
     *
     * @param document  the new document
     */
    protected void setupDocument( IDocument document )
    {

        // setup document partitioning
        ldifDocumentSetupParticipant.setup( document );

        // initial parsing of whole document
        this.ldifModel = this.ldifParser.parse( document.get() );

        // add listener for incremental parsing
        document.addDocumentListener( this );

    }


    // ── TEAR DOWN THE ELEMENT ─────────────────────────────────────────────────
    // When the relay station goes offline the document listener is removed.
    /**
     * {@inheritDoc}
     *
     * <p>Removes this provider from the document's listener list before
     * delegating to the superclass.</p>
     */
    protected void disposeElementInfo( Object element, ElementInfo info )
    {
        IDocument document = info.fDocument;
        document.removeDocumentListener( this );

        super.disposeElementInfo( element, info );
    }


    // ── CREATE THE DOCUMENT ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Creates a new {@link Document}, loads content from the editor input,
     * and calls {@link #setupDocument} to wire partitioning and parsing.</p>
     */
    protected IDocument createDocument( Object element ) throws CoreException
    {
        if ( element instanceof IEditorInput )
        {
            IDocument document = new Document();
            if ( setDocumentContent( document, ( IEditorInput ) element ) )
            {
                setupDocument( document );
            }
            return document;
        }

        return null;
    }


    // ── SAVE THE DOCUMENT ─────────────────────────────────────────────────────
    // The relay operator transmits the current document content to the file
    // on disk, handling workspace resources and external files differently.
    /**
     * {@inheritDoc}
     *
     * <p>Saves the document to the file identified by {@code element}.
     * Supports {@link FileEditorInput} (workspace resources),
     * {@link IPathEditorInput}, and the Eclipse 3.2/3.3 file-editor inputs.</p>
     */
    protected void doSaveDocument( IProgressMonitor monitor, Object element, IDocument document, boolean overwrite )
        throws CoreException
    {
        File file = null;
        String elementClassName = element.getClass().getName();
        if ( element instanceof FileEditorInput )
        // FileEditorInput class is used when the file is opened
        // from a project in the workspace.
        {
            writeDocumentContent( document, ( ( FileEditorInput ) element ).getFile(), monitor );
            return;
        }
        else if ( element instanceof IPathEditorInput )
        {
            IPathEditorInput pei = ( IPathEditorInput ) element;
            IPath path = pei.getPath();
            file = path.toFile();
        }
        else if ( elementClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" ) //$NON-NLS-1$
            || elementClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) ) //$NON-NLS-1$
        // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
        // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
        // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
        // opening a file from the menu File > Open... in Eclipse 3.3.x
        {
            file = new File( ( ( IEditorInput ) element ).getToolTipText() );
        }

        if ( file != null )
        {
            try
            {
                file.createNewFile();

                if ( file.exists() )
                {
                    if ( file.canWrite() )
                    {
                        Writer writer = new FileWriter( file );
                        writeDocumentContent( document, writer, monitor );
                    }
                    else
                    {
                        throw new CoreException( new Status( IStatus.ERROR,
                            "org.eclipse.ui.examples.rcp.texteditor", IStatus.OK, "file is read-only", null ) ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                else
                {
                    throw new CoreException( new Status( IStatus.ERROR,
                        "org.eclipse.ui.examples.rcp.texteditor", IStatus.OK, "error creating file", null ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            catch ( IOException e )
            {
                throw new CoreException( new Status( IStatus.ERROR,
                    "org.eclipse.ui.examples.rcp.texteditor", IStatus.OK, "error when saving file", e ) ); //$NON-NLS-1$ //$NON-NLS-2$
            }

        }
    }


    // ── WRITE DOCUMENT TO A WORKSPACE FILE ───────────────────────────────────
    /**
     * Writes the document content to an {@link IFile} (workspace resource).
     *
     * @param document  the document to save
     * @param file      the workspace file
     * @param monitor   a progress monitor
     * @throws CoreException  if writing fails
     */
    private void writeDocumentContent( IDocument document, IFile file, IProgressMonitor monitor ) throws CoreException
    {
        if ( file != null )
        {
            file.setContents( new ByteArrayInputStream( document.get().getBytes() ), true, true, monitor );
        }
    }


    // ── WRITE DOCUMENT TO A WRITER ────────────────────────────────────────────
    /**
     * Writes the document content to a {@link Writer} (external file).
     *
     * @param document  the document to save
     * @param writer    the destination writer
     * @param monitor   a progress monitor (unused)
     * @throws IOException  if writing fails
     */
    private void writeDocumentContent( IDocument document, Writer writer, IProgressMonitor monitor ) throws IOException
    {
        Writer out = new BufferedWriter( writer );
        try
        {
            out.write( document.get() );
        }
        finally
        {
            out.close();
        }
    }


    // ── GET THE OPERATION RUNNER ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} — no runnable context is needed.</p>
     */
    protected IRunnableContext getOperationRunner( IProgressMonitor monitor )
    {
        return null;
    }


    // ── CHECK MODIFIABILITY ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} if the file can be written, or if it does not
     * yet exist (to allow editing new files before they are saved).</p>
     */
    public boolean isModifiable( Object element )
    {
        String elementClassName = element.getClass().getName();
        if ( element instanceof IPathEditorInput )
        {
            IPathEditorInput pei = ( IPathEditorInput ) element;
            File file = pei.getPath().toFile();
            return file.canWrite() || !file.exists(); // Allow to edit new files
        }
        else if ( elementClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" ) //$NON-NLS-1$
            || elementClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) ) //$NON-NLS-1$
        // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
        // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
        // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
        // opening a file from the menu File > Open... in Eclipse 3.3.x
        {
            File file = new File( ( ( IEditorInput ) element ).getToolTipText() );
            return file.canWrite() || !file.exists(); // Allow to edit new files
        }

        return false;
    }


    // ── CHECK READ-ONLY ───────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code !isModifiable(element)}.</p>
     */
    public boolean isReadOnly( Object element )
    {
        return !isModifiable( element );
    }


    // ── CHECK STATE VALIDATION ────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Always returns {@code true} — no external state validation needed.</p>
     */
    public boolean isStateValidated( Object element )
    {
        return true;
    }
}
