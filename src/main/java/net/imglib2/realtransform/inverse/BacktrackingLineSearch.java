package net.imglib2.realtransform.inverse;

import java.util.Arrays;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.imglib2.realtransform.RealTransform;

/**
 * A generic backtracking line search to iteratively estimate the inverse of a {@link RealTransform}.
 * This should never be used when a closed-form inverse is available.
 * 
 * @author John Bogovic
 *
 */
public class BacktrackingLineSearch
{
	double[] target;	// target point
	double[] x;			
	double[] y;
	
	double fx;	
	double[] x_ap;
	double[] y_ap;
	double[] dir;
	
	double currentSquaredError;
	
	private final int nd;
	private DifferentiableRealTransform fwdXfm;
	
	// optimization parameters
	private int maxIters = 1000;	// maximum iterations
	private double eps = 0.1;		// maximum error
	private double epsSquared = eps*eps;

	// line search parameters
	private double c = 0.5;
	private double beta = 0.5;
	private double initStepSize = 10;
	private int lineSearchMaxTries = 16;		
	
	protected static Logger logger = LogManager.getLogger( 
			BacktrackingLineSearch.class.getName() );

	public BacktrackingLineSearch( final int nd )
	{
		this.nd = nd;
		y = new double[ nd ];
		x_ap = new double[ nd ];
		y_ap = new double[ nd ];
	}

	public BacktrackingLineSearch( final DifferentiableRealTransform fwdXfm )
	{
		this.fwdXfm = fwdXfm;
		this.nd = fwdXfm.numSourceDimensions();
		y = new double[ nd ];
		x_ap = new double[ nd ];
		y_ap = new double[ nd ];
	}

	public void setForwardTransform( final DifferentiableRealTransform fwdXfm )
	{
		assert( fwdXfm.numSourceDimensions() == nd );
		this.fwdXfm = fwdXfm;
	}
	
	public void setC( final double c )
	{
		this.c = c;
	}

	public void setBeta( final double beta )
	{
		this.beta = beta;
	}

	public void setInitStep( final double initStepSize )
	{
		this.initStepSize = initStepSize;
	}

	public void setMaxIterations( final int maxIters )
	{
		this.maxIters = maxIters;
	}

	public void setMaxLineSearchTries( final int lineSearchMaxTries )
	{
		this.lineSearchMaxTries = lineSearchMaxTries;
	}

	public void setEpsilon( final double eps )
	{
		this.eps = eps;
		epsSquared = eps*eps;
	}

	public void setEstimate( final double[] est )
	{
		this.x = est;
		fwdXfm.apply( x, y );
		fx = squaredError( y );
	}

	public void setTarget( final double[] tgt )
	{
		this.target = tgt;
	}

	public void setDirection( final double[] dir )
	{
		this.dir = dir;
//		logger.trace( "    dir     : " + Arrays.toString( dir ));

		double mag = dirMag();
		for ( int i = 0; i < nd; i++ )
			dir[ i ] = dir[ i ] / mag;

//		logger.trace( "    dir norm: " + Arrays.toString( dir ));
	}
	
	/**
	 * Compute the squared error between this estimate and the target.
	 * 
	 * @param estimate the current estimate
	 * @return the squared error
	 */
	public double squaredError( double[] estimate )
	{
		double squaredError = 0;
		for ( int d = 0; d < nd; d++ )
		{
			squaredError += ( estimate[ d ] - target[ d ]) * ( estimate[ d ] - target[ d ]);
		}
		return squaredError;
	}

	/**
	 * Compute the squared error between the destination of the input source point
	 * and the target.
	 * 
	 * @param source the current source
	 * @return the squared error
	 */
	public double squaredErrorAt( double[] source )
	{
		double[] srcXfm = new double[ nd ];
		fwdXfm.apply( source, srcXfm );
		double squaredError = 0;
		for ( int d = 0; d < nd; d++ )
		{
			squaredError += ( srcXfm[ d ] - target[ d ]) * ( srcXfm[ d ] - target[ d ]);
		}
		return squaredError;
	}

	public double dirMag()
	{
		double mag = 0;
		for ( int i = 0; i < nd; i++ )
			mag += dir[ i ] * dir[ i ];
		
		return Math.sqrt( mag );
	}
	
	/**
	 * Perform backtracking line search.
	 * 
	 * @param c the armijoCondition parameter
	 * @param beta the fraction to multiply the step size at each iteration ( < 1 )
	 * @param maxtries max number of tries
	 * @param t0 initial step size
	 * @return the step size
	 */
	public double backtrackingLineSearch( double c, double beta, int maxtries, double t0 )
	{
		double t = t0; // step size

		int k = 0;
		// boolean success = false;
		while ( k < maxtries )
		{
			if ( armijoCondition( c, t ) )
			{
				// success = true;
				break;
			}
			else
				t *= beta;

			k++;
		}

		return t;
	}
	
	private double sumSquaredErrorsDeriv( double[] y, double[] x )
	{
		double errDeriv = 0.0;
		for ( int i = 0; i < nd; i++ )
			errDeriv += ( y[ i ] - x[ i ] ) * ( y[ i ] - x[ i ] );

		return 2 * errDeriv;
	}
	
	private boolean armijoCondition( double c, double t )
	{
		for ( int i = 0; i < x.length; i++ )
			x_ap[ i ] = x[ i ] + t * dir[ i ];
		
		fwdXfm.apply( x_ap, y_ap );
		double fx_ap = squaredError( y_ap );
		
		//double m = sumSquaredErrorsDeriv( this.target, y_ap ); // * descentDirectionMag.get( 0 );
		double m = 1;

//		logger.trace( "   x   : " + Arrays.toString( x ));
//		logger.trace( "   x_ap: " + Arrays.toString( x_ap ));
//		logger.trace( "   y   : " + Arrays.toString( y ));
//		logger.trace( "   y_ap: " + Arrays.toString( y_ap ));
//		logger.trace( "   fx      : " + fx );
//		logger.trace( "   fx_ap   : " + fx_ap );
//		logger.trace( "   fx + ctm: " + ( fx - (c*t*m)) ) ;

//		System.out.println( "   x   : " + Arrays.toString( x ));
//		System.out.println( "   x_ap: " + Arrays.toString( x_ap ));
//		System.out.println( "   y   : " + Arrays.toString( y ));
//		System.out.println( "   y_ap: " + Arrays.toString( y_ap ));
//		System.out.println( "   fx      : " + fx );
//		System.out.println( "   fx_ap   : " + fx_ap );
//		System.out.println( "   fx + ctm: " + ( fx - (c*t*m)) ) ;

		if ( fx_ap < fx - c * t * m )
		{
			currentSquaredError = fx_ap;
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Find the source point that, when pushed through the forward transform,
	 * results in the given destination point.  Iteratively estimates the result
	 * with backtracking line search.
	 * <p>
	 * Terminates either when a maximum iteration count has been reached, or the error
	 * falls below a specified threshold.
	 * 
	 * @param source the source point that will be overwritten
	 * @param destination the destination point
	 * @return error of inverse
	 */
	public double iterativeInverse( final double[] source, final double[] destination )
	{
//		System.out.println( "start source: " + Arrays.toString( source ) );
//		System.out.println( " " );

		// keep track of target globally
		this.target = destination;
		
		// initialize at destination
//		System.arraycopy( destination, 0, source, 0, source.length );

		double stepSize = initStepSize;		
		
		// temporary destination
		double[] tmp = new double[ nd ];
		
		// temporary src
		double[] tmpsrc = new double[ nd ];
				
		// the displacement
		double[] displacement = new double[ nd ];
		
		double olderror = Double.MAX_VALUE;
		double currentError = 0;

		int i = 0;
		while( i < maxIters )
		{
			stepSize = initStepSize;
//			System.out.println( "i: " + i );

			setEstimate( source );

			fwdXfm.directionToward( displacement, source, destination );
//			System.out.println( "displacement: " + Arrays.toString( displacement ));

			setDirection( displacement );
			stepSize = backtrackingLineSearch( c, beta, lineSearchMaxTries, stepSize );

//			System.out.println( "stepSize: " + stepSize );

			for ( int d = 0; d < nd; d++ )
			{
				tmpsrc[ d ] = source[ d ] + stepSize * displacement[ d ]; 
			}
//			System.out.println( "tmpsrc: " + Arrays.toString( tmpsrc ) );

			fwdXfm.apply( tmpsrc, tmp );
			currentError = squaredError( tmp );
//			System.out.println( "tmpdst: " + Arrays.toString( tmp ) );

//			System.out.println( "squared err: " + currentError );
			if( currentError > olderror )
			{
//				System.out.println( "breaking early");
				// make sure to return error (not squared)
				return Math.sqrt( currentError );
			}
			else
			{
				System.arraycopy( tmpsrc, 0, source, 0, source.length );
			}
			olderror = currentError;

//			System.out.println( "source: " + Arrays.toString( source ) );
//			System.out.println( " " );

			if( currentError < epsSquared )
			{
//				System.out.println( "breaking early");
				break;
			}
			i++;
		}

		currentSquaredError = currentError;

		// make sure to return error (not squared)		
		return Math.sqrt( currentError );
	}

	public double getLastSquaredError()
	{
		return currentSquaredError;
	}

	public double getLastError()
	{
		return Math.sqrt( currentSquaredError );
	}
}
