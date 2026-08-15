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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// Like C-3PO patiently translating for R2-D2 in the middle of a crisis,
// this class takes raw resource keys and hands back something humans
// can actually read. We bridge the gap between internal key codes and
// the friendly messages the operator sees on screen.
/**
 * Translates resource bundle keys into human-readable message strings
 * for the OpenLDAP config editor dialogs. Think of us as the protocol
 * droid of the dialog layer — we speak the language of resource files
 * so the UI doesn't have to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    // Like C-3PO refusing to step into a situation without knowing the
    // odds, this private constructor makes sure nobody accidentally
    // instantiates this utility class. We're a translator, not a
    // character — you call us, you don't create us.
    /**
     * Private constructor — this is a static utility class, so we
     * keep it uninstantiable. No droids for hire here.
     */
    private Messages()
    {
        // Nothing to do
    }


    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // Like C-3PO consulting his six million forms of communication to
    // find the right phrase, we look up the given key in our resource
    // bundle and return whatever message we find there. If the key
    // doesn't exist, we wrap it in bangs so it's obviously broken.
    /**
     * Retrieves the message string associated with the given resource key.
     * If no matching entry exists in the bundle, we return the key itself
     * wrapped in exclamation marks so it's obvious something needs fixing.
     *
     * @param key the lookup key identifying the desired message string
     * @return the translated message, or {@code !key!} if the key is missing
     */
    public static String getString( String key )
    {
        try
        {
            return RESOURCE_BUNDLE.getString( key );
        }
        catch ( MissingResourceException e )
        {
            return '!' + key + '!';
        }
    }
}
