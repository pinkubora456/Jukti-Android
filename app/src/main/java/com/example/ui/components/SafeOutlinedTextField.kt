package com.example.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

private class InputTracker {
    var lastInputTime: Long = 0L
    var lastInsertedChar: Char? = null
    var lastInsertionIdx: Int = -1
}

fun deduplicateStringInput(oldVal: String, newVal: String): String {
    if (newVal.length > oldVal.length + 1) {
        var prefixLen = 0
        while (prefixLen < oldVal.length && prefixLen < newVal.length && oldVal[prefixLen] == newVal[prefixLen]) {
            prefixLen++
        }
        var suffixLen = 0
        while (suffixLen < (oldVal.length - prefixLen) &&
            suffixLen < (newVal.length - prefixLen) &&
            oldVal[oldVal.length - 1 - suffixLen] == newVal[newVal.length - 1 - suffixLen]
        ) {
            suffixLen++
        }
        val insertedLen = newVal.length - prefixLen - suffixLen
        if (insertedLen > 1) {
            val inserted = newVal.substring(prefixLen, prefixLen + insertedLen)
            val firstChar = inserted[0]
            if (inserted.all { it == firstChar }) {
                return newVal.removeRange(prefixLen, prefixLen + insertedLen - 1)
            }
        }
    }
    return newVal
}

fun deduplicateTextFieldValue(oldVal: TextFieldValue, newVal: TextFieldValue): TextFieldValue {
    val sanitizedText = deduplicateStringInput(oldVal.text, newVal.text)
    if (sanitizedText != newVal.text) {
        val diff = newVal.text.length - sanitizedText.length
        val newCursor = (newVal.selection.start - diff).coerceIn(0, sanitizedText.length)
        return TextFieldValue(
            text = sanitizedText,
            selection = TextRange(newCursor)
        )
    }
    return newVal
}

@Composable
fun SafeOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    val tracker = remember { InputTracker() }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val deduplicated = deduplicateStringInput(value, newValue)
            val now = System.currentTimeMillis()
            val timeDiff = now - tracker.lastInputTime

            if (deduplicated.length == value.length + 1) {
                var insertIndex = 0
                while (insertIndex < value.length && insertIndex < deduplicated.length && value[insertIndex] == deduplicated[insertIndex]) {
                    insertIndex++
                }
                if (insertIndex < deduplicated.length) {
                    val insertedChar = deduplicated[insertIndex]
                    if (timeDiff <= 50L && insertedChar == tracker.lastInsertedChar &&
                        (insertIndex == tracker.lastInsertionIdx || insertIndex == tracker.lastInsertionIdx + 1)
                    ) {
                        return@OutlinedTextField
                    }
                    tracker.lastInsertedChar = insertedChar
                    tracker.lastInsertionIdx = insertIndex
                    tracker.lastInputTime = now
                }
            } else if (deduplicated != value) {
                tracker.lastInsertedChar = null
                tracker.lastInsertionIdx = -1
                tracker.lastInputTime = now
            }

            onValueChange(deduplicated)
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors
    )
}

@Composable
fun SafeOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    val tracker = remember { InputTracker() }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val deduplicated = deduplicateTextFieldValue(value, newValue)
            val now = System.currentTimeMillis()
            val timeDiff = now - tracker.lastInputTime
            val oldText = value.text
            val newText = deduplicated.text

            if (newText.length == oldText.length + 1) {
                var insertIndex = 0
                while (insertIndex < oldText.length && insertIndex < newText.length && oldText[insertIndex] == newText[insertIndex]) {
                    insertIndex++
                }
                if (insertIndex < newText.length) {
                    val insertedChar = newText[insertIndex]
                    if (timeDiff <= 50L && insertedChar == tracker.lastInsertedChar &&
                        (insertIndex == tracker.lastInsertionIdx || insertIndex == tracker.lastInsertionIdx + 1)
                    ) {
                        return@OutlinedTextField
                    }
                    tracker.lastInsertedChar = insertedChar
                    tracker.lastInsertionIdx = insertIndex
                    tracker.lastInputTime = now
                }
            } else if (newText != oldText) {
                tracker.lastInsertedChar = null
                tracker.lastInsertionIdx = -1
                tracker.lastInputTime = now
            }

            onValueChange(deduplicated)
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors
    )
}
