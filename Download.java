import java.awt.image.ImageObserver;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;
//this class downloads a file from the url
class Download extends Observable implements Runnable {
    //Max size of download buffer
    private static final int MAX_BUFFER_SIZE=1024;
    public static final String [] STATUSES={"Downloading", "Paused",
    "Complete", "Cancelled","Error"};
    //These are the status code
    public static final int DOWNLOADING=0;
    public static final int PAUSED=1;
    public static final int COMPLETE=2;
    public static final int CANCELLED=3;
    public static final int ERROR=4;
    private URL url;//download url
    private int size;//size of download
    private int downloaded;//number of byte downloaded
    private int status;//current status of download
    //constructor for download
    public Download(URL url){
        this.url=url;
        size=-1;
        downloaded=0;
        status=DOWNLOADING;
        //begin the download
        download();
    }
    //get this download's url
    public String getUrl(){
        return url.toString();
    }
    //get this download's size
    public int getSize(){
        return size/1000;
    }
    //get this download's progress
    public float getProgress(){
        return ((float)downloaded/size)*100;
    }
    //get this download's status
    public int getStatus(){
        return status;
    }
    //pause this download
    public void pause(){
        status=PAUSED;
        stateChanged();
    }
    //resume this download
    public void resume(){
        status=DOWNLOADING;
        stateChanged();
        download();
    }
    //cancel this download
    public void cancel(){
        status=CANCELLED;
        stateChanged();
    }
    //mark this download as having an error
    private void error(){
        status=ERROR;
        stateChanged();
    }
    //start or resume download
    public void download(){
        Thread thread=new Thread(this);
        thread.start();
    }
    //get file name portion of url
    private String getFileName(URL url){
        String fileName=url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') +1);
    }
    //download file
    public void run(){
        RandomAccessFile file=null;
        InputStream stream=null;
        try{
            //open connection to url
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            //specify what portion of file to download
            connection.setRequestProperty("Range","bytes=" +downloaded + "-");
            //connect to server
            connection.connect();
            //make sure response code is in the 200 range
            if (connection.getResponseCode()/100 !=2){
                error();
            }
            //check for valid content length
            int contentLength=connection.getContentLength();
            if(contentLength<1){
                error();
            }
            //set the size of this download if it hasn't been set yet
            if (size==-1){
                size=contentLength;
                stateChanged();
            }
            //open file and seek to the end of it
            file=new RandomAccessFile(getFileName(url),"rw");
            file.seek(downloaded);
            stream=connection.getInputStream();
            while (status==DOWNLOADING){
                //size buffer according to how much of the file is left to download
                byte buffer[];
                if (size-downloaded>MAX_BUFFER_SIZE){
                    buffer=new byte[MAX_BUFFER_SIZE];
                }
                else {
                    buffer=new byte[size-downloaded];
                }
                //read from server into buffer
                int read=stream.read(buffer);
                if (read==-1){
                    break;
                }
                //write buffer to file
                file.write(buffer,0,read);
                downloaded+=read;
                stateChanged();
            }
            //change status to complete if this point was reached because downloading has finished
            if (status==DOWNLOADING){
                status=COMPLETE;
                stateChanged();
            }
        }
        catch (Exception e){
            error();
        }
        finally {
            //close file
            if (file!=null){
                try {
                    file.close();
                }
                catch (Exception e){
                    //catch phrase above
                }
            }
        }
        //close connection to server
        if(stream!=null){
            try {
                stream.close();
            }
            catch (Exception e){
                //catch phrase above
            }
        }
    }
    //notify observers that this download's status has changed
    private void stateChanged(){
        setChanged();
        notifyObservers();
    }
}
