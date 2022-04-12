package net.imglib2.util;

import static org.junit.Assert.*;
import org.junit.Test;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.PositionFieldTransform;
import net.imglib2.type.numeric.real.DoubleType;

public class RealIntervalsTests {

	static final double EPS = 1e-9;
	static final FinalRealInterval itvl = new FinalRealInterval( 
			new double[] { 0, 0, 0 }, new double[] { 40, 30, 20 });

	@Test
	public void testBboxCornersAffine()
	{
		final AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale(2, 3, 4);

		final RealInterval bbox = RealIntervals.boundingIntervalCorners( itvl, xfm );
		assertEquals( "max x ", itvl.realMax(0) * 2, bbox.realMax(0), EPS );
		assertEquals( "max y ", itvl.realMax(1) * 3, bbox.realMax(1), EPS );
		assertEquals( "max z ", itvl.realMax(2) * 4, bbox.realMax(2), EPS );
	}

	@Test
	public void testBboxFacesAffine()
	{
		final AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale(2, 3, 4);

		final RealInterval bbox = RealIntervals.boundingIntervalFaces( itvl, xfm, 10, 10, 10 );
		assertEquals( "max x ", itvl.realMax(0) * 2, bbox.realMax(0), EPS );
		assertEquals( "max y ", itvl.realMax(1) * 3, bbox.realMax(1), EPS );
		assertEquals( "max z ", itvl.realMax(2) * 4, bbox.realMax(2), EPS );
	}

	@Test
	public void testBboxVolumeAffine()
	{
		final AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale(2, 3, 4);

		final RealInterval bbox = RealIntervals.boundingIntervalVolume( itvl, xfm, 10 );
		assertEquals( "max x ", itvl.realMax(0) * 2, bbox.realMax(0), EPS );
		assertEquals( "max y ", itvl.realMax(1) * 3, bbox.realMax(1), EPS );
		assertEquals( "max z ", itvl.realMax(2) * 4, bbox.realMax(2), EPS );
	}

	@Test
	public void testBboxCornersPfield()
	{
		final PositionFieldTransform<DoubleType> xfm = pfield();

		// estimating with the corners does NOT correctly estimate the bounding box 
		// for this transformation.  Make sure that it behaves as expected - 
		// returning the original interval.
		final RealInterval bbox = RealIntervals.boundingIntervalCorners( itvl, xfm );
		assertEquals( "min x ", itvl.realMin(0), bbox.realMin(0), EPS );
		assertEquals( "max x ", itvl.realMax(0), bbox.realMax(0), EPS );

		assertEquals( "min y ", itvl.realMin(1), bbox.realMin(1), EPS );
		assertEquals( "max y ", itvl.realMax(1), bbox.realMax(1), EPS );

		assertEquals( "min z ", itvl.realMin(2), bbox.realMin(2), EPS );
		assertEquals( "max z ", itvl.realMax(2), bbox.realMax(2), EPS );
	}

	@Test
	public void testBboxFacesPfield()
	{
		final PositionFieldTransform<DoubleType> xfm = pfield();
		final RealInterval bbox = RealIntervals.boundingIntervalFaces( itvl, xfm, 5, 5, 5 );
		assertEquals( "min x ", itvl.realMin(0), bbox.realMin(0), EPS );
		assertEquals( "max x ", itvl.realMax(0) + 5, bbox.realMax(0), EPS );

		assertEquals( "min y ", itvl.realMin(1) - 5, bbox.realMin(1), EPS );
		assertEquals( "max y ", itvl.realMax(1), bbox.realMax(1), EPS );

		assertEquals( "min z ", itvl.realMin(2), bbox.realMin(2), EPS );
		assertEquals( "max z ", itvl.realMax(2), bbox.realMax(2), EPS );
		
		final RealInterval bboxSamples = RealIntervals.boundingIntervalFacesSamples( itvl, xfm, 8, 6, 4 );
		assertEquals( "min x ", itvl.realMin(0), bboxSamples.realMin(0), EPS );
		assertEquals( "max x ", itvl.realMax(0) + 5, bboxSamples.realMax(0), EPS );

		assertEquals( "min y ", itvl.realMin(1) - 5, bboxSamples.realMin(1), EPS );
		assertEquals( "max y ", itvl.realMax(1), bboxSamples.realMax(1), EPS );

		assertEquals( "min z ", itvl.realMin(2), bboxSamples.realMin(2), EPS );
		assertEquals( "max z ", itvl.realMax(2), bboxSamples.realMax(2), EPS );
	}

	@Test
	public void testBboxVolumePfield()
	{
		final PositionFieldTransform<DoubleType> xfm = pfield();
		final RealInterval bbox = RealIntervals.boundingIntervalVolume( itvl, xfm, 5 );
		assertEquals( "min x ", itvl.realMin(0), bbox.realMin(0), EPS );
		assertEquals( "max x ", itvl.realMax(0) + 5, bbox.realMax(0), EPS );

		assertEquals( "min y ", itvl.realMin(1) - 5, bbox.realMin(1), EPS );
		assertEquals( "max y ", itvl.realMax(1), bbox.realMax(1), EPS );

		assertEquals( "min z ", itvl.realMin(2), bbox.realMin(2), EPS );
		assertEquals( "max z ", itvl.realMax(2), bbox.realMax(2), EPS );
		
		final RealInterval bboxSamples = RealIntervals.boundingIntervalVolumeSamples( itvl, xfm, 8, 6, 4 );
		assertEquals( "min x samples", itvl.realMin(0), bboxSamples.realMin(0), EPS );
		assertEquals( "max x samples", itvl.realMax(0) + 5, bboxSamples.realMax(0), EPS );

		assertEquals( "min y samples", itvl.realMin(1) - 5, bboxSamples.realMin(1), EPS );
		assertEquals( "max y samples", itvl.realMax(1), bboxSamples.realMax(1), EPS );

		assertEquals( "min z samples", itvl.realMin(2), bboxSamples.realMin(2), EPS );
		assertEquals( "max z samples", itvl.realMax(2), bboxSamples.realMax(2), EPS );
	}

	private static PositionFieldTransform< DoubleType > pfield()
	{
		return new PositionFieldTransform<>(
				pfieldComponent( 1, 15, 3, 0, 5),
				pfieldComponent( 0, 20, 3, 1, -5 ),
				pfieldIdentity( 2 ));
	}

	private static RealRandomAccessible<DoubleType> pfieldComponent( final int dim, final double pos, final double width, 
			final int outdim, final double amount )
	{
		return new FunctionRealRandomAccessible<>( 3,
				(p,v) -> {
					final double pd = p.getDoublePosition( dim );
					final double outp = p.getDoublePosition( outdim );
					if( pd >= pos - width && pd < pos + width )
						v.set( outp + amount );
					else 
						v.set( outp );
				},
				DoubleType::new );
	}

	/**
	 * The identity position field for a specified dimension
	 * 
	 * @param dim the dimension
	 * @return the field
	 */
	private static RealRandomAccessible<DoubleType> pfieldIdentity( final int dim )
	{
		return new FunctionRealRandomAccessible<>( 3,
				(p,v) -> { v.set( p.getDoublePosition( dim )); },
				DoubleType::new );
	}

}
