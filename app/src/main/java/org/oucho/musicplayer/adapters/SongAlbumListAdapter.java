package org.oucho.musicplayer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.oucho.musicplayer.PlaybackService;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.model.Song;
import org.oucho.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;


public class SongAlbumListAdapter extends AdapterWithHeader<SongAlbumListAdapter.SongViewHolder> implements FastScroller.SectionIndexer {

    //private final int mThumbWidth;
    //private final int mThumbHeight;
    private List<Song> mSongList = Collections.emptyList();


    public SongAlbumListAdapter(Context c) {
        //mThumbWidth = c.getResources().getDimensionPixelSize(R.dimen.art_thumbnail_size);
        //mThumbHeight = mThumbWidth;
    }

    public void setData(List<Song> data) {
        mSongList = data;
        notifyDataSetChanged();
    }

    public List<Song> getSongList() {
        return mSongList;
    }

    @Override
    public SongViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.song_album_list_item, parent, false);

        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolderImpl(SongViewHolder holder, int position) {
        Song song = getItem(position);

        String Track = String.valueOf(position + 1);

        holder.vTitle.setText(song.getTitle());
        holder.vTrackNumber.setText(Track);
    }

    public Song getItem(int position) {
        return mSongList.get(position);
    }

    @Override
    public int getItemCountImpl() {
        return mSongList.size();
    }

    @Override
    public int getItemViewTypeImpl(int position) {
        return 0;
    }

    @Override
    public String getSectionForPosition(int position) {
        if(position >= 1) { // on ne prend pas en compte le header
            position--; // je répète : on ne prend pas en compte le header
            String title = getItem(position).getTitle();
            if (title.length() > 0) {
                return title.substring(0, 1);
            }
        }
        return "";
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView vTitle;
        private final TextView vTrackNumber;


        public SongViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vTrackNumber = (TextView) itemView.findViewById(R.id.track_number);
            itemView.setOnClickListener(this);

            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);

        }

        int PrevCurrentPos;

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            triggerOnItemClickListener(position, v);
/*
            View trackNumber = v.findViewById(R.id.track_number);
            View VuMeter = v.findViewById(R.id.vu_meter);

            trackNumber.setVisibility(View.INVISIBLE);
            VuMeter.setVisibility(View.VISIBLE);

            PrevCurrentPos = position;*/

        }
    }
}
