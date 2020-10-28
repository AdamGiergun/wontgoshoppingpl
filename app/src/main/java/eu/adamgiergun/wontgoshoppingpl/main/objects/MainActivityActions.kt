package eu.adamgiergun.wontgoshoppingpl.main.objects

import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity.Actions

internal object MainActivityActions {
    val nextAction = MutableLiveData<Actions>()

    fun setNextAction(action: Actions) {
        nextAction.value = action
    }

    var lastActionIsAddList = false
}