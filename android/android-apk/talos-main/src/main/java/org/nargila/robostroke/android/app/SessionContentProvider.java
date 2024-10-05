package org.nargila.robostroke.android.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import org.nargila.robostroke.android.common.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;

public class SessionContentProvider extends ContentProvider {
    private static final int URI_MATCH = 42;

    static final String AUTHORITY = "org.nargila.robostroke.android.app.SessionFileProvider";

    // UriMatcher used to match against incoming requests
    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, "*", URI_MATCH);

        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {

        // Check incoming Uri against the matcher
        if (uriMatcher.match(uri) == URI_MATCH) {
            File file = FileHelper.getFile(
                    getContext(),
                    RoboStrokeActivity.ROBOSTROKE_DATA_DIR + "/tmp",
                    uri.getLastPathSegment()
            );
            if (!file.exists()) {
                throw new FileNotFoundException("Invalid file path");
            }
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            return pfd;
        }
        throw new FileNotFoundException("Unsupported uri: " + uri.toString());
    }

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s,
                      String[] as) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String s, String[] as1,
                        String s1) {
        return null;
    }
}
