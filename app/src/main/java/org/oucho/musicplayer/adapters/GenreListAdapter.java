package org.oucho.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.model.Genre;
import org.oucho.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;


public class GenreListAdapter extends BaseAdapter<GenreListAdapter.GenreViewHolder>
        implements FastScroller.SectionIndexer {


    private List<Genre> mGenreList = Collections.emptyList();

    public void setData(List<Genre> data) {
        mGenreList = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mGenreList.size();
    }

    @Override
    public void onBindViewHolder(GenreViewHolder viewHolder, int position) {
        Genre genre = mGenreList.get(position);
        viewHolder.vName.setText(genre.getName());

    }

    @Override
    public GenreViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.genre_list_item, parent, false);
        return new GenreViewHolder(itemView);
    }

    @Override
    public String getSectionForPosition(int position) {
        String name = mGenreList.get(position).getName();
        if (name.length() > 0) {
            return name.substring(0, 1);
        }

        return "";

    }

    class GenreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView vName;


        public GenreViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            triggerOnItemClickListener(position, v);
        }
    }
}
