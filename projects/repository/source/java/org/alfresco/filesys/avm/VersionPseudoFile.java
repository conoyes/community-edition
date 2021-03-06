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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.avm;

import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFolderNetworkFile;
import org.alfresco.service.cmr.avm.VersionDescriptor;

/**
 * Version Pseudo File Class
 * 
 * <p>Represents an AVM store version as a folder.
 *
 * @author gkspencer
 */
public class VersionPseudoFile extends PseudoFile {

	/**
	 * Class constructor
	 * 
	 * @param name String
	 * @param relPath String
	 */
	public VersionPseudoFile( String name, String relPath)
	{
		super( name, FileAttribute.Directory + FileAttribute.ReadOnly);
		
		// Create static file information from the store details
		
		FileInfo fInfo = new FileInfo( name, 0L, FileAttribute.Directory + FileAttribute.ReadOnly);
		
		fInfo.setPath( relPath);
		fInfo.setFileId( relPath.hashCode());
		
		long timeNow = System.currentTimeMillis();
		fInfo.setCreationDateTime( timeNow);
		fInfo.setModifyDateTime( timeNow);
		fInfo.setAccessDateTime( timeNow);
		fInfo.setChangeDateTime( timeNow);
		
		setFileInfo( fInfo);
	}
	
	/**
	 * Class constructor
	 * 
	 * @param name String
	 * @param verDesc VersionDescriptor
	 * @param relPath String
	 */
	public VersionPseudoFile( String name, VersionDescriptor verDesc, String relPath)
	{
		super( name, FileAttribute.Directory + FileAttribute.ReadOnly);
		
		// Create static file information from the store details
		
		FileInfo fInfo = new FileInfo( name, 0L, FileAttribute.Directory + FileAttribute.ReadOnly);
		fInfo.setCreationDateTime( verDesc.getCreateDate());
		
		fInfo.setPath( relPath);
		fInfo.setFileId( relPath.hashCode());
		
		long timeNow = System.currentTimeMillis();
		fInfo.setCreationDateTime( timeNow);
		fInfo.setModifyDateTime( timeNow);
		fInfo.setAccessDateTime( timeNow);
		fInfo.setChangeDateTime( timeNow);
		
		setFileInfo( fInfo);
	}
	
    /**
     * Return a network file for reading/writing the pseudo file
     * 
     * @param netPath String
     * @return NetworkFile
     */
	@Override
	public NetworkFile getFile(String netPath) {
		
		// Split the path to get the name
		
		String[] paths = FileName.splitPath( netPath);
		
		// Create a network file for the folder
		
		return new PseudoFolderNetworkFile( paths[1], netPath);
	}

    /**
     * Return the file information for the pseudo file
     *
     * @return FileInfo
     */
	@Override
	public FileInfo getFileInfo() {
		return getInfo();
	}
}
