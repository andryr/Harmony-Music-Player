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
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;

/**
 * Created by Andry on 28/10/15.
 */
public class AlbumListAdapter extends BaseAdapter<AlbumListAdapter.AlbumViewHolder>
        implements FastScroller.SectionIndexer {

    private final int mArtworkWidth;
    private final int mArtworkHeight;
    private final Context mContext;
    private int mLayoutId = R.layout.album_grid_item;
    private List<Album> mAlbumList = Collections.emptyList();


    public AlbumListAdapter(Context context, int artworkWidth, int artworkHeight) {
        mArtworkWidth = artworkWidth;
        mArtworkHeight = artworkHeight;
        mContext = context;
    }

    public void setData(List<Album> data) {
        mAlbumList = data;
        notifyDataSetChanged();
    }

    public void setLayoutId(int layoutId) {
        mLayoutId = layoutId;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                mLayoutId, parent, false);


        return new AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder viewHolder, int position) {
        Album album = mAlbumList.get(position);
        viewHolder.vName.setText(album.getAlbumName());
        if (mLayoutId != R.layout.small_album_grid_item) {
            viewHolder.vArtist.setText(album.getArtistName());
        }

        //évite de charger des images dans les mauvaises vues si elles sont recyclées
        viewHolder.vArtwork.setTag(position);

        ArtworkCache.getInstance().loadBitmap(album.getId(), viewHolder.vArtwork, mArtworkWidth, mArtworkHeight, ArtworkHelper.getDefaultArtworkDrawable(mContext));


    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    @Override
    public String getSectionForPosition(int position) {
        String name = getItem(position).getAlbumName();
        if (name.length() > 0) {
            return name.substring(0, 1);
        }

        return "";
    }

    public Album getItem(int position) {
        return mAlbumList.get(position);
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView vArtwork;
        TextView vName;
        TextView vArtist;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            vArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
            vName = (TextView) itemView.findViewById(R.id.album_name);
            vArtwork.setOnClickListener(this);
            if (mLayoutId != R.layout.small_album_grid_item) {
                vArtist = (TextView) itemView.findViewById(R.id.artist_name);
                itemView.findViewById(R.id.album_info).setOnClickListener(this);
            } else {
                vName.setOnClickListener(this);
            }
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
