/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by getApplication()licable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
 
/**
 * @author Andres Almiray
 */ 
 
import org.javalite.instrumentation.Instrumentation

target(activejdbcInstrument: "Instrument source code with Activejdbc") {
    Instrumentation instrumentation = new Instrumentation()
    instrumentation.outputDirectory = projectMainClassesDir
    addUrlIfNotPresent rootLoader, projectMainClassesDir
    addUrlIfNotPresent Instrumentation.class.classLoader, projectMainClassesDir
    instrumentation.instrument()
}

setDefaultTarget(activejdbcInstrument)
