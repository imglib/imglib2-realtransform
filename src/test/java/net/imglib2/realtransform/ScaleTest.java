/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

public class ScaleTest {

	double[] v23 = new double[] { 2, 3 };
	double[] v45 = new double[] { 4, 5 };
	double[] result2;

	double[] v234 = new double[] { 2, 3, 4 };
	double[] v567 = new double[] { 5, 6, 7 };
	double[] result3;

	private Scale s2;
	private Scale s3;

	private Scale2D s2d;
	private Scale3D s3d;

	@Before
	public void setUp() throws Exception {
		s2 = new Scale( v23 );
		s2d = new Scale2D( v45 );

		result2 = new double[2];
		for( int i = 0; i < 2; i++ )
			result2[i] = v23[i]*v45[i];

		s3 = new Scale( v234 );
		s3d = new Scale3D( v567 );

		result3 = new double[3];
		for( int i = 0; i < 3; i++ )
			result3[i] = v234[i]*v567[i];
	}

	@Test
	public void testScaleConcatenate()
	{
		s2.set(v23);
		s2d.set(v45);
		s2.preConcatenate(s2d);
		assertArrayEquals("scale 2d preConcatenate", result2, s2.getScaleCopy(), 1e-9 );

		s2.set(v23); // reset
		s2.concatenate(s2d);
		assertArrayEquals("scale 2d concatenate", result2, s2.getScaleCopy(), 1e-9 );


		s3.set(v234);
		s3d.set(v567);
		s3.preConcatenate(s3d);
		assertArrayEquals("scale 3d preConcatenate", result3, s3.getScaleCopy(), 1e-9 );

		s3.set(v234); // reset
		s3.concatenate(s3d);
		assertArrayEquals("scale 3d concatenate", result3, s3.getScaleCopy(), 1e-9 );
	}

	@Test
	public void testScale2dConcatenate()
	{
		s2.set(v23);
		s2d.set(v45);

		// test Scale class
		s2d.preConcatenate(s2);
		assertArrayEquals("preConcatenate", result2, s2d.getScaleCopy(), 1e-9 );

		s2d.set(v45); // reset
		s2d.concatenate(s2);
		assertArrayEquals("concatenate", result2, s2d.getScaleCopy(), 1e-9 );
	}

	@Test
	public void testScale3dConcatenate()
	{
		s3.set(v234);
		s3d.set(v567);

		// test Scale class
		s3d.preConcatenate(s3);
		assertArrayEquals("preConcatenate", result3, s3d.getScaleCopy(), 1e-9 );

		s3d.set(v567); // reset
		s3d.concatenate(s3);
		assertArrayEquals("concatenate", result3, s3d.getScaleCopy(), 1e-9 );
	}
}
