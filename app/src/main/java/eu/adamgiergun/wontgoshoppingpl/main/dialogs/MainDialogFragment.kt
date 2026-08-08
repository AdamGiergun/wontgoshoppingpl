package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import android.content.DialogInterface
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.adamgiergun.wontgoshoppingpl.R
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel

internal abstract class MainDialogFragment : AppCompatDialogFragment() {

    /**
     * Dialogi biorą styl z motywu Material 3. Wcześniej istniały w dwóch
     * wariantach — zwykłym i „rich" z pomarańczowym tłem — wybieranych przy
     * każdym otwarciu.
     */
    protected fun getStyledBuilder() = MaterialAlertDialogBuilder(requireContext())

    protected val mViewModel: DaysFragmentViewModel by lazy {
        ViewModelProvider(requireActivity())[DaysFragmentViewModel::class.java]
    }

    protected val showInfoCancelled = DialogInterface.OnClickListener { _, _ -> Toast.makeText(context, R.string.info_cancelled, Toast.LENGTH_SHORT).show() }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mViewModel.setActionNone()
    }
}