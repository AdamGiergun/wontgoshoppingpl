package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import eu.adamgiergun.wontgoshoppingpl.databinding.WidgetInfoFragmentBinding

internal class WidgetInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return WidgetInfoFragmentBinding.inflate(inflater).root
    }
}