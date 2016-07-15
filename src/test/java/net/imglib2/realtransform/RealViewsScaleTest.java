package net.imglib2.realtransform;


import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;


public class RealViewsScaleTest {
	
	@Test
	public void testScale()
	{
		// Create a black image (zeroes) with a box (ones) in the middle
		final Img< UnsignedByteType > img = new ArrayImgFactory< UnsignedByteType >().create( new long[]{100, 100, 100}, new UnsignedByteType() );
		final RandomAccess< UnsignedByteType > ra = img.randomAccess();
		final long[] pos = new long[img.numDimensions()];
		for (int x=35; x<65; ++x) {
			pos[0] = x;
			for (int y=35; y<65; ++y) {
				pos[1] = y;
				for (int z=35; z<65; ++z) {
					pos[2] = z;
					ra.setPosition(pos);
					ra.get().set(1);
				}
			}
		}

		//final InterpolatorFactory<UnsignedByteType, RandomAccessible< UnsignedByteType > > factory = new NLinearInterpolatorFactory< UnsignedByteType >();
		
		// Scale down to 30%
		final IntervalView< UnsignedByteType > scaled = RealViews.scale( img, new NearestNeighborInterpolatorFactory(), new long[]{30, 30, 30} ); // TODO doesn't type check
		
		// Count white pixels:
		long count = 0;
		final Cursor< UnsignedByteType > c = scaled.cursor();
		while ( c.hasNext() )
		{
			c.fwd();
			count += c.get().get(); // TODO: ArrayIndexOutOfBoundsException !! Missing Views.extendMirrorSingle in RealViews.scale
		}
		
		System.out.println("Number of filled pixels: " + count);
	}

	static public void main(String[] args) {
		new RealViewsScaleTest().testScale();
	}
}
