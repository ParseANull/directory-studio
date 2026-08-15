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
package org.apache.directory.studio.connection.ui;


import java.net.URL;

import org.eclipse.ui.INewWizard;


/**
 * OSGi service interface for connection-type providers.
 *
 * Register an implementation as a Declarative Services component to add an
 * entry to the New Connection type-picker. The service is discovered at
 * runtime via ServiceTracker — no extension-point registration required.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IConnectionTypeContribution
{
    /** Stable identifier for this connection type (e.g. "ldap", "scim"). */
    String getId();

    /** Human-readable label shown in the type-picker dialog. */
    String getLabel();

    /**
     * Optional icon URL for the type-picker dialog; may return {@code null}
     * to fall back to a generic icon. Typically obtained via
     * {@code FileLocator.find(FrameworkUtil.getBundle(getClass()), new Path("..."), null)}.
     */
    URL getIconUrl();

    /** Creates a fresh wizard instance each time a new connection is requested. */
    INewWizard createWizard();
}
