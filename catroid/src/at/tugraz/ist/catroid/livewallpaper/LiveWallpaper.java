package at.tugraz.ist.catroid.livewallpaper;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import at.tugraz.ist.catroid.ProjectManager;
import at.tugraz.ist.catroid.content.Project;
import at.tugraz.ist.catroid.content.Script;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.StartScript;
import at.tugraz.ist.catroid.content.WhenScript;
import at.tugraz.ist.catroid.content.bricks.Brick;

public class LiveWallpaper extends WallpaperService {

	private ProjectManager projectManager;
	private Project currentProject;
	private List<Sprite> spriteList;

	@Override
	public Engine onCreateEngine() {

		projectManager = ProjectManager.getInstance();
		currentProject = projectManager.getCurrentProject();
		spriteList = currentProject.getSpriteList();

		return new CatWallEngine();
	}

	private class CatWallEngine extends Engine {
		private boolean mVisible = false;

		private Paint paint;
		private Script scriptToHandle;
		private Brick brickToHandle;

		private boolean startScript = false;
		private boolean tappedScript = false;

		private WallpaperCostume wallpaperCostume = WallpaperCostume.getInstance();

		private final Handler mHandler = new Handler();

		private final Runnable mUpdateDisplay = new Runnable() {
			public void run() {
				draw();
			}
		};

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if (visible) {
				startScript = true;
				tappedScript = false;
				handleScript();
				draw();
			} else {
				mHandler.removeCallbacks(mUpdateDisplay);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			draw();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
		}

		public void handleScript() {
			for (Sprite sprite : spriteList) {
				for (int i = 0; i < sprite.getNumberOfScripts(); i++) {
					scriptToHandle = sprite.getScript(i);
					Log.v("DEBUG", "script of sprite name:" + sprite.getScript(i).toString());
					for (int j = 0; j < scriptToHandle.getBrickList().size(); j++) {
						brickToHandle = scriptToHandle.getBrick(j);
						Log.v("DEBUG", "Brick Name:" + scriptToHandle.getBrick(j).toString());
						if (startScript && scriptToHandle instanceof StartScript) {
							brickToHandle.executeLiveWallpaper();
							draw();
						} else if (tappedScript && scriptToHandle instanceof WhenScript) {
							brickToHandle.executeLiveWallpaper();
							draw();
						}
					}
				}
			}

			resetFlag();
		}

		public void resetFlag() {
			if (startScript) {
				startScript = false;
			}

			//			if (tappedScript) {
			//				tappedScript = false;
			//			}
		}

		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;

			try {
				c = holder.lockCanvas();
				paint = new Paint();
				if (c != null) {

					if (wallpaperCostume.getBackground() != null) {
						c.drawBitmap(wallpaperCostume.getBackground(), 0, 0, paint);
						if (wallpaperCostume.getCostume() != null && !wallpaperCostume.isCostumeHidden()) {

							c.drawBitmap(wallpaperCostume.getCostume(), wallpaperCostume.getTop(),
									wallpaperCostume.getLeft(), paint);
						}
					}

				}

			} finally {
				if (c != null) {
					holder.unlockCanvasAndPost(c);
				}
			}
			mHandler.removeCallbacks(mUpdateDisplay);
			if (mVisible) {
				mHandler.postDelayed(mUpdateDisplay, 100);
			}
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (!tappedScript && wallpaperCostume.touchedInsideTheCostume(event.getX(), event.getY())) {
					tappedScript = true;
					handleScript();
				}
			}

		}
	}

}
