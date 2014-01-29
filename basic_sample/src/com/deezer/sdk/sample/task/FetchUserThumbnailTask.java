package com.deezer.sdk.sample.task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.deezer.sdk.model.User;


/**
 * An async task which fetches a user thumbnail image from Deezer servers
 * 
 * @author Deezer
 * 
 */
public class FetchUserThumbnailTask extends AsyncTask<User, User, Void> {
    
    /** Buffer size */
    private static final int BUFFERED_STREAM_BUFFER_SIZE = 8196;
    
    /** Log cat tag. */
    private static final String LOG_TAG = "FetchUserThumbnailTask";
    
    /** RAM cache of thumbs. Associates a url to an image, shared by all instances. */
    private static Map<String, Drawable> sThumbnailsCache = new HashMap<String, Drawable>();
    
    /** the current app context */
    private Context mContext;
    
    private UserThumbnailTaskListener mListener;
    
    /**
     * Defines the behavior of a listener for thumbnail download events
     * 
     * @author Deezer
     * 
     */
    public interface UserThumbnailTaskListener {
        
        /**
         * Called to notify the listener that the thumbnail image of a user has been downloaded
         * successfuly.
         * 
         * @param user
         *            the user whose thumbnail is now available.
         * @param drawable
         *            the thumbnail
         */
        public void thumbnailLoaded(User user, Drawable drawable);
    }
    
    /**
     * @param context
     *            the app's context
     */
    public FetchUserThumbnailTask(Context context, UserThumbnailTaskListener listener) {
        mContext = context;
        mListener = listener;
    }
    
    
    @Override
    protected Void doInBackground(User... params) {
        
        for (User user : params) {
            
            //android guidelines suggest to check the cancel state
            if (isCancelled()) {
                return null;
            }
            
            String pictureUrl = user.getPictureUrl();
            
            Log.d(LOG_TAG, "Getting " + pictureUrl);
            if (pictureUrl == null || pictureUrl.length() == 0) {
                Log.d(LOG_TAG, pictureUrl + " is null or empty. Passed");
                continue;
            }
            
            //check in ram cache
            Drawable d = sThumbnailsCache.get(pictureUrl);
            
            if (d == null) {
                
                //if not present, download
                try {
                    d = cacheUrlAndCreateDrawable(pictureUrl);
                    if (d != null) {
                        sThumbnailsCache.put(pictureUrl, d);
                    }
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Error happened during download of " + pictureUrl, e);
                }
            }
            
            //if in cache now
            if (d != null) {
                publishProgress(user);
            }
        }
        return null;
    }
    
    @Override
    protected void onProgressUpdate(User... values) {
        super.onProgressUpdate(values);
        for (User user : values) {
            mListener.thumbnailLoaded(user, sThumbnailsCache.get(user.getPictureUrl()));
        }
    }
    
    
    /**
     * Put a picture Url content in cache and returns its drawable content.
     * 
     * @param pictureUrl
     *            the Url of the picture to download.
     * @return the drawable of the image pointed by the url.
     * 
     * @throws IOException
     *             if an IO error happens during download or copy to cache.
     * @throws MalformedURLException
     *             if the url is malformed.
     */
    private Drawable cacheUrlAndCreateDrawable(String pictureUrl)
            throws MalformedURLException, IOException {
        
        Drawable d;
        
        //create cache file 
        String fileName = createCacheFileName(pictureUrl);
        File cacheFile = new File(mContext.getCacheDir(), fileName);
        
        // download the file (if it does not exist)
        Log.d(LOG_TAG, "Fetching " + pictureUrl);
        if (!cacheFile.exists()) {
            downloadToCache(pictureUrl, cacheFile);
        }
        
        //create bitmap from downloaded bytes and put it in ram cache
        Log.d(LOG_TAG, pictureUrl + " in cache");
        d = getDrawableFromCache(cacheFile);
        return d;
    }
    
    /**
     * Create the fileName of the cache file associated to an url.
     * 
     * @param url
     *            the url to cache.
     * @return the fileName of the cache file.
     */
    private String createCacheFileName(String url) {
        return "thumb-" + url.hashCode() + "-"
                + url.substring(url.lastIndexOf('/') + 1);
    }
    
    
    /**
     * Downloads a given url to disk cache.
     * 
     * @param url
     *            the uri of a file to download.
     * @param cacheFile
     *            the cache file destination.
     * 
     * @throws IOException
     *             if an IO error happens during download or copy to cache.
     * @throws MalformedURLException
     *             if the url is malformed.
     */
    private void downloadToCache(String url, File cacheFile)
            throws MalformedURLException, IOException {
        
        Log.v(LOG_TAG, "Fetching " + url + " and caching in " + cacheFile.getAbsolutePath());
        InputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        
        try {
            //internal buffer for download.
            final byte[] buffer = new byte[5000];
            inputStream = (InputStream) new URL(url).getContent();
            if (inputStream == null) {
                return;
            }
            
            outputStream = new BufferedOutputStream(new FileOutputStream(cacheFile),
                    BUFFERED_STREAM_BUFFER_SIZE);
            int read = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            
            Log.d(LOG_TAG, url + " fetched");
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Can't close cache input stream", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Can't close cache output stream", e);
                }
            }
        }
    }
    
    /**
     * Reads a drawable from cache file.
     * 
     * @param cacheFile
     *            the file containing the drawable.
     * @return the drawable in cacheFile or null if the file could not be read
     */
    private Drawable getDrawableFromCache(File cacheFile) {
        
        BufferedInputStream inputStream = null;
        try {
            FileInputStream fis = new FileInputStream(cacheFile);
            Log.d(LOG_TAG, "Has fis " + fis);
            inputStream = new BufferedInputStream(fis,
                    BUFFERED_STREAM_BUFFER_SIZE);
            Log.d(LOG_TAG, "Has is " + inputStream);
            return Drawable.createFromStream(inputStream, "");
        }
        catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Can't find file :" + cacheFile, e);
            return null;
        }
        catch (OutOfMemoryError e) {
            Log.e(LOG_TAG, "Not enough memory :" + cacheFile, e);
            return null;
        }
        catch (NullPointerException e) {
            Log.e(LOG_TAG, "NPE :" + cacheFile, e);
            return null;
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Can't close cache input stream", e);
                }
            }
        }
    }
    
}
