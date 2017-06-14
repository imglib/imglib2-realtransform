package net.imglib2.realtransform;

import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.type.numeric.RealType;

public class InvertibleDeformationFieldTransform< T extends RealType< T > > extends WrappedIterativeInvertibleRealTransform< DeformationFieldTransform< T > >
{

	public InvertibleDeformationFieldTransform( final DeformationFieldTransform< T > def )
	{
		super( def );
	}

}
