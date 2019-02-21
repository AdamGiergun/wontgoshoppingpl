package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
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
    private static float density;

    private DayRecyclerViewAdapter(ListOfLimitedCommerceDays days, OnListFragmentInteractionListener listener) {
        this.days = days;
        this.listener = listener;
    }

    static DayRecyclerViewAdapter getInstance(Context context, OnListFragmentInteractionListener listener) {
        DayRecyclerViewAdapter dayRecyclerViewAdapter =
                new DayRecyclerViewAdapter(
                        ListOfLimitedCommerceDays.getList(context),
                        listener);
        dayRecyclerViewAdapter.setLook(context);
        density = context.getResources().getDisplayMetrics().density;
        return dayRecyclerViewAdapter;
    }

    private void setLook(Context context) {
        isLookRich = new AppPreferences(context).isLookRich(context);
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
        holder.setup(day);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView dayView;
        private final TextView timeView;
        private final TextView nameView;
        private LimitedCommerceDay day;

        private ViewHolder(CardView view) {
            super(view);
            cardView = view;
            dayView = view.findViewById(R.id.date);
            timeView = view.findViewById(R.id.time);
            nameView = view.findViewById(R.id.name);
        }

        private void setup(final LimitedCommerceDay day) {
            this.day = day;
            dayView.setText(day.getDateAndWeekdayString());
            timeView.setText(day.getTimeString());
            nameView.setText(day.getName());

            setupCardView();
        }

        private void setupCardView() {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onListFragmentInteraction(day);
                    }
                }
            });
            if (isLookRich) {
                cardView.setBackgroundResource(day.getBackgroundResource());
            } else {
                cardView.setCardBackgroundColor(
                        day.getBackgroundColor(
                                cardView.getContext()));
                cardView.setRadius(8 * density);
                cardView.setCardElevation(4 * density);
            }
        }

        LimitedCommerceDay getDay() {
            return day;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + timeView.getText() + "'";
        }
    }
}
