/*
 * Copyright (c) 2011 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nargila.robostroke.android.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Environment;

/**
 * file maker helper class.
 * Creates a file under a given directory in Aandroid's external storage
 * @author tshalif
 *
 */
public class FileHelper {
	private final static Logger logger =  LoggerFactory.getLogger(FileHelper.class);
	
	/**
	 * return 1st level directory on root of external storage directory - creating it if necessary
	 */
    public static File getDir(String dirName) {
    	File root = Environment.getExternalStorageDirectory();
    	if (root.canWrite()){
    		
    		File outdir = new File(root, dirName);
    		    		
    		outdir.mkdir();
    		
    		touchNoMedia(outdir);
    		
    		return outdir;
    	}
    	
    	logger.warn("external storage not available");
    	
    	return null;
    	
    }

	private static void touchNoMedia(File outdir) {
		File noMedia = new File(outdir, ".nomedia");
		
		if (!noMedia.exists()) {
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(noMedia);
				fileOutputStream.write(32);
				fileOutputStream.close();
			} catch (IOException e) {
				logger.error("can't touch .nomedia file", e);
			}
		}
	}	
	
	/**
	 * Creates a file under a given directory in Aandroid's external storage
	 * @param dirName directory in which to create file
	 * @param fileName name of file
	 * @return file object
	 */
    public static File getFile(String dirName, String fileName) {
    	File outdir = getDir(dirName);
    	
    	if (outdir != null) {    		
    		return new File(outdir, fileName);
    	}
    	
    	return null;
    	
    }
    
    public static boolean hasExternalStorage() {
    	String state = Environment.getExternalStorageState();

    	return Environment.MEDIA_MOUNTED.equals(state);
    }

	public static void cleanDir(File dir, long olderThen) {
		if (dir.isDirectory()) {
			File[] list = dir.listFiles();
			
			if (list != null) {
				for (File f: list) {
					if (olderThen > 0 && olderThen < System.currentTimeMillis() - f.lastModified()) { 
						f.delete();
					}
				}
			}
		}
	}
}
