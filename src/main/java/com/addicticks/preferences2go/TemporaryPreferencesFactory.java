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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * Preferences factory that uses {@link TemporaryPreferences}.
 * 
 * <p>
 * Preference values can be loaded from a file on startup. Also the contents
 * of the preference tree can be pretty printed.
 * 
 * 
 * 
 * @author Addicticks
 */
public class TemporaryPreferencesFactory implements PreferencesFactory {
    
    private static final Logger LOGGER = Logger.getLogger(TemporaryPreferencesFactory.class.getName());
    
    /**
     * System Property. If set it's assumed to be the name of a Java Preferences
     * XML file conforming to the DTD as explained in {@link java.util.prefs.Preferences}.
     * The content of this XML file will be loaded on startup.
     * 
     */
    public static final String KEY_XML_FILE = "pref2go.xmlFile";
    
    /**
     * System Property. If set to "true" the contents of the loaded
     * preferences are pretty printed and logged to standard logger (level INFO)
     * on startup. This only has effect if {@link #KEY_XML_FILE} is set.
     */
    public static final String KEY_PRINT_PREF = "pref2go.printPref";
    
    private static final String INDENT = "    ";
    
    private final String xmlFileString;
    private final TemporaryPreferences systemRootPrefs;
    private final TemporaryPreferences userRootPrefs;

    public TemporaryPreferencesFactory() throws FileNotFoundException, IOException, InvalidPreferencesFormatException, BackingStoreException {
        systemRootPrefs = new TemporaryPreferences(
                null, // parent (null means root)
                "", // name (root has no node name)
                TemporaryPreferences.TreeType.SYSTEM // type
        );
        userRootPrefs = new TemporaryPreferences(
                null, // parent (null means root)
                "", // name (root has no node name)
                TemporaryPreferences.TreeType.USER // type
        );
        xmlFileString = System.getProperty(KEY_XML_FILE);
        loadPreferencesFromXMLFile();

    }
    
    
    private void loadPreferencesFromXMLFile() throws FileNotFoundException, IOException, InvalidPreferencesFormatException, BackingStoreException {
        if (xmlFileString != null) {
            LOGGER.log(Level.FINEST, "Java System Property " + KEY_XML_FILE + " found with value \"{0}\"", xmlFileString);
            LOGGER.log(Level.FINEST, "Attempting to load preferences from : {0}", xmlFileString);
            try (FileInputStream xmlStream = new FileInputStream(xmlFileString)) {
                XmlSupport.importPreferences(xmlStream, userRootPrefs, systemRootPrefs);
                
                LOGGER.log(Level.INFO, "Preferences succesfully loaded from file \"" + xmlFileString + "\"");
                
                if (Boolean.parseBoolean(System.getProperty(KEY_PRINT_PREF, "false"))) {
                    String prettyPrintSystemPrefs = prettyPrintPrefs(systemRootPrefs);
                    String prettyPrintUserPrefs = prettyPrintPrefs(userRootPrefs);
                    String prettyPrintPrefs = (prettyPrintSystemPrefs == null)
                            ? prettyPrintUserPrefs : prettyPrintSystemPrefs + System.lineSeparator() + prettyPrintUserPrefs;
                    LOGGER.logp(Level.INFO, "Addicticks", "Preferences2Go",
                            ((isRFAPreferences(systemRootPrefs, userRootPrefs)) ? "RFA " : "")
                            + "Preference values :" + System.lineSeparator() + prettyPrintPrefs);
                }
            }
        }
    }
    
    private boolean isRFAPreferences(Preferences sysPref, Preferences usrPref) throws BackingStoreException {
        return (sysPref.nodeExists("com/reuters/rfa") || usrPref.nodeExists("com/reuters/rfa"));
    }
    
    private static String prettyPrintPrefs(Preferences pref) {
        try {
            if (pref.childrenNames().length == 0 && pref.keys().length == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            if (pref.isUserNode()) {
                sb.append(INDENT).append("Preferences type : USER").append(System.lineSeparator());
            } else {
                sb.append(INDENT).append("Preferences type : SYSTEM").append(System.lineSeparator());
            }
            prettyPrintPrefs2(pref, sb);
            return sb.toString();
        } catch (BackingStoreException ex) {
            // Should never happen
            return "Could not read preferences " + ex;
        }
    }
    
    private static void prettyPrintPrefs2(Preferences pref, StringBuilder sb) throws BackingStoreException {
        if (pref.childrenNames().length == 0 && pref.keys().length == 0) {
            sb.append(INDENT).append(INDENT).append(pref.absolutePath()).append(System.lineSeparator());
            return;
        }
        for (String keyName : pref.keys()) {
            sb.append(INDENT).append(INDENT).append(pref.absolutePath()).append("/").append(keyName).append(" : ")
                    .append(pref.get(keyName, null)).append(System.lineSeparator());
        }
        for (String subNodeName : pref.childrenNames()) {
            prettyPrintPrefs2(pref.node(subNodeName), sb);
        }
    }
    

    @Override
    public Preferences systemRoot() {
        return systemRootPrefs;
    }

    @Override
    public Preferences userRoot() {
        return userRootPrefs;
    }
    
}
