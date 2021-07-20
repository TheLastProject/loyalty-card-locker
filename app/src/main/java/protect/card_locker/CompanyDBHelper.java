package protect.card_locker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CompanyDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "CatimaCompanyList.db";
    public static final int DATABASE_VERSION = 1;

    static class LoyaltyCardProgramDbCountries
    {
        public static final String TABLE = "countries";
        public static final String ID = "_id";
    }

    static class LoyaltyCardProgramDbIds
    {
        public static final String TABLE = "programs";
        public static final String ID = "_id";
        public static final String NAME = "name";
    }

    static class LoyaltyCardProgramDbIdsCountries
    {
        public static final String TABLE = "programCountries";
        public static final String programId = "programId";
        public static final String countryId = "countryId";
    }

    public CompanyDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // drop all tables
        db.execSQL("drop table if exists " + LoyaltyCardProgramDbCountries.TABLE);
        db.execSQL("drop table if exists " + LoyaltyCardProgramDbIds.TABLE);
        db.execSQL("drop table if exists " + LoyaltyCardProgramDbIdsCountries.TABLE);

        // create table for program countries
        db.execSQL("create table " + CompanyDBHelper.LoyaltyCardProgramDbCountries.TABLE + "(" +
                CompanyDBHelper.LoyaltyCardProgramDbCountries.ID + " TEXT primary key not null)");

        // create table for programs
        db.execSQL("create table " + LoyaltyCardProgramDbIds.TABLE + "(" +
                LoyaltyCardProgramDbIds.ID + " int primary key not null," +
                LoyaltyCardProgramDbIds.NAME + " TEXT not null)");

        // create associative table for programs in countries
        db.execSQL("create table " + LoyaltyCardProgramDbIdsCountries.TABLE + "(" +
                LoyaltyCardProgramDbIdsCountries.programId + " TEXT," +
                LoyaltyCardProgramDbIdsCountries.countryId + " TEXT," +
                "primary key (" + LoyaltyCardProgramDbIdsCountries.programId + "," + LoyaltyCardProgramDbIdsCountries.countryId + "))");

        // create all entries
        addCompany(0, new Company.Builder("Albert Heijn").addBarcodeFormat(BarcodeFormat.EAN_13).create(), createLocaleList(Arrays.asList("NL")));
        addCompany(1, new Company.Builder("Air Miles").addBarcodeFormat(BarcodeFormat.EAN_13).addPrefix("470").create(), createLocaleList(Arrays.asList("NL")));
        addCompany(2, new Company.Builder("HEMA").addBarcodeFormat(BarcodeFormat.QR_CODE).create(), createLocaleList(Arrays.asList("NL")));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    private List<Locale> createLocaleList(List<String> locales) {
        List<Locale> localeList = new ArrayList<>();
        for (String locale : locales) {
            localeList.add(new Locale("en", locale));
        }

        return localeList;
    }

    private void addCompany(Integer id, Company company, List<Locale> locales) {
        SQLiteDatabase db = getWritableDatabase();

        // Create programs
        // TODO: Insert supported barcodes and translations rules
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoyaltyCardProgramDbIds.ID, id);
        contentValues.put(LoyaltyCardProgramDbIds.NAME, company.getName());
        db.insert(CompanyDBHelper.LoyaltyCardProgramDbIds.TABLE, null, contentValues);

        // Couple to locales
        ContentValues localeContentValues;
        ContentValues programLocales;
        for (Locale locale: locales) {
            String countryCode = locale.getCountry();

            // Create locale table if it doesn't exist
            localeContentValues = new ContentValues();
            localeContentValues.put(LoyaltyCardProgramDbCountries.ID, countryCode);

            db.insertWithOnConflict(LoyaltyCardProgramDbCountries.TABLE, "", localeContentValues, SQLiteDatabase.CONFLICT_IGNORE);

            // Couple to programs
            programLocales = new ContentValues();
            programLocales.put(LoyaltyCardProgramDbIdsCountries.countryId, countryCode);
            programLocales.put(LoyaltyCardProgramDbIdsCountries.programId, id);
            db.insert(LoyaltyCardProgramDbIdsCountries.TABLE, "", programLocales);
        }
    }
}
