package com.android.demo.notepad3;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class EfficientAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private Cursor mCursor;

    EfficientAdapter(Context context, Cursor cursor) {
        mCursor = cursor;

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
        return mCursor.getLong(mCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_ROWID));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        EfficientAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.notes_row, parent, false);

            holder = new EfficientAdapter.ViewHolder();
            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            holder.text3 = (TextView) convertView.findViewById(R.id.text3);

            convertView.setTag(holder);
        } else {
            holder = (EfficientAdapter.ViewHolder) convertView.getTag();
        }
        mCursor.moveToPosition(position);
        holder.text1.setText(mCursor.getString(
                mCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
        holder.text2.setText(mCursor.getString(
                mCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
        holder.text3.setText(mCursor.getString(
                mCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)));

        return convertView;
    }

    private class ViewHolder {
        TextView text1;
        TextView text2;
        TextView text3;
    }
}
