
/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.realtransform.interval;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.PositionFieldTransform;

public class RealIntervalsTests {

	static final double EPS = 1e-9;
	static final FinalRealInterval itvl = new FinalRealInterval(
			new double[] { 0, 0, 0 }, new double[] { 40, 30, 20 });

	@Test
	public void testBboxCornersAffine()
	{
		final AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale(2, 3, 4);

		final RealInterval bbox = xfm.boundingInterval( itvl, IntervalSamplingMethod.CORNERS );
		assertEquals( "max x ", itvl.realMax(0) * 2, bbox.realMax(0), EPS );
		assertEquals( "max y ", itvl.realMax(1) * 3, bbox.realMax(1), EPS );
		assertEquals( "max z ", itvl.realMax(2) * 4, bbox.realMax(2), EPS );
	}

	@Test
	public void testBboxFacesAffine()
	{
		final AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale(2, 3, 4);

		final RealInterval bbox = xfm.boundingInterval( itvl, new FacesSpacing( 10, 10, 10 ) );
		assertEquals( "max x ", itvl.realMax(0) * 2, bbox.realMax(0), EPS );
		assertEquals( "max y ", itvl.realMax(1) * 3, bbox.realMax(1), EPS );
		assertEquals( "max z ", itvl.realMax(2) * 4, bbox.realMax(2), EPS );
	}

	@Test
	public void testBboxVolumeAffine()
	{
		final AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale(2, 3, 4);

		final RealInterval bbox = xfm.boundingInterval( itvl, new VolumeSpacing( 10 ) );
		assertEquals( "max x ", itvl.realMax(0) * 2, bbox.realMax(0), EPS );
		assertEquals( "max y ", itvl.realMax(1) * 3, bbox.realMax(1), EPS );
		assertEquals( "max z ", itvl.realMax(2) * 4, bbox.realMax(2), EPS );
	}

	@Test
	public void testBboxCornersPfield()
	{
		final PositionFieldTransform xfm = pfield();

		// estimating with the corners does NOT correctly estimate the bounding box
		// for this transformation.  Make sure that it behaves as expected -
		// returning the original interval.
		final RealInterval bbox = xfm.boundingInterval( itvl, IntervalSamplingMethod.CORNERS );
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
		final PositionFieldTransform xfm = pfield();
		final RealInterval bbox = xfm.boundingInterval( itvl, new FacesSpacing( 5, 5, 5 ) );
		assertEquals( "min x ", itvl.realMin(0), bbox.realMin(0), EPS );
		assertEquals( "max x ", itvl.realMax(0) + 5, bbox.realMax(0), EPS );

		assertEquals( "min y ", itvl.realMin(1) - 5, bbox.realMin(1), EPS );
		assertEquals( "max y ", itvl.realMax(1), bbox.realMax(1), EPS );

		assertEquals( "min z ", itvl.realMin(2), bbox.realMin(2), EPS );
		assertEquals( "max z ", itvl.realMax(2), bbox.realMax(2), EPS );

		final RealInterval bboxSamples = xfm.boundingInterval( itvl, new FacesSteps( 8, 6, 4 ) );
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
		final PositionFieldTransform xfm = pfield();
		final RealInterval bbox = xfm.boundingInterval( itvl, new VolumeSpacing( 5 ) );
		assertEquals( "min x ", itvl.realMin(0), bbox.realMin(0), EPS );
		assertEquals( "max x ", itvl.realMax(0) + 5, bbox.realMax(0), EPS );

		assertEquals( "min y ", itvl.realMin(1) - 5, bbox.realMin(1), EPS );
		assertEquals( "max y ", itvl.realMax(1), bbox.realMax(1), EPS );

		assertEquals( "min z ", itvl.realMin(2), bbox.realMin(2), EPS );
		assertEquals( "max z ", itvl.realMax(2), bbox.realMax(2), EPS );

		final RealInterval bboxSamples = xfm.boundingInterval( itvl, new VolumeSteps( 8, 6, 4 ) );
		assertEquals( "min x samples", itvl.realMin(0), bboxSamples.realMin(0), EPS );
		assertEquals( "max x samples", itvl.realMax(0) + 5, bboxSamples.realMax(0), EPS );

		assertEquals( "min y samples", itvl.realMin(1) - 5, bboxSamples.realMin(1), EPS );
		assertEquals( "max y samples", itvl.realMax(1), bboxSamples.realMax(1), EPS );

		assertEquals( "min z samples", itvl.realMin(2), bboxSamples.realMin(2), EPS );
		assertEquals( "max z samples", itvl.realMax(2), bboxSamples.realMax(2), EPS );
	}

	/**
	 * This strange position field is designed so that different bounding box
	 * estimation methods perform differently.
	 *
	 * @return a position field.
	 */
	private static PositionFieldTransform pfield()
	{
		// center
		final double xc = 15;
		final double yc = 20;

		// width
		final double xwidth = 3;
		final double ywidth = 3;

		// amount
		final double dx =  5;
		final double dy = -5;

		final FunctionRealRandomAccessible<RealPoint> vecField = new FunctionRealRandomAccessible<>( 3,
				(p,v) -> {
					final double x = p.getDoublePosition( 0 );
					final double y = p.getDoublePosition( 1 );
					final double z = p.getDoublePosition( 2 );

					if( y >= xc - xwidth && y < xc + xwidth )
						v.setPosition(x + dx, 0);
					else
						v.setPosition(x, 0);

					if( x >= yc - ywidth && x < yc + ywidth )
						v.setPosition(y + dy, 1);
					else
						v.setPosition(y, 1);

					// identity for z
					v.setPosition(z, 2);
				},
				() -> new RealPoint( 3 ));

		return new PositionFieldTransform( vecField );
	}

}
