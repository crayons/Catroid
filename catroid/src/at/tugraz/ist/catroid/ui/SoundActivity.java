/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010  Catroid development team 
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.common.Consts;
import at.tugraz.ist.catroid.common.SoundInfo;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.io.SoundManager;
import at.tugraz.ist.catroid.io.StorageHandler;
import at.tugraz.ist.catroid.ui.dialogs.RenameSoundDialog;
import at.tugraz.ist.catroid.utils.ActivityHelper;
import at.tugraz.ist.catroid.utils.Utils;

public class SoundActivity extends ListActivity {
	private Sprite sprite;
	private ArrayList<SoundInfo> soundInfoList;
	private final int REQUEST_SELECT_MUSIC = 0;
	public SoundInfo selectedSoundInfo;
	private RenameSoundDialog renameSoundDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sound);
		sprite = ProjectManager.getInstance().getCurrentSprite();

		ArrayList<SoundInfo> currentSounds = sprite.getSoundList();
		soundInfoList = new ArrayList<SoundInfo>(currentSounds);

		setListAdapter(new SoundAdapter(this, R.layout.activity_sound_soundlist_item, soundInfoList));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Utils.checkForSdCard(this)) {
			return;
		}

		//set new functionality for actionbar add button:
		ScriptTabActivity scriptTabActivity = (ScriptTabActivity) getParent();
		ActivityHelper activityHelper = scriptTabActivity.activityHelper;
		if (activityHelper != null) {
			activityHelper.changeClickListener(R.id.btn_action_add_sprite, createAddSoundClickListener());
		}
	}

	private View.OnClickListener createAddSoundClickListener() {
		return new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("audio/*");
				startActivityForResult(Intent.createChooser(intent, "Select music"), REQUEST_SELECT_MUSIC);
			}
		};
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch (id) {
			case Consts.DIALOG_RENAME_SOUND:
				if (selectedSoundInfo == null) {
					dialog = null;
				} else {
					renameSoundDialog = new RenameSoundDialog(this);
					dialog = renameSoundDialog.createDialog(selectedSoundInfo);
				}
				break;
			default:
				dialog = null;
				break;
		}
		return dialog;
	}

	@Override
	protected void onPause() {
		super.onPause();
		ProjectManager projectManager = ProjectManager.getInstance();
		if (projectManager.getCurrentProject() != null) {
			projectManager.saveProject(this);
		}
	}

	private void updateSoundAdapter(String title, String fileName) {
		SoundInfo newSoundInfo = new SoundInfo();
		newSoundInfo.setTitle(title);
		newSoundInfo.setSoundFileName(fileName);
		soundInfoList.add(newSoundInfo);
		sprite.addSoundInfoToSoundList(newSoundInfo);
		((SoundAdapter) getListAdapter()).notifyDataSetChanged();
		//scroll down the list to the new item:
		{
			final ListView listView = getListView();
			listView.post(new Runnable() {
				public void run() {
					listView.setSelection(listView.getCount() - 1);
				}
			});
		}

	}

	public void handlePositiveButtonRenameSound(View v) {
		renameSoundDialog.handleOkButton();
	}

	public void handleNegativeButtonRenameSound(View v) {
		renameSoundDialog.renameDialog.cancel();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//when new sound title is selected and ready to be added to the catroid project
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SELECT_MUSIC) {
			String audioPath = "";
			//get real path of soundfile --------------------------
			{
				Uri audioUri = data.getData();
				String[] proj = { MediaStore.Audio.Media.DATA };
				Cursor actualSoundCursor = managedQuery(audioUri, proj, null, null, null);
				int actualSoundColumnIndex = actualSoundCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
				actualSoundCursor.moveToFirst();
				audioPath = actualSoundCursor.getString(actualSoundColumnIndex);
			}
			//-----------------------------------------------------

			//copy music to catroid:
			try {
				if (audioPath.equalsIgnoreCase("")) {
					throw new IOException();
				}
				File soundFile = StorageHandler.getInstance().copySoundFile(audioPath);
				String soundTitle = soundFile.getName().substring(33, soundFile.getName().length() - 4);
				String soundFileName = soundFile.getName();
				updateSoundAdapter(soundTitle, soundFileName);
			} catch (IOException e) {
				Utils.displayErrorMessage(this, this.getString(R.string.error_load_sound));
			}
		}
	}

	private class SoundAdapter extends ArrayAdapter<SoundInfo> {
		private ArrayList<SoundInfo> soundInfoItems;
		private SoundActivity activity;

		public SoundAdapter(final SoundActivity activity, int textViewResourceId, ArrayList<SoundInfo> items) {
			super(activity, textViewResourceId, items);
			this.soundInfoItems = items;
			this.activity = activity;
		}

		@Override
		public View getView(final int position, View convView, ViewGroup parent) {

			View convertView = convView;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.activity_sound_soundlist_item, null);
			}

			final SoundInfo soundInfo = soundInfoItems.get(position);

			if (soundInfo != null) {
				ImageView soundImage = (ImageView) convertView.findViewById(R.id.sound_img);
				final TextView soundNameTextView = (TextView) convertView.findViewById(R.id.sound_name);
				final Button renameSoundButton = (Button) convertView.findViewById(R.id.rename_button);

				soundNameTextView.setText(soundInfo.getTitle());

				renameSoundButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						activity.selectedSoundInfo = soundInfo;
						activity.showDialog(Consts.DIALOG_RENAME_SOUND);
					}
				});

				ImageButton playSound = (ImageButton) convertView.findViewById(R.id.play_button);
				playSound.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						SoundManager.getInstance().playSoundFile(soundInfo.getAbsolutePath());
					}
				});

				//play sound on speaker picture
				soundImage.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						SoundManager.getInstance().playSoundFile(soundInfo.getAbsolutePath());
					}
				});

				ImageButton stopSound = (ImageButton) convertView.findViewById(R.id.stop_button);
				stopSound.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						SoundManager.getInstance().stop();
					}
				});

				ImageButton deleteSound = (ImageButton) convertView.findViewById(R.id.delete_button);
				deleteSound.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						soundInfoItems.remove(soundInfo);
						sprite.removeSoundInfoFromSoundList(soundInfo);
						StorageHandler.getInstance().deleteFile(soundInfo.getAbsolutePath());
						notifyDataSetChanged();
					}
				});

			}
			return convertView;
		}
	}
}
