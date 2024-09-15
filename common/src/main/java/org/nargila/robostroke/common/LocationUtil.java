/*
 * Copyright (c) 2024 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.robostroke.common;

public class LocationUtil {
	private static final double R = 6371; // km
	
	/**
	 * calc distance between two points, using spherical earth (ignoring ellipsoidal effects) calculation. 
	 * @see http://www.movable-type.co.uk/scripts/latlong.html
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static final double distSphericalEarth(double lat1, double lon1, double lat2, double lon2) {
	
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1); 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		
		return d * 1000;
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
	/* Vincenty Inverse Solution of Geodesics on the Ellipsoid (c) Chris Veness 2002-2010             */
	/*                                                                                                */
	/* from: Vincenty inverse formula - T Vincenty, "Direct and Inverse Solutions of Geodesics on the */
	/*       Ellipsoid with application of nested equations", Survey Review, vol XXII no 176, 1975    */
	/*       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf                                             */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

	/**
	 * Calculates geodetic distance between two points specified by latitude/longitude using 
	 * Vincenty inverse formula for ellipsoids
	 *
	 * @param   {Number} lat1, lon1: first point in decimal degrees
	 * @param   {Number} lat2, lon2: second point in decimal degrees
	 * @returns (Number} distance in metres between points
	 */
	public static final double distVincenty(double lat1, double lon1, double lat2, double lon2) {
		double a = 6378137, b = 6356752.314245,  f = 1/298.257223563;  // WGS-84 ellipsoid params
		double L = Math.toRadians(lon2-lon1);
		double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

		double lambda = L, lambdaP, iterLimit = 100;
		
		double cosSqAlpha, sinSigma,cos2SigmaM, sigma, cosSigma;
		do {
			double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + 
					(cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
			
			if (sinSigma==0) return 0;  // co-incident points
			
			cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha*sinAlpha;
			cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
			if (Double.isNaN(cos2SigmaM)) cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0 (§6)
			double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1-C) * f * sinAlpha *
			(sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
		} while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

		if (iterLimit==0) return Double.NaN;  // formula failed to converge

		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
				B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		double s = b*A*(sigma-deltaSigma);

		return s;
	}
	
}
