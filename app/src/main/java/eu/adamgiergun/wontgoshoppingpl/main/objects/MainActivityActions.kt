package eu.adamgiergun.wontgoshoppingpl.main.objects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity.Actions

internal object MainActivityActions {

    @Suppress("ObjectPropertyName")
    private val _nextAction = MutableLiveData<Actions>()

    val nextAction : LiveData<Actions>
        get() = _nextAction

    fun setNextAction(action: Actions) {
        _nextAction.value = action
    }

    var lastActionIsAddList = false
}