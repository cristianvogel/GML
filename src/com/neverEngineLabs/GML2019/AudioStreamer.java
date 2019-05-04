/**
 * Class to handle threaded audio playback
 * basic timer implementation offsets start time
 */

package com.neverEngineLabs.GML2019;

import javafx.scene.media.AudioClip;

import java.util.Timer;
import java.util.TimerTask;
import static processing.core.PApplet.println;


public class AudioStreamer extends Thread {




    private int _priority;
    private String _url;
    private String _token = "";
    private double _volume;
    private AudioClip _soundClip;


    //Timed Event
    private TimerTask _callback;
    private Timer _timer;


    public AudioStreamer(String url, float offset) {

        this.setName(url);
        _url = url;
        _volume = 0.9;
        _soundClip = new AudioClip(_url);
        _priority = 1;
        _timer = new Timer();
        _callback = new TimerTask() {
            @Override
            public void run() {
                play();
                cancel();
            }
        };
        _timer = new Timer ();
        _timer.schedule(_callback, (long) (offset+0.5) * 1000);
    }

    public AudioStreamer(String url, float offset, int priority) {
        this.setName(url);
        _url = url;
        _volume = 0.9;
        _soundClip = new AudioClip(_url);
        _priority = priority;
        _timer = new Timer();

        _callback = new TimerTask() {
            @Override
            public void run() {
                play();
                cancel();
            }
        };
        _timer = new Timer ();
        _timer.schedule(_callback, (long) (offset+0.5) * 1000);
    }

    public AudioStreamer(String url, float offset, int priority, String token) {

        this.setName(url);
        _token = token;
        _url = url;
        _volume = 0.9;
        _soundClip = new AudioClip(_url);
        _priority = priority;
        _callback = new TimerTask() {
            @Override
            public void run() {
                play();
                cancel();
            }
        };
        _timer = new Timer( _token);
        _timer.schedule(_callback, (long) (offset+0.5) * 1000);

    }


    public void play() {


            println("Starting audio for \'" + _token + "\' from " + _url);



            //     idea to spatialise ... not really working
            //     _soundClip.play(_volume, 0 , 1, map(_priority,0,15,-1,1), _priority );

            _soundClip.play(_volume, 0, 1, 0.5, _priority);

        }

}


