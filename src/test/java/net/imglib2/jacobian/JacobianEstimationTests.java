package net.imglib2.jacobian;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.function.Predicate;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM;
import org.hamcrest.Matcher;
import org.junit.Test;

import jitk.spline.ThinPlateR2LogRSplineKernelTransform;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.DisplacementFieldTransform;
import net.imglib2.realtransform.ThinplateSplineTransform;
import net.imglib2.realtransform.inverse.AbstractDifferentiableRealTransform;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;
import net.imglib2.realtransform.inverse.RealTransformFiniteDerivatives;

public class JacobianEstimationTests
{

	public ThinPlateR2LogRSplineKernelTransform expandingBy4Dim2() {

		final int ndims = 2;
		final double[][] srcPts = new double[][]{
				{-1.0, 0.0, 1.0, 0.0}, // x
				{0.0, -1.0, 0.0, 1.0}}; // y

		final double[][] tgtPts = new double[][]{
				{-2.0, 0.0, 2.0, 0.0}, // x
				{0.0, -2.0, 0.0, 2.0}}; // y

		return new ThinPlateR2LogRSplineKernelTransform(ndims, srcPts, tgtPts);
	}

	public ThinPlateR2LogRSplineKernelTransform contractingBy4Dim2() {

		final int ndims = 2;
		final double[][] srcPts = new double[][]{
				{-1.0, 0.0, 1.0, 0.0}, // x
				{0.0, -1.0, 0.0, 1.0}}; // y

		final double[][] tgtPts = new double[][]{
				{-0.5, 0.0, 0.5, 0.0}, // x
				{0.0, -0.5, 0.0, 0.5}}; // y

		return new ThinPlateR2LogRSplineKernelTransform(ndims, srcPts, tgtPts);
	}

	public ThinPlateR2LogRSplineKernelTransform genPtListIdentity2d() {

		final int ndims = 2;
		final double[][] srcPts = new double[][]{
				{-1.0, 0.0, 1.0, 0.0, 0.0}, // x
				{0.0, -1.0, 0.0, 1.0, 0.0}}; // y

		final double[][] tgtPts = new double[][]{
				{-1.0, 0.0, 1.0, 0.0, 0.0}, // x
				{0.0, -1.0, 0.0, 1.0, 0.0}}; // y

		return new ThinPlateR2LogRSplineKernelTransform(ndims, srcPts, tgtPts);
	}

	public ThinPlateR2LogRSplineKernelTransform genPtListNonlinearSmall() {

		final int ndims = 2;
		final double[][] srcPts = new double[][]{
				{-1.0, 0.0, 1.0, 0.0, 0.0}, // x
				{0.0, -1.0, 0.0, 1.0, 0.0}}; // y

		final double[][] tgtPts = new double[][]{
				{-1.1, 0.0, 1.1, 0.0, 0.1}, // x
				{0.0, -1.1, 0.0, 1.1, -0.1}}; // y

		return new ThinPlateR2LogRSplineKernelTransform(ndims, srcPts, tgtPts);
	}

	public ThinPlateR2LogRSplineKernelTransform genPtListNonlinear2d() {

		final int ndims = 2;
		final double[][] srcPts = new double[][]{
				{-1.0, 0.0, 1.0, 0.0, 0.0}, // x
				{0.0, -1.0, 0.0, 1.0, 0.0}}; // y

		final double[][] tgtPts = new double[][]{
				{-1.0, 0.0, 1.0, 0.0, 0.5}, // x
				{0.0, -1.0, 0.0, 1.0, 0.5}}; // y

		return new ThinPlateR2LogRSplineKernelTransform(ndims, srcPts, tgtPts);
	}

	public RealTransformFiniteDerivatives buildConstantDfield() {

		return new RealTransformFiniteDerivatives(
				new DisplacementFieldTransform(
						new FunctionRealRandomAccessible<>(2,
								(p, v) -> {
									v.setPosition(2, 0);
									v.setPosition(3, 0);
								},
								() -> new RealPoint(2))));
	}

	private void helper2d(final double min, final double max, final Matcher<Double> matcher,
			final ThinPlateR2LogRSplineKernelTransform tps,
			final DifferentiableRealTransform tform) {

		final double[] p = new double[2];
		for (double x = min; x <= max; x++)
			for (double y = min; y <= max; y++) {
				p[0] = x;
				p[1] = y;

				final double[][] jTps = tps.jacobian(p);
				final DMatrixRMaj jacobianMtx = new DMatrixRMaj(jTps);
				final double jdet = CommonOps_DDRM.det(jacobianMtx);

				final DMatrixRMaj jacobianMtxFd = tform.jacobianMatrix(p);
				final double jdetFd = CommonOps_DDRM.det(jacobianMtxFd);

				final String s = String.format("jacobian determinant at (%.2f, %.2f)", x, y);
				assertThat(s, jdet, matcher);
			}
	}

	private void helper2d(final double min, final double max,
			final double trueDet, final double eps,
			final DifferentiableRealTransform tform) {

		final double[] p = new double[2];
		for (double x = min; x <= max; x++)
			for (double y = min; y <= max; y++) {
				p[0] = x;
				p[1] = y;

				final DMatrixRMaj jacobianMtx = tform.jacobianMatrix(p);
				final double jdet = CommonOps_DDRM.det(jacobianMtx);
				assertEquals(trueDet, jdet, eps);
			}
	}

	@Test
	public void testJacobianAffine() {

		final DifferentiableAffine id = new DifferentiableAffine(2, 1.0);
		final DifferentiableAffine s4 = new DifferentiableAffine(2, 4.0);
		final DifferentiableAffine s1o4 = new DifferentiableAffine(2, 0.25);

		final double eps = 1e-4;
		helper2d(-4, 4, 1, eps, id);
		helper2d(-4, 4, 4 * 4, eps, s4);
		helper2d(-4, 4, 0.25 * 0.25, eps, s1o4);
	}

	@Test
	public void testJacobianNonlinear() {

		final Predicate<Double> gt0 = x -> x > 0.0;
		// helper2d(-4, 4, new PredicateMatcher( gt0 ),
		// genPtListNonlinearSmall());

		final ThinPlateR2LogRSplineKernelTransform tpsraw = genPtListNonlinear2d();
		final RealTransformFiniteDerivatives tform = new RealTransformFiniteDerivatives(new ThinplateSplineTransform(tpsraw));

		// helper2d(-4, 4, new PredicateMatcher( gt0 ), tpsraw, tform);
	}
	
	@Test
	public void testJacobianSingular() {
		// tests that it is possible to get a singular jacobian matrix without
		// an exception being thrown

		final double EPS = 1e-9;
		final double[] x = new double[2];

		// transform taking every point to the origin
		DifferentiableLinearRealTransform tform = new DifferentiableLinearRealTransform(2, 0);
		final DMatrixRMaj jac = tform.jacobianMatrix(x);

		// jacobian is the zero matrix
		assertArrayEquals(new double[4], jac.getData(), EPS);
	}

	@Test
	public void testJacobianConstantDfield() {

		final RealTransformFiniteDerivatives xfm = buildConstantDfield();
		final double eps = 1e-4;
		helper2d(-4, 4, 1, eps, xfm);
	}

	private static class DifferentiableAffine extends AffineTransform implements DifferentiableRealTransform {

		final DMatrixRMaj jacobianMtx;

		public DifferentiableAffine(final int n, final double s) {

			super(n);
			jacobianMtx = new DMatrixRMaj(n, n);

			for (int i = 0; i < n; i++)
				this.set(s, i, i);

			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++)
					jacobianMtx.set(i, j, this.get(i, j));
		}

		@Override
		public void directionToward(final double[] displacement, final double[] x, final double[] y) {

			RealTransformFiniteDerivatives.directionToward(jacobian(x), displacement, x, y);
		}

		@Deprecated
		public AffineTransform jacobian(final double[] x) {

			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public DMatrixRMaj jacobianMatrix(final double[] x) {

			return jacobianMtx;
		}

		@Override
		public DifferentiableAffine copy() {

			return new DifferentiableAffine(numDimensions(), this.get(0, 0));
		}
	}
	
	private static class DifferentiableLinearRealTransform extends AbstractDifferentiableRealTransform
	{
		final DMatrixRMaj mtx;

		final DMatrixRMaj p, q;

		public DifferentiableLinearRealTransform(final int n, final double s) {

			super( n );
			mtx = new DMatrixRMaj(n, n);

			for (int i = 0; i < n; i++)
				mtx.set(i, i, s);

			p = new DMatrixRMaj(n,n);
			q = new DMatrixRMaj(n,n);
		}

		@Deprecated
		public AffineTransform jacobian(final double[] x) {

			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public DMatrixRMaj jacobianMatrix(final double[] x) {

			return mtx;
		}

		@Override
		public DifferentiableAffine copy() {

			return new DifferentiableAffine(super.n, mtx.get(0, 0));
		}

		@Override
		public int numSourceDimensions() {

			return super.n;
		}

		@Override
		public int numTargetDimensions() {

			return super.n;
		}

		@Override
		public void apply(double[] source, double[] target) {

			MatrixVectorMult_DDRM.mult(mtx, DMatrixRMaj.wrap(super.n, 1, source), DMatrixRMaj.wrap(super.n, 1, target));
		}

		@Override
		public void apply(RealLocalizable source, RealPositionable target) {

			for (int i = 0; i < source.numDimensions(); i++)
				p.set(i, source.getDoublePosition(i));

			MatrixVectorMult_DDRM.mult(mtx, p, q);

			for (int i = 0; i < target.numDimensions(); i++)
				target.setPosition(q.get(i), i);

		}
	}	

}
