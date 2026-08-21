package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeaconCyan
import com.example.ui.theme.DeepSlate800
import com.example.ui.theme.DeepSlate900
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StealthCalculatorScreen(viewModel: MainViewModel) {
    var displayValue by remember { mutableStateOf("0") }
    var pinBuffer by remember { mutableStateOf("") }

    fun onKeyClick(key: String) {
        when (key) {
            "AC" -> {
                displayValue = "0"
                pinBuffer = ""
            }
            "C" -> {
                if (displayValue.length > 1) {
                    displayValue = displayValue.dropLast(1)
                } else {
                    displayValue = "0"
                }
                if (pinBuffer.isNotEmpty()) {
                    pinBuffer = pinBuffer.dropLast(1)
                }
            }
            "=" -> {
                try {
                    // Simple evaluation
                    displayValue = evaluateSimpleExpression(displayValue)
                } catch (e: Exception) {
                    displayValue = "Error"
                }
            }
            else -> {
                if (key.all { it.isDigit() }) {
                    pinBuffer += key
                    if (pinBuffer.length > 4) {
                        pinBuffer = pinBuffer.takeLast(4)
                    }

                    // Verify PIN with MainViewModel
                    if (pinBuffer.length == 4) {
                        val isRealUnlocked = viewModel.verifyCalculatorPin(pinBuffer)
                        if (!isRealUnlocked) {
                            // If fake pin or wrong pin, reset buffer smoothly
                            pinBuffer = ""
                        }
                    }
                }

                if (displayValue == "0" || displayValue == "Error") {
                    displayValue = key
                } else {
                    displayValue += key
                }
            }
        }
    }

    Surface(
        color = DeepSlate900,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Disguise App Bar Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Calculator",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Display Screen
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    text = displayValue,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Keypad Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val buttonRows = listOf(
                    listOf("AC", "C", "%", "/"),
                    listOf("7", "8", "9", "*"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "=")
                )

                for (row in buttonRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (btn in row) {
                            val weight = if (btn == "=" || btn == "0") 2f else 1f
                            val isOp = btn in listOf("/", "*", "-", "+", "=")
                            val isFunc = btn in listOf("AC", "C", "%")

                            val bgColor = when {
                                isOp -> BeaconCyan
                                isFunc -> DeepSlate800
                                else -> Color(0xFF334155)
                            }

                            val txtColor = when {
                                isOp -> DeepSlate900
                                else -> Color.White
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(weight)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(36.dp))
                                    .background(bgColor)
                                    .clickable { onKeyClick(btn) }
                            ) {
                                Text(
                                    text = btn,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = txtColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateSimpleExpression(expr: String): String {
    return try {
        val sanitized = expr.replace("×", "*").replace("÷", "/")
        if (sanitized.contains("+")) {
            val parts = sanitized.split("+")
            (parts[0].toDouble() + parts[1].toDouble()).toString()
        } else if (sanitized.contains("-")) {
            val parts = sanitized.split("-")
            (parts[0].toDouble() - parts[1].toDouble()).toString()
        } else if (sanitized.contains("*")) {
            val parts = sanitized.split("*")
            (parts[0].toDouble() * parts[1].toDouble()).toString()
        } else if (sanitized.contains("/")) {
            val parts = sanitized.split("/")
            (parts[0].toDouble() / parts[1].toDouble()).toString()
        } else {
            expr
        }
    } catch (e: Exception) {
        "0"
    }
}
