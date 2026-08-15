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
package org.apache.directory.studio.templateeditor.editor.widgets;


import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: EditorWidgetUtils — R2-D2 WIRING THE CONTROL PANELS ───────────────────
// When R2-D2 needs to connect multiple systems together on the Tantive IV, he
// plugs his arm into the junction port and reads the raw data, then normalizes
// it into something the other panels can use. This utility class does the same:
// it reads an LDAP attribute from an entry, normalizes all its values into a
// single display string, and returns it so any panel widget can render it.
// The tricky part is that LDAP attributes can hold binary data (images, keys)
// or string data — we handle both cases cleanly.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utilities for reading LDAP attribute values from an entry and converting
 * them into display strings. Used by {@link EditorLabel} and {@link EditorLink}
 * to normalize both string and binary attributes into a renderable form.
 * Think of R2-D2 plugging into the junction port and normalizing the raw data.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorWidgetUtils
{
    // ── GET CONCATENATED VALUES: READ AND NORMALIZE THE ATTRIBUTE DATA ────────────
    // R2-D2 reads the raw data from the junction port, normalizes it, and returns
    // a clean string. Binary attributes get a placeholder label ("Binary value"),
    // string attributes get all their values joined with ", " separators.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string values of the named LDAP attribute from the given entry,
     * joined with ", " separators. Binary attributes return a localized placeholder
     * label ("Binary value" or "Binary values"). Returns an empty string if the
     * entry, attribute type, or attribute is null.
     *
     * <p>For example — R2-D2 reads and normalizes the attribute data:</p>
     * <pre>
     *   getConcatenatedValues(entry, "mail");
     *   // "luke@rebels.org, skywalker@rebels.org"
     * </pre>
     *
     * @param entry          the LDAP entry to read from; may be {@code null}
     * @param attributeType  the attribute type name to look up (e.g. "mail"); may be {@code null}
     * @return the concatenated string value, a binary placeholder, or {@code ""}
     */
    public static String getConcatenatedValues( IEntry entry, String attributeType )
    {
        if ( ( entry != null ) && ( attributeType != null ) )
        {
            // Getting the requested attribute
            IAttribute attribute = entry.getAttribute( attributeType );
            if ( attribute != null )
            {
                if ( attribute.getValues().length != 0 )
                {
                    // Checking the type of the value(s)
                    if ( attribute.isBinary() )
                    {
                        // Binary value(s)
                        if ( attribute.getBinaryValues().length == 1 )
                        {
                            return Messages.getString( "EditorWidgetUtils.BinaryValue" ); //$NON-NLS-1$
                        }
                        else
                        {
                            return Messages.getString( "EditorWidgetUtils.BinaryValues" ); //$NON-NLS-1$
                        }
                    }
                    else if ( attribute.isString() )
                    {
                        // String value(s)
                        return EditorWidgetUtils.concatenateValues( attribute.getStringValues() );
                    }
                }
            }
        }

        return ""; //$NON-NLS-1$
    }


    // ── CONCATENATE VALUES: JOIN MULTIPLE VALUES WITH COMMA SEPARATOR ─────────────
    // R2-D2 snaps the wires together — each value joined with ", " so the display
    // panel shows them all on a single line. The trailing ", " is trimmed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Joins the given string array with ", " separators. For example, given
     * {@code ["a", "b", "c"]} returns {@code "a, b, c"}.
     *
     * @param values  the string values to join; may be {@code null}
     * @return the joined string; empty string if {@code values} is null
     */
    private static String concatenateValues( String[] values )
    {
        StringBuilder sb = new StringBuilder();

        if ( values != null )
        {
            for ( String value : values )
            {
                sb.append( value );
                sb.append( ", " ); //$NON-NLS-1$
            }

            sb.delete( sb.length() - 2, sb.length() );
        }

        return sb.toString();
    }
}
