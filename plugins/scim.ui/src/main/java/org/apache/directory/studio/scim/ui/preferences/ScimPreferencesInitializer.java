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
package org.apache.directory.studio.scim.ui.preferences;


import org.apache.directory.studio.scim.ui.ScimUIConstants;
import org.apache.directory.studio.scim.ui.ScimUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;


public class ScimPreferencesInitializer extends AbstractPreferenceInitializer
{
    @Override
    public void initializeDefaultPreferences()
    {
        DefaultScope.INSTANCE.getNode( ScimUIPlugin.PLUGIN_ID ).put( ScimUIConstants.PREF_DEFAULT_SCIM_VERSION,
            "V2_0" );
        DefaultScope.INSTANCE.getNode( ScimUIPlugin.PLUGIN_ID ).put( ScimUIConstants.PREF_DEFAULT_AUTH_METHOD,
            "bearer" );
        DefaultScope.INSTANCE.getNode( ScimUIPlugin.PLUGIN_ID ).put( ScimUIConstants.PREF_DEFAULT_PAGE_SIZE, "100" );
        DefaultScope.INSTANCE.getNode( ScimUIPlugin.PLUGIN_ID ).put( ScimUIConstants.PREF_DEFAULT_TIMEOUT_MS,
            "30000" );
        DefaultScope.INSTANCE.getNode( ScimUIPlugin.PLUGIN_ID ).put( ScimUIConstants.PREF_AUTO_DISCOVER_SCHEMAS,
            "true" );
    }
}
