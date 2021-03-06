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
package at.tugraz.ist.catroid.content.bricks;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.ui.ScriptTabActivity;
import at.tugraz.ist.catroid.ui.dialogs.BrickTextDialog;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class SetXBrick implements Brick, OnClickListener {
	private static final long serialVersionUID = 1L;
	private int xPosition;
	private Sprite sprite;

	@XStreamOmitField
	private transient View view;

	public SetXBrick(Sprite sprite, int xPosition) {
		this.sprite = sprite;
		this.xPosition = xPosition;
	}

	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	public void execute() {
		sprite.costume.aquireXYWidthHeightLock();
		sprite.costume.setXPosition(xPosition);
		sprite.costume.releaseXYWidthHeightLock();
	}

	public Sprite getSprite() {
		return this.sprite;
	}

	public View getView(Context context, int brickId, BaseAdapter adapter) {

		view = View.inflate(context, R.layout.brick_set_x, null);

		TextView textX = (TextView) view.findViewById(R.id.brick_set_x_text_view);
		EditText editX = (EditText) view.findViewById(R.id.brick_set_x_edit_text);
		editX.setText(String.valueOf(xPosition));
		textX.setVisibility(View.GONE);
		editX.setVisibility(View.VISIBLE);
		editX.setOnClickListener(this);

		return view;
	}

	public View getPrototypeView(Context context) {
		return View.inflate(context, R.layout.brick_set_x, null);
	}

	@Override
	public Brick clone() {
		return new SetXBrick(getSprite(), xPosition);
	}

	public void onClick(View view) {
		ScriptTabActivity activity = (ScriptTabActivity) view.getContext();
		
		BrickTextDialog editDialog = new BrickTextDialog() {
			@Override
			protected void initialize() {
				input.setText(String.valueOf(xPosition));
				input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
						| InputType.TYPE_NUMBER_FLAG_SIGNED);
				input.setSelectAllOnFocus(true);
			}
			
			@Override
			protected boolean handleOkButton() {
				try {
					xPosition = Integer.parseInt(input.getText().toString());
				} catch (NumberFormatException exception) {
					Toast.makeText(getActivity(), R.string.error_no_number_entered, Toast.LENGTH_SHORT).show();
				}
				
				return true;
			}
		};
		
		editDialog.show(activity.getSupportFragmentManager(), "dialog_set_x_brick");
	}
}
