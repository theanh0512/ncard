package com.user.ncard.ui.catalogue;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by trong-android-dev on 25/10/17.
 */

public class S3TransferUtil implements LifecycleObserver {

    public static final int TYPE_IMAGES = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;

    String getMetaData(int type) {
        if (type == TYPE_IMAGES) {
            return "image";
        } else if (type == TYPE_AUDIO) {
            return "audio";
        } else if (type == TYPE_VIDEO) {
            return "video";
        }
        return "image";
    }

    // Indicates that no upload is currently selected
    private static final int INDEX_NOT_CHECKED = -1;

    // TAG for logging;
    private static final String TAG = "S3TransferUtil";

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    // A List of all transfers
    private List<TransferObserver> observers;

    // Context
    private Context context;

    private String folderName;

    private TrackingUpload trackingUpload;
    int startFileCount = 0;
    int totalFileCount = 0;
    List<String> fileNames = null; // For names to upload to server
    int type = TYPE_IMAGES;

    /**
     * This map is used to provide data to the SimpleAdapter above. See the
     * fillMap() function for how it relates observers to rows in the displayed
     * activity.
     */
//    private ArrayList<HashMap<String, Object>> transferRecordMaps;

    // Which row in the UI is currently checked (if any)
    private int checkedIndex;

    public S3TransferUtil(Context context, String folderName) {
        this.context = context;
        this.folderName = folderName;
        // Initializes TransferUtility, always do this before using it.
        transferUtility = Util.getTransferUtility(context);
        checkedIndex = INDEX_NOT_CHECKED;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        // Get the data from any transfer's that have already happened,
        initData();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        // Clear transfer listeners to prevent memory leak, or
        // else this activity won't be garbage collected.
        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }
    }

    /**
     * Gets all relevant transfers from the Transfer Service for populating the
     * UI
     */
    private void initData() {
        // Use TransferUtility to get all upload transfers.
        observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        TransferListener listener = new UploadListener(null); // TODO: check again file
        for (TransferObserver observer : observers) {

            // For each transfer we will will create an entry in
            // transferRecordMaps which will display
            // as a single row in the UI
            HashMap<String, Object> map = new HashMap<String, Object>();
            Util.fillMap(map, observer, false);

            // Sets listeners to in progress transfers
            if (TransferState.WAITING.equals(observer.getState())
                    || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                    || TransferState.IN_PROGRESS.equals(observer.getState())) {
                observer.setTransferListener(listener);
            }
        }
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    public void beginUpload(String filePath, int type, TrackingUpload trackingUpload) {

        if (filePath == null) {
            Toast.makeText(context, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        this.trackingUpload = trackingUpload;
        startFileCount = 0;
        totalFileCount = 1;
        // Create file name
        this.fileNames = new ArrayList<String>();
        File file = new File(filePath);
        String realName = file.getName().substring(0, file.getName().lastIndexOf(".")) + "_" + System.currentTimeMillis();
        String ext = file.getName().substring(file.getName().lastIndexOf("."));
        this.fileNames.add(realName + ext);

        this.type = type;

        // Meta data
        ObjectMetadata metadata = new ObjectMetadata();
        Map<String, String> userMetadata = new HashMap<String, String>();
        userMetadata.put("mediatype", getMetaData(type));
        metadata.setUserMetadata(userMetadata);

        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME + folderName, file.getName(),
                file, metadata);
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        observer.setTransferListener(new UploadListener(file));
    }

    // Upload multi files
    public void beginUploads(List<String> files, int type, TrackingUpload trackingUpload) {
        this.trackingUpload = trackingUpload;
        if (files == null || files.size() == 0) {
            /*Toast.makeText(context, "Could not find the filepath of the selected files",
                    Toast.LENGTH_LONG).show();*/
            this.trackingUpload.onFinishAll(null, type);
            return;
        }
        // Create file names list
        this.fileNames = new ArrayList<String>();
        for (int i = 0; i < files.size(); i++) {
            File file = new File(files.get(i));
            String name = file.getName().replace("Luban_", ""); // Remove lib Luban name prefix
            String realName = name.substring(0, name.lastIndexOf(".")) + "_" + System.currentTimeMillis();
            String ext = name.substring(name.lastIndexOf("."));
            this.fileNames.add(realName + ext);
        }
        this.type = type;

        // Meta data
        ObjectMetadata metadata = new ObjectMetadata();
        Map<String, String> userMetadata = new HashMap<String, String>();
        userMetadata.put("mediatype", getMetaData(type));
        metadata.setUserMetadata(userMetadata);

        startFileCount = 0;
        totalFileCount = files.size();
        for (int i = 0; i < files.size(); i++) {
            File file = new File(files.get(i));
            TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME + folderName, this.fileNames.get(i),
                    file, metadata);
            observer.setTransferListener(new UploadListener(file));
        }
    }

    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    public class UploadListener implements TransferListener {

        private File file;

        public UploadListener(File file) {
            this.file = file;
        }

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            trackingUpload.onProgressChanged(file, id, bytesCurrent, bytesTotal);
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(TAG, "onStateChanged: " + id + ", " + newState);
            if (trackingUpload != null) {
                if (newState == TransferState.COMPLETED) {
                    startFileCount++;
                    trackingUpload.onFinishAt(startFileCount);
                }
                if (startFileCount == totalFileCount) {
                    trackingUpload.onFinishAll(getAllFileNamesFromFilePath(), type);
                }
            }
        }
    }

    private List<String> getAllFileNamesFromFilePath() {
        List<String> fileNames = new ArrayList<>();
        if (this.fileNames != null && this.fileNames.size() > 0) {
            for (int i = 0; i < this.fileNames.size(); i++) {
                fileNames.add(Constants.PATH_NCARD + folderName + "/" + this.fileNames.get(i));
            }
        }
        return fileNames;
    }

    public interface TrackingUpload {

        void onFinishAll(List<String> fileNames, int type);

        void onFinishAt(int index);

        void onProgressChanged(File file, int id, long bytesCurrent, long bytesTotal);
    }
}
