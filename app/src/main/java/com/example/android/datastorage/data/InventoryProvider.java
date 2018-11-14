package com.example.android.datastorage.data;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.net.Uri;
import android.util.Log;
import com.example.android.datastorage.data.InventoryContract.ProductEntry;
import java.net.URI;
import java.security.Provider;
public class InventoryProvider extends ContentProvider {
    /* URI matcher code for the content URI for the books table*/
    private static final int BOOKS = 100;
    /* URI matcher code for the content URI for a single book in the boos table*/
    private static final int BOOK_ID = 101;
    /*UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.*/
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /*Static initializer. This is run the first time anything is called from this class.*/
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS + "/#", BOOK_ID);
    }
    /* Tag for the log messages*/
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    /*Database helper object (Create and initialize a InventoryHelper object to gain access to the inventory database.)*/
    private InventoryHelper mDbHelper;
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryHelper(getContext());
        return true;
    }
    /* Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.*/
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        /* Set notification URI on the Cursor
        If the data at this URI changes, then we know we need to update the Cursor*/
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    /* Insert new data into the provider with the given ContentValues.*/
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    /* Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.*/
    private Uri insertProduct(Uri uri, ContentValues values) {
        /*Check that the name is not null*/
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        /*Check that the price is valid*/
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
        if (price == null || !ProductEntry.isValidPrice(price)) {
            throw new IllegalArgumentException("Product requires valid price");
        }
        /*If the quantity is provided, check that it's greater than or equal to 0*/
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity requires valid value");
        }
        /* Check that the supplier name is not null*/
        String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null){
            throw new IllegalArgumentException("Supplier requires a name");
        }
        /*Check that the phone number is valid*/
        Integer phone = values.getAsInteger(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (phone == null) {
            throw new IllegalArgumentException("Product requires valid supplier number");
        }
        /*Get writable database*/
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new product with the given values
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        /* Notify all listeners that the data has changed for the product content URI*/
        /* URI: content://com.example.android.datastorage/datastorage */
        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }
    /* Updates the data at the given selection and selection arguments, with the new ContentValues.*/
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    /*Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.*/
    private int updateProduct(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        // If the COLUMN_PRODUCT_NAME key is present, check that the name value is not null.*/
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a title");
            }
        }
        // If the COLUMN_PRICE key is present, check that the price value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
            if (price == null || !ProductEntry.isValidPrice(price)) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }
        // If the COLUMN_QUANTITY key is present, check that the quantity value is valid.
        if (values.containsKey(ProductEntry.COLUMN_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }
        // If the COLUMN_SUPPLIER_NAME key is present, check that the value is valid.
        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)){
            //Check that the supplier name is not blank
            String supplier = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null){
                throw new IllegalArgumentException("Product requires valid Supplier");
            }
        }
        // If the COLUMN_SUPPLIER_PHONE_NUMBER key is present, check that the value is valid.
        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER)){
            Integer phone = values.getAsInteger(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (phone == null){
                throw new IllegalArgumentException("Supplier name requires valid phone number");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsUpdated;
    }
    /* Delete the data at the given selection and selection arguments.*/
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        /*Get writeable database*/
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME,selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
    /* Returns the MIME type of data for the content URI.*/
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}