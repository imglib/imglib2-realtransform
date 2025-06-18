package net.imglib2.realtransform;

import org.junit.Test;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.distortion.SphericalCurvatureZDistortion;
import net.imglib2.util.Intervals;

public class SphericalCurvatureZDistortionTest
{

	private static final double EPS = 1E-3;

	@Test
	public void inverseTest() throws Exception
	{
		final SphericalCurvatureZDistortion tform = new SphericalCurvatureZDistortion( 3, 2, 10000.0 );
		final FinalRealInterval interval = Intervals.createMinMaxReal( -1.0, -1.0, -1.0, 2.01, 2.01, 2.01 );
		final double[] step = new double[] { 0.1, 0.1, 0.1 };

		IterableInverseTests.inverseTransformTestHelper( tform, interval, step, EPS );
	}

}