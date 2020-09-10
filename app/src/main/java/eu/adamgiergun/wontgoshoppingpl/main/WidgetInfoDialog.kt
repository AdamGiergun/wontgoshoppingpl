package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R

internal class WidgetInfoDialog : MyDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val builder = getStyledBuilder()
        builder.setView(dialogView)
        return builder.create()
    }

    private val dialogView: View
        get() {
            val view = requireActivity().layoutInflater.inflate(R.layout.info_about_app_widget, null)
            view.setOnClickListener {
                mainViewModel.setAppWidgetInfoDisplayed()
                if (dialog != null) dialog!!.dismiss()
            }
            return view
        }

    companion object {
        internal fun newInstance(): NoticeDialogFragment {
            return WidgetInfoDialog()
        }
    }
}