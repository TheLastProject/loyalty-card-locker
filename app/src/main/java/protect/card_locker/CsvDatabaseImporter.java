package protect.card_locker;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

/**
 * Class for importing a database from CSV (Comma Separate Values)
 * formatted data.
 *
 * The database's loyalty cards are expected to appear in the CSV data.
 * A header is expected for the each table showing the names of the columns.
 */
public class CsvDatabaseImporter implements DatabaseImporter
{
    public void importData(DBHelper db, InputStreamReader input) throws IOException, FormatException, InterruptedException
    {
        BufferedReader bufferedReader = new BufferedReader(input);

        bufferedReader.mark(100);

        Integer version = 1;

        try {
            version = Integer.parseInt(bufferedReader.readLine());
        } catch (NumberFormatException _e) {
            // Assume version 1
        }

        bufferedReader.reset();

        switch (version) {
            case 1:
                parseV1(db, bufferedReader);
                break;
            case 2:
                parseV2(db, bufferedReader);
                break;
            default:
                throw new FormatException(String.format("No code to parse version %s", version));
        }
    }

    public void parseV1(DBHelper db, BufferedReader input) throws IOException, FormatException, InterruptedException
    {
        final CSVParser parser = new CSVParser(input, CSVFormat.RFC4180.withHeader());

        SQLiteDatabase database = db.getWritableDatabase();
        database.beginTransaction();

        try
        {
            for (CSVRecord record : parser)
            {
                importLoyaltyCard(database, db, record);

                if(Thread.currentThread().isInterrupted())
                {
                    throw new InterruptedException();
                }
            }

            parser.close();
            database.setTransactionSuccessful();
        }
        catch(IllegalArgumentException|IllegalStateException e)
        {
            throw new FormatException("Issue parsing CSV data", e);
        }
        finally
        {
            database.endTransaction();
            database.close();
        }
    }

    public void parseV2(DBHelper db, BufferedReader input) throws IOException, FormatException, InterruptedException
    {
        SQLiteDatabase database = db.getWritableDatabase();
        database.beginTransaction();

        Integer part = 0;
        String stringPart = "";

        try {
            while (true) {
                String tmp = input.readLine();

                if (tmp == null || tmp.isEmpty()) {
                    switch (part) {
                        case 0:
                            // This is the version info, ignore
                            break;
                        case 1:
                            parseV2Groups(db, database, stringPart);
                            break;
                        case 2:
                            parseV2Cards(db, database, stringPart);
                            break;
                        case 3:
                            parseV2CardGroups(db, database, stringPart);
                            break;
                        default:
                            throw new FormatException("Issue parsing CSV data, too many parts for v2 parsing");
                    }

                    if (tmp == null) {
                        break;
                    }

                    part += 1;
                    stringPart = "";
                } else {
                    stringPart += tmp + "\n";
                }
            }
            database.setTransactionSuccessful();
        } catch (FormatException e) {
            throw new FormatException("Issue parsing CSV data", e);
        } finally {
            database.endTransaction();
            database.close();
        }
    }


    public void parseV2Groups(DBHelper db, SQLiteDatabase database, String data) throws IOException, FormatException, InterruptedException
    {
        // Parse groups
        final CSVParser groupParser = new CSVParser(new StringReader(data), CSVFormat.RFC4180.withHeader());

        try {
            for (CSVRecord record : groupParser) {
                importGroup(database, db, record);

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            }

            groupParser.close();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new FormatException("Issue parsing CSV data", e);
        }
    }

    public void parseV2Cards(DBHelper db, SQLiteDatabase database, String data) throws IOException, FormatException, InterruptedException
    {
        // Parse cards
        final CSVParser cardParser = new CSVParser(new StringReader(data), CSVFormat.RFC4180.withHeader());

        try {
            for (CSVRecord record : cardParser) {
                importLoyaltyCard(database, db, record);

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            }

            cardParser.close();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new FormatException("Issue parsing CSV data", e);
        }
    }

    public void parseV2CardGroups(DBHelper db, SQLiteDatabase database, String data) throws IOException, FormatException, InterruptedException
    {
        // Parse card group mappings
        final CSVParser cardGroupParser = new CSVParser(new StringReader(data), CSVFormat.RFC4180.withHeader());

        try {
            for (CSVRecord record : cardGroupParser) {
                importCardGroupMapping(database, db, record);

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            }

            cardGroupParser.close();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new FormatException("Issue parsing CSV data", e);
        }
    }

    /**
     * Extract an image from the items array. The index into the array
     * is determined by looking up the index in the fields map using the
     * "key" as the key. If no such key exists, defaultValue is returned
     * if it is not null. Otherwise, a FormatException is thrown.
     */
    private Bitmap extractImage(String key, CSVRecord record)
    {
        if(record.isMapped(key))
        {
            return Utils.base64ToBitmap(record.get(key));
        }

        return null;
    }

    /**
     * Extract a string from the items array. The index into the array
     * is determined by looking up the index in the fields map using the
     * "key" as the key. If no such key exists, defaultValue is returned
     * if it is not null. Otherwise, a FormatException is thrown.
     */
    private String extractString(String key, CSVRecord record, String defaultValue)
            throws FormatException
    {
        String toReturn = defaultValue;

        if(record.isMapped(key))
        {
            toReturn = record.get(key);
        }
        else
        {
            if(defaultValue == null)
            {
                throw new FormatException("Field not used but expected: " + key);
            }
        }

        return toReturn;
    }

    /**
     * Extract an integer from the items array. The index into the array
     * is determined by looking up the index in the fields map using the
     * "key" as the key. If no such key exists, or the data is not a valid
     * int, a FormatException is thrown.
     */
    private Integer extractInt(String key, CSVRecord record, boolean nullIsOk)
            throws FormatException
    {
        if(record.isMapped(key) == false)
        {
            throw new FormatException("Field not used but expected: " + key);
        }

        String value = record.get(key);
        if(value.isEmpty() && nullIsOk)
        {
            return null;
        }

        try
        {
            return Integer.parseInt(record.get(key));
        }
        catch(NumberFormatException e)
        {
            throw new FormatException("Failed to parse field: " + key, e);
        }
    }

    /**
     * Import a single loyalty card into the database using the given
     * session.
     */
    private void importLoyaltyCard(SQLiteDatabase database, DBHelper helper, CSVRecord record)
            throws IOException, FormatException
    {
        int id = extractInt(DBHelper.LoyaltyCardDbIds.ID, record, false);

        String store = extractString(DBHelper.LoyaltyCardDbIds.STORE, record, "");
        if(store.isEmpty())
        {
            throw new FormatException("No store listed, but is required");
        }

        String note = extractString(DBHelper.LoyaltyCardDbIds.NOTE, record, "");

        String cardId = extractString(DBHelper.LoyaltyCardDbIds.CARD_ID, record, "");
        if(cardId.isEmpty())
        {
            throw new FormatException("No card ID listed, but is required");
        }

        String barcodeType = extractString(DBHelper.LoyaltyCardDbIds.BARCODE_TYPE, record, "");

        Integer headerColor = null;

        if(record.isMapped(DBHelper.LoyaltyCardDbIds.HEADER_COLOR))
        {
            headerColor = extractInt(DBHelper.LoyaltyCardDbIds.HEADER_COLOR, record, true);
        }

        int starStatus = 0;
        try {
            starStatus = extractInt(DBHelper.LoyaltyCardDbIds.STAR_STATUS, record, false);
        } catch (FormatException _e ) {
            // This field did not exist in versions 0.28 and before
            // We catch this exception so we can still import old backups
        }
        if (starStatus != 1) starStatus = 0;

        Bitmap icon = extractImage(DBHelper.LoyaltyCardDbIds.ICON, record);

        helper.insertLoyaltyCard(database, id, store, note, cardId, barcodeType, headerColor, starStatus, icon);
    }

    /**
     * Import a single group into the database using the given
     * session.
     */
    private void importGroup(SQLiteDatabase database, DBHelper helper, CSVRecord record)
            throws IOException, FormatException
    {
        String id = extractString(DBHelper.LoyaltyCardDbGroups.ID, record, null);

        helper.insertGroup(database, id);
    }

    /**
     * Import a single card to group mapping into the database using the given
     * session.
     */
    private void importCardGroupMapping(SQLiteDatabase database, DBHelper helper, CSVRecord record)
            throws IOException, FormatException
    {
        Integer cardId = extractInt(DBHelper.LoyaltyCardDbIdsGroups.cardID, record, false);
        String groupId = extractString(DBHelper.LoyaltyCardDbIdsGroups.groupID, record, null);

        List<Group> cardGroups = helper.getLoyaltyCardGroups(cardId);
        cardGroups.add(helper.getGroup(groupId));
        helper.setLoyaltyCardGroups(database, cardId, cardGroups);
    }
}