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

package org.apache.directory.studio.ldifeditor.editor.text;


import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;


// ── CLASS: LdifAnnotationHover — HAN SOLO READS THE MARGIN WARNING ────────────
// Han glances at the blinking warning light in the instrument cluster,
// looks up what that code means, and tells the crew in plain language
// what has gone wrong on that particular panel.
// LdifAnnotationHover does the same: given the line number the user hovered
// over, it looks up the container and the invalid part at that offset and
// returns a plain-text explanation for the gutter tooltip.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IAnnotationHover} for the LDIF editor ruler area.
 * When the user hovers over a line in the left ruler, returns the combined
 * invalid-string message from the {@link LdifContainer} and the specific
 * {@link LdifPart} at that line's offset.
 * Returns {@code null} if there is nothing wrong at that line.
 * Think of this as Han Solo reading the instrument cluster warning and
 * explaining it out loud.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifAnnotationHover implements IAnnotationHover
{

    /** The LDIF editor whose model we look up annotations in. */
    private ILdifEditor editor;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new annotation hover for {@code editor}.
     *
     * @param editor  the LDIF editor whose parsed model to query
     */
    public LdifAnnotationHover( ILdifEditor editor )
    {
        this.editor = editor;
    }


    // ── RETURN THE HOVER TEXT FOR A LINE ─────────────────────────────────────
    // Han reads the blinking code, translates it, and reports back.
    /**
     * {@inheritDoc}
     *
     * <p>Converts {@code lineNumber} to a document offset, finds the enclosing
     * {@link LdifContainer} and the specific {@link LdifPart} at that offset,
     * and returns
     * {@code container.getInvalidString() + " - " + part.getInvalidString()}.
     * Returns {@code null} on {@link BadLocationException} or if no container
     * or part is found.</p>
     *
     * @param sourceViewer  the source viewer
     * @param lineNumber    the zero-based line number the user hovered over
     * @return              a plain-text error message, or {@code null}
     */
    public String getHoverInfo( ISourceViewer sourceViewer, int lineNumber )
    {

        try
        {
            if ( this.editor != null )
            {

                int offset = sourceViewer.getDocument().getLineOffset( lineNumber );
                LdifContainer container = LdifFile.getContainer( this.editor.getLdifModel(), offset );
                if ( container != null )
                {
                    LdifPart part = LdifFile.getContainerContent( container, offset );
                    if ( part != null )
                    {
                        // return container.getClass().getName() + " - " +
                        // part.getClass().getName();
                        return container.getInvalidString() + " - " + part.getInvalidString(); //$NON-NLS-1$
                    }
                }
            }
        }
        catch ( BadLocationException e )
        {
        }

        return null;
    }

}
