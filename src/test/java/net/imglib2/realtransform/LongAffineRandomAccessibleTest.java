/**
 *
 */
package net.imglib2.realtransform;

import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 *
 */
public class LongAffineRandomAccessibleTest
{
	private static int maxValue = 20;

	private static final long[][] tests = new long[ 100 ][];

	private static final double[][] testsDouble = new double[ tests.length ][];

	private static final byte[] bytes = new byte[ 51 * 28 * 16 ];

	private static Random rnd = new Random( 0 );

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		rnd.nextBytes( bytes );

		for ( int i = 0; i < tests.length; ++i )
		{
//			final int n = rnd.nextInt( 5 ) + 1;
			final int n = 3;
			final long[] test = new long[ ( n + 1 ) * n ];
			final double[] testDouble = new double[ test.length ];
			for ( int j = 0; j < test.length; ++j )
			{
				test[ j ] = rnd.nextInt( maxValue ) - maxValue / 2;
				testDouble[ j ] = test[ j ];
			}

			tests[ i ] = test;
			testsDouble[ i ] = testDouble;
		}


	}

	@Test
	public void test()
	{
		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( bytes, 11, 15, 8 );
		final ExtendedRandomAccessibleInterval< UnsignedByteType, ArrayImg< UnsignedByteType, ByteArray > > extended = Views.extendMirrorSingle( img );
		final RealRandomAccessible< UnsignedByteType > interpolant = Views.interpolate( extended, new NearestNeighborInterpolatorFactory<>() );

		for ( int i = 0; i < tests.length; ++i )
		{
			final AffineTransform affine = new AffineTransform( testsDouble[ i ] );
			final LongAffineTransform longAffine = new LongAffineTransform( tests[ i ] );
			final LongAffineTransform longAffineInferred = LongAffineTransform.decomposeLongReal( affine ).getA();

			final IntervalView< UnsignedByteType > affineTransformed = Views.interval( Views.raster( new AffineRealRandomAccessible<>( interpolant, affine ) ), img );
			final IntervalView< UnsignedByteType > longAffineTransformed = Views.interval( new LongAffineRandomAccessible<>( extended, longAffine ), img );
			final IntervalView< UnsignedByteType > longAffineInferredTransformed = Views.interval( new LongAffineRandomAccessible<>( extended, longAffineInferred ), img );

			final Cursor< UnsignedByteType > cursorAffine = Views.flatIterable( affineTransformed ).cursor();
			final Cursor< UnsignedByteType > cursorLongAffine = Views.flatIterable( longAffineTransformed ).cursor();
			final Cursor< UnsignedByteType > cursorLongAffineInferred = Views.flatIterable( longAffineInferredTransformed ).cursor();

			while ( cursorAffine.hasNext() )
			{
				final UnsignedByteType valueAffine = cursorAffine.next();
				final UnsignedByteType valueLongAffine = cursorLongAffine.next();
				final UnsignedByteType valueLongAffineInferred = cursorLongAffineInferred.next();

				Assert.assertEquals( valueAffine.get(), valueLongAffine.get() );
				Assert.assertEquals( valueAffine.get(), valueLongAffineInferred.get() );
			}
		}
	}
}
