package io.ubendren.imageprocessing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ajith on 12/21/17.
 */

public class OfferRecyclerViewAdapter extends RecyclerView.Adapter<OfferRecyclerViewAdapter.OfferViewHolder> {

    private List<OfferItem> offerItemList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public OfferRecyclerViewAdapter(Context context, List<OfferItem> offerItemList) {
        this.context = context;
        this.offerItemList = offerItemList;
    }


    @Override
    public OfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_offers_coloumn, null);
        OfferViewHolder viewHolder = new OfferViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(OfferViewHolder holder, int position) {
        final OfferItem offerItem = offerItemList.get(position);

        //Download image using picasso library
        if (!TextUtils.isEmpty(offerItem.getOffer_thumbnail())) {
            Picasso.with(context).load(offerItem.getOffer_thumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imageView);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(offerItem);
            }
        };

        holder.imageView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != offerItemList ? offerItemList.size() : 0);
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {

        protected ImageView imageView;

        public OfferViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.offer_thumbnail);
        }
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}