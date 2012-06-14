package at.tugraz.ist.catroid.test.content.brick;

import android.test.AndroidTestCase;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.content.bricks.Brick;
import at.tugraz.ist.catroid.content.bricks.SetVelocityBrick;
import at.tugraz.ist.catroid.physics.PhysicWorld;

import com.badlogic.gdx.math.Vector2;

public class SetVelocityBrickTest extends AndroidTestCase {
	private float xValue = 3.50f;
	private float yValue = 5.50f;
	//private Vector2 velocity;
	private PhysicWorld physicWorld;
	private Sprite sprite;
	private SetVelocityBrick setVelocityBrick;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sprite = new Sprite("testSprite");
		physicWorld = new PhysicWorldMock();
		setVelocityBrick = new SetVelocityBrick(physicWorld, sprite, xValue, yValue);
		//velocity = new Vector2(xValue, yValue);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		sprite = null;
		physicWorld = null;
		setVelocityBrick = null;
	}

	public void testRequiredResources() {
		assertEquals(setVelocityBrick.getRequiredResources(), Brick.NO_RESOURCES);
	}

	public void testGetSprite() {
		assertEquals(setVelocityBrick.getSprite(), sprite);
	}

	public void testClone() {
		Brick clone = setVelocityBrick.clone();
		assertEquals(setVelocityBrick.getSprite(), clone.getSprite());
		assertEquals(setVelocityBrick.getRequiredResources(), clone.getRequiredResources());
	}

	public void testExecution() {
		assertFalse(((PhysicWorldMock) physicWorld).isExecuted());
		setVelocityBrick.execute();
		assertTrue(((PhysicWorldMock) physicWorld).isExecuted());
	}

	public void testNullSprite() {
		setVelocityBrick = new SetVelocityBrick(null, sprite, xValue, yValue);
		try {
			setVelocityBrick.execute();
			fail("Execution of ChangeXByBrick with null Sprite did not cause a " + "NullPointerException to be thrown");
		} catch (NullPointerException expected) {
			// expected behavior
		}
	}

	@SuppressWarnings("serial")
	class PhysicWorldMock extends PhysicWorld {

		public boolean executed;

		public PhysicWorldMock() {
			executed = false;
		}

		public boolean isExecuted() {
			return executed;
		}

		@Override
		public void setVelocity(Sprite sprite, Vector2 v) {
			executed = true;
		}

	}
}
