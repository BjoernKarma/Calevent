/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.android.calevent.stub;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public class DirectoryEntry {
    private String name;
    private String location;
    private String city;
    private String address;
    private String contact;
    private int hour;
    private int minute;

    
    private int resID;

    public DirectoryEntry(String name, int resID) {
        this.name = name;
        this.resID = resID;
    }
    
    public DirectoryEntry(String name, String location, String city, String address, String contact, int hour, int minute, int resID) {
        this.name = name;
        this.location = location;
        this.city = city;
        this.address = address;
        this.contact = contact;
        this.hour = hour;
        this.minute = minute;
        this.resID = resID;
    }

    public String getName() {
        return name;
    }

    public Drawable getDrawable(Resources res) {
        return res.getDrawable(resID);
    }

    public Bitmap getBitmap(Resources res) {
        return BitmapFactory.decodeResource(res, resID);
    }

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}
}
