package net.imglib2.realtransform;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class RealViewsScaleTest
{
	@Test
	public void testScale()
	{
		// Create a black image (zeroes) with a box (ones) in the middle
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 100 );
		final FinalInterval box = new FinalInterval( new long[]{ 35, 35, 35 }, new long[]{ 64, 64, 64 } );
		for ( final UnsignedByteType t : Views.iterable( Views.interval( img, box ) ) )
			t.set( 1 );

		// Scale down
		final RandomAccessibleInterval< UnsignedByteType > scaled =
				RealViews.scale( img, new NearestNeighborInterpolatorFactory<>(), 0.4, 0.37, 0.11 );

		System.out.println( "Interval: " + Util.printInterval( scaled ) );

		// Count white pixels
		long count = 0;
		for ( final UnsignedByteType t : Views.iterable( scaled ) )
			count += t.get();

		System.out.println( "Number of filled pixels: " + count );

		Assert.assertEquals( 528, count );
	}
}