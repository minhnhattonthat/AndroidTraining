/*
 * Copyright (C) 2008 Google Inc.
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

package com.android.demo.notepad3;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.android.demo.notepad3.Notepadv3.bodyUnedited;
import static com.android.demo.notepad3.Notepadv3.titleUnedited;

public class NoteEdit extends Activity {

    private EditText mTitleText;
    private EditText mBodyText;

    private Long mRowId;
    private NotesDbAdapter mDbHelper;

    private Cursor noteCursor;

    private void populateFields() {
        if (mRowId != null) {
            noteCursor = mDbHelper.fetchNote(mRowId);

            mTitleText.setText(noteCursor.getString(
                    noteCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(noteCursor.getString(
                    noteCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = savedInstanceState != null ? savedInstanceState.getLong(NotesDbAdapter.KEY_ROWID)
                : null;

        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                    : null;
        }
        populateFields();


        confirmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean checkEmpty = mTitleText.getText().toString().trim().length() > 0;
                if (!checkEmpty) {
                    mTitleText.setError("Title cannot be empty.");
                } else {
                    saveState(true, false);
                }
            }

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState(false, true);
    }

    @Override
    public void onBackPressed() {
        saveState(true, true);
    }


    private void saveState(boolean shouldFinish, boolean notClick) {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        final boolean checkEmpty = title.trim().length() > 0;

        if (!checkEmpty || notClick) {
            setResult(RESULT_CANCELED);

        } else if (mRowId == null) {
            long id = mDbHelper.createNote(title, body);

            if (id >= 0) {
                mRowId = id;
                setResult(RESULT_OK);
            } else {
                setResult(RESULT_CANCELED);
            }

        } else if (!titleUnedited.equals(title) || !bodyUnedited.equals(body)) {
            mDbHelper.updateNote(mRowId, title, body);
            setResult(RESULT_OK);

        } else {
            setResult(RESULT_CANCELED);
        }

        if (shouldFinish) {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        noteCursor = mDbHelper.fetchNote(mRowId);
    }
}
