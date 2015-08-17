package com.stfalcon.dorozhnyjpatrul.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stfalcon.dorozhnyjpatrul.R;
import com.stfalcon.dorozhnyjpatrul.models.GridItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandr on 17/08/15.
 */
public class GridAdapter  extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    List<GridItem> mItems;

    public GridAdapter() {
        super();
        mItems = new ArrayList<GridItem>();
        GridItem species = new GridItem();
        species.setState(GridItem.STATE_IN_PROCESS);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);

        species = new GridItem();
        species.setState(GridItem.STATE_IN_PROCESS);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);

        species = new GridItem();
        species.setState(GridItem.STATE_IN_PROCESS);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);

        species = new GridItem();
        species.setState(GridItem.STATE_UPLOADED);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);

        species = new GridItem();
        species.setState(GridItem.STATE_UPLOADED);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);

        species = new GridItem();
        species.setState(GridItem.STATE_UPLOADED);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);

        species = new GridItem();
        species.setState(GridItem.STATE_ERROR);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);

        species = new GridItem();
        species.setState(GridItem.STATE_UPLOADED);
        species.setThumbnail(R.drawable.ic_stfalcon);
        mItems.add(species);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        GridItem nature = mItems.get(i);
        switch (nature.getState()){
            case GridItem.STATE_IN_PROCESS:
                viewHolder.tvspecies.setImageResource(android.R.drawable.ic_menu_upload);
                break;
            case GridItem.STATE_UPLOADED:
                viewHolder.tvspecies.setImageResource(android.R.drawable.ic_menu_save);
                break;
            case GridItem.STATE_ERROR:
                viewHolder.tvspecies.setImageResource(android.R.drawable.ic_delete);
                break;
        }
        viewHolder.imgThumbnail.setImageResource(nature.getThumbnail());
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgThumbnail;
        public ImageView tvspecies;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView)itemView.findViewById(R.id.img_thumbnail);
            tvspecies = (ImageView)itemView.findViewById(R.id.img_state);
        }
    }
}
