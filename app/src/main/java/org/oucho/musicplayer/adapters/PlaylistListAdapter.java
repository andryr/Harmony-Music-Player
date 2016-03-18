package org.oucho.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.model.Playlist;
import org.oucho.musicplayer.utils.ThemeHelper;
import org.oucho.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;


public class PlaylistListAdapter extends AdapterWithHeader<PlaylistListAdapter.PlaylistViewHolder>
        implements FastScroller.SectionIndexer {

    private List<Playlist> mPlaylistList = Collections.emptyList();

    public void setData(List<Playlist> data) {
        mPlaylistList = data;
        notifyDataSetChanged();
    }

    public Playlist getItem(int position)
    {
        return mPlaylistList.get(position);
    }

    @Override
    public int getItemCountImpl() {
        return mPlaylistList.size();
    }

    @Override
    public int getItemViewTypeImpl(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolderImpl(PlaylistViewHolder viewHolder, int position) {
        Playlist playlist = getItem(position);
        viewHolder.vName.setText(playlist.getName());

    }

    @Override
    public PlaylistViewHolder onCreateViewHolderImpl(ViewGroup parent, int type) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.playlist_browser_item, parent, false);
        return new PlaylistViewHolder(itemView);
    }

    @Override
    public String getSectionForPosition(int position) {
        if(position >= 1) { //on ne prend pas en compte le header
            position--;
            String name = mPlaylistList.get(position).getName();
            if (name.length() > 0) {
                return name.substring(0, 1);
            }
        }
        return "";
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView vName;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.name);

            ThemeHelper.tintImageView(itemView.getContext(), (ImageView) itemView.findViewById(R.id.icon));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            triggerOnItemClickListener(position, v);
        }
    }
}
