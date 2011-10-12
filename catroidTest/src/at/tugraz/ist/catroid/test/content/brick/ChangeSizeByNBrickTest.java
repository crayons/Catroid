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

import android.test.InstrumentationTestCase;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.common.Consts;
import at.tugraz.ist.catroid.common.CostumeData;
import at.tugraz.ist.catroid.common.Values;
import at.tugraz.ist.catroid.content.Project;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.bricks.ChangeSizeByNBrick;
import at.tugraz.ist.catroid.io.StorageHandler;
import at.tugraz.ist.catroid.test.R;
import at.tugraz.ist.catroid.test.utils.TestUtils;
import at.tugraz.ist.catroid.utils.UtilFile;

public class ChangeSizeByNBrickTest extends InstrumentationTestCase {

	private double positiveSize = 20;
	private double negativeSize = -30;

	private static final int IMAGE_FILE_ID = R.raw.icon;

	private File testImage;
	private final String projectName = "testProject";
	private CostumeData costumeData;

	@Override
	protected void setUp() throws Exception {

		File projectFile = new File(Consts.DEFAULT_ROOT + "/" + projectName);

		if (projectFile.exists()) {
			UtilFile.deleteDirectory(projectFile);
		}

		Project project = new Project(getInstrumentation().getTargetContext(), projectName);
		StorageHandler.getInstance().saveProject(project);
		ProjectManager.getInstance().setProject(project);

		testImage = TestUtils.saveFileToProject(this.projectName, "testImage.png", IMAGE_FILE_ID, getInstrumentation()
				.getContext(), TestUtils.TYPE_IMAGE_FILE);

		costumeData = new CostumeData();
		costumeData.setCostumeFilename(testImage.getName());
		costumeData.setCostumeName("name");
	}

	@Override
	protected void tearDown() throws Exception {
		File projectFile = new File(Consts.DEFAULT_ROOT + "/" + projectName);

		if (projectFile.exists()) {
			UtilFile.deleteDirectory(projectFile);
		}
		if (testImage != null && testImage.exists()) {
			testImage.delete();
		}
	}

	public void testSize() {
		Sprite sprite = new Sprite("testSprite");
		assertEquals("Unexpected initial sprite size value", 100.0, sprite.getSize());

		double initialSize = sprite.getSize();

		ChangeSizeByNBrick brick = new ChangeSizeByNBrick(sprite, positiveSize);
		brick.execute();
		assertEquals("Incorrect sprite size value after ChangeSizeByNBrick executed", initialSize + positiveSize,
				sprite.getSize());

		initialSize = sprite.getSize();
		brick = new ChangeSizeByNBrick(sprite, negativeSize);
		brick.execute();
		assertEquals("Incorrect sprite size value after ChangeSizeByNBrick executed", initialSize + negativeSize,
				sprite.getSize());
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

	public void testCostumeToBig() {
		Values.SCREEN_HEIGHT = 800;
		Values.SCREEN_WIDTH = 480;

		Sprite sprite = new Sprite("testSprite");
		sprite.getCostume().changeImagePath(costumeData);

		ChangeSizeByNBrick brick = new ChangeSizeByNBrick(sprite, Double.MAX_VALUE);

		brick.execute();

		int newWidth = sprite.getCostume().getImageWidth();
		int newHeight = sprite.getCostume().getImageHeight();

		assertTrue("Costume has a wrong size after setting it!", newWidth == Consts.MAX_COSTUME_WIDTH
				|| newHeight == Consts.MAX_COSTUME_HEIGHT);
	}

	public void testCostumeToSmall() {
		Values.SCREEN_HEIGHT = 800;
		Values.SCREEN_WIDTH = 480;

		Sprite sprite = new Sprite("testSprite");
		sprite.getCostume().changeImagePath(costumeData);

		ChangeSizeByNBrick brick = new ChangeSizeByNBrick(sprite, -Double.MAX_VALUE);

		brick.execute();

		int newWidth = sprite.getCostume().getImageWidth();
		int newHeight = sprite.getCostume().getImageHeight();

		assertTrue("Costume has a wrong size after setting it!", newWidth == 1 || newHeight == 1);
	}
}
