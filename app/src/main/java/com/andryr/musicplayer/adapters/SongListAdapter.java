package com.andryr.musicplayer.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;

/**
 * Created by Andry on 28/10/15.
 */
public class SongListAdapter extends BaseAdapter<SongListAdapter.SongViewHolder> implements FastScroller.SectionIndexer {

    private List<Song> mSongList = Collections.emptyList();

    public void setData(List<Song> data) {
        mSongList = data;
        notifyDataSetChanged();
    }

    public Song getItem(int position) {
        return mSongList.get(position);
    }

    public List<Song> getSongList() {
        return mSongList;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.song_list_item, parent, false);

        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = getItem(position);

        holder.vTitle.setText(song.getTitle());
        holder.vArtist.setText(song.getArtist());
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    @Override
    public String getSectionForPosition(int position) {
        return getItem(position).getTitle().substring(0, 1);
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView vTitle;
        private final TextView vArtist;


        public SongViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            itemView.findViewById(R.id.item_view).setOnClickListener(this);

            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);


            Drawable drawable = menuButton.getDrawable();

            drawable.mutate();
            ThemeHelper.tintDrawable(itemView.getContext(), drawable);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            triggerOnItemClickListener(position, v);


        }
    }
}
