package com.fibonacciapp.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.*

class FibonacciKeyboardService : InputMethodService() {
    
    private var isEncryptMode = true
    private val secretKey = "MySecret2024"
    
    override fun onCreateInputView(): View {
        return createKeyboard()
    }
    
    private fun createKeyboard(): LinearLayout {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF2C2C2C.toInt())
            setPadding(8, 8, 8, 8)
        }
        
        mainLayout.addView(createRow(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "⌫")))
        mainLayout.addView(createRow(listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "⏎")))
        mainLayout.addView(createRow(listOf("z", "x", "c", "v", "b", "n", "m", ",", ".", "?")))
        
        val specialRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        specialRow.addView(createKey("🔒", 1.5f) { toggleEncryptMode() })
        specialRow.addView(createKey("Space", 3f) { handleSpace() })
        specialRow.addView(createKey("👁", 1.5f) { showDecryptPreview() })
        
        mainLayout.addView(specialRow)
        return mainLayout
    }
    
    private fun createRow(keys: List<String>): LinearLayout {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        keys.forEach { key ->
            val weight = when(key) { "⌫", "⏎" -> 1.5f; else -> 1f }
            row.addView(createKey(key, weight) { handleKeyPress(key) })
        }
        return row
    }
    
    private fun createKey(text: String, weight: Float, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(if (text == "🔒" && isEncryptMode) 0xFF4CAF50.toInt() else 0xFF555555.toInt())
            layoutParams = LinearLayout.LayoutParams(0, 120).apply {
                this.weight = weight
                setMargins(2, 2, 2, 2)
            }
            setOnClickListener { onClick() }
        }
    }
    
    private fun handleKeyPress(key: String) {
        val ic = currentInputConnection ?: return
        when (key) {
            "⌫" -> ic.deleteSurroundingText(1, 0)
            "⏎" -> ic.commitText("\n", 1)
            else -> {
                val output = if (isEncryptMode) fibonacciEncrypt(key) else key
                ic.commitText(output, 1)
            }
        }
    }
    
    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        val space = if (isEncryptMode) fibonacciEncrypt(" ") else " "
        ic.commitText(space, 1)
    }
    
    private fun toggleEncryptMode() {
        isEncryptMode = !isEncryptMode
        val status = if (isEncryptMode) "🔒 Encrypt Mode ON" else "📝 Normal Mode"
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
    }
    
    private fun showDecryptPreview() {
        val ic = currentInputConnection ?: return
        val recentText = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        if (recentText.isNotEmpty()) {
            val decrypted = fibonacciDecrypt(recentText)
            Toast.makeText(this, "👁 Decrypted: $decrypted", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun fibonacciEncrypt(text: String): String = processText(text, true)
    private fun fibonacciDecrypt(text: String): String = processText(text, false)
    
    private fun processText(text: String, encrypt: Boolean): String {
        val fibonacci = generateFibonacci(50)
        val keyOffset = secretKey.sumOf { it.code } % 100
        
        return text.mapIndexed { index, char ->
            val fibIndex = (index + keyOffset) % fibonacci.size
            val fibValue = fibonacci[fibIndex].toInt()
            
            if (char.code in 32..126) {
                val shifted = if (encrypt) {
                    ((char.code - 32 + fibValue) % 95) + 32
                } else {
                    ((char.code - 32 - fibValue + 95) % 95) + 32
                }
                shifted.toChar()
            } else char
        }.joinToString("")
    }
    
    private fun generateFibonacci(count: Int): List<Long> {
        val fib = mutableListOf(0L, 1L)
        for (i in 2 until count) {
            fib.add(fib[i-1] + fib[i-2])
        }
        return fib
    }
}
