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