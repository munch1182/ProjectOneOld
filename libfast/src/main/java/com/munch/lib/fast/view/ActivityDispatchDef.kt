package com.munch.lib.fast.view

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Create by munch1182 on 2022/5/6 17:11.
 */

fun supportDef(dialogCreator: ((AppCompatActivity) -> BottomSheetDialogFragment)? = null): ActivityDispatch {
    return SupportActionBar + SupportConfigDialog(dialogCreator)
}