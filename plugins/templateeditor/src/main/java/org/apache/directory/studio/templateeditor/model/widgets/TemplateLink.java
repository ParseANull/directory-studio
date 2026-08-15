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
package org.apache.directory.studio.templateeditor.model.widgets;


// ── CLASS: TemplateLink — C-3PO DECODING A HYPERLINK COMMUNIQUÉ ──────────────────
// In the Imperial communiqué, a link directive is simply a clickable text or URL.
// C-3PO reads the value field, which is the display text and/or the URL that the
// link widget will render as a clickable hyperlink in the editor form. It is the
// simplest interaction widget — one field, one action.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Model object for a template link (hyperlink) widget. Stores the link value,
 * which is the text and/or URL rendered as a clickable hyperlink in the editor.
 * The link may also bind to an LDAP attribute whose value is the URL.
 *
 * <p>Think of this as a C-3PO-decoded hyperlink communiqué directive:</p>
 * <pre>
 *   link.setValue( "http://directory.apache.org" );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateLink extends AbstractTemplateWidget
{
    /** The default value — {@code null} means display the bound LDAP attribute value. */
    public static String DEFAULT_VALUE = null;

    /** The label value */
    private String value = DEFAULT_VALUE;


    // ── CONSTRUCTOR: REGISTER THE LINK COMMUNIQUÉ ────────────────────────────────
    // C-3PO receives a new hyperlink directive and files it inside the parent
    // communiqué. The template parser sets the value from the XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateLink} and registers it as a child of the
     * given parent widget.
     *
     * @param parent  the enclosing widget (composite, section, or form)
     */
    public TemplateLink( TemplateWidget parent )
    {
        super( parent );
    }


    // ── GET VALUE: THE LINK TEXT OR URL ──────────────────────────────────────────
    // C-3PO reads the value field from the communiqué — the static URL or link text
    // to display. If null, the bound LDAP attribute supplies the URL at runtime.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the static link value (URL or display text), or {@code null} if the
     * bound LDAP attribute should supply it at runtime.
     *
     * @return the link value, or {@code null}
     */
    public String getValue()
    {
        return value;
    }


    // ── SET VALUE: THE LINK TEXT OR URL ──────────────────────────────────────────
    /**
     * Sets the static link value (URL or display text). Pass {@code null} to use
     * the bound LDAP attribute's live value instead.
     *
     * @param value  the link value, or {@code null}
     */
    public void setValue( String value )
    {
        this.value = value;
    }
}
