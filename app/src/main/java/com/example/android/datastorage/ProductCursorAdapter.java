package com.example.android.datastorage;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datastorage.data.InventoryContract.ProductEntry;
import com.example.android.datastorage.data.InventoryContract;
public class ProductCursorAdapter extends CursorAdapter {
    /* Constructs a new ProductCursorAdapter.*/
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }
    /*Makes a new blank list item view. No data is set (or bound) to the views yet.
     * context app context
     * cursor  The cursor from which to get the data. The cursor is already
     *         moved to the correct position.
     * parent  The parent to which the new view is attached to
     *         return the newly created list item view.*/
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }
    /* This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     * view    Existing view, returned earlier by newView() method
     * context app context
     * cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.*/
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.product_name);
        final TextView quantityTextView = view.findViewById(R.id.quantity);
        final Button sell = view.findViewById(R.id.sell);
        sell.setText(R.string.sell_copy);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView supplierTextView = view.findViewById(R.id.supplier_name);
        TextView supplierPhoneTextView = view.findViewById(R.id.supplier_phone_number);
        /* Find the columns of product attributes that needs to be displayed*/
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
        int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        /*Read the product attributes to the cursor for the current product*/
        String productName = cursor.getString(nameColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        String productSupplier = cursor.getString(supplierColumnIndex);
        String productSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
        /* Update the TextViews with the attributes for the current product*/
        nameTextView.setText(productName);
        quantityTextView.setText(context.getString(R.string.remaining) + productQuantity);
        priceTextView.setText(context.getString(R.string.dollar_sign) + productPrice);
        supplierTextView.setText(productSupplier);
        supplierPhoneTextView.setText(productSupplierPhone);
        /* OnClickListener for sell button*/
        sell.setOnClickListener(new View.OnClickListener() {
            int id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
            Uri content = Uri.withAppendedPath(ProductEntry.CONTENT_URI, Integer.toString(id));
            @Override
            public void onClick(View v) {
                sell.setEnabled(true);
                String getQuantity = productQuantity;
                int quantity = Integer.parseInt(getQuantity);
                if (quantity <= 1){
                    Toast.makeText(context, R.string.sold_out, Toast.LENGTH_SHORT).show();
                    sell.setEnabled(false);
                }
                if (quantity!=0){
                    sell.setEnabled(true);
                }
                else {
                    quantity --;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                    context.getContentResolver().update(content, values, null, null);
                    if (quantity == 0){
                        sell.setEnabled(true);
                    }
                }
            }
        });
    }
}