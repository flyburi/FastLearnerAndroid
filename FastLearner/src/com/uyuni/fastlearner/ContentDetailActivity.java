package com.uyuni.fastlearner;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.uyuni.fastlearner.contentprovider.BaseContentProvider;
import com.uyuni.fastlearner.db.ContentsTable;

public class ContentDetailActivity extends Activity {
	private Spinner mCategory;
	private EditText mTitleText;
	private EditText mBodyText;

	private Uri contentUri;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.content_edit);

		mCategory = (Spinner) findViewById(R.id.category);
		mTitleText = (EditText) findViewById(R.id.content_edit_summary);
		mBodyText = (EditText) findViewById(R.id.content_edit_description);
		Button confirmButton = (Button) findViewById(R.id.content_edit_button);

		Bundle extras = getIntent().getExtras();

		contentUri = (bundle == null) ? null : (Uri) bundle
				.getParcelable(BaseContentProvider.CONTENT_ITEM_TYPE);

		if (extras != null) {
			contentUri = extras
					.getParcelable(BaseContentProvider.CONTENT_ITEM_TYPE);

			fillData(contentUri);
		}

		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (TextUtils.isEmpty(mTitleText.getText().toString())) {
					makeToast();
				} else {
					setResult(RESULT_OK);
					finish();
				}
			}

		});
	}

	private void fillData(Uri uri) {
		String[] projection = { ContentsTable.COLUMN_SUMMARY,
				ContentsTable.COLUMN_DESCRIPTION, ContentsTable.COLUMN_CATEGORY };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		if (cursor != null) {
			cursor.moveToFirst();
			String category = cursor.getString(cursor
					.getColumnIndexOrThrow(ContentsTable.COLUMN_CATEGORY));

			for (int i = 0; i < mCategory.getCount(); i++) {

				String s = (String) mCategory.getItemAtPosition(i);
				if (s.equalsIgnoreCase(category)) {
					mCategory.setSelection(i);
				}
			}

			mTitleText.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(ContentsTable.COLUMN_SUMMARY)));
			mBodyText.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(ContentsTable.COLUMN_DESCRIPTION)));

			cursor.close();
		}
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putParcelable(BaseContentProvider.CONTENT_ITEM_TYPE,
				contentUri);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	private void saveState() {
		String category = (String) mCategory.getSelectedItem();
		String summary = mTitleText.getText().toString();
		String description = mBodyText.getText().toString();

		if (description.length() == 0 && summary.length() == 0) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(ContentsTable.COLUMN_CATEGORY, category);
		values.put(ContentsTable.COLUMN_SUMMARY, summary);
		values.put(ContentsTable.COLUMN_DESCRIPTION, description);

		if (contentUri == null) {
			contentUri = getContentResolver().insert(
					BaseContentProvider.CONTENT_URI, values);
		} else {
			getContentResolver().update(contentUri, values, null, null);
		}
	}

	private void makeToast() {
		Toast.makeText(ContentDetailActivity.this, "Please maintain a summary",
				Toast.LENGTH_LONG).show();
	}
}
