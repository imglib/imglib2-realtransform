/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.realtransform;

import net.imglib2.RealPoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ScaleAndTranslation}
 * 
 * @author Philipp Hanslovsky
 *
 */
public class ScaleAndTranslationTest {
	
	private final double sc1 = 1.0, sc2 = 1.5, sc3 = 2.0;
	private final double sh1 = 1.0, sh2 = 1.0, sh3 = 1.3;
	
	private final double[] scales  = new double[] { sc1, sc2, sc3 };
	private final double[] shifts  = new double[] { sh1, sh2, sh3 };
	private final double[] scales2 = new double[] { sc2, sc3, sc1 };
	private final double[] shifts2 = new double[] { sh2, sh3, sh1 };
	
	private final double[] transformMatrixReference = new double[] {
		1.0, 0.0, 0.0, 1.0,
		0.0, 1.5, 0.0, 1.0,
		0.0, 0.0, 2.0, 1.3
	};
	
	
	private final double[] sourceCoordinate = new double[] { 1.0, 2.0, 3.0 };
	private final double[] targetCoordinate = new double[] { 
			sc1*sourceCoordinate[0] + sh1,
			sc2*sourceCoordinate[1] + sh2,
			sc3*sourceCoordinate[2] + sh3
	};
	private final double[] targetCoordinateAppliedConcatenated = new double[] {
			scales[0]*scales2[0]*sourceCoordinate[0] + scales[0]*shifts2[0]+ shifts[0],
			scales[1]*scales2[1]*sourceCoordinate[1] + scales[1]*shifts2[1]+ shifts[1],
			scales[2]*scales2[2]*sourceCoordinate[2] + scales[2]*shifts2[2]+ shifts[2]
	};
	private final double[] targetCoordinateAppliedPreConcatenated = new double[] {
			scales2[0]*scales[0]*sourceCoordinate[0] + scales2[0]*shifts[0]+ shifts2[0],
			scales2[1]*scales[1]*sourceCoordinate[1] + scales2[1]*shifts[1]+ shifts2[1],
			scales2[2]*scales[2]*sourceCoordinate[2] + scales2[2]*shifts[2]+ shifts2[2]
	};
	
	private final float[] sourceCoordinateFloat = new float[] { 1.0f, 2.0f, 3.0f };
	private final float[] targetCoordinateFloat = new float[] {
			(float) (sc1*sourceCoordinate[0] + sh1),
			(float) (sc2*sourceCoordinate[1] + sh2),
			(float) (sc3*sourceCoordinate[2] + sh3)
	};
	private final float[] targetCoordinateAppliedConcatenatedFloat = new float[] {
			(float) (scales[0]*scales2[0]*sourceCoordinate[0] + scales[0]*shifts2[0]+ shifts[0]),
			(float) (scales[1]*scales2[1]*sourceCoordinate[1] + scales[1]*shifts2[1]+ shifts[1]),
			(float) (scales[2]*scales2[2]*sourceCoordinate[2] + scales[2]*shifts2[2]+ shifts[2])
	};
	private final float[] targetCoordinateAppliedPreConcatenatedFloat = new float[] {
			(float) (scales2[0]*scales[0]*sourceCoordinate[0] + scales2[0]*shifts[0]+ shifts2[0]),
			(float) (scales2[1]*scales[1]*sourceCoordinate[1] + scales2[1]*shifts[1]+ shifts2[1]),
			(float) (scales2[2]*scales[2]*sourceCoordinate[2] + scales2[2]*shifts[2]+ shifts2[2])
	};
	
	private final RealPoint sourcePoint = new RealPoint( sourceCoordinate.clone() );
	private final RealPoint targetPoint = new RealPoint( targetCoordinate.clone() );
	
	private final double[] result                = new double[ sourceCoordinate.length ];
	private final float[] resultFloat            = new float[ sourceCoordinate.length ];
	private final RealPoint resultPoint          = new RealPoint( result.clone() );
	private final ScaleAndTranslation transform  = new ScaleAndTranslation( scales, shifts );
	private final ScaleAndTranslation transform2 = new ScaleAndTranslation( scales2, shifts2 );

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		final ScaleAndTranslation[] transforms = new ScaleAndTranslation[] { 
				transform, 
				transform.copy(), 
				transform.inverse().inverse() 
				};

		
		for ( final ScaleAndTranslation t : transforms ) {
			t.apply( sourceCoordinate, result );
			t.apply( sourceCoordinateFloat, resultFloat );
			t.apply( sourcePoint, resultPoint );
			
			Assert.assertArrayEquals( targetCoordinate, result, 0.0 );
			Assert.assertArrayEquals( targetCoordinateFloat, resultFloat, 0.0f );
			for ( int d = 0; d < resultPoint.numDimensions(); ++d ) {
				Assert.assertEquals( targetPoint.getDoublePosition( d ), resultPoint.getDoublePosition( d ), 0.0 );
			}
		}
	}
	
	@Test
	public void testInverse() {
		
		transform.applyInverse( result, targetCoordinate );
		transform.applyInverse( resultFloat, targetCoordinateFloat );
		transform.applyInverse( resultPoint, targetPoint );
		
		Assert.assertArrayEquals( sourceCoordinate, result, 0.0 );
		Assert.assertArrayEquals( sourceCoordinateFloat, resultFloat, 0.0f );
		for ( int d = 0; d < resultPoint.numDimensions(); ++d ) {
			Assert.assertEquals( sourcePoint.getDoublePosition( d ), resultPoint.getDoublePosition( d ), 0.0 );
		}
		
		transform.inverse().apply( targetCoordinate , result);
		transform.inverse().apply( targetCoordinateFloat, resultFloat );
		transform.inverse().apply( targetPoint, resultPoint );
		
		Assert.assertArrayEquals( sourceCoordinate, result, 0.0 );
		Assert.assertArrayEquals( sourceCoordinateFloat, resultFloat, 0.0f );
		for ( int d = 0; d < resultPoint.numDimensions(); ++d ) {
			Assert.assertEquals( sourcePoint.getDoublePosition( d ), resultPoint.getDoublePosition( d ), 0.0 );
		}
	}
	
	
	@Test
	public void testConcatenation() {
		ScaleAndTranslation concatenated    = transform.copy().concatenate( transform2 );
		ScaleAndTranslation preConcatenated = transform.copy().preConcatenate( transform2 );
		ScaleAndTranslation unity           = transform.copy().concatenate( transform.inverse() );
		
		concatenated.apply( sourceCoordinate, result );
		concatenated.apply( sourceCoordinateFloat, resultFloat );
		
		Assert.assertArrayEquals( targetCoordinateAppliedConcatenated, result, 0.0 );
		Assert.assertArrayEquals( targetCoordinateAppliedConcatenatedFloat, resultFloat, 0.0f );
		
		preConcatenated.apply( sourceCoordinate, result );
		preConcatenated.apply( sourceCoordinateFloat, resultFloat );
		
		Assert.assertArrayEquals( targetCoordinateAppliedPreConcatenated, result, 0.0 );
		Assert.assertArrayEquals( targetCoordinateAppliedPreConcatenatedFloat, resultFloat, 0.0f );
		
		unity.apply( sourceCoordinate, result );
		unity.apply( sourceCoordinateFloat, resultFloat );
		
		Assert.assertArrayEquals( sourceCoordinate, result, 0.0 );
		Assert.assertArrayEquals( sourceCoordinateFloat, resultFloat, 0.0f );
		
		Assert.assertEquals( ScaleAndTranslationGet.class, transform.getConcatenableClass() );
		Assert.assertEquals( ScaleAndTranslationGet.class, transform.getPreConcatenableClass() );
	}
	
	
	@Test
	public void testGet() {
		double[] transformMatrix = transform.getRowPackedCopy();
		Assert.assertArrayEquals( transformMatrixReference, transformMatrix, 0.0 );
		Assert.assertArrayEquals( scales, transform.getScaleCopy(), 0.0 );
		Assert.assertArrayEquals( shifts, transform.getTranslationCopy(), 0.0 );
		for ( int d = 0; d < transform.numDimensions(); ++d ) {
			Assert.assertEquals( scales[ d ], transform.getScale( d ), 0.0 );
			Assert.assertEquals( shifts[ d ], transform.getTranslation( d ), 0.0 );
			RealPoint ref = new RealPoint( new double[ scales.length ] );
			RealPoint rp  = transform.d( d );
			ref.setPosition( scales[d], d);
			for ( int k = 0; k < transform.numDimensions(); ++k )
				Assert.assertEquals( ref.getDoublePosition( k ), rp.getDoublePosition( k ), 0.0 );
		}
		
		for ( int m = 0; m < transform.numDimensions(); ++m ) {
			for ( int n = 0; n < transform.numDimensions() + 1; ++n ) {
				if ( n == m )
					Assert.assertEquals( scales[m], transform.get( m, n ), 0.0 );
				else if ( n == transform.numDimensions() )
					Assert.assertEquals( shifts[m], transform.get( m, n ), 0.0 );
				else
					Assert.assertEquals( 0.0, transform.get( m, n ), 0.0 );
			}
		}
	}
	
	@Test
	public void testChained() {
		InvertibleRealTransformSequence seq = new InvertibleRealTransformSequence();
		seq.add( new Scale3D( scales ) );
		seq.add( new Translation3D( shifts ) );
		
		double[] dummy = new double[ result.length ];
		
		transform.apply( sourceCoordinate, result );
		seq.apply( sourceCoordinate, dummy );
		Assert.assertArrayEquals( result, dummy, 0.0 );
		
		transform.applyInverse( sourceCoordinate, result );
		seq.applyInverse( sourceCoordinate, dummy );
		Assert.assertArrayEquals( result, dummy, 0.0 );
	}

}
