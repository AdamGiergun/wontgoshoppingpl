package eu.adamgiergun.niekupiszpl;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eu.adamgiergun.niekupiszpl.DayFragment.OnListFragmentInteractionListener;

public class DayRecyclerViewAdapter extends RecyclerView.Adapter<DayRecyclerViewAdapter.ViewHolder> {

    private final ListOfCommerceFreeDays days;
    private final OnListFragmentInteractionListener listener;

    DayRecyclerViewAdapter(ListOfCommerceFreeDays days, OnListFragmentInteractionListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CommerceFreeDay day = days.get(position);
        holder.day = day;
        holder.dayView.setText(day.getDayString());
        holder.timeView.setText(day.getTimeString(holder.timeView.getContext()));
        holder.nameView.setText(day.getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
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
        final View mView;
        final TextView dayView;
        final TextView timeView;
        final TextView nameView;
        CommerceFreeDay day;

        ViewHolder(View view) {
            super(view);
            mView = view;
            dayView = view.findViewById(R.id.date);
            timeView = view.findViewById(R.id.time);
            nameView = view.findViewById(R.id.name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + timeView.getText() + "'";
        }
    }
}
