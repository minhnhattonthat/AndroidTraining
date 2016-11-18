package com.nhatton.ggtalkvn;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;


public class SoundAdapter extends BaseAdapter {

    private SoundDbAdapter mDbHelper;

    private LayoutInflater mInflater;

    private Cursor mCursor;

    SoundAdapter(Context context, Cursor cursor) {
        mCursor = cursor;
        mDbHelper = new SoundDbAdapter(context);
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return mCursor.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndexOrThrow(SoundDbAdapter.KEY_ROWID));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SoundAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_row, parent, false);

            holder = new SoundAdapter.ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.description);
            holder.buttonDelete = (Button) convertView.findViewById(R.id.button_delete);

            convertView.setTag(holder);
        } else {
            holder = (SoundAdapter.ViewHolder) convertView.getTag();
        }
        mCursor.moveToPosition(position);

        holder.text.setText(mCursor.getString(
                mCursor.getColumnIndexOrThrow(SoundDbAdapter.KEY_DESCRIPTION)));

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDbHelper.open();

                Cursor c = mCursor;

                int rowId = c.getInt(c.getColumnIndexOrThrow(SoundDbAdapter.KEY_ROWID));

                mDbHelper.deleteSound(rowId);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        TextView text;
        Button buttonDelete;
    }
}