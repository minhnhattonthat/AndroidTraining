/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")savedInstanceState;
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

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import java.text.DateFormat;

public class Notepadv3 extends ListActivity {
    public static final DateFormat df = DateFormat.getDateTimeInstance();
    public static String titleUnedited = "";
    public static String bodyUnedited = "";

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int DUPLICATE_ID = Menu.FIRST + 2;

    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notes_list);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        fillData();

        registerForContextMenu(getListView());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            mNotesCursor.close();
            mDbHelper.close();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mNotesCursor = mDbHelper.fetchAllNotes();
    }

    private void fillData() {
        mNotesCursor = mDbHelper.fetchAllNotes();

        setListAdapter(new EfficientAdapter(this, mNotesCursor));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        menu.add(0, DUPLICATE_ID, 0, R.string.menu_duplicate);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case DELETE_ID:

                mDbHelper.deleteNote(info.id);
                fillData();
                Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
                return true;
            case DUPLICATE_ID:

                mDbHelper.duplicateNote(info.id);
                fillData();
                Toast.makeText(this, "File duplicated", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Cursor note = mNotesCursor;
        note.moveToPosition(position);
        titleUnedited = note.getString(
                note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
        bodyUnedited = note.getString(
                note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));

        Intent i = new Intent(this, NoteEdit.class);

        i.putExtra(NotesDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        fillData();

        if (resultCode != 0) {
            switch (requestCode) {
                case 0: {
                    Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
                    break;
                }
                case 1: {
                    Toast.makeText(this, "File edited", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}