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
package org.apache.directory.studio.scim.ui;


import java.net.URL;

import org.apache.directory.studio.connection.ui.IConnectionTypeContribution;
import org.apache.directory.studio.scim.ui.wizards.NewScimConnectionWizard;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.INewWizard;
import org.osgi.framework.FrameworkUtil;


/**
 * DS component that registers the SCIM connection type with the connection-type picker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ScimConnectionTypeContribution implements IConnectionTypeContribution
{
    @Override
    public String getId()
    {
        return "scim"; //$NON-NLS-1$
    }


    @Override
    public String getLabel()
    {
        return "SCIM Connection"; //$NON-NLS-1$
    }


    @Override
    public URL getIconUrl()
    {
        return FileLocator.find(
            FrameworkUtil.getBundle( getClass() ),
            new Path( "resources/icons/scim_connection.gif" ), //$NON-NLS-1$
            null );
    }


    @Override
    public INewWizard createWizard()
    {
        return new NewScimConnectionWizard();
    }
}
