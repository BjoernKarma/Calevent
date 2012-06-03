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

import org.android.calevent.frontend.R;

public class Directory {
    private static DirectoryCategory[] mCategories;

    public static void initializeDirectory() {
      mCategories = new DirectoryCategory[] {
                new DirectoryCategory("List", new DirectoryEntry[] {
                        new DirectoryEntry("Event 1", R.drawable.red_balloon),
                        new DirectoryEntry("Event 2", R.drawable.green_balloon),
                        new DirectoryEntry("Event 3", R.drawable.blue_balloon)}),
                new DirectoryCategory("Map", new DirectoryEntry[] {
                        new DirectoryEntry("Event 4", R.drawable.blue_bike),
                        new DirectoryEntry("Event 5", R.drawable.rainbow_bike),
                        new DirectoryEntry("Event 6", R.drawable.chrome_wheel)}),
               };

    }

    public static int getCategoryCount() {
        return mCategories.length;
    }

    public static DirectoryCategory getCategory(int i) {
        return mCategories[i];
    }
}
