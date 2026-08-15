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
/**
 * ACI item editor-specific inner value editors.
 * Each class here handles one particular data type that appears inside an ACI string:
 * attribute types, attribute type + value pairs, LDAP filters, max-value-count tuples,
 * restrictedBy tuples, and subtree specifications. They pop up a small dialog when the
 * user needs to supply or change that value in the visual editor.
 * Think of these as the ISB data-entry terminals: each one is specialised for exactly
 * one field on the security directive form.
 */
package org.apache.directory.studio.aciitemeditor.valueeditors;
