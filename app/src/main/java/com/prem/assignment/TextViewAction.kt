package com.prem.assignment

import android.graphics.Typeface
import android.widget.TextView

data class TextViewAction(
    var actionType: ActionType,
    val textView: TextView,
    val oldText: String,
    val oldTextSize: Int,
    val oldTypeface: Typeface?
)

enum class ActionType {
    ADD,
    REMOVE,
    UPDATE_TEXT_SIZE,
    UPDATE_TEXT,
    UPDATE_TEXT_STYLE_IS_BOLD,
    UPDATE_TEXT_STYLE_ITALIC,
    UPDATE_FONT
}
