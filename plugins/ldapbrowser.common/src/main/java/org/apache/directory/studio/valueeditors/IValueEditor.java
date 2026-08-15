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


import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: IValueEditor — THE JEDI CODE ──────────────────────────────────────
// The Jedi Order is governed by a sacred Code — a small set of commitments that
// every Jedi, whatever their fighting style or temperament, must honour: they
// must be able to sense the Force, maintain discipline, protect the Republic,
// and act with wisdom. No individual Jedi can opt out of any clause.
// Our IValueEditor is the same kind of contract: every value editor, regardless
// of what type of LDAP attribute it handles, must be able to display a value,
// return a raw editing form, convert that back to a directory value, manage its
// own lifecycle (create/dispose), and expose its name and icon. No clause is
// optional.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The contract every LDAP value editor must fulfil. A value editor knows how to
 * display and edit the value of a single LDAP attribute. Implementations range
 * from simple in-place text fields to complex dialogs for binary, image, DN, or
 * certificate data.
 * <p>
 * Think of this interface as the Jedi Code: every editor, no matter its
 * speciality, must honour the same set of commitments — display, edit, convert,
 * and manage its own lifecycle.
 * </p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IValueEditor
{

    // ── A JEDI READS THE SEARCH RESULT SCROLL ────────────────────────────────
    // In the Jedi Archives, a Knight reads a scroll containing multiple entries
    // from the census: they scan all the attributes in the hierarchy and return
    // a single, human-readable summary — typically a comma-separated list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable string representing the given attribute hierarchy
     * for display in the search result editor. An attribute hierarchy may contain
     * multiple attributes each with multiple values — a common approach is to
     * return a comma-separated list of all display values.
     *
     * @param attributeHierarchy  the attribute hierarchy to summarise; never
     *                            {@code null}
     * @return                    a non-null display string for the hierarchy
     */
    String getDisplayValue( AttributeHierarchy attributeHierarchy );


    // ── A JEDI READS A SINGLE ENTRY SCROLL ───────────────────────────────────
    // In the entry editor, a Jedi focuses on exactly one attribute value and
    // reads it aloud in Basic so the council can understand it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable string representing a single LDAP value for
     * display in the entry editor. This is the string that appears in the
     * "Value" column of the attribute table.
     *
     * @param value  the attribute value to display; never {@code null}
     * @return       a non-null display string for the value
     */
    String getDisplayValue( IValue value );


    // ── A JEDI ACCEPTS A SEARCH RESULT MISSION BRIEF ─────────────────────────
    // The Jedi Council receives an attribute hierarchy and must decide whether
    // this Jedi is the right one for the mission. If yes, they return the raw
    // briefing material the mission editor will need; if no, they return null
    // to hand the mission to another Jedi.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw value used as input for the cell editor (via
     * {@link #getCellEditor()}) if this editor can handle the given attribute
     * hierarchy, or {@code null} if it cannot. Called from the search result
     * editor. It is common to return {@code null} when the hierarchy contains
     * more than one value, since multi-value editing is handled by
     * {@link MultivaluedValueEditor}.
     *
     * @param attributeHierarchy  the attribute hierarchy to assess
     * @return                    the raw value for cell editing, or {@code null}
     *                            if this editor cannot handle the hierarchy
     */
    Object getRawValue( AttributeHierarchy attributeHierarchy );


    // ── A JEDI CHECKS WHETHER A VALUE EXISTS IN THE FORCE ────────────────────
    // Before a Jedi Knight attempts to sense a presence in the Force, they
    // first check whether there is actually a presence there to sense — an
    // empty vessel returns false; a living being returns true.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given value is non-null and has actual content
     * that this editor can work with. This is a quick guard check — callers use
     * it to decide whether to even attempt editing or displaying a value.
     *
     * @param value  the value to check
     * @return       {@code true} if the value is present; {@code false} if null
     *               or otherwise empty
     */
    boolean hasValue( IValue value );


    // ── A JEDI ACCEPTS A SINGLE-ENTRY MISSION BRIEF ───────────────────────────
    // The Council hands a Jedi a single attribute value and asks: "Can you
    // handle this?" If yes, they return the raw editing material; if no, null.
    // Called from the entry editor, which works one value at a time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw value used as input for the cell editor if this editor
     * can handle the given single value, or {@code null} if it cannot. Called
     * from the entry editor. Note: the value may be empty (about to be created)
     * — implementations should handle that case.
     *
     * @param value  the attribute value to assess
     * @return       the raw value for cell editing, or {@code null} if this
     *               editor cannot handle the value
     */
    Object getRawValue( IValue value );


    // ── A JEDI DELIVERS THE MISSION REPORT TO THE DIRECTORY ──────────────────
    // After the mission, the Jedi delivers their report back to the Senate
    // archive. The raw notes from their field editor are converted into the
    // exact format the archive requires: a plain string or a byte array.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts the raw value returned by the cell editor into the final
     * {@code String} or {@code byte[]} value that will be written to the LDAP
     * directory. Called after the user finishes editing. The {@code rawValue}
     * is whatever the cell editor's {@code getValue()} method returned.
     *
     * @param rawValue  the raw value from the cell editor
     * @return          the directory-ready {@code String} or {@code byte[]} value
     */
    Object getStringOrBinaryValue( Object rawValue );


    // ── A JEDI REPORTS THEIR NAME ─────────────────────────────────────────────
    // Every Jedi carries a name — Luke Skywalker, Mace Windu, Ahsoka Tano —
    // so the Council can address them correctly in the registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this value editor, as previously set by
     * {@link #setValueEditorName(String)}. The name is assigned from the
     * Eclipse extension-registry entry for this editor.
     *
     * @return  the editor's display name
     */
    String getValueEditorName();


    // ── THE COUNCIL ASSIGNS THE JEDI'S NAME ──────────────────────────────────
    // When a new Jedi is inducted into the Order, the Council formally assigns
    // their name and records it in the Jedi Archives so any other Jedi or
    // administrative system can look them up by name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Assigns a display name to this value editor. Called during initialisation
     * by the plugin framework — the name comes from the editor's extension-point
     * declaration and is used in the UI to identify this editor.
     *
     * @param name  the editor's display name to store
     */
    void setValueEditorName( String name );


    // ── A JEDI PRESENTS THEIR HOLOCRON BADGE ─────────────────────────────────
    // Each Jedi Knight carries a holographic badge — a small image that lets
    // other Force-users and allies identify them at a glance in the registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this value editor's icon, as previously
     * set by {@link #setValueEditorImageDescriptor(ImageDescriptor)}. The image
     * is assigned from the Eclipse extension-registry entry.
     *
     * @return  the editor's image descriptor
     */
    ImageDescriptor getValueEditorImageDescriptor();


    // ── THE COUNCIL ASSIGNS THE JEDI'S HOLOCRON BADGE ────────────────────────
    // When inducted, the Council also issues the Jedi's holocron badge — the
    // visual symbol that identifies them in the Order's registry and UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Assigns an image descriptor to this value editor. Called during
     * initialisation by the plugin framework — the icon comes from the editor's
     * extension-point declaration and is shown next to the editor's name in the
     * UI.
     *
     * @param imageDescriptor  the image descriptor to store
     */
    void setValueEditorImageDescriptor( ImageDescriptor imageDescriptor );


    // ── A JEDI CREATES THEIR TRAINING CHAMBER ────────────────────────────────
    // Before any training session, a Jedi constructs their practice chamber
    // under the designated section of the Jedi Temple. The chamber is their
    // workspace; without it, they can't operate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT control for this value editor under the given parent
     * composite. For dialog-based editors this is typically a no-op (no
     * persistent control is created); for in-place editors this creates the
     * text widget that appears in the table cell.
     *
     * @param parent  the parent SWT composite to create the control inside
     */
    void create( Composite parent );


    // ── A JEDI DISMANTLES THEIR TRAINING CHAMBER ─────────────────────────────
    // When the Order calls a Jedi back from training, they dismantle their
    // practice chamber and return all SWT resources to the Temple's pool.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases all SWT resources held by this value editor. Called when the
     * owning view or editor is disposed. Implementations should null out any
     * widget references to avoid memory leaks.
     */
    void dispose();


    // ── A JEDI HANDS OVER THEIR LIGHTSABER ───────────────────────────────────
    // The Jedi Council uses a standard lightsaber handle — the CellEditor —
    // that any Jedi can grip. Each Jedi's saber has a different crystal (raw
    // value type), but the handle is always the same so the Council can wield
    // it uniformly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the JFace {@link CellEditor} that can edit the raw values returned
     * by {@link #getRawValue(IValue)} or {@link #getRawValue(AttributeHierarchy)}.
     * The object returned by {@code CellEditor.getValue()} is then passed to
     * {@link #getStringOrBinaryValue(Object)} to produce the directory value.
     *
     * @return  the cell editor for this value editor
     */
    CellEditor getCellEditor();

    // A constant for the emtpy string and null string.
    String EMPTY = ""; //$NON-NLS-1$
    String NULL = ">>> Error, the configured value editor can not handle this value! <<<"; //$NON-NLS-1$
}
