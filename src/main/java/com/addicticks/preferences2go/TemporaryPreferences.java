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

import java.util.HashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Preferences that live only in memory.
 * 
 * @see java.util.prefs.Preferences
 * @author Addicticks
 */
public class TemporaryPreferences extends AbstractPreferences {

    /**
     * Determines the type of tree to which the preference node belongs.
     * Has little or no value because our preferences are temporary and
     * live only in memory.
     */
    public static enum TreeType{
        USER,
        SYSTEM
    }
    private final TreeType treeType;
    
    /**
     * Contains all the preference entries of this node. This is
     * effectively our backing store.
     * AbstractPreferences (from which we inherit) take care of all 
     * synchronization so there's no need to use a concurrent hash map
     * here.
     */
    private final HashMap<String, String> entries = new HashMap<>();

    /**
     * Creates a preference node with the specified parent and the specified name relative to its parent.
     * 
     * <p>The tree type will be inherited from the <tt>parent</tt> unless <tt>parent</tt>
     * is null in which case it will be <tt>USER</tt>.
     * 
     * @param parent the parent of this preference node, or <tt>null</tt> if this is the root.
     * @param name the name of this preference node, relative to its parent, or "" if this is the root.
     * @throws IllegalArgumentException if name contains a slash ('/'), or parent is <tt>null</tt> and name isn't "".
     * 
     */
    public TemporaryPreferences(TemporaryPreferences parent, String name) {
        this(parent, name, TreeType.USER);
    }
    
    /**
     * Creates a preference node with the specified parent and the specified name relative to its parent.
     * 
     * @param parent the parent of this preference node, or <tt>null</tt> if this is the root.
     * @param name the name of this preference node, relative to its parent, or "" if this is the root.
     * @param treeType either USER or SYSTEM. It's almost completely irrelevant for temporary preferences. This parameter
     * is ignored except on the root node, i.e. when <tt>parent = null</tt>. If this parameter is <tt>null</tt>
     * the tree type will default to USER.
     * @throws IllegalArgumentException if name contains a slash ('/'), or parent is <tt>null</tt> and name isn't "".
     * 
     */
    public TemporaryPreferences(TemporaryPreferences parent, String name, TreeType treeType) {
        super(parent, name);
        
        // Any node is always new since nodes live only in memory.
        newNode = true;
        
        if (parent != null) {
            // Inherit tree type from parent
            this.treeType = parent.treeType;
        } else {
            if (treeType != null) {
                this.treeType = treeType;
            } else {
                this.treeType = TreeType.USER;
            }
        }
    }

    @Override
    public boolean isUserNode() {
        return (treeType == TreeType.USER);
    }


    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        // It is ok for this method to return an empty array because the
        // method "need not return the names of any nodes already cached" 
        // and since our preferences live in memory they are indeed already
        // cached.
        return new String[0];
    }

    
    @Override
    protected AbstractPreferences childSpi(String childName) {
        return new TemporaryPreferences(this, childName);
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        return entries.keySet().toArray(new String[entries.size()]);
    }

    @Override
    protected String getSpi(String key) {
        return entries.get(key);
    }

    @Override
    protected void putSpi(String key, String value) {
        entries.put(key, value);
    }

    @Override
    protected void removeSpi(String key) {
        entries.remove(key);
    }

    @Override
    protected void flushSpi() {
        // Nothing to flush. Data is in memory
        // and therefore already flushed.
    }

    @Override
    protected void syncSpi() {
        // Nothing to sync. Data is in memory
        // and therefore already sync'ed.
    }

    @Override
    protected void removeNodeSpi() {
        entries.clear();
    }
    
}
