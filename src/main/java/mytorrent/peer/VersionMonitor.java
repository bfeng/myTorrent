/*
 * The MIT License
 *
 * Copyright 2012 ASUS.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mytorrent.peer;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.FileObject;
/**
 *
 * @author swang
 */
public class FileMonitor extends Thread {
    
    FileSystemManager fsManager;
    DefaultFileMonitor Push_fm;
    DefaultFileMonitor Pull_fm;
    
    public FileMonitor() {
        
        //VFS: need one
        try {
            fsManager = VFS.getManager();
        } catch (FileSystemException ex) {
            Logger.getLogger(FileMonitor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error create VFS.manager!");
        }
        
        //Need two monitor with different Listener for each peer
        Push_fm = new DefaultFileMonitor(new PushFileListener());
        Pull_fm = new DefaultFileMonitor(new PullFileListener());
        
    }

    @Override
    public void run()  {
        try {
            init();
        } catch (Exception ex) {
            Logger.getLogger(FileMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        System.out.println("Good Bye");
        
        
    }
    
    private void init() throws Exception{
        
        //received for pull monitor
        FileObject initfiles_push = fsManager.resolveFile(new File("received/").getAbsolutePath());
                
        Push_fm.addFile(initfiles_push);
        Push_fm.setRecursive(false);
        Push_fm.start();
                
        
        
        //shared for push monitor
        FileObject initfiles_pull = fsManager.resolveFile(new File("shared/").getAbsolutePath());
        
        Pull_fm.addFile(initfiles_pull);
        Pull_fm.setRecursive(false);
        Pull_fm.start();
        
        
        
        
    }
    
    

    private class PushFileListener implements FileListener {

        @Override
        public void fileCreated(FileChangeEvent fce) throws Exception {
            //todo
            FileObject temp = fce.getFile();
            System.out.println("PUSH WARNING: Original copy SOMEHOW created!?" + temp.getName().getBaseName());
        }

        @Override
        public void fileDeleted(FileChangeEvent fce) throws Exception {
            //todo
            FileObject temp = fce.getFile();
            System.out.println("PUSH WARNING: Original copy shall not be deleted!! It is causing error!"+ temp.getName().getBaseName());
            
        }

        @Override
        public void fileChanged(FileChangeEvent fce) throws Exception {
            //todo
            FileObject temp = fce.getFile();
            System.out.println("PUSH TEST: file changed, the version will be increased."+ temp.getName().getBaseName());
        }
    }
        
    private class PullFileListener implements FileListener {

        @Override
        public void fileCreated(FileChangeEvent fce) throws Exception {
            //todo
            System.out.println("PULL TEST: Local Copy Created");
        }

        @Override
        public void fileDeleted(FileChangeEvent fce) throws Exception {
            //todo
            System.out.println("PULL TEST: Local Copy Deleted!");
        }

        @Override
        public void fileChanged(FileChangeEvent fce) throws Exception {
            //todo
            System.out.println("PULL WARNING: Local Copy shall not be modified!");
        }
    }
    
    public static void main(String[] args){
        System.out.println("Start:");
        FileMonitor fmr = new FileMonitor();
        fmr.start();
        
        
        
        File temp = new File("received/");
        for (String item : temp.list()) {
             System.out.println(item);
        }
        
        File temp2 = new File("shared/");
        for (String item : temp2.list()) {
             System.out.println(item);
        }
       
        
        while(true);
        
    }
    
    
    
    
}
