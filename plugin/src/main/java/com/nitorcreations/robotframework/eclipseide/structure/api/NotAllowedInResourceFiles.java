/**
 * Copyright 2012 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.structure.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to tag methods whose return value should always be null for
 * resource files. I.e. the resource file is syntactically broken if the
 * annotated method returns a non-null value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotAllowedInResourceFiles {
    /**
     * @return since which Robot Framework version is it not allowed? (i.e. it
     *         was allowed before this version)
     */
    String value() default "";
}