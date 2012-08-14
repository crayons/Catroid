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
import android.media.MediaPlayer;
import android.test.InstrumentationTestCase;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.common.SoundInfo;
import at.tugraz.ist.catroid.content.Project;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.bricks.PlaySoundBrick;
import at.tugraz.ist.catroid.content.bricks.StopAllSoundsBrick;
import at.tugraz.ist.catroid.io.SoundManager;
import at.tugraz.ist.catroid.io.StorageHandler;
import at.tugraz.ist.catroid.test.R;
import at.tugraz.ist.catroid.test.utils.TestUtils;
import at.tugraz.ist.catroid.utils.Utils;

public class StopAllSoundsBrickTest extends InstrumentationTestCase {
	private static final int SOUND_FILE_ID = R.raw.longtestsound;
	private static final String TEST_PROJECT_NAME = TestUtils.TEST_PROJECT_NAME1;
	private static final String SOUND_FILE_NAME = "LongTestSound";

	private Context context;
	private File soundFile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = getInstrumentation().getTargetContext();

		Utils.updateScreenWidthAndHeight(context);

		Project project = new Project(context, TEST_PROJECT_NAME);
		assertTrue("cannot save project", StorageHandler.getInstance().saveProjectSynchronously(project));
		ProjectManager.getInstance().setProject(project);

		soundFile = TestUtils.saveFileToProject(TEST_PROJECT_NAME, SOUND_FILE_NAME, SOUND_FILE_ID, getInstrumentation()
				.getContext(), TestUtils.TYPE_SOUND_FILE);
		assertTrue("Cannot read sound file", soundFile.canRead());
	}

	@Override
	protected void tearDown() throws Exception {
		if (soundFile != null && soundFile.exists()) {
			soundFile.delete();
		}
		SoundManager.getInstance().clear();
		TestUtils.deleteTestProjects();
		super.tearDown();
	}

	public void testStopOneSound() {
		final String soundFilePath = soundFile.getAbsolutePath();
		assertNotNull("Could not open test sound file", soundFilePath);
		assertTrue("Could not open test sound file", soundFilePath.length() > 0);

		MediaPlayer mediaPlayer = SoundManager.getInstance().getMediaPlayer();

		Sprite testSprite = new Sprite("1");
		PlaySoundBrick playSoundBrick = new PlaySoundBrick(testSprite);
		StopAllSoundsBrick stopAllSoundsBrick = new StopAllSoundsBrick(new Sprite("1"));
		SoundInfo soundInfo = createSoundInfo(soundFile);
		playSoundBrick.setSoundInfo(soundInfo);
		testSprite.getSoundList().add(soundInfo);
		playSoundBrick.execute();
		assertTrue("MediaPlayer is not playing", mediaPlayer.isPlaying());
		stopAllSoundsBrick.execute();
		assertFalse("MediaPlayer is still playing", mediaPlayer.isPlaying());

	}

	public void testStopSimultaneousPlayingSounds() throws InterruptedException {
		final MediaPlayer mediaPlayer1 = SoundManager.getInstance().getMediaPlayer();
		final MediaPlayer mediaPlayer2 = SoundManager.getInstance().getMediaPlayer();

		Thread threadSound1 = new Thread(new Runnable() {
			public void run() {
				Sprite testSprite = new Sprite("8");
				PlaySoundBrick playSoundBrick = new PlaySoundBrick(testSprite);

				SoundInfo soundInfo = createSoundInfo(soundFile);
				playSoundBrick.setSoundInfo(soundInfo);
				testSprite.getSoundList().add(soundInfo);
				playSoundBrick.execute();
			}
		});

		Thread threadSound2 = new Thread(new Runnable() {
			public void run() {
				Sprite testSprite = new Sprite("9");
				PlaySoundBrick playSoundBrick = new PlaySoundBrick(testSprite);

				SoundInfo soundInfo = createSoundInfo(soundFile);
				playSoundBrick.setSoundInfo(soundInfo);
				testSprite.getSoundList().add(soundInfo);
				playSoundBrick.execute();
			}
		});

		StopAllSoundsBrick testBrick1 = new StopAllSoundsBrick(new Sprite("10"));
		threadSound1.start();
		threadSound2.start();
		Thread.sleep(200);
		assertTrue("mediaPlayer1 is not playing", mediaPlayer1.isPlaying());
		assertTrue("mediaPlayer2 is not playing", mediaPlayer2.isPlaying());
		testBrick1.execute();
		assertFalse("mediaPlayer1 is not stopped", mediaPlayer1.isPlaying());
		assertFalse("mediaPlayer2 is not stopped", mediaPlayer2.isPlaying());
		Thread.sleep(1000);
	}

	private static SoundInfo createSoundInfo(File file) {
		SoundInfo soundInfo = new SoundInfo();
		soundInfo.setSoundFileName(file.getName());
		soundInfo.setTitle(file.getName() + "Title");
		return soundInfo;
	}
}
