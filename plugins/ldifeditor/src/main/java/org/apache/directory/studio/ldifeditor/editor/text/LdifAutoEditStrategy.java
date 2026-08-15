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
import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecTypeLine;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;


// ── CLASS: LdifAutoEditStrategy — C-3PO COMPLETES THE SENTENCE ────────────────
// When C-3PO is helping write a modify communiqué he knows that after the
// last attribute-value line of a mod-spec, the next line should start with the
// same attribute name — so he pre-fills it before the operator even types.
// LdifAutoEditStrategy does the same: when the smart-insert preference is on
// and the operator presses Enter inside a change-modify mod-spec, it appends
// the mod-spec's attribute name (followed by ": ") to the new-line text so the
// next attr-val line is pre-populated.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IAutoEditStrategy} for the LDIF editor.
 * When the "smart insert attribute in mod-spec" preference is enabled and the
 * operator presses Enter inside a {@link LdifChangeModifyRecord}'s
 * {@link LdifModSpec}, pre-fills the next line with the mod-spec's attribute
 * description so the operator does not have to re-type it.
 * Think of this as C-3PO pre-filling the attribute name for the operator.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifAutoEditStrategy implements IAutoEditStrategy
{

    /** The LDIF editor whose model we inspect. */
    private ILdifEditor editor;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new auto-edit strategy for {@code editor}.
     *
     * @param editor  the LDIF editor this strategy is attached to
     */
    public LdifAutoEditStrategy( ILdifEditor editor )
    {
        this.editor = editor;
    }


    // ── PRE-FILL THE ATTRIBUTE NAME ON ENTER ─────────────────────────────────
    // C-3PO checks whether we are inside a mod-spec and the operator just
    // pressed Enter; if so he appends the attribute name to the command text.
    /**
     * {@inheritDoc}
     *
     * <p>If the smart-insert preference is on, the command is a line-delimiter
     * insertion ({@code c.length == 0} and {@code c.text} ends with a legal
     * line delimiter), and the caret is inside a {@link LdifModSpec} of a
     * {@link LdifChangeModifyRecord}, appends
     * {@code <attribute>: } to {@code c.text} so the next line begins with the
     * mod-spec attribute name.</p>
     */
    public void customizeDocumentCommand( IDocument d, DocumentCommand c )
    {

        LdifFile model = editor.getLdifModel();
        LdifContainer container = LdifFile.getContainer( model, c.offset );
        LdifContainer innerContainer = container != null ? LdifFile.getInnerContainer( container, c.offset ) : null;
        LdifPart part = container != null ? LdifFile.getContainerContent( container, c.offset ) : null;

        boolean smartInsertAttributeInModSpec = LdifEditorActivator.getDefault().getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_SMARTINSERTATTRIBUTEINMODSPEC );
        if ( smartInsertAttributeInModSpec )
        {
            if ( c.length == 0 && c.text != null && TextUtilities.endsWith( d.getLegalLineDelimiters(), c.text ) != -1 )
            {

                if ( container instanceof LdifChangeModifyRecord && innerContainer instanceof LdifModSpec
                    && ( part instanceof LdifAttrValLine || part instanceof LdifModSpecTypeLine ) )
                {
                    LdifModSpec modSpec = ( LdifModSpec ) innerContainer;
                    String att = modSpec.getModSpecType().getUnfoldedAttributeDescription();
                    c.text += att + ": "; //$NON-NLS-1$
                }
            }
        }

        boolean autoWrap = LdifEditorActivator.getDefault().getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FORMATTER_AUTOWRAP );

        if ( autoWrap )
        {

        }

    }

}
