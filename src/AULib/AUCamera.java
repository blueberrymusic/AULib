/**
 * Andrew's Utilities
 * A collection of utilities for Processing.
 * http://imaginary-institute.com/resources/AULibrary/AULibrary.php
 *
 * Copyright (c) 2014 Andrew Glassner Andrew Glassner http://glassner.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      Andrew Glassner http://glassner.com
 * @modified    10/29/2014
 * @version     1.0.0 (1)
 */

package AULib;


import processing.core.*;

/**
 * 
 * @example AUCamera_demo 
 * 
 * (the tag @example followed by the name of an example included in folder 'examples' will
 * automatically include the example in the javadoc.)
 *
 */

/*************************************************
* FIELDS
*************************************************/	

public class AUCamera {
	
	// theSketch is a reference to the parent sketch
	PApplet theSketch;

	public static final int SHUTTER_OPEN = 0;				// always open
	public static final int SHUTTER_UNIFORM = 1;		 // uniformly turns transparent, then opaque
	public static final int SHUTTER_BLADE_RIGHT = 2; // blade moving right
	public static final int SHUTTER_BLADE_LEFT = 3;	// blade moving left
	public static final int SHUTTER_BLADE_DOWN = 4;	// blade moving down
	public static final int SHUTTER_BLADE_UP = 5;		// blade moving up
	public static final int SHUTTER_IRIS = 6;				// circular iris
	
	AUMultiField exposure;
	int numFrames, numExposures;
	boolean autoNormalize255, autoSave, autoExit;
	boolean timeWrap;
	String saveFormat, savePath;
	float rampTime, blendTime;
	int shutterType;
	int frameNumber, exposureNumber;
	AUField shutter;
	PGraphics shutterPG, savePG;
	int preRoll;
	
	public AUCamera(PApplet _theSketch, int _numFrames, int _numExposures, boolean _saveFrames) {
		if (_theSketch == null) {
			AULib.reportError("AUCamera", "AUCamera", "theSketch is null", "");
		}
		theSketch = _theSketch;
		exposure = new AUMultiField(theSketch, 3, theSketch.width, theSketch.height);
		exposure.flatten((float)0.);
		numFrames = Math.max(1, _numFrames);
		numExposures = _saveFrames ? Math.max(1, _numExposures): 1;
		autoNormalize255 = false;
		autoSave = _saveFrames;
		autoExit = true;
		saveFormat = "png";
		savePath = "pngFrames";
		rampTime = (float).2;
		blendTime = (float).25;
		shutterType = SHUTTER_OPEN;
		frameNumber = 0;
		exposureNumber = 0;
		shutter = new AUField(theSketch, theSketch.width, theSketch.height);
		shutterPG = theSketch.createGraphics(theSketch.width, theSketch.height);
		savePG = theSketch.createGraphics(theSketch.width, theSketch.height);
		preRoll = 0;
		timeWrap = true;
	}
	
	// individual value setters
	public void setNumFrames(int _numFrames)		{ numFrames = _numFrames; }
	public void setNumExposures(int _numExposures)  { numExposures = _numExposures; }
	public void setAutoNormalize(boolean _autoNormalize255) { autoNormalize255 = _autoNormalize255; }
	public void setAutoSave(boolean _autoSave)		{ autoSave = _autoSave; }
	public void setAutoExit(boolean _autoExit)		{ autoExit = _autoExit; }
	public void setSaveFormat(String _saveFormat)	{ saveFormat = _saveFormat; }
	public void setSavePath(String _savePath)		{ savePath = _savePath; }
	public void setRampTime(float _rampTime)		{ rampTime = _rampTime; }
	public void setBlendtime(float _blendTime)		{ blendTime = _blendTime; }
	public void setShutterType(int _shutterType)	{ shutterType = _shutterType; }
	public void setTimeWrap(boolean _timeWrap)      { timeWrap = _timeWrap; }
	public void setPreRoll(int _preRoll)            { 
		if (_preRoll < 0) {
			AULib.reportError("AUCamera", "setPreRoll", "preRoll is less than 0, using absolute value instead.", "");
			_preRoll = Math.abs(_preRoll);
		}
		preRoll = _preRoll;
		int preRollFrames = (int)Math.floor(preRoll/numExposures);
		int preRollExposures = preRoll - (preRollFrames * numExposures);
		frameNumber = numFrames - (preRollFrames+1);
		exposureNumber = numExposures - (preRoll - (preRollFrames * numExposures));
	}
	
	public float getTime() {
		float t = ((frameNumber * numExposures) + exposureNumber)/(1.f*numExposures*numFrames);
		if (timeWrap) t = t % 1.0f;
		return t;
	}
	
	public float getFrameTime() {
		float t = frameNumber + AUMisc.jnorm(exposureNumber, 0, numExposures);
		return t;
	}
	
	public void expose() {
		doExposure(true);
	}
	
	public void expose(boolean _doExpose) {
		doExposure(_doExpose);
	}

	void doExposure(boolean doSaveFile) {
		// add in this exposure with proper weight
		float v = getExposureValue();
		theSketch.loadPixels();
		switch (shutterType) {
			default:
			case SHUTTER_OPEN:
				exposeOpenShutter();
				break;
			case SHUTTER_UNIFORM:
				exposeShutterUniform(v);
				break;
			case SHUTTER_BLADE_RIGHT:
			case SHUTTER_BLADE_LEFT:
			case SHUTTER_BLADE_DOWN:
			case SHUTTER_BLADE_UP:
				exposeShutterBlade(v);
				break;
			case SHUTTER_IRIS:
				buildShutter();
				exposeWithShutter(shutter);
				break;
		}
		checkToSave(doSaveFile);
	}
	
	void exposeOpenShutter() {
		for (int y=0; y<theSketch.height; y++) {
			for (int x=0; x<theSketch.width; x++) {
				int c = theSketch.pixels[(y*theSketch.width)+x];
				exposure.fields[0].z[y][x] += AUMisc.jred(c);
				exposure.fields[1].z[y][x] += AUMisc.jgreen(c);
				exposure.fields[2].z[y][x] += AUMisc.jblue(c);
			}
		}
	}
	
	void exposeShutterUniform(float v) {
		for (int y=0; y<theSketch.height; y++) {
			for (int x=0; x<theSketch.width; x++) {
				int c = theSketch.pixels[(y*theSketch.width)+x];
				exposure.fields[0].z[y][x] += v * AUMisc.jred(c);
				exposure.fields[1].z[y][x] += v * AUMisc.jgreen(c);
				exposure.fields[2].z[y][x] += v * AUMisc.jblue(c);
			}
		}
	}
	
	void exposeShutterBlade(float v) {
		for (int y=0; y<theSketch.height; y++) {
			for (int x=0; x<theSketch.width; x++) {
				boolean addIn = false;
				switch (shutterType) {
					default:
					case SHUTTER_BLADE_RIGHT:
						addIn = x < theSketch.width*v;
						break;
					case SHUTTER_BLADE_LEFT:
						addIn = x > (theSketch.width*(1-v));
						break;
					case SHUTTER_BLADE_DOWN:
						addIn = y < theSketch.height*v;
						break;
					case SHUTTER_BLADE_UP:
						addIn = y > (theSketch.height*(1-v));
						break;
				}
				if (addIn) {
					int c = theSketch.pixels[(y*theSketch.width)+x];
					exposure.fields[0].z[y][x] += AUMisc.jred(c);
					exposure.fields[1].z[y][x] += AUMisc.jgreen(c);
					exposure.fields[2].z[y][x] += AUMisc.jblue(c);
				}
			}
		}
	}
	
	void exposeWithShutter(AUField thisShutter) {
		for (int y=0; y<theSketch.height; y++) {
			for (int x=0; x<theSketch.width; x++) {
				int c = theSketch.pixels[(y*theSketch.width)+x];
				float w = thisShutter.z[y][x];
				exposure.fields[0].z[y][x] += w * AUMisc.jred(c);
				exposure.fields[1].z[y][x] += w * AUMisc.jgreen(c);
				exposure.fields[2].z[y][x] += w * AUMisc.jblue(c);
			}
		}
	}
	
	void checkToSave(boolean doSaveFile) {
		if (preRoll > 0) {
			if (++exposureNumber >= numExposures) {
				++frameNumber;
				exposureNumber = 0;
			}
			if (--preRoll == 0) {
				frameNumber = 0;
			}
			return;
		}
		if ((++exposureNumber >= numExposures) && autoSave) {
			if (numExposures < 1) numExposures = 1;
			if (autoNormalize255) {
				exposure.setRangeTogether(0, 255, 3); 
			} else {	// just average
				exposure.mul((float)(1./numExposures));
			}
			if (doSaveFile) {
				saveNow();
			}
			exposureNumber = 0; 
			exposure.flatten(0);
			++frameNumber;
			if ((frameNumber >= numFrames) && autoExit) {
				if (doSaveFile) exitNow();
			}
		}
	}
	
	// I now have special code for the OPEN, UNIFORM, and BLADE shutters,
	// but I'll leave the masks here as examples of the technique
	void buildShutter() {
		float v = getExposureValue();
		shutterPG.beginDraw();
		shutterPG.background(0);
		shutterPG.fill(255);
		switch (shutterType) {
			default:
			case SHUTTER_OPEN:
				shutterPG.background(255);
				break;
			case SHUTTER_UNIFORM:
				shutterPG.background(255 * v);
				break;
			case SHUTTER_BLADE_RIGHT:
				shutterPG.rect(0, 0, theSketch.width*v, theSketch.height);
				break;
			case SHUTTER_BLADE_LEFT:
				shutterPG.rect(theSketch.width*(1-v), 0, theSketch.width*v, theSketch.height);
				break;
			case SHUTTER_BLADE_DOWN:
				shutterPG.rect(0, 0, theSketch.width, theSketch.height*v); 
				break;
			case SHUTTER_BLADE_UP:
				shutterPG.rect(0, theSketch.height*(1-v), theSketch.width, theSketch.height*v);
				break;
			case SHUTTER_IRIS:
				float maxr = AUMisc.jmag(theSketch.width/2.f, theSketch.height/2.f);
				shutterPG.ellipse(theSketch.width/2.f, theSketch.height/2.f, 2*v*maxr, 2*v*maxr);
				break;
		}
		shutterPG.endDraw();
		shutter.fromPixels(AUField.FIELD_LUM, shutterPG);
		shutter.mul(1/255.f);
	}
	
	// given where we are in the exposure, find how much to weight this image's contribution
	float getExposureValue() {
		float r = rampTime;
		float w = blendTime * rampTime;
		float a = AUMisc.jnorm(exposureNumber, 0, numExposures);
		a = AUMisc.jconstrain(a, 0f, 1f);
		float rampIn = AUMisc.jnorm(a, 0, r+w);
		if (a < r-w) {
			return rampIn;
		}
		if (a < r+w) {	 //blend to flat section
			float t = AUMisc.jnorm(a, r-w, r+w);
			return AUMisc.jlerp(rampIn, 1, AUMisc.jease(t));
		}
		if (a < 1-(r+w)) {
			return 1f;		// flat middle
		}
		float rampOut = 1-AUMisc.jnorm(a, 1-(r+w), 1);
		if (a < r+w) {	 //blend out of flat section
			float t = AUMisc.jnorm(a, 1-(r+w), 1-(r-w));
			return AUMisc.jlerp(1, rampOut, AUMisc.jease(t));
		}
		return rampOut;
	}
	
	// save the current exposure to a file
	void saveNow() {
		exposure.RGBtoPixels(0, 0);
		String path = savePath + "/frame" + PApplet.nf(frameNumber, 5) + "." + saveFormat;
		theSketch.save(path);
	}
	
	// this doesn't exit immediately, but instead the next time draw() finishes
	void exitNow() {
		theSketch.exit();
	}
		
}
