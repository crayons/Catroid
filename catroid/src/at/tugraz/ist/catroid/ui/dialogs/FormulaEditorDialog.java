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
package at.tugraz.ist.catroid.ui.dialogs;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.formulaeditor.CalcGrammarParser;
import at.tugraz.ist.catroid.formulaeditor.CatKeyboardView;
import at.tugraz.ist.catroid.formulaeditor.Formula;
import at.tugraz.ist.catroid.formulaeditor.FormulaEditorEditText;
import at.tugraz.ist.catroid.formulaeditor.FormulaElement;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class FormulaEditorDialog extends DialogFragment implements OnClickListener, OnKeyListener {

	/**
	 * 
	 */
	public static final String FRAGMENT_TAG_FORMULA_EDITOR = "formula_editor_dialog";
	private static final int PARSER_OK = -1;
	private static final int PARSER_STACK_OVERFLOW = -2;
	private Context context;
	private static Brick currentBrick = null;
	private static Formula currentFormula = null;
	private FormulaEditorEditText formulaEditorEditText;
	private CatKeyboardView catKeyboardView;
	private LinearLayout brickSpace;
	private View brickView;
	private Button okButton = null;
	private Button undoButton = null;
	private Button redoButton = null;
	private long confirmBack = 0;
	private boolean buttonIsBackButton = true;
	public boolean restoreInstance = false;

	public FormulaEditorDialog() {
		//do not remove, used for orientation change
	}

	public FormulaEditorDialog(Brick brick, Formula formula) {
		currentBrick = brick;
		currentFormula = formula;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			restoreInstance = savedInstanceState.getBoolean("restoreInstance");
		}
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_FRAME, R.style.dialog_fullscreen);

	}

	@Override
	public void onSaveInstanceState(Bundle saveInstanceState) {
		saveInstanceState.putBoolean("restoreInstance", true);
		super.onSaveInstanceState(saveInstanceState);
	}

	public static void showDialog(View view, Brick brick, Formula formula) {
		SherlockFragmentActivity activity = null;
		if (SherlockFragmentActivity.class.isAssignableFrom(view.getContext().getClass())) { //this view is from any SherlockFragmentActivity
			activity = (SherlockFragmentActivity) view.getContext();
		} else {
			activity = (SherlockFragmentActivity) ((ContextWrapper) view.getContext()).getBaseContext(); //this view is from within this dialog, happens when you click an edittext within the editor
		}

		FormulaEditorDialog formulaEditorDialog = null;
		if (activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_FORMULA_EDITOR) == null) {
			formulaEditorDialog = new FormulaEditorDialog(brick, formula);
			formulaEditorDialog.show(activity.getSupportFragmentManager(), FRAGMENT_TAG_FORMULA_EDITOR);
		} else {
			formulaEditorDialog = (FormulaEditorDialog) activity.getSupportFragmentManager().findFragmentByTag(
					FRAGMENT_TAG_FORMULA_EDITOR);
			formulaEditorDialog.setInputFormula(formula);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View dialogView = inflater.inflate(R.layout.dialog_formula_editor, container);
		context = dialogView.getContext();
		brickSpace = (LinearLayout) dialogView.findViewById(R.id.formula_editor_brick_space);
		if (brickSpace != null) {
			brickView = currentBrick.getView(context, 0, null);
			brickSpace.addView(brickView);
		}

		okButton = (Button) dialogView.findViewById(R.id.formula_editor_ok_button);
		okButton.setOnClickListener(this);

		undoButton = (Button) dialogView.findViewById(R.id.formula_editor_undo_button);
		undoButton.setOnClickListener(this);

		redoButton = (Button) dialogView.findViewById(R.id.formula_editor_redo_button);
		redoButton.setOnClickListener(this);

		formulaEditorEditText = (FormulaEditorEditText) dialogView.findViewById(R.id.formula_editor_edit_field);
		if (brickSpace != null) {
			brickSpace.measure(0, 0);
		}
		catKeyboardView = (CatKeyboardView) dialogView.findViewById(R.id.keyboardcat);
		catKeyboardView.setEditText(formulaEditorEditText);
		//catKeyboardView.setCurrentBrick(currentBrick);

		makeRedoButtonClickable(false);
		makeUndoButtonClickable(false);
		makeOkButtonBackButton();

		if (brickSpace != null) {
			formulaEditorEditText.init(this, brickSpace.getMeasuredHeight(), catKeyboardView);
		} else {
			formulaEditorEditText.init(this, 0, catKeyboardView);
		}

		setInputFormula(currentFormula);
		getDialog().setOnKeyListener(this);
		return dialogView;
	}

	public void setInputFormula(Formula newFormula) {

		int orientation = getResources().getConfiguration().orientation;

		if (restoreInstance) { //after orientation switch
			restoreInstance = false;
			if (!formulaEditorEditText.restoreFieldFromPreviousHistory()) { //history is only deleted when editor is shut down by  user!
				formulaEditorEditText.enterNewFormula(newFormula.toString()); // this happens when onSaveInstanceState() is being called but not by orientation change (e.g.user turns off screen)
			}
			refreshFormulaPreviewString(formulaEditorEditText.getText().toString());

			currentFormula.highlightTextField(brickView,
					getResources().getDrawable(R.drawable.edit_text_formula_editor_selected), orientation);

		} else if (newFormula == currentFormula) {

			if (!formulaEditorEditText.hasChanges()) {
				currentFormula.removeTextFieldHighlighting(brickView, orientation);
				formulaEditorEditText.enterNewFormula(currentFormula.toString());
				currentFormula.highlightTextField(brickView,
						getResources().getDrawable(R.drawable.edit_text_formula_editor_selected), orientation);
			} else {
				formulaEditorEditText.quickSelect();
			}
			refreshFormulaPreviewString(formulaEditorEditText.getText().toString());
		} else {
			if (!formulaEditorEditText.hasChanges()) {
				if (currentFormula != null) {
					currentFormula.refreshTextField(brickView);
				}
				formulaEditorEditText.endEdit();
				currentFormula.removeTextFieldHighlighting(brickView, orientation);
				currentFormula = newFormula;
				currentFormula.highlightTextField(brickView,
						getResources().getDrawable(R.drawable.edit_text_formula_editor_selected), orientation);
				makeOkButtonBackButton();
				formulaEditorEditText.enterNewFormula(newFormula.toString());
			} else {
				showToast(R.string.formula_editor_save_first);
			}

		}

	}

	private int parseFormula(String formulaToParse) {
		CalcGrammarParser parser = CalcGrammarParser.getFormulaParser(formulaToParse);
		FormulaElement parserFormulaElement = parser.parseFormula();
		int parserResult = parser.getErrorCharacterPosition();

		if (parserResult == PARSER_OK) {
			currentFormula.setRoot(parserFormulaElement);
		}

		return parserResult;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.formula_editor_ok_button:
				if (buttonIsBackButton) {
					onUserDismiss();
				} else {
					String formulaToParse = formulaEditorEditText.getText().toString();
					int err = parseFormula(formulaToParse);
					switch (err) {
						case PARSER_OK:
							if (brickSpace != null) {
								currentFormula.refreshTextField(brickView);
							}
							formulaEditorEditText.formulaSaved();
							showToast(R.string.formula_editor_changes_saved);
							break;
						case PARSER_STACK_OVERFLOW:
							showToast(R.string.formula_editor_parse_fail_formula_too_long);
							break;
						default:
							showToast(R.string.formula_editor_parse_fail);
							formulaEditorEditText.highlightParseError(err);
					}
				}
				break;

			case R.id.formula_editor_undo_button:
				makeUndoButtonClickable(formulaEditorEditText.undo());
				makeRedoButtonClickable(true);
				if (buttonIsBackButton) {
					makeOkButtonSaveButton();
				}
				break;

			case R.id.formula_editor_redo_button:
				makeRedoButtonClickable(formulaEditorEditText.redo());
				makeUndoButtonClickable(true);
				if (buttonIsBackButton) {
					makeOkButtonSaveButton();
				}
				break;

			default:
				break;

		}
	}

	public void showToast(int ressourceId) {
		Toast userInfo = Toast.makeText(context, ressourceId, Toast.LENGTH_SHORT);
		userInfo.setGravity(Gravity.TOP, 0, 10);
		userInfo.show();
	}

	private void onUserDismiss() { //dont override onDismiss, this must not be called on orientation change
		formulaEditorEditText.endEdit();
		currentFormula = null;
		currentBrick = null;
		dismiss();
	}

	public void makeUndoButtonClickable(boolean clickable) {
		undoButton.setClickable(clickable);
	}

	public void makeRedoButtonClickable(boolean clickable) {
		redoButton.setClickable(clickable);
	}

	public void makeOkButtonSaveButton() {
		okButton.setText(R.string.formula_editor_button_save);
		buttonIsBackButton = false;
	}

	public void makeOkButtonBackButton() {
		okButton.setText(R.string.formula_editor_button_return);
		buttonIsBackButton = true;
	}

	@Override
	public boolean onKey(DialogInterface di, int keyCode, KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					if (formulaEditorEditText.hasChanges()) {
						if (System.currentTimeMillis() <= confirmBack + 2000) {
							showToast(R.string.formula_editor_changes_discarded);
							onUserDismiss();
						} else {
							showToast(R.string.formula_editor_confirm_discard);
							confirmBack = System.currentTimeMillis();
						}
					} else {
						onUserDismiss();
					}
					break;
				default:
					break;

			}
		} else if (event.getAction() == KeyEvent.ACTION_DOWN) {

		}
		return true;
	}

	public void refreshFormulaPreviewString(String formulaString) {
		currentFormula.refreshTextField(brickView, formulaEditorEditText.getText().toString());

	}

}
