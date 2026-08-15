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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifInvalidPart;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModDnRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifInvalidContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifSepContainer;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;


// ── CLASS: LdifCompletionProcessor — R2-D2 SUGGESTS ESCAPE ROUTES ────────────
// R2-D2 scans the current corridor context (which container type are we in?
// what mod-spec are we inside? what prefix has the operator typed?) and then
// pops up a ranked list of the best routes forward: changetype keywords,
// attribute names from the schema, mod-spec attribute completions, and template
// snippets.
// LdifCompletionProcessor extends TemplateCompletionProcessor to provide all of
// that: context-specific proposals assembled from the parsed LDIF model and the
// connected schema, merged with template proposals and a comment prefix.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link TemplateCompletionProcessor} for the LDIF editor.
 * Computes {@link ICompletionProposal}s based on the parsed model context at the
 * caret offset:
 * <ul>
 *   <li>changetype keywords ({@code add}, {@code modify}, {@code delete},
 *       {@code moddn}) when inside a record that lacks a changetype;</li>
 *   <li>mod-dn field names ({@code newrdn}, {@code deleteoldrdn},
 *       {@code newsuperior}) when inside a {@link LdifChangeModDnRecord};</li>
 *   <li>the mod-spec attribute name when inside a {@link LdifModSpec};</li>
 *   <li>schema attribute names when inside a content or add record;</li>
 *   <li>a comment prefix when the caret is at the start of a line;</li>
 *   <li>template proposals from the plugin's template store.</li>
 * </ul>
 * Think of this as R2-D2 scanning the surrounding context and presenting ranked
 * escape routes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifCompletionProcessor extends TemplateCompletionProcessor
{

    // private final static String Dn = "dn: ";
    /** changetype: add keyword + line separator. */
    private final static String CT_ADD = "changetype: add" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$

    /** changetype: modify keyword + line separator. */
    private final static String CT_MODIFY = "changetype: modify" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$

    /** changetype: delete keyword + line separator. */
    private final static String CT_DELETE = "changetype: delete" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$

    /** changetype: moddn keyword + line separator. */
    private final static String CT_MODDN = "changetype: moddn" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$

    /** newrdn: field prefix. */
    private final static String MD_NEWRDN = "newrdn: "; //$NON-NLS-1$

    /** deleteoldrdn: 1 field. */
    private final static String MD_DELETEOLDRDN_TRUE = "deleteoldrdn: 1"; //$NON-NLS-1$

    // private final static String MD_DELETEOLDRDN_FALSE = "deleteoldrdn: 0";
    /** newsuperior: field prefix. */
    private final static String MD_NEWSUPERIOR = "newsuperior: "; //$NON-NLS-1$

    /** The LDIF editor whose model and connection we use. */
    private final ILdifEditor editor;

    /** The content assistant that drives auto-insert behaviour. */
    private final ContentAssistant contentAssistant;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new completion processor for {@code editor}.
     *
     * @param editor            the LDIF editor
     * @param contentAssistant  the content assistant (used to toggle auto-insert)
     */
    public LdifCompletionProcessor( ILdifEditor editor, ContentAssistant contentAssistant )
    {
        this.editor = editor;
        this.contentAssistant = contentAssistant;
    }


    // ── COMPUTE PROPOSALS ────────────────────────────────────────────────────
    // R2 scans the context, assembles candidate routes, and returns the list.
    /**
     * {@inheritDoc}
     *
     * <p>Re-reads auto-insert, auto-activation, and delay preferences before
     * computing proposals.  Merges template proposals, changetype keywords,
     * moddn fields, mod-spec attribute, schema attribute names, and a comment
     * prefix into a single array.</p>
     */
    public ICompletionProposal[] computeCompletionProposals( ITextViewer viewer, int offset )
    {

        IPreferenceStore store = LdifEditorActivator.getDefault().getPreferenceStore();
        contentAssistant.enableAutoInsert( store
            .getBoolean( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_INSERTSINGLEPROPOSALAUTO ) );
        contentAssistant.enableAutoActivation( store
            .getBoolean( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_ENABLEAUTOACTIVATION ) );
        contentAssistant.setAutoActivationDelay( store
            .getInt( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_AUTOACTIVATIONDELAY ) );

        List<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>();

        LdifFile model = editor.getLdifModel();
        LdifContainer container = LdifFile.getContainer( model, offset );
        LdifContainer innerContainer = container != null ? LdifFile.getInnerContainer( container, offset ) : null;
        LdifPart part = container != null ? LdifFile.getContainerContent( container, offset ) : null;
        int documentLine = -1;
        int documentLineOffset = -1;
        String prefix = ""; //$NON-NLS-1$
        try
        {
            documentLine = viewer.getDocument().getLineOfOffset( offset );
            documentLineOffset = viewer.getDocument().getLineOffset( documentLine );
            prefix = viewer.getDocument().get( documentLineOffset, offset - documentLineOffset );
        }
        catch ( BadLocationException e )
        {
        }
        // TemplateContextType contextType = getContextType(viewer, new
        // Region(offset, 0));

        // Add context dependend template proposals
        ICompletionProposal[] templateProposals = super.computeCompletionProposals( viewer, offset );
        if ( templateProposals != null )
        {
            proposalList.addAll( Arrays.asList( templateProposals ) );
        }

        // changetype: xxx
        if ( container instanceof LdifRecord )
        {
            LdifRecord record = ( LdifRecord ) container;
            LdifPart[] parts = record.getParts();
            if ( parts.length > 1 && ( !( parts[1] instanceof LdifChangeTypeLine ) || !parts[1].isValid() ) )
            {
                if ( CT_ADD.startsWith( prefix ) )
                    proposalList.add( new CompletionProposal( CT_ADD, offset - prefix.length(), prefix.length(), CT_ADD
                        .length(), LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_ADD ),
                        CT_ADD.substring( 0, CT_ADD.length() - BrowserCoreConstants.LINE_SEPARATOR.length() ), null,
                        null ) );
                if ( CT_MODIFY.startsWith( prefix ) )
                    proposalList.add( new CompletionProposal( CT_MODIFY, offset - prefix.length(), prefix.length(),
                        CT_MODIFY.length(), LdifEditorActivator.getDefault().getImage(
                            LdifEditorConstants.IMG_LDIF_MODIFY ), CT_MODIFY.substring( 0, CT_MODIFY.length()
                            - BrowserCoreConstants.LINE_SEPARATOR.length() ), null, null ) );
                if ( CT_DELETE.startsWith( prefix ) )
                    proposalList.add( new CompletionProposal( CT_DELETE, offset - prefix.length(), prefix.length(),
                        CT_DELETE.length(), LdifEditorActivator.getDefault().getImage(
                            LdifEditorConstants.IMG_LDIF_DELETE ), CT_DELETE.substring( 0, CT_DELETE.length()
                            - BrowserCoreConstants.LINE_SEPARATOR.length() ), null, null ) );
                if ( CT_MODDN.startsWith( prefix ) )
                    proposalList.add( new CompletionProposal( CT_MODDN, offset - prefix.length(), prefix.length(),
                        CT_MODDN.length(), LdifEditorActivator.getDefault().getImage(
                            LdifEditorConstants.IMG_LDIF_RENAME ), CT_MODDN.substring( 0, CT_MODDN.length()
                            - BrowserCoreConstants.LINE_SEPARATOR.length() ), null, null ) );
            }

        }

        // changetype: modify
        if ( container instanceof LdifChangeModDnRecord )
        {
            LdifChangeModDnRecord record = ( LdifChangeModDnRecord ) container;
            if ( ( record.getNewrdnLine() == null || !record.getNewrdnLine().isValid() )
                && MD_NEWRDN.startsWith( prefix ) )
            {
                proposalList.add( new CompletionProposal( MD_NEWRDN, offset - prefix.length(), prefix.length(),
                    MD_NEWRDN.length(), null, null, null, null ) );
            }
            if ( ( record.getDeloldrdnLine() == null || !record.getDeloldrdnLine().isValid() )
                && MD_DELETEOLDRDN_TRUE.startsWith( prefix ) )
            {
                proposalList.add( new CompletionProposal( MD_DELETEOLDRDN_TRUE, offset - prefix.length(), prefix
                    .length(), MD_DELETEOLDRDN_TRUE.length(), null, null, null, null ) );
            }
            if ( ( record.getNewsuperiorLine() == null || !record.getNewsuperiorLine().isValid() )
                && MD_NEWSUPERIOR.startsWith( prefix ) )
            {
                proposalList.add( new CompletionProposal( MD_NEWSUPERIOR, offset - prefix.length(), prefix.length(),
                    MD_NEWSUPERIOR.length(), null, null, null, null ) );
            }
        }

        // modspecs
        if ( innerContainer instanceof LdifModSpec )
        {
            LdifModSpec modSpec = ( LdifModSpec ) innerContainer;
            String att = modSpec.getModSpecType().getRawAttributeDescription();
            if ( att != null && att.startsWith( prefix ) )
            {
                proposalList.add( new CompletionProposal( att, offset - prefix.length(), prefix.length(), att.length(),
                    null, null, null, null ) );
            }
        }

        // attribute descriptions
        if ( container instanceof LdifContentRecord || container instanceof LdifChangeAddRecord )
        {

            if ( part instanceof LdifInvalidPart
                || part instanceof LdifAttrValLine
                || ( part instanceof LdifSepLine && ( container instanceof LdifContentRecord || container instanceof LdifChangeAddRecord ) ) )
            {

                String rawAttributeDescription = prefix;
                String rawValueType = ""; //$NON-NLS-1$

                if ( part instanceof LdifAttrValLine )
                {
                    LdifAttrValLine line = ( LdifAttrValLine ) part;
                    rawAttributeDescription = line.getRawAttributeDescription();
                    rawValueType = line.getRawValueType();
                }

                if ( offset <= part.getOffset() + rawAttributeDescription.length() )
                {
                    Schema schema = editor.getConnection() != null ? editor.getConnection().getSchema()
                        : Schema.DEFAULT_SCHEMA;
                    String[] attributeNames = SchemaUtils.getNamesAsArray( schema.getAttributeTypeDescriptions() );
                    Arrays.sort( attributeNames );
                    for ( String attributeName : attributeNames )
                    {
                        if ( rawAttributeDescription.length() == 0
                            || Strings.toLowerCase( attributeName ).startsWith(
                                Strings.toLowerCase( rawAttributeDescription ) ) )
                        {

                            String proposal = attributeName;

                            if ( rawValueType.length() == 0 )
                            {
                                if ( SchemaUtils.isBinary( schema.getAttributeTypeDescription( proposal ), schema ) )
                                {
                                    proposal += ":: "; //$NON-NLS-1$
                                }
                                else
                                {
                                    proposal += ": "; //$NON-NLS-1$
                                }
                            }

                            proposalList
                                .add( new CompletionProposal( proposal, offset - rawAttributeDescription.length(),
                                    rawAttributeDescription.length(), proposal.length() ) );
                        }
                    }
                }
            }
        }

        // comment
        boolean commentOnly = false;
        if ( documentLineOffset == offset )
        {
            commentOnly = proposalList.isEmpty();
            proposalList.add( new CompletionProposal( "# ", offset, 0, 2, LdifEditorActivator.getDefault().getImage( //$NON-NLS-1$
                LdifEditorConstants.IMG_LDIF_COMMENT ), "# - Comment", null, null ) ); //$NON-NLS-1$
        }

        // adjust auto-insert
        this.contentAssistant.enableAutoInsert( !commentOnly );

        ICompletionProposal[] proposals = ( ICompletionProposal[] ) proposalList.toArray( new ICompletionProposal[0] );
        return proposals;

    }


    // ── EXTRACT THE LINE PREFIX ───────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the text from the start of the line containing {@code offset}
     * to {@code offset}, which is the full prefix typed so far on that line.</p>
     */
    protected String extractPrefix( ITextViewer viewer, int offset )
    {

        IDocument document = viewer.getDocument();
        if ( offset > document.getLength() )
            return ""; //$NON-NLS-1$

        try
        {
            int documentLine = viewer.getDocument().getLineOfOffset( offset );
            int documentLineOffset = viewer.getDocument().getLineOffset( documentLine );
            String prefix = viewer.getDocument().get( documentLineOffset, offset - documentLineOffset );
            return prefix;
        }
        catch ( BadLocationException e )
        {
            return ""; //$NON-NLS-1$
        }
    }


    // ── CONTEXT INFORMATION ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} — context information is not provided.</p>
     */
    public IContextInformation[] computeContextInformation( ITextViewer viewer, int offset )
    {
        return null;
    }


    // ── AUTO-ACTIVATION CHARACTERS ───────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns all 52 ASCII letters plus {@code ':'} as auto-activation
     * characters so content assist triggers on any letter or colon keystroke.</p>
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {

        char[] chars = new char[53];
        for ( int i = 0; i < 26; i++ )
            chars[i] = ( char ) ( 'a' + i );
        for ( int i = 0; i < 26; i++ )
            chars[i + 26] = ( char ) ( 'A' + i );
        chars[52] = ':';

        return chars;
    }


    // ── CONTEXT-INFORMATION AUTO-ACTIVATION ───────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} — context-information auto-activation is not
     * used.</p>
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }


    // ── ERROR MESSAGE ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} — no error message is shown.</p>
     */
    public String getErrorMessage()
    {
        return null;
    }


    // ── CONTEXT-INFORMATION VALIDATOR ─────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} — context-information validation is not used.</p>
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }


    // ── RETURN TEMPLATES FOR CONTEXT TYPE ────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns templates from the plugin's template store for
     * {@code contextTypeId}.</p>
     */
    protected Template[] getTemplates( String contextTypeId )
    {
        Template[] templates = LdifEditorActivator.getDefault().getLdifTemplateStore().getTemplates( contextTypeId );
        return templates;
    }


    // ── DETERMINE THE TEMPLATE CONTEXT TYPE ──────────────────────────────────
    // R2 figures out where the caret is and picks the matching template bucket.
    /**
     * {@inheritDoc}
     *
     * <p>Maps the current caret context to one of the LDIF template context
     * type IDs:
     * <ul>
     *   <li>{@code LDIF_FILE_TEMPLATE_ID} — top-level / separator context;</li>
     *   <li>{@code LDIF_MODIFICATION_RECORD_TEMPLATE_ID} — inside a modify
     *       record but not in a mod-spec;</li>
     *   <li>{@code LDIF_MODIFICATION_ITEM_TEMPLATE_ID} — inside a mod-spec;</li>
     *   <li>{@code LDIF_MODDN_RECORD_TEMPLATE_ID} — inside a moddn record;</li>
     *   <li>{@code null} — no template context applies.</li>
     * </ul>
     * </p>
     */
    protected TemplateContextType getContextType( ITextViewer viewer, IRegion region )
    {

        int offset = region.getOffset();

        LdifFile model = editor.getLdifModel();
        LdifContainer container = LdifFile.getContainer( model, offset );
        LdifContainer innerContainer = container != null ? LdifFile.getInnerContainer( container, offset ) : null;
        LdifPart part = container != null ? LdifFile.getContainerContent( container, offset ) : null;
        int documentLine = -1;
        int documentLineOffset = -1;
        String prefix = ""; //$NON-NLS-1$
        try
        {
            documentLine = viewer.getDocument().getLineOfOffset( offset );
            documentLineOffset = viewer.getDocument().getLineOffset( documentLine );
            prefix = viewer.getDocument().get( documentLineOffset, offset - documentLineOffset );
        }
        catch ( BadLocationException e )
        {
        }

        // FILE
        if ( container == null && innerContainer == null && part == null )
        {
            return LdifEditorActivator.getDefault().getLdifTemplateContextTypeRegistry().getContextType(
                LdifEditorConstants.LDIF_FILE_TEMPLATE_ID );
        }
        if ( container instanceof LdifSepContainer && innerContainer == null && part instanceof LdifSepLine )
        {
            return LdifEditorActivator.getDefault().getLdifTemplateContextTypeRegistry().getContextType(
                LdifEditorConstants.LDIF_FILE_TEMPLATE_ID );
        }
        if ( ( container instanceof LdifInvalidContainer && part instanceof LdifInvalidPart && "d".equals( prefix ) ) //$NON-NLS-1$
            || ( container instanceof LdifContentRecord && part instanceof LdifInvalidPart && "dn".equals( prefix ) ) //$NON-NLS-1$
            || ( container instanceof LdifContentRecord && part instanceof LdifInvalidPart && "dn:".equals( prefix ) ) ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getLdifTemplateContextTypeRegistry().getContextType(
                LdifEditorConstants.LDIF_FILE_TEMPLATE_ID );
        }

        // MODIFICATION RECORD
        if ( container instanceof LdifChangeModifyRecord && innerContainer == null
            && ( part instanceof LdifSepLine || part instanceof LdifInvalidPart ) )
        {
            return LdifEditorActivator.getDefault().getLdifTemplateContextTypeRegistry().getContextType(
                LdifEditorConstants.LDIF_MODIFICATION_RECORD_TEMPLATE_ID );
        }

        // MODIFICATION ITEM
        if ( container instanceof LdifChangeModifyRecord && innerContainer instanceof LdifModSpec )
        {
            return LdifEditorActivator.getDefault().getLdifTemplateContextTypeRegistry().getContextType(
                LdifEditorConstants.LDIF_MODIFICATION_ITEM_TEMPLATE_ID );
        }

        // MODDN RECORD
        if ( container instanceof LdifChangeModDnRecord && innerContainer == null
            && ( part instanceof LdifSepLine || part instanceof LdifInvalidPart ) )
        {
            return LdifEditorActivator.getDefault().getLdifTemplateContextTypeRegistry().getContextType(
                LdifEditorConstants.LDIF_MODDN_RECORD_TEMPLATE_ID );
        }

        // TemplateContextType contextType =
        // Activator.getDefault().getContextTypeRegistry().getContextType(LdifEditorConstants.LDIF_FILE_TEMPLATE_ID);
        // TemplateContextType contextType =
        // Activator.getDefault().getContextTypeRegistry().getContextType(LdifEditorConstants.LDIF_MODIFICATION_RECORD_TEMPLATE_ID);

        return null;

    }


    // ── ICON FOR A TEMPLATE ───────────────────────────────────────────────────
    // The icon of a template proposal depends on what changetype keyword or
    // operation type appears in its body.
    /**
     * {@inheritDoc}
     *
     * <p>Returns an image based on the first changetype or mod-op keyword found
     * in the template pattern (add/modify/delete/moddn/dn), or the generic
     * template icon if none is found.</p>
     */
    protected Image getImage( Template template )
    {

        if ( template.getPattern().indexOf( "add: " ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MOD_ADD );
        }
        else if ( template.getPattern().indexOf( "replace: " ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MOD_REPLACE );
        }
        else if ( template.getPattern().indexOf( "delete: " ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MOD_DELETE );
        }

        else if ( template.getPattern().indexOf( "changetype: add" ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_ADD );
        }
        else if ( template.getPattern().indexOf( "changetype: modify" ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_MODIFY );
        }
        else if ( template.getPattern().indexOf( "changetype: delete" ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_DELETE );
        }
        else if ( template.getPattern().indexOf( "changetype: moddn" ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_LDIF_RENAME );
        }
        else if ( template.getPattern().indexOf( "dn: " ) > -1 ) //$NON-NLS-1$
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_ENTRY );
        }

        else
        {
            return LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_TEMPLATE );
        }

    }

}
