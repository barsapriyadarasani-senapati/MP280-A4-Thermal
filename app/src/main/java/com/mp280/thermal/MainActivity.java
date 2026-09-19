package com.mp280.thermal;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;

public class MainActivity extends Activity {

    private static final String ACTION_USB_PERMISSION =
            "com.mp280.thermal.USB_PERMISSION";

    private EditText textBox;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (!ACTION_USB_PERMISSION.equals(intent.getAction())) {
                return;
            }

            UsbDevice device;

            if (Build.VERSION.SDK_INT >= 33) {
                device = intent.getParcelableExtra(
                        UsbManager.EXTRA_DEVICE,
                        UsbDevice.class
                );
            } else {
                device = intent.getParcelableExtra(
                        UsbManager.EXTRA_DEVICE
                );
            }

            if (intent.getBooleanExtra(
                    UsbManager.EXTRA_PERMISSION_GRANTED,
                    false
            )) {

                if (device != null) {
                    printWithUSB(device);
                }

            } else {
                Toast.makeText(
                        MainActivity.this,
                        "USB permission denied",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter =
                new IntentFilter(ACTION_USB_PERMISSION);

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                    usbReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                    usbReceiver,
                    filter
            );
        }

        createScreen();
    }

    private void createScreen() {

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

        layout.addView(
                textBox,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        Button printButton = new Button(this);
        printButton.setText("PRINT");

        layout.addView(printButton);

        printButton.setOnClickListener(v -> requestUSBPrinter());

        setContentView(layout);
    }

    private void requestUSBPrinter() {

        UsbConnection connection =
                UsbPrintersConnections.selectFirstConnected(this);

        if (connection == null) {

            Toast.makeText(
                    this,
                    "MP280 not detected. Connect USB first.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        UsbManager usbManager =
                (UsbManager) getSystemService(Context.USB_SERVICE);

        if (usbManager == null) {
            return;
        }

        PendingIntent permissionIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        new Intent(ACTION_USB_PERMISSION),
                        Build.VERSION.SDK_INT >= 31
                                ? PendingIntent.FLAG_MUTABLE
                                : 0
                );

        if (usbManager.hasPermission(connection.getDevice())) {

            printWithUSB(connection.getDevice());

        } else {

            usbManager.requestPermission(
                    connection.getDevice(),
                    permissionIntent
            );
        }
    }

    private void printWithUSB(UsbDevice device) {

        String text = textBox.getText().toString();

        if (text.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Write something first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new Thread(() -> {

            try {

                UsbManager usbManager =
                        (UsbManager) getSystemService(
                                Context.USB_SERVICE
                        );

                UsbConnection usbConnection =
                        new UsbConnection(
                                usbManager,
                                device
                        );

                EscPosPrinter printer =
                        new EscPosPrinter(
                                usbConnection,
                                203,
                                48f,
                                32
                        );

                String formattedText =
                        "[L]" +
                        text
                                .replace("[", "\\[")
                                .replace("\n", "\n[L]");

                printer.printFormattedText(
                        formattedText,
                        0
                );

                printer.disconnectPrinter();

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "Printed successfully!",
                                Toast.LENGTH_LONG
                        ).show()
                );

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "Print error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

        }).start();
    }

    @Override
    protected void onDestroy() {

        try {
            unregisterReceiver(usbReceiver);
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }
}
