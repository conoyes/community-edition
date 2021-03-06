/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.util.SimplePath;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A Transactional? implementation of AVM path lookup cache
 * 
 * @author britt
 * 
 * @deprecated
 */
public class TransactionalLookupCache implements LookupCache, InitializingBean
{
    private static Log fgLogger = LogFactory.getLog(TransactionalLookupCache.class);
    private static Log fgLoggerInit = LogFactory.getLog(TransactionalLookupCache.class.getName() + ".init");
   

    /**
     * The Map of of keys to lookups.
     */
    private SimpleCache<LookupKey, Lookup> fCache;

    /**
     * Reference to the Node DAO.
     */
    private AVMNodeDAO fAVMNodeDAO;

    /**
     * Reference to the Store DAO.
     */
    private AVMStoreDAO fAVMStoreDAO;

    /**
     * Make one up.
     */
    public TransactionalLookupCache()
    {
    }
    
    /**
     * Set up the node dao.
     * @param dao The dao to set.
     */
    public void setAvmNodeDAO(AVMNodeDAO dao)
    {
        fAVMNodeDAO = dao;
    }

    /**
     * Set the store dao.
     * @param dao The dao to set.
     */
    public void setAvmStoreDAO(AVMStoreDAO dao)
    {
        fAVMStoreDAO = dao;
    }

    public void setTransactionalCache(SimpleCache<LookupKey, Lookup> cache)
    {
        fCache = cache;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
       if (fgLoggerInit.isDebugEnabled())
          fgLoggerInit.debug("Transactional AVM lookup cache initialised");
    }
    
    /**
     * Lookup a path. Try to fulfill the request from the cache.
     * @param store The AVMStore.
     * @param version The versions.
     * @param path The path we are looking up.
     * @param write Whether this is a write lookup.
     * @param includeDeleted
     * @return
     */
    public Lookup lookup(AVMStore store, int version, SimplePath path,
                         boolean write, boolean includeDeleted)
    {
        // Create a key object.
        LookupKey key = new LookupKey(version, path, store.getName(), write, includeDeleted);
        // Is it in the cache?
        Lookup found = findInCache(key);
        // TODO Testing.
        // found = null;
        if (found != null)
        {
            if (fgLogger.isDebugEnabled())
            {
                fgLogger.debug("Cache Hit: " + key + ", " + found.getCurrentNode().getId());
            }
            return found;
        }
        // Make up a Lookup to hold the results.
        if (path.size() == 0)
        {
            return null;
        }
        Lookup result = new Lookup(store, store.getName(), version);
        // Grab the root node to start the lookup.
        DirectoryNode dir = null;
        // Versions less than 0 mean get current.
        if (version < 0)
        {
            dir = store.getRoot();
        }
        else
        {
            VersionRoot vRoot = AVMDAOs.Instance().fVersionRootDAO.getByVersionID(store, version);
            if (vRoot != null)
            {
                dir = vRoot.getRoot();
            }
//            dir = fAVMNodeDAO.getAVMStoreRoot(store, version);
        }
        if (dir == null)
        {
            return null;
        }
        // Add an entry for the root.
        result.add(dir, "", true, write);
        dir = (DirectoryNode)result.getCurrentNode();
        if (path.size() == 1 && path.get(0).equals(""))
        {
            fCache.put(key, result);
            return result;
        }
        // Now look up each path element in sequence up to one
        // before the end.
        for (int i = 0; i < path.size() - 1; i++)
        {
            if (!AVMRepository.GetInstance().can(null, dir, PermissionService.READ_CHILDREN, result.getDirectlyContained()))
            {
                throw new AccessDeniedException("Not allowed to read children: " + path.get(i));
            }
            Pair<AVMNode, Boolean> child = dir.lookupChild(result, path.get(i), includeDeleted);
            if (child == null)
            {
                return null;
            }
            // Every element that is not the last needs to be a directory.
            if (child.getFirst().getType() != AVMNodeType.PLAIN_DIRECTORY &&
                child.getFirst().getType() != AVMNodeType.LAYERED_DIRECTORY)
            {
                return null;
            }
            result.add(child.getFirst(), path.get(i), child.getSecond(), write);
            dir = (DirectoryNode)result.getCurrentNode();
        }
        // Now look up the last element.
        if (!AVMRepository.GetInstance().can(null, dir, PermissionService.READ_CHILDREN, result.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read children: " + path.get(path.size() - 1));
        }
        Pair<AVMNode, Boolean> child = dir.lookupChild(result, path.get(path.size() - 1),
                                        includeDeleted);
        if (child == null)
        {
            return null;
        }
        result.add(child.getFirst(), path.get(path.size() - 1), child.getSecond(), write);
        fCache.put(key, result);
        return result;
    }

    /**
     * Try to find a match in the cache.
     * @param key The lookup key.
     * @return A valid for this session Lookup or null if not found.
     */
    private synchronized Lookup findInCache(LookupKey key)
    {
        Lookup found = fCache.get(key);
        if (found != null)
        {
            // Get a freshened Lookup.
            Lookup result = new Lookup(found, fAVMNodeDAO, fAVMStoreDAO);
            // Check that nothing horrible is wrong.  This should
            // be assertible, but I'll leave the check in for now.
            if (!result.isValid())
            {
                fgLogger.error("Invalid entry in cache: " + key);
                return null;
            }
            return result;
        }
        // Alternatively for a read lookup a write can match.
        if (!key.isWrite())
        {
            // Make a copy of the key and set it to 'write'
            LookupKey newKey = new LookupKey(key);
            newKey.setWrite(true);
            found = fCache.get(newKey);
            if (found != null)
            {
                // We found it. Make a freshened copy of the Lookup.
                Lookup result = new Lookup(found, fAVMNodeDAO, fAVMStoreDAO);
                // Check for badness.  This should be assertible but I'll
                // leave the check in for now.
                if (!result.isValid())
                {
                    fgLogger.error("Invalid entry in cache: " + newKey);
                    return null;
                }
                return result;
            }
        }
        return null;
    }

    // Following are the cache invalidation calls.

    /**
     * Called when a simple write operation occurs.  This
     * invalidates all read lookups and all layered lookups.
     */
    public synchronized void onWrite(String storeName)
    {
        // Invalidate if it's a read lookup in the store, or
        // any read lookup is it's layered.
        List<LookupKey> keys = new ArrayList<LookupKey>();
        for (LookupKey key : fCache.getKeys())
        {
            keys.add(key);
        }
        for (LookupKey key : keys)
        {
            Lookup value = fCache.get(key);
            if ((key.getStoreName().equals(storeName) &&
                !key.isWrite()) || value == null ||
                (!key.isWrite() && value.isLayered()))
            {
                if (fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("Invalidating: " + key + ", " + (value != null ? value.getCurrentNode().getId() : -2));
                }
                fCache.remove(key);
            }
        }
    }

    /**
     * Called when a delete has occurred in a store.  This invalidates both
     * reads and write lookups in that store.
     */
    public synchronized void onDelete(String storeName)
    {
        // Invalidate any entries that are in the store or are layered lookups.
        List<LookupKey> keys = new ArrayList<LookupKey>();
        for (LookupKey key : fCache.getKeys())
        {
            keys.add(key);
        }
        for (LookupKey key : keys)
        {
            Lookup value = fCache.get(key);
            if (key.getStoreName().equals(storeName) ||
                value == null || value.isLayered())
            {
                if (fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("Invalidating: " + key + ", " + (value != null ? value.getCurrentNode().getId() : -2));
                }
                fCache.remove(key);
            }
        }
    }

    /**
     * Called when a snapshot occurs in a store.  This invalidates write
     * lookups.  Read lookups stay untouched.
     */
    public synchronized void onSnapshot(String storeName)
    {
        // Invalidate any entries that in the store and writes or
        // any layered lookups.
        List<LookupKey> keys = new ArrayList<LookupKey>();
        for (LookupKey key : fCache.getKeys())
        {
            keys.add(key);
        }
        for (LookupKey key : keys)
        {
            Lookup value = fCache.get(key);
            if ((key.getStoreName().equals(storeName) &&
                 key.isWrite()) ||
                value == null || value.isLayered())
            {
                if (fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("Invalidating: " + key + ", " + (value != null ? value.getCurrentNode().getId() : -2));
                }
                fCache.remove(key);
            }
        }
    }

    public synchronized void reset()
    {
        fCache.clear();
    }
}
