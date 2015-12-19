package com.andryr.musicplayer.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.utils.ArtworkHelper;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;

/**
 * Created by Andry on 28/10/15.
 */
public class AlbumListAdapter extends BaseAdapter<AlbumListAdapter.AlbumViewHolder>
        implements FastScroller.SectionIndexer {

    private int mLayoutId = R.layout.album_grid_item;
    private List<Album> mAlbumList = Collections.emptyList();

    public void setData(List<Album> data) {
        mAlbumList = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    public Album getItem(int position) {
        return mAlbumList.get(position);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder viewHolder, int position) {
        Album album = mAlbumList.get(position);
        viewHolder.vName.setText(album.getAlbumName());
        if(mLayoutId != R.layout.small_album_grid_item) {
            viewHolder.vArtist.setText(album.getArtistName());
        }


        ArtworkHelper.loadArtwork(album.getId(), viewHolder.vArtwork);


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
    public String getSectionForPosition(int position) {
        String name = getItem(position).getAlbumName();
        if (name.length() > 0) {
            return name.substring(0, 1);
        }

        return "";
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
            if(mLayoutId != R.layout.small_album_grid_item) {
                vArtist = (TextView) itemView.findViewById(R.id.artist_name);
                itemView.findViewById(R.id.album_info).setOnClickListener(this);
            }
            else
            {
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
