package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst

class AskForLook : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.ask_for_look_fragment, container, false)
        setText(view)
        return view
    }

    private fun setText(parentView: View) {
        val textView = parentView.findViewById<TextView>(R.id.textView)
        textView.text = LocalizedConst.getAskForLook()
    }

    companion object {
        fun newInstance(): AskForLook {
            return AskForLook()
        }
    }
}