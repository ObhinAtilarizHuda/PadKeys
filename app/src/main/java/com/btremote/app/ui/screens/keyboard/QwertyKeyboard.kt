package com.btremote.app.ui.screens.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

private data class KKey(
    val label: String,
    val code: HidKeyCode? = null,
    val modFlag: Int = 0,
    val weight: Float = 1f,
    val isModifier: Boolean = false
)

@Composable
fun QwertyKeyboard(viewModel: KeyboardViewModel) {
    var shift by remember { mutableStateOf(false) }
    var ctrl by remember { mutableStateOf(false) }
    var alt by remember { mutableStateOf(false) }
    var gui by remember { mutableStateOf(false) }

    fun send(code: HidKeyCode, fixedMod: Int = 0) {
        var mod = fixedMod
        if (shift) mod = mod or MODIFIER_LEFT_SHIFT
        if (ctrl) mod = mod or MODIFIER_LEFT_CTRL
        if (alt) mod = mod or MODIFIER_LEFT_ALT
        if (gui) mod = mod or MODIFIER_LEFT_GUI
        viewModel.pressKey(code, mod)
        shift = false
        ctrl = false
        alt = false
        gui = false
    }

    fun toggleModifier(flag: Int) {
        when (flag) {
            MODIFIER_LEFT_SHIFT -> shift = !shift
            MODIFIER_LEFT_CTRL -> ctrl = !ctrl
            MODIFIER_LEFT_ALT -> alt = !alt
            MODIFIER_LEFT_GUI -> gui = !gui
        }
    }

    val fRow = (1..12).map { i ->
        KKey("F$i", HidKeyCode.valueOf("F$i"), weight = 0.7f)
    }
    val numberRow = listOf(
        KKey("`", HidKeyCode.GRAVE), KKey("1", HidKeyCode.NUM_1), KKey("2", HidKeyCode.NUM_2),
        KKey("3", HidKeyCode.NUM_3), KKey("4", HidKeyCode.NUM_4), KKey("5", HidKeyCode.NUM_5),
        KKey("6", HidKeyCode.NUM_6), KKey("7", HidKeyCode.NUM_7), KKey("8", HidKeyCode.NUM_8),
        KKey("9", HidKeyCode.NUM_9), KKey("0", HidKeyCode.NUM_0), KKey("-", HidKeyCode.MINUS),
        KKey("=", HidKeyCode.EQUAL), KKey("Bksp", HidKeyCode.BACKSPACE, weight = 1.6f)
    )
    val qRow = listOf(
        KKey("Tab", HidKeyCode.TAB, weight = 1.3f),
        KKey("Q", HidKeyCode.Q), KKey("W", HidKeyCode.W), KKey("E", HidKeyCode.E),
        KKey("R", HidKeyCode.R), KKey("T", HidKeyCode.T), KKey("Y", HidKeyCode.Y),
        KKey("U", HidKeyCode.U), KKey("I", HidKeyCode.I), KKey("O", HidKeyCode.O),
        KKey("P", HidKeyCode.P), KKey("[", HidKeyCode.LEFT_BRACKET),
        KKey("]", HidKeyCode.RIGHT_BRACKET), KKey("\\", HidKeyCode.BACKSLASH)
    )
    val homeRow = listOf(
        KKey("Caps", HidKeyCode.CAPS_LOCK, weight = 1.5f),
        KKey("A", HidKeyCode.A), KKey("S", HidKeyCode.S), KKey("D", HidKeyCode.D),
        KKey("F", HidKeyCode.F), KKey("G", HidKeyCode.G), KKey("H", HidKeyCode.H),
        KKey("J", HidKeyCode.J), KKey("K", HidKeyCode.K), KKey("L", HidKeyCode.L),
        KKey(";", HidKeyCode.SEMICOLON), KKey("'", HidKeyCode.QUOTE),
        KKey("Enter", HidKeyCode.ENTER, weight = 1.7f)
    )
    val bottomRow = listOf(
        KKey("Shift", null, modFlag = MODIFIER_LEFT_SHIFT, weight = 1.9f, isModifier = true),
        KKey("Z", HidKeyCode.Z), KKey("X", HidKeyCode.X), KKey("C", HidKeyCode.C),
        KKey("V", HidKeyCode.V), KKey("B", HidKeyCode.B), KKey("N", HidKeyCode.N),
        KKey("M", HidKeyCode.M), KKey(",", HidKeyCode.COMMA), KKey(".", HidKeyCode.PERIOD),
        KKey("/", HidKeyCode.SLASH),
        KKey("Shift", null, modFlag = MODIFIER_LEFT_SHIFT, weight = 1.9f, isModifier = true)
    )
    val spaceRow = listOf(
        KKey("Ctrl", null, modFlag = MODIFIER_LEFT_CTRL, weight = 1.2f, isModifier = true),
        KKey("Alt", null, modFlag = MODIFIER_LEFT_ALT, weight = 1.2f, isModifier = true),
        KKey("Space", HidKeyCode.SPACE, weight = 4.5f),
        KKey("Alt", null, modFlag = MODIFIER_LEFT_ALT, weight = 1.2f, isModifier = true),
        KKey("Ctrl", null, modFlag = MODIFIER_LEFT_CTRL, weight = 1.2f, isModifier = true),
        KKey("←", HidKeyCode.LEFT_ARROW, weight = 0.9f),
        KKey("↓", HidKeyCode.DOWN_ARROW, weight = 0.9f),
        KKey("↑", HidKeyCode.UP_ARROW, weight = 0.9f),
        KKey("→", HidKeyCode.RIGHT_ARROW, weight = 0.9f)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(fRow) { key ->
                val active = false
                val bg = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val fg = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .pointerInput(key.label) {
                            detectTapGestures(onTap = {
                                key.code?.let { send(it) }
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(key.label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        KeyRow(numberRow, shift, ctrl, alt, gui, { key ->
            if (key.isModifier) toggleModifier(key.modFlag) else key.code?.let { send(it) }
        })
        KeyRow(qRow, shift, ctrl, alt, gui, { key ->
            if (key.isModifier) toggleModifier(key.modFlag) else key.code?.let { send(it) }
        })
        KeyRow(homeRow, shift, ctrl, alt, gui, { key ->
            if (key.isModifier) toggleModifier(key.modFlag) else key.code?.let { send(it) }
        })
        KeyRow(bottomRow, shift, ctrl, alt, gui, { key ->
            if (key.isModifier) toggleModifier(key.modFlag) else key.code?.let { send(it) }
        })
        KeyRow(spaceRow, shift, ctrl, alt, gui, { key ->
            if (key.isModifier) toggleModifier(key.modFlag) else key.code?.let { send(it) }
        })
    }
}

@Composable
private fun KeyRow(
    keys: List<KKey>,
    shift: Boolean,
    ctrl: Boolean,
    alt: Boolean,
    gui: Boolean,
    onPress: (KKey) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEach { key ->
            val active = when (key.modFlag) {
                MODIFIER_LEFT_SHIFT -> shift && key.isModifier
                MODIFIER_LEFT_CTRL -> ctrl && key.isModifier
                MODIFIER_LEFT_ALT -> alt && key.isModifier
                MODIFIER_LEFT_GUI -> gui && key.isModifier
                else -> false
            }
            val bg = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
            val fg = if (active) Color.White else MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .weight(key.weight)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .pointerInput(key.label) {
                        detectTapGestures(onTap = { onPress(key) })
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    key.label,
                    color = fg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
