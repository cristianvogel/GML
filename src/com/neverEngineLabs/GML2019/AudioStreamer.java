/**
 * Class to handle threaded audio playback
 * basic timer implementation offsets start time
 */

package com.neverEngineLabs.GML2019;

import javafx.scene.media.AudioClip;
import rita.support.RiTimer;

import java.util.concurrent.ThreadLocalRandom;

import static processing.core.PApplet.map;
import static processing.core.PApplet.println;
import static rita.RiTa.random;


public class AudioStreamer extends Thread {


    private int _priority;
    private String _url;
        private double _volume;
        private AudioClip soundClip;


        //Timed Event

          private boolean _offsetTimerComplete;
          private float _startTime;
          private  RiTimer _offsetTimer;
          private ThreadLocalRandom _random;

    AudioStreamer (String url, float wait) {

            this.setName(url);
            _offsetTimerComplete = false;
            _url = url;
            _volume = 0.75;
            _startTime = wait;
            soundClip = new AudioClip(_url);
            _priority = 1;

            if(wait != 0) {
                _offsetTimer = new RiTimer(this, _startTime, "timerDone");
                _offsetTimer.pauseFor(0.1f);
            } else _offsetTimerComplete = true;
        }

    AudioStreamer (String url, float wait, int priority) {
        this.setName(url);
        _offsetTimerComplete = false;
        _url = url;
        _volume = 0.75;
        _startTime = wait;
        soundClip = new AudioClip(_url);
        _priority = priority;

        if(wait != 0) {
            _offsetTimer = new RiTimer(this, _startTime, "timerDone");
            //println(_offsetTimer.id()+" instantiatied");
        } else _offsetTimerComplete = true;
    }

        public void run()
        {

           if (_offsetTimerComplete && !soundClip.isPlaying()) {
                println("Wait complete, starting playback from "+_url);
                soundClip.play(_volume, 0 , 1, map(_priority,0,15,-1,1), _priority );
            }
        }


    /**
     * Callback method for RiTimer
     */
    private void timerDone () {

            _offsetTimerComplete = true;
            _offsetTimer.stop();
        }


        public void setVolume (double volume)
        {
            _volume = volume;
            soundClip.setVolume(volume);
        }

        public boolean getStatus() {
            return soundClip.isPlaying();
        }


}

