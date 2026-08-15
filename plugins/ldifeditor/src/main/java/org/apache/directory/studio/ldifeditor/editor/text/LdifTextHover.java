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
import org.apache.directory.studio.ldifparser.model.lines.LdifValueLineBase;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;


// ── CLASS: LdifTextHover — HAN SOLO READS THE ENCRYPTED ANNOTATION ────────────
// Han Solo hovers over a field label on the Falcon's instrument panel and a
// tooltip pops up showing the decoded Base64 value beneath the raw encoded
// string — handy when you need to know what "aGVsbG8=" actually says.
// LdifTextHover does the same: when the cursor rests on a Base64-encoded
// LDIF value line, it returns the decoded string as the hover tooltip text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link ITextHover} for the LDIF editor.
 * When the cursor hovers over a {@link LdifValueLineBase} that uses Base64
 * encoding (value-type {@code "::"}) this class returns the decoded string as
 * the hover tooltip.  For all other positions it returns {@code null} so
 * Eclipse shows nothing.
 * Think of this as Han Solo's instrument panel tooltip that decodes encrypted
 * field annotations on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifTextHover implements ITextHover
{

    /** The LDIF editor whose model we query for the hovered part. */
    private ILdifEditor editor;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new text hover for the given {@code editor}.
     *
     * @param editor  the LDIF editor whose model to inspect
     */
    public LdifTextHover( ILdifEditor editor )
    {
        this.editor = editor;
    }


    // ── RETURN DECODED VALUE AS HOVER TEXT ────────────────────────────────────
    // Find the LDIF part under the hover region; if it's a Base64 value line,
    // return the decoded string so the user can read the human-readable value.
    /**
     * {@inheritDoc}
     *
     * <p>Looks up the {@link LdifContainer} and {@link LdifPart} at
     * {@code hoverRegion.getOffset()} in the editor's LDIF model.  If the
     * part is a {@link LdifValueLineBase} with a Base64 value type, returns
     * {@link LdifValueLineBase#getValueAsString()}; otherwise returns
     * {@code null}.</p>
     */
    public String getHoverInfo( ITextViewer textViewer, IRegion hoverRegion )
    {

        if ( this.editor != null )
        {

            LdifContainer container = LdifFile.getContainer( this.editor.getLdifModel(), hoverRegion.getOffset() );
            if ( container != null )
            {
                LdifPart part = LdifFile.getContainerContent( container, hoverRegion.getOffset() );
                if ( part != null )
                {
                    if ( part instanceof LdifValueLineBase )
                    {
                        LdifValueLineBase line = ( LdifValueLineBase ) part;
                        if ( line.isValueTypeBase64() )
                        {
                            return line.getValueAsString();
                        }
                    }
                }
            }
        }

        return null;
    }


    // ── RETURN A ZERO-LENGTH REGION FOR THE HOVER ─────────────────────────────
    // Eclipse calls this first to ask "is there something to hover here?";
    // we return a zero-length region at the caret to trigger getHoverInfo().
    /**
     * {@inheritDoc}
     *
     * <p>Returns a zero-length {@link Region} at {@code offset} so Eclipse
     * calls {@link #getHoverInfo} for every cursor position.  Returns
     * {@code null} if the editor reference is gone.</p>
     */
    public IRegion getHoverRegion( ITextViewer textViewer, int offset )
    {

        if ( this.editor != null )
        {
            return new Region( offset, 0 );
        }

        return null;
    }

}
