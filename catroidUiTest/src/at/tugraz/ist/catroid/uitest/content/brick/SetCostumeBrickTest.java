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
package at.tugraz.ist.catroid.uitest.content.brick;

import java.util.List;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.content.bricks.SetCostumeBrick;
import at.tugraz.ist.catroid.ui.ScriptActivity;
import at.tugraz.ist.catroid.uitest.mockups.MockGalleryActivity;
import at.tugraz.ist.catroid.uitest.util.Utils;

import com.jayway.android.robotium.solo.Solo;

public class SetCostumeBrickTest extends ActivityInstrumentationTestCase2<ScriptActivity> {
	private Solo solo;

	/*
	 * public class MockGalleryActivity extends Activity {
	 * private static final String RESOURCE_LOCATION = "res/drawable/catroid_sunglasses";
	 * 
	 * @Override
	 * protected void onCreate(android.os.Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState);
	 * finish();
	 * }
	 * 
	 * @Override
	 * protected void onDestroy() {
	 * Intent resultIntent = new Intent();
	 * File resourceFile = new File(RESOURCE_LOCATION);
	 * if (!resourceFile.exists() || !resourceFile.canRead())
	 * throw new RuntimeException("Could not open resource file: " + resourceFile.getAbsolutePath());
	 * resultIntent.setData(Uri.fromFile(resourceFile));
	 * 
	 * setResult(RESULT_OK, resultIntent);
	 * super.onDestroy();
	 * }
	 * }
	 */

	public SetCostumeBrickTest() {
		super("at.tugraz.ist.catroid", ScriptActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		List<Brick> brickList = Utils.createTestProject();
		solo = new Solo(getInstrumentation(), getActivity());

		// Replace onClickListener of Brick in order to use MockGalleryActivity
		int index = -1;
		for (int i = 0; i < brickList.size(); i++) {
			if (brickList.get(i).getClass() == SetCostumeBrick.class) {
				index = i;
			}
		}
		final int setCostumeBrickIndex = index;
		ImageView imageView = (ImageView) solo.getView(R.id.costume_image_view);

		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getInstrumentation().getContext(), MockGalleryActivity.class);
				getActivity().startActivityForResult(intent, setCostumeBrickIndex);
			}
		};
		imageView.setOnClickListener(listener);
	}

	@Override
	public void tearDown() throws Exception {
		try {
			solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		getActivity().finish();
		super.tearDown();
	}

	public void testChangeCostume() throws InterruptedException {
		Thread.sleep(1000);
		solo.clickOnView(solo.getView(R.id.costume_image_view));
		Thread.sleep(5000);
		assertTrue(true);
	}
}
