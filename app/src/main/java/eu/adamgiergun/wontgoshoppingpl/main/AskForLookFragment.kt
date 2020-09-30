package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import eu.adamgiergun.wontgoshoppingpl.databinding.AskForLookFragmentBinding

internal class AskForLookFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AskForLookFragmentBinding.inflate(inflater).root
    }
}