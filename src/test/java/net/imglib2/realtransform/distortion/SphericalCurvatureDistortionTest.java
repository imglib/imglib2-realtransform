package net.imglib2.realtransform.distortion;

import org.junit.Test;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.IterableInverseTests;
import net.imglib2.util.Intervals;

public class SphericalCurvatureDistortionTest
{
	private static final double EPS = 1E-6;

	@Test
	public void inverseTest() throws Exception
	{
		final SphericalCurvatureDistortionTransform tform = new SphericalCurvatureDistortionTransform( 3, 2, 1000.0 );
		final FinalRealInterval interval = Intervals.createMinMaxReal( -1.0, -1.0, -1.0, 2.01, 2.01, 2.01 );
		final double[] step = new double[] { 0.1, 0.1, 0.1 };

		IterableInverseTests.inverseTransformTestHelper( tform, interval, step, EPS );
	}

}