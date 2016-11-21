package com.nhatton.ggtalkvn;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.ContextMenu;
import android.view.View;
import android.view.MenuItem;
import android.widget.ListView;

import static com.nhatton.ggtalkvn.TTSActivity.tts;

public class CollectionActivity extends ListActivity{

    public SoundDbHelper mDbHelper;
    public SoundAdapter mAdapter;
    private Cursor mSoundCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        mDbHelper = new SoundDbHelper(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = mSoundCursor;
        c.moveToPosition(position);
        String description = c.getString(c.getColumnIndexOrThrow(SoundDbHelper.KEY_DESCRIPTION));
        tts.speak(description, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mSoundCursor.close();
        mDbHelper.close();
    }

    @Override
    protected void onRestart() {
        mDbHelper = new SoundDbHelper(this);
        mDbHelper.open();
        fillData();
        super.onRestart();
    }

    private void fillData() {
        mSoundCursor = mDbHelper.fetchAllSounds();
        setListAdapter(mAdapter = new SoundAdapter(this, mSoundCursor));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) menuInfo;
        menu.add(0, info.position, 0, R.string.enter_fullscreen);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id > -1) {
            Cursor cursor = mDbHelper.fetchAllSounds();
            cursor.moveToPosition(id);
            Intent fs_intent = new Intent(this, FullscreenActivity.class);
            String description = cursor.getString(cursor.getColumnIndexOrThrow(SoundDbHelper.KEY_DESCRIPTION));
            fs_intent.putExtra("TEXT_TO_FULLSCREEN", description);
            startActivity(fs_intent);
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
