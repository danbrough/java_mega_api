/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.mega;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AndroidMegaAPI extends MegaAPI {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(AndroidMegaAPI.class.getSimpleName());
  
  private Context appContext;
  
  public AndroidMegaAPI(Context appContext) {
    super();
    this.appContext = appContext;
  }

  public void test(){
//    PreferenceManager.getDefaultSharedPreferences(appContext).
  }
}
