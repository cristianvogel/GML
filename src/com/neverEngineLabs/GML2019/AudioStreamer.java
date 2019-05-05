/**
 * Class to handle threaded audio playback
 * basic timer implementation offsets start time
 */

package com.neverEngineLabs.GML2019;

import javafx.scene.media.AudioClip;

import java.util.Timer;
import java.util.TimerTask;

import static processing.core.PApplet.map;
import static processing.core.PApplet.println;




public class AudioStreamer extends Thread  {


    IStreamNotify streamNotify;
    private int _id;
    private int _priority;
    private String _url;
    private String _token = "";
    private double _volume;
    private AudioClip _soundClip;


    //Timed Event
    private TimerTask _callback;
    private Timer _timer;


    public AudioStreamer( GML_SonifiedHaikus main, String url, float offset) {

        this.setName(url);
        streamNotify = main;
        _url = url;
        _volume = 2;
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
        _timer.schedule(_callback, (long) (offset+0.5) * 500); //compensate for buffering by halving requested delay
    }

    public AudioStreamer(GML_SonifiedHaikus main, String url, float offset, int priority) {
       this(main, url,offset);
       _priority = priority;
    }


    public AudioStreamer(GML_SonifiedHaikus main, String url, float offset, int priority, String token) {
        this(main, url, offset,priority);
        _token = token;
    }

    public AudioStreamer(GML_SonifiedHaikus main, String url, float offset, int priority, String searchString, int id) {
        this(main, url, offset, priority, searchString);
                _id = id;
        this.setName("AudioThread"+_id);
    }


    public void play() {

            println("Starting audio for \'" + _token + "\' from " + _url + " in thread "+_id);

        streamNotify.playbackStart(_url, _token);

            //     idea to spatialise ... not really working
            //     _soundClip.play(_volume, 0 , 1, map(_priority,0,15,-1,1), _priority );

            _soundClip.play(_volume, 0, 1, map(_priority,0,15,0,1), _priority);

        while (_soundClip.isPlaying())
        { streamNotify.playbackStatus(_token+" playing");}
        streamNotify.playbackStatus(_token+" stopped");
        }

}


