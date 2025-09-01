// app/src/main/java/com/example/hyejade/MainActivity.kt
package com.example.hyejade

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyejade.ui.theme.HyeJadeTheme
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.unit.dp             // ← dp 확장

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HyeJadeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GraphScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }

    }
}

@Composable
fun GraphScreen(modifier: Modifier = Modifier, vm: GraphViewModel = viewModel()) {


    val output by vm.output.collectAsState()
    val running by vm.running.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("LangGraph Runner", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.runRagCode() }, enabled = !running) {
            Text(if (running) "Running..." else "Run RagCode.main()")
        }
        Spacer(Modifier.height(12.dp))
        Text("Captured Output:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        val scroll = rememberScrollState()
        Text(
            text = if (output.isBlank()) "No output yet." else output,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scroll),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGraphScreen() {
    HyeJadeTheme { GraphScreen() }
}
