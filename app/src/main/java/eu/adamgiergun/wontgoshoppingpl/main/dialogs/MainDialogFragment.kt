package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel

internal abstract class MainDialogFragment : AppCompatDialogFragment() {

    protected fun getStyledBuilder() = if (mViewModel.isLookRich)
        AlertDialog.Builder(requireContext(), R.style.RichAlertDialogStyle)
    else
        AlertDialog.Builder(requireContext())

    protected val mViewModel: DaysFragmentViewModel by lazy {
        ViewModelProvider(requireActivity())[DaysFragmentViewModel::class.java]
    }

    protected val showInfoCancelled = DialogInterface.OnClickListener { _, _ -> Toast.makeText(context, R.string.info_cancelled, Toast.LENGTH_SHORT).show() }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mViewModel.setActionNone()
    }
}