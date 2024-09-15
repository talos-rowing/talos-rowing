/*
 * Copyright (c) 2024 Tal Shalif
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
package org.nargila.robostroke.android.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.nargila.robostroke.android.common.FileHelper;
import org.nargila.robostroke.common.Pair;

import android.os.Environment;

class ReplayFileList {

  List<Pair<File,Date>> files;

  ReplayFileList(RoboStrokeActivity owner) {
    File dir =  FileHelper.getDir(owner, "RoboStroke");

    File[] fileList = dir == null ? new File[0] : dir.listFiles();

    ArrayList<Pair<File, Date>> sortedList = new ArrayList<>(fileList.length);

    boolean fileNameErrors = false;

    for (final File f: dir.listFiles()) {
      String name = f.getName();
      int idx = name.indexOf("-dataInput.txt");

      if (idx != -1) {
        Date date;
        try {
          date = new Date(new Long(name.substring(0, idx)));
        } catch (NumberFormatException e) {
          fileNameErrors = true;
          continue;
        }

        sortedList.add(Pair.create(f, date));
      }

    }

    if (fileNameErrors) {
      owner.reportError("malformed file names found under Talos Rowing replay directory", null);
    }

    Collections.sort(sortedList, new Comparator<Pair<File,Date>>() {
      @Override
      public int compare(Pair<File, Date> p1,
          Pair<File, Date> p2) {

        return p1.second.compareTo(p2.second);
      };
    });

    files = sortedList;
  }
}
