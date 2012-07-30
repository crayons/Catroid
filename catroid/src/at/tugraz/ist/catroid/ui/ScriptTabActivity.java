/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.ui;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.common.CostumeData;
import at.tugraz.ist.catroid.common.SoundInfo;
import at.tugraz.ist.catroid.content.Script;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.content.bricks.ChangeSizeByNBrick;
import at.tugraz.ist.catroid.stage.PreStageActivity;
import at.tugraz.ist.catroid.stage.StageActivity;
import at.tugraz.ist.catroid.ui.dialogs.AddBrickDialog;
import at.tugraz.ist.catroid.ui.dialogs.BrickCategoryDialog;
import at.tugraz.ist.catroid.ui.dialogs.DeleteCostumeDialog;
import at.tugraz.ist.catroid.ui.dialogs.DeleteSoundDialog;
import at.tugraz.ist.catroid.ui.dialogs.FormulaEditorDialog;
import at.tugraz.ist.catroid.ui.dialogs.RenameCostumeDialog;
import at.tugraz.ist.catroid.ui.dialogs.RenameSoundDialog;
import at.tugraz.ist.catroid.utils.ActivityHelper;
import at.tugraz.ist.catroid.utils.Utils;

public class ScriptTabActivity extends TabActivity implements OnDismissListener, OnCancelListener {
	protected ActivityHelper activityHelper;

	private TabHost tabHost;
	private boolean addScript;
	private boolean isCanceled;
	public SoundInfo selectedSoundInfo;
	private RenameSoundDialog renameSoundDialog;
	public CostumeData selectedCostumeData;
	public int selectedPosition;
	private RenameCostumeDialog renameCostumeDialog;
	private DeleteCostumeDialog deleteCostumeDialog;
	private DeleteSoundDialog deleteSoundDialog;
	public String selectedCategory;
	public static final int DIALOG_RENAME_COSTUME = 0;
	public static final int DIALOG_RENAME_SOUND = 1;
	public static final int DIALOG_BRICK_CATEGORY = 2;
	public static final int DIALOG_ADD_BRICK = 3;
	public static final int DIALOG_DELETE_COSTUME = 4;
	public static final int DIALOG_DELETE_SOUND = 5;
	public static final int DIALOG_FORMULA = 6;

	private boolean dontcreateNewBrick;

	private FormulaEditorDialog currentFormulaEditorDialog;
	private Brick currentBrick;

	private void setupTabHost() {
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("info", "ScriptTabActivity.onCreate()");
		super.onCreate(savedInstanceState);
		addScript = false;
		isCanceled = false;
		dontcreateNewBrick = false;
		currentFormulaEditorDialog = null;
		currentBrick = null;

		setContentView(R.layout.activity_scripttab);
		Utils.loadProjectIfNeeded(this);

		setupTabHost();
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

		Intent intent; // Reusable Intent for each tab

		intent = new Intent().setClass(this, ScriptActivity.class);
		setupTab(R.drawable.ic_tab_scripts_selector, this.getString(R.string.scripts), intent);
		intent = new Intent().setClass(this, CostumeActivity.class);
		int costumeIcon;
		String costumeLabel;

		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		if (ProjectManager.getInstance().getCurrentProject().getSpriteList().indexOf(currentSprite) == 0) {
			costumeIcon = R.drawable.ic_tab_background_selector;
			costumeLabel = this.getString(R.string.backgrounds);
		} else {
			costumeIcon = R.drawable.ic_tab_costumes_selector;
			costumeLabel = this.getString(R.string.costumes);
		}
		setupTab(costumeIcon, costumeLabel, intent);
		intent = new Intent().setClass(this, SoundActivity.class);
		setupTab(R.drawable.ic_tab_sounds_selector, this.getString(R.string.sounds), intent);

		setUpActionBar();
		if (getLastNonConfigurationInstance() != null) {
			selectedCategory = (String) ((ArrayList<?>) getLastNonConfigurationInstance()).get(0);
			Log.i("info", "selectedCategory: " + selectedCategory);
			selectedCostumeData = (CostumeData) ((ArrayList<?>) getLastNonConfigurationInstance()).get(1);
			selectedSoundInfo = (SoundInfo) ((ArrayList<?>) getLastNonConfigurationInstance()).get(2);
		}

		FormulaEditorDialog.setOwnerActivity(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i("info", "ScriptTabActivity.onSaveInstanceState()");

		int brickIndexToSave = -1;
		int scriptIndexToSave = -1;

		if (this.currentBrick != null) {
			if (((ChangeSizeByNBrick) currentBrick).isEditorActive()) {

				Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();

				for (int h = 0; h < currentSprite.getNumberOfScripts(); h++) {

					Script script = ProjectManager.getInstance().getCurrentSprite().getScript(h);
					ArrayList<Brick> brickList = script.getBrickList();

					Log.i("info", "brickList.size(): " + brickList.size());

					for (int i = 0; i < brickList.size(); i++) {
						if (brickList.get(i).equals(this.currentBrick)) {
							brickIndexToSave = i;
							scriptIndexToSave = h;
							Log.i("info", "Brick found! index: " + brickIndexToSave);
						}
					}
				}

				View view = new View(this);
				view.setId(R.id.formula_editor_ok_button);
				this.currentFormulaEditorDialog.onClick(view);
				((ChangeSizeByNBrick) currentBrick).setEditorStatus(false);
			}
		}

		outState.putInt("brickIndex", brickIndexToSave);
		outState.putInt("scriptIndex", scriptIndexToSave);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i("info", "ScriptTabActivity.onRestoreInstanceState()");

		int savedBrickIndex = savedInstanceState.getInt("brickIndex");
		int savedScriptIndex = savedInstanceState.getInt("scriptIndex");
		Log.i("info", "savedBrickIndex: " + savedBrickIndex);
		Log.i("info", "savedScriptIndex: " + savedScriptIndex);

		if (savedBrickIndex != -1) {
			ChangeSizeByNBrick oldBrick = (ChangeSizeByNBrick) ProjectManager.getInstance().getCurrentSprite()
					.getScript(savedScriptIndex).getBrickList().get(savedBrickIndex);
			oldBrick.onClick(new View(this));
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public ArrayList<Object> onRetainNonConfigurationInstance() {
		Log.i("info", "ScriptTabActivity.onRetainNonConfigurationInstance()");
		ArrayList<Object> savedMember = new ArrayList<Object>();
		savedMember.add(selectedCategory);
		savedMember.add(selectedCostumeData);
		savedMember.add(selectedSoundInfo);
		return savedMember;
	}

	private void setUpActionBar() {
		Log.i("info", "ScriptTabActivity.setUpActionBar()");
		activityHelper = new ActivityHelper(this);

		String title = this.getResources().getString(R.string.sprite_name) + " "
				+ ProjectManager.getInstance().getCurrentSprite().getName();
		activityHelper.setupActionBar(false, title);

		activityHelper.addActionButton(R.id.btn_action_add_button, R.drawable.ic_plus_black, R.string.add, null, false);

		activityHelper.addActionButton(R.id.btn_action_play, R.drawable.ic_play_black, R.string.start,
				new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(ScriptTabActivity.this, PreStageActivity.class);
						startActivityForResult(intent, PreStageActivity.REQUEST_RESOURCES_INIT);
					}
				}, false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("info", "ScriptTabActivity.onActivityResult()");
		if (requestCode == PreStageActivity.REQUEST_RESOURCES_INIT && resultCode == RESULT_OK) {
			Intent intent = new Intent(ScriptTabActivity.this, StageActivity.class);
			startActivity(intent);
		}
	}

	private void setupTab(Integer drawableId, final String tag, Intent intent) {
		Log.i("info", "ScriptTabActivity.setupTab()");
		View tabview = createTabView(drawableId, tabHost.getContext(), tag);

		TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
		tabHost.addTab(setContent);

	}

	private static View createTabView(Integer id, final Context context, final String text) {
		Log.i("info", "ScriptTabActivity.createTabView()");
		View view = LayoutInflater.from(context).inflate(R.layout.activity_tabscriptactivity_tabs, null);
		TextView tabTextView = (TextView) view.findViewById(R.id.tabsText);
		ImageView tabImageView = (ImageView) view.findViewById(R.id.tabsIcon);
		tabTextView.setText(text);
		if (id != null) {
			tabImageView.setImageResource(id);
			tabImageView.setVisibility(ImageView.VISIBLE);
			tabImageView.setTag(id);
		}
		return view;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.i("info", "ScriptTabActivity.onCreateDialog()");
		Dialog dialog = null;
		switch (id) {
			case DIALOG_RENAME_SOUND:
				if (selectedSoundInfo != null) {
					renameSoundDialog = new RenameSoundDialog(this);
					dialog = renameSoundDialog.createDialog(selectedSoundInfo);
				}
				break;
			case DIALOG_RENAME_COSTUME:
				if (selectedCostumeData != null) {
					renameCostumeDialog = new RenameCostumeDialog(this);
					dialog = renameCostumeDialog.createDialog(selectedCostumeData);
				}
				break;
			case DIALOG_BRICK_CATEGORY:
				dialog = new BrickCategoryDialog(this);
				dialog.setOnDismissListener(this);
				dialog.setOnCancelListener(this);
				break;
			case DIALOG_ADD_BRICK:
				if (selectedCategory != null) {
					dialog = new AddBrickDialog(this, selectedCategory);
				}
				break;
			case DIALOG_DELETE_COSTUME:
				if (selectedCostumeData != null) {
					deleteCostumeDialog = new DeleteCostumeDialog(this);
					dialog = deleteCostumeDialog.createDialog();
				}
				break;
			case DIALOG_DELETE_SOUND:
				if (selectedSoundInfo != null) {
					deleteSoundDialog = new DeleteSoundDialog(this);
					dialog = deleteSoundDialog.createDialog();
				}
				break;
			case DIALOG_FORMULA:
				dialog = this.currentFormulaEditorDialog;
				Log.i("info", "case: DIALOG_FORMULA" + dialog);
				break;
			default:
				dialog = null;
				break;
		}
		return dialog;
	}

	public void setCurrentFormulaEditorDialog(FormulaEditorDialog currentFormulaEditorDialog) {
		Log.i("info", "ScriptTabActivity.setCurrentFormulaEditorDialog(): " + currentFormulaEditorDialog);
		this.currentFormulaEditorDialog = currentFormulaEditorDialog;
	}

	public void setCurrentBrick(Brick brick) {
		Log.i("info", "ScriptTabActivity.setCurrentBrick(): " + brick);
		this.currentBrick = brick;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		Log.i("info", "ScriptTabActivity.onPrepareDialog()");
		switch (id) {
			case DIALOG_RENAME_SOUND:
				EditText soundTitleInput = (EditText) dialog.findViewById(R.id.dialog_rename_sound_editText);
				soundTitleInput.setText(selectedSoundInfo.getTitle());
				break;
			case DIALOG_RENAME_COSTUME:
				EditText costumeTitleInput = (EditText) dialog.findViewById(R.id.dialog_rename_costume_editText);
				costumeTitleInput.setText(selectedCostumeData.getCostumeName());
				break;
			case DIALOG_FORMULA:
				//				dialog = this.currentFormulaEditorDialog;
				Log.i("info", "case DIALOG_FORMULA" + " dialog: " + dialog + " currentFormulaEditorDialog: "
						+ this.currentFormulaEditorDialog);
				//				this.showDialog(DIALOG_FORMULA, null);
				break;
		}
	}

	public void handlePositiveButtonRenameSound(View v) {
		renameSoundDialog.handleOkButton();
	}

	public void handleNegativeButtonRenameSound(View v) {
		dismissDialog(DIALOG_RENAME_SOUND);
	}

	public void handlePositiveButtonRenameCostume(View v) {
		renameCostumeDialog.handleOkButton();
	}

	public void handleNegativeButtonRenameCostume(View v) {
		dismissDialog(DIALOG_RENAME_COSTUME);
	}

	public void handlePositiveButtonDeleteCostume(View v) {
		deleteCostumeDialog.handleOkButton();
	}

	public void handleNegativeButtonDeleteCostume(View v) {
		dismissDialog(DIALOG_DELETE_COSTUME);
	}

	public void handlePositiveButtonDeleteSound(View v) {
		deleteSoundDialog.handleOkButton();
	}

	public void handleNegativeButtonDeleteSound(View v) {
		dismissDialog(DIALOG_DELETE_SOUND);
	}

	public void onDismiss(DialogInterface dialogInterface) {
		Log.i("info", "ScriptTabActivity.onDismiss()");
		if (!dontcreateNewBrick) {
			if (!isCanceled) {
				if (addScript) {

					((ScriptActivity) getCurrentActivity()).setAddNewScript();
					addScript = false;
				}

				((ScriptActivity) getCurrentActivity()).updateAdapterAfterAddNewBrick(dialogInterface);

			}
			isCanceled = false;
		}
		dontcreateNewBrick = false;
	}

	public void onCancel(DialogInterface dialog) {
		isCanceled = true;
	}

	public void setNewScript() {
		addScript = true;
	}

	public void setDontcreateNewBrick() {
		dontcreateNewBrick = true;
	}
}
