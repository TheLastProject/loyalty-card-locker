package protect.card_locker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Catima.db";
    public static final int ORIGINAL_DATABASE_VERSION = 1;
    public static final int DATABASE_VERSION = 7;

    static class LoyaltyCardDbGroups
    {
        public static final String TABLE = "groups";
        public static final String ID = "_id";
        public static final String ORDER = "orderId";
    }

    static class LoyaltyCardDbIds
    {
        public static final String TABLE = "cards";
        public static final String ID = "_id";
        public static final String STORE = "store";
        public static final String NOTE = "note";
        public static final String HEADER_COLOR = "headercolor";
        public static final String HEADER_TEXT_COLOR = "headertextcolor";
        public static final String CARD_ID = "cardid";
        public static final String BARCODE_TYPE = "barcodetype";
        public static final String STAR_STATUS = "starstatus";
        public static final String ICON = "icon";
    }

    static class LoyaltyCardDbIdsGroups
    {
        public static final String TABLE = "cardsGroups";
        public static final String cardID = "cardId";
        public static final String groupID = "groupId";
    }

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // create table for card groups
        db.execSQL("create table " + LoyaltyCardDbGroups.TABLE + "(" +
                LoyaltyCardDbGroups.ID + " TEXT primary key not null," +
                LoyaltyCardDbGroups.ORDER + " INTEGER DEFAULT '0')");

        // create table for cards
        db.execSQL("create table " + LoyaltyCardDbIds.TABLE + "(" +
                LoyaltyCardDbIds.ID + " INTEGER primary key autoincrement," +
                LoyaltyCardDbIds.STORE + " TEXT not null," +
                LoyaltyCardDbIds.NOTE + " TEXT not null," +
                LoyaltyCardDbIds.HEADER_COLOR + " INTEGER," +
                LoyaltyCardDbIds.HEADER_TEXT_COLOR + " INTEGER," +
                LoyaltyCardDbIds.CARD_ID + " TEXT not null," +
                LoyaltyCardDbIds.BARCODE_TYPE + " TEXT not null," +
                LoyaltyCardDbIds.STAR_STATUS + " INTEGER DEFAULT '0'," +
                LoyaltyCardDbIds.ICON + " BLOB)");

        // create associative table for cards in groups
        db.execSQL("create table " + LoyaltyCardDbIdsGroups.TABLE + "(" +
                LoyaltyCardDbIdsGroups.cardID + " INTEGER," +
                LoyaltyCardDbIdsGroups.groupID + " TEXT," +
                "primary key (" + LoyaltyCardDbIdsGroups.cardID + "," + LoyaltyCardDbIdsGroups.groupID +"))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Upgrade from version 1 to version 2
        if(oldVersion < 2 && newVersion >= 2)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.NOTE + " TEXT not null default ''");
        }

        // Upgrade from version 2 to version 3
        if(oldVersion < 3 && newVersion >= 3)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.HEADER_COLOR + " INTEGER");
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.HEADER_TEXT_COLOR + " INTEGER");
        }

        // Upgrade from version 3 to version 4
        if(oldVersion < 4 && newVersion >= 4)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.STAR_STATUS + " INTEGER DEFAULT '0'");
        }

        // Upgrade from version 4 to version 5
        if(oldVersion < 5 && newVersion >= 5)
        {
            db.execSQL("create table " + LoyaltyCardDbGroups.TABLE + "(" +
                    LoyaltyCardDbGroups.ID + " TEXT primary key not null)");

            db.execSQL("create table " + LoyaltyCardDbIdsGroups.TABLE + "(" +
                    LoyaltyCardDbIdsGroups.cardID + " INTEGER," +
                    LoyaltyCardDbIdsGroups.groupID + " TEXT," +
                    "primary key (" + LoyaltyCardDbIdsGroups.cardID + "," + LoyaltyCardDbIdsGroups.groupID +"))");
        }

        // Upgrade from version 5 to 6
        if(oldVersion < 6 && newVersion >= 6)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbGroups.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbGroups.ORDER + " INTEGER DEFAULT '0'");
        }
        
        // Upgrade from version 6 to 7
        if (oldVersion < 7 && newVersion >= 7)
        {
            db.execSQL("ALTER TABLE " + LoyaltyCardDbIds.TABLE
                    + " ADD COLUMN " + LoyaltyCardDbIds.ICON + " BLOB");
        }
    }

    public long insertLoyaltyCard(final String store, final String note, final String cardId,
                                  final String barcodeType, final Integer headerColor,
                                  final int starStatus, final Bitmap icon)
    {


        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.STORE, store);
        contentValues.put(LoyaltyCardDbIds.NOTE, note);
        contentValues.put(LoyaltyCardDbIds.CARD_ID, cardId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_TYPE, barcodeType);
        contentValues.put(LoyaltyCardDbIds.HEADER_COLOR, headerColor);
        contentValues.put(LoyaltyCardDbIds.HEADER_TEXT_COLOR, Color.WHITE);
        contentValues.put(LoyaltyCardDbIds.STAR_STATUS, starStatus);
        contentValues.put(LoyaltyCardDbIds.ICON, Utils.bitmapToByteArray(icon));

        final long newId = db.insert(LoyaltyCardDbIds.TABLE, null, contentValues);
        return newId;
    }

    public boolean insertLoyaltyCard(final SQLiteDatabase db, final int id, final String store,
                                     final String note, final String cardId,
                                     final String barcodeType, final Integer headerColor,
                                     final int starStatus, final Bitmap icon)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.ID, id);
        contentValues.put(LoyaltyCardDbIds.STORE, store);
        contentValues.put(LoyaltyCardDbIds.NOTE, note);
        contentValues.put(LoyaltyCardDbIds.CARD_ID, cardId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_TYPE, barcodeType);
        contentValues.put(LoyaltyCardDbIds.HEADER_COLOR, headerColor);
        contentValues.put(LoyaltyCardDbIds.HEADER_TEXT_COLOR, Color.WHITE);
        contentValues.put(LoyaltyCardDbIds.STAR_STATUS,starStatus);
        contentValues.put(LoyaltyCardDbIds.ICON, Utils.bitmapToByteArray(icon));
        final long newId = db.insert(LoyaltyCardDbIds.TABLE, null, contentValues);
        return (newId != -1);
    }

    public boolean updateLoyaltyCard(final int id, final String store, final String note,
                                     final String cardId, final String barcodeType,
                                     final Integer headerColor, final Bitmap icon)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.STORE, store);
        contentValues.put(LoyaltyCardDbIds.NOTE, note);
        contentValues.put(LoyaltyCardDbIds.CARD_ID, cardId);
        contentValues.put(LoyaltyCardDbIds.BARCODE_TYPE, barcodeType);
        contentValues.put(LoyaltyCardDbIds.HEADER_COLOR, headerColor);
        contentValues.put(LoyaltyCardDbIds.HEADER_TEXT_COLOR, Color.WHITE);
        contentValues.put(LoyaltyCardDbIds.ICON, Utils.bitmapToByteArray(icon));
        int rowsUpdated = db.update(LoyaltyCardDbIds.TABLE, contentValues,
                LoyaltyCardDbIds.ID + "=?",
                new String[]{Integer.toString(id)});
        return (rowsUpdated == 1);
    }

    public boolean updateLoyaltyCardStarStatus(final int id, final int starStatus)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbIds.STAR_STATUS,starStatus);
        int rowsUpdated = db.update(LoyaltyCardDbIds.TABLE, contentValues,
                LoyaltyCardDbIds.ID + "=?",
                new String[]{Integer.toString(id)});
        return (rowsUpdated == 1);
    }

    public LoyaltyCard getLoyaltyCard(final int id)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("select * from " + LoyaltyCardDbIds.TABLE +
                " where " + LoyaltyCardDbIds.ID + "=?", new String[]{String.format("%d", id)});

        LoyaltyCard card = null;

        if(data.getCount() == 1)
        {
            data.moveToFirst();

            card = LoyaltyCard.toLoyaltyCard(data);
        }

        data.close();

        return card;
    }

    public List<Group> getLoyaltyCardGroups(final int id)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("select * from " + LoyaltyCardDbGroups.TABLE + " g " +
                " LEFT JOIN " + LoyaltyCardDbIdsGroups.TABLE + " ig ON ig." + LoyaltyCardDbIdsGroups.groupID + " = g." + LoyaltyCardDbGroups.ID +
                " where " + LoyaltyCardDbIdsGroups.cardID + "=?" +
                " ORDER BY " + LoyaltyCardDbIdsGroups.groupID, new String[]{String.format("%d", id)});

        List<Group> groups = new ArrayList<>();

        if (!data.moveToFirst()) {
            return groups;
        }

        groups.add(Group.toGroup(data));

        while (data.moveToNext()) {
            groups.add(Group.toGroup(data));
        }

        return groups;
    }

    public void setLoyaltyCardGroups(final int id, List<Group> groups)
    {
        SQLiteDatabase db = getWritableDatabase();

        // First delete lookup table entries associated with this card
        db.delete(LoyaltyCardDbIdsGroups.TABLE,
                LoyaltyCardDbIdsGroups.cardID + " = ? ",
                new String[]{String.format("%d", id)});

        // Then create entries for selected values
        for (Group group : groups) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(LoyaltyCardDbIdsGroups.cardID, id);
            contentValues.put(LoyaltyCardDbIdsGroups.groupID, group._id);
            db.insert(LoyaltyCardDbIdsGroups.TABLE, null, contentValues);
        }
    }

    public void setLoyaltyCardGroups(final SQLiteDatabase db, final int id, List<Group> groups)
    {
        // First delete lookup table entries associated with this card
        db.delete(LoyaltyCardDbIdsGroups.TABLE,
                LoyaltyCardDbIdsGroups.cardID + " = ? ",
                new String[]{String.format("%d", id)});

        // Then create entries for selected values
        for (Group group : groups) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(LoyaltyCardDbIdsGroups.cardID, id);
            contentValues.put(LoyaltyCardDbIdsGroups.groupID, group._id);
            db.insert(LoyaltyCardDbIdsGroups.TABLE, null, contentValues);
        }
    }

    public boolean deleteLoyaltyCard (final int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        // Delete card
        int rowsDeleted = db.delete(LoyaltyCardDbIds.TABLE,
                LoyaltyCardDbIds.ID + " = ? ",
                new String[]{String.format("%d", id)});

        // And delete lookup table entries associated with this card
        db.delete(LoyaltyCardDbIdsGroups.TABLE,
                LoyaltyCardDbIdsGroups.cardID + " = ? ",
                new String[]{String.format("%d", id)});

        return (rowsDeleted == 1);
    }

    public Cursor getLoyaltyCardCursor()
    {
        // An empty string will match everything
        return getLoyaltyCardCursor("");
    }

    /**
     * Returns a cursor to all loyalty cards with the filter text in either the store or note.
     *
     * @param filter
     * @return Cursor
     */
    public Cursor getLoyaltyCardCursor(final String filter)
    {
        return getLoyaltyCardCursor(filter, null);
    }

    /**
     * Returns a cursor to all loyalty cards with the filter text in either the store or note in a certain group.
     *
     * @param filter
     * @param group
     * @return Cursor
     */
    public Cursor getLoyaltyCardCursor(final String filter, Group group)
    {
        String actualFilter = String.format("%%%s%%", filter);
        String[] selectionArgs = { actualFilter, actualFilter };
        StringBuilder groupFilter = new StringBuilder();
        String limitString = "";

        SQLiteDatabase db = getReadableDatabase();

        if (group != null) {
            List<Integer> allowedIds = getGroupCardIds(group._id);

            // Empty group
            if (allowedIds.size() > 0) {
                groupFilter.append("AND (");

                for (int i = 0; i < allowedIds.size(); i++) {
                    groupFilter.append(LoyaltyCardDbIds.ID + " = " + allowedIds.get(i));
                    if (i != allowedIds.size() - 1) {
                        groupFilter.append(" OR ");
                    }
                }
                groupFilter.append(") ");
            } else {
                limitString = "LIMIT 0";
            }
        }

        Cursor res = db.rawQuery("select * from " + LoyaltyCardDbIds.TABLE +
                " WHERE (" + LoyaltyCardDbIds.STORE + "  LIKE ? " +
                " OR " + LoyaltyCardDbIds.NOTE + " LIKE ? )" +
                groupFilter.toString() +
                " ORDER BY " + LoyaltyCardDbIds.STAR_STATUS + " DESC," + LoyaltyCardDbIds.STORE + " COLLATE NOCASE ASC " +
                limitString, selectionArgs, null);

        return res;
    }

    public int getLoyaltyCardCount()
    {
        // An empty string will match everything
        return getLoyaltyCardCount("");
    }

    /**
     * Returns the amount of loyalty cards with the filter text in either the store or note.
     *
     * @param filter
     * @return Integer
     */
    public int getLoyaltyCardCount(String filter)
    {
        String actualFilter = String.format("%%%s%%", filter);
        String[] selectionArgs = { actualFilter, actualFilter };

        SQLiteDatabase db = getReadableDatabase();
        Cursor data =  db.rawQuery("SELECT Count(*) FROM " + LoyaltyCardDbIds.TABLE +
                " WHERE " + LoyaltyCardDbIds.STORE + "  LIKE ? " +
                " OR " + LoyaltyCardDbIds.NOTE + " LIKE ? "
                , selectionArgs, null);

        int numItems = 0;

        if(data.getCount() == 1)
        {
            data.moveToFirst();
            numItems = data.getInt(0);
        }

        data.close();

        return numItems;
    }

    /**
     * Returns a cursor to all groups.
     *
     * @return Cursor
     */
    public Cursor getGroupCursor()
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor res = db.rawQuery("select * from " + LoyaltyCardDbGroups.TABLE +
                " ORDER BY " + LoyaltyCardDbGroups.ORDER + " ASC," + LoyaltyCardDbGroups.ID + " COLLATE NOCASE ASC", null, null);
        return res;
    }

    public List<Group> getGroups() {
        Cursor data = getGroupCursor();

        List<Group> groups = new ArrayList<>();

        if (!data.moveToFirst()) {
            return groups;
        }

        groups.add(Group.toGroup(data));

        while (data.moveToNext()) {
            groups.add(Group.toGroup(data));
        }

        return groups;
    }

    public void reorderGroups(final List<Group> groups)
    {
        Integer order = 0;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues;

        for (Group group : groups)
        {
            contentValues = new ContentValues();
            contentValues.put(LoyaltyCardDbGroups.ORDER, order);

            db.update(LoyaltyCardDbGroups.TABLE, contentValues,
                    LoyaltyCardDbGroups.ID + "=?",
                    new String[]{group._id});

            order++;
        }
    }

    public Group getGroup(final String groupName)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("select * from " + LoyaltyCardDbGroups.TABLE +
                " where " + LoyaltyCardDbGroups.ID + "=?", new String[]{groupName});

        Group group = null;

        if(data.getCount() == 1)
        {
            data.moveToFirst();

            group = Group.toGroup(data);
        }

        data.close();

        return group;
    }

    public int getGroupCount()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data =  db.rawQuery("SELECT Count(*) FROM " + LoyaltyCardDbGroups.TABLE, null);

        int numItems = 0;

        if(data.getCount() == 1)
        {
            data.moveToFirst();
            numItems = data.getInt(0);
        }

        data.close();

        return numItems;
    }

    public List<Integer> getGroupCardIds(final String groupName)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data =  db.rawQuery("SELECT " + LoyaltyCardDbIdsGroups.cardID +
                " FROM " + LoyaltyCardDbIdsGroups.TABLE +
                " WHERE " + LoyaltyCardDbIdsGroups.groupID + " =? ", new String[]{groupName});

        List<Integer> cardIds = new ArrayList<>();

        if (!data.moveToFirst()) {
            return cardIds;
        }

        cardIds.add(data.getInt(0));

        while (data.moveToNext()) {
            cardIds.add(data.getInt(0));
        }

        data.close();

        return cardIds;
    }

    public long insertGroup(final String name)
    {
        if (name.isEmpty()) return -1;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbGroups.ID, name);
        contentValues.put(LoyaltyCardDbGroups.ORDER, getGroupCount());
        final long newId = db.insert(LoyaltyCardDbGroups.TABLE, null, contentValues);
        return newId;
    }

    public boolean insertGroup(final SQLiteDatabase db, final String name)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbGroups.ID, name);
        contentValues.put(LoyaltyCardDbGroups.ORDER, getGroupCount());
        final long newId = db.insert(LoyaltyCardDbGroups.TABLE, null, contentValues);
        return (newId != -1);
    }

    public boolean updateGroup(final String groupName, final String newName)
    {
        if (newName.isEmpty()) return false;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardDbGroups.ID, newName);
        try {
            int rowsUpdated = db.update(LoyaltyCardDbGroups.TABLE, contentValues,
                    LoyaltyCardDbGroups.ID + "=?",
                    new String[]{groupName});
            return (rowsUpdated == 1);
        } catch (android.database.sqlite.SQLiteConstraintException _e) {
            return false;
        }
    }

    public boolean deleteGroup(final String groupName)
    {
        SQLiteDatabase db = getWritableDatabase();
        // Delete group
        int rowsDeleted = db.delete(LoyaltyCardDbGroups.TABLE,
                LoyaltyCardDbGroups.ID + " = ? ",
                new String[]{groupName});

        // And delete lookup table entries associated with this group
        db.delete(LoyaltyCardDbIdsGroups.TABLE,
                LoyaltyCardDbIdsGroups.groupID + " = ? ",
                new String[]{groupName});

        return (rowsDeleted == 1);
    }

    public int getGroupCardCount(final String groupName)
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor data =  db.rawQuery("SELECT Count(*) FROM " + LoyaltyCardDbIdsGroups.TABLE +
                " where " + LoyaltyCardDbIdsGroups.groupID + "=?",
                new String[]{groupName});

        int numItems = 0;

        if(data.getCount() == 1)
        {
            data.moveToFirst();
            numItems = data.getInt(0);
        }

        data.close();

        return numItems;
    }
}

