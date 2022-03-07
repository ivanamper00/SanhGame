package com.bigteeti.sanhgame.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dakuinternational.common.domain.model.DataContent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(): ViewModel() {

    fun sendData(data: List<DataContent>) {
        _uiEvent.value = AppEvent.DataReceived(data)
    }

    private val _uiEvent = MutableLiveData<AppEvent>()
    val uiEvent: LiveData<AppEvent> get() = _uiEvent
}