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
package org.apache.directory.studio.apacheds.configuration.editor;

// ── CLASS: JavaVersion — IMPERIAL ENGINEERS CHECKING DEATH STAR BLUEPRINT COMPATIBILITY ──────────
// Before the Empire can fire the Death Star's superlaser, Moff Tarkin's engineers verify
// which blueprint revision is installed in the targeting computer: "Is this the Mark I
// system or the Mark II?  The firing sequence depends on it."
// This enum plays the same role: it identifies which Java version the server is targeted
// at, so the configuration editor can enable or disable features that only exist in
// certain JVM generations.  Pick the wrong revision and the targeting computer misfires.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Identifies the Java version that the ApacheDS server is compiled and run against.
 * We use this in the configuration editor to gate certain features — for example,
 * cipher suites or security algorithms that only exist on Java 8 and later.
 * Think of each constant as an Imperial blueprint revision: choose the right one
 * or the targeting computer won't accept the firing sequence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum JavaVersion
{
    JAVA_7( "Java 7" ),
    JAVA_8( "Java 8" );

    /** The name of the selected version */
    private String name;


    // ── STAMPING THE BLUEPRINT REVISION NUMBER ────────────────────────────────────────────────────
    // The Imperial archivist stamps the revision number onto the blueprint cover page.
    // "This is the Mark II targeting system — Java 8."  We store that display name
    // so the UI can present it in a human-readable combo or label.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Associates a human-readable display name with each enum constant.
     * The name is what the UI shows in dropdowns or labels — not the enum
     * constant identifier itself.
     *
     * <p>For example — stamping the revision label:</p>
     * <pre>
     *   JavaVersion.JAVA_8  // stored name: "Java 8"
     *   JavaVersion.JAVA_7  // stored name: "Java 7"
     * </pre>
     *
     * @param name  The human-readable label for this Java version (e.g., {@code "Java 8"}).
     */
    private JavaVersion( String name )
    {
        this.name = name;
    }


    // ── READING THE BLUEPRINT REVISION LABEL ─────────────────────────────────────────────────────
    // The engineer picks up the blueprint and reads the cover stamp: "Java 8."
    // getName hands back that display label so the UI can show it without exposing
    // the raw enum constant name.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display name for this Java version.
     * Use this for UI labels and combo box entries — it gives you a
     * presentable string like {@code "Java 8"} rather than {@code "JAVA_8"}.
     *
     * <p>For example — reading the revision stamp:</p>
     * <pre>
     *   String label = JavaVersion.JAVA_8.getName();  // "Java 8"
     * </pre>
     *
     * @return  The display name; never {@code null}.
     */
    public String getName()
    {
        return name;
    }
}
