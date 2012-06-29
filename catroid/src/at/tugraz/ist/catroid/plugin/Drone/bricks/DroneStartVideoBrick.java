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
package at.tugraz.ist.catroid.plugin.Drone.bricks;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.plugin.Drone.other.DroneVideoCostume;
import at.tugraz.ist.catroid.stage.StageActivity;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class DroneStartVideoBrick implements Brick {

	private Sprite sprite;

	@XStreamOmitField
	private transient View view;

	public DroneStartVideoBrick(Sprite sprite) {
		this.sprite = sprite;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		//DroneHandler.getInstance().getDrone().startVideo();
		try {
			StageActivity.stageListener.removeActor(sprite.costume);
			sprite.costume.disposeTextures();
			DroneVideoCostume costume = new DroneVideoCostume(sprite);
			sprite.costume = costume;
			StageActivity.stageListener.addActor(sprite.costume);
		} catch (NullPointerException e) {
			// TODO: handle exception
		}
	}

	@Override
	public Sprite getSprite() {
		return sprite;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter adapter) {
		if (view == null) {
			view = View.inflate(context, R.layout.toolbox_brick_drone_start_video, null);
		}
		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		return View.inflate(context, R.layout.toolbox_brick_drone_start_video, null);
	}

	@Override
	public Brick clone() {
		return new DroneStartVideoBrick(getSprite());
	}

	@Override
	public int getRequiredResources() {
		return WIFI_DRONE;
	}
}
