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


import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.text.LdifPartitionScanner;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;


// ── CLASS: LdifDocumentSetupParticipant — RELAY STATION ANTENNA INSTALLER ────
// Before the Alliance relay station can start routing transmissions the
// antenna array must be bolted on.  The partitioner is that antenna: it tells
// Eclipse where each LDIF record begins and ends so the text editor can apply
// the right syntax rules to each zone.
// LdifDocumentSetupParticipant installs a FastPartitioner backed by
// LdifPartitionScanner on any new document, but only if one is not already
// in place.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IDocumentSetupParticipant} that installs LDIF document
 * partitioning on a new {@link IDocument}.
 * Called by the Eclipse file-buffer framework when a document is first created.
 * Installs a {@link FastPartitioner} backed by a {@link LdifPartitionScanner}
 * for the {@code LDIF_RECORD} content type, but only if no partitioner is
 * already registered for that partitioning ID.
 * Think of this as the antenna installer: bolt the partitioner on once and
 * leave it running.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifDocumentSetupParticipant implements IDocumentSetupParticipant
{
    /** The LDIF Partitioning ID */
    public final static String LDIF_PARTITIONING = LdifEditorConstants.LDIF_PARTITIONING;


    // ── CONSTRUCT THE SETUP PARTICIPANT ───────────────────────────────────────
    /**
     * Creates a new {@code LdifDocumentSetupParticipant}.
     */
    public LdifDocumentSetupParticipant()
    {
    }


    // ── INSTALL THE PARTITIONER ───────────────────────────────────────────────
    // The installer checks whether the antenna is already in place and only
    // bolts on a new one if the slot is empty.
    /**
     * {@inheritDoc}
     *
     * <p>If {@code document} supports {@link IDocumentExtension3} and no
     * partitioner is yet registered for {@link #LDIF_PARTITIONING}, creates a
     * new {@link FastPartitioner} and connects it.</p>
     */
    public void setup( IDocument document )
    {

        if ( document instanceof IDocumentExtension3 )
        {
            IDocumentExtension3 extension3 = ( IDocumentExtension3 ) document;
            if ( extension3.getDocumentPartitioner( LdifDocumentSetupParticipant.LDIF_PARTITIONING ) == null )
            {
                IDocumentPartitioner partitioner = createDocumentPartitioner();
                extension3.setDocumentPartitioner( LDIF_PARTITIONING, partitioner );
                partitioner.connect( document );
            }
        }
    }


    // ── FACTORY: CREATE THE PARTITIONER ──────────────────────────────────────
    // The installer assembles the specific antenna model for LDIF records.
    /**
     * Creates a {@link FastPartitioner} backed by a {@link LdifPartitionScanner}
     * recognising the {@code LDIF_RECORD} content type.
     *
     * @return the new document partitioner
     */
    private IDocumentPartitioner createDocumentPartitioner()
    {
        IDocumentPartitioner partitioner = new FastPartitioner( new LdifPartitionScanner(), new String[]
            { LdifPartitionScanner.LDIF_RECORD } );
        return partitioner;
    }
}
