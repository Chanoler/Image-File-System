import java.awt.Point;

public class Hilbert {
	
	/* All of the code in this file was stolen from Wikipedia and converted from C to Java.
	 * https://en.wikipedia.org/wiki/Hilbert_curve
	 */
	
	//convert d to (x,y)
	public static void d2xy(int n, int d, Point p) {
	    int rx, ry, s, t=d;
	    p.x = 0;
	    p.y = 0;
	    for (s=1; s<n; s*=2) {
	        rx = 1 & (t/2);
	        ry = 1 & (t ^ rx);
	        rot(s, p, rx, ry);
	        p.x += s * rx;
	        p.y += s * ry;
	        t /= 4;
	    }
	}

	//rotate/flip a quadrant appropriately
	public static void rot(int n, Point p, int rx, int ry) {
	    if (ry == 0) {
	        if (rx == 1) {
	            p.x = n-1 - p.x;
	            p.y = n-1 - p.y;
	        }

	        //Swap x and y
	        int t  = p.x;
	        p.x = p.y;
	        p.y = t;
	    }
	}
}
