package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.designsystem.theme.AppTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    isPassword: Boolean = false,
    maxLength: Int? = null,
    imeAction: ImeAction = ImeAction.Done,
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = if (focused) AppTheme.colors.primary else if (!showLabel) AppTheme.colors.border else Color.Transparent
    val background = if (focused || !showLabel) AppTheme.colors.surface else AppTheme.colors.primaryTint(0.08f)
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        if (showLabel) BasicText(
            text = label,
            style = AppTheme.typography.caption.copy(color = AppTheme.colors.textSecondary),
            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp),
        )
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(56.dp)
                    .background(background, RoundedCornerShape(16.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                decorationBox = { editor ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (!showLabel && value.isEmpty()) BasicText(label, style = AppTheme.typography.body.copy(color = AppTheme.colors.textSecondary))
                        editor()
                    }
                },
                value = value,
                onValueChange = { newValue -> onValueChange(if (maxLength != null) newValue.take(maxLength) else newValue) },
                textStyle = AppTheme.typography.body.copy(color = AppTheme.colors.textPrimary),
                cursorBrush = SolidColor(AppTheme.colors.primary),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions =
                    KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = { focusManager.clearFocus() },
                    ),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
            )
        }
    }
}
