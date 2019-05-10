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

    private ItemTouchHelper.SimpleCallback dayTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            DaysRecyclerViewAdapter.ViewHolder dayViewHolder = (DaysRecyclerViewAdapter.ViewHolder) viewHolder;
            dayViewHolder.getDay().deleteFromGoogleCalendar(getContext().getApplicationContext());
            setRecyclerViewAdapter();
            //daysRecyclerViewAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_list, container, false);
        if (view instanceof RecyclerView) {
            daysRecyclerView = (RecyclerView) view;
            daysRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            setRecyclerViewAdapter();
            attachItemTouchHelper(daysRecyclerView);
        }
        return view;
    }

    void setRecyclerViewAdapter() {
        DaysRecyclerViewAdapter daysRecyclerViewAdapter = DaysRecyclerViewAdapter.newInstance(getContext().getApplicationContext(), listener);
        daysRecyclerView.setAdapter(daysRecyclerViewAdapter);
    }

    private void attachItemTouchHelper(RecyclerView recyclerView) {
        ItemTouchHelper dayTouchHelper = new ItemTouchHelper(dayTouchHelperCallback);
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
        void onListFragmentInteraction(Day day);
    }
}
