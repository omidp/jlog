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
package com.jlog.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

/**
 * @author Omid 
 *
 */
public class LocalLogReader implements LogReader
{

    @Override
    public void readLogFiles(String directory, JLogFileReader lr, JLogFileReadComplete rc) 
    {
        File srcDir = new File(directory);
        if(srcDir.exists() == false)
            throw new LogReaderException(directory + " does not exists");
        try
        {
            Collection<File> listFiles = FileUtils.listFiles(srcDir, new String[] {"log"}, true);
            for (Iterator iterator = listFiles.iterator(); iterator.hasNext();)
            {
                File file = (File) iterator.next();
                try(FileInputStream fis = new FileInputStream(file))
                {
                    JLogFile.readLines(fis, lr);                
                }
                rc.afterRead(file.getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            throw new LogReaderException(e);
        }
    }

    @Override
    public void open()
    {
        
    }

}
