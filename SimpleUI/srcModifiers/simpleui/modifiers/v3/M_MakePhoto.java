package simpleui.modifiers.v3;

import java.io.File;
import java.io.IOException;

import simpleui.modifiers.ModifierInterface;
import simpleui.util.ActivityLifecycleListener;
import simpleui.util.IO;
import simpleui.util.ImageTransform;
import simpleui.util.KeepProcessAliveService;
import android.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

@TargetApi(5)
public abstract class M_MakePhoto implements ModifierInterface,
ActivityLifecycleListener {

	public final static int TAKE_PICTURE = 5356;
	public static final int SELECT_FROM_FILE = 24656;

	private int maxWidth = 640;
	private int maxHeight = 480;
	private int imageQuality = 60;

	private static final String LOG_TAG = "M_MakePhoto";

	private Bitmap takenBitmap;
	private Uri takenBitmapUri;

	private M_ImageView imageViewModifier;

	private Activity activity;

	private String imageFileName;

	/**
	 * if true the selected image from the gallery will be rewritten to the SD
	 * card
	 */
	private boolean rewriteImageToStorage = true;

	public M_MakePhoto() {
	}

	public M_MakePhoto(final Uri uri) {
		if (uri != null) {
			setFileToLoadInImageView(IO.toFile(uri));
		}
	}

	public M_MakePhoto(final File f) {
		setTakenBitmapFileAndUri(f);
	}

	private void setTakenBitmapFileAndUri(final File takenBitmapFile) {
		if (takenBitmapFile != null) {
			takenBitmapUri = IO.toUri(takenBitmapFile);
		}
	}

	public void setFileToLoadInImageView(final File bitmap) {
		try {
			setTakenBitmapFileAndUri(bitmap);
			takenBitmap = IO.loadBitmapFromUri(takenBitmapUri);
			refreshImageInImageView();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param startBitmap
	 *            the bitmap to show when the view is created
	 * @param maxWidth
	 *            e.g. 640
	 * @param maxHeight
	 *            e.g. 480
	 * @param jpgQuality
	 *            from 0 (small file size, bad quality) to 100 (large file size
	 *            goode image quality)
	 * @param restoreImageFromStorage
	 *            if true the selected image from the gallery will be rewritten
	 *            to the SD card
	 */
	public M_MakePhoto(final Bitmap startBitmap, final int maxWidth, final int maxHeight,
			final int jpgQuality, final boolean restoreImageFromStorage) {
		this.takenBitmap = startBitmap;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.imageQuality = jpgQuality;
		this.rewriteImageToStorage = restoreImageFromStorage;
	}

	@Override
	public View getView(final Context context) {

		final LinearLayout box = new LinearLayout(context);
		box.setOrientation(LinearLayout.VERTICAL);

		final String caption = getModifierCaption();
		if (caption != null) {
			final M_Caption c = new M_Caption(caption);
			box.addView(c.getView(context));
		}

		imageViewModifier = new M_ImageView();

		if (takenBitmap != null && takenBitmap.isRecycled()) {
			Log.w(LOG_TAG, "Image bitmap was recycled but "
					+ "not null so setting it to null");
			takenBitmap = null;
		}

		if (takenBitmapUri != null && takenBitmap == null) {
			try {
				takenBitmap = IO.loadBitmapFromUri(takenBitmapUri);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		box.addView(imageViewModifier.getView(context));
		refreshImageInImageView();

		final M_Button bTakePhoto = new M_Button(R.drawable.ic_menu_camera,
				getTextOnTakePhotoButton()) {

			@Override
			public void onClick(final Context context, final Button clickedButton) {
				takePhoto((Activity) context);
			}
		};
		box.addView(bTakePhoto.getView(context));

		final M_Button bSelectFromFile = new M_Button(R.drawable.ic_menu_gallery,
				getTextOnLoadFileButton()) {

			@Override
			public void onClick(final Context context, final Button clickedButton) {
				selectPhotoFromFile((Activity) context);
			}
		};
		box.addView(bSelectFromFile.getView(context));

		final M_Button deleteButton = new M_Button(R.drawable.ic_menu_delete,
				getTextOnDeleteButton()) {

			@Override
			public void onClick(final Context context, final Button clickedButton) {
				if (onDeleteRequest((Activity) context)) {
					removeImage();
				}
			}
		};
		box.addView(deleteButton.getView(context));

		return box;
	}

	public void removeImage() {
		imageViewModifier.removeImage();
		takenBitmapUri = null;
	}

	public abstract boolean onDeleteRequest(Activity context);

	public abstract String getTextOnDeleteButton();

	/**
	 * @return null to not add a caption
	 */
	public abstract String getModifierCaption();

	public abstract String getTextOnLoadFileButton();

	public abstract String getTextOnTakePhotoButton();

	public void takePhoto(final Activity activity) {

		/*
		 * TODO check if sd card available if yes then do it the current way if
		 * no use
		 * 
		 * i.putExtra( android.provider.MediaStore.EXTRA_OUTPUT,
		 * android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		 * 
		 * instead and hope that the device returns an data!=null in
		 * onActivityResult(Activity a, int requestCode, int resultCode, Intent
		 * data)
		 */

		final Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		imageFileName = getImageFileName();
		final File file = new File(Environment.getExternalStorageDirectory(),
				imageFileName);

		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			takenBitmapUri = IO.toUri(file);
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, takenBitmapUri);
			KeepProcessAliveService.startKeepAliveService(activity);
			Log.d(LOG_TAG, "Starting image capture to store in file: " + file);
			activity.startActivityForResult(i, TAKE_PICTURE);
		} catch (final IOException e) {
			onImageCantBeStoredInStorage(e);
		}

	}

	protected void selectPhotoFromFile(final Activity context) {
		// Intent intent = new Intent(Intent.ACTION_PICK,
		// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		final Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		KeepProcessAliveService.startKeepAliveService(context);
		context.startActivityForResult(intent, M_MakePhoto.SELECT_FROM_FILE);
	}

	public void onImageCantBeStoredInStorage(final IOException e) {
		e.printStackTrace();
	}

	/**
	 * @return e.g. "/myAppCache/" + new Date().getTime() + ".jpg"
	 */
	public abstract String getImageFileName();

	@Override
	public boolean save() {
		if (takenBitmapUri == null) {
			return true;
		}
		if (save(activity, IO.toFile(takenBitmapUri))) {
			Log.i(LOG_TAG,
					"Save action correct so setting bitmap reference to null");
			setTakenBitmap(null);
			return true;
		}
		return false;
	}

	/**
	 * @param activity
	 * @param takenBitmapFile
	 * @return
	 */
	public abstract boolean save(Activity activity, File takenBitmapFile);

	@Override
	public boolean onCloseWindowRequest(final Activity a) {
		return true;
	}

	@Override
	public void onStop(final Activity activity) {
	}

	@Override
	public void onActivityResult(final Activity a, final int requestCode, final int resultCode,
			final Intent data) {

		Log.d(LOG_TAG, "onActivityResult");
		Log.d(LOG_TAG, "resultCode=" + resultCode);
		Log.d(LOG_TAG, "requestCode=" + requestCode);
		if (resultCode == Activity.RESULT_OK) {
			activity = a;
			if (requestCode == M_MakePhoto.TAKE_PICTURE) {
				getBitmap(a, data);
			} else if (requestCode == M_MakePhoto.SELECT_FROM_FILE) {
				loadBitmapFromFile(a, data);
			}
			refreshImageInImageView();
		}
		KeepProcessAliveService.stopKeepAliveService();
	}

	private void refreshImageInImageView() {
		if (imageViewModifier != null && takenBitmap != null) {
			// TODO process image
			Log.d(LOG_TAG, "takenBitmap.getWidth()=" + takenBitmap.getWidth());
			Log.d(LOG_TAG, "takenBitmap.getHeight()=" + takenBitmap.getHeight());
			imageViewModifier.setImage(takenBitmapUri, takenBitmap);
		}
	}

	/**
	 * TODO
	 * http://stackoverflow.com/questions/2169649/open-an-image-in-androids-
	 * built-in-gallery-app-programmatically/4470069#4470069
	 * 
	 * @param a
	 * @param data
	 */
	private void loadBitmapFromFile(final Activity a, final Intent data) {
		if (data == null || data.getData() == null) {
			Log.e(LOG_TAG, "Could not load image from intent " + data);
			return;
		}

		final Uri selectedImageUri = data.getData();

		// MEDIA GALLERY
		String filePath = null;
		try {
			filePath = getPathFromImageFileSelectionIntent(a, selectedImageUri);
		} catch (final Exception e1) {
			e1.printStackTrace();
		}

		if (filePath == null) {
			Log.d(LOG_TAG, "Loading image from gallery intent");
			filePath = getFilePathFromGalleryIntent(a, selectedImageUri);
		}

		if (filePath == null) {
			Log.e(LOG_TAG, "Could not load image from intent " + data);
			return;
		}

		Log.i(LOG_TAG, "Loadin bitmap from " + filePath);
		try {
			setTakenBitmapFileAndUri(new File(filePath));
			setTakenBitmap(BitmapFactory.decodeFile(filePath));
			if (takenBitmap != null) {
				setTakenBitmap(ImageTransform.rotateAndResizeBitmap(a,
						takenBitmap, takenBitmapUri, maxWidth, maxHeight));
				if (rewriteImageToStorage) {
					setTakenBitmapFileAndUri(new File(
							Environment.getExternalStorageDirectory(),
							getImageFileName()));
					ImageTransform.tryToStoreBitmapToTargetFile(takenBitmap,
							IO.toFile(takenBitmapUri), imageQuality);
				}
			} else {
				Log.e(LOG_TAG, "Could not load bitmap from file " + filePath);
			}
		} catch (final Exception e) {
			Log.e(LOG_TAG, "Error while loading bitmap from " + filePath);
			e.printStackTrace();
		}
	}

	public static String getPathFromImageFileSelectionIntent(final Activity a, final Uri uri) {
		Log.i(LOG_TAG, "Loading image from storage path uri: " + uri);
		final String[] projection = { MediaColumns.DATA };
		final Cursor cursor = a.managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			final int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();
			return cursor.getString(columnIndex);
		} else {
			return uri.getPath();
		}
	}

	@Deprecated
	private String getFilePathFromGalleryIntent(final Activity a, final Uri uri) {
		try {
			final String[] filePathColumn = { MediaColumns.DATA };
			final Cursor cursor = a.getContentResolver().query(uri, filePathColumn,
					null, null, null);
			cursor.moveToFirst();
			final int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			final String filePath = cursor.getString(columnIndex);
			cursor.close();
			return filePath;
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private void getBitmap(final Activity a, final Intent data) {
		try {
			setTakenBitmap((Bitmap) data.getExtras().get("data"));
			Log.i(LOG_TAG,
					"Got small image preview for backup, now trying to load real bitmap");
		} catch (final Exception e1) {
			Log.i(LOG_TAG, "Could not load the small image preview "
					+ "for backup (not possible on some devices).");
		}
		try {

			if (takenBitmapUri == null && data != null) {
				takenBitmapUri = data.getData();
			}
			a.getContentResolver().notifyChange(takenBitmapUri, null);

			setTakenBitmap(rotateAndResizeReceivedImage(a, takenBitmapUri,
					maxWidth, maxHeight));

			setTakenBitmapFileAndUri(new File(
					Environment.getExternalStorageDirectory(), imageFileName));
			ImageTransform.tryToStoreBitmapToTargetFile(takenBitmap,
					IO.toFile(takenBitmapUri), imageQuality);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void setTakenBitmap(final Bitmap takenBitmap) {
		this.takenBitmap = takenBitmap;
	}

	@Override
	public void onPause(final Activity activity) {
	}

	@Override
	public void onResume(final Activity activity) {
	}

	private static Bitmap rotateAndResizeReceivedImage(final Context context,
			final Uri uri, final int maxWidthInPixel, final int maxHeightInPixel) {
		Log.d(LOG_TAG, "Loading bitmap object from uri: " + uri);
		Bitmap b = ImageTransform.getBitmapFromUri(context, uri,
				maxWidthInPixel, maxHeightInPixel);
		b = ImageTransform.rotateAndResizeBitmap(context, b, uri,
				maxWidthInPixel, maxHeightInPixel);
		return b;
	}

}
