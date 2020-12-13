package protect.card_locker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;

import androidx.core.graphics.ColorUtils;

public class Utils {
    private static final String TAG = "Catima";

    // Barcode config dialog
    private static AlertDialog setBarcodeDialog;

    // Activity request codes
    public static final int MAIN_REQUEST = 1;
    public static final int SELECT_BARCODE_REQUEST = 2;
    public static final int PICK_IMAGE = 3;

    static final double LUMINANCE_MIDPOINT = 0.5;

    static public LetterBitmap generateIcon(Context context, String store, Integer backgroundColor) {
        if (store.length() == 0) {
            return null;
        }

        int tileLetterFontSize = context.getResources().getDimensionPixelSize(R.dimen.tileLetterFontSize);
        int pixelSize = context.getResources().getDimensionPixelSize(R.dimen.cardThumbnailSize);

        if (backgroundColor == null) {
            backgroundColor = LetterBitmap.getDefaultColor(context, store);
        }

        return new LetterBitmap(context, store, store,
                tileLetterFontSize, pixelSize, pixelSize, backgroundColor, needsDarkForeground(backgroundColor) ? Color.BLACK : Color.WHITE);
    }

    static public boolean needsDarkForeground(Integer backgroundColor) {
        return ColorUtils.calculateLuminance(backgroundColor) > LUMINANCE_MIDPOINT;
    }

    static public void startCameraBarcodeScan(Context context, Activity activity) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(BarcodeSelectorActivity.SUPPORTED_BARCODE_TYPES);

        String prompt = context.getResources().getString(R.string.scanCardBarcode);
        integrator.setPrompt(prompt);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    static public void createSetBarcodeDialog(final Context context, final Activity activity, boolean isUpdate, final String initialCardId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_create, null));

        if (isUpdate) {
            builder.setTitle(context.getString(R.string.editCardTitle));
        } else {
            builder.setTitle(context.getString(R.string.addCardTitle));
        }

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setBarcodeDialog.cancel();
            }
        });
        setBarcodeDialog = builder.create();
        setBarcodeDialog.show();

        View addFromCamera = setBarcodeDialog.getWindow().findViewById(R.id.add_from_camera);
        View addManually = setBarcodeDialog.getWindow().findViewById(R.id.add_manually);

        addFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startCameraBarcodeScan(context, activity);

                setBarcodeDialog.hide();
            }
        });

        addManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, BarcodeSelectorActivity.class);
                if (initialCardId != null) {
                    final Bundle b = new Bundle();
                    b.putString("initialCardId", initialCardId);
                    i.putExtras(b);
                }
                activity.startActivityForResult(i, Utils.SELECT_BARCODE_REQUEST);

                setBarcodeDialog.hide();
            }
        });
    }

    static public BarcodeValues parseSetBarcodeActivityResult(int requestCode, int resultCode, Intent intent) {
        String contents = null;
        String format = null;

        IntentResult result =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null)
        {
            Log.i(TAG, "Received barcode information from capture");
            contents = result.getContents();
            format = result.getFormatName();
        }

        if(requestCode == Utils.SELECT_BARCODE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Log.i(TAG, "Received barcode information from typing it");

            contents = intent.getStringExtra(BarcodeSelectorActivity.BARCODE_CONTENTS);
            format = intent.getStringExtra(BarcodeSelectorActivity.BARCODE_FORMAT);
        }

        Log.i(TAG, "Read barcode id: " + contents);
        Log.i(TAG, "Read format: " + format);

        return new BarcodeValues(format, contents);
    }

    static public byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    static public Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    static public String bitmapToBase64(Bitmap bitmap) {
        return Base64.encodeToString(bitmapToByteArray(bitmap), Base64.URL_SAFE);
    }

    static public Bitmap base64ToBitmap(String base64) {
        return byteArrayToBitmap(Base64.decode(base64, Base64.URL_SAFE));
    }

    static public Bitmap resizeBitmapForIcon(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        Integer maxSize = 128;

        Integer width = bitmap.getWidth();
        Integer height = bitmap.getHeight();

        if (height > width) {
            Integer scale = height / maxSize;
            height = maxSize;
            width = width / scale;
        } else if (width > height) {
            Integer scale = width / maxSize;
            width = maxSize;
            height = height / scale;
        } else {
            height = maxSize;
            width = maxSize;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}
