package com.example.android.datastorage;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.android.datastorage.data.InventoryContract.ProductEntry;
import com.example.android.datastorage.data.InventoryHelper;
public class EditorActivity extends AppCompatActivity {
    /** EditText field to enter the product name */
    private EditText mNameEditText;
    /** EditText field to enter the product price */
    private Spinner mPriceSpinner;
    /** EditText field to enter the product quantity */
    private EditText mQuantityEditText;
    /** EditText field to enter the supplier name */
    private EditText mSupplierEditText;
    /** EditText field to enter the supplier phone number */
    private EditText mNumberEditText;
    /** Product price. The possible valid values are in the InventoryContract.java file:
    * PRICE_ONE, PRICE_TWO, or PRICE_THREE*/
    private int mPrice = ProductEntry.PRICE_ONE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_layout);
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceSpinner = (Spinner) findViewById(R.id.spinner_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mNumberEditText = (EditText) findViewById(R.id.edit_supplier_number);
        setupSpinner();
    }
    /** Setup the dropdown spinner that allows the user to select the price of the product.*/
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_prices, android.R.layout.simple_spinner_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mPriceSpinner.setAdapter(genderSpinnerAdapter);
        // Set the integer mSelected to the constant values
        mPriceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.price_one))) {
                        mPrice = ProductEntry.PRICE_ONE;
                    } else if (selection.equals(getString(R.string.price_two))) {
                        mPrice = ProductEntry.PRICE_TWO;
                    } else {
                        mPrice = ProductEntry.PRICE_THREE;
                    }
                }
            }
            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPrice = ProductEntry.PRICE_THREE;
            }
        });
    }
    /**
     * Get user input from editor and save new product into database.
     */
    private void insertPet() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierPhoneString = mNumberEditText.getText().toString().trim();
        // Create database helper
        InventoryHelper mInventoryHelper = new InventoryHelper(this);
        // Gets the database in write mode
        SQLiteDatabase db = mInventoryHelper.getWritableDatabase();
        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRICE, mPrice);
        values.put(ProductEntry.COLUMN_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);
        // Insert a new row for product in the database, returning the ID of that new row.
        long newRowId = db.insert(ProductEntry.TABLE_NAME, null, values);
        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "Product saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                insertPet();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}