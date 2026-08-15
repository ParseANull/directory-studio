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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class ScimUIPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "org.apache.directory.studio.scim.ui";

    private static ScimUIPlugin plugin;


    @Override
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
        plugin = this;
    }


    @Override
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    public static ScimUIPlugin getDefault()
    {
        return plugin;
    }


    public ImageDescriptor getImage( String key )
    {
        ImageDescriptor descriptor = getImageRegistry().getDescriptor( key );
        if ( descriptor == null )
        {
            URL url = FileLocator.find( getBundle(), new Path( key ), null );
            if ( url != null )
            {
                descriptor = ImageDescriptor.createFromURL( url );
                getImageRegistry().put( key, descriptor );
            }
        }
        return descriptor;
    }
}
