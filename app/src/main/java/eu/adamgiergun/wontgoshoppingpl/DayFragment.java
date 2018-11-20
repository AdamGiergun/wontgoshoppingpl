package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import java.util.Calendar;

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
            DayRecyclerViewAdapter.ViewHolder dayViewHolder = (DayRecyclerViewAdapter.ViewHolder) viewHolder;
            dayViewHolder.day.deleteFromGoogleCalendar(getContext());
            dayRecyclerViewAdapter.notifyDataSetChanged();
        }
    };

    public DayFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            Calendar calStart = Calendar.getInstance();
            dayRecyclerViewAdapter = new DayRecyclerViewAdapter(getListOfLimitedCommerceDays(context), listener);
            boolean isLookRich = new AppPreferences(context).isLookRich(context);
            dayRecyclerViewAdapter.setLookRich(isLookRich);
            recyclerView.setAdapter(dayRecyclerViewAdapter);
            ItemTouchHelper dayTouchHelper = new ItemTouchHelper(this.dayTouchHelper);
            dayTouchHelper.attachToRecyclerView(recyclerView);

/*            Calendar calEnd = Calendar.getInstance();
            String timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
            Toast.makeText(container.getContext(), timeDifference, Toast.LENGTH_SHORT).show();*/
//            Log.d("LIST_TIME", String.valueOf(timeDifference));

        }
        return view;
    }

    private ListOfLimitedCommerceDays getListOfLimitedCommerceDays(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        LimitedCommerceDayType.TypesList types = appPreferences.getLimitedCommerceDayTypes(context);
        return ListOfLimitedCommerceDays.getList(context, types);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(LimitedCommerceDay day);
    }
}
