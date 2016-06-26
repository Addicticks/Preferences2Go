/*
 * Copyright 2016 Addicticks.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.addicticks.preferences2go;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Addicticks
 */
public class FactoryTest {
    
    public FactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        System.setProperty("java.util.prefs.PreferencesFactory", "com.addicticks.preferences2go.TemporaryPreferencesFactory");
        System.setProperty(TemporaryPreferencesFactory.KEY_XML_FILE, "src/test/resources/test-pref-values.xml");
        System.setProperty(TemporaryPreferencesFactory.KEY_PRINT_PREF, "true");
    }
    
    @After
    public void tearDown() {
        System.getProperties().remove("java.util.prefs.PreferencesFactory");
    }

    
    @Test
    public void testFactoryInit() throws BackingStoreException {
        Preferences userRoot = Preferences.userRoot();
        assertTrue(userRoot.nodeExists("/com/reuters/rfa/AddicticksNamespace/Connections/TREPProd"));
        Preferences node = userRoot.node("/com/reuters/rfa/AddicticksNamespace/Connections/TREPProd");
        assertNotNull(node.get("serverList", null));
    }
}
