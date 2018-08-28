package com.patrikrizek.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.patrikrizek.inventoryapp.data.BookContract;
import com.patrikrizek.inventoryapp.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;
    private Uri mCurrentBookUri;
    private EditText mProductNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;
    private boolean mBookHasChanged = false;
    private Button mBtnQuantityIncrease;
    private Button mBtnQuantityDecrease;
    private int quantity;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate( saveInstanceState );
        setContentView( R.layout.activity_editor );

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        if (mCurrentBookUri == null) {
            setTitle( getString( R.string.editor_activity_title_new_book ) );
            invalidateOptionsMenu();
        } else {
            setTitle( getString( R.string.editor_activity_title_edit_book ) );
            getLoaderManager().initLoader( EXISTING_BOOK_LOADER, null, this );
        }

        mProductNameEditText = (EditText) findViewById( R.id.edit_book_name );
        mPriceEditText = (EditText) findViewById( R.id.edit_book_price );
        mQuantityEditText = (EditText) findViewById( R.id.edit_book_quantity );
        mSupplierNameEditText = (EditText) findViewById( R.id.edit_supplier_name );
        mSupplierPhoneNumberEditText = (EditText) findViewById( R.id.edit_supplier_phone_number );
        mBtnQuantityIncrease = (Button) findViewById( R.id.btn_increase );
        mBtnQuantityDecrease = (Button) findViewById( R.id.btn_decrease );

        mProductNameEditText.setOnTouchListener( mTouchListener );
        mPriceEditText.setOnTouchListener( mTouchListener );
        mQuantityEditText.setOnTouchListener( mTouchListener );
        mSupplierNameEditText.setOnTouchListener( mTouchListener );
        mSupplierPhoneNumberEditText.setOnTouchListener( mTouchListener );
        mBtnQuantityIncrease.setOnClickListener( mOnClickListenerIncrease );
        mBtnQuantityDecrease.setOnClickListener( mOnClickListenerDecrease );


        Button callButton = findViewById( R.id.btn_call );
        callButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierPhoneNumber = mSupplierNameEditText.getText().toString().trim();
                if (supplierPhoneNumber == null || TextUtils.isEmpty( supplierPhoneNumber )) {
                    Toast.makeText( getApplicationContext(), R.string.invalid_or_empty_number, Toast.LENGTH_SHORT ).show();
                } else {
                    String currentPhoneNumber = mSupplierPhoneNumberEditText.getText().toString().trim();
                    Intent intent = new Intent( Intent.ACTION_DIAL, Uri.fromParts( "tel", currentPhoneNumber, null ) );
                    startActivity( intent );
                }
            }
        } );
    }

    private void saveBook() {
        String productNameString = mProductNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        if (mCurrentBookUri == null && TextUtils.isEmpty( productNameString )
                || TextUtils.isEmpty( priceString )
                || TextUtils.isEmpty( quantityString )
                || TextUtils.isEmpty( supplierNameString )
                || TextUtils.isEmpty( supplierPhoneNumberString )) {
            Toast.makeText( this, getString( R.string.error_while_empty_editor ), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put( BookEntry.COLUMN_PRODUCT_NAME, productNameString );
        if(!TextUtils.isEmpty( productNameString )) {

        }

        int price = 0;
        if (!TextUtils.isEmpty( priceString )) {
            price = Integer.parseInt( priceString );
        }
        values.put( BookEntry.COLUMN_PRICE, price );

        int quantity = 0;
        if (!TextUtils.isEmpty( quantityString )) {
            quantity = Integer.parseInt( quantityString );
        }
        values.put( BookEntry.COLUMN_QUANTITY, quantity );

        values.put( BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString );

        values.put( BookEntry.COLUMN_PRICE, supplierPhoneNumberString );


        if (mCurrentBookUri == null) {

            Uri newUri = getContentResolver().insert( BookEntry.CONTENT_URI, values );
            if (newUri == null) {
                Toast.makeText( this, getString( R.string.editor_insert_book_failed ), Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.editor_insert_book_successful ), Toast.LENGTH_SHORT ).show();
                finish();
            }
        } else {
            int rowsAffected = getContentResolver().update( mCurrentBookUri, values, null, null );
            if (rowsAffected == 0) {
                Toast.makeText( this, getString( R.string.editor_update_book_failed ), Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.editor_update_book_successful ), Toast.LENGTH_SHORT ).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_editor, menu );
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu( menu );

        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem( R.id.action_delete );
            menuItem.setVisible( false );
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBook();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask( EditorActivity.this );
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask( EditorActivity.this );
                    }
                };
                showUnsavedChangesDialog( discardButtonClickListener );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed() {
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discarButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog( discarButtonClickListener );
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_PRODUCT_NAME, BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUPPLIER_NAME, BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader( this, mCurrentBookUri, projection, null, null, null );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_PRODUCT_NAME );
            int priceColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_PRICE );
            int quantityColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_QUANTITY );
            int supplierNameColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_SUPPLIER_NAME );
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex( BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER );

            String productName = cursor.getString( productNameColumnIndex );
            int price = cursor.getInt( priceColumnIndex );
            int currentQuantity = cursor.getInt( quantityColumnIndex );
            String supplierName = cursor.getString( supplierNameColumnIndex );
            String supplierPhoneNumber = cursor.getString( supplierPhoneNumberColumnIndex );

            mProductNameEditText.setText( productName );
            mPriceEditText.setText( Integer.toString( price ) );
            mQuantityEditText.setText( Integer.toString( currentQuantity ) );
            mSupplierNameEditText.setText( supplierName );
            mSupplierPhoneNumberEditText.setText( supplierPhoneNumber );
            quantity = currentQuantity;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductNameEditText.setText( "" );
        mPriceEditText.setText( "" );
        mQuantityEditText.setText( "" );
        mSupplierNameEditText.setText( "" );
        mSupplierPhoneNumberEditText.setText( "" );
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.unsaved_changes_dialog_msg );
        builder.setPositiveButton( R.string.discard, discardButtonClickListener );
        builder.setNegativeButton( R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.delete_dialog_msg );
        builder.setPositiveButton( R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteBook();
            }
        } );
        builder.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete( mCurrentBookUri, null, null );

            if (rowsDeleted == 0) {
                Toast.makeText( this, getString( R.string.editor_delete_book_failed ), Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.editor_delete_book_successful ), Toast.LENGTH_SHORT ).show();
            }
        }
        finish();
    }

    private View.OnClickListener mOnClickListenerIncrease = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            quantityIncrease( mCurrentBookUri );
        }
    };

    public void quantityIncrease(Uri currentProductUri) {
        quantity++;
        ContentValues values = new ContentValues();
        values.put( BookContract.BookEntry.COLUMN_QUANTITY, quantity );
        getContentResolver().update( currentProductUri, values, null, null );
        Toast.makeText( this, getString( R.string.btn_quantity_increase ), Toast.LENGTH_SHORT ).show();
    }

    private View.OnClickListener mOnClickListenerDecrease = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            quantityDecrease( mCurrentBookUri );
        }
    };

    public void quantityDecrease(Uri currentProductUri) {
        if (quantity != 0 || quantity > 0) {
            quantity--;
            ContentValues values = new ContentValues();
            values.put( BookContract.BookEntry.COLUMN_QUANTITY, quantity );
            getContentResolver().update( currentProductUri, values, null, null );
            Toast.makeText( this, getString( R.string.btn_quantity_decrease ), Toast.LENGTH_SHORT ).show();
        } else {
            Toast.makeText( this, getString( R.string.btn_quantity_empty_stock ), Toast.LENGTH_SHORT ).show();
        }
    }
}

