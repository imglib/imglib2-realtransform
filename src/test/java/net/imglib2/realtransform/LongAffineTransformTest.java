/**
 *
 */
package net.imglib2.realtransform;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

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
			System.out.println( "Matrix is singular, not important for this test." );
		}
	}

	public void testSeparateApply(
			final int i,
			final int j )
	{
//		System.out.println( "values: " + Arrays.toString( testsRealDouble[ i ] ) );

		final AffineTransform tAffine = new AffineTransform( testsRealDouble[ i ] );

//		System.out.println( "affine: " + Arrays.toString( tAffine.getRowPackedCopy() ) );

		final double[] testVector = testRealDoubleVectors[ i ][ j ];
		final double[] testVectorCopy1 = testVector.clone();
		final double[] testVectorCopy2 = testVector.clone();

//		System.out.println( "affine test: " + Arrays.toString( testVectorCopy1 ) );
//		System.out.println( "round test:  " + Arrays.toString( testVectorCopy2 ) );

		tAffine.apply( testVectorCopy1, testVectorCopy1 );

		final Pair< LongAffineTransform, AffineTransform > pair = LongAffineTransform.splitAffineTransform( tAffine );

		System.out.println( "round:  " + Arrays.toString( pair.getA().toAffineTransform().getRowPackedCopy() ) );
		System.out.println( "rest:   " + Arrays.toString( pair.getB().getRowPackedCopy() ) );


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

	@Test
	public void testCollinear()
	{
		for ( int i = 0; i < collinearVectors.length; ++i )
		{
			assertTrue( LongAffineTransform.collinear( collinearVectors[ i ][ 0 ], collinearVectors[ i ][ 1 ] ) );
		}

		for ( int i = 0; i < nonCollinearVectors.length; ++i )
		{
			assertFalse( LongAffineTransform.collinear( nonCollinearVectors[ i ][ 0 ], nonCollinearVectors[ i ][ 1 ] ) );
		}
	}

}
