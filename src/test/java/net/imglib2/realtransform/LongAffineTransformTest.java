/**
 *
 */
package net.imglib2.realtransform;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import Jama.Matrix;
import net.imglib2.util.Pair;

/**
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 *
 */
public class LongAffineTransformTest
{
	private static int maxValue = 4;

	private static final long[][] tests = new long[ 100 ][];

	private static final double[][] testsDouble = new double[ tests.length ][];

	private static final double[][] testsRealDouble = new double[ tests.length ][];

	private static final long[][][] testVectors = new long[ tests.length ][ 5 ][];

	private static final double[][][] testDoubleVectors = new double[ tests.length ][ 5 ][];

	private static final double[][][] testRealDoubleVectors = new double[ tests.length ][ 5 ][];

	private static final double[][][] collinearVectors = {
			{ { 1, 2, 3, 4, 5 }, { 2, 4, 6, 8, 10 } },
			{ { 0, 0, 1 }, { 0, 0, 2 } },
			{ { 0, 0, 0, 0 }, { 1, 2, 3, 4 } }
	};

	private static final double[][][] nonCollinearVectors = {
			{ { 1, 2, 2, 4, 5 }, { 2, 4, 6, 8, 10 } },
			{ { 0, 0, 1 }, { 0, 1, 2 } },
			{ { 0, 0, 0, 1 }, { 1, 2, 3, 4 } }
	};

	private static Random rnd = new Random( 0 );

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		for ( int i = 0; i < tests.length; ++i )
		{
			final int n = rnd.nextInt( 5 ) + 1;
			final long[] test = new long[ ( n + 1 ) * n ];
			final double[] testDouble = new double[ test.length ];
			final double[] testRealDouble = new double[ test.length ];
			for ( int j = 0; j < test.length; ++j )
			{
				test[ j ] = rnd.nextInt( maxValue ) - maxValue / 2;
				testDouble[ j ] = test[ j ];
				testRealDouble[ j ] = rnd.nextDouble() * maxValue - maxValue / 2;
			}

			tests[ i ] = test;
			testsDouble[ i ] = testDouble;
			testsRealDouble[ i ] = testRealDouble;

			for ( int j = 0; j < testVectors[ i ].length; ++j )
			{
				final long[] testVector = new long[ n ];
				final double[] testDoubleVector = new double[ n ];
				final double[] testRealDoubleVector = new double[ n ];
				for ( int k = 0; k < n; ++k )
				{
					testVector[ k ] = rnd.nextInt( maxValue ) - maxValue / 2;
					testDoubleVector[ k ] = testVector[ k ];
					testRealDoubleVector[ k ] = rnd.nextDouble() * maxValue - maxValue / 2;
				}

				testVectors[ i ][ j ] = testVector;
				testDoubleVectors[ i ][ j ] = testDoubleVector;
				testRealDoubleVectors[ i ][ j ] = testRealDoubleVector;
			}
		}
	}

	public void testAffine( final int i )
	{
		final LongAffineTransform t = new LongAffineTransform( tests[ i ] );
		final AffineTransform tAffine = new AffineTransform( testsDouble[ i ] );

		assertArrayEquals( testsDouble[ i ], t.toAffineTransform().getRowPackedCopy(), 0.001 );
	}

	public void testApply(
			final int i,
			final int j )
	{
		try
		{
			final LongAffineTransform t = new LongAffineTransform( tests[ i ] );
			final AffineTransform tAffine = new AffineTransform( testsDouble[ i ] );

			final long[] testVector = testVectors[ i ][ j ];
			final long[] testVectorCopy = testVector.clone();

			final double[] testDoubleVector = testDoubleVectors[ i ][ j ];
			final double[] testDoubleVectorCopy = testDoubleVector.clone();

			t.apply( testVectorCopy, testVectorCopy );
			tAffine.apply( testDoubleVectorCopy, testDoubleVectorCopy );

			assertArrayEquals( testDoubleVectorCopy, Arrays.stream( testVectorCopy ).asDoubleStream().toArray(), 0.001 );
		}
		catch ( final RuntimeException e )
		{
//			System.out.println( "Matrix is singular, skipping for this test." );
		}
	}

	public void testSeparateApply(
			final int i,
			final int j )
	{
//		System.out.println( "values: " + Arrays.toString( testsRealDouble[ i ] ) );

		final AffineTransform tAffine = new AffineTransform( testsRealDouble[ i ] );

//		System.out.print( "original:" );
//		new Matrix( tAffine.getRowPackedCopy(), tAffine.numDimensions() + 1 ).transpose().print( 5, 2 );

//		System.out.println( "affine: " + Arrays.toString( tAffine.getRowPackedCopy() ) );

		final double[] testVector = testRealDoubleVectors[ i ][ j ];
		final double[] testVectorCopy1 = testVector.clone();
		final double[] testVectorCopy2 = testVector.clone();

//		System.out.println( "affine test: " + Arrays.toString( testVectorCopy1 ) );
//		System.out.println( "round test:  " + Arrays.toString( testVectorCopy2 ) );

		tAffine.apply( testVectorCopy1, testVectorCopy1 );

		final Pair< LongAffineTransform, AffineTransform > pair = LongAffineTransform.decomposeLongReal( tAffine );

//		System.out.print( "round:" );
//		new Matrix( pair.getA().toAffineTransform().getRowPackedCopy(), testVector.length + 1 ).transpose().print( 5, 2 );
//		System.out.print( "rest:" );
//		new Matrix( pair.getB().getRowPackedCopy(), testVector.length + 1 ).transpose().print( 5, 2 );


//		assertArrayEquals(
//				Arrays.stream( testsRealDouble[ i ] ).map( a -> Math.round( a ) ).toArray(),
//				pair.getA().toAffineTransform().getRowPackedCopy(),
//				0.001 );

		pair.getA().apply( testVectorCopy2, testVectorCopy2 );

//		System.out.println( "round apply:  " + Arrays.toString( testVectorCopy2 ) );

		pair.getB().apply( testVectorCopy2, testVectorCopy2 );

//		System.out.println( "affine apply: " + Arrays.toString( testVectorCopy1 ) );
//		System.out.println( "round and rest apply:  " + Arrays.toString( testVectorCopy2 ) );

		assertArrayEquals( testVectorCopy1, testVectorCopy2, 0.001 );
	}

	@Test
	public void testSingularMatrix()
	{
		final Matrix matrix = new Matrix(
				new double[] {
					1.12,  -0.13,   1.57,  -1.05,  -1.40,
					-0.64,   0.73,  -0.27,   0.61,   1.66,
					1.39,  -0.29,   0.98,   1.36,   1.48,
					0.90,  -1.98,   1.81,   0.57,  -1.32,
					1.29,   0.63,   1.48,  -1.99,  -0.76 }, 5 ).transpose();

//		matrix.print( 5, 2 );

		final Matrix matrixLong = new Matrix(
				new double[] {
					1,  0,  2,  -1,  -1,
					0,  1,  0,  1,   2,
					1,  0,  1,  1,   1,
					1,  -2, 2,  1,   -1,
					1,  1,  1,  -2,  -1 }, 5 ).transpose();

//		System.out.println( Arrays.toString( matrixLong.eig().getRealEigenvalues() ) );
//
//		System.out.println( "det " + matrixLong.det() );
//
//		System.out.println( "rank " + matrixLong.rank() );

		LongAffineTransform.fullRank( matrix, matrixLong );

//		System.out.println( Arrays.toString( matrixLong.eig().getRealEigenvalues() ) );
//
//		System.out.println( "det " + matrixLong.det() );
//
//		System.out.println( "rank " + matrixLong.rank() );

		matrixLong.transpose().inverse(); // would throw exception if still singular
	}



	@Test
	public void test()
	{
		for ( int i = 0; i < tests.length; ++i )
		{
			for ( int j = 0; j < testVectors[ i ].length; ++j )
			{
				testApply( i, j );
				testSeparateApply( i, j );
			}
		}
	}
}
