package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.databinding.InfoAboutAppWidgetBinding

internal class WidgetInfoDialog : MyDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val builder = getStyledBuilder()
        builder.setView(dialogView)
        return builder.create()
    }

    private val dialogView: View
        get() {
            return InfoAboutAppWidgetBinding.inflate(requireActivity().layoutInflater).run {
                root.apply {
                    setOnClickListener {
                        mainViewModel.setAppWidgetInfoDisplayed()
                        dialog?.run { dismiss() }
                    }
                }
            }
        }
}