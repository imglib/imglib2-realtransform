package net.imglib2.realtransform;


/**
 * An <em>n</em> transform that applies a scaling first and then shifts coordinates.
 * This fields defines the necessary getters.
 * 
 * @author Philipp Hanslovsky <hanslovskyp@janelia.hhmi.org>
 */
public interface ScaleAndTranslationGet extends AffineGet {
	
	/**
	 * Get a field of the <em>n</em>-dimensional scale vector.
	 * 
	 * @param d
	 * @return
	 */
	public double getScale( final int d );

	/**
	 * Get a copy of the <em>n</em>-dimensional scale vector.
	 * 
	 * @return
	 */
	public double[] getScaleCopy();
	
	/**
	 * Get a field of the <em>n</em>-dimensionsional translation vector.
	 * 
	 * @param d
	 * @return
	 */
	public double getTranslation( final int d );

	/**
	 * Get a copy of the <em>n</em>-dimensionsional translation vector.
	 * 
	 * @return
	 */
	public double[] getTranslationCopy();

	@Override
	ScaleAndTranslationGet inverse();

}
