package com.lessons.convertorjpgtopng

import moxy.MvpView
import moxy.viewstate.strategy.alias.SingleState

@SingleState
interface MainView : MvpView {
    fun setResult(completed: Boolean)
    fun setError()
}