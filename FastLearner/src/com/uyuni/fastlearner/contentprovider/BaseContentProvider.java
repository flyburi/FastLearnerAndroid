package com.uyuni.fastlearner.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.uyuni.fastlearner.db.ContentsDatabaseHelper;
import com.uyuni.fastlearner.db.ContentsTable;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class BaseContentProvider extends ContentProvider {

	// database
	private ContentsDatabaseHelper database;

	// used for the UriMacher
	private static final int CONTENTS = 10;
	private static final int CONTENT_ID = 20;

	private static final String AUTHORITY = "com.uyuni.fastlearner.contentprovider";

	private static final String BASE_PATH = "contents";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/contents";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/content";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTENTS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CONTENT_ID);
	}

	@Override
	public boolean onCreate() {
		database = new ContentsDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(ContentsTable.TABLE_CONTENT);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case CONTENTS:
			break;
		case CONTENT_ID:
			// adding the ID to the original query
			queryBuilder.appendWhere(ContentsTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		long id = 0;
		switch (uriType) {
		case CONTENTS:
			id = sqlDB.insert(ContentsTable.TABLE_CONTENT, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case CONTENTS:
			rowsDeleted = sqlDB.delete(ContentsTable.TABLE_CONTENT, selection,
					selectionArgs);
			break;
		case CONTENT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(ContentsTable.TABLE_CONTENT,
						ContentsTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(ContentsTable.TABLE_CONTENT,
						ContentsTable.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case CONTENTS:
			rowsUpdated = sqlDB.update(ContentsTable.TABLE_CONTENT, values,
					selection, selectionArgs);
			break;
		case CONTENT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(ContentsTable.TABLE_CONTENT, values,
						ContentsTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(ContentsTable.TABLE_CONTENT, values,
						ContentsTable.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { ContentsTable.COLUMN_CATEGORY,
				ContentsTable.COLUMN_SUMMARY, ContentsTable.COLUMN_DESCRIPTION,
				ContentsTable.COLUMN_ID };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

}
