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
