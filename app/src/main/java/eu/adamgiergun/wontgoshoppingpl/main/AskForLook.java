package eu.adamgiergun.wontgoshoppingpl.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst;

public class AskForLook extends Fragment {

    static AskForLook newInstance() {
        return new AskForLook();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ask_for_look_fragment, container, false);
        setText(view);
        return view;
    }

    private void setText(View parentView) {
        TextView textView = parentView.findViewById(R.id.textView);
        textView.setText(LocalizedConst.getAskForLook());
    }
}
