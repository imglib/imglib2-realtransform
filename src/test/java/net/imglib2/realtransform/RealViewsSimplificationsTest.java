package net.imglib2.realtransform;

import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * @author Christian Dietz, University of Konstanz
 *
 */
public class RealViewsSimplificationsTest
{

	private static RealRandomAccessible< FloatType > INTERPOLATED;

	private static final AffineGet MIXED2D = create( new double[] {
			Math.sqrt( 2 ) / 2.0, -Math.sqrt( 2 ) / 2.0, 2.0,
			Math.sqrt( 2 ) / 2.0, Math.sqrt( 2 ) / 2.0, 5.5
	}, 2 );

	private static final AffineGet TRANSLATION2D = create( new double[] {
			1.0, 0.0, 2.0,
			0.0, 1.0, 5.5
	}, 2 );

	private static final AffineGet TRANSLATION3D = create( new double[] {
			1.0, 0.0, 0.0, 2.0,
			0.0, 1.0, 0.0, 5.5,
			0.0, 0.0, 1.0, 7.5
	}, 3 );

	private static final AffineGet TRANSLATION4D = create( new double[] {
			1.0, 0.0, 0.0, 0.0, 2.0,
			0.0, 1.0, 0.0, 0.0, 5.5,
			0.0, 0.0, 1.0, 0.0, 7.5,
			0.0, 0.0, 0.0, 1.0, 10.5,
	}, 4 );

	private static final AffineGet SCALE2D = create( new double[] {
			2.0, 0.0, 0.0,
			0.0, 1.0, 0.0
	}, 2 );

	private static final AffineGet SCALE3D = create( new double[] {
			2.0, 0.0, 0.0, 0.0,
			0.0, 4.0, 0.0, 0.0,
			0.0, 0.0, 5.5, 0.0
	}, 3 );

	private static final AffineGet SCALE4D = create( new double[] {
			9.0, 0.0, 0.0, 0.0, 0.0,
			0.0, 1.0, 0.0, 0.0, 0.0,
			0.0, 0.0, 3.0, 0.0, 0.0,
			0.0, 0.0, 0.0, 1.0, 0.0,
	}, 4 );

	private static final AffineGet SCALEANDTRANSLATION2D = create( new double[] {
			2.0, 0.0, 2.0,
			0.0, 1.0, 4.0
	}, 2 );

	private static final AffineGet SCALEANDTRANSLATION3D = create( new double[] {
			2.0, 0.0, 0.0, 2.0,
			0.0, 4.0, 0.0, 4.0,
			0.0, 0.0, 5.5, 0.0
	}, 3 );

	private static final AffineGet SCALEANDTRANSLATION4D = create( new double[] {
			9.0, 0.0, 0.0, 0.0, 0.0,
			0.0, 1.0, 0.0, 0.0, 3.0,
			0.0, 0.0, 3.0, 0.0, 0.0,
			0.0, 0.0, 0.0, 1.0, 0.0,
	}, 4 );

	@Before
	public void init()
	{
		INTERPOLATED = Views.interpolate( rndExtendedRAIFloats(), new NLinearInterpolatorFactory< FloatType >() );
	}

	@SuppressWarnings( "rawtypes" )
	@Test
	public void testSimplifingMixed()
	{
		RealRandomAccessible< FloatType > transform = RealViews.transform( RealViews.transform( INTERPOLATED, TRANSLATION2D ), TRANSLATION2D );
		RealRandomAccessible< FloatType > simplifiedTransform = RealViewsSimplifyUtils.simplifyReal( transform );

		Assert.assertFalse( ( ( RealTransformRealRandomAccessible ) simplifiedTransform ).getSource() instanceof RealTransformRealRandomAccessible );
	}

	@Test
	public void testSimplifingMixedConsistency()
	{
		RealRandomAccessible< FloatType > transform = RealViews.transform( RealViews.transform( INTERPOLATED, MIXED2D ), TRANSLATION2D );
		RealRandomAccessible< FloatType > simplifiedTransform = RealViewsSimplifyUtils.simplifyReal( transform );

		Cursor< FloatType > cursor1 = Views.interval( Views.raster( transform ), new FinalInterval( 20, 20 ) ).cursor();
		Cursor< FloatType > cursor2 = Views.interval( Views.raster( simplifiedTransform ), new FinalInterval( 20, 20 ) ).cursor();

		while ( cursor1.hasNext() && cursor2.hasNext() )
		{
			Assert.assertTrue( cursor1.next().get() == cursor2.next().get() );
		}
	}

	@Test
	public void testSimplifingTransformRealAffineConsistency()
	{
		RealRandomAccessible< FloatType > transform = RealViews.transform( RealViews.transform( RealViews.transform( INTERPOLATED, MIXED2D ), MIXED2D ), TRANSLATION2D );
		RealRandomAccessible< FloatType > simplifiedTransform = RealViewsSimplifyUtils.simplifyReal( transform );

		Cursor< FloatType > cursor1 = Views.interval( Views.raster( transform ), new FinalInterval( 20, 20 ) ).cursor();
		Cursor< FloatType > cursor2 = Views.interval( Views.raster( simplifiedTransform ), new FinalInterval( 20, 20 ) ).cursor();

		while ( cursor1.hasNext() && cursor2.hasNext() )
		{
			Assert.assertTrue( cursor1.next().get() == cursor2.next().get() );
		}
	}

	@Test
	public void testSimplifingTransformRealAffineConsistency2()
	{
		RealRandomAccessible< FloatType > transform = RealViews.transform( INTERPOLATED, TRANSLATION2D );
		RealRandomAccessible< FloatType > simplifiedTransform = RealViewsSimplifyUtils.simplifyReal( transform );

		Cursor< FloatType > cursor1 = Views.interval( Views.raster( transform ), new FinalInterval( 20, 20 ) ).cursor();
		Cursor< FloatType > cursor2 = Views.interval( Views.raster( simplifiedTransform ), new FinalInterval( 20, 20 ) ).cursor();

		while ( cursor1.hasNext() && cursor2.hasNext() )
		{
			Assert.assertTrue( cursor1.next().get() == cursor2.next().get() );
		}
	}

	private static RandomAccessible< FloatType > rndExtendedRAIFloats()
	{
		ArrayImg< FloatType, FloatArray > floats = ArrayImgs.floats( 20, 20 );

		Random r = new Random();
		for ( FloatType t : floats )
		{
			t.set( r.nextFloat() );
		}
		return Views.extendZero( floats );
	}

	@Test
	public void testExlusiveTranslation()
	{
		Assert.assertTrue( RealViewsSimplifyUtils.isExlusiveTranslation( TRANSLATION2D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExlusiveTranslation( TRANSLATION3D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExlusiveTranslation( TRANSLATION4D ) );

		Assert.assertFalse( RealViewsSimplifyUtils.isExlusiveTranslation( SCALEANDTRANSLATION2D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExlusiveTranslation( SCALEANDTRANSLATION3D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExlusiveTranslation( SCALEANDTRANSLATION4D ) );

		Assert.assertFalse( RealViewsSimplifyUtils.isExlusiveTranslation( SCALE2D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExlusiveTranslation( SCALE3D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExlusiveTranslation( SCALE4D ) );
	}

	@Test
	public void testExlusiveScaling()
	{
		Assert.assertFalse( RealViewsSimplifyUtils.isExclusiveScale( TRANSLATION2D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExclusiveScale( TRANSLATION3D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExclusiveScale( TRANSLATION4D ) );

		Assert.assertFalse( RealViewsSimplifyUtils.isExclusiveScale( SCALEANDTRANSLATION2D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExclusiveScale( SCALEANDTRANSLATION3D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isExclusiveScale( SCALEANDTRANSLATION4D ) );

		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScale( SCALE2D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScale( SCALE3D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScale( SCALE4D ) );
	}

	@Test
	public void testExlusiveScalingAndTranslation()
	{
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( TRANSLATION2D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( TRANSLATION3D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( TRANSLATION4D ) );

		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( SCALEANDTRANSLATION2D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( SCALEANDTRANSLATION3D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( SCALEANDTRANSLATION4D ) );

		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( SCALE2D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( SCALE3D ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isExclusiveScaleAndTranslation( SCALE4D ) );
	}

	@Test
	public void testIdentity()
	{
		Assert.assertTrue( RealViewsSimplifyUtils.isIdentity( new AffineTransform2D() ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isIdentity( new AffineTransform3D() ) );
		Assert.assertTrue( RealViewsSimplifyUtils.isIdentity( new AffineTransform( 4 ) ) );

		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( TRANSLATION2D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( TRANSLATION3D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( TRANSLATION4D ) );

		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( SCALEANDTRANSLATION2D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( SCALEANDTRANSLATION3D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( SCALEANDTRANSLATION4D ) );

		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( SCALE2D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( SCALE3D ) );
		Assert.assertFalse( RealViewsSimplifyUtils.isIdentity( SCALE4D ) );
	}

	@Test
	public void testSimplification()
	{
		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( TRANSLATION2D ) instanceof Translation2D );
		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( TRANSLATION3D ) instanceof Translation3D );
		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( TRANSLATION4D ) instanceof Translation );

		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( SCALE2D ) instanceof Scale2D );
		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( SCALE3D ) instanceof Scale3D );
		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( SCALE4D ) instanceof Scale );

		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( SCALEANDTRANSLATION2D ) instanceof ScaleAndTranslation );
		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( SCALEANDTRANSLATION3D ) instanceof ScaleAndTranslation );
		Assert.assertTrue( RealViewsSimplifyUtils.simplifyRealTransform( SCALEANDTRANSLATION4D ) instanceof ScaleAndTranslation );
	}

	private static AffineGet create( double[] input, int numDims )
	{
		if ( numDims == 2 )
		{
			AffineTransform2D affine = new AffineTransform2D();
			affine.set( input );
			return affine;
		}
		else if ( numDims == 3 )
		{
			AffineTransform3D affine = new AffineTransform3D();
			affine.set( input );
			return affine;
		}
		else
		{
			AffineTransform affine = new AffineTransform( numDims );
			affine.set( input );
			return affine;
		}
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Test
	public void testNarrowReduction()
	{

		final Img< FloatType > floats = ArrayImgs.floats( 20, 20 );
		final RealRandomAccessible< FloatType > interpolate = Views.interpolate( floats, new NLinearInterpolatorFactory< FloatType >() );

		final RealRandomAccessible< FloatType > translatedB = RealViews.transformReal( RealViews.transformReal( interpolate, TRANSLATION2D ), TRANSLATION2D );

		Assert.assertTrue( translatedB instanceof RealTransformRealRandomAccessible );

		RealRandomAccessible< FloatType > simplified = RealViewsSimplifyUtils.simplifyReal( translatedB );

		Assert.assertTrue( ( ( RealTransformRealRandomAccessible ) simplified ).getTransformToSource() instanceof Translation2D );

		Assert.assertArrayEquals( new double[] { 1.0, 0.0, -4.0, 0.0, 1.0, -11.0 },
				( ( AffineGet ) ( ( RealTransformRealRandomAccessible< FloatType, ? > ) simplified ).getTransformToSource() ).getRowPackedCopy(), 0 );

	}

	@Test
	public void testIdentityReduction()
	{
		final RealRandomAccessible< FloatType > translatedB = RealViews.transformReal( RealViews.transformReal( INTERPOLATED, TRANSLATION2D ), TRANSLATION2D.inverse() );
		Assert.assertFalse( RealViewsSimplifyUtils.simplifyReal( translatedB ) instanceof RealTransformRealRandomAccessible );
	}

}
