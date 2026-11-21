package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette
import java.text.DecimalFormat

private enum class UnitCategory(val title: String) {
    LENGTH("Length"),
    TEMPERATURE("Temperature"),
    WEIGHT("Weight"),
    VOLUME("Volume"),
    DATA("Data"),
    SPEED("Speed")
}

private data class UnitDef(
    val name: String,
    val symbol: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

private val unitCatalog: Map<UnitCategory, List<UnitDef>> = mapOf(
    UnitCategory.LENGTH to listOf(
        UnitDef("Millimeter", "mm", { it * 0.001 }, { it / 0.001 }),
        UnitDef("Centimeter", "cm", { it * 0.01 }, { it / 0.01 }),
        UnitDef("Meter", "m", { it }, { it }),
        UnitDef("Kilometer", "km", { it * 1000.0 }, { it / 1000.0 }),
        UnitDef("Inch", "in", { it * 0.0254 }, { it / 0.0254 }),
        UnitDef("Foot", "ft", { it * 0.3048 }, { it / 0.3048 }),
        UnitDef("Yard", "yd", { it * 0.9144 }, { it / 0.9144 }),
        UnitDef("Mile", "mi", { it * 1609.344 }, { it / 1609.344 })
    ),
    UnitCategory.TEMPERATURE to listOf(
        UnitDef("Celsius", "°C", { it }, { it }),
        UnitDef("Fahrenheit", "°F", { (it - 32.0) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32.0 }),
        UnitDef("Kelvin", "K", { it - 273.15 }, { it + 273.15 })
    ),
    UnitCategory.WEIGHT to listOf(
        UnitDef("Milligram", "mg", { it * 0.000001 }, { it / 0.000001 }),
        UnitDef("Gram", "g", { it * 0.001 }, { it / 0.001 }),
        UnitDef("Kilogram", "kg", { it }, { it }),
        UnitDef("Ounce", "oz", { it * 0.0283495231 }, { it / 0.0283495231 }),
        UnitDef("Pound", "lb", { it * 0.45359237 }, { it / 0.45359237 })
    ),
    UnitCategory.VOLUME to listOf(
        UnitDef("Milliliter", "mL", { it * 0.001 }, { it / 0.001 }),
        UnitDef("Liter", "L", { it }, { it }),
        UnitDef("Teaspoon", "tsp", { it * 0.00492892 }, { it / 0.00492892 }),
        UnitDef("Tablespoon", "tbsp", { it * 0.0147868 }, { it / 0.0147868 }),
        UnitDef("US Cup", "cup", { it * 0.236588 }, { it / 0.236588 }),
        UnitDef("US Gallon", "gal", { it * 3.78541 }, { it / 3.78541 })
    ),
    UnitCategory.DATA to listOf(
        UnitDef("Byte", "B", { it }, { it }),
        UnitDef("Kilobyte", "KB", { it * 1024.0 }, { it / 1024.0 }),
        UnitDef("Megabyte", "MB", { it * 1048576.0 }, { it / 1048576.0 }),
        UnitDef("Gigabyte", "GB", { it * 1073741824.0 }, { it / 1073741824.0 }),
        UnitDef("Terabyte", "TB", { it * 1099511627776.0 }, { it / 1099511627776.0 })
    ),
    UnitCategory.SPEED to listOf(
        UnitDef("Meters/sec", "m/s", { it }, { it }),
        UnitDef("Kilometers/hr", "km/h", { it / 3.6 }, { it * 3.6 }),
        UnitDef("Miles/hr", "mph", { it * 0.44704 }, { it / 0.44704 }),
        UnitDef("Knots", "kn", { it * 0.514444 }, { it / 0.514444 })
    )
)

@Composable
fun UnitConverterApp() {
    val palette = LocalDesktopPalette.current
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var inputText by remember { mutableStateOf("1") }
    var fromIndex by remember(selectedCategory) { mutableIntStateOf(0) }
    var toIndex by remember(selectedCategory) {
        mutableIntStateOf(if ((unitCatalog[selectedCategory]?.size ?: 0) > 2) 2 else 1)
    }

    val units = unitCatalog[selectedCategory].orEmpty()
    val safeFrom = units.getOrElse(fromIndex) { units.first() }
    val safeTo = units.getOrElse(toIndex) { units.last() }

    val formatter = remember { DecimalFormat("#,##0.######") }
    val numericInput = inputText.toDoubleOrNull()
    val baseValue = numericInput?.let { safeFrom.toBase(it) }
    val convertedValue = baseValue?.let { safeTo.fromBase(it) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Category Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            UnitCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.title, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.accent.copy(alpha = 0.22f),
                        selectedLabelColor = palette.accent
                    )
                )
            }
        }

        // Input + From/To Selector Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { next ->
                    if (next.length <= 16) inputText = next
                },
                label = { Text("Value") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("unit_input")
            )

            UnitDropdownSelector(
                label = "From",
                units = units,
                selectedIndex = fromIndex,
                onSelect = { fromIndex = it },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    val temp = fromIndex
                    fromIndex = toIndex
                    toIndex = temp
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accent.copy(alpha = 0.16f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = "Swap units",
                    tint = palette.accent
                )
            }

            UnitDropdownSelector(
                label = "To",
                units = units,
                selectedIndex = toIndex,
                onSelect = { toIndex = it },
                modifier = Modifier.weight(1f)
            )
        }

        // Highlighted Primary Result Card
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.accent.copy(alpha = 0.14f))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (numericInput != null) "${formatter.format(numericInput)} ${safeFrom.symbol} =" else "Enter a valid number",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (convertedValue != null) "${formatter.format(convertedValue)} ${safeTo.symbol}" else "—",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = palette.accent,
                modifier = Modifier.testTag("unit_result")
            )
        }

        // All Equivalent Units Reference Table
        Text(
            text = "All ${selectedCategory.title} Equivalents",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(units) { unit ->
                val value = baseValue?.let { unit.fromBase(it) }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${unit.name} (${unit.symbol})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = value?.let { formatter.format(it) } ?: "—",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitDropdownSelector(
    label: String,
    units: List<UnitDef>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = units.getOrElse(selectedIndex) { units.first() }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(selected.symbol, style = MaterialTheme.typography.labelLarge)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEachIndexed { idx, unit ->
                DropdownMenuItem(
                    text = { Text("${unit.name} (${unit.symbol})") },
                    onClick = {
                        onSelect(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}
