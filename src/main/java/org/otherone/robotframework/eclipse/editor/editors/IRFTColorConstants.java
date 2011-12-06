/**
 * Copyright 2011 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.otherone.robotframework.eclipse.editor.editors;

import org.eclipse.swt.graphics.RGB;

public interface IRFTColorConstants {
  RGB COMMENT = new RGB(128, 128, 128);
  RGB TABLE = new RGB(192, 0, 192);
  RGB SETTING = new RGB(0, 192, 0);
  RGB SETTING_VALUE = new RGB(0, 255, 0);
  RGB SETTING_FILE = new RGB(255, 255, 255);
  RGB SETTING_FILE_ARG = new RGB(0, 255, 64);
  RGB VARIABLE = new RGB(0, 170, 180);
  RGB VARIABLE_VALUE = new RGB(180, 192, 202);
  //RGB TESTCASE = new RGB(222, 0, 0);
  RGB KEYWORD_NEW = new RGB(255, 255, 50);
  RGB KEYWORD = new RGB(255, 180, 0);
  RGB KEYWORD_ARG = new RGB(255, 100, 0);
  //RGB ACTION = new RGB(0, 100, 255);
  RGB DEFAULT = new RGB(192, 192, 192);
  RGB UNKNOWN = new RGB(255, 140, 0);
  RGB FIELD_BG = new RGB(8, 16, 24);

  RGB FG = new RGB(210, 210, 210);
  RGB BG = new RGB(16, 32, 48);
  RGB FG_SELECTION = new RGB(255, 255, 255);
  RGB BG_SELECTION = new RGB(56, 83, 104);

  RGB ARGUMENT_SEPARATOR = BG; // new RGB(32, 64, 96);
}
