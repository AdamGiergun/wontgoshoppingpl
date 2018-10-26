package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.*;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import static eu.adamgiergun.wontgoshoppingpl.LimitedCommerceDay.Type.*;

public class DayFragment extends Fragment {

    private OnListFragmentInteractionListener listener;

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
            DayRecyclerViewAdapter dayRecyclerViewAdapter = new DayRecyclerViewAdapter(getListOfLimitedCommerceDays(context), listener);
            boolean isLookRich = new AppPreferences(getContext()).isLookRich(getContext());
            dayRecyclerViewAdapter.setLookRich(isLookRich);
            recyclerView.setAdapter(dayRecyclerViewAdapter);
/*            Calendar calEnd = Calendar.getInstance();
            Long timeDifference = calEnd.getTimeInMillis() - calStart.getTimeInMillis();
            Log.d("TIME", String.valueOf(timeDifference));
*/
        }
        return view;
    }

    private ListOfLimitedCommerceDays getListOfLimitedCommerceDays(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        ArrayList<LimitedCommerceDay.Type> types = new ArrayList<>();
        if (appPreferences.showHolidays(context))
            types.add(HOLIDAY);
        if (appPreferences.showNonCommerceSundays(context))
            types.add(NON_COMMERCE_DAY);
        if (appPreferences.showCommerceSundays(context))
            types.add(COMMERCE_SUNDAY);
        return ListOfLimitedCommerceDays.getListOfLimitedCommerceDays(context, types);
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
