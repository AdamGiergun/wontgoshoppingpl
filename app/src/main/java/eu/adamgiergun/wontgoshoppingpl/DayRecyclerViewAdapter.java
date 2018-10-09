package eu.adamgiergun.wontgoshoppingpl;

import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import eu.adamgiergun.wontgoshoppingpl.DayFragment.OnListFragmentInteractionListener;

public class DayRecyclerViewAdapter extends RecyclerView.Adapter<DayRecyclerViewAdapter.ViewHolder> {

    private final ListOfLimitedCommerceDays days;
    private final OnListFragmentInteractionListener listener;

    DayRecyclerViewAdapter(ListOfLimitedCommerceDays days, OnListFragmentInteractionListener listener) {
        this.days = days;
        this.listener = listener;
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
        holder.dayView.setText(day.getDayString());
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
