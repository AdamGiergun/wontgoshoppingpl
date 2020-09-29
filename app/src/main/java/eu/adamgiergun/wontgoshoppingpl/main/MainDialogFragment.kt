package eu.adamgiergun.wontgoshoppingpl.main

import android.content.DialogInterface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import eu.adamgiergun.wontgoshoppingpl.R
import kotlinx.coroutines.launch

internal open class MainDialogFragment : AppCompatDialogFragment() {

    protected fun getStyledBuilder() = if (mainViewModel.isLookRich)
        AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
    else
        AlertDialog.Builder(requireContext())

    protected val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    protected fun getOnClickListener(dialog: MainDialogFragment) = View.OnClickListener {
        activity?.supportFragmentManager?.let { fragmentManager ->
            dialog.show(fragmentManager, "")
        }
    }

    protected val addToCalendar = DialogInterface.OnClickListener { _, _ -> lifecycleScope.launch { mainViewModel.addToCalendar() } }

    protected val showInfoCancelled = DialogInterface.OnClickListener { _, _ -> Toast.makeText(context, R.string.info_cancelled, Toast.LENGTH_SHORT).show() }
}