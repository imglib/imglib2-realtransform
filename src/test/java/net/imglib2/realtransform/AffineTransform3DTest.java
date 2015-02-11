/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
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

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Stephan Saalfeld <saalfelds@janelia.hhmi.org>
 *
 */
public class AffineTransform3DTest
{

	protected Random rnd = new Random( 0 );
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		 rnd.setSeed( 0 );
	}

	@Test
	public void testRotate()
	{
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set(
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble() );
		
		final AffineTransform3D dR = new AffineTransform3D();
		
		for ( int i = 0; i < 100; ++i )
		{
			final double d = rnd.nextDouble() * 8 * Math.PI - 4 * Math.PI;
			final double dcos = Math.cos( d );
			final double dsin = Math.sin( d );
			
			final int axis = rnd.nextInt( 3 );
		
			switch ( axis )
			{
			case 0:
				dR.set(
						1.0f, 0.0f, 0.0f, 0.0f,
						0.0f, dcos, -dsin, 0.0f,
						0.0f, dsin, dcos, 0.0f );
				break;
			case 1:
				dR.set(
						dcos, 0.0f, dsin, 0.0f,
						0.0f, 1.0f, 0.0f, 0.0f,
						-dsin, 0.0f, dcos, 0.0f );
				break;
			default:
				dR.set(
						dcos, -dsin, 0.0f, 0.0f,
						dsin, dcos, 0.0f, 0.0f,
						0.0f, 0.0f, 1.0f, 0.0f );
				break;
			}

			dR.concatenate( affine );
			affine.rotate( axis, d );
			
			assertArrayEquals( dR.getRowPackedCopy(), affine.getRowPackedCopy(), 0.001 );
		}
	}
	
	@Test
	public void testScale()
	{
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set(
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(),
				rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble() );
		
		final AffineTransform3D dR = new AffineTransform3D();
		
		for ( int i = 0; i < 100; ++i )
		{
			final double s = rnd.nextDouble() * 20 - 10;
			
			dR.set(
					s, 0.0, 0.0, 0.0,
					0.0, s, 0.0, 0.0,
					0.0, 0.0, s, 0.0 );
			
			dR.concatenate( affine );
			affine.scale( s );
			
			assertArrayEquals( dR.getRowPackedCopy(), affine.getRowPackedCopy(), 0.001 );
		}
	}

}
