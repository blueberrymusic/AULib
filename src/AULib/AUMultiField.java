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
 * @example AUMultiField_demo 
 * @example AUMultiField_WithMask_demo 
 * @example AUMultiField_Compositing_demo
 * 
 * (the tag @example followed by the name of an example included in folder 'examples' will
 * automatically include the example in the javadoc.)
 *
 */

/*************************************************
* MULTIFIELDS
*************************************************/	

public class AUMultiField {
	
	// theSketch is a reference to the parent sketch
	PApplet theSketch;
	
	public AUField[] fields;
	public int w, h;
	
	/**********************
	 * Constructors
	 *********************/

	public AUMultiField(PApplet _theSketch, int _numFields, int _wid, int _hgt) {
		if (_theSketch == null) {
			AULib.reportError("AUMultiField", "AUMultiField", "theSketch is null", "");
		}
		theSketch = _theSketch;
		w = Math.max(1, _wid);
		h = Math.max(1, _hgt);
		_numFields = Math.max(1, _numFields);
		fields = new AUField[_numFields];
		for (int i=0; i<_numFields; i++) {
			 fields[i] = new AUField(theSketch, _wid, _hgt);
		}
	}
	
	/**********************
	 * Flatten
	 *********************/
	
	public void flattenRGBA(float _fr, float _fg, float _fb, float _fa) {
		if (fields.length < 4) {
			AULib.reportError("AUMultiField", "flattenRGBA", "less than 4 fields avialable", "fields.length="+Float.toString(fields.length));
			return;
		}
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				fields[0].z[y][x] = _fr;
				fields[1].z[y][x] = _fg;
				fields[2].z[y][x] = _fb;
				fields[3].z[y][x] = _fa;
			}
		}
	}
	
	public void flattenRGB(float _fr, float _fg, float _fb) {
		if (fields.length < 3) {
			AULib.reportError("AUMultiField", "flattenRGB", "less than 3 fields avialable", "fields.length="+Float.toString(fields.length));
			return;
		}
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				fields[0].z[y][x] = _fr;
				fields[1].z[y][x] = _fg;
				fields[2].z[y][x] = _fb;
			}
		}
	}

	public void flatten(float _v) {
		for (int f=0; f<fields.length; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					fields[f].z[y][x] = _v;
				}
			}
		}
	}
	
	public void flattenField(int _fieldNumber, float _v) {
		if (_fieldNumber >= fields.length) {
			AULib.reportError("AUMultiField", "flattenField", "field "+_fieldNumber+" is not present", "fields.length="+Float.toString(fields.length));
			return;
		}
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				fields[_fieldNumber].z[y][x] = _v;
			}
		}
	}
	
	/**********************
	 * from pixels
	 *********************/

	void loadFromPixels(boolean _saveAlpha, PGraphics _pg) {
		int fieldsNeeded = 3;
		if (_saveAlpha) fieldsNeeded = 4;
		if (fields.length < fieldsNeeded) {
			AULib.reportError("AUMultiField", "RGBAfromPixels", "there are not at least "+fieldsNeeded+" fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		int wid = 0;
		int hgt = 0;
		int[] thesePixels;
		if (_pg != null) {
			wid = _pg.width;
			hgt = _pg.height;
			_pg.loadPixels();
			thesePixels = _pg.pixels;
		} else {
			wid = theSketch.width;
			hgt = theSketch.height;
			theSketch.loadPixels();
			thesePixels = theSketch.pixels;
		}
		for (int y=0; y<h; y++) {
			if (y >= hgt) continue;
			for (int x=0; x<w; x++) {
				if (x >= wid) continue;
				int c = thesePixels[(y*wid)+x];
				/*
				int c = 0;
				if (_pg != null) c = _pg.pixels[(y*wid)+x];
				else c = theSketch.pixels[(y*wid)+x];
				*/
				fields[0].z[y][x] = (c>>16) & 0xFF;//AUMisc.jred(c);
				fields[1].z[y][x] = (c>>8) & 0xFF;//AUMisc.jgreen(c);				
				fields[2].z[y][x] = c & 0xFF;//AUMisc.jblue(c);
				if (_saveAlpha) fields[3].z[y][x] = (c>>24) & 0xFF;//AUMisc.jalpha(c);
			}
		}
	}
	
	public void RGBfromPixels() {
		loadFromPixels(false, null);
	}
	
	public void RGBAfromPixels() {
		loadFromPixels(true, null);
	}
	
	public void RGBfromPixels(PGraphics _pg) {
		loadFromPixels(false, _pg);
	}
	
	public void RGBAfromPixels(PGraphics _pg) {
		loadFromPixels(true, _pg);
	}
	
	/**********************
	 * Add constants
	 *********************/

	public void RGBAadd(float _fr, float _fg, float _fb, float _fa) {
		if (fields.length < 4) {
			AULib.reportError("AUMultiField", "RGBAadd", "there are not at least 4 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[0].add(_fr);
		fields[1].add(_fg);
		fields[2].add(_fb);
		fields[3].add(_fa);
	}
	
	public void RGBadd(float _fr, float _fg, float _fb) {
		if (fields.length < 3) {
			AULib.reportError("AUMultiField", "RGBadd", "there are not at least 3 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[0].add(_fr);
		fields[1].add(_fg);
		fields[2].add(_fb);
	}
	
	public void add(float _a) {
		for (int f=0; f<fields.length; f++) {
			fields[f].add(_a);
		}
	}
	
	public void addField(int _fieldNumber, float _a) {
		if (_fieldNumber >= fields.length) {
			AULib.reportError("AUMultiField", "addField", "field "+_fieldNumber+" is not present", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[_fieldNumber].add(_a);
	}
	
	/**********************
	 * Multiply constants
	 *********************/

	public void RGBAmul(float _mr, float _mg, float _mb, float _ma) {
		if (fields.length < 4) {
			AULib.reportError("AUMultiField", "RGBAmul", "there are not at least 4 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[0].mul(_mr);
		fields[1].mul(_mg);
		fields[2].mul(_mb);
		fields[3].mul(_ma);
	}

	public void RGBmul(float _mr, float _mg, float _mb) {
		if (fields.length < 3) {
			AULib.reportError("AUMultiField", "RGBmul", "there are not at least 3 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[0].mul(_mr);
		fields[1].mul(_mg);
		fields[2].mul(_mb);
	}
	
	public void mul(float _m) {
		for (int f=0; f<fields.length; f++) {
			fields[f].mul(_m);
		}
	}
	
	public void mulField(int _fieldNumber, float _m) {
		if (_fieldNumber >= fields.length) {
			AULib.reportError("AUMultiField", "mulField", "field "+_fieldNumber+" is not present", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[_fieldNumber].mul(_m);
	}
	
	/**********************
	 * Add and Multiply MultiFields
	 *********************/

	public void add(AUMultiField _mf) {
		if ((_mf.w != w) || (_mf.h != h)) {
			AULib.reportError("AUMultiField", "add", "the two fields do not have the same size ", "");
			return;
		}
		int numFields = Math.min(_mf.fields.length, fields.length);
		for (int f=0; f<numFields; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					fields[f].z[y][x] += _mf.fields[f].z[y][x];
				}
			}
		}
	}
	
	public void mul(AUMultiField _mf) {
		if ((_mf.w != w) || (_mf.h != h)) {
			AULib.reportError("AUMultiField", "mul", "the two fields do not have the same size", "");
			return;
		}
		int numFields = Math.min(_mf.fields.length, fields.length);
		for (int f=0; f<numFields; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					fields[f].z[y][x] *= _mf.fields[f].z[y][x];
				}
			}
		}
	}
	
	/**********************
	 * Duplicate, copy, swap
	 *********************/
	
	public AUMultiField dupe() {
		AUMultiField mf = new AUMultiField(theSketch, fields.length, w, h);
		for (int f=0; f<fields.length; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					mf.fields[f].z[y][x] = fields[f].z[y][x];
				}
			}
		}
		return mf;
	}
	
	public void copy(AUMultiField _dst) {
		if ((_dst.w != w) || (_dst.h != h) || (_dst.fields.length != fields.length)) {
			AULib.reportError("AUMultiField", "copy", "the two fields do not have the same size or depth", "");
			return;
		}
		for (int f=0; f<fields.length; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					_dst.fields[f].z[y][x] = fields[f].z[y][x];
				}
			}
		}
	}
	
	public void copyFieldToField(int from, int to) {
		if ((fields.length <= from) || (fields.length <= to)) {
			AULib.reportError("AUMultiField", "copyField", "either to or from is larger than the number of fields available", "");
			return;
		}

		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				fields[to].z[y][x] = fields[from].z[y][x];
			}
		}
	}
	
	public void swapFields(int a, int b) {
		if ((fields.length <= a) || (fields.length <= b)) {
			AULib.reportError("AUMultiField", "swapFields", "either to or from is larger than the number of fields available", "");
			return;
		}
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				float old_a = fields[a].z[y][x];
				fields[a].z[y][x] = fields[b].z[y][x];
				fields[b].z[y][x] = old_a;
			}
		}
	}
	
	public void copySeveralFields(int from, int to, int n) {
		if (to > from) {
			for (int i=n-1; i>=0; i--) {
				copyFieldToField(from+i, to+i);
			}
		} else {
			for (int i=0; i<n; i++) {
				copyFieldToField(from+i, to+i);
			}
		}
	}

	public void swapSeveralFields(int a, int b, int n) {
		for (int i=0; i<n; i++) {
			swapFields(a+i, b+i);
		}
	}
	
	/**********************
	 * Set range
	 *********************/

	public void setRangeTogether(float _zmin, float _zmax, int _numFields) {
		int numFields = Math.min(fields.length, _numFields);
		float fmin = fields[0].z[0][0];
		float fmax = fmin;
		for (int f=0; f<numFields; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					float zv = fields[f].z[y][x];
					fmin = Math.min(fmin, zv);
					fmax = Math.max(fmax, zv);
				}
			}
		}
		for (int f=0; f<numFields; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					fields[f].z[y][x] = AUMisc.jmap(fields[f].z[y][x], fmin, fmax, _zmin, _zmax);
				}
			}
		}
	}

	public void setRangeSeparate(float _zmin, float _zmax, int _numFields) {
		int numFields = Math.min(fields.length, _numFields);
		float[] minVals = new float[fields.length];
		float[] maxVals = new float[fields.length];
		for (int f=0; f<numFields; f++) {
			minVals[f] = fields[f].z[0][0];
			maxVals[f] = minVals[f];
		}
		for (int f=0; f<numFields; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					float fv = fields[f].z[y][x];
					minVals[f] = Math.min(minVals[f], fv);
					maxVals[f] = Math.max(maxVals[f], fv);
				}
			}
		}

		for (int f=0; f<numFields; f++) {
			for (int y=0; y<h; y++) {
				for (int x=0; x<w; x++) {
					fields[f].z[y][x] = AUMisc.jmap(fields[f].z[y][x], minVals[f], maxVals[f], _zmin, _zmax);
				}
			}
		}
	}
	
	public void setRangeTogether(float _zmin, float _zmax) {
		setRangeTogether(_zmin, _zmax, fields.length);
	}
	
	public void setRangeSeparate(float _zmin, float _zmax) {
		setRangeSeparate(_zmin, _zmax, fields.length);
	}

	public void normalizeTogether(int _numFields) {
		setRangeTogether(0, 1, _numFields);
	}

	public void normalizeSeparate(int _numFields) {
		setRangeSeparate(0, 1, _numFields);
	}
	
	public void normalizeTogether() {
		setRangeTogether(0, 1, fields.length);
	}

	public void normalizeSeparate() {
		setRangeSeparate(0, 1, fields.length);
	}
	
	public void normalizeRGBTogether() {
		setRangeTogether(0, 255, 3);
	}

	public void normalizeRGBSeparate() {
		setRangeSeparate(0, 255, 3);
	}
	
	public void normalizeRGBATogether() {
		setRangeTogether(0, 255, 4);
	}

	public void normalizeRGBASeparate() {
		setRangeSeparate(0, 255, 4);
	}
	
	/**********************
	 * Set triple and quad
	 *********************/
	
	public void setTriple(int _x, int _y, float _v0, float _v1, float _v2) {
		if ((_x < 0) || (_x >= w) || (_y < 0) || (_y >= h)) {
			AULib.reportError("AUMultiField", "setTriple", "the point x,y is beyond the field's bounds", "x="+Float.toString(_x)+" y="+Float.toString(_y));
		}
		if (fields.length < 3) {
			AULib.reportError("AUMultiField", "setTriple", "there are not at least 3 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[0].z[_y][_x] = _v0;
		fields[1].z[_y][_x] = _v1;
		fields[2].z[_y][_x] = _v2;
	}
	
	public void setQuad(int _x, int _y, float _v0, float _v1, float _v2, float _v3) {
		if ((_x < 0) || (_x >= w) || (_y < 0) || (_y >= h)) {
			AULib.reportError("AUMultiField", "setQuad", "the point x,y is beyond the field's bounds", "x="+Float.toString(_x)+" y="+Float.toString(_y));
			return;
		}
		if (fields.length < 4) {
			AULib.reportError("AUMultiField", "setQuad", "there are not at least 4 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		fields[0].z[_y][_x] = _v0;
		fields[1].z[_y][_x] = _v1;
		fields[2].z[_y][_x] = _v2;
		fields[3].z[_y][_x] = _v3;
	}
		
	/**********************
	 * Set/Get a color
	 *********************/
	
	public void RGBAsetColor(int _x, int _y, int _c) {
		if (fields.length < 4) {
			AULib.reportError("AUMultiField", "RGBAsetColor", "there are not at least 4 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		int ired = (_c >> 16) & 0xFF;
		int igrn = (_c >> 8) & 0xFF;
		int iblu = (_c) & 0xFF;
		int ialpha = (_c >> 24) & 0xFF;
		setQuad(_x, _y, ired, igrn, iblu, ialpha);
		//setQuad(_x, _y, AUMisc.jred(_c), AUMisc.jgreen(_c), AUMisc.jblue(_c), AUMisc.jalpha(_c));
	}
	
	public void RGBsetColor(int _x, int _y, int _c) {
		if (fields.length < 3) {
			AULib.reportError("AUMultiField", "setColor", "there are not at least 3 fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		int ired = (_c >> 16) & 0xFF;
		int igrn = (_c >> 8) & 0xFF;
		int iblu = (_c) & 0xFF;
		setTriple(_x, _y, ired, igrn, iblu);
		//setTriple(_x, _y, AUMisc.jred(_c), AUMisc.jgreen(_c), AUMisc.jblue(_c));
	}
	
	int getColor(int _x, int _y, boolean _getAlpha) {
		int fieldsNeeded = 3;
		if (_getAlpha) fieldsNeeded = 4;
		if (fields.length < fieldsNeeded) {
			AULib.reportError("AUMultiField", "setColor", "there are not at least "+fieldsNeeded+" fields available", "fields.length="+Float.toString(fields.length));
			return 0;
		}
		int red = Math.round(fields[0].z[_y][_x]);
		int grn = Math.round(fields[1].z[_y][_x]);
		int blu = Math.round(fields[2].z[_y][_x]);
		int alf = 255;
		if (_getAlpha) alf = (int)(fields[3].z[_y][_x]);
		//int clr = AUMisc.jcolor(red, grn, blu, alf);
		int clr = ((alf&0xFF)<<24) | ((red&0xFF)<<16) | ((grn&0xFF)<<8) | (blu&0xFF);
		return clr;
	}
	
	public int getRGBAColor(int _x, int _y) {
		return getColor(_x, _y, true);
	}

	public int getRGBColor(int _x, int _y) {
		return getColor(_x, _y, false);
	}

	
	/**********************
	 * over
	 *********************/
	
	// This is very much like writeToPixels, but for efficiency I'm repeating it here for compositions between
	// AUMultiFields, rather than have to have tests at every pixel
	
	public void over(AUMultiField B) {
		over(B, null);
	}

	public void over(AUMultiField B, AUField _mask) {
		if (fields.length < 3) {
			AULib.reportError("AUMultiField", "over", "The source has only "+fields.length+" layers, and needs at least 3", "");
			return;
		}
		if (B.fields.length < 3) {
			AULib.reportError("AUMultiField", "over", "The destination has only "+B.fields.length+" layers, and needs at least 3", "");
			return;
		}
		if ((B.w != w) || (B.h != h)) {
			AULib.reportError("AUMultiField", "over", "the two fields do not have the same size", "");
			return;
		}
		AUField AalphaField = null;
		if (_mask != null) {
			AalphaField= _mask;
		} else if (fields.length > 3) {
			AalphaField = fields[3];
		}
		
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {

				float Ared = fields[0].z[y][x];
				float Agrn = fields[1].z[y][x];
				float Ablu = fields[2].z[y][x];
				float Aalf = 1.f;
				if (AalphaField != null) Aalf = AalphaField.z[y][x]/255.f;

				float Bred = B.fields[0].z[y][x];
				float Bgrn = B.fields[1].z[y][x];
				float Bblu = B.fields[2].z[y][x];
				float Balf = 1.f;
				if (B.fields.length > 3) Balf = B.fields[3].z[y][x]/255.f;
				
				float b2 = (1.f-Aalf) * Balf;
				float newAlf = Aalf + b2;
				
				if (newAlf == 0) {
					B.fields[0].z[y][x] = 0;
					B.fields[1].z[y][x] = 0;
					B.fields[2].z[y][x] = 0;
					if (B.fields.length > 3) B.fields[3].z[y][x] = 0;
				} else {
					B.fields[0].z[y][x] = ((Aalf * Ared) + (b2 * Bred))/newAlf;
					B.fields[1].z[y][x] = ((Aalf * Agrn) + (b2 * Bgrn))/newAlf;
					B.fields[2].z[y][x] = ((Aalf * Ablu) + (b2 * Bblu))/newAlf;
					if (B.fields.length > 3) B.fields[3].z[y][x] = newAlf * 255.f;
				}
			}
		}
	}
	
	/**********************
	 * write to Pixels
	 *********************/
	
	void writeToPixels(int _dx, int _dy, AUField _mask, boolean _useLayer3AsMask, int _mx, int _my, PGraphics _pg) {
		if ((_mask != null) && (_useLayer3AsMask)) {
			AULib.reportError("AUMultiField", "writeToPixels", "you supplied a mask AND said to use layer 3 as alpha. Ignoring layer 3 and using the mask.", "");
			_useLayer3AsMask = false;
		}
		int fieldsNeeded = 3;
		if (_useLayer3AsMask) fieldsNeeded = 4;
		if (fields.length < fieldsNeeded) {
			AULib.reportError("AUMultiField", "writeToPixels", "there are not at least "+fieldsNeeded+" fields available", "fields.length="+Float.toString(fields.length));
			return;
		}
		float h = fields[0].h;
		float w = fields[0].w;
		int[] thesePixels;
		AUField thisMask = null;
		if (_mask != null) thisMask = _mask;
		else if (_useLayer3AsMask) thisMask = fields[3];
		int wid = 0;
		int hgt = 0;
		if (_pg != null) {
			wid = _pg.width;
			hgt = _pg.height;
			_pg.loadPixels();
			thesePixels = _pg.pixels;
		} else {
			wid = theSketch.width;
			hgt = theSketch.height;
			theSketch.loadPixels();
			thesePixels = theSketch.pixels;
		}
		for (int y=0; y<h; y++) {
			int py = y+_dy;
			if ((py < 0) || (py >= hgt)) continue;
			for (int x=0; x<w; x++) {
				int px = x+_dx;
				if ((px < 0) || (px >= wid)) continue;
				int index = (py*wid)+px;
				int pixelColor = thesePixels[index];
				
				float fieldRed = fields[0].z[py][px];
				float fieldGreen = fields[1].z[py][px];
				float fieldBlue = fields[2].z[py][px];
				float fieldAlpha = 1;

				if (thisMask != null) {
					int maskX = x + _mx;
					int maskY = y + _my;
					if ((maskX >= 0) && (maskX < thisMask.w) && (maskY >= 0) && (maskY < thisMask.h)) {
						fieldAlpha = thisMask.z[maskY][maskX] / 255.f;
					}
				}
				
				float pixelRed = (pixelColor >> 16) & 0xFF; 
				float pixelGreen = (pixelColor >> 8) & 0xFF; 
				float pixelBlue = pixelColor & 0xFF; 
				float pixelAlpha = ((pixelColor >> 24) & 0xFF) / 255.f; 
				
				float blendF = fieldAlpha;
				float blendP = 1.f;
				if (_pg != null) {   // the screen is always opaque, but PGraphics might not be
					blendF = fieldAlpha;
					blendP = pixelAlpha; // * (1.f - fieldAlpha); <- Alvy's scale factor not needed any more
				}
				
				float blendRed = 0;
				float blendGreen = 0;
				float blendBlue = 0;
				
				// use my blending algorithm to support when blendP != 1
				float blendAlpha = blendP + ((1.f - blendP) * blendF);
				if (blendAlpha != 0) {
					float kappa = 1.f - blendF;
					float k2 = 1.f - kappa;
					blendRed   = ((fieldRed * k2) + (pixelRed * kappa))/blendAlpha;
					blendGreen = ((fieldGreen * k2) + (pixelGreen * kappa))/blendAlpha;
					blendBlue  = ((fieldBlue * k2) + (pixelBlue * kappa))/blendAlpha;
				}
				int ired = Math.round(blendRed);
				int igrn = Math.round(blendGreen);
				int iblu = Math.round(blendBlue);
				int ialf = Math.round(255.f * blendAlpha); 
				
				int newColor = ((ialf & 0xFF) << 24) | ((ired & 0xFF) << 16) | ((igrn & 0xFF) << 8) | (iblu & 0xFF);
								
				thesePixels[index] = newColor;
			}
		}
		if (_pg != null) {
			_pg.updatePixels();
		} else {
			theSketch.updatePixels();
		}
	}
	
	
	// There are 8 versions, but we don't include RGBA when a mask is present. I think this
	// is easier than exposing writeToPixels and asking people to think through the choices every time.
	
	
	public void RGBtoPixels(float _dx, float _dy) {  // copy opaque RGB values into pixels
		writeToPixels(Math.round(_dx), Math.round(_dy), null, false, Math.round(_dx), Math.round(_dy), null);
	}
	
	public void RGBAtoPixels(float _dx, float _dy) {  // copy RGBA values into pixels
		writeToPixels(Math.round(_dx), Math.round(_dy), null, true, Math.round(_dx), Math.round(_dy), null);
	}
	
	public void RGBtoPixels(float _dx, float _dy, AUField _mask) {  // use mask to mix in RGB
		writeToPixels(Math.round(_dx), Math.round(_dy), _mask, false, Math.round(_dx), Math.round(_dy), null);
	}
	
	public void RGBtoPixels(float _dx, float _dy, AUField _mask, float _mx, float _my) {  // use mask to mix in RGB
		writeToPixels(Math.round(_dx), Math.round(_dy), _mask, false, Math.round(_mx), Math.round(_my), null);
	}
	

	
	public void RGBtoPixels(float _dx, float _dy, PGraphics _pg) {  // copy opaque RGB values into pixels
		writeToPixels(Math.round(_dx), Math.round(_dy), null, false, Math.round(_dx), Math.round(_dy), _pg);
	}
	
	public void RGBAtoPixels(float _dx, float _dy, PGraphics _pg) {  // copy RGBA values into pixels
		writeToPixels(Math.round(_dx), Math.round(_dy), null, true, Math.round(_dx), Math.round(_dy), _pg);
	}
	
	public void RGBtoPixels(float _dx, float _dy, AUField _mask, PGraphics _pg) {  // use mask to mix in RGB
		writeToPixels(Math.round(_dx), Math.round(_dy), _mask, false, Math.round(_dx), Math.round(_dy), _pg);
	}
	
	public void RGBtoPixels(float _dx, float _dy, AUField _mask, float _mx, float _my, PGraphics _pg) {  // use mask to mix in RGB
		writeToPixels(Math.round(_dx), Math.round(_dy), _mask, false, Math.round(_mx), Math.round(_my), _pg);
	}
}



  