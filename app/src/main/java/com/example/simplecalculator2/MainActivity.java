package com.example.simplecalculator2; // IMPORTANT: Ensure this package name matches your project's structure

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.DecimalFormat;
import com.example.simplecalculator2.R; // EXPLICITLY IMPORT THE R CLASS

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private boolean lastNumeric = false; // True if the last input was a digit/decimal
    private boolean lastDot = false;     // True if a decimal point has been added to the current number
    private boolean operatorPressed = false; // NEW: Tracks if an operator was just pressed

    // Calculation state variables
    private String currentNumber = "";
    private String currentOperator = "";
    private double operand1 = 0.0;
    private boolean isError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextView
        tvDisplay = findViewById(R.id.tvDisplay);

        // Initial state
        tvDisplay.setText("0");
    }

    // --- Digit Input (0-9) ---
    public void onDigit(View view) {
        // Clear error state if a digit is pressed
        if (isError) {
            tvDisplay.setText("0");
            isError = false;
        }

        // NEW LOGIC: If an operator was just pressed, clear the display for the second number.
        if (operatorPressed) {
            tvDisplay.setText("");
            operatorPressed = false; // Reset the flag
        }

        String digit = ((Button) view).getText().toString();

        // Replace "0" if we are starting a new number and the digit is not "0"
        // This is necessary only if we didn't just clear the display above
        if (tvDisplay.getText().toString().equals("0") && !digit.equals("0")) {
            tvDisplay.setText("");
        }

        tvDisplay.append(digit);
        lastNumeric = true;
        currentNumber = tvDisplay.getText().toString();
    }

    // --- Clear (C) ---
    public void onClear(View view) {
        tvDisplay.setText("0");
        currentNumber = "";
        currentOperator = "";
        operand1 = 0.0;
        lastNumeric = false;
        lastDot = false;
        isError = false;
        operatorPressed = false; // Clear the new flag too
    }

    // --- Decimal Point (.) ---
    public void onDecimalPoint(View view) {
        // NEW LOGIC: If an operator was just pressed, start the second number with "0."
        if (operatorPressed) {
            tvDisplay.setText("0");
            operatorPressed = false; // Reset the flag
            lastNumeric = true;
        }

        // Only allow a decimal point if the last input was numeric and we haven't added one yet
        if (lastNumeric && !lastDot) {
            tvDisplay.append(".");
            lastNumeric = false; // The next input must be numeric to continue
            lastDot = true;
            currentNumber = tvDisplay.getText().toString();
        }
    }

    // --- Operator Input (+, -, *, /) ---
    public void onOperator(View view) {
        if (lastNumeric && !isError) {
            String buttonText = ((Button) view).getText().toString();

            // 1. If an operation is already waiting, perform it first (chain calculation)
            if (!currentOperator.isEmpty()) {
                double result = performCalculation();
                if (isError) return; // Stop if performCalculation resulted in an error

                // Set the result as the new operand1 and display it
                operand1 = result;
                tvDisplay.setText(formatResult(result));
            } else {
                // 2. First operation: store the current number as operand1
                try {
                    operand1 = Double.parseDouble(currentNumber);
                } catch (NumberFormatException e) {
                    tvDisplay.setText("Error");
                    isError = true;
                    return;
                }
            }

            // 3. Set the new operator
            currentOperator = buttonText;
            lastNumeric = false;
            lastDot = false;
            currentNumber = ""; // Ready for the second operand input
            operatorPressed = true; // SET FLAG: Tell the next onDigit press to clear the display
        }
    }

    // --- Equal (=) ---
    public void onEqual(View view) {
        // Must have a second number and a pending operator
        if (lastNumeric && !currentOperator.isEmpty() && !isError) {
            double result = performCalculation();
            if (isError) return;

            // Display the final result
            tvDisplay.setText(formatResult(result));

            // Reset state for a new calculation starting from the result
            operand1 = result;
            currentOperator = ""; // Clear the operator
            currentNumber = formatResult(result);
            lastNumeric = true;
            lastDot = currentNumber.contains(".");
            operatorPressed = false; // Calculation is finished
        }
    }

    // --- Core Calculation Logic ---
    private double performCalculation() {
        double operand2;

        try {
            operand2 = Double.parseDouble(currentNumber);
        } catch (NumberFormatException e) {
            tvDisplay.setText("Error");
            isError = true;
            return 0.0;
        }

        double result = 0.0;
        switch (currentOperator) {
            case "+":
                result = operand1 + operand2;
                break;
            case "-":
                result = operand1 - operand2;
                break;
            case "*":
                result = operand1 * operand2;
                break;
            case "/":
                if (operand2 == 0.0) {
                    tvDisplay.setText("Error: Div by 0");
                    isError = true;
                    return 0.0;
                } else {
                    result = operand1 / operand2;
                }
                break;
            default:
                // Should not happen
                break;
        }

        return result;
    }

    // --- Formatting Helper: Removes unnecessary decimal zeros (e.g., 5.0 -> 5) ---
    private String formatResult(double result) {
        // Use a DecimalFormat to handle formatting concisely
        DecimalFormat df = new DecimalFormat("0.########");
        return df.format(result);
    }
}
