package org.oucho.musicplayer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.images.ArtworkCache;
import org.oucho.musicplayer.images.ArtworkHelper;
import org.oucho.musicplayer.model.Album;
import org.oucho.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;


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

    public void setLayoutId() {
        mLayoutId = R.layout.small_album_grid_item;
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

        final ImageView vArtwork;
        final TextView vName;
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

/*            Drawable drawable = menuButton.getDrawable();

            drawable.mutate();
            ThemeHelper.tintDrawable(itemView.getContext(), drawable);*/

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            triggerOnItemClickListener(position, v);
        }
    }
}
