package eu.adamgiergun.wontgoshoppingpl.main

import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.wontgoshoppingpl.R

internal abstract class MainDialogFragment : AppCompatDialogFragment() {

    protected fun getStyledBuilder() = if (mainViewModel.isLookRich)
        AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
    else
        AlertDialog.Builder(requireContext())

    protected val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    protected val showInfoCancelled = DialogInterface.OnClickListener { _, _ -> Toast.makeText(context, R.string.info_cancelled, Toast.LENGTH_SHORT).show() }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mainViewModel.setActionsNone()
    }
}