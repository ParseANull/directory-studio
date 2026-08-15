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
package org.apache.directory.studio.templateeditor.model.widgets;


// ── ENUM: WidgetAlignment — C-3PO CATALOGUING IMPERIAL POSITIONING CODES ─────────
// In the Imperial communiqué system, C-3PO recognises exactly five positioning
// directives that govern where a component sits within its container: NONE (no
// preference stated), BEGINNING (left/top edge), CENTER (the middle), END
// (right/bottom edge), and FILL (consume all available space). Every widget's
// layout in the template XML maps to one of these five codes.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Enumerates the five alignment positions a template widget may request for
 * itself within its parent container. The value is stored in the template XML
 * and applied by the editor widgets when building SWT {@code GridData} layouts.
 *
 * <ul>
 *   <li>{@link #NONE} — no explicit alignment; the layout manager decides.</li>
 *   <li>{@link #BEGINNING} — left edge (horizontal) or top edge (vertical).</li>
 *   <li>{@link #CENTER} — centred on the axis.</li>
 *   <li>{@link #END} — right edge (horizontal) or bottom edge (vertical).</li>
 *   <li>{@link #FILL} — expand to fill all available space on the axis.</li>
 * </ul>
 *
 * <p>Think of these as the five positioning directives in a C-3PO-decoded
 * Imperial communiqué:</p>
 * <pre>
 *   NONE      → "no instruction provided — use default"
 *   BEGINNING → "place at the leading edge"
 *   CENTER    → "centre this element"
 *   END       → "place at the trailing edge"
 *   FILL      → "expand to consume remaining space"
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum WidgetAlignment
{
    /** No alignment specified; the layout manager uses its default. */
    NONE,

    /** Align to the left edge (horizontal) or top edge (vertical). */
    BEGINNING,

    /** Centre the widget on the axis. */
    CENTER,

    /** Align to the right edge (horizontal) or bottom edge (vertical). */
    END,

    /** Stretch to fill all remaining space on the axis. */
    FILL
}
