package com.example.flyingandroidclient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {

    private val _optionsVisibility = MutableLiveData<Boolean>(false)

    val optionsVisibility: LiveData<Boolean>
        get() {
            return _optionsVisibility
        }

    fun toggleOptionsVisibility() {
        _optionsVisibility.value = !_optionsVisibility.value!!
    }
}