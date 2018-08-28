package com.patrikrizek.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.patrikrizek.inventoryapp.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {
    public BookCursorAdapter(Context context, Cursor c) {
        super( context, c, 0 );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from( context ).inflate( R.layout.list_item, parent, false );
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productNameTextView = (TextView) view.findViewById( R.id.product_name );
        TextView priceTextView = (TextView) view.findViewById( R.id.price );
        TextView quantityTextView = (TextView) view.findViewById( R.id.quantity );
        TextView supplierNameTextView = (TextView) view.findViewById( R.id.supplier_name );
        TextView supplierPhoneNumberTextView = (TextView) view.findViewById( R.id.supplier_phone_number );

        int productNameColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_PRODUCT_NAME );
        int priceColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_PRICE );
        int quantityColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_QUANTITY );
        int supplierNameColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_SUPPLIER_NAME );
        int supplierPhoneNumberColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER );
        String productName = cursor.getString( productNameColumnIndex );
        String price = cursor.getString( priceColumnIndex );
        String quantity = cursor.getString( quantityColumnIndex );
        String supplierName = cursor.getString( supplierNameColumnIndex );
        String supplierPhoneNumber = cursor.getString( supplierPhoneNumberColumnIndex );

        if (TextUtils.isEmpty( productName )) {
            productName = context.getString( R.string.product_name_not_entered );
        }

        if (TextUtils.isEmpty( productName )) {
            price = context.getString( R.string.price_not_entered );
        }

        if (TextUtils.isEmpty( productName )) {
            quantity = context.getString( R.string.quantity_not_entered );
        }

        if (TextUtils.isEmpty( productName )) {
            supplierName = context.getString( R.string.supplier_name_not_entered );
        }

        if (TextUtils.isEmpty( productName )) {
            supplierPhoneNumber = context.getString( R.string.supplier_phone_number_not_entered );
        }

        productNameTextView.setText(productName);
        priceTextView.setText(price);
        quantityTextView.setText(quantity);
        supplierNameTextView.setText(supplierName);
        supplierPhoneNumberTextView.setText(supplierPhoneNumber);

        Button saleBtn = (Button) view.findViewById( R.id.btn_sale );
        final String productID = cursor.getString( cursor.getColumnIndexOrThrow(BookEntry._ID));
        final String quantityAfterSale = quantity;
        saleBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.productSale( Integer.valueOf( productID ), Integer.valueOf( quantityAfterSale ) );
            }
        });
    }

}
