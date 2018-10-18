package eu.adamgiergun.wontgoshoppingpl;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import eu.adamgiergun.wontgoshoppingpl.DayFragment.OnListFragmentInteractionListener;

public class DayRecyclerViewAdapter extends RecyclerView.Adapter<DayRecyclerViewAdapter.ViewHolder> {

    private final ListOfLimitedCommerceDays days;
    private final OnListFragmentInteractionListener listener;
    private boolean isLookRich;

    DayRecyclerViewAdapter(ListOfLimitedCommerceDays days, OnListFragmentInteractionListener listener) {
        this.days = days;
        this.listener = listener;
        isLookRich = false;
    }

    void setLookRich(boolean isLookRich) {
        this.isLookRich = isLookRich;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        LimitedCommerceDay day = days.get(position);
        holder.day = day;
        holder.dayView.setText(day.getDateString());
        holder.timeView.setText(day.getTimeString(holder.timeView.getContext()));
        holder.nameView.setText(day.getName());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onListFragmentInteraction(holder.day);
                }
            }
        });
        if (isLookRich) {
            if (day.isHoliday()) {
                holder.cardView.setBackgroundResource(R.drawable.card_bg_red);
            } else {
                holder.cardView.setBackgroundResource(R.drawable.card_bg_orange);
            }
        } else {
            if (day.isHoliday()) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FF0000"));
            } else {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FF1493"));
            }
            float density = holder.cardView.getContext().getResources().getDisplayMetrics().density;
            holder.cardView.setRadius(8*density);
            holder.cardView.setCardElevation(4*density);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final TextView dayView;
        final TextView timeView;
        final TextView nameView;
        LimitedCommerceDay day;

        ViewHolder(CardView view) {
            super(view);
            cardView = view;
            dayView = view.findViewById(R.id.date);
            timeView = view.findViewById(R.id.time);
            nameView = view.findViewById(R.id.name);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + timeView.getText() + "'";
        }
    }
}
