package eu.adamgiergun.wontgoshoppingpl.main

import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R

internal open class MyDialogFragment : NoticeDialogFragment() {

    fun getStyledBuilder() = if (mainViewModel.isLookRich)
        AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
    else
        AlertDialog.Builder(requireContext())

    val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }
}