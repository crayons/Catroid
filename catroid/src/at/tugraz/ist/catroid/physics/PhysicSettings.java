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
package at.tugraz.ist.catroid.physics;

import com.badlogic.gdx.math.Vector2;

/**
 * @author robert
 * 
 */
public class PhysicSettings {

	public static class Render {
		public final static boolean RENDER_COLLISION_FRAMES = true;
		public final static boolean RENDER_BODIES = true;
		public final static boolean RENDER_JOINTS = false;
		public final static boolean RENDER_AABBs = false;
		public final static boolean RENDER_INACTIVE_BODIES = true;
	}

	public static class World {
		// Ratio of pixels to meters
		public final static float RATIO = 40.0f;
		public final static float TIMESTEP = 1.0f / 30.0f;
		public final static int VELOCITY_ITERATIONS = 10;
		public final static int POSITION_ITERATIONS = 10;

		public final static int DEAULT_MASS = 1;
		public final static Vector2 DEFAULT_GRAVITY = new Vector2(0, -10);
		public final static boolean IGNORE_SLEEPING_OBJECTS = false;

		public final static boolean SURROUNDING_BOX = true;
		public final static int SURROUNDING_BOX_FRAME_SIZE = 1;
	}
}
