/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jlog.grok;

import java.util.Map;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.Match;


/**
 * @author Omid 
 *
 */
public class GrokCompiler
{

    
    public static final String CAUSED_PATTERN = "%{JAVACAUSED:msg}";
    public static final String STACKTRACE_PATTERN = "%{JAVASTACKTRACEPART:msg}";
    
    
    private static class GorkCompilerLoader
    {
        private static final GrokCompiler INSTANCE = new GrokCompiler();
    }

    private GrokCompiler()
    {
    }
    
    public static GrokCompiler getInstance()
    {
        return GorkCompilerLoader.INSTANCE;
    }
    
    

    public io.krakens.grok.api.GrokCompiler newInstance()
    {
        io.krakens.grok.api.GrokCompiler grokCompiler = io.krakens.grok.api.GrokCompiler.newInstance();
        grokCompiler.register("JAVACLASS", "(?:[a-zA-Z$_][a-zA-Z$_0-9]*\\.)*[a-zA-Z$_][a-zA-Z$_0-9]*");
        grokCompiler.register("JAVAFILE", "(?:[A-Za-z0-9_. -]+)");
        grokCompiler.register("JAVAMETHOD", "(?:(<(?:cl)?init>)|[a-zA-Z$_][a-zA-Z$_0-9]*)");
        grokCompiler.register("JAVASTACKTRACEPART",
                "%{JAVACLASS:class}\\.%{JAVAMETHOD:method}\\(%{JAVAFILE:file}(?::%{NUMBER:line})?\\)");
        grokCompiler.register("JAVALOGMESSAGE", "(.*)");
        grokCompiler.register("JAVACAUSED", "Caused by: (.*)");
        grokCompiler.registerDefaultPatterns();
        return grokCompiler;
    }

    public void parseJavaException(String pattern, String logLine, AnalyzeLog al)
    {
        Grok grok = newInstance().compile(pattern);
        Match gm = grok.match(logLine);
        final Map<String, Object> capture = gm.capture();
        LogValueHolder vo = new LogValueHolder("" + capture.get("line"), "" + capture.get("class"), "" + capture.get("method"),
                "" + capture.get("file"), "" + capture.get("msg"), "", "", "");
        if (vo.getMessage() != null && !"null".equals(vo.getMessage()))
            al.process(vo);
    }

}
