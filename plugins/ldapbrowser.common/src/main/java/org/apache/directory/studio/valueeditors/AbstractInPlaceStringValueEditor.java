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

package org.apache.directory.studio.valueeditors;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;


// ── CLASS: AbstractInPlaceStringValueEditor — C-3PO Whispers to Han at the Cantina ──
// In A New Hope, at the Mos Eisley cantina, Greedo and Ponda Baba are speaking
// rapid Rodian. C-3PO sidles up beside Han, leans close, and whispers a quick
// running translation directly in his ear — no formal channel, no grand ceremony,
// just fast, discreet, inline help right where Han is sitting. This class does
// the same for string LDAP values: it edits text directly in the table cell
// (via a JFace TextCellEditor) rather than opening a separate dialog. No context
// switch, no pop-up — the translation appears right in place.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for value editors that edit string-typed LDAP attribute
 * values inline — directly inside the table or tree cell, without opening a
 * dialog. We extend JFace's {@link TextCellEditor}, which gives us an SWT
 * {@code Text} widget embedded in the cell, and we implement {@link IValueEditor}
 * to plug into Directory Studio's value-editor framework.
 * Think of this class as C-3PO whispering translations to Han at the Mos Eisley
 * cantina: quick, in-place, no interruption to the flow.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractInPlaceStringValueEditor extends TextCellEditor implements IValueEditor
{
    /** The name of this value editor */
    private String name;

    /** The image of this value editor */
    private ImageDescriptor imageDescriptor;


    // ── C-3PO Sidles Up Beside Han at the Bar ────────────────────────────────
    // The moment C-3PO spots Greedo sliding into the booth across from Han, he
    // positions himself right next to Han, tunes his audio receptors to the
    // Rodian dialect, and readies his translation buffers. No formal setup,
    // no flourish — just quietly in position and ready to whisper.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new AbstractInPlaceStringValueEditor, delegating to
     * {@link TextCellEditor#TextCellEditor()} so the embedded SWT {@code Text}
     * widget infrastructure is initialized. The {@code name} and
     * {@code imageDescriptor} fields stay null until the framework sets them.
     */
    protected AbstractInPlaceStringValueEditor()
    {
        super();
    }


    // ── C-3PO Checks Whether Han Wants the Raw Rodian ────────────────────────
    // Before whispering, C-3PO glances at Han: did he ask for the raw alien
    // sounds, or does he want a clean Basic translation? The answer comes from
    // a preference setting back at the rebel base.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks the Directory Studio preference that controls whether values should
     * be shown in their raw internal form or in a formatted, user-friendly form.
     * When true, {@code getDisplayValue} methods should not beautify or summarise
     * the value; they should return it as-is.
     *
     * <p>For example — C-3PO's pre-whisper check:</p>
     * <pre>
     *   if preference PREFERENCE_SHOW_RAW_VALUES:
     *       whisper raw Rodian phonetics
     *   else:
     *       whisper clean Basic translation
     * </pre>
     *
     * @return {@code true} if raw values should be displayed as-is,
     *         {@code false} if formatting is allowed.
     */
    protected boolean showRawValues()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES );
    }


    // ── C-3PO Whispers the Whole Table's Conversation to Han ─────────────────
    // Multiple aliens are talking at once — Greedo, Ponda Baba, the bartender.
    // C-3PO listens to all of them and whispers a comma-separated summary:
    // "Greedo said X, Ponda Baba said Y, the bartender said Z."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a single display string that represents all values across an
     * {@link AttributeHierarchy}. We collect every {@link IValue}, call
     * {@link #getDisplayValue(IValue)} on each, and join the results with
     * {@code ", "}. This is the string the browser shows when the cell is not
     * being actively edited.
     *
     * <p>For example — C-3PO's multi-voice summary:</p>
     * <pre>
     *   "value1, value2, value3"
     * </pre>
     *
     * @param attributeHierarchy  The set of attributes and their values to
     *                            summarise; null returns the NULL constant.
     * @return                    A comma-separated display string, or the NULL
     *                            constant if the hierarchy is null.
     */
    @Override
    public String getDisplayValue( AttributeHierarchy attributeHierarchy )
    {
        if ( attributeHierarchy == null )
        {
            return NULL;
        }

        List<IValue> valueList = new ArrayList<>();

        for ( IAttribute attribute : attributeHierarchy )
        {
            valueList.addAll( Arrays.asList( attribute.getValues() ) );
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( IValue value : valueList )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " ); //$NON-NLS-1$
            }

            sb.append( getDisplayValue( value ) );
        }

        return sb.toString();
    }


    // ── C-3PO Whispers One Line of Greedo's Threat to Han ────────────────────
    // Greedo leans forward and says one thing in Rodian. C-3PO catches it,
    // runs it through his translation algorithms, and whispers the Basic
    // version directly into Han's ear — just that one remark, nothing more.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display string for a single {@link IValue}. We delegate to
     * {@link StringValueEditorUtils#getDisplayValue(Object)} after fetching the
     * raw value, which handles null, empty, and the raw-values preference in a
     * consistent way across all string editors.
     *
     * <p>For example — C-3PO translating one remark:</p>
     * <pre>
     *   rawValue = getRawValue(value)
     *   return StringValueEditorUtils.getDisplayValue(rawValue)
     * </pre>
     *
     * @param value  The LDAP value to display; may be null or an empty sentinel.
     * @return       A human-readable string for the cell.
     */
    @Override
    public String getDisplayValue( IValue value )
    {
        Object obj = getRawValue( value );
        return StringValueEditorUtils.getDisplayValue( obj );
    }


    // ── C-3PO Retrieves the Exact Rodian Phrasing from the Booth ─────────────
    // Han asks C-3PO to get the precise words Greedo used — not a summary, the
    // actual raw phrase. C-3PO checks whether there is only one thing being said
    // (single attribute, single value) and returns it; anything more complex and
    // he shrugs and says he cannot condense it to one quote.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw editable value drawn from an {@link AttributeHierarchy},
     * for use as the initial content of the in-place text widget. We handle
     * three cases:
     * <ul>
     *   <li>One attribute, zero values → return an appropriate EMPTY sentinel</li>
     *   <li>One attribute, one value   → return {@link #getRawValue(IValue)}</li>
     *   <li>Anything else              → return null (editor will not activate)</li>
     * </ul>
     *
     * <p>For example — C-3PO finding the one quote:</p>
     * <pre>
     *   if 0 values: hand Han a blank notepad (EMPTY sentinel)
     *   if 1 value:  hand Han the exact quote
     *   else:        "I cannot summarise all of that, Captain Solo."
     * </pre>
     *
     * @param attributeHierarchy  The attributes and values to inspect; may be
     *                            null (returns null).
     * @return                    The raw value for the text widget, or null if
     *                            the hierarchy cannot be represented as a single
     *                            editable string.
     */
    @Override
    public Object getRawValue( AttributeHierarchy attributeHierarchy )
    {
        if ( ( attributeHierarchy != null ) && ( attributeHierarchy.size() == 1 ) )
        {
            if ( attributeHierarchy.getAttribute().getValueSize() == 0 )
            {
                if ( attributeHierarchy.getAttribute().isString() )
                {
                    return IValue.EMPTY_STRING_VALUE;
                }
                else
                {
                    return IValue.EMPTY_BINARY_VALUE;
                }
            }
            else if ( attributeHierarchy.getAttribute().getValueSize() == 1 )
            {
                return getRawValue( attributeHierarchy.getAttribute().getValues()[0] );
            }
        }

        return null;
    }


    // ── C-3PO Confirms Greedo Actually Said Something ─────────────────────────
    // Before C-3PO bothers whispering, he checks whether Greedo actually spoke —
    // if the value is null or empty and carries no string or binary content,
    // there is nothing to translate and Han does not need to hear anything.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given {@link IValue} is something this editor can
     * handle inline. We accept values that are either string or binary; null
     * or other types return false.
     *
     * @param value  The LDAP value to check.
     * @return       {@code true} if the value is non-null and is string or
     *               binary; {@code false} otherwise.
     */
    @Override
    public boolean hasValue( IValue value )
    {
        return ( value != null ) && ( value.isString() || value.isBinary() );
    }


    // ── C-3PO Pulls Out the Exact Spoken Words ───────────────────────────────
    // Han wants the precise raw text — what Greedo said word-for-word, as
    // C-3PO's translation buffer holds it, before any polishing. C-3PO hands
    // back the exact string from his buffer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the raw string value from a single {@link IValue}. We delegate
     * to {@link StringValueEditorUtils#getRawValue(IValue)}, which handles the
     * string/binary flag combinations consistently across all string editors.
     *
     * @param value  The LDAP value to unwrap; may be null.
     * @return       The raw string (or binary-as-string) object ready for the
     *               text widget, or null if the value is null.
     */
    @Override
    public Object getRawValue( IValue value )
    {
        return StringValueEditorUtils.getRawValue( value );
    }


    // ── C-3PO Confirms Han's Reply Came Out as Readable Basic ────────────────
    // After Han formulates his reply, C-3PO checks it is actual human text
    // before passing it back to be saved. Only a proper String or byte[] makes
    // it through his verification step.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts the editor's raw value back to the form the LDAP model can
     * store. We delegate to
     * {@link StringValueEditorUtils#getStringOrBinaryValue(Object)}, which
     * returns a {@code String} for text values and a {@code byte[]} for
     * binary-encoded ones.
     *
     * @param rawValue  The value from the text widget after editing.
     * @return          A {@code String} or {@code byte[]} for the model, or
     *                  null if the type was not recognized.
     */
    @Override
    public Object getStringOrBinaryValue( Object rawValue )
    {
        return StringValueEditorUtils.getStringOrBinaryValue( rawValue );
    }


    // ── Han Points at C-3PO as His Official Interpreter ─────────────────────
    // The cantina bartender demands to know who is responsible for the
    // translation. Han jerks his thumb at C-3PO: "Him. He is the editor."
    // C-3PO returns a reference to himself.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link CellEditor} instance the JFace framework should use
     * to drive the edit lifecycle. Since this class itself extends
     * {@link TextCellEditor} (which extends {@link CellEditor}), we just
     * return {@code this}.
     *
     * @return This editor instance.
     */
    @Override
    public CellEditor getCellEditor()
    {
        return this;
    }


    // ── C-3PO Repeats What Was Just Whispered Back to Han ────────────────────
    // Han asks C-3PO to confirm what he just heard. C-3PO glances at the text
    // widget: if it shows the EMPTY sentinel he returns null (nothing was said),
    // otherwise he returns whatever text is currently in the widget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Retrieves the current text from the embedded SWT {@code Text} widget and
     * returns it as the editor's value. If the widget contains the EMPTY
     * sentinel string we return null to signal that the user cleared the value.
     *
     * <p>For example — C-3PO's echo:</p>
     * <pre>
     *   if text.getText().equals(EMPTY): return null   // nothing was said
     *   else:                           return text.getText()
     * </pre>
     *
     * @return The current text, or null if the text widget shows the EMPTY
     *         sentinel.
     */
    @Override
    protected Object doGetValue()
    {
        if ( EMPTY.equals( text.getText() ) )
        {
            return null;
        }
        else
        {
            return text.getText();
        }
    }


    // ── C-3PO Memorizes Han's Next Line Before the Conversation Continues ─────
    // Han scribbles his planned response and hands the note to C-3PO. If the
    // note is an EmptyValue placeholder C-3PO unwraps it to get the actual
    // string before loading it into the text widget; otherwise he loads it
    // directly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes a value into the embedded SWT {@code Text} widget so the user
     * sees the current content when the cell editor activates. If the value is
     * an {@link IValue.EmptyValue} we extract its string representation first,
     * because {@code TextCellEditor} cannot handle our custom sentinel type.
     *
     * <p>For example — C-3PO loading the text widget:</p>
     * <pre>
     *   if value instanceof IValue.EmptyValue:
     *       super.doSetValue(emptyValue.getStringValue())
     *   else:
     *       super.doSetValue(value)
     * </pre>
     *
     * @param value  The value to load into the text widget — may be an
     *               {@link IValue.EmptyValue} sentinel or a plain String.
     */
    @Override
    protected void doSetValue( Object value )
    {
        if ( value instanceof IValue.EmptyValue )
        {
            super.doSetValue( ( ( IValue.EmptyValue ) value ).getStringValue() );
        }
        else
        {
            super.doSetValue( value );
        }
    }


    // ── Han Labels C-3PO for the Bartender's Benefit ─────────────────────────
    // The cantina bartender is writing up a tab and needs a name for the droid.
    // Han scrawls "Threepio — String Translator" on a napkin and slaps it on
    // C-3PO's chest plate so everyone knows who he is.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the human-readable display name for this value editor. The name
     * appears in the "Open With" context menu and the value editor preference
     * page so users know which editor they are choosing.
     *
     * @param name  The display name to assign — for example "String Editor".
     */
    @Override
    public void setValueEditorName( String name )
    {
        this.name = name;
    }


    // ── C-3PO States His Name When the Bouncer Asks ───────────────────────────
    // The door bouncer at the cantina demands identification. C-3PO stands up
    // straight and recites the name tag Han wrote for him earlier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display name previously set via
     * {@link #setValueEditorName(String)}. Used by the UI to label this editor
     * in menus and preference pages.
     *
     * @return The editor's display name, or null if none has been set yet.
     */
    @Override
    public String getValueEditorName()
    {
        return name;
    }


    // ── Han Slaps a Holographic Badge on C-3PO's Chest Plate ─────────────────
    // To make C-3PO recognizable across the busy cantina, Han sticks a small
    // holographic badge — an icon — on C-3PO's chest plate. Now everyone can
    // spot the string translator at a glance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@link ImageDescriptor} used to represent this value editor
     * visually in the UI — for example, as an icon next to the editor's name
     * in the "Open With" menu.
     *
     * @param imageDescriptor  The image descriptor to use as this editor's
     *                         icon; may be null to clear it.
     */
    @Override
    public void setValueEditorImageDescriptor( ImageDescriptor imageDescriptor )
    {
        this.imageDescriptor = imageDescriptor;
    }


    // ── C-3PO Flashes His Badge at the Cantina Bouncer ───────────────────────
    // The bouncer at the exit demands to see C-3PO's credentials. C-3PO holds
    // up the holographic badge Han attached earlier so the bouncer can verify
    // it and wave him through.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ImageDescriptor} previously set via
     * {@link #setValueEditorImageDescriptor(ImageDescriptor)}. The UI uses this
     * to render the editor's icon in menus and preference pages.
     *
     * @return The editor's image descriptor, or null if none has been set.
     */
    @Override
    public ImageDescriptor getValueEditorImageDescriptor()
    {
        return imageDescriptor;
    }
}
