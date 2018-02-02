package eu.adamgiergun.niekupiszpl;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        ListOfCommerceFreeDays nonCommerceDays = new ListOfCommerceFreeDays(resources, R.array.non_commerce_days);
        ListOfCommerceFreeDays nationalHolidays = new ListOfCommerceFreeDays(resources, R.array.national_holidays);
        TextView testText = findViewById(R.id.test_text);
        CommerceFreeDay commerceFreeDay = nonCommerceDays.get(2);
        String test = commerceFreeDay.name + " " + commerceFreeDay.startTime.toString();
        testText.setText(test);
    }
}
