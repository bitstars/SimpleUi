package v3;

import java.io.File;

import tools.KeepProcessAliveService;
import v2.simpleUi.ActivityLifecycleListener;
import v2.simpleUi.M_Button;
import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;

public abstract class M_FilePickerButton extends M_Button implements
ActivityLifecycleListener {

	private static final int SELECT_FILE_CODE = 213;
	private File folderLocation;

	public M_FilePickerButton(final String buttonText) {
		super(R.drawable.ic_input_add, buttonText);
	}

	public M_FilePickerButton(final String buttonText,
			final File folderLocation) {
		this(buttonText);
		this.folderLocation = folderLocation;
	}

	@Override
	public void onClick(final Context context, final Button clickedButton) {
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		KeepProcessAliveService.startKeepAliveService(context);
		final String type = "file/*";
		if (folderLocation != null) {// User specified a folder to start from
			intent.setDataAndType(Uri.fromFile(folderLocation), type);
		} else {
			intent.setType(type);
		}
		((Activity) context).startActivityForResult(intent, SELECT_FILE_CODE);
	}

	@Override
	public void onActivityResult(final Activity a, final int requestCode, final int resultCode,
			final Intent data) {
		if (requestCode == SELECT_FILE_CODE) {
			KeepProcessAliveService.stopKeepAliveService();
			if (resultCode == Activity.RESULT_OK) {

				final String filePath = M_MakePhoto
						.getPathFromImageFileSelectionIntent(a, data.getData());
				final File file = new File(filePath);
				if (file.isFile()) {
					onFilePathReceived(a, filePath, file, data);
					return;
				}
			}
			onNoFileSuccessfullySelected(a, data);
		}
	}

	public void onNoFileSuccessfullySelected(final Activity a, final Intent data) {
		// on default do nothing
	}

	/**
	 * The class where this modifier is created in has to impelemt
	 * {@link ActivityLifecycleListener} and inform this
	 * {@link M_FilePickerButton} when
	 * {@link ActivityLifecycleListener#onActivityResult(Activity, int, int, Intent)}
	 * is called, so it has to pass the event to it
	 * 
	 * @param a
	 * @param filePath
	 * @param file
	 * @param data
	 */
	public abstract void onFilePathReceived(Activity a, String filePath,
			File file, Intent data);

	@Override
	public void onStop(final Activity activity) {
	}

	@Override
	public boolean onCloseWindowRequest(final Activity activity) {
		return true;
	}

	@Override
	public void onPause(final Activity activity) {
	}

	@Override
	public void onResume(final Activity activity) {
	}

}
