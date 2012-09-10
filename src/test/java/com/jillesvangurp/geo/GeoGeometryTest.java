/**
 * Copyright (c) 2012, Jilles van Gurp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.jillesvangurp.geo;

import static com.jillesvangurp.geo.GeoGeometry.bboxContains;
import static com.jillesvangurp.geo.GeoGeometry.distance;
import static com.jillesvangurp.geo.GeoGeometry.polygonContains;
import static com.jillesvangurp.geo.GeoGeometry.roundToDecimals;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

@Test
public class GeoGeometryTest {
	double[][] polygon = new double[][] {
		{ -1, 1 },
		{ 2, 2 },
		{ 3, -1 },
		{ -2, -4 }
	};

	public void shouldCheckContainmentForPolygonCorrectly() {
		assertThat("origin should be in there", polygonContains(0, 0, polygon));
		assertThat("should be outside", !polygonContains(20, 20, polygon));
		assertThat("should be inside", polygonContains(0.5, 0.5, polygon));
		assertThat("should be inside", polygonContains(0.5, -0.5, polygon));
		assertThat("should be inside", polygonContains(-0.5, 0.5, polygon));
		assertThat("should be inside", polygonContains(-0.5, -0.5, polygon));
	}

	public void shouldBboxContainPoint() {
		double bbox[] = new double[] {-1,1,-2,2};

		assertThat("should be contained", bboxContains(bbox, 0, 0));
		assertThat("should be contained", bboxContains(bbox, 1, 2));
		assertThat("should be contained", bboxContains(bbox, -1, 2));
		assertThat("should be contained", bboxContains(bbox, 1, 2));
		assertThat("should be contained", bboxContains(bbox, -1, -2));
		assertThat("should not be contained", !bboxContains(bbox, -1.1, -2.1));
	}

	public void shouldGetCorrectBboxForPolygon() {
		double[] bbox = GeoGeometry.getBbox(polygon);
		assertThat("should be contained", bboxContains(bbox, 0, 0));
		for (double[] coordinate : polygon) {
			assertThat("should contain point", bboxContains(bbox, coordinate[0], coordinate[1]));
			assertThat("should not contain point", !bboxContains(bbox, 20*coordinate[0], 20*coordinate[1]));
		}
	}

	public void shouldRoundDouble() {
		assertThat(roundToDecimals(0.1234567891111112, 17), is(0.1234567891111112));
	}

	public void shouldCalculateDistance() {
		double d  = distance(52.530564,13.394964, 52.530564,13.410821);
		assertThat("should be a bit more than 1072m", d - 1072 >0 && d-1073 <0);
	}

	public void shouldTranslateCorrectly() {
	    double[] translated = GeoGeometry.translate(52.530564,13.394964, 1000, 3000);
	    double pythagorasDistance = Math.sqrt(pow(1000, 2)+pow(3000,2));
	    double distance = distance(new double[]{52.530564,13.394964}, translated);
        assertThat("distance should be correct for translated coordinate", abs(distance - pythagorasDistance) < 1.0);
	}

	public void shouldHaveDistanceOfRadiusForEachPoint() {
	    int radius = 5000;
        int segments = 500;
        double[][] polygon = GeoGeometry.circle2polygon(segments, 52.530564,13.394964, radius);
	    double d=0;
	    double[] last = null;

	    for(double[] point: polygon) {
	        if(last != null) {
	            d+=distance(last, point);
	        }
	        double distance = distance(new double[]{52.530564,13.394964}, point);
            assertThat("should have distance of radius to origin", abs(radius-distance) < 1);
            last=point;
	    }
	    // close the circle
        d+=distance(polygon[0],last);
        assertThat("circumference should be very close to length of the polygon", abs(d-2*Math.PI*radius) < 1);
	}
}
