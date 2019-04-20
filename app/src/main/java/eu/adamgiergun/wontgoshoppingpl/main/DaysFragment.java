package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import eu.adamgiergun.wontgoshoppingpl.day.Day;
import eu.adamgiergun.wontgoshoppingpl.R;

public class DaysFragment extends Fragment {

    private OnListFragmentInteractionListener listener;
    private RecyclerView daysRecyclerView;
    private DaysRecyclerViewAdapter daysRecyclerViewAdapter;

    private ItemTouchHelper.SimpleCallback dayTouchHelper = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            deleteDayFromGoogleCalendar((DaysRecyclerViewAdapter.ViewHolder) viewHolder);
            daysRecyclerViewAdapter.notifyDataSetChanged();
        }

        private void deleteDayFromGoogleCalendar(DaysRecyclerViewAdapter.ViewHolder dayViewHolder) {
            Day day = dayViewHolder.getDay();
            day.deleteFromGoogleCalendar(getContext());
            refreshRecyclerView();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_list, container, false);
        if (view instanceof RecyclerView) {
            initializeRecyclerView(view);
        }
        return view;
    }

    private void initializeRecyclerView(View view) {
        daysRecyclerView = (RecyclerView) view;
        daysRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter();
        attachItemTouchHelper(daysRecyclerView);
    }

    private void setAdapter() {
        daysRecyclerViewAdapter = DaysRecyclerViewAdapter.newInstance(getContext(), listener);
        daysRecyclerView.setAdapter(daysRecyclerViewAdapter);
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

    void refreshRecyclerView() {
        setAdapter();
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Day day);
    }
}
