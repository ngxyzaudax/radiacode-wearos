package com.ngxyzaudax.radiacode.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ngxyzaudax.radiacode.RadiaCodeApp

class MainViewModelFactory(private val app: RadiaCodeApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainViewModel(app) as T
}
