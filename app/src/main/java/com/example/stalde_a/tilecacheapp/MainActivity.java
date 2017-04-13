package com.example.stalde_a.tilecacheapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends ListActivity {

    private static final int REQUEST_CODE_GRANT_URI_PERMISSION = 100;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 98;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 99;

    private TileCacheAdapter mTileCacheAdapter;
    private List<TileCache> mTileCaches = new ArrayList<>();
    private Context mContext;
    private TileCacheManager mTileCacheManager;
    private SdCardAccess mSdCardAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSdCardAccess = new SdCardAccess(this);
        mTileCacheManager = new TileCacheManager(this, mSdCardAccess);

        mContext = this;

        Button grantUriPermissionButton = (Button) findViewById(R.id.grantUriPermissionButton);
        grantUriPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!mSdCardAccess.hasWritePermission()) {
                        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_GRANT_URI_PERMISSION);
                    } else {
                        showMessage("URI permission is already granted");
                    }
                } catch (Exception e) {
                    showErrorMessage(e);
                }
            }
        });

        Button revokeUriPermissionButton = (Button) findViewById(R.id.revokeUriPermissionButton);
        revokeUriPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri sdCardTreeUri = mSdCardAccess.getSdCardTreeUri();
                if (sdCardTreeUri != null) {
                    revokeUriPermission(sdCardTreeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
        });

        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(false);
            }
        });

        Button addSdCardButton = (Button) findViewById(R.id.addSdCardButton);
        addSdCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(true);
            }
        });

        Button refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData();
                mTileCacheAdapter.notifyDataSetChanged();
            }
        });

        setData();

        mTileCacheAdapter = new TileCacheAdapter(this,
                R.layout.row_layout, R.id.listText, mTileCaches);

        setListAdapter(mTileCacheAdapter);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void setData() {
        mTileCaches.clear();
        try {
            mTileCaches.addAll(mTileCacheManager.getTileCaches());
        } catch (Exception e) {
            showErrorMessage(e);
        }
    }

    private void add(final Boolean onSdCard) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        final EditText edittext = new EditText(mContext);
        alert.setMessage("Name");
        alert.setTitle("Tile Cache");
        alert.setView(edittext);
        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable text = edittext.getText();
                if (text != null && !Objects.equals(text.toString(), "")) {
                    try {
                        mTileCaches.add(mTileCacheManager.create(text.toString(), onSdCard));
                        mTileCacheAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        showErrorMessage(e);
                    }
                }
            }
        });
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_GRANT_URI_PERMISSION) {
                Uri treeUri = data.getData();
                this.grantUriPermission(this.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                this.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                mSdCardAccess.setSdCardTreeUri(treeUri);
            }
        }
    }

    private void showMessage(String message) {
        showMessage("Info", message);
    }

    private void showMessage(String title, String message) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setMessage(message);
        alert.setTitle(title);
        alert.show();
    }

    private void showErrorMessage(Exception e) {
        showMessage("Error", e.getMessage());
    }

    private class TileCacheAdapter extends ArrayAdapter<TileCache> {

        private TileCacheAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<TileCache> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TileCache tileCache = getItem(position);
                        mTileCacheManager.delete(tileCache);
                        mTileCaches.remove(tileCache);
                        mTileCacheAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        showErrorMessage(e);
                    }
                }
            });
            Button readDatabaseButton = (Button) view.findViewById(R.id.readDatabaseButton);
            readDatabaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TileCache selectedTileCache = (TileCache) getListView().getItemAtPosition(position);
                        SQLiteDatabase database = mTileCacheManager.getDatabase(selectedTileCache);
                    } catch (Exception e) {
                        showErrorMessage(e);
                    }
                }
            });
            return view;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showMessage("READ_EXTERNAL_STORAGE permission was granted");
                } else {
                    showMessage("READ_EXTERNAL_STORAGE permission was denied");
                }
                return;
            }
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showMessage("WRITE_EXTERNAL_STORAGE permission was granted");
                } else {
                    showMessage("WRITE_EXTERNAL_STORAGE permission was denied");
                }
                return;
            }
        }
    }
}