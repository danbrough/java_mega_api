/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.logback;

import ch.qos.logback.classic.PatternLayout;

public class ColourConsoleAppender<E> extends
    ch.qos.logback.core.ConsoleAppender<E> {

  static {
    PatternLayout.defaultConverterMap.put("highlight2",
        ColourConverter.class.getName());
  }
}
