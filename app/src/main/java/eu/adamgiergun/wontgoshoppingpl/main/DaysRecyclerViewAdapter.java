package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences;
import eu.adamgiergun.wontgoshoppingpl.day.Day;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.main.DaysFragment.OnListFragmentInteractionListener;

public class DaysRecyclerViewAdapter extends RecyclerView.Adapter<DaysRecyclerViewAdapter.ViewHolder> {

    private final Day.List days;
    private final OnListFragmentInteractionListener listener;
    private boolean isLookRich;
    private static float density;

    private DaysRecyclerViewAdapter(Day.List days, OnListFragmentInteractionListener listener) {
        this.days = days;
        this.listener = listener;
    }

    static DaysRecyclerViewAdapter newInstance(Context appContext, OnListFragmentInteractionListener listener) {
        DaysRecyclerViewAdapter daysRecyclerViewAdapter =
                new DaysRecyclerViewAdapter(
                        Day.List.newInstance(appContext),
                        listener);
        daysRecyclerViewAdapter.setLook(appContext);
        density = appContext.getResources().getDisplayMetrics().density;
        return daysRecyclerViewAdapter;
    }

    private void setLook(Context appContext) {
        isLookRich = AppPreferences.getInstance(appContext).isLookRich(appContext);
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
        Day day = days.get(position);
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
        private Day day;

        private ViewHolder(CardView view) {
            super(view);
            cardView = view;
            dayView = view.findViewById(R.id.date);
            timeView = view.findViewById(R.id.time);
            nameView = view.findViewById(R.id.name);
        }

        private void setup(final Day day) {
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
                cardView.setCardBackgroundColor(getColor());
                cardView.setRadius(8 * density);
                cardView.setCardElevation(4 * density);
            }
        }

        private int getColor() {
            int colorRes = day.getBackgroundColorResource();
            Resources resources = cardView.getContext().getResources();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return resources.getColor(colorRes, null);
            } else {
                //noinspection deprecation
                return resources.getColor(colorRes);
            }
        }

        Day getDay() {
            return day;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + timeView.getText() + "'";
        }
    }
}
