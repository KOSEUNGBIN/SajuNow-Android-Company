package com.test.landvibe.company.profit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.landvibe.company.R;

import java.util.List;

/**
 * Created by Administrator on 2016-06-22.
 */
public class ExchangesAdapter extends RecyclerView.Adapter<ExchangesAdapter.MyViewHolder> {

    private List<Exchange> exchangeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            genre = (TextView) view.findViewById(R.id.genre);
            year = (TextView) view.findViewById(R.id.year);
        }
    }


    public ExchangesAdapter(List<Exchange> exchangeList) {
        this.exchangeList = exchangeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_listview_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
       Exchange exchange = exchangeList.get(position);
        holder.title.setText(exchange.getExchange_amount());
        holder.genre.setText(exchange.getStatus());
        holder.year.setText(exchange.getExchange_date());

    }

    @Override
    public int getItemCount() {
        return exchangeList.size();
    }

    public void removeItem(int position) {
        exchangeList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, exchangeList.size());
    }
}
