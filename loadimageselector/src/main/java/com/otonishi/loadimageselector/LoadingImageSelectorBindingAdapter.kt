package com.otonishi.loadimageselector

import androidx.databinding.BindingAdapter

@BindingAdapter("lis_selected")
fun LoadingImageSelector.setSelectorSelected(
    isSelectorSelected: Boolean?
) {
    if (isSelectorSelected != null) {
        this.setSelectorSelected(isSelectorSelected)
    }
}

@BindingAdapter("lis_state")
fun LoadingImageSelector.setState(
    lisState: Int?
) {
    if (lisState != null) {
        this.setState(LoadingImageSelector.State.findStateByValue(lisState))
    }
}