package protect.card_locker;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.sql.Blob;

import androidx.annotation.Nullable;

public class LoyaltyCard
{
    public final int id;
    public final String store;
    public final String note;
    public final String cardId;
    public final String barcodeType;

    @Nullable
    public final Integer headerColor;

    @Nullable
    public final Integer headerTextColor;

    public final int starStatus;

    @Nullable
    public final Bitmap icon;

    public LoyaltyCard(final int id, final String store, final String note, final String cardId,
                       final String barcodeType, final Integer headerColor, final Integer headerTextColor,
                       final int starStatus, final Bitmap icon)
    {
        this.id = id;
        this.store = store;
        this.note = note;
        this.cardId = cardId;
        this.barcodeType = barcodeType;
        this.headerColor = headerColor;
        this.headerTextColor = headerTextColor;
        this.starStatus = starStatus;
        this.icon = icon;
    }

    public static LoyaltyCard toLoyaltyCard(Cursor cursor)
    {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.ID));
        String store = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.STORE));
        String note = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.NOTE));
        String cardId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.CARD_ID));
        String barcodeType = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.BARCODE_TYPE));
        int starred = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.STAR_STATUS));
        Bitmap icon = Utils.byteArrayToBitmap(cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.ICON)));

        int headerColorColumn = cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.HEADER_COLOR);
        int headerTextColorColumn = cursor.getColumnIndexOrThrow(DBHelper.LoyaltyCardDbIds.HEADER_TEXT_COLOR);

        Integer headerColor = null;
        Integer headerTextColor = null;

        if(cursor.isNull(headerColorColumn) == false)
        {
            headerColor = cursor.getInt(headerColorColumn);
        }

        if(cursor.isNull(headerTextColorColumn) == false)
        {
            headerTextColor = cursor.getInt(headerTextColorColumn);
        }

        return new LoyaltyCard(id, store, note, cardId, barcodeType, headerColor, headerTextColor, starred, icon);
    }
}
