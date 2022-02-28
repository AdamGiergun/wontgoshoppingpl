package eu.adamgiergun.wontgoshoppingpl.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import eu.adamgiergun.wontgoshoppingpl.databinding.WidgetInfoFragmentBinding
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.MainViewModel

internal class WidgetInfoFragment : Fragment() {

    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return WidgetInfoFragmentBinding.inflate(inflater).run {
            mainViewModel = model
            root
        }
    }
}