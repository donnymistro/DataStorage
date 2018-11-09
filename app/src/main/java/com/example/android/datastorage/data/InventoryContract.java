package com.example.android.datastorage.data;
import android.provider.BaseColumns;
public final class InventoryContract {
    private InventoryContract(){}
     /*Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single book.*/
    public static final class ProductEntry implements BaseColumns {
        /** Name of database table for products */
        public final static String TABLE_NAME = "Books";
        /* Unique ID number for the book.*/
        public final static String _ID = BaseColumns._ID;
        /*Here we assign column titles and data type*/
        public final static String COLUMN_PRODUCT_NAME ="Product Name";
        public final static String COLUMN_QUANTITY = "Quantity";
        public final static String COLUMN_SUPPLIER_NAME = "Supplier Name";
        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "Supplier Phone Number";
        public final static String COLUMN_PRICE = "Price";
        public static final int PRICE_ONE = 5;
        public static final int PRICE_TWO = 10;
        public static final int PRICE_THREE = 20;
        public static final String NUMBER_ONE = "555-555-5555";
        public static final String NUMBER_TWO = "123-456-7777";
    }
}