package com.example.calci.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.calci.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isNewOperation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Number buttons
        binding.btn7.setOnClickListener { appendNumber("7") }
        binding.btn8.setOnClickListener { appendNumber("8") }
        binding.btn9.setOnClickListener { appendNumber("9") }
        binding.btn4.setOnClickListener { appendNumber("4") }
        binding.btn5.setOnClickListener { appendNumber("5") }
        binding.btn6.setOnClickListener { appendNumber("6") }
        binding.btn1.setOnClickListener { appendNumber("1") }
        binding.btn2.setOnClickListener { appendNumber("2") }
        binding.btn3.setOnClickListener { appendNumber("3") }
        binding.btn0.setOnClickListener { appendNumber("0") }
        binding.btnDot.setOnClickListener { appendNumber(".") }

        // Operators
        binding.btnAdd.setOnClickListener { appendNumber("+") }
        binding.btnSub.setOnClickListener { appendNumber("-") }
        binding.btnMul.setOnClickListener { appendNumber("*") }
        binding.btnDiv.setOnClickListener { appendNumber("/") }

        // Clear
        binding.btnClear.setOnClickListener { clearText() }

        // Delete last character
        binding.btnDel.setOnClickListener { deleteLastChar() }

        // Equal
        binding.btnEqual.setOnClickListener { calculateResult() }
    }

    private fun appendNumber(number: String) {
        if (isNewOperation) {
            binding.calculation.text = ""
            isNewOperation = false
        }
        binding.calculation.text =
            binding.calculation.text.toString() + number
    }

    private fun clearText() {
        binding.calculation.text = ""
        binding.resultText.text = ""
        isNewOperation = true
    }

    private fun deleteLastChar() {
        val currentText = binding.calculation.text.toString()
        if (currentText.isNotEmpty()) {
            binding.calculation.text = currentText.dropLast(1)
        }
    }

    private fun calculateResult() {
        try {
            val expression = binding.calculation.text.toString()
            val result = evaluateExpression(expression)
            binding.resultText.text = result.toString()
            isNewOperation = true
        } catch (e: Exception) {
            binding.resultText.text = "Error"
            isNewOperation = true
        }
    }

    // Basic evaluation (Note: for production, use a safe parser)
    private fun evaluateExpression(expr: String): Double {
        val expression = expr.replace("×", "*").replace("÷", "/")

        return object {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }

}
