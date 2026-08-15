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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: DatabaseWrapperLabelProvider — The Holographic Display Over a Cargo Hold ──
// When Luke peers at the Death Star schematics projected by R2-D2, each section
// of the station lights up with a clear label.  DatabaseWrapperLabelProvider is
// that holographic display for the Databases table: it draws the database icon,
// builds a human-readable "TYPE (suffix)" label for each entry, and greys out
// disabled databases (when that feature is supported).  Private helpers extract
// the type and suffix from the OlcDatabaseConfig model.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link StyledCellLabelProvider} for the Databases table.  It renders
 * each {@link DatabaseWrapper} as "TYPE (suffix)" with a database icon, and
 * applies a greyed style for disabled databases (pending OpenLDAP 2.5 support).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DatabaseWrapperLabelProvider extends StyledCellLabelProvider
{
    /** The Style to use when a database is disabled */
    private static final Styler grayedStyle = new Styler()
    {
        @Override
        public void applyStyles( TextStyle textStyle )
        {
            textStyle.foreground = CommonUIPlugin.getDefault().getColor( CommonUIConstants.DISABLED_COLOR );
        }
    };


    // ── getImage — Show the Database Icon ─────────────────────────────────────
    // R2-D2 lights up the cargo-hold icon on the holographic display.  We
    // return the database icon for DatabaseWrapper entries, and null otherwise.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get the Database image, if it's a Database. We can show two different icons, depending
     * on the Database status : enabled or disabled.
     *
     * @return the database icon, or {@code null} if the element is not a {@link DatabaseWrapper}
     */
    public Image getImage( Object element )
    {
        if ( element instanceof DatabaseWrapper )
        {
            // the olcDisabled AT is only present in 2.5
            // TODO : check with the schemaManager
            /*
            DatabaseWrapper database = (DatabaseWrapper) element;
            Boolean disabled = database.getDatabase().getOlcDisabled();

            if ( ( disabled == null ) || !disabled )
            {
                return OpenLdapConfigurationPlugin.getDefault().getImage(
                    OpenLdapConfigurationPluginConstants.IMG_DATABASE );
            }
            else
            {
                return OpenLdapConfigurationPlugin.getDefault().getImage(
                    OpenLdapConfigurationPluginConstants.IMG_DISABLED_DATABASE );
            }
             */

            return OpenLdapConfigurationPlugin.getDefault().getImage(
                OpenLdapConfigurationPluginConstants.IMG_DATABASE );
        }

        return null;
    }


    // ── update — Render the Database Row in the Table ─────────────────────────
    // R2-D2 projects the full label — "TYPE (suffix)" — and dims the projection
    // if the database is disabled.  Until the olcDisabled attribute is available
    // in the schema, we always render in the normal style.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Shows the Database name, and grey it if it's disabled.
     *
     * {@inheritDoc}
     */
    @Override
    public void update( ViewerCell cell )
    {
        Object element = cell.getElement();

        if ( element instanceof DatabaseWrapper )
        {
            DatabaseWrapper database = (DatabaseWrapper) element;
            OlcDatabaseConfig databaseConfig = database.getDatabase();
            String databaseType = getDatabaseType( databaseConfig );
            String databaseSuffix = getSuffix( databaseConfig );

            String databaseName = new StringBuilder( databaseType ).append( " (" ).append( databaseSuffix ).append( ")" ).toString();

            // the olcDisabled AT is only present in 2.5
            // TODO : check with the schemaManager
            /*
            Boolean disabled = database.getDatabase().getOlcDisabled();
            StyledString styledString = null;

            // Grey the database if it's disabled.
            if ( ( disabled == null ) || !disabled )
            {
                styledString = new StyledString( databaseName, grayedStyle );
            }
            else
            {
                styledString = new StyledString( databaseName, null );
            }
            */

            StyledString styledString = new StyledString( databaseName, null );
            cell.setText( styledString.toString() );
            cell.setStyleRanges( styledString.getStyleRanges() );
            cell.setImage( getImage( database ) );
        }

        super.update(cell);
    }


    // ── getDatabaseType — Extract the Type Name from the Config ───────────────
    // R2-D2 looks up the cargo type code and translates it to a human-readable
    // name (MDB, BDB, HDB, etc.).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Return the database type.
     *
     * @param database the database config
     * @return the database type name, or {@link DatabaseTypeEnum#NONE} if unknown
     */
    private String getDatabaseType( OlcDatabaseConfig database )
    {
        if ( database != null )
        {
            String databaseType = OpenLdapConfigurationPluginUtils.stripOrderingPrefix( database.getOlcDatabase() );

            DatabaseTypeEnum databasetype = DatabaseTypeEnum.getDatabaseType( databaseType );

            if ( databaseType != null )
            {
                return databasetype.name();
            }
            else
            {
                return DatabaseTypeEnum.NONE.name();
            }
        }

        return null;
    }


    // ── getSuffix — Extract the First Suffix DN from the Config ───────────────
    // R2-D2 reads the destination label stamped on the cargo: the olcSuffix DN.
    // If there is no suffix, we fall back to the NONE name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Return the Database suffix DN
     *
     * @param database the database config
     * @return the first suffix DN as a string, or the NONE label if absent
     */
    private String getSuffix( OlcDatabaseConfig database )
    {
        if ( database != null )
        {
            List<Dn> suffixes = database.getOlcSuffix();

            if ( ( suffixes != null ) && !suffixes.isEmpty() )
            {
                return suffixes.get( 0 ).toString();
            }
        }

        return DatabaseTypeEnum.NONE.getName();
    }
}
