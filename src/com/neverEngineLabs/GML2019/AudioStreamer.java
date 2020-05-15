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




public class AudioStreamer extends Thread {


    private float _rate;
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


    public AudioStreamer(IStreamNotify main, String url, float offset) {

        this.setName(url);
        streamNotify = main;
        _url = url;
        _volume = 0.8;
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
        _id = Thread.activeCount() + 1;
        _timer = new Timer();
        _timer.schedule(_callback, (long) (offset + 0.5) * 500); //compensate for buffering by halving requested delay
        //  _timer.schedule(_callback, (long) (offset+0.5) );
    }

    public AudioStreamer(IStreamNotify main, String url, float offset, int priority) {
        this(main, url, offset);
        _priority = priority;
    }


    public AudioStreamer(IStreamNotify main, String url, float offset, int priority, String token) {
        this(main, url, offset, priority);
        _token = token;
    }

    public AudioStreamer(IStreamNotify main, String url, float offset, int priority, String searchString, int id) {
        this(main, url, offset, priority, searchString);
        _id = id;
        this.setName("AudioThread" + _id);
    }

    public AudioStreamer(IStreamNotify main, String url, float offset, float rate) {
        this(main, url, offset);
        _rate = rate;
    }


    private void play() {

        println("Starting audio for \'" + _token + "\' from " + _url + " in thread " + _id);

        streamNotify.playbackStart(_url, _token);
        streamNotify.console("Starting audio for \'" + _token + "\' from " + _url + " in thread " + _id);

        // can only change rate if playing from disk
        if (_url.contains("file:")) {
            _soundClip.play(_volume, 0, _rate, 0.5, _priority);
        } else {
            _soundClip.play(_volume, 0, 1, 0.5, _priority);
        }

        if (_soundClip.isPlaying()) {
            streamNotify.playbackStatus(_token + " playing");
        } else {
            streamNotify.playbackStatus(_token + " stopped");
        }

    }
}


