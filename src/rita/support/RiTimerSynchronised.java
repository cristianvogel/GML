package rita.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.neverEngineLabs.GML2019.AudioStreamer;
import rita.RiTa;
import rita.RiTaEvent;

/**
 *
 * CristianVogel:
 * Added multithread synchronisation locks
 * https://www.baeldung.com/java-atomic-variables
 *
 * A basic timer implementation to which one can pass
 * a PApplet, a RiTaEventListener, or any other object
 * that implements the method: onRiTaEvent(RiTaEvent re)<p>
 * A typical use in Processing might be:<pre>
 void setup(RiTaEvent re)
 {
 new RiTimerSynchronised(this, 1.0);

 OR

 RiTimerSynchronised.start(this, 1.0);

 DO NOT USE as this instantiates a non-locked Timer
 RiTa.timer(this, 1.0);


 }

 void onRiTaEvent(RiTaEvent re)
 {
 // called every 1 second
 }

 </pre>
 or, if (outside of Processing) and the callback (myEventFunc(re)) was in another class (e.g., MyApplet):<pre>
 public class MyApplet extends Applet
 {
 RiTimerSynchronised timer;

 public void init()
 {
 timer = new RiTimerSynchronised(this, 1.0, "eventHandler");
 }

 void myEventFunc(RiTaEvent re)
 {
 // called every 1 second
 }
 } </pre>
 * @author dhowe
 */
public class RiTimerSynchronised implements Constants
{
    protected volatile static List<RiTimerSynchronised> timers = new ArrayList<RiTimerSynchronised>();

    protected volatile static int idGen = 0;

    protected volatile Timer internalTimer;
    protected volatile boolean paused, isDaemon;
    protected volatile float period;
    protected volatile Object parent;
    protected volatile Method callback;
    private volatile int  id;


// don't use this constructor
    /**
    public  RiTimerSynchronised(Object parent, float period) {

        this(parent, period, null);
    }
**/

    public  RiTimerSynchronised(Object parent, float period, String callbackName) {

        this.parent = parent;
        this.period = period;
        this.id = ++idGen;
        this.callback = RiTa._findCallback(parent, callbackName);
        init(0, period);
        timers.add(this);
    }

    public synchronized String toString()
    {
        return "RiTimerSynchronised#"+id;
    }

    private synchronized void init(float startOffset, float thePeriod)
    {
        final RiTimerSynchronised rt = this;
        (internalTimer = new Timer(isDaemon)).schedule(new TimerTask() {
            public void run()
            {
                new RiTaEvent(rt, EventType.Timer, id).fire(parent, callback);
            }
        }, (long) (Math.max(0, startOffset * 1000)), (long) (thePeriod * 1000));
    }

    public synchronized void stop() {

        internalTimer.cancel();
    }

    public synchronized RiTimerSynchronised pause(boolean b) {

        if (b) {
            internalTimer.cancel();
        }
        else {
            init(0, period);
        }
        return this;
    }

    public synchronized void pauseFor(float seconds) {

        internalTimer.cancel();
        init(seconds, period);
    }

    public synchronized int id()
    {
        return id;
    }

    public synchronized static RiTimerSynchronised findById(int id)
    {
        for (Iterator<RiTimerSynchronised>it = timers.iterator(); it.hasNext();)
        {
            RiTimerSynchronised rt = it.next();
            if (rt.id() == id)
                return rt;
        }
        return null;
    }

    public synchronized static void main(String[] args) throws InterruptedException
    {
        RiTimer rt = new RiTimer(new Object() {
            public void onRiTaEvent(RiTaEvent rte) {
                System.out.println(rte.source()+" :: "+System.currentTimeMillis()+ " :: "+rte.data);
            }
        }, 1);

        System.out.println(rt.id());
        RiTimer rt2 = new RiTimer(new Object() {
            public void dynFun() {
                System.out.println(System.currentTimeMillis());
            }
        }, 1, "dynFun");
        System.out.println(rt2);

    }

}// end
