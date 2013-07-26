/*******************************************************************************
 * Copyright (c) 2013 Dan Brough dan@danbrough.org. All rights reserved. 
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 * 
 ******************************************************************************/
package org.danbrough.logback;

import static ch.qos.logback.core.pattern.color.ANSIConstants.BOLD;
import static ch.qos.logback.core.pattern.color.ANSIConstants.DEFAULT_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.GREEN_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.RED_FG;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class ColourConverter extends
    ForegroundCompositeConverterBase<ILoggingEvent> {
  @Override
  protected String getForegroundColorCode(ILoggingEvent event) {
    Level level = event.getLevel();
    switch (level.toInt()) {
    case Level.ERROR_INT:
      return BOLD + RED_FG;
    case Level.WARN_INT:
      return BOLD + ANSIConstants.YELLOW_FG;
    case Level.INFO_INT:
      return GREEN_FG;
    case Level.DEBUG_INT:
      return BOLD + ANSIConstants.CYAN_FG;
    case Level.TRACE_INT:
      return ANSIConstants.CYAN_FG;
    default:
      return DEFAULT_FG;
    }
  }
}
