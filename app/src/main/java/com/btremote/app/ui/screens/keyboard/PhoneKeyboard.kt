package com.btremote.app.ui.screens.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.btremote.app.bluetooth.HidKeyCode
import com.btremote.app.bluetooth.MODIFIER_LEFT_ALT
import com.btremote.app.bluetooth.MODIFIER_LEFT_CTRL
import com.btremote.app.bluetooth.MODIFIER_LEFT_GUI
import com.btremote.app.bluetooth.MODIFIER_LEFT_SHIFT

private enum class PhoneAction {
    CHAR, CODE, SHIFT, CTRL, ALT, GUI, BACKSPACE, ENTER, SPACE, PAGE, SPACER
}

private data class PhoneKey(
    val label: String,
    val action: PhoneAction,
    val weight: Float = 1f,
    val char: Char? = null,
    val code: HidKeyCode? = null,
    val page: Int = 0
)

private fun charKey(label: String, char: Char = label.first()) =
    PhoneKey(label, PhoneAction.CHAR, char = char)

private fun codeKey(label: String, code: HidKeyCode, weight: Float = 1f) =
    PhoneKey(label, PhoneAction.CODE, weight = weight, code = code)

private fun shiftKey() = PhoneKey("SHIFT", PhoneAction.SHIFT, weight = 1.4f)
private fun backspaceKey() = PhoneKey("DEL", PhoneAction.BACKSPACE, weight = 1.4f)
private fun enterKey() = PhoneKey("ENTER", PhoneAction.ENTER, weight = 1.4f)
private fun spaceKey(weight: Float = 3.4f) = PhoneKey("SPACE", PhoneAction.SPACE, weight = weight)
private fun pageKey(label: String, page: Int, weight: Float = 1.4f) =
    PhoneKey(label, PhoneAction.PAGE, weight = weight, page = page)
private fun spacerKey(weight: Float = 0.5f) = PhoneKey("", PhoneAction.SPACER, weight = weight)

@Composable
fun PhoneKeyboard(viewModel: KeyboardViewModel) {
    var page by remember { mutableStateOf(0) }
    var shift by remember { mutableStateOf(false) }
    var ctrl by remember { mutableStateOf(false) }
    var alt by remember { mutableStateOf(false) }
    var gui by remember { mutableStateOf(false) }

    fun activeModifiers(): Int {
        var mod = 0
        if (shift) mod = mod or MODIFIER_LEFT_SHIFT
        if (ctrl) mod = mod or MODIFIER_LEFT_CTRL
        if (alt) mod = mod or MODIFIER_LEFT_ALT
        if (gui) mod = mod or MODIFIER_LEFT_GUI
        return mod
    }

    fun resetModifiers() {
        shift = false
        ctrl = false
        alt = false
        gui = false
    }

    fun sendChar(char: Char) {
        val (code, baseModifier) = HidKeyCode.fromChar(char) ?: return
        viewModel.pressKey(code, baseModifier or activeModifiers())
        resetModifiers()
    }

    fun sendCode(code: HidKeyCode) {
        viewModel.pressKey(code, activeModifiers())
        resetModifiers()
    }

    fun handleKey(key: PhoneKey) {
        when (key.action) {
            PhoneAction.CHAR -> {
                val char = key.char ?: return
                sendChar(if (shift) char.uppercaseChar() else char)
            }
            PhoneAction.CODE -> key.code?.let { sendCode(it) }
            PhoneAction.SHIFT -> shift = !shift
            PhoneAction.CTRL -> ctrl = !ctrl
            PhoneAction.ALT -> alt = !alt
            PhoneAction.GUI -> gui = !gui
            PhoneAction.BACKSPACE -> sendCode(HidKeyCode.BACKSPACE)
            PhoneAction.ENTER -> sendCode(HidKeyCode.ENTER)
            PhoneAction.SPACE -> sendCode(HidKeyCode.SPACE)
            PhoneAction.PAGE -> page = key.page
            PhoneAction.SPACER -> Unit
        }
    }

    val rows = when (page) {
        0 -> listOf(
            "qwertyuiop".map { charKey(it.toString(), it) },
            listOf(spacerKey()) + "asdfghjkl".map { charKey(it.toString(), it) } + listOf(spacerKey()),
            listOf(shiftKey()) + "zxcvbnm".map { charKey(it.toString(), it) } + listOf(backspaceKey()),
            listOf(
                pageKey("?123", 1),
                charKey(",", ','),
                spaceKey(),
                charKey(".", '.'),
                enterKey()
            )
        )
        1 -> listOf(
            "1234567890".map { charKey(it.toString(), it) },
            listOf('@', '#', '$', '%', '&', '*', '-', '+', '(', ')').map { charKey(it.toString(), it) },
            listOf('/', ':', ';', '"', '\'', '!', '?', ',', '.').map { charKey(it.toString(), it) } +
                listOf(backspaceKey()),
            listOf(
                pageKey("ABC", 0),
                pageKey("#+=", 2),
                spaceKey(3.2f),
                enterKey()
            )
        )
        else -> listOf(
            listOf('[', ']', '{', '}', '<', '>', '\\', '|', '^', '~').map { charKey(it.toString(), it) },
            listOf('_', '=', '+', '`', '!', '?', ':', ';', '"', '\'').map { charKey(it.toString(), it) },
            listOf('@', '#', '$', '%', '&', '*', '-', '(', ')').map { charKey(it.toString(), it) } +
                listOf(backspaceKey()),
            listOf(
                pageKey("123", 1),
                pageKey("ABC", 0),
                spaceKey(3.2f),
                enterKey()
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToolbarKey("ESC", Modifier.weight(1f)) { sendCode(HidKeyCode.ESCAPE) }
            ToolbarKey("TAB", Modifier.weight(1f)) { sendCode(HidKeyCode.TAB) }
            ToolbarKey("CTRL", Modifier.weight(1f), active = ctrl) { ctrl = !ctrl }
            ToolbarKey("ALT", Modifier.weight(1f), active = alt) { alt = !alt }
            ToolbarKey("←", Modifier.weight(1f)) { sendCode(HidKeyCode.LEFT_ARROW) }
            ToolbarKey("↑", Modifier.weight(1f)) { sendCode(HidKeyCode.UP_ARROW) }
            ToolbarKey("↓", Modifier.weight(1f)) { sendCode(HidKeyCode.DOWN_ARROW) }
            ToolbarKey("→", Modifier.weight(1f)) { sendCode(HidKeyCode.RIGHT_ARROW) }
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    if (key.action == PhoneAction.SPACER) {
                        Spacer(Modifier.weight(key.weight))
                    } else {
                        PhoneKeyButton(
                            key = key,
                            shift = shift,
                            ctrl = ctrl,
                            alt = alt,
                            gui = gui,
                            modifier = Modifier.weight(key.weight),
                            onKey = ::handleKey
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneKeyButton(
    key: PhoneKey,
    shift: Boolean,
    ctrl: Boolean,
    alt: Boolean,
    gui: Boolean,
    modifier: Modifier = Modifier,
    onKey: (PhoneKey) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val active = when (key.action) {
        PhoneAction.SHIFT -> shift
        PhoneAction.CTRL -> ctrl
        PhoneAction.ALT -> alt
        PhoneAction.GUI -> gui
        else -> false
    }
    val background by animateColorAsState(
        targetValue = when {
            pressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            active -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(80),
        label = "phoneKeyBackground"
    )
    val foreground by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(80),
        label = "phoneKeyForeground"
    )
    val outline = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val display = if (shift && key.action == PhoneAction.CHAR) key.label.uppercase() else key.label

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, outline, RoundedCornerShape(10.dp))
            .pointerInput(key) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onKey(key) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = display,
            color = foreground,
            fontSize = if (key.label.length > 3) 10.sp else 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun ToolbarKey(
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val background by animateColorAsState(
        targetValue = when {
            pressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            active -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(80),
        label = "toolbarKeyBackground"
    )
    val foreground by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(80),
        label = "toolbarKeyForeground"
    )
    val outline = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, outline, RoundedCornerShape(8.dp))
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = foreground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
