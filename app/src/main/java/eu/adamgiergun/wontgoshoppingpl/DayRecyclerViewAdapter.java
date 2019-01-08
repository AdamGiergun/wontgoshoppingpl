package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
            dayView.setText(day.getDateString());
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
                cardView.setBackgroundResource(getBackroundResource());
            } else {
                cardView.setCardBackgroundColor(getCardColor(cardView.getContext()));
                cardView.setRadius(8 * density);
                cardView.setCardElevation(4 * density);
            }
        }

        private int getBackroundResource() {
            int backgroundResource;
            switch (day.getType()) {
                case HOLIDAY:
                    backgroundResource = R.drawable.card_bg_red;
                    break;
                case NON_COMMERCE_DAY:
                    backgroundResource = R.drawable.card_bg_orange;
                    break;
                case COMMERCE_SUNDAY:
                    backgroundResource = R.drawable.card_bg_green;
                    break;
                default:
                    backgroundResource = 0;
                    break;
            }
            return backgroundResource;
        }

        private int getCardColor(Context context) {
            int color;
            switch (day.getType()) {
                case HOLIDAY:
                    color = Color.parseColor("#FF0000");
                    break;
                case NON_COMMERCE_DAY:
                    color = Color.parseColor("#FF1493");
                    break;
                case COMMERCE_SUNDAY:
                    color = ContextCompat.getColor(context, android.R.color.holo_green_dark);
                    break;
                default:
                    color = 0;
                    break;
            }
            return color;
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
