/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.images.ArtworkHelper;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;

/**
 * Created by Andry on 28/10/15.
 */
public class SongListAdapter extends AdapterWithHeader<SongListAdapter.SongViewHolder> implements FastScroller.SectionIndexer {

    private final int mThumbWidth;
    private final int mThumbHeight;
    private final Context mContext;
    private List<Song> mSongList = Collections.emptyList();


    public SongListAdapter(Context c) {
        mThumbWidth = c.getResources().getDimensionPixelSize(R.dimen.art_thumbnail_size);
        mThumbHeight = mThumbWidth;
        mContext = c;
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
                R.layout.song_list_item, parent, false);

        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolderImpl(SongViewHolder holder, int position) {
        Song song = getItem(position);

        holder.vTitle.setText(song.getTitle());
        holder.vArtist.setText(song.getArtist());

        //évite de charger des images dans les mauvaises vues si elles sont recyclées
        holder.vArtwork.setTag(position);

        ArtworkCache.getInstance().loadBitmap(song.getAlbumId(), holder.vArtwork, mThumbWidth, mThumbHeight, ArtworkHelper.getDefaultThumbDrawable(mContext));
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
        private final TextView vArtist;
        private final ImageView vArtwork;


        public SongViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            vArtwork = (ImageView) itemView.findViewById(R.id.artwork);
            itemView.setOnClickListener(this);

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
