package com.patrikrizek.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.patrikrizek.inventoryapp.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {
    public static final String LOG_TAG = BookProvider.class.getSimpleName();
    private static final int BOOKS = 100;
    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher( UriMatcher.NO_MATCH );

    static {
        sUriMatcher.addURI( BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS );
        sUriMatcher.addURI( BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID );
    }

    private BookDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper( getContext() );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match( uri );
        switch (match) {
            case BOOKS:
                cursor = database.query( BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                cursor = database.query( BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                break;
            default:
                throw new IllegalArgumentException( "Cannot query unknown URI " + uri );
        }
        cursor.setNotificationUri( getContext().getContentResolver(), uri );
        return cursor;

    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match( uri );
        switch (match) {
            case BOOKS:
                return insertBook( uri, contentValues );
            default:
                throw new IllegalArgumentException( "Insertion is not supported for " + uri );
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        String productName = values.getAsString( BookEntry.COLUMN_PRODUCT_NAME );
        if (productName == null) {
            throw new IllegalArgumentException( "Book requires a product name" );
        }
        Integer price = values.getAsInteger( BookEntry.COLUMN_PRICE );
        if (price != null && price < 0) {
            throw new IllegalArgumentException( "Book requires valid price" );
        }
        Integer quantity = values.getAsInteger( BookEntry.COLUMN_QUANTITY );
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException( "Book requires quantity at least one pcs" );
        }
        String supplierName = values.getAsString( BookEntry.COLUMN_SUPPLIER_NAME );
        if (supplierName == null) {
            throw new IllegalArgumentException( "Book requires a supplier name" );
        }
        Integer supplierPhoneNumber = values.getAsInteger( BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER );
        if (supplierPhoneNumber != null && supplierPhoneNumber < 0 && supplierPhoneNumber > 1000000000) {
            throw new IllegalArgumentException( "Book requires valid quantity" );
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert( BookEntry.TABLE_NAME, null, values );
        if (id == -1) {
            Log.e( LOG_TAG, "Failed to insert row for " + uri );
            return null;
        }

        getContext().getContentResolver().notifyChange( uri, null );
        return ContentUris.withAppendedId( uri, id );
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match( uri );
        switch (match) {
            case BOOKS:
                return updateBook( uri, contentValues, selection, selectionArgs );
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                return updateBook( uri, contentValues, selection, selectionArgs );
            default:
                throw new IllegalArgumentException( "Update is not supported for " + uri );
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey( BookEntry.COLUMN_PRODUCT_NAME )) {
            String productName = values.getAsString( BookEntry.COLUMN_PRODUCT_NAME );
            if (productName == null) {
                throw new IllegalArgumentException( "Book requires a product name" );
            }
        }

        if (values.containsKey( BookEntry.COLUMN_PRICE )) {
            Integer price = values.getAsInteger( BookEntry.COLUMN_PRICE );
            if (price != null && price < 0) {
                throw new IllegalArgumentException( "Book requires valid price" );
            }
        }

        if (values.containsKey( BookEntry.COLUMN_QUANTITY )) {
            Integer quantity = values.getAsInteger( BookEntry.COLUMN_QUANTITY );
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException( "Book requires valid quantity" );
            }
        }

        if (values.containsKey( BookEntry.COLUMN_SUPPLIER_NAME )) {
            String supplierName = values.getAsString( BookEntry.COLUMN_SUPPLIER_NAME );
            if (supplierName == null) {
                throw new IllegalArgumentException( "Book requires a supplier name" );
            }
        }

        if (values.containsKey( BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER )) {
            Integer supplierPhoneNumber = values.getAsInteger( BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER );
            if (supplierPhoneNumber != null && supplierPhoneNumber < 0 && supplierPhoneNumber > 1000000000) {
                throw new IllegalArgumentException( "Book requires valid quantity" );
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdate = database.update( BookEntry.TABLE_NAME, values, selection, selectionArgs );
        if (rowsUpdate != 0) {
            getContext().getContentResolver().notifyChange( uri, null );
        }
        return rowsUpdate;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match( uri );
        switch (match) {
            case BOOKS:
                rowsDeleted = database.delete( BookEntry.TABLE_NAME, selection, selectionArgs );
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                rowsDeleted = database.delete( BookEntry.TABLE_NAME, selection, selectionArgs );
                break;
            default:
                throw new IllegalArgumentException( "Deletion is not supported for " + uri );
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange( uri, null );
        }

        return rowsDeleted;
    }
}
