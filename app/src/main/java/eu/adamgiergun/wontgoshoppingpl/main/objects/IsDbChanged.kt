package eu.adamgiergun.wontgoshoppingpl.main.objects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

internal object IsDbChanged {
    private val _value = MutableLiveData<Boolean>()
    val value: LiveData<Boolean>
        get() = _value

    fun set(value: Boolean) {
        _value.value = value
    }
}