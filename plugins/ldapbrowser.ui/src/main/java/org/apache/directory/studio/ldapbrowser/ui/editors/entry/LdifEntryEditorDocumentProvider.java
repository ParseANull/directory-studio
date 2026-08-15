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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.utils.AttributeComparator;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldifeditor.editor.LdifDocumentProvider;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifInvalidContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;


// ── CLASS: LdifEntryEditorDocumentProvider — C-3PO TRANSLATING FOR R2-D2 ─────
// C-3PO stands between R2-D2 and the human crew, translating R2's binary beeps
// into plain speech and converting human commands back into R2's binary language.
// LdifEntryEditorDocumentProvider does exactly that: it bridges the LDAP object
// model (structured Java objects: IEntry, IAttribute, IValue) and the Eclipse
// text editor (a plain IDocument containing LDIF text), translating in both
// directions so the LDIF editor and the LDAP model always stay in sync.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The document provider that connects the LDIF text editor to the LDAP working copy.
 * It translates the LDAP entry object model into LDIF text when the editor opens or
 * when the model changes externally, and translates the LDIF text back into LDAP
 * modify operations when the user saves.
 * Think of it as C-3PO: fluent in both the "protocol droids" language (LDIF text)
 * and the "human" language (the structured LDAP model), translating continuously
 * in both directions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEntryEditorDocumentProvider extends LdifDocumentProvider
{

    private EntryEditorInput input;

    private boolean inSetContent = false;

    private LdifEntryEditor editor;


    // ── C-3PO IS ASSIGNED TO A SPECIFIC MISSION ──────────────────────────────
    // C-3PO is introduced to the crew he'll be translating for — he knows who
    // he's working with and that relationship drives everything he does.
    // We store the editor reference so we can call back to it during save,
    // document-change, and working-copy-modified events.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a document provider bound to the given LDIF entry editor.
     * We need the editor reference to call {@link LdifEntryEditorDocumentProvider#workingCopyModified}
     * back into the editor and to use it as the "editor" token for the shared working copy.
     *
     * @param editor  The LDIF entry editor this provider serves.
     */
    public LdifEntryEditorDocumentProvider( LdifEntryEditor editor )
    {
        this.editor = editor;
    }


    // ── C-3PO TRANSLATES THE HUMAN MESSAGE INTO R2'S FORMAT AND TRANSMITS ────
    // C-3PO takes the human crew's spoken command, validates the grammar, encodes
    // it into R2's binary format, and broadcasts it — but refuses to transmit if
    // the message is malformed, reporting the problem to the captain instead.
    // We validate that the LDIF document contains exactly one valid content record,
    // then call saveSharedWorkingCopy to push the changes to the LDAP server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the LDIF text and saves changes to the LDAP server.
     * Eclipse calls this in response to Ctrl+S. We check: exactly one record, a valid
     * content record, no invalid containers, and the DN hasn't changed (we don't support
     * rename-on-save). Only then do we flush the shared working copy to the server.
     *
     * @param monitor    Progress monitor for reporting save progress.
     * @param element    The editor input element (expected to be an {@link EntryEditorInput}).
     * @param document   The LDIF document whose text we're saving.
     * @param overwrite  Whether to overwrite if the server copy has changed; not fully used here.
     * @throws CoreException if the LDIF is invalid, the DN changed, or the server rejects the update.
     */
    @Override
    protected void doSaveDocument( IProgressMonitor monitor, Object element, IDocument document, boolean overwrite )
        throws CoreException
    {
        LdifRecord[] records = getLdifModel().getRecords();
        if ( records.length != 1 || !( records[0] instanceof LdifContentRecord ) )
        {
            throw new CoreException( new Status( IStatus.ERROR, BrowserUIConstants.PLUGIN_ID, Messages
                .getString( "LdifEntryEditorDocumentProvider.InvalidRecordType" ) ) ); //$NON-NLS-1$
        }
        if ( !records[0].isValid() )
        {
            throw new CoreException( new Status( IStatus.ERROR, BrowserUIConstants.PLUGIN_ID, NLS.bind( Messages
                .getString( "LdifEntryEditorDocumentProvider.InvalidLdif" ), records[0].getInvalidString() ) ) ); //$NON-NLS-1$
        }
        for ( LdifContainer ldifContainer : getLdifModel().getContainers() )
        {
            if ( ldifContainer instanceof LdifInvalidContainer )
            {
                LdifInvalidContainer cont = ( LdifInvalidContainer ) ldifContainer;
                throw new CoreException( new Status( IStatus.ERROR, BrowserUIConstants.PLUGIN_ID, NLS.bind( Messages
                    .getString( "LdifEntryEditorDocumentProvider.InvalidLdif" ), cont.getInvalidString() ) ) ); //$NON-NLS-1$
            }
        }

        EntryEditorInput input = getEntryEditorInput( element );
        try
        {
            Dn newDn = new Dn( records[0].getDnLine().getValueAsString() );
            if ( !newDn.equals( input.getResolvedEntry().getDn() ) )
            {
                throw new CoreException( new Status( IStatus.ERROR, BrowserUIConstants.PLUGIN_ID, NLS.bind( Messages
                    .getString( "LdifEntryEditorDocumentProvider.ModDnNotSupported" ), records[0].getInvalidString() ) ) ); //$NON-NLS-1$
            }
        }
        catch ( LdapInvalidDnException e )
        {
            throw new CoreException( new Status( IStatus.ERROR, BrowserUIConstants.PLUGIN_ID, Messages
                .getString( "LdifEntryEditorDocumentProvider.InvalidDN" ) ) ); //$NON-NLS-1$
        }

        IStatus status = input.saveSharedWorkingCopy( false, editor );
        if ( status != null && !status.isOK() )
        {
            BrowserUIPlugin.getDefault().getLog().log( status );
            throw new CoreException( status );
        }
    }


    // ── C-3PO HEARS THE HUMAN SPEAK AND UPDATES R2'S MEMORY ──────────────────
    // The human crew edits the telemetry readout directly — C-3PO hears the
    // change, checks that what was said is a complete, valid sentence, and if so
    // updates R2's memory banks with the new information.
    // We only update the LDAP working copy when the LDIF text is fully valid;
    // we also guard against echo-loops using the inSetContent flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to LDIF text changes by syncing the LDAP shared working copy.
     * Called by the Eclipse text framework on every keystroke. We skip the update
     * if we're the ones who set the content (avoids an echo loop), and we skip if
     * the LDIF isn't fully valid yet (no point applying a half-typed attribute name).
     * When valid, we parse the LDIF into a {@link DummyEntry} and call
     * {@link CompoundModification#replaceAttributes} to sync the working copy.
     *
     * @param event  The document change event from the Eclipse text framework.
     */
    @Override
    public void documentChanged( DocumentEvent event )
    {
        super.documentChanged( event );

        // the document change was caused by the model update
        // no need to update the model again, don't fire more events
        if ( inSetContent )
        {
            return;
        }

        // only continue if the LDIF model is valid
        LdifRecord[] records = getLdifModel().getRecords();
        if ( records.length != 1 || !( records[0] instanceof LdifContentRecord ) || !records[0].isValid()
            || !records[0].getDnLine().isValid() )
        {
            return;
        }
        for ( LdifContainer ldifContainer : getLdifModel().getContainers() )
        {
            if ( ldifContainer instanceof LdifInvalidContainer )
            {
                return;
            }
        }

        // update shared working copy
        try
        {
            LdifContentRecord modifiedRecord = ( LdifContentRecord ) records[0];
            IBrowserConnection browserConnection = input.getSharedWorkingCopy( editor ).getBrowserConnection();
            DummyEntry modifiedEntry = ModelConverter.ldifContentRecordToEntry( modifiedRecord, browserConnection );
            ( ( DummyEntry ) input.getSharedWorkingCopy( editor ) ).setDn( modifiedEntry.getDn() );
            new CompoundModification().replaceAttributes( modifiedEntry, input.getSharedWorkingCopy( editor ), this );
        }
        catch ( LdapInvalidDnException e )
        {
            throw new RuntimeException( e );
        }
    }


    // ── C-3PO RESTORES R2'S MEMORY FROM THE LAST GOOD BACKUP ─────────────────
    // R2's memory gets corrupted — C-3PO restores the last clean backup first,
    // then asks the archive system to reload the original document from scratch.
    // We reset the LDAP working copy before calling super so the document provider
    // sees a clean state and doesn't try to diff against a corrupted working copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Resets the LDIF document and the LDAP working copy to their last saved state.
     * Eclipse calls this when the user reverts the editor. We reset the working copy
     * first so the underlying LDAP state is clean before the document is reloaded.
     *
     * @param element  The editor input element.
     * @param monitor  Progress monitor for the reset operation.
     * @throws CoreException if the reset fails.
     */
    @Override
    protected void doResetDocument( Object element, IProgressMonitor monitor ) throws CoreException
    {
        // reset working copy first
        if ( input != null )
        {
            input.resetSharedWorkingCopy( editor );
        }

        super.doResetDocument( element, monitor );
    }


    // ── C-3PO HEARS R2 BEEP AND UPDATES THE HUMAN-READABLE DISPLAY ───────────
    // R2 beeps a status update — but only if the beep didn't come from C-3PO
    // himself (he'd be talking to himself otherwise). C-3PO then reformats
    // R2's data into human-readable LDIF text and refreshes the big screen.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the LDAP model when the working copy changes externally.
     * If the change came from our own {@link #documentChanged} handler (detected by
     * {@code source == this}), we skip to avoid an echo loop. Otherwise we regenerate
     * the LDIF text from the current working copy and push it into the document,
     * then reset the dirty flag if the working copy is no longer dirty.
     *
     * @param input   The entry editor input whose working copy changed.
     * @param source  The object that triggered the change — checked to avoid self-echo.
     */
    public void workingCopyModified( EntryEditorInput input, Object source )
    {
        // the model change was caused by the document change
        // no need to set the content again, don't fire more events
        if ( source == this )
        {
            return;
        }

        IDocument document = getDocument( input );
        if ( document != null )
        {
            try
            {
                inSetContent = true;
                IEntry sharedWorkingCopy = input.getSharedWorkingCopy( editor );
                setDocumentInput( document, sharedWorkingCopy );

                // reset dirty state
                if ( !input.isSharedWorkingCopyDirty( editor ) )
                {
                    super.doResetDocument( input, null );
                }
            }
            catch ( CoreException e )
            {
                throw new RuntimeException( e );
            }
            finally
            {
                inSetContent = false;
            }
        }
    }


    // ── C-3PO FORMATS THE DATA FOR THE BIG SCREEN ────────────────────────────
    // C-3PO takes R2's raw binary data, sorts it into a readable format,
    // and sets it as the display text on the bridge's main readout panel.
    // We sort attribute-value pairs by the standard LDAP sort order,
    // build an LDIF content record, format it, and set it as the document text.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Renders the given LDAP entry as LDIF text and pushes it into the document.
     * We sort attribute values using {@link AttributeComparator}, build an
     * {@link LdifContentRecord}, format it with the configured LDIF format parameters,
     * then call {@link IDocument#set} to replace the document's entire content.
     *
     * @param document  The Eclipse text document to write the LDIF text into.
     * @param entry     The LDAP entry to render as LDIF.
     */
    private void setDocumentInput( IDocument document, IEntry entry )
    {
        // sort attribute-value lines
        List<IValue> sortedValues = AttributeComparator.toSortedValues( entry );
        LdifContentRecord newRecord = LdifContentRecord.create( entry.getDn().getName() );
        for ( IValue value : sortedValues )
        {
            newRecord.addAttrVal( ModelConverter.valueToLdifAttrValLine( value ) );
        }
        newRecord.finish( LdifSepLine.create() );

        // format
        String newContent = newRecord.toFormattedString( Utils.getLdifFormatParameters() );

        // set content
        document.set( newContent );
    }


    // ── C-3PO CHECKS WHETHER HE KNOWS THIS CREW MEMBER'S LANGUAGE ────────────
    // Before translating, C-3PO checks if he actually has a translation module
    // for the requesting crew member — some input types get a null shortcut to
    // avoid performance overhead from a full document lookup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the document for the given element, with a short-circuit for elements without an extension.
     * The extension check is a performance optimization: an {@link EntryEditorInput} without an extension
     * is a dummy input used internally; computing its document would be expensive and pointless.
     *
     * @param element  The element to look up the document for.
     * @return the {@link IDocument}, or {@code null} if this element has no extension set.
     */
    public IDocument getDocument( Object element )
    {
        if ( element instanceof EntryEditorInput )
        {
            EntryEditorInput input = ( EntryEditorInput ) element;
            if ( input.getExtension() == null )
            {
                // this is a performance optimization
                return null;
            }
        }

        return super.getDocument( element );
    }


    // ── C-3PO OPENS THE MISSION DOSSIER AND PREPARES THE FIRST TRANSLATION ───
    // C-3PO receives a sealed mission dossier, opens it, reads the entry data,
    // and prepares the first formatted translation for the big screen.
    // We create an IDocument, populate it with the LDIF representation of the
    // working copy, and set up LDIF syntax highlighting.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the initial {@link IDocument} for the given editor input.
     * Eclipse calls this when the editor first opens. We resolve the shared working
     * copy (which triggers initialization if needed), render it as LDIF text, and
     * configure LDIF syntax partitioning on the document.
     *
     * @param element  The editor input element; expected to be an {@link EntryEditorInput}.
     * @return a newly created {@link IDocument} populated with the entry's LDIF representation.
     * @throws CoreException if the input is not an {@link EntryEditorInput}.
     */
    @Override
    protected IDocument createDocument( Object element ) throws CoreException
    {
        input = getEntryEditorInput( element );
        IEntry entry = getEntryEditorInput( element ).getSharedWorkingCopy( editor );
        IDocument document = new Document();
        if ( entry != null )
        {
            setDocumentInput( document, entry );
        }
        setupDocument( document );
        return document;
    }


    // ── C-3PO CASTS THE SEALED ENVELOPE TO A TYPED DOSSIER ───────────────────
    // C-3PO removes the wax seal and checks: is this the right kind of document?
    // If so, he hands it over in typed form. If not, he reports the discrepancy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Casts the given element to {@link EntryEditorInput}, throwing if it isn't one.
     * Used internally to safely extract the typed input from the generic {@code Object} parameter
     * that Eclipse's document provider API uses throughout.
     *
     * @param element  The element to cast.
     * @return the element cast to {@link EntryEditorInput}.
     * @throws CoreException if {@code element} is not an {@link EntryEditorInput}.
     */
    private EntryEditorInput getEntryEditorInput( Object element ) throws CoreException
    {
        if ( element instanceof EntryEditorInput )
        {
            EntryEditorInput input = ( EntryEditorInput ) element;
            return input;
        }
        else
        {
            throw new CoreException( new Status( IStatus.ERROR, BrowserUIConstants.PLUGIN_ID,
                "Expected EntryEditorInput, was " + element ) ); //$NON-NLS-1$
        }
    }


    // ── C-3PO CHECKS IF HE CAN WRITE TO THE BIG SCREEN ──────────────────────
    // C-3PO confirms whether the display screen is unlocked and ready to receive
    // new content — if the working copy is null, there's nothing to edit.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the document for the given element is editable.
     * The LDIF editor is modifiable only when the entry's shared working copy exists —
     * if the entry hasn't been loaded yet (e.g., still fetching from the server),
     * we make the text read-only to prevent the user editing against a stale base.
     *
     * @param element  The editor input element.
     * @return {@code true} if the working copy is non-null and editing is safe.
     */
    @Override
    public boolean isModifiable( Object element )
    {
        if ( element instanceof EntryEditorInput )
        {
            EntryEditorInput editorInput = ( EntryEditorInput ) element;
            IEntry entry = editorInput.getSharedWorkingCopy( editor );
            return ( entry != null );
        }

        return false;
    }
}
