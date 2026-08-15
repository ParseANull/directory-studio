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


import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifparser.model.LdifEOFPart;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifInvalidPart;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.lines.LdifLineBase;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifValueLineBase;
import org.apache.directory.studio.ldifparser.parser.LdifParser;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;


// ── CLASS: LdifDoubleClickStrategy — REBEL OPERATOR DOUBLE-TAPS THE FIELD ─────
// The Rebel operator double-taps a field label on the console to select just
// the semantic segment they need: the attribute name, the value-type separator,
// or the value itself — not the whole line with its trailing newline.
// LdifDoubleClickStrategy implements that smart selection: it parses the
// current partition, finds which token sub-segment the caret is in, and
// selects only that segment on double-click.  If the "LDIF double-click"
// preference is off, it falls back to the standard word-break strategy.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link ITextDoubleClickStrategy} for the LDIF editor.
 * When the "LDIF double-click" preference is enabled, parses the current
 * partition and selects the logical sub-segment (attribute, value-type, or
 * value) that the caret is within.  Falls back to
 * {@link DefaultTextDoubleClickStrategy} when the preference is off or when
 * the caret is on a separator or invalid part.
 * Think of this as the Rebel operator double-tapping to select a semantic field
 * segment rather than a plain word.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifDoubleClickStrategy implements ITextDoubleClickStrategy
{

    /** Index constant for the offset element of a range array. */
    private static final int OFFSET = 0;

    /** Index constant for the length element of a range array. */
    private static final int LENGTH = 1;

    /**
     * Default double-click strategy used as fallback.
     */
    private DefaultTextDoubleClickStrategy delegateDoubleClickStrategy;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new LDIF double-click strategy, also constructing the fallback
     * {@link DefaultTextDoubleClickStrategy}.
     */
    public LdifDoubleClickStrategy()
    {
        this.delegateDoubleClickStrategy = new DefaultTextDoubleClickStrategy();
    }


    // ── HANDLE DOUBLE-CLICK ───────────────────────────────────────────────────
    // If the smart-click preference is on, parse the partition and select the
    // right sub-segment; otherwise delegate to the default strategy.
    /**
     * {@inheritDoc}
     *
     * <p>If the {@code PREFERENCE_LDIFEDITOR_DOUBLECLICK_USELDIFDOUBLECLICK}
     * preference is {@code false}, delegates to the default strategy.
     * Otherwise parses the current partition with a fresh {@link LdifParser},
     * locates the {@link LdifPart} at the cursor, and selects the sub-segment
     * (line start, value type, or value) that contains the cursor.  Falls back
     * to the default strategy for separators, invalid parts, and EOF.</p>
     */
    public void doubleClicked( ITextViewer viewer )
    {

        if ( !LdifEditorActivator.getDefault().getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_DOUBLECLICK_USELDIFDOUBLECLICK ) )
        {
            delegateDoubleClickStrategy.doubleClicked( viewer );
        }
        else
        {

            int cursorPos = viewer.getSelectedRange().x;
            if ( cursorPos < 0 )
            {
                return;
            }

            try
            {
                LdifParser parser = new LdifParser();
                IDocument document = viewer.getDocument();
                ITypedRegion partition = document.getPartition( cursorPos );

                // now use position relative to partition
                int offset = partition.getOffset();
                int relativePos = cursorPos - offset;

                // parse partition
                String s = document.get( partition.getOffset(), partition.getLength() );
                LdifFile model = parser.parse( s );
                LdifContainer container = LdifFile.getContainer( model, relativePos );
                if ( container != null )
                {
                    LdifPart part = LdifFile.getContainerContent( container, relativePos );

                    if ( part != null && !( part instanceof LdifSepLine ) && !( part instanceof LdifInvalidPart )
                        && !( part instanceof LdifEOFPart ) )
                    {

                        // calculate selected range
                        int[] range = null;
                        if ( part instanceof LdifValueLineBase )
                        {
                            LdifValueLineBase line = ( LdifValueLineBase ) part;
                            range = getRange( relativePos, part.getOffset(), new String[]
                                { line.getRawLineStart(), line.getRawValueType(), line.getRawValue() } );
                        }
                        else if ( part instanceof LdifLineBase )
                        {
                            LdifLineBase line = ( LdifLineBase ) part;
                            range = new int[]
                                { part.getOffset(), part.getLength() - line.getRawNewLine().length() };
                        }

                        // set range on viewer, add global offset
                        int start = range != null ? range[OFFSET] : part.getOffset();
                        start += offset;
                        int length = range != null ? range[LENGTH] : part.getLength();
                        viewer.setSelectedRange( start, length );
                    }
                    else
                    {
                        // use default double click strategy
                        delegateDoubleClickStrategy.doubleClicked( viewer );
                    }
                }

            }
            catch ( BadLocationException e )
            {
                e.printStackTrace();
            }
        }
    }


    // ── FIND THE SUB-SEGMENT RANGE ────────────────────────────────────────────
    // Walk the sub-segment lengths in order; the first one whose cumulative
    // end exceeds the cursor position is the one to select.
    /**
     * Returns the {@code [offset, length]} of the sub-segment in {@code parts}
     * that contains {@code pos}.
     *
     * @param pos     the cursor position (relative to the start of the first part)
     * @param offset  the starting offset of the first segment
     * @param parts   the ordered segment strings (may contain {@code null})
     * @return        {@code int[]{segOffset, segLength}}, or {@code null} if not found
     */
    private int[] getRange( int pos, int offset, String[] parts )
    {

        for ( int i = 0; i < parts.length; i++ )
        {
            if ( parts[i] != null )
            {
                if ( pos < offset + parts[i].length() )
                {
                    return new int[]
                        { offset, parts[i].length() };
                }
                offset += parts[i].length();
            }
        }
        return null;
    }

}
