package org.oucho.musicplayer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.images.ArtistImageCache;
import org.oucho.musicplayer.images.ArtistImageHelper;
import org.oucho.musicplayer.model.Artist;
import org.oucho.musicplayer.widgets.FastScroller;

import java.util.Collections;
import java.util.List;


public class ArtistListAdapter extends BaseAdapter<ArtistListAdapter.ArtistViewHolder>
        implements FastScroller.SectionIndexer {

    private final int mThumbWidth;
    private final int mThumbHeight;
    private final Context mContext;
    private List<Artist> mArtistList = Collections.emptyList();

    public ArtistListAdapter(Context c) {
        mThumbWidth = c.getResources().getDimensionPixelSize(R.dimen.art_thumbnail_size);
        mThumbHeight = mThumbWidth;
        mContext = c;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.artist_list_item, parent, false);
        return new ArtistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder viewHolder, int position) {
        Artist artist = mArtistList.get(position);
        viewHolder.vName.setText(artist.getName());
        viewHolder.vAlbumCount.setText(viewHolder.vAlbumCount.getContext().getResources()
                .getQuantityString(R.plurals.albums_count,
                        artist.getAlbumCount(), artist.getAlbumCount()));

        //évite de charger des images dans les mauvaises vues si elles sont recyclées
        viewHolder.vArtistImage.setTag(position);

        ArtistImageCache.getInstance().loadBitmap(artist.getName(), viewHolder.vArtistImage, mThumbWidth, mThumbHeight, ArtistImageHelper.getDefaultArtistThumb(mContext));

    }

    @Override
    public int getItemCount() {
        return mArtistList.size();
    }

    public void setData(List<Artist> data) {
        mArtistList = data;
        notifyDataSetChanged();

    }

    @Override
    public String getSectionForPosition(int position) {
        String name = getItem(position).getName();
        if (name.length() > 0) {
            return name.substring(0, 1);
        }

        return "";
    }

    public Artist getItem(int position) {
        return mArtistList.get(position);
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView vName;
        final TextView vAlbumCount;
        final ImageView vArtistImage;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.artist_name);
            vAlbumCount = (TextView) itemView.findViewById(R.id.album_count);
            vArtistImage = (ImageView) itemView.findViewById(R.id.artwork);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            triggerOnItemClickListener(position, v);
        }
    }
}
