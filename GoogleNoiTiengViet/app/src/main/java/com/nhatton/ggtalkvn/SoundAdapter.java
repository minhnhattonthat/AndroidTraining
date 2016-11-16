package com.nhatton.ggtalkvn;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SoundAdapter extends ArrayAdapter<Sound> {
    private ArrayList<Sound> items;

    private Context sContext = null;

    public SoundAdapter(Context context, int textViewResourceId, ArrayList<Sound> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.sContext = context;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater)sContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_row, null);
        }

        Sound sound = items.get(position);

        if (sound != null) {
            TextView description = (TextView) view.findViewById(R.id.description);
            if (description != null) {
                description.setText(sound.getDescription());
            }
        }

        return view;
    }
}