package com.example.android.datastorage;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.android.datastorage.data.InventoryContract.ProductEntry;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;

import org.w3c.dom.Text;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri mCurrentProductUri;
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
    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;
    /* OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.*/
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_layout);
        /* Handle intent for edit product */
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null){
            setTitle(R.string.editor_add_product);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_product_name);
        mPriceSpinner = findViewById(R.id.spinner_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);
        mSupplierEditText = findViewById(R.id.edit_supplier_name);
        mNumberEditText = findViewById(R.id.edit_supplier_number);
        final Button increment = findViewById(R.id.increment);
        final Button decrement = findViewById(R.id.decrement);
        Button order = findViewById(R.id.order);
        Button delete = findViewById(R.id.delete);
        if (mCurrentProductUri == null){
            setTitle(getString(R.string.editor_add_product));
            invalidateOptionsMenu();
            increment.setVisibility(View.VISIBLE);
            decrement.setVisibility(View.VISIBLE);
            order.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        } else {
            setTitle(getString(R.string.editor_edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mNumberEditText.setOnTouchListener(mTouchListener);
        mPriceSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();
        //Set up onClickListeners for the buttons
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getQuantity = mQuantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(getQuantity)){
                    decrement.setVisibility(View.GONE);
                    Context context = getApplicationContext();
                    CharSequence text = "A Valid Product must be saved first";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }
                int quantity = Integer.parseInt(getQuantity);
                if (quantity <= 0  ){
                    Context context = getApplicationContext();
                    CharSequence text = "No imaginary negative books allowed";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    mQuantityEditText.setText(String.valueOf(quantity --));
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                    getContentResolver().update(mCurrentProductUri, values, null, null);
                    }
            }
        });
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getQuantity = mQuantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(getQuantity)){
                    increment.setVisibility(View.GONE);
                    Context context = getApplicationContext();
                    CharSequence text = "A Valid Product must be saved first";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    int quantity = Integer.parseInt(getQuantity);
                    mQuantityEditText.setText(String.valueOf(quantity ++));
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                    getContentResolver().update(mCurrentProductUri, values, null, null);
                }
            }
        });
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getPhoneNumber = mNumberEditText.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + getPhoneNumber));
                startActivity(intent);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
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
    /* Get user input from editor and save product into database.*/
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierPhoneString = mNumberEditText.getText().toString().trim();
        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString)
                && TextUtils.isEmpty(supplierPhoneString)){
            Toast.makeText(this, getString(R.string.null_title),
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(this, getString(R.string.null_quantity),
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(this, getString(R.string.null_supplier),
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(this, getString(R.string.null_phone),
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (TextUtils.isEmpty(nameString)){
                Toast.makeText(this, getString(R.string.null_title),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(supplierString)){
                Toast.makeText(this, getString(R.string.null_supplier),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(supplierPhoneString) || supplierPhoneString.length() < 10 || supplierPhoneString.length() > 10){
                Toast.makeText(this, getString(R.string.null_phone),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(quantityString )){
                Toast.makeText(this, getString(R.string.null_quantity),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRICE, mPrice);
        values.put(ProductEntry.COLUMN_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);
        /* Determine if this is a new or existing product by checking if mCurrentProductUri is null or not*/
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update__product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    /* This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the MainActivity.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*This method is called when the back button is pressed.*/
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(supplierPhoneColumnIndex);
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(quantity);
            mSupplierEditText.setText(supplier);
            mNumberEditText.setText(phone);
            // Price is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Price 3, 1 is Price 1, 2 is Price 2).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (price) {
                case ProductEntry.PRICE_ONE:
                    mPriceSpinner.setSelection(1);
                    break;
                case ProductEntry.PRICE_TWO:
                    mPriceSpinner.setSelection(2);
                    break;
                default:
                    mPriceSpinner.setSelection(0);
                    break;
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mNumberEditText.setText("");
        mPriceSpinner.setSelection(0); // Select price three.
    }
    /*Show a dialog that warns the user there are unsaved changes that will be lost
     *if they continue leaving the editor.
     *discardButtonClickListener is the click listener for what to do when
     *the user confirms they want to discard their changes*/
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    /*Prompt the user to confirm that they want to delete this pet.*/
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    /* Perform the deletion of the product in the database.*/
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}