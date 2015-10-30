package com.andryr.musicplayer.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.fragments.ArtistFragment;
import com.andryr.musicplayer.fragments.ArtistListFragment;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;

/**
 * Created by Andry on 28/10/15.
 */
public class ArtistListAdapter extends BaseAdapter<ArtistListAdapter.ArtistViewHolder>
        implements FastScroller.SectionIndexer {

    class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView vName;
        TextView vAlbumCount;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.artist_name);
            vAlbumCount = (TextView) itemView.findViewById(R.id.album_count);
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            triggerOnItemClickListener(position, v);
        }
    }

    private List<Artist> mArtistList = Collections.emptyList();


    public ArtistListAdapter(Context c) {

    }

    @Override
    public int getItemCount() {
        return mArtistList.size();
    }

    public Artist getItem(int position) {
        return mArtistList.get(position);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder viewHolder, int position) {
        Artist artist = mArtistList.get(position);
        viewHolder.vName.setText(artist.getName());
        viewHolder.vAlbumCount.setText(viewHolder.vAlbumCount.getContext().getResources()
                .getQuantityString(R.plurals.albums_count,
                        artist.getAlbumCount(), artist.getAlbumCount()));

    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.artist_list_item, parent, false);
        return new ArtistViewHolder(itemView);
    }


    public void setData(List<Artist> data) {
        mArtistList = data;
        notifyDataSetChanged();

    }

    @Override
    public String getSectionForPosition(int position) {
        return getItem(position).getName().substring(0, 1);
    }
}
