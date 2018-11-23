package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

public class DayFragment extends Fragment {

    private OnListFragmentInteractionListener listener;
    private DayRecyclerViewAdapter dayRecyclerViewAdapter;

    private ItemTouchHelper.SimpleCallback dayTouchHelper = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            deleteDayFromGoogleCalendar((DayRecyclerViewAdapter.ViewHolder) viewHolder);
            dayRecyclerViewAdapter.notifyDataSetChanged();
        }

        private void deleteDayFromGoogleCalendar(DayRecyclerViewAdapter.ViewHolder dayViewHolder) {
            LimitedCommerceDay day = dayViewHolder.getDay();
            day.deleteFromGoogleCalendar(getContext());
        }
    };

    public DayFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_list, container, false);
        if (view instanceof RecyclerView) {
            initializeRecyclerView(view);
        }
        return view;
    }

    private void initializeRecyclerView(View view) {
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(setAndGetDayRecyclerViewAdapter());
        attachItemTouchHelper(recyclerView);
    }

    private DayRecyclerViewAdapter setAndGetDayRecyclerViewAdapter() {
         dayRecyclerViewAdapter = DayRecyclerViewAdapter.getInstance(getContext(), listener);
         return dayRecyclerViewAdapter;
    }

    private void attachItemTouchHelper(RecyclerView recyclerView) {
        ItemTouchHelper dayTouchHelper = new ItemTouchHelper(this.dayTouchHelper);
        dayTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(LimitedCommerceDay day);
    }
}
