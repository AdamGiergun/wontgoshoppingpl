package eu.adamgiergun.niekupiszpl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DayFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onListFragmentInteraction(CommerceFreeDay day) {
        if (day.addToGoogleCalendar())
            showInfo(R.string.info_added_to_calendar);
        else
            showInfo(R.string.error_adding_to_calendar_failed);
    }

    private void showInfo(int info) {
        Toast.makeText(
                getBaseContext(),
                info,
                Toast.LENGTH_SHORT)
                .show();
    }
}
