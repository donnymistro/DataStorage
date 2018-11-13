package com.example.android.datastorage;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;
import com.example.android.datastorage.data.InventoryHelper;
import com.example.android.datastorage.data.InventoryContract.ProductEntry;
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;
    private InventoryHelper mDbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        /*Find the ListView which will be populated with the product data*/
        ListView productListView = findViewById(R.id.product_list);
        /* Find and set empty view on the ListView, so that it only shows when the list has 0 items.*/
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);
        /* Setup an adapter to create a list item for each row of product data in the Cursor.*/
        /* There is no product data until the loader finishes, so pass in null for the Cursor*/
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter) ;
        /* Set up on click listener for edit intent of the editor activity */
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });
        /* Kick off loader*/
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new InventoryHelper(this);
    }
     /* Helper method to insert hardcoded product data into the database. For debugging purposes only.*/
    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and the product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Learn German");
        values.put(ProductEntry.COLUMN_PRICE, ProductEntry.PRICE_THREE);
        values.put(ProductEntry.COLUMN_QUANTITY, "9");
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Rosetta Stone");
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, ProductEntry.NUMBER_TWO);
        // Insert a new row for Learn German in the database, returning the ID of that new row.
        // The first argument for db.insert() is the products table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Learn German.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }
    /*Helper method to delete all products in the database.*/
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_two, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /*Define a projection that specifies the wanted columns for the table*/
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        /* This Loader will execute the ContentProvider's query method on a background thread*/
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /*Update PetCursorAdapter with this new cursor containing new product data*/
        mCursorAdapter.swapCursor(data);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*Callback called when the data needs to be deleted*/
        mCursorAdapter.swapCursor(null);
    }
}