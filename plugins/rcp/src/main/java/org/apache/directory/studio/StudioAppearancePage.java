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
 */
package org.apache.directory.studio;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Replacement for the built-in Eclipse Appearance preference page that omits
 * the legacy "Color and font theme" row (superseded by the CSS theme selector).
 *
 * All preference logic is delegated to the original ViewsPreferencePage via
 * reflection so that tab, font, and scaling settings continue to work normally.
 */
public class StudioAppearancePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private static final String VIEWS_PAGE_CLASS = "org.eclipse.ui.internal.dialogs.ViewsPreferencePage";

    private PreferencePage delegate;


    public StudioAppearancePage()
    {
        setTitle( "Appearance" );
    }


    @Override
    public void init( IWorkbench workbench )
    {
        try
        {
            delegate = ( PreferencePage ) Class.forName( VIEWS_PAGE_CLASS )
                .getDeclaredConstructor().newInstance();
            ( ( IWorkbenchPreferencePage ) delegate ).init( workbench );
        }
        catch ( ReflectiveOperationException e )
        {
            delegate = null;
        }
    }


    @Override
    protected Control createContents( Composite parent )
    {
        if ( delegate == null )
        {
            return new Label( parent, SWT.NONE );
        }
        try
        {
            Method m = findMethod( delegate.getClass(), "createContents", Composite.class );
            m.setAccessible( true );
            Control content = ( Control ) m.invoke( delegate, parent );
            hideColorsAndFontsThemeRow();
            return content;
        }
        catch ( ReflectiveOperationException e )
        {
            return new Label( parent, SWT.NONE );
        }
    }


    private void hideColorsAndFontsThemeRow()
    {
        try
        {
            Field f = delegate.getClass().getDeclaredField( "colorsAndFontsThemeCombo" );
            f.setAccessible( true );
            ComboViewer viewer = ( ComboViewer ) f.get( delegate );
            if ( viewer == null )
            {
                return;
            }
            Composite row = viewer.getCombo().getParent();
            excludeFromLayout( row );
            row.getParent().layout( true, true );
        }
        catch ( ReflectiveOperationException e )
        {
            // Silently accept — the combo stays visible but functionality is unaffected.
        }
    }


    private static void excludeFromLayout( Composite composite )
    {
        for ( Control child : composite.getChildren() )
        {
            child.setVisible( false );
            Object ld = child.getLayoutData();
            if ( ld instanceof GridData )
            {
                ( ( GridData ) ld ).exclude = true;
            }
        }
        Object ld = composite.getLayoutData();
        if ( ld instanceof GridData )
        {
            ( ( GridData ) ld ).exclude = true;
        }
        composite.setVisible( false );
    }


    @Override
    public boolean performOk()
    {
        return delegate != null ? delegate.performOk() : true;
    }


    @Override
    protected void performDefaults()
    {
        if ( delegate == null )
        {
            return;
        }
        try
        {
            Method m = findMethod( delegate.getClass(), "performDefaults" );
            m.setAccessible( true );
            m.invoke( delegate );
        }
        catch ( ReflectiveOperationException e )
        {
            super.performDefaults();
        }
    }


    private static Method findMethod( Class<?> clazz, String name, Class<?>... params ) throws NoSuchMethodException
    {
        try
        {
            return clazz.getDeclaredMethod( name, params );
        }
        catch ( NoSuchMethodException e )
        {
            if ( clazz.getSuperclass() != null )
            {
                return findMethod( clazz.getSuperclass(), name, params );
            }
            throw e;
        }
    }
}
