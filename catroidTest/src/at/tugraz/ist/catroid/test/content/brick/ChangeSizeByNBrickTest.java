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
package at.tugraz.ist.catroid.test.content.brick;

import java.io.File;

import android.content.Context;
import android.test.InstrumentationTestCase;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.content.Project;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.bricks.ChangeSizeByNBrick;
import at.tugraz.ist.catroid.test.R;
import at.tugraz.ist.catroid.test.utils.TestUtils;
import at.tugraz.ist.catroid.utils.Utils;

public class ChangeSizeByNBrickTest extends InstrumentationTestCase {

	private Context context;
	private float positiveSize = 20;
	//	private float negativeSize = -30;

	private static final int IMAGE_FILE_ID = R.raw.icon;

	private File testImage;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = getInstrumentation().getTargetContext();

		Utils.updateScreenWidthAndHeight(context);

		Project project = new Project(context, TestUtils.TEST_PROJECT_NAME1);
		assertTrue("cannot save project", TestUtils.saveProjectAndWait(this, project));
		ProjectManager.getInstance().setProject(project);

		testImage = TestUtils.saveFileToProject(TestUtils.TEST_PROJECT_NAME1, "testImage.png", IMAGE_FILE_ID, context,
				TestUtils.TYPE_IMAGE_FILE);
	}

	@Override
	protected void tearDown() throws Exception {
		if (testImage != null && testImage.exists()) {
			testImage.delete();
		}
		TestUtils.deleteTestProjects();
		super.tearDown();
	}

	public void testSize() {
		Sprite sprite = new Sprite("testSprite");
		assertEquals("Unexpected initial sprite size value", 1f, sprite.costume.getSize());

		float initialSize = sprite.costume.getSize();

		ChangeSizeByNBrick brick = new ChangeSizeByNBrick(sprite, positiveSize);
		brick.execute();
		assertEquals("Incorrect sprite size value after ChangeSizeByNBrick executed", initialSize
				+ (positiveSize / 100), sprite.costume.getSize());
	}

	public void testNullSprite() {
		ChangeSizeByNBrick brick = new ChangeSizeByNBrick(null, positiveSize);

		try {
			brick.execute();
			fail("Execution of ChangeSizeByNBrick with null Sprite did not cause a NullPointerException to be thrown");
		} catch (NullPointerException e) {
			// expected behavior
		}
	}
}
