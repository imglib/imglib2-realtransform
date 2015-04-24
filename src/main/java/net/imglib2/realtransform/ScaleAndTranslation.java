/**
 * 
 */
package net.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InvertibleRealTransform;

/**
 * @author Philipp Hanslovsky <hanslovskyp@janelia.hhmi.org>
 * Transform that applies a scaling first and then shifts coordinates.
 * This transform is faster than using an {@link AffineTransform} with 
 * entries on the diagonal only.
 *
 */
public class ScaleAndTranslation implements InvertibleRealTransform 
{
	
	private final ScaleAndTranslation inverse;
	private final double[] scales;
	private final double[] translations;
	private final int nDim;
	
	

	/**
	 * @param scales Array containing scales
	 * @param translations Array containing translations
	 */
	public ScaleAndTranslation( final double[] scales, final double[] translations ) 
	{
		super();
		assert translations.length == scales.length;
		this.scales = scales.clone(); // clone?
		this.translations = translations.clone(); // clone?
		this.nDim   = translations.length;
		this.inverse = this.inverse();
	}
	
	
	/**
	 * private constructor that takes inverse to avoid object creation when calling
	 * {@link #inverse}
	 * @param inverse 
	 * @param scales Array containing scales
	 * @param shifts Array containing translations
	 * @param nDim Number of Dimensions
	 */
	private ScaleAndTranslation(
			final ScaleAndTranslation inverse, 
			final double[] scales,
			final double[] shifts, 
			final int nDim 
			) 
	{
		super();
		this.inverse = inverse;
		this.scales = scales;
		this.translations = shifts;
		this.nDim = nDim;
	}
	

	@Override
	public int numSourceDimensions() 
	{
		return nDim;
	}

	@Override
	public int numTargetDimensions() 
	{
		return nDim;
	}

	@Override
	public void apply(final double[] source, final double[] target) 
	{
		assert source.length == nDim && target.length == nDim;
		for (int i = 0; i < nDim; i++) 
		{
			target[i] = scales[i]*source[i] + translations[i];
		}
	}

	@Override
	public void apply(final float[] source, final float[] target) 
	{
		assert source.length == nDim && target.length == nDim;
		for (int i = 0; i < nDim; i++) 
		{
			target[i] = (float) (scales[i]*source[i] + translations[i]);
		}
	}

	@Override
	public void apply(final RealLocalizable source, final RealPositionable target) 
	{
		assert source.numDimensions() == nDim && target.numDimensions() == nDim;
		for ( int d = 0; d < nDim; ++d ) 
		{
			target.setPosition( scales[d]*source.getDoublePosition( d ) + translations[d], d);
		}
	}

	@Override
	public void applyInverse(final double[] source, final double[] target) 
	{
		// target is the source for the inverse transform, thus switch order in call of this.inverse.apply
		this.inverse.apply( target, source );
	}

	@Override
	public void applyInverse(final float[] source, final float[] target) 
	{
		// target is the source for the inverse transform, thus switch order in call of this.inverse.apply
		this.inverse.apply( target, source );
	}

	@Override
	public void applyInverse(final RealPositionable source, final RealLocalizable target) 
	{
		// target is the source for the inverse transform, thus switch order in call of this.inverse.apply
		this.inverse.apply( target, source );
	}

	@Override
	public ScaleAndTranslation inverse() 
	{
		final double[] invertedShifts = new double[ nDim ];
		final double[] invertedScales = new double[ nDim ];
		for (int i = 0; i < nDim; i++) 
		{
			invertedScales[i] = 1.0 /scales[i];
			invertedShifts[i] = -translations[i] * invertedScales[i];
		}
		return new ScaleAndTranslation( this, invertedScales, invertedShifts, nDim );
	}

	@Override
	public ScaleAndTranslation copy() 
	{
		return new ScaleAndTranslation( inverse, scales, translations, nDim );
	}

}
