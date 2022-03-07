package com.bigteeti.sanhgame.ui.activity

import com.dakuinternational.common.domain.model.DataContent
import com.dakuinternational.common.ui.event.UiEvent

sealed class AppEvent: UiEvent(){
    data class DataReceived(var list: List<DataContent>): AppEvent()
    data class Selected(var data: DataContent): AppEvent()
}

