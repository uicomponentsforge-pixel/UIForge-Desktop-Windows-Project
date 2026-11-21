package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette
import java.text.DecimalFormat

private enum class CalcOp(val symbol: String) {
    ADD("+"),
    SUB("−"),
    MUL("×"),
    DIV("÷")
}

@Composable
fun CalculatorApp() {
    val palette = LocalDesktopPalette.current
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<CalcOp?>(null) }
    var resetOnNextDigit by remember { mutableStateOf(false) }
    var expressionPreview by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val formatter = remember { DecimalFormat("#.##########") }

    fun formatValue(v: Double): String {
        if (v.isNaN() || v.isInfinite()) return "Error"
        val rounded = if (v == -0.0) 0.0 else v
        return formatter.format(rounded)
    }

    fun compute(a: Double, b: Double, op: CalcOp): Double? {
        return when (op) {
            CalcOp.ADD -> a + b
            CalcOp.SUB -> a - b
            CalcOp.MUL -> a * b
            CalcOp.DIV -> if (b == 0.0) null else a / b
        }
    }

    fun onDigit(digit: String) {
        errorMessage = null
        if (resetOnNextDigit || display == "0") {
            display = digit
            resetOnNextDigit = false
        } else if (display.length < 16) {
            display += digit
        }
    }

    fun onDecimal() {
        errorMessage = null
        if (resetOnNextDigit) {
            display = "0."
            resetOnNextDigit = false
        } else if (!display.contains(".")) {
            display += "."
        }
    }

    fun onOperator(op: CalcOp) {
        errorMessage = null
        val currentVal = display.toDoubleOrNull() ?: return
        if (operand1 != null && pendingOp != null && !resetOnNextDigit) {
            val result = compute(operand1!!, currentVal, pendingOp!!)
            if (result == null) {
                errorMessage = "Cannot divide by zero"
                operand1 = null
                pendingOp = null
                display = "0"
                expressionPreview = ""
                resetOnNextDigit = true
                return
            }
            operand1 = result
            display = formatValue(result)
        } else {
            operand1 = currentVal
        }
        pendingOp = op
        expressionPreview = "${formatValue(operand1!!)} ${op.symbol}"
        resetOnNextDigit = true
    }

    fun onEquals() {
        errorMessage = null
        val op = pendingOp ?: return
        val a = operand1 ?: return
        val b = display.toDoubleOrNull() ?: return
        val result = compute(a, b, op)
        if (result == null) {
            errorMessage = "Cannot divide by zero"
            expressionPreview = "${formatValue(a)} ${op.symbol} ${formatValue(b)}"
            display = "0"
            operand1 = null
            pendingOp = null
            resetOnNextDigit = true
            return
        }
        expressionPreview = "${formatValue(a)} ${op.symbol} ${formatValue(b)} ="
        display = formatValue(result)
        operand1 = null
        pendingOp = null
        resetOnNextDigit = true
    }

    fun onClearAll() {
        display = "0"
        operand1 = null
        pendingOp = null
        resetOnNextDigit = false
        expressionPreview = ""
        errorMessage = null
    }

    fun onBackspace() {
        errorMessage = null
        if (resetOnNextDigit) return
        display = if (display.length <= 1 || (display.length == 2 && display.startsWith("-"))) {
            "0"
        } else {
            display.dropLast(1)
        }
    }

    fun onPercent() {
        val current = display.toDoubleOrNull() ?: return
        display = formatValue(current / 100.0)
        resetOnNextDigit = true
    }

    fun onToggleSign() {
        val current = display.toDoubleOrNull() ?: return
        if (current != 0.0) {
            display = formatValue(-current)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Readout Panel
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = errorMessage ?: expressionPreview.ifEmpty { " " },
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = display,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_display")
            )
        }

        // Keypad Grid (5 rows x 4 columns)
        val rows = listOf(
            listOf("AC", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "=")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            rows.forEach { rowKeys ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    rowKeys.forEach { key ->
                        val isEquals = key == "="
                        val isOp = key in setOf("÷", "×", "−", "+")
                        val isUtil = key in setOf("AC", "⌫", "%")

                        val bgColor = when {
                            isEquals -> palette.accent
                            isOp -> palette.accent.copy(alpha = 0.22f)
                            isUtil -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }
                        val fgColor = when {
                            isEquals -> Color.White
                            isOp -> palette.accent
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .clickable {
                                    when (key) {
                                        "AC" -> onClearAll()
                                        "⌫" -> onBackspace()
                                        "%" -> onPercent()
                                        "±" -> onToggleSign()
                                        "." -> onDecimal()
                                        "+" -> onOperator(CalcOp.ADD)
                                        "−" -> onOperator(CalcOp.SUB)
                                        "×" -> onOperator(CalcOp.MUL)
                                        "÷" -> onOperator(CalcOp.DIV)
                                        "=" -> onEquals()
                                        else -> onDigit(key)
                                    }
                                }
                                .testTag("calc_btn_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontFamily = if (key.all { it.isDigit() }) JetBrainsMonoFamily else null,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = fgColor
                            )
                        }
                    }
                }
            }
        }
    }
}
