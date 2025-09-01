// app/src/main/java/com/example/hyejade/GraphViewModel.kt
package com.example.hyejade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class GraphViewModel : ViewModel() {
    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running

    fun runRagCode() {
        if (_running.value) return
        _running.value = true
        viewModelScope.launch {
            try {
                val out = withContext(Dispatchers.Default) {
                    captureStdout { RagCode.main(arrayOf()) }  // ← Java 메인 호출
                }
                _output.value = out
            } finally {
                _running.value = false
            }
        }
    }

    private fun <T> captureStdout(block: () -> T): String {
        val original = System.out
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        return try {
            System.setOut(ps)
            block()
            ps.flush()
            baos.toString(Charsets.UTF_8.name())
        } finally {
            System.setOut(original)
            ps.close()
        }
    }
}
