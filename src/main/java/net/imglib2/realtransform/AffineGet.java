/*
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

package net.imglib2.realtransform;

import net.imglib2.EuclideanSpace;
import net.imglib2.RealLocalizable;

/**
 * An <em>n</em>-dimensional affine transformation whose <em>n</em>&times;(
 * <em>n</em>+1) affine transformation matrix can be accessed via row and column
 * index.
 * 
 * @author Stephan Saalfeld
 */
public interface AffineGet extends InvertibleRealTransform, EuclideanSpace
{
	/**
	 * Get a field of the <em>n</em>&times;(<em>n</em>+1) affine transformation
	 * matrix.
	 * 
	 * @param row
	 *            the row index
	 * @param column
	 *            the column index
	 * @return the value
	 */
	public double get(final int row, final int column);

	/**
	 * Get a copy of the <em>n</em>&times;(<em>n</em>+1) affine transformation
	 * matrix as a row packed array similar to Jama.
	 * 
	 * @return the array of values
	 */
	public double[] getRowPackedCopy();

	/**
	 * Get the constant partial differential vector for dimension d.
	 * 
	 * @param d
	 *            the dimension
	 * @return the partial differential vector
	 */
	public RealLocalizable d(int d);

	@Override
	AffineGet inverse();

	@Override
	AffineGet copy();
}
