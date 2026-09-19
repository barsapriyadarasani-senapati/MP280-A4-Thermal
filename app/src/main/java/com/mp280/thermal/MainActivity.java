package com.mp280.thermal;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    EditText textBox;
    Button printButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("MP280 Thermal Printer");
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        layout.addView(title);

        textBox = new EditText(this);
        textBox.setHint("Write what you want to print...");
        textBox.setTextSize(18);
        textBox.setGravity(Gravity.TOP);
        textBox.setMinLines(8);

        layout.addView(textBox,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                ));

        printButton = new Button(this);
        printButton.setText("PRINT");

        layout.addView(printButton);

        printButton.setOnClickListener(v -> {

            String text = textBox.getText().toString();

            if (text.trim().isEmpty()) {
                Toast.makeText(
                        this,
                        "Write something first",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    this,
                    "Printer connection will be added next",
                    Toast.LENGTH_SHORT
            ).show();
        });

        setContentView(layout);
    }
}
