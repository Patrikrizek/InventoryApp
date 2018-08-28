package com.patrikrizek.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.patrikrizek.inventoryapp.data.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;
    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        FloatingActionButton fab = (FloatingActionButton) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( MainActivity.this, EditorActivity.class );
                startActivity( intent );
            }
        } );

        ListView bookListView = (ListView) findViewById( R.id.list );

        View emptyView = findViewById( R.id.empty_view );
        bookListView.setEmptyView( emptyView );

        mCursorAdapter = new BookCursorAdapter( this, null );
        bookListView.setAdapter( mCursorAdapter );

        bookListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent( MainActivity.this, EditorActivity.class );
                Uri currentBookUri = ContentUris.withAppendedId( BookEntry.CONTENT_URI, id );
                intent.setData( currentBookUri );
                startActivity( intent );
            }
        } );

        getLoaderManager().initLoader( BOOK_LOADER, null, this );
    }

    private void insertBook() {
        ContentValues values = new ContentValues();
        values.put( BookEntry.COLUMN_PRODUCT_NAME, "The Example Book" );
        values.put( BookEntry.COLUMN_PRICE, 10 );
        values.put( BookEntry.COLUMN_QUANTITY, 1 );
        values.put( BookEntry.COLUMN_SUPPLIER_NAME, "John Doe" );
        values.put( BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, 123456789 );
        Uri newUri = getContentResolver().insert( BookEntry.CONTENT_URI, values );
    }

    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete( BookEntry.CONTENT_URI, null, null );
        Log.v( "MainActivity", rowsDeleted + " rows deleted from book database" );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_book:
                insertBook();
                return true;
            case R.id.action_delete_entire_database:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_PRODUCT_NAME, BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUPPLIER_NAME, BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader( this, BookEntry.CONTENT_URI, projection, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor( data );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor( null );
    }

    public void productSale(int id, int quantity) {
        if (quantity != 0 || quantity > 0) {
            quantity--;
            ContentValues values = new ContentValues();
            values.put( BookEntry.COLUMN_QUANTITY, quantity );
            Uri updatedProductUri = ContentUris.withAppendedId( BookEntry.CONTENT_URI, id );
            int mRowsModified = getContentResolver().update( updatedProductUri, values, null, null );
            Toast.makeText( this, getString( R.string.btn_sale_one_product_sold ), Toast.LENGTH_SHORT ).show();
        } else {
            Toast.makeText( this, getString( R.string.btn_sale_sold_out ), Toast.LENGTH_SHORT ).show();
        }
    }
}