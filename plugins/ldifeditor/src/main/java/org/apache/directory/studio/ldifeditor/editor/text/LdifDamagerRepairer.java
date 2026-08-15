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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifparser.model.LdifEOFPart;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifInvalidPart;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeDeleteRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModDnRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifModSpec;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDeloldrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifLineBase;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecSepLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifModSpecTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewsuperiorLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifVersionLine;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


// ── CLASS: LdifDamagerRepairer — C-3PO COLOUR-CODES THE TRANSMISSION ──────────
// C-3PO reads every line of the incoming communiqué and annotates each token
// with its diplomatic colour code: DN lines are one colour, attribute names
// another, keywords another, and so on — all driven by the current preference
// settings so the colour scheme can be changed at runtime.
// LdifDamagerRepairer implements both Eclipse's damage (mark what changed) and
// repair (repaint with token colours) roles — always repainting the whole
// partition so colours stay consistent after incremental edits.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IPresentationDamager} and {@link IPresentationRepairer} for
 * the LDIF editor.
 * Always damages the full partition on any change ({@link #getDamageRegion}
 * returns the partition), then walks every overlapping {@link LdifContainer}'s
 * parts and adds {@link StyleRange}s colour-coded by token type (comments, DNs,
 * attribute names, value types, values, changetype keywords, and moddn/add/
 * modify/delete type colours).
 * Think of this as C-3PO colour-coding every line of the transmission according
 * to its diplomatic token class.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifDamagerRepairer implements IPresentationDamager, IPresentationRepairer
{
    /** The LDIF editor whose model drives the colour decisions. */
    private ILdifEditor editor;

    /**
     * Optional runtime overrides: maps preference key (rgb/style suffix included)
     * to {@link RGB} or {@link Integer} for live preview.
     */
    private Map<String, Object> textAttributeKeyToValueMap;

    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new damager-repairer for {@code editor}.
     *
     * @param editor  the LDIF editor whose model and preferences to use
     */
    public LdifDamagerRepairer( ILdifEditor editor )
    {
        super();
        this.editor = editor;
    }


    // ── DOCUMENT CHANGED ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — the document reference is obtained from the model each time.</p>
     */
    public void setDocument( IDocument document )
    {
    }


    // ── MARK THE DAMAGE REGION ───────────────────────────────────────────────
    // The whole partition is always repainted; LDIF tokens can span multiple
    // lines so we cannot predict a smaller safe region.
    /**
     * {@inheritDoc}
     *
     * <p>Always returns the full {@code partition} as the damage region.</p>
     */
    public IRegion getDamageRegion( ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged )
    {
        return partition;
    }


    // ── REPAINT THE PRESENTATION ─────────────────────────────────────────────
    // C-3PO walks the model containers that overlap the damaged region and
    // adds a colour range for each token segment.
    /**
     * {@inheritDoc}
     *
     * <p>Collects all {@link LdifContainer}s whose region overlaps {@code damage},
     * then delegates to {@link #highlight} to add {@link StyleRange}s for each
     * token segment.</p>
     */
    public void createPresentation( TextPresentation presentation, ITypedRegion damage )
    {

        LdifFile ldifModel = this.editor.getLdifModel();
        List<LdifContainer> allContainers = ldifModel.getContainers();
        List<LdifContainer> containerList = new ArrayList<LdifContainer>();

        for ( LdifContainer ldifContainer : allContainers )
        {
            Region containerRegion = new Region( ldifContainer.getOffset(), ldifContainer.getLength() );

            if ( TextUtilities.overlaps( containerRegion, damage ) )
            {
                containerList.add( ldifContainer );
            }
        }

        LdifContainer[] containers = ( LdifContainer[] ) containerList
            .toArray( new LdifContainer[containerList.size()] );
        this.highlight( containers, presentation, damage );
    }


    // ── LOAD A TEXT ATTRIBUTE FROM PREFERENCES ───────────────────────────────
    /**
     * Reads the {@link TextAttribute} for {@code key} from the preference store.
     * If {@link #textAttributeKeyToValueMap} contains an override for the colour
     * or style key, that value is used instead.
     * Uses {@code null} colour when the preference value is the default-default
     * string, so system high-contrast themes are not overridden.
     *
     * @param key  the base preference key
     *             (e.g. {@code PREFERENCE_LDIFEDITOR_SYNTAX_COMMENT})
     * @return     the resolved {@link TextAttribute}
     */
    private TextAttribute getTextAttribute( String key )
    {
        IPreferenceStore store = LdifEditorActivator.getDefault().getPreferenceStore();

        String colorKey = key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX;
        String styleKey = key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX;

        RGB rgb = PreferenceConverter.getColor( store, colorKey );
        int style = store.getInt( styleKey );

        if ( textAttributeKeyToValueMap != null )
        {
            if ( textAttributeKeyToValueMap.containsKey( colorKey ) )
            {
                rgb = ( RGB ) textAttributeKeyToValueMap.get( colorKey );
            }
            if ( textAttributeKeyToValueMap.containsKey( styleKey ) )
            {
                style = ( ( Integer ) textAttributeKeyToValueMap.get( styleKey ) ).intValue();
            }
        }

        /*
         * Use color <code>null</code> if the default is the default string, This is important
         * to not override the system color when a high-contrast theme is used.
         */
        boolean isDefaultDefaultKey = IPreferenceStore.STRING_DEFAULT_DEFAULT.equals( store.getString( colorKey ) );
        Color color = isDefaultDefaultKey ? null : LdifEditorActivator.getDefault().getColor( rgb );
        TextAttribute textAttribute = new TextAttribute( color, null, style );
        return textAttribute;
    }


    // ── RUNTIME COLOUR OVERRIDE ───────────────────────────────────────────────
    // The preference page calls this to show a live preview without saving.
    /**
     * Overrides the colour and style used for {@code key} without changing
     * the preference store.  The override is stored in
     * {@link #textAttributeKeyToValueMap} and takes effect on the next repaint.
     *
     * <p>The {@code key} should be one of the
     * {@code PREFERENCE_LDIFEDITOR_SYNTAX_xxx} constants (without the
     * {@code _rgb} or {@code _style} suffix).</p>
     *
     * @param key    the base preference key
     * @param rgb    the new foreground colour
     * @param style  the new SWT font style
     */
    public void setTextAttribute( String key, RGB rgb, int style )
    {
        if ( textAttributeKeyToValueMap == null )
        {
            textAttributeKeyToValueMap = new HashMap<String, Object>();
        }
        textAttributeKeyToValueMap.put( key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX, rgb );
        textAttributeKeyToValueMap.put( key + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX,
            new Integer( style ) );
    }


    // ── WALK AND COLOUR EACH TOKEN ────────────────────────────────────────────
    // C-3PO inspects each part of each container and applies the matching
    // colour range to the presentation.
    /**
     * Adds colour {@link StyleRange}s to {@code presentation} for each part of
     * each container.  Handles: version line, comment, dn, attr-val, changetype,
     * newrdn, deleteoldrdn, newsuperior, modspec-type, modspec-sep, control, and
     * nested {@link LdifModSpec}.
     *
     * @param containers   the containers to highlight
     * @param presentation the text presentation to add style ranges to
     * @param damage       the damaged region (used when recursing into mod-specs)
     */
    private void highlight( LdifContainer[] containers, TextPresentation presentation, ITypedRegion damage )
    {
        TextAttribute COMMENT_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_COMMENT );
        TextAttribute KEYWORD_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_KEYWORD );
        TextAttribute DN_TEXT_ATTRIBUTE = getTextAttribute( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_DN );
        TextAttribute ATTRIBUTE_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_ATTRIBUTE );
        TextAttribute VALUETYPE_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUETYPE );
        TextAttribute VALUE_TEXT_ATTRIBUTE = getTextAttribute( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUE );
        TextAttribute ADD_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEADD );
        TextAttribute MODIFY_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODIFY );
        TextAttribute DELETE_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEDELETE );
        TextAttribute MODDN_TEXT_ATTRIBUTE = getTextAttribute(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODDN );

        for ( int z = 0; z < containers.length; z++ )
        {

            LdifContainer container = containers[z];

            LdifPart[] parts = container.getParts();

            for ( int i = 0; i < parts.length; i++ )
            {

                // int offset = damage.getOffset() + parts[i].getOffset();
                int offset = parts[i].getOffset();

                if ( parts[i] instanceof LdifLineBase )
                {
                    LdifLineBase line = ( LdifLineBase ) parts[i];

                    if ( line instanceof LdifVersionLine )
                    {
                        this.addStyleRange( presentation, offset, line.getLength(), KEYWORD_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifCommentLine )
                    {
                        this.addStyleRange( presentation, offset, line.getLength(), COMMENT_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifDnLine )
                    {
                        LdifDnLine dnLine = ( LdifDnLine ) line;
                        int dnSpecLength = dnLine.getRawDnSpec().length();
                        int valueTypeLength = dnLine.getRawValueType().length();
                        int dnLength = dnLine.getRawDn().length();
                        this.addStyleRange( presentation, offset, dnSpecLength, DN_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + dnSpecLength, valueTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + dnSpecLength + valueTypeLength, dnLength,
                            DN_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifAttrValLine )
                    {
                        LdifAttrValLine attrValLine = ( LdifAttrValLine ) line;
                        int attributeNameLength = attrValLine.getRawAttributeDescription().length();
                        int valueTypeLength = attrValLine.getRawValueType().length();
                        int valueLength = attrValLine.getRawValue().length();
                        this.addStyleRange( presentation, offset, attributeNameLength, ATTRIBUTE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + attributeNameLength, valueTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + attributeNameLength + valueTypeLength, valueLength,
                            VALUE_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifChangeTypeLine )
                    {
                        LdifChangeTypeLine changeTypeLine = ( LdifChangeTypeLine ) line;
                        int changeTypeSpecLength = changeTypeLine.getRawChangeTypeSpec().length();
                        int valueTypeLength = changeTypeLine.getRawValueType().length();
                        int changeTypeLength = changeTypeLine.getRawChangeType().length();
                        this.addStyleRange( presentation, offset, changeTypeSpecLength, KEYWORD_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + changeTypeSpecLength, valueTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );

                        if ( container instanceof LdifChangeAddRecord )
                        {
                            this.addStyleRange( presentation, offset + changeTypeSpecLength + valueTypeLength,
                                changeTypeLength, ADD_TEXT_ATTRIBUTE );
                        }
                        else if ( container instanceof LdifChangeModifyRecord )
                        {
                            this.addStyleRange( presentation, offset + changeTypeSpecLength + valueTypeLength,
                                changeTypeLength, MODIFY_TEXT_ATTRIBUTE );
                        }
                        else if ( container instanceof LdifChangeModDnRecord )
                        {
                            this.addStyleRange( presentation, offset + changeTypeSpecLength + valueTypeLength,
                                changeTypeLength, MODDN_TEXT_ATTRIBUTE );
                        }
                        else if ( container instanceof LdifChangeDeleteRecord )
                        {
                            this.addStyleRange( presentation, offset + changeTypeSpecLength + valueTypeLength,
                                changeTypeLength, DELETE_TEXT_ATTRIBUTE );
                        }
                    }
                    else if ( line instanceof LdifNewrdnLine )
                    {
                        LdifNewrdnLine newrdnLine = ( LdifNewrdnLine ) line;
                        int newrdnSpecLength = newrdnLine.getRawNewrdnSpec().length();
                        int valueTypeLength = newrdnLine.getRawValueType().length();
                        int newrdnLength = newrdnLine.getRawNewrdn().length();
                        this.addStyleRange( presentation, offset, newrdnSpecLength, KEYWORD_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + newrdnSpecLength, valueTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + newrdnSpecLength + valueTypeLength, newrdnLength,
                            VALUE_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifDeloldrdnLine )
                    {
                        LdifDeloldrdnLine deleteoldrdnLine = ( LdifDeloldrdnLine ) line;
                        int deleteoldrdnSpecLength = deleteoldrdnLine.getRawDeleteOldrdnSpec().length();
                        int valueTypeLength = deleteoldrdnLine.getRawValueType().length();
                        int deleteoldrdnLength = deleteoldrdnLine.getRawDeleteOldrdn().length();
                        this.addStyleRange( presentation, offset, deleteoldrdnSpecLength, KEYWORD_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + deleteoldrdnSpecLength, valueTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + deleteoldrdnSpecLength + valueTypeLength,
                            deleteoldrdnLength, VALUE_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifNewsuperiorLine )
                    {
                        LdifNewsuperiorLine newsuperiorLine = ( LdifNewsuperiorLine ) line;
                        int newsuperiorSpecLength = newsuperiorLine.getRawNewSuperiorSpec().length();
                        int valueTypeLength = newsuperiorLine.getRawValueType().length();
                        int newsuperiorLength = newsuperiorLine.getRawNewSuperiorDn().length();
                        this.addStyleRange( presentation, offset, newsuperiorSpecLength, KEYWORD_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + newsuperiorSpecLength, valueTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + newsuperiorSpecLength + valueTypeLength,
                            newsuperiorLength, VALUE_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifModSpecTypeLine )
                    {
                        LdifModSpecTypeLine modSpecTypeLine = ( LdifModSpecTypeLine ) line;
                        int modTypeLength = modSpecTypeLine.getRawModType().length();
                        int valueTypeLength = modSpecTypeLine.getRawValueType().length();
                        int attributeDescriptionLength = modSpecTypeLine.getRawAttributeDescription().length();
                        this.addStyleRange( presentation, offset, modTypeLength, KEYWORD_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + modTypeLength, valueTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + modTypeLength + valueTypeLength,
                            attributeDescriptionLength, ATTRIBUTE_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifModSpecSepLine )
                    {
                        this.addStyleRange( presentation, offset, line.getLength(), VALUETYPE_TEXT_ATTRIBUTE );
                    }
                    else if ( line instanceof LdifControlLine )
                    {
                        LdifControlLine controlLine = ( LdifControlLine ) line;
                        int controlSpecLength = controlLine.getRawControlSpec().length();
                        int controlTypeLength = controlLine.getRawControlType().length();
                        int oidLength = controlLine.getRawOid().length();
                        int critLength = controlLine.getRawCriticality().length();
                        int valueTypeLength = controlLine.getRawControlValueType().length();
                        int valueLength = controlLine.getRawControlValue().length();
                        this.addStyleRange( presentation, offset, controlSpecLength, KEYWORD_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + controlSpecLength, controlTypeLength,
                            VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + controlSpecLength + controlTypeLength, oidLength,
                            ATTRIBUTE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + controlSpecLength + controlTypeLength + oidLength,
                            critLength, KEYWORD_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + controlSpecLength + controlTypeLength + oidLength
                            + critLength, valueTypeLength, VALUETYPE_TEXT_ATTRIBUTE );
                        this.addStyleRange( presentation, offset + controlSpecLength + controlTypeLength + oidLength
                            + critLength + valueTypeLength, valueLength, VALUE_TEXT_ATTRIBUTE );
                    }
                    else
                    {
                        // this.addStyleRange(presentation, offset,
                        // line.getLength(), DEFAULT_TEXT_ATTRIBUTE);
                    }
                }
                else if ( parts[i] instanceof LdifModSpec )
                {
                    LdifModSpec modSpec = ( LdifModSpec ) parts[i];
                    this.highlight( new LdifContainer[]
                        { modSpec }, presentation, damage );

                }
                else if ( parts[i] instanceof LdifInvalidPart )
                {
                    // LdifUnknownPart unknownPart =
                    // (LdifUnknownPart)parts[i];
                    // this.addStyleRange(presentation, offset,
                    // unknownPart.getLength(), UNKNOWN_TEXT_ATTRIBUTE);
                    // this.addStyleRange(presentation, offset,
                    // parts[i].getLength(), DEFAULT_TEXT_ATTRIBUTE);
                }
                else if ( parts[i] instanceof LdifEOFPart )
                {
                    // ignore
                }
                else
                {
                    // TODO
                    System.out.println( "LdifDamagerRepairer: Unspecified Token: " + parts[i].getClass() ); //$NON-NLS-1$
                }

            }
        }

    }


    // ── ADD A STYLE RANGE TO THE PRESENTATION ────────────────────────────────
    /**
     * Adds a {@link StyleRange} to {@code presentation} if {@code offset >= 0}
     * and {@code length > 0}.
     * Translates the {@link TextAttribute} into foreground colour, background,
     * bold/italic style, underline, and strikethrough.
     *
     * @param presentation  the text presentation to update
     * @param offset        the character offset of the range
     * @param length        the length of the range
     * @param textAttribute the visual style to apply
     */
    private void addStyleRange( TextPresentation presentation, int offset, int length, TextAttribute textAttribute )
    {
        if ( offset >= 0 && length > 0 )
        {
            StyleRange range = new StyleRange( offset, length, textAttribute.getForeground(), textAttribute
                .getBackground(), textAttribute.getStyle() & ( SWT.BOLD | SWT.ITALIC ) );
            range.underline = ( textAttribute.getStyle() & TextAttribute.UNDERLINE ) != 0;
            range.strikeout = ( textAttribute.getStyle() & TextAttribute.STRIKETHROUGH ) != 0;
            presentation.addStyleRange( range );
        }
    }

}
