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
package org.apache.directory.studio.apacheds.configuration;


// ── CLASS: ApacheDS2ConfigurationPluginConstants — DEATH STAR TECHNICAL SCHEMATICS REFERENCE
// The Death Star technical readout is full of part numbers, section codes, and resource paths.
// Nobody memorises them — you look them up in the reference appendix.
// This interface is that appendix: every image path, every file name constant, and the
// plugin ID, collected in one place so the rest of the plugin can reference them by name.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Symbolic constants for the {@code apacheds.configuration} plugin — plugin ID, image resource
 * paths, and well-known LDIF file names.
 * All values are compile-time constants accessed directly as static fields.
 * Think of it as the Death Star technical readout's reference appendix.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ApacheDS2ConfigurationPluginConstants
{
    /** The plug-in ID */
    String PLUGIN_ID = ApacheDS2ConfigurationPluginConstants.class.getPackage().getName();

    // ------
    // IMAGES
    // ------
    String IMG_EDITOR = "resources/icons/editor.gif"; //$NON-NLS-1$
    String IMG_EXPORT = "resources/icons/export.gif"; //$NON-NLS-1$
    String IMG_INDEX = "resources/icons/index.png"; //$NON-NLS-1$
    String IMG_IMPORT = "resources/icons/import.gif"; //$NON-NLS-1$
    String IMG_EXTENDED_OPERATION = "resources/icons/extended_operation.gif"; //$NON-NLS-1$
    String IMG_HORIZONTAL_ORIENTATION = "resources/icons/horizontal_orientation.gif"; //$NON-NLS-1$
    String IMG_INTERCEPTOR = "resources/icons/interceptor.gif"; //$NON-NLS-1$
    String IMG_NEW_SERVER_CONFIGURATION_FILE_WIZARD = "resources/icons/new_server_configuration_file_wizard.gif"; //$NON-NLS-1$
    String IMG_PARTITION = "resources/icons/partition.gif"; //$NON-NLS-1$
    String IMG_PARTITION_SYSTEM = "resources/icons/partition_system.gif"; //$NON-NLS-1$
    String IMG_PASSWORD_POLICY = "resources/icons/password_policy.gif"; //$NON-NLS-1$
    String IMG_PASSWORD_POLICY_DEFAULT = "resources/icons/password_policy_default.gif"; //$NON-NLS-1$
    String IMG_REPLICATION_CONSUMER = "resources/icons/replication_consumer.gif"; //$NON-NLS-1$
    String IMG_VERTICAL_ORIENTATION = "resources/icons/vertical_orientation.gif"; //$NON-NLS-1$

    String CONFIG_LDIF = "config.ldif"; //$NON-NLS-1$
    String OU_CONFIG = "ou=config"; //$NON-NLS-1$
    String OU_CONFIG_LDIF = "ou=config.ldif"; //$NON-NLS-1$
}
