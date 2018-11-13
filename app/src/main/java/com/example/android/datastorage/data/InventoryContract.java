package com.example.android.datastorage.data;
import android.net.Uri;
import android.provider.BaseColumns;
import android.content.ContentResolver;
public final class InventoryContract {
    private InventoryContract() {
    }
    public static final String CONTENT_AUTHORITY = "com.example.android.datastorage";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";
    /*Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single book.*/
    public static final class ProductEntry implements BaseColumns {
        /*The content URI to access the product data in the provider*/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);
        /*The MIME type of the CONTENT_URI for a list of product.*/
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        /*The MIME type of the CONTENT_URI for a single product.*/
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        /*Name of database table for products*/
        public final static String TABLE_NAME = "books";
        /* Unique ID number for the book.*/
        public final static String _ID = BaseColumns._ID;
        /*Here we assign column titles and data type*/
        public final static String COLUMN_PRODUCT_NAME = "book_name";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_SUPPLIER_NAME = "publisher_name";
        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
        public static final String NUMBER_ONE = "555-555-5555";
        public static final String NUMBER_TWO = "123-456-7777";
        public final static String COLUMN_PRICE = "price";
        public static final int PRICE_ONE = 5;
        public static final int PRICE_TWO = 10;
        public static final int PRICE_THREE = 20;
        public static boolean isValidPrice(int price) {
            if (price == PRICE_ONE || price == PRICE_TWO || price == PRICE_THREE) {
                return true;
            } else {
                return false;
            }
        }
    }
}