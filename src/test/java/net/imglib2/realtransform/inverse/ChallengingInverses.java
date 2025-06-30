package net.imglib2.realtransform.inverse;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

import net.imglib2.realtransform.ThinplateSplineTransform;

public class ChallengingInverses
{
	final int nd = 2;

	double[][] srcPts = transpose( new double[][] {
		{64.93826968043624,136.5169914263442},
		{949.2718628215123,196.43904910366302},
		{481.4932190179267,94.95814497271988},
		{842.9585346843338,934.8334372564303},
		{1180.2617303195639,887.4756819953235},
		{1148.3677318784103,724.1397505845674},
		{403.87246158700066,1121.4631843388372},
		{282.79379326150405,659.0099372622876}});

	double[][] tgtPts = transpose(new double[][] {
		{1.2160852376668474,2.982585528555182},
		{12.643983273336763,4.749085819443516},
		{6.74658999452494,2.792347035690284},
		{10.537771388046828,14.179479680032008},
		{15.130672144356495,13.75823730297402},
		{14.790960549954892,11.652025417684083},
		{4.577866506453839,16.017301399163536},
		{3.544833825697882,9.943901208947302}});

	@Test
	public void testInv() {

		final ThinplateSplineTransform tps = new ThinplateSplineTransform( srcPts, tgtPts );
		final WrappedIterativeInvertibleRealTransform tf = new WrappedIterativeInvertibleRealTransform( tps );

		// Configure optimizer for this challenging case
		InverseRealTransformGradientDescent optimizer = tf.getOptimzer();
		optimizer.setTolerance( 0.001 );
		optimizer.setMaxIters( 2000 );
		optimizer.setBeta( 0.5 );

		int idx = 0;
		final double[] p0 = new double[] { srcPts[0][idx], srcPts[1][idx] };
		final double[] q0 = new double[] { tgtPts[0][idx], tgtPts[1][idx] };
		final double[] res = new double[nd];

		tf.applyInverse( res, q0 );
		assertArrayEquals(p0, res, 0.1);
	}

	private static double[][] transpose(double[][] matrix) {
	    if (matrix == null || matrix.length == 0) {
	        return new double[0][0];
	    }
	    
	    int rows = matrix.length;
	    int cols = matrix[0].length;
	    
	    // Create transposed matrix with swapped dimensions
	    double[][] transposed = new double[cols][rows];
	    
	    // Fill the transposed matrix
	    for (int i = 0; i < rows; i++) {
	        for (int j = 0; j < cols; j++) {
	            transposed[j][i] = matrix[i][j];
	        }
	    }
	    
	    return transposed;
	}
	
}
